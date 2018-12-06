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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Account implements Serializable{
	private transient String username;
	private transient String password;
	private transient ArrayList<Account> friends;
	private transient ArrayList<Message> chatHistory;
	private transient ArrayList<ClientThread> connected;
	private static final long serialVersionUID = 3L;
	
	
	public Account(String name, String pw, ClientThread ct) {
		username = name;
		password = pw;
		chatHistory = new ArrayList<Message>();
		connected = new ArrayList<ClientThread>(1);
		connected.add(ct);
		friends = new ArrayList<Account>();
	}
	
	public ArrayList<Message> getChatHistory() {
		return chatHistory;
	}
	
	public ArrayList<Account> getFriends(){
		return friends;
	}
	
	public boolean addFriend(Account a) {
		friends.add(a);
		return true;
	}
	
	public boolean isOnline() {
		return connected.size() > 0;
	}
	
	public String getName() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.writeUTF(this.username);
		out.writeUTF(this.password);
		out.writeObject(friends);
		out.writeObject(chatHistory);
	}
	
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException{
		this.username = in.readUTF();
		this.password = in.readUTF();
		this.friends = (ArrayList<Account>) in.readObject();
		this.chatHistory = (ArrayList<Message>) in.readObject();
		this.connected = new ArrayList<ClientThread>();
	}
	
	@Override
	public String toString() {
		return username;
	}
	
	public ArrayList<Account> getOnlineFriends(){
		ArrayList<Account> onlineFriends = new ArrayList<Account>();
		for(Account acc : friends) {
			if(acc.isOnline())
				onlineFriends.add(acc);
		}
		return onlineFriends;
	}

	public void removeConnected(ClientThread clientThread) {
		connected.remove(clientThread);
	}
	
	
	//Sending request to user
	public boolean sendFriendRequest(String from) throws SocketException {
		return (Boolean)connected.get(0).sendObject(new Request("newFriendRequest", new Object[] {from}));
	}
	
	public ArrayList<ClientThread> getConnected(){
		return connected;	
	}
	
	public void addConnection(ClientThread ct) {
		connected.add(ct);
	}
	
}
