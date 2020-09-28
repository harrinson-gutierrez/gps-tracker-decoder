package com.esiccpro.iot.serviceiot.server.convert;

public interface IConvertToMessage<T, U> {
	U convert(T base);
}
