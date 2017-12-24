package node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;

import util.StringUtilsNode;

public class NodeOld implements Comparable<NodeOld> {

	String id;

	String URI;

	String label;

	String content;
	
	int userId;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	List<NodeOld> nodes = new ArrayList<NodeOld>();

	String domain;

	public NodeOld(String label, NodeOld node) {
		super();
		this.label = label;
		this.addNode(node);
	}

	public NodeOld(String label, String uri) {
		this.label = label;
		this.URI = uri;
	}

	public NodeOld(String id, String label, String uri) {
		this.id = id;
		this.label = label;
		this.URI = uri;
	}
	
	public NodeOld(String id, String label, String uri, int userId) {
		this.id = id;
		this.label = label;
		this.URI = uri;
		this.userId = userId;
	}	

	public NodeOld(String id) {
		this.id = id;
	}

	public NodeOld() {
	}

	/**
	 * 
	 * @param testKey
	 * @return
	 */
	public double getTopValueByKey(String testKey) {
		double top = 0d;
		for (String key : relationalFeatures.keySet()) {
			if (key.equals(testKey)) {
				double toptest = relationalFeatures.get(key);
				if (toptest > top) {
					top = toptest;
				}
			}
		}
		return top;
	}

	public Map<String, Double> getRelationalFeatures() {
		if (relationalFeatures == null) {
			relationalFeatures = new HashMap<String, Double>();
		}
		return relationalFeatures;
	}

	private String getAttributeString(List<Resource> resources) {

		StringBuilder attributeString = new StringBuilder(" ");

		for (int i = 0; (i < 3 && i < resources.size()); i++) {

			if (resources.get(i) != null && resources.get(i).getLocalName() != null
					&& !resources.get(i).getLocalName().contains("Thing")
					&& !StringUtilsNode.hasDigit(resources.get(i).getLocalName())) {
				attributeString.append(resources.get(i).getLocalName().toLowerCase() + " ");
			}

		}

		// System.out.println(attributeString.toString().trim());

		/*
		 * String a = StringUtils.replace(attributeString.toString().trim(),"_",
		 * " "); String b = StringUtils.replace(a,"-"," ");
		 * //System.out.println(b);
		 * System.out.println(TextSimilarity.filterStoppings(b));
		 * System.out.println(TextSimilarity.filterStoppings(b));
		 */

		return attributeString.toString().trim();

	}

	public void setRelationalFeatures(Map<String, Double> relationalFeatures) {
		this.relationalFeatures = relationalFeatures;
	}

	public Map<String, Double> relationalFeatures;

	public Map<String, String> observedAtrributes;

	public Map<String, String> observedAtrributesOriginal;

	public Map<String, String> getObservedAtrributesOriginal() {
		return observedAtrributesOriginal;
	}

	public void setObservedAtrributesOriginal(Map<String, String> observedAtrributesOriginal) {
		this.observedAtrributesOriginal = observedAtrributesOriginal;
	}

	public Map<String, String> getObservedAtrributes() {
		return observedAtrributes;
	}

	public void setObservedAtrributes(Map<String, String> observedAtrributes) {
		this.observedAtrributes = observedAtrributes;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getURI() {
		return URI;
	}

	public void setURI(String uri) {
		this.URI = uri;
	}

	public String getAttribute1() {
		return attribute1;
	}

	public void setAttribute1(String attribute1) {
		this.attribute1 = attribute1;
	}

	public String getAttribute2() {
		return attribute2;
	}

	public void setAttribute2(String attribute2) {
		this.attribute2 = attribute2;
	}

	public String getAttribute3() {
		return attribute3;
	}

	public void setAttribute3(String attribute3) {
		this.attribute3 = attribute3;
	}

	String attribute1;

	String attribute2;

	String attribute3;

	String attribute4;

	String attribute5;

	public String getAttribute5() {
		return attribute5;
	}

	public void setAttribute5(String attribute5) {
		this.attribute5 = attribute5;
	}

	public String getAttribute4() {
		return attribute4;
	}

	public void setAttribute4(String attribute4) {
		this.attribute4 = attribute4;
	}

	public String getLink1() {
		return link1;
	}

	public void setLink1(String link1) {
		this.link1 = link1;
	}

	public String getLink2() {
		return link2;
	}

	public void setLink2(String link2) {
		this.link2 = link2;
	}

	String link1;

	String link2;

	public List<String> getWikiPageExternalLinks() {
		return wikiPageExternalLinks;
	}

	public void setWikiPageExternalLinks(List<String> wikiPageExternalLinks) {
		this.wikiPageExternalLinks = wikiPageExternalLinks;
	}

	public List<String> getRdfsSeeAlso() {
		return rdfsSeeAlso;
	}

	public void setRdfsSeeAlso(List<String> rdfsSeeAlso) {
		this.rdfsSeeAlso = rdfsSeeAlso;
	}

	public List<String> getOwlSameAs() {
		return owlSameAs;
	}

	public void setOwlSameAs(List<String> owlSameAs) {
		this.owlSameAs = owlSameAs;
	}

	List<String> wikiPageExternalLinks = new ArrayList<String>();

	List<String> rdfsSeeAlso = new ArrayList<String>();

	List<String> owlSameAs = new ArrayList<String>();

	public void setNodes(List<NodeOld> nodes) {
		this.nodes = nodes;
	}

	public List<NodeOld> neighbours = new ArrayList<NodeOld>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void addNode(NodeOld node) {
		if (this.getNodes() == null) {
			this.nodes = new ArrayList<NodeOld>();
		}
		
		if (!this.getNodes().contains(node)) {
			nodes.add(node);	
		}
		
	}

	public List<NodeOld> getNodes() {
		return nodes;
	}

	public List<NodeOld> getLinkedNodes(NodeOld node) {
		return node.getNodes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.id;
	}

	public boolean isUnlabeled() {
		return this.getLabel() == null;
	}

	@Override
	public int compareTo(NodeOld o) {
		if (Integer.valueOf(this.id) < Integer.valueOf(o.id)) {
			return -1;
		} else if (Integer.valueOf(this.id) > Integer.valueOf(o.id)) {
			return 1;
		} else {
			return 0;
		}
	}

}
