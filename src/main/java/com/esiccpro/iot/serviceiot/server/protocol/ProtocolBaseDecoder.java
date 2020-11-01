package com.esiccpro.iot.serviceiot.server.protocol;

import com.esiccpro.iot.serviceiot.server.model.Position;

import io.netty.buffer.ByteBuf;

public interface ProtocolBaseDecoder {

	byte[] sendAccept();

	byte[] sendRejected();
	
	Position handleData(ByteBuf inBuffer, Position position);
}
