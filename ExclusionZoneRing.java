
public class ExclusionZoneRing extends ExclusionZone {
	int innerRadius = 0;
	int outerRadius = 0;
	double innerProbability = 0;
	double outerProbability = 0;
	
	ExclusionZoneRing(int newX, int newZ, boolean authoritative, String[] data) {
		type            = "Ring";
		x               = newX; 
		z               = newZ;
		isAuthoritative = authoritative;
		
		// TODO: Check for correct number of arguments
		
		// Data: innerRadius,innerProbability,outerRadius,outerProbability
		innerRadius      = Integer.parseInt(data[0]);
		innerProbability = Double.parseDouble(data[1]);
		
		outerRadius      = Integer.parseInt(data[2]);
		outerProbability = Double.parseDouble(data[3]);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append(baseString());     s.append(",");
		s.append(innerRadius);      s.append(",");
		s.append(innerProbability); s.append(",");
		s.append(outerRadius);      s.append(",");
		s.append(outerProbability);

		return s.toString();
	}
	
	public double spawnProbability(double rX, double rZ) {
		double distance = this.getDistance(rX,rZ);
		if (distance >= innerRadius && distance <= outerRadius) {
			if (Double.compare(innerProbability, outerProbability) == 0) {
				// Flat value
				return innerProbability;
			} else {
				// Linear mapping
				return innerProbability + ((outerProbability-innerProbability) * ((distance-innerRadius)/(outerRadius-innerRadius)));
			}
		}
		return -1;
	}
}
