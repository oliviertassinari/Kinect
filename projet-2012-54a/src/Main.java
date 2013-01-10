import com.googlecode.javacv.FrameGrabber.Exception;

import Kinect.Kinect;

public class Main
{
    public static void main(String[] args)
    {
    	System.load(System.getProperty("user.dir")+"\\lib\\fannfloat.dll"); 

    	new Kinect();
    }
}
