import java.util.LinkedList;
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

    private boolean teamGame;
    private int score0;
    private int score1;
    private String multiIP;
    private int multiPort;

    
    private Messagerie messagerie;
    private LinkedList<Player> players;
    private LinkedList<Ghost> ghosts;
    private LinkedList<Ghost> ghostsToRemove;
	
	
    public Game(int gameID, int width, int height, String ip, int port, boolean isTeamGame) {
	teamGame = isTeamGame;
	
	mazeHeight = height;
	mazeWidth = width;
	maze = KruskalMaze.getNewMaze(mazeWidth, mazeHeight);
	
	players = new LinkedList<Player>();
	ghosts = new LinkedList<Ghost>();
	ghostsToRemove = new LinkedList<Ghost>();
	
	multiIP = ip;
	multiPort = port;
	messagerie = new Messagerie(players, multiPort, multiIP);
	
	isRunning = true;
	initializeGhosts();
	STATE = STARTING;
    }
	
    public void initializeGhosts() {
	int nbGhost = (int)(Math.sqrt(mazeHeight*mazeWidth)/2);
	int ghostSpeed = 3;
	nbGhost = (nbGhost==0) ? 1 : nbGhost;
	for (int i = 0; i < nbGhost ; i++) {
	    Ghost g = new Ghost(ghostSpeed, maze);
	    ghosts.add(g);
	}
    }
    public void run() {
	Player winner;
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
		    mes ="WELCOME"+" "+getLI(gameID)+" "+getLI(mazeHeight)+" "+getLI(mazeWidth)+" "+getLI(ghosts.size())+" "+char15(multiIP)+" "+multiPort+"***";
		    for (Player p : players) {
			p.send(mes);
			p.initializePosition(maze);
		    }
		    for (Player p : players) {
			p.sendPosition();
		    }
		}
		break;
	    case PLAYING:
		for (Ghost g: ghostsToRemove) {
		    ghosts.remove(g);
		}
		if (ghosts.isEmpty() || players.isEmpty()) {
		    STATE = FINISH;
		}
		for (Ghost g: ghosts) {
		    g.update();
		    if (g.willMove()) {
			g.moove(maze);
			// un ghost peut bouger sur place
			messagerie.sendMessageFant(g.getX(), g.getY());
		    }
		}
		this.displayMaze();
		try {
		    // 1000 pour avoir des tests lisibles
		    Thread.sleep(1000);
		}catch (Exception e) {}
		break;
	    case FINISH:
		if (teamGame){
		}else{ // not a teamGame
		    winner  = players.getFirst();
		    for (Player p : players) {
			if (p.getScore() > winner.getScore())
			    winner = p;
		    }
		    messagerie.sendMessageEnd(winner, winner.getScore());
		}
		isRunning = false;
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
		p.move(endX, endY, false);
	    }else{
		p.move(endX,endY,true);
		messagerie.sendMessageScore(p, p.getScore(), p.getX(), p.getY());
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
	if (teamGame){
	    p.setTeam( (getNumberOfTeam(1) > getNumberOfTeam(0)) ? 0 : 1);
	}
	p.setNotReady();
	players.add(p);
    }

    public void changeTeam(Player p, int team){
	if (teamGame){
	    if (team != 0 && team != 1){
		p.setTeam(team);
		// send you re in team team
	    }else{
		//invalid team number
	    }
	}else{
	    // this is not a team game
	}
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
	messagerie.sendMessageAll(message,p);
    }
	
    public void send(Player playerFrom, String id, String message) {
	Player playerTo = null;
	for (Player p : players) {
	    if (p.getId() == id)
		playerTo = p;
	}
	if (playerTo != null) {
	    messagerie.sendMessageTo(message, playerFrom, playerTo);
	}
	// rien n est specifie dans le sujet si l id est inccorect
    }
	
    public void sendSize(Player p) {
	String mes = "size!"+" "+getLI(gameID)+" "+getLI(mazeHeight)+" "+getLI(mazeWidth)+"***";
	p.send(mes);
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
	
    public int getID() {
	return gameID;
    }
	
    public boolean waitForPlayers() {
	return (STATE == STARTING);
    }
	
    public void sendListOfPlayersPlaying(Player p) {
	String mes;
	mes  = "GLIST!"+" "+getLI(players.size())+"***";
	p.send(mes);
		
	Player player;
	for (int i = 0; i<players.size();i++) {
	    player = players.get(i);
	    mes = "GPLAYER"+" "+player.getId()+" "+char3(player.getX())+" "+char3(player.getY())+" "+char4(player.getScore())+"***";
	    p.send(mes);
	}
    }
	
    public void sendListOfPlayers(Player p ) {
	String mes;
	mes = "LIST!"+" "+getLI(gameID)+" "+getLI(players.size())+"***";
	p.send(mes);
		
	for (int i = 0; i<players.size();i++) {
	    mes = "PLAYER"+" "+players.get(i).getId()+"***";
	    p.send(mes);
	}
    }
	
    public int getNumberOfPlayers() {
	return players.size();
    }
	
    public String getLI(int x) {
	String s="";
	s+= (char)x%256;
	s+= (char)x/256;
	return s;
    }
	
    public String char3(int x) {
	if (x>999) {
	    return "999";
	}else {
	    String s =""+x;
	    while (s.length()<3)
		s = "0"+s;
	    return s;
	}
    }
    public String char4(int x) {
	if (x>9999) {
	    return "9999";
	}else {
	    String s =""+x;
	    while (s.length()<4)
		s = "0"+s;
	    return s;
	}
    }
    public String char15(String mes) {
	String s = mes;
	while(s.length()<15) {
	    s+="#";
	}
	return s;
    }
	
    public int getPort() {
	return multiPort;
    }
    public int getNumberOfTeam(int x){
	int count = 0;

	for (int i=0; i<players.size(); i++){
	    if (players.get(i).getTeam() == x)
		count++;
	}
	return count;
    }
}
