package skpq.util;

import java.util.Date;

public class PointInterest {
	
	private double[] coordinates;
	private String name;
	private String checkinDate;
	private String checkinHash;
	private String adress;
	private String category;
	
	public PointInterest(double[] coordinates) {
		
		if(coordinates.length == 2) {
			this.coordinates = coordinates;
			this.checkinDate = " ";
		}else {
			System.out.println("Verify the POI's coordinates. Incorrect number of values.");
			this.coordinates = null;
		}
		
	}
	
	public double[] getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(double[] coordinates) {
		this.coordinates = coordinates;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCheckinDate() {
		return checkinDate;
	}
	public void setCheckinDate(String checkinDate) {
		this.checkinDate = checkinDate;
	}
	public String getCheckinHash() {
		return checkinHash;
	}
	public void setCheckinHash(String checkinHash) {
		this.checkinHash = checkinHash;
	}
	public String getAdress() {
		return adress;
	}
	public void setAdress(String adress) {
		this.adress = adress;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public double getLat() {
		return coordinates[0];
	}
	
	public double getLgt() {
		return coordinates[1];
	}
	
	public String toString() {		
		return checkinDate.toString() + " " + coordinates[0] + " " + coordinates[1] + " " + name + " " + checkinHash + " " + adress + " " + category;
	}
}
