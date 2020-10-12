package com.esiccpro.iot.serviceiot.server.model;

public final class Position {
	
	public enum PositionType {LONG, DOUBLE, DATETIME, STRING}
	
	private Position() {}
	
	public static final String PREAMBLE = "Preambulo";
	public static final String LENGHT_DATA = "Longitude";
	public static final String CODEC = "Codec";
	public static final String DATA_AVL = "Data avl";
	public static final String TIME = "Time";
	public static final String PRIORITY = "Prioridad";
	public static final String LONGITUD = "Longitud";
	public static final String LATITUD = "Latitud";
	public static final String ALTITUDE = "Altitude";
	public static final String IMEI = "Imei";
}
