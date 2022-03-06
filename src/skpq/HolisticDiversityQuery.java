package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeSet;

import similarity.JaccardDistance;
import skpq.util.ContextProportionality;
import skpq.util.Datasets;
import util.experiment.ExperimentException;
import xxl.util.StarRTree;

/*
 * This class describes a top-k spatial keyword preference query that employs the Holistic diversity function [1] to rank POIs.
 * 
 * [1] Kalamatianos, Georgios, Georgios J. Fakas, and Nikos Mamoulis. 
 * "Proportionality in Spatial Keyword Search." Proceedings of the 2021 
 * International Conference on Management of Data. 2021.
 */
public class HolisticDiversityQuery extends SpatialQueryLD {

	private double radius;	
	private double gamma;
	private String city;
	private int numkey;
	
	public HolisticDiversityQuery(int k, String keywords, double gamma, String neighborhood, double radius, StarRTree objectsOfInterest, 
			boolean debug, String city)	throws IOException {
		
		super(k, keywords, objectsOfInterest, debug);
		
		this.radius = radius;
		this.city = city;
		this.gamma = gamma;

		this.numkey = countKeywords(keywords);
	}

	@Override
	protected TreeSet<SpatialObject> execute(String queryKeywords, int k) throws ExperimentException, IOException,
			ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
				
		TreeSet<SpatialObject> topK = new TreeSet<>();		

		System.out.println("Processing HDSKPQ query...\n");

		if (debug) {
			printQueryName();
		}

		Datasets data = new Datasets();

		try {
			topK = data.loadResultstoReOrderBN("SKPQ", keywords, k);		
		} catch (IOException e2) {
			e2.printStackTrace();
		}	
		
		Iterator<SpatialObject> it = topK.iterator();		
		
		
		//For each POI in the result set, do
		while(it.hasNext()) {
			
			SpatialObject poi = it.next();		
			
			double pS = 0;
			double pC = 0;
			
			Iterator<SpatialObject> other = topK.iterator();						
				
			//Diversity score é aplicado apenas no query result entre POIs
			while(other.hasNext()) {								
				
				SpatialObject poi_other = other.next();
				
				if(poi.getId() != poi_other.getId()) {
					
					//somatorio da distancia euclidiana entre p e p', sem considerar q. Deu um valor muito alto
//					pS = pS + distFrom(Double.parseDouble(poi.getLat()), Double.parseDouble(poi.getLgt()), 
//							Double.parseDouble(poi_other.getLat()), Double.parseDouble(poi_other.getLgt()));					
					
					pS = pS + ContextProportionality.ptolomyDiversity(Double.parseDouble(poi.getBestNeighbor().getLat()), Double.parseDouble(poi.getBestNeighbor().getLgt()),
							Double.parseDouble(poi.getLat()), Double.parseDouble(poi.getLgt()),							
							Double.parseDouble(poi_other.getLat()), Double.parseDouble(poi_other.getLgt()));
					
					//somatorio da jaccard distance entre p e p'
					pC = pC + JaccardDistance.apply(poi.getBestNeighbor().getName(), poi_other.getBestNeighbor().getName());									
									
				}
			}

			//DF
			double df = (1-gamma) * pC + gamma * pS;
			
		
		
			//aplica HDF
			double hdf = (1-gamma) *  poi.getScore() + gamma * (df /(k-1));
			
//			System.out.println("HDF: "+ hdf);
//			System.out.println();
			
			poi.setScore(hdf);		
		}
		
		saveResults(topK); 			
//		evaluateQuery("HDSKPQ", keywords, city, numkey, radius, null, gamma);			
		
		return topK;		
	}
	
	@Override
	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {
		
		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("hdskpq/HDSKPQ-LD [" + "k=" + k + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= topK.size(); a++) {

			SpatialObject obj = it.next();

			if (debug) {
				System.out.println("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat()
						+ ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
			}

			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");

			outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat="
					+ obj.getBestNeighbor().getLat() + ", lgt=" + obj.getBestNeighbor().getLgt() + ", score="
					+ obj.getBestNeighbor().getScore() + "]\n");
		}

		outputFile.close();		
	}	
}
