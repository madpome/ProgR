import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;


class Occupe_Joueur implements Runnable {
    private Socket sock;
    private Serveur serveur;
    private ByteBuffer byteBuff;
    private Player p;
    
    public Occupe_Joueur (Socket socket, Serveur server, Player p) {
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
		String rcvMessage = readAMsg(sock).trim();
		TypeMessage mes = filtreMsg(rcvMessage);
		if (mes != null) {
		    serveur.processMessage(p, mes);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    public String readAMsg (Socket sc) {
	int nbrAst = 0;
	String res = "";
	try {
	    BufferedReader br = new BufferedReader (new InputStreamReader(sc.getInputStream()));
	    int c = 0;
	    while (nbrAst != 3) {
		c = br.read();
		res+= (char)c;
		if (c == '*') {
		    nbrAst++;
		} else {
		    nbrAst = 0;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return res;
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
	if (mots[0].equals("UP")) {
	    System.out.println ("Message : "+mots[0]);	    
	    if (len == 2) {
		int l = mots[1].length();
		flag = ((mots[1].substring(0, l-3)).length() == 2);
	    } else {
		return null;
	    }
	    type = 0;
	} else if (mots[0].equals("DOWN")) {
	    System.out.println ("Message : "+mots[0]);
	    
	    if (len == 2) {
		int l = mots[1].length();
		flag = ((mots[1].substring(0, l-3)).length() == 2);
	    } else {
		return null;
	    }
	    type = 1;	    
	} else if (mots[0].equals("RIGHT")) {
	    System.out.println ("Message : "+mots[0]);

	    if (len == 2) {
		int l = mots[1].length();
		flag = ((mots[1].substring(0, l-3)).length() == 2);
	    } else {
		return null;
	    }
	    type = 2;
	} else if (mots[0].equals("LEFT")) {
	    System.out.println ("Message : "+mots[0]);

	    if (len == 2) {
		int l = mots[1].length();
		flag = ((mots[1].substring(0, l-3)).length() == 2);
	    } else {
		return null;
	    }
	    type = 3;
	} else if (mots[0].equals("LIST?")) {
	    System.out.println ("Message : "+mots[0]);
		    
	    type = 5;
	    if (len == 2) {
		int l = mots[1].length();
		flag = ((mots[1].substring(0, l-3)).length() == 2);
	    } else {
		return null;
	    }

	} else if (mots[0].equals("ALL?")) {
	    type = 6;
	    String tmp[] = new String [mots.length - 1];
	    for (int i = 1; i<mots.length; i++) {
		tmp[i-2] = mots[i];
	    }
	    flag = lessThan200(tmp);

	} else if (mots[0].equals("SEND?")) {
	    System.out.println ("Message : "+mots[0]);
	    
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

	    
	} else if (mots[0].equals("NEW")) {
	    System.out.println ("Message : "+mots[0]);
	    type = 8;
	    if (len != 3) {
		return null;
	    } else {
		String id = mots[1];
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
		System.out.println ("id = " + id + "flag = "+flag);
				
		// Fin de la verificaotion de l'id
	    }
	} else if (mots[0].equals("REG")) {
	    System.out.println ("Message : "+mots[0]);
	    type = 9;
	    //REG id port m***
	    if (mots.length != 4) {
		return null;
	    }
	    String id = mots[1];
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
	} else if (mots[0].equals("START***")) {
	    System.out.println ("Message : "+mots[0]);
	    type = 10;
	    flag = (len == 1);
	} else if (mots[0].equals("UNREG***")) {
	    System.out.println ("Message : "+mots[0]);
	    type = 11;
	    flag = (len == 1);
	} else if (mots[0].equals("GAMES?***")) {
	    	    System.out.println ("Message : "+mots[0]);
	    type = 12;
	    flag = (len == 1);
	} else if (mots[0].equals("QUIT***")) {
	    	    System.out.println ("Message : "+mots[0]);
	    type = 13;
	    flag = (len == 1);
	} else if (mots[0].equals("GLIST?***")) {
	    System.out.println ("Message : "+mots[0]);
	    type = 14;
	    flag = (len == 1);
	}
	if (flag) {
	    int lenS = mots[len - 1].length();
	    mots[len-1] = mots[len - 1].substring(0, lenS-3);
	    res = determineTypeMessage(type, mots);
	    return res;
	} else {
	    return null;
	}
    }

    public TypeMessage determineTypeMessage (int type, String [] mots) {
	//Dans cette fonction, on sait que tout est bien formate
	if (0 <= type  && type <= 3) {
	    return (new Direction (Integer.parseInt(mots[1]), type));
	} else if (4 <= type  && type <= 5) {
	    return (new SizeList (mots[1], type));
	} else if (type == 6) {
	    return (new All (concatenateStringTab(mots, 1, mots.length - 1)));
	} else if (type == 7) {
	    return (new Send (mots[1], concatenateStringTab(mots, 2, mots.length - 1)));
	} else if (type == 8) {
	    return (new New (mots[1], Integer.parseInt(mots[2])));
	} else if (type == 9) {
	    return (new Reg (mots[1], Integer.parseInt(mots[2]), mots[3]));
	} else if (10 <= type && type <= 14) {
	    return (new NoArgs (type));
	} else {
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
	System.out.println ("s = " + s + " length = "+l);

	if (l < 1 || l > 8) {
	    return false;
	}
	System.out.println ("POIOPEIWQOPEIWQO");

	s = s.toUpperCase();
	for (int i = 0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if (!('0' <= c && c <= '9' || 'A' <= c && c <= 'Z')) {
		System.out.println (c);
		return false;
	    }
	}
	System.out.println ("SPODIPOSA ");
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
	    PrintWriter pw = new PrintWriter (new OutputStreamWriter (sock.getOutputStream()));
	    pw.println (s);
	    pw.flush();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}

