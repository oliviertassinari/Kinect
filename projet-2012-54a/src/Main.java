import com.googlecode.fannj.Fann;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;

import javax.swing.JFrame;

import java.awt.GridLayout;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Main
{
    public static void main(String[] args) throws Exception
    {
    	//System.setProperty("jna.library.path", "E:/Programation/Java/Bibliotheque/fann/bin/");

    	//System.out.println( System.getProperty("jna.library.path") ); //maybe the path is malformed
    	//File file = new File(System.getProperty("jna.library.path") + "fannfloat.dll");
    	//System.out.println("Is the dll file there:" + file.exists());

    	//System.load("E:/Programation/Java/Bibliotheque/fann/bin/fannfloat.dll");

    	//System.out.println(System.getProperty("user.dir"));

    	System.load(System.getProperty("user.dir")+"\\lib\\fannfloat.dll"); 

        Fann fann = new Fann("ann/geste.net");

        int timeUp = 0;
        String textUp = "";

    	//OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("video/depth_pact54_test1.mkv");
    	//OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("video/depth_pact54_test2.mpg");
    	OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("video/depth_pact42_test1.mkv");
		grabber.start();

		IplImage imageGrab = grabber.grab();
		int width  = imageGrab.width();
		int height = imageGrab.height();
    	CvPoint minPoint = new CvPoint();
    	CvPoint maxPoint = new CvPoint();
    	double[] minVal = new double[1];
    	double[] maxVal = new double[1];

    	MainPosition mainPosition = new MainPosition();
    	MainPosition mainPositionLeft = new MainPosition();
    	MainPosition mainPositionRight = new MainPosition();

    	JFrame Fenetre = new JFrame();
		Fenetre.setLayout(new GridLayout(1, 2));
		Fenetre.setTitle("module JavaCV");
		Fenetre.setSize(width*2+20, height);
		Fenetre.setLocationRelativeTo(null);
		Fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/*creation de la fenetre utilisée pour l'affichage de la video. L'objet CanvasFrame en JavaCV peut utiliser
		l'accélération materielle pour afficher les vidéos, profitons-en ! */
		CanvasFrame fenetreFrame1 = new CanvasFrame("AVI Playback Demo");
		fenetreFrame1.setVisible(false);
		Fenetre.getContentPane().add(fenetreFrame1.getCanvas());

		CanvasFrame fenetreFrame2 = new CanvasFrame("AVI Playback Demo");
		fenetreFrame2.setVisible(false);
		Fenetre.getContentPane().add(fenetreFrame2.getCanvas());

		Fenetre.setVisible(true);

		CvMemStorage storage = CvMemStorage.create();

		while((imageGrab = grabber.grab()) != null)
		{
			  /*informations pratiques:
			  les images en couleurs sont la plupart du temps représentées sous forme d'un ensemble de 
			  valeurs codées sur 8 bits, représentant par exemple l'intensité de rouge, vert et bleu d'un pixel
			  
			  Travail biblio #1: formats de représentation des images dans le domaine non-compréssé: RGB, YYV, ...
			  
			  on suppose dans cet exemple que le format de l'image est RGB non compréssé, échantillonné sur 24 bits. 
			  Chaque pixel de l'image est représenté par 24 bits donnant dans l'ordre la composante Bleu, Verte et Rouge du pixel 
			  (l'inversion bleu/rouge est lié au format AVI non compréssé)
			  L'image est alors représentée en mémoire comme une succession de pixels, stockés ligne par ligne, comme suit :
			    en bits: 
			  BBBBBBBBGGGGGGGGRRRRRRRRBBBBBBBBGGGGGGGGRRRRRRRRBBBBBBBBGGGGGGGGRRRRRRRRBBBBBBBBGGGGGGGGRRRRRRRR ....
			    ou en octets:
			  BGRBGRBGR ....
			  
			  afin de manipuler les pixels de l'image, nous devons
			  1- récupérer les données de pixels présents dans la mémoire
			  2- localiser le pixel
			*/
			
			/*1 - on récupére la mémoire où sont stoqués les pixels*/
			//ByteBuffer rgb_data = imageTraitement.getByteBuffer();
			
			/*2 - La mémoire des pixels étant construite de manière linéaire, afin de sauter une ligne entière de pixels 
			il faut aller width pixels plus loin. Comme chaque pixel est codé sur 1 octet, cela revient à 
			sauter 3*width pixels. Cette valeur s'appelle "stride" ou "pitch" de l'image", et est aussi acessible 
			dans la fonction IplImage.widthStep()
			
			Note: certains formats d'images utilisent un pitch supérieur à la largeur des images en pixels (pour
			d'obscures raisons qui ne nous concernent pas dans ce module), vous ne devriez pas avoir à voue en occuper.
			
			Le même raisonement vaut pour le déplacement horizontal de pixel.
			
			    Par exemple, pour accéder accéder au pixel 30 horizontal et 20 vertical:
			*/
			//int pixel_index = 3*30 + 3*width*20;
			
			/*3 - lire la composante bleu de notre pixel*/
			//int blue_value = rgb_data.get(pixel_index);
			/*ATTENTION!! Pour Java, le type Byte est toujours signé, donc une valeur non signée dans l'image de 200 est 
			en fait lue par cette fonction comme 200 = 0b11001000 -> signé (bit de poid fort mis)
			-> 0b11111111 - 0b11001000 = -55  !!!
			Ceci est propre à Java et à la manipulation des pixels via des bytes 
			Vous n'aurez pas le même problème dans d'autres langages ou en utilisant les autres "buffer" fourni
			par l'objet IplImage, mais ces methodes sont moins rapides. 
			
			La correction s'écrit donc:
			*/
			//if (blue_value < 0 ) blue_value = 255 + blue_value;
			
			/*4 - mettre la composante verte à 0 pour ce même pixel (ou +2 pour la composante rouge */
			//rgb_data.put(pixel_index + 1, (byte) 0);
			
			
			 /*Exercices: 
			  0- (optionnel mais amusant) utilisez la WebCam au lieu d'un fichier video
			  1- saturez la composante ROUGE de chaque image à pleine intensité
			  2- saturez la composante bleu du quart inférieur droit de l'image à pleine intensité
			  3- effectuez les mêmes manipulations avec deux CanvasFrame, l'un pour l'image originale, l'autre pour l'image modifiée
			      Regardez la documentation d'IplImage pour créer une nouvelle image vide
			  4- seuillez les pixels à noir si l'intensité du vert dépasse 50%
			  5- identifiez les pixels qui peuvent faire partie d'une zone de la peau (ou autre critère) et effacez tous les autres pixels
			  6- identifiez les différentes régions dans l'exercice précédent, et déssinez un rectangle de couleur autour de chaque région
			
			  Travail biblio #2: quels outils pour la detection de région (segmentation par région) ? 
			
			 NOTE: pour ces exercices, vous devrez coder les algorithmes vous-memes (e.g. interdit d'utilser cvFindContour ...)
			 une fois l'algorithme codé, vous avez le droit d'utiliser la version OpenCV equivalente si elle existe afin 
			 de comparer les performances de vos algorithmes avec JavaCV
			 
			 Votre code sera bien entendu rendu disponible sous le GIT de votre projet pact, et vous aurez la sympathique attention de 
			 prévenir votre expert JavaCV de l'endroit où il peut trouver le code.
			 */

			//Exercie 1
			/*ByteBuffer imageGrabBuffer = imageGrab.getByteBuffer();
    		for(int x = 0; x < width; x++)
    		{
    			for(int y = 0; y < height; y++)
    			{
    				imageGrabBuffer.put(3*x + 3*width*y, (byte) 255);
    			}
    		}*/
			
			//Exercice 2
			/*ByteBuffer imageGrabBuffer = imageGrab.getByteBuffer();
    		for(int x = (int)(width/2); x < width; x++)
    		{
    			for(int y = (int)(height/2); y < height; y++)
    			{
    				imageGrabBuffer.put(3*x + 3*width*y + 2, (byte) 255);
    			}
    		}*/

			//Exercice 4
			/*ByteBuffer imageGrabBuffer = imageGrab.getByteBuffer();
    		for(int x = 0; x < width; x++)
    		{
    			for(int y = 0; y < height; y++)
    			{
    				if(getUnsignedByte(imageGrabBuffer, 3*x + 3*width*y + 1) > (int)(255/2))
    				{
    					imageGrabBuffer.put(3*x + 3*width*y, (byte) 255);
    					imageGrabBuffer.put(3*x + 3*width*y + 1, (byte) 255);
    					imageGrabBuffer.put(3*x + 3*width*y + 2, (byte) 255);
    				}
    			}
    		}*/	
			
			//Exercice 5 - 6

			long timeBegin = System.currentTimeMillis();

			IplImage imageTraitement = IplImage.create(width, height, IPL_DEPTH_8U, 1);
//        	cv2CvtColor(imageGrab, imageTraitement, CV_RGB2GRAY);
        	cvCvtColor(imageGrab, imageTraitement, CV_RGB2GRAY);

//        	cv2LUT(imageTraitement, imageTraitement);

//         	cv2Smooth(imageTraitement, imageTraitement, CV_GAUSSIAN, 9, 9, 1, 0);
        	cvSmooth(imageTraitement, imageTraitement, CV_GAUSSIAN, 9, 9, 1, 0);

        	IplImage imageThreshold = imageTraitement.clone();

//        	cv2MinMaxLoc(imageTraitement, minVal, maxVal, minPoint, maxPoint, null);
        	cvMinMaxLoc(imageTraitement, minVal, maxVal, minPoint, maxPoint, null);

        	int isFind = 1;
        	ArrayList<CvPoint> centerList = new ArrayList<CvPoint>();

        	while(isFind != 0 && isFind < 3) //2 it�rations max
        	{
        		isFind++;

	//        	cv2Threshold(imageTraitement, imageThreshold, minVal[0] + 15*isFind, 255, CV_THRESH_BINARY);
	        	cvThreshold(imageTraitement, imageThreshold, minVal[0] + 15*isFind, 255, CV_THRESH_BINARY);

	        	cvCircle(imageGrab, minPoint, 3, CvScalar.YELLOW, -1, 8, 0);

	        	CvSeq contour = new CvSeq();
	         	cvFindContours(imageThreshold.clone(), storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
	 //       	contour = cv3FindContours(imageTraitement, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
	         	
	            while(contour != null && !contour.isNull())
	            {
	            	if(contour.elem_size() > 0)
	                {
	            		double aire = cvContourArea(contour, CV_WHOLE_SEQ, 0);
	 //               	double aire = cv2ContourArea(contour, CV_WHOLE_SEQ, 0);

	                	if(aire > 50 && aire < 100000)
	                	{
	                		isFind = 0; //true

	                		cvDrawContours(imageGrab, contour, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
	//	                	cv2DrawContours(imageGrab, contour, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);

	                	    CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.015, 0);
	                		cvDrawContours(imageGrab, points, CvScalar.GREEN, CvScalar.GREEN, -1, 1, CV_AA);

	                		CvSeq convex = cvConvexHull2(contour, storage, CV_COUNTER_CLOCKWISE, 1);
	                		cvDrawContours(imageGrab, convex, CvScalar.RED, CvScalar.RED, -1, 1, CV_AA);

	                		CvPoint centre = getContourCenter(convex, storage);
	                		cvCircle(imageGrab, centre, 3, CvScalar.RED, -1, 8, 0);

	                		CvSeq hull = cvConvexHull2(contour, storage, CV_COUNTER_CLOCKWISE, 0);
	                		CvSeq defect = cvConvexityDefects(contour, hull, storage);

	                		while(defect != null)
	                		{
	                    		for(int i = 0; i < defect.total(); i++)
	                    		{
	                    			 CvConvexityDefect convexityDefect = new CvConvexityDefect(cvGetSeqElem(defect, i));

	                    			 if(convexityDefect.depth() > 10)
	                    			 {
		                    			 cvCircle(imageGrab, convexityDefect.start(), 3, CvScalar.MAGENTA, -1, 8, 0);
		                    			 cvCircle(imageGrab, convexityDefect.end(), 3, CvScalar.CYAN, -1, 8, 0);
		                    			 cvCircle(imageGrab, convexityDefect.depth_point(), 3, CvScalar.WHITE, -1, 8, 0);

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

        	long[] center = mainPosition.get(0);
			long[] centreLeft = mainPositionLeft.get(0);
			long[] centreRight = mainPositionRight.get(0);

			if(centreLeft[0] == 0 && centreRight[0] == 0) //Vide
			{
				if(centerList.size() == 1) //1 centre d�tect�
				{
					CvPoint centre = centerList.get(0);
					mainPosition.add(timeBegin, centre, getDepth(imageTraitement, centre));
				}
				else
				{
	    			CvPoint centre1 = centerList.get(0);
	    			CvPoint centre2 = centerList.get(1);

        			if(centre2.x() > centre1.x()) //center1 : left
        			{
        				mainPositionLeft.add(timeBegin, centre1, getDepth(imageTraitement, centre1));
        				mainPositionRight.add(timeBegin, centre2, getDepth(imageTraitement, centre2));
        				
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
        				mainPositionLeft.add(timeBegin, centre2, getDepth(imageTraitement, centre2));
        				mainPositionRight.add(timeBegin, centre1, getDepth(imageTraitement, centre1));	
        				
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
					mainPositionLeft.add(timeBegin, centerList.get(minLengthListToLeft[0]), getDepth(imageTraitement, centerList.get(minLengthListToLeft[0])));
					centerList.remove(minLengthListToLeft[0]);
				}
				else
				{
					choose = 2;
    				mainPositionRight.add(timeBegin, centerList.get(minLengthListToRight[0]), getDepth(imageTraitement, centerList.get(minLengthListToRight[0])));
    				centerList.remove(minLengthListToRight[0]);
				}

				if(centerList.size() == 1)
				{
					if(choose == 1)
					{
						mainPositionRight.add(timeBegin, centerList.get(0), getDepth(imageTraitement, centerList.get(0)));
					}
					else
					{
						mainPositionLeft.add(timeBegin, centerList.get(0), getDepth(imageTraitement, centerList.get(0)));
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

						mainPositionRight.add(timeBegin, centerList.get(minLengthListToRight[0]), getDepth(imageTraitement, centerList.get(minLengthListToRight[0])));
					}
					else
					{
						lengthListToLeft = new double[centerList.size()];

						for(int i = 0; i < centerList.size(); i++)
						{
							lengthListToLeft[i] = getLenght(centreLeft[1], centreLeft[2], centerList.get(i).x(), centerList.get(i).y());
						}

						minLengthListToLeft = getMinList(lengthListToLeft);

						mainPositionLeft.add(timeBegin, centerList.get(minLengthListToLeft[0]), getDepth(imageTraitement, centerList.get(minLengthListToLeft[0])));
					}					
				}
			}

			CvFont font = new CvFont(CV_FONT_HERSHEY_COMPLEX, 0.6, 1); 
			
			if(mainPositionLeft.get(0)[0] == timeBegin)
			{
				cvPutText(imageGrab, "Gauche", cvPoint((int)mainPositionLeft.get(0)[1]-20, (int)mainPositionLeft.get(0)[2]-10), font, CvScalar.BLUE);
			}
			if(mainPositionRight.get(0)[0] == timeBegin)
			{
				cvPutText(imageGrab, "Droite", cvPoint((int)mainPositionRight.get(0)[1]-20, (int)mainPositionRight.get(0)[2]-10), font, CvScalar.RED);
			}

			String out = "";
			long[] positionLast = mainPositionLeft.get(19);

			for(int i = 0; i < 19; i++)
			{
	    		long[] position = mainPositionLeft.get(i);
	    		long[] position2 = mainPositionLeft.get(i+1);

	    		out += ((float)(position[1]-position2[1])/400)+" "+((float)(position[2]-position2[2])/400)+" "+((float)(position[3]-position2[3])/400)+" "+((float)i/18)+" ";
			}

			//System.out.println(out);

	        float[] inputs = new float[76];

			for(int i = 0; i < 19; i++)
			{
	    		long[] position = mainPositionLeft.get(i);
	    		long[] position2 = mainPositionLeft.get(i+1);

				inputs[4*i] = (float)(position[1]-position2[1])/400;
				inputs[4*i+1] = (float)(position[2]-position2[2])/400;
				inputs[4*i+2] = (float)(position[3]-position2[3])/400;
				inputs[4*i+3] = (float)i/18;
			}

			float[] outputs = fann.run(inputs);

	        //System.out.println(outputs[0] +" "+ outputs[1]+ " "+outputs[2]);

			if(positionLast[0] != 0)
			{
		        if(outputs[0] > 0.9)
		        {
		        	textUp = "geste main gauche : en bas";
		        	timeUp = 0;
		        }
		        else if(outputs[1] > 0.9)
		        {
		        	textUp = "geste main gauche : a gauche";
		        	timeUp = 0;
		        }
		        else if(outputs[2] > 0.9)
		        {
		        	textUp = "geste main gauche : en bas a gauche";
		        	timeUp = 0;
		        }
			}

	        if(textUp != "" &&  timeUp < 800)
	        {
	        	timeUp += 100;
	        	cvPutText(imageGrab, textUp, cvPoint(20, 20), font, CvScalar.BLACK);
	        }
			
			fenetreFrame1.showImage(imageThreshold);
			fenetreFrame2.showImage(imageGrab);

			cvClearMemStorage(storage);
			
			long timeEnd = 100-(int)(System.currentTimeMillis()-timeBegin);

			if(timeEnd > 0)
			{
				Thread.sleep(timeEnd);
			}

			System.out.println(-timeEnd+100+" ms");
	    }

        fann.close();

		grabber.stop();
		fenetreFrame1.dispose();
		fenetreFrame2.dispose();
    }

    public static CvPoint getContourCenter(CvSeq contour, CvMemStorage storage)
    {
    	CvBox2D box = cvMinAreaRect2(contour, storage);

    	return new CvPoint((int)box.center().x(), (int)box.center().y());
    }
    
    public static void cv2CvtColor(IplImage src, IplImage dst, int code)
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
    
    public static void cv2MinMaxLoc(IplImage src, double[] minVal, double[] maxVal, CvPoint minPoint, CvPoint maxPoint, IplImage mask)
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
    
    public static void cv2Threshold(IplImage src, IplImage dst, double threshold, double maxValue, int thresholdType)
    {
    	if(thresholdType == CV_THRESH_BINARY)
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

    public static void cv2Smooth(IplImage src, IplImage dst, int smoothtype, int param1, int param2, double param3, double param4)
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
    				if(isOnContour(srcBuffer, width, height, x, y) && pixelMarque[x][y] == 0) //Pixel sur un contour et non marqu�
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

	public static double cv2ContourArea(CvSeq contour, CvSlice slice, int mode)
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

    public static void cv2LUT(IplImage src, IplImage dst)
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
				dstBuffer.put(pixelIndex, (byte)(255 - getUnsignedByte(srcBuffer, pixelIndex)));
			}
		}
    }

    public static void cv2DrawContours(IplImage src, CvSeq contour, CvScalar external_color, CvScalar hole_color,  int max_level,  int thickness, int lineType)
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

    public static double getAngle(CvPoint point0, CvPoint pointCentre, CvPoint point1)
    {
		double p0c = Math.sqrt(Math.pow(pointCentre.x()-point0.x(),2) + Math.pow(pointCentre.y()-point0.y(),2)); // p0->c (b)   
		double p1c = Math.sqrt(Math.pow(pointCentre.x()-point1.x(),2) + Math.pow(pointCentre.y()-point1.y(),2)); // p1->c (a)
		double p0p1 = Math.sqrt(Math.pow(point1.x()-point0.x(),2) + Math.pow(point1.y()-point0.y(),2)); // p0->p1 (c)

		return Math.acos((p1c*p1c+p0c*p0c-p0p1*p0p1)/(2*p1c*p0c))*(180/Math.PI);
    }

    public static int getDepth(IplImage src, CvPoint point)
    {
    	return getUnsignedByte(src.getByteBuffer(), point.x() + src.width()*point.y());
    }

    public static double getLenght(long x1, long y1, int x2, int y2)
    {
    	return Math.sqrt(((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)));
    }

    public static int[] getMinList(double[] list)
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
