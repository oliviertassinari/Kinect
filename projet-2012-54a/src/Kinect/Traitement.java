package Kinect;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.OpenKinectFrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvBox2D;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvConvexityDefect;


public class Traitement implements Runnable
{
	private Thread runner;
 
	private MainPosition mainPosition = new MainPosition();
	private MainPosition mainPositionLeft = new MainPosition(); 
	private MainPosition mainPositionRight = new MainPosition();
	private MainPosition mainPositionFiltreLeft = new MainPosition(); 
	private MainPosition mainPositionFiltreRight = new MainPosition();
	private IplImage imageTraitement;
	private long timeLastGrab;
	private OneEuroFilter filtreLeft = new OneEuroFilter(0.1, 5.0, 0.01, 1.0);
	private OneEuroFilter filtreRight = new OneEuroFilter(0.1, 5.0, 0.01, 1.0);

	public Traitement()
    {
		runner = new Thread(this, "kinect");
		runner.start();
    }

	public void run()
	{
		try
		{
	    	//OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("video/depth_pact54_test1.mkv");
	    	//OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("video/depth_pact54_test2.mpg");
	    	//OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("video/depth_pact42_test1.mkv");
	    	//OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("video/depth_pact54_test2.mkv");
	        KinectGrabber grabber = new KinectGrabber();

			grabber.start();

			IplImage imageGrab = grabber.grab();
			int width  = imageGrab.width();
			int height = imageGrab.height();
	    	CvPoint minPoint = new CvPoint();
	    	CvPoint maxPoint = new CvPoint();
	    	double[] minVal = new double[1];
	    	double[] maxVal = new double[1];

	    	// creation window used to display the video, the object in JavaCv can use the material acceleration
	    	JFrame Fenetre = new JFrame();
			Fenetre.setLayout(new GridLayout(1, 2));
			Fenetre.setTitle("module JavaCV");
			Fenetre.setSize(width*2+20, height);
			Fenetre.setLocationRelativeTo(null);
			Fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			CanvasFrame fenetreFrame1 = new CanvasFrame("AVI Playback Demo");
			fenetreFrame1.setVisible(false);
			Fenetre.getContentPane().add(fenetreFrame1.getCanvas());

			CanvasFrame fenetreFrame2 = new CanvasFrame("AVI Playback Demo");
			fenetreFrame2.setVisible(false);
			Fenetre.getContentPane().add(fenetreFrame2.getCanvas());

			Fenetre.setVisible(true);

			CvMemStorage storage = CvMemStorage.create();

			// OpenCV2.cv2Smooth(imageTraitement, imageTraitement, CV_GAUSSIAN, 9, 9, 1, 0);
			// OpenCV2.cv2MinMaxLoc(imageTraitement, minVal, maxVal, minPoint, maxPoint, null);
			// OpenCV2.cv2Threshold(imageTraitement, imageThreshold, minVal[0] + 4*isFind, 255, CV_THRESH_BINARY);
			// contour = OpenCV2.cv3FindContours(imageTraitement, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
			// OpenCV2.cv2Threshold(imageTraitement, imageThreshold, minVal[0] + 4*isFind, 255, CV_THRESH_BINARY);
			// double aire = OpenCV2.cv2ContourArea(contour, CV_WHOLE_SEQ, 0);
			// OpenCV2.cv2DrawContours(imageDislay2, contour, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);

			while((imageGrab = grabber.grab()) != null)
			{
				timeLastGrab = System.currentTimeMillis();

				IplImage imageDislay2 = IplImage.create(width, height, IPL_DEPTH_8U, 3);
				cvCvtColor(imageGrab, imageDislay2, CV_GRAY2RGB);

				imageTraitement = imageGrab.clone();

	        	cvSmooth(imageTraitement, imageTraitement, CV_BLUR, 3);

	        	IplImage imageThreshold = imageTraitement.clone();

	        	cvMinMaxLoc(imageTraitement, minVal, maxVal, minPoint, maxPoint, null);

	        	int isFind = 1;
	        	ArrayList<CvPoint> centerList = new ArrayList<CvPoint>();

	        	while(isFind != 0 && isFind < 3) //2 iterations max
	        	{
	        		isFind++;

		        	cvThreshold(imageTraitement, imageThreshold, minVal[0] + 4*isFind, 255, CV_THRESH_BINARY);

		        	cvCircle(imageDislay2, minPoint, 3, CvScalar.YELLOW, -1, 8, 0);

		        	CvSeq contour = new CvSeq();
		         	cvFindContours(imageThreshold.clone(), storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

		            while(contour != null && !contour.isNull())
		            {
		            	if(contour.elem_size() > 0)
		                {
		            		double aire = cvContourArea(contour, CV_WHOLE_SEQ, 0);

		                	if(aire > 400 && aire < 24000)
		                	{
		                		isFind = 0; //true

		                		cvDrawContours(imageDislay2, contour, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);

		                	    CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.015, 0);
		                		cvDrawContours(imageDislay2, points, CvScalar.GREEN, CvScalar.GREEN, -1, 1, CV_AA);

		                		CvSeq convex = cvConvexHull2(contour, storage, CV_COUNTER_CLOCKWISE, 1);
		                		cvDrawContours(imageDislay2, convex, CvScalar.RED, CvScalar.RED, -1, 1, CV_AA);

		                		CvPoint centre = getContourCenter(convex, storage);
		                		cvCircle(imageDislay2, centre, 3, CvScalar.RED, -1, 8, 0);

		                		CvSeq hull = cvConvexHull2(contour, storage, CV_COUNTER_CLOCKWISE, 0);
		                		CvSeq defect = cvConvexityDefects(contour, hull, storage);

		                		while(defect != null)
		                		{
		                    		for(int i = 0; i < defect.total(); i++)
		                    		{
		                    			 CvConvexityDefect convexityDefect = new CvConvexityDefect(cvGetSeqElem(defect, i));
	
		                    			 if(convexityDefect.depth() > 10)
		                    			 {
			                    			 cvCircle(imageDislay2, convexityDefect.start(), 3, CvScalar.MAGENTA, -1, 8, 0);
			                    			 cvCircle(imageDislay2, convexityDefect.end(), 3, CvScalar.CYAN, -1, 8, 0);
			                    			 cvCircle(imageDislay2, convexityDefect.depth_point(), 3, CvScalar.WHITE, -1, 8, 0);
	
			                    			 //System.out.println(convexityDefect.depth());
		                    			 }
		                    		}

		                		    defect = defect.h_next();
		                		}

		                		centerList.add(centre);
		                 	}
		                }
		                contour = contour.h_next();
		            }
	        	}

	        	getPositionHand(centerList);
	        	getPositionFiltreHand();
	        	reconnaissanceDeMvt();

	    		cvCircle(imageDislay2, new CvPoint((int)mainPositionFiltreLeft.get(0)[1], (int)mainPositionFiltreLeft.get(0)[2]), 3, CvScalar.BLACK, -1, 8, 0);
	    		cvCircle(imageDislay2, new CvPoint((int)mainPositionFiltreRight.get(0)[1], (int)mainPositionFiltreRight.get(0)[2]), 3, CvScalar.BLACK, -1, 8, 0);

				CvFont font = new CvFont(CV_FONT_HERSHEY_COMPLEX, 0.6, 1); 

				if(mainPositionLeft.get(0)[0] == timeLastGrab)
				{
					cvPutText(imageDislay2, "Gauche", cvPoint((int)mainPositionLeft.get(0)[1]-20, (int)mainPositionLeft.get(0)[2]-10), font, CvScalar.BLUE);
				}
				if(mainPositionRight.get(0)[0] == timeLastGrab)
				{
					cvPutText(imageDislay2, "Droite", cvPoint((int)mainPositionRight.get(0)[1]-20, (int)mainPositionRight.get(0)[2]-10), font, CvScalar.RED);
				}

				fenetreFrame1.showImage(imageThreshold);
				fenetreFrame2.showImage(imageDislay2);

				cvClearMemStorage(storage);

				long timeEnd = 50-(int)(System.currentTimeMillis()-timeLastGrab);
	
				if(timeEnd > 0)
				{
					Thread.sleep(timeEnd);
				}

				System.out.println(-timeEnd+50+" ms");
		    }

			grabber.stop();
			fenetreFrame1.dispose();
			fenetreFrame2.dispose();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
    }

    public void getPositionHand(ArrayList<CvPoint> centerList)
    {
    	long[] center = mainPosition.get(0);
		long[] centreLeft = mainPositionLeft.get(0);
		long[] centreRight = mainPositionRight.get(0);

		if(centreLeft[0] == 0 && centreRight[0] == 0) //Vide
		{
			if(centerList.size() == 1) //1 centre detecte
			{
				CvPoint centre = centerList.get(0);
				mainPosition.add(timeLastGrab, centre, getDepth(centre), 0);
			}
			else if(centerList.size() > 1)
			{
    			CvPoint centre1 = centerList.get(0);
    			CvPoint centre2 = centerList.get(1);

    			if(centre2.x() > centre1.x()) //center1 : left
    			{
    				mainPositionLeft.add(timeLastGrab, centre1, getDepth(centre1), 0);
    				mainPositionRight.add(timeLastGrab, centre2, getDepth(centre2), 0);

	    			if(center[0] != 0) //non vide
	    			{
	    				if(getLenght(center[0], center[1], centre1.x(), centre1.y()) < getLenght(center[0], center[1], centre2.x(), centre2.y()))
	    				{
	    					mainPositionLeft.add(mainPosition);
	    				}
	    				else
	    				{
	    					mainPositionRight.add(mainPosition);
	    				}
	    			}
    			}
    			else
    			{
    				mainPositionLeft.add(timeLastGrab, centre2, getDepth(centre2), 0);
    				mainPositionRight.add(timeLastGrab, centre1, getDepth(centre1), 0);	

	    			if(center[0] != 0) //non vide
	    			{
	    				if(getLenght(center[0], center[1], centre1.x(), centre1.y()) < getLenght(center[0], center[1], centre2.x(), centre2.y()))
	    				{
	    					mainPositionRight.add(mainPosition);
	    				}
	    				else
	    				{
	    					mainPositionLeft.add(mainPosition);
	    				}
	    			}
    			}
			}
		}
		else if(centerList.size() > 0)
		{
			int choose = 0;
			double[] lengthListToLeft = new double[centerList.size()];
			double[] lengthListToRight = new double[centerList.size()];

			for(int i = 0; i < centerList.size(); i++)
			{
				lengthListToLeft[i] = getLenght(centreLeft[1], centreLeft[2], centerList.get(i).x(), centerList.get(i).y());
				lengthListToRight[i] = getLenght(centreRight[1], centreRight[2], centerList.get(i).x(), centerList.get(i).y());
			}

			int[] minLengthListToLeft = getMinList(lengthListToLeft);
			int[] minLengthListToRight = getMinList(lengthListToRight);

			if(minLengthListToLeft[1] < minLengthListToRight[1])
			{
				choose = 1;
				mainPositionLeft.add(timeLastGrab, centerList.get(minLengthListToLeft[0]), getDepth(centerList.get(minLengthListToLeft[0])), 0);
				centerList.remove(minLengthListToLeft[0]);
			}
			else
			{
				choose = 2;
				mainPositionRight.add(timeLastGrab, centerList.get(minLengthListToRight[0]), getDepth(centerList.get(minLengthListToRight[0])), 0);
				centerList.remove(minLengthListToRight[0]);
			}

			if(centerList.size() == 1)
			{
				if(choose == 1)
				{
					mainPositionRight.add(timeLastGrab, centerList.get(0), getDepth(centerList.get(0)), 0);
				}
				else
				{
					mainPositionLeft.add(timeLastGrab, centerList.get(0), getDepth(centerList.get(0)), 0);
				}
			}
			else if(centerList.size() > 1)
			{
				if(choose == 1)
				{
					lengthListToRight = new double[centerList.size()];
					
					for(int i = 0; i < centerList.size(); i++)
					{
						lengthListToRight[i] = getLenght(centreRight[1], centreRight[2], centerList.get(i).x(), centerList.get(i).y());
					}

					minLengthListToRight = getMinList(lengthListToRight);

					mainPositionRight.add(timeLastGrab, centerList.get(minLengthListToRight[0]), getDepth(centerList.get(minLengthListToRight[0])), 0);
				}
				else
				{
					lengthListToLeft = new double[centerList.size()];

					for(int i = 0; i < centerList.size(); i++)
					{
						lengthListToLeft[i] = getLenght(centreLeft[1], centreLeft[2], centerList.get(i).x(), centerList.get(i).y());
					}

					minLengthListToLeft = getMinList(lengthListToLeft);

					mainPositionLeft.add(timeLastGrab, centerList.get(minLengthListToLeft[0]), getDepth(centerList.get(minLengthListToLeft[0])), 0);
				}					
			}
		}
    }

    public void getPositionFiltreHand()
    {
    	if(mainPositionLeft.get(0)[0] == timeLastGrab)
    	{
    		double[] result = filtreLeft.filter(mainPositionLeft.get(0)[1], mainPositionLeft.get(0)[2]);

    		mainPositionFiltreLeft.add(timeLastGrab, new CvPoint((int)result[0], (int)result[1]), mainPositionLeft.get(0)[3], 0);
    		mainPositionFiltreLeft.computeDerivees();
    	}

    	if(mainPositionRight.get(0)[0] == timeLastGrab)
    	{
    		double[] result = filtreRight.filter(mainPositionRight.get(0)[1], mainPositionRight.get(0)[2]);

    		mainPositionFiltreRight.add(timeLastGrab, new CvPoint((int)result[0], (int)result[1]), mainPositionRight.get(0)[3], 0);
    		mainPositionFiltreRight.computeDerivees();
    	}
    }

    private int ct1 = 0;
    private int ct2 = 0;
    private int ct3 = 0;
    private long timeOrigin = System.currentTimeMillis();

    public void reconnaissanceDeMvt()
    {
    	for(int i = 0; i < 19; i++)
    	{
    		long[] position = mainPositionFiltreLeft.get(i);
    		long[] derivee = mainPositionFiltreLeft.getDerivee(i);

    		if(position[0] > timeOrigin)
    		{
	    		if(derivee[1] < 0.1 && derivee[2] < 0.1) //stable en x, y
	    		{
	    			ct1 += 1;
	    		}
	    		else
	    		{
	    			ct1 = 0;
	    		}

	    		if(ct1 > 8)
	    		{
	    			int dz = 0;
	    			
	    			for(int j = 0; j <= i; j++)
	    			{
	    				dz += mainPositionFiltreLeft.getDerivee(j)[3];
	    			}
	    			
	    			if(dz > 10)
	    			{
	    				timeOrigin = timeLastGrab;
	    				ct1 = 0;
	    				System.out.println("pause");
	    			}
	    		}
    		}
    		else
    		{
    			break;
    		}
    	}
    }

    public CvPoint getContourCenter(CvSeq contour, CvMemStorage storage)
    {
    	CvBox2D box = cvMinAreaRect2(contour, storage);

    	return new CvPoint((int)box.center().x(), (int)box.center().y());
    }

    public int getDepth(CvPoint point)
    {
    	return OpenCV2.getUnsignedByte(imageTraitement.getByteBuffer(), point.x() + imageTraitement.width()*point.y());
    }

    public double getLenght(long x1, long y1, int x2, int y2)
    {
    	return Math.sqrt(((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)));
    }

    public int[] getMinList(double[] list)
    {
    	double min = list[0];
    	int index = 0;

    	for(int i = 1; i < list.length; i++)
    	{
    		if(list[i] < min)
    		{
    			min = list[i];
    			index = i;
    		}
    	}

    	int[] r = {index, (int)min};

    	return r;
    }
}