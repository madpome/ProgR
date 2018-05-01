import java.util.ArrayList;

public class Serveur {
	private ArrayList<Player> players;
	private ArrayList<Game> games;
	
	
	public void processMessage(Player p, TypeMessage tm ) {
		if (tm instanceof Direction) {
			for (Game g : games) {
				if (g.contains(p)){
					g.moovePlayer(p, ((Direction) tm).direction, ((Direction) tm).pas);
				}
			}
		}else {
			// etc
		}
	}
}
