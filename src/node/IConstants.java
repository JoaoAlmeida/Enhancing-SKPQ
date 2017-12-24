package node;

import java.util.ArrayList;
import java.util.List;

public abstract class IConstants {

	public final static String LIKE = "LIKE";

	public final static String DISLIKE = "DISLIKE";

	public final static String NO_LABEL = "NOLABEL";
	
	public final static String LDSD = "LDSD";
	
	public final static String LDSD_LOD = "LDSD_LOD";

	public final static String SEED_EVALUATION = "SEED_EVALUATION";
	

	/*
	 * public final static String
	 * RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING =
	 * "RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING"; public final static
	 * String RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING =
	 * "RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING"; public final static
	 * String RELATIONAL_UNOBSERVED_ATTRIBUTE =
	 * "RELATIONAL_UNOBSERVED_ATTRIBUTE_";
	 */

	public final static String RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING = "RA_II";
	public final static String RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING = "RA_IO";
	public final static String RELATIONAL_OBSERVED_ATTRIBUTE_DIRECT = "RA_D";
	public final static String RELATIONAL_UNOBSERVED_ATTRIBUTE = "UA_";

	public static final int K = 3;

	public static int N = 2;
	
	public static int N_PRIME = 2;

	public static boolean USE_MAX_MIN_CLASSIFIER = true;

	// Similarity calculus will remove abstract, comment and name properties
	public static boolean REMOVE_COMMON_FEATURES = false;

	public static double semanticSimilarityThreshold = 0.5;

	public static double CLASSIFICATION_THRESHOLD = 0.0;

	public static int AMOUNT_OF_ITERATIONS_TO_STABALIZE = 3;

	public static boolean useSemanticSimilarityForSelectingTrainingSet = false;

	// used to consider or not predicted label as a observed attributed
	public static boolean ADD_PREDICTED_LABEL = true;
	
	public static boolean DISCARD_PREDICTED_LABEL = false;
	
	public static boolean CHECK_LABEL = false;	

	// penalize similarity by ontology property amount
	public static boolean PENALIZE_SIMILARITY_BY_AMOUNT_OF_ONTOLOGY_DATATYPE_PROPERTY = false;

	public static int NUMBER_OF_ITERATIONS = 3;

	public final static String wikiPageExternalLink = "http://dbpedia.org/ontology/wikiPageExternalLink";

	public final static String owlsameAs = "https://www.w3.org/2002/07/owl#sameAs";

	// Only one can be selected true
	public static boolean USE_ICA = false;
	
	public static boolean USE_SEMANTIC_DISTANCE = false;

	public static boolean useNeighboursSelectedBySemanticSimilarityForTraningSet = false;

	public static boolean computeSemanticSimilarityOfRelationalAttributes = false;
	
	public static List<String> getLabels() {
		List<String> labels = new ArrayList<String>();
		labels.add(IConstants.DISLIKE);
		labels.add(IConstants.LIKE);
		return labels;
	}

	public static String getValidLabel(String label) {
		if (getLabels().contains(label)) {
			return label;
		}
		return null;
	}	

}
