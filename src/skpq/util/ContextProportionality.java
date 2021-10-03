package skpq.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import skpq.SpatialObject;
import skpq.SpatialQueryLD;

/*
 * Employs the spatial proportionality [1] to measure the spatial diveristy in the neighborhood of a POI.
 * 
 * [1] Kalamatianos, Georgios, Georgios J. Fakas, and Nikos Mamoulis. 
 * "Proportionality in Spatial Keyword Search." Proceedings of the 2021 
 * International Conference on Management of Data. 2021.
 */

public class ContextProportionality {

	/* Ptolomy's Diversity [2] between two places p1 and p2, and a query location
	 *
	 * [2] Zhi Cai, Georgios Kalamatianos, Georgios J. Fakas, Nikos Mamoulis, and Dimitris
	 * Papadias. 2020. Diversified spatial keyword search on RDF data. VLDB J. 29, 5
	 * (2020), 1171–1189.
	 */
	public static double ptolomyDiversity(double queryLat, double queryLgt, double p1Lat, double p1Lgt,
			double p2Lat, double p2Lgt) {
		
		double diversityScore = SpatialQueryLD.distFrom(p1Lat, p1Lgt, p2Lat, p2Lgt) / 
				(SpatialQueryLD.distFrom(p1Lat, p1Lgt, queryLat, queryLgt) + SpatialQueryLD.distFrom(p2Lat, p2Lgt, queryLat, queryLgt));

		return diversityScore;
	}

	public static double spatialProportionality(SpatialObject poi, WebContentArrayCache featuresCache) {	

		double spatialProportionalityScore = 0;

		try {
			featuresCache.load();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		ArrayList<SpatialObject> featureSet = featuresCache.getArray(poi.getURI());

		for (int b = 0; b < featureSet.size(); b++) {

			SpatialObject f1 = featureSet.get(b);

			for(int c = 0; c < featureSet.size();c++) {

				if(b != c) {
					SpatialObject f2 = featureSet.get(c);

					double diversityScore = ContextProportionality.ptolomyDiversity(Double.parseDouble(poi.getLat()), Double.parseDouble(poi.getLgt()), 
							Double.parseDouble(f1.getLat()), Double.parseDouble(f1.getLgt()), Double.parseDouble(f2.getLat()), Double.parseDouble(f2.getLgt()));

					double spatialScore = 1 - diversityScore;			
				}
			}
		}

		return spatialProportionalityScore;
	}
}
