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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.Socket;
import java.sql.Time;

import assignment7.Chat.CHAT_TYPE;
import assignment7.Friend.*;

public class ClientGUI extends Application {
	private Client client;
	private final double hMarg = 5.0, vMarg = 10.0;
	private String username;
	private final int connectPaneXSize = 1000, connectPaneYSize = 500;
	private final int chatPaneXSize = 1000, chatPaneYSize = 500;
	private final int chatBoxXSize = 200, chatBoxYSize = 200;
	private final int portNum = 8000;
	private ObservableList<Chat> openChats = FXCollections.observableArrayList();
	private ObservableList<String> availableUsers = FXCollections.observableArrayList();
	private ObservableList<Friend> friends = FXCollections.observableArrayList();	
	public ObservableList<Request> friendReq = FXCollections.observableArrayList(); 
	private ScrollPane scrollChat;
	
	private static void error(String cause, String msg) {
		// Error alert
		Alert errorAlert = new Alert(AlertType.WARNING);
		errorAlert.setTitle("Error: " + cause);
		errorAlert.setHeaderText(cause);
		errorAlert.setContentText(msg);
		errorAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		errorAlert.showAndWait();
	}
	
	private static void alertInfo(String title, String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(title);
		alert.setContentText(msg);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait();
	}
	
	public static void GUIStart(String[] args) {
		launch(args);
	}
	
	public Chat findChat(String name) {
		for(Chat c: openChats) {
			if(c.chatName.equals(name)) {
				return c;
			}
		}
		return null;
	}
	
	@Override
	public void init() {
		this.client = new Client(this);
		
		availableUsers.addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				if(c.next() && c.wasAdded()) {
					for(String s: c.getAddedSubList()) {
						Chat.addUser(s);
					}
				}
			}
		});
		
		friendReq.addListener(new ListChangeListener<Request>() {
			@Override
			public void onChanged(Change<? extends Request> c) {
				if(c.next()) {
					for(Request r: c.getAddedSubList()) {
						try {
							Boolean res = (Boolean) r.invoke(this);
							client.sendResponse(res);
						}
						catch(Exception e) {
							
						}
					}
				}
			}
		});
	}
	
	@Override
	public void start(Stage primaryStage) {			
		/* Big container objects */
		GridPane connectPane = new GridPane();
		connectPane.setAlignment(Pos.CENTER);
		connectPane.setHgap(hMarg);
		connectPane.setVgap(vMarg);
		
		BorderPane chatPane = new BorderPane();
		scrollChat = new ScrollPane();
		scrollChat.setMinHeight(chatBoxYSize+20);
		scrollChat.setMinWidth(chatBoxXSize+20);
		scrollChat.setStyle("-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: black;");
		GridPane emptyChat = new GridPane();
		
		Scene connectScene = new Scene(connectPane, connectPaneXSize, connectPaneYSize);
		Scene chatScene = new Scene(chatPane, chatPaneXSize, chatPaneYSize);
		
		/*----------------------ConnectPane-------------------*/
		
		Label serverIPLabel = new Label("Server IP Address: ");
		TextField serverIPField = new TextField();
		Button connect = new Button("Connect");
		connect.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				// TODO: Display progress dialog while connecting (to prevent user from trying to login/register)
				// Disconnect from server
				if(client.connected) {
					Request logoffReq = new Request("logOff", (new Object[] {}));
					client.sendRequest(logoffReq);
					try {
						client.serverClose();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					client.connected = false;
					connect.setText("Connect");
				}
				// Establish connection to server
				else {
					if(serverIPField.getText().equals("")) {
						error("No Server IP Address", "Please enter a valid server IP address before attempting to connect.");
						return;
					}	
					
					if(!client.connect(serverIPField.getText(), portNum)) {
						error("Server Connection Failed", "Please try again or enter another server IP address.");
						client.connected = false;
						return;
					}				
					client.connected = true;
					connect.setText("Disconnect");
				}
			}
		});
		
		TextField userField = new TextField();
		
		serverIPField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent k) {
				if(k.getCode().equals(KeyCode.ENTER)) {
					connect.fire();
					userField.requestFocus();
				}
			}
		});
		
		// Add listener to input stream from server
		
		Label userLabel = new Label("Username: ");
		
		Label passwordLabel = new Label("Password: ");
		PasswordField passwordField = new PasswordField();
		
		Button login = new Button("Login");
		login.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				// Login
				if(!client.connected) {
					error("Not Connected to Server", "Please connect to the server before attempting to login.");
					return;
				}
				if(userField.getText().equals("")) {
					error("Empty Username", "Please enter a valid username and try again.");
					return;
				}
				else if(passwordField.getText().equals("")) {
					error("Empty Password", "Please enter a valid password and try again.");
					return;
				}
				
				String un = userField.getText();
				String pw = passwordField.getText();
				Object [] args = {un, pw};
				Request loginReq = new Request("login", args);
				client.sendRequest(loginReq);
				Boolean ret = client.getResult();
				client.loggedIn = ret;
				if(!ret) {
					error("Account Not Found", "Please login with a valid username and password.");
					return;
				}
				username = un;
				Chat.addUser(un);
				primaryStage.setScene(chatScene);
				//primaryStage.centerOnScreen();
			}
		});
		GridPane.setHalignment(login, HPos.CENTER);
		
		passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent k) {
				if(k.getCode().equals(KeyCode.ENTER)) {
					login.fire();
				}
			}
		});
		
		userField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent k) {
				if(k.getCode().equals(KeyCode.ENTER)) {
					login.fire();
				}
			}
		});
		
		Button register = new Button("Register");
		register.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				// Register user
				if(!client.connected) {
					error("Not Connected to Server", "Please connect to the server before attempting to register.");
					return;
				}
				
				String un = userField.getText();
				String pw = passwordField.getText();
				Object [] args = {un, pw};
				Request registerReq = new Request("register", args);
				client.sendRequest(registerReq);
				Boolean ret = client.getResult();
				System.out.println(ret);
				if(!ret) {
					error("Username Taken", "Please register with another username.");
					return;
				}
				client.loggedIn = true;
				username = un;
				Chat.addUser(un);
				primaryStage.setScene(chatScene);
				//primaryStage.centerOnScreen();
			}
		});
		GridPane.setHalignment(register, HPos.CENTER);
		
		connectPane.add(serverIPLabel, 0, 0);
		connectPane.add(serverIPField, 1, 0);
		connectPane.add(connect, 2, 0);
		connectPane.add(userLabel, 0, 2);
		connectPane.add(userField, 1, 2);
		connectPane.add(passwordLabel, 0, 3);
		connectPane.add(passwordField, 1, 3);
		connectPane.add(login, 0, 4, 2, 1);
		connectPane.add(register, 0, 5, 2, 1);
		
		/*----------------------ChatPane-------------------*/
		
		BorderPane top = new BorderPane();
		GridPane left = new GridPane();
		BorderPane center = new BorderPane();
		GridPane right = new GridPane();
		
		/* Top Area of BorderPane */
		Button logout = new Button("Logout");
		logout.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
  			Request logoutReq = new Request("logOut", new Object[] {});
  			client.sendRequest(logoutReq);
  			client.getResult();
				primaryStage.setScene(connectScene);
				serverIPField.requestFocus();
				//primaryStage.centerOnScreen();
				
				passwordField.setText("");
				
				// Reset chat view
				openChats.clear();
				scrollChat.setContent(emptyChat);
				client.loggedIn = false;
			}
		});
		
		Button newChatButton = new Button("New Chat");
		newChatButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				newChatDialog(newChatButton);
			}
		});
		
		top.setLeft(logout);
		top.setRight(newChatButton);
		
		/* Left Area of BorderPane */
		left.setHgap(hMarg);
		left.setVgap(vMarg);
		left.setMinWidth(200.0);
		
		Label usersLabel = new Label("Chats");
		usersLabel.setStyle("-fx-underline: true; -fx-font-weight: bold;");
		GridPane.setHalignment(usersLabel, HPos.LEFT);
		
		VBox chats = new VBox(5);		

		ToggleGroup chatTG = new ToggleGroup();
		ArrayList<RadioButton> userRadios = new ArrayList<RadioButton>();
		
		if(userRadios.size() > 0)
			userRadios.get(0).setSelected(true);
		
		openChats.addListener(new ListChangeListener<Chat>() {
			@Override
			public void onChanged(Change<? extends Chat> c) {
				if(!c.next())
					return;
				if(c.wasAdded()) {
					for(Chat chat: c.getAddedSubList()) {
						RadioButton rb = new RadioButton(chat.chatName);
						rb.setToggleGroup(chatTG);
						userRadios.add(rb);
						rb.setOnAction(new EventHandler<ActionEvent>() {
							public void handle(ActionEvent e) {
								scrollChat.setContent(chat.chatBox);
							}
						});
						GridPane g = new GridPane();
						g.setMinWidth(chatBoxXSize);
						g.setMinHeight(chatBoxYSize);
						GridPane.setFillHeight(g, true);
						g.setHgap(15.0);
						g.setVgap(2.0);
						chat.chatBox = g;
						chats.getChildren().add(rb);
						chat.rb = rb;
						rb.setSelected(true);
						scrollChat.setContent(g);
					}
				}
				if(c.wasRemoved()) {
					for(Chat chat: c.getRemoved()) {
						chats.getChildren().remove(chat.rb);
						userRadios.remove(chat.rb);
					}
					Chat chat = openChats.get(0);
					userRadios.get(0).setSelected(true);
					scrollChat.setContent(chat.chatBox);
				}
			}
		});
		
		left.add(usersLabel, 0, 0);
		left.add(chats, 0, 1);
		
		BorderPane.setAlignment(left, Pos.CENTER_LEFT);
		
		/* Center Area of BorderPane */				
		emptyChat.setMinWidth(chatBoxXSize);
		emptyChat.setMinHeight(chatBoxYSize);
		GridPane.setFillHeight(emptyChat, true);
		scrollChat.setContent(emptyChat);
		scrollChat.setMaxHeight(chatBoxYSize-40);
		scrollChat.setVvalue(1.0);
		
		center.setTop(scrollChat);
		
		GridPane sendMsgPane = new GridPane();
		
		TextField messageText = new TextField();
		messageText.setPromptText("Enter a message");
		
		Button sendMsg = new Button("Send Message");
		sendMsg.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				String message = messageText.getText();
				messageText.setText("");
				RadioButton rb = (RadioButton) chatTG.getSelectedToggle();
				int i = userRadios.indexOf(rb);
				if(i == -1) {
					error("No Chat Selected", "Please select a chat.");
					return;
				}
				Chat active = openChats.get(i);
				Time current = new Time(System.currentTimeMillis());
				//active.showMessage(username, message, current);
				Message m = new Message(message, username, active.chatName, current);
				Request sendMsg = new Request("sendMessage", new Object[]{m});
				client.sendRequest(sendMsg);
				System.out.println(client.getResult());
			}
		});
		
		messageText.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent k) {
				if(k.getCode().equals(KeyCode.ENTER)) {
					sendMsg.fire();
				}
			}
		});
		
		sendMsgPane.add(messageText, 0, 0);
		sendMsgPane.add(sendMsg, 1, 0);
		
		center.setCenter(sendMsgPane);
		
		BorderPane editChatBP = new BorderPane();
		Button editChat = new Button("Edit Chat");
		editChat.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				RadioButton rb = (RadioButton) chatTG.getSelectedToggle();
				int i = userRadios.indexOf(rb);
				if(i == -1) {
					error("No Chat Selected", "Please select a chat.");
					return;
				}
				Chat active = openChats.get(i);
				if(active.chatName.equals("Broadcast")) {
					error("Can't Edit Broadcast", "Please select another chat.");
					return;
				}
				editChatDialog(editChat, active);
			}
		});
		editChatBP.setTop(editChat);
		
		center.setRight(editChatBP);
		
		
		/*
		 * Test
		 */		
		//newChat("testing", CHAT_TYPE.GROUP);
		
		/* Right Area of BorderPane */
		right.setHgap(hMarg);
		right.setVgap(vMarg);
		right.setMinWidth(200.0);
		
		Label friendsLabel = new Label("Your Friends");
		friendsLabel.setStyle("-fx-underline: true; -fx-font-weight: bold;");
		GridPane.setHalignment(friendsLabel, HPos.LEFT);
		
		ScrollPane friendsListScroll = new ScrollPane();
		friendsListScroll.setMaxHeight(chatBoxYSize-40);
		GridPane friendsList = new GridPane();
		friendsList.setHgap(hMarg);
		friendsList.setVgap(vMarg);
		friendsListScroll.setContent(friendsList);
		friends.addListener(new ListChangeListener<Friend>() {
			@Override
			public void onChanged(Change<? extends Friend> c) {
				if(c.next()) {
					friendsList.getChildren().clear();
					int row = 0;
					for(Friend f: friends.sorted()) {
						Label l = new Label(f.toString());
						friendsList.add(l, 0, row);
						row++;
					}
				}
			}
		});
		
		// Add friend dropdown
		Label newFriendLabel = new Label("Add New Friend");
		newFriendLabel.setStyle("-fx-underline: true; -fx-font-weight: bold;");
		ComboBox<String> addFriendCB = new ComboBox<String>(availableUsers);
		
		Button addFriend = new Button("Add Friend");
		addFriend.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				String user = addFriendCB.getValue();
				if(user.equals("")) {
					error("No User Selected", "Please select a user to friend request and try again.");
					return;
				}
				Object [] params = {user};
				Request friendReq = new Request("sendFriendRequest", params);
				client.sendRequest(friendReq);
				Boolean res = client.getResult();
				if(!res) {
					error("Friend Request Not Sent", "Sorry, we could not send your friend request at this time, please try again.");
				}
				else {
					//alertInfo("Friend Request Sent", "Your friend request to " + user + " was sent successfully.");
				}
			}
		});
		
		
		right.add(friendsLabel, 0, 0);
		right.add(friendsListScroll, 0, 1);
		right.add(newFriendLabel, 0, 2);
		right.add(addFriendCB, 0, 3);
		right.add(addFriend, 1, 3);
		
		BorderPane.setAlignment(right, Pos.CENTER_RIGHT);		
		
		/* BorderPane Area Setting */
		
		chatPane.setTop(top);
		chatPane.setLeft(left);
		chatPane.setCenter(center);
		chatPane.setRight(right);
		
		/*----------------------StageSetting-------------------*/
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent event) {
		    	try {
		    		if(client.connected) {
		    			client.serverClose();
		    		}
		    		
		    	}
		    	catch(Exception e) {
		    		error("Couldn't Close Server Connection", "I done fucked up.");
		    	}
		    }
		});
		
		primaryStage.setScene(connectScene);
		serverIPField.requestFocus();
		//primaryStage.centerOnScreen();
		primaryStage.show();
	}
	
	public void newChat(String chatName, CHAT_TYPE chatType, ArrayList<String> members) {
		Chat newChat = new Chat(chatType, chatName, null, members);
		openChats.add(newChat);
		if(newChat.chatType == CHAT_TYPE.DIRECT) {
			alertInfo("New Direct Chat", "You've been added to a direct chat called " + chatName + ".");
		}
		else {
			alertInfo("New Group Chat", "You've been added to a group chat called " + chatName + ".");
		}
	}
	
	public void removedFromChat(String chatName, CHAT_TYPE chatType) {
		Chat c = findChat(chatName);
		if(c != null)
			openChats.remove(c);
		else 
			return;
		if(c.chatType == CHAT_TYPE.DIRECT) {
			alertInfo("Removed From Chat", "You've been removed from a direct chat called " + chatName + ".");
		}
		else {
			alertInfo("Removed From Chat", "You've been removed from a group chat called " + chatName + ".");
		}
	}
	
	private static ArrayList<String> getAvailableUsers() {
		return new ArrayList<String>();
	}
	
	public void acceptedFriendReq(String reqUser) {
		friends.add(new Friend(reqUser, STATUS.ONLINE));
	}
	
	public void newFriendRequest(String reqUser) {
		Alert alert = 
        new Alert(AlertType.CONFIRMATION, 
            reqUser + " would like to be your friend.\nDo you accept?",
             ButtonType.YES, 
             ButtonType.NO);
		alert.setHeaderText("New Friend Request");
		alert.setTitle("New Friend Request");
		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == ButtonType.YES) {
			Object [] args = {reqUser};
			Request newFriend = new Request("addFriend", args);
			client.sendRequest(newFriend);
			client.getResult();
			friends.add(new Friend(reqUser, STATUS.ONLINE));
		}
	}
	
	public void displayMessage(Message msg) {
		Chat c = findChat(msg.getChannelName());
		if(c != null) {
			Chat.addUser(msg.getUser());
			c.showMessage(msg.getUser(), msg.getMessage(), msg.getTime());
		}
		scrollChat.setVvalue(1.0);
	}
	
	public void updateChat(String oldName, String newName, ArrayList<String> members, CHAT_TYPE ct) {
		Chat c = findChat(oldName);
		if(c != null) {
			c.chatName = newName;
			c.rb.setText(newName);
			c.members = members;
			c.chatType = ct;
		}
	}
	
	public void updateUsers(ArrayList<String> nonFriends, ArrayList<Friend> friendList) {
		availableUsers.clear();
		for(String name: nonFriends) {
			availableUsers.add(name);
		}
		
		friends.clear();
		for(Friend f: friendList) {
			friends.add(f);
		}
	}


	private void newChatDialog(Button clicked) {
    final Stage dialog = new Stage();
    dialog.setTitle("New Chat");
    dialog.initModality(Modality.NONE);
    dialog.initOwner((Stage) clicked.getScene().getWindow());
    
    GridPane main = new GridPane();
    main.setHgap(10.0);
    main.setVgap(10.0);
    main.setAlignment(Pos.CENTER);
    
    /* Chat Type Column */
    Label chatTypeLabel = new Label("Chat Type");
    chatTypeLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    GridPane.setHalignment(chatTypeLabel, HPos.CENTER);
    
    ObservableList<String> chatType = FXCollections.observableArrayList();
    chatType.add("Direct");
    chatType.add("Group");
    ComboBox<String> ctCombo = new ComboBox<String>(chatType);
    ctCombo.setValue("Direct");
    
    /* Recipient Column */        
    Label recLabel = new Label("Recipient");
    recLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    GridPane.setHalignment(recLabel, HPos.CENTER);
    
    ObservableList<String> users = FXCollections.observableArrayList();
		// Retrieve user list from server
		Object [] args = {};
		Request getFriends = new Request("getFriends", args);
		client.sendRequest(getFriends);
		ArrayList<Friend> userList = (ArrayList<Friend>) client.getList();
		for(Friend f: userList) {
			if(f.getStatus() == STATUS.ONLINE)
				users.add(f.getName());
		}

    ComboBox<String> userCB = new ComboBox<String>(users);
    if(users.size() > 0)
    	userCB.setValue(users.get(0));
    
    /* Members Column */
    VBox membersBox = new VBox(5.0);
    
    Label membersLabel = new Label("Members");
    membersLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    GridPane.setHalignment(membersLabel, HPos.CENTER);
    
    ArrayList<CheckBox> userCBs = new ArrayList<CheckBox>();
    for(String u: users) {
    	HBox hb = new HBox();
    	CheckBox cb = new CheckBox();
    	cb.setAlignment(Pos.CENTER_LEFT);
    	userCBs.add(cb);
    	hb.getChildren().add(cb);
    	Label cbLabel = new Label(u);
    	cbLabel.setAlignment(Pos.CENTER_RIGHT);
    	hb.getChildren().add(cbLabel);
    	membersBox.getChildren().add(hb);
    }
    
    /* Chat Name Column */
    Label chatNameLabel = new Label("Chat Name");
    chatNameLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    TextField chatNameField = new TextField();
    GridPane.setHalignment(chatNameLabel, HPos.CENTER);
    
    /* Create Chat Column */        
    Button makeChat = new Button("Create New Chat");
    makeChat.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
      	CHAT_TYPE ct;
      	Chat c;
      	if(ctCombo.getValue().equals("Direct")) {
      		ct = CHAT_TYPE.DIRECT;
      		ArrayList<String> recipients = new ArrayList<String>();
      		recipients.add(userCB.getValue());
      		recipients.add(1, username);
      		
      		GridPane g = new GridPane();
					g.setMinWidth(chatBoxXSize);
					g.setMinHeight(chatBoxYSize);
					GridPane.setFillHeight(g, true);
					g.setHgap(15.0);
					g.setVgap(2.0);
					String name;
      		if(userCB.getValue().compareTo(username) < 0)
      			name = userCB.getValue() + "-" + username;
      		else
      			name = username + "-" + userCB.getValue();
      		c = new Chat(ct, name, g, recipients);
      		
  				Object [] args = {c.chatName, recipients, false};
  				Request createChannelReq = new Request("createChannel", args);
  				client.sendRequest(createChannelReq);
  				Boolean ret = client.getResult();
  				if(ret == null || !ret) 
  					error("Chat Already Exists", "Please use the existing chat.");
      	}
      	else {
      		ct = CHAT_TYPE.GROUP;
      		ArrayList<String> recipients = new ArrayList<String>();
      		recipients.add(username);
      		for(CheckBox cb: userCBs) {
      			if(cb.isSelected()) {
      				int i = userCBs.indexOf(cb);
      				recipients.add(users.get(i));
      			}
      		}
      		if(recipients.size() == 0) {
      			error("No Members Selected", "Please select one or more members for the group chat.");
      			return;
      		}
      		if(chatNameField.getText().equals("")) {
      			error("Chat Name Empty", "Please enter a name for your group chat.");
      			return;
      		}
      		GridPane g = new GridPane();
					g.setMinWidth(chatBoxXSize);
					g.setMinHeight(chatBoxYSize);
					GridPane.setFillHeight(g, true);
					g.setHgap(15.0);
					g.setVgap(2.0);
      		c = new Chat(ct, chatNameField.getText(), g, recipients);
      		
  				Object [] args = {c.chatName, recipients, true};
  				Request createChannelReq = new Request("createChannel", args);
  				client.sendRequest(createChannelReq);
  				Boolean ret = client.getResult();
  				if(ret == null || !ret) {
  					error("Chat Name Taken", "Please choose another chat name.");
  				}
      	}
        dialog.close();
      }
    });
    
		chatNameField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent k) {
				if(k.getCode().equals(KeyCode.ENTER)) {
					makeChat.fire();
				}
			}
		});
        
    ctCombo.setOnAction(new EventHandler<ActionEvent>() {
    	public void handle(ActionEvent e) {
    		if(ctCombo.getValue().equals("Direct") && main.getChildren().contains(membersBox)) {
    			main.getChildren().remove(membersLabel);
    			main.getChildren().remove(membersBox);
    			main.getChildren().remove(chatNameLabel);
    			main.getChildren().remove(chatNameField);
    			main.getChildren().remove(makeChat);
    			
    			main.add(recLabel, 1, 0);
    			main.add(userCB, 1, 1);
    			main.add(makeChat, 2, 1);
    		}
    		else if(ctCombo.getValue().equals("Group") && main.getChildren().contains(userCB)) {
    			main.getChildren().remove(recLabel);
    			main.getChildren().remove(userCB);
    			main.getChildren().remove(makeChat);
    			
    			main.add(membersLabel, 1, 0);
    			main.add(membersBox, 1, 1);
    			main.add(chatNameLabel, 2, 0);
    			main.add(chatNameField, 2, 1);
    			main.add(makeChat, 3, 1);
    		}
    	}
    });
        
    main.add(chatTypeLabel, 0, 0);
    main.add(ctCombo, 0, 1);
    main.add(recLabel, 1, 0);
    main.add(userCB, 1, 1);
    main.add(makeChat, 2, 1);
    
    Scene dialogScene = new Scene(main, 600, 150);
    dialog.setScene(dialogScene);
    dialog.show();
  }
	
	private void editChatDialog(Button clicked, Chat active) {
    final Stage dialog = new Stage();
    dialog.setTitle("Edit Chat");
    dialog.initModality(Modality.NONE);
    dialog.initOwner((Stage) clicked.getScene().getWindow());
    
    GridPane main = new GridPane();
    main.setHgap(10.0);
    main.setVgap(10.0);
    main.setAlignment(Pos.CENTER);
    
    /* Chat Type Column */
    Label chatTypeLabel = new Label("Chat Type");
    chatTypeLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    GridPane.setHalignment(chatTypeLabel, HPos.CENTER);
    
    ObservableList<String> chatType = FXCollections.observableArrayList();
    chatType.add("Direct");
    chatType.add("Group");
    ComboBox<String> ctCombo = new ComboBox<String>(chatType);
    if(active.chatType == CHAT_TYPE.DIRECT)
    	ctCombo.setValue("Direct");
    else
    	ctCombo.setValue("Group");
    
    /* Recipient Column */        
    Label recLabel = new Label("Recipient");
    recLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    GridPane.setHalignment(recLabel, HPos.CENTER);
    
    ObservableList<String> users = FXCollections.observableArrayList();
		// Retrieve user list from server
		Object [] args = {};
		Request getFriends = new Request("getFriends", args);
		client.sendRequest(getFriends);
		ArrayList<Friend> userList = (ArrayList<Friend>) client.getList();
		for(Friend f: userList) {
			if(f.getStatus() == STATUS.ONLINE)
				users.add(f.getName());
		}

    ComboBox<String> userCB = new ComboBox<String>(users);
    if(active.chatType == CHAT_TYPE.DIRECT) {
    	userCB.setValue(active.members.get(0));
    }
    else if(users.size() > 0)
    	userCB.setValue(users.get(0));
    
    /* Members Column */
    VBox membersBox = new VBox(5.0);
    
    Label membersLabel = new Label("Members");
    membersLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    GridPane.setHalignment(membersLabel, HPos.CENTER);
    
    ArrayList<CheckBox> userCBs = new ArrayList<CheckBox>();
    ArrayList<String> userCBList = new ArrayList<String>(users);
    userCBList.add(username);
    for(String u: userCBList) {
    	HBox hb = new HBox();
    	CheckBox cb = new CheckBox();
    	cb.setAlignment(Pos.CENTER_LEFT);
    	userCBs.add(cb);
    	if(active.members.contains(u))
    		cb.setSelected(true);
    	hb.getChildren().add(cb);
    	Label cbLabel = new Label(u);
    	cbLabel.setAlignment(Pos.CENTER_RIGHT);
    	hb.getChildren().add(cbLabel);
    	membersBox.getChildren().add(hb);
    }
    
    /* Chat Name Column */
    Label chatNameLabel = new Label("Chat Name");
    chatNameLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
    TextField chatNameField = new TextField();
    chatNameField.setText(active.chatName);
    GridPane.setHalignment(chatNameLabel, HPos.CENTER);
    
    /* Create Chat Column */        
    Button makeChat = new Button("Modify Chat");
    makeChat.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent e) {
      	CHAT_TYPE ct;
      	Chat c;
      	if(ctCombo.getValue().equals("Direct")) {
      		ct = CHAT_TYPE.DIRECT;
      		ArrayList<String> recipients = new ArrayList<String>();
      		recipients.add(userCB.getValue());
      		recipients.add(1, username);
					
					String name;
      		if(userCB.getValue().compareTo(username) < 0)
      			name = userCB.getValue() + "-" + username;
      		else
      			name = username + "-" + userCB.getValue();
      		
      		Object [] args = {active.chatName, name, recipients, false};
  				Request modifyChannelReq = new Request("modifyChannel", args);
  				client.sendRequest(modifyChannelReq);
  				Boolean ret = client.getResult();
  				if(ret == null)
  					return;
  				if(!ret) 
  					error("Chat Already Exists", "Please use the existing chat.");
      	}
      	else {
      		ct = CHAT_TYPE.GROUP;
      		ArrayList<String> recipients = new ArrayList<String>();
      		for(CheckBox cb: userCBs) {
      			if(cb.isSelected()) {
      				int i = userCBs.indexOf(cb);
      				recipients.add(userCBList.get(i));
      			}
      		}
      		if(recipients.size() == 0) {
      			error("No Members Selected", "Please select one or more members for the group chat.");
      			return;
      		}
      		if(chatNameField.getText().equals("")) {
      			error("Chat Name Empty", "Please enter a name for your group chat.");
      			return;
      		}

  				Object [] args = {active.chatName, chatNameField.getText(), recipients, true};
  				Request modifyChannelReq = new Request("modifyChannel", args);
  				client.sendRequest(modifyChannelReq);
  				Boolean ret = client.getResult();
  				if(ret == null)
  					return;
  				if(!ret) {
  					error("Chat Name Taken", "Please choose another chat name.");
  				}
      	}
        dialog.close();
      }
    });
    
		chatNameField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent k) {
				if(k.getCode().equals(KeyCode.ENTER)) {
					makeChat.fire();
				}
			}
		});
        
    ctCombo.setOnAction(new EventHandler<ActionEvent>() {
    	public void handle(ActionEvent e) {
    		if(ctCombo.getValue().equals("Direct") && main.getChildren().contains(membersBox)) {
    			main.getChildren().remove(membersLabel);
    			main.getChildren().remove(membersBox);
    			main.getChildren().remove(chatNameLabel);
    			main.getChildren().remove(chatNameField);
    			main.getChildren().remove(makeChat);
    			
    			main.add(recLabel, 1, 0);
    			main.add(userCB, 1, 1);
    			main.add(makeChat, 2, 1);
    		}
    		else if(ctCombo.getValue().equals("Group") && main.getChildren().contains(userCB)) {
    			main.getChildren().remove(recLabel);
    			main.getChildren().remove(userCB);
    			main.getChildren().remove(makeChat);
    			
    			main.add(membersLabel, 1, 0);
    			main.add(membersBox, 1, 1);
    			main.add(chatNameLabel, 2, 0);
    			main.add(chatNameField, 2, 1);
    			main.add(makeChat, 3, 1);
    		}
    	}
    });
    
    main.add(chatTypeLabel, 0, 0);
    main.add(ctCombo, 0, 1);
    
		if(ctCombo.getValue().equals("Direct")) {
			main.add(recLabel, 1, 0);
			main.add(userCB, 1, 1);
			main.add(makeChat, 2, 1);
		}
		else if(ctCombo.getValue().equals("Group")) {			
			main.add(membersLabel, 1, 0);
			main.add(membersBox, 1, 1);
			main.add(chatNameLabel, 2, 0);
			main.add(chatNameField, 2, 1);
			main.add(makeChat, 3, 1);
		}
    
    Scene dialogScene = new Scene(main, 600, 150);
    dialog.setScene(dialogScene);
    dialog.show();
  }
}
