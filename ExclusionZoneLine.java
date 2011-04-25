
public class ExclusionZoneLine extends ExclusionZone {
	String axis = "X";
	int width = 0;
	double innerProbability = 0;
	double  outerProbability = 0;

	
	public ExclusionZoneLine(int newX, int newZ, boolean authoritative, String[] data) {
		type            = "Line";
		x               = newX; 
		z               = newZ;
		isAuthoritative = authoritative;
		
		// TODO: Check for correct number of arguments
		
		// Data: Axis,Distance,Inner Probability, Outer Probability
		axis             = data[0];
		width         = Integer.parseInt(data[1]);
		innerProbability = Double.parseDouble(data[2]);
		outerProbability = Double.parseDouble(data[3]);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append(baseString());     s.append(",");
		s.append(axis);             s.append(",");
		s.append(width);            s.append(",");
		s.append(innerProbability); s.append(",");
		s.append(outerProbability);

		return s.toString();
	}

	public double spawnProbability(double rX, double rZ) {
		double lineLoc = 0;
		double playerLoc = 0;
		if (axis.equalsIgnoreCase("X")) {
			lineLoc = x;
			playerLoc = rX;
		} else if (axis.equalsIgnoreCase("Z")) {
			lineLoc = z;
			playerLoc = rZ;
		} else {
			return -1;
		}
		
		double distance = Math.abs(lineLoc - playerLoc);
		
		if (distance <= width) {
			if (Double.compare(innerProbability, outerProbability) == 0) {
				// Flat value
				return innerProbability;
			} else {
				// Linear mapping
				return innerProbability + ((outerProbability-innerProbability) * (distance/width));
			}
		}
		return -1;
	}

}
