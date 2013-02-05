package Kinect;

/**
 * Implementation du filtre passe bas
 */
class LowPassFilter
{
	private double x, X;
	private boolean initialized = false;

	/**
	 * Initialise le filtre
	 */
	public LowPassFilter()
	{
	    x = X = 0;
	}

	/**
	 * Calcule la nouvelle valeur filtre
	 * @param value la valeur a filtrer
	 * @param alpha le facteur du filtre entre 0 et 1
	 * @return la valeur filtre
	 */
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

	/**
	 * Teste si le filtre a deja calculer une valeur
	 * @return true si le filtre a deja calculer une valeur
	 */
	public boolean isInitialized()
	{
	    return initialized;
	}

	/**
	 * Retourne la dernieur valeur donne en entre
	 * @return la dernier valeur donne en entre
	 */
	public double lastRawValue()
	{
	    return x;
	}
}

