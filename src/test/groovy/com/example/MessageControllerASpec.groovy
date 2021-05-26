package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.inject.Inject

@MicronautTest
class MessageControllerASpec extends Specification implements TestPropertyProvider {

    @Inject
    @Client("/")
    HttpClient client

    @Inject
    MessageTestListener messageListener

    @Override
    Map<String, String> getProperties() {
        KafkaContainerProvider.getProperties()
    }

    @Ignore
    def "message controller should respond"() {
        when:
        HttpResponse<Message> resp = client.toBlocking().exchange(HttpRequest.GET("/messages"), Message)

        then:
        resp.status() == HttpStatus.OK

        resp.getBody().get().getContent() == "GET_MESSAGE"
    }

    def "listener should consume message published from service A"() {
        given:
        println "MC Started test A"
        def messageId = "1"
        Message message = new Message(messageId, "a content A")

        println "MC A message listener id=" + messageListener.getId()

        when:
        def response = client.toBlocking().exchange(HttpRequest.POST("/messages", message))

        then:
        response.status() == HttpStatus.OK

        new PollingConditions(timeout: 10).eventually {
            assert messageListener.containsMessageMatching { Message m -> m.getId() == messageId }
            assert messageListener.getReceivedMessages().size() == 1
        }
        cleanup:
        println "MC Finished test A"
    }
}