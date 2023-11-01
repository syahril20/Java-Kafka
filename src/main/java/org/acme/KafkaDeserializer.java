package org.acme;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import org.acme.models.KafkaModel;

public class KafkaDeserializer extends ObjectMapperDeserializer<KafkaModel> {
    public KafkaDeserializer(){
        super(KafkaModel.class);
    }
}
