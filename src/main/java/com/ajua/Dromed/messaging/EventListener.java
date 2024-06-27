//package com.ajua.Dromed.messaging;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EventListener {
//
//    @Autowired
//    private EventHandlerService eventHandlerService;
//
//    @KafkaListener(topics = "drone-events", groupId = "group_id")
//    public void consume(@Payload String message) throws JsonProcessingException {
//        // Deserialize the message and determine the type of event
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode node = objectMapper.readTree(message);
//        String eventType = node.get("eventType").asText();
//
//        if ("DroneRegisteredEvent".equals(eventType)) {
//            DroneRegisteredEvent event = objectMapper.treeToValue(node, DroneRegisteredEvent.class);
//            eventHandlerService.handleDroneRegisteredEvent(event);
//        } else if ("MedicationLoadedEvent".equals(eventType)) {
//            MedicationLoadedEvent event = objectMapper.treeToValue(node, MedicationLoadedEvent.class);
//            eventHandlerService.handleMedicationLoadedEvent(event);
//        }
//    }
//}
