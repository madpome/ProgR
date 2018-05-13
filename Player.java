public class Player {

private String id;
private int port;
private String ip;
private Occupe_Joueur oj;
//classe d'envoie
private int x;
private int y;
private boolean bot;
// 0 ou 1
private int timeBeforeMove;
private int team;
private int score;
private boolean ready;

public Player(String id, int port) {
	bot = false;
	this.id = id;
	this.port = port;
	timeBeforeMove = 0;
	ready = false;
	//create reception and send object
}
public void setBot(boolean b){
	bot = b;
}
public void update(){
	timeBeforeMove--;
}
public boolean willMove(){
	return timeBeforeMove == 0;
}
public void setTime(int time){
	this.timeBeforeMove = time;
}
public boolean isBot(){
	return bot;
}
public Player(){
	ready = false;
}
public void setId(String id) {
	this.id = id;
}
public int getTeam(){
	return team;
}
public void setTeam(int eq){
	this.team = eq;
}
public void setPort(int port) {
	this.port = port;
}
public void move(int x, int y, int ghLevel) {
	String mes;
	this.x = x;
	this.y = y;
	if (ghLevel > 0) {
		score += ghLevel;
		mes = "MOF "+char3(x)+" "+char3(y)+" "+char4(score)+"***";
	}else {
		mes = "MOV "+char3(x)+" "+char3(y)+"***";
	}
	send(mes);
}
public void initializePosition(int[][] maze) {
	// on initialise le score du joueur a 0 et sa position de maniere aleatoire dans le labyrinthe
	this.score = 0;

	int height = maze.length;
	int width = maze[0].length;

	int x;
	int y;

	do {
		y = (int) (Math.random()*height);
		x = (int) (Math.random()*width);
	} while(maze[y][x] == 0);

	this.x = x;
	this.y = y;
}
public void send(String message) {
	oj.writeToClient(message);
}

public void sendPosition() {
	String message = "POS "+" "+id+" "+char3(x)+" "+char3(y)+"***";
	send(message);
}
public void quit() {
	// disconnect the player
	send("BYE***");
}
public void setOJ(Occupe_Joueur oj){
	this.oj = oj;
}
public String getId() {
	return id;
}
public int getPort(){
	return port;
}
public String getIp(){
	return ip;
}
public void setIp(String ip){
	this.ip = ip;
}
public int getX() {
	return x;
}
public int getY() {
	return y;
}
public int getScore() {
	return score;
}

public boolean isReady() {
	return ready;
}

public void setReady() {
	ready = true;
}

public void setNotReady() {
	ready = false;
}

public String char3(int x) {
	if (x<10)
		return "00"+x;
	if (x<100)
		return "0"+x;
	if (x<1000)
		return ""+x;
	return "999";
}
public String char4(int x) {
	if (x<10)
		return "000"+x;
	if (x<100)
		return "00"+x;
	if (x<1000)
		return "0"+x;
	if (x<10000)
		return ""+x;
	return "9999";
}
public boolean isDifferent(String ip, int port){
	return (!((ip.equals(this.ip) && this.port == port)));
}
}
