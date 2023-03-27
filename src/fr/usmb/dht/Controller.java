package fr.usmb.dht;

import java.util.List;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Random;

public class Controller implements peersim.core.Control{
	
	 private int dhtPid;
	
	 private int executedStep = 0;
	 private final List<Runnable> steps = new ArrayList<>();

	 public Controller(String prefix) {
	    	
		// recuperation du pid de la couche applicative
	    this.dhtPid = Initializer.getdhtPid();
	    int nodeNb = Network.size();
		
		// pour chaque noeud, on les ajoutent à l'anneau
		for (int i = 1; i < nodeNb; i++) {
			
			DhtNode current = (DhtNode) Network.get(i).getProtocol(this.dhtPid);

		    int randomNodeId = new Random().nextInt(i);
		    
		    //System.out.println(i + " " + randomNodeId);
		    
		    while (randomNodeId == i) {
				randomNodeId = new Random().nextInt(i);
				
				DhtNode nextNode = ((DhtNode) Network.get(randomNodeId).getProtocol(this.dhtPid));
				System.out.println(i + " " + randomNodeId);
			}
		    
		    int destId = randomNodeId;
			
			Message joinMsg = new Message(MessageType.JOIN, "I'm joining the network !", current);
			this.steps.add(() -> sendMsg(current, joinMsg, Network.get(destId)));
			
		} 
		
		Message treeMsg = new Message(MessageType.SHOW_TREE, "");
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid),treeMsg,Network.get(0)));
		 
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

}
