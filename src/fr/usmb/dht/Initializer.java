package fr.usmb.dht;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Random;

import peersim.config.*;

	/*
	  Module d'initialisation de helloWorld: 
	  Fonctionnement:
	    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
	    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
	 */

public class Initializer implements peersim.core.Control {
    
    private int dhtPid;
    
    private ArrayList<Integer> nodeUids = new ArrayList<>();

    public Initializer(String prefix) {
    	
    	// recuperation du pid de la couche applicative
    	this.dhtPid = Configuration.getPid(prefix + ".dhtProtocolPid");
	
    }

    public boolean execute() {
    	
    	// recuperation de la taille du reseau
		int nodeNb = Network.size();
		
		DhtNode current;
		Node destination;
		
		if (nodeNb < 1) {
		    System.err.println("Network size is not positive");
		    System.exit(1);
		}
	
		// recuperation de la couche applicative de l'emetteur (le noeud 0)
		DhtNode node0 = (DhtNode)Network.get(0).getProtocol(this.dhtPid);
		node0.setTransportLayer(0);
		node0.setNeighbors(node0, node0);
		/*int node0Uid = new Random().nextInt(1000);
		while (nodeUids.contains(node0Uid)) {
			node0Uid = new Random().nextInt(1000);
		}
		node0.setNodeUid(node0Uid);*/
		
		node0.setNodeUid(5);
		
		
		DhtNode node1 = (DhtNode)Network.get(1).getProtocol(this.dhtPid);
		node1.setTransportLayer(1);
		node1.setNodeUid(3);
			
		Message joinMsg = new Message(MessageType.JOIN, "I'm joining the network !", node1);
		node1.send(joinMsg, Network.get(0));
		
		DhtNode node2 = (DhtNode)Network.get(2).getProtocol(this.dhtPid);
		node2.setTransportLayer(2);
		node2.setNodeUid(7);
			
		Message joinMsg2 = new Message(MessageType.JOIN, "I'm joining the network !", node2);
		node2.send(joinMsg2, Network.get(0));
		
		DhtNode node3 = (DhtNode)Network.get(3).getProtocol(this.dhtPid);
		node3.setTransportLayer(3);
		node3.setNodeUid(6);
			
		Message joinMsg3 = new Message(MessageType.JOIN, "I'm joining the network !", node3);
		node3.send(joinMsg3, Network.get(0));
			
		
		// pour chaque noeud, on fait le lien entre la couche applicative et la couche transport
		
		/*int j = 100;
		for (int i = 1; i < nodeNb; i++) {
			
			//On fait en sorte que le uid soit unique
			int nodeUid = new Random().nextInt(1000);
			while (nodeUids.contains(nodeUid)) {
				nodeUid = new Random().nextInt(1000);
			}
			nodeUids.add(nodeUid);
		    current = (DhtNode) Network.get(i).getProtocol(this.dhtPid);
		    current.setTransportLayer(i);
		    current.setNodeUid(nodeUid);
		    
		    int randomNodeId = new Random().nextInt(nodeNb);
		    
		    DhtNode nextNode;
		    
		    nextNode = ((DhtNode) Network.get(randomNodeId).getProtocol(this.dhtPid));
		    
			while (randomNodeId == i || nextNode.getRightNeighor() == null || nextNode.getLeftNeighor() == null) {
				randomNodeId = new Random().nextInt(nodeNb);
				nextNode = ((DhtNode) Network.get(randomNodeId).getProtocol(this.dhtPid));
			}
			
			current = (DhtNode) Network.get(i).getProtocol(this.dhtPid);
			
			Message joinMsg = new Message(MessageType.JOIN, "I'm joining the network !", current);
			current.send(joinMsg, Network.get(randomNodeId));
			//EDSimulator.add((j + 1000), joinMsg, Network.get(randomNodeId), 0);
			
			
		}*/
		
		
		
	
		System.out.println("Initialization completed");
		return false;
    }
    
}