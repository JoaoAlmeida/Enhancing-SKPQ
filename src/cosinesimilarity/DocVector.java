package cosinesimilarity;

import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVectorFormat;

public class DocVector
{
	public Map<String, Integer> terms;
    public OpenMapRealVector vector;
    
    public DocVector(Map<String, Integer> terms)
    {
        this.terms = terms;
        this.vector = new OpenMapRealVector(terms.size());        
    }

    public void setEntry(String term, Float freq)
    {
        if (terms.containsKey(term))
        {
            int pos = terms.get(term);
            vector.setEntry(pos, freq);
        }
    }

    public void normalize()
    {
        double sum = vector.getL1Norm();
        vector = (OpenMapRealVector) vector.mapDivide(sum);
    }

    @Override
    public String toString()
    {
        RealVectorFormat formatter = new RealVectorFormat();
        return formatter.format(vector);
    }
}
