package Kinect;

class LowPassFilter
{   // this class is used to implement the low pass filter
	
	private double x, X;
	private boolean initialized = false;

	
	// this constructor initializes the variable double alpha which is a smoothing factor ]0,1]
	public LowPassFilter(double alpha)
	{
	    x = X = 0;
	}

	
	// this constructor implements the filter using the equation
	public double filter(double value, double alpha) // value is the raw, X is the filtered data
	{
	    if(initialized)
	    {
	        X = alpha * value + (1 - alpha) * X; /* alpha*value is the contribution of the new input data
	        										(1-alpha)*X adds inertia frome the previous value */
	    }
	    else
	    {
	        X = value;
	        initialized = true;
	    }

	    x = value;

	    return X; // this is the new filtered data
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

