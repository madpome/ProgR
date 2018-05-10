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
			}else{
				System.out.println("null\n");
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
private int LEtoInt(String s){
	return (s.charAt(0)+s.charAt(1)*256);
}
public TypeMessage filtreMsg (String msg) {
	String type = getType(msg);
	int typemsg = -1;
	int space = (msg.charAt(type.length())==' ') ? 1 : 0;
	TypeMessage res;
	String [] args = new String[10];
	if(type == null) {
		return null;
	}
	String reste;
	int x = 0;
	if(space == 1) {
		reste = msg.substring(type.length()+1,msg.length()-3);
	}else{
		reste = msg.substring(type.length(), msg.length()-3);
	}
	//Je traite tous les messages du genre : TYPE BLABLA***
	//Je les sépare des msg TYPE***
	if(space==1) {
		int len = reste.length();
		if (type.equals("UP")) {
			System.out.println ("Message : "+type);
			if (len == 3) {
				x = Integer.parseInt(reste);
				args[0] = ""+x;
			} else {
				System.out.println("null");
				return null;
			}
			typemsg = 0;
		} else if (type.equals("DOWN")) {
			System.out.println ("Message : "+type);

			if (len == 3) {
				x = Integer.parseInt(reste);
				args[0] = ""+x;
			} else {
				System.out.println("null");
				return null;
			}
			typemsg = 1;
		} else if (type.equals("RIGHT")) {
			System.out.println ("Message : "+type);

			if (len == 3) {
				x = Integer.parseInt(reste);
				args[0] = ""+x;
			} else {
				System.out.println("null");
				return null;
			}
			typemsg = 2;
		} else if (type.equals("LEFT")) {
			System.out.println ("Message : "+type);
			if (len == 3) {
				x = Integer.parseInt(reste);
				args[0] = ""+x;
			} else {
				System.out.println("null");
				return null;
			}
			typemsg = 3;
		} else if (type.equals("LIST?")) {
			System.out.println ("Message : "+type);
			if (len == 2) {
				args[0] = ""+LEtoInt(reste);
			} else {
				return null;
			}
			typemsg = 5;

		} else if (type.equals("ALL?")) {
			typemsg = 6;
			if(reste.length()>200) {
				return null;
			}
			args[0] = reste;
		} else if (type.equals("SEND?")) {
			System.out.println ("Message : "+type);
			typemsg = 7;
			//SEND? id message***
			String id = "";
			for(int i = 0; i<type.length(); i++) {
				if(type.charAt(i)!=' ') {
					id+=type.charAt(i);
				}else{
					break;
				}
			}
			if(!isAlphaNum(id)) {
				return null;
			}
			//Pas da forme ID MSG
			if((type.length() <= (id.length()+1)) || type.charAt(id.length())!=' ') {
				return null;
			}
			String msg2 = type.substring(id.length()+1,type.length());
			if(msg2.length()>200) {
				return null;
			}
			args[0] = id;
			args[1] = msg2;
		} else if (type.equals("NEW")) {
			System.out.println ("Message : "+type);
			typemsg = 8;
			String id = "";
			for(int i = 0; i<type.length(); i++) {
				if(type.charAt(i)!=' ') {
					id+=type.charAt(i);
				}else{
					break;
				}
			}
			if(!isAlphaNum(id)) {
				return null;
			}
			//Pas la forme ID PORT
			if((type.length() != (id.length()+5)) || type.charAt(id.length())!=' ') {
				return null;
			}
			String msg2 = type.substring(id.length()+1,type.length());
			System.out.println("NEW "+id+" "+msg2);
			args[0] = id;
			args[1] = msg2;
		}else if (type.equals("REG")) {
			System.out.println ("Message : "+type);
			typemsg = 9;
			//REG id port m***
			String id = "";
			for(int i = 0; i<type.length(); i++) {
				if(type.charAt(i)!=' ') {
					id+=type.charAt(i);
				}else{
					break;
				}
			}
			if(!isAlphaNum(id) || (type.length()!= (id.length()+8))) {
				return null;
			}
			String port = type.substring(id.length()+1,id.length()+5);
			if(!isNum(port)) {
				return null;
			}
			args[0]=id;
			args[1]=port;
			args[2]=""+LEtoInt(type.substring(type.length()-2,type.length()));
		}
	}else{
		if (type.equals("START")) {
			System.out.println ("Message : "+type);
			typemsg = 10;
		} else if (type.equals("UNREG")) {
			System.out.println ("Message : "+type);
			typemsg = 11;
		} else if (type.equals("GAMES?")) {
			System.out.println ("Message : "+type);
			typemsg = 12;
		} else if (type.equals("QUIT")) {
			System.out.println ("Message : "+type);
			typemsg = 13;
		} else if (type.equals("GLIST?")) {
			System.out.println ("Message : "+type);
			typemsg = 14;
		}
	}
	if(typemsg == -1) {
		return null;
	}
	return determineTypeMessage(typemsg,args);
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
		PrintWriter pw = new PrintWriter (new OutputStreamWriter (sock.getOutputStream()));
		pw.println (s);
		pw.flush();
	} catch (Exception e) {
		e.printStackTrace();
	}
}

}
