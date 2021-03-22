package com.bgc.iot.serviceiot.server.protocol.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public final class RegistrationMessageUtil {
	
	private RegistrationMessageUtil() {}
	
	public static void sendByteArray(ChannelHandlerContext ctx, byte[] out) {
		ByteBuf encoded = ctx.alloc().buffer(1);
		encoded.writeBytes(out);
		ctx.writeAndFlush(encoded);
	}
}
