package main.java.bgu.spl.ServerData;


/**
 * @author	EvilBamba 
 *	a game interface for each text-based game to implement
 */
public interface TBGP {
	public void play();//starting the game
	public void SELECTRESP(int msg,Player player);//the selectresp for the game
	public void TXTRESP(String msg, Player player);//the text resp from the client
	public void initialize(Room r);//every game in initialize should get the room
	
}
