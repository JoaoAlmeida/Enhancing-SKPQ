package lucene;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;

public class ListIndexer extends Indexer {
	protected List<String> sourceList;

	public ListIndexer(List<String> sourceList) throws IOException {
		super();
		this.sourceList = sourceList;
	}

	@Override
	protected void startIndexing(IndexWriter indexWriter) throws IOException {
		for (String doc : this.sourceList) {
			indexWriter.addDocument(getDocument(doc));
		}
	}

	private Document getDocument(String doc) throws IOException {
		Document document = new Document();

		FieldType fieldType = new FieldType();
		fieldType.setStored(true);
		fieldType.setStoreTermVectors(true);
		fieldType.setTokenized(true);
		fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		Field contentField = new Field(fieldName, doc, fieldType);

		document.add(contentField);

		return document;
	}
}