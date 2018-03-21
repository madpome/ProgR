
public class Player {
	
	private String id;
	private int port;
	//classe d'envoie
	
	private int x;
	private int y;
	private int score;
	
	public Player(String id, int port) {
		this.id = id;
		this.port = port;
		//create reception and send object
	}
	public void moove(int x, int y, boolean crossedGhost) {
		this.x = x;
		this.y = y;
		if (crossedGhost) {
			score += 1;
			// send [MOF x y p***]
		}else {
			// send [MOV x y***] 
		}
	}
	public void initialize(int x, int y) {	
		this.x = x;
		this.y = y;
		this.score = 0;
		// send [POS id x y***]
	}
	public String getId() {
		return id;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
