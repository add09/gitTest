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
import java.util.Comparator;

public class Friend implements Comparable<Friend>, Serializable {
	public static enum STATUS {
		ONLINE,
		OFFLINE
	};
	private String name;
	private STATUS status;
	
	Friend(String n, STATUS s) {
		name = n;
		status = s;
	}
	
	public String getName() {
		return name;
	}
	
	public STATUS getStatus() {
		return status;
	}
	
	public void setStatus(STATUS s) {
		status = s;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		String res = name;
		if(status == STATUS.ONLINE)
			res += " : Online";
		else
			res += " : Offline";
		return res;
	}

	@Override
	public int compareTo(Friend f) {
		if(this.status == STATUS.ONLINE && f.status == STATUS.OFFLINE)
			return -1;
		else if(this.status == STATUS.OFFLINE && f.status == STATUS.ONLINE)
			return 1;
		else {
			return name.compareTo(f.name);
		}
	}
}
