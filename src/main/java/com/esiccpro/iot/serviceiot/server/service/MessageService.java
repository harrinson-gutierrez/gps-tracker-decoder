package com.esiccpro.iot.serviceiot.server.service;

import java.util.Map;

import com.esiccpro.iot.common.model.Message;
import com.esiccpro.iot.serviceiot.server.convert.IConvertToMessage;
import com.esiccpro.iot.serviceiot.server.protocol.IBaseProtocol;

public interface MessageService {

	void processMessage(IBaseProtocol protocol, IConvertToMessage<Map<String, Object>, Message> converter);

}