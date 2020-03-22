package skpq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import skpq.SpatialQueryLD;

public class User {

	private String name;
	public ArrayList<String> reviews;
	private ArrayList<PointInterest> pointsVisited;
	

	private int profileNumber;
	
	public User(String reviewsFilePath) {
		reviews = new ArrayList<>();
		try {
			loadReviews(reviewsFilePath);
		} catch (IOException e) {
			System.out.println("Reviews could not be loaded. Error: " + e + "\n");
			e.printStackTrace();
		}
	}
	
	/*profile number used in profiles with check-ins */
	public User(int profileNumber) {
		this.profileNumber = profileNumber;
		this.pointsVisited = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getReviews() {
		return reviews;
	}

	private void loadReviews(String reviewsFilePath) throws IOException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(reviewsFilePath)), "ISO-8859-1")));

		String line = reader.readLine();		

		// read database name
		name = line.split(" ")[1];

		line = reader.readLine();

		while (line == null || !line.equals("@data")) {
			line = reader.readLine();
		}
		
		line = reader.readLine();
		
		while (line != null) {
			line = line.trim();
			
			reviews.add(line.substring(3, line.length() - 1));
			line = reader.readLine();
		}

		reader.close();
	}

	public void loadCheckinProfile() throws UnsupportedEncodingException, IOException {
		
		BufferedReader reader = new BufferedReader((new InputStreamReader(
				new FileInputStream(new File("./profiles/check-ins/New York/user profile " + profileNumber + ".txt")), "ISO-8859-1")));
		
		String line = reader.readLine();
		
		while(line != null) {
			
			String[] lineVec = line.split("\\t");

			String venueID = lineVec[2];
			String venueName = lineVec[3];
			String venueCategory = lineVec[5];

			String[] venueLocationVec = lineVec[4].split(",");
			
			double lat = Double.parseDouble(venueLocationVec[0].split("\\{")[1]);
			double lgt = Double.parseDouble(venueLocationVec[1]);
			
			double[] coordinates = {lat, lgt};
			
			PointInterest poi = new PointInterest(coordinates);
			
			poi.setCheckinHash(venueID);
			poi.setName(venueName);
			poi.setAdress(venueLocationVec[2]+venueLocationVec[3]+venueLocationVec[4]);
			poi.setCategory(venueCategory);
			
//			System.out.println(poi.toString());	
			pointsVisited.add(poi);
			
			line = reader.readLine();
		}
		
		System.out.println(pointsVisited.size());
		
		reader.close();
	}
	
	public double findClosest(double[] coordinates) {
		
		Iterator<PointInterest> it = pointsVisited.iterator();
		
		PointInterest poi = it.next();
		
		double minDist = Double.MAX_VALUE;
		double dist = 0;
		
		while(it.hasNext()) {
			
			dist = SpatialQueryLD.distFrom(coordinates[0], coordinates[1], poi.getLat(), poi.getLgt());
			
			if(dist < minDist) {
				minDist = dist;
			}

			poi = it.next();	
		}

		return minDist;
	}
	
	public static void main(String[] args) throws Throwable {

		User u = new User(1);
		u.loadCheckinProfile();
		
		double[] d = {25.26060445, 55.3246791};
		
		u.findClosest(d);
	}

}
