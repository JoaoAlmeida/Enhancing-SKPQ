package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import util.experiment.ExperimentException;
import xxl.util.StarRTree;

public class PSMSearch extends SpatialQueryLD {

	private double radius;	
	private String match;
	private double alpha;
	private String city;
	private int numkey;

	public PSMSearch(int k, String keywords, double alpha, double radius, StarRTree objectsOfInterest, boolean debug, String match, String city) throws IOException  {
		super(k, keywords, objectsOfInterest, debug);
		this.radius = radius;
		this.match = match;
		this.city = city;
		this.alpha = alpha;

		this.numkey = countKeywords(keywords);
	}

	@Override
	protected TreeSet<SpatialObject> execute(String queryKeywords, int k) throws ExperimentException, IOException,
	ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
		TreeSet<SpatialObject> topK = new TreeSet<>();

		try {
			interestObjectSet = loadObjectsInterest("./"+city.toLowerCase()+"/"+city+"LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Processing SKPQ query...\n");

		if (debug) {
			printQueryName();
		}

		topK = findFeaturesPareto(interestObjectSet, keywords, radius, match, city.toLowerCase(), alpha);			

		saveGroupResultsBNPareto(topK); 			
		evaluateQueryGroup("PSM", keywords, k, city, numkey, radius, match, alpha);

//		saveResults(topK);			
//		evaluateQuery("PSM", keywords, city, numkey, radius, match, alpha);	

		return topK;
	}

	@Override
	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {
		
		Writer outputFile = new OutputStreamWriter(new FileOutputStream("skpq/PSM-LD [" + "k=" + k + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= topK.size(); a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
			outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
					+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
		}

		outputFile.close();	
	}

	//Safe to use. K must be 20.
	protected void saveGroupResultsBNPareto(TreeSet<SpatialObject> topK) throws IOException{

		if(k == 20) {
			/* Imprime 5 */
			Writer outputFile = new OutputStreamWriter(new FileOutputStream("skpq/PSM-LD [" + "k=" + "5" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

			Iterator<SpatialObject> it = topK.descendingIterator();

			for(int a = 1; a <= 5; a++){
				SpatialObject obj = it.next();
				outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
				outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
						+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
			}

			outputFile.close();

			/* Imprime 10 */
			outputFile = new OutputStreamWriter(new FileOutputStream("skpq/PSM-LD [" + "k=" + "10" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

			it = topK.descendingIterator();

			for(int a = 1; a <= 10; a++){
				SpatialObject obj = it.next();
				outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
				outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
						+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
			}

			outputFile.close();

			/* Imprime 15 */
			outputFile = new OutputStreamWriter(new FileOutputStream("skpq/PSM-LD [" + "k=" + "15" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

			it = topK.descendingIterator();

			for(int a = 1; a <= 15; a++){
				SpatialObject obj = it.next();
				outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
				outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
						+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
			}

			outputFile.close();

			/* Imprime 20 */
			outputFile = new OutputStreamWriter(new FileOutputStream("skpq/PSM-LD [" + "k=" + "20" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

			it = topK.descendingIterator();

			for(int a = 1; a <= 20; a++){
				SpatialObject obj = it.next();
				outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
				outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat=" + obj.getBestNeighbor().getLat() + ", lgt="
						+ obj.getBestNeighbor().getLgt() + ", score=" + obj.getBestNeighbor().getScore() + "]\n");
			}

			outputFile.close();
		}else {
			System.out.println("K has to be 20");
			System.exit(0);
		}
	}	

}
