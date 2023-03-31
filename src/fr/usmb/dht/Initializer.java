package fr.usmb.dht;

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
    
	 /*
     * Pid de la dht
     */
    private static int dhtPid;
    
    /*
     * Nombre de noeuds dans la dht
     */
    private static int nodeNb;
    
    /*
     * Liste des uids des noeuds
     */
    private static ArrayList<Integer> nodeUids = new ArrayList<>();

    /*
     * Constructeur
     */
    public Initializer(String prefix) {
    	
    	// recuperation du pid de la couche applicative
    	dhtPid = Configuration.getPid(prefix + ".dhtProtocolPid");
	
    }
    
    /*
     * Fonction principale du simulateur
     */
    public boolean execute() {
    	
    	// Recuperation de la taille du reseau
		nodeNb = Network.size();
		
		// Verification de la taille de la dht
		if (nodeNb < 1) {
		    System.err.println("Network size is not positive");
		    System.exit(1);
		}
	
		// Recuperation de la couche applicative de l'emetteur (le noeud 0)
		DhtNode node0 = (DhtNode)Network.get(0).getProtocol(dhtPid);
		
		// On définit les voisins de notre premier noeud
		// Il aura lui même comme voisin
		node0.setNeighbors(node0, node0);
		
		// On attribut une couche de transport à notre noeud
		node0.setTransportLayer(0);
		
		// On attribut une valeur aléatoire à l'uid de notre noeud dans un pool 10 fois supérieur au nombre de noeud
		int node0Uid = new Random().nextInt(nodeNb * 10);
		node0.setNodeUid(node0Uid);
		nodeUids.add(node0Uid); // On ajoute cette uid à la liste des uids
		
		// Pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative et leur donne un uid unique
		for (int i = 1; i < nodeNb; i++) {
			
			//¨On fait en sorte que le uid soit unique
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
    
    /*
     * Retourne le nombre de noeuds de la dht
     */
    public static int getNodeNb() {
    	return nodeNb;
    }
    
    /*
     * Retourne le pid de la dht
     */
    public static int getDhtPid() {
    	return dhtPid;
    }
    
}