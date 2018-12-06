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

import java.util.ArrayList;
import java.util.List;

public class InputHandler implements Runnable {
	Client client;
	List<Object> inputs;
	InputStreamListener isl;
	
	InputHandler(Client c, List<Object> in, InputStreamListener isl) {
		client = c;
		inputs = new ArrayList<Object>(in);
		this.isl = isl;
	}

	@Override
	public void run() {
		Thread.yield();
		synchronized(isl) {
			if(inputs.size() == 0)
				return;
			client.handleInput(inputs);
			System.out.println(inputs);
			for(Object o: inputs)
				isl.data.remove(o);
		}
	}
}
