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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import assignment7.Chat.CHAT_TYPE;

public class ClientThread extends Thread {
	private Socket socket;
	private Server server;
	private static final long serialVersionUID = 4;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Account user;
	private ArrayList<Channel> channels;
	
	public ClientThread(Socket socket, Server server) throws IOException {
		channels = new ArrayList<Channel>();
		this.socket = socket;
		this.server = server;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		this.user = null;
	}
	
	public ClientThread(Socket socket, Server server, boolean debug) throws IOException {
		channels = new ArrayList<Channel>();
		this.socket = socket;
		this.server = server;
	}
	
	public boolean login(String username, String password) throws SocketException {
		Account account = server.getAccounts().get(username);
		if(user != null || account == null)
			return false;
		if(account.getPassword().equals(password)) {
			user = account;
			user.addConnection(this);
			server.getBroadcast().addUser(username, this);
			channels.add(server.getBroadcast());
			server.getOnline().add(username);
			sendObject(new Request("newChat", new Object[] {"Broadcast", CHAT_TYPE.GROUP, server.getBroadcast().getMembers()}));
			return true;
		}
		return false;
	}
	 
	public boolean register(String username, String password) throws SocketException {
		if(user != null || server.getAccounts().get(username) != null)
			return false;
		Account account = new Account(username, password, this);
		server.getAccounts().put(username, account);
		login(username, password);
		return true;
	}
	
	
	@Override
	public void run() {
		Request rq;
		while(!socket.isClosed()){
			try {
				rq = (Request) in.readObject();
				Object o = rq.invoke(this);
				sendObject(o);
			} catch (SocketException e) {
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
      }
		}
	}
	
	public ArrayList<String> getUsers(){
		return server.getUsers();
	}
	
	public Account getUser() {
		return user;
	}
	
	public boolean addFriend(String user) throws SocketException {
		if(server.getAccounts().get(user).getFriends().contains(this.user))
			return false;
		if(!server.getAccounts().get(user).addFriend(this.user))
			return false;
		else
			this.user.addFriend(server.getAccounts().get(user));
		for(ClientThread ct: server.getAccounts().get(user).getConnected()) {
			if(!ct.sendObject(new Request("acceptedFriendReq", new Object [] {this.user.getName()})))
				return false;
		}
		return true;
	}
	
	public ArrayList<Friend> getFriends(){
		ArrayList<Account> friends = user.getFriends();
		ArrayList<Friend> friendsList = new ArrayList<Friend>();
		for(Account acc : friends) {
			if(acc.isOnline())
				friendsList.add(new Friend(acc.toString(), Friend.STATUS.ONLINE));
			else
				friendsList.add(new Friend(acc.toString(), Friend.STATUS.OFFLINE));
		}
		return friendsList;
	}
	
	public boolean logOut() throws SocketException {
		String un = null;
		if(user != null) {
			for(Channel c : channels)
				c.removeUser(user.getName(), this);
			user.removeConnected(this);
			un = user.getName();
			user = null;
			server.getOnline().remove(un);
		}
		channels.clear();
		return true;
	}
	
	public void logOff() {
		if(user != null) {
			user.removeConnected(this);
			server.getClients().remove(this);
		}
		try {
			logOut();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getNonFriends() {
		ArrayList<String> nonFriend  = getUsers();
		for(Friend f: getFriends()) {
			nonFriend.remove(f.getName());
		}
		while(nonFriend.remove(user.getName())) {}
		return nonFriend;
	}
	
	public boolean sendFriendRequest(String username) throws SocketException {
		return server.getAccounts().get(username).sendFriendRequest(this.user.getName());
	}
	
	public boolean createChannel(String name, ArrayList<String> users, Boolean isGroup) throws SocketException {
		ArrayList<ClientThread> participants = new ArrayList<ClientThread>();
		for(String s: users) {
			participants.addAll(server.getAccounts().get(s).getConnected());
		}
		Channel c = new Channel(name, isGroup, users.size(), participants, server);
		if(server.getChannels().contains(c))
			return false;
		server.getChannels().add(c);
		for(int i = 0; i < users.size(); ++i) {
			Account  acc = server.getAccounts().get((String)users.get(i));
			ArrayList<ClientThread> temp = acc.getConnected();
			for(ClientThread ct : temp) {
				ct.channels.add(c);
				CHAT_TYPE chatType;
				if(isGroup)
					chatType = CHAT_TYPE.GROUP;
				else
					chatType = CHAT_TYPE.DIRECT;
				ct.sendObject(new Request("newChat", new Object[] {name, chatType, users}));
				//c.addUser(acc.toString(), ct);
			}
		}
		return true;
	}
	
	public boolean modifyChannel(String oldName, String newName, ArrayList<String> users, Boolean isGroup) {
		Channel c = null;
		for(int i = 0; i < channels.size(); ++i) {
			if(channels.get(i).getName().equals(oldName)) {
				c = channels.get(i);
				break;
			}
		}
		if(c == null)
			return false;
		c.setIsGroup(isGroup);
		CHAT_TYPE cType;
		if(isGroup)
			cType = CHAT_TYPE.GROUP;
		else
			cType = CHAT_TYPE.DIRECT;
		try {
			c.setName(newName);
			ArrayList<String> mems = c.getMembers();
			for(String u: users) {
				if(!mems.contains(u)) {
					for(ClientThread ct: server.getAccounts().get(u).getConnected()) {
						ct.channels.add(c);
						c.addUser(u, ct);
						ct.sendObject(new Request("newChat", new Object[] {newName, cType, c.getMembers()}));
					}
				}
			}
			for(String m: mems) {
				if(!users.contains(m)) {
					for(ClientThread ct: server.getAccounts().get(m).getConnected()) {
						ct.sendObject(new Request("removedFromChat", new Object[] {oldName, cType}));
						c.removeUser(m, ct);
					}
				}
			}
			for(String s: users) {
				for(ClientThread cl: server.getAccounts().get(s).getConnected()) {
					Request updateChat = new Request("updateChat", new Object [] {oldName, newName, users, cType});
					cl.sendObject(updateChat);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public boolean sendMessage(Message msg) {
		Channel c = null;
		for(int i = 0; i < channels.size(); ++i) {
			if(channels.get(i).getName().equals(msg.getChannelName())) {
				c = channels.get(i);
				break;
			}
		}
		if(c == null)
			return false;
		c.sendMessage(msg);
		return true;
	}
	
	private Socket getSocket() {
		return socket;
	}
	
	public void writeMessage(Message m) {
		try {
			out.writeObject(m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized boolean sendObject(Object o) throws SocketException {
		try {
			out.writeObject(o);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
