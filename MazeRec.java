import java.util.*;
public class MazeRec {
static Random rand = new Random();
static Scanner sc = new Scanner(System.in);
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
	//1 = noWall 0 = Wall
	int xmin = 1;
	int ymin = 1;
	int xmax = width -1;
	int ymax = height -1;
	char [][] maze = new char[height][width];
	for(int i = 0; i<height; i++) {
		for(int j = 0; j<width; j++) {
			if(i==0 || j==0  || i==(height-1) || j==(width-1)) {
				maze[i][j] = '0';
			}else{
				maze[i][j] = '1';
			}
		}
	}
	for(int i = 0; i < height; i++) {
		for(int j =0; j<width; j++) {
			System.out.print(""+maze[i][j]);
		}
		System.out.println("");
	}
	makeMaze(maze,xmin,ymin,xmax,ymax,1,1);
	for(int i = 0; i < height; i++) {
		for(int j =0; j<width; j++) {
			if(maze[i][j] == '1') {
				System.out.print(' ');
			}else{
				System.out.print("\u2588");
			}
		}
		System.out.println("");
	}
}
public static void afficheMaze(char [][] maze){
	for(int i = 0; i<maze[0].length; i++) {
		System.out.print(".");
	}
	System.out.println("");
	for(int i = 0; i < maze.length; i++) {
		for(int j =0; j<maze[0].length; j++) {
			if(maze[i][j] == '1') {
				System.out.print(' ');
			}else{
				System.out.print("\u2588");
			}
		}
		System.out.println("");
	}
}
public static void makeMaze(char [][] maze, int xmin,int ymin,int xmax, int ymax, int side,int first){
	if((xmax-xmin)<=3 || (ymax-ymin)<=3) {
		return;
	}
	//afficheMaze(maze);
	//System.out.println("xmin = "+xmin+" ymin = "+ymin+" xmax = "+xmax+" ymax = "+ymax);
	//sc.nextLine();
	//Ligne horizontale, sinon verticale
	//On coupe au milieu car balec frere
	if(side==1) {
		for(int i=xmin; i<xmax; i++) {
			maze[(ymin+ymax)/2][i] = '0';
		}
		if(first==0) {
			int a = rand.nextInt(xmax-xmin-2)+xmin+1;
			if (a == (xmax+xmin)/2) {
				a++;
			}
			maze[(ymin+ymax)/2][a] = '1';
		}else{
			int a = rand.nextInt(xmax-xmin-1)/2+1;
			if (a == (xmax+xmin)/2) {
				a--;
			}
			maze[(ymin+ymax)/2][a] = '1';
			a = rand.nextInt((xmax-xmin)/2)+(xmax+xmin)/2;
			if (a == (xmax+xmin)/2) {
				a++;
			}
			maze[(ymin+ymax)/2][a] = '1';
		}
		makeMaze(maze,xmin,ymin,xmax,(ymin+ymax)/2,1-side,0);
		makeMaze(maze,xmin,((ymin+ymax)/2),xmax,ymax,1-side,0);

	}else{
		for(int i=ymin; i<ymax; i++) {
			maze[i][(xmin+xmax)/2] = '0';
		}
		int a = rand.nextInt(ymax-ymin-1)+1+ymin;
		if (a == (ymax+ymin)/2) {
			a++;
		}
		maze[a][(xmin+xmax)/2]= '1';
		makeMaze(maze,xmin,ymin,(xmin+xmax)/2,ymax,1-side,0);
		makeMaze(maze,(xmin+xmax)/2,ymin,xmax,ymax,1-side,0);
	}
}
}
