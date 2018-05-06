import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;


class Occupe_Joueur implements Runnable {
    private SocketChannel sock;
    private Serveur serveur;
    private ByteBuffer byteBuff;
    private Player p;
    
    public Occupe_Joueur (SocketChannel socket, Serveur server, Player p) {
	this.sock = socket;
	this.serveur = server;
	this.p = p;
	
    }

    public void run () {
	serveur.processMessage(p,new NoArgs(TypeMessage.GAMES));
	try {
	    while (true) {
		/* On prend une ligne.
		   Si elle ne se termine pas par "***"
		   Alors on n'envoie rien
		   Si elle contient "***" pas à la fin
		   Alors on n'envoie rien
		   Sinon, on la verifie, et on envoie si c'est valide
		*/
		String rcvMessage = readAMsg(sock);
		TypeMessage mes = filtreMsg(rcvMessage);
		if (mes != null) {
		    serveur.processMessage(p, mes);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public String readAMsg (SocketChannel sc) {
	int nbrAst = 0;
	String res = "";
	
	// On alloue un ByteBuffer pour lire un par un
	ByteBuffer bb = ByteBuffer.allocate(1);
	StringBuilder sb = new StringBuilder();
	char cRead = '\0';
	while (true) {
	    try {
		sc.read(bb);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    byte [] byteTab = new  byte[1];
	    bb.get(byteTab);
	    String s = new String(byteTab);
	    char c = s.charAt(0);
	    if (c == '*') {
		nbrAst++;
	    } else {
		nbrAst = 0;
	    }
	    res = res + c;
	    if (nbrAst == 3) {
		return res;
	    }
	}
    }
    
    /*
      Liste des messages valides :
      0/1/2/3  UP/DOWN/LEFT/RIGHT d***
      4/ SIZE? m***
      5/ LIST? m***
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
    public TypeMessage filtreMsg (String msg) {
	// On verifie que la fin de la chaine est ***
	for (int i = 0; i<3; i++) {
	    int index = msg.length() - 1 - i;
	    if (msg.charAt(index) != '*') {
		return null;
	    }
		
	}
	// On verifie que la chaine ne contient pas *** avant la fin
	for (int i = 0; i<msg.length()-3; i++) {
	    if (msg.charAt(i) == '*' &&
		msg.charAt(i+1) == '*' &&
		msg.charAt(i+2) == '*') {
		return null;
	    }
	}	    

	    
	String [] mots = msg.split (" ");
	int len = mots.length;
	boolean flag = false;
	int type = -1;
	TypeMessage res;
	if (len == 0) {
	    return null;
	}
	switch (mots[0]){
	case "UP" :
	    type = 0;
	case "DOWN" :
	    type = 1;
	case "RIGHT" :
	    type = 2;
	case "LEFT" :
	    type = 3;
	case "SIZE?" :
	    type = 4;
	case "LIST?" :
	    type = 5;
	    if (len == 2) {
		int l = mots[1].length();
		flag = ((mots[1].substring(0, l-3)).length() == 2);
	    } else {
		return null;
	    }
	    break;
	case "ALL?" :
	    type = 6;
	    String tmp[] = new String [mots.length - 1];
	    for (int i = 1; i<mots.length; i++) {
		tmp[i-2] = mots[i];
	    }
	    flag = lessThan200(tmp);
	    break;
	case "SEND?" :
	    type = 7;
	    //SEND? id message***
	    if (len != 3) {
		return null;
	    }
	    String id = mots[1];
	    // Debut de la verification de l'id
	    flag = (isAlphaNum(id));
	    
	    String tmp2[] = new String [mots.length - 2];
	    for (int i = 2; i<mots.length; i++) {
		tmp2[i-2] = mots[i];
	    }
	    flag = flag && isAlphaNum(id) && lessThan200(tmp2);
	    // Fin de la verification de l'id, on est sur que id est une chaine de longueur comprise entre 1 et 8, alphanumerique
	    break;
	case "NEW" :
	    type = 8;
	    if (len != 3) {
		return null;
	    } else {
		id = mots[1];
		String port = mots[2];

		// Debut de la verification du port
		int l = port.length();
		if (l < 3) {
		    // si mots[2] == "***" par exemple
		    return null;
		}
		port = port.substring(0, l-3);
		if (!isNumber(port)) {
		    return null;
		}
		// Fin de la verification du port, le port est un int

		// Debut de la verification de l'id
		l = id.length();
		flag = isAlphaNum(id);
		// Fin de la verificaotion de l'id
	    }
	    break;
	case "REG" :
	    type = 9;
	    //REG id port m***
	    if (mots.length != 4) {
		return null;
	    }
	    id = mots[1];
	    String port = mots[2];
	    String m = mots[3];
	    
	    // Debut de la verification de l'id
	    flag = isAlphaNum(id);
	    // Fin de la verification de l'id
	    
	    // On est sur que id est une chaine de longueur comprise
	    // entre 1 et 8, alphanumerique
	    
	    // Debut de la verification du port
	    int l = port.length();
	    if (!isNumber(port)) {
		return null;
	    }
	    // Fin de la verification du port, le port est un int
	    
	    // Debut de la verification de m
	    m = mots[3].substring(0, m.length()-3);
	    flag = flag && (m.length() == 2);
	    break;
	case "START***" :
	    type = 10;
	case "UNREG***" :
	    type = 11;
	case "GAMES?***" :
	    type = 12;
	case "QUIT***" :
	    type = 13;
	case "GLIST?***" :
	    type = 14;
	    flag = (len == 1);
	    break;
	default :
	    return null;
	}
	if (flag) {
	    int lenS = mots[len - 1].length();
	    mots[len - 1] = mots[len - 1].substring(0, len-3);
	    res = determineTypeMessage(type, mots);
	    return res;
	} else {
	    return null;
	}
    }

    public TypeMessage determineTypeMessage (int type, String [] mots) {
	//Dans cette fonction, on sait que tout est bien formate
	switch (type) {
	case 0 :
	case 1 :
	case 2 :
	case 3 :
	    return (new Direction (Integer.parseInt(mots[1]), type));
	case 4 :
	case 5 :
	    return (new SizeList (mots[1], type));
	case 6 :
	    return (new All (concatenateStringTab(mots, 1, mots.length - 1)));
	case 7 :
	    return (new Send (mots[1], concatenateStringTab(mots, 2, mots.length - 1)));
	case 8 :
	    return (new New (mots[1], Integer.parseInt(mots[2])));
	case 9 :
	    return (new Reg (mots[1], Integer.parseInt(mots[2]), mots[3]));
	case 10 :
	case 11 :
	case 12 :
	case 13 :
	case 14 :
	    return (new NoArgs (type));
	default :
	    return null;
	}     
    }

    public String concatenateStringTab (String[] mots, int fst, int lst) {
	String res = "";
	for (int i = fst; i<=lst; i++) {
	    res+= mots[i]+" ";
	}
	return res;
    }
    public boolean isNumber (String s) {
	try {
	    int i = Integer.valueOf(s);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    public boolean isAlphaNum (String s) {
	int l = s.length();
	if (l < 1 || l > 8) {
	    return false;
	}
	s = s.toUpperCase();
	for (int i = 0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if (!('0' < c && c < '9' || 'A' < c && c < 'Z')) {
		return false;
	    }
	}
	return true;
    }

    public boolean lessThan200 (String [] mess) {
	int len = 0;
	int last = mess.length - 1;
	for (int i = 0; i<mess.length-1; i++) {
	    len+= 1 + mess[i].length();
	}
	// -3 pour enlever les 3 * a la fin
	return (len + mess[last].length() - 3 <= 200);
    }

    public void writeToClient (String s) {
	try {
	    byteBuff = ByteBuffer.wrap(s.getBytes());
	    sock.write(byteBuff);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}

