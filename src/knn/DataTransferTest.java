package knn;

import java.util.List;

import node.IConstants;
import node.NodeOld;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class DataTransferTest {

	/**
	 * @param testNode2s
	 * @param trainingNode2s
	 * @param useRelationalAttributes
	 * @param useLabel
	 * @return
	 */
	public static Instances[] createTrainingTestDataset(List<NodeOld> testNode2s,
			List<NodeOld> trainingNode2s, boolean useRelationalAttributes) {

		Attribute a1 = new Attribute("attribute1",(FastVector)null);//TYPE
		Attribute a2 = new Attribute("attribute2",(FastVector)null);//SUBJECT
		Attribute a3 = new Attribute("attribute3",(FastVector)null);//NAME
		Attribute a4 = new Attribute("attribute4");//WIKILINKS
		Attribute a5 = new Attribute("attribute5");//SAMEAS

		FastVector attrs = new FastVector();
		attrs.addElement(a1);
		attrs.addElement(a2);
		attrs.addElement(a3);
		attrs.addElement(a4);
		attrs.addElement(a5);

		// automatically creating relational_attributes
		int attrsSize = attrs.size();
		if (useRelationalAttributes) {
			NodeOld nodeTest = trainingNode2s.get(0);
			for (int i = attrsSize + 1; i < nodeTest.getRelationalFeatures().size() + (attrsSize + 1); i++) {
				attrs.addElement(new Attribute("relational_attribute" + (i)));
			}
		}

		FastVector my_nominal_values = new FastVector(IConstants.getLabels().size());
		for (String label : IConstants.getLabels()) {
			my_nominal_values.addElement(label);
		}

		Attribute classAtrribute = new Attribute("label", my_nominal_values);
		attrs.addElement(classAtrribute);

		Instances trainingDataset = new Instances("trainingDataset", attrs, 0);
		Instances testDataset = new Instances("testDataset", attrs, 0);

		for (NodeOld node : trainingNode2s) {
			Instance instance = createInstances(useRelationalAttributes, a1, a2, a3, a4, a5, attrs, attrsSize, classAtrribute, node);
			trainingDataset.add(instance);
		}

		for (NodeOld node : testNode2s) {
			Instance instance = createInstances(useRelationalAttributes, a1, a2, a3, a4, a5, attrs, attrsSize, classAtrribute, node);
			testDataset.add(instance);
		}

		// http://www.zitnik.si/wordpress/2011/09/25/quick-intro-to-weka/#codesyntax_8

		System.out.println(trainingDataset);
		System.out.println(testDataset);
		//System.out.println(" ");

		Instances[] datasets = new Instances[2];
		datasets[0] = trainingDataset;
		datasets[1] = testDataset;

		return datasets;

	}

	/**
	 * @param useLabel
	 * @param useRelationalAttributes
	 * @param a1
	 * @param a2
	 * @param a3
	 * @param attrs
	 * @param attrsSize
	 * @param predictedLabel
	 * @param classAtrribute
	 * @param node
	 * @return
	 */
	private static Instance createInstances(boolean useRelationalAttributes, Attribute a1, Attribute a2,
			Attribute a3, Attribute a4, Attribute a5, FastVector attrs, int attrsSize, Attribute classAtrribute, NodeOld node) {
		
		Instance instance = new Instance(attrs.size());
		instance.setValue(a1, String.valueOf(node.getAttribute1()));
		instance.setValue(a2, String.valueOf(node.getAttribute2()));
		instance.setValue(a3, String.valueOf(node.getAttribute2()));
		instance.setValue(a4, Double.valueOf(node.getAttribute4()));
		instance.setValue(a5, Double.valueOf(node.getAttribute5()));

		if (useRelationalAttributes) {
			int x = attrsSize;
			for (String key : node.getRelationalFeatures().keySet()) {
				// x=j keeps the index when it starts the relational attributes
				instance.setValue(x++,
						Double.valueOf(node.getRelationalFeatures().get(key)));
			}
		}
		instance.setValue(classAtrribute, node.getLabel());
		return instance;
	}

}
