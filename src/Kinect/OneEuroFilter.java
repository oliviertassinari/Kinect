package Kinect;

/**
 * Implementation du filtre d'un euro.
 */
public class OneEuroFilter
{
    private double freq;
    private double minFreqCut;
    private double beta;
    private double dFreqCut;
    private double lastTime;
    private LowPassFilter x;
    private LowPassFilter dx;

    /**
     * Initialise le filtre.
     * @param freq fréquence d'acquisition des données
     * @param minFreqCut fréquence de coupure minimale
     * @param beta facteur de contribution de la dérivée
     * @param dFreqCut fréquence de coupure minimale de la dérivée
     */
    public OneEuroFilter(double freq, double minFreqCut, double beta, double dFreqCut)
    {
        this.freq = freq;
        this.minFreqCut = minFreqCut;
        this.beta = beta;
        this.dFreqCut = dFreqCut;

        x = new LowPassFilter();
        dx = new LowPassFilter();
    }

    /**
     * Retourne la valeur alpha pour le filtre passe bas.
     * @param freqCut fréquence de coupure désirée
     * @return la valeur alpha pour le filtre passe bas
     */
    public double getAlpha(double freqCut)
    {
        double sampleRate = 1.0 / freq;
    	double tau = 1/(2*Math.PI*freqCut);
        return 1/(1 + tau/sampleRate);
    }

    /**
     * Retourne la valeur filtrée.
     * @param value valeur a filtrer
     * @param v
     * @param dv
     * @return valeur filtrée
     */
    public double filter(double value)
    {
        if(lastTime != 0)
        {
        	freq = 1000 / (System.currentTimeMillis() - lastTime);
        }

        lastTime = System.currentTimeMillis();

    	double dvalue = x.isInitialized() ? (value - x.lastRawValue()) * freq : 0.0;
        double edvalue = dx.filter(dvalue, getAlpha(dFreqCut));

        return x.filter(value, getAlpha(minFreqCut + beta * Math.abs(edvalue)));
    }
}