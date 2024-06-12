/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

/**
 *
 * @author yusuf24
 */
public class Message implements java.io.Serializable {

    //message types enum
    public static enum Message_Type {
        None, Connect, GroupCreated, GetOldUsers, GetOldGroups, ClientDisconnect, SendGroupMessage, SendGroupName, GetMessage, SendUserName, SendUserMessage, File
    }
    //message type
    public Message_Type type;
    //message content
    public Object content;

    public Message(Message_Type t) {
        this.type = t;
    }
}
