/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

/**
 *
 * @author yusuf24
 */
import Message.Message;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author talha
 */
class ServerThread extends Thread {

    public void run() {
        //listen while server is open
        while (!Server.serverSocket.isClosed()) {
            try {
                Server.Display("Waiting for client..");
                // waiting untill client came
                Socket clientSocket = Server.serverSocket.accept();
                // if client came, move this part
                Server.Display("Client connected..");
                //create SClient with connected client
                SClient nclient = new SClient(clientSocket, Server.IdClient);

                Server.IdClient++;
                //add client to list
                Server.Clients.add(nclient);
                //listen message for client
                nclient.listenThread.start();

            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

public class Server extends Thread {

    public static ServerSocket serverSocket;
    public static int IdClient = 0;
    // servers port for listening
    public static int port = 0;
    //thread which is for server listenings
    public static ServerThread runThread;

    public static ArrayList<SClient> Clients = new ArrayList<>();
    public static ArrayList<String> Groups = new ArrayList<>();
    boolean isListening;
    //semafor
    public static Semaphore pairTwo = new Semaphore(1, true);

    // giving port for starting
    public static void Start(int openport) {
        try {
            Server.port = openport;
            Server.serverSocket = new ServerSocket(Server.port);

            Server.runThread = new ServerThread();
            Server.runThread.start();

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Display(String msg) {

        System.out.println(msg);

    }

    Server(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
//    public void Listen() {
//        this.isListening = true;
//        this.start();
//    }
//
//    public void Stop() {
//
//        try {
//            this.isListening = false;
//            this.serverSocket.close();
//        } catch (IOException ex) {
//            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

         // Listen for incoming connections
    public static void Listen() {
        try {
            while (true) {
                // Display("Waiting for client..");
                Socket clientSocket = Server.serverSocket.accept();
                // Display("Client connected..");
                SClient nclient = new SClient(clientSocket, Server.IdClient);
                Server.IdClient++;
                Server.Clients.add(nclient);
                nclient.listenThread.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Listen2() {
        this.isListening = true;
        this.start();
    }
    
    // Stop the server
    public static void Stop() {
        try {
            Server.serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // server to client message
    public static void Send(SClient cl, Message msg) {

        try {
            cl.sOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void SendtoClient(String s, int index) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
