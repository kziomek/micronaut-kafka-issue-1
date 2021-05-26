package com.example

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.testcontainers.containers.KafkaContainer

class KafkaContainerProvider {

    private static final AdminClient adminClient
    private static KafkaContainer kafka = new KafkaContainer()

    // this code will make kafka container available for app so messages can be published and consumed
    static {
        kafka.start()
        Properties props = new Properties()
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
        adminClient = AdminClient.create(props)
    }

    static Map<String, String> getProperties() {
        ['kafka.bootstrap.servers': kafka.bootstrapServers]
    }
}