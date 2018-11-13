import java.util.ArrayList;
import java.awt.Graphics;
import java.net.ServerSocket;
import java.awt.Graphics;
import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.*;
public class Serveur {
private final int minimalWidth = 5;
private final int maximalWidth = 150;
private final int defaultWidth = 10;
private final int minimalHeight = 5;
private final int maximalHeight = 150;
private final int defaultHeight = 10;

private ArrayList<Player> players;
private ArrayList<Game> games;

private ServeurDisplay display;
private Occupe_Connection oc;
private int nextGameId;

public Serveur(int port) {
	nextGameId = 0;

	players = new ArrayList<Player>();
	games = new ArrayList<Game>();
	oc = new Occupe_Connection(this, port);
	Thread t = new Thread(oc);
	t.start();
	InetAddress myIA;
	try{
		myIA =InetAddress.getLocalHost();
	} catch(Exception e) {
		e.printStackTrace();
		return;
	}
	System.out.println("Server launched, IP = "+myIA.getHostAddress() +" port: "+oc.getPort());
	display = new ServeurDisplay(this);
	Thread t2 = new Thread(display);
	t2.start();
}

public void processMessage(Player p, TypeMessage tm ) {


	int count  = 0;
	boolean gameFound = false;
	Game g = getGame(p);

	if (tm instanceof Direction) {
		g = getGame(p);
		if (g!=null) {
			if (g.isPlaying()) {
				g.movePlayer(p, ((Direction) tm).direction, ((Direction) tm).pas);
			}else if (g.isOver()) {
				disconnect(g,p);
			}
		}
	}else if (tm instanceof SizeList) {
		g = getGame(((SizeList) tm).m);
		if (g!=null && !p.isReady()) {
			if (((SizeList) tm).type == TypeMessage.SIZE) {
				g.sendSize(p);
			}else {
				g.sendListOfPlayers(p);
			}
		}else {
			p.send("DUNNO***");
		}
	}else if (tm instanceof All) {
		g = getGame(p);
		if (g != null) {
			if (g.isPlaying()) {
				g.sendAll(p, ((All) tm).message);
				p.send("ALL!***");
			}else if (g.isOver()) {
				disconnect(g,p);
			}
		}
	}else if (tm instanceof Send) {
		g = getGame(p);
		if (g != null) {
			if (g.isPlaying()) {
				if (g.contains(((Send) tm).id)) {
					g.send(p,((Send) tm).id, ((Send) tm).message);
					p.send("SEND!***");
				}else{
					p.send("NOSEND***");
				}
			}else if (g.isOver()) {
				disconnect(g,p);
			}
		}
	}else if (tm instanceof New) {
		g = getGame(p);
		if (g != null || isDuplicata(p.getIp(),((New)tm).port)) {
			p.send("REGNO***");
		} else {
			// c'est bien au pif ici ?
			String multiIP = "234.255.255.255";
			int multiPort;
			do {
				multiPort = (int)(Math.random()*1000)+5000;
			} while((!isNewPort(multiPort)));
			g= new Game(nextGameId++, defaultWidth, defaultHeight, multiIP, multiPort, ((New)tm).isTeam());

			games.add(g);

			Thread t = new Thread(g);
			t.start();
			p.setId(((New) tm).id);
			p.setPort(((New) tm).port);
			g.addPlayer(p);
			players.add(p);
			p.send("REGOK"+" "+getLI(g.getID())+"***");
		}
	}else if (tm instanceof Reg) {
		g = getGame(p);
		if (g != null || isDuplicata(p.getIp(),((Reg)tm).port)) {
			p.send("REGNO***");
		} else {
			g = getGame(((Reg) tm).m);
			if (g!=null && g.waitForPlayers()) {
				if (((g.isTeam() && ((Reg) tm).isTeam()) || (!g.isTeam() && !((Reg) tm).isTeam()))) {
					p.setId(((Reg) tm).id);
					p.setPort(((Reg) tm).port);
					g.addPlayer(p);
					players.add(p);
					p.send("REGOK"+" "+getLI(((Reg) tm).m)+"***");
				}else{
					p.send("REGNO***");
				}
			}else{
				p.send("REGNO***");
			}
		}
	}else if (tm instanceof SetSize) {
		g = getGame(p);
		System.out.println(((SetSize) tm).w);
		if (((SetSize) tm).w >= minimalWidth && ((SetSize) tm).w <= maximalWidth && ((SetSize) tm).h >= minimalHeight && ((SetSize) tm).h <= maximalHeight && g!=null && !p.isReady() && !g.isPlaying()) {
			g.setSize(((SetSize) tm).w,((SetSize) tm).h);
			g.reshuffle();
			p.send("SETSIZE!***");
		}else{
			p.send("DUNNO***");
		}
	}else if (tm instanceof NoArgs) {
		switch (((NoArgs) tm).type) {
		case TypeMessage.START:
			p.setReady();
			break;
		case TypeMessage.UNREG:
			int idGame = -1;
			g= getGame(p);
			if (g != null && !p.isReady()) {
				idGame = g.getID();
				gameFound = true;
				g.removePlayer(p);
				if (g.isEmpty()) {
					games.remove(g);
					display.reset();
				}
				players.remove(p);
			}
			if (gameFound) {
				p.send("UNREGOK"+" "+getLI(idGame)+"***");
			} else {
				p.send("DUNNO***");
			}

			break;
		case TypeMessage.GAMES:
			g = getGame(p);
			if (g==null || !p.isReady()) {
				// le joueur nest pas dans une game ou nest pas pret
				for (Game g2 : games) {
					if (g2.waitForPlayers()) {
						count++;
					}
				}
				p.send("GAMES"+" "+getLI(count)+"***");

				for (Game g2 : games) {
					if (g2.waitForPlayers()) {
						p.send("GAME"+" "+getLI(g2.getID())+" "+getLI(g2.getNumberOfPlayers())+"***");
					}
				}
			}
			break;
		case TypeMessage.QUIT:
			g = getGame(p);
			if (g != null && g.isPlaying()) {
				disconnect(g,p);
			}
			break;
		case TypeMessage.GLIST:
			g = getGame(p);
			if (g!=null) {
				if (g.isPlaying()) {
					g.sendListOfPlayersPlaying(p);
				}else if (g.isOver()) {
					disconnect(g,p);
				}
			}
			break;
		case TypeMessage.CHANGETEAM:
			g = getGame(p);
			if (g != null && !p.isReady() && g.isTeam()) {
				g.changeTeam(p);
				p.send("CHANGETEAM!***");
			}else{
				p.send("DUNNO***");
			}
			break;
		case TypeMessage.TLIST:
			g = getGame(p);
			if (g!=null && g.isTeam() && !p.isReady()) {

				if (g.isOver()) {
					disconnect(g,p);
				}else{
					g.sendListOfTeam(p);
				}
			}else{
				p.send("DUNNO***");
			}
			break;
		case TypeMessage.MAP:
			g = getGame(p);
			if (g != null && g.isPlaying()) {
				g.sendMap(p);
			}else{
				p.send("DUNNO***");
			}
			break;
		case TypeMessage.POS:
			g = getGame(p);
			if (g!=null && g.isPlaying()) {
				g.sendPos(p);
			}else{
				p.send("DUNNO***");
			}
			break;

		case TypeMessage.ADDBOT:
			g = getGame(p);
			if(g!=null && !g.isPlaying() && !p.isReady()) {
				g.addBot();
				p.send("BOTADD!***");
			}
			break;
		case TypeMessage.RMBOT:
			g = getGame(p);
			if(g!=null && !g.isPlaying() && !p.isReady()) {
				if(g.rmBot()) {
					p.send("RMBOT!***");
				}else{
					p.send("NOBOT!***");
				}
			}
			break;
		case TypeMessage.BOT:
			g = getGame(p);
			if(g!=null && (g.isPlaying() || !p.isReady())) {
				int n = g.nbBot();
				p.send("NBBOT! "+getLI(n)+"***");
			}
			break;

		}

	}
}

public String getLI(int x) {
	String s="";
	s+= (char)(x%128);
	s+= (char)(x/128);
	return s;
}

public boolean isNewPort(int p) {
	for (Game g : games) {
		if (g.getPort() == p)
			return false;
	}
	return true;
}
public static int LEtoInt(char c1, char c2){
	return (c1+128*c2);
}
private static boolean ServOk(int port){
	try{
		ServerSocket serv = new ServerSocket(port);
		try{
			serv.close();
			return true;
		}catch(Exception e) {
			return false;
		}
	}catch(Exception e) {
		return false;
	}
}

public void disconnect(Game g, Player p){
	players.remove(p);
	//a revoir (13/05)
	g.removePlayer(p);
	if (g.isEmpty()) {
		games.remove(g);
		display.reset();
	}
	p.quit();
}
public Game getGame(Player p){
	for (Game g : games) {
		if (g.contains(p)) {
			return g;
		}
	}
	return null;
}
public Game getGame(int n){

	for (Game g : games) {
		if (g.getID() == n) {
			return g;
		}
	}
	return null;
}
public int getPort(){
	return oc.getPort();
}

public int getNumberOfGame(){
	return games.size();
}
public int getNumberOfPlayers(int i){
	return games.get(i).getNumberOfPlayers();
}

public void displayGame(Graphics g, int gameNumber, int posX, int posY, int decalagePlayer){
	g.drawRect(posX,posY,248,300);
	String mes="";
	if (games.get(gameNumber).waitForPlayers()) {
		mes = "WAITING";
	}else if (games.get(gameNumber).isPlaying()) {
		mes = "PLAYING";
	}else if (games.get(gameNumber).isOver()) {
		mes = "OVER";
	}
	g.drawString("ID: "+games.get(gameNumber).getID()+"   "+mes,posX,posY+40);
	for (int i = 0; i<5; i++) {
		if (i+decalagePlayer < games.get(gameNumber).getNumberOfPlayers()) {
			if (games.get(gameNumber).isTeam()) {
				g.setColor(games.get(gameNumber).getPlayerTeam(i+decalagePlayer)==0 ? Color.BLUE : Color.RED);
			}
			g.drawString(games.get(gameNumber).getPlayerID(i+decalagePlayer),posX, 50+ posY+30*(i+1));
			g.setColor(Color.BLACK);
			if (games.get(gameNumber).waitForPlayers()) {
				if (games.get(gameNumber).isPlayerReady(i + decalagePlayer)) {
					g.setColor(Color.GREEN);
					g.drawString("R",posX + 200, 50 + posY + 30*(i+1));
				}else{
					g.setColor(Color.RED);
					g.drawString("W",posX + 200, 50 + posY + 30*(i+1));
				}
				g.setColor(Color.BLACK);
			}else{
				g.drawString(""+games.get(gameNumber).getPlayerScore(i+decalagePlayer),posX+200,50+posY+30*(i+1));
			}

		}
	}
}
public int getGameID(int n){
	return games.get(n).getID();
}
public boolean isDuplicata( String ip, int port){
	for (Player p : players) {
		if (!p.isDifferent(ip,port)) {
			return true;
		}
	}
	return false;
}
public void displayMaze(Graphics g, int posX, int posY, int gameNumber){
	Game game = null;
	for (Game ga : games) {
		if (ga.getID()== gameNumber) {
			game = ga;
		}
	}
	if (game != null) {
		game.afficheLaby(g,posX,posY);
	}

}

public void killGame(int i){
	ArrayList<Player> toRemove = new ArrayList<Player>();
	for (Player p : players) {
		if (games.get(i).contains(p)) {
			toRemove.add(p);
		}
	}
	for (Player p : toRemove) {
		disconnect(games.get(i),p);
	}
}
public static void main (String args[]) {
	try{
		if (args.length > 0) {
			try{
				Serveur serveur = new Serveur(Integer.parseInt(args[0]));
			}catch(NumberFormatException e) {
				System.out.println("Mauvais argument");
			}
		}else{
			int port = 4000;
			while(port<9999 && !ServOk(port)) {
				port++;
			}
			Serveur serveur = new Serveur(port);
		}
	}catch(Exception e) {
		e.printStackTrace();
	}
}
}
