package main.java.bgu.spl.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;

import main.java.bgu.spl.protocol.ProtocolCallback;
import main.java.bgu.spl.protocol.ServerProtocol;
import main.java.bgu.spl.tokenizer.MessageTokenizer;

/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 * 
 */
public class ProtocolTask<T> implements Runnable {

	private final ServerProtocol<T> _protocol;
	private final MessageTokenizer<T> _tokenizer;
	private final ConnectionHandler<T> _handler;
	private final ProtocolCallback<T> _callback;

	public ProtocolTask(final ServerProtocol<T> protocol, final MessageTokenizer<T> tokenizer, final ConnectionHandler<T> h) {
		this._protocol = protocol;
		this._tokenizer = tokenizer;
		this._handler = h;
		this._callback= new gameProtocolCallback();  // our gameProtocolCallback
	}

	// we synchronize on ourselves, in case we are executed by several threads
	// from the thread pool.
	public synchronized void run() {
      // go over all complete messages and process them.
      while (_tokenizer.hasMessage()) {
         T msg = _tokenizer.nextMessage();
         this._protocol.processMessage(msg,_callback);  // here we added the callback to the processMessage
      }
	}

	public void addBytes(ByteBuffer b) {
		_tokenizer.addBytes(b);
	}
	
	private class gameProtocolCallback implements ProtocolCallback<T>{ // private class that implements ProtocolCallback
																	   // the sendMessage will add the message to the out vector
		@Override
		public void sendMessage(T msg) throws IOException {
			_handler.addOutData(_tokenizer.getBytesForMessage(msg));
			
		}
		
	}
}
