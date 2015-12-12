package br.com.vigilante;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.videoio.VideoCapture;

public class Main extends JFrame{
	private static final long serialVersionUID = 1L;
	static BufferedImage image;
	static JFrame imageFrame;
	
	public static final int IDLE_STATE = 1;
	public static final int LEARNING_STATE = 10;
	public static final int SERIOUS_STATE = 100;
	
	public static int ModusOperandi = IDLE_STATE;
	
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

	public static void main(String args[]) throws InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		HOGDescriptor hogDesc = new HOGDescriptor();
		hogDesc.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
		
		VideoCapture camera = new VideoCapture(0);
		//VideoCapture camera = new VideoCapture("HarlemShake.mp4");
		//VideoCapture camera = new VideoCapture("ExplosaoRestaurante.mp4");
		//VideoCapture camera = new VideoCapture("TrainCrash.mp4");
		//VideoCapture camera = new VideoCapture("rush_01.mov");
		//VideoCapture camera = new VideoCapture("3452204_031_c.mov");

		Mat frame = new Mat();
		camera.read(frame);

		if (!camera.isOpened()) {
			System.out.println("Error");
		} else {
			Main t = new Main();
			image = MatToBufferedImage(frame);
			ImagePanel imgPanel = new ImagePanel(image);
			imgPanel.setLocation(30, 30);
			imgPanel.setSize(image.getWidth(), image.getHeight());
			imgPanel.setVisible(true);
			
			JPanel alertPanel = new JPanel();
			alertPanel.setLocation(image.getWidth()/2 + 30, image.getHeight() + 30);
			alertPanel.setSize(60, 60);
			alertPanel.setVisible(true);
			alertPanel.setBackground(Color.BLACK);
			
			t.add(alertPanel);
			t.add(imgPanel);
			
			t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			t.setTitle("Webcam");
			t.setSize(image.getWidth() + 60, image.getHeight() + 180);
			t.setLocation(0, 0);
			t.setVisible(true);
			
			Mat lastFrame = new Mat();
			Imgproc.GaussianBlur(frame, frame, new Size(15, 15), 0, 0);
			frame.copyTo(lastFrame);
			double[] samples = new double[10];
			int i = 0;
			while (true) {
				if (camera.read(frame)) {
					Imgproc.GaussianBlur(frame, frame, new Size(15, 15), 0, 0);
					Mat imageFrame = new Mat();
					frame.copyTo(imageFrame);
					Core.absdiff(frame, lastFrame, imageFrame);
					/*
					MatOfRect found = new MatOfRect();
					MatOfDouble weight = new MatOfDouble();
					
					hogDesc.detectMultiScale(imageFrame, found, weight, 0, new Size(4, 4), new Size(32, 32), 1.05, 2, false);
					Rect[] rects = found.toArray();
					if (rects.length > 0) {
						for (int j = 0; j < rects.length; j++) {
							Imgproc.rectangle(imageFrame, new Point(rects[j].x, rects[j].y), 
									new Point(rects[j].x + rects[j].width, 
											rects[j].y + rects[j].height), new Scalar(0, 255, 0), 2);
						}
					}
					*/
					imgPanel.image = MatToBufferedImage(imageFrame);
					double blackPercentage = evaluateBlackPercent(imgPanel.image);
					
					samples[i] = blackPercentage;
					if(i == 9){
						double avg = average(samples);
						System.out.println("Percent of black: "+ String.valueOf(avg));
						if(avg < 0.3)
							alertPanel.setBackground(Color.RED);
						else
							alertPanel.setBackground(Color.GREEN);
						alertPanel.repaint();
					}
					imgPanel.repaint();
					frame.copyTo(lastFrame);
					
					i = (++i) % 10;
				}
			}
		}
		camera.release();
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, this);
	}

	public Main() {
	}

	public Main(BufferedImage img) {
		image = img;
	}

	// Show image on window
	public void window(JFrame frame0, Main t, BufferedImage img, String text, int x, int y) {
		frame0.getContentPane().add(new Main(img));
		frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame0.setTitle(text);
		frame0.setSize(img.getWidth(), img.getHeight() + 30);
		frame0.setLocation(x, y);
		frame0.setVisible(true);
	}

	// Load an image
	public BufferedImage loadImage(String file) {
		BufferedImage img;

		try {
			File input = new File(file);
			img = ImageIO.read(input);

			return img;
		} catch (Exception e) {
			System.out.println("erro");
		}

		return null;
	}

	// Save an image
	public void saveImage(BufferedImage img) {
		try {
			File outputfile = new File("Images/new.png");
			ImageIO.write(img, "png", outputfile);
		} catch (Exception e) {
			System.out.println("error");
		}
	}

	// Grayscale filter
	public BufferedImage grayscale(BufferedImage img) {
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				Color c = new Color(img.getRGB(j, i));

				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);

				Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);

				img.setRGB(j, i, newColor.getRGB());
			}
		}

		return img;
	}

	public static BufferedImage MatToBufferedImage(Mat frame) {
		// Mat() to BufferedImage
		int type = 0;
		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);

		return image;
	}
	
	public static double average(double[] samples){
		double d = 0.0;
		for(int i = 0; i < samples.length; i++){
			d += samples[i];
		}
		return d/samples.length;
	}
	
	public static double evaluateBlackPercent(BufferedImage imageFrame){
		double percent = 0.0;
		
		double black = 0;
		double total = 0;
		for(int i = 0; i < imageFrame.getWidth(); i += 2){
			for(int j = 0; j < imageFrame.getHeight(); j += 2){
				int rgbColor = imageFrame.getRGB(i, j);
				float[] hsb = new float[3];
				Color.RGBtoHSB(rgbColor & 0xFF0000, rgbColor & 0x00FF00, rgbColor & 0x0000FF, hsb);
				if(hsb[2] < 0.2)
					black++;
				total++;
			}
		}
		
		percent = black/total;
		
		return percent;
	}
}
