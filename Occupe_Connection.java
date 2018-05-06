import java.io.*;
import java.net.*;
import java.util.*;
public class Occupe_Connection implements Runnable {
/* S'occupe d'attendre des connections par le serveur
   la récupere et créer un thread qui va le gerer Pour
   occupe_joueur et occupe_serveur
 */
private Serveur serv;
private int port;
public Occupe_Connection(Serveur serv, int port){
	this.serv = serv;
	this.port = port;
}
public void run(){
	try{
		ServerSocket serveur = new ServerSocket(port);
		while(true) {
			Player player = new Player();
			Socket so = serveur.accept();
			Occupe_Joueur oc_jo = new Occupe_Joueur(so, serv, player);
			player.setOJ(oc_jo);
			Thread t = new Thread(oc_jo);
			t.start();
		}
	}catch(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}
}
public int getPort(){
	return port;
}
}
