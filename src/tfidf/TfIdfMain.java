/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tfidf;

import java.io.FileNotFoundException;
import java.io.IOException;


public class TfIdfMain {
    
    /**
     * Main method
     * @param args
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void cosineSimilarityTFIDF(String one, String two) throws FileNotFoundException, IOException
    {
        DocumentParser dp = new DocumentParser();
        //dp.parseFiles("D:\\folderToIndex");
        
       // dp.parseFiles("D:\\folderToIndex");
        
        dp.parseStrings(one,two);
        dp.tfIdfCalculator(); //calculates tfidf
        dp.getCosineSimilarity(); //calculated cosine similarity

    }
}
