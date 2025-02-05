import java.lang.*;

class TypeMessage {
public static final int UP = 0;
public static final int DOWN = 1;
public static final int LEFT = 2;
public static final int RIGHT = 3;
public static final int SIZE = 4;
public static final int LIST = 5;
public static final int ALL = 6;
public static final int SEND = 7;
public static final int NEW = 8;
public static final int REG  = 9;
public static final int START = 10;
public static final int UNREG = 11;
public static final int GAMES = 12;
public static final int QUIT = 13;
public static final int GLIST = 14;
public static final int TLIST = 15;
public static final int CHANGETEAM = 16;
public static final int NEWT = 17;
public static final int REGT = 18;
public static final int MAP = 19;
public static final int POS = 20;
public static final int SETSIZE = 21;
public static final int ADDBOT = 22;
public static final int RMBOT = 23;
public static final int BOT = 24;

/*
   Liste des messages valides :
   0/1/2/3  UP/DOWN/LEFT/RIGHT d***
   4/5/ SIZE?/LIST? m***
   6/ ALL? message***
   7/ SEND? id message***
   8/ NEW id port***
   9/ REG id port m***
   10/ START***
   11/ UNREG***
   12/ GAMES?***
   13/ QUIT***
   14/ GLIST?***
   15/ TLIST?***
   16/ CHANGETEAM***
   17/ NEWT id port***
   18/ REGT id port m***
   19/ MAP***
   20/ POS?***
   21/ SETSIZE h w***

 */
}

class Direction extends TypeMessage {
int pas;
int direction;         // 0 = UP, 1 = DOWN, 2 = LEFT, 3 = RIGHT

public Direction (int pas, int direction) {
	this.pas = pas;
	this.direction = direction;
}
}

class SizeList extends TypeMessage {
int m;
int type;         //4 = SIZE?, 5 = LIST?

public SizeList (String m, int type) {
	try{
		this.m = Integer.parseInt(m);
	}catch(NumberFormatException e) {
		System.out.println("Err SizeList constru");
	}
	this.type = type;
}
}

class All extends TypeMessage {
String message;

public All (String mes) {
	this.message = mes;
}
}

class Send extends TypeMessage {
String id;
String message;

public Send (String id, String message) {
	this.id = id;
	this.message = message;
}
}

class New extends TypeMessage {
String id;
int port;
boolean team;         // false = solo, true = team

public New (String id, int port, boolean f) {
	this.id = id;
	this.port = port;
	this.team = f;
}
public boolean isTeam(){
	return team;
}
}

class Reg extends TypeMessage {
String id;
int port;
int m;
boolean team;         // false = solo, true = team

public Reg (String id, int port, String m, boolean team) {
	this.id = id;
	this.port = port;
	try{
		this.m = Integer.parseInt(m);
	}catch(NumberFormatException e) {
		System.out.println("Err Reg constru");
	}
	this.team = team;
}
public boolean isTeam(){

	return team;

}

}

class NoArgs extends TypeMessage {
int type;         // 10 = START, 11 = UNREG, 12 = GAMES?, 13 = QUIT, 14 = GLIST?, 15 = TLIST?, 16 = CHANGETEAM, 19 = MAP, 20 = POS
public NoArgs (int type) {
	this.type = type;
}
}

class SetSize extends TypeMessage {
int type, h, w;

public SetSize (int type, int h, int w) {
	this.type = type;
	this.h = h;
	this.w = w;
}
}
