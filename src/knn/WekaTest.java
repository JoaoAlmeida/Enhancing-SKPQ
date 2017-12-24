package knn;
//https://weka.wikispaces.com/Use+WEKA+in+your+Java+code

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.lazy.IBk;
import weka.core.FastVector;
import weka.core.Instances;
 
public class WekaTest {
	
	public static void main(String[] args) throws Exception {
		WekaTest.classifyTest();
	}
	
public static void classifyTest() throws Exception {
		
		
		BufferedReader datafile = readDataFile("like.txt");
		
	
 
		Instances data = new Instances(datafile);
		data.setClassIndex(data.numAttributes() - 1);
		
		
		BufferedReader datafile2 = readDataFile("dislike.txt");
		 
		Instances data2 = new Instances(datafile2);
		data2.setClassIndex(data2.numAttributes() - 1);		
 
		// Do 10-split cross validation
		//Instances[][] split = crossValidationSplit(data, 3);
 
		// Separate split into training and testing arrays
		//Instances[] trainingSplits = split[0];
		//Instances[] testingSplits = split[1];
		
		
		
		
 
		// Use a set of classifiers
/*		Classifier[] models = { 
				new J48(), // a decision tree
				new PART(), 
				new DecisionTable(),//decision table majority classifier
				new DecisionStump() //one-level decision tree
		};*/
		
		Classifier knn = new 	IBk();		
 

		
 
			// Collect every group of predictions for current model in a FastVector
			FastVector predictions = new FastVector();
 
			// For each training-testing split pair, train and test the classifier

				Evaluation validation = classify(knn, data, data2);
 
				System.out.println("TRAINING DATA: \n "+data);
				
				System.out.println();
				
				System.out.println("TEST DATA: \n "+data2);
				
				System.out.println(validation);
				
				System.out.println(validation.predictions());
				
				
				predictions.appendElements(validation.predictions());
 
				// Uncomment to see the summary for each training-testing pair.
				//System.out.println(knn.toString());

 
			// Calculate overall accuracy of current classifier on all splits
			double accuracy = calculateAccuracy(predictions);
 
			// Print current classifier's name and accuracy in a complicated,
			// but nice-looking way.
			System.out.println("Accuracy of " + knn.getClass().getSimpleName() + ": "
					+ String.format("%.2f%%", accuracy)
					+ "\n---------------------------------");
		}	
	
	public static BufferedReader readDataFile(String filename) {
	
		BufferedReader inputReader = null;
 
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
 
		return inputReader;
	}
 
	public static Evaluation classify(Classifier model,
			Instances trainingSet, Instances testingSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainingSet);
 
		try {
			model.buildClassifier(trainingSet);
			
			evaluation.evaluateModel(model, testingSet);
		} catch (Exception e) {
			e.getCause();
			e.getMessage();
			e.printStackTrace();
		}

 
		return evaluation;
	}
 
	public static double calculateAccuracy(FastVector predictions) {
		double correct = 0;
 
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			if (np.predicted() == np.actual()) {
				correct++;
			}
		}
 
		return 100 * correct / predictions.size();
	}
 
	public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
		Instances[][] split = new Instances[2][numberOfFolds];
 
		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}
 
		return split;
	}
 
	public static void classifyReadFile() throws Exception {
		BufferedReader datafile = readDataFile("like.txt");
 
		Instances data = new Instances(datafile);
		data.setClassIndex(data.numAttributes() - 1);
 
		// Do 10-split cross validation
		Instances[][] split = crossValidationSplit(data, 3);
 
		// Separate split into training and testing arrays
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];
 
		// Use a set of classifiers
/*		Classifier[] models = { 
				new J48(), // a decision tree
				new PART(), 
				new DecisionTable(),//decision table majority classifier
				new DecisionStump() //one-level decision tree
		};*/
		
		Classifier[] models = {new 	IBk()};		
 
		// Run for each model
		for (int j = 0; j < models.length; j++) {
 
			// Collect every group of predictions for current model in a FastVector
			FastVector predictions = new FastVector();
 
			// For each training-testing split pair, train and test the classifier
			for (int i = 0; i < trainingSplits.length; i++) {
				
				Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);
 
								
				
				predictions.appendElements(validation.predictions());
 
				System.out.println("predictions "+predictions.getRevision());
				
				// Uncomment to see the summary for each training-testing pair.
				System.out.println(models[j].toString());
			}
 
			// Calculate overall accuracy of current classifier on all splits
			double accuracy = calculateAccuracy(predictions);
 
			// Print current classifier's name and accuracy in a complicated,
			// but nice-looking way.
			System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
					+ String.format("%.2f%%", accuracy)
					+ "\n---------------------------------");
		}
 
	}
}
