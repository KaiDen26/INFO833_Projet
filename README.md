# Projet INFO833 Construction d'une DHT
GUERIOT Benjamin, NICOLAS Thomas

Projet réalisé en groupe. Le but de ce projet est de concevoir et implémenter une DHT en dessus de PeerSim. C'est à dire créer un anneau composé de noeuds, chaque noeud ayant un identifiant unique.

Cet anneau va servir à stocker des données sur les noeuds en fonction de leurs indentifiants afin d'avoir une répartition équitable sur l'anneau.

# Architecture

## Arborescence

```python
📦 INFO833_Projet
├─ 📁 src
│  └─ 📁 fr
│     └─ 📁 usmb
│        ├─ 📁 dht  # Fichiers composant la DHT
│        │  ├─ 📝 Controller.java   # Crée et envoi les actions au simulateur
│        │  ├─ 📝 DhtNode.java      # Gère les actions des nodes
│        │  ├─ 📝 HWTransport.java  # Conditionne l'envoi des messages
│        │  ├─ 📝 Initializer.java  # Initialise les nodes de l'anneau
│        │  ├─ 📝 Message.java      # Objet Message
│        │  └─ 📝 MessagType.java   # Classe Enumeration pour les types de messages
│        └─ 📁 peersim   # Fichiers d'execution peersim
│           └─ 📝 Simulator.java  # Fichier de simulation
└─ 📝 config_file.cfg  # Fichier de configuration
```

## Technologies

Pour simuler notre DHT, nous utilisons le simulateur *Peersim* qui est un simulateur paire à paire pouvant simuler un très large nombre de nodes.

# Fonctionnalités développées

- Insertion de noeuds dans l'anneau
- Extraction de noeuds
- Envoi de messages entre deux noeuds
- Stockage de données par leurs identifiants
- Réplication des données avec un degré de 3
- Création et utilisation de tables de routage
- Maintient du routage en cas de départ ou arrivée de noeuds
- Maintient de la réplication des données en cas de départ ou arrivée de noeuds

# Explications techniques

## Initialisation

Afin de faire fonctionner notre DHT, il est nécessaire d'attribuer à chaque noeud un uid unique. Le premier noeud crée est le noeud initial de notre anneau, celui-ci est son propre voisin de gauche et son propre voisin de droite.

## JOIN / LEAVE

Lorsqu'un noeud rejoint la DHT, il doit être placé à la position correspondant à son uid. Ce noeud demande à un noeud aléatoire de la DHT à le placer, cette demande est effectué gràce un l'envoi d'un message de type **JOIN** au noeud choisi.
Le noeud aléatoire vérifie les uid de ses voisins de gauche et de droite pour savoir si le noeud doit être placé entre l'un des deux. Si le noeud à placer n'est pas situé entre le noeud actuel et l'un des ses voisins, le message est envoyé au voisin avec l'uid le plus proche de l'uid du noeud à placer.

Une fois l'emplacement pour le nouveau noeud trouvé, On envoi un message de type **PLACE_RIGHT** ou **PLACE_LEFT** avec le noeud à placé aux noeuds devenant les voisins de celui à placer, On envoi également un message de type **PLACE_BOTH** au noeud à placer pour qu'il change ses voisins.

## SEND / DELIVER



## PUT / GET



## ADVANCED ROUTING



# Difficultés

 - Acces concurrent sur le meme noeud
 - 

# Evolutions potentielles

- Mise en place du routing lors d'un JOIN
- Ajout d'un flag et une file d'attente sur chaque noeud pour gérer les acces concurrent

# Compétences acquises

- Compréhension du fonctionnement d'une DHT
- Utilisation de PeerSim 

# Utilisation

Prérequis :
- Java 5 minimum

## Installation

Cloner le projet dans un repertoire :

`git clone https://github.com/Kaiden26/INFO833_Projet`

## Lancement

Ajouter les librairies *peersim-1.0.5.jar* *jep-2.3.0.jar* *djep-1.0.0.jar* *peersim-doclet.jar*

Lancer la classe *Simulator* dans src/fr/usmb/peersim avec l'argument config_file.cfg


