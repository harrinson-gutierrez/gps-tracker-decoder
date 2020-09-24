package com.esiccpro.iot.serviceiot.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnectionInterceptorFactoryChain;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.messaging.MessageChannel;


@Configuration
public class TcpServerConfig {

	@Value("${tcp.server.port}")
	private int port;
	
	@Bean
	public AbstractServerConnectionFactory serverConnectionFactory(TcpConnectionInterceptorFactoryChain helloWorldInterceptorFactory) {
		TcpNetServerConnectionFactory serverConnectionFactory = new TcpNetServerConnectionFactory(port);
		serverConnectionFactory.setSingleUse(false);
		ByteArrayCrLfSerializer  serializer = new ByteArrayCrLfSerializer ();
		serverConnectionFactory.setSerializer(serializer);
		serverConnectionFactory.setDeserializer(serializer);
		return serverConnectionFactory;
	}

	@Bean
	public TcpReceivingChannelAdapter inTwo(AbstractServerConnectionFactory cfTwo,MessageChannel inboundChannel) {
	    TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
	    adapter.setConnectionFactory(cfTwo);
	    adapter.setOutputChannel(inboundChannel);
	    return adapter;
	}
	
	@Bean
	public MessageChannel inboundChannel() {
		return new DirectChannel();
	}

	
	
	@Bean
	public TcpInboundGateway inboundGateway(AbstractServerConnectionFactory serverConnectionFactory,
			MessageChannel inboundChannel) {
		TcpInboundGateway tcpInboundGateway = new TcpInboundGateway();
		tcpInboundGateway.setConnectionFactory(serverConnectionFactory);
		tcpInboundGateway.setRequestChannel(inboundChannel);
		return tcpInboundGateway;
	}
}
