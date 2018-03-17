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
	char [][] maze = new char[height][width];
	for(int i = 0; i<height; i++) {
		for (int j = 0; j<width; j++) {
			maze[i][j] = 0;
		}
	}
	//Idea : create a wall (horizontal or vertical)
	//Then create a hole in this wall, and repeat on the two thing created until there's no possible wall

}
//infX/supX sont les bornes inf de mon maze que je suis en train de construire
public void createMaze(char [][] maze, int infX,int supX, int infY,int supY){
	//Si la distance de infX et supX =1 ET idem pour Y alors on stop car on a plus la place frere je crois je sais pas en fait
	//Random pour savoir si ligne ou colonnes
	//Random pour savoir laquelle

}
}
