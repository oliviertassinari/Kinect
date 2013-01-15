package Kinect;

public class OneEuroFilter
{
    private double sampleRate;
    private double minFreqCut;
    private double beta;
    private double dFreqCut;
    private LowPassFilter x;
    private LowPassFilter dx;

    public OneEuroFilter(double sampleRate, double minFreqCut, double beta, double dFreqCut)
    {
        this.sampleRate = sampleRate;
        this.minFreqCut = minFreqCut;
        this.beta = beta;
        this.dFreqCut = dFreqCut;

        x = new LowPassFilter(getAlpha(minFreqCut));
        dx = new LowPassFilter(getAlpha(dFreqCut));
    }

    public double getAlpha(double freqCut)
    {
        double tau = 1/(2*Math.PI*freqCut);
        return 1/(1 + tau/sampleRate);
    }

    double filter(double value)
    {
        double dvalue = x.isInitialized() ? (value - x.lastRawValue()) / sampleRate : 0.0;
        double edvalue = dx.filter(dvalue, getAlpha(dFreqCut));

        return x.filter(value, getAlpha(minFreqCut + beta * Math.abs(edvalue)));
    }
}