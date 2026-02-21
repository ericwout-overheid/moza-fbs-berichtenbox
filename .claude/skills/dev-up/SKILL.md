---
name: dev-up
description: Start or stop local development dependencies (PostgreSQL, Kafka, MinIO, Jaeger). Use when user says /dev-up, /dev-down, "start deps", "stop deps".
---

# Dev Dependencies Skill

Manage local development infrastructure via Docker Compose.

## Commands

### `/dev-up` — Start dependencies

```bash
docker compose -f infrastructure/docker-compose.deps.yml up -d
```

After starting, verify health by running:
```bash
docker compose -f infrastructure/docker-compose.deps.yml ps
```

Show the port mapping table to the user.

### `/dev-down` — Stop dependencies

```bash
docker compose -f infrastructure/docker-compose.deps.yml down
```

### `/dev-up status` — Show status

```bash
docker compose -f infrastructure/docker-compose.deps.yml ps
```

## Port Mapping

| Service | Port | Notes |
|---------|------|-------|
| PostgreSQL | `localhost:5432` | user: `fbs`, pass: `fbs`, db: `fbs` |
| Kafka | `localhost:29092` | KRaft mode, **niet** 9092 (dat is intern) |
| MinIO API | `localhost:9000` | user: `minioadmin`, pass: `minioadmin` |
| MinIO Console | `localhost:9001` | Web UI |
| Jaeger UI | `localhost:16686` | Traces bekijken |
| Jaeger OTLP | `localhost:4317` | gRPC endpoint voor traces |

## Notes

- Kafka topics worden automatisch aangemaakt door de `kafka-init` container.
- PostgreSQL databases voor alle services worden aangemaakt bij eerste start.
- MinIO buckets worden aangemaakt door de `minio-init` container.
