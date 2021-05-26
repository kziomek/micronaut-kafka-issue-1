package com.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.reactivex.Completable;
import lombok.AllArgsConstructor;

@Controller("/messages")
@AllArgsConstructor
public class MessageController {

    private MessageKafkaProducer producer;

    @Get
    public Message get() {
        return new Message("1", "GET_MESSAGE");
    }

    @Post
    public Completable publish(Message message) {
        return producer.send(message.getId(), message)
            .doOnSuccess(s -> System.out.println("MessageController published message. " + message))
            .doOnError(e -> System.out.println("MessageController failed to publish message. " + message + e))
            .ignoreElement();
    }
}