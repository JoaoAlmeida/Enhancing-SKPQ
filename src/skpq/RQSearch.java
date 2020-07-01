package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import skpq.util.SpatioTreeHeapEntry;
import util.experiment.ExperimentException;
import xxl.core.cursors.Cursor;
import xxl.util.StarRTree;

/**
 * Process a top-k Range Query (RQ) using LOD.
 * 
 * The top-k RQ retrieves ranked objects based on their text relevance and the
 * query region.
 * 
 * @author Joao Paulo
 */

public class RQSearch extends SpatialQueryLD {
	
	private String queryLocation;	
	private double radius;
	boolean evaluate = true;

	public RQSearch(int k, String keywords, String queryLocation, double radius, StarRTree objectsOfInterest, boolean debug)
			throws IOException {
		super(k, keywords, objectsOfInterest, debug);

		this.queryLocation = queryLocation;
		this.radius	= radius;
	}

	@Override
	protected TreeSet<SpatialObject> execute(String queryKeywords, int k) throws ExperimentException {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();

		try {
			interestObjectSet = loadObjectsInterest("hotel_LGD.txt");
		} catch (IOException e) {
			System.out.println("Objects of interest loading failed!");
			e.printStackTrace();
		}

		System.out.println("Processing Range query...\n");

		if (debug) {
			printQueryName();
		}

		TreeSet<SpatialObject> topK = null;

		Cursor leaves = objectsOfInterest.query(1);
		SpatioTreeHeapEntry leafEntry = new SpatioTreeHeapEntry(leaves.next());
		Cursor interestPointer = objectsOfInterest.query(leafEntry.getMBR());

			SpatioTreeHeapEntry point = new SpatioTreeHeapEntry(interestPointer.next());

			String coordinates = String.valueOf(point.getMBR().getCorner(false).getValue(0)) + ", "
					+ String.valueOf(point.getMBR().getCorner(false).getValue(1));

			queryLocation = "25.23544055, 55.2796043";

			SpatialObject queryLocationObj = identifyURILocation(queryLocation, interestObjectSet);

			topK = findFeaturesLGD(queryLocationObj, keywords, radius);

			Cursor dummy = null; 

			if(topK.size() < k){

				dummy = objectsOfInterest.query(leafEntry.getMBR());									

				while (topK.size() < k && dummy.hasNext()) {										

					point = new SpatioTreeHeapEntry(dummy.next());

					coordinates = String.valueOf(point.getMBR().getCorner(false).getValue(0)) + ", "
							+ String.valueOf(point.getMBR().getCorner(false).getValue(1));												 

					topK.add(identifyURILocation(coordinates, interestObjectSet));
				}
			}

			try {
				saveResults(topK);
				if(evaluate){
//					evaluateQuery(keywords, String.valueOf(radius), k, false);	
				}							
			} catch (IOException e) {
				System.out.println("We can't save the results on your disk!");
				e.printStackTrace();
			}			

		return topK;
	}

	@Override
	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {
				
		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("RQ-LD [" + "k=" + k + ", kw=" + getKeywords() + ", radius=" + radius + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.iterator();

		for (int a = 1; a <= k; a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "] " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}
}