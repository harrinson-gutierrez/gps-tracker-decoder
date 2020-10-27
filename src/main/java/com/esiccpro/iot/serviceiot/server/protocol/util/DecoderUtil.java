package com.esiccpro.iot.serviceiot.server.protocol.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.esiccpro.iot.serviceiot.server.model.Position.PositionType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DecoderUtil {

	private DecoderUtil() {
	}

	public static String dumpHex(ByteBuf buf, int start, int lenght) {
		ByteBuf bufCopyRange = buf.copy(start, lenght);
		String copyDump = ByteBufUtil.prettyHexDump(bufCopyRange);

		log.info("Hex: {}", copyDump);

		return ByteBufUtil.hexDump(bufCopyRange);
	}

	public static Object convertHexToObject(String dumpHex, PositionType type, boolean comp2) {
		try {
			switch (type) {
			case LONG:
				return comp2 ? hexToLong2(dumpHex) : Long.parseLong(dumpHex, 16);
			case STRING:
				return new String(Hex.decodeHex(dumpHex.toCharArray()), StandardCharsets.UTF_8);
			case DATETIME:
				log.info("datetime value {}", Long.parseLong(dumpHex, 16));
				return new Date(Long.parseLong(dumpHex, 16));
			default:
				break;
			}
		} catch (DecoderException e) {
			return null;
		}

		return null;
	}

	public static Object getValuePosition(ByteBuf buf, int start, int lenght, PositionType type) {
		log.info("PositionType: {}", type);

		String dumpHex = dumpHex(buf, start, lenght);

		return convertHexToObject(dumpHex, type, false);
	}
	
	public static Object getValuePosition(ByteBuf buf, int start, int lenght, PositionType type, boolean com2) {
		log.info("PositionType: {}", type);

		String dumpHex = dumpHex(buf, start, lenght);

		return convertHexToObject(dumpHex, type, com2);
	}

	public static Object getValuePositionReverse(ByteBuf buf, int start, int lenght, PositionType type) {
			log.info("PositionType: {}", type);

			String dumpHex = dumpHex(buf, start, lenght);
			
			log.info("dumpHex: {}", dumpHex);
			
			dumpHex = hexToReverse(dumpHex);
			
			log.info("reversed: {}", dumpHex);
			
			return convertHexToObject(dumpHex, type, false);
		}
	
	public static Object getValuePositionReverse(ByteBuf buf, int start, int lenght, PositionType type, boolean com2) {
		log.info("PositionType: {}", type);

		String dumpHex = dumpHex(buf, start, lenght);
		
		log.info("dumpHex: {}", dumpHex);
		
		dumpHex = hexToReverse(dumpHex);
		
		log.info("reversed: {}", dumpHex);
		
		return convertHexToObject(dumpHex, type, com2);
	}

	public static String hexToReverse(String hex) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i <= hex.length() - 2; i = i + 2) {
			result.append(new StringBuilder(hex.substring(i, i + 2)).reverse());
		}
		return result.reverse().toString();
	}

	public static long hexToLong2(String hex) {
		String bin = Long.toString(Long.parseLong(hex, 16), 2);
		
		String binCompl = bin.replace('0', 'X').replace('1', '0').replace('X', '1');
		
		return (Long.parseLong(binCompl, 2) + 1) * -1;
	}

}
