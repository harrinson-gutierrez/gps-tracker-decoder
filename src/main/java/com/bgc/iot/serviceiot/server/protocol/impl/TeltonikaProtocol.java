package com.bgc.iot.serviceiot.server.protocol.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bgc.iot.serviceiot.server.NettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

@Component
public class TeltonikaProtocol {
	
	@Autowired
	private TeltonikaProtocolDecoder teltonikaProtocolDecoder;
	
	public TeltonikaProtocol() throws InterruptedException {
		new NettyServer(10001, new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(teltonikaProtocolDecoder);
			}
		});
	}
}
