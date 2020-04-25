package drivingNavigationPathDectecion;

import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.opencv.core.Core;

public class CameraUserDisplay extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static LogService LogService = new LogService();
	private JPanel contentPane;
	private static final String ANSI_RED_BACKGROUND = "\u001B[41m";
	private int stopper = 4;
	CameraVideoProcessManager videoCap;

	static
	{
		LogService.log("Loading library:" + Core.NATIVE_LIBRARY_NAME);
		nu.pattern.OpenCV.loadShared();
	}


	public CameraUserDisplay()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();

		videoCap = new CameraVideoProcessManager(this.contentPane);

		setContentPane(contentPane);
		contentPane.setLocale(getLocale());
		new DisplayImageToJPanelThread().start();
		setBounds(0, 0, videoCap.getOneFrame().getWidth() + 15, videoCap.getOneFrame().getHeight() + 35);

		int height = videoCap.getOneFrame().getHeight();
		int width = videoCap.getOneFrame().getWidth();
		log("height" + height + "width " + width);

	}

	private void log(String text)
	{
		LogService.log(text);

	}

	public void paint(Graphics g)
	{

		g = contentPane.getGraphics();
		g.drawImage(videoCap.getOneFrame(), 0, 0, this);

	}

	class DisplayImageToJPanelThread extends Thread
	{
		@Override
		public void run()
		{
			for (;;)
			{
				repaint();
				try
				{
					Thread.sleep(150);
				} catch (InterruptedException e)
				{
				}
			}
		}
	}
}