package com.esiccpro.iot.serviceiot.server.protocol.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.esiccpro.iot.common.model.VehicleEvent;
import com.esiccpro.iot.serviceiot.server.convert.IConvertToMessage;
import com.esiccpro.iot.serviceiot.server.model.Position;

@Component
public class OtherSenderConverter implements IConvertToMessage<Map<String, Object>, VehicleEvent> {
	
	@Override
	public VehicleEvent convert(Map<String, Object> base) {
		VehicleEvent message = new VehicleEvent();
		message.setTimeEvent(new Timestamp(new Date().getTime()));
		message.setLongitud((double)base.get(Position.LONGITUD));
		message.setLatitud((double)base.get(Position.LATITUD));
		message.setAltitud((long)base.get(Position.ALTITUDE));
		message.setImei((String)base.get(Position.IMEI));
		return message;	
	} 
}

