package com.esiccpro.iot.serviceiot.server.protocol;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esiccpro.iot.serviceiot.server.model.Position.PositionType;
import com.esiccpro.iot.serviceiot.server.protocol.util.MessageUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

@Data
public abstract class Protocol {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(Protocol.class);
	
	private String imei;
	
	private final Map<String, Object> positions; 
	
	private void setPosition(String position, Object value) {
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
			case DOUBLE:
				double valueD = MessageUtil.getDoubleFromByteArray(byteArray);
				setPosition(position, valueD);
				
				LOGGER.info("Position: {} Value: {} Hex: {}",position, valueD, copyDump);
				break;
			case STRING:
				String text = new String(byteArray);
				setPosition(position, text);
				
				LOGGER.info("Position: {} Value: {} Hex: {}",position, text, copyDump);
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
	
	public abstract byte[] sendAccept();
	
	public abstract byte[] sendRejected();
	
	public abstract void handle(ChannelHandlerContext ctx, ByteBuf inBuffer);
	
	public abstract void sendAck(ChannelHandlerContext ctx, byte[] msg);
}
