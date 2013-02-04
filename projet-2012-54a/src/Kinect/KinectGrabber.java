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
		cvNormalize(src, src, 65535, 0, CV_MINMAX, null);

		IplImage pImg = IplImage.create(640, 480, IPL_DEPTH_8U, 1);
		cvConvertScale(src, pImg, 1/256.0, 0);

		ByteBuffer pImgByteBuffer = pImg.getByteBuffer();

		for(int x = 0; x < 640; x++)
		{
			for(int y = 0; y < 480; y++)
			{
				int srcPixelIndex = x + 640*y;

				if(pImgByteBuffer.get(srcPixelIndex) == 0)
				{
					pImgByteBuffer.put(x + 640*y, (byte)255);
				}
			}
		}

		return pImg;
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

		    // We are only concerned with eliminating 'white' noise from the data.
		    // We consider any pixel with a depth of 255 as a possible candidate for filtering.
		    if(OpenCV2.getUnsignedByte(srcByteBuffer, depthIndex) == 255) //White
		    {
		      // The filter collection is used to count the frequency of each
		      // depth value in the filter array. This is used later to determine
		      // the statistical mode for possible assignment to the candidate.
		      int[][] filterCollection = new int[49][2];

		      // The inner and outer band counts are used later to compare against the threshold 
		      // values set in the UI to identify a positive filter result.
		      int innerBandCount = 0;
		      int outerBandCount = 0;

		      // The following loops will loop through a 5 X 5 matrix of pixels surrounding the 
		      // candidate pixel. This defines 2 distinct 'bands' around the candidate pixel.
		      // If any of the pixels in this matrix are non-0, we will accumulate them and count
		      // how many non-0 pixels are in each band. If the number of non-0 pixels breaks the
		      // threshold in either band, then the average of all non-0 pixels in the matrix is applied
		      // to the candidate pixel.
		      for(int yi = -3; yi < 4; yi++)
		      {
		    	  for(int xi = -3; xi < 4; xi++)
		    	  {
		          // yi and xi are modifiers that will be subtracted from and added to the
		          // candidate pixel's x and y coordinates that we calculated earlier. From the
		          // resulting coordinates, we can calculate the index to be addressed for processing.

		          // We do not want to consider the candidate
		          // pixel (xi = 0, yi = 0) in our process at this point.
		          // We already know that it's 0
		          if(xi != 0 || yi != 0)
		          {
		            // We then create our modified coordinates for each pass
		            int xSearch = x + xi;
		            int ySearch = y + yi;

		            // While the modified coordinates may in fact calculate out to an actual index, it 
		            // might not be the one we want. Be sure to check
		            // to make sure that the modified coordinates
		            // match up with our image bounds.
		            if(xSearch >= 0 && xSearch < 640 && ySearch >= 0 && ySearch < 480)
		            {
		              int index = xSearch + (ySearch * 640);
		              // We only want to look for non-0 values
		              if(OpenCV2.getUnsignedByte(srcByteBuffer, index) != 255)
		              {
		                // We want to find count the frequency of each depth
		                for(int i = 0; i < 49; i++)
		                {
		                  if(filterCollection[i][0] == OpenCV2.getUnsignedByte(srcByteBuffer, index))
		                  {
		                    // When the depth is already in the filter collection
		                    // we will just increment the frequency.
		                    filterCollection[i][1]++;
		                    break;
		                  }
		                  else if (filterCollection[i][0] == 0)
		                  {
		                    // When we encounter a 0 depth in the filter collection
		                    // this means we have reached the end of values already counted.
		                    // We will then add the new depth and start it's frequency at 1.
		                    filterCollection[i][0] = OpenCV2.getUnsignedByte(srcByteBuffer, index);
		                    filterCollection[i][1]++;
		                    break;
		                  }
		                }

		                // We will then determine which band the non-0 pixel
		                // was found in, and increment the band counters.
		                if (yi != 3 && yi != -3 && xi != 3 && xi != -3)
		                  innerBandCount++;
		                else
		                  outerBandCount++;
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
		          // This means we have reached the end of our
		          // frequency distribution and can break out of the
		          // loop to save time.
		          if (filterCollection[i][0] == 0)
		            break;
		          if (filterCollection[i][1] > frequency)
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
