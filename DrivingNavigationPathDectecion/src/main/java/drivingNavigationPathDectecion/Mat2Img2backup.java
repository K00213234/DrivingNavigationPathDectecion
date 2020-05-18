package drivingNavigationPathDectecion;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

public class Mat2Img2backup implements VectorDataImageGenerator
{

	int DELAY_CAPTION = 100;
	int DELAY_BLUR = 100;
	int MAX_KERNEL_LENGTH = 10;
	Mat dst = new Mat();
	Mat src = new Mat();

	String windowName = "Filter Demo 1";

	Mat mat = new Mat();
	BufferedImage img;
	JPanel displayJPanel;

	public Mat2Img2backup(JPanel displayJPanel)
	{
		this.displayJPanel = displayJPanel;

	}

	public Mat2Img2backup(JPanel displayJPanel, Mat mat)
	{
		this.displayJPanel = displayJPanel;
		getSpace(mat);
	}

	

	private Mat linedet(Mat mat)
	{

		Mat dst = new Mat(), cdst = new Mat(), cdstP;
		// List<Point> point1 = new ArrayList<Point>();
		/// List<Point> point2 = new ArrayList<Point>();
		double angle1, angle2, avgangle;
		Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);

		Imgproc.Canny(dst, dst, 50, 200, 3, false);
		// Copy edges to the images that will display the results in BGR
		// Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
		cdstP = cdst.clone();
		// Standard Hough Line Transform
		Mat lines = new Mat(); // will hold the results of the detection
		// Imgproc.blur(dst, dst, new Size(3, 3));

		Imgproc.HoughLines(dst, lines, 1.2, Math.PI / 180, 250); // runs the actual detection
		Point s1 = new Point(640, 240);
		Point s2 = new Point(0, 240);
		// Imgproc.line(mat,s1,s2, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);
		Point s3 = new Point(320, 1000);
		Point s4 = new Point(320, 000);
		// Imgproc.line(mat,s3,s4, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);

		// Draw the lines
		for (int x = 0; x < lines.rows(); x++)
		{

			double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a * rho, y0 = b * rho;
			Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
			Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));

			Imgproc.line(mat, pt1, pt2, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);

			// System.out.print("point set 1"+ x +" x "+pt1 +" "+ pt2 +"\n");
			point1.add(pt1);
			point1.add(pt2);
		}

		for (int x = 1; x < lines.rows(); x++)
		{
			// System.out.print("x"+ x+ "lines"+lines.rows());
			double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a * rho, y0 = b * rho;
			Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
			Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
			// System.out.print("point set 2"+pt1 +" "+ pt2 +"\n");
			Imgproc.line(mat, pt1, pt2, new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
			//

			System.out.print(pt1 + " " + pt2 + "\n");
			point2.add(pt1);
			point2.add(pt2);

		}
		for (int x = 1; x < lines.rows(); x++)
		{
			Point p1 = point1.get(x - 1);
			Point p12 = point1.get(x);
			Point p2 = point2.get(x - 1);
			Point p22 = point2.get(x);
			Point pointonline = new Point();
			Point intersect = new Point();
			intersect = lineLineIntersection(p1, p12, p2, p22);
			angle1 = calcAngle(p1, p12);
			angle2 = calcAngle(p2, p22);
			avgangle = (angle1 + angle2) / 2;
			System.out.print("\n pointonlinr " + pointonline);
			if (avgangle > 90 && avgangle < 270)
			{
				avgangle += 180;
			}

			pointonline = pointatangle(intersect, avgangle).clone();
			// System.out.print("\n angle1 "+angle1+" angle2 "+ angle2 +" avgangle " +
			// avgangle +"\n intersect point = "+ intersect +" new point "+ pointonline +"
			// \n ");
			// System.out.print("pre processed "+ p1 +" p1 "+ p2 + " p2 "+p12 +" p12 "+ p22
			// +" p22" + "\n");
			Imgproc.line(mat, intersect, pointonline, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);

			// System.out.print("line sucess");
		}
		// p1.x= (p1.x+p12.x)/2;
		// p1.y= (p1.y+p12.y)/2;
		// p2.x= (p2.x+p22.x)/2;
		// p2.y= (p2.y+p22.y)/2;
		// System.out.print("processed "+ p1 +" p1 "+ p2 + " p2 "+p12 +" p12 "+ p22 +"
		// p22" + "\n");

		// Point p3 =point1.get(x-1);
		// Point p4 =point1.get(x);
		// Point p32 =point2.get(x-1);
		// Point p42 =point2.get(x);
		// System.out.print("pre processed "+ p3 +" p3 "+ p4 + " p4 "+p32 +" p32 "+ p42
		// +" p42" + "\n");

		// p3.x= (p3.x+p32.x)/2;
		// p3.y= (p3.y+p32.y)/2;
		// p4.x= (p32.x+p42.x)/2;
		// p4.y= (p32.y+p42.y)/2;
		// System.out.print("processed "+ p3 +" p3 "+ p4 + " p4 "+p32 +" p32 "+ p42 +"
		// p42" + "\n");
		// Imgproc.line(mat, p3, p4, new Scalar(255,255 ,0), 1, Imgproc.LINE_AA, 0);
		// Imgproc.line(mat, p4, p3, new Scalar(255,0 ,0), 1, Imgproc.LINE_AA, 0);
//}

		// Probabilistic Line Transform
		// Mat linesP = new Mat(); // will hold the results of the detection
		// Imgproc.blur(dst, dst, new Size(3, 3));
		// Imgproc.Canny(dst, dst, 50,250, 3, false);
		// Imgproc.HoughLinesP(dst, linesP, 1.2, Math.PI/180, 200); // runs the actual
		// detection
		// Draw the lines
//      for (int x = 0; x < linesP.rows(); x++) {
//         double[] l = linesP.get(x, 0);
//         Imgproc.line(mat, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 255,0), 2, Imgproc.LINE_AA, 0);

		// }

		return mat;
	}

	public List<Point> point1 = new ArrayList<Point>();
	public List<Point> point2 = new ArrayList<Point>();
	public List<Point> reset = new ArrayList<Point>();

	public void resetter()
	{
		this.point1.clear();
		this.point2.clear();
	}

	Mat linedet1(Mat mat)
	{
		Point corner = new Point(0, 0);
		Point corner1 = new Point(480, 640);

		Mat dst1 = new Mat(), cdst = new Mat(), cdstP;

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
		cdstP = cdst.clone();
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

			angle1 = calcAngle(pt1, pt2);

			if ((angle1 < 100 && angle1 > 80) || (angle1 < 280 && angle1 > 260))
			{

				// System.out.println("too close to 90 p1");

			}
			else
			{
				Imgproc.line(mat, pt1, pt2, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
			}

			point1.add(pt1);
			point1.add(pt2);

//			}
		}

		for (int x = 1; x < lines.rows(); x++)
		{
			angle1 = 0;
			angle2 = 0;
			avgangle = 0; // System.out.print("x"+ x+ "lines"+lines.rows());
			double rho = lines.get(x, 0)[0], theta = lines.get(x, 0)[1];
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a * rho, y0 = b * rho;
			Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
			Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
			System.out.print("point set 2" + pt1 + " " + pt2 + "\n");

			angle2 = calcAngle(pt1, pt2);

			if ((angle2 < 100 && angle2 > 80) || (angle2 < 280 && angle2 > 260))
			{
				// System.out.println("too close to 90 p2");

			}
			else
			{
				Imgproc.line(mat, pt1, pt2, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);
				//

				// System.out.print(pt1 +" "+ pt2 +"\n");
			}

			point2.add(pt1);
			point2.add(pt2);

		}
//		}
		for (int x = 1; x < point2.size(); x += 2)
		{
			Point p1 = point1.get(x - 1);
			Point p12 = point1.get(x);
			Point p2 = point2.get(x - 1);
			Point p22 = point2.get(x);
			Point pointonline = new Point();
			Point intersect = new Point();
			intersect = lineLineIntersection(p1, p12, p2, p22);
			avgangle = 0;
			angle1 = 0;
			angle2 = 0;
			// System.out.println("intersect inside image y" + intersect.y + " x " +
			// intersect.x + "c1 y " + corner1.y
			// + "c1 x " + corner1.x + "c y " + corner.y + "c x " + corner.x);

			if ((intersect.y > corner.x) && (intersect.x < corner1.y) && (intersect.y > corner.x)
					&& (intersect.y < corner1.x))
			{
				// System.out.println("intersect inside image y" + intersect.y + " x " +
				// intersect.x);
				Imgproc.circle(mat, intersect, 5, new Scalar(0, 0, 255));

			}
			else
			{
				angle1 = calcAngle(p1, p12);
				System.out.println("angle1" + angle1);
				angle2 = calcAngle(p2, p22);
				System.out.println("angle2" + angle2);

				avgangle = (angle1 + angle2) / 2;

				if (((angle2 < 100 && angle2 > 80) || (angle2 < 280 && angle2 > 260)))
				{
					if (point2.size() > x + 1)
					{
						p2 = point2.get(x);
						p22 = point2.get(x + 1);
						angle2 = calcAngle(p2, p22);
						avgangle = (angle1 + angle2) / 2;
						// System.out.println("point2 replaced");
						intersect = lineLineIntersection(p1, p12, p2, p22);

					}
				}
				else if ((angle1 < 100 && angle1 > 80) || (angle1 < 280 && angle1 > 260))
				{
					if (point1.size() > x + 1)
					{
						p1 = point1.get(x);
						p12 = point1.get(x + 1);
						angle1 = calcAngle(p1, p12);
						avgangle = (angle1 + angle2) / 2;
						System.out.println(" point replaced");

						intersect = lineLineIntersection(p1, p12, p2, p22);

					}
					// System.out.println("too close to 90 on intersect angle1" + angle1 + " angle2"
					// + angle2);

				}
				else
				{

//						System.out.print("point = avgangle");
//					System.out.print("point = avgangle intersect= " + intersect + " avgangle=" + avgangle);

					pointonline = (pointatangle(intersect, avgangle)).clone();

					// System.out.println("\n pointonlinr "+pointonline);

					if ((avgangle < 60) || avgangle < 300)
					{

						if (avgangle < 60)
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
						System.out.print(
								"point = avgangle - 180 intersect= " + intersect + " avgangle=" + (avgangle - 180)
						);
						poline180 = pointatangle(intersect, (avgangle - 180)).clone();
					}
					else
					{
						System.out.print(
								"point = avgangle + 180 intersect= " + intersect + " avgangle=" + (avgangle + 180)
						);
						poline180 = pointatangle(intersect, (avgangle + 180)).clone();
					} // Point poline270 = pointatangle(intersect , (avgangle+270));

					// Imgproc.line(mat, poline90,poline180, new Scalar(0, 255,255), 3,
					// Imgproc.LINE_AA, 1);
					// Imgproc.line(mat , poline270,poline, new Scalar(0, 255,255), 3,
					// Imgproc.LINE_AA, 1);
					// System.out.print("\n angle1 "+angle1+" angle2 "+ angle2 +" avgangle " +
					// avgangle +"\n intersect point = "+ intersect +" new point "+ pointonline +"
					// \n ");
					// System.out.print("pre processed "+ p1 +" p1 "+ p2 + " p2 "+p12 +" p12 "+ p22
					// +" p22" + "\n");
					Imgproc.line(mat, poline180, pointonline, new Scalar(255, 255, 255), 1, Imgproc.LINE_AA, 0);
//	      }

					// System.out.print("line sucess");
				}

			}
		}
		// }
		System.out.println("\n \n \n point2 size" + point2.size() + "\n\n\n");
		if (point2.size() > 1000)
		{
			System.out.println("\n \n \n \n \n \n work mofo \n \n \n \n \n");
			resetter();
			// mat.empty();
			displayJPanel.removeAll();
			return src;
		}
		return mat;
	}

	public Mat runner(Mat src)
	{

		if (displayCaption("Original Image", src, dst) != 0)
		{
			System.exit(0);
		}
		dst = src.clone();
		if (displayDst(DELAY_CAPTION) != 0)
		{
		}
		if (displayCaption("Homogeneous Blur", src, dst) != 0)
		{
			System.exit(0);
		}
		for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2)
		{
			Imgproc.blur(src, dst, new Size(i, i), new Point(-1, -1));
			displayDst(DELAY_BLUR);
			return dst;
		}
		if (displayCaption("Gaussian Blur", src, dst) != 0)
		{
			System.exit(0);
		}
		for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2)
		{
			Imgproc.GaussianBlur(src, dst, new Size(i, i), 0, 0);
			displayDst(DELAY_BLUR);
			return dst;
		}
		if (displayCaption("Median Blur", src, dst) != 0)
		{
			System.exit(0);
		}
		for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2)
		{
			Imgproc.medianBlur(src, dst, i);
			displayDst(DELAY_BLUR);
			return dst;
		}
		if (displayCaption("Bilateral Blur", src, dst) != 0)
		{
			System.exit(0);
		}
		for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2)
		{
			Imgproc.bilateralFilter(src, dst, i, i * 2, i / 2);
			displayDst(DELAY_BLUR);
			return dst;
		}
		// displayCaption( "Done!" ,src , dst);
		// System.exit(0);
		return dst;
	}

	int displayCaption(String caption, Mat dst, Mat src)
	{
		dst = Mat.zeros(src.size(), src.type());
		Imgproc.putText(
				src, caption, new Point(src.cols() / 4, src.rows() / 2), Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(255, 255, 255)
		);
		return displayDst(DELAY_CAPTION);
	}

	int displayDst(int delay)
	{
		HighGui.imshow(windowName, dst);
		int c = HighGui.waitKey(delay);
		if (c >= 0)
		{
			return -1;
		}
		return 0;
	}


	private Mat lineTester(Mat mat)
	{
		// was having issues with line processing
		// this class uses fixed values for line points to prevent issues
		// this allows the testing of the processing along each step needed to find what
		// is causing current error

		Mat dst = new Mat(), cross = new Mat(), cdstP;

		double angle1 = 0, angle2 = 0, avgangle = 0;

		cross = mat.clone();
		cdstP = cross.clone();

		List<Point> point1 = new ArrayList<Point>();
		List<Point> point2 = new ArrayList<Point>();
		Mat lines = new Mat(); // will hold the results of the detection
		// Imgproc.blur(mat, cross, new Size(3, 3));
		Imgproc.Canny(mat, dst, 50, 250, 3, false);

		Point s1 = new Point(640, 240);// horizontal line s1
		Point s2 = new Point(0, 240);// horizontal line s2
		// Imgproc.line(dst,s1,s2, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);//
		// horizontal line
		// Imgproc.line(mat,s1,s2, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);//
		// horizontal line

		Point s6 = new Point(640, 480);// horizontal line s1
		Point s5 = new Point(0, 0);// horizontal line s2
		// Imgproc.line(dst,s6,s5, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);//
		// horizontal line
		// Imgproc.line(mat,s6,s5, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);//
		// horizontal line

		Point s3 = new Point(320, 600);// vertical line s3
		Point s4 = new Point(320, 000);// vertical line s4
		// Imgproc.line(dst,s3,s4, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);//
		// vertical line
		// Imgproc.line(mat,s3,s4, new Scalar(255, 0,0 ), 1, Imgproc.LINE_AA, 0);//
		// horizontal line

		System.out.println("cannyrun");

		Imgproc.HoughLinesP(dst, lines, 1, Math.PI / 180, 50, 200, 30);
		System.out.println("HoughLinesP");

		Mat houghLines = new Mat();

		houghLines.create(lines.rows(), lines.cols(), CvType.CV_8UC1);
		System.out.println("lines size " + lines.rows() + " row " + lines.cols() + " cols ");

		// Draw the lines
		for (int i = 0; i < lines.rows(); i++)
		{
			System.out.println("loop run" + i);

			double x1, y1, x2, y2;

			x1 = lines.get(i, 0)[0];
			y1 = lines.get(i, 0)[1];
			x2 = lines.get(i, 0)[2];
			y2 = lines.get(i, 0)[3];
			// System.out.println("loop points x1 "+ x1 +" y1 "+ y1+ " x2 "+x2+ "y2"+y2);
			Point pt1 = new Point(x1, y1);
			Point pt2 = new Point(x2, y2);

			// Drawing lines on an image

			Imgproc.line(cdstP, pt1, pt2, new Scalar(255, 255, 0), 3);
			if (i % 2 == 0)
			{
				point1.add(pt1);
				point1.add(pt2);
			}
			else
			{
				point2.add(pt1);
				point2.add(pt2);
			}

		}

		for (int x = 1; x < point2.size(); x++)
		{

			System.out.println("point p1 " + point1.size() + " x " + x);
			Point poline, poline90, poline270, poline180;
			Point p1 = point1.get(x);
			System.out.println("point p1" + p1);
			Point p12 = point1.get(x - 1);
			System.out.println("point p12" + p12);

			Point p2 = point2.get(x - 1);
			System.out.println("point p2" + p2);

			Point p22 = point2.get(x);
			System.out.println("point p1" + p1);
			Point pointonline = new Point();
			Point intersect = lineLineIntersection(p1, p12, p2, p22);
			angle1 = calcAngle(p1, p12);
			angle2 = calcAngle(p2, p22);
			avgangle = (angle1 + angle2) / 2;
			double avg180 = 0;
			// System.out.print("\n pointonlinr "+pointonline);
			pointonline = pointatangle(intersect, avgangle).clone();
			System.out.print(
					"\n angle1 " + angle1 + " angle2 " + angle2 + " avgangle " + avgangle + "\n intersect point = "
							+ intersect + " new point " + pointonline + " \n "
			);
			// System.out.print("pre processed "+ p1 +" p1 "+ p2 + " p2 "+p12 +" p12 "+ p22
			// +" p22" + "\n");
			Imgproc.circle(cdstP, intersect, 3, new Scalar(0, 0, 255));
			poline = pointatangle(intersect, avgangle).clone();
			// poline90 = pointatangle(intersect, (avgangle + 90));
			if (avgangle < 180)
			{
				avg180 += 180;
			}
			else
			{
				avg180 = avgangle -= 180;
			}

			poline180 = pointatangle(intersect, (avg180));
			// poline270 = pointatangle(intersect, (avgangle + 270));

			// Imgproc.line(cdstP, poline90, poline270, new Scalar(0, 255, 0), 3,
			// Imgproc.LINE_AA, 1);
			Imgproc.line(cdstP, poline180, poline, new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 1);

			System.out.println("line sucess");
		}
		Mat proc = new Mat();
		mat.copyTo(proc, cdstP);

		mat = proc.clone();
		return proc;
	}

	private byte saturate(double val)
	{
		int iVal = (int) Math.round(val);
		iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
		return (byte) iVal;
	}

	public Mat contrast(Mat mat)
	{
		Mat image = mat;
		Mat newImage = Mat.zeros(image.size(), image.type());
		Imgproc.blur(mat, mat, new Size(3, 3));
		double alpha = 1.0; /* < Simple contrast control */
		int beta = 0; /* < Simple brightness control */
		System.out.println(" Basic Linear Transforms ");
		System.out.println("-------------------------");
		// try (Scanner scanner = new Scanner(System.in)) {
		// System.out.print("* Enter the alpha value [1.0-3.0]: ");
		alpha = 2.0;
		// System.out.print("* Enter the beta value [0-100]: ");
		beta = 0;
		// }
		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];
		for (int y = 0; y < image.rows(); y++)
		{
			for (int x = 0; x < image.cols(); x++)
			{
				for (int c = 0; c < image.channels(); c++)
				{
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + c];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					newImageData[(y * image.cols() + x) * image.channels() + c] = saturate(alpha * pixelValue + beta);
				}
			}
		}
		newImage.put(0, 0, newImageData);
		// Imgproc.blur(newImage, newImage, new Size(3, 3));

		return newImage;
	}

	public Mat filtered(Mat mat)
	{
		Mat image = mat;

		Mat newImage = Mat.zeros(image.size(), image.type());
		// Core.inRange(mat, new Scalar(0,0, 0),
		// new Scalar(200,200, 200), thresh);
		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];
		for (int y = 0; y < image.rows(); y++)
		{
			System.out.print(" y" + y);
			for (int x = 0; x < image.cols(); x++)
			{
				for (int c = 0; c < image.channels(); c++)
				{
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + c];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					// System.out.println(" x "+ x +" y "+y +" pixel " +pixelValue);
					newImageData[(y * image.cols() + x) * image.channels() + c] = saturate(1 * pixelValue + 1);
				}
			}
		}
		newImage.put(0, 0, newImageData);

		return newImage;
	}

	protected Mat filter(Mat frames)
	{
		Mat frame = frames;
		Mat frameHSV = new Mat();
		Mat black = new Mat();
		// Imgproc.blur(frames, frames, new Size(3, 3));

		Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);
		Mat thresh = new Mat();
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

	double calcAngle(Point pt2, Point pt1)
	{
		double theta = Math.atan2(pt1.y - pt2.y, pt1.x - pt2.x);

		theta += Math.PI / 2.0;
		double angle = Math.toDegrees(theta);
		if (angle < 0)
		{
			angle += 360;
		}
		if (angle < 360)
		{
			angle = angle % 360;

		}
		return angle;
	}

	public void getSpace(Mat mat)
	{
		int type = 0;
		if (mat.channels() == 1)
		{
			type = BufferedImage.TYPE_BYTE_GRAY;
		}
		else if (mat.channels() == 3)
		{
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		/// this.mat=src;

		img = new BufferedImage(mat.cols(), mat.rows(), type);
	}

	Point pointatangle(Point Pos, double angle)
	{
		Point len = new Point();
		len = Pos.clone();

		double lendub = (len.x * len.x) + (len.y * len.y);
		double x1 = len.x;
		double y1 = len.y;

		System.out.println("points " + len.x + " =posx " + len.y + "posy x  =" + x1 + " y " + y1 + " lendub " + lendub);

		lendub = Math.sqrt(lendub);
		System.out.println("lenght " + len);
		// if(len<=0){len*=-1;}
		System.out.print("pos original " + Pos + "\n");
		angle = (angle * Math.PI / 180);

		// Get SOH
		double op = (double) Math.sin(angle) * lendub;
		// Get CAH
		double ad = (double) Math.cos(angle) * lendub;
		// Add to old Vector
		System.out.print("ad " + ad + "op" + op + "\n");
		Point points = new Point(ad, op);
		System.out.print("points ad op " + points + "\n");
		double x = Pos.x + points.x;
		double y = Pos.y + points.y;

		Point loc = Pos.clone();
		loc.x = x;
		loc.y = y;
		System.out.print("points loc " + loc);
		return loc;
	}

	Point lineLineIntersection(Point A, Point B, Point C, Point D)
	{

		// Line AB represented as a1x + b1y = c1
		double a1 = B.y - A.y;
		double b1 = A.x - B.x;
		double c1 = a1 * (A.x) + b1 * (A.y);

		// Line CD represented as a2x + b2y = c2
		double a2 = D.y - C.y;
		double b2 = C.x - D.x;
		double c2 = a2 * (C.x) + b2 * (C.y);

		double determinant = a1 * b2 - a2 * b1;

		if (determinant == 0)
		{
			// The lines are parallel. This is simplified
			// by returning a pair of FLT_MAX

			return new Point(0, 0);
		}
		else
		{
			double x = (b2 * c1 - b1 * c2) / determinant;
			double y = (a1 * c2 - a2 * c1) / determinant;

			return new Point(x, y);
		}
	}

	public BufferedImage getImage(Mat mat)
	{

		// mat=filterpts(mat);
		// getSpace(mat);
		//

		dst = mat;
		// mat=runner(mat);
		mat = linedet1(mat);
		//

		// mat=contrast(mat);
		// mat=doCanny(mat);

		getSpace(mat);

		// mat=filtered(mat);
		// mat=doCanny(mat);
		// mat=filter(mat);
		// mat=lineTester(mat);
		WritableRaster raster = img.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		mat.get(0, 0, data);
		return img;
	}
}
