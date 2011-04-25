import java.util.Arrays;

public abstract class ExclusionZone
{
	public boolean isAuthoritative = false;
	public double x = 0;
	public double z = 0;
	public String type = "Abstract";
	
	public abstract String toString();

	protected final String baseString() {
		StringBuilder s = new StringBuilder();
		
		s.append(type); s.append(",");
		s.append(x);    s.append(",");
		s.append(z);    s.append(",");

		if (isAuthoritative) {
			s.append("yes");
		} else {
			s.append("no");
		}
		
		return s.toString();
	}

	
	public static ExclusionZone fromString(String text) {
		// Type,Authoritative,X,Z[,typeData]
		String[] s = text.split(",");
		
		if(s.length < 4) { return null; }
		
		String desiredType = s[0];
		boolean thisAuth = s[1].equalsIgnoreCase("yes");
		double thisX = Double.parseDouble(s[2]);
		double thisZ = Double.parseDouble(s[3]);
		
		String[] data;
		if (s.length > 4) {
			data = Arrays.copyOfRange(s, 4, s.length);
		} else {
			data = null;
		}
		
		try {
			if (desiredType.equalsIgnoreCase("ring")) {
				return new ExclusionZoneRing(thisX, thisZ, thisAuth, data);
			} else if (desiredType.equalsIgnoreCase("line")) {
				return new ExclusionZoneLine(thisX, thisZ, thisAuth, data);
			} else if (desiredType.equalsIgnoreCase("rectangle")) {
				return new ExclusionZoneRectangle(thisX, thisZ, thisAuth, data);
			}
		} catch (Exception e) {
			// No smart handling
			return null;
		}
		return null;
	}
	
	protected double getDistance(double targetX, double targetZ) {
		return Math.sqrt(Math.pow(x-targetX, 2) + Math.pow(z-targetZ, 2));
	}
	
	public abstract double spawnProbability(double rX, double rZ);
}