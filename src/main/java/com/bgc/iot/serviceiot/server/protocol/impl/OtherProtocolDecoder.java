package com.bgc.iot.serviceiot.server.protocol.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bgc.iot.serviceiot.server.model.Position;
import com.bgc.iot.serviceiot.server.model.Position.PositionType;
import com.bgc.iot.serviceiot.server.protocol.ProtocolBaseDecoder;
import com.bgc.iot.serviceiot.server.protocol.util.DecoderUtil;
import com.bgc.iot.serviceiot.server.protocol.util.MessageUtil;
import com.bgc.iot.serviceiot.server.service.MessageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
	
	@Autowired
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
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
        
        log.debug("Dump Hex {}", dumpHex);
        log.debug("Dump Hex {}", ByteBufUtil.hexDump(inBuffer));
        
        byte[] buf = MessageUtil.getByteArrayForBuffer(inBuffer);

        log.debug("raw bytes {}", buf);

        Position position = new Position();
   
		if(!this.channelToImeiMap.containsKey(ctx)) {
			log.debug("New device connected otro protocol");
			
			position.set(Position.IMEI, DecoderUtil.getValuePosition(inBuffer, 4, 15, PositionType.STRING));

			ByteBuf bufCopyRange = inBuffer.copy(19, 2);

			ByteBuf copy = Unpooled.copiedBuffer(Unpooled.wrappedBuffer(new byte[]{0x02}), bufCopyRange);
			
			log.info("bytes ack {}", ByteBufUtil.prettyHexDump(copy));
			
			this.channelToImeiMap.put(ctx, (String)position.get(Position.IMEI));

			ByteBuf encoded = ctx.alloc().buffer(3);
			encoded.writeBytes(copy);
			ctx.writeAndFlush(encoded);
			
		}else {	
			log.debug("Message for IMEI {}", this.channelToImeiMap.get(ctx));
			position.set(Position.IMEI,this.channelToImeiMap.get(ctx));
			
			List<Position> positions = handleData(inBuffer, position);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			for (Position pos : positions) {
				String json = gson.toJson(pos.getPositions());
				messageService.processMessage(json);
				log.info("Json: {}", json);
			}

			log.debug("READABLE BYTES {}", inBuffer.readableBytes());
			ByteBuf ack = inBuffer.copy(inBuffer.readableBytes() - 3, 2);
			
			log.debug("Hex dump {}", ByteBufUtil.prettyHexDump(inBuffer));
			
			ByteBuf copy = Unpooled.copiedBuffer(Unpooled.wrappedBuffer(new byte[]{0x02}), ack);
			
			ByteBuf encoded = ctx.alloc().buffer(3);
			encoded.writeBytes(copy);
			log.info("bytes ack {}", ByteBufUtil.prettyHexDump(encoded));
			ctx.writeAndFlush(encoded);
		}      
    }
    
    public List<Position> handleData(ByteBuf inBuffer, Position positionTemp) {
    	
    	List<Position> positions = new ArrayList<>();
    	
    	positionTemp.set(Position.PREAMBLE, DecoderUtil.getValuePositionReverse(inBuffer, 0, 1, PositionType.LONG));
    	positionTemp.set(Position.LENGHT_DATA, DecoderUtil.getValuePositionReverse(inBuffer, 1, 2, PositionType.LONG));
    	
    	int lenghtBytes =  inBuffer.readableBytes();
    	
		log.debug("Lenght byteBuf {}", lenghtBytes);
		
		int countData = (lenghtBytes - 5) / 145;
		
		log.debug("Trazas {}", countData);
		
		for(int i=0; i<countData; i++) {
			int posRange = 3 + (i * 145);
			
			log.debug("DATA AVL N° REGISTRO {} IN POS {}", i+1, posRange);
			
			Position position = positionTemp;
			
			position.set(Position.TAG_4, DecoderUtil.getValuePositionReverse(inBuffer, posRange, 1, PositionType.LONG));
	    	position.set(Position.ID_SECONDARY, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 1, 2, PositionType.LONG));
	    	position.set(Position.TAG_10, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 3, 1, PositionType.LONG));
	    	position.set(Position.NUM_FILE_RECORD, DecoderUtil.getValuePositionReverse(inBuffer,posRange + 4, 2, PositionType.LONG));
	    	position.set(Position.TAG_20, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 6, 1, PositionType.LONG));
	    	
	    	long datetime = (long) DecoderUtil.getValuePositionReverse(inBuffer, posRange + 7, 4, PositionType.LONG);
	    	position.set(Position.TIME, new Date(datetime * 1000));	
	    	
	    	position.set(Position.TAG_30, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 11, 1, PositionType.LONG));
	    	position.set(Position.SATELITE, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 12, 1, PositionType.LONG));
	    	
	    	double lat = Double.parseDouble(DecoderUtil.getValuePositionReverse(inBuffer, posRange + 13, 4, PositionType.LONG) + "");
	    	
	    	double latDivided = Double.parseDouble(lat + "") / 1000000;
	    	
	    	log.debug("Lat: {}", latDivided);
	    	
	    	position.set(Position.LATITUD, latDivided);
	    	
	    	double lng = Double.parseDouble(DecoderUtil.getValuePositionReverse(inBuffer, posRange + 17, 4, PositionType.LONG, true) + "");
	    	
	    	double lngDivided = Double.parseDouble(lng + "") / 1000000;

	    	log.debug("Lng: {}", lngDivided);
	    
	    	position.set(Position.LONGITUD, lngDivided);
	    	position.set(Position.TAG_33, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 21, 1, PositionType.LONG));
	    	position.set(Position.SPEED, (long)DecoderUtil.getValuePositionReverse(inBuffer, posRange + 22, 2, PositionType.LONG) / 10);
	    	position.set(Position.DIRECTION, (long)DecoderUtil.getValuePositionReverse(inBuffer, posRange + 24, 2, PositionType.LONG) / 10);
	    	position.set(Position.TAG_34, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 26, 1, PositionType.LONG));
	    	position.set(Position.ALTITUDE, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 27, 2, PositionType.LONG));
	    	position.set(Position.TAG_35, DecoderUtil.getValuePositionReverse(inBuffer, posRange + 29, 1, PositionType.LONG));
	    	position.set("HDOP", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 30, 1, PositionType.LONG));
	    	position.set("TAG_40", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 31, 1, PositionType.LONG));
	    	
	    	position.set("DEVICE_STATUS", getDeviceStatus(inBuffer, posRange));
	    	
	    	position.set("TAG_41", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 34, 1, PositionType.LONG));
	    	position.set("POWER_SUPPLY", (long) DecoderUtil.getValuePositionReverse(inBuffer, posRange + 35, 2, PositionType.LONG) / 1000);
	    	position.set("TAG_42", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 37, 1, PositionType.LONG));
	    	position.set("BATERY_VOLTAGE", (long) DecoderUtil.getValuePositionReverse(inBuffer, posRange + 38, 2, PositionType.LONG) / 1000);
	    	position.set("TAG_47", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 40, 1, PositionType.LONG));
	    	position.set("ACCELERATION", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 41, 1, PositionType.LONG));
	    	position.set("BRAKING", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 42, 1, PositionType.LONG));
	    	position.set("CURVE_ASCELERATION", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 43, 1, PositionType.LONG));
	    	position.set("STRONG_PUNCH", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 44, 1, PositionType.LONG));
	    	position.set("TAG_50", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 45, 1, PositionType.LONG));
	    	position.set("VOLTAGE_INO", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 46, 2, PositionType.LONG));
	    	position.set("TAG_51", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 48, 1, PositionType.LONG));
	    	position.set("VOLTAGE_IN1", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 49, 2, PositionType.LONG));
	    	position.set("TAG_A1", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 51, 1, PositionType.LONG)); 	
	    	position.set("CAN8_BITR16", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 52, 1, PositionType.LONG));
	    	position.set("TAG_A2", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 53, 1, PositionType.LONG));
	    	position.set("CAN8_BITR17", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 54, 1, PositionType.LONG));
	    	position.set("TAG_A3", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 55, 1, PositionType.LONG));
	    	position.set("CAN8_BITR18", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 56, 1, PositionType.LONG));
	    	position.set("TAG_A4", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 57, 1, PositionType.LONG));
	    	position.set("CAN8_BITR19", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 58, 1, PositionType.LONG));
	    	position.set("TAG_A5", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 59, 1, PositionType.LONG));
	    	position.set("CAN8_BITR20", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 60, 1, PositionType.LONG));
	    	position.set("TAG_A6", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 61, 1, PositionType.LONG));
	    	position.set("CAN8_BITR21", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 62, 1, PositionType.LONG));
	    	position.set("TAG_A7", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 63, 1, PositionType.LONG));
	    	position.set("CAN8_BITR22", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 64, 1, PositionType.LONG));
	    	position.set("TAG_B0", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 65, 1, PositionType.LONG));
	    	position.set("CAN16_BITR5", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 66, 2, PositionType.LONG));
	    	position.set("TAG_B1", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 68, 1, PositionType.LONG));
	    	position.set("CAN16_BITR6", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 69, 2, PositionType.LONG));
	    	position.set("TAG_B2", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 71, 1, PositionType.LONG));
	    	position.set("CAN16_BITR7", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 72, 2, PositionType.LONG));
	    	position.set("TAG_B3", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 74, 1, PositionType.LONG));
	    	position.set("CAN16_BITR8", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 75, 2, PositionType.LONG));
	    	position.set("TAG_B4", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 77, 1, PositionType.LONG));
	    	position.set("CAN16_BITR9", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 78, 2, PositionType.LONG));
	    	position.set("TAG_B5", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 80, 1, PositionType.LONG));
	    	position.set("CAN16_BITR10", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 81, 2, PositionType.LONG));
	    	position.set("TAG_B6", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 83, 1, PositionType.LONG));
	    	position.set("CAN16_BITR11", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 84, 2, PositionType.LONG));
	    	position.set("TAG_B7", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 86, 1, PositionType.LONG));
	    	position.set("CAN16_BITR12", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 87, 2, PositionType.LONG));
	    	position.set("TAG_B8", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 89, 1, PositionType.LONG));
	    	position.set("CAN16_BITR13", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 90, 2, PositionType.LONG));
	    	position.set("TAG_B9", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 92, 1, PositionType.LONG));
	    	position.set("CAN16_BITR14", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 93, 2, PositionType.LONG));
	    	
	    	position.set("TAG_C4", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 95, 1, PositionType.LONG));
	    	position.set("CAN8_BITR0", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 96, 1, PositionType.LONG));
	    	position.set("TAG_C5", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 97, 1, PositionType.LONG));
	    	position.set("CAN8_BITR1", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 98, 1, PositionType.LONG));
	    	position.set("TAG_C6", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 99, 1, PositionType.LONG));
	    	position.set("CAN8_BITR2", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 100, 1, PositionType.LONG));
	    	position.set("TAG_C7", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 101, 1, PositionType.LONG));
	    	position.set("CAN8_BITR3", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 102, 1, PositionType.LONG));
	    	position.set("TAG_C8", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 103, 1, PositionType.LONG));
	    	position.set("CAN8_BITR4", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 104, 1, PositionType.LONG));
	    	position.set("TAG_C9", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 105, 1, PositionType.LONG));
	    	position.set("CAN8_BITR5", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 106, 1, PositionType.LONG));

	    	position.set("TAG_CA", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 107, 1, PositionType.LONG));
	    	position.set("CAN8_BITR6", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 108, 1, PositionType.LONG));
	    	position.set("TAG_CB", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 109, 1, PositionType.LONG));
	    	position.set("CAN8_BITR7", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 110, 1, PositionType.LONG));
	    	position.set("TAG_CC", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 111, 1, PositionType.LONG));
	    	position.set("CAN8_BITR8", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 112, 1, PositionType.LONG));
	    	position.set("TAG_CD", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 113, 1, PositionType.LONG));
	    	position.set("CAN8_BITR9", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 114, 1, PositionType.LONG));
	    	position.set("TAG_CE", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 115, 1, PositionType.LONG));
	    	position.set("CAN8_BITR10", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 116, 1, PositionType.LONG));
	    	position.set("TAG_CF", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 117, 1, PositionType.LONG));
	    	position.set("CAN8_BITR11", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 118, 1, PositionType.LONG));
	    	position.set("TAG_D0", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 119, 1, PositionType.LONG));
	    	position.set("CAN8_BITR12", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 120, 1, PositionType.LONG));
	    	position.set("TAG_D1", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 121, 1, PositionType.LONG));
	    	position.set("CAN8_BITR13", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 122, 1, PositionType.LONG));
	    	position.set("TAG_D2", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 123, 1, PositionType.LONG));
	    	position.set("CAN8_BITR14", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 124, 1, PositionType.LONG));
	    	
	    	position.set("TAG_D6", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 125, 1, PositionType.LONG));
	    	position.set("CAN16_BITR0", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 126, 2, PositionType.LONG));
	    	position.set("TAG_D7", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 128, 1, PositionType.LONG));
	    	position.set("CAN16_BITR1", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 129, 2, PositionType.LONG));
	    	position.set("TAG_D8", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 131, 1, PositionType.LONG));
	    	position.set("CAN16_BITR2", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 132, 2, PositionType.LONG));
	    	position.set("TAG_D9", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 134, 1, PositionType.LONG));
	    	position.set("CAN16_BITR3", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 135, 2, PositionType.LONG));
	    	
	    	position.set("TAG_DA", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 137, 1, PositionType.LONG));
	    	position.set("CAN16_BITR4", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 138, 2, PositionType.LONG));
	    	
	    	position.set("TAG_DB", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 140, 1, PositionType.LONG));
	    	position.set("CAN32_BITR0", DecoderUtil.getValuePositionReverse(inBuffer, posRange + 141, 4, PositionType.LONG));
	    	
	    	positions.add(position);
		}

    	return positions;
    }
    
    public Map<String, Object> getDeviceStatus(ByteBuf inBuffer, int posRange) {
    	String hexDeviceStatus1 = DecoderUtil.hexToReverse(DecoderUtil.dumpHex(inBuffer, posRange + 32, 1));
    	String bytesDeviceStatus1 = DecoderUtil.hexToBinary(hexDeviceStatus1);
    
    	log.info("Bytes Device status {}", bytesDeviceStatus1);
    	
    	String hexDeviceStatus2 = DecoderUtil.hexToReverse(DecoderUtil.dumpHex(inBuffer, posRange + 33, 1));
    	String bytesDeviceStatus2 = DecoderUtil.hexToBinary(hexDeviceStatus2);
    
    	log.info("Bytes Device status 2 {}", bytesDeviceStatus2);
    	
    	char[] result = (bytesDeviceStatus1 + bytesDeviceStatus2).toCharArray();
     	  	
    	log.info("Bytes Device status result {}", result);
    	
    	HashMap<String, Object> deviceStatusHash = new HashMap<>();
    	
    	deviceStatusHash.put("BIT_0", result[0] == '0'? "Parking" : "Mov");
    	deviceStatusHash.put("BIT_1", result[1] == '0' ? "Dentro permitido": "Exede permitido");	
    	deviceStatusHash.put("BIT_2", result[2] == '0' ? "Botton" : "");
    	deviceStatusHash.put("BIT_3", result[3] == '0' ? "SIM Card ok" : "");
    	deviceStatusHash.put("BIT_4", result[4] == '0' ? "Fuera" : "Dentro");
    	deviceStatusHash.put("BIT_5", result[5] == '0' ? "Normal" : "< 3.7V");
    	deviceStatusHash.put("BIT_6", result[6] == '0' ? "Conectado" : "Desconectado");
    	deviceStatusHash.put("BIT_7", result[7] == '0' ? "Normal" : "Fuera normal");
    	deviceStatusHash.put("BIT_8", result[8] == '0' ? "Normal" : "Fuera normal");
    	deviceStatusHash.put("BIT_9", result[9] == '0' ? "Detenido" : "Iniciado");
    	deviceStatusHash.put("BIT_10", result[10] == '0' ? "Normal" : "Golpe");
    	deviceStatusHash.put("BIT_11", result[11] == '0' ? "Disp Exter" : "Disp GPS Track");
    	deviceStatusHash.put("BIT_12", Integer.parseInt(bytesDeviceStatus1 + bytesDeviceStatus2, 2));
    	deviceStatusHash.put("BIT_13", result[13]);
    	deviceStatusHash.put("BIT_14", result[14] == '0' ? "Off" : "ON");
    	deviceStatusHash.put("BIT_15", result[15] == '0' ? "No alarm" : "Activada");
    	
    	return deviceStatusHash;
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
	