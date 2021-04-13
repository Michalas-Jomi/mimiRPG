package me.jomi.mimiRPG.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersistentDataTypeCustom {
	public static final PersistentDataType<byte[], String[]> stringArray = new PersistentDataType<byte[], String[]>() {
		@Override public Class<String[]> getComplexType() { return String[].class; }
		@Override public Class<byte[]> getPrimitiveType() { return byte[].class; }

		@Override
		public byte[] toPrimitive(String[] complex, PersistentDataAdapterContext context) {
	        ByteArrayOutputStream b = new ByteArrayOutputStream();
	        DataOutputStream out = new DataOutputStream(b);
	        
	        try {
	        	out.writeShort(complex.length);
	        	for (int i = 0; i < complex.length; i++)
					out.writeUTF(complex[i]);
	        } catch (IOException e) {
				e.printStackTrace();
			}
			
	        return b.toByteArray();
		}
		@Override
		public String[] fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
	        ByteArrayInputStream b = new ByteArrayInputStream(primitive);
	        DataInputStream in = new DataInputStream(b);

	        String[] w = null;
	        
	        try {
				int len = in.readShort();
				w = new String[len];
				for (int i=0; i < len; i++)
					w[i] = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	        return w;
		}
	};
	
	public static final PersistentDataType<String, JSONObject> json = new PersistentDataType<String, JSONObject>() {
		@Override public Class<JSONObject> getComplexType() { return JSONObject.class; }
		@Override public Class<String> getPrimitiveType() 			 { return String.class; }

		@Override
		public String toPrimitive(JSONObject complex, PersistentDataAdapterContext context) {
			return complex.toJSONString();
		}
		@Override
		public JSONObject fromPrimitive(String primitive, PersistentDataAdapterContext context) {
			try {
				return (JSONObject) new JSONParser().parse(primitive);
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
	};
}
