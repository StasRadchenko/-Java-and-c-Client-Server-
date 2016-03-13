package main.java.bgu.spl.protocol;

/**
 * 
 A protocol that describes the behavior of the server.**
 @param<T>type of message that the protocol handles.
*/
public interface ServerProtocol<T> {
	
	void processMessage(T msg, ProtocolCallback<T> callback);
	boolean isEnd(T msg);
}