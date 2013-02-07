package Kinect;


public class reconnaissanceMvt
{
	private MainPosition mainPosition;
	private String side;
    private int ct1;
    private int ct2;
    private long timeOrigin;

    public reconnaissanceMvt(MainPosition mainPosition, String side)
    {
    	this.mainPosition = mainPosition;
    	this.side = side;
    	ct1 = 0;
    	ct2 = 0;
    	timeOrigin = System.currentTimeMillis();
    }

    public void compute(long timeLastGrab)
    {
    	long[] positionCurrent = mainPosition.getFiltre(0);

    	for(int i = 0; i < 19; i++)
    	{
    		long[] position = mainPosition.getFiltre(i);

    		if(mainPosition.get(i)[0] > timeOrigin)
    		{
    			if(Math.abs(position[0]-positionCurrent[0]) < 40 && Math.abs(position[1]-positionCurrent[1]) < 40) //stable en x, y
	    		{
	    			ct1 += 1;
	    		}
	    		else
	    		{
	    			ct1 = 0;
	    		}

    			if(Math.abs(position[0]-positionCurrent[0]) < 40 && Math.abs(position[2]-positionCurrent[2]) < 40) //stable en x, z
    			{
    				ct2 += 1;
    			}
	    		else
	    		{
	    			ct2 = 0;
	    		}

	    		if(ct1 > 20)
	    		{
	    			float dz = 0;

	    			for(int j = 0; j <= i; j++)
	    			{
	    				dz += mainPosition.getDerivee(j)[2]; //z
	    			}

	    			if(dz < -0.25 && position[2] - positionCurrent[2] > 4)
	    			{
	    				timeOrigin = timeLastGrab;
	    				ct1 = 0;
	    				ct2 = 0;
	    				System.out.println(side+" pause "+dz+" "+ (position[2] - positionCurrent[2]));
	    			}
	    		}

	    		if(ct2 > 20)
	    		{
	    			float dy = 0;

	    			for(int j = 0; j <= i; j++)
	    			{
	    				dy += mainPosition.getDerivee(j)[1];
	    			}

	    			if(Math.abs(dy) > 3)
	    			{
	    				timeOrigin = timeLastGrab;
	    				ct1 = 0;
	    				ct2 = 0;
	    				System.out.println(side+" volume "+dy);
	    			}
	    		}
    		}
    		else
    		{
    			break;
    		}
    	}
    }
}
