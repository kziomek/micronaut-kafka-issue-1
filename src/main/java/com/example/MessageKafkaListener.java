package com.example;

import io.micronaut.configuration.kafka.ConsumerAware;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.OffsetStrategy;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.core.annotation.NonNull;
import org.apache.kafka.clients.consumer.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@KafkaListener(
    clientId = "${application.kafka.messages.client-id}",
    groupId = "${application.kafka.messages.group-id}",
    offsetReset = OffsetReset.EARLIEST,
    threads = 1,
    offsetStrategy = OffsetStrategy.SYNC
)
class MessageKafkaListener implements ConsumerAware<String, Message> {

    Consumer<String, Message> consumer;
    String groupId;

    List<Message> receivedMessages = new ArrayList<Message>();

    String id = UUID.randomUUID().toString();

    @Topic("${application.kafka.messages.topic}")
    void onMessage(Message message) {
        System.out.println("MC App Listener (id="+ id +", groupId=" + groupId + ") " +" onMessage " + message);
        receivedMessages.add(message);
    }

    @Override
    public void setKafkaConsumer(@NonNull Consumer<String, Message> consumer) {
        this.consumer = consumer;
        this.groupId = consumer.groupMetadata().groupId();
    }
}