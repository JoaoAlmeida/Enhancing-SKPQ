/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import dataset.DatasetTupleFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import util.RandomUtil;

import util.experiment.DefaultExperimentManager;
import util.experiment.Experiment;
import util.experiment.ExperimentException;
import util.experiment.ExperimentFactory;
import util.experiment.ExperimentManager;
import util.experiment.ExperimentResult;
import util.experiment.ExperimentRunner;
import util.sse.Vocabulary;
import util.statistics.StatisticCenter;
import xxl.util.StarRTree;


/**
 *
 * @author joao
 */
public class SKPQFramework extends DefaultExperimentManager {

    private Experiment experiment;

    SKPQFramework(Properties cfg, int round) {
        super(cfg, round);
    }

    public String getOutputCaptions() {
        return "categoryName experimentName dataset neighborhood plusMode queryType numQueries numKeywords numResults radius";
    }

    public String getNameID() {
        return getProperties().getProperty("experiment.categoryName") + "_"
                + getProperties().getProperty("experiment.name") + "_"
                + getDataSet() + "_"
                + getNeighborhood(Integer.parseInt(getProperties().getProperty("query.neighborhood"))) + "_"
                + getProperties().getProperty("experiment.plusMode") + "_"
                + getProperties().getProperty("query.type") + "_"
                + getProperties().getProperty("query.numQueries") + "_"
                + getProperties().getProperty("query.numKeywords") + "_"
                + getProperties().getProperty("query.numResults") + "_"
                + getProperties().getProperty("query.radius");
    }

    private String getNeighborhood(int value) {
        return value == 0 ? "nn" : (value == 1 ? "range" : (value == 2 ? "influence" : "error"));
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

            String randomSeed = getDataSet() + this.getRound();

            if (getProperties().getProperty("experiment.name").equals("SKPQ-LD")) {
            	
                RandomUtil.setNewSeed(randomSeed.hashCode());

                PreferenceFileTermManager fileTermManager = createFileTermManager();

                PreferenceTreeTermManager treeTermManager = createTreeTermManager();

                PreferenceIndex prefIndex = createSpatialInvertedIndex(fileTermManager, treeTermManager, false);

                StarRTree rTree = createRtree();

                prefIndex.open();//create the vocabulary

                experiment = new PreferenceSearch(this,
                        Boolean.parseBoolean(getProperties().getProperty("experiment.debug")),
                        prefIndex.getTermVocabulary(),
                        Integer.parseInt(getProperties().getProperty("query.numKeywords")),
                        Integer.parseInt(getProperties().getProperty("query.numResults")),
                        Integer.parseInt(getProperties().getProperty("query.numQueries")),
                        0.0 /*alfa*/,
                        Double.parseDouble(getProperties().getProperty("dataset.spaceMaxValue")),
                        getProperties().getProperty("query.type"),
                        getProperties().getProperty("query.keywords"),
                        Integer.parseInt(getProperties().getProperty("query.numMostFrequentTerms")),
                        Integer.parseInt(getProperties().getProperty("query.numWarmUpQueries")),
                        prefIndex, rTree,
                        Integer.parseInt(getProperties().getProperty("query.neighborhood")),
                        Boolean.parseBoolean(getProperties().getProperty("experiment.plusMode")),
                        Double.parseDouble(getProperties().getProperty("query.radius")));

                //Acrescentado no doutorado para fazer analise de qualidade -- artigo
            } else if (getProperties().getProperty("experiment.name").equals("TextSearch")) {
                
                RandomUtil.setNewSeed(randomSeed.hashCode());

                TextFileTermManager fileTermManager = createTextFileTermManager();

                TextTreeTermManager treeTermManager = createTextTreeTermManager();

                TextIndex prefIndex = createTextIndex(fileTermManager, treeTermManager, false);
             
                prefIndex.open();//create the vocabulary
                
                experiment = new TextSearch(this,
                        Boolean.parseBoolean(getProperties().getProperty("experiment.debug")),
                        prefIndex.getTermVocabulary(),
                        Integer.parseInt(getProperties().getProperty("query.numKeywords")),
                        Integer.parseInt(getProperties().getProperty("query.numResults")),
                        Integer.parseInt(getProperties().getProperty("query.numQueries")),
                        0.0 /*alfa*/,
                        Double.parseDouble(getProperties().getProperty("dataset.spaceMaxValue")),
                        getProperties().getProperty("query.type"),
                        getProperties().getProperty("query.keywords"),
                        Integer.parseInt(getProperties().getProperty("query.numMostFrequentTerms")),
                        Integer.parseInt(getProperties().getProperty("query.numWarmUpQueries")),
                        prefIndex, 
                        Integer.parseInt(getProperties().getProperty("query.neighborhood")),
                        Boolean.parseBoolean(getProperties().getProperty("experiment.plusMode")),
                        Double.parseDouble(getProperties().getProperty("query.radius")));
                
            }else if (getProperties().getProperty("experiment.name").equals("BooleanRangeSearch")) {
                
                RandomUtil.setNewSeed(randomSeed.hashCode());

                TextFileTermManager fileTermManager = createTextFileTermManager();

                TextTreeTermManager treeTermManager = createTextTreeTermManager();

                TextIndex prefIndex = createTextIndex(fileTermManager, treeTermManager, false);
                
                StarRTree rTree = createRtree();
             
                prefIndex.open();//create the vocabulary
                
                experiment = new TextSearch(this,
                        Boolean.parseBoolean(getProperties().getProperty("experiment.debug")),
                        prefIndex.getTermVocabulary(),
                        Integer.parseInt(getProperties().getProperty("query.numKeywords")),
                        Integer.parseInt(getProperties().getProperty("query.numResults")),
                        Integer.parseInt(getProperties().getProperty("query.numQueries")),
                        0.0 /*alfa*/,
                        Double.parseDouble(getProperties().getProperty("dataset.spaceMaxValue")),
                        getProperties().getProperty("query.type"),
                        getProperties().getProperty("query.keywords"),
                        Integer.parseInt(getProperties().getProperty("query.numMostFrequentTerms")),
                        Integer.parseInt(getProperties().getProperty("query.numWarmUpQueries")),
                        prefIndex, 
                        Integer.parseInt(getProperties().getProperty("query.neighborhood")),
                        Boolean.parseBoolean(getProperties().getProperty("experiment.plusMode")),
                        Double.parseDouble(getProperties().getProperty("query.radius")));
                
            }else if (getProperties().getProperty("experiment.name").equals("RangeSearch")) {
                
                RandomUtil.setNewSeed(randomSeed.hashCode());

                RangeFileTermManager fileTermManager = createRangeFileTermManager();

                RangeTreeTermManager treeTermManager = createRangeTreeTermManager();

                RangeIndex prefIndex = createRangeIndex(fileTermManager, treeTermManager, false);
             
                StarRTree rTree = createRtree();
                
                prefIndex.open();//create the vocabulary
                
                experiment = new RangeSearch(this,
                        Boolean.parseBoolean(getProperties().getProperty("experiment.debug")),
                        prefIndex.getTermVocabulary(),
                        Integer.parseInt(getProperties().getProperty("query.numKeywords")),
                        Integer.parseInt(getProperties().getProperty("query.numResults")),
                        Integer.parseInt(getProperties().getProperty("query.numQueries")),
                        0.0 /*alfa*/,
                        Double.parseDouble(getProperties().getProperty("dataset.spaceMaxValue")),
                        getProperties().getProperty("query.type"),
                        getProperties().getProperty("query.keywords"),
                        Integer.parseInt(getProperties().getProperty("query.numMostFrequentTerms")),
                        Integer.parseInt(getProperties().getProperty("query.numWarmUpQueries")),
                        prefIndex, rTree,
                        Integer.parseInt(getProperties().getProperty("query.neighborhood")),
                        Boolean.parseBoolean(getProperties().getProperty("experiment.plusMode")),
                        Double.parseDouble(getProperties().getProperty("query.radius")));
            } else {
                throw new RuntimeException("Experiment: '" + getProperties().getProperty("experiment.name") + "' was not developed yet!");
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

    private String getDataSet() {
        String dataset = getProperties().getProperty("dataset.featuresFile");
        if (dataset.indexOf("/") != -1) {
            dataset = dataset.substring(dataset.lastIndexOf("/") + 1);
        }
        if (dataset.indexOf(".") != -1) {
            dataset = dataset.substring(0, dataset.lastIndexOf("."));
        }
        if (dataset.indexOf("-") != -1) {
            dataset = dataset.substring(0, dataset.lastIndexOf("-"));
        }
        return dataset;
    }

    private StarRTree createRtree() throws FileNotFoundException, IOException, ClassNotFoundException {
        StarRTree rTree = new StarRTree(this, "",
                getProperties().getProperty("experiment.folder") + "/rtree",
                Integer.parseInt(getProperties().getProperty("srtree.dimensions")),
                Integer.parseInt(getProperties().getProperty("srtree.cacheSize")),
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                Integer.parseInt(getProperties().getProperty("srtree.tree.minNodeCapacity")),
                Integer.parseInt(getProperties().getProperty("srtree.tree.maxNodeCapacity")));

        LoadRTree.load(rTree, getProperties().getProperty("dataset.objectsFile"));
        return rTree;
    }

    private PreferenceIndex createSpatialInvertedIndex(PreferenceFileTermManager fileTermManager,
            PreferenceTreeTermManager treeTermManager, boolean constructionTime) {
        return new PreferenceIndex(this,
                getProperties().getProperty("experiment.folder"),
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                Integer.parseInt(getProperties().getProperty("s2i.treesOpen")),
                fileTermManager, treeTermManager,
                constructionTime);
    }
    
    private RangeIndex createRangeIndex(RangeFileTermManager fileTermManager,
            RangeTreeTermManager treeTermManager, boolean constructionTime) {
        return new RangeIndex(this,
                getProperties().getProperty("experiment.folder"),
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                Integer.parseInt(getProperties().getProperty("s2i.treesOpen")),
                fileTermManager, treeTermManager,
                constructionTime);
    }
    
    private TextIndex createTextIndex(TextFileTermManager fileTermManager,
            TextTreeTermManager treeTermManager, boolean constructionTime) {
        return new TextIndex(this,
                getProperties().getProperty("experiment.folder"),
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                Integer.parseInt(getProperties().getProperty("s2i.treesOpen")),
                fileTermManager, treeTermManager,
                constructionTime);
    }

    private PreferenceTreeTermManager createTreeTermManager() {
        return new PreferenceTreeTermManager(this, getProperties().getProperty("experiment.folder"),
                Integer.parseInt(getProperties().getProperty("s2i.tree.dimensions")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.cacheSize")),
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.minNodeCapacity")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.maxNodeCapacity")),
                Integer.parseInt(getProperties().getProperty("s2i.treesOpen")));
    }

    private PreferenceFileTermManager createFileTermManager() {
        return new PreferenceFileTermManager(this,
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                getProperties().getProperty("experiment.folder") + "/s2i",
                Integer.parseInt(getProperties().getProperty("s2i.fileCacheSize")));
    }
    
    private RangeTreeTermManager createRangeTreeTermManager() {
        return new RangeTreeTermManager(this, getProperties().getProperty("experiment.folder"),
                Integer.parseInt(getProperties().getProperty("s2i.tree.dimensions")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.cacheSize")),
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.minNodeCapacity")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.maxNodeCapacity")),
                Integer.parseInt(getProperties().getProperty("s2i.treesOpen")));
    }

    private RangeFileTermManager createRangeFileTermManager() {
        return new RangeFileTermManager(this,
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                getProperties().getProperty("experiment.folder") + "/s2i",
                Integer.parseInt(getProperties().getProperty("s2i.fileCacheSize")));
    }
    
        private TextTreeTermManager createTextTreeTermManager() {
        return new TextTreeTermManager(this, getProperties().getProperty("experiment.folder"),
                Integer.parseInt(getProperties().getProperty("s2i.tree.dimensions")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.cacheSize")),
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.minNodeCapacity")),
                Integer.parseInt(getProperties().getProperty("s2i.tree.maxNodeCapacity")),
                Integer.parseInt(getProperties().getProperty("s2i.treesOpen")));
    }

    private TextFileTermManager createTextFileTermManager() {
        return new TextFileTermManager(this,
                Integer.parseInt(getProperties().getProperty("disk.blockSize")),
                getProperties().getProperty("experiment.folder") + "/s2i",
                Integer.parseInt(getProperties().getProperty("s2i.fileCacheSize")));
    }

    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            args = new String[]{"framework.properties"};
        }
        ExperimentRunner runner = new ExperimentRunner(new SKFrameworkFactory(), args[0]);
        runner.run();
    }
}
class SKFrameworkFactory implements ExperimentFactory {

    public ExperimentManager produce(Properties cfg, int round) {
        return new SKPQFramework(cfg, round);
    }
}
