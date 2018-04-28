public class Occupe_Connection implements Runnable {
/* S'occupe d'attendre des connections par le serveur
   la récupere et créer un thread qui va le gerer Pour
   occupe_joueur et occupe_serveur
 */
private int port;
private Serveur serv;
public void Occupe_Connection(int port, Serveur serv){
	this.port = port;
	this.serv = serv;
}
public void run(){
	try{
		ServerSocket serv = new ServerSocket(4000);
		while(true) {
			Socket so = serv.accept();
			Occupe_Joueur oc_jo = new Occupe_Joueur(so, serv);
			Occupe_Serveur oc_se = new Occupe_Serveur(so, serv);
			Thread t = new Thread(oc_jo);
			Thread t2 = new Thread(oc_se);
			t.start();
			t2.start();
		}
	}catch(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}
}
}
