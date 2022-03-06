package weka;

import java.io.File;
import java.util.Random;

import predictor.CrossValidationSingleRun;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Predictor {
	
	Instances dataset;
	Classifier cls;
	Instances train, test;
	
	public Predictor(String sourcePath) throws Exception{
		
		DataSource source = new DataSource(sourcePath);
		
		Instances dataset = source.getDataSet();
		dataset.setClassIndex(1);

		int trainSize = (int) Math.round(dataset.numInstances() * 20 / 100); //10%
			int testSize = dataset.numInstances() - trainSize;			
		
			System.out.println(dataset.toString());
		this.dataset = tf_idf(dataset);	
		System.out.println(this.dataset.toString());
		train = new Instances(this.dataset, 0, trainSize);
		test = new Instances(this.dataset, trainSize, testSize);
		
		cls = buildSVMClassifier();
//		cls = buildKNNClassifier();
	}
	
	private Instances tf_idf(Instances data) throws Exception{
		
		Instances output;
		StringToWordVector filter = new StringToWordVector();    
		
		
		
		NGramTokenizer t = new NGramTokenizer();
	    t.setNGramMaxSize(1);
	    t.setNGramMinSize(1);   
		t.setDelimiters("\\W");
	    
		filter.setInputFormat(data);
		filter.setWordsToKeep(1000000);
		filter.setTokenizer(t);
		filter.setAttributeIndices("last");
//	    filter.setAttributeIndicesArray(new int[]{0});
	    filter.setDoNotOperateOnPerClassBasis(true);
//		
//		filter.setTFTransform(true);
//	    filter.setIDFTransform(true);			
		filter.setLowerCaseTokens(true);
//		filter.setOutputWordCounts(true);
//		
//		filter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL,StringToWordVector.TAGS_FILTER));
//		
//		   
//
	    filter.setStopwords(new File("stopwords/english-stop-words.txt"));
//	    	    
//	        Stemmer s = new /*Iterated*/LovinsStemmer();
//	        filter.setStemmer(s);
	    
	    
	   
	    
	    filter.batchFinished();
	    output = Filter.useFilter(data, filter);
	    
	    return output;
	}
	
	private Classifier buildSVMClassifier() throws Exception{
		
		LibSVM libsvm = new LibSVM();
		
		String[] options = new String[6];
		options[0] = "-S"; options[1] = "0";//svm  type
//		options[2] ="-K"; options[3] = "0"; //linear kernel = 0, radius kernel = 2
		options[2] = "-G"; options[3] = "1.0";//gamma
		options[4] = "-C"; options[5] = "1.0";//cost

//		SVM
		libsvm.setOptions(options);
	    libsvm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE));
	    dataset.setClassIndex(0);
		libsvm.buildClassifier(dataset);		
		return libsvm;
	}
	
	private Classifier buildKNNClassifier() throws Exception{
		
		IBk knn = new IBk();	
	    
//		KNN	
	    knn.setKNN(2);
		knn.buildClassifier(dataset);
		
		return knn;
	}
	
	private Classifier buildNBClassifier() throws Exception{
		
		NaiveBayes nb = new NaiveBayes();
	   	
//		String[] options = new String[2];
//		options[0] = "-c"; options[1] = "first";
		
		String[] options = weka.core.Utils.splitOptions("-K");
		nb.setOptions(options);
		nb.setUseSupervisedDiscretization(true);
		
		nb.buildClassifier(dataset);
		
		return nb;
	}


	public Classifier getClassifier(String nome) throws Exception{
		
		if(nome.equals("SVM")){
			return buildSVMClassifier();
		}else if(nome.equals("KNN")){
			return buildKNNClassifier();
		}else if(nome.equals("NB")){
			return buildNBClassifier();
		}else{
			return null;
		}
	}
	public void printEvaluationResults() throws Exception{
		
		System.out.println("Tamanho conjunto de teste: " + train.numInstances());
		Evaluation eval3 = new Evaluation(train);
		eval3.evaluateModel(cls, train);
		
		System.out.println(eval3.toSummaryString());
	}
	
	public double classifyInstance(String description) throws Exception{

		FastVector atts;
		Instances data;

		double[] vals;

		atts = new FastVector();

		atts.addElement(new Attribute("description", (FastVector) null));
		atts.addElement(new Attribute("rate"));

		data = new Instances("Classifying Instance", atts, 0);
		data.setClassIndex(0);
		

		vals = new double[data.numAttributes()];		

		vals[0] = data.attribute(0).addStringValue(description);
		vals[1] = 1;

		data.add(new Instance(0, vals));	
		System.out.println("Inst: " + data.toString());
		data = tf_idf(data);
		System.out.println("Inst: " + data.toString());
		System.out.println("distribution: " + cls.distributionForInstance(data.instance(0))[0]);
		return cls.classifyInstance(data.instance(0));
	}
	
	public void crossValidation(int seed, int folds, Classifier cls) throws Exception{
		
		 // randomize data		
	    Random rand = new Random(seed);
	    Instances randData = new Instances(dataset);
	    randData.randomize(rand);
	    if (randData.classAttribute().isNominal())
	      randData.stratify(folds);
	    
		 // perform cross-validation
	    Evaluation eval = new Evaluation(randData);
	    for (int n = 0; n < folds; n++) {
	      Instances train = randData.trainCV(folds, n);
	      Instances test = randData.testCV(folds, n);
	      // the above code is used by the StratifiedRemoveFolds filter, the
	      // code below by the Explorer/Experimenter:
	      // Instances train = randData.trainCV(folds, n, rand);

	      // build and evaluate classifier
	      
	      Classifier clsCopy = Classifier.makeCopy(cls);
	      clsCopy.buildClassifier(train);
	      eval.evaluateModel(clsCopy, test);
	    }
//	    System.out.println("area under the ROC " + dataset.classIndex());
//	    System.out.println("area under the ROC " + eval.precision(0));
//	    System.out.println("area under the ROC " + eval.recall(0));
	    
	      // output evaluation
	      System.out.println();
	      System.out.println("=== Setup ===");
	      System.out.println("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
	      System.out.println("Dataset: " + dataset.relationName());
	      System.out.println("Folds: " + folds);
	      System.out.println("Seed: " + seed);
	      System.out.println();
	      System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
	     
	}
	
	public static void main(String[] args) throws Exception{
		
		Predictor p = new Predictor("carol_teste.arff");
		
//		p.crossValidation(0, 10, p.getClassifier("KNN"));
		
//		p.printEvaluationResults();
		
//		System.out.println("\n\nPredict: " + p.classifyInstance("Slightly tired hotel, but probably the best hotel in Waterville. Skip the in-house"));
		
//		------------------------------------------------------
				
	}
}
