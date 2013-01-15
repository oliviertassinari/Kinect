package Kinect;

class LowPassFilter
{
	private double x, X;
	private boolean initialized = false;

	public LowPassFilter(double alpha)
	{
	    x = X = 0;
	}

	public double filter(double value, double alpha)
	{
	    if(initialized)
	    {
	        X = alpha * value + (1 - alpha) * X;
	    }
	    else
	    {
	        X = value;
	        initialized = true;
	    }

	    x = value;

	    return X;
	}

	public boolean isInitialized()
	{
	    return initialized;
	}

	public double lastRawValue()
	{
	    return x;
	}
}

