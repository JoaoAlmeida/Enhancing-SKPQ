package cosinesimilarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.store.LockObtainFailedException;

import lucene.FileIndexer;
import lucene.Indexer;
import lucene.ListIndexer;

public class LuceneCosineSimilarity {
	
	public static void main(String[] args) throws LockObtainFailedException,IOException {
		getCosineSimilarity("aa aa","bb aa bb");
	}
	
	
	public static double getCosineSimilarity(String one, String two, List<String> l)  {
		
		double cosineSimilarity = 0d;
		
		List<String> list = new LinkedList<String>();
		list.add(one);
		list.add(two);
		list.addAll(l);

		try {
			Indexer index = new ListIndexer(l);
		
			
			index.index();
	
			VectorGenerator vectorGenerator = new VectorGenerator();
			vectorGenerator.getAllTerms();
			DocVector[] docVector = vectorGenerator.getDocumentVectors(); 
			System.out.println("one "+one);
			System.out.println("two "+two);
			System.out.println("docVector[0] "+docVector[0]);
			System.out.println("docVector[1] "+docVector[1]);
			cosineSimilarity = CosineSimilarity.CosineSimilarity(docVector[0], docVector[1]);
		
		} catch (Exception e) {
			e.printStackTrace();
		}			
	return cosineSimilarity;
		
	}	
	
	
	/**
	 * @param one
	 * @param two
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public static void index(List<String> l)  {
		try {
		Indexer index = new ListIndexer(l);
	
		index.index();
	} catch (Exception e) {
		e.printStackTrace();
	}
		
	}	
	
	
	/**
	 * @param one
	 * @param two
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public static void getCosineSimilarity(List<String> l) throws LockObtainFailedException,IOException {

		Indexer index = new ListIndexer(l);
	
		index.index();

		VectorGenerator vectorGenerator = new VectorGenerator();
		vectorGenerator.getAllTerms();
		DocVector[] docVector = vectorGenerator.getDocumentVectors(); // getting
																		// document
																		// vectors
		for (int i = 0; i < docVector.length; i++) {
			for (int j = i; j < docVector.length; j++) {
				double cosineSimilarity = CosineSimilarity.CosineSimilarity(docVector[i], docVector[j]);
				System.out.println("Cosine Similarity Score between document "+ i + " and " + j + "  = " + cosineSimilarity);
			}
		}
	}	

	/**
	 * @param one
	 * @param two
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public static double getCosineSimilarity(String one, String two) {
		
		double cosineSimilarity = 0d;
		
		try {
			
			List<String> l = new ArrayList<String>();
			l.add(one);
			l.add(two);
			Indexer index = new ListIndexer(l);
		
			index.index();
	
			VectorGenerator vectorGenerator = new VectorGenerator();
			vectorGenerator.getAllTerms();
			DocVector[] docVector = vectorGenerator.getDocumentVectors(); // getting
									
																			// vectors
			for (int i = 0; i < docVector.length; i++) {
				for (int j = i; j < docVector.length; j++) {
					if (i!=j) {
						cosineSimilarity = CosineSimilarity.CosineSimilarity(docVector[i], docVector[j]);
						//System.out.println("Cosine Similarity Score between document "+ i + " and " + j + "  = " + cosineSimilarity);
					}				
				}
			}
			
			
			} catch (Exception e) {			
				e.printStackTrace();
			}			
		return cosineSimilarity;
	}
	
	
/**
 * @throws LockObtainFailedException
 * @throws IOException
 */
public static void getCosineSimilarityByFileDirectory() throws LockObtainFailedException,IOException {
		Indexer index = null;
		index = new FileIndexer();
		index.index();

		VectorGenerator vectorGenerator = new VectorGenerator();
		vectorGenerator.getAllTerms();
		DocVector[] docVector = vectorGenerator.getDocumentVectors(); // getting
																		// document
																		// vectors
		for (int i = 0; i < docVector.length; i++) {
			for (int j = i; j < docVector.length; j++) {
				double cosineSimilarity = CosineSimilarity.CosineSimilarity(docVector[i], docVector[j]);
				System.out.println("Cosine Similarity Score between document "+ i + " and " + j + "  = " + cosineSimilarity);
			}
		}
	}	
	
	
}