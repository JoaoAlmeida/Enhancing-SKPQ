package xml;



import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import node.Lodica;
import node.Node;
import node.NodeUtil;

public class WriteXMLFile {

	public static void createXML(List<Node> cnns) {

	  try {
		Set<Node> nodes = new HashSet<Node>();
		//Collections.sort(new ArrayList<Node>(Lodica.trainingSet));
		nodes.addAll(cnns);
		//Collections.sort(cnns);
		//NodeUtil.printNodes(nodes);
		if (Lodica.trainingSet!=null) {
			//NodeUtil.printNodes(Lodica.trainingSet);
			nodes.addAll(Lodica.trainingSet);
		} 
		
		try {
			NodeUtil.checkForDistinctIdsAndURIs(new ArrayList<Node>(nodes));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		
		doc.setXmlStandalone(true);
		
		Element rootElement = doc.createElement("graphml");
		doc.appendChild(rootElement);

        // setting attribute to element
        Attr xmlns = doc.createAttribute("xmlns");
        xmlns.setValue("http://graphml.graphdrawing.org/xmlns");
        rootElement.setAttributeNode(xmlns);
        
        
        //  graphTH element
        Element graph = doc.createElement("graph");
        rootElement.appendChild(graph);

        // edgedefault attribute to element
        Attr edgedefault = doc.createAttribute("edgedefault");
        edgedefault.setValue("undirected");
        graph.setAttributeNode(edgedefault);
        
        
        
        //  supercars element
        Element keyEle = doc.createElement("key");
        
        graph.appendChild(keyEle);
        //rootElement.appendChild(keyEle);

        // setting attribute to element
        Attr attId = doc.createAttribute("id");
        attId.setValue("name");
        keyEle.setAttributeNode(attId);
        
        // setting attribute to element
        Attr attFor = doc.createAttribute("target");
        attFor.setValue("node");
        keyEle.setAttributeNode(attFor); 
        
        // setting attribute to element
        Attr attName = doc.createAttribute("attr.name");
        attName.setValue("name");
        keyEle.setAttributeNode(attName);      
        
        // setting attribute to element
        Attr attType = doc.createAttribute("attr.type");
        attType.setValue("string");
        keyEle.setAttributeNode(attType);    		
		
        
		for (Node node : nodes) {
			//  node element
	        Element supercar = doc.createElement("node");
	        graph.appendChild(supercar);
	        //rootElement.appendChild(supercar);
	        // setting attribute to element
	        Attr attr = doc.createAttribute("id");
	        attr.setValue(node.getId());
	        supercar.setAttributeNode(attr);
	        // data element
	        Element data = doc.createElement("data");
	        Attr attrType = doc.createAttribute("key");
	        attrType.setValue("name");
	        data.setAttributeNode(attrType);
	        data.appendChild(doc.createTextNode(new ResourceImpl(node.getURI()).getLocalName()+" ("+node.getId()+") ("+node.getLabel()+")"));
	        //data.appendChild(doc.createTextNode(node.getURI()+" ("+node.getId()+") ("+node.getLabel()+")"));
	        supercar.appendChild(data);
		}
        
        
		for (Node node : nodes) {
			for (Node nodeTarget : node.getNodes()) {
		        //  supercars element
		        Element edge = doc.createElement("edge");
		        graph.appendChild(edge);
		       // rootElement.appendChild(edge);
		        // setting attribute to element
		        Attr attrsource = doc.createAttribute("source");
		        attrsource.setValue(node.getId());
		        edge.setAttributeNode(attrsource);
		        // setting attribute to element
		        Attr attrstarget = doc.createAttribute("target");
		        attrstarget.setValue(nodeTarget.getId());
		        edge.setAttributeNode(attrstarget); 
			}
		
		}
           
        
        
		//supercar.appendChild(data);

        
       

		

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("lodica.xml"));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		System.out.println("File saved!");

	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
	}
}
