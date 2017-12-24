package lucene;

import org.apache.lucene.store.RAMDirectory;

public class MyRAMDirectory {
	private static RAMDirectory instance = null;
	
	protected MyRAMDirectory(){}
	
	public static RAMDirectory getInstance(){
		if(instance == null){
			instance = new RAMDirectory();
		}
		return instance;
	}
}
