package Kinect;

public class OneEuroFilter
{
    private double sampleRate;
    private double minFreqCut;
    private double beta;
    private double dFreqCut;
    private LowPassFilter x;
    private LowPassFilter dx;
    private LowPassFilter y;
    private LowPassFilter dy;

    public OneEuroFilter(double sampleRate, double minFreqCut, double beta, double dFreqCut)
    {
        this.sampleRate = sampleRate;
        this.minFreqCut = minFreqCut;
        this.beta = beta;
        this.dFreqCut = dFreqCut;

        x = new LowPassFilter(getAlpha(minFreqCut));
        dx = new LowPassFilter(getAlpha(dFreqCut));

        y = new LowPassFilter(getAlpha(minFreqCut));
        dy = new LowPassFilter(getAlpha(dFreqCut));
    }

    public double getAlpha(double freqCut)
    {
        double tau = 1/(2*Math.PI*freqCut);
        return 1/(1 + tau/sampleRate);
    }

    public double filter(double value, LowPassFilter v, LowPassFilter dv)
    {
        double dvalue = v.isInitialized() ? (value - v.lastRawValue()) / sampleRate : 0.0;
        double edvalue = dv.filter(dvalue, getAlpha(dFreqCut));

        return v.filter(value, getAlpha(minFreqCut + beta * Math.abs(edvalue)));
    }

    public double[] filter(double value1, double value2)
    {
    	double[] result = { filter(value1, x, dx), filter(value2, y, dy) };
    	return result;
    }
}