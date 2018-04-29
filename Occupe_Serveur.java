import java.util.*;
import java.net.*;
import java.io.*;

class Occupe_Serveur implements Runnable {
    private Serveur serveur;
    private Socket so;
    
    public Occupe_Serveur (Socket so, Serveur serv) {
	this.so = so;
	this.serveur = serv;
    }

    public void run() {
	System.out.println ("On attend un truc");
	
    }
}
