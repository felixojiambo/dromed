package com.ajua.Dromed.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(String topic, Object payload) {
        kafkaTemplate.send(topic, payload);
    }
}