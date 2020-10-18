package com.esiccpro.iot.serviceiot.server;

import java.net.InetSocketAddress;

import javax.annotation.PreDestroy;

import com.esiccpro.iot.serviceiot.server.factory.EventLoopGroupFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {

	private ServerBootstrap serverBootstrap;
	
	public NettyServer(int port, ChannelInitializer<SocketChannel> channelInit) throws InterruptedException {
		this.serverBootstrap = new ServerBootstrap();
		this.serverBootstrap.group(EventLoopGroupFactory.getBossGroup(), EventLoopGroupFactory.getWorkerGroup())
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(channelInit);
		
        ChannelFuture future = serverBootstrap.bind().sync();
        if (future.isSuccess()) {
            log.info("start-up Netty Server {}", port);
        }
	}
	
    @PreDestroy
    public void destory() throws InterruptedException {
    	EventLoopGroupFactory.getBossGroup().shutdownGracefully().sync();
    	 EventLoopGroupFactory.getWorkerGroup().shutdownGracefully().sync();
        log.info("Close Netty");
    }
}
