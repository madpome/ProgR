import java.util.*;
class cell {
boolean unvisited;
boolean lwall,rwall,uwall,dwall;
int x;
int y;
public cell(){
	unvisited = true;
	lwall = rwall = uwall = dwall =true;
}
public void visit(){
	unvisited = false;
}
public void setX(int x){
	this.x=x;
}
public void setY(int y){
	this.y=y;
}
}
public class MazeGenerator {
public static void main(String [] args){
	if(args.length != 2) {
		System.out.println("Require a width and a height");
		System.exit(1);
	}
	int height = 0; //Nombre de lignes
	int width =  0; //Nombre de colonnes
	try{
		width = Integer.parseInt(args[0]);
		height = Integer.parseInt(args[1]);
	}catch(Exception e) {
		e.printStackTrace();
		System.out.println("Wrong arguments");
		System.exit(1);
	}
	if(height <=0 || width <=0) {
		System.out.println("width or height too small");
		System.exit(1);
	}
	Stack<cell> stack = new Stack<cell>();
	int nb_unvisited = height*width;
	cell [][] maze = new cell[height][width];
	init_maze(maze,width,height);
	cell curr = maze[0][0];
	curr.visit();
	nb_unvisited--;
	int x = 0;
	int y = 0;
	int k = 0;
	Random rand = new Random();
	ArrayList<cell> neighbour;
	while(nb_unvisited!=0) {
		neighbour = unvisited(x,y,width,height,maze);
		if(neighbour.size() != 0) {
			k = rand.nextInt(neighbour.size());
			stack.push(curr);
			removeWall(curr,neighbour.get(k));
			curr = neighbour.get(k);
			nb_unvisited--;
			curr.visit();
		}else{
			if(stack.empty() && nb_unvisited !=0) {

			}else{
				curr = stack.pop();
			}
		}
	}

	//Idea : create a wall (horizontal or vertical)
	//Then create a hole in this wall, and repeat on the two thing created until there's no possible wall

}
public static ArrayList<cell> unvisited(int x, int y,int width, int height,cell[][] maze){
	ArrayList<cell> list=new ArrayList<cell>();
	if(x+1<width && maze[y][x+1].unvisited) {
		list.add(maze[y][x+1]);
	}
	if(y+1<height && maze[y+1][x].unvisited) {
		list.add(maze[y+1][x]);
	}
	if(y-1>=0 && maze[y-1][x].unvisited) {
		list.add(maze[y-1][x]);
	}
	if(x-1>=0 && maze[y][x-1].unvisited) {
		list.add(maze[y][x-1]);
	}
	return list;
}
public static void removeWall(cell curr, cell neigh){
	if(curr.x>neigh.x) {
		curr.rwall = false;
		neigh.lwall = false;
	}else if(curr.x < neigh.x) {
		curr.lwall = false;
		neigh.rwall = false;
	}else if(curr.y > neigh.y) {
		curr.dwall = false;
		neigh.uwall = false;
	}else{
		curr.uwall = false;
		neigh.dwall = false;
	}
}
public static void init_maze(cell[][] maze, int width, int height){
	for( int j= 0; j<height; j++) {
		for (int i=0; i<width; i++) {
			maze[j][i]=new  cell();
			maze[j][i].setX(i);
			maze[j][i].setY(j);
		}
	}
}
//infX/supX sont les bornes inf de mon maze que je suis en train de construire
public void createMaze(char [][] maze, int infX,int supX, int infY,int supY){
	//Si la distance de infX et supX =1 ET idem pour Y alors on stop car on a plus la place frere je crois je sais pas en fait
	//Random pour savoir si ligne ou colonnes
	//Random pour savoir laquelle

}
}
