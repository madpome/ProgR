
public class Ghost {
	private int x;
	private int y;
	private int speed;
	private int timeBeforeMove;
	// pattern de déplacement ?
	
	public Ghost(int x, int y, int s) {
		this.x = x;
		this.y = y;
		speed = timeBeforeMove = s;
	}
	
	public void update() {
		timeBeforeMove--;
	}
	
	public boolean willMove() {
		return timeBeforeMove == 0;
	}
	
	public void moove(int[][] maze) {
		//compute x & y
		timeBeforeMove = speed;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
