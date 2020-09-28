package com.esiccpro.iot.serviceiot.server.protocol;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface IBaseProtocol {
	
	byte[] sendAccept();
	
	byte[] sendRejected();
	
	void handle(ChannelHandlerContext ctx, ByteBuf inBuffer);
	
	void sendAck(ChannelHandlerContext ctx, byte[] msg);
	
	Map<String, Object> getPositions();
}
