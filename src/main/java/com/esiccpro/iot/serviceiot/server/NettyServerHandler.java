package com.esiccpro.iot.serviceiot.server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.esiccpro.iot.serviceiot.server.model.Position;
import com.esiccpro.iot.serviceiot.server.model.Position.PositionType;
import com.esiccpro.iot.serviceiot.server.protocol.Protocol;
import com.esiccpro.iot.serviceiot.server.protocol.converter.TeltonikaSenderConverter;
import com.esiccpro.iot.serviceiot.server.protocol.impl.TeltonikaProtocol;
import com.esiccpro.iot.serviceiot.server.protocol.util.MessageUtil;
import com.esiccpro.iot.serviceiot.server.protocol.util.RegistrationMessageUtil;
import com.esiccpro.iot.serviceiot.server.service.MessageService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Qualifier("somethingServerHandler")
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
	
	private Map<ChannelHandlerContext, String> channelToImeiMap = new HashMap<>();

	private MessageService messageService;
	
	@Autowired
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	/**
     * Client connection triggers
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active......");
    }

    /**
     * Client sending message will trigger
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("Server receives message: {}", msg);
       
        ByteBuf inBuffer = (ByteBuf) msg;
        String dumpHex = ByteBufUtil.prettyHexDump(inBuffer);
        
        log.info("Dump Hex {}", dumpHex);
        
        byte[] buf = MessageUtil.getByteArrayForBuffer(inBuffer);

        log.info("raw bytes {}", buf);

		Protocol protocol = null;
		
		if(!this.channelToImeiMap.containsKey(ctx)) {
			log.info("New device connected");
			
			protocol = new TeltonikaProtocol(new HashMap<>());
			protocol.set(Position.IMEI, inBuffer, 2, 15, PositionType.STRING);
			
			log.info("Connected with IMEI {}", (String)protocol.getPositions().get(Position.IMEI));		
			
			this.channelToImeiMap.put(ctx, (String)protocol.getPositions().get(Position.IMEI));
			
			log.info("Send Accept {}", protocol.sendAccept());			
	
			RegistrationMessageUtil.sendByteArray(ctx, protocol.sendAccept());
		}else {
			log.info("Message for IMEI {}", this.channelToImeiMap.get(ctx));
			protocol = new TeltonikaProtocol(new HashMap<>());
			protocol.setImei(this.channelToImeiMap.get(ctx));
			protocol.handle(ctx, inBuffer);

			messageService.processMessage(protocol, new TeltonikaSenderConverter());
			
			protocol.sendAck(ctx, buf);
		}      
    }

    @Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		log.info("channelRegistered");
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		log.info("channelUnregistered");
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		log.info("channelReadComplete");
		super.channelReadComplete(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("channelInactive");
		super.channelInactive(ctx);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		log.info("channelWritabilityChanged");
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.info("exceptionCaught");
		cause.printStackTrace();
		ctx.close();
	}
}