
public class Ghost {
private int x;
private int y;
private int speed;
private int level;
private int timeBeforeMove;
// pattern de deplacement ?

public Ghost(int s, int[][] maze) {
	this.x = 0;
	this.y = 0;
	level = (int)Math.random()*5+1;
	speed = timeBeforeMove = s;
	speed-=(level*2);

	//on place le fantome aleatoirement en appelant moove
	this.move(maze);

}

public void update() {
	timeBeforeMove--;
}

public boolean willMove() {
	return timeBeforeMove == 0;
}

public void move(int[][] maze) {
	int x;
	int y;

	do {
		x = (int) (Math.random()*maze.length);
		y = (int) (Math.random()*maze[0].length);
	} while(maze[x][y] == 0);

	this.x = y;
	this.y =x;

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
