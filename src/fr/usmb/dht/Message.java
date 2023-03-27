package fr.usmb.dht;

import java.util.ArrayList;
import java.util.List;

public class Message {

    public MessageType type;
    private String content;
    
    private DhtNode target;

    private DhtNode[] neighbors;
    
    private ArrayList<DhtNode> senders = new ArrayList<>();
    
    private int remaining = 0;
    
    private Message data;
    
    Message(MessageType type, String content) {
    	this.type = type;
		this.content = content;
    }
    
    Message(MessageType type, String content, DhtNode nodeToPlace) {
    	this.type = type;
		this.content = content;
		this.target = nodeToPlace;
    }
    
    Message(MessageType type, String content, Message data) {
    	this.type = type;
		this.content = content;
		this.data = data;
    }
    
    Message(MessageType type, String content, DhtNode[] neighbors) {
    	this.type = type;
		this.content = content;
		this.neighbors = neighbors;
    }
    
    public Message data2Save() {
    	return this.data;
    }
    
    public boolean isType(MessageType type) {
    	return this.type == type;
    }
    
    public void addSender(DhtNode node) {
    	this.senders.add(node);
    }
    
    public List<DhtNode> getSenders(){
	   return this.senders;
    }
    
    public DhtNode getLastSender() {
    	return this.senders.get(senders.size() - 1);
    }
    
    public void setRemaining(int remaining) {
    	this.remaining = remaining;
    }
    
    public Integer getRemaining() {
    	return this.remaining;
    }
    
    public void decreaseRemaining() {
    	this.remaining--;
    }

    public String getContent() {
    	return this.content;
    }
    
    public DhtNode getTarget() {
    	return this.target;
    }
    
    public DhtNode[] getNeighbors() {
    	return this.neighbors;
    }
    
    public MessageType getType() {
    	return this.type;
    }
    
    public String toString() {
    	
    	if(!this.senders.get(senders.size() - 1).equals(this.senders.get(0))) {
    		return "[" + this.type.getDescription() + "] from " + this.senders.get(senders.size() - 1) + " about " + this.senders.get(senders.size() - 1).getUid();
    	} else {
    		return "[" + this.type.getDescription() + "] from " + this.senders.get(senders.size() - 1);
    	}
    	
    }

    
}