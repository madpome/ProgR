
public class Ghost {
	private int x;
	private int y;
	private int speed;
	private int timeBeforeMove;
	// pattern de deplacement ?
	
	public Ghost(int s, int[][] maze) {
		this.x = 0;
		this.y = 0;
		speed = timeBeforeMove = s;
		
		//on place le fantome aleatoirement en appelant moove
		this.moove(maze);
		
	}
	
	public void update() {
		timeBeforeMove--;
	}
	
	public boolean willMove() {
		return timeBeforeMove == 0;
	}
	
	public void moove(int[][] maze) {
		int height = maze.length;
		int width = maze[0].length;
		
		int x;
		int y;
		
		do {
			x = (int) (Math.random()*height);
			y = (int) (Math.random()*width);
		}while(maze[x][y] == 0);
		
		this.x = x;
		this.y =y;
		
		timeBeforeMove = speed;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
