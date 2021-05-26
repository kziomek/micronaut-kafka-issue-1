## Dependencies
 - java AdoptOpenJDK 11.0.10+9
 - docker 20.10.5
 - macOS BigSur 11.2.3

## How to run

`./gradlew clean build --info`

## How application works
Application has `MessageController` with @Post method which publish message to Kafka topic.
Application has MessageKafkaListener which consumes messages from the topic (and do nothing with them).

## Tests
1. How do we test message is published to the topic?
`MessageTestListener` consumes topic and stores messages in `receivedMessages` list so tests can look into the list to prove message is published and can be consumed.
Test listener is defined with different group-id than app listener so both listeners can consume the same messages from the topic.

2. Specs
There are 2 specs annotated with @MicronautTest which works when run individually
- MessageControllerSpecA
- MessageControllerSpecB

## What is the problem
Tests pass when run individually, but failed when run together (one after another, not in parallel)

When you analyse logs you can see that consumer created in first spec subscribes to the topic before first test runs and don't close after test is completed.
When second test starts it creates new listener with new consumer which can't consume from the topic because consumer from first test still exists and is assigned to the topic partition.
As a result second test waits for message for 10 seconds and fails.
You should see in logs first listener consumes messages from second test.

## Available workarounds
a) Use different group-id for each of tests
 - it is an option but each test would consume messages from the beginning
b) delete topic between tests
 - it might be slower option and could increase tests execution time in total
c) define different client-id for app and test listener

## Is it a bug or intended behaviour?
It took us some time to investigate the issue and find out that `client-id` makes the difference.
We found out that tests pass when client-id is different in test and app listeners.
It would be great to understand reason behind it if it's intended or necessary behaviour.
Otherwise we would consider it as not desirable and hard to track issue.

What made it worse tests were failing only in developer's computers and Jenkins build was green.
It seems developers can easily introduce bug which is relatively hard to track,
 especially that we didn't observe same issue when tests were executed in Jenkins.


## Micronaut 2.5.4 Documentation

- [User Guide](https://docs.micronaut.io/2.5.4/guide/index.html)
- [API Reference](https://docs.micronaut.io/2.5.4/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/2.5.4/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

## Feature kafka documentation

- [Micronaut Kafka Messaging documentation](https://micronaut-projects.github.io/micronaut-kafka/latest/guide/index.html)

## Feature testcontainers documentation

- [https://www.testcontainers.org/](https://www.testcontainers.org/)

## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

