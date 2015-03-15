package it.polimi.core;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



public class Security {
	//simmetric key
	private SecretKey key;
	private Cipher chiper = null;
	
	public Security(){
		KeyGenerator gen = null;
		try {
			gen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		//generating the simmetric key
		gen.init(192);
		this.key = gen.generateKey();
		try {
			//creating the chiper
			chiper = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	public void setSecurity(byte[] bytes){
		Key key = new SecretKeySpec(bytes, "AES");
		this.key = (SecretKey) key;
		try {
		//creating the chiper
			chiper = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	private byte[] encrypt(byte[] bytes,SecretKey key){
		byte[] encByte = null;
		try {
			chiper.init(Cipher.ENCRYPT_MODE, key);
			//ecrypt the message 
			encByte = chiper.doFinal(bytes);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
		return encByte;
	}

	public byte[] encrypt(String msg){
		byte[] bytes=null;
		try {
			bytes=encrypt(msg.getBytes("UTF-8"),this.key);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return bytes;
	}
	public byte[] encrypt(byte[] bytes){
		return encrypt(bytes,this.key);
	}
	
	public String decrypt(byte[] msg){ 
		String returned="";
		try {
			return new String(decrypt(msg,this.key),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return returned;
	}
	public byte[] decryptByte(byte[] msg){
		return decrypt(msg,this.key);
	}
	private byte[] decrypt(byte[] msg,SecretKey key){
		byte[] encByte=null;
		try {
			chiper.init(Cipher.DECRYPT_MODE, key);	
			//decrypt the message
			encByte = chiper.doFinal(msg);		
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} 
		return encByte;
	}
	
	public SecretKey getKey(){
		return this.key;
	}

}
