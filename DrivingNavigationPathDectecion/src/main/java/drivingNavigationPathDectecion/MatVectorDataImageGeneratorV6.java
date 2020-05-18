package drivingNavigationPathDectecion;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MatVectorDataImageGeneratorV6 implements VectorDataImageGenerator
{

    private BufferedImage bufferedImage;
    private final JPanel displayJPanel;
    private final List<Point> pointArray1 = new ArrayList<Point>();// These should be an array of lines
    private final List<Point> pointArray2 = new ArrayList<Point>();//

    public MatVectorDataImageGeneratorV6(JPanel displayJPanel)
    {
        this.displayJPanel = displayJPanel;
    }

    public MatVectorDataImageGeneratorV6(JPanel displayJPanel, Mat imageDataMat)
    {
        this.displayJPanel = displayJPanel;
        displayMatOnSCreenUsingBufferedImage(imageDataMat);
    }

    //
    // public methods
    //
    public BufferedImage getImage(Mat imageDataMat)
    {
        Mat dectectLinesImageDataMat = dectectLinesInImageData(imageDataMat);

        displayMatOnSCreenUsingBufferedImage(dectectLinesImageDataMat);

        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        dectectLinesImageDataMat.get(0, 0, data);
        return bufferedImage;
    }
    //
    // private methods
    //

    private void reset()
    {
        this.pointArray1.clear();
        this.pointArray2.clear();
    }

    private Mat dectectLinesInImageData(Mat imageDataMat)
    {

        Mat edgesMat = getEdgesMat(imageDataMat);

        Mat houghLinesMat = detectStandardHoughLineAndTranform(edgesMat);

        workWithTheLines(imageDataMat, houghLinesMat);

        if (pointArray2.size() > 20)// to limit number of comparisons made
        {
            reset();
            displayJPanel.removeAll();
            final Mat blankMat = new Mat();
            return blankMat;
        }
        else
        {
            return imageDataMat;
        }
    }

    private Mat getEdgesMat(Mat imageDataMat)
    {
        final double sigmaX = 1;
        final Size ksize = new Size(1, 1);

        Mat edgesMat = new Mat();

        Imgproc.GaussianBlur(imageDataMat, imageDataMat, ksize, sigmaX);
        Imgproc.Canny(imageDataMat, edgesMat, 150, 300, 3, true);

        return edgesMat;
    }

    private Mat detectStandardHoughLineAndTranform(Mat edgesMat)
    {
        Mat houghLinesMat = new Mat(); // will hold the results of the detection

        final int threshold = 125;
        final double rho = 1;
        Imgproc.HoughLines(edgesMat, houghLinesMat, rho, Math.PI / 720, threshold); // runs the actual detection

        return houghLinesMat;
    }

    private int getImageType(Mat mat)
    {
        switch (mat.channels())
        {
            case 1:
                return BufferedImage.TYPE_BYTE_GRAY;
            case 3:
                return BufferedImage.TYPE_3BYTE_BGR;
            default:
                return BufferedImage.TYPE_CUSTOM;// default type
        }
    }

    private void displayMatOnSCreenUsingBufferedImage(Mat imageDataMat)
    {
        int type = getImageType(imageDataMat);

        try
        {
            bufferedImage = new BufferedImage(imageDataMat.cols(), imageDataMat.rows(), type);
        }
        catch (Exception e) // sometimes Mat is null
        {
            CameraUserDisplay.LogService.log(e.getMessage());

        }
    }

    private void workWithTheLines(Mat imageDataMat, Mat houghLinesMat)
    {

        checkIsSuitableAngle(0, imageDataMat, houghLinesMat, this.pointArray1);
        checkIsSuitableAngle(1, imageDataMat, houghLinesMat, this.pointArray2);

        final int imageHeight = 480;
        final int imageLenght = 640;

        Point bottomLeftCornerPoint = new Point(0, 0);
        Point topRightCornerPoint = new Point(imageHeight, imageLenght);

        for (int index = 1; index < pointArray2.size(); index += 2)
        {
            StraightLine line1 = new StraightLine(pointArray1.get(index - 1), pointArray1.get(index));
            StraightLine line2 = new StraightLine(pointArray2.get(index - 1), pointArray2.get(index));

            if (StraightLine.linesIntersect(line1, line2))
            {
                Point intersectionPoint = StraightLine.getPointOfLinesIntersection(line1, line2);

                if (isInBounds(bottomLeftCornerPoint, topRightCornerPoint, intersectionPoint))
                {
                    Imgproc.circle(imageDataMat, intersectionPoint, 5, new Scalar(0, 0, 255));
                }
                else
                {
                    drawPathForDrone(imageDataMat, index, line1, line2, intersectionPoint);// lines intersect outside of
                    // the image
                }
            }
        }
    }

    private boolean isInBounds(Point bottomLeftCornerPoint, Point topRightCornerPoint, Point point)
    {
        return (point.y > bottomLeftCornerPoint.x) && (point.x < topRightCornerPoint.y)
                && (point.y > bottomLeftCornerPoint.x) && (point.y < topRightCornerPoint.x);

    }

    private static void checkIsSuitableAngle(int startIndex, Mat imageDataMat, Mat houghLinesMat,
            List<Point> pointArray)// addingPolarCoridanteSytem
    {
        for (int index = startIndex; index < houghLinesMat.rows(); index++)
        {
            double[] lineLenghtAndAngle = houghLinesMat.get(index, 0);
            double triangleHypotenuseLenght = lineLenghtAndAngle[0];// rho is
            double thetaAngle = lineLenghtAndAngle[1];
            double triangleSideALenght = Math.cos(thetaAngle);
            double triangleSideBLenght = Math.sin(thetaAngle);
            
            
            //TODO: refactor readability
            Point pointWhatEver = new Point(triangleSideALenght * triangleHypotenuseLenght, triangleSideBLenght * triangleHypotenuseLenght);

            final int lineLenght = 1000;
            
            Point pointAhead = offsetPointBy(pointWhatEver, lineLenght, -triangleSideBLenght, triangleSideALenght);
            Point pointBehind = offsetPointBy(pointWhatEver, -lineLenght, -triangleSideBLenght, triangleSideALenght);


            double angle = calcAngleFromDriverBumber(pointAhead, pointBehind);

            if ((angle < 100 && angle > 80) || (angle < 280 && angle > 260))
            {
                CameraUserDisplay.LogService.log("Going horizontal");
            }
            else
            {
                Imgproc.line(imageDataMat, pointAhead, pointBehind, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, 0);

            }
            pointArray.add(pointAhead);
            pointArray.add(pointBehind);
        }
    }
        public static Point offsetPointBy(Point point, final int lineLenght, double xFactor, double yFactor)
    {
        final double x = Math.round(point.x + lineLenght * xFactor);
        final double y = Math.round(point.y + lineLenght * yFactor);

        return new Point(x, y);
    }

    private String getCarDirection(double drivingAngle)
    {
        if (drivingAngle < 89)
        {
            return ("going right");
        }
        else if (drivingAngle >= 89 || drivingAngle <= 91)
        {
            return ("going straight");
        }
        else
        {
            return ("going left");
        }
    }

    private void drawPathForDrone(Mat imageDataMat, int index, StraightLine line1, StraightLine line2,
            Point intersectionPoint)
    {
        double angle1 = calcAngleFromDriverBumber(line1.pointA, line1.pointB);
        double angle2 = calcAngleFromDriverBumber(line2.pointA, line2.pointB);
        double averageAngle = (angle1 + angle2) / 2;

        if (((angle2 < 100 && angle2 > 80) || (angle2 < 280 && angle2 > 260)) && (pointArray2.size() > index + 1))
        {
            line2.pointA = pointArray2.get(index);
            line2.pointB = pointArray2.get(index + 1);
        }
        else if ((angle1 < 100 && angle1 > 80) || (angle1 < 280 && angle1 > 260) && (pointArray1.size() > index + 1))
        {
            line1.pointA = pointArray1.get(index);
            line1.pointB = pointArray1.get(index + 1);
        }
        else
        {
            Point pointOnLine = (getPointAtAngle(intersectionPoint, averageAngle)).clone();
            Point poline180 = new Point();

            Point poline90 = new Point();
            if ((averageAngle < 60) || averageAngle > 300)
            {
                log("" + averageAngle);

                String direction = getCarDirection(averageAngle);
                directCar(direction);

                poline90 = getPointAtAngle(intersectionPoint, (averageAngle + 90)).clone();
                if (averageAngle > 180)
                {
                    poline180 = getPointAtAngle(intersectionPoint, (averageAngle - 180)).clone();
                }
                else
                {
                    log("point = avgangle + 180 intersect= " + intersectionPoint + " avgangle=" + (averageAngle + 180));
                    poline180 = getPointAtAngle(intersectionPoint, (averageAngle + 180)).clone();
                }
            }
            Point poline270 = getPointAtAngle(intersectionPoint, (averageAngle + 270));
            Imgproc.line(imageDataMat, poline90, poline180, new Scalar(0, 255, 255), 3, Imgproc.LINE_AA, 1);
            Imgproc.line(imageDataMat, poline270, pointOnLine, new Scalar(0, 255, 255), 3, Imgproc.LINE_AA, 1);
            Point polinem90 = getPointAtAngle(intersectionPoint, (averageAngle + 180)).clone();
            log("intersection point = " + intersectionPoint + " avgangle=" + (averageAngle + 180) + "pointonline" + pointOnLine);
            poline90 = getPointAtAngle(intersectionPoint, (averageAngle + 90)).clone();
            Imgproc.line(imageDataMat, intersectionPoint, pointOnLine, new Scalar(255, 255, 255), 1, Imgproc.LINE_AA, 0);
            Imgproc.line(imageDataMat, polinem90, poline90, new Scalar(255, 255, 255), 1, Imgproc.LINE_AA, 0);
            Imgproc.line(imageDataMat, pointOnLine, poline180, new Scalar(255, 255, 255), 1, Imgproc.LINE_AA, 0);

        }
    }

    private static double calcAngleFromDriverBumber(Point point2, Point point1)
    {
        // Car line is 0x axis

        double theta = Math.atan2(point1.y - point2.y, point1.x - point2.x);

        theta += Math.PI / 2.0;

        double angle = Math.toDegrees(theta);
        if (angle < 0)
        {
            angle += 360;
        }
        if (angle > 360)
        {
            angle %= 360;
        }
        return angle;
    }

    private void directCar(String message)
    {
        System.out.println("\n\n " + message + " \n\n");
    }

    private Point getPointAtAngle(Point position, double angle)
    {
        Point pointIntheDistance = GeometryMethods.pointSquaredIntheDistance(position);

        double lineDistance = GeometryMethods.pythagoreanDistance(position, pointIntheDistance);

        // log("pos original " + position,
        // CameraUserDisplay.LogService.DebuggingAtAngleToken);
        // log("pos future " + pointSquaredIntheDistance,
        // CameraUserDisplay.LogService.DebuggingAtAngleToken);
        // log("lendub " + lineDistance,
        // CameraUserDisplay.LogService.DebuggingAtAngleToken);
        double angleRadians = (angle * Math.PI / 180);

        // Get SineOH
        double opposite = Math.sin(angleRadians) * lineDistance;
        // Get CAH
        double adjacent = Math.cos(angleRadians) * lineDistance;
        // Add to old Vector

        log("ad " + adjacent + "op" + opposite);

        Point point = new Point(adjacent, opposite);
        CameraUserDisplay.LogService.log("points ad op " + point);
        double x = position.x + point.x;
        double y = position.y + point.y;

        Point location = position.clone();
        location.x = x;
        location.y = y;

        log("points loc " + location);
        log("points loc location.x" + location.x);
        log("points loc location.y" + location.y);

        log("lenght " + x + "" + y);
        log("points loc " + location.toString());
        return location;
    }

    private void log(String message, LogToken token)
    {
        CameraUserDisplay.LogService.log(message, token);
    }

    private void log(String message)
    {
        CameraUserDisplay.LogService.log(message);
    }
}
