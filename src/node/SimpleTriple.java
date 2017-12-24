/**
 * 
 */
package node;


public class SimpleTriple {
	private String subject;
	private String predicate;
	private String object;
	
	/**
	 * Empty constructor
	 */
	public SimpleTriple() {
		super();
	}

	/**
	 * Fully loaded Constructor
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public SimpleTriple(String subject, String predicate, String object) {
		this();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the predicate
	 */
	public String getPredicate() {
		return predicate;
	}

	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return the object
	 */
	public String getObject() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setObject(String object) {
		this.object = object;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Subject: " + this.subject + "], [Predicate: " + this.predicate + "], [Object: " + this.object + "]";
	}
	
	
}
