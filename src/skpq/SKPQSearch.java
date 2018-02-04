package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import xxl.util.StarRTree;

/**
 * Process a Spatial Preference Keyword Query using LOD.
 * 
 * @author João Paulo
 */

public class SKPQSearch extends SpatialQueryLD {

	private double radius;	
	
	public SKPQSearch(int k, String keywords, String neighborhood, double radius, StarRTree objectsOfInterest, boolean debug) throws IOException {
		super(k, keywords, objectsOfInterest, debug);
		this.radius = radius;				
	}

	public TreeSet<SpatialObject> execute(String queryKeywords, int k) {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
		TreeSet<SpatialObject> topK = new TreeSet<>();

		try {
			interestObjectSet = loadObjectsInterest("hotel_LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Processing SKPQ query...\n");

		if (debug) {
			printQueryName();
		}

		topK = findFeaturesLGD(interestObjectSet, keywords, radius);
		
		try {	
			saveResults(topK);
			evaluateQuery(keywords, null, k);
		} catch (IOException e) {
			e.printStackTrace();
		}		

		return topK;
	}
	
	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {			
		
		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("SKPQ-LD [" + "k=" + k + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= k; a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}
	
	@Override
	public void printQueryName(){
		System.out.println("\nk = " + k + " | keywords = [ " + keywords + " ]" + " | type = range [radius = " + radius + "]\n\n");
	}
}
