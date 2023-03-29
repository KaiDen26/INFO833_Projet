package fr.usmb.dht;

import java.util.List;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Random;

public class Controller implements peersim.core.Control{
	
	 private int dhtPid;
	
	 private int executedStep = 0;
	 private final List<Runnable> steps = new ArrayList<>();
	 
	 private ArrayList<Integer> msgUids = new ArrayList<>();

	 public Controller(String prefix) {
	    	
		// recuperation du pid de la couche applicative
	    this.dhtPid = Initializer.getDhtPid();
	    int nodeNb = Network.size();
		
		// pour chaque noeud, on les ajoutent à l'anneau
		for (int i = 1; i < nodeNb; i++) {
			
			DhtNode current = (DhtNode) Network.get(i).getProtocol(this.dhtPid);

		    int randomNodeId = new Random().nextInt(i);
		    
		    //System.out.println(i + " " + randomNodeId);
		    
		    while (randomNodeId == i) {
				randomNodeId = new Random().nextInt(i);
			}
		    
		    int destId = randomNodeId;
			
			Message joinMsg = new Message(MessageType.JOIN, "Joining the network !", current);
			this.steps.add(() -> sendMsg(current, joinMsg, Network.get(destId)));
			
		} 
		
		Message treeMsg = new Message(MessageType.SHOW_TREE, "");
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid), treeMsg, Network.get(0)));
		
		Message leaveMsg = new Message(MessageType.LEAVE, "");
		this.steps.add(() -> sendMsg((DhtNode) Network.get(3).getProtocol(this.dhtPid), leaveMsg, Network.get(3)));
		
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid), treeMsg, Network.get(0)));
		
		
		Message joinMsg = new Message(MessageType.JOIN, "Joining the network !", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg((DhtNode) Network.get(3).getProtocol(this.dhtPid), joinMsg, Network.get(0)));
		
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid), treeMsg, Network.get(0)));
		
		DhtNode node2 = (DhtNode) Network.get(2).getProtocol(this.dhtPid);
		
		Message deliverMsg = new Message(MessageType.DELIVER, "Hello dude, wanna meet ?", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		deliverMsg.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg, Network.get(2)));
		
		Message deliverMsg2 = new Message(MessageType.DELIVER, "Hello dude, wanna meet ?", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		deliverMsg2.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg2, Network.get(2)));
		
		
		DhtNode node3 = (DhtNode) Network.get(3).getProtocol(this.dhtPid);
		Message leaving = new Message(MessageType.LEAVE, "Leaving", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg(node3, leaving, Network.get(3)));
		
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid), treeMsg, Network.get(0)));

		Message joinMsg2 = new Message(MessageType.JOIN, "Joining the network !", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg(node3, joinMsg2, Network.get(8)));
		
		Message deliverMsg3 = new Message(MessageType.DELIVER, "Hello dude, wanna meet ?", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		deliverMsg3.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg3, Network.get(2)));
		
		
		Message deliverMsg4 = new Message(MessageType.DELIVER, 8000.54, (DhtNode) Network.get(4).getProtocol(this.dhtPid));
		deliverMsg4.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg4, Network.get(2)));
		
		Message leaving2 = new Message(MessageType.LEAVE, "Leaving", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg(node3, leaving2, Network.get(3)));
		
		this.steps.add(node2::showInfos);
		
		this.steps.add(node3::showInfos);
		
	}
	 
	public void sendMsg(DhtNode node, Message msg, Node dest) {
		node.send(msg, dest);
	}
	 
	@Override
	public boolean execute() {
		
		if (this.executedStep == this.steps.size()) return false;
		
		this.steps.get(this.executedStep).run();
        this.executedStep++;
		
		return false;
	}

	public int generateNewDataId() {
		int msgUid = new Random().nextInt(Initializer.getNodeNb() * 10);
		while (msgUids.contains(msgUid)) {
			msgUid = new Random().nextInt(Initializer.getNodeNb() * 10);
		}
		msgUids.add(msgUid);
		return msgUid;
	}
}
