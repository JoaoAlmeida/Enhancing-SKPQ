package skpq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class User {

	private String name;
	public ArrayList<String> reviews;

	public User(String reviewsFilePath) {
		reviews = new ArrayList<>();
		try {
			loadReviews(reviewsFilePath);
		} catch (IOException e) {
			System.out.println("Reviews could not be loaded. Error: " + e + "\n");
			e.printStackTrace();
		}
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

	public void loadReviews(String reviewsFilePath) throws IOException {

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

	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		User u = new User("profiles/location.arff");
		u.finalize();
	}

}
