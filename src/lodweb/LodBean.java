package lodweb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import node.IConstants;
import node.Lodica;
import node.NodePrediction;
import node.SimpleTriple;
import node.SparqlWalk;



@ManagedBean(name="lodBean")
@ViewScoped
public class LodBean implements Serializable {
	
	public LodBean(){

	}
	
	private boolean isEvaluation = false;
	
    public boolean isEvaluation() {
		return isEvaluation;
	}
    
    public boolean getIsEvaluation() {
		return isEvaluation;
	}
    
	public void setEvaluation(boolean isEvaluation) {
		this.isEvaluation = isEvaluation;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public List<SelectItem> getDomains() {
		return domains;
	}
	public void setDomains(List<SelectItem> domains) {
		this.domains = domains;
	}

	private String domain;
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}

	private List<SelectItem> domains;	

	private String text;
	
	private String subject = "";
    
    public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	private String link;	
   
    public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    
    @PostConstruct
    public void init() {
    	domains = new ArrayList<SelectItem>();
        for (Resource resource : SparqlWalk.getDbpediaDomains(30)){
        	domains.add(new SelectItem(resource.getLocalName(),resource.getLocalName()));
		}
    }    

    /**
     * DROPDOWN MENU
     * @param event
     */
    public void fireSelection(ValueChangeEvent event) {
    	//System.out.println("Domain chosen : "+event.getNewValue()+", Old: "+event.getOldValue());
    	System.out.println("Domain chosen : "+event.getNewValue());
    	setEvaluation(false);
    	
    	lodica = null;
    	
    	populateTriples(SparqlWalk.searchDbpediaResourcesTriplesByDomain("http://dbpedia.org/ontology/"+event.getNewValue(),50));
    	
    }
    
    public void searchLod(String query){
    	System.out.println("A query eh "+ query);
    	this.setSubject(query);
    	populateTriples(SparqlWalk.searchDbpediaResourcesTriples(query));
    }
    


   
    Lodica lodica;
    
	public Lodica getIcaInstance() {
		if (lodica == null) {
			lodica = new Lodica();
		}
		return lodica;

	}
   
    

	public void getRelatedResources() throws Exception {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String resourceURI = params.get("resourceUri");
		this.subject = new ResourceImpl(resourceURI).getLocalName().toString();
		// populateTriples(SparqlWalk.getDbpediaObjecstTriplesBySubjectURI(resourceURI));

		
		Lodica.isLocalTest=false;
		//Lodica.useExternalClassifiedNodesForTrainingSet = true;
		
		List<NodePrediction> nodePredictions = null;//getIcaInstance().runLODICAFistVersionWithObservedAttributes(resourceURI);

		List<SimpleTriple> simpleTriples = new LinkedList<SimpleTriple>();

		for (NodePrediction nodePrediction : nodePredictions) {
			simpleTriples.add(new SimpleTriple(resourceURI,
					"("+nodePrediction.getNode().getId()+") :" +nodePrediction.getPredictedLabel() + ":" + nodePrediction.getPredictionScore(),
					nodePrediction.getNode().getURI()));
		}

		this.setEvaluation(true);
		populateTriples(simpleTriples);

	}
    
    public void updateLabelLIKE() throws Exception {
   	 	Map<String,String> params =FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
   	 	String resourceLIKE = params.get("resourceLIKE");
   	 	System.out.println("resourceLIKE "+ resourceLIKE);
   	 	throw new Exception("fix this with new code");
   	   // Lodica.updateLabelFromWeb(resourceLIKE,IConstants.LIKE);
   }
    
    public void updateLabelDISLIKE() throws Exception {
   	 	Map<String,String> params =FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
   	 	String resourceDISLIKE = params.get("resourceDISLIKE");
   	 	System.out.println("resourceDISLIKE "+ resourceDISLIKE);
   	 throw new Exception("fix this with new code");
   	 	//Lodica.updateLabelFromWeb(resourceDISLIKE,IConstants.DISLIKE);
   }      
    
   
    public void addMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }    
	
	private List<ResourceView> resourceViews = new ArrayList<ResourceView>();

	public List<ResourceView> getResourceViews() {
		return resourceViews;
	}
	public void setResourceViews(List<ResourceView> resourceViews) {
		this.resourceViews = resourceViews;
	}

	public void populateTriples(List<SimpleTriple> triples){
		
		//System.out.println(triples.size());
		
		resourceViews = new ArrayList<ResourceView>();
		
		
		for (SimpleTriple triple : triples) {
			ResourceView resourceView = new ResourceView();
			resourceView.setSubject(triple.getSubject());
			resourceView.setProperty(triple.getPredicate());
			resourceView.setObject(triple.getObject());
			this.resourceViews.add(resourceView);
		}
	
	}	

	
}
