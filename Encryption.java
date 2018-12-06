package assignment7;

/*  EE422C Project 7 submission by
 *  2018/Dec/03
 *  <Daniel Schmekel>  
 *  <ds52427>    
 *  <Cole Morgan>  
 *  <cm55332>  
 *  Slip days used: <0>*
 *  Github: https://github.com/EE422C-Fall-2018/project-7-chat-project-7-pair-10/tree/master
 *  
 *  Fall 2018
 *  
 *  Describe here known bugs or issues in this file. 
 *  If your issue spans multiplefiles, or you are not sure about details, add comments to the README.txt file.*/

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Key;

import javax.crypto.Cipher;

public class Encryption {
	public static byte[] encrypt(Key pub, Serializable obj) {
		try {
			Cipher ciph = Cipher.getInstance("RSA");
			ciph.init(Cipher.ENCRYPT_MODE, pub);
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(s);
			o.writeObject(obj);
			return ciph.doFinal(s.toByteArray());
		}
		catch(Exception e) {
			System.out.println("Something went wrong in message encryption.");
		}
		return new byte[1];
	}
	
	public static byte[] decrypt(Key priv, Serializable obj) {
		try {
			Cipher ciph = Cipher.getInstance("RSA");
			ciph.init(Cipher.DECRYPT_MODE, priv);
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(s);
			o.writeObject(obj);
			return ciph.doFinal(s.toByteArray());
		}
		catch(Exception e) {
			System.out.println("Something went wrong in message encryption.");
		}
		return new byte[1];
	}
}
