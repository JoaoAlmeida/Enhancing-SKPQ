package lodweb;

import java.io.Serializable;

public class ResourceView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String resourceId;
	
	private String subject;
	
	private String property;
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	private String object;

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}





}
