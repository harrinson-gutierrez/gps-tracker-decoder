package com.esiccpro.iot.serviceiot.server.protocol.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esiccpro.iot.serviceiot.server.NettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

@Component
public class OtherProtocol {
	
	@Autowired
	OtherProtocolDecoder otherProtocolDecoder;
	
	public OtherProtocol() throws InterruptedException {
		new NettyServer(10002, new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(otherProtocolDecoder);
			}
		});
	}
}
