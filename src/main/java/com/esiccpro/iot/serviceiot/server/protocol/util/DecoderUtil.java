package com.esiccpro.iot.serviceiot.server.protocol.util;

import java.util.Date;

import com.esiccpro.iot.serviceiot.server.model.Position.PositionType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DecoderUtil {
	
	private DecoderUtil() {}
	
	 public static Object getValuePosition(ByteBuf buf, int start, int end, PositionType type) {
			ByteBuf bufCopyRange = buf.copy(start, end);
			String copyDump = ByteBufUtil.prettyHexDump(bufCopyRange);
			
			byte[] byteArray = MessageUtil.getByteArrayForBuffer(bufCopyRange);
			
			switch (type) {
				case LONG:
					long value = MessageUtil.getLongFromByteArray(byteArray);
					log.info("Value: {} Hex: {}", value, copyDump);
					return value;
				case DOUBLE:
					double valueD = MessageUtil.getDoubleFromByteArray(byteArray);
					log.info("Value: {} Hex: {}", valueD, copyDump);
					return valueD;
				case STRING:
					String text = new String(byteArray);				
					log.info("Value: {} Hex: {}", text, copyDump);
					return text;
				case DATETIME:
					Date currentDate = new Date(MessageUtil.getLongFromByteArray(byteArray));
					log.info("Value: {} Hex: {}", currentDate, copyDump);
					return currentDate;
				default:
					break;
				}
			
			return null;
		}
	 
	public static long hexToLong2(String hex) 
	{
		String bin = Long.toString(Long.parseLong(hex, 16), 2);
		
		String binCompl = bin.replace('0', 'X').replace('1', '0').replace('X', '1');
		
		return (Long.parseLong(binCompl, 2) + 1) * -1;
	}
}
