package com.esiccpro.iot.serviceiot.server.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esiccpro.iot.common.model.Message;
import com.esiccpro.iot.serviceiot.ampq.config.MessageProducerConfig;
import com.esiccpro.iot.serviceiot.server.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    public MessageServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    @Override
    public byte[] processMessage(byte[] message) {
        String messageContent = new String(message);
        LOGGER.info("Receive message from client tcp: {}", messageContent);
        String responseContent = String.format("Message \"%s\" is processed in producer ampq", messageContent);
        
        Message messageRequest = new Message();
        messageRequest.setTitle("message title");
        messageRequest.setBody(messageContent);
        messageRequest.setDate(LocalDateTime.now());
        
        LOGGER.info("Send message from producer: {}", responseContent);
        
        rabbitTemplate.convertAndSend(MessageProducerConfig.EXCHANGE_NAME, MessageProducerConfig.ROUTING_KEY, messageRequest);
        
        return new byte[]{ 01 };
    }

}