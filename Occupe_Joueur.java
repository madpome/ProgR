import java.util.*;
import java.net.*;
import java.io.*;

class Occupe_Joueur implements Runnable {
    private Socket sock;
    private Serveur serveur;
    public Occupe_Joueur (Socket socket, Serveur server) {
	this.sock = socket;
	this.serveur = server;
    }

    public void run () {
	try {
	    BufferedReader br = new BufferedReader (new InputStreamReader (sock.getInputStream()));
	    PrintWriter pw = new PrintWriter (new OutputStreamWriter (sock.getOutputStream()));

	    while (true) {
		/* On prend une ligne.
		   Si elle ne se termine pas par "***"
		   Alors on n'envoie rien
		   Si elle contient "***" pas à la fin
		   Alors on n'envoie rien
		   Sinon, on la verifie, et on envoie si c'est valide
		*/
		
		String rcvMessage = br.readLine();

		if (filtreMsg(rcvMessage)) {
		    pw.println (rcvMessage);
		    pw.flush();
		    /*
		      serveur.getMessage(rcvMessage);
		     */
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    public boolean filtreMsg (String msg) {
	// On verifie que la fin de la chaine est ***
	for (int i = 0; i<3; i++) {
	    int index = msg.length() - 1 - i;
	    if (msg.charAt(index) != '*') {
		return false;
	    }
		
	}
	// On verifie que la chaine ne contient pas *** avant la fin
	for (int i = 0; i<msg.length()-3; i++) {
	    if (msg.charAt(i) == '*' &&
		msg.charAt(i+1) == '*' &&
		msg.charAt(i+2) == '*') {
		return false;
	    }
	}	    

	    
	String [] mots = msg.split (" ");
	int len = mots.length;
	if (len == 0) {
	    return false;
	}
	switch (mots[0]){
	case "UP" :
	case "DOWN" :
	case "RIGHT" :
	case "LEFT" :
	case "SIZE?" :
	case "LIST?" :
	    if (len == 2) {
		int l = mots[1].length();
		return isNumber(mots[1].substring(0, l-3));
	    } else {
		return false;
	    }
	case "QUIT***" :
	case "GLIST?***" :
	case "START***" :
	case "UNREG***" :
	case "GAMES?***" :
	    return (len == 1);
	case "ALL?" :
	    return true;
	case "SEND?" :
	    //SEND? id message***
	    if (len != 3) {
		return false;
	    }
	    String id = mots[1];
	    // Debut de la verification de l'id
	    int l = id.length();
	    if (l<1 || l > 8) {
		return false;
	    }
	    return isNumber(id);
	    // Fin de la verification de l'id, on est sur que id est une chaine de longueur comprise entre 1 et 8, alphanumerique

	case "NEW" :
	    if (len != 3) {
		return false;
	    } else {
		id = mots[1];
		String port = mots[2];

		// Debut de la verification du port
		l = port.length();
		if (l < 3) {
		    // si mots[2] == "***" par exemple
		    return false;
		}
		port = port.substring(0, l-3);
		if (!isNumber(port)) {
		    return false;
		}
		// Fin de la verification du port, le port est un int

		// Debut de la verification de l'id
		l = id.length();
		if (l<1 || l > 8) {
		    return false;
		} else {
		    return (isAlphaNum(id));
		}
		// Fin de la verificaotion de l'id
	    }

	case "REG" :
	    //REG id port m***
	    if (mots.length != 4) {
		return false;
	    }
	    id = mots[1];
	    String port = mots[2];
	    String m = mots[3];
	    
	    // Debut de la verification de l'id
	    l = id.length();
	    if (l<1 || l > 8) {
		return false;
	    } else if (!isAlphaNum(id)) {
		return false;
	    }
	    // Fin de la verification de l'id, on est sur que id est une chaine de longueur comprise entre 1 et 8, alphanumerique

	    // Debut de la verification du port
	    l = port.length();
	    if (!isNumber(port)) {
		return false;
	    }
	    // Fin de la verification du port, le port est un int

	    // Debut de la verification de m
	    m = mots[3].substring(0, m.length()-3);
	    return isNumber(m);
	default :
	    return false;
	}
    }

    /*
      Liste des messages valides :
      UP/DOWN/LEFT/RIGHT d***
      SIZE? m***
      LIST? m***
      ALL? message***
      SEND? id message***
      NEW id port***
      REG id port m***
      START***
      UNREG***
      GAMES?***
      QUIT***
      GLIST?***

    */

    public boolean isNumber (String s) {
	try {
	    int i = Integer.valueOf(s);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    public boolean isAlphaNum (String s) {
	s = s.toUpperCase();
	for (int i = 0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if (!('0' < c && c < '9' || 'A' < c && c < 'Z')) {
		return false;
	    }
	}
	return true;
    }

}

