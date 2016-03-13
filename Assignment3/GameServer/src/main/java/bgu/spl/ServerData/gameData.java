package main.java.bgu.spl.ServerData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import main.java.bgu.spl.protocol.ProtocolCallback;
import main.java.bgu.spl.tokenizer.StringMessage;

/**
 * @author EvilBamba
 *
 *	@code implemented as a Singleton 
 *	      this class contains all the data concerning the players and game rooms 
 */
public class gameData {
	private ConcurrentHashMap<ProtocolCallback<StringMessage>, Player> clients;
	private ConcurrentHashMap<String, Room> rooms;
	private ConcurrentHashMap<String, Class<? extends TBGP>> games;
	private ArrayList<String> names;
	/**
	 * private default constructor 
	 */
	private gameData() {
		clients = new ConcurrentHashMap<ProtocolCallback<StringMessage>, Player>();
		rooms = new ConcurrentHashMap<String, Room>();
		games = new ConcurrentHashMap<String, Class<? extends TBGP>>();
		names = new ArrayList<String>();
		games.put("BLUFFER", BLUFFER.class);
	}

	private static class SingletonHolder {
		private static gameData instance = new gameData();
	}

	public static gameData getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * called when a client wants to register with a new nickname
	 * @param nick - the requested nickname
	 * @param callback - the client's callback
	 */
	public synchronized void NICK(StringMessage nick, ProtocolCallback<StringMessage> callback) {
		if (clients.containsKey(callback) || names.contains(nick.getMessage()))
			SYSMSG("NICK REJECTED", callback);
		else {
			clients.put(callback, new Player(callback, nick.getMessage()));
			names.add(nick.getMessage());
			SYSMSG("NICK ACCEPTED", callback);
		}
	}

	/**
	 * called when a client wants to join to a new or existing room
	 * @param room - the requested room
	 * @param callback - the client's callback
	 */
	public synchronized void JOIN(StringMessage room, ProtocolCallback<StringMessage> callback) {
		if (clients.containsKey(callback)) {
			Room r = rooms.get(room.getMessage());
			Player t = clients.get(callback);
			if (r != null) {
				if (t.getCurrRoom() != null) {
					if (rooms.get(t.getCurrRoom()).isActive() || rooms.get(room.getMessage()).isActive()){
						SYSMSG("JOIN REJECTED", callback);}
					else {
						rooms.get(t.getCurrRoom()).removePlayer(callback);
						rooms.get(room.getMessage()).addPlayer(callback, clients.get(callback));
					}
				} else {
					if (!rooms.get(room.getMessage()).isActive())
						rooms.get(room.getMessage()).addPlayer(callback, clients.get(callback));
				}
			} else {
				if (t.getCurrRoom() == null || !rooms.get(t.getCurrRoom()).isActive()) {
					rooms.put(room.getMessage(), new Room(room.getMessage()));
					if (t.getCurrRoom() != null)
						rooms.get(t.getCurrRoom()).removePlayer(callback);
					rooms.get(room.getMessage()).addPlayer(callback, clients.get(callback));
				}
				else
					SYSMSG("JOIN REJECTED", callback);
					
			}
		} else
			SYSMSG("JOIN REJECTED", callback);
	}

	/**
	 * called when a client wants to send a message to the other players in the room
	 * @param msg - the message to send
	 * @param callback - the clients callback
	 */
	public synchronized void MSG(StringMessage msg, ProtocolCallback<StringMessage> callback) {
		String room = clients.get(callback).getCurrRoom();
		if (room != null) {
			rooms.get(room).sendMessage(msg.getMessage().toLowerCase(), clients.get(callback));
		} else
			SYSMSG("MSG REJECTED", callback);
	}

	/**
	 * called when a client wants to quit
	 * @param callback - the client's callback
	 * @return
	 */
	public synchronized boolean QUIT(ProtocolCallback<StringMessage> callback) {
		if (clients.containsKey(callback)) {
			String room = clients.get(callback).getCurrRoom();
			if (room != null) {
				if (rooms.get(room).removePlayer(callback) == false) {
					SYSMSG("QUIT REJECTED", callback);
					return false;
				}
				names.remove(clients.get(callback).getName());
				clients.remove(callback);

			} else {
				names.remove(clients.get(callback).getName());
				clients.remove(callback);
			}
			SYSMSG("QUIT ACCEPTED", callback);
			return true;
		}
		SYSMSG("QUIT ACCEPTED", callback);
		return true;
	}

	/**
	 * called when a client wants to know which games the server supports 
	 * @param callback - the client's callback
	 */
	public void LISTGAMES(ProtocolCallback<StringMessage> callback) {
		String res = "LISTGAMES ";
		if (!games.isEmpty()) {
			res += "ACCEPTED ";
			for (Entry<String, Class<? extends TBGP>> entry : games.entrySet()) {
				String key = entry.getKey();
				res += key + " ";
			}
			SYSMSG(res, callback);
		} else
			SYSMSG(res + "REJECTED", callback);
	}

	/**Whenever the client sends the command STARTGAME "NAME OF GAME" we will check if our server supports
	 * this game,and if the client not already in room that in the middle of the game.We will get from our map
	 * according to the game name its class and create new instance of this class,then we will send to the room of the
	 * client the game we created.If we failed one of the checks send to the client REJECTED.
	 * @param gameName
	 * @param callback
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public synchronized void StartGame(StringMessage gameName, ProtocolCallback<StringMessage> callback)
			throws InstantiationException, IllegalAccessException {
		String _gamName = gameName.getMessage().toUpperCase();
		if (games.containsKey(_gamName) && clients.containsKey(callback)) {
			String room = clients.get(callback).getCurrRoom();
			if (room != null && !rooms.get(room).isActive()) {
				SYSMSG("STARTGAME ACCEPTED", callback);
				rooms.get(room).StartGame(games.get(_gamName).newInstance());
			} else
				SYSMSG("STARTGAME REJECTED", callback);
		} else
			SYSMSG("STARTGAME REJECTED", callback);
	}

	/**The text respond of the player to the game play,the game itself will react according to the game logic 
	 * to the client text respond
	 * @param msg
	 * @param callback
	 */
	public void TXTRESP(StringMessage msg, ProtocolCallback<StringMessage> callback) {
		if (clients.containsKey(callback)) {
			String room = clients.get(callback).getCurrRoom();
			if (room != null && rooms.get(room).isActive()) {
				rooms.get(room).TXTRESP(msg.getMessage().toLowerCase(), clients.get(callback));
			}
		} else
			SYSMSG("TXTRESP REJECTED", callback);
	}

	/**The select respond of the player to the game play,the game itself will react according to the game logic 
	 * to the client select respond
	 * @param msg
	 * @param callback
	 */
	public void SELECTRESP(int msg, ProtocolCallback<StringMessage> callback) {
		if (clients.containsKey(callback)) {
			String room = clients.get(callback).getCurrRoom();
			if (room != null && rooms.get(room).isActive()) {
				rooms.get(room).SELECTRESP(msg, clients.get(callback));
			}
		} else
			SYSMSG("SELECTRESP REJECTED", callback);
	}

	/**
	 * Handling all the messages in the SYSMSG format 
	 * @param msg - the requested message
	 * @param callback - the callback to return the message
	 */
	public void SYSMSG(String msg, ProtocolCallback<StringMessage> callback) {
		try {
			callback.sendMessage(new StringMessage("SYSMSG " + msg));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**Handling all the messages in the GAMEMSG format 
	 * @param msg
	 * @param callback
	 */
	public void GAMEMSG(String msg, ProtocolCallback<StringMessage> callback) {
		try {
			callback.sendMessage(new StringMessage("GAMEMSG " + msg));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**Handling all the messages in the USRMSG format 
	 * @param msg
	 * @param callback
	 */
	public void USRMSG(String msg, ProtocolCallback<StringMessage> callback) {
		try {
			callback.sendMessage(new StringMessage("USRMSG " + msg));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**Handling all the messages in the ASKCHOICES format 
	 * @param msg
	 * @param callback
	 */
	public void ASKCHOICES(String msg, ProtocolCallback<StringMessage> callback) {
		try {
			callback.sendMessage(new StringMessage("ASKCHOICES " + msg));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**Handling all the messages in the ASKTXT format 
	 * @param msg
	 * @param callback
	 */
	public void ASKTXT(String msg, ProtocolCallback<StringMessage> callback) {
		try {
			callback.sendMessage(new StringMessage("ASKTXT " + msg));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
