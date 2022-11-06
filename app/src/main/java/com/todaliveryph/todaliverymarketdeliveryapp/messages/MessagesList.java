package com.todaliveryph.todaliverymarketdeliveryapp.messages;

public class MessagesList {

    private String mobile, name, lastMessage,profilePic,chatKey;

    private int unseenMessages;

    public MessagesList(String mobile, String name, String lastMessage,String profilePic, int unseenMessages,String chatKey) {
        this.mobile = mobile;
        this.name = name;
        this.lastMessage = lastMessage;
        this.profilePic = profilePic;
        this.unseenMessages = unseenMessages;
        this.chatKey = chatKey;
    }

    public String getMobile() {
        return mobile;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getProfilePic() {return profilePic;}

    public int getUnseenMessages() {
        return unseenMessages;
    }

    public String getChatKey() {return chatKey;}
}
