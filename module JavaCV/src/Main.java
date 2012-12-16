import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;

import javax.swing.JFrame;
import java.awt.GridLayout;
import java.nio.ByteBuffer;

public class Main
{
    public static void main(String[] args) throws Exception
    {
    	OpenCVFrameGrabber grabber = new OpenCVFrameGrabber("depth_pact54_test1.mkv");
		grabber.start();

		IplImage imageGrab = grabber.grab();
		IplImage imageTraitement;
		int width  = imageGrab.width();
		int height = imageGrab.height();
		long timeBegin;
		long timeEnd;
    	CvPoint minPoint = new CvPoint();
    	CvPoint maxPoint = new CvPoint();
    	double[] minVal = new double[1];
    	double[] maxVal = new double[1];
    	CvSeq contour;

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
			
			timeBegin = System.currentTimeMillis();
			
			imageTraitement = IplImage.create(width, height, IPL_DEPTH_8U, 1); 
         	cv2CvtColor(imageGrab, imageTraitement, CV_RGB2GRAY);
 //       	cvCvtColor(imageGrab, imageTraitement, CV_RGB2GRAY);
//         	cv2Smooth(imageTraitement, imageTraitement, CV_GAUSSIAN, 9, 9, 1, 0);
        	cvSmooth(imageTraitement, imageTraitement, CV_GAUSSIAN, 9, 9, 1, 0);
        	cv2MinMaxLoc(imageTraitement, minVal, maxVal, minPoint, maxPoint, null);
 //       	cvMinMaxLoc(imageTraitement, minVal, maxVal, minPoint, maxPoint, null);
        	cv2Threshold(imageTraitement, imageTraitement, minVal[0] + 15, 255, CV_THRESH_BINARY);
  //      	cvThreshold(imageTraitement, imageTraitement, minVal[0] + 15, 255, CV_THRESH_BINARY);


        	cvCircle(imageGrab, minPoint, 3, CvScalar.YELLOW, -1, 8, 0);

        	contour = new CvSeq();
  //      	cvFindContours(imageTraitement.clone(), storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
        	contour = cv3FindContours(imageTraitement, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

            while(contour != null && !contour.isNull())
            {
            	if(contour.elem_size() > 0)
                {
                	//double aire = cvContourArea(contour, CV_WHOLE_SEQ, 0);
                	double aire = cv2ContourArea(contour, CV_WHOLE_SEQ, 0);

        	    	System.out.println(aire+"->"+contour.total());

                	if(aire > 50 && aire < 10000)
                	{
                		//cvDrawContours(imageGrab, contour, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
	                	cv2DrawContours(imageGrab, contour, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
 
                	    CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.015, 0);
                		cvDrawContours(imageGrab, points, CvScalar.GREEN, CvScalar.GREEN, -1, 1, CV_AA);

                		CvSeq convex = cvConvexHull2(contour, storage, CV_COUNTER_CLOCKWISE, 1);
                		cvDrawContours(imageGrab, convex, CvScalar.RED, CvScalar.RED, -1, 1, CV_AA);

                		CvPoint centre1 = getContourCenter1(convex, storage);
                		cvCircle(imageGrab, centre1, 3, CvScalar.RED, -1, 8, 0);

                		//panneau3.add((cvGet2D(frame, centre1.y(), centre1.x()).getVal(0)));


                		/*CvPoint[] coordonne = new CvPoint[points.total()];

                		for(int i = 0; i < points.total(); i++)
                		{
                			coordonne[i] = new CvPoint(cvGetSeqElem(points, i));
                		}
                		
                		for(int i = 0; i < points.total(); i++)
                		{
                			double angle;
                			
                			if(i == 0){
                				angle = getAngle(coordonne[points.total()-1], coordonne[i], coordonne[i+1]);
                			}
                			else if(i == points.total()-1){
                				angle = getAngle(coordonne[i-1], coordonne[i], coordonne[0]);
                			}
                			else{
                				angle = getAngle(coordonne[i-1], coordonne[i], coordonne[i+1]);
                			}

                			if(angle < 90)
                			{
                				cvCircle(imageGrab, coordonne[i], 3, CvScalar.WHITE, -1, 8, 0);
                			}
                		}*/
                	}
                }
                contour = contour.h_next();
            }

			fenetreFrame1.showImage(imageTraitement);
			fenetreFrame2.showImage(imageGrab);

			cvClearMemStorage(storage);

			timeEnd = 100-(int)(System.currentTimeMillis()-timeBegin);
			
			if(timeEnd > 0)
			{
				Thread.sleep(timeEnd);
			}
			
			System.out.println(-timeEnd+100+" ms");
	    }

		grabber.stop();
		fenetreFrame1.dispose();
		fenetreFrame2.dispose();
    }

    public static CvPoint getContourCenter1(CvSeq contour, CvMemStorage storage)
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
}
