package br.com.vigilante;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

public class Main extends JFrame{
	private static final long serialVersionUID = 1L;

	private static JFrame window;
	private static ImagePanel imgPanel;
	private static ImagePanel origImgPanel;
	
	private static VideoAnalyzer videoAnalyzer;

	private static JPanel situationPanel;
	
	public static class ImagePanel extends JPanel{
		private static final long serialVersionUID = 1L;
		public BufferedImage image;
		
		public ImagePanel() { }
		public ImagePanel(BufferedImage img){
			image = img;
		}
		
		@Override
		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, this);
		}
		
		public static BufferedImage toBufferedImage(Image img)
		{
		    if (img instanceof BufferedImage)
		    {
		        return (BufferedImage) img;
		    }

		    // Create a buffered image with transparency
		    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		    // Draw the image on to the buffered image
		    Graphics2D bGr = bimage.createGraphics();
		    bGr.drawImage(img, 0, 0, null);
		    bGr.dispose();

		    // Return the buffered image
		    return bimage;
		}
	}

	public static void main(String args[]) {
		System.out.println("Starting Vigilante system");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		VideoCapture vc = new VideoCapture(0); //WEBCAM
		//VideoCapture vc = new VideoCapture("HarlemShake.mp4");
		//VideoCapture vc = new VideoCapture("ExplosaoRestaurante.mp4");
		//VideoCapture vc = new VideoCapture("TrainCrash.mp4");
		//VideoCapture vc = new VideoCapture("rush_01.mov");
		//VideoCapture vc = new VideoCapture("3452204_031_c.mov");
		
		videoAnalyzer = new VideoAnalyzer(vc);
		
		openScreen();
		
		while(true){
			ArrayList<BufferedImage> images = videoAnalyzer.operate();
			imgPanel.image = images.get(0);
			origImgPanel.image = images.get(1);
			//System.out.println("Situation: " + videoAnalyzer.getLastSituation().getOptionName());
			
			switch(videoAnalyzer.getLastSituation()){
			case NORMAL: 
				situationPanel.setBackground(Color.GREEN);
				break;
			case IDLE: 
				situationPanel.setBackground(Color.LIGHT_GRAY);
				break;
			case LEARNING: 
				situationPanel.setBackground(Color.WHITE);
				break;
			case CRITIC: 
				situationPanel.setBackground(Color.RED);
				break;
			}
			
			imgPanel.repaint();
			origImgPanel.repaint();
		}
		
	}	
	
	public static void openScreen(){
		window = new JFrame("Vigilante");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		imgPanel = new ImagePanel();
		imgPanel.setSize(400, 300);
		imgPanel.setLocation(50, 100);
		
		origImgPanel = new ImagePanel();
		origImgPanel.setSize(400, 300);
		origImgPanel.setLocation(450, 100);
		
		JMenuBar menuBar = new JMenuBar();

		JMenu modusOperandi = new JMenu("Modus Operandi");
		menuBar.add(modusOperandi);
		
		JMenu senseMenu = new JMenu("Sensibilidade");
		menuBar.add(senseMenu);
		
		JMenuItem idleMenuItem = new JMenuItem("Idle");
		idleMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				changeModusOperandi(VideoAnalyzer.ModusOperandi.IDLE);
			}
		});
		JMenuItem learningMenuItem = new JMenuItem("Learn");
		learningMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				changeModusOperandi(VideoAnalyzer.ModusOperandi.LEARN);
			}
		});
		JMenuItem realMenuItem = new JMenuItem("Real");
		realMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				changeModusOperandi(VideoAnalyzer.ModusOperandi.REAL);
			}
		});
		
		modusOperandi.add(idleMenuItem);
		modusOperandi.add(learningMenuItem);
		modusOperandi.add(realMenuItem);
		
		ButtonGroup sensibilityGroup = new ButtonGroup();
		final JRadioButtonMenuItem senseButton10 = new JRadioButtonMenuItem("10%");
		senseButton10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				senseButton10.setSelected(true);
				videoAnalyzer.setSensibility(0.1);
			}
		});
		final JRadioButtonMenuItem senseButton20 = new JRadioButtonMenuItem("20%");
		senseButton20.setSelected(true);
		senseButton20.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				senseButton20.setSelected(true);
				videoAnalyzer.setSensibility(0.2);
			}
		});
		final JRadioButtonMenuItem senseButton40 = new JRadioButtonMenuItem("40%");
		senseButton40.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				senseButton40.setSelected(true);
				videoAnalyzer.setSensibility(0.4);
			}
		});
		final JRadioButtonMenuItem senseButton50 = new JRadioButtonMenuItem("50%");
		senseButton50.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				senseButton50.setSelected(true);
				videoAnalyzer.setSensibility(0.5);
			}
		});
		final JRadioButtonMenuItem senseButton75 = new JRadioButtonMenuItem("75%");
		senseButton75.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				senseButton75.setSelected(true);
				videoAnalyzer.setSensibility(0.75);
			}
		});
		
		sensibilityGroup.add(senseButton10);
		sensibilityGroup.add(senseButton20);
		sensibilityGroup.add(senseButton40);
		sensibilityGroup.add(senseButton50);
		sensibilityGroup.add(senseButton75);
		
		senseMenu.add(senseButton10);
		senseMenu.add(senseButton20);
		senseMenu.add(senseButton40);
		senseMenu.add(senseButton50);
		senseMenu.add(senseButton75);
		
		window.setJMenuBar(menuBar);
		
		JPanel logoContainerPanel = new JPanel();
		logoContainerPanel.setPreferredSize(new Dimension(1000, 100));
		logoContainerPanel.setAlignmentX(CENTER_ALIGNMENT);
		logoContainerPanel.setAlignmentY(CENTER_ALIGNMENT);
		logoContainerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		ImagePanel logoPanel = new ImagePanel();
		logoPanel.image = ImagePanel.toBufferedImage(new ImageIcon("vigilante.png").getImage());
		logoPanel.setPreferredSize(new Dimension(200, 100));
		logoPanel.setAlignmentX(CENTER_ALIGNMENT);
		logoPanel.setAlignmentY(CENTER_ALIGNMENT);
		logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.PAGE_AXIS));
		
		logoContainerPanel.setLayout(new BorderLayout());
		logoContainerPanel.add(logoPanel, BorderLayout.CENTER);
		
		JPanel videoPanel = new JPanel();
		videoPanel.setLayout(new BoxLayout(videoPanel, BoxLayout.LINE_AXIS));
		videoPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		videoPanel.setPreferredSize(new Dimension(900, 400));
		videoPanel.setAlignmentY(CENTER_ALIGNMENT);
		videoPanel.setAlignmentX(CENTER_ALIGNMENT);
		
		videoPanel.add(imgPanel);
		videoPanel.add(origImgPanel);
		
		situationPanel = new JPanel();
		situationPanel.setPreferredSize(new Dimension(200, 50));
		situationPanel.setAlignmentX(LEFT_ALIGNMENT);
		situationPanel.setLayout(new BoxLayout(situationPanel, BoxLayout.PAGE_AXIS));
		situationPanel.setBackground(Color.RED);
		
		window.getContentPane().add(logoContainerPanel, BorderLayout.PAGE_START);
		window.getContentPane().add(videoPanel, BorderLayout.CENTER);
		window.getContentPane().add(situationPanel, BorderLayout.PAGE_END);
		
		window.setSize(830, 530);
		window.setVisible(true);
		window.setLocation(0, 0);
	}
	
	public static void changeModusOperandi(VideoAnalyzer.ModusOperandi mode){
		videoAnalyzer.setMode(mode);
	}
}

