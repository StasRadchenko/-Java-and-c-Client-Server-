package main.java.bgu.spl.Thread_Per_Client;
import java.io.*;
import java.net.*;

import main.java.bgu.spl.protocol.GameProtocolFactory;
import main.java.bgu.spl.protocol.ServerProtocolFactory;
import main.java.bgu.spl.tokenizer.StringMessage;
import main.java.bgu.spl.tokenizer.TokenizerFactory;
import main.java.bgu.spl.tokenizer.gameTokenizerFactory;


 
class MultipleClientProtocolServer implements Runnable {
    private ServerSocket serverSocket;
    private int listenPort;
    private  final ServerProtocolFactory<StringMessage> pFactory;//Factory creating our protocols
    private final TokenizerFactory<StringMessage> tFactory;//Factory creating tokenizer
    
    
    public MultipleClientProtocolServer(int port, GameProtocolFactory<StringMessage> p,TokenizerFactory<StringMessage> t)
    {
        serverSocket = null;
        listenPort = port;
        pFactory = p;
        tFactory =t;
    }
    
    public void run()
    {
        try {
            serverSocket = new ServerSocket(listenPort);
            System.out.println("Listening...");
        }
        catch (IOException e) {
            System.out.println("Cannot listen on port " + listenPort);
        }
        
        while (true)
        {
            try {
                ConnectionHandler newConnection = new ConnectionHandler(serverSocket.accept(), pFactory.create(),tFactory.create());
                
            new Thread(newConnection).start();
            }
            catch (IOException e)
            {
                System.out.println("Failed to accept on port " + listenPort);
            }
        }
    }
    
 
    // Closes the connection
    public void close() throws IOException
    {
        serverSocket.close();
    }
    
    public static void main(String[] args) throws IOException
    {
        // Get port
        int port = Integer.decode(args[0]).intValue();
        
        MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new GameProtocolFactory<StringMessage>(), new gameTokenizerFactory());
        Thread serverThread = new Thread(server);
        serverThread.start();
        try {
            serverThread.join();
        }
        catch (InterruptedException e)
        {
            System.out.println("Server stopped");
        }
        
        
                
    }
}