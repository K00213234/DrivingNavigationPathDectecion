package drivingNavigationPathDectecion;

import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class CameraVideoProcessManager
{

	private final int cameraDeviceNumber = 1;
	private VectorDataImageGenerator vectorDataImageGenerator;
	private VideoCapture videoCapture;

	CameraVideoProcessManager(JPanel displayJPanel)
	{
		vectorDataImageGenerator = new MatVectorDataImageGeneratorV6(displayJPanel);
		videoCapture = new VideoCapture();
		videoCapture.open(cameraDeviceNumber);
	}

	BufferedImage getOneFrame()
	{
		
		Mat imageData = new Mat();
		videoCapture.read(imageData);
		return vectorDataImageGenerator.getImage(imageData);
	}

}