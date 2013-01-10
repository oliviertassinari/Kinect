package Kinect;

public class OneEuroFilter
{
    private double freq;
    private double mincutoff;
    private double beta;
    private double dcutoff;
    private LowPassFilter x;
    private LowPassFilter dx;
    private double lasttime;
    private static double UndefinedTime = -1;

    public OneEuroFilter(double freq, double mincutoff, double beta, double dcutoff) throws Exception
    {
        this.freq = freq;
        this.mincutoff = mincutoff;
        this.beta = beta;
        this.dcutoff = dcutoff;

        x = new LowPassFilter(getAlpha(mincutoff));
        dx = new LowPassFilter(getAlpha(dcutoff));
        lasttime = UndefinedTime;
    }

    public double getAlpha(double cutoff)
    {
        double te = 1/freq;
        double tau = 1/(2*Math.PI*cutoff);
        return 1/(1 + tau/te);
    }

    double filter(double value) throws Exception
    {
        return filter(value, UndefinedTime);
    }

    double filter(double value, double timestamp) throws Exception
    {
        // update the sampling frequency based on timestamps
        if (lasttime != UndefinedTime && timestamp != UndefinedTime) {
            freq = 1.0 / (timestamp - lasttime);
        }

        lasttime = timestamp;
        // estimate the current variation per second
        double dvalue = x.hasLastRawValue() ? (value - x.lastRawValue()) * freq : 0.0; // FIXME: 0.0 or value?
        double edvalue = dx.filterWithAlpha(dvalue, getAlpha(dcutoff));
        // use it to update the cutoff frequency
        double cutoff = mincutoff + beta * Math.abs(edvalue);
        // filter the given value
        return x.filterWithAlpha(value, getAlpha(cutoff));
    }
}