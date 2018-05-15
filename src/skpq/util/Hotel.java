package skpq.util;

public class Hotel implements Comparable<Object>{

	String OsmLabel;
	String TripAdvisorLabel;
	int frequency;
	int id;
			
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOsmLabel() {
		return OsmLabel;
	}

	public void setOsmLabel(String osmLabel) {
		OsmLabel = osmLabel;
	}

	public String getTripAdvisorLabel() {
		return TripAdvisorLabel;
	}

	public void setTripAdvisorLabel(String tripAdvisorLabel) {
		TripAdvisorLabel = tripAdvisorLabel;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int compareTo(Object other) {
		if (other instanceof Hotel) {
			Hotel otherDocument = (Hotel) other;
			double thisScore = this.getFrequency();
			double otherScore = otherDocument.getFrequency();
			if (thisScore < otherScore) {
				return -1;
			} else if (thisScore > otherScore) {
				return 1;
			} else {// They are equals
				Hotel outro = (Hotel) other;
				return this.getId() - outro.getId();
			}
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
}


