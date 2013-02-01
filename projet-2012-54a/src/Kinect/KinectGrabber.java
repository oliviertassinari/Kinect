package Kinect;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16U;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMap;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.License;
import org.OpenNI.MapOutputMode;
import org.OpenNI.StatusException;

import com.googlecode.javacv.cpp.opencv_core.IplImage;


public class KinectGrabber
{
	private Context context;
	private DepthMetaData depthMD;
	private DepthGenerator depthGenerator;

	public KinectGrabber()
	{
		try
		{
			context = new Context();    
			context.addLicense(new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=")); 

			depthGenerator = DepthGenerator.create(context);
			depthGenerator.setMapOutputMode(new MapOutputMode(640, 480, 30)); 

			context.setGlobalMirror(true);
		}
		catch(GeneralException e)
		{
			e.printStackTrace();
		}
	}

	public void start()
	{
		try
		{
			context.startGeneratingAll();
			depthMD = depthGenerator.getMetaData();
		}
		catch(StatusException e)
		{
			e.printStackTrace();
		} 
	}

	public IplImage grab()
	{
		try
		{
			context.waitAnyUpdateAll();

			DepthMap depthM = depthMD.getData();
		    int i = depthM.getXRes() * depthM.getYRes() * depthM.getBytesPerPixel();
		    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(i);
		    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		    depthM.copyToBuffer(byteBuffer, i);

		    IplImage lol = IplImage.create(depthM.getXRes(), depthM.getYRes(), IPL_DEPTH_16U, 1);
		    ByteBuffer lolBuffer = lol.getByteBuffer();

    		for(int x = 0; x < 640; x++)
    		{
    			for(int y = 0; y < 480; y++)
    			{
    				int srcPixelIndex = 2*x + 2*640*y;
    				lolBuffer.put(2*x + 2*640*y, (byte)OpenCV2.getUnsignedByte(byteBuffer, srcPixelIndex));
    				lolBuffer.put(2*x + 2*640*y+1, (byte)OpenCV2.getUnsignedByte(byteBuffer, srcPixelIndex+1));
    			}
    		}

			return lol;
		}
		catch(StatusException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	  private BufferedImage bufToImage(ByteBuffer pixelsRGB)
	  /* Transform the ByteBuffer of pixel data into a BufferedImage
	     Converts RGB bytes to ARGB ints with no transparency. 
	  */
	  {
	    int[] pixelInts = new int[640 * 480];
	 
	    int rowStart = 0;
	        // rowStart will index the first byte (red) in each row;
	        // starts with first row, and moves down

	    int bbIdx;               // index into ByteBuffer
	    int i = 0;               // index into pixels int[]
	    int rowLen = 640 * 3;    // number of bytes in each row
	    for (int row = 0; row < 480; row++) {
	      bbIdx = rowStart;
	      // System.out.println("bbIdx: " + bbIdx);
	      for (int col = 0; col < 640; col++) {
	        int pixR = pixelsRGB.get( bbIdx++ );
	        int pixG = pixelsRGB.get( bbIdx++ );
	        int pixB = pixelsRGB.get( bbIdx++ );
	        pixelInts[i++] = 
	           0xFF000000 | ((pixR & 0xFF) << 16) | 
	           ((pixG & 0xFF) << 8) | (pixB & 0xFF);
	      }
	      rowStart += rowLen;   // move to next row
	    }

	    // create a BufferedImage from the pixel data
	    BufferedImage im = 
	       new BufferedImage( 640, 480, BufferedImage.TYPE_INT_ARGB);
	    im.setRGB( 0, 0, 640, 480, pixelInts, 0, 640 );
	    return im;
	  } 
	
	public void stop()
	{
		try
		{
			context.stopGeneratingAll();
		}
		catch(StatusException e)
		{
		}

		context.release();
	}
}
