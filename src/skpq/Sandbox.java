package skpq;

import java.text.DecimalFormat;

import cosinesimilarity.LuceneCosineSimilarity;

public class Sandbox {

	public static void main(String[] args) {
 	 
//		System.out.println("Convertendo Double para Float");
		String str = "44 25.3088637 55.3689992 (amenity) (hospital) Al Taawun Mall";
		String[] strVec = str.split("\\)");
		System.out.println(strVec[strVec.length - 1].trim());
		
//		System.out.println(LuceneCosineSimilarity.getCosineSimilarity("casa azul verde roxo vermelho", "verde"));
		
//		double x = 5.13466213E1;
		
//		System.out.println("x = " + (float) x);
	}

}
