package com.esiccpro.iot.serviceiot.server.service;

import com.esiccpro.iot.common.model.VehicleEvent;

public interface MessageService {

	void processMessage(VehicleEvent converter);

}