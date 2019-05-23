package com.example.better.better1933.Model;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class DBValue {
	public String strValue;
	public String type;
	private DBValue(){
	}
	public static DBValue parseFromCustomValue(String value, String type){
		DBValue newDBValue = new DBValue();
		newDBValue.type = type;
		newDBValue.strValue = value;
		return newDBValue;
	}
	public static DBValue parseFromCustomValue(String value, Class type){
		return parseFromCustomValue(value, type.getName());
	}
	public DBValue(String value){
		this.type = String.class.getName();
		this.strValue = ConvertToBase64(value);
	}
	public DBValue(Date value){
		this.type = Date.class.getName();
		this.strValue = Long.toString( value.getTime());
	}
	public DBValue(int value){
		this.type = int.class.getName();
		this.strValue = Integer.toString( value);
	}
	public DBValue(float value){
		this.type = float.class.getName();
		this.strValue = Float.toString( value);
	}
	public DBValue(double value){
		this.type = double.class.getName();
		this.strValue = Double.toString( value);
	}
	public DBValue(boolean value){
		this.type = boolean.class.getName();
		this.strValue = Boolean.toString(value);
	}
	public Object GetValue() {
		try {
			switch (type) {
				case "java.lang.String":
					return new String(Base64.decode(strValue, Base64.DEFAULT), StandardCharsets.UTF_8);
				case "java.util.Date":
					return new Date(Long.parseLong(strValue));
				case "int":
					return Integer.parseInt(strValue);
				case "float":
					return Float.parseFloat(strValue);
				case "double":
					return Double.parseDouble(strValue);
				case "boolean":
					return Boolean.parseBoolean(strValue);
				default:
					return strValue;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	private String ConvertToBase64(String str){
		return Base64.encodeToString(str.getBytes(),Base64.DEFAULT);
	}

}
