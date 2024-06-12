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
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SClient {

    int id;
    public String name = "NoName";
    public int turn = 1;
    Socket soket;
    ObjectOutputStream sOutput;
    ObjectInputStream sInput;
    Listen listenThread; // thread for listening from client
    SClient rival;
    public boolean paired = false;
    public String getMessageUserName = "";

    public SClient(Socket gelenSoket, int id) {
        this.soket = gelenSoket;
        this.id = id;
        try {
            this.sOutput = new ObjectOutputStream(this.soket.getOutputStream());
            this.sInput = new ObjectInputStream(this.soket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        //thread nesneleri
        this.listenThread = new Listen(this);

    }

    public void Send(Message message) {  // sending message to client
        try {
            this.sOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // thread for listening to clients
    class Listen extends Thread {

        SClient TheClient;

        Listen(SClient TheClient) {
            this.TheClient = TheClient;
        }

        public void run() {
            //while client is connected
            while (TheClient.soket.isConnected()) {
                try {
                    //waiting for message
                    Message received = (Message) (TheClient.sInput.readObject());
                    //if message came, check the type of message
                    switch (received.type) {
                        case Connect:
                            TheClient.name = received.content.toString();
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                Server.Send(Server.Clients.get(i), received);
                            }
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                if (Server.Clients.get(i) != TheClient) {
                                    Message msg = new Message(Message.Message_Type.GetOldUsers);
                                    msg.content = Server.Clients.get(i).name;
                                    Server.Send(TheClient, msg);
                                }
                            }
                            for (int i = 0; i < Server.Groups.size(); i++) {
                                Message msg = new Message(Message.Message_Type.GetOldGroups);
                                msg.content = Server.Groups.get(i);
                                Server.Send(TheClient, msg);
                            }
                            break;
                        case GroupCreated:
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                Server.Send(Server.Clients.get(i), received);
                            }
                            Server.Groups.add(received.content.toString());
                            break;
                        case SendGroupName:
                            String msgName = received.content.toString();
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                Server.Send(Server.Clients.get(i), received);
                            }
                            break;
                        case SendGroupMessage:
                            Message msg = new Message(Message.Message_Type.SendGroupMessage);
                            msg.content = TheClient.name + ": " + received.content.toString();
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                Server.Send(Server.Clients.get(i), msg);
                            }
                            break;
                        case SendUserName:
                            String msgUserName = received.content.toString();
                            getMessageUserName = msgUserName;
                            Message msgg = new Message(Message.Message_Type.SendUserName);
                            msgg.content = TheClient.name;
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                if (Server.Clients.get(i).name.equals(getMessageUserName)) {
                                    Server.Send(Server.Clients.get(i), msgg);
                                }
                            }
                            break;
                        case SendUserMessage:
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                if (Server.Clients.get(i).name.equals(getMessageUserName)) {
                                    Server.Send(Server.Clients.get(i), received);
                                }
                            }
                            break;
                        case ClientDisconnect:
                            String user = received.content.toString();
                            int removeInx = -1;
                            for (int i = 0; i < Server.Clients.size(); i++) {
                                if (!Server.Clients.get(i).name.equals(user)) {
                                    Server.Send(Server.Clients.get(i), received);
                                } else {
                                    removeInx = i;
                                }
                            }
                            Server.Clients.remove(removeInx);
                            break;
                        case File:

                            break;
                    }

                } catch (EOFException ex) {
                    // Handle end of stream gracefully (e.g., close the connection)
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                    //if client closed, remove from list
                    Server.Clients.remove(TheClient);

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                    //if client closed, remove from list
                    Server.Clients.remove(TheClient);
                }
            }

        }
    }

}
