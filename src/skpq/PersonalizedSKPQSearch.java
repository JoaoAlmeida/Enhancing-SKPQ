package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import weka.Predictor;
import xxl.util.StarRTree;

/**
 * Process a Spatial Preference Keyword Query using LOD.
 * 
 * @author Joãoo Paulo
 */
public class PersonalizedSKPQSearch extends SpatialQueryLD {

	private double radius;	

	public PersonalizedSKPQSearch(int k, String keywords, String neighborhood, double radius, StarRTree objectsOfInterest, boolean debug) throws IOException {
		super(k, keywords, objectsOfInterest, debug);
		this.radius = radius;				
	}

	public TreeSet<SpatialObject> execute(String queryKeywords, int k) {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
		TreeSet<SpatialObject> topK = new TreeSet<>();
		TreeSet<SpatialObject> pTopK = new TreeSet<>();
		
		try {
			interestObjectSet = loadObjectsInterest("hotel_LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Processing SKPQ query...\n");

		if (debug) {
			printQueryName();
		}

		try {
			topK = findFeaturesExperiment(interestObjectSet, keywords, radius);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		
		//Predicting
		try {
			Predictor p = new Predictor("WEKADataset.arff");
			
			Iterator<SpatialObject> it = topK.iterator();			
			
			
			while(it.hasNext()){
				
				SpatialObject obj = it.next();
				
				double score = p.classifyInstance(obj.getName());
				
				System.out.println("Descrição? " + obj.getName());
				System.out.println("Score antigo: " + obj.getScore());
				System.out.println("Score novo: " + score);
				
				obj.setScore(score);
				
				pTopK.add(obj);
			}
		} catch (Exception e1) {
			System.out.println("Error during prediction process");
			e1.printStackTrace();
		}
		
		try {	
			saveResults(pTopK);
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

		for (int a = 1; a <= topK.size(); a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", BN=" + obj.bestNeighbor.getURI() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}

	@Override
	public void printQueryName(){
		System.out.println("\nk = " + k + " | keywords = [ " + keywords + " ]" + " | type = range [radius = " + radius + "]\n\n");
	}
}
