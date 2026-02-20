package nl.fbs.cloudevents

import io.cloudevents.kafka.CloudEventDeserializer
import io.cloudevents.kafka.CloudEventSerializer

/**
 * Factory helpers voor Kafka CloudEvents serializers en deserializers.
 *
 * Biedt convenience methods voor het aanmaken van serializers/deserializers
 * voor gebruik met Kafka producers en consumers.
 */
object FbsKafkaCloudEvents {

    /**
     * Maakt een CloudEvents Kafka serializer aan.
     *
     * @return een geconfigureerde [CloudEventSerializer]
     */
    fun createSerializer(): CloudEventSerializer = CloudEventSerializer()

    /**
     * Maakt een CloudEvents Kafka deserializer aan.
     *
     * @return een geconfigureerde [CloudEventDeserializer]
     */
    fun createDeserializer(): CloudEventDeserializer = CloudEventDeserializer()

    /**
     * Kafka producer configuratie properties voor CloudEvents serialisatie.
     */
    fun serializerProperties(): Map<String, String> = mapOf(
        "value.serializer" to CloudEventSerializer::class.java.name
    )

    /**
     * Kafka consumer configuratie properties voor CloudEvents deserialisatie.
     */
    fun deserializerProperties(): Map<String, String> = mapOf(
        "value.deserializer" to CloudEventDeserializer::class.java.name
    )
}
