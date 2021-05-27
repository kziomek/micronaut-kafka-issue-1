## Description
This code reproduces an issue where test kafka listener does not close correctly after test.

Issue can be reproduced in following configuration:
1. Define 2 kafka listeners. One in application code and second in test code.
2. Set different groupId for kafka listeners so they can consume the same messages from the same topic.
3. Set the same clientId for both consumers
4. Define 2 test in two separate classes annotated with @MicronautTest


## Run tests to reproduce issue
Run tests to see them failed.
`./gradlew clean build --info`

You can grep console logs by MC to see logs from application.
I.e. you can see listener is supposed to be closed after first test but then consumes a message produced in second test.
``./gradlew clean build --info | grep MC`

## Workaround to the issue
We believe using the same clientId in MessageKafkaListener and MessageTestListener somehow prevent MessageTestListener instance from being properly disposed.
You can change clientId to different value in MessageTestListener and rerun tests to see them passed.

## Archive folder
Look into archive folder to see example logs. I produced these files locally and committed them.
Application doesn't write to archive folder.

archive/differentClientIds.txt - in this log you can see expected behaviour. Listener id=ebce8f72-5ed4-4d28-ae56-10aaa8a09752 is disposed before executing second test.
archive/sameClientIds.txt - in this log you can see listener id=4bb7b57d-e963-423b-b0d6-cb60986360d2 is supposed to be disposed but then consumes message.

## How application works
Application has `MessageController` with @Post method which publish message to Kafka topic.
Application has MessageKafkaListener which consumes messages from the topic.

## Tests
1. How do we test message is published to the topic?
`MessageTestListener` consumes from the topic and stores messages in its internal collection so tests can look into it to prove message was published and can be consumed.
`MessageTestListener` is defined with different group-id than `MessageKafkaListener` so both listeners can consume the same messages from the topic.

2. Specs
There are 2 specs annotated with @MicronautTest which works when run individually.
- MessageControllerSpecA
- MessageControllerSpecB

Tests pass when run individually, but fail when run together (one after another, not in parallel).

## Why tests fail
When you analyse logs you can see that consumer created in first spec subscribes to the topic before first test runs and don't close after test is completed.
When second test starts it creates new listener with new consumer which can't consume from the topic because consumer from first test still exists and is assigned to the topic partition.
As a result second test waits for message for 10 seconds and fails.
You should see in logs first listener consumes messages from second test.

## What are available workarounds
a) Use different group-id for each of tests
 - it is an option but each test would consume messages from the beginning
b) delete topic between tests
 - recreating topic between tests might be a bit slower option and could increase tests execution time in total
c) define different client ids for app listener and test listener
 - it worked but it took us a lot of time to figure out client-id might causing the issue

## Why using the same client-id stops micronaut from closing listener and consumer correctly
It took us some time to investigate the issue and find out that `client-id` causes the issue.
We found out that tests pass when client-id is not shared between test listener and app listener.
It might not be bad idea to have separate client ids but why would micronaut fail to close listener when client-id is shared?

What made it worse tests were failing only in developer's Macs but Jenkins build was green.
This way bug made it's way to master branch.

## Related issues
https://issues.apache.org/jira/browse/KAFKA-3992?focusedCommentId=15823394&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-15823394

## Environment Information

- **Operating System**: macOS BigSur 11.2.3
- **Micronaut Version:** 2.5.4
- **JDK Version:** AdoptOpenJDK 11.0.10+9
- **Docker:** 20.10.5

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

