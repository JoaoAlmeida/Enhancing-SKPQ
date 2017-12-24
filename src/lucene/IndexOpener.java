package lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class IndexOpener {
	public static IndexReader getIndexReader() throws IOException {
		IndexReader indexReader = null;
		if (Configuration.INDEX_IN_RAM) {
			indexReader = DirectoryReader.open(MyRAMDirectory.getInstance());
		} else {
			indexReader = DirectoryReader.open(FSDirectory.open(new File(
					Configuration.INDEX_DIRECTORY).toPath()));
		}
		return indexReader;
	}

	public static Integer totalDocumentInIndex() throws IOException {
		Integer maxDoc = getIndexReader().maxDoc();
		getIndexReader().close();
		return maxDoc;
	}
}
