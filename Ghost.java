import java.util.LinkedList;
public class Ghost {
private int x;
private int y;
private int speed;
private int level;
private int timeBeforeMove;
// pattern de deplacement ?

    public Ghost(int s, int[][] maze, LinkedList<Player> players, LinkedList<Ghost> ghosts) {
	this.x = 0;
	this.y = 0;
	level = (int)((Math.random()*5)+1);
	speed = timeBeforeMove = s;
	speed-=(level*2);

	//on place le fantome aleatoirement en appelant moove
	this.move(maze, players, ghosts);

}

public void update() {
	timeBeforeMove--;
}

public boolean willMove() {
	return timeBeforeMove == 0;
}

    public void move(int[][] maze, LinkedList<Player> players, LinkedList<Ghost> ghosts) {
	int x;
	int y;
	boolean crossSomething;
	do {
		y = (int) (Math.random()*maze.length);
		x = (int) (Math.random()*maze[0].length);
		crossSomething = false;
		for (Player p : players){
		    if (p.getX() == x && p.getY() == y){
			crossSomething = true;
		    }
		}
		for (Ghost g : ghosts){
		    if (g.getX() == x && g.getY() == y){
			crossSomething = true;
		    }
		}
	} while(maze[y][x] == 0 || crossSomething);

	this.x = x;
	this.y = y;

	timeBeforeMove = speed;
}
public int getLevel(){
	return level;
}
public int getX() {
	return x;
}
public int getY() {
	return y;
}
}
