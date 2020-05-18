package drivingNavigationPathDectecion;

import org.opencv.core.Point;

public class GeometryMethods
{

    public static double pythagoreanDistance(Point a, Point b)
    {
        double cDistanceSquared = Math.pow(b.x - a.x, 2) + Math.pow(b.y - b.y, 2);
        System.out.println("points " + a.x + " =posx " + a.y + "posy x  =" + b.x + " y " + b.y + " len^2 " + cDistanceSquared);
        return Math.sqrt(cDistanceSquared);
    }

    public static Point pointSquaredIntheDistance(Point point)
    {
        return new Point(point.x * point.x, point.y * point.y);
    }
}
