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

import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.sql.Time;
import java.util.ArrayList;

import assignment7.Chat.CHAT_TYPE;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;

public class Channel {
	private ObservableList<Message> channelChat;
	private ArrayList<ClientThread> participants;
	private Server server;
	private boolean isGroup;
	private String name;
	private int nUsers;
	
	public Channel(String name, boolean isGroup, int amountOfUsers, ArrayList<ClientThread> participants, Server server) {
		this.participants = participants;
		this.name = name;
		this.isGroup = isGroup;
		this.nUsers = amountOfUsers;
		channelChat = FXCollections.observableList(new ArrayList<Message>());
		channelChat.addListener(new ListChangeListener<Message>() {

			@Override
			public void onChanged(Change<? extends Message> arg0) {
				synchronized(this){
					try{
						if(arg0.next()) {
							Message msg = channelChat.get(channelChat.size() - 1);
							msg.setTime(new Time(System.currentTimeMillis()));
							ObjectOutputStream update;
							ArrayList<ClientThread> currentParticipants = new ArrayList<ClientThread>(participants);
							for(int i = 0; i < currentParticipants.size(); ++i) {
								currentParticipants.get(i).writeMessage(msg);	
							}
							if(!msg.getUser().equals("Server")) {
								server.getAccounts().get(msg.getUser()).getChatHistory().add(msg);
							}
							server.updateFile();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void sendMessage(Message message) {
		channelChat.add(message);
	}
	
	public boolean addUser(String username, ClientThread ct) throws SocketException {
		if(!isGroup && nUsers > 2)
			return false;
		Message msg = new Message(username + " joined the channel.", "Server", name, new Time(System.currentTimeMillis()));
		channelChat.add(msg);
		participants.add(ct);
		++nUsers;
		return true;
	}
	
	public void removeUser(String username, ClientThread ct) throws SocketException {
		if(participants.remove(ct)) {
			Message msg = new Message(username + " left the channel.", "Server", name, new Time(System.currentTimeMillis()));
			channelChat.add(msg);
			--nUsers;
		}
	}
	
	public void setIsGroup(boolean b) {
		isGroup = b;
	}
	
	public void setName(String s) {
		name = s;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public ArrayList<String> getMembers() {
		ArrayList<String> members = new ArrayList<String>();
		for(ClientThread cliThread: participants)
			members.add(cliThread.getUser().getName());
		return members;
	}
	
	@Override
	public boolean equals(Object o) {
		if( o instanceof Channel && o.toString().equals(name))
			return true;
		return false;
	}
}
