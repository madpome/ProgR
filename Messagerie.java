public class Messagerie {
private LinkedList<Player> playerList;
private int multiPort;
private String ipAddress;
private DatagramSocket dso;
public Messagerie(LinkedList<Player> playerList, int multiPort, String multiAd){
	this.playerList = playerList;
	this.multiPort = multiPort;
	dso = new DatagramSocket();
}
public boolean sendMessageTo(String msg, Player playerFrom, Player playerTo){
	try{
		String msg2 = "MESP "+playerFrom.getId()+" "+msg+"+++";
		byte[] data = msg2.getBytes();
		DatagramPacket paq = new DatagramPacket(data, data.length,
		                                        InetAddress.getByName(playerTo.getIp(), playerTo.getPort()));
		dso.send(paq);
		return true;
	}catch(Exception e) {
		e.printStackTrace();
		return false;
	}
}
public boolean sendMessageFant(int x, int y){
	try{
		String msg2 = "FANT "+x+" "+y+"+++";
		byte[] data = msg2.getBytes();
		DatagramPacket paq = new DatagramPacket(data, data.length,
		                                        InetAddress.getByName(playerTo.getIp(), playerTo.getPort()));
		dso.send(paq);
		return true;
	}catch(Exception e) {
		e.printStackTrace();
		return false;
	}
}
public boolean sendMessageScore(Player playerScored, int score, int x,int y){
	try{
		String msg2 = "SCOR "+playerFrom.getId()+" "+score+" "+x+" "+y+"+++";
		byte[] data = msg2.getBytes();
		DatagramPacket paq = new DatagramPacket(data, data.length,
		                                        InetAddress.getByName(ipAddress, multiPort));
		dso.send(paq);
		return true;
	}catch(Exception e) {
		e.printStackTrace();
		return false;
	}
}
public boolean sendMessageEnd(Player gagnant, int score){
	try{
		String msg2 = "END "+gagnant.getId()+" "+score+"+++";
		byte[] data = msg2.getBytes();
		DatagramPacket paq = new DatagramPacket(data, data.length,
		                                        InetAddress.getByName(ipAddress, multiPort));
		dso.send(paq);
		return true;
	}catch(Exception e) {
		e.printStackTrace();
		return false;
	}
}
public boolean sendMessageAll(String msg, Player playerFrom){
	try{
		String msg2 = "MESA "+playerFrom.getId()+" "+msg+"+++";
		byte[] data = msg2.getBytes();
		DatagramPacket paq = new DatagramPacket(data, data.length,
		                                        InetAddress.getByName(ipAddress, multiPort));
		dso.send(paq);
		return true;
	}catch(Exception e) {
		e.printStackTrace();
		return false;
	}

}
}
