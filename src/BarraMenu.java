import javax.swing.JMenu;

public class BarraMenu extends JFrame implements ActionListener, KeyListener{
	JBarraMenu barraMenu;
	JMenu useIt, seeItRun,viewTheCode,exit;
	JMenuItem sirOpen, sirSave, sirType, sirDir;
	JMenuItem vtcOpen, vtcSave,vtcType, vtcDir;
	JLabel lblCode = new JLabel("<html><hl align='cente'>Servidor FTP</hl></html>");
	
	public static void main(Strin[] args){
		BarraMenu fr = new BarraMenu();
		maximiseFrame(fr);
		fr.setVisible(true);
	}		
	
	public BarraMenu(){
		setLayout(new FlowLayout());
		setSize(400,300);
		setTitle("Servidor FTP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.addKeyListener(this);
		
		barraMenu=new JBarraMenu();
		
		useIt= new JMenu("Archivo");
		useIt.addMenuListener(new thisMenuListener());
		barraMenu.add(useIt);	
		
		exit=new JMenu("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		exit.addMenuListener(new thisMenuListener());
		barraMenu.add(exit);
		
		seeItRun=new JMenu("")
		}
}
