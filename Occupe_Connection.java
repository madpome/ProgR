import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
public class Occupe_Connection implements Runnable {
/* S'occupe d'attendre des connections par le serveur
   la récupere et créer un thread qui va le gerer Pour
   occupe_joueur et occupe_serveur
 */
private Serveur serv;
public Occupe_Connection(Serveur serv){
	this.serv = serv;
}
public void run(){
	try{
		ServerSocketChannel serv = ServerSocketChannel.open();
		serv.socket().bind(new InetSocketAddress(4000));
		while(true) {
			Player player = new Player();
			SocketChannel so = serv.accept();
			Occupe_Joueur oc_jo = new Occupe_Joueur(so, serv, player);
			player.setOJ(oc_jo);
			Thread t = new Thread(oc_jo);
			t.start();
			serv.addPlayer(player);
		}
	}catch(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}
}
}
