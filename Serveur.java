import java.util.ArrayList;
import java.awt.Graphics;
import java.net.ServerSocket;
import java.awt.Graphics;
public class Serveur {
private ArrayList<Player> players;
private ArrayList<Game> games;

private Occupe_Connection oc;
private int nextGameId;
private int defaultWidth;
private int defaultHeight;

public Serveur(int port) {
	nextGameId = 0;
	defaultWidth = 10;
	defaultHeight = 10;

	players = new ArrayList<Player>();
	games = new ArrayList<Game>();
	oc = new Occupe_Connection(this, port);
	Thread t = new Thread(oc);
	t.start();
	System.out.println("Server launched, port: "+oc.getPort());
	/*ServeurDisplay sd = new ServeurDisplay(this);
	   Thread t2 = new Thread(sd);
	   t2.start();*/
}

public void processMessage(Player p, TypeMessage tm ) {
	int count  = 0;
	boolean gameFound = false;
	if (tm instanceof Direction) {
		for (Game g : games) {
			if (g.contains(p)) {
				g.movePlayer(p, ((Direction) tm).direction, ((Direction) tm).pas);
			}
		}
	}else if (tm instanceof SizeList) {
		if (((SizeList) tm).type == TypeMessage.SIZE) {
			for (Game g : games) {
				/*
				   if (g.contains(p)) {
				        System.out.println(((Direction) tm).pas);
				        g.movePlayer(p, ((Direction) tm).direction, ((Direction) tm).pas);
				   }*/
				if (g.getID() == ((SizeList) tm).m) {
					gameFound = true;
					g.sendSize(p);
				}
			}
		}else {
			for (Game g : games) {
				if (g.getID() == ((SizeList) tm).m) {
					gameFound = true;
					g.sendListOfPlayers(p);
				}
			}
		}
		if (!gameFound) {
			p.send("DUNNO***");
		}
	}else if (tm instanceof All) {
		for (Game g : games) {
			if (g.contains(p)) {
				g.sendAll(p, ((All) tm).message);
			}
		}
		p.send("ALL!***");
	}else if (tm instanceof Send) {
		for (Game g : games) {
			if (g.contains(p)) {
				if (g.contains(((Send) tm).id)) {
					gameFound = true;
					g.send(p,((Send) tm).id, ((Send) tm).message);
				}
			}
		}
		if (gameFound) {
			p.send("SEND!***");
		}else {
			p.send("NOSEND***");
		}
	}else if (tm instanceof New) {
		for (Game g : games) {
			if (g.contains(p)) {
				gameFound = true;
			}
		}
		if (gameFound) {
			p.send("REGNO***");
		}else {
			// c'est bien au pif ici ?
			String multiIP = "234.255.255.255";
			int multiPort;
			do {
				multiPort = (int)(Math.random()*1000)+5000;
			} while((!isNewPort(multiPort)));
			Game g;
			if(((New)tm).isTeam()) {
				g= new Game(nextGameId++, defaultWidth, defaultHeight, multiIP, multiPort, true);
			}else{
				g= new Game(nextGameId++, defaultWidth, defaultHeight, multiIP, multiPort, false);

			}
			games.add(g);

			Thread t = new Thread(g);
			t.start();
			p.setId(((New) tm).id);
			p.setPort(((New) tm).port);
			g.addPlayer(p);
			p.send("REGOK"+" "+getLI(g.getID())+"***");
		}

	}else if (tm instanceof Reg) {
		for (Game g : games) {
			if (g.contains(p)) {
				gameFound = true;
			}
		}
		if (gameFound) {
			p.send("REGNO***");
		}else {
			for (Game g : games) {
				if (g.getID() == ((Reg) tm).m && g.waitForPlayers()) {
					if (((g.isTeam() && ((Reg) tm).isTeam()) || (!g.isTeam() && !((Reg) tm).isTeam()))) {
						p.setId(((Reg) tm).id);
						p.setPort(((Reg) tm).port);
						g.addPlayer(p);
						p.send("REGOK"+" "+getLI(((Reg) tm).m)+"***");
					}else{
						p.send("REGNO***");
					}
					break;
				}
			}
		}

	}else if (tm instanceof NoArgs) {
		switch (((NoArgs) tm).type) {
		case TypeMessage.START:
			p.setReady();
			break;
		case TypeMessage.UNREG:
			int idGame = -1;
			for (Game g : games) {
				if (g.contains(p) && !p.isReady()) {
					idGame = g.getID();
					gameFound = true;
					g.removePlayer(p);
				}
				if (gameFound) {
					p.send("UNREGOK"+" "+getLI(idGame)+"***");
				}else {
					p.send("DUNNO***");
				}
			}
			break;
		case TypeMessage.GAMES:
			for( Game g : games) {
				if (g.contains(p) && p.isReady())
					gameFound = true;
			}
			if (!gameFound) {
				for (Game g : games) {
					if (g.waitForPlayers())
						count++;
				}
				p.send("GAMES"+" "+getLI(count)+"***");

				for (int i = 0; i<count; i++) {
					p.send("GAME"+" "+getLI(games.get(i).getID())+" "+getLI(games.get(i).getNumberOfPlayers())+"***");
				}
			}
			break;
		case TypeMessage.QUIT:
			for (Game g : games) {
				if (g.contains(p)) {
					g.removePlayer(p);
				}
			}
			p.quit();
			break;
		case TypeMessage.GLIST:
			for (Game g : games) {
				if (g.contains(p)) {
					g.sendListOfPlayersPlaying(p);
				}
			}
			break;
		case TypeMessage.CHANGETEAM:
			for(Game g : games) {
				if(g.contains(p) && g.isTeam()) {
					g.changeTeam(p);
				}else{
					p.send("DUNNO***");
				}
			}
			break;
		case TypeMessage.TLIST:
			gameFound = false;
			for (Game g : games) {
				if (g.contains(p) && g.isTeam()) {
					g.sendListOfTeam(p);
					gameFound = true;
				}
			}
			break;
		case TypeMessage.MAP:
			gameFound = false;
			for(Game g : games) {
				if(g.contains(p)) {
					g.sendMap(p);
					gameFound=true;
					break;
				}
			}
			if(!gameFound) p.send("DUNNO***");
			break;
		case TypeMessage.POS:
			gameFound = false;
			for(Game g : games) {
				if(g.contains(p)) {
					g.sendPos(p);
					gameFound = true;
					break;
				}
			}
			if(!gameFound) p.send("DUNNO***");
			break;
		}

	}
}

public String getLI(int x) {
	String s="";
	s+= (char)(x%256);
	s+= (char)(x/256);
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
	return (c1+256*c2);
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
	g.drawRect(posX,posY,250,300);
	g.drawString("GameID: "+games.get(gameNumber).getID(),posX,posY+40);
	for (int i = 0; i<5; i++) {
		if (i+decalagePlayer < games.get(gameNumber).getNumberOfPlayers()) {

			g.drawString(games.get(gameNumber).getPlayerID(i+decalagePlayer),posX, 50+ posY+30*(i+1));
		}
	}
}
public int getGameID(int n){
	return games.get(n).getID();
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
