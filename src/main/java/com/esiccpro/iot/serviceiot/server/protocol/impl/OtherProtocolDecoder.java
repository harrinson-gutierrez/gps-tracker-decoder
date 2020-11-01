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
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@Component
public class OtherProtocolDecoder extends ChannelInboundHandlerAdapter implements ProtocolBaseDecoder {
	
	private Map<ChannelHandlerContext, String> channelToImeiMap = new HashMap<>();

	private MessageService messageService;
	
	private OtherSenderConverter otherSenderConverter;
	
	@Autowired
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	
	@Autowired
	public void setOtherSenderConverter(OtherSenderConverter otherSenderConverter) {
		this.otherSenderConverter = otherSenderConverter;
	}

	/**
     * Client connection triggers
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel active other protocol......");
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
        log.info("Dump Hex {}", ByteBufUtil.hexDump(inBuffer));
        
        byte[] buf = MessageUtil.getByteArrayForBuffer(inBuffer);

        log.info("raw bytes {}", buf);

        Position position = new Position();

		if(!this.channelToImeiMap.containsKey(ctx)) {
			log.info("New device connected otro protocol");
			
			position.set(Position.IMEI, DecoderUtil.getValuePosition(inBuffer, 4, 15, PositionType.STRING));

			
			ByteBuf bufCopyRange = inBuffer.copy(19, 2);

			ByteBuf copy = Unpooled.copiedBuffer(Unpooled.wrappedBuffer(new byte[]{0x02}), bufCopyRange);
			
			log.info("bytes ack {}", ByteBufUtil.prettyHexDump(copy));
			
			this.channelToImeiMap.put(ctx, (String)position.get(Position.IMEI));

			ByteBuf encoded = ctx.alloc().buffer(3);
			encoded.writeBytes(copy);
			ctx.writeAndFlush(encoded);
			
		}else {
			log.info("Message for IMEI {}", this.channelToImeiMap.get(ctx));
			position.set(Position.IMEI,this.channelToImeiMap.get(ctx));
			position = handleData(inBuffer, position);
			
			messageService.processMessage(otherSenderConverter.convert(position.getPositions()));

			ByteBuf ack = inBuffer.copy(148, 2);
			
			ByteBuf copy = Unpooled.copiedBuffer(Unpooled.wrappedBuffer(new byte[]{0x02}), ack);
			
		
			ByteBuf encoded = ctx.alloc().buffer(3);
			encoded.writeBytes(copy);
			log.info("bytes ack {}", ByteBufUtil.prettyHexDump(encoded));
			ctx.writeAndFlush(encoded);
		}      
    }
    
    public Position handleData(ByteBuf inBuffer, Position position) {
    	position.set(Position.PREAMBLE, DecoderUtil.getValuePositionReverse(inBuffer, 0, 1, PositionType.LONG));
    	position.set(Position.LENGHT_DATA, DecoderUtil.getValuePositionReverse(inBuffer, 1, 2, PositionType.LONG));
    	position.set(Position.TAG_4, DecoderUtil.getValuePositionReverse(inBuffer, 3, 1, PositionType.LONG));
    	position.set(Position.ID_SECONDARY, DecoderUtil.getValuePositionReverse(inBuffer, 4, 2, PositionType.LONG));
    	position.set(Position.TAG_10, DecoderUtil.getValuePositionReverse(inBuffer, 6, 1, PositionType.LONG));
    	position.set(Position.NUM_FILE_RECORD, DecoderUtil.getValuePositionReverse(inBuffer, 7, 2, PositionType.LONG));
    	position.set(Position.TAG_20, DecoderUtil.getValuePositionReverse(inBuffer, 9, 1, PositionType.LONG));
    	position.set(Position.TIME, DecoderUtil.getValuePositionReverse(inBuffer, 10, 4, PositionType.DATETIME));
    	position.set(Position.TAG_30, DecoderUtil.getValuePositionReverse(inBuffer, 14, 1, PositionType.LONG));
    	
    	double lat = Double.parseDouble(DecoderUtil.getValuePositionReverse(inBuffer, 16, 4, PositionType.LONG) + "");
    	
    	double latDivided = Double.parseDouble(lat + "") / 1000000;
    	
    	log.info("Lat: {}", latDivided);
    	
    	position.set(Position.LATITUD, latDivided);
    	
    	double lng = Double.parseDouble(DecoderUtil.getValuePositionReverse(inBuffer, 20, 4, PositionType.LONG, true) + "");
    	
    	double lngDivided = Double.parseDouble(lng + "") / 1000000;

    	log.info("Lng: {}", lngDivided);
    
    	position.set(Position.LONGITUD, lngDivided);
    	position.set(Position.TAG_33, DecoderUtil.getValuePositionReverse(inBuffer, 24, 1, PositionType.LONG));
    	position.set(Position.SPEED, DecoderUtil.getValuePositionReverse(inBuffer, 25, 2, PositionType.LONG));
    	position.set(Position.DIRECTION, DecoderUtil.getValuePositionReverse(inBuffer, 27, 2, PositionType.LONG));
    	position.set(Position.TAG_34, DecoderUtil.getValuePositionReverse(inBuffer, 29, 1, PositionType.LONG));
    	position.set(Position.ALTITUDE, DecoderUtil.getValuePositionReverse(inBuffer, 30, 2, PositionType.LONG));
    	return position;
    }
    
	public byte[] sendAccept() {
		return new byte[] { 0x01 };
	}

	public byte[] sendRejected() {
		return new byte[] { 0x00f };
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
	