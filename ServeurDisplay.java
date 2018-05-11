import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class ServeurDisplay extends JPanel implements ActionListener,Runnable{
    
    private Serveur serveur;
    private int decalageGame;

    private JButton[][] buttons;
    private int[] decalagePlayer;
    private int displayedMaze;

    

    public ServeurDisplay(Serveur serveur){
	this.serveur = serveur;
	decalageGame = 0;
	decalagePlayer = new int[4];
	displayedMaze = -1;
	
	JFrame frame = new JFrame();
	frame.setTitle("Serveur port: "+serveur.getPort());
	frame.setResizable(false);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	JButton up = new JButton("up");
	up.setActionCommand("upG");
	up.addActionListener(this);
	up.setBounds(270,0,100,40);
	JButton down = new JButton("down");
	down.setActionCommand("downG");
	down.addActionListener(this);
	down.setBounds(270,680,100,40);

	buttons = new JButton[4][4];
	for (int i = 0; i< 4; i++){
	    buttons[i][0] = new JButton();
	    buttons[i][0].setActionCommand("up_"+i);
	    buttons[i][1] = new JButton();
	    buttons[i][1].setActionCommand("down_"+i);
	    buttons[i][2] = new JButton();
	    buttons[i][2].setActionCommand("display_"+i);
	    buttons[i][3] = new JButton();
	    buttons[i][3].setActionCommand("kill_"+i);
	}
	this.setLayout(null);
	this.setPreferredSize(new Dimension(1280,720));
	this.setFocusable(true);
	this.add(up);
	this.add(down);
	for (int i=0; i< 4; i++){
	    for (int j=0; j<4; j++){
		buttons[i][j].setBounds((i%2==0)?50+62*j:340+62*j,(i<2)?290:630,62,50);
		buttons[i][j].addActionListener(this);
		buttons[i][j].setVisible(false);
		this.add(buttons[i][j]);
	    }
	}

	frame.add(this);
	frame.pack();
	frame.setVisible(true);
    }
    public void run(){
	while (true){
	    try{
		Thread.sleep(33);
		repaint();
	    }catch (Exception e){}
	}
    }

    public void paintComponent(Graphics g){
	super.paintComponent(g);
	g.setFont(new Font("Arial",0,25));
	for (int i = 0; i< 4; i++){
	    if (i + 2*decalageGame < serveur.getNumberOfGame()){
		serveur.displayGame(g,i+2*decalageGame,(i%2==0)?50:340,(i<2)?40:380,decalagePlayer[i]);
		for (int j=0; j<4; j++){
		    buttons[i][j].setVisible(true);
		}
	    }else{
		for (int j=0; j<4; j++){
		    buttons[i][j].setVisible(false);
		}
	    }
	}
	serveur.displayMaze(g,640,0,displayedMaze);
    }

    public void actionPerformed(ActionEvent event){
	System.out.println(event.getActionCommand());
	String[] splitted = event.getActionCommand().split("_");
	int n = 0;
	if (splitted.length == 2)
	    n = Integer.parseInt(splitted[1]);
	switch(splitted[0]){
	case "upG":
	    decalageGame = (decalageGame>0)?decalageGame - 1 : 0;
	    reset(decalagePlayer);
	    break;
	case "downG":
	    decalageGame = ((decalageGame + 1)*2 < serveur.getNumberOfGame())? decalageGame + 1 : decalageGame;
	    reset(decalagePlayer);
	    break;
	case "up":
	    decalagePlayer[n] = (decalagePlayer[n]>0) ? decalagePlayer[n]-1 : 0;
	    break;
	case "down":
	    decalagePlayer[n] = (decalagePlayer[n]< serveur.getNumberOfPlayers(n+2*decalageGame)-1)? decalagePlayer[n] + 1 : decalagePlayer[n];
	    break;
	case "display":
	    displayedMaze = serveur.getGameID(n + 2*decalageGame);
	    break;
	case "kill":
	    break;
	    
	    
	}
    }
    public void reset(int[] t){
	for (int i=0; i <t.length; i++){
	    t[i] = 0;
	}
    }
}
