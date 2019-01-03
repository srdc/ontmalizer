package tr.com.srdc.ontmalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.EnumeratedClass;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XSOMParser;

import tr.com.srdc.ontmalizer.helper.AnnotationFactory;
import tr.com.srdc.ontmalizer.helper.Constants;
import tr.com.srdc.ontmalizer.helper.NamingUtil;
import tr.com.srdc.ontmalizer.helper.SimpleTypeRestriction;
import tr.com.srdc.ontmalizer.helper.URLResolver;
import tr.com.srdc.ontmalizer.helper.XSDUtil;

/**
 * @author Atakan Kaya, Mustafa Yuksel
 *
 * XSD2OWLMapper Class converts XML schemas to ontologies.
 *
 */
public class XSD2OWLMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(XSD2OWLMapper.class);

    // Variables to parse XSD schema
    private XSSchemaSet schemaSet = null;

    // Variables to create ontology
    private OntModel ontology = null;

    // To number classes named Class_#
    private int attrLocalSimpleTypeCount = 1;

    // To handle nodes with text content
    private Property hasValue = null;

    private String opprefix = Constants.DEFAULT_OBP_PREFIX;
    private String dtpprefix = Constants.DEFAULT_DTP_PREFIX;

    private ArrayList<OntClass> abstractClasses = null;
    private ArrayList<OntClass> mixedClasses = null;

    private XSOMParser parser;
    // Used by XML2OWLMapper
    /*default */ Map<String, OntClass> rootTypeMap = new HashMap<>();

    /**
     * Creates a new XSD2OWLMapper instance.
     *
     * @param xsdFile - An XML Schema File to be converted
     */
    public XSD2OWLMapper(File xsdFile) {
        initParser();
        parseXSD(xsdFile);
        initOntology();
    }

    /**
     * Creates a new XSD2OWLMapper instance.
     *
     * @param xsdInputStream - An XML Schema InputStream to be converted
     */
    public XSD2OWLMapper(InputStream xsdInputStream) {
        initParser();
        parseXSD(xsdInputStream);
        initOntology();
    }

    /**
     * Creates a new XSD2OWLMapper instance.
     *
     * @param xsdURL - An XML Schema URL to be converted
     */
    public XSD2OWLMapper(URL xsdURL) {
        initParser();
        parser.setEntityResolver(new URLResolver());
        parseXSD(xsdURL);
        initOntology();
    }

    

    /**
     * This constructor allows the caller to customize the parser before the XSD is parsed, by calling 
     * setParserAnnotationParser, setParserEntityResolver, setParserErrorHandler
     * 
     * When using this constructor, the caller must call parseXSD before convertXSD2OWL.
     */
    public XSD2OWLMapper() {
        initParser();
        initOntology();
    }
    
    public void setParserAnnotationParser(AnnotationParserFactory annotationParserFactory) {
        parser.setAnnotationParser(annotationParserFactory);
    }
    
    public void setParserAnnotationParser(Class<?> annotationParser) {
        parser.setAnnotationParser(annotationParser);
    }
    
    public void setParserEntityResolver (EntityResolver resolver) {
        parser.setEntityResolver(resolver);
    }
    
    public void setParserErrorHandler (ErrorHandler errorHandler) {
        parser.setErrorHandler(errorHandler);
    }
    
    private void initParser() {
        parser = new XSOMParser(SAXParserFactory.newInstance());
        parser.setAnnotationParser(new AnnotationFactory());
        parser.setErrorHandler(new MyErrorHandler());  
    }
    
    public static byte[] createChecksum(String filename) throws NoSuchAlgorithmException, IOException {
        //actually lets us get the file (if it isn't in a jar)
        if (filename.startsWith("file:")) {
            filename = filename.replaceFirst("file:", "");
        }
        
        InputStream fis;
        File file = new File(filename);
        if (file.exists()) {
            fis = new FileInputStream(filename);
        }
        else {
            fis =  XSD2OWLMapper.class.getResourceAsStream(filename);
        }
        
        MessageDigest complete = MessageDigest.getInstance("MD5");
        if (fis == null) {
            LOGGER.warn("File " + filename + " not found, taking hash of filename");
            return complete.digest(filename.getBytes());
        } else {
            byte[] buffer = new byte[1024];

            int numRead;
    
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
    
            fis.close();
            return complete.digest();
        }
    }
    
    
    public static String getMD5Checksum(String filename) {
        try {
            byte[] b = createChecksum(filename);
            String result = "";
            for (int i=0; i < b.length; i++) {
                result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            return result;
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.error("Error computing hash for {}, using filename as hash!", filename, e);
            return filename;
        }
    }
    
    
    public void parseXSD(File file) {
        try {
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
                       
            parser.parse(file);
            schemaSet = parser.getResult();
            LOGGER.info("Schema size: {}, Schema Set: {}", schemaSet.getSchemaSize(), schemaSet);
        } catch (SAXException | IOException e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    public void parseXSD(InputStream is) {
        try {
            parser.parse(is);
            schemaSet = parser.getResult();
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }
    // Used to customize logging for Errors    
    class MyErrorHandler implements ErrorHandler{

        @Override
        public void warning(SAXParseException paramSAXParseException) throws SAXException {
            LOGGER.debug("SAX ParseException, ", paramSAXParseException);
        }

        @Override
        public void error(SAXParseException paramSAXParseException) throws SAXException {
            LOGGER.error("SAX ParseException, ", paramSAXParseException);
            
        }

        @Override
        public void fatalError(SAXParseException paramSAXParseException) throws SAXException {
            LOGGER.error("FATAL ERROR: SAX ParseException, ", paramSAXParseException);
            throw paramSAXParseException;
        }
        
    }

    public void parseXSD(URL url) {
        try {
            InputSource inputSource = new InputSource(url.openStream());
            inputSource.setSystemId(url.toExternalForm());
            parser.parse(inputSource);
            schemaSet = parser.getResult();
            //schema = schemaSet.getSchema(1);
        } catch (IOException | SAXException e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    private void initOntology() {
        ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        Map<String, String> prefixMap = ontology.getNsPrefixMap();
        prefixMap.put(Constants.ONTMALIZER_BASE_URI_PREFIX, Constants.ONTMALIZER_BASE_URI);
        ontology.setNsPrefixes(prefixMap);

        hasValue = ontology.createOntProperty(Constants.ONTMALIZER_VALUE_PROP_NAME);

        abstractClasses = new ArrayList<>();
        mixedClasses = new ArrayList<>();
    }
    
    public URI convertURNtoURI(String targetNamespace) throws URISyntaxException {
        String uriString = targetNamespace;
        if (targetNamespace.startsWith("urn:")) {
            uriString = targetNamespace.replaceAll(":", "/");
            uriString = "http://www.urn.com/" + uriString;
        }
        return new URI(uriString);
    }

    /**
     * Converts the XML schema file to an ontology.
     */
    public void convertXSD2OWL() {
        
        Collection<XSSchema> schemaCollection = schemaSet.getSchemas();
        for (XSSchema schema : schemaCollection) {
            String mainURI;
            String nameSpace = schema.getTargetNamespace();
            if ("".equals(nameSpace)) {
                LOGGER.warn("Namespace for schema {} is empty string.", schema);
            }

            try {
                URI uri = new URI(schema.getTargetNamespace());
                if (uri.isAbsolute()) {
                    mainURI = uri.toString();
                } else {
                    String filename = schema.getLocator().getSystemId();
                    String schemaHash = getMD5Checksum(filename);
                    System.out.println("Filename: " + filename + ", md5: " + schemaHash);
                    
                    mainURI = filename +"/" + schemaHash + "/";
                }
            
            } catch (URISyntaxException e) {
                LOGGER.warn("Exception creating URI for {}", nameSpace, e);
                mainURI = "http://malformed-uri.com";
            }

            ontology.setNsPrefix("", mainURI + "#");
            
            LOGGER.info("Main URI: {}", mainURI);
            Iterator<XSSimpleType> simpleTypes = schema.iterateSimpleTypes();
            while (simpleTypes.hasNext()) {
                XSSimpleType type = (XSSimpleType) simpleTypes.next();
                if (type.isGlobal()) {
                    convertSimpleType(mainURI, type, null, null);
                }
            }
            
            Iterator<XSComplexType> complexTypes = schema.iterateComplexTypes();
            while (complexTypes.hasNext()) {
                XSComplexType type = (XSComplexType) complexTypes.next();
                if (type.isGlobal()) {
                    convertComplexType(mainURI, type, null, null);
                }
            }
    
            Iterator<XSElementDecl> elements = schema.iterateElementDecls();
            while (elements.hasNext()) {
                XSElementDecl element = (XSElementDecl) elements.next();
                if (element.isGlobal()) {
                    convertElement(mainURI, element, null);
                }
            }

            // these are all global
            Iterator<XSModelGroupDecl> groups = schema.iterateModelGroupDecls();
            while (groups.hasNext()) {
                convertModelGroupDecl(mainURI, groups.next());
            }
    
            // these are also all global
            Iterator<XSAttGroupDecl> attGroups = schema.iterateAttGroupDecls();
            while (attGroups.hasNext()) {
                convertAttributeGroup(mainURI, (XSAttGroupDecl) attGroups.next());
            }
    
            createDefaultTextPropertyForMixedClasses(mainURI);
        }
    }



    /* This function creates RDFS Datatypes except for Enumerated Simple Types.
	 * If there are both enumeration facets and other facets, then other facets will be ignored.
     */
    private OntClass convertSimpleType(String mainURI, XSSimpleType simple, String parentURI, String elementName) {
        String NS = simple.getTargetNamespace() + "#";
        String URI;
        String simpleTypeName = makeSimpleTypeName(simple, elementName);
        if (parentURI != null) {
             URI = getURI(parentURI, simpleTypeName);
        }else {
            URI = getURI(mainURI, simpleTypeName);
        }
        LOGGER.debug("NS: {}, URI: {}, simple: {}", NS, URI, simple);

        if (simple.isGlobal()) {
            // TODO: Mustafa: Why would we define new simple types in the XSD namespace?
            // The following if should not evaluate to true...
            if (NS.equals(XSD.getURI())) {
                // If element type is an XSD datatype
                // An example case:
                // <xs:element name="test" type="xs:string" />

                // adds a nullDatatype
                /*OntClass dataType = ontology.createOntResource(OntClass.class,
                        RDFS.Datatype,
                        parentURI + Constants.DATATYPE_SUFFIX);
                        */
                OntClass dataType = ontology.createOntResource(OntClass.class,
                    RDFS.Datatype,
                    URI + Constants.DATATYPE_SUFFIX);
                Resource xsdResource = XSDUtil.getXSDResource(simple.getName());
                
                if (xsdResource !=null && !"nullDatatype".equals(dataType.toString())) {
                    // Set super class to the element type
                    dataType.addSuperClass(xsdResource);

                    // Set Equivalent Datatype
                    OntClass eqDataType = ontology.createOntResource(OntClass.class,
                            RDFS.Datatype,
                            null);
                    eqDataType.addProperty(OWL2.onDatatype, xsdResource);
                    dataType.addEquivalentClass(eqDataType);
                }
                if ("nullDataType".equals(dataType.toString())) {
                    return null;
                }
                addTextAnnotation(simple, dataType);
                return dataType;
            } else if (parentURI != null) {
                // An example case:
                // <xs:element name="test" type="SimpleGlobalType1" />

                OntClass datatype = ontology.createOntResource(OntClass.class,
                        RDFS.Datatype,
                        parentURI + Constants.DATATYPE_SUFFIX);
                LOGGER.debug("datatype: {} used RDFS.Datatype", datatype);

                /**
                 * The following part adds equivalentClass to an enumarated
                 * class, if it is not added already. Like this:
                 *
                 * :simpletype1Datatype a rdfs:Datatype ; rdfs:subClassOf
                 * :simpletype1; owl:equivalentClass [ a rdfs:Datatype ;
                 * owl:onDatatype :simpletype1 ] .
                 */
                Resource onDatatype = ontology.getEnumeratedClass(URI); // If Enumerated Class
                if (onDatatype != null) {
                    Iterator<OntClass> prevEqClasses = datatype.listEquivalentClasses();
                    boolean insertedBefore = false;
                    while (prevEqClasses.hasNext()) {
                        Resource prevOnDatatype = prevEqClasses.next().getProperty(OWL2.onDatatype).getResource();
                        if (prevOnDatatype.getURI().equals(onDatatype.getURI())) {
                            insertedBefore = true;
                        }
                    }

                    if (!insertedBefore) {
                        datatype.addSuperClass(onDatatype);
                        OntClass equivClass = ontology.createOntResource(OntClass.class,
                                RDFS.Datatype,
                                null);
                        LOGGER.debug("equivClass: {} used RDFS.Datatype", equivClass);
                        equivClass.addProperty(OWL2.onDatatype, onDatatype);
                        datatype.addEquivalentClass(equivClass);
                    }
                }

                addTextAnnotation(simple, datatype);
                return datatype;
            } else if (simple.isList() || simple.isUnion()) {
                // To convert global union or global list simple type definitions.
                // It will create an rdfs:Datatype which is a subclass of xs:anySimpleType.
                return convertListOrUnion(URI);
            } else if (simple.isRestriction()) {
                return convertRestriction(mainURI, simple, URI);
            }
        } else if (simple.isLocal()) {
            if (simple.isList() || simple.isUnion()) {
                // It will create an rdfs:Datatype which is a subclass of xs:anySimpleType
                return convertListOrUnion(parentURI);
            } else if (simple.isRestriction()) {
                return convertRestriction(mainURI, simple, parentURI);
            }
        }

        return null;
    }

    private OntClass convertListOrUnion(String URI) {
        OntClass dataType = ontology.createOntResource(OntClass.class,
                RDFS.Datatype,
                URI + Constants.DATATYPE_SUFFIX);

        LOGGER.debug("datatype: {} used rdf datatyep", dataType);
        Resource anySimpleType = ontology.getResource(XSD.getURI() + "anySimpleType");
        dataType.addSuperClass(anySimpleType);

        OntClass eqDataType = ontology.createOntResource(OntClass.class,
                RDFS.Datatype,
                null);
        LOGGER.debug("equivDatatype: {} used RDFS.Datatype", eqDataType);
        eqDataType.addProperty(OWL2.onDatatype, anySimpleType);
        dataType.addEquivalentClass(eqDataType);

        return dataType;
    }

    private OntClass convertEnumeration(String mainURI, EnumeratedClass enumClass, XSSimpleType simple, SimpleTypeRestriction facets) {
        XSRestrictionSimpleType restriction = simple.asRestriction();

        XSType base = restriction.getBaseType();
        String baseNS = base.getTargetNamespace() + "#";
        String baseURI = getURI(mainURI, base);

        enumClass.addSuperClass(ontology.createClass(Constants.ONTMALIZER_ENUMERATEDVALUE_CLASS_NAME));

        if (baseNS.equals(XSD.getURI())) {
            enumClass.addSuperClass(ontology.createAllValuesFromRestriction(null,
                    hasValue,
                    XSDUtil.getXSDResource(base.getName())));
            enumClass.addSuperClass(ontology.createMaxCardinalityRestriction(null,
                    hasValue,
                    1));
        } else {
            enumClass.addSuperClass(ontology.createClass(baseURI));
        }

        OntClass enumSuperClass = ontology.createClass(Constants.ONTMALIZER_ENUMERATION_CLASS_NAME);
        //This statement should be added, but there is no Enumeration resource in any vocabulary. Only in LinkedModel
        //enumSuperClass.addSuperClass(DTYPE.Enumeration);
        //Individual enumResource = ontology.createIndividual(URI + "_Enumeration", enumSuperClass);
        Individual enumResource = ontology.createIndividual(enumClass.getURI() + "_Enumeration", enumSuperClass);

        for (int i = 0, length = facets.enumeration.length; i < length; i++) {
            String memberURI = enumClass.getURI() + "_"
                    + facets.enumeration[i].replace('%', '_')
                    .replace(' ', '_')
                    .replace('[', '_')
                    .replace(']', '_');
            // If there are other characters that are not allowed, replace methods can be added.

            Individual oneOf = ontology.createIndividual(memberURI, enumClass);
            oneOf.addProperty(hasValue, facets.enumeration[i], XSDUtil.getXSDDatatype(base.getName()));
            enumClass.addOneOf(ontology.getIndividual(oneOf.getURI()));

            enumResource.addProperty(hasValue, oneOf);
        }
        addTextAnnotation(simple, enumClass);
        return enumClass;
    }

    private OntClass convertRestriction(String mainURI, XSSimpleType simple, String URI) {
        XSRestrictionSimpleType restriction = simple.asRestriction();

        SimpleTypeRestriction facets = new SimpleTypeRestriction();
        facets.initFacets(restriction);

        XSSimpleType baseType = restriction.getBaseType().asSimpleType();
        String baseURI = getURI(mainURI, baseType);

        SimpleTypeRestriction baseFacets = new SimpleTypeRestriction();
        if (baseType.isRestriction()) {
            baseFacets.initFacets(baseType.asRestriction());
        }

        OntClass datatype = ontology.createOntResource(OntClass.class,
                RDFS.Datatype,
                URI + Constants.DATATYPE_SUFFIX);
        Resource onDatatype = null;

        if (baseType.getTargetNamespace().equals(XSDDatatype.XSD)) {
            onDatatype = XSDUtil.getXSDResource(baseType.getName());
        } else if (baseFacets.enumeration != null) {
            onDatatype = ontology.createResource(baseURI);
        } else {
            onDatatype = ontology.createOntResource(OntClass.class,
                    RDFS.Datatype,
                    baseURI + Constants.DATATYPE_SUFFIX);
        }
        // I did not use getResource methods because the class I am looking for may not be created yet.
        LOGGER.debug("datatype: {}, onDataType: {} both use rdf datatype", datatype, onDatatype);
        datatype.addSuperClass(onDatatype);

        OntClass equivClass = ontology.createOntResource(OntClass.class,
                RDFS.Datatype,
                null);
        LOGGER.debug("equiv class: {} used rdf data type", equivClass);
        equivClass.addProperty(OWL2.onDatatype, onDatatype);

        RDFList list = getFacetList(facets, restriction);
        if (list != null) {
            equivClass.addProperty(OWL2.withRestrictions, list);
        }
        LOGGER.debug("Data type: {}, equiv class: {}", datatype, equivClass);
        datatype.addEquivalentClass(equivClass);

        // If an enumeration facet is available, then we also generate an OWL:Class containing the EnumeratedValues.
        if (facets.enumeration != null) {
            return convertEnumeration(mainURI, ontology.createEnumeratedClass(URI, null),
                    simple,
                    facets);
        } else {
            addTextAnnotation(simple, datatype);
            return datatype;
        }

    }
    public String makeSimpleTypeName(XSSimpleType type, String elementName) {
        if (type.getName() == null) {
            return elementName + ".simpleType";
        } else {
            return type.getName();
        }
    }
    
    public String makeComplexTypeName(XSComplexType complex, String elementName) {
        if (complex.getName() == null) {
            return elementName + ".complexType";
        } else {
            return complex.getName();
        }
    }

    private OntClass convertComplexType(String mainURI, XSComplexType complex, String parentURI, String elementName) {
        OntClass complexClass = null;
        String URI;
        String typeName = makeComplexTypeName(complex, elementName);

        // if the name is null, it is an anon type and must have a (non-null) parent
        if (parentURI != null) {
            URI = getURI(parentURI, typeName);
        }else {
            URI = getURI(mainURI, typeName);
        }
        
        if (complex.isGlobal()) {
            // because global, do not include the parent URI here
            complexClass = ontology.createClass(getURI(mainURI, complex));
            if (parentURI != null) {
                OntClass element = ontology.createClass(parentURI);
                element.addSuperClass(complexClass);

                addTextAnnotation(complex, complexClass);
                // This case occurs when an element refers to a named complex type 
                // (it will get processed with the global ones, so we don't process it again here)
                return complexClass;
            }
        } else if (complex.isLocal()) {
            complexClass = ontology.createClass(URI);
        }

        XSType baseType = complex.getBaseType();
        String baseURI = getURI(mainURI, baseType);

        LOGGER.debug("Complex Type. Basetype: {}, baseURI: {}, complex: {}", baseType, baseURI, complex);
        
        if (baseType.isSimpleType()) {
            if (ontology.getOntResource(baseURI) != null
                    && !baseType.getTargetNamespace().equals(XSDDatatype.XSD)) // If base type is an enumeration simple type
            {
                complexClass.addSuperClass(ontology.getOntResource(baseURI));
            } else {
                if (baseType.getTargetNamespace().equals(XSDDatatype.XSD)) {
                    complexClass.addSuperClass(ontology.createAllValuesFromRestriction(null,
                            hasValue,
                            XSDUtil.getXSDResource(baseType.getName())));
                } else if (ontology.getOntResource(baseURI + Constants.DATATYPE_SUFFIX) != null) {
                    complexClass.addSuperClass(ontology.createAllValuesFromRestriction(null,
                            hasValue,
                            ontology.getOntResource(baseURI + Constants.DATATYPE_SUFFIX)));
                }
                complexClass.addSuperClass(ontology.createMaxCardinalityRestriction(null,
                        hasValue,
                        1));
            }

            Iterator<? extends XSAttributeUse> attributeUses = complex.getAttributeUses().iterator();
            while (attributeUses.hasNext()) {
                XSAttributeUse attributeUse = (XSAttributeUse) attributeUses.next();
                convertAttribute(mainURI, attributeUse, complexClass);
            }
        } else if (baseType.isComplexType()) {
            if (complex.getDerivationMethod() == XSType.EXTENSION) {
                complexClass.addSuperClass(ontology.createClass(baseURI));

                XSContentType expl = complex.getExplicitContent();
                if (expl != null) {
                    // particle is a thing in a schema that can be repeating
                    XSParticle particle = expl.asParticle();
                    if (particle != null) {
                        // term is an element or a model group (or a reference to one of those)
                        XSTerm term = particle.getTerm();
                        // model group = sequence or choice
                        if (term.isModelGroup()) {
                            convertGroup(mainURI, term.asModelGroup(), complexClass, complexClass.getURI());
                        } else if (term.isModelGroupDecl()) {
                            XSModelGroupDecl group = term.asModelGroupDecl();
                            OntClass groupClass = ontology.createClass(getURI(mainURI, group));
                            LOGGER.debug("Adding groupclass as superclass: {}", groupClass);
                            groupClass.addSubClass(complexClass);
                        }
                    }
                }
            } else if (complex.getDerivationMethod() == XSType.RESTRICTION
                    && baseURI.equals("http://www.w3.org/2001/XMLSchema#anyType")) {
                /* For the case
				 * <xs:complexType name="complex16">
				 * 	<xs:sequence>
				 * 		<xs:element name="element16_3" type="xs:string" />
				 * 	</xs:sequence>
				 * </xs:complexType>
                 */

                XSParticle particle = complex.getContentType().asParticle();
                if (particle != null) {
                    XSTerm term = particle.getTerm();
                    if (term.isModelGroup()) {
                        convertGroup(mainURI, term.asModelGroup(), complexClass, complexClass.getURI());
                    } else if (term.isModelGroupDecl()) {
                        XSModelGroupDecl group = term.asModelGroupDecl();
                        OntClass groupClass = ontology.createClass(getURI(mainURI, group));
                        LOGGER.trace("Adding groupclass as superclass: {}", groupClass);
                        groupClass.addSubClass(complexClass);
                    }
                }
            } else /* If this complex type is a restriction to its base.
				 * Note that this part is slightly different from TopBraid.
				 * I added the allowed restrictions by getDeclaredAttributeUses below
             */ {
                ontology.createClass(baseURI);
                complexClass.addSuperClass(ontology.createClass(baseURI));
            }

            Iterator<? extends XSAttributeUse> attributeUses = complex.getDeclaredAttributeUses().iterator();
            while (attributeUses.hasNext()) {
                XSAttributeUse attributeUse = (XSAttributeUse) attributeUses.next();
                convertAttribute(mainURI, attributeUse, complexClass);
            }
        }

        Iterator<? extends XSAttGroupDecl> attGroups = complex.iterateAttGroups();
        while (attGroups.hasNext()) {
            XSAttGroupDecl attGroup = (XSAttGroupDecl) attGroups
                    .next();
            OntClass attgClass = ontology.createClass(getURI(mainURI, attGroup));
            attgClass.addSubClass(complexClass);
        }

        if (complex.isMixed()) {
            mixedClasses.add(complexClass);
        }
        if (complex.isAbstract()) {
            abstractClasses.add(complexClass);
        }
        
        addTextAnnotation(complex, complexClass);

        return complexClass;
    }
    
    private void convertElement(String mainURI, XSElementDecl element, OntClass parent) {
        XSType elementType = element.getType();
        String URI;
        if (parent != null) {
            URI = getURI(parent.getURI(), element);
        }else {
            URI = getURI(mainURI, element);
        }

        OntClass elementTypeClass = null;

        if (elementType.isSimpleType()) {
            elementTypeClass = convertSimpleType(mainURI, elementType.asSimpleType(), URI, element.getName());
        } else if (elementType.isComplexType()) {
            elementTypeClass = convertComplexType(mainURI, elementType.asComplexType(), URI, element.getName());
        }
        if (element.isGlobal()) {
            rootTypeMap.put(element.getTargetNamespace() + "#" + element.getName(), elementTypeClass);
        }
        
        XSElementDecl subs = element.getSubstAffiliation();
        if (subs != null && elementTypeClass != null) {
            OntClass subsClass = ontology.createClass(getURI(mainURI, subs));
            
            LOGGER.debug("Adding superclass: {}", subsClass);
            subsClass.addSubClass(elementTypeClass);
        }
    }
    // mainURI is schema-specific
    private void convertAttribute(String mainURI, XSAttributeUse attributeUse, OntClass complexClass) {
        XSAttributeDecl attribute = attributeUse.getDecl();

        String NS = attribute.getType().getTargetNamespace();
        String URI = null;

        if (attribute.getType().isGlobal()) {
            URI = getURI(mainURI, attribute.getType());
        } else {
            // attribute types are almost always global
            URI = mainURI + "#Class_" + attrLocalSimpleTypeCount;
        }

        /**
         * xsd:IDREFS, xsd:ENTITIES and xsd:NMTOKENS are sequence-valued
         * datatypes which do not fit the RDF datatype model.
         * http://mail-archives.apache.org/mod_mbox/jena-users/201206.mbox/%3CCAFq2biyPYPKt0mnsnEajqwrQjOYH_geaFbVXvOuVeeDVYDgs2A@mail.gmail.com%3E
         */
        if (NS.equals(XSDDatatype.XSD)) {
            if (attribute.getType().getName().equals("IDREFS")
                    || attribute.getType().getName().equals("ENTITIES")
                    || attribute.getType().getName().equals("NMTOKENS")) {
                return;
            }
        }

        // Mustafa: All simple types are datatype properties now!
        Property prop = ontology.createDatatypeProperty(mainURI + "#" + NamingUtil.createPropertyName(dtpprefix, attribute.getName()));
        if (!NS.equals(XSDDatatype.XSD)) {
            convertSimpleType(mainURI, attribute.getType(), URI, attribute.getName());
            attrLocalSimpleTypeCount++;
        }
        //LOGGER.debug("Adding stuff to complex class={}", complexClass);
        if (attributeUse.isRequired()) {
            ontology.createCardinalityRestriction(null, prop, 1).addSubClass(complexClass);
        } else {
            ontology.createMaxCardinalityRestriction(null, prop, 1).addSubClass(complexClass);
        }

        if (NS.equals(XSDDatatype.XSD)) {
            ontology.createAllValuesFromRestriction(null,
                    prop,
                    XSDUtil.getXSDResource(attribute.getType().getName())).addSubClass(complexClass);
        } else if (ontology.getOntResource(URI + Constants.DATATYPE_SUFFIX) != null) {
            ontology.createAllValuesFromRestriction(null,
                    prop,
                    ontology.getOntResource(URI + Constants.DATATYPE_SUFFIX)).addSubClass(complexClass);
        } else if (ontology.getOntResource(URI) != null) {
            ontology.createAllValuesFromRestriction(null,
                    prop,
                    ontology.getOntResource(URI)).addSubClass(complexClass);
        }
    }

    
    // mainURI is schema-specific 
    // parentURI gets more specific as we recursivey call this method, but we don't want a new parent class for each recursion
    private void convertGroup(String mainURI, XSModelGroup group, OntClass parent, String parentURI) {
        assert(parent != null);
        
        LOGGER.debug("working with group={}, parent={}", group, parent);
        XSParticle[] particles = group.getChildren();

        int childParticleCounter = 1;
        for (XSParticle p : particles) {
            
            XSTerm term = p.getTerm();
            if (term.isElementDecl()) {
                XSElementDecl element = term.asElementDecl();
                Property prop = null;
                if (element.getType().isSimpleType()) {

                    prop = ontology.createDatatypeProperty(mainURI + "#" + NamingUtil.createPropertyName(dtpprefix, element.getName()));
                    if (element.getType().getTargetNamespace().equals(XSDDatatype.XSD)) {
                        AllValuesFromRestriction superClass;
                        String typeName = element.getType().getName();
                        if ("anyType".equals(typeName) || "anySimpleType".equals(typeName)) {
                            superClass = ontology.createAllValuesFromRestriction(null,
                                    prop, OWL.Thing);
                        } else {
                            superClass = ontology.createAllValuesFromRestriction(null,
                                prop, XSDUtil.getXSDResource(typeName));
                        }
                        LOGGER.debug("Adding superclass to parent: {}", superClass);
                        parent.addSuperClass(superClass);
                    } else {
                    AllValuesFromRestriction superClass = ontology.createAllValuesFromRestriction(null,
                            prop,
                            ontology.getResource(getURI(mainURI, element.getType()) + Constants.DATATYPE_SUFFIX));
                        LOGGER.debug("Adding superclass to parent: {}", superClass);
                        parent.addSuperClass(superClass);
                        XSSimpleType simpleType = element.getType().asSimpleType();
                        if (simpleType.isGlobal()) {
                            convertSimpleType(mainURI, simpleType, getURI(mainURI, element.getType()), element.getName());
                        } else {
                            convertSimpleType(mainURI, simpleType, getURI(parentURI, element.getType()), element.getName());
                        }
                    }
                } else if (element.getType().isComplexType()) {
                    prop = ontology.createObjectProperty(mainURI + "#" + NamingUtil.createPropertyName(opprefix, element.getName()));
                    // TODO: Mustafa: How will this be possible?
                    if (element.getType().getTargetNamespace().equals(XSDDatatype.XSD)) {
                        if (element.getType().getName().equals("anyType")) {
                            parent.addSuperClass(ontology.createAllValuesFromRestriction(null,
                                    prop,
                                    OWL.Thing));
                        } else {
                            parent.addSuperClass(ontology.createAllValuesFromRestriction(null,
                                    prop,
                                    XSDUtil.getXSDResource(element.getType().getName())));
                        }
                    } else if (element.getType().isGlobal()) {
                        
                        XSType type = element.getType();
                        String typeNS = type.getTargetNamespace();
                        LOGGER.trace("Main URI: {}, type target namespace: {}", mainURI, typeNS);
                        
                        //TODO is it correct to use the type namespace in all cases? There are at least some cases where this is necessary.  
                        //Resource resource = ontology.createResource(getURI(mainURI, element.getType()));
                        Resource resource;
                        if ("".equals(typeNS)) {
                            LOGGER.debug("typeNS is empty string so using mainURI: {}", mainURI);
                            resource = ontology.createResource(getURI(mainURI, element.getType()));
                        } else {
                            resource = ontology.createResource(getURI(typeNS, element.getType()));
                        }
                        parent.addSuperClass(ontology.createAllValuesFromRestriction(null,
                                prop, resource));
                    } else if (element.getType().isLocal()) {
                        OntClass elementTypeClass = null;
                        if (element.getType().isSimpleType()) {
                            elementTypeClass = convertSimpleType(mainURI, element.getType().asSimpleType(), parentURI, element.getName());
                        } else if (element.getType().isComplexType()) {
                            elementTypeClass = convertComplexType(mainURI, element.getType().asComplexType(), parentURI, element.getName());
                        }
                    LOGGER.debug("Adding class for element in group: {}", elementTypeClass);
                    parent.addSuperClass(ontology.createAllValuesFromRestriction(null,
                            prop, elementTypeClass));
                    }
                }
                // Cardinality constraints for the elements
                int minOccurs = p.getMinOccurs().intValue();
                int maxOccurs = p.getMaxOccurs().intValue();
                if (maxOccurs == 1) {
                    if (minOccurs == 1) {
                        parent.addSuperClass(ontology.createCardinalityRestriction(null, prop, 1));
                    } else // minOccurs can be 0 in this case, logically
                    {
                        parent.addSuperClass(ontology.createMaxCardinalityRestriction(null, prop, 1));
                    }
                } else {
                    parent.addSuperClass(ontology.createMinCardinalityRestriction(null, prop, minOccurs));
                    if (maxOccurs == -1) {
                        parent.addSuperClass(ontology.createMaxCardinalityRestriction(null, prop, Integer.MAX_VALUE));
                    } else {
                        parent.addSuperClass(ontology.createMaxCardinalityRestriction(null, prop, maxOccurs));
                    }
                }
            } else if (term.isModelGroup()) {
                XSModelGroup modelGroup = term.asModelGroup();
                String thisURI = parentURI + getGroupTypeString(modelGroup) + childParticleCounter+"_";
                convertGroup(mainURI, modelGroup, parent, thisURI);
            } else if (term.isModelGroupDecl()) {
                // group decl's are global
                XSModelGroupDecl groupDecl = term.asModelGroupDecl();
                OntClass groupClass = ontology.createClass(getURI(mainURI, groupDecl));
                LOGGER.debug("Adding groupclass as superclass: {}", groupClass);
                groupClass.addSubClass(parent);
            }
            childParticleCounter++;
            
        }
        LOGGER.debug("parent superclasses: {}", parent.listSuperClasses().toList());
    }
    
    public static String getGroupTypeString(XSModelGroup modelGroup) {
        Compositor compositor = modelGroup.getCompositor();
        switch (compositor) {
        case ALL:
            return "_all_";
        case CHOICE:
            return "_choice_";
        case SEQUENCE:
            return "_sequence_";
        default:
            LOGGER.error("Unknown XSModelGroup: {}", modelGroup);
            assert(false);
            return "_";
        }
    }
    
    /**
     * 
     * @param mainURI - mainURI for the current schema
     * @param group
     */
    private void convertModelGroupDecl(String mainURI, XSModelGroupDecl group) {
        OntClass groupClass = ontology.createClass(getURI(mainURI, group));
        convertGroup(mainURI, group.getModelGroup(), groupClass, groupClass.getURI());
    }
    
    /**
     * 
     * @param mainURI - mainURI for the current schema
     * @param attGroup
     */
    private void convertAttributeGroup(String mainURI, XSAttGroupDecl attGroup) {
        OntClass attgClass = ontology.createClass(getURI(mainURI, attGroup));

        Iterator<? extends XSAttributeUse> attributes = attGroup.iterateAttributeUses();
        while (attributes.hasNext()) {
            convertAttribute(mainURI, (XSAttributeUse) attributes.next(), attgClass);
        }
    }

    private void createDefaultTextPropertyForMixedClasses(String mainURI) {
        if (mixedClasses.isEmpty()) {
            return;
        }

        Property prop = ontology.createDatatypeProperty(mainURI + "#" + NamingUtil.createPropertyName(dtpprefix, Constants.MIXED_CLASS_DEFAULT_PROP_NAME));

        for (OntClass mixedClass : mixedClasses) {
            LOGGER.debug("adding subclass: {}", mixedClass);
            ontology.createAllValuesFromRestriction(null, prop, XSD.xstring).addSubClass(mixedClass);
            ontology.createMaxCardinalityRestriction(null, prop, 1).addSubClass(mixedClass);
        }
    }

    private void addTextAnnotation(XSType xsType, OntClass ontClass) {
        if (xsType.getAnnotation() != null && xsType.getAnnotation().getAnnotation() != null) {
            String textAnnotation = xsType.getAnnotation().getAnnotation().toString();
            ontClass.addProperty(RDFS.comment, textAnnotation);
        }
    }

    /**
     * @return returns the ontology. Abstract complex types and mixed attributes
     * cannot be represented in this ontology.
     */
    public OntModel getOntology() {
        return ontology;
    }

    /**
     * @param out - Output stream to write to.
     * @param format - Output format may be one of these values;
     * "RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3".
     */
    public void writeOntology(OutputStream out, String format) {
        ontology.write(out, format, null);
    }

    /**
     * @param writer - Output writer to write to.
     * @param format - Output format may be one of these values;
     * "RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3".
     */
    public void writeOntology(Writer writer, String format) {
        ontology.write(writer, format, null);
    }

    private RDFList getFacetList(SimpleTypeRestriction facets, XSRestrictionSimpleType restriction) {
        RDFList list = null;

        XSType temp = restriction.getBaseType();
        String NS = temp.getTargetNamespace();
        String name = temp.getName();

        while (!NS.equals(XSDDatatype.XSD)) {
            temp = temp.getBaseType();
            NS = temp.getTargetNamespace();
            name = temp.getName();
        }

        if (facets.hasFacet()) {
            list = ontology.createList();
        }

        if (facets.maxInclusive != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "maxInclusive"), facets.maxInclusive, XSDUtil.getXSDDatatype(name))
            );

        }
        if (facets.minInclusive != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "minInclusive"), facets.minInclusive, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.maxExclusive != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "maxExclusive"), facets.maxExclusive, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.minExclusive != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "minExclusive"), facets.minExclusive, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.length != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "length"), facets.length, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.maxLength != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "maxLength"), facets.maxLength, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.minLength != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "minLength"), facets.minLength, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.pattern != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "pattern"), facets.pattern, XSDDatatype.XSDstring)
            );
        }
        if (facets.totalDigits != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "totalDigits"), facets.totalDigits, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.fractionDigits != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "fractionDigits"), facets.fractionDigits, XSDUtil.getXSDDatatype(name))
            );
        }
        if (facets.whiteSpace != null) {
            list = list.cons(
                    ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "whiteSpace"), facets.whiteSpace, XSDDatatype.XSDstring)
            );
        }

        return list;
    }

    /**
     * 
     * @param parentURI - parentURI
     * @param currentObject
     * @return
     */
    private String getURI(String mainURI, String currentObject) {

        // mainURI contains at most one '#'
        assert(mainURI.split("#").length <3);
//        if (mainURI.contains("#")) {
//            mainURI = mainURI.substring(0, mainURI.indexOf('#'));
//        }
        mainURI = mainURI.replaceFirst("#", "/");
        return mainURI + "#" + currentObject;
    }
    
    /**
     * 
     * @param parentURI - 
     * @param decl
     * @return
     */
    private String getURI(String mainURI, XSDeclaration decl) {
        if (decl.getTargetNamespace().equals(XSDDatatype.XSD)) {
            return XSD.getURI() + decl.getName();
        }
        // mainURI contains at most one '#'
        assert(mainURI.split("#").length <3);
        mainURI = mainURI.replaceFirst("#", "/");
        return mainURI + "#" + decl.getName();
    }

    /**
     * @return - returns a list of abstract classes in the ontology. These
     * values cannot be represented in ontology file.
     */
    public ArrayList<OntClass> getAbstractClasses() {
        return abstractClasses;
    }

    /**
     * @return - returns a list of classes having mixed attribute in the
     * ontology. These values cannot be represented in ontology file.
     */
    public ArrayList<OntClass> getMixedClasses() {
        return mixedClasses;
    }

    /**
     * @return - Prefix for object properties. Default value is "has".
     */
    public String getObjectPropPrefix() {
        return this.opprefix;
    }

    /**
     * @return prefix for data type properties. Default value is empty string.
     */
    public String getDataTypePropPrefix() {
        return this.dtpprefix;
    }

    /**
     * @param opprefix - Prefix for object properties. Default value is "has".
     */
    public void setObjectPropPrefix(String opprefix) {
        this.opprefix = opprefix;
    }

    /**
     * @param dtpprefix - Prefix for data type properties. Default value is
     * empty string.
     */
    public void setDataTypePropPrefix(String dtpprefix) {
        this.dtpprefix = dtpprefix;
    }
}
