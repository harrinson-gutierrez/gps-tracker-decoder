package com.esiccpro.iot.serviceiot.server.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import com.esiccpro.iot.serviceiot.server.service.MessageService;

@MessageEndpoint
public class TcpServerEndpoint {
	private MessageService messageService;

	@Autowired
	public TcpServerEndpoint(MessageService messageService) {
		this.messageService = messageService;
	}

	@ServiceActivator(inputChannel = "inboundChannel")
	public byte[] process(byte[] message) {
		return messageService.processMessage(message);
	}
}
