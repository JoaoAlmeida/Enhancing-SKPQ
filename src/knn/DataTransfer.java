package knn;

import java.util.List;

import node.NodeOld;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;




public class DataTransfer {

	public static Instances[] createTrainingTestDatasetFromAttributes(NodeOld testNodeOld, List<NodeOld> trainingNodeOlds) {
		
		Attribute a1 = new Attribute("wikiPageExternalLinks", 0);
		Attribute a2 = new Attribute("rdfsSeeAlso", 1);
		Attribute a3 = new Attribute("owlSameAs", 2);
		
		FastVector my_nominal_values = new FastVector(2); 
		my_nominal_values.addElement("LIKE"); 
		my_nominal_values.addElement("DISLIKE"); 
		Attribute a4 = new Attribute("label",my_nominal_values);	

		
		//http://www.zitnik.si/wordpress/2011/09/25/quick-intro-to-weka/
		
		FastVector attrs = new FastVector();
		attrs.addElement(a1);
		attrs.addElement(a2);
		attrs.addElement(a3);
		attrs.addElement(a4);
		
		
		Instances trainingDataset = new Instances("trainingDataset", attrs, 0);
		Instances testDataset = new Instances("testDataset", attrs,0);	
			
		for (NodeOld node : trainingNodeOlds) {
			Instance instance = new Instance(attrs.size());
			instance.setValue(a1, Integer.valueOf(node.getOwlSameAs().size()));
			instance.setValue(a2, Integer.valueOf(node.getWikiPageExternalLinks().size()));
			instance.setValue(a3, Integer.valueOf(node.getRdfsSeeAlso().size()));
			instance.setValue(a4, node.getLabel());
			trainingDataset.add(instance);
		}
								
		Instance instance = new Instance(attrs.size());
		instance.setValue(a1, Integer.valueOf(testNodeOld.getOwlSameAs().size()));
		instance.setValue(a2, Integer.valueOf(testNodeOld.getWikiPageExternalLinks().size()));
		instance.setValue(a3, Integer.valueOf(testNodeOld.getRdfsSeeAlso().size()));
		instance.setValue(a4, testNodeOld.getLabel());
		testDataset.add(instance);

		
		//http://www.zitnik.si/wordpress/2011/09/25/quick-intro-to-weka/#codesyntax_8
		

		//System.out.println(trainingDataset);
		//System.out.println(testDataset);

		Instances[] datasets = new Instances[2];
		datasets[0] = trainingDataset;
		datasets[1] = testDataset;

		return datasets;

	}

	

}
