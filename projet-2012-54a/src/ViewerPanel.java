
// ViewerPanel.java
// Andrew Davison, August 2011, ad@fivedots.psu.ac.th
// Version 2; copy to parent directory to use with OpenNIViewer.java

/* Based on OpenNI's SimpleViewer example
     Initialize OpenNI *without* using an XML file;
     Display a grayscale depth map (darker means further away, although black
     means "too close" for a depth value to be calculated).
*/

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.text.DecimalFormat;

import org.OpenNI.*;

import java.nio.ShortBuffer;


public class ViewerPanel extends JPanel implements Runnable
{
  private static final int MAX_DEPTH_SIZE = 10000;  

  // image vars
  private byte[] imgbytes;
  private BufferedImage image = null;   // for displaying the depth image
  private int imWidth, imHeight;
  private float histogram[];       // for the depth values
  private int maxDepth = 0;      // largest depth value

  private volatile boolean isRunning;
  
  // used for the average ms processing information
  private int imageCount = 0;
  private long totalTime = 0;
  private DecimalFormat df;
  private Font msgFont;

  // OpenNI
  private Context context;
  private DepthMetaData depthMD;


  public ViewerPanel()
  {
    setBackground(Color.WHITE);

    df = new DecimalFormat("0.#");  // 1 dp
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    configOpenNI();

    histogram = new float[MAX_DEPTH_SIZE];

    imWidth = depthMD.getFullXRes();
    imHeight = depthMD.getFullYRes();
    System.out.println("Image dimensions (" + imWidth + ", " +
                                              imHeight + ")");
    // create empty image object of correct size and type
    imgbytes = new byte[imWidth * imHeight];
    image = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_BYTE_GRAY);

    new Thread(this).start();
  }


private void configOpenNI()
{
	try
	{
		context = new Context();    
		context.addLicense(new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=")); 
		  
		DepthGenerator depthGenerator = DepthGenerator.create(context);
		depthGenerator.setMapOutputMode(new MapOutputMode(640, 480, 30)); 
		
		context.setGlobalMirror(true);
		
		context.startGeneratingAll(); 
		System.out.println("Started context generating..."); 

		depthMD = depthGenerator.getMetaData();
	}
	catch(GeneralException e)
	{
		e.printStackTrace();
	} 
}



  public Dimension getPreferredSize()
  { return new Dimension(imWidth, imHeight); }


  public void run()
  {
    isRunning = true;
    while(isRunning)
    {
      try {
        context.waitAnyUpdateAll();
      }
      catch(StatusException e)
      {  System.out.println(e); 
         System.exit(1);
      }

      long startTime = System.currentTimeMillis();
      updateDepthImage();
      imageCount++;
      totalTime += (System.currentTimeMillis() - startTime);
      repaint();
    }

    // close down
    try {
      context.stopGeneratingAll();
    }
    catch (StatusException e) {}
    context.release();
    System.exit(1);
  }  // end of run()


  public void closeDown()
  {  isRunning = false;  } 



  private void updateDepthImage()
  {
    ShortBuffer depthBuf = depthMD.getData().createShortBuffer();
    calcHistogram(depthBuf);
    depthBuf.rewind();

    while(depthBuf.remaining() > 0)
    {
      int pos = depthBuf.position();
      short depth = depthBuf.get();
      imgbytes[pos] = (byte) histogram[depth];
    }
  }  // end of updateDepthImage()



  private void calcHistogram(ShortBuffer depthBuf)
  {
    // reset histogram[]
    for (int i = 0; i <= maxDepth; i++)
      histogram[i] = 0;

    // record number of different depths in histogram[]
    int numPoints = 0;
    maxDepth = 0;
    while (depthBuf.remaining() > 0) {
      short depthVal = depthBuf.get();
      if (depthVal > maxDepth)
        maxDepth = depthVal;
      if ((depthVal != 0)  && (depthVal < MAX_DEPTH_SIZE)){    // skip histogram[0]
        histogram[depthVal]++;
        numPoints++;
      }
    }
    // System.out.println("No. of numPoints: " + numPoints);
    // System.out.println("Maximum depth: " + maxDepth);

    // convert into a cummulative depth count (skipping histogram[0])
    for (int i = 1; i <= maxDepth; i++)
      histogram[i] += histogram[i-1];

    /* convert cummulative depth into 8-bit range (0-255), which will later become grayscales
        - darker means further away, although black
          means "too close" for a depth value to be calculated).
    */
    if (numPoints > 0) {
      for (int i = 1; i <= maxDepth; i++)   // skipping histogram[0]
        histogram[i] = (int) (256 * (1.0f - (histogram[i] / (float) numPoints)));
    }
  }  // end of calcHistogram()



  public void paintComponent(Graphics g)
  // Draw the depth image and statistics info
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    // convert image pixel array into an image
    DataBufferByte dataBuffer = new DataBufferByte(imgbytes, imWidth * imHeight);
    Raster raster = Raster.createPackedRaster(dataBuffer, imWidth, imHeight, 8, null);
    image.setData(raster);
    if (image != null)
      g2.drawImage(image, 0, 0, this);

    writeStats(g2);
  } // end of paintComponent()




  private void writeStats(Graphics2D g2)
  /* write statistics in bottom-left corner, or
     "Loading" at start time */
  {
    g2.setColor(Color.BLUE);
    g2.setFont(msgFont);
    int panelHeight = getHeight();
    if (imageCount > 0) {
      double avgGrabTime = (double) totalTime / imageCount;
      g2.drawString("Pic " + imageCount + "  " +
                   df.format(avgGrabTime) + " ms", 
                   5, panelHeight-10);  // bottom left
    }
    else  // no image yet
      g2.drawString("Loading...", 5, panelHeight-10);
  }  // end of writeStats()


} // end of ViewerPanel class

