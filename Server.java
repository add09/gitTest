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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * 
 * @author Daniel
 * Server class for the chat clients. Listens for new connections and handles information for all clients.
 */
public class Server {
	
	private ServerSocket server;
	private ArrayList<ClientThread> clientList;
	private ObservableMap<String, Account> accounts;
	private Key publicKey = ParamServer.pubKey;
	private Key privateKey;
	private ArrayList<Channel> channels;
	private Channel bc;
	private ObservableList<String> online;
	
	private File accountsFile;
	
	/**
	 * 
	 * @param port port number.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Server(int port) throws IOException, ClassNotFoundException {
		server = new ServerSocket(port);
		channels = new ArrayList<Channel>();
		clientList = new ArrayList<ClientThread>();
		online = FXCollections.observableArrayList();
		online.addListener(new ListChangeListener() {

			@Override
			public void onChanged(Change c) {
				if(c.next()) {
					// TODO Auto-generated method stub
					ArrayList<ClientThread> temp = new ArrayList<ClientThread>(clientList);
					for(int i = 0; i < temp.size(); ++i) {
						if(temp.get(i).getUser() != null) {
							try {
								temp.get(i).sendObject(new Request("updateUsers", new Object[] {temp.get(i).getNonFriends(), temp.get(i).getFriends()}));
							}
							catch (Exception e) {
								
							}
						}
						
					}
				}
			}
			
			
		});
		
		accountsFile = new File("accounts.ser");
		accountsFile.createNewFile();
		accounts = FXCollections.observableHashMap();
		try {
			//Object o = readObject();
			accounts.putAll((Map<String, Account>)readObject());
		}
		catch(Exception e) {
			
		}
		if(accounts == null) {
			accounts = FXCollections.observableHashMap();
		}
		accounts.addListener(new MapChangeListener<String, Account>() {

			@Override
			public void onChanged(Change<? extends String, ? extends Account> arg0) {
				// TODO Auto-generated method stub
				updateFile();
			}
			
		});
		channels.add(new Channel("Broadcast", true, 0, new ArrayList<ClientThread>(), this));
		bc = channels.get(0);
	}
	
	public Channel getBroadcast() {
		return bc;
	}
	
	public ObservableList<String> getOnline(){
		return online;
	}
	
	/**
	 * The server will start accepting new socket connections.
	 * @throws IOException
	 */
	public void start() throws IOException {
		while(true) {
			Socket socket = server.accept();
			ClientThread thread = new ClientThread(socket, this);
			clientList.add(thread);
			thread.start();
		}
	}
	
	/**
	 * Clears the "accounts.txt" file.
	 * @return true if successful, false otherwise.
	 */
	private boolean clearFile() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(accountsFile.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		pw.close();
		return true;
	}
	
	/**
	 * Reads an object from the file.
	 * @return
	 */
	private Object readObject() {
		FileInputStream fis;
		try {
			fis = new FileInputStream(accountsFile);
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis));
			return ois.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	
	/**
	 * Writes down the current accounts.
	 * @param o the objects to write to the file.
	 * @return true if success otherwise false.
	 */
	private boolean writeAccounts(Object o) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(accountsFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(fos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			oos.writeObject(o);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * The list of the current sockets
	 * @return the list of the current sockets connected to the server.
	 */
	public ArrayList<ClientThread> getClients() {
		return clientList;
	}

	/**
	 * The map with all accounts.
	 * @return a map with all accounts, the key are the usernames.
	 */
	public ObservableMap<String, Account> getAccounts() {
		// TODO Auto-generated method stub
		return accounts;
	}
	
	/**
	 * Updates the file.
	 */
	public void updateFile() {
		clearFile();
		Map<String, Account> m = new HashMap<String, Account>();
		m.putAll(accounts);
		writeAccounts(m);
	}
	
	/**
	 * Returns an list of possible channels that the account can connect to.
	 * @param account the account which channels it can connect to are returned.
	 * @return A list of channels the account can connect to are returned.
	 */
	public ArrayList<Channel> getChannels(){
		return channels;
	}
	
	public Key getPublicKey() {
		return publicKey;
	}
	
	public Key getPrivateKey() {
		return privateKey;
	}
	
	public ArrayList<String> getUsers(){
		ArrayList<String> onlineUsers = new ArrayList<String>();
		for(String s: accounts.keySet()) {
			if(accounts.get(s).isOnline())
				onlineUsers.add(s);
		}
		return onlineUsers;
	}
}
