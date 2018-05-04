import java.util.ArrayList;

public class Serveur {
	private ArrayList<Player> players;
	private ArrayList<Game> games;
	
	private int nextGameId;
	private int defaultWidth;
	private int defaultHeight;
	
	public Serveur() {
		nextGameId = 0;
		defaultWidth = 10;
		defaultHeight = 10;
		
		players = new ArrayList<Player>();
		games = new ArrayList<Game>();
	}
	
	public void processMessage(Player p, TypeMessage tm ) {
		int count  = 0;
		boolean gameFound = false;
		if (tm instanceof Direction) {
			for (Game g : games) {
				if (g.contains(p)){
					g.moovePlayer(p, ((Direction) tm).direction, ((Direction) tm).pas);
				}
			}
		}else if (tm instanceof SizeList){
			if (((SizeList) tm).type == TypeMessage.SIZE){
				for (Game g : games) {
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
				if (g.contains(p)){
					g.sendAll(p, ((All) tm).message);
				}
			}
		}else if (tm instanceof Send){
			for (Game g : games) {
				if (g.contains(p)){
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
				String multiIP;
				int multiPort;
				Game g = new Game(nextGameId++, defaultWidth, defaultHeight, multiIP, multiPort);
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
					if (g.getID() == ((Reg) tm).m && g.waitForPlayers()){
						p.setId(((New) tm).id);
						p.setPort(((New) tm).port);
						g.addPlayer(p);
						gameFound = true;
					}
				}
				if (gameFound) {
					p.send("REGOK"+" "+getLI(((Reg) tm).m)+"***");
				}else {
					p.send("REGNO***");
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
						gameFound = false;
						g.removePlayer(p);
					}
				}
				if (gameFound) {
					p.send("UNREGOK"+" "+getLI(idGame)+"***");
				}else {
					p.send("DUNNO***");
				}
				break;
			case TypeMessage.GAMES:
				for( Game g : games) {
					if (g.contains(p) && p.isReady())
						gameFound = true;
				}
				if (gameFound) {
					// le joueur est pret et dans une partie
				}else {
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
			}
		}
	}
	
	public String getLI(int x) {
		String s="";
		s+= (char)x%256;
		s+= (char)x/256;
		return s;
	}
}