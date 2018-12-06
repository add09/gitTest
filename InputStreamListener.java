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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class InputStreamListener implements Runnable {
	private Client client;
  private ObjectInputStream inObj;
  public ObservableList<Object> data;
  private InputStreamListener self = this;

  public InputStreamListener(InputStream in, Client client) throws IOException {
  	this.client = client;
    this.inObj = new ObjectInputStream(in);
    this.data = FXCollections.observableArrayList();
    
    data.addListener(new ListChangeListener<Object>() {
    	@SuppressWarnings("unchecked")
    	@Override
			public void onChanged(Change change) {
    		if(change.next() && change.wasAdded()) {
    			Thread handle = new Thread(new InputHandler(client, change.getAddedSubList(), self));
    			handle.start();
    		}
    	}
    });
  }

  public void run() {
  	Object o = null;
  	while(!client.connected) {};
  	while(client.connected) {
		  try {
		  	o = inObj.readObject();
		  }
		  catch (SocketException e) {
			  
		  }
		  catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  synchronized(this) {
		  	data.add(o);
		  }
  	}
  }
}
