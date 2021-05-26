package com.example

import io.micronaut.configuration.kafka.ConsumerAware
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.OffsetStrategy
import io.micronaut.configuration.kafka.annotation.Topic
import org.apache.kafka.clients.consumer.Consumer

import io.micronaut.core.annotation.NonNull

import javax.annotation.PreDestroy

@KafkaListener(
//    clientId = '${application.kafka.messages.client-id}-test',
    clientId = '${application.kafka.messages.client-id}', //TODO Using app listener client-id for test listener causes test consumer not being closed between tests
    groupId = 'test-kafka-group-1',
    offsetReset = OffsetReset.EARLIEST,
    threads = 1,
    offsetStrategy = OffsetStrategy.SYNC
)
class MessageTestListener implements ConsumerAware<String, Message> {

    Consumer<String, Message> consumer
    String groupId

    def receivedMessages = new ArrayList<Message>()

    def id = UUID.randomUUID()

    @Topic('${application.kafka.messages.topic}')
    void onLocationCluster(Message message) {
        println "MC Test Listener (id=" + id + ", groupId=" + groupId + ") onMessage " + message
        receivedMessages.add(message)
    }

    def containsMessageMatching(Closure<Boolean> isMatch) {
        println "MC Test Listener (id="+ id +", groupId=" + groupId + ") containsMessageMatching size " + receivedMessages.size()
        !receivedMessages.findAll { isMatch(it) }.isEmpty()
    }

    @Override
    void setKafkaConsumer(@NonNull Consumer<String, Message> consumer) {
        this.consumer = consumer
        this.groupId = consumer.groupMetadata().groupId()
    }

    @PreDestroy
    void preDestroy() {
        println "MC Test Listener (id="+ id +", groupId=" + groupId + ") preDestroy() invoked "
    }
}
