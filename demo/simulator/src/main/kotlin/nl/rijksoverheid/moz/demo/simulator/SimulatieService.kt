package nl.rijksoverheid.moz.demo.simulator

import com.fasterxml.jackson.databind.ObjectMapper
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.client.FbsClient
import nl.rijksoverheid.moz.common.model.Bericht
import nl.rijksoverheid.moz.common.model.BerichtAanmaakVerzoek
import nl.rijksoverheid.moz.common.model.BerichtStatus
import nl.rijksoverheid.moz.common.model.BerichtStatusWijziging
import nl.rijksoverheid.moz.common.model.OntvangerIdType
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

data class SimulatieConfig(
    val aantalGebruikers: Int = 10,
    val berichtenPerSeconde: Int = 5,
    val duurSeconden: Int = 60
)

data class SimulatieStatus(
    val actief: Boolean,
    val verstuurd: Long,
    val gelezen: Long,
    val gearchiveerd: Long,
    val gestart: Instant?
)

data class SimulatieEvent(
    val type: String,
    val afzender: String? = null,
    val ontvanger: String? = null,
    val onderwerp: String? = null,
    val detail: String? = null,
    val timestamp: Instant = Instant.now()
)

@ApplicationScoped
class SimulatieService(
    private val fbsClient: FbsClient,
    private val objectMapper: ObjectMapper,
    @param:ConfigProperty(name = "fbs.berichtenmagazijn.url")
    private val berichtenmagazijnUrl: String
) {
    private val log = Logger.getLogger(SimulatieService::class.java)
    private val httpClient = HttpClient.newHttpClient()
    private val actief = AtomicBoolean(false)
    private val verstuurd = AtomicLong(0)
    private val gelezen = AtomicLong(0)
    private val gearchiveerd = AtomicLong(0)
    private var gestart: Instant? = null
    private var scheduler: ScheduledExecutorService? = null
    private val processor: BroadcastProcessor<String> = BroadcastProcessor.create()
    private val berichtIds = CopyOnWriteArrayList<UUID>()
    private val random = java.util.Random()

    fun start(config: SimulatieConfig): SimulatieStatus {
        if (actief.get()) return status()

        resetCounters()
        actief.set(true)
        gestart = Instant.now()

        val exec = Executors.newScheduledThreadPool(config.aantalGebruikers.coerceAtMost(10))
        scheduler = exec

        val intervalMs = (1000L / config.berichtenPerSeconde).coerceAtLeast(50)

        exec.scheduleAtFixedRate({
            if (!actief.get()) return@scheduleAtFixedRate
            try {
                executeRandomAction()
            } catch (e: Exception) {
                log.warnf(e, "Simulatie actie mislukt: %s", e.message)
                publishEvent(SimulatieEvent(
                    type = "ERROR",
                    detail = "Actie mislukt: ${e.message}"
                ))
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS)

        // Auto-stop after duration
        exec.schedule({
            stop()
        }, config.duurSeconden.toLong(), TimeUnit.SECONDS)

        log.infof("Simulatie gestart: %d gebruikers, %d msg/s, %ds",
            config.aantalGebruikers, config.berichtenPerSeconde, config.duurSeconden)

        publishEvent(SimulatieEvent(
            type = "SIMULATIE_GESTART",
            detail = "${config.aantalGebruikers} gebruikers, ${config.berichtenPerSeconde} msg/s, ${config.duurSeconden}s"
        ))

        return status()
    }

    fun stop(): SimulatieStatus {
        if (!actief.get()) return status()
        actief.set(false)
        scheduler?.shutdownNow()
        scheduler = null

        publishEvent(SimulatieEvent(
            type = "SIMULATIE_GESTOPT",
            detail = "Verstuurd: ${verstuurd.get()}, Gelezen: ${gelezen.get()}, Gearchiveerd: ${gearchiveerd.get()}"
        ))

        log.infof("Simulatie gestopt: verstuurd=%d, gelezen=%d, gearchiveerd=%d",
            verstuurd.get(), gelezen.get(), gearchiveerd.get())

        return status()
    }

    fun status(): SimulatieStatus = SimulatieStatus(
        actief = actief.get(),
        verstuurd = verstuurd.get(),
        gelezen = gelezen.get(),
        gearchiveerd = gearchiveerd.get(),
        gestart = gestart
    )

    fun eventStream(): Multi<String> = processor

    private fun executeRandomAction() {
        val roll = random.nextInt(100)
        when {
            roll < 60 -> verstuurBericht()
            roll < 90 && berichtIds.isNotEmpty() -> leesBericht()
            berichtIds.isNotEmpty() -> archiveerBericht()
            else -> verstuurBericht() // fallback if no messages to read
        }
    }

    private fun verstuurBericht() {
        val org = DemoData.organisaties.random()
        val bsn = DemoData.burgerBsns.random()
        val onderwerpen = DemoData.onderwerpen[org.naam] ?: return
        val onderwerp = onderwerpen.random()
        val inhoud = DemoData.berichtInhouden[org.naam] ?: "Demo bericht inhoud"

        val verzoek = BerichtAanmaakVerzoek(
            ontvangerIdType = OntvangerIdType.BSN,
            ontvangerId = bsn,
            onderwerp = onderwerp,
            inhoud = inhoud
        )

        val url = "${berichtenmagazijnUrl.trimEnd('/')}/api/v1/berichten"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("API-Version", "1.0.0")
            .header("X-Afzender-OIN", org.oin)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(verzoek)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 201) {
            log.warnf("Bericht aanmaken mislukt: %d %s", response.statusCode(), response.body())
            return
        }
        val bericht = objectMapper.readValue(response.body(), Bericht::class.java)
        berichtIds.add(bericht.id)
        if (berichtIds.size > 500) berichtIds.removeAt(0)
        verstuurd.incrementAndGet()

        publishEvent(SimulatieEvent(
            type = "BERICHT_ONTVANGEN",
            afzender = org.naam,
            ontvanger = "BSN ***${bsn.takeLast(4)}",
            onderwerp = onderwerp
        ))
    }

    private fun leesBericht() {
        val berichtId = berichtIds.randomOrNull() ?: return
        try {
            fbsClient.berichten().werkBerichtBij(
                berichtId,
                BerichtStatusWijziging(BerichtStatus.GELEZEN)
            )
            gelezen.incrementAndGet()
            publishEvent(SimulatieEvent(
                type = "BERICHT_GELEZEN",
                detail = "Bericht $berichtId gelezen"
            ))
        } catch (e: Exception) {
            // Bericht was possibly already read, ignore
            log.debugf("Lees mislukt voor %s: %s", berichtId, e.message)
        }
    }

    private fun archiveerBericht() {
        val berichtId = berichtIds.randomOrNull() ?: return
        try {
            fbsClient.berichten().werkBerichtBij(
                berichtId,
                BerichtStatusWijziging(BerichtStatus.GEARCHIVEERD)
            )
            gearchiveerd.incrementAndGet()
            publishEvent(SimulatieEvent(
                type = "BERICHT_GEARCHIVEERD",
                detail = "Bericht $berichtId gearchiveerd"
            ))
        } catch (e: Exception) {
            log.debugf("Archiveer mislukt voor %s: %s", berichtId, e.message)
        }
    }

    private fun resetCounters() {
        verstuurd.set(0)
        gelezen.set(0)
        gearchiveerd.set(0)
        berichtIds.clear()
    }

    private fun publishEvent(event: SimulatieEvent) {
        try {
            processor.onNext(objectMapper.writeValueAsString(event))
        } catch (e: Exception) {
            log.debugf("SSE publish mislukt: %s", e.message)
        }
    }
}
