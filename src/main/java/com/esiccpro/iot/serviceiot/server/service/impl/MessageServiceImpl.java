package com.esiccpro.iot.serviceiot.server.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esiccpro.iot.common.model.VehicleEvent;
import com.esiccpro.iot.serviceiot.ampq.config.MessageProducerConfig;
import com.esiccpro.iot.serviceiot.server.convert.IConvertToMessage;
import com.esiccpro.iot.serviceiot.server.protocol.Protocol;
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
    public void processMessage(Protocol protocol, IConvertToMessage<Map<String, Object>, VehicleEvent> converter) {
    	
    	VehicleEvent message = converter.convert(protocol.getPositions());
    	message.setImei(protocol.getImei());
    	
        LOGGER.info("Receive message from handler protocol: {}", message);

        rabbitTemplate.convertAndSend(MessageProducerConfig.EXCHANGE_NAME, MessageProducerConfig.ROUTING_KEY, message);
    }

}