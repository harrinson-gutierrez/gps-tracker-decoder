package com.esiccpro.iot.serviceiot.server.protocol.impl;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esiccpro.iot.serviceiot.server.model.Position.PositionType;
import com.esiccpro.iot.serviceiot.server.protocol.util.MessageUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Data;

@Data
public abstract class Protocol {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(Protocol.class);
	
	public final Map<String, Object> positions; 
	
	private void setPosition(String position, long value) {
		this.positions.put(position, value);
	}
	
	private void setPosition(String position, Date value) {
		this.positions.put(position, value);
	}
	
	public void set(String position, ByteBuf buf, int start, int end, PositionType type) {
		ByteBuf bufCopyRange = buf.copy(start, end);
		String copyDump = ByteBufUtil.prettyHexDump(bufCopyRange);
		
		byte[] byteArray = MessageUtil.getByteArrayForBuffer(bufCopyRange);
		
		switch (type) {
			case LONG:
				long value = MessageUtil.getLongFromByteArray(byteArray);
				setPosition(position, value);
				
				LOGGER.info("Position: {} Value: {} Hex: {}",position, value, copyDump);
				break;
			case DATETIME:
				long dateTime = MessageUtil.getLongFromByteArray(byteArray);
				Date currentDate = new Date(dateTime);
				setPosition(position, currentDate);
				
				LOGGER.info("Position: {} Value: {} Hex: {}",position, currentDate, copyDump);
				break;
			default:
				break;
			}
	}
}
