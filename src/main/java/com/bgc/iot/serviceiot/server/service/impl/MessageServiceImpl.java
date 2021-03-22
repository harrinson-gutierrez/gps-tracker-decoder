package com.bgc.iot.serviceiot.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bgc.iot.serviceiot.ampq.config.MessageProducerConfig;
import com.bgc.iot.serviceiot.server.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    public MessageServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    @Override
    public void processMessage(String data) {

        LOGGER.info("Receive message from handler protocol: {}", data);

        rabbitTemplate.convertAndSend(MessageProducerConfig.EXCHANGE_NAME, MessageProducerConfig.ROUTING_KEY, data);
    }

}