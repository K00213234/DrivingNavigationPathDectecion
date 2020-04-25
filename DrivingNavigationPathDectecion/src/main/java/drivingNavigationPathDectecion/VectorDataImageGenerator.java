package drivingNavigationPathDectecion;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;

public interface VectorDataImageGenerator {
BufferedImage getImage(Mat imageData) ;
}