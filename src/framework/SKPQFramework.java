package framework;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import skpq.PersonalizedSKPQSearch;
import skpq.RQSearch;
import skpq.SKPQPareto;
import skpq.SKPQSearch;
import util.experiment.DefaultExperimentManager;
import util.experiment.Experiment;
import util.experiment.ExperimentException;
import util.experiment.ExperimentFactory;
import util.experiment.ExperimentManager;
import util.experiment.ExperimentResult;
import util.experiment.ExperimentRunner;
import xxl.util.StarRTree;

/**
 *
 * @author Joao Paulo
 */
public class SKPQFramework extends DefaultExperimentManager {

	private Experiment experiment;	

	SKPQFramework(Properties cfg, int round) {
		super(cfg, round);
	}

	public String getOutputCaptions() {
//		return "categoryName experimentName dataset neighborhood plusMode queryType numQueries numKeywords numResults radius";
		return "experimentName dataset neighborhood queryType numQueries numKeywords numResults radius";
	}

	public String getNameID() {		
		return getProperties().getProperty("experiment.name") + "_" + getProperties().getProperty("query.name") + "_"
				+ getProperties().getProperty("query.keywords") + "_"
				+ getNeighborhood(Integer.parseInt(getProperties().getProperty("query.neighborhood"))) + "_"		
				+ getProperties().getProperty("query.numResults") + "_" + getProperties().getProperty("query.radius");
	}

	private String getNeighborhood(int value) {
		return value == 0 ? "nn" : (value == 1 ? "range" : (value == 2 ? "influence" : (value == 3 ? "pareto" : "error")));
	}

	public String getTestId() {
		return getNameID().substring(getNameID().indexOf("_") + 1).replace('_', ' ') + "_round" + this.getRound();
	}

	public String getOutputVariables() {
		return getNameID().replace('_', ' ');
	}

	@Override
	public void open() throws ExperimentException {
		super.open();
		
		try {
			if (getProperties().getProperty("query.name").equals("SKPQ-LD")) {		

				StarRTree objectsOfInterest = createRtree();
				
				experiment = new SKPQSearch(Integer.parseInt(getProperties().getProperty("query.numResults")),
						getProperties().getProperty("query.keywords"),
						getProperties().getProperty("query.neighborhood"),
						Double.parseDouble(getProperties().getProperty("query.radius")), objectsOfInterest,
						Boolean.parseBoolean(getProperties().getProperty("experiment.debug", "false")),
						getProperties().getProperty("query.match", "default"), getProperties().getProperty("query.city"));
		
			} else if (getProperties().getProperty("query.name").equals("PSKPQ-LD")) {		

				StarRTree objectsOfInterest = createRtree();
				
				experiment = new PersonalizedSKPQSearch(Integer.parseInt(getProperties().getProperty("query.numResults")),
						getProperties().getProperty("query.keywords"),
						getProperties().getProperty("query.neighborhood"),
						Double.parseDouble(getProperties().getProperty("query.radius")), objectsOfInterest,
						Boolean.parseBoolean(getProperties().getProperty("experiment.debug")));
			
			}else if(getProperties().getProperty("query.name").equals("Pareto-LD")) {
				
				StarRTree objectsOfInterest = createRtree();
				
				experiment = new SKPQPareto(Integer.parseInt(getProperties().getProperty("query.numResults")),
						getProperties().getProperty("query.keywords"),
						getProperties().getProperty("query.neighborhood"),
						Double.parseDouble(getProperties().getProperty("query.radius")), objectsOfInterest,
						Boolean.parseBoolean(getProperties().getProperty("experiment.debug")),getProperties().getProperty("query.city"));
				
			} else if (getProperties().getProperty("query.name").equals("RQ-LD")) {

				StarRTree objectsOfInterest = createRtree();			
					
					experiment = new RQSearch(Integer.parseInt(getProperties().getProperty("query.numResults")),
							getProperties().getProperty("query.keywords")," ",
							Double.parseDouble(getProperties().getProperty("query.radius")), objectsOfInterest, 
							Boolean.parseBoolean(getProperties().getProperty("experiment.debug")));
					
			} else if (getProperties().getProperty("query.name").equals("BRQ-LD")) {


			} else {
				throw new RuntimeException(
						"Experiment: '" + getProperties().getProperty("query.name") + "' was not developed yet!");
			}
			experiment.open();			
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}

	public void run() throws ExperimentException {
		long time = System.currentTimeMillis();
		experiment.run();
		getCount("totalExecutionTime").update(System.currentTimeMillis() - time);
	}

	@Override
	public void close() throws ExperimentException {
		super.close();

		experiment.close();		
	}

	public ExperimentResult[] getResult() {
		return experiment.getResult();
	}

	private StarRTree createRtree() throws FileNotFoundException, IOException, ClassNotFoundException {

		StarRTree rTree = new StarRTree(this, "", getProperties().getProperty("experiment.folder") + "/rtree",
				Integer.parseInt(getProperties().getProperty("srtree.dimensions")),
				Integer.parseInt(getProperties().getProperty("srtree.cacheSize")),
				Integer.parseInt(getProperties().getProperty("disk.blockSize")),
				Integer.parseInt(getProperties().getProperty("srtree.tree.minNodeCapacity")),
				Integer.parseInt(getProperties().getProperty("srtree.tree.maxNodeCapacity")));

		LoadRTree.load(rTree, getProperties().getProperty("dataset.objectsFile"));

		return rTree;
	}

	public static void main(String[] args) throws IOException {
		
		FileUtils.cleanDirectory(new File("./thrash"));
		FileUtils.cleanDirectory(new File("./evaluations"));
		
		if (args == null || args.length == 0) {
			args = new String[] { "framework.properties" };
		}
		ExperimentRunner runner = new ExperimentRunner(new SKPQFrameworkFactory(), args[0]);
		runner.run();
		Toolkit.getDefaultToolkit().beep();
		Toolkit.getDefaultToolkit().beep();
	}
}

class SKPQFrameworkFactory implements ExperimentFactory {

	public ExperimentManager produce(Properties cfg, int round) {
		return new SKPQFramework(cfg, round);
	}
}
