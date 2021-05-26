package com.example;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.reactivex.Single;

@KafkaClient(value = "message-client")
public interface MessageKafkaProducer {

    @Topic("${application.kafka.messages.topic}")
    Single<Message> send(@KafkaKey String id, Message message);
}