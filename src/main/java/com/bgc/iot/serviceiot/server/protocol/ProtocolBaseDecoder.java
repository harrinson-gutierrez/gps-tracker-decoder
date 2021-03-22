package com.bgc.iot.serviceiot.server.protocol;

public interface ProtocolBaseDecoder {

	byte[] sendAccept();

	byte[] sendRejected();
}
