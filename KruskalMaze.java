
import java.util.*;
import java.io.*;
class cell {
int x;
int y;
//Si mur alors -1
int set;
int type;
public cell(int x,int y,int set,int type){
	this.x =x;
	this.y =y;
	this.set = set;
	this.type = type;
}
}
public class KruskalMaze {
public static int[][] getNewMaze(int width, int height){
	ArrayList<Integer> set = new ArrayList<Integer>();
	ArrayList<cell> walls = new ArrayList<cell>();
	//-1 = WALL 0 = CELL N >=1 = EITHER OF THEM + N = COMPOSANTE ID
	cell [][] maze = new cell[height][width];
	//init
	init_maze(maze,set,walls);
	createMaze(maze,set,walls);
	return cellToInt(maze);
}
public static void init_maze(cell[][] maze,ArrayList<Integer>set,ArrayList<cell>walls){
	int width = maze[0].length;
	int height = maze.length;
	int id = 1;
	for(int i = 0; i< maze.length; i++) {
		for(int j = 0; j<maze[0].length; j++) {
			if(i==0 || i==(height-1) || j==0 || j==(width-1)) {
				maze[i][j] = new cell(j,i,-1,-1);
			}else{
				if(i%2 == 1) {
					if(j%2 == 1) {
						set.add(id);
						maze[i][j] = new cell(j,i,id++,1);
					}else{
						maze[i][j] = new cell(j,i,-1,-1);
						walls.add(maze[i][j]);
					}
				}else{
					if(j%2 == -1) {
						set.add(id);
						maze[i][j] = new cell(j,i,id++,1);
					}else{
						maze[i][j] = new cell(j,i,-1,-1);
						walls.add(maze[i][j]);

					}
				}
			}
		}
	}
}
/*
   Algo :
   Tant qu'il y a plus d'une COMPOSANTE
   On prend un mur vert au hasard
   Si ses voisins sont d'un id different, on les unifie (reparcour toute la maze)
   Sinon on créer un mur ? pas trop compris mais go test
   et on continue
 */
//Tant qu'il y a plus d'une composante
//Pour avoir un wall au random vaut mieux les stocker je pense ?
public static void createMaze(cell[][] maze,ArrayList<Integer> set,ArrayList<cell>walls){
	Random rand = new Random();
	int width = maze[0].length;
	int height = maze.length;
	cell truc;
	int x;
	int y;
	while(set.size()>1) {
		truc = walls.remove(rand.nextInt(walls.size()));
		x = truc.x;
		y = truc.y;
		//Si pas tous le meme set, on les relies
		int set1 = maze[y+1][x].set;
		int set2 = maze[y-1][x].set;
		int set3 = maze[y][x-1].set;
		int set4 = maze[y][x+1].set;
		int tochange2 =-1;
		int tochange = -1;
		if(set1 !=-1) {
			tochange2 =set1;
			if(set2!=-1) {
				if(set2!=set1) tochange = set2;
			}else if(set3!=-1) {
				if(set1!=set3) tochange = set3;
			}else if(set4!=-1) {
				if(set1!=set4) tochange = set4;
			}
		}else if(set2!=-1) {
			tochange2 = set2;
			if(set3!=-1) {
				if(set3!=set2) tochange = set3;
			}else if(set4!=-1) {
				if(set4!=set2) tochange = set4;
			}
		}else if(set3!=-1) {
			tochange2=set3;
			if(set4!=-1) {
				if(set3!=set4) tochange = set4;
			}
		}
		int set5 =-1;
		for(int i = 1; i<height; i++) {
			for(int j = 1; j<width; j++) {
				set5 = maze[i][j].set;
				if(set5==tochange && set5!=-1 && tochange!=-1) {
					maze[i][j].set = tochange2;
				}
			}
		}
		if(tochange!=-1) {
			maze[y][x] = new cell(y,x,tochange2,1);
			set.remove(new Integer(tochange));
		}
		//afficheMaze(maze);
	}
}
public static void afficheMaze(cell [][] maze){
	System.out.println("");
	for(int i = 0; i < maze.length; i++) {
		for(int j =0; j<maze[0].length; j++) {
			if(maze[i][j].type == 1) {
				System.out.print("  ");
			}else{
				System.out.print("\u2588\u2588");
			}
		}
		System.out.println("");
	}
}
public static void saveMaze(cell [][] maze,String name){
	BufferedWriter output = null;
	try{
		File file = new File(name+".maze");
		output = new BufferedWriter(new FileWriter(file));
		for(int i = 0; i<maze.length; i++) {
			for(int j = 0; j<maze[0].length; j++) {
				output.write((maze[i][j].type==1) ? '1' : '0');
			}
			output.write("\n");
		}
		output.close();
	}catch(Exception e) {
		e.printStackTrace();
	}
}

private static int[][] cellToInt(cell[][] cells){
	int[][] maze = new int[cells.length][cells[0].length];
	for (int i=0; i<cells.length; i++) {
		for (int j= 0; j<cells[0].length; j++) {
			maze[i][j] = cells[i][j].type == 1 ? 1 : 0;
		}
	}
	return maze;
}
}
