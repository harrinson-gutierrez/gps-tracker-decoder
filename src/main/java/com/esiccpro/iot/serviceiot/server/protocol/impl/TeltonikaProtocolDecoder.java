package com.esiccpro.iot.serviceiot.server.protocol.impl;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esiccpro.iot.serviceiot.server.model.Position;
import com.esiccpro.iot.serviceiot.server.model.Position.PositionType;
import com.esiccpro.iot.serviceiot.server.protocol.ProtocolBaseDecoder;
import com.esiccpro.iot.serviceiot.server.protocol.util.DecoderUtil;
import com.esiccpro.iot.serviceiot.server.protocol.util.MessageUtil;
import com.esiccpro.iot.serviceiot.server.protocol.util.RegistrationMessageUtil;
import com.esiccpro.iot.serviceiot.server.service.MessageService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@Component
public class TeltonikaProtocolDecoder extends ChannelInboundHandlerAdapter implements ProtocolBaseDecoder {
	
	private Map<ChannelHandlerContext, String> channelToImeiMap = new HashMap<>();

	private MessageService messageService;
	
	private TeltonikaSenderConverter teltonikaSenderConverter;
	
	@Autowired
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	
	@Autowired
	public void setTeltonikaSenderConverter(TeltonikaSenderConverter teltonikaSenderConverter) {
		this.teltonikaSenderConverter = teltonikaSenderConverter;
	}

	/**
     * Client connection triggers
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active teltonika......");
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

        Map<String, Object> positions = new HashMap<>();

		if(!this.channelToImeiMap.containsKey(ctx)) {
			log.info("New device connected");
			
			positions.put(Position.IMEI, DecoderUtil.getValuePosition(inBuffer, 2, 15, PositionType.STRING));
			
			this.channelToImeiMap.put(ctx, (String)positions.get(Position.IMEI));
			
			log.info("Send Accept {}", sendAccept());			
	
			RegistrationMessageUtil.sendByteArray(ctx, sendAccept());
			
		}else {
			log.info("Message for IMEI {}", this.channelToImeiMap.get(ctx));
			positions.put(Position.IMEI,this.channelToImeiMap.get(ctx));
			positions = handleData(inBuffer, positions);
			
			messageService.processMessage(teltonikaSenderConverter.convert(positions));

			sendAck(ctx, buf);
		}      
    }
    
    public Map<String, Object> handleData(ByteBuf inBuffer, Map<String, Object> positions){
    	positions.put(Position.PREAMBLE, DecoderUtil.getValuePosition(inBuffer, 0, 4, PositionType.LONG));
    	positions.put(Position.LENGHT_DATA, DecoderUtil.getValuePosition(inBuffer, 4, 4, PositionType.LONG));
    	positions.put(Position.CODEC, DecoderUtil.getValuePosition(inBuffer, 8, 1, PositionType.LONG));
    	positions.put(Position.DATA_AVL, DecoderUtil.getValuePosition(inBuffer, 9, 1, PositionType.LONG));
    	positions.put(Position.TIME, DecoderUtil.getValuePosition(inBuffer, 10, 8, PositionType.DATETIME));
    	positions.put(Position.PRIORITY, DecoderUtil.getValuePosition(inBuffer, 18, 1, PositionType.LONG));
    	
    	long lng = DecoderUtil.hexToLong2(ByteBufUtil.hexDump(inBuffer.copy(19, 4)));
    	
    	double lngDivided = Double.parseDouble(lng + "") / 10000000;

    	log.info("Lng: {}", lngDivided);
    	
    	positions.put(Position.LONGITUD, lngDivided);
    	
    	double lat = Double.parseDouble(DecoderUtil.getValuePosition(inBuffer, 23, 4, PositionType.LONG) + "");
    	double latDivided = Double.parseDouble(lat + "") / 10000000;
    	
    	log.info("Lat: {}", latDivided);
    	
    	positions.put(Position.LATITUD, latDivided);
    	positions.put(Position.ALTITUDE, DecoderUtil.getValuePosition(inBuffer, 27, 2, PositionType.LONG));
    	return positions;
    }
    
    
	public byte[] sendAccept() {
		return new byte[] { 0x01 };
	}

	public byte[] sendRejected() {
		return new byte[] { 0x00f };
	}

	public void sendAck(ChannelHandlerContext ctx, byte[] msg) {
		int decimal = Integer.parseInt(Integer.toBinaryString(msg[9] & 0xFF).replace(' ', '0'), 2);
		ByteBuf encoded = ctx.alloc().buffer(4);
		encoded.writeInt(decimal);
		ctx.writeAndFlush(encoded);
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
	