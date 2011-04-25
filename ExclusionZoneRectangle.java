
public class ExclusionZoneRectangle extends ExclusionZone {
	int secondX       = 0;
	int secondZ       = 0;
	double probability = 0;

	public ExclusionZoneRectangle(int newX, int newZ, boolean authoritative, String[] data) {
		type            = "Line";
		x               = newX; 
		z               = newZ;
		isAuthoritative = authoritative;
		
		// TODO: Check for correct number of arguments
		
		// Data: X,Z,Probability
		secondX     = Integer.parseInt(data[0]);
		secondZ     = Integer.parseInt(data[1]);
		probability = Double.parseDouble(data[2]);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append(baseString()); s.append(",");
		s.append(secondX);      s.append(",");
		s.append(secondZ);      s.append(",");
		s.append(probability);

		return s.toString();
	}

	public double spawnProbability(double rX, double rZ) {
		if (isWithin(rX,x,secondX) && isWithin(rZ,z,secondZ)) {
			return probability;
		}
		
		return -1;
	}
	
	private boolean isWithin(double testValue, double boundaryA, double boundaryB) {
		return ((testValue <= boundaryA && testValue >= boundaryB) || (testValue <= boundaryB && testValue >= boundaryA));
	}

}
