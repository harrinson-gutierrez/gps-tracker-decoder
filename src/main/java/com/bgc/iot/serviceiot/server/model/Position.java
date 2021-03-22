package com.bgc.iot.serviceiot.server.model;

import java.util.HashMap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Position {
	
	public enum PositionType {LONG, DOUBLE, DATETIME, STRING}
	
	private HashMap<String, Object> positions;
	
	public Position() {
		this.positions = new HashMap<>();
	}
	
	public void set(String position, Object value) {
		log.info("Position: {} Value: {}", position, value);
		positions.put(position, value);
	}
	
	public Object get(String position) {
		return positions.get(position);
	}

	public static final String PREAMBLE = "PREAMBLE";
	public static final String LENGHT_DATA = "LENGHT_DATA";
	public static final String CODEC = "CODEC";
	public static final String DATA_AVL = "DATA_AVL";
	public static final String TIME = "TIME";
	public static final String PRIORITY = "PRIORITY";
	public static final String LONGITUD = "LONGITUD";
	public static final String LATITUD = "LATITUD";
	public static final String ALTITUDE = "ALTITUDE";
	public static final String IMEI = "IMEI";
	public static final String SPEED = "SPEED";
	public static final String DIRECTION = "DIRECTION";
	
	public static final String ID_SECONDARY = "ID_SECONDARY";
	public static final String NUM_FILE_RECORD = "NUM_FILE_RECORD";
	
	public static final String TAG_4 = "Tag_4";
	public static final String TAG_10 = "Tag_10";
	public static final String TAG_20 = "TAG_20";
	public static final String TAG_30 = "TAG_30";
	public static final String TAG_33 = "TAG_33";
	public static final String TAG_34 = "TAG_34";
	public static final String TAG_35 = "TAG_35";
	
	public static final String SATELITE = "Salite_cordenadas";
}
