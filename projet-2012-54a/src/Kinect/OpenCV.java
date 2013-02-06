package Kinect;

import static com.googlecode.javacv.cpp.opencv_core.CV_SEQ_CONTOUR;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateSeq;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvSeqPush;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.nio.ByteBuffer;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSlice;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Implémentation des fonctions d'OpenCV en java.
 */
public class OpenCV
{
    public static void cvThreshold(IplImage src, IplImage dst, double threshold, double maxValue, int thresholdType)
    {
    	if(thresholdType == CV_THRESH_BINARY_INV)
    	{
    		int width = src.width();
    		int height = src.height();
    		int pixelIndex;

    		ByteBuffer srcBuffer = src.getByteBuffer();
    		ByteBuffer dstBuffer = dst.getByteBuffer();

    		for(int x = 0; x < width; x++)
    		{
    			for(int y = 0; y < height; y++)
    			{
    				pixelIndex = x + width*y;

    				if(getUnsignedByte(srcBuffer, pixelIndex) > threshold)
    				{
    					dstBuffer.put(pixelIndex, (byte) maxValue);
    				}
    				else
    				{
        				dstBuffer.put(pixelIndex, (byte) 0);
    				}
    			}
    		}
    	}
    }

	public static void cvCvtColor(IplImage src, IplImage dst, int code)
    {
    	if(code == CV_RGB2GRAY)
    	{
    		int width = src.width();
    		int height = src.height();
    		int srcPixelIndex;
    		
    		ByteBuffer srcBuffer = src.getByteBuffer();
    		ByteBuffer dstBuffer = dst.getByteBuffer();
    		
    		for(int x = 0; x < width; x++)
    		{
    			for(int y = 0; y < height; y++)
    			{
    				srcPixelIndex = 3*x + 3*width*y;
    				dstBuffer.put(x + width*y, (byte) (0.299*getUnsignedByte(srcBuffer, srcPixelIndex) + 0.587*getUnsignedByte(srcBuffer, srcPixelIndex+1) + 0.114*getUnsignedByte(srcBuffer, srcPixelIndex+2)));
    			}
    		}
    	}
    }

    public static void cvMinMaxLoc(IplImage src, double[] minVal, double[] maxVal, CvPoint minPoint, CvPoint maxPoint, IplImage mask)
    {
		int width = src.width();
		int height = src.height();
		int minPointX = 0, minPointY  = 0, maxPointX  = 0, maxPointY = 0;
		ByteBuffer srcBuffer = src.getByteBuffer();
		int value;
		minVal[0] = 255;
		maxVal[0] = 0;
		
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				value = getUnsignedByte(srcBuffer, x + width*y);
				
				if(value < minVal[0])
				{
					minVal[0] = value;
					minPointX = x;
					minPointY = y;
				}
				if(value > maxVal[0])
				{
					maxVal[0] = value;
					maxPointX = x;
					maxPointY = y;
				}
			}
		}

    	minPoint.x(minPointX);
    	minPoint.y(minPointY);
    	maxPoint.x(maxPointX);
    	maxPoint.y(maxPointY);
    }

    public static void cvSmooth(IplImage src, IplImage dst, int smoothtype, int param1, int param2, double param3, double param4)
    {
    	if(smoothtype == CV_GAUSSIAN)
    	{
    		float[] noyau = new float[param1];
    		int width = src.width();
    		int height = src.height();
    		ByteBuffer srcBuffer = src.getByteBuffer();
    		ByteBuffer dstBuffer = dst.getByteBuffer();
    		int value = 0;

    		for(int i = 0; i < param1; i++)
    		{
   				noyau[i] = (float)(1/(2*Math.PI*param3*param3)*Math.exp(-((i-param1/2)*(i-param1/2))/(2*param3*param3)));
    		}

    		//Filtre gaussien 2D <> double filtre gaussien 1D
    		for(int x = 0; x < width; x++)
    		{
    			for(int y = 0; y < height; y++)
    			{
    				for(int i = -param1/2; i <= param1/2; i++)
    	    		{
	    				if(0 < x + i && x + i < width)
	    				{
	    					value += getUnsignedByte(srcBuffer, x + i + width*y)*noyau[i+param1/2];
	    				}
    	    		}

	    			for(int j = -param2/2; j <= param2/2; j++)
	    			{
	    				if(0 < y + j && y + j < height)
	    				{
	    					value += getUnsignedByte(srcBuffer, x + width*(y + j))*noyau[j+param1/2];
	    				}
	    			}

    				dstBuffer.put(x + width*y, (byte) value);
    				value = 0;
    			}
    		}
    	}
    }
    
    public static CvSeq cv2FindContours(IplImage src, CvMemStorage storage, CvSeq first_contour, int header_size, int mode, int method)
    {
    	if(mode == CV_RETR_LIST && method == CV_CHAIN_APPROX_SIMPLE)
    	{
    		int width = src.width();
    		int height = src.height();
    		ByteBuffer srcBuffer = src.getByteBuffer();

    		first_contour = cvCreateSeq(CV_SEQ_CONTOUR, header_size, Loader.sizeof(CvPoint.class), storage);

    		for(int x = 0; x < width; x++)
    		{
    			for(int y = 0; y < height; y++)
    			{
    				if(isOnContour(srcBuffer, width, height, x, y))
    				{
    					CvPoint point = new CvPoint(x, y);
    					cvSeqPush(first_contour, point);
    				}
    			}
    		}
    	}

    	return first_contour; 
    }
    
    public static CvSeq cv3FindContours(IplImage src, CvMemStorage storage, CvSeq first_contour, int header_size, int mode, int method)
    {
    	if(mode == CV_RETR_LIST && method == CV_CHAIN_APPROX_SIMPLE)
    	{
    		int width = src.width();
    		int height = src.height();
    		int[][] pixelMarque = new int[width][height];
    		ByteBuffer srcBuffer = src.getByteBuffer();

    		for(int x = 0; x < width; x++)
    		{
    			for(int y = 0; y < height; y++)
    			{
    				if(isOnContour(srcBuffer, width, height, x, y) && pixelMarque[x][y] == 0) //Pixel sur un contour et non marqué
    				{
    					CvSeq contour = cvCreateSeq(CV_SEQ_CONTOUR, header_size, Loader.sizeof(CvPoint.class), storage);
    					contour.h_next(first_contour);
    					first_contour = contour;
    					
    					marquer(srcBuffer, width, height, first_contour, pixelMarque, x, y);

    					srcBuffer.put(x + width*y, (byte) 150);
    				}
    			}
    		}
    	}

    	return first_contour; 
    }
 
    public static void traverser(ByteBuffer srcBuffer, int width, int height, CvSeq contour, int[][] pixelMarque, int x, int y)
    {
    	if(x-1 >= 0 && y-1 >= 0 && pixelMarque[x-1][y-1] == 0 && isOnContour(srcBuffer, width, height, x-1, y-1))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x-1, y-1);
    		
    		if(srcBuffer.get(x-1 + width*y) == 0) //Noir
    		{
    			pixelMarque[x-1][y] = 1;
    		}
    		if(srcBuffer.get(x + width*(y-1)) == 0)
    		{
    			pixelMarque[x][y-1] = 1;
    		}
    	}
    	else if(y-1 >= 0 && pixelMarque[x][y-1] == 0 && isOnContour(srcBuffer, width, height, x, y-1))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x, y-1);
    	}
    	else if(y-1 >= 0 && x+1 < width && pixelMarque[x+1][y-1] == 0 && isOnContour(srcBuffer, width, height, x+1, y-1))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x+1, y-1);

    		if(srcBuffer.get(x + width*(y-1)) == 0) //Noir
    		{
    			pixelMarque[x][y-1] = 1;
    		}
    		if(srcBuffer.get(x+1 + width*y) == 0)
    		{
    			pixelMarque[x+1][y] = 1;
    		}
    	}
    	else if(x-1 >= 0 && pixelMarque[x-1][y] == 0 && isOnContour(srcBuffer, width, height, x-1, y))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x-1, y);
    	}
    	else if(x+1 < width && pixelMarque[x+1][y] == 0 && isOnContour(srcBuffer, width, height, x+1, y))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x+1, y);
    	}
    	else if(x-1 >= 0 && y+1 < height && pixelMarque[x-1][y+1] == 0 && isOnContour(srcBuffer, width, height, x-1, y+1))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x-1, y+1);
 
    		if(srcBuffer.get(x-1 + width*y) == 0) //Noir
    		{
    			pixelMarque[x-1][y] = 1;
    		}
    		if(srcBuffer.get(x + width*(y+1)) == 0)
    		{
    			pixelMarque[x][y+1] = 1;
    		}
    	}
    	else if(y+1 < height && pixelMarque[x][y+1] == 0 && isOnContour(srcBuffer, width, height, x, y+1))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x, y+1);
    	}
    	else if(x+1 < width && y+1 < height && pixelMarque[x+1][y+1] == 0 && isOnContour(srcBuffer, width, height, x+1, y+1))
    	{
    		marquer(srcBuffer, width, height, contour, pixelMarque, x+1, y+1);

    		if(srcBuffer.get(x+1 + width*y) == 0) //Noir
    		{
    			pixelMarque[x+1][y] = 1;
    		}
    		if(srcBuffer.get(x + width*(y+1)) == 0)
    		{
    			pixelMarque[x][y+1] = 1;
    		}
    	}
    }
  
    public static void marquer(ByteBuffer srcBuffer, int width, int height, CvSeq contour, int[][] pixelMarque, int x, int y)
    {
		CvPoint point = new CvPoint(x, y);
		cvSeqPush(contour, point);
		pixelMarque[x][y] = 1;

		traverser(srcBuffer, width, height, contour, pixelMarque, x, y);
    }

    public static boolean isOnContour(ByteBuffer srcBuffer, int width, int height, int x, int y)
    {
    	if(srcBuffer.get(x + width*y) == 0) //Noir
		{
    		if(x-1 >= 0 && y-1 >= 0 && srcBuffer.get(x-1 + width*(y-1)) == -1) //Blanc
    		{
    			return true;
    		}
    		else if(y-1 >= 0 && srcBuffer.get(x + width*(y-1)) == -1)
    		{
    			return true;
    		}
    		else if(x+1 < width && y-1 >= 0 && srcBuffer.get(x+1 + width*(y-1)) == -1)
    		{
    			return true;
    		}
    		else if(x-1 >= 0 && srcBuffer.get(x-1 + width*y) == -1)
    		{
    			return true;
    		}
    		else if(x+1 < width && srcBuffer.get(x+1 + width*y) == -1)
    		{
    			return true;
    		}
    		else if(x-1 >= 0 && y+1 < height && srcBuffer.get(x-1 + width*(y+1)) == -1)
    		{
    			return true;
    		}
    		else if(y+1 < height && srcBuffer.get(x + width*(y+1)) == -1)
    		{
    			return true;
    		}
    		else if(x+1 < width && y+1 < height && srcBuffer.get(x+1 + width*(y+1)) == -1)
    		{
    			return true;
    		}
    		else{
    			return false;
    		}
		}
		else
		{
			return false;
		}
    }

	public static double cvContourArea(CvSeq contour, CvSlice slice, int mode)
	{
		double area = 0;

		if(contour.total() > 2)
		{
			int[][] coordonne = new int[contour.total()][2];
	
			for(int i = 0; i < contour.total(); i++)
			{
				CvPoint point = new CvPoint(cvGetSeqElem(contour, i));
				coordonne[i][0] = point.x();
				coordonne[i][1] = point.y();
			}
			
			for(int i = 0; i < coordonne.length-1; i++)
			{
				area += coordonne[i][0]*coordonne[i+1][1] - coordonne[i+1][0]*coordonne[i][1];
			}

	    	return Math.abs(area/2);
		}
		else
		{
			return 0;
		}
	}

    public static int getUnsignedByte(ByteBuffer bb, int index)
    {
		return (short) (bb.get(index) & 0xff);
    }

    public static void cvDrawContours(IplImage src, CvSeq contour, CvScalar external_color, CvScalar hole_color,  int max_level,  int thickness, int lineType)
    {
		CvPoint[] coordonne = new CvPoint[contour.total()];
		int width = src.width();
		ByteBuffer srcBuffer = src.getByteBuffer();

		for(int i = 0; i < contour.total(); i++)
		{
			coordonne[i] = new CvPoint(cvGetSeqElem(contour, i));

			srcBuffer.put(3*coordonne[i].x() + 3*width*coordonne[i].y(), (byte) 255);
			srcBuffer.put(3*coordonne[i].x() + 3*width*coordonne[i].y()+1, (byte) 0);
			srcBuffer.put(3*coordonne[i].x() + 3*width*coordonne[i].y()+2, (byte) 0);
		}
    }
}
