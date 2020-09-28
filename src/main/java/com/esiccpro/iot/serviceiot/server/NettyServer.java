package com.esiccpro.iot.serviceiot.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Component
@Slf4j
public class NettyServer {
    /**
     * boss Thread groups are used to handle connection work
     */
    private EventLoopGroup boss = new NioEventLoopGroup();
    /**
     * work Thread groups for data processing
     */
    private EventLoopGroup work = new NioEventLoopGroup();

    @Value("${netty.port}")
    private Integer port;

    @Autowired
    @Qualifier("somethingChannelInitializer")
    private ServerChannelInitializer somethingChannelInitializer;
    
	
    /**
     * Start Netty Server
     *
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, work)
                // Specify Channel
                .channel(NioServerSocketChannel.class)
                //Set socket address using specified port
                .localAddress(new InetSocketAddress(port))

                //Number of server-side connectable queues corresponding to backlog parameter in TCP/IP protocol listen function
                .option(ChannelOption.SO_BACKLOG, 1024)

                //Set up a long TCP connection. Typically, TCP will automatically send an activity detection datagram if there is no data communication within two hours
                .childOption(ChannelOption.SO_KEEPALIVE, true)

                //Enhance network load by packaging small packets into larger frames for transmission
                .childOption(ChannelOption.TCP_NODELAY, true)

                .childHandler(somethingChannelInitializer);
        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            log.info("start-up Netty Server");
        }
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        work.shutdownGracefully().sync();
        log.info("Close Netty");
    }
}