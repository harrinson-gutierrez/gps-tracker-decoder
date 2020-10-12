package com.esiccpro.iot.serviceiot.server.service;

import java.util.Map;

import com.esiccpro.iot.common.model.VehicleEvent;
import com.esiccpro.iot.serviceiot.server.convert.IConvertToMessage;
import com.esiccpro.iot.serviceiot.server.protocol.Protocol;

public interface MessageService {

	void processMessage(Protocol protocol, IConvertToMessage<Map<String, Object>, VehicleEvent> converter);

}