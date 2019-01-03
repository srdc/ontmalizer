package tr.com.srdc.ontmalizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping.IllegalPrefixException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tr.com.srdc.ontmalizer.data.TypedResource;
import tr.com.srdc.ontmalizer.helper.Constants;
import tr.com.srdc.ontmalizer.helper.NamingUtil;
import tr.com.srdc.ontmalizer.helper.XSDUtil;

/**
 * XML2OWLMapper Class converts XML instances to RDF Models with respect to an
 * ontology. This ontology must be an ontology that was created from the schema
 * of the XML instance.
 *
 * @author Atakan Kaya, Mustafa Yuksel
 *
 */
public class XML2OWLMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(XML2OWLMapper.class);

    // Variables for parsing XML instance
    private DocumentBuilderFactory dbf = null;
    private DocumentBuilder db = null;
    private Document document = null;

    // Variables for creating RDF model
    private Model model = null;

    // Variables for storing the mapped ontology
    private OntModel ontology = null;

    // Variables to uniquely name resources
    private int no = 0;	// This is a random value between 0 and 9999999 for each instance of this class.
    private Map<String, Integer> count = null; // This map holds how many times a resource has been used while naming.

    // Property prefixes
    private String opprefix = Constants.DEFAULT_OBP_PREFIX;
    private String dtpprefix = Constants.DEFAULT_DTP_PREFIX;

    private String baseURI = Constants.ONTMALIZER_INSTANCE_BASE_URI;
    private String baseNS = Constants.ONTMALIZER_INSTANCE_BASE_NS;

//	private ArrayList<OntClass> abstractClasses 	= null;
    private ArrayList<OntClass> mixedClasses = null;

    private String NS = null;
    private String nsPrefix = null;

    private XSD2OWLMapper xsdMapper;
    private int prefixCount = 0;

    /**
     * Creates a new XML2OWLMapper instance.
     *
     * @param xmlFile - XML File to be converted
     * @param mapping - mapping must be an XSD2OWLMapper instance which wraps
     * the ontology that was created from the schema of the XML instance.
     */
    public XML2OWLMapper(File xmlFile, XSD2OWLMapper mapping) {
        try {
            initDocumentBuilder();
            document = db.parse(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("{}", e.getMessage());
        }

        initializeEnvironment(mapping);
    }

    /**
     * Creates a new XML2OWLMapper instance.
     *
     * @param xmlInputStream - XML InputStream to be converted
     * @param mapping - mapping must be an XSD2OWLMapper instance which wraps
     * the ontology that was created from the schema of the XML instance.
     */
    public XML2OWLMapper(InputStream xmlInputStream, XSD2OWLMapper mapping) {
        try {
            initDocumentBuilder();
            document = db.parse(xmlInputStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("{}", e.getMessage());
        }

        initializeEnvironment(mapping);
    }

    /**
     * Creates a new XML2OWLMapper instance.
     *
     * @param xmlURL - XML URL to be converted
     * @param mapping - mapping must be an XSD2OWLMapper instance which wraps
     * the ontology that was created from the schema of the XML instance.
     */
    public XML2OWLMapper(URL xmlURL, XSD2OWLMapper mapping) {
        try {
            initDocumentBuilder();
            InputSource inputSource = new InputSource(xmlURL.openStream());
            inputSource.setSystemId(xmlURL.toExternalForm());
            document = db.parse(inputSource);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOGGER.error("{}", e.getMessage());
        }

        initializeEnvironment(mapping);
    }

    /**
     * Initializes the XML DocumentBuilder variables
     *
     * @throws ParserConfigurationException
     */
    private void initDocumentBuilder() throws ParserConfigurationException {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringComments(true);
        db = dbf.newDocumentBuilder();
    }

    /**
     * initializes the variables required to map xml 2 owl
     *
     * @param mapping
     */
    private void initializeEnvironment(XSD2OWLMapper mapping) {
        xsdMapper = mapping;
        ontology = mapping.getOntology();
//		abstractClasses = mapping.getAbstractClasses();
        mixedClasses = mapping.getMixedClasses();

        model = ModelFactory.createDefaultModel();

        Random random = new Random();
        no = random.nextInt(9999999);

        // Get all the named resources the count map
        count = new HashMap<>();
        ResIterator it = ontology.listResourcesWithProperty(null);
        while (it.hasNext()) {
            Resource resource = (Resource) it.next();
            if (resource != null && resource.getURI() != null) {
                count.put(resource.getURI(), 1);
            }
        }

        
        //Updated to use 'setNsPrefixes' since setNsPrefix is no longer a method in Jena 3.7
        // Import all the namespace prefixes to the model
        Map<String, String> nsmap = ontology.getBaseModel().getNsPrefixMap();
        Map<String, String> newNsMap = new HashMap<String, String>();
        Iterator<String> keys = nsmap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            // The default namespace (i.e. the null prefix) should always refer to baseURI in instances
            if (key.equals("")) {
                //model.setNsPrefix("", baseURI);
                newNsMap.put("", baseURI);
            } else {
                //model.setNsPrefix(key, nsmap.get(key));
                newNsMap.put(key,  nsmap.get(key));
            }
        }
        model.setNsPrefixes(newNsMap);
        this.opprefix = mapping.getObjectPropPrefix();
        this.dtpprefix = mapping.getDataTypePropPrefix();
    }

    /**
     * Creates a new XML2OWLMapper instance. Note that use of this constructor
     * should be avoided due to a possible loss of information. For example,
     * abstract classes cannot be represented by an ontology file. These
     * features are supported by a XSD2OWLMapper instance.
     *
     * @param xmlFile - XML File to be converted
     * @param ontology - Output ontology file to write to.
     */
    public XML2OWLMapper(File xmlFile, File ontology) {
        this(xmlFile, new XSD2OWLMapper(ontology));
    }

    /**
     * Converts the XML instance to a RDF data model.
     */
    public void convertXML2OWL() {
        Element root = document.getDocumentElement();

        // Set namespace and its prefix if it is not set before.
        if (NS == null) {
            setNSPrefix(root);
        }

        OntClass rootType = xsdMapper.rootTypeMap.get(NS + root.getLocalName());
        if (rootType == null) {
            // add relevant details to NPE
            LOGGER.error("NS={}, rootlocalname={}", NS, root.getLocalName());
            LOGGER.error("root map: {}", xsdMapper.rootTypeMap);
            LOGGER.error("\n");
            throw new NullPointerException("rootType is null. Could not getOntClass for NS=" + NS + ", rootLocalName=" + root.getLocalName());
        }
        
        Resource modelRoot = model.createResource(baseURI
                + Constants.ONTMALIZER_INSTANCE_NAME_PREFIX
                + no
                + "_"
                + root.getLocalName()
                + "_"
                + count.get(rootType.getURI()), rootType);
        count.put(rootType.getURI(), count.get(rootType.getURI()) + 1);

        // First traverse the attributes of the root element
        traverseAttributes(root, modelRoot, rootType);

        // Then, proceed with child nodes
        NodeList children = root.getChildNodes();
        for (int i = 0, length = children.getLength(); i < length; i++) {
            traverseChildren(children.item(i), modelRoot, rootType);
        }
    }

    private void traverseAttributes(Node node, Resource subject, Resource subjectType) {
        NamedNodeMap attributes = node.getAttributes();
        LOGGER.debug("There are {} attributes for this node", attributes.getLength());
        for (int i = 0, length = attributes.getLength(); i < length; i++) {
            Node attributeNode = attributes.item(i);
            LOGGER.debug("Node: {}, attribute Node: {}, attribute local name: {}, subject: {}, subjectType:{}", node, attributeNode, attributeNode.getLocalName(), subject, subjectType);
            TypedResource atObjType = findObjectType(subjectType, attributeNode.getLocalName(), attributeNode, true);

            if (atObjType != null) {
                Property atProp = model.createProperty(NS + NamingUtil.createPropertyName(dtpprefix, attributes.item(i).getLocalName()));

                Literal value = model.createTypedLiteral(attributes.item(i).getNodeValue(), atObjType.getResource().getURI());
                if (subject != null) {
                    subject.addLiteral(atProp, value);
                }
            }
        }
    }

    private void traverseChildren(Node node, Resource subject, Resource subjectType) {
        if (node == null) {
            return;
        }
        LOGGER.debug("Traversing child. Node={}, subject={}, subjectType={}", node, subject, subjectType);
        TypedResource objectType = null;
        Resource object = null;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            LOGGER.debug("Node {} is an element node. Local name: {}, Node name: {}, Node namespace: {},", node, node.getLocalName(), node.getNodeName(), node.getNamespaceURI());
            objectType = findObjectType(subjectType, node.getLocalName(), node, false);
            if (objectType != null) {
            	LOGGER.debug("Object type: {} objectType.resource: {}", objectType, objectType.getResource());
                /**
                 * The type of the element might be overridden with the use of
                 * xsi:type, if the original type of the element is abstract, or
                 * a superclass. Therefore, we have to be sure about the actual
                 * type.
                 */
                Element element = (Element) node;
                LOGGER.trace("Type: {}", element.getAttribute("type"));
                String overriddenXsiType = element.getAttributeNS(Constants.XSI_NS, "type");
                LOGGER.trace("overridden type: {}", overriddenXsiType);
                if (overriddenXsiType != null && !overriddenXsiType.equals("")) {
                    String overriddenNS = null;
                    String overriddenType = null;
                    if (overriddenXsiType.contains(":")) {
                        String[] strarr = overriddenXsiType.split(":");
                        String prefix = strarr[0];
                        overriddenType = strarr[1];
                        overriddenNS = document.lookupNamespaceURI(prefix) + "#";
                    } else {
                        overriddenType = overriddenXsiType;
                        overriddenNS = NS;
                    }
                    objectType = findResourceType(overriddenNS + overriddenType);
                }

                if (!objectType.isDatatype()) {
                    if (objectType.getResource() == null) {
                        LOGGER.warn("Object type has null resource! ObjectType: {}", objectType);
                    }else {
                        if (!count.containsKey(objectType.getResource().getURI())){
                            LOGGER.warn("Haven't seen resource with URI '{}' before. Known URIs: {}", objectType.getResource().getURI(), count.keySet());
                            if (!count.containsKey(objectType.getResource().getURI())){
                                throw new NullPointerException("Count doesn't contain URI: " + objectType.getResource().getURI() 
                                +"\nfor resource: " + objectType.getResource() + "\n for objectType: " + objectType);
                            }
                        }
                        object = model.createResource(baseURI
                                + Constants.ONTMALIZER_INSTANCE_NAME_PREFIX
                                + no
                                + "_"
                                + objectType.getResource().getLocalName()
                                + "_"
                                + count.get(objectType.getResource().getURI()), objectType.getResource());
                        LOGGER.trace("objectType: {}, objectType.getResource(): {}", objectType, objectType.getResource());                    
                        count.put(objectType.getResource().getURI(), count.get(objectType.getResource().getURI()) + 1);
                        Property prop = model.createProperty(NS + NamingUtil.createPropertyName(opprefix, node.getLocalName()));
                        subject.addProperty(prop, object);
                  }
                } else if (node.getFirstChild() != null && node.getFirstChild().getNodeValue() != null) {
                    Property prop = model.createProperty(NS + NamingUtil.createPropertyName(dtpprefix, node.getLocalName()));
                    Literal value = model.createTypedLiteral(node.getFirstChild().getNodeValue().trim(), objectType.getResource().getURI());
                    subject.addLiteral(prop, value);
                }
            }
            if (node.hasAttributes() && objectType != null) {
                traverseAttributes(node, object, objectType.getResource());
            }

        } // This case is only valid for instances of mixed classes
        else if (node.getNodeType() == Node.TEXT_NODE) {
            if (node.getNodeValue().trim().equals("")) {
                return;
            }

            // Check if mixed class
            Iterator<OntClass> it = mixedClasses.iterator();
            while (it.hasNext()) {
                OntClass mixed = (OntClass) it.next();
                if (mixed.getURI().equals(subjectType.getURI())) {
                    break;
                }
                if (!it.hasNext()) {
                    return;
                }
            }

            Property hasTextContent = model.createProperty(NS + NamingUtil.createPropertyName(dtpprefix, Constants.MIXED_CLASS_DEFAULT_PROP_NAME));
            subject.addProperty(hasTextContent, node.getNodeValue().trim(), XSDDatatype.XSDstring);

        }
        LOGGER.debug("Object: {}", object);
        if (object != null) {
            NodeList list = node.getChildNodes();
            for (int i = 0, length = list.getLength(); i < length; i++) {
                traverseChildren(list.item(i), object, objectType.getResource());
            }
        }

    }

    private TypedResource findObjectType(Resource root, String prop, Node node, boolean isNodeAttribute) {
        Queue<OntClass> queue = new LinkedList<>();
        TypedResource result = new TypedResource();
        
        OntClass temp;
        if (!isNodeAttribute && !root.getNameSpace().equals(node.getNamespaceURI()+'#') && node.getNamespaceURI() != null) {
            LOGGER.info("Different name spaces: {}, {}\nNode:{}", root.getNameSpace(), node.getNamespaceURI(), node);
            
            return findResourceType(node.getNamespaceURI()+'#'+ node.getLocalName());
        } else {
            temp = (OntClass) root;
        }
        while (temp != null) {
            ExtendedIterator<OntClass> itres = temp.listSuperClasses();
            while (itres.hasNext()) {
                OntClass rescl = (OntClass) itres.next();
                if (rescl.isRestriction()) {
                    if (rescl.asRestriction().isAllValuesFromRestriction()) {
                        AllValuesFromRestriction avfres = rescl.asRestriction().asAllValuesFromRestriction();
                        /**
                         * In some cases, a resource can be both an object and
                         * datatype property. If, at the same time the prefixes
                         * opprefix and dtpprefix are identical, then we have to
                         * be careful. We check directly the RDF type of the
                         * AllValuesFrom restriction in this case.
                         */
                        if (avfres.getOnProperty().getLocalName().equals(NamingUtil.createPropertyName(opprefix, prop))
                                && opprefix.equals(dtpprefix)
                                && avfres.getOnProperty().isObjectProperty()
                                && avfres.getOnProperty().isDatatypeProperty()) {
                            result.setDatatype(findResourceType(avfres.getAllValuesFrom().getURI()).isDatatype());
                            result.setResource(avfres.getAllValuesFrom());
                            return result;
                        } else if (avfres.getOnProperty().getLocalName()
                                .equals(NamingUtil.createPropertyName(opprefix, prop))
                                && avfres.getOnProperty().isObjectProperty()) {
                            result.setDatatype(false);
                            result.setResource(avfres.getAllValuesFrom());
                            return result;
                        } else if (avfres.getOnProperty().getLocalName()
                                .equals(NamingUtil.createPropertyName(dtpprefix, prop))
                                && avfres.getOnProperty().isDatatypeProperty()) {
                            result.setDatatype(true);
                            result.setResource(avfres.getAllValuesFrom());
                            return result;
                        }
                    }
                }
            }

            ExtendedIterator<OntClass> it = temp.listSuperClasses();
            while (it.hasNext()) {
                OntClass superCl = (OntClass) it.next();
                if (!superCl.isRestriction() && !superCl.isEnumeratedClass()) {
                    queue.add(superCl);
                }
            }

            temp = queue.poll();
        }

        return null;
    }

    private TypedResource findResourceType(String uri) {
        TypedResource result = new TypedResource();

        if (uri.startsWith(XSDDatatype.XSD)) {
            result.setDatatype(true);
            result.setResource(XSDUtil.getXSDResource(uri.substring(uri.lastIndexOf("#"), uri.length())));
        } else {
            OntClass cls = ontology.getOntClass(uri);
            if (cls != null && cls.getRDFType(true).getURI().equals(Constants.OWL_CLASS_URI)) {
                result.setDatatype(false);
                result.setResource(cls);
                return result;
            }

            // This can be the case, since this function is called from different places
            if (!uri.endsWith(Constants.DATATYPE_SUFFIX)) {
                uri = uri + Constants.DATATYPE_SUFFIX;
            }

            cls = ontology.getOntClass(uri);
            if (cls != null && cls.getRDFType(true).getURI().equals(Constants.RDFS_TYPE_URI)) {
                result.setDatatype(true);
                result.setResource(cls);
            }

        }

        return result;
    }

    /**
     * @param out - Output stream to write the model to.
     * @param format - Output format may be one of these values;
     * "RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3".
     */
    public void writeModel(OutputStream out, String format) {
        if (format.equals("RDF/XML") || format.equals("RDF/XML-ABBREV")) {
            // This part is to add xml:base attribute to the RDF/XML and RDF/XML-ABBREV output
            RDFWriter writer = model.getWriter(format);
            writer.setProperty("xmlbase", baseNS);
            writer.write(model, out, baseURI);
        } else {
            model.write(out, format, baseURI);
        }
    }

    /**
     * @param out - Output writer to write the model to.
     * @param format - Output format may be one of these values;
     * "RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3".
     */
    public void writeModel(Writer out, String format) {
        if (format.equals("RDF/XML") || format.equals("RDF/XML-ABBREV")) {
            // This part is to add xml:base attribute to the RDF/XML and RDF/XML-ABBREV output
            RDFWriter writer = model.getWriter(format);
            writer.setProperty("xmlbase", baseNS);
            writer.write(model, out, baseURI);
        } else {
            model.write(out, format, baseURI);
        }
    }

    /**
     * @param out - Output stream to write the ontology to.
     * @param format - Output format may be one of these values;
     * "RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3".
     */
    public void writeOntology(OutputStream out, String format) {
        ontology.write(out, format, null);
    }

    /**
     * @param out - Output writer to write the ontology to.
     * @param format - Output format may be one of these values;
     * "RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3".
     */
    public void writeOntology(Writer out, String format) {
        ontology.write(out, format, null);
    }

    /**
     * @param baseNS - Don't use the # symbol. - Default
     * http://www.example.org/example
     */
    public void setBaseURI(String baseNS) {
        this.baseNS = baseNS;
        this.baseURI = baseNS + "#";
    }

    private void setNSPrefix(Element root) {
        LOGGER.debug("Root: {}", root);
        NS = root.getNamespaceURI() + "#";
        LOGGER.debug("NS: {}", NS);
        // This part tries to get a prefix. For example, if NS is A/B/C or A:B:C then it will get C.
        try {
            URI uri = new URI(root.getNamespaceURI());

            if (uri.isAbsolute()) {
                int last = NS.lastIndexOf('/');

                if (nsPrefix != null) {
                    model.setNsPrefix(nsPrefix, NS);
                } else {
                    try {
                        // Mustafa: If the XML instance has a prefix already, use it!
                        String xmlNSprefix = root.getPrefix();
                        if (xmlNSprefix != null && !xmlNSprefix.equals("")) {
                            model.setNsPrefix(xmlNSprefix, NS);
                        } else if (last != -1) {
                            model.setNsPrefix(root.getNamespaceURI().substring(last + 1), NS);
                        } else {
                            last = NS.lastIndexOf(':');
                            if (last != -1) {
                                model.setNsPrefix(root.getNamespaceURI().substring(last + 1), NS);
                            } else {
                                model.setNsPrefix("NS", NS);
                            }
                        }
                    } catch (IllegalPrefixException e) {
                        model.setNsPrefix("ont" + prefixCount++, NS);
                    }
                }
            } else {
                NS = "http://uri-not-absolute.com#";
                model.setNsPrefix("NS", NS);
            }
        } catch (URISyntaxException e) {
            NS = "http://uri-not-valid.com#";
            model.setNsPrefix("NS", NS);
        }
    }

    /**
     * @param namespace - namespace with # symbol
     * @param prefix - prefix for the namespace
     */
    public void setNSPrefix(String namespace, String prefix) {
        this.NS = namespace;
        this.nsPrefix = prefix;
        model.setNsPrefix(prefix, namespace);
    }

    public Model getModel() {
        return this.model;
    }
}
