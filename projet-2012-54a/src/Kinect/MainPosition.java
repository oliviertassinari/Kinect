package Kinect;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;

public class MainPosition
{
	private long positions[][] = new long[20][4];

	public MainPosition()
	{
	}

	public void add(long time, CvPoint centre, long depth)
	{
		for(int i = 19; i > 0; i--)
		{
			positions[i][0] = positions[i-1][0];
			positions[i][1] = positions[i-1][1];
			positions[i][2] = positions[i-1][2];
			positions[i][3] = positions[i-1][3];
		}

		positions[0][0] = time;
		positions[0][1] = centre.x();
		positions[0][2] = centre.y();
		positions[0][3] = depth;
	}

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

	public long[] get(int index)
	{
		return positions[index];
	}
}
