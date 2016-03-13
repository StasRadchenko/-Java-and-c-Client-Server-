package main.java.bgu.spl.protocol;

public class GameProtocolFactory<StringMessage> implements ServerProtocolFactory<StringMessage> {

	/**
	 * creates new TBGProtocol implementing ServerProtocol
	 * @see main.java.bgu.spl.protocol.ServerProtocolFactory#create()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ServerProtocol<StringMessage> create() {
		return (ServerProtocol<StringMessage>) new TBGProtocol();
	}

}
