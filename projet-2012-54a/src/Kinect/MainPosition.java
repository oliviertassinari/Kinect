package Kinect;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;

public class MainPosition
{
	private long positions[][] = new long[20][5];
	private long derivees[][] = new long[19][4];

	public MainPosition()
	{
	}

	// this method keeps the different positions of the hand
	public void add(long time, CvPoint centre, long depth, long state)
	{
		for(int i = 19; i > 0; i--)
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
	
	public void computeDerivees()
	{
		for(int i = 18; i > 0; i--)
		{
			derivees[i][0] = derivees[i-1][0];
			derivees[i][1] = derivees[i-1][1];
			derivees[i][2] = derivees[i-1][2];
			derivees[i][3] = derivees[i-1][3];
		}
		
		long deltaT = positions[0][0]-positions[1][0];
		
		derivees[0][0] = positions[0][0];
		derivees[0][1] = (positions[0][1]-positions[1][1])/deltaT;
		derivees[0][2] = (positions[0][2]-positions[1][2])/deltaT;
		derivees[0][3] = (positions[0][3]-positions[1][3])/deltaT;
	}

	
	// this method adds positions in the list of positions
	public void add(MainPosition mainPosition)
	{
		for(int i = 19; i > 0; i--)
		{
			positions[i][0] = mainPosition.get(i)[0];
			positions[i][1] = mainPosition.get(i)[1];
			positions[i][2] = mainPosition.get(i)[2];
			positions[i][3] = mainPosition.get(i)[3];
		}
	}


	// this method returns the index of a position
	public long[] get(int index)
	{
		return positions[index];
	}
	
}
