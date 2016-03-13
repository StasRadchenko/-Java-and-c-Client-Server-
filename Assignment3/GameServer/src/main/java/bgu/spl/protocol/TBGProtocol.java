package main.java.bgu.spl.protocol;

import main.java.bgu.spl.ServerData.gameData;
import main.java.bgu.spl.tokenizer.StringMessage;

/**
 * this is our game protocol , it handles all the messages received from the
 * client the protocol know if the connection should be closed or terminated
 * 
 * @author EvilBamba
 *
 */
public class TBGProtocol implements ServerProtocol<StringMessage>, AsyncServerProtocol<StringMessage> {
	private boolean shouldClose = false;
	private boolean _connectionTerminated = false;

	public TBGProtocol() {
	}// empty constructor

	/**
	 * for each command it will invoke the proper function
	 * 
	 * @_msg - the received message
	 * @_callback - the client's callback
	 */
	public void processMessage(StringMessage _msg, ProtocolCallback<StringMessage> callback) {
		if (_connectionTerminated)
			return;

		StringMessage command;
		StringMessage msg;
		if (_msg.getMessage().contains(" ")) {
			command = new StringMessage(_msg.getMessage().substring(0, _msg.getMessage().indexOf(' ')));
			msg = new StringMessage(_msg.getMessage().substring(_msg.getMessage().indexOf(' ') + 1));
		} else {
			command = new StringMessage(_msg.getMessage());
			msg = new StringMessage(_msg.getMessage());

		}

		switch (command.getMessage()) {

		/**
		 * tries to register the client with the given nickname
		 */
		case "NICK":
			if (!(msg.getMessage().isEmpty()) && msg != null && !msg.equals(command)) {
				gameData.getInstance().NICK(msg, callback);
			} else {
				gameData.getInstance().SYSMSG("NICK REJECTED", callback);
			}
			break;

		/**
		 * tries to register the client to the given room
		 */
		case "JOIN":
			if (!msg.getMessage().equals(command.getMessage())) {
				gameData.getInstance().JOIN(msg, callback);
			} else {
				gameData.getInstance().SYSMSG("JOIN REJECTED", callback);
			}
			break;

		case "MSG":
			gameData.getInstance().MSG(msg, callback);
			break;

		/**
		 * return a list of the supported games
		 */
		case "LISTGAMES":
			gameData.getInstance().LISTGAMES(callback);
			break;

		/**
		 * tries to start a game in the player's game room
		 */
		case "STARTGAME":
			try {
				gameData.getInstance().StartGame(msg, callback);
			} catch (InstantiationException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IllegalAccessException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			break;

		/**
		 * tries to disconnect
		 */
		case "QUIT":
			boolean ans = gameData.getInstance().QUIT(callback);
			if (ans == true)
				shouldClose = true;
			else
				shouldClose = false;

			break;

		/**
		 * sends the client's TXTRESP to the game
		 */
		case "TXTRESP":
			gameData.getInstance().TXTRESP(msg, callback);
			break;

		/**
		 * sends the client's SELECTRESP to the game
		 */
		case "SELECTRESP":
			if (tryParse(msg.getMessage()) != null && tryParse(msg.getMessage())>=0)
				gameData.getInstance().SELECTRESP(tryParse(msg.getMessage()), callback);
			else
				gameData.getInstance().SYSMSG("SELECTRESP REJECTED", callback);
			break;

		/**
		 * @return - UNIDENTIFIED for each unrecognized case
		 */
		default:
			gameData.getInstance().SYSMSG(_msg + " UNIDENTIFIED", callback);

		}
	}

	/*
	 * return if the connection should close
	 * 
	 * @see protocol.AsyncServerProtocol#shouldClose()
	 */
	public boolean shouldClose() {
		return shouldClose;
	}

	/*
	 * return if the client can End
	 * 
	 * @see protocol.AsyncServerProtocol#shouldClose()
	 */
	public boolean isEnd(StringMessage msg) {
		return (msg.getMessage().equals("QUIT") && shouldClose);
	}

	/*
	 * changes a SELECTRESP to an integer
	 * 
	 * @see protocol.AsyncServerProtocol#shouldClose()
	 */
	private Integer tryParse(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/*
	 * terminates the connection
	 */
	public void connectionTerminated() {
		_connectionTerminated = true;
	}

}
