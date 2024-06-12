/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

/**
 *
 * @author yusuf24
 */
import App.ChatApp;
import App.ChatScreen;
import Message.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static chatclient.Client.sInput;
import java.io.EOFException;
import java.io.FileOutputStream;

// thread which is listening messages from server
class Listen extends Thread {

    public void run() {
        while (Client.socket.isConnected()) {
            try {
                //waiting for message
                Message received = (Message) (sInput.readObject());
                //if message came, check the type of message
                switch (received.type) {
                    case Connect:
                        String newUser = received.content.toString();
                        ChatApp.myApp.userModel.addElement(newUser);
                        break;
                    case GroupCreated:
                        String newGroup = received.content.toString();
                        ChatApp.myApp.groupModel.addElement(newGroup);
                        ChatApp.myApp.groupArray.add(newGroup);
                        ChatApp.myApp.groupMessageArray.add("");
                        break;
                    case GetOldUsers:
                        String oldUser = received.content.toString();
                        ChatApp.myApp.userModel.addElement(oldUser);
                        break;
                    case GetOldGroups:
                        String oldGroup = received.content.toString();
                        ChatApp.myApp.groupModel.addElement(oldGroup);
                        ChatApp.myApp.groupArray.add(oldGroup);
                        ChatApp.myApp.groupMessageArray.add("");
                        break;
                    case SendGroupName:
                        ChatApp.myApp.getMessageGroupName = received.content.toString();
                        break;
                    case SendGroupMessage:
                        int indx = 0;
                        String mesg = received.content.toString();
                        for (int i = 0; i < ChatApp.myApp.groupArray.size(); i++) {
                            if (ChatApp.myApp.groupArray.get(i).equals(ChatApp.myApp.getMessageGroupName)) {
                                ChatApp.myApp.groupMessageArray.set(i, (ChatApp.myApp.groupMessageArray.get(i) + mesg + "\n"));
                                indx = i;
                            }
                        }
                        if (ChatScreen.myChatScreen != null) {
                            ChatScreen.myChatScreen.txta_chat.setText(ChatApp.myApp.groupMessageArray.get(indx));
                        }
                        break;
                    case SendUserName:
                        ChatApp.myApp.getMessageUserName = received.content.toString();
                        int result = ChatApp.myApp.checkUserList(received.content.toString());
                        if (result == -1) {
                            ChatApp.myApp.userArray.add(ChatApp.myApp.getMessageUserName);
                            ChatApp.myApp.userMessageArray.add("");
                        }
                        break;
                    case SendUserMessage:
                        int index = 0;
                        String mesag = received.content.toString();
                        for (int i = 0; i < ChatApp.myApp.userArray.size(); i++) {
                            if (ChatApp.myApp.userArray.get(i).equals(ChatApp.myApp.getMessageUserName)) {
                                ChatApp.myApp.userMessageArray.set(i, (ChatApp.myApp.userMessageArray.get(i) + mesag));
                                index = i;
                            }
                        }
                        if (ChatScreen.myChatScreen != null && ChatScreen.myChatScreen.lbl_chat.getText().equals(ChatApp.myApp.getMessageUserName)) {
                            ChatScreen.myChatScreen.txta_chat.setText(ChatApp.myApp.userMessageArray.get(index));
                        }
                        break;
                    case ClientDisconnect:
                        String disconnectUser = received.content.toString();
                        for (int i = 0; i < ChatApp.myApp.userModel.size(); i++) {
                            if (ChatApp.myApp.userModel.get(i).equals(disconnectUser)) {
                                ChatApp.myApp.userModel.remove(i);
                                break;
                            }
                        }
                        break;
                    case File:

                        break;
                }

            } catch (IOException ex) {

                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                break;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }

    }

}

public class Client {

    public static Socket socket;              // every clients need socket
    public static ObjectInputStream sInput;   // for get the content
    public static ObjectOutputStream sOutput; // for send the content
    public static Listen listenMe;            // thread of listening the server

    public static void Start(String ip, int port) {
        try {
            Client.socket = new Socket(ip, port);
            Client.Display("Servera bağlandı");
            Client.sInput = new ObjectInputStream(Client.socket.getInputStream());
            Client.sOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listenMe = new Listen();
            Client.listenMe.start(); // start listening thread

            //sending name to server
            Message msg = new Message(Message.Message_Type.Connect);
            msg.content = ChatApp.myApp.txtb_userName.getText();
            Client.Send(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // for stop the client
    public static void Stop() {
        try {
            if (Client.socket != null) {
                Message msg = new Message(Message.Message_Type.ClientDisconnect);
                msg.content = ChatApp.myApp.txtb_userName.getText();
                Client.Send(msg);
                Client.listenMe.stop();
                Client.socket.close();
                Client.sOutput.flush();
                Client.sOutput.close();
                Client.sInput.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Display(String msg) {
        System.out.println(msg);
    }

    public static void Send(Message msg) { // message sending function
        try {
            Client.sOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
