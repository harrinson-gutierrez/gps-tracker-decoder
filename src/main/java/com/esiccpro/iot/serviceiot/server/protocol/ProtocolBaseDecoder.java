package com.esiccpro.iot.serviceiot.server.protocol;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface ProtocolBaseDecoder {

	byte[] sendAccept();

	byte[] sendRejected();
	
	Map<String, Object> handleData(ByteBuf inBuffer, Map<String, Object> positions);

	void sendAck(ChannelHandlerContext ctx, byte[] msg);
}
