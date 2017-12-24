package similarity;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.metrics.StringMetrics;
import org.simmetrics.tokenizers.Tokenizers;

import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;

public class TextSimilarity {
	

		
		public static void main(String[] args) throws Exception {
			
			String tagSet1 = "Ocean's Twelve is a 2004 American comedy heist film, which acts as the sequel to 2001's Ocean's Eleven. Like its predecessor, which was a remake of the 1960 heist film Ocean's 11, the film was directed by Steven Soderbergh and used an ensemble cast. It was released in the United States on December 10, 2004. A third film, Ocean's Thirteen, was released on June 8, 2007, in the United Statesâ€”thus forming the Ocean's Trilogy. The film stars George Clooney, Brad Pitt, Matt Damon, Catherine Zeta-Jones, Andy GarcÃ­a, Julia Roberts, Don Cheadle, Bernie Mac. It was the tenth highest-grossing film of 2004";
			String tagSet2 = "Ocean's Thirteen is a 2007 American comedy heist film directed by Steven Soderbergh and starring an ensemble cast. It is the third and final film in the Soderbergh series (Ocean's Trilogy) following the 2004 sequel Ocean's Twelve and the 2001 film Ocean's Eleven, which itself was a remake of the 1960 Rat Pack film Ocean's 11. All the male cast members reprise their roles from the previous installments, but neither Julia Roberts nor Catherine Zeta-Jones return.Al Pacino and Ellen Barkin joined the cast as their new targets.Filming began in July 2006 in Las Vegas and Los Angeles, based on a script by Brian Koppelman and David Levien. The film was screened for the Out of Competition presentation at the 2007 Cannes Film Festival. It was released on June 8, 2007, in the United States and in several countries in the Middle East on June 6";
			
/*			String output = ExudeData.getInstance().filterStoppings(tagSet1);
			System.out.println("output : "+output);
			tagSet1 = output;
			
			output = ExudeData.getInstance().filterStoppings(tagSet2);
			System.out.println("output : "+output);
			tagSet2 = output;*/			

			
			
		/*	10:41:29,875 INFO  [STDOUT] Running.. ALL_SEARCH_TYPE
			10:41:30,847 INFO  [STDOUT] User tags 5 for user id 3028
			10:41:31,932 INFO  [STDOUT] Relevant Items size 62 for user id 3028
			10:41:32,501 INFO  [STDOUT] comparing excellent excellent just excellent great soundtrack funny shit annoying  with scenic hostage meryl streep 
			10:41:33,247 INFO  [STDOUT] comparing excellent excellent just excellent great soundtrack funny shit annoying  with to see 19th century a. s. byatt adaptation victorian nudity (full frontal - notable) 
			10:41:33,788 INFO  [STDOUT] comparing excellent excellent just excellent great soundtrack funny shit annoying  with werewolf 
			10:41:34,327 INFO  [STDOUT] comparing excellent excellent just excellent great soundtrack funny shit annoying  with ensemble cast food multiple storylines 
			10:41:34,900 INFO  [STDOUT] comparing excellent excellent just excellent great soundtrack funny shit annoying  with sci-fi. dark clowns ghosts horror spielberg thriller 80s ghosts child in peril dogs epilepsy warning father daughter relationship haunted house mother daughter relationship paranormal smoking spirits storm swimming pool television toys cant remember tumeys dvds dont want to see craig t.nelson 70mm afi 100 (movie quotes) afi 100 (thrills) 
			10:41:35,483 INFO  [STDOUT] comparing excellent excellent just excellent great soundtrack funny shit annoying  with families family keanu reeves cant remember k movie mysterious parcel ron howard 
			10:41:36,047 INFO  [STDOUT] comparing excellent excellent just excellent great soundtrack funny shit annoying  with family bonds would see again young romance youth less than 300 ratings 
			ogma 95 in netflix queue chefs estate family gatherings father-son relationship forest freemason letters library vhs male nudity racism secrets siblings speech waitress dogma voto de castidade. bibliothek low budget nudity (topless) nudity (topless - brief) dogme95 sexual abuse birthday family incest suicide traumatic childhood not corv lib skinut*/ 
			
			System.out.println((float)TextSimilarity.computeDiceSimilarity(tagSet1, tagSet2));
			System.out.println((float)TextSimilarity.computeConsineSimilarity("car", "bus"));
			System.out.println((float)TextSimilarity.computeJakardSimilarity(tagSet1, tagSet2));
			System.out.println((float)TextSimilarity.computeEuclidianSimilarity(tagSet1, tagSet2));
					
		}
		
		/**
		 * @param inputString
		 * @return
		 */
		public static String filterStoppings(String inputString){
			
			String output = null;
			try {
				output = ExudeData.getInstance().filterStoppings(inputString);
			} catch (InvalidDataException e) {

				e.printStackTrace();
			}
			//System.out.println("output : "+output);
			
			return output;
		}
		
		
		public static String getSwearWords(String inputString){
			
			String output = null;
			try {
				output = ExudeData.getInstance().getSwearWords(inputString);
			} catch (InvalidDataException e) {

				e.printStackTrace();
			}
			//System.out.println("output : "+output);
			
			return output;
		}		
		
		/**
		 * @param inputString
		 * @return
		 */
		public static String filterStoppingsKeepDuplicates(String inputString){
			
			String output = null;
			try {
				output = ExudeData.getInstance().filterStoppingsKeepDuplicates(inputString);
			} catch (InvalidDataException e) {

				e.printStackTrace();
			}
			//System.out.println("output : "+output);
			
			return output;
		}

		
		/**
		 * It computes consine similarity between string vectors
		 * @param tagSet1
		 * @param tagSet2
		 * @return
		 */
		public static float computeConsineSimilarity(String tagSet1, String tagSet2){
			StringMetric metric = StringMetrics.cosineSimilarity();
			
			//StringMetric metric2 = 
		      //      with(new CosineSimilarity<String>())
		        //    .tokenize(Tokenizers.whitespace())
		          //  .tokenize(Tokenizers.qGram(3))
		            //.build();
			return metric.compare(tagSet1, tagSet1);			
			
			
		}
		
		/**
		 * @param tagSet1
		 * @param tagSet2
		 * @return
		 */
		public static float computeJakardSimilarity(String tagSet1, String tagSet2){
			
			StringMetric metric = StringMetrics.jaccard();
			return metric.compare(tagSet1, tagSet1);			
		}
		 
		/**
		 * @param tagSet1
		 * @param tagSet2
		 * @return
		 */
		public static float computeDiceSimilarity(String tagSet1, String tagSet2){
			StringMetric metric = StringMetrics.dice();
			return metric.compare(tagSet1, tagSet1);				
			
		}
		
		/**
		 * @param tagSet1
		 * @param tagSet2
		 * @return
		 */
		public static float computeEuclidianSimilarity(String tagSet1, String tagSet2){
			StringMetric metric = StringMetrics.euclideanDistance();
			return metric.compare(tagSet1, tagSet1);				
		}
			

	


}
