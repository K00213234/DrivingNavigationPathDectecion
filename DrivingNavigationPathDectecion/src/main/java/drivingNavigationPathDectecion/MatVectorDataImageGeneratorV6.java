package drivingNavigationPathDectecion;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MatVectorDataImageGeneratorV6 implements VectorDataImageGenerator
{

	private BufferedImage img;
	private JPanel displayJPanel;
	private List<Point> pointArray1 = new ArrayList<Point>();
	private List<Point> pointArray2 = new ArrayList<Point>();

	public MatVectorDataImageGeneratorV6(JPanel displayJPanel)
	{
		this.displayJPanel = displayJPanel;

	}

	public MatVectorDataImageGeneratorV6(JPanel displayJPanel, Mat mat)
	{
		this.displayJPanel = displayJPanel;
		getSpace(mat);
	}

	//
	// public methods
	//
	public BufferedImage getImage(Mat mat)
	{
		mat = dectectLinesInImageData(mat);

		getSpace(mat);

		WritableRaster raster = img.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		mat.get(0, 0, data);
		return img;
	}
	//
	// private methods
	//

	private void reset()
	{
		this.pointArray1.clear();
		this.pointArray2.clear();
	}

	private Mat dectectLinesInImageData(Mat mat)
	{
		Point corner = new Point(0, 0);
		Point corner1 = new Point(480, 640);

		Mat dst1 = new Mat();

		double angle1, angle2, avgangle;
		// Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);
		// Imgproc.blur(mat, mat, new Size(1, 1));

		// Imgproc.GaussianBlur(mat, mat, new Size(3, 3),1);

		// Imgproc.Canny(mat, dst1, 150,300, 3, true);
		Imgproc.GaussianBlur(mat, mat, new Size(1, 1), 1);
		Imgproc.Canny(mat, dst1, 150, 300, 3, true);

		// dst1.copyTo(mat);
		// Copy edges to the images that will display the results in BGR

		// Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
		// Standard Hough Line Transform
		Mat lines = new Mat(); // will hold the results of the detection
		// Imgproc.blur(dst, dst, new Size(3, 3));
		// Imgproc.HoughLines(mat, lines, 1, Math.PI/180, 125 ); // runs the actual
		// detection

		Imgproc.HoughLines(dst1, lines, 1, Math.PI / 720, 125); // runs the actual detection

		// Point s1= new Point(640 ,240 );
		// Point s2= new Point(0, 240);
		// Imgproc.line(mat,s1,s2, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);
		// Point s3= new Point(320 ,1000);
		// Point s4= new Point( 320, 000);
		// Imgproc.line(mat,s3,s4, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);

		// Draw the lines
		for (int x = 0; x < lines.rows(); x++)
		{
			angle1 = 0;
			angle2 = 0;
			avgangle = 0;
			double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a * rho, y0 = b * rho;
			Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
			Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));

			angle1 = calcAngleFromDriverBumber(pt1, pt2);

			if ((angle1 < 100 && angle1 > 80) || (angle1 < 280 && angle1 > 260))
			{

				// MyCamera.LogService.log("too close to 90 p1");

			}
			else
			{
				Imgproc.line(mat, pt1, pt2, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
			}

			pointArray1.add(pt1);
			pointArray1.add(pt2);

//			}
		}

		for (int x = 1; x < lines.rows(); x++)
		{
			angle1 = 0;
			angle2 = 0;
			avgangle = 0; // MyCamera.LogService.log("x"+ x+ "lines"+lines.rows());
			double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a * rho, y0 = b * rho;
			Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
			Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
			CameraUserDisplay.LogService.log("point set 2" + pt1 + " " + pt2);

			angle2 = calcAngleFromDriverBumber(pt1, pt2);

			if ((angle2 < 100 && angle2 > 80) || (angle2 < 280 && angle2 > 260))
			{
				// MyCamera.LogService.log("too close to 90 p2");

			}
			else
			{
				Imgproc.line(mat, pt1, pt2, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
				//

				// MyCamera.LogService.log(pt1 +" "+ pt2 +"\n");
			}

			pointArray2.add(pt1);
			pointArray2.add(pt2);

		}
//		}
		for (int x = 1; x < pointArray2.size(); x += 2)
		{
			StraightLine line1 = new StraightLine();
			StraightLine line2 = new StraightLine();

			line1.pointA = pointArray1.get(x - 1);
			line1.pointB = pointArray1.get(x);
			line2.pointA = pointArray2.get(x - 1);
			line2.pointB = pointArray2.get(x);
			Point pointonline = new Point();

			// line2.pointB);

			if (StraightLine.linesIntersect(line1, line2))
			{
				Point intersect = StraightLine.getPointOfLinesIntersection(line1, line2);

				avgangle = 0;
				angle1 = 0;
				angle2 = 0;
				// MyCamera.LogService.log("intersect inside image y" + intersect.y + " x " +
				// intersect.x + "c1 y " + corner1.y
				// + "c1 x " + corner1.x + "c y " + corner.y + "c x " + corner.x);

				if ((intersect.y > corner.x) && (intersect.x < corner1.y) && (intersect.y > corner.x)
						&& (intersect.y < corner1.x))
				{
					// MyCamera.LogService.log("intersect inside image y" + intersect.y + " x " +
					// intersect.x);
					Imgproc.circle(mat, intersect, 5, new Scalar(0, 0, 255));

				}
				else
				{
					angle1 = calcAngleFromDriverBumber(line1.pointA, line1.pointB);
					CameraUserDisplay.LogService.log("angle1" + angle1);
					angle2 = calcAngleFromDriverBumber(line2.pointA, line2.pointB);
					CameraUserDisplay.LogService.log("angle2" + angle2);

					avgangle = (angle1 + angle2) / 2;

					if (((angle2 < 100 && angle2 > 80) || (angle2 < 280 && angle2 > 260)))
					{
						if (pointArray2.size() > x + 1)
						{
							line2.pointA = pointArray2.get(x);
							line2.pointB = pointArray2.get(x + 1);
							angle2 = calcAngleFromDriverBumber(line2.pointA, line2.pointB);
							avgangle = (angle1 + angle2) / 2;
							// MyCamera.LogService.log("point2 replaced");
//							if (StraightLine.linesIntersect(line1, line2))
//								intersect = StraightLine.getPointOfLinesIntersection(line1, line2);
//							else intersect = new Point(0, 0);

						}
					}
					else if ((angle1 < 100 && angle1 > 80) || (angle1 < 280 && angle1 > 260))
					{
						if (pointArray1.size() > x + 1)
						{
							line1.pointA = pointArray1.get(x);
							line1.pointB = pointArray1.get(x + 1);
							angle1 = calcAngleFromDriverBumber(line1.pointA, line1.pointB);
							// avgangle = (angle1 + angle2) / 2;
							avgangle = 0;
							CameraUserDisplay.LogService.log(" point replaced");
//
//							if (StraightLine.linesIntersect(line1, line2))
//								intersect = StraightLine.getPointOfLinesIntersection(line1, line2);

						}
						// MyCamera.LogService.log("too close to 90 on intersect angle1" + angle1 + "
						// angle2"
						// + angle2);

					}
					else
					{

//						MyCamera.LogService.log("point = avgangle");
//					MyCamera.LogService.log("point = avgangle intersect= " + intersect + " avgangle=" + avgangle);

						pointonline = (getPointAtAngle(intersect, avgangle)).clone();

						// MyCamera.LogService.log("\n pointonlinr "+pointonline);

						if ((avgangle < 60) || avgangle < 300)
						{
							System.out.println(avgangle);

							if (avgangle < 90)
							{

								System.out.println("\n\n going right \n\n");

							}
							else
							{
								System.out.println("\n\n going left \n\n");

							}
						}

						Point poline180 = new Point();

						// Point poline = pointatangle(intersect , avgangle).clone();
						// Point poline90 = pointatangle(intersect , (avgangle+90));

						if (avgangle > 180)
						{
							CameraUserDisplay.LogService.log(
									"point = avgangle - 180 intersect= " + intersect + " avgangle=" + (avgangle - 180)
							);
							poline180 = getPointAtAngle(intersect, (avgangle - 180)).clone();
						}
						else
						{
							CameraUserDisplay.LogService.log(
									"point = avgangle + 180 intersect= " + intersect + " avgangle=" + (avgangle + 180)
							);

							poline180 = getPointAtAngle(intersect, (avgangle + 180)).clone();
						} // Point poline270 = pointatangle(intersect , (avgangle+270));

						// Imgproc.line(mat, poline90,poline180, new Scalar(0, 255,255), 3,
						// Imgproc.LINE_AA, 1);
						// Imgproc.line(mat , poline270,poline, new Scalar(0, 255,255), 3,
						// Imgproc.LINE_AA, 1);
						// MyCamera.LogService.log("\n angle1 "+angle1+" angle2 "+ angle2 +" avgangle "
						// +
						// avgangle +"\n intersect point = "+ intersect +" new point "+ pointonline +"
						// \n ");
						// MyCamera.LogService.log("pre processed "+ p1 +" p1 "+ p2 + " p2 "+p12 +" p12
						// "+ p22
						// +" p22" + "\n");
						Imgproc.line(mat, poline180, pointonline, new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);

						// MyCamera.LogService.log("line sucess");
					}
				}
			}
		}
		// }
		CameraUserDisplay.LogService.log("\n \n \n point2 size" + pointArray2.size() + "\n\n\n");
		if (pointArray2.size() > 20)
		{

			reset();
			// mat.empty();

			displayJPanel.removeAll();
			final Mat blankMat = new Mat();
			return blankMat;
		}
		return mat;
	}

	protected Mat filter(Mat frames)
	{
		Mat frame = frames;
		Mat frameHSV = new Mat();
		Mat black = new Mat();
		// Imgproc.blur(frames, frames, new Size(3, 3));

		Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);

		Core.inRange(frame, new Scalar(255, 255, 255), new Scalar(255, 255, 255), black);

		// Core.inRange(frameHSV, new Scalar(00,00, 00),
		// new Scalar(200,200, 200), thresh);
		// newImageData();
		Mat mask = new Mat(new Size(frames.cols(), frames.rows()), CvType.CV_8UC1);
		mask.setTo(new Scalar(0.0));
		Core.inRange(frame, new Scalar(10, 10, 10), new Scalar(100, 100, 100), mask);
		// Core.subtract(frame,m,mask );
		return mask;

	}

	private double calcAngleFromDriverBumber(Point pt2, Point pt1)
	{
		//Car line is 0x axis
		
		double theta = Math.atan2(pt1.y - pt2.y, pt1.x - pt2.x);

		theta += Math.PI / 2.0;
		double angle = Math.toDegrees(theta);
		if (angle < 0)
		{
			angle += 360;
		}
		if (angle > 360)
		{
			angle = angle % 360;

		}
		return angle;
	}

	Point getPointAtAngle(Point position, double angle)
	{
//Slope 45
		Point pointIntheDistance = GeometryMethods.pointIntheDistance(position);

		double lineDistance = GeometryMethods.pythagoreanDistance(position, pointIntheDistance);

		CameraUserDisplay.LogService
				.log("pos original " + position, CameraUserDisplay.LogService.DebuggingAtAngleToken);
		CameraUserDisplay.LogService
				.log("pos future " + pointIntheDistance, CameraUserDisplay.LogService.DebuggingAtAngleToken);
		CameraUserDisplay.LogService.log("lendub " + lineDistance, CameraUserDisplay.LogService.DebuggingAtAngleToken);

		double angleRadians = (angle * Math.PI / 180);

		// Get SOH 
		double opposite = (double) Math.sin(angleRadians) * lineDistance;
		// Get CAH
		double adjacent = (double) Math.cos(angleRadians) * lineDistance;
		// Add to old Vector
		CameraUserDisplay.LogService.log("ad " + adjacent + "op" + opposite );
		Point point = new Point(adjacent, opposite);
		CameraUserDisplay.LogService.log("points ad op " + point);
		double x = position.x + point.x;
		double y = position.y + point.y;

		Point location = position.clone();
		location.x = x;
		location.y = y;
		CameraUserDisplay.LogService.log("points loc " + location);
		CameraUserDisplay.LogService.log("points loc location.x" + location.x);
		CameraUserDisplay.LogService.log("points loc location.y" + location.y);

		CameraUserDisplay.LogService.log("lenght " + x + "" + y);
		CameraUserDisplay.LogService.log("points loc " + location.toString());
		return location;
	}
	private void getSpace(Mat mat)
	{
		int type = getImageType(mat);

		try
		{
			img = new BufferedImage(mat.cols(), mat.rows(), type);
		} catch (Exception e) // sometimes Mat is null
		{
			CameraUserDisplay.LogService.log(e.getMessage());

		}
	}

	private int getImageType(Mat mat)
	{
		if (mat.channels() == 1) return BufferedImage.TYPE_BYTE_GRAY;
		else if (mat.channels() == 3) return BufferedImage.TYPE_3BYTE_BGR;
		else return BufferedImage.TYPE_CUSTOM;// default type
	}

}