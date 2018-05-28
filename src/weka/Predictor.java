package weka;

import java.io.File;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Predictor {
	
	Instances dataset;
	LibSVM libsvm;
	
	public Predictor(String sourcePath) throws Exception{
		
		DataSource source = new DataSource(sourcePath);
		Instances dataset = source.getDataSet();
		dataset.setClassIndex(dataset.numAttributes()-1);
		
		this.dataset = tf_idf(dataset);		
		buildClassifier();
	}
	
	private Instances tf_idf(Instances data) throws Exception{
		
		StringToWordVector filter = new StringToWordVector();    
		filter.setWordsToKeep(1000000);
		
		filter.setIDFTransform(true);
		
		filter.setTFTransform(true);
		filter.setLowerCaseTokens(true);
		filter.setOutputWordCounts(true);
		
		filter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL,StringToWordVector.TAGS_FILTER));
		
		NGramTokenizer t = new NGramTokenizer();
	    t.setNGramMaxSize(3);
	    t.setNGramMinSize(2);    
	    filter.setTokenizer(t);     

	    filter.setStopwords(new File("stopwords/english-stop-words.txt"));
	    	    
	        Stemmer s = new /*Iterated*/LovinsStemmer();
	        filter.setStemmer(s);
	    
	    
	    filter.setInputFormat(data);
	    
	    filter.batchFinished();
	    data = Filter.useFilter(data, filter);
	    
	    return data;
	}
	
	private void buildClassifier() throws Exception{
		
		libsvm = new LibSVM();
		
		String[] options = new String[8];
		options[0] = "-S"; options[1] = "0";
		options[2] ="-K"; options[3] = "2";
		options[4] = "-G"; options[5] = "1.0";
		options[6] = "-C"; options[7] = "1.0";
	    
		libsvm.setOptions(options);
	    libsvm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE));
		libsvm.buildClassifier(dataset);
	}
	
	public void printEvaluationResults() throws Exception{
		
		Evaluation eval3 = new Evaluation(dataset);
		eval3.evaluateModel(libsvm, dataset);
		
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
		data.setClassIndex(data.numAttributes()-1);

		vals = new double[data.numAttributes()];		

		vals[0] = data.attribute(0).addStringValue(description);

		data.add(new Instance(1.0, vals));	

		data = tf_idf(data);

		return libsvm.classifyInstance(data.instance(0));
	}
	
	public static void main(String[] args) throws Exception{
		
		Predictor p = new Predictor("WEKADataset.arff");
		
//		p.printEvaluationResults();
		
		System.out.println("\n\nPredict: " + p.classifyInstance("Hotel TourismThing Node Cassells Hotel Apartments"));
	}
}
