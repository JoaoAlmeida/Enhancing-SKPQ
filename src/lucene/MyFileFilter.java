package lucene;

import java.io.File;
import java.io.FileFilter;

public class MyFileFilter implements FileFilter {
	@Override
	public boolean accept(File pathname) {
		return pathname.getName().toLowerCase()
				.endsWith(Configuration.FILE_EXTENSION);
	}
}
