package main.java.bgu.spl.protocol;

public interface ProtocolCallback<T> {
	void sendMessage(T msg) throws java.io.IOException;
}
