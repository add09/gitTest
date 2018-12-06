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

import java.io.Serializable;
import java.sql.Time;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String msg;
	private String user;
	private String channelName;
	private Time time;
	
	public Message(String message, String username, String channelName, Time time) {
		this.msg = message;
		this.user = username;
		this.channelName = channelName;
		this.time = time;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public String getUser() {
		return user;
	}
	
	public Time getTime() {
		return time;
	}
	
	public void setTime(Time time) {
		this.time = time;
	}
	
	public String getChannelName() {
		return channelName;
	}
}
