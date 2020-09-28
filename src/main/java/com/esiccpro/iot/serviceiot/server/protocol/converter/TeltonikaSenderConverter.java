package com.esiccpro.iot.serviceiot.server.protocol.converter;

import java.util.Date;
import java.util.Map;

import com.esiccpro.iot.common.model.Message;
import com.esiccpro.iot.serviceiot.server.convert.IConvertToMessage;
import com.esiccpro.iot.serviceiot.server.model.Position;

public class TeltonikaSenderConverter implements IConvertToMessage<Map<String, Object>, Message> {
	
	@Override
	public Message convert(Map<String, Object> base) {
		Message message = new Message();
		message.setTime((Date)base.get(Position.TIME));
		message.setLongitud((long)base.get(Position.LONGITUD));
		message.setLatitud((long)base.get(Position.LATITUD));
		message.setAltitud((long)base.get(Position.ALTITUDE));
		return message;	
	} 
}
