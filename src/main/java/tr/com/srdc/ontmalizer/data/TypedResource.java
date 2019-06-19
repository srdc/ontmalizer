/**
 *
 */
package tr.com.srdc.ontmalizer.data;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Mustafa
 *
 */
public class TypedResource {

    private boolean isDatatype;
    private Resource resource;
    
    // used to hold on to the property we get from the ontology when determining the resource's type
    private OntProperty property;

    public TypedResource() {
    }

    public TypedResource(boolean isDatatype, Resource resource) {
        this.isDatatype = isDatatype;
        this.resource = resource;
    }

    /**
     * @return the isDatatype
     */
    public boolean isDatatype() {
        return isDatatype;
    }

    /**
     * @param isDatatype the isDatatype to set
     */
    public void setDatatype(boolean isDatatype) {
        this.isDatatype = isDatatype;
    }

    /**
     * @return the resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * @return the property that links to this resource (may be null)
     */
    public OntProperty getProperty() {
        return property;
    }
    
    /**
     * @param the property that links to this resource
     */
    public void setProperty(OntProperty avfrOnProp) {
        this.property = avfrOnProp;
        
    }

}
