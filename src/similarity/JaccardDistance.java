package similarity;

public class JaccardDistance {

	public static double apply(String a, String b) {
				
		JaccardSimilarity js = new JaccardSimilarity();		
		
		return 1 - js.apply(a, b);		
	}
	
	public static void main(String[] args) {
		
		
		System.out.println(JaccardDistance.apply("tasa", "casa"));
	}
}

