package com.opsflow.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String REQUEST_CREATED = "request.created";
    public static final String REQUEST_APPROVED = "request.approved";
    public static final String REQUEST_REJECTED = "request.rejected";
    public static final String REQUEST_COMPLETED = "request.completed";

    @Bean
    public NewTopic requestCreatedTopic() {
        return TopicBuilder.name(REQUEST_CREATED).partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic requestApprovedTopic() {
        return TopicBuilder.name(REQUEST_APPROVED).partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic requestRejectedTopic() {
        return TopicBuilder.name(REQUEST_REJECTED).partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic requestCompletedTopic() {
        return TopicBuilder.name(REQUEST_COMPLETED).partitions(3).replicas(1).build();
    }
}
