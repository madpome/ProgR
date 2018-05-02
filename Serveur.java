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
						// send [SIZE! m h w***]
					}
				}
			}else {
				for (Game g : games) {
					if (g.getID() == ((SizeList) tm).m) {
						gameFound = true;
						// send [LIST! m s***]
						// then send s * [PLAYER id***]
					}
				}
			}
			
			if (!gameFound) {
				// send [DUNNO***]
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
						g.send(((Send) tm).id, ((Send) tm).message);
					}
				}
			}
			if (gameFound) {
				// send [SEND!***]
			}else {
				//send [NOSEND***]
			}
		}else if (tm instanceof New) {
			Game g = new Game(nextGameId++, defaultWidth, defaultHeight);
			p.setId(((New) tm).id);
			p.setPort(((New) tm).port);
			g.addPlayer(p);
			
			//send [REGOK m***] m=nextGameId - 1
		}else if (tm instanceof Reg) {
			for (Game g : games) {
				if (g.getID() == ((Reg) tm).m && g.waitForPlayers()){
					p.setId(((New) tm).id);
					p.setPort(((New) tm).port);
					g.addPlayer(p);
					gameFound = true;
				}
			}
			if (gameFound) {
				// send [REGOK m***]
			}else {
				//send [REGNO***]
			}
			
		}else if (tm instanceof NoArgs) {
			switch (((NoArgs) tm).type) {
			case TypeMessage.START:
				p.setReady();
				break;
			case TypeMessage.UNREG:
				for (Game g : games) {
					if (g.contains(p)) {
						gameFound = false;
						g.removePlayer(p);
					}
				}
				if (gameFound) {
					//[UNREGOK m***] m = nextGameId - 1
				}else {
					//[DUNNO***]
				}
				break;
			case TypeMessage.GAMES:
				for (Game g : games) {
					if (g.waitForPlayers())
						count++;
				}
				//send [GAMES n***] n = count
				
				//send [GAME m s***]
				break;
			case TypeMessage.QUIT:
				for (Game g : games) {
					if (g.contains(p)) {
						g.removePlayer(p);
					}
				}
				p.disconnect();
				break;
			case TypeMessage.GLIST:
				for (Game g : games) {
					if (g.contains(p)) {
						g.sendListOfPlayers(p);
					}
				}
				break;
			}
		}
	}
}