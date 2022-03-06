package skpq.util;

import xxl.util.TreeHeapEntry;

/**
 *
 * @author joao
 */
public class SpatioTreeHeapEntry extends TreeHeapEntry{
    private double lowerBoundSpatialScore;    

    public SpatioTreeHeapEntry(Object entry){
        super(entry);
    }

    public void setLowerBoundSpatialScore(double spatioPartialScore){
        this.lowerBoundSpatialScore=spatioPartialScore;
    }
    
    public double getLowerBoundSpatialScore(){
        return this.lowerBoundSpatialScore;
    }

}