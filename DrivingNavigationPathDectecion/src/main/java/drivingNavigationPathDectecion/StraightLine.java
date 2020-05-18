package drivingNavigationPathDectecion;

import org.opencv.core.Point;

public class StraightLine
{

	public StraightLine()
	{
	}

	public StraightLine(Point pointA, Point pointB)
	{
		this.pointA = pointA;
		this.pointB = pointB;
	}

	public Point pointA;
	public Point pointB;

	public static double getDeterminant(StraightLine line1, StraightLine line2)
	{
		return getDeterminant(line1.pointA, line1.pointB, line2.pointA, line2.pointB);

	}

	public static double getDeterminant(Point line1PointA, Point line1PointB, Point line2PointA, Point line2PointB)
	{

		// Line AB represented as a1x + b1y = c1
		double a1 = line1PointB.y - line1PointA.y;
		double b1 = line1PointA.x - line1PointB.x;

		double a2 = line2PointB.y - line2PointA.y;
		double b2 = line2PointA.x - line2PointB.x;

		return a1 * b2 - a2 * b1;
	}

	public static boolean linesIntersect(StraightLine line1, StraightLine line2)
	{
		return linesIntersect(line1.pointA, line1.pointB, line2.pointA, line2.pointB);
	}

	
	public static boolean linesIntersect(Point line1PointA, Point line1PointB, Point line2PointA, Point line2PointB)
	{

		double determinant = getDeterminant(line1PointA, line1PointB, line2PointA, line2PointB);
		return determinant != 0;
	}

	public static Point getPointOfLinesIntersection(StraightLine line1, StraightLine line2)
	{
		return getPointOfLinesIntersection(line1.pointA, line1.pointB, line2.pointA, line2.pointB);

	}

	public static Point getPointOfLinesIntersection(Point line1PointA, Point line1PointB, Point line2PointA,
			Point line2PointB)
	{

		// Line AB represented as a1x + b1y = c1
		double a1 = line1PointB.y - line1PointA.y;
		double b1 = line1PointA.x - line1PointB.x;
		double c1 = a1 * (line1PointA.x) + b1 * (line1PointA.y);

		// Line CD represented as a2x + b2y = c2
		double a2 = line2PointB.y - line2PointA.y;
		double b2 = line2PointA.x - line2PointB.x;
		double c2 = a2 * (line2PointA.x) + b2 * (line2PointA.y);

		double determinant = getDeterminant(line1PointA, line1PointB, line2PointA, line2PointB);

		double x = (b2 * c1 - b1 * c2) / determinant;
		double y = (a1 * c2 - a2 * c1) / determinant;

		return new Point(x, y);
	}
}
