package predictor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Evaluation {
	
	public Evaluation() {
			
	}
	
	/* THIS METHOD IS NOT FINISHED! You must change the algorithm manually in the learner.evaluate() method */
	public void crossValidating(String[] profiles, String algName) throws IOException, Exception {			
		
		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("crossvalidating - " + algName + " (5 folds).txt"), "ISO-8859-1");
		
		for(int i = 0; i < profiles.length; i++) {
			MyLearner learner = new MyLearner(profiles[i]);
			
			outputFile.write("===== Dataset: " + profiles[i] + " =====\n\n");		
			outputFile.write(learner.evaluate() + "\n\n");
		}
		
		outputFile.close();
	}	
	
	public static void main (String[] args) throws Exception {
		Evaluation eval = new Evaluation();
		
		String[] experiment = {"room", "value", "location", "service", "clean"};
		
		eval.crossValidating(experiment, "KNN");		
	}
}
