package com.esiccpro.iot.serviceiot.server.protocol.converter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import com.esiccpro.iot.common.model.VehicleEvent;
import com.esiccpro.iot.serviceiot.server.convert.IConvertToMessage;
import com.esiccpro.iot.serviceiot.server.model.Position;

public class TeltonikaSenderConverter implements IConvertToMessage<Map<String, Object>, VehicleEvent> {
	
	@Override
	public VehicleEvent convert(Map<String, Object> base) {
		VehicleEvent message = new VehicleEvent();
		message.setTimeEvent(new Timestamp(((Date)base.get(Position.TIME)).getTime()));
		message.setLongitud((long)base.get(Position.LONGITUD));
		message.setLatitud((long)base.get(Position.LATITUD));
		message.setAltitud((long)base.get(Position.ALTITUDE));
		message.setImei((String)base.get(Position.IMEI));
		return message;	
	} 
}
