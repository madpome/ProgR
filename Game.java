import java.util.ArrayList;

class Game implements Runnable{
	//constants
	private static final int STARTING = 0;
	private static final int PLAYING = 1;
	private static final int FINISH = 2;
	
	private boolean isRunning;
	private int STATE;
	private int mazeLength;
	private int[][] maze;
	private ArrayList<Player> players;
	private ArrayList<Ghost> ghosts;
	private ArrayList<Ghost> ghostsToRemove;
	
	
	public Game(int n) {
		mazeLength = n;
		//create maze
		players = new ArrayList<Player>();
		ghosts = new ArrayList<Ghost>();
		ghostsToRemove = new ArrayList<Ghost>();
		isRunning = true;
		STATE = STARTING;
	}
	
	public void run() {
		while(isRunning) {
			switch(STATE) {
			case STARTING:
				// on check si tous les joueurs sont ready
				break;
			case PLAYING:
				for (Ghost g: ghostsToRemove) {
					ghosts.remove(g);
				}
				for (Ghost g: ghosts) {
					g.update();
					if (g.willMove()) {
						g.moove(maze);
					}
				}
				if (ghosts.isEmpty() || players.isEmpty()) {
					STATE = FINISH;
				}
				break;
			case FINISH:
				// je sais pas trop ce qu'on fait là
				break;
			}
		}
	}
	
	public void moovePlayer(String id, int direction, int distance) {
		int startX;
		int startY;
		int endX;
		int endY;
		for (Player p: players) {
			if (id.equals(p.getId())) {
				startX = p.getX();
				startY = p.getY();
				//TODO compute endX & endY
				endX=0;
				endY=0;
				Ghost g = checkForColision(startX, endX, startY, endY, p);
				synchronized(this) {
					if (g == null || ghostsToRemove.contains(g)) {
						p.moove(endX, endY, false);
					}else{
						p.moove(endX,endY,true);
						ghostsToRemove.add(g);
					}
				}
			}
		}
	}
	public Ghost checkForColision(int startX, int endX, int startY,int endY, Player p) {
		int gx;
		int gy;
		for (Ghost g : ghosts) {
			gx = g.getX();
			gy = g.getY();
			if (gx >= startX && gx <= endX && gy >= startY && gy <= endY) {
				return g;
			}
		}
		return null;
	}
	public void addPlayer(Player p) {
		players.add(p);
	}
}
