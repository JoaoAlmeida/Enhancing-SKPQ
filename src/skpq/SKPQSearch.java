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
 * @author Joao Paulo
 */

public class SKPQSearch extends SpatialQueryLD {

	private double radius;	
	private String match;
	private String neighborhood;
	
	public SKPQSearch(int k, String keywords, String neighborhood, double radius, StarRTree objectsOfInterest, boolean debug, String match) throws IOException {
		super(k, keywords, objectsOfInterest, debug);
		this.radius = radius;
		this.match = match;
		this.neighborhood = neighborhood;
	}

//	public SKPQSearch(int k, String keywords, String neighborhood, double radius, StarRTree objectsOfInterest, boolean debug) throws IOException {
//		super(k, keywords, objectsOfInterest, debug);
//		this.radius = radius;
//		this.match = "default";
//	}
	
	public TreeSet<SpatialObject> execute(String queryKeywords, int k) throws IOException {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
		TreeSet<SpatialObject> topK = new TreeSet<>();

		try {
			interestObjectSet = loadObjectsInterest("hotelLondon_LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Processing SKPQ query...\n");

		if (debug) {
			printQueryName();
		}

		//Organizar isso daqui, d� pra colocar um print pra cada dentro dos ifs
		if(neighborhood.equals("3")) {
			topK = findFeaturesPareto(interestObjectSet, keywords, radius, match);
			try {
//				saveResults(topK);
				saveGroupResultsBNPareto(topK);
//				evaluateQuery(keywords, null, k);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}else if(neighborhood.equals("1")) {
//			range
			topK = findFeaturesLGDBN(interestObjectSet, keywords, radius, match);
//			topK = findFeaturesLGDFast(interestObjectSet, keywords, radius, match);
			
			try {
//				saveResults(topK);
				saveGroupResultsBN(topK);
//				evaluateQuery(keywords, null, k);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}else if(neighborhood.equals("2")) {
//			influence
			topK = findFeaturesInfluence(interestObjectSet, keywords, radius, match);
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
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}

//	Safe to use. K must be 20
protected void saveGroupResults(TreeSet<SpatialObject> topK) throws IOException{
		
		/* Imprime 5 */
		Writer outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "5" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		Iterator<SpatialObject> it = topK.descendingIterator();
		
		for(int a = 1; a <= 5; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 10 */
		outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "10" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 10; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 15 */
		outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "15" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 15; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 20 */
		outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "20" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 20; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
	}

//Safe to use. K must be 20.
protected void saveGroupResultsBN(TreeSet<SpatialObject> topK) throws IOException{
	
	/* Imprime 5 */
	Writer outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "5" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	Iterator<SpatialObject> it = topK.descendingIterator();
	
	for(int a = 1; a <= 5; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
	
	/* Imprime 10 */
	outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "10" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	it = topK.descendingIterator();
	
	for(int a = 1; a <= 10; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
	
	/* Imprime 15 */
	outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "15" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	it = topK.descendingIterator();
	
	for(int a = 1; a <= 15; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
	
	/* Imprime 20 */
	outputFile = new OutputStreamWriter(new FileOutputStream("skpq/SKPQ-LD [" + "k=" + "20" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	it = topK.descendingIterator();
	
	for(int a = 1; a <= 20; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
}

//Safe to use. K must be 20.
protected void saveGroupResultsBNPareto(TreeSet<SpatialObject> topK) throws IOException{
	
	/* Imprime 5 */
	Writer outputFile = new OutputStreamWriter(new FileOutputStream("skpq/ParetoSearch-LD [" + "k=" + "5" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	Iterator<SpatialObject> it = topK.descendingIterator();
	
	for(int a = 1; a <= 5; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
	
	/* Imprime 10 */
	outputFile = new OutputStreamWriter(new FileOutputStream("skpq/ParetoSearch-LD [" + "k=" + "10" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	it = topK.descendingIterator();
	
	for(int a = 1; a <= 10; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
	
	/* Imprime 15 */
	outputFile = new OutputStreamWriter(new FileOutputStream("skpq/ParetoSearch-LD [" + "k=" + "15" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	it = topK.descendingIterator();
	
	for(int a = 1; a <= 15; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
	
	/* Imprime 20 */
	outputFile = new OutputStreamWriter(new FileOutputStream("skpq/ParetoSearch-LD [" + "k=" + "20" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
	
	it = topK.descendingIterator();
	
	for(int a = 1; a <= 20; a++){
		SpatialObject obj = it.next();
		outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
				+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
	}
	
	outputFile.close();
}

	@Override
	public void printQueryName(){
		System.out.println("\nk = " + k + " | keywords = [ " + keywords + " ]" + " | type = range [radius = " + radius + "]\n\n");
	}
}
