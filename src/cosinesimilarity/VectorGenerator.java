package cosinesimilarity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

import lucene.AllTerms;
import lucene.Configuration;
import lucene.IndexOpener;

public class VectorGenerator {
	DocVector[] docVector;
	private Map<String, Integer> allterms;
	Integer totalNoOfDocumentInIndex;
	IndexReader indexReader;

	public VectorGenerator() throws IOException {
		allterms = new HashMap<String, Integer>();
		indexReader = IndexOpener.getIndexReader();
		totalNoOfDocumentInIndex = IndexOpener.totalDocumentInIndex();
		docVector = new DocVector[totalNoOfDocumentInIndex];
	}

	public void getAllTerms() throws IOException {
		AllTerms allTerms = new AllTerms();
		allTerms.initAllTerms();
		allterms = allTerms.getAllTerms();
	}

	public DocVector[] getDocumentVectors() throws IOException {
		TFIDFSimilarity tfidfSIM = new ClassicSimilarity();
		Map<String, Float> termsIDF = new HashMap<String, Float>();
		for (String term : allterms.keySet()) {
			// Calculate IDF for all terms
			Float idf = tfidfSIM.idf(indexReader.docFreq(new Term(Configuration.FILE_CONTENTS, term)),totalNoOfDocumentInIndex);
			termsIDF.put(term, idf);
		}

		for (int docId = 0; docId < totalNoOfDocumentInIndex; docId++) {
			Terms vector = indexReader.getTermVector(docId,
					Configuration.FILE_CONTENTS);
			TermsEnum termsEnum = null;
			termsEnum = vector.iterator();
			BytesRef text = null;
			docVector[docId] = new DocVector(allterms);
			while ((text = termsEnum.next()) != null) {
				String term = text.utf8ToString();
				// Calculate tf
				Float tf = tfidfSIM.tf(termsEnum.totalTermFreq());
				// Calculate tf-idf
				Float freq = tf * termsIDF.get(term);
				docVector[docId].setEntry(term, freq);
			}
			docVector[docId].normalize();
			// System.out.println("Doc "+docId+": "+docVector[docId]);
		}
		indexReader.close();
		return docVector;
	}
}
