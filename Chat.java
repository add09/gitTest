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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class Chat {
	public static enum CHAT_TYPE {
		DIRECT,
		GROUP
	}
	private static ArrayList<Color> colors = new ArrayList<Color>();
	private static Map<String, Color> usernameColor = new HashMap<String, Color>();
	private static Set<Color> takenColors = new HashSet<Color>();
	DateFormat format = new SimpleDateFormat("HH:mm");
	
	String chatName;
	CHAT_TYPE chatType;
	RadioButton rb;
	GridPane chatBox;
	int nextRow;
	ArrayList<String> members;
	
	static {
		/* Fetch available colors from Color class */
		try {
			for(Field f: Color.class.getFields()) {
				if(Modifier.isStatic(f.getModifiers())) {
					if(f.getType().equals(Color.class) && f.getName().contains("DARK")) {
						colors.add((Color)f.get(null));
					}
				}
			}
		}
		catch(Exception e) {
			System.out.println("Ooops, something went wrong when getting colors.");
		}
	}
	
	Chat(CHAT_TYPE chatType, String chatName, GridPane chatBox, ArrayList<String> members) {
		this.chatType = chatType;
		this.chatBox = chatBox;
		nextRow = 0;
		this.chatName = chatName;
		this.members = members;
	}
	
	private static Color getNewColor() {
		Color c = null;
		Iterator<Color> it = colors.iterator();
		while(it.hasNext()) {
			Color col = it.next();
			if(!usernameColor.values().contains(col)) {
				c = col;
				break;
			}
		}
		if(c == null) {
			takenColors.clear();
			c = colors.get(0);
		}
		takenColors.add(c);
		return c;
	}
	
	public static void addUser(String user) {
		if(usernameColor.keySet().contains(user))
			return;
		usernameColor.put(user, getNewColor());
	}
	
	public void showMessage(String user, String msg, Time time) {
		Label timeLabel = new Label(format.format(time.getTime()));
		chatBox.add(timeLabel, 0, nextRow);
		Label userLabel = new Label(user);
		userLabel.setTextFill(usernameColor.get(user));
		chatBox.add(userLabel, 1, nextRow);
		Label msgLabel = new Label(msg);
		chatBox.add(msgLabel, 2, nextRow);
		
		nextRow++;
	}
}