package drivingNavigationPathDectecion;

public class AppMainCall {

	public static void main(String[] args) {
		System.out.println("Starting DrivingNavigationPathDectecion");
		try
		{
		CameraUserDisplay myCamera = CameraUserDisplay.getInstance();
		myCamera.setVisible(true);
		}
		catch(Exception exception) {
			System.out.println(exception.getMessage());	
		}
		System.out.println("Clsoing DrivingNavigationPathDectecion");
	}
	public static LogService LogService = new LogService (); 
}
