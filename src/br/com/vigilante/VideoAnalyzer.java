package br.com.vigilante;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Date;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import br.com.arduino.Arduino;

public class VideoAnalyzer {
	
	private final int MAX_SAMPLE_SIZE = 40;
	private final double FRAMES_PER_SECOND = 10;
	private final double FRAME_DT = 1000 / FRAMES_PER_SECOND;
	
	private VideoCapture camera;
	private ModusOperandi mode;
	
	private Mat lastFrame;
	private Mat currentFrame;
	private double[] samples;
	private int sampleCounter;
	private ArrayList<Double> learnedSamples;
	private double normalPattern;
	private AnalysisSituation lastSituation;
	private Arduino ard;
	private Integer monitoring;
	private double lastTime;
	
	public enum ModusOperandi{
		IDLE, LEARN, REAL;
	}

	public VideoAnalyzer(VideoCapture vc){
		camera = vc;
		mode = ModusOperandi.IDLE;
		lastFrame = new Mat();
		currentFrame = new Mat();
		samples = new double[10];
		learnedSamples = new ArrayList<Double>();
		sampleCounter = 0;
		normalPattern = -1;
		ard = new Arduino();
		lastTime = 0;
		setLastSituation(AnalysisSituation.IDLE);
		monitoring = 0;
	}

	public VideoCapture getCamera() {
		return camera;
	}

	public void setCamera(VideoCapture camera) {
		this.camera = camera;
	}

	public ModusOperandi getMode() {
		return mode;
	}

	public boolean setMode(ModusOperandi mode) {
		if(mode != this.mode){
			if(mode == ModusOperandi.REAL && normalPattern < 0){
				return false;
			}
			this.mode = mode;
		}
		
		return true;
	}
	
	public Mat getLastFrame() {
		return lastFrame;
	}

	public void setLastFrame(Mat lastFrame) {
		this.lastFrame = lastFrame;
	}

	public Mat getCurrentFrame() {
		return currentFrame;
	}

	public void setCurrentFrame(Mat currentFrame) {
		this.currentFrame = currentFrame;
	}

	public double[] getSamples() {
		return samples;
	}

	public void setSamples(double[] samples) {
		this.samples = samples;
	}

	public int getSampleCounter() {
		return sampleCounter;
	}

	public void setSampleCounter(int sampleCounter) {
		this.sampleCounter = sampleCounter;
	}

	public ArrayList<Double> getLearnedSamples() {
		return learnedSamples;
	}

	public void setLearnedSamples(ArrayList<Double> learnedSamples) {
		this.learnedSamples = learnedSamples;
	}

	public double getNormalPattern() {
		return normalPattern;
	}

	public void setNormalPattern(double normalPattern) {
		this.normalPattern = normalPattern;
	}

	public AnalysisSituation getLastSituation() {
		return lastSituation;
	}

	public void setLastSituation(AnalysisSituation lastSituation) {
		this.lastSituation = lastSituation;
	}

	public BufferedImage operate(){
		double currTime = (new Date()).getTime();
		double timeDiff = currTime - lastTime;
		if (timeDiff > FRAME_DT) {
			lastTime = currTime;
			if(camera.read(currentFrame)){
				if(lastFrame == null){
					lastFrame = new Mat();
					Imgproc.GaussianBlur(currentFrame, currentFrame, new Size(15, 15), 0, 0);
					currentFrame.copyTo(lastFrame);
				}
				switch(mode){
					case IDLE:
						return operateInIdle();
					case LEARN:
						return operateInLearning();
					case REAL:
						return operateInReal();
				}
			}
		}
		return MatToBufferedImage(lastFrame);
	}
	
	private BufferedImage operateInIdle(){
		ard.comunicacaoArduino('1');
		System.out.println("System in idle");
		camera.read(currentFrame);
		preProcessFrame();
		currentFrame.copyTo(lastFrame);
		return MatToBufferedImage(currentFrame);
	}
	
	private BufferedImage operateInLearning(){
		ard.comunicacaoArduino('1');
		preProcessFrame();
		Mat imageFrame = new Mat();
		currentFrame.copyTo(imageFrame);
		Core.absdiff(currentFrame, lastFrame, imageFrame);
		BufferedImage image = MatToBufferedImage(imageFrame);
		double blackPercentage = evaluateBlackPercent(image);
		
		learnedSamples.add(blackPercentage);
		
		if(learnedSamples.size() == MAX_SAMPLE_SIZE){
			normalPattern = evaluateNormalPattern();
			System.out.println("Evaluated normal pattern: " + String.valueOf(normalPattern));
		}

		this.setMode(ModusOperandi.REAL);
		currentFrame.copyTo(lastFrame);
		return image;
	}
	
	private BufferedImage operateInReal(){
		preProcessFrame();
		Mat imageFrame = new Mat();
		currentFrame.copyTo(imageFrame);
		Core.absdiff(currentFrame, lastFrame, imageFrame);
		BufferedImage image = MatToBufferedImage(imageFrame);
		double blackPercentage = evaluateBlackPercent(image);
		
		samples[sampleCounter] = blackPercentage;
		if(sampleCounter == 9){
			double avg = average(samples);
			System.out.println("Percent of black: "+ String.valueOf(avg));
			
			if(normalPattern - avg > normalPattern*0.2){
				ard.comunicacaoArduino('2');
				if (monitoring == 0) {
					try {
						Monitor.incluir();
					} catch (Exception e) {
						
					}
				}	
				monitoring = 1;
				setLastSituation(AnalysisSituation.CRITIC);
			} else {
				ard.comunicacaoArduino('0');
				setLastSituation(AnalysisSituation.NORMAL);
				monitoring = 0;
			}
			System.out.println("Situation: " + this.lastSituation.getOptionName());
		}
		currentFrame.copyTo(lastFrame);
		sampleCounter = (++sampleCounter) % 10;
		
		return image;
	}
	
	private void preProcessFrame() {
		Mat tmp = new Mat(currentFrame.width(), currentFrame.height(), currentFrame.type());
		Core.flip(currentFrame, tmp, 1);
		Imgproc.GaussianBlur(tmp, currentFrame, new Size(15, 15), 0, 0);
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
	
	public double evaluateNormalPattern(){
		double d = 0.0;
		for(int i = 0; i < learnedSamples.size(); i++){
			d += learnedSamples.get(i);
		}
		return d/learnedSamples.size();
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
