package lucene;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public abstract class Indexer {
	protected final String fieldName;

	public Indexer() {
		fieldName = Configuration.FILE_CONTENTS;
	}

	protected abstract void startIndexing(IndexWriter indexWriter)
			throws IOException;

	public void index() throws IOException {
		IndexWriter indexWriter = this.getIndexWriter();

		this.startIndexing(indexWriter);

		indexWriter.close();
	}

	protected IndexWriter getIndexWriter() throws IOException {
		IndexWriterConfig config = this.getIndexWriterConfig();
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		Directory directory = null;
		if (Configuration.INDEX_IN_RAM) {
			directory = MyRAMDirectory.getInstance();
		} else {
			directory = FSDirectory
					.open(new File(Configuration.INDEX_DIRECTORY).toPath());
		}

		return new IndexWriter(directory, config);
	}

	protected IndexWriterConfig getIndexWriterConfig() throws IOException {
		// Create reader for stop words file
		Reader stopWordsReader = new FileReader(new File(
				Configuration.STOP_WORDS_FILE));
		Analyzer analyzer = new StandardAnalyzer(stopWordsReader);

		// Standard stop words
		// Analyzer analyzer = new
		// StandardAnalyzer(StandardAnalyzer.STOP_WORDS_SET);

		return new IndexWriterConfig(analyzer);
	}
}
