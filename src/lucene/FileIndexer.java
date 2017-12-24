package lucene;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;

public class FileIndexer extends Indexer
{
	private final File sourceDirectory;

	public FileIndexer() throws IOException
	{
		super();
        this.sourceDirectory = new File(Configuration.SOURCE_DIRECTORY);
	}

	@Override
	protected void startIndexing(IndexWriter indexWriter) throws IOException {
		indexDirectory(sourceDirectory, indexWriter);
	}
	
	private void indexDirectory(File directory, IndexWriter writer) throws IOException
	{
		if(!directory.isDirectory())
		{
			return;
		}

		FileFilter filter = new MyFileFilter();
		for (File file : directory.listFiles())
		{
			if(!file.isDirectory()
					&& !file.isHidden()
					&& file.exists()
					&& file.canRead()
					&& filter.accept(file))
			{
				//System.out.println("Indexing "+file.getCanonicalPath());
				Document document = getDocument(file);
				writer.addDocument(document);
			}
			else if(file.isDirectory())
			{
				indexDirectory(file, writer);
			}
		}
	}

	private Document getDocument(File file) throws IOException
	{
		Document document = new Document();

		FieldType fieldType = new FieldType();
		fieldType.setStored(true);
		fieldType.setStoreTermVectors(true);
		fieldType.setTokenized(true);
		fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		//Field contentField = new Field(fieldName, new FileReader(file), fieldType);
		Field contentField = new Field(fieldName, getAllText(file), fieldType);

		document.add(contentField);

		return document;
	}
	
	public String getAllText(File f) throws FileNotFoundException, IOException {
        String textFileContent = "";

        for (String line : Files.readAllLines(Paths.get(f.getAbsolutePath()))) {
            textFileContent += line;
        }
        return textFileContent;
    }
}