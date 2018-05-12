import java.util.ArrayList;
import java.awt.Graphics;
import java.net.ServerSocket;
import java.awt.Graphics;
public class Serveur {
    private final int minimalWidth = 5;
    private final int maximalWidth = 30;
    private final int defaultWidth = 10;
    private final int minimalHeight = 5;
    private final int maximalHeight = 30;
    private final int defaultHeight = 10;
    
    private ArrayList<Player> players;
    private ArrayList<Game> games;

    private Occupe_Connection oc;
    private int nextGameId;

    public Serveur(int port) {
	nextGameId = 0;

	players = new ArrayList<Player>();
	games = new ArrayList<Game>();
	oc = new Occupe_Connection(this, port);
	Thread t = new Thread(oc);
	t.start();
	System.out.println("Server launched, port: "+oc.getPort());
	ServeurDisplay sd = new ServeurDisplay(this);
	Thread t2 = new Thread(sd);
	t2.start();
    }

    public void processMessage(Player p, TypeMessage tm ) {
	int count  = 0;
	boolean gameFound = false;
	Game g = null;
	if (tm instanceof Direction) {
	    g = getGame(p);
	    if (g!=null){
		if (g.isPlaying()){
		    g.movePlayer(p, ((Direction) tm).direction, ((Direction) tm).pas);
		}else if (g.isOver()){
		    disconnect(g,p);
		}
	    }
	}else if (tm instanceof SizeList) {
	    g = getGame(((SizeList) tm).m);
	    if (g!=null && !p.isReady()){
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
	    if (g != null){
		if (g.isPlaying()){
		    g.sendAll(p, ((All) tm).message);
		    p.send("ALL!***");
		}else if (g.isOver()){
		    disconnect(g,p);
		}
	    }
	}else if (tm instanceof Send) {
	    g = getGame(p);
	    if (g != null){
		if (g.isPlaying()){
		    if (g.contains(((Send) tm).id)) {
			g.send(p,((Send) tm).id, ((Send) tm).message);
			p.send("SEND!***");
		    }else{  
			p.send("NOSEND***");
		    }
		}else if (g.isOver()){
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
		if (g!=null && g.waitForPlayers()){
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
	}else if (tm instanceof SetSize){
	    g = getGame(p);
	    if (((SetSize) tm).w >= minimalWidth && ((SetSize) tm).w <= maximalWidth && ((SetSize) tm).h >= minimalHeight && ((SetSize) tm).h <= maximalHeight && g!=null && !p.isReady()){
		g.setSize(((SetSize) tm).w,((SetSize) tm).h);
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
		    if (g.isEmpty()){
			games.remove(g);
		    }
		}
		if (gameFound) {
		    p.send("UNREGOK"+" "+getLI(idGame)+"***");
		} else {
		    p.send("DUNNO***");
		}

		break;
	    case TypeMessage.GAMES:
		g = getGame(p);
		if (g==null || !p.isReady()){
		    // le joueur nest pas dans une game ou nest pas pret
		    for (Game g2 : games) {
			if (g2.waitForPlayers()){
			    count++;
			}
		    }
		    p.send("GAMES"+" "+getLI(count)+"***");
			        	
		    for (Game g2 : games){
			if (g2.waitForPlayers()){			
			    p.send("GAME"+" "+getLI(g2.getID())+" "+getLI(g2.getNumberOfPlayers())+"***");
			}
		    }
		}
		break;
	    case TypeMessage.QUIT:
		g = getGame(p);
		if (g != null){
		    disconnect(g,p);
		}
		break;
	    case TypeMessage.GLIST:
		g = getGame(p);
		if (g!=null ){
		    if (g.isPlaying()){
		    g.sendListOfPlayersPlaying(p);
		    }else if (g.isOver()){
			disconnect(g,p);
		    }
		}
		break;
	    case TypeMessage.CHANGETEAM:
		g = getGame(p);
		if (g != null && !p.isReady() && g.isTeam()){
		    g.changeTeam(p);
		}else{
		    p.send("DUNNO***");
		}
		break;
	    case TypeMessage.TLIST:
		g = getGame(p);
		if (g!=null && g.isTeam()){
		    if (g.isPlaying()){
			g.sendListOfTeam(p);
		    }else if (g.isOver()){
			disconnect(g,p);
		    }else{
			p.send("DUNNO***");
		    }
		}else{
		    p.send("DUNNO***");
		}
		break;
	    case TypeMessage.MAP:
		g = getGame(p);
		if (g != null && g.isPlaying()){
		    g.sendMap(p);
		    break;
		}else{
		    p.send("DUNNO***");
		}
		break;
	    case TypeMessage.POS:
		g = getGame(p);
		if (g!=null && g.isPlaying()){
		    g.sendPos(p);
		    gameFound = true;
		    break;
		}else{
		    p.send("DUNNO***");
		}
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

    public void disconnect(Game g, Player p){
	g.removePlayer(p);
	if (g.isEmpty()){
	    games.remove(g);
	}
	p.quit();
    }
    public Game getGame(Player p){
	for (Game g: games){
	    if (g.contains(p)){
		return g;
	    }
	}
	return null;
    }
public Game getGame(int n){
    
    for (Game g: games){
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
    public boolean isDuplicata( String ip, int port){
	for (Player p : players){
	    if (!p.isDifferent(ip,port)){
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
