/* CHAT ROOM <Client.java>
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * <Cole Morgan>
 * <cm55332>
 * <16345>
 * <Daniel Schmekel>
 * <Student2 EID>
 * <Student2 5-digit Unique No.>
 * Slip days used: <0>
 * Fall 2018
 */

/*
   Describe here known bugs or issues in this file. If your issue spans multiple
   files, or you are not sure about details, add comments to the README.txt file.
 */

package assignment7;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class Client {
	String userName;
	Key privateKey, publicKey;
	Key serverPubKey;
	boolean connected = false;
	Socket serverSocket;
	private InputStreamListener serverListen;
	private ObjectOutputStream serverOut;
	private volatile ArrayList<Boolean> results = new ArrayList<Boolean>();
	private volatile ArrayList<ArrayList<?>> responses = new ArrayList<ArrayList<?>>();
	public boolean loggedIn = false;
	private ClientGUI gui;
	
	Client(ClientGUI gui) {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();
			privateKey = kp.getPrivate();
			publicKey = kp.getPublic();
		}
		catch(Exception e) {
			System.out.println("Something went wrong in key pair generation.");
		}
		
		this.gui = gui;
	}
	
	public void handleInput(List<Object> requests) {
		for(Object o: requests) {
			// Handle incoming messages
			if(o instanceof Message) {
				// Show message
				new Thread(new Runnable() {
			    @Override public void run() {
			        Platform.runLater(new Runnable() {
			            @Override public void run() {
			            	try {
			        				Message m = (Message) o;
			        				gui.displayMessage(m);
			            	}
			            	catch(Exception e) {
			            		
			            	}
			            }
			        });
			    	}
			    }).start();	
			}
			// Handle boolean responses
			else if(o instanceof Boolean) {
				System.out.println(o);
				results.add((Boolean)o);
			}
			// Handle requests
			else if(o instanceof Request) {
				try {
					new Thread(new Runnable() {
				    @Override public void run() {
				        Platform.runLater(new Runnable() {
				            @Override public void run() {
				            	try {
				            		Object ret = ((Request)o).invoke(gui);
				            		if(ret != null)
				            			sendResponse(ret);
				            	}
				            	catch(Exception e) {
				            		
				            	}
				            }
				        });
				    	}
				    }).start();					
				} 
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(o instanceof ArrayList<?>) {
				responses.add((ArrayList<?>)o);
			}
			else {
				System.out.println("Something was sent that I didn't expect.");
			}
		}		
	}
	
	public boolean connect(String ip, int port) {
		serverSocket = new Socket();
		try {
			serverSocket.connect(new InetSocketAddress(ip, port), 10000);
		} catch (IOException e) {
			connected = false;
			return false;
		}
		// Establish input/output streams
		try {
			serverListen = new InputStreamListener(serverSocket.getInputStream(), this);
			Thread t = new Thread(serverListen);
			t.start();
			serverOut = new ObjectOutputStream(serverSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connected = true;
		return true;
	}
	
	// TODO: Write function for setting server public key (called by request object)
	
	public boolean sendRequest(Request r) {
		try {
			synchronized(this) {
				serverOut.writeObject(r);
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean sendResponse(Object r) {
		try {
			synchronized(this) {
				serverOut.writeObject(r);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void serverClose() throws IOException {
		sendRequest(new Request("logOff", new Object[] {}));
		serverSocket.close();
		connected = false;
	}
	
	public Boolean getResult() {
		while(results.size() == 0) {}
		Boolean result = results.get(results.size()-1);
		results.remove(result);
		return result;
	}
	
	public ArrayList<?> getList() {
		while(responses.size() == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ArrayList<?> ret = responses.get(responses.size()-1);
		responses.remove(ret);
		return ret;
	}
	
}
