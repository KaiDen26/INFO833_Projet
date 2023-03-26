package fr.usmb.dht;

public class Message {

    public MessageType type;
    private String content;

    Message(MessageType type, String content) {
    	this.type = type;
		this.content = content;
    }

    public String getContent() {
    	return this.content;
    }

    
}