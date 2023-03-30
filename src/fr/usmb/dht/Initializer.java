package fr.usmb.dht;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Random;

import peersim.config.*;

	/*
	  Module d'initialisation de DhtNode: 
	  Fonctionnement:
	    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
	    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
	 */

public class Initializer implements peersim.core.Control {
    
    private static int dhtPid;
    private static int nodeNb;
    
    private ArrayList<Integer> nodeUids = new ArrayList<>();

    public Initializer(String prefix) {
    	
    	// recuperation du pid de la couche applicative
    	dhtPid = Configuration.getPid(prefix + ".dhtProtocolPid");
	
    }
    
    public boolean execute() {
    	
    	// recuperation de la taille du reseau
		nodeNb = Network.size();
		
		if (nodeNb < 1) {
		    System.err.println("Network size is not positive");
		    System.exit(1);
		}
	
		// recuperation de la couche applicative de l'emetteur (le noeud 0)
		DhtNode node0 = (DhtNode)Network.get(0).getProtocol(dhtPid);
		node0.setNeighbors(node0, node0);
		node0.setTransportLayer(0);
		int node0Uid = new Random().nextInt(nodeNb * 10);
		node0.setNodeUid(node0Uid);
		nodeUids.add(node0Uid);
		
		//pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative et leur donne un uid unique
		for (int i = 1; i < nodeNb; i++) {
			
			//On fait en sorte que le uid soit unique
			int nodeUid = new Random().nextInt(nodeNb * 10);
			while (nodeUids.contains(nodeUid)) {
				nodeUid = new Random().nextInt(nodeNb * 10);
			}
			nodeUids.add(nodeUid);
			DhtNode current = (DhtNode) Network.get(i).getProtocol(dhtPid);
		    current.setTransportLayer(i);
		    current.setNodeUid(nodeUid);
		
		}
		
		System.out.println("Initialization completed");
		return false;
    }
    
    public static int getNodeNb() {
    	return nodeNb;
    }
    
    public static int getDhtPid() {
    	return dhtPid;
    }
    
}