package com.esiccpro.iot.serviceiot.server.protocol.impl;

import java.util.Map;

import com.esiccpro.iot.serviceiot.server.model.Position;
import com.esiccpro.iot.serviceiot.server.model.Position.PositionType;
import com.esiccpro.iot.serviceiot.server.protocol.IBaseProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;

public class TeltonikaProtocol extends Protocol implements IBaseProtocol {
	
	public TeltonikaProtocol(Map<String, Object> positions) {
		super(positions);
	}

	@Override
	public void handle(ChannelHandlerContext ctx, ByteBuf inBuffer) {	
		String dumpHex = ByteBufUtil.prettyHexDump(inBuffer);
		LOGGER.info(dumpHex);
		
		set(Position.PREAMBLE, inBuffer, 0, 4, PositionType.LONG);
		set(Position.LENGHT_DATA, inBuffer, 4, 4, PositionType.LONG);
		set(Position.CODEC, inBuffer, 8, 1, PositionType.LONG);
		set(Position.DATA_AVL, inBuffer, 9, 1, PositionType.LONG);
		set(Position.TIME, inBuffer, 10, 8, PositionType.DATETIME);
		set(Position.PRIORITY, inBuffer, 18, 1, PositionType.LONG);
		set(Position.LONGITUD, inBuffer, 19, 4, PositionType.LONG);
		set(Position.LATITUD, inBuffer, 23, 4, PositionType.LONG);
		set(Position.ALTITUDE, inBuffer, 27, 2, PositionType.LONG);
	}

	@Override
	public byte[] sendAccept() {
		return new byte[] { 0x01  };
	}
	
	@Override
	public byte[] sendRejected() {
		return new byte[] { 0x00f };
	}

	
	public void sendAck(ChannelHandlerContext ctx, byte[] msg) {
		int decimal = Integer.parseInt(Integer.toBinaryString(msg[9] & 0xFF).replace(' ', '0'), 2);
		ByteBuf encoded = ctx.alloc().buffer(4);
		encoded.writeInt(decimal);
		ctx.writeAndFlush(encoded);
	}
}
 