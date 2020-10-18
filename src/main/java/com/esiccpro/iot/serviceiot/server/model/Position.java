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
	public static final String SPEED = "speed";
	public static final String DIRECTION = "direction";
	
	public static final String ID_SECONDARY = "ID_SECONDARY";
	public static final String NUM_FILE_RECORD = "NUM_FILE_RECORD";
	
	public static final String TAG_4 = "Tag_4";
	public static final String TAG_10 = "Tag_10";
	public static final String TAG_20 = "TAG_20";
	public static final String TAG_30 = "TAG_30";
	public static final String TAG_33 = "TAG_33";
	public static final String TAG_34 = "TAG_34";
}
