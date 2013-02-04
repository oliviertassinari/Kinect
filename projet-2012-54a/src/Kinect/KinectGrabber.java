package Kinect;

import static com.googlecode.javacv.cpp.opencv_core.CV_MINMAX;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_16U;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvConvertScale;
import static com.googlecode.javacv.cpp.opencv_core.cvNormalize;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMap;
import org.OpenNI.GeneralException;
import org.OpenNI.License;
import org.OpenNI.MapOutputMode;
import org.OpenNI.StatusException;

import com.googlecode.javacv.cpp.opencv_core.IplImage;


public class KinectGrabber
{
	private Context context;
	private DepthGenerator depthGenerator;

	public KinectGrabber()
	{
		try
		{
			context = new Context();    
			context.addLicense(new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4="));
			context.setGlobalMirror(true);

			depthGenerator = DepthGenerator.create(context);
			depthGenerator.setMapOutputMode(new MapOutputMode(640, 480, 30));
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

			IplImage imageDepth = IplImage.create(640, 480, IPL_DEPTH_16U, 1);
			ByteBuffer depthByteBuffer = imageDepth.getByteBuffer();

			DepthMap depthM = depthGenerator.getDepthMap();
		    depthM.copyToBuffer(depthByteBuffer, 640 * 480 * 2);

			return fillHoleWithInterpolation(scale(imageDepth));
		}
		catch(GeneralException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public IplImage scale(IplImage src)
	{
		cvNormalize(src, src, -8000, 65535, CV_MINMAX, null);

		IplImage dst = IplImage.create(640, 480, IPL_DEPTH_8U, 1);
		cvConvertScale(src, dst, 1/256.0, 0);

		ByteBuffer dstByteBuffer = dst.getByteBuffer();

		for(int x = 0; x < 640; x++)
		{
			for(int y = 0; y < 480; y++)
			{
				int srcPixelIndex = x + 640*y;

				if(dstByteBuffer.get(srcPixelIndex) == 0)
				{
					dstByteBuffer.put(x + 640*y, (byte)255);
				}
			}
		}

		return dst;
	}

	public IplImage fillHoleWithInterpolation(IplImage src)
	{
		IplImage dst = src.clone();
		ByteBuffer dstByteBuffer = dst.getByteBuffer();

		ByteBuffer srcByteBuffer = src.getByteBuffer();

		int innerBandThreshold = 2;
		int outerBandThreshold = 5;

		for(int x = 0; x < 640; x++)
		{
			for(int y = 0; y < 480; y++)
			{
				int depthIndex = x + (y * 640);

			    if(srcByteBuffer.get(depthIndex) == -1) // White
			    {
			    	int[][] filterCollection = new int[49][2];
	
			    	int innerBandCount = 0;
			    	int outerBandCount = 0;

			    	for(int yi = -2; yi < 3; yi++)
			    	{
			    		for(int xi = -2; xi < 3; xi++)
			    		{
			    			if(xi != 0 || yi != 0)
			    			{
			    				int xSearch = x + xi;
			    				int ySearch = y + yi;

			    				if(xSearch >= 0 && xSearch < 640 && ySearch >= 0 && ySearch < 480)
			    				{
			    					int index = xSearch + (ySearch * 640);

			    					if(srcByteBuffer.get(index) != -1) // White
			    					{
			    						// We want to find count the frequency of each depth
			    						for(int i = 0; i < 24; i++)
			    						{
			    							if(filterCollection[i][0] == srcByteBuffer.get(index))
			    							{
			    								// When the depth is already in the filter collection
			    								filterCollection[i][1]++;
			    								break;
			    							}
			    							else if (filterCollection[i][0] == 0)
			    							{
			    								// We will then add the new depth and start it's frequency at 1.
			    								filterCollection[i][0] = srcByteBuffer.get(index);
			    								filterCollection[i][1]++;
			    								break;
			    							}
			    						}

			    						// We will then determine which band the non-0 pixel
			    						if(yi != 2 && yi != -2 && xi != 2 && xi != -2)
			    						{
			    							innerBandCount++;
			    						}
			    						else
			    						{
			    							outerBandCount++;
			    						}
			    					}
			    				}
			    			}
			    		}
			    	}

			    	// Once we have determined our inner and outer band non-zero counts, and 
			    	// accumulated all of those values, we can compare it against the threshold
			    	// to determine if our candidate pixel will be changed to the
			    	// statistical mode of the non-zero surrounding pixels.
			    	if(innerBandCount >= innerBandThreshold || outerBandCount >= outerBandThreshold)
			    	{
			    		int frequency = 0;
			    		int depth = 0;
	  
			    		// This loop will determine the statistical mode
			    		// of the surrounding pixels for assignment to
			    		// the candidate.
			    		for(int i = 0; i < 24; i++)
			    		{
			    			if(filterCollection[i][0] == 0)
			    			{
			    				break;
			    			}
			    			if(filterCollection[i][1] > frequency)
			    			{
			    				depth = filterCollection[i][0];
			    				frequency = filterCollection[i][1];
			    			}
			    		}

			    		dstByteBuffer.put(depthIndex, (byte)depth);
			    	}
			    }
			}
		}

		return dst;
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
