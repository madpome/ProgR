class TypeMessage {
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

    */        
}

class Direction extends TypeMessage {
    int pas;
    int direction; // 0 = UP, 1 = DOWN, 2 = LEFT, 3 = RIGHT
    
    public Direction (int pas, int direction) {
	this.pas = pas;
	this.direction = direction;
    }
}

class SizeList extends TypeMessage {
    int m;
    int type; //4 = SIZE?, 5 = LIST?
    
    public SizeList (int m, int type) {
	this.m = m;
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

    public New (String id, int port) {
	this.id = id;
	this.port = port;
    }
}

class Reg extends TypeMessage {
    String id;
    int port;
    int m;

    public Reg (String id, int port, int m) {
	this.id = id;
	this.port = port;
	this.m = m;
    }
}

class NoArgs extends TypeMessage {
    int type; // 10 = START, 11 = UNREG, 12 = GAMES?, 13 = QUIT, 14 = GLIST?
    public NoArgs (int type) {
	this.type = type;
    }
}
