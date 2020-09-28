package com.esiccpro.iot.serviceiot.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

@Component
@Qualifier("somethingChannelInitializer")
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	 @Autowired
	 @Qualifier("somethingServerHandler")
	 private ChannelInboundHandlerAdapter somethingServerHandler;
	
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(somethingServerHandler);
    }
}