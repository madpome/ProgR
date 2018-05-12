import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.concurrent.TimeUnit;


class Occupe_Joueur implements Runnable {
    private Socket sock;
    private Serveur serveur;
    private ByteBuffer byteBuff;
    private Player p;
    private BufferedReader br;
    private PrintWriter pw;
    public Occupe_Joueur (Socket socket, Serveur server, Player p) {
	this.sock = socket;
	this.serveur = server;
	this.p = p;
	try {
	    this.br = new BufferedReader (new InputStreamReader(sock.getInputStream()));
	    this.pw = new PrintWriter (new OutputStreamWriter (sock.getOutputStream()));
	} catch (Exception e) {}

    }

    public void run () {
	serveur.processMessage(p,new NoArgs(TypeMessage.GAMES));
	int step = 0;
	try {
	    while (true) {
		/* On prend une ligne.
		   Si elle ne se termine pas par "***"
		   Alors on n'envoie rien
		   Si elle contient "***" pas à la fin
		   Alors on n'envoie rien
		   Sinon, on la verifie, et on envoie si c'est valide
		*/
		String rcvMessage = readAMsg(br).trim();
		System.out.println ("("+rcvMessage+")");
		TypeMessage mes = filtreMsg(rcvMessage);
		if (mes != null) {
		    serveur.processMessage(p, mes);
		}else{
		    System.out.println("OJ null\n");
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public String readAMsg (BufferedReader br) {
	int nbrAst = 0;
	String res = "";
	try {
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

    private String getType(String msg){
	String s ="";
	int inarow = 0;
	for(int i = 0; i< msg.length(); i++) {
	    if(msg.charAt(i)=='*') {
		inarow++;
	    }else{
		inarow = 0;
	    }
	    if(msg.charAt(i)==' ') {
		return s;
	    }
	    if(inarow == 3) {
		s = s.substring(0,s.length()-2);
		return s;
	    }
	    s+=msg.charAt(i);
	}
	return null;
    }

    private int LEtoInt(String s){
	return (s.charAt(0)+s.charAt(1)*256);
    }
    
    public TypeMessage filtreMsg (String msg) {
	boolean team = false;
	String type = getType(msg);
	int typemsg = -1;
	int space = (msg.charAt(type.length())==' ') ? 1 : 0;
	TypeMessage res;
	String [] args = new String[10];
	if(type == null) {
	    System.out.println("0");
	    return null;
	}
	String reste;
	int x = 0;
	reste = msg.substring(type.length() + space, msg.length()-3);
	//Je traite tous les messages du genre : TYPE BLABLA***
	//Je les sépare des msg TYPE***
	if(space==1) {
	    int len = reste.length();
	    if (type.equals("UP")) {
		if (len == 3) {
		    try{
			x = Integer.parseInt(reste);
			args[0] = ""+x;
		    }catch(NumberFormatException e) {
			return null;
		    }
		} else {
		    return null;
		}
		typemsg = 0;
	    } else if (type.equals("DOWN")) {

		if (len == 3) {
		    try{
			x = Integer.parseInt(reste);
			args[0] = ""+x;
		    }catch(NumberFormatException e) {
			return null;
		    }
		} else {
		    return null;
		}
		typemsg = 1;
	    } else if (type.equals("RIGHT")) {

		if (len == 3) {
		    try{
			x = Integer.parseInt(reste);
			args[0] = ""+x;
		    }catch(NumberFormatException e) {
			return null;
		    }
		} else {
		    return null;
		}
		typemsg = 2;
	    } else if (type.equals("LEFT")) {
		if (len == 3) {
		    try{
			x = Integer.parseInt(reste);
			args[0] = ""+x;
		    }catch(NumberFormatException e) {
			return null;
		    }
		} else {
		    return null;
		}
		typemsg = 3;
	    } else if (type.equals("LIST?")) {
		if (len == 2) {
		    args[0] = ""+LEtoInt(reste);
		} else {
		    return null;
		}
		typemsg = 5;
	    } else if (type.equals("SIZE?")) {
		if (len == 2) {
		    args[0] = ""+LEtoInt(reste);
		} else {
		    return null;
		}
		typemsg = 4;
	    } else if (type.equals("ALL?")) {
		typemsg = 6;
		if(reste.length()>200) {
		    return null;
		}
		args[0] = reste;
	    } else if (type.equals("SEND?")) {
		typemsg = 7;
		//SEND? id message***
		String id = "";
		for(int i = 0; i<reste.length(); i++) {
		    if(reste.charAt(i)!=' ') {
			id+=reste.charAt(i);
		    }else{
			break;
		    }
		}
		if(!isAlphaNum(id)) {
		    return null;
		}
		//Pas da forme ID MSG
		if((reste.length() <= (id.length()+1)) || reste.charAt(id.length())!=' ') {
		    return null;
		}
		String msg2 = reste.substring(id.length()+1,reste.length());
		if(msg2.length()>200) {
		    return null;
		}
		args[0] = id;
		args[1] = msg2;
	    } else if (type.equals("NEW") || type.equals("NEWT")) {
		String id = "";
		for(int i = 0; i<reste.length(); i++) {
		    if(reste.charAt(i)!=' ') {
			id+=reste.charAt(i);
		    }else{
			break;
		    }
		}
		if(!isAlphaNum(id)) {
		    return null;
		}
		//Pas la forme ID PORT
		if((reste.length() != (id.length()+5)) || reste.charAt(id.length())!=' ') {
		    return null;
		}
		String msg2 = reste.substring(id.length()+1,reste.length());
		if(!isNum(msg2)) {
		    return null;
		}
		args[0] = id;
		args[1] = msg2;
		team = type.equals("NEWT");
		typemsg = (team) ? 17 : 8;
	    }else if (type.equals("REG") || type.equals("REGT")) {
		//REG id port m***
		String id = "";
		for(int i = 0; i<reste.length(); i++) {
		    if(reste.charAt(i)!=' ') {
			id+=reste.charAt(i);
		    }else{
			break;
		    }
		}
		if(!isAlphaNum(id) || (reste.length()!= (id.length()+8))) {
		    return null;
		}
		String port = reste.substring(id.length()+1,id.length()+5);
		if(!isNum(port)) {
		    return null;
		}
		args[0]=id;
		args[1]=port;
		args[2]=""+LEtoInt(reste.substring(reste.length()-2,reste.length()));
		team = type.equals("REGT");
		typemsg = (team) ? 18 : 9;
	    } else if (type.equals("SETSIZE?")) {
		typemsg = 21;
		if (reste.length() == 5) {
		    if (reste.charAt(2) != ' ') {
			return null;
		    }
		} else {
		    return null;
		}
		args[0] = ""+LEtoInt(reste.substring(0, 2));
		args[1] = ""+LEtoInt(reste.substring(3, 5));		
	    }
	}else{
	    if (type.equals("START")) {
		typemsg = 10;
	    } else if (type.equals("UNREG")) {
		typemsg = 11;
	    } else if (type.equals("GAMES?")) {
		typemsg = 12;
	    } else if (type.equals("QUIT")) {
		typemsg = 13;
	    } else if (type.equals("GLIST?")) {
		typemsg = 14;
	    } else if (type.equals("TLIST?")) {
		typemsg = 15;
	    } else if (type.equals("CHANGETEAM")) {
		typemsg = 16;
	    } else if (type.equals("MAP")) {
		typemsg = 19;
	    } else if (type.equals("POS?")) {
		typemsg = 20;
	    }
	}
	System.out.println ("Message : "+type);
	return determineTypeMessage(typemsg,args, team);
    }

    public TypeMessage determineTypeMessage (int type, String [] mots, boolean team) {
	//Dans cette fonction, on sait que tout est bien formate
	if (0 <= type  && type <= 3) {

	    try{
		return (new Direction (Integer.parseInt(mots[0]), type));
	    }catch(NumberFormatException e) {
		return null;
	    }
	} else if (4 <= type  && type <= 5) {
	    return (new SizeList (mots[0], type));
	} else if (type == 6) {
	    return (new All (mots[0]));
	} else if (type == 7) {
	    return (new Send (mots[0], mots[1]));
	} else if (type == 8 || type == 17) {
	    try{
		return (new New (mots[0], Integer.parseInt(mots[1]), team));
	    }catch(NumberFormatException e) {
		return null;
	    }
	} else if (type == 9 || type == 18) {
	    try{
		return (new Reg (mots[0], Integer.parseInt(mots[1]), mots[2], team));
	    }catch(NumberFormatException e) {
		return null;
	    }
	} else if ((10 <= type && type <= 16) ||  type == 19 || type == 20) {
	    return (new NoArgs (type));
	} else if (type == 21) {
	    try {
		return new SetSize (type, Integer.parseInt(mots[0]), Integer.parseInt(mots[1]));
	    } catch (Exception e) {
		return null;
	    }
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
	System.out.println ("ID alphaNum = " + s + " length = "+l);

	if (l < 1 || l > 8) {
	    return false;
	}

	s = s.toUpperCase();
	for (int i = 0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if (!('0' <= c && c <= '9' || 'A' <= c && c <= 'Z')) {
		System.out.println (c);
		return false;
	    }
	}
	return true;
    }
    public boolean isNum(String s){
	for(int i = 0; i<s.length(); i++) {
	    if(s.charAt(i)>'9' || s.charAt(i)<'0') {
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
	    pw.println (s);
	    pw.flush();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
