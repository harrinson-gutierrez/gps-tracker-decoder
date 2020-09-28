package com.esiccpro.iot.serviceiot.server.protocol.util;

import java.math.BigInteger;

import io.netty.buffer.ByteBuf;

public class MessageUtil {

	private MessageUtil() {
		
	}
	
	public static long getLongFromByteArray(byte[] msg) {
		String valueString = "";
		for (int i = 0; i < msg.length; i++) {
			byte b = msg[i];
			String binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
			valueString += binaryString;
		}
		return new BigInteger(valueString, 2).longValue();

	}

	public static byte[] getByteArrayForBuffer(ByteBuf buffer) {
		int bufferLength = buffer.readableBytes();
		byte[] byteArray = new byte[bufferLength];
		for (int i = 0; i < bufferLength; i++) {
			byteArray[i] = buffer.getByte(i);
		}
		return byteArray;
	}

	public static String printMsgBinary(byte[] msg) {
		String binaryString = "";
		for (int i = 0; i < msg.length; i++) {
			byte b = msg[i];
			binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		};
		return binaryString;
	}

	public static String printMsgChars(byte[] msg) {
		String msgString = "";
		for (int i = 0; i < msg.length; i++) {
			byte b = msg[i];
			String charString = ("" + (char) b).replaceAll("[^A-Za-z0-9]", "#");
			msgString += charString;
		}
		return msgString;
	}
}
