import java.util.ArrayList;
import java.util.Scanner;

class Game implements Runnable{
	//constants
	private static final int STARTING = 0;
	private static final int PLAYING = 1;
	private static final int FINISH = 2;
	
	private static final int UP = 0;
	private static final int RIGHT = 1;
	private static final int DOWN = 2;
	private static final int LEFT = 3;
	
	private int gameID;
	private boolean isRunning;
	private int STATE;
	private int mazeHeight;
	private int mazeWidth;
	private int[][] maze;
	private ArrayList<Player> players;
	private ArrayList<Ghost> ghosts;
	private ArrayList<Ghost> ghostsToRemove;
	
	
	public Game(int gameID, int width, int height) {
		mazeHeight = height;
		mazeWidth = width;
		maze = KruskalMaze.getNewMaze(mazeWidth, mazeHeight);
		players = new ArrayList<Player>();
		ghosts = new ArrayList<Ghost>();
		ghostsToRemove = new ArrayList<Ghost>();
		isRunning = true;
		STATE = STARTING;
		
		// tests purpose
		ghosts.add(new Ghost(2,maze));
		ghosts.add(new Ghost(3,maze));
	}
	
	public void run() {
		boolean allReady = true;
		String mes;
		byte[] mes2 = new byte[7*8];
		while(isRunning) {
			switch(STATE) {
			case STARTING:
				allReady = true;
				for (Player p : players) {
					if (!p.isReady())
						allReady = false;
				}
				if (allReady) {
					STATE = PLAYING;
					for (Player p : players) {
						mes ="WELCOME "      +"***";
						mes2 = "WELCOME".getBytes();
						// p.send [WELCOME m h w f ip port***]
						p.initialize(maze);
					}
				}
				break;
			case PLAYING:
				for (Ghost g: ghostsToRemove) {
					ghosts.remove(g);
				}
				for (Ghost g: ghosts) {
					g.update();
					if (g.willMove()) {
						// ghosts dont moove for tests
						//g.moove(maze);
					}
				}
				/*if (ghosts.isEmpty() || players.isEmpty()) {
					STATE = FINISH;
				}*/
				this.displayMaze();
				try {
				Thread.sleep(1000);
				}catch (Exception e) {}
				break;
			case FINISH:
				// je sais pas trop ce qu'on fait là
				break;
			}
		}
	}
	
	public void moovePlayer(Player p, int direction, int distance) {
		int startX;
		int startY;
		int endX = 0;
		int endY = 0;
		startX = p.getX();
		startY = p.getY();

		boolean set = false;
		switch (direction) {
		case UP:
			for (int i = 0; i<= distance; i++) {
				if (!set && maze[startX-i][startY] == 0) {
					endX = startX - i + 1;
					set = true;
				}
			}
			if (!set) {
				endX = startX - distance;
			}
			endY = startY;
		break;
		case DOWN:
			for (int i = 0; i<= distance; i++) {
				if (!set && maze[startX+i][startY] == 0) {
					endX = startX + i - 1;
					set = true;
				}
			}
			if (!set) {
				endX = startX + distance;
			}
			endY = startY;
			break;
		case RIGHT:
			for (int i = 0; i<= distance; i++) {
				if (!set && maze[startX][startY+i] == 0) {
					endY = startY + i - 1;
					set = true;
				}
			}
			if (!set) {
				endY = startY + distance;
				}
			endX = startX;
			break;
			case LEFT:
				for (int i = 0; i<= distance; i++) {
				if (!set && maze[startX][startY-i] == 0) {
						endY = startY - i + 1;
					set = true;
				}
			}
			if (!set) {
				endY = startY - distance;
			}
			endX = startX;
			
			break;
		}
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
	public Ghost checkForColision(int startX, int endX, int startY,int endY, Player p) {
		int gx;
		int gy;
		int sx,ex,sy,ey;
		
		if (startX <= endX) {
			sx = startX;
			ex = endX;
		}else {
			sx = endX;
			ex = startX;
		}

		if (startY <= endY) {
			sy = startY;
			ey = endY;
		}else {
			sy = endY;
			ey = startY;
		}
		
		
		for (Ghost g : ghosts) {
			gx = g.getX();
			gy = g.getY();
			if (gx >= sx&& gx <= ex && gy >= sy && gy <= ey) {
				return g;
			}
		}
		return null;
	}
	public void addPlayer(Player p) {
		p.setNotReady();
		players.add(p);
	}
	
	public void removePlayer(Player p) {
		players.remove(p);
	}
	
	private void displayMaze() {
		boolean wrote = false;
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j<maze[0].length; j++) {
				wrote = false;
				for(Ghost g : ghosts) {
					if (g.getX() == i && g.getY() == j) {
						System.out.print("G");
						wrote = true;
					}
				}
				for (Player p : players) {
					if (p.getX() == i && p.getY() == j) {
						System.out.print("P");
						wrote = true;
					}
				}
				if (!wrote) {
					System.out.print(maze[i][j] == 1 ? " " : "*");
				}
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	public void sendAll(Player p, String message) {
		//TODO
	}
	
	public void send(String id, String message) {
		//TODO
	}
	public boolean contains (Player player) {
		for (Player p : players) {
			if (p == player)
				return true;
		}
		return false;
	}
	public boolean contains(String id) {
		for (Player p : players) {
			if (p.getId() == id)
				return true;
		}
		return false;
	}
	
	// tests purpose
	public static void main (String[] args) {
		Game g = new Game(0,10,10);
		Player p = new Player("id",0);
		g.addPlayer(p);
		Thread t = new Thread(g);
		t.start();
		Scanner sc = new Scanner(System.in);
		int d;
		int i;
		while (true) {
			d = sc.nextInt();
			i = sc.nextInt();
			g.moovePlayer(p, d, i);
		}
	}
	
	public int getID() {
		return gameID;
	}
	
	public boolean waitForPlayers() {
		return (STATE == STARTING);
	}
	
	public void sendListOfPlayers(Player p) {
		//send [GLIST! s***]  s = players.size()
		
		//send [GPLAYER id x y p***] id/x/y/p = p.get(i).getId()/getX()/getY()/getScore()
	}
}
