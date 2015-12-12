package br.com.vigilante;

import java.awt.Color;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import br.com.arduino.*;

public class Main extends JFrame{
	private static final long serialVersionUID = 1L;

	private static JFrame window;
	private static ImagePanel imgPanel;
	private static JPanel alertPanel;
	
	private static VideoAnalyzer videoAnalyzer;
	
	public static class ImagePanel extends JPanel{
		private static final long serialVersionUID = 1L;
		public BufferedImage image;
		
		public ImagePanel() { }
		public ImagePanel(BufferedImage img){
			image = img;
		}
		
		@Override
		public void paint(Graphics g) {
			g.drawImage(image, 30, 30, this);
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
			imgPanel.image = videoAnalyzer.operate();
			System.out.println("Situation: " + videoAnalyzer.getLastSituation().getOptionName());
			imgPanel.repaint();
		}
		
	}	
	
	public static void openScreen(){
		window = new JFrame("Vigilante");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		imgPanel = new ImagePanel();
		imgPanel.setSize(800, 600);
		imgPanel.setLocation(50, 100);
		
		alertPanel = new JPanel();
		alertPanel.setLocation(900, 200);
		alertPanel.setSize(60, 60);
		alertPanel.setVisible(true);
		alertPanel.setBackground(Color.BLACK);
		
		JMenuBar menuBar = new JMenuBar();

		JMenu modusOperandi = new JMenu("Modus Operandi");
		menuBar.add(modusOperandi);
		
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
		
		window.setJMenuBar(menuBar);
		
		window.add(alertPanel);
		window.add(imgPanel);
		
		window.setSize(1000, 1000);
		window.setVisible(true);
		window.setLocation(0, 0);
	}
	
	public static void changeModusOperandi(VideoAnalyzer.ModusOperandi mode){
		videoAnalyzer.setMode(mode);
	}
}

