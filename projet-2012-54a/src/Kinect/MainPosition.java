package Kinect;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;

/**
 * Structure de donnée pour enregistrer la position des mains.
 */
public class MainPosition
{
	private long positions[][] = new long[60][5];
	private float derivees[][] = new float[59][4];

	public MainPosition()
	{
	}

	/**
	 * Ajouter une nouvelle position.
	 * @param time temps en milliseconde depuis 1970
	 * @param centre coordonnées x y
	 * @param depth coordonnée z
	 * @param state état de la main
	 */
	public void add(long time, CvPoint centre, long depth, long state)
	{
		for(int i = 59; i > 0; i--)
		{
			positions[i][0] = positions[i-1][0];
			positions[i][1] = positions[i-1][1];
			positions[i][2] = positions[i-1][2];
			positions[i][3] = positions[i-1][3];
			positions[i][4] = positions[i-1][4];
		}

		positions[0][0] = time;
		positions[0][1] = centre.x();
		positions[0][2] = centre.y();
		positions[0][3] = depth;
		positions[0][4] = state;
		

	}

	/**
	 * Calcule la dérivée de la 1er case du tableau.
	 */
	public void computeDerivees()
	{
		for(int i = 58; i > 0; i--)
		{
			derivees[i][0] = derivees[i-1][1];
			derivees[i][1] = derivees[i-1][2];
			derivees[i][2] = derivees[i-1][3];
		}

		float deltaT = positions[0][0]-positions[1][0];

		derivees[0][0] = (positions[0][1]-positions[1][1])/deltaT;
		derivees[0][1] = (positions[0][2]-positions[1][2])/deltaT;
		derivees[0][2] = (positions[0][3]-positions[1][3])/deltaT;
	}

	
	/**
	 * Transfer de position.
	 * @param mainPosition source des données
	 */
	public void add(MainPosition mainPosition)
	{
		for(int i = 59; i > 0; i--)
		{
			positions[i][0] = mainPosition.get(i)[0];
			positions[i][1] = mainPosition.get(i)[1];
			positions[i][2] = mainPosition.get(i)[2];
			positions[i][3] = mainPosition.get(i)[3];
		}
	}

	/**
	 * Retourne une position.
	 * @param index index
	 * @return position demandée
	 */
	public long[] get(int index)
	{
		return positions[index];
	}

	/**
	 * Retourne une dérivée.
	 * @param index index
	 * @return dérivée demandée
	 */
	public float[] getDerivee(int index)
	{
		return derivees[index];
	}
}
