package main;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Util {
	
	private static Properties p;
	public static boolean symetric;
	
	public static String byte2str( byte[] b ) {
		String ret = "";
		for (int i = 0 ; i < b.length ; i++) {
			String g = Integer.toHexString(((char)b[i])&0x00ff);
			ret += (g.length()==1?"0":"") + g;
		}
		return ret;
	}
	
	public static byte[] str2byte( String ss ) {
		byte[] ret = new byte[ss.length()/2];
		for (int i = 0 ; i < ret.length ; i++) {
			ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
		}
		return ret;
	}
	
	public static void initializeProperties() {
		try (InputStream input = new FileInputStream("./data/keys.properties")) {
			p = new Properties();
			p.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SecretKey getSymmetricKey(String property) {
		String key = p.getProperty(property);
		byte[] decodedKey = Base64.getDecoder().decode(key); 
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		return originalKey;
	}
	
	public static SecretKey getAsymmetricKey(String property) {
		String key = p.getProperty(property);
		byte[] decodedKey = Base64.getDecoder().decode(key);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "RSA");
		return originalKey;
	}
}
