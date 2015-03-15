package it.polimi.core;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;



public class RSA {
	
	private KeyPair kp;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	public RSA(){
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			this.kp = kpg.genKeyPair();
			this.publicKey = kp.getPublic();
			this.privateKey = kp.getPrivate();			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/*encrypt with its own public key*/
	public  byte[] encrypt(byte[] msg){
		return this.encrypt(msg, this.publicKey);
	}
	
	/*encrypt with passed public key*/
	public byte[] encrypt(byte[] msg, PublicKey key){
		byte[] encByte = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			//encrypt the message
			encByte = cipher.doFinal(msg);	
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return encByte;
	}
	
	public byte[] encrypt(int number, PublicKey key){
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((number >>> offset) & 0xFF);
        }
		return encrypt(b,key);
	}
	
	public byte[] decrypt(byte[] msg){
		byte[] decByte=null;
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
			//decrypt the message
			decByte = cipher.doFinal(msg);	
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
		return decByte;
	}
	public byte[] decrypt(String msg){
		int length=msg.length(),i;
		byte[] bytes=new byte[length];
		for(i=0;i<length;i++){
			bytes[i]=(byte)msg.charAt(i);
		}
		return decrypt(bytes);
	}
	

	public PublicKey getPublicKey() {
		return publicKey;
	}
	
}
