package com.esiccpro.iot.serviceiot.server.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esiccpro.iot.common.model.Message;
import com.esiccpro.iot.serviceiot.ampq.config.MessageProducerConfig;
import com.esiccpro.iot.serviceiot.server.convert.IConvertToMessage;
import com.esiccpro.iot.serviceiot.server.protocol.IBaseProtocol;
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
    public void processMessage(IBaseProtocol protocol, IConvertToMessage<Map<String, Object>, Message> converter) {
    	
    	Message message = converter.convert(protocol.getPositions());
    	
        LOGGER.info("Receive message from handler protocol: {}", message);

        rabbitTemplate.convertAndSend(MessageProducerConfig.EXCHANGE_NAME, MessageProducerConfig.ROUTING_KEY, message);
    }

}