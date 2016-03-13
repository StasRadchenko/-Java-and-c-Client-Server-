package main.java.bgu.spl.Thread_Per_Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import main.java.bgu.spl.protocol.ProtocolCallback;
import main.java.bgu.spl.protocol.ServerProtocol;
import main.java.bgu.spl.tokenizer.MessageTokenizer;
import main.java.bgu.spl.tokenizer.StringMessage;


class ConnectionHandler implements Runnable {

	private BufferedReader in;
	private PrintWriter out;
	private Socket _clientSocket;
	private ServerProtocol<StringMessage> _protocol;//our protocol for processing messages from our client
	private ProtocolCallback<StringMessage> _callback;//our way to send the responds to our client
	private MessageTokenizer<StringMessage> _tokenizer;

	public ConnectionHandler(Socket acceptedSocket, ServerProtocol<StringMessage> p, MessageTokenizer<StringMessage> t) {
		_tokenizer = t;
		in = null;
		out = null;
		_clientSocket = acceptedSocket;
		_protocol = p;
		System.out.println("Accepted connection from client!");
		System.out.println("The client is from: " + acceptedSocket.getInetAddress() + ":" + acceptedSocket.getPort());
	}

	public void run() {

		try {
			initialize();
		} catch (IOException e) {
			System.out.println("Error in initializing I/O");
		}

		try {
			process();
		} catch (IOException e) {
			System.out.println("Error in I/O");
		}

		System.out.println("Connection closed - bye bye...");
		close();
	}

	/**while the is an message from the client,get the bytes using the tokenizer and put the message into the
	 * tokenizer.while the tokenizer has message we will process it using our protocol.
	 * if we got "QUIT" from the client we will close the connection 
	 * 
	 * @throws IOException
	 */
	public void process() throws IOException {

		String msg;

		while ((msg = in.readLine()) != null) {
			_tokenizer.addBytes(_tokenizer.getBytesForMessage(new StringMessage(msg)));
			System.out.println("Received \"" + msg + "\" from client");
			if (_tokenizer.hasMessage()) {

				_protocol.processMessage(_tokenizer.nextMessage(), _callback);

				if (_protocol.isEnd(new StringMessage(msg))) {
					break;
				}
			}
		}
	}

	// Starts listening
	public void initialize() throws IOException {
		// Initialize I/O
		in = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream(), "UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(_clientSocket.getOutputStream(), "UTF-8"), true);
		_callback = new gameCallback();//creating the callback for this client

		System.out.println("I/O initialized");
	}

	// Closes the connection
	public void close() {
		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}

			_clientSocket.close();
		} catch (IOException e) {
			System.out.println("Exception in closing I/O");
		}
	}
	
	private class gameCallback implements ProtocolCallback<StringMessage>{

			/* sends the respond after the process in protocol to the client 
			 * @see protocol.ProtocolCallback#sendMessage(java.lang.Object)
			 */
			public void sendMessage(StringMessage msg) throws IOException {
				out.println(msg.getMessage());
				
			}
	}


}
