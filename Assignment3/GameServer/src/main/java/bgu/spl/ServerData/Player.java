package main.java.bgu.spl.ServerData;

import main.java.bgu.spl.protocol.ProtocolCallback;
import main.java.bgu.spl.tokenizer.StringMessage;

/**Object player.Each client get its own object Player,that holds the NICK that the client took.If the client 
 * joins room we will update its current room(String name of the room).In addition we hold boolean flag if the client
 * got the right answer in this round.The object contains in addition a callback for this specific client
 * @author EvilBamba
 *
 */
public class Player {
	private String currRoom;
    private final String playerName;
    private final ProtocolCallback<StringMessage> playerCallback;
    private boolean roundWinner;
    
    
    /**
     * @param call - the client's callback
     * @param name - the client server nickname
     */
    public Player(ProtocolCallback<StringMessage> call , String name){
    	currRoom=null;
    	playerName=name;
    	playerCallback=call;
    	roundWinner=false;
    }

    /**
     * @return if the player won in the last round
     */
    public boolean roundWinner(){
    	return roundWinner;
    } 
    
    /**
     * @return this players nickname in the game server
     */
    public String getName(){
    	return playerName;
    }
    
    /**
     * @return the clients callback
     */
    public ProtocolCallback<StringMessage> getCallback(){
    	return playerCallback;
    }
    
    /**
     * @return this player's current game room
     */
    public String getCurrRoom(){
    	return currRoom;
    }
    
    /**
     * activates when a player change his room
     * @param roomName - the player's current room
     */
    public void setRoom(String roomName){
    	currRoom=roomName;
    }

	/**
	 * @param rWinner - boolean indicating if the player won the last round
	 */
	public void setRoundWinner(boolean rWinner) {
		roundWinner = rWinner;
	}
    
}
