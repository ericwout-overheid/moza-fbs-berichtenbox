#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# FBS Demo — One-command startup script
# =============================================================================
# Gebruik: ./demo/scripts/demo.sh [start|stop|status]
#
# Start alle services nodig voor de FBS demo:
# 1. Base infrastructure via infrastructure/docker-compose.deps.yml (reuse if running)
# 2. Extra infrastructure via demo/docker-compose.yml (profiel PostgreSQL)
# 3. Profiel Service (gekloond van MinBZK/moza-profiel-service)
# 4. Berichtenmagazijn (port 8080)
# 5. Berichtenlijst (port 8081)
# 6. Mock Services (port 8095)
# 7. Simulator (port 8092)
# 8. Frontend (port 5173)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEMO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROJECT_DIR="$(cd "$DEMO_DIR/.." && pwd)"
DEPS_DIR="$DEMO_DIR/.deps"
LOG_DIR="$DEMO_DIR/.logs"
PID_DIR="$DEMO_DIR/.pids"
INFRA_COMPOSE="$PROJECT_DIR/infrastructure/docker-compose.deps.yml"
DEMO_COMPOSE="$DEMO_DIR/docker-compose.yml"

# Kleuren
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_ok()    { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }

# CORS environment variables for existing services
export QUARKUS_HTTP_CORS=true
export QUARKUS_HTTP_CORS_ORIGINS=http://localhost:5173
export QUARKUS_HTTP_CORS_METHODS=GET,POST,PATCH,DELETE,OPTIONS
export QUARKUS_HTTP_CORS_HEADERS=Content-Type,Authorization,API-Version,X-Afzender-OIN

# =============================================================================
# Prerequisites check
# =============================================================================
check_prerequisites() {
    log_info "Controleer vereisten..."
    local missing=0

    if ! command -v docker &>/dev/null; then
        log_error "Docker is niet geinstalleerd"
        missing=1
    fi

    if ! command -v java &>/dev/null; then
        # Try to find JDK 21 in ~/.jdks
        local jdk_dir
        jdk_dir=$(find ~/.jdks -maxdepth 1 -name '*21*' -type d 2>/dev/null | sort -V | tail -1)
        if [[ -n "$jdk_dir" ]]; then
            export JAVA_HOME="$jdk_dir"
            export PATH="$JAVA_HOME/bin:$PATH"
            log_info "JAVA_HOME ingesteld op $JAVA_HOME"
        else
            log_error "Java 21+ is niet gevonden. Installeer JDK 21 of zet JAVA_HOME."
            missing=1
        fi
    else
        local java_version
        java_version=$(java -version 2>&1 | head -1 | grep -oP '"\K[^"]+' | cut -d. -f1)
        if [[ "$java_version" -lt 21 ]]; then
            # Try to find a JDK 21+ anyway
            local jdk_dir
            jdk_dir=$(find ~/.jdks -maxdepth 1 -name '*21*' -type d 2>/dev/null | sort -V | tail -1)
            if [[ -n "$jdk_dir" ]]; then
                export JAVA_HOME="$jdk_dir"
                export PATH="$JAVA_HOME/bin:$PATH"
                log_info "JAVA_HOME ingesteld op $JAVA_HOME (PATH java was $java_version)"
            else
                log_error "Java 21+ vereist, gevonden: $java_version"
                missing=1
            fi
        fi
    fi

    if ! command -v node &>/dev/null; then
        log_error "Node.js is niet geinstalleerd"
        missing=1
    fi

    if [[ $missing -ne 0 ]]; then
        log_error "Niet alle vereisten zijn voldaan. Installeer de ontbrekende tools."
        exit 1
    fi

    log_ok "Alle vereisten aanwezig"
}

# =============================================================================
# Health check helpers
# =============================================================================
wait_for_health() {
    local name="$1"
    local url="$2"
    local timeout="${3:-30}"
    local elapsed=0

    while [[ $elapsed -lt $timeout ]]; do
        if curl -sf "$url" &>/dev/null; then
            log_ok "$name is beschikbaar"
            return 0
        fi
        sleep 1
        elapsed=$((elapsed + 1))
    done

    log_warn "$name niet bereikbaar op $url na ${timeout}s"
    return 1
}

wait_for_port() {
    local name="$1"
    local port="$2"
    local timeout="${3:-30}"
    local elapsed=0

    while [[ $elapsed -lt $timeout ]]; do
        if curl -sf "http://localhost:$port/q/health" &>/dev/null; then
            log_ok "$name is beschikbaar op poort $port"
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
    done

    log_warn "$name niet bereikbaar op poort $port na ${timeout}s — check logs in $LOG_DIR/"
    return 1
}

is_port_open() {
    curl -sf "http://localhost:$1" &>/dev/null || \
    curl -sf "http://localhost:$1/q/health" &>/dev/null
}

# Check specifically for a Quarkus service (uses /q/health)
is_quarkus_running() {
    curl -sf "http://localhost:$1/q/health" &>/dev/null
}

# =============================================================================
# Start infrastructure
# =============================================================================
start_infrastructure() {
    # 1. Base infra (reuse if already running)
    if is_port_open 5432 || docker exec fbs-postgres pg_isready -U fbs -d fbs &>/dev/null 2>&1; then
        log_ok "Base infrastructuur draait al (PostgreSQL op :5432)"
    else
        log_info "Start base infrastructuur (infrastructure/docker-compose.deps.yml)..."
        docker compose -f "$INFRA_COMPOSE" up -d

        log_info "Wacht op PostgreSQL (FBS)..."
        local attempts=0
        while [[ $attempts -lt 30 ]]; do
            if docker exec fbs-postgres pg_isready -U fbs -d fbs &>/dev/null 2>&1; then
                log_ok "PostgreSQL (FBS) is klaar"
                break
            fi
            sleep 1
            attempts=$((attempts + 1))
        done
    fi

    # Check Kafka
    if docker ps --format '{{.Names}}' 2>/dev/null | grep -q fbs-kafka; then
        log_ok "Kafka draait al"
    else
        log_info "Wacht op Kafka..."
        sleep 5
    fi

    # Check MinIO
    if is_port_open 9000; then
        log_ok "MinIO draait al"
    else
        wait_for_health "MinIO" "http://localhost:9000/minio/health/live" 20 || true
    fi

    # 2. Demo-specific extra infra (profiel PostgreSQL)
    log_info "Start demo-specifieke infrastructuur..."
    docker compose -f "$DEMO_COMPOSE" up -d

    # Wait for profiel PostgreSQL
    local attempts=0
    while [[ $attempts -lt 20 ]]; do
        if docker exec fbs-demo-postgres-profiel pg_isready -U profiel -d profiel &>/dev/null 2>&1; then
            log_ok "PostgreSQL (Profiel) is klaar op :5433"
            break
        fi
        sleep 1
        attempts=$((attempts + 1))
    done

    log_ok "Infrastructuur gereed"
}

# =============================================================================
# Start profiel-service
# =============================================================================
start_profiel_service() {
    log_info "Profiel Service voorbereiden..."

    if [[ ! -d "$DEPS_DIR/moza-profiel-service" ]]; then
        log_info "Clone moza-profiel-service..."
        mkdir -p "$DEPS_DIR"
        git clone https://github.com/MinBZK/moza-profiel-service.git "$DEPS_DIR/moza-profiel-service" 2>/dev/null || {
            log_warn "Kan moza-profiel-service niet klonen. Profiel Service wordt overgeslagen."
            return 1
        }
    fi

    cd "$DEPS_DIR/moza-profiel-service"

    if ! ls target/*-runner.jar target/quarkus-app/quarkus-run.jar &>/dev/null 2>&1; then
        log_info "Build moza-profiel-service (dit kan even duren)..."
        if [[ -f mvnw ]]; then
            ./mvnw package -DskipTests -q >"$LOG_DIR/profiel-build.log" 2>&1 || {
                log_warn "Build profiel-service mislukt — check $LOG_DIR/profiel-build.log"
                cd "$PROJECT_DIR"
                return 1
            }
        elif [[ -f gradlew ]]; then
            ./gradlew build -x test -q >"$LOG_DIR/profiel-build.log" 2>&1 || {
                log_warn "Build profiel-service mislukt — check $LOG_DIR/profiel-build.log"
                cd "$PROJECT_DIR"
                return 1
            }
        fi
    fi

    cd "$PROJECT_DIR"

    # Start profiel-service
    log_info "Start Profiel Service op poort 8088..."
    local jar
    jar=$(find "$DEPS_DIR/moza-profiel-service/target" -name "quarkus-run.jar" -o -name "*-runner.jar" 2>/dev/null | head -1)
    if [[ -n "$jar" ]]; then
        QUARKUS_HTTP_PORT=8088 \
        QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://localhost:5433/profiel" \
        QUARKUS_DATASOURCE_USERNAME=profiel \
        QUARKUS_DATASOURCE_PASSWORD=profiel \
        QUARKUS_HTTP_CORS=true \
        QUARKUS_HTTP_CORS_ORIGINS=http://localhost:5173 \
        java -Dquarkus.profile=dev -jar "$jar" >"$LOG_DIR/profiel-service.log" 2>&1 &
        echo $! > "$PID_DIR/profiel-service.pid"
        wait_for_port "Profiel Service" 8088 45 || true
    else
        log_warn "Geen jar gevonden voor profiel-service"
        return 1
    fi
}

# =============================================================================
# Start Quarkus service in dev mode
# =============================================================================
start_quarkus_service() {
    local name="$1"
    local module="$2"
    local port="$3"

    # Skip if our Quarkus service is already running
    if is_quarkus_running "$port"; then
        log_ok "$name draait al op poort $port"
        return 0
    fi

    # Warn if something else is using the port
    if is_port_open "$port"; then
        log_warn "Poort $port is bezet door een ander proces! $name kan niet starten."
        log_warn "Stop het andere proces op poort $port en probeer opnieuw."
        return 1
    fi

    log_info "Start $name op poort $port..."
    cd "$PROJECT_DIR"
    ./gradlew "$module:quarkusDev" \
        -Dquarkus.http.host=0.0.0.0 \
        --no-daemon \
        >"$LOG_DIR/$name.log" 2>&1 &
    echo $! > "$PID_DIR/$name.pid"
    wait_for_port "$name" "$port" 90 || true
}

# =============================================================================
# Start demo backend services
# =============================================================================
start_demo_service() {
    local name="$1"
    local dir="$2"
    local port="$3"

    # Skip if our Quarkus service is already running
    if is_quarkus_running "$port"; then
        log_ok "$name draait al op poort $port"
        return 0
    fi

    # Warn if something else is using the port
    if is_port_open "$port"; then
        log_warn "Poort $port is bezet door een ander proces! $name kan niet starten."
        return 1
    fi

    log_info "Start $name op poort $port..."
    cd "$dir"
    JAVA_HOME="${JAVA_HOME:-}" ./gradlew quarkusDev \
        -Dquarkus.http.host=0.0.0.0 \
        --no-daemon \
        >"$LOG_DIR/$name.log" 2>&1 &
    echo $! > "$PID_DIR/$name.pid"
    cd "$PROJECT_DIR"
    wait_for_port "$name" "$port" 90 || true
}

# =============================================================================
# Start frontend
# =============================================================================
start_frontend() {
    # Skip if already running
    if curl -sf http://localhost:5173 &>/dev/null; then
        log_ok "Frontend draait al op poort 5173"
        return 0
    fi

    log_info "Start frontend op poort 5173..."
    cd "$DEMO_DIR/frontend"

    if [[ ! -d node_modules ]]; then
        log_info "Installeer frontend dependencies..."
        npm install --silent >"$LOG_DIR/frontend-install.log" 2>&1
    fi

    npm run dev >"$LOG_DIR/frontend.log" 2>&1 &
    echo $! > "$PID_DIR/frontend.pid"
    cd "$PROJECT_DIR"

    sleep 3
    if curl -sf http://localhost:5173 &>/dev/null; then
        log_ok "Frontend is beschikbaar op http://localhost:5173"
    else
        log_warn "Frontend niet bereikbaar — check $LOG_DIR/frontend.log"
    fi
}

# =============================================================================
# Stop all
# =============================================================================
stop_all() {
    log_info "Stop alle demo services..."

    if [[ -d "$PID_DIR" ]]; then
        for pidfile in "$PID_DIR"/*.pid; do
            if [[ -f "$pidfile" ]]; then
                local pid
                pid=$(cat "$pidfile")
                local name
                name=$(basename "$pidfile" .pid)
                if kill -0 "$pid" 2>/dev/null; then
                    kill "$pid" 2>/dev/null || true
                    log_ok "Gestopt: $name (PID $pid)"
                fi
                rm -f "$pidfile"
            fi
        done
    fi

    log_info "Stop demo Docker Compose (profiel PostgreSQL)..."
    docker compose -f "$DEMO_COMPOSE" down 2>/dev/null || true

    echo ""
    log_info "Base infrastructuur (PostgreSQL, Kafka, MinIO, Jaeger) is NIET gestopt."
    log_info "Stop die apart met: docker compose -f infrastructure/docker-compose.deps.yml down"

    log_ok "Demo services gestopt"
}

# =============================================================================
# Status
# =============================================================================
show_status() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  FBS Demo — Service Status${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""

    check_service_status "PostgreSQL (FBS)" 5432
    check_service_status "PostgreSQL (Profiel)" 5433
    check_service_status "Kafka" 29092
    check_service_status "MinIO" 9000
    check_service_status "Jaeger" 16686
    check_service_status "Berichtenmagazijn" 8080
    check_service_status "Berichtenlijst" 8081
    check_service_status "Profiel Service" 8088
    check_service_status "Mock Services" 8095
    check_service_status "Simulator" 8092
    check_service_status "Frontend" 5173

    echo ""
    echo -e "  ${BLUE}Frontend:${NC}          http://localhost:5173"
    echo -e "  ${BLUE}Berichtenmagazijn:${NC} http://localhost:8080"
    echo -e "  ${BLUE}Berichtenlijst:${NC}    http://localhost:8081"
    echo -e "  ${BLUE}Jaeger UI:${NC}         http://localhost:16686"
    echo -e "  ${BLUE}MinIO Console:${NC}     http://localhost:9001"
    echo ""
}

check_service_status() {
    local name="$1"
    local port="$2"

    if curl -sf "http://localhost:$port" &>/dev/null || \
       curl -sf "http://localhost:$port/q/health" &>/dev/null; then
        echo -e "  ${GREEN}●${NC} $name (poort $port)"
    else
        # Special check for PostgreSQL (no HTTP)
        if [[ "$port" == "5432" ]] && docker exec fbs-postgres pg_isready -U fbs &>/dev/null 2>&1; then
            echo -e "  ${GREEN}●${NC} $name (poort $port)"
        elif [[ "$port" == "5433" ]] && docker exec fbs-demo-postgres-profiel pg_isready -U profiel &>/dev/null 2>&1; then
            echo -e "  ${GREEN}●${NC} $name (poort $port)"
        else
            echo -e "  ${RED}●${NC} $name (poort $port)"
        fi
    fi
}

# =============================================================================
# Main
# =============================================================================
main() {
    local command="${1:-start}"

    case "$command" in
        start)
            mkdir -p "$LOG_DIR" "$PID_DIR"

            echo ""
            echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
            echo -e "${BLUE}  FBS Demo — Federatief Berichtenstelsel${NC}"
            echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
            echo ""

            check_prerequisites

            # 1. Infrastructure (reuse base, start demo extras)
            start_infrastructure

            # 2. Profiel Service (optional)
            start_profiel_service || log_warn "Profiel Service overgeslagen — demo werkt zonder"

            # 3. Berichtenmagazijn
            start_quarkus_service "berichtenmagazijn" ":services:berichtenmagazijn" 8080

            # 4. Berichtenlijst
            start_quarkus_service "berichtenlijst" ":services:berichtenlijst" 8081

            # 5. Mock Services
            start_demo_service "mock-services" "$DEMO_DIR/mock-services" 8095

            # 6. Simulator
            start_demo_service "simulator" "$DEMO_DIR/simulator" 8092

            # 7. Frontend
            start_frontend

            # Status overzicht
            show_status
            ;;

        stop)
            stop_all
            ;;

        status)
            show_status
            ;;

        restart)
            stop_all
            sleep 2
            main start
            ;;

        *)
            echo "Gebruik: $0 [start|stop|status|restart]"
            exit 1
            ;;
    esac
}

main "$@"
