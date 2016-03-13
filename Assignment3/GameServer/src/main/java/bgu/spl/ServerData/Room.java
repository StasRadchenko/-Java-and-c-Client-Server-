package main.java.bgu.spl.ServerData;

import java.util.concurrent.ConcurrentHashMap;

import main.java.bgu.spl.protocol.ProtocolCallback;
import main.java.bgu.spl.tokenizer.StringMessage;

/**The object room contains rooms name(creating when client  to connect to room that not existing
 * in addition our room contains the clients that in the room and boolean flag that tells us if the game in the 
 * middle of the game,in addition the room contains the game that playing in the room in the current moment. 
 * @author EvilBamba
 *
 */
public class Room {
	private String _name;
	private ConcurrentHashMap<ProtocolCallback<StringMessage>, Player> _players;
	private boolean isActive;
	private TBGP _game;

	/**constructor to initialize all the param for the object
	 * @param name
	 */
	public Room(String name) {
		_name = name;
		_players = new ConcurrentHashMap<ProtocolCallback<StringMessage>, Player>();
		isActive = false;
		_game = null;
	}

	/**indicates if the room in the middle of the game
	 * @return
	 */
	public boolean isActive() {
		return isActive;
	}

	/**Adds clients that willing to join the room,will reject the request if the room currently in the middle of the game
	 * if we succeed we will send to the client that trying to join ACCEPTED else REJECTED
	 * @param callback
	 * @param player
	 */
	public void addPlayer(ProtocolCallback<StringMessage> callback, Player player) {
		if (!isActive()) {
			player.setRoom(_name);
			_players.putIfAbsent(callback, player);
			gameData.getInstance().SYSMSG("JOIN ACCEPTED", callback);
		} else
			gameData.getInstance().SYSMSG("JOIN REJECTED", callback);
	}

	/**Sending message to all players in the room from the current client,excluding the current client itself,
	 * in the end the current client will get SYSMSG ACCEPTED.
	 * @param msg
	 * @param player
	 */
	public void sendMessage(String msg, Player player) {
		_players.forEach((k, v) -> {
			if (!(v.getName().equals(player.getName())))
				gameData.getInstance().USRMSG(player.getName() + ": " + msg, v.getCallback());
		});

		gameData.getInstance().SYSMSG("MSG ACCEPTED", player.getCallback());
	}

	/**The function activated when the player trying to quit the server or moving out from this room,
	 * if the room currently in the middle of the game return to the client with rejected
	 * @param callback
	 * @return
	 */
	public boolean removePlayer(ProtocolCallback<StringMessage> callback) {
		if (isActive() != true) {
			_players.get(callback).setRoom(null);
			_players.remove(callback);
			return true;
		}
		return false;
	}

	/**returns the number of payers in the room.
	 * @return
	 */
	public int numOfPlayers() {
		return _players.size();
	}

	/**return map of players in the room.
	 * @return
	 */
	public ConcurrentHashMap<ProtocolCallback<StringMessage>, Player> getPlayers() {
		return _players;
	}

	/**Starts the game we received from our server data,changes the flag that this room currently in game,
	 * initialize the game and starts the game.
	 * @param game
	 */
	public void StartGame(TBGP game) {
		isActive = true;
		_game = game;
		_game.initialize(this);
		_game.play();
	}

	/**Text respond to the game from the player,sends the respond to the current game
	 * @param msg
	 * @param player
	 */
	public void TXTRESP(String msg, Player player) {
		_game.TXTRESP(msg, player);
	}

	/**Select respond from the player to the game
	 * @param msg
	 * @param player
	 */
	public void SELECTRESP(int msg, Player player) {
		_game.SELECTRESP(msg, player);
	}

	/**Boolean flag the whenever the game is finished change the state of the room to non playing
	 * 
	 */
	public void done() {
		isActive = false;
	}

}
