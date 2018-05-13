import java.util.LinkedList;
import java.util.Scanner;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class Game implements Runnable {
//constants
private static final int STARTING = 0;
private static final int PLAYING = 1;
private static final int FINISH = 2;

private static final int LEFT = 3;
private static final int DOWN = 1;
private static final int RIGHT = 2;
private static final int UP = 0;

private int gameID;
private boolean isRunning;
private int STATE;
private int mazeHeight;
private int mazeWidth;
private int[][] maze;

private boolean teamGame;
private int nbT0;
private int nbT1;
private int botId;
private int score0;
private int score1;
private String multiIP;
    private int multiPort;


    private BufferedImage ghostImage;
    private BufferedImage playerImage;
    private BufferedImage playerBlueImage;
    private BufferedImage playerRedImage;
    private BufferedImage playerBlackImage;
    private BufferedImage playerBlackRedImage;
    private BufferedImage playerBlackBlueImage;
private Messagerie messagerie;
private LinkedList<Player> players;
private LinkedList<Ghost> ghosts;
private LinkedList<Ghost> ghostsToRemove;


public Game(int gameID, int width, int height, String ip, int port, boolean isTeamGame) {
	this.gameID = gameID;
	botId = 0;
	teamGame = isTeamGame;
	try{
		ghostImage = (BufferedImage)ImageIO.read(new File("src/ghost.png"));
		playerImage = (BufferedImage)ImageIO.read(new File("src/player.png"));
		playerBlueImage = (BufferedImage)ImageIO.read(new File("src/player_blue.png"));
		playerRedImage = (BufferedImage)ImageIO.read(new File("src/player_red.png"));
		playerBlackImage = (BufferedImage)ImageIO.read(new File("src/player_black.png"));
		playerBlackRedImage = (BufferedImage)ImageIO.read(new File("src/player_black_red.png"));
		playerBlackBlueImage = (BufferedImage)ImageIO.read(new File("src/player_black_blue.png"));
	}catch (Exception e) {}

	players = new LinkedList<Player>();
	ghosts = new LinkedList<Ghost>();
	ghostsToRemove = new LinkedList<Ghost>();

	mazeHeight = height;
	mazeWidth = width;
	maze = KruskalMaze.getNewMaze(mazeWidth, mazeHeight);
	initializeGhosts();


	multiIP = ip;
	multiPort = port;
	messagerie = new Messagerie(players, multiPort, multiIP);

	isRunning = true;
	STATE = STARTING;
}

//Remelange les ghost
public void reshuffle(){
	synchronized (this){
		for(Ghost g : ghosts) {
			g.move(maze,players,ghosts);
		}
	}
}
public void initializeGhosts() {
	int nbGhost = (int)(Math.sqrt(mazeHeight*mazeWidth)/2);
	int ghostSpeed;
	nbGhost = (nbGhost==0) ? 1 : nbGhost;
	for (int i = 0; i < nbGhost; i++) {
		ghostSpeed =(int) (10*Math.random() + 15);
		Ghost g = new Ghost(ghostSpeed, maze, players, ghosts);
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

			synchronized (this){
				for (Player p : players) {
					if (!p.isReady()) {

						allReady = false;
					}
				}
			}

			if (allReady) {
				STATE = PLAYING;
				mes ="WELCOME"+" "+getLI(gameID)+" "+getLI(mazeHeight)+" "+getLI(mazeWidth)+" "+getLI(ghosts.size())+" "+char15(multiIP)+" "+multiPort+"***";
				for (Player p : players) {
					if(!p.isBot()) {
						p.send(mes);
					}
					p.initializePosition(maze);
				}
				for (Player p : players) {
					if(!p.isBot())
						p.sendPosition();
				}
			}
			break;
		case PLAYING:
			for (Ghost g : ghostsToRemove) {
				ghosts.remove(g);
			}
			if (ghosts.isEmpty() || players.isEmpty()) {
				STATE = FINISH;
			}
			for (Ghost g : ghosts) {
				g.update();
				if (g.willMove()) {
					g.move(maze, players, ghosts);
					// un ghost peut bouger sur place
					messagerie.sendMessageFant(g.getX(), g.getY());
				}
			}
			for(Player p : players) {
				if(p.isBot()) {
					p.update();
					if(p.willMove()) {
						int d = (int)(Math.random()*4);
						while(!OkDir(d,p.getX(),p.getY())) {
							d=(int)(Math.random()*4);
						}
						int dist =(int)(Math.random()*4+1);
						p.setTime((int)(Math.random()*2+1));
						movePlayer(p,d,dist);
					}
				}
			}
			this.displayMaze();
			try {
				// 1000 pour avoir des tests lisibles
				Thread.sleep(1000);
			}catch (Exception e) {}
			break;
		case FINISH:
			if (teamGame) {
				boolean a = (score0>score1) ? (messagerie.sendTeamEnd("0",score0)) : (messagerie.sendTeamEnd("1",score1));
			}else{ // not a teamGame
				if (!players.isEmpty()) {
					winner  = players.getFirst();
					for (Player p : players) {
						if (p.getScore() > winner.getScore())
							winner = p;
					}
					messagerie.sendMessageEnd(winner, winner.getScore());
				}
			}
			isRunning = false;
			break;
		}
	}
}
public boolean OkDir(int dir, int x, int y){
	switch (dir) {
	case UP:
		if(maze[y-1][x]==1) {
			return false;
		}
		break;
	case DOWN:
		if(maze[y+1][x]==1) {
			return false;
		}
		break;
	case LEFT:
		if(maze[y][x-1]==1) {
			return false;
		}
		break;
	case RIGHT:
		if(maze[y][x+1]==1) {
			return false;
		}
		break;
	}
	return true;
}
public boolean onlyBot(){
	for(Player p : players) {
		if(!p.isBot()) {
			return false;
		}
	}
	return true;
}
public void movePlayer(Player p, int direction, int distance) {
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
			if (!set && maze[startY-i][startX] == 0) {
				endY = startY - i + 1;
				set = true;
			}
		}
		if (!set) {
			endY = startY - distance;
		}
		endX = startX;
		break;
	case DOWN:
		for (int i = 0; i<= distance; i++) {
			if (!set && maze[startY+i][startX] == 0) {
				endY = startY + i - 1;
				set = true;
			}
		}
		if (!set) {
			endY = startY + distance;
		}
		endX = startX;
		break;
	case RIGHT:
		for (int i = 0; i<= distance; i++) {
			if (!set && maze[startY][startX+i] == 0) {
				endX = startX + i - 1;
				set = true;
			}
		}
		if (!set) {
			endX = startX + distance;
		}
		endY = startY;
		break;
	case LEFT:
		for (int i = 0; i<= distance; i++) {
			if (!set && maze[startY][startX-i] == 0) {
				endX = startX - i + 1;
				set = true;
			}
		}
		if (!set) {
			endX = startX - distance;
		}
		endY = startY;

		break;
	}
	Ghost g;
	boolean hasAlreadyMoved = false;
	int xscore = 0;
	synchronized (this) {
		while((g = checkForCollision(startX, endX, startY, endY, p))!=null) {
			xscore += g.getLevel();
			ghostsToRemove.add(g);
		}
		p.move(endX,endY,xscore);
		messagerie.sendMessageScore(p, p.getScore(), p.getX(), p.getY());
	}
}
public Ghost checkForCollision(int startX, int endX, int startY,int endY, Player p) {
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
		if(!ghostsToRemove.contains(g)) {
			gx = g.getX();
			gy = g.getY();
			if (gx >= sx && gx <= ex && gy >= sy && gy <= ey) {
				return g;
			}
		}
	}
	return null;
}
public void addBot(){
	Player p = new Player("bot"+botId++,0);
	p.setBot(true);
	p.setTime(1);
	addPlayer(p);
}
public synchronized boolean rmBot(){
	for(Player p : players) {
		if(p.isBot()) {
			players.remove(p);
			if(isTeam()) {
				int a = p.getTeam()==0 ? nbT0-- : nbT1--;
			}
			return true;
		}
	}
	return false;
}
public int nbBot(){
	int k =0;
	for (Player p : players) {
		if(p.isBot()) {
			k++;
		}
	}
	return k;
}
public synchronized void addPlayer(Player p) {
	if (teamGame) {
		int x = whichTeam() ? 1 : 0;
		if(x==1) {
			p.setTeam(1);
			nbT1++;
		}else{
			p.setTeam(0);
			nbT0++;
		}
	}
	if(p.isBot()) {
		p.setReady();
	}else{
		p.setNotReady();
	}
	players.add(p);
}
public void sendMap(Player p){
	int x = p.getX();
	int y = p.getY();
	String msg = "MAP! \n";
	for(int i = 0; i<mazeHeight; i++) {
		for(int j = 0; j<mazeWidth; j++) {
			if(maze[i][j] == 0) {
				msg+="\u2588";
			}else{
				if(i==y && j==x) {
					msg+="\u263A";
				}else{
					msg+=" ";
				}
			}
		}
		msg+="\n";
	}
	msg+="***";
	p.send(msg);
}
public void changeTeam(Player p){
	if (teamGame) {
		p.setTeam(1-p.getTeam());
		if(p.getTeam()==0) {
			nbT0++;
			nbT1--;
		}else{
			nbT0--;
			nbT1++;
		}
	}
}
public void sendPos(Player p){
	p.send("POS "+p.getX()+" "+p.getY()+"***");
}
public synchronized void removePlayer(Player p) {
	players.remove(p);
	if(isTeam()) {
		if(p.getTeam()==0) {
			nbT0--;
		}else{
			nbT1--;
		}
	}
}

private void displayMaze() {
	boolean wrote = false;
	for (int i = 0; i < maze.length; i++) {
		for (int j = 0; j<maze[0].length; j++) {
			wrote = false;
			for (Player p : players) {
				if (p.getY() == i && p.getX() == j && !wrote) {
					System.out.print("\u263A");
					wrote = true;
				}
			}
			if(!wrote) {
				for(Ghost g : ghosts) {
					if (g.getY() == i && g.getX() == j && !wrote) {
						System.out.print("\u2689");
						wrote = true;
					}
				}
			}
			if (!wrote) {
				System.out.print(maze[i][j] == 1 ? " " : "\u2588");
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
	for (Player p : players) {
		if (p.getId().equals(id) && !p.isBot()) {
			messagerie.sendMessageTo(message, playerFrom, p);
		}
	}
}

public void sendSize(Player p) {
	String mes = "SIZE!"+" "+getLI(gameID)+" "+getLI(mazeHeight)+" "+getLI(mazeWidth)+"***";
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
		if (p.getId().equals(id))
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
public boolean isPlaying(){
	return (STATE == PLAYING);
}
public boolean isOver(){
	return (STATE == FINISH);
}

public void sendListOfPlayersPlaying(Player p) {
	String mes;
	mes  = "GLIST!"+" "+getLI(players.size())+"***";
	p.send(mes);

	Player player;
	for (int i = 0; i<players.size(); i++) {
		player = players.get(i);
		mes = "GPLAYER"+" "+player.getId()+" "+char3(player.getX())+" "+char3(player.getY())+" "+char4(player.getScore())+"***";
		p.send(mes);
	}
}

public void sendListOfPlayers(Player p ) {
	String mes;
	mes = "LIST!"+" "+getLI(gameID)+" "+getLI(players.size())+"***";
	p.send(mes);

	for (int i = 0; i<players.size(); i++) {
		mes = "PLAYER"+" "+players.get(i).getId()+"***";
		p.send(mes);
	}
}
public void sendListOfTeam(Player p ){
	String mes = "TLIST! "+p.getTeam()+" "+getLI(getT(p.getTeam()))+"***";
	p.send(mes);
	for(int i =0; i<players.size(); i++) {
		if(players.get(i).getTeam()==p.getTeam()) {
			mes = "TPLAYER "+players.get(i).getId()+"***";
			p.send(mes);
		}
	}
}

public void setSize(int width, int height){
	mazeHeight = height;
	mazeWidth = width;
	maze = KruskalMaze.getNewMaze(mazeWidth, mazeHeight);
	initializeGhosts();
}
public int getNumberOfPlayers() {
	return players.size();
}
public int getT(int a){
	return (a==0) ? nbT0 : nbT1;
}
public String getLI(int x) {
	String s="";
	s+= (char)(x%256);
	s+= (char)(x/256);
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
public String getPlayerID(int i){
	return players.get(i).getId();
}
    public int getPlayerScore(int i){
	return players.get(i).getScore();
    }
    public int getPlayerTeam(int i){
	return players.get(i).getTeam();
    }
public boolean isPlayerReady(int i){
	return players.get(i).isReady();
}
public int getPort() {
	return multiPort;
}
public boolean isTeam(){
	return teamGame;
}
public boolean whichTeam(){
    return nbT0>nbT1;
}
    public boolean isEmpty(){
	return (players.isEmpty() || onlyBot());
    }
    public void afficheLaby(Graphics g, int posX,int posY){
	int caseSize = 640/maze.length;
	if (640/maze[0].length < caseSize)
	    caseSize = 640/maze[0].length;
	for (int i=0; i<maze.length; i++) {
	    for (int j=0; j<maze[i].length; j++) {
		if (maze[i][j] == 0) {
		    g.fillRect(posX+caseSize*j,posY+caseSize*i,caseSize,caseSize);
		}
	    }
	}
	Graphics2D g2;
	if (STATE != STARTING) {
	    BufferedImage playIm = new BufferedImage(caseSize, caseSize, playerImage.getType());
	    g2 = playIm.createGraphics();
	    g2.drawImage(playerImage,0,0,caseSize,caseSize,0,0, playerImage.getWidth(), playerImage.getHeight(),null);
	    g2.dispose();

	    BufferedImage blackIm = new BufferedImage(caseSize, caseSize, playerBlackImage.getType());
	    g2 = blackIm.createGraphics();
	    g2.drawImage(playerBlackImage,0,0,caseSize,caseSize,0,0, playerBlackImage.getWidth(), playerBlackImage.getHeight(),null);
	    g2.dispose();
	    BufferedImage blueIm = new BufferedImage(caseSize, caseSize, playerBlueImage.getType());
	    g2 = blueIm.createGraphics();
	    g2.drawImage(playerBlueImage,0,0,caseSize,caseSize,0,0, playerBlueImage.getWidth(), playerBlueImage.getHeight(),null);
	    g2.dispose();
	    BufferedImage redIm = new BufferedImage(caseSize, caseSize, playerRedImage.getType());
	    g2 = redIm.createGraphics();
	    g2.drawImage(playerRedImage,0,0,caseSize,caseSize,0,0, playerRedImage.getWidth(), playerRedImage.getHeight(),null);
	    g2.dispose();
	    BufferedImage blackBlueIm = new BufferedImage(caseSize, caseSize, playerBlackBlueImage.getType());
	    g2 = blackBlueIm.createGraphics();
	    g2.drawImage(playerBlackBlueImage,0,0,caseSize,caseSize,0,0, playerBlackBlueImage.getWidth(), playerBlackBlueImage.getHeight(),null);
	    g2.dispose();
	    BufferedImage blackRedIm = new BufferedImage(caseSize, caseSize, playerBlackRedImage.getType());
	    g2 = blackRedIm.createGraphics();
	    g2.drawImage(playerBlackRedImage,0,0,caseSize,caseSize,0,0, playerBlackRedImage.getWidth(), playerBlackRedImage.getHeight(),null);
	    g2.dispose();
		
	    for (Player p : players) {
		if (isTeam()){
		    if (p.isBot()){
			if (p.getTeam()==0){
			    g.drawImage(blackBlueIm, caseSize*p.getX() + posX, caseSize*p.getY()+posY,null);
			}else{// team = 1
			    g.drawImage(blackRedIm, caseSize*p.getX() + posX, caseSize*p.getY()+posY,null);
			}
		    }else{ // no bot
			if (p.getTeam() == 0){
			    g.drawImage(blueIm, caseSize*p.getX() + posX, caseSize*p.getY()+posY,null);
			}else{
			    g.drawImage(redIm, caseSize*p.getX() + posX, caseSize*p.getY()+posY,null);
			}
		    }
		}else{ // no team
		    if (p.isBot()){
			g.drawImage(blackIm, caseSize*p.getX() + posX, caseSize*p.getY()+posY,null);
		    }else{
			g.drawImage(playIm, caseSize*p.getX() + posX, caseSize*p.getY()+posY,null);
		    }
		}
	    }


	    BufferedImage ig = new BufferedImage(caseSize, caseSize, ghostImage.getType());
	    g2 = ig.createGraphics();
	    g2.drawImage(ghostImage,0,0,caseSize,caseSize,0,0, ghostImage.getWidth(), ghostImage.getHeight(),null);
	    g2.dispose();
	    for (Ghost h : ghosts) {
		g.drawImage(ig, posX+caseSize*h.getX(), posY+caseSize*h.getY(),null);
	    }
	}
    }
}
