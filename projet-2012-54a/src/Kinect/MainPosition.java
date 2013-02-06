package Kinect;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;

/**
 * Structure de donnée pour enregistrer la position des mains.
 */
public class MainPosition
{
	private long[][] positions;
	private long[][] positionsFiltre;
	private float[][] derivees;
	private OneEuroFilter oneEuroFilterX;
	private OneEuroFilter oneEuroFilterY;

	public MainPosition()
	{
		reset();
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
		for(int i = positions.length-1; i > 0; i--)
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


		for(int i = positionsFiltre.length-1; i > 0; i--)
		{
			positionsFiltre[i][0] = positionsFiltre[i-1][0];
			positionsFiltre[i][1] = positionsFiltre[i-1][1];
			positionsFiltre[i][2] = positionsFiltre[i-1][2];
		}

		positionsFiltre[0][0] = (int)oneEuroFilterX.filter(centre.x());
		positionsFiltre[0][1] = (int)oneEuroFilterY.filter(centre.y());
		positionsFiltre[0][2] = depth;


		// Calcule de dérivée
		for(int i = derivees.length-1; i > 0; i--)
		{
			derivees[i][0] = derivees[i-1][0];
			derivees[i][1] = derivees[i-1][1];
			derivees[i][2] = derivees[i-1][2];
		}

		if(positions[1][0] == 0)
		{
			derivees[0][0] = 0;
			derivees[0][1] = 0;
			derivees[0][2] = 0;
		}
		else
		{
			float deltaT = positions[0][0]-positions[1][0];

			derivees[0][0] = (positionsFiltre[0][0]-positionsFiltre[1][0])/deltaT;
			derivees[0][1] = (positionsFiltre[0][1]-positionsFiltre[1][1])/deltaT;
			derivees[0][2] = (positionsFiltre[0][2]-positionsFiltre[1][2])/deltaT;
		}

	}

	/**
	 * Réinitialise les données
	 */
	public void reset()
	{
		positions = new long[60][5];
		positionsFiltre = new long[60][3];
		derivees = new float[59][3];

		oneEuroFilterX = new OneEuroFilter(30, 1.0, 0.04, 1.0);
		oneEuroFilterY = new OneEuroFilter(30, 1.0, 0.04, 1.0);

		//System.out.println("reset");
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
	 * Retourne une position filtré.
	 * @param index index
	 * @return position filtré demandée
	 */
	public long[] getFiltre(int index)
	{
		return positionsFiltre[index];
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
