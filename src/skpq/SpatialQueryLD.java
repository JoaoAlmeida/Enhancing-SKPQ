package skpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import skpq.util.WebContentCache;

/**
 *  Essentials to process a top-k query using LOD
 * 
 * @author  João Paulo
 */

public class SpatialQueryLD {

	protected WebContentCache searchCache;
	protected static BufferedReader reader;
	protected boolean USING_GRAPH;
	protected Model model = getTestModel();
	protected int k;
	protected String keywords;
	protected char quotes = '"';
	private final String cacheFileName = "descriptions.ch";	

	public SpatialQueryLD(int k, String keywords) throws IOException {
		this.k = k;
		this.keywords = keywords;
		this.USING_GRAPH = false; //default option		
		
		searchCache = new WebContentCache(cacheFileName);
		searchCache.load();
	}
	
	public SpatialQueryLD(String keywords) throws IOException {		
		this.keywords = keywords;
		this.USING_GRAPH = false; //default option
		
		searchCache = new WebContentCache(cacheFileName);
		searchCache.load();
	}
	
	protected boolean isUSING_GRAPH() {
		return USING_GRAPH;
	}


	protected void setUSING_GRAPH(boolean uSING_GRAPH) {
		USING_GRAPH = uSING_GRAPH;
	}

	protected int getK() {
		return k;
	}

	protected String getKeywords() {
		return keywords;
	}

	protected Model getTestModel() {

		Model model = ModelFactory.createDefaultModel();
		return model;
	}
	
	protected static ArrayList<SpatialObject> loadObjectsInterest(String inputFileName) throws IOException {

		ArrayList<SpatialObject> objectsInterest = new ArrayList<>();
		SpatialObject obj;

		reader = new BufferedReader((new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8")));

		String line = reader.readLine();
		// System.out.println(line);

		int i = 1;

		while (line != null) {			
			String uri = line.substring(line.indexOf("http") - 1).trim();
			
			String osmLabel = line.substring(line.indexOf(" ", line.indexOf(" ") + 1), line.indexOf("http") - 1).trim();
			
			String[] lineVec = line.split(" ");
						
			String lat = lineVec[0];
			String lgt = lineVec[1];
			// String uri = line.split("http")[1];
			obj = new SpatialObject(i, osmLabel, uri, lat, lgt);
			objectsInterest.add(obj);
			line = reader.readLine();
			// System.out.println(line);
			// uri = line.split(" ")[1];
			i++;
		}

		return objectsInterest;
	}

	// Distância euclidiana em metros
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000; // meters
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
		* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (double) (earthRadius * c);

		return dist;
	}
}
