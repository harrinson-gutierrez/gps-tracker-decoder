package com.bgc.iot.serviceiot.server.protocol.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bgc.iot.serviceiot.server.model.KeyValueObject;
import com.bgc.iot.serviceiot.server.model.Position;
import com.bgc.iot.serviceiot.server.model.Position.PositionType;
import com.bgc.iot.serviceiot.server.protocol.ProtocolBaseDecoder;
import com.bgc.iot.serviceiot.server.protocol.util.DecoderUtil;
import com.bgc.iot.serviceiot.server.protocol.util.MessageUtil;
import com.bgc.iot.serviceiot.server.protocol.util.RegistrationMessageUtil;
import com.bgc.iot.serviceiot.server.service.MessageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	@Autowired
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
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

        Position position = new Position();

		if(!this.channelToImeiMap.containsKey(ctx)) {
			log.info("New device connected");
			
			position.set(Position.IMEI, DecoderUtil.getValuePosition(inBuffer, 2, 15, PositionType.STRING));
			
			this.channelToImeiMap.put(ctx, (String)position.get(Position.IMEI));
			
			log.info("Send Accept {}", sendAccept());			
	
			RegistrationMessageUtil.sendByteArray(ctx, sendAccept());
			
		}else {
			log.info("Message for IMEI {}", this.channelToImeiMap.get(ctx));
			position.set(Position.IMEI,this.channelToImeiMap.get(ctx));
			
			List<Position> positions = handleData(inBuffer, position);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			for (Position pos : positions) {
				String json = gson.toJson(pos.getPositions());
				messageService.processMessage(json);
				log.info("Json: {}", json);
			}

			sendAck(ctx, buf);
		}      
    }
    
    public List<Position> handleData(ByteBuf inBuffer, Position positionTemp){
    	List<Position> positions = new ArrayList<>();
    	
    	positionTemp.set(Position.PREAMBLE, DecoderUtil.getValuePosition(inBuffer, 0, 4, PositionType.LONG));
    	positionTemp.set(Position.LENGHT_DATA, DecoderUtil.getValuePosition(inBuffer, 4, 4, PositionType.LONG));
    	positionTemp.set(Position.CODEC, DecoderUtil.getValuePosition(inBuffer, 8, 1, PositionType.LONG));
    	positionTemp.set(Position.DATA_AVL, DecoderUtil.getValuePosition(inBuffer, 9, 1, PositionType.LONG));
    	
    	int count = inBuffer.readableBytes();
    	log.info("COUNTER {}", count);
    	
    	for(int i=0; i<(long) positionTemp.get(Position.DATA_AVL); i++) {
    		
    		int pos = (int) (10 + (long) positionTemp.get(Position.LENGHT_DATA)  * i);
    		
    		log.info("DATA AVL N° {} REGISTRO {} IN POS {}",(long) positionTemp.get(Position.DATA_AVL), i+1, pos);
    		
    		Position position = positionTemp;
    		
        	position.set(Position.TIME, DecoderUtil.getValuePosition(inBuffer, pos, 8, PositionType.DATETIME));
        	position.set(Position.PRIORITY, DecoderUtil.getValuePosition(inBuffer, pos + 8, 1, PositionType.LONG));
        	
        	double lng = Double.parseDouble(DecoderUtil.getValuePosition(inBuffer, pos + 9, 4, PositionType.LONG, true) + "");
        	
        	double lngDivided = Double.parseDouble(lng + "") / 10000000;

        	log.info("Lng: {}", lngDivided);
        	
        	position.set(Position.LONGITUD, lngDivided);
        	
        	double lat = Double.parseDouble(DecoderUtil.getValuePosition(inBuffer, pos + 13, 4, PositionType.LONG) + "");
        	double latDivided = Double.parseDouble(lat + "") / 10000000;
        	
        	log.info("Lat: {}", latDivided);
        	
        	position.set(Position.LATITUD, latDivided);
        	position.set(Position.ALTITUDE, DecoderUtil.getValuePosition(inBuffer, pos + 17, 2, PositionType.LONG));
        	
        	position.set("ANGLE", DecoderUtil.getValuePosition(inBuffer, pos + 19, 2, PositionType.LONG));
        	
        	position.set("SATELITE", DecoderUtil.getValuePosition(inBuffer, pos + 21, 1, PositionType.LONG));
        	position.set(Position.SPEED, DecoderUtil.getValuePosition(inBuffer, pos + 22, 2, PositionType.LONG));
        	
        	position.set("EVENTS_IO", DecoderUtil.getValuePosition(inBuffer, pos + 24, 2, PositionType.LONG));
        	position.set("N_ID_TOTAL", DecoderUtil.getValuePosition(inBuffer, pos + 26, 2, PositionType.LONG));
        	
        	position.set("N1_IO", DecoderUtil.getValuePosition(inBuffer, pos + 28, 2, PositionType.LONG));
        	
        	int initialIO = 30;
        	
        	List<KeyValueObject> n1IoList = new ArrayList<>();
        	for(int k=0; k<(long)position.get("N1_IO"); k++) {
        		n1IoList.add(
        				new KeyValueObject(
        						DecoderUtil.getValuePosition(inBuffer, pos + initialIO + (k*3), 2, PositionType.LONG),
        						 DecoderUtil.getValuePosition(inBuffer, pos + initialIO + 2 + (k*3), 1, PositionType.LONG)));
        	}
        	
        	position.set("N1_IO_CHILDREN", n1IoList);
        	     	
        	initialIO += (long)position.get("N1_IO") * 3;
        	
        	position.set("N2_IO", DecoderUtil.getValuePosition(inBuffer, pos + initialIO, 2, PositionType.LONG));
        	
        	initialIO += 2;
        	
        	List<KeyValueObject> n2IoList = new ArrayList<>();
        	for(int k=0; k<(long)position.get("N2_IO"); k++) {
        		n2IoList.add(
        				new KeyValueObject(
        						DecoderUtil.getValuePosition(inBuffer, pos + initialIO + (k*4), 2, PositionType.LONG),
        						 DecoderUtil.getValuePosition(inBuffer, pos + initialIO + 2 + (k*4), 2, PositionType.LONG)));
        	}
        	
        	position.set("N2_IO_CHILDREN", n2IoList);
        	
        	initialIO += (long)position.get("N2_IO") * 4;
            	    
        	position.set("N4_IO", DecoderUtil.getValuePosition(inBuffer, pos + initialIO, 2, PositionType.LONG));
        	
        	initialIO += 2;
        	
        	List<KeyValueObject> n4IoList = new ArrayList<>();
        	for(int k=0; k<(long)position.get("N4_IO"); k++) {
        		n4IoList.add(
        				new KeyValueObject(
        						DecoderUtil.getValuePosition(inBuffer, pos + initialIO + (k*6), 2, PositionType.LONG),
        						 DecoderUtil.getValuePosition(inBuffer, pos + initialIO + 2 + (k*6), 4, PositionType.LONG)));
        	}
        	
        	position.set("N4_IO_CHILDREN", n4IoList);
        	
        	initialIO += (long)position.get("N4_IO") * 6;
        	
        	position.set("N8_IO", DecoderUtil.getValuePosition(inBuffer, pos + initialIO, 2, PositionType.LONG));
        	position.set("NX_IO", DecoderUtil.getValuePosition(inBuffer, pos + initialIO + 2, 2, PositionType.LONG));
        	position.set("AVL_LENGHT_END", DecoderUtil.getValuePosition(inBuffer, pos + initialIO + 4, 1, PositionType.LONG));
        	
        	position.set("CRC-16", DecoderUtil.getValuePosition(inBuffer, pos + initialIO + 5, 4, PositionType.LONG));
        	
        	positions.add(position);
    	}
   
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
	