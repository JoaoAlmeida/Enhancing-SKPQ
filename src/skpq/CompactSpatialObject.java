package skpq;

import java.io.Serializable;

@SuppressWarnings({ "rawtypes", "serial" })
public class CompactSpatialObject implements Serializable {

	//Change this id in case of big changes on this class. A exception will be launched to warn about the compatibility issues over serialization: https://blog.caelum.com.br/entendendo-o-serialversionuid/	
	private String uri;
	
	public CompactSpatialObject(String uri) {		
		this.uri = uri;
	}


	public String getURI() {
		return uri;
	}
}
