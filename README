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
│        │  └─ 📝 MessagType.java   # Classe Enumeration pour les type de messages
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
- Maintient de la réplication des données en cas de départ ou arrivée

# Explications techniques

# Difficultés

# Compétences acquises

- Déploiement de bases de données NoSQL dans une application
- Réplication d’une base de donnée pour sa résilience 
- Utilisation de base de données secondaire pour stockage chaud (logs, gestion des connexions en temps réel)  
- Gestion des sockets entre FrontEnd et BackEnd
- Pipeline d’agrégats pour mongoDB


# Utilisation

Prérequis :
- Java 5 minimum

## Installation :

Cloner le projet dans un repertoire :

`git clone https://github.com/Kaiden26/INFO833_Projet'

## Lancement :

Ajouter les librairies *peersim-1.0.5.jar* *jep-2.3.0.jar* *djep-1.0.0.jar* *peersim-doclet.jar*

Lancer la classe *Simulator* dans src/fr/usmb/peersim avec l'argument config_file.cfg


