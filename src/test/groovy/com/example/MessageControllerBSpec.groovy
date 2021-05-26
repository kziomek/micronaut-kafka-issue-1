package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.inject.Inject

@MicronautTest
class MessageControllerBSpec extends Specification implements TestPropertyProvider {

    @Inject
    @Client("/")
    HttpClient client

    @Inject
    MessageTestListener messageListener

    @Override
    Map<String, String> getProperties() {
        KafkaContainerProvider.getProperties()
    }

    def "listener should consume message published from service B"() {
        given:
        println "MC Started test B"

        def messageId = "2"
        Message message = new Message(messageId, "a content B")

        println "MC B message listener id=" + messageListener.getId()

        when:
        def response = client.toBlocking().exchange(HttpRequest.POST("/messages", message))

        then:
        response.status() == HttpStatus.OK

        new PollingConditions(timeout: 10).eventually {
            assert messageListener.containsMessageMatching { Message m -> m.getId() == messageId }
            assert messageListener.getReceivedMessages().size() == 1
        }
        cleanup:
        println "MC Finished test B"
    }
}