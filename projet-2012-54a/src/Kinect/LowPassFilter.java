package Kinect;

class LowPassFilter
{
	private double y, s;
	private double alpha;
	private boolean initialized = false;

	public LowPassFilter(double alpha)
	{
	    y = s = 0;
	    this.alpha = alpha;
	}

	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}

	public double filter(double value)
	{
	    double result;
	 
	    if(initialized)
	    {
	        result = alpha * value + (1.0 - alpha) * s;
	    }
	    else
	    {
	        result = value;
	        initialized = true;
	    }

	    y = value;
	    s = result;

	    return result;
	}

	public double filterWithAlpha(double value, double alpha)
	{
		this.alpha = alpha;
	    return filter(value);
	}

	public boolean hasLastRawValue()
	{
	    return initialized;
	}

	public double lastRawValue()
	{
	    return y;
	}
}

