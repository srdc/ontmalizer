package tr.com.srdc.ontmalizer;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.InputSource;

import tr.com.srdc.ontmalizer.helper.AnnotationFactory;
import tr.com.srdc.ontmalizer.helper.Constants;
import tr.com.srdc.ontmalizer.helper.NamingUtil;
import tr.com.srdc.ontmalizer.helper.SimpleTypeRestriction;
import tr.com.srdc.ontmalizer.helper.URLResolver;
import tr.com.srdc.ontmalizer.helper.XSDUtil;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.EnumeratedClass;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;


/**
 * @author Atakan Kaya, Mustafa Yuksel
 * 
 * XSD2OWLMapper Class converts XML schemas to ontologies.
 *
 */
public class XSD2OWLMapper {
	
	// Variables to parse XSD schema
	private XSSchemaSet schemaSet 	= null;
	private XSSchema schema 		= null;
	
	// Variables to create ontology
	private OntModel ontology 		= null;
	
	// To number classes named Anon_# and Class_#
	private int anonCount 			= 1;
	private int attrLocalSimpleTypeCount = 1;

	// To handle nodes with text content
	private Property hasValue 		= null;
	
	private String opprefix 		= Constants.DEFAULT_OBP_PREFIX;
	private String dtpprefix 		= Constants.DEFAULT_DTP_PREFIX;
	
	private ArrayList<OntClass> abstractClasses 	= null;
	private ArrayList<OntClass> mixedClasses 		= null;

	private String mainURI 			= null;
	
	/**
	 * Creates a new XSD2OWLMapper instance. 
	 * @param xsdFile
	 * - An XML Schema File to be converted
	 */
	public XSD2OWLMapper(File xsdFile) {
		parseXSD(xsdFile);
		initOntology();
	}
	
	/**
	 * Creates a new XSD2OWLMapper instance. 
	 * @param xsdInputStream
	 * - An XML Schema InputStream to be converted
	 */
	public XSD2OWLMapper(InputStream xsdInputStream) {
		parseXSD(xsdInputStream);
		initOntology();
	}
	
	/**
	 * Creates a new XSD2OWLMapper instance. 
	 * @param xsdInputStream
	 * - An XML Schema URL to be converted
	 */
	public XSD2OWLMapper(URL xsdURL) {
		parseXSD(xsdURL);
		initOntology();
	}
	
	private void parseXSD(File file) {
		try {
			XSOMParser parser = new XSOMParser();
			parser.setAnnotationParser(new AnnotationFactory());
			parser.parse(file);
			schemaSet = parser.getResult();
			schema = schemaSet.getSchema(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseXSD(InputStream is) {
		try {
			XSOMParser parser = new XSOMParser();
			parser.setAnnotationParser(new AnnotationFactory());
			parser.parse(is);
			schemaSet = parser.getResult();
			schema = schemaSet.getSchema(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseXSD(URL url) {
		try {
			XSOMParser parser = new XSOMParser();
			parser.setAnnotationParser(new AnnotationFactory());
			parser.setEntityResolver(new URLResolver());
			InputSource inputSource = new InputSource(url.openStream());
			inputSource.setSystemId(url.toExternalForm());
			parser.parse(inputSource);
			schemaSet = parser.getResult();
			schema = schemaSet.getSchema(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initOntology() {
		ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		ontology.setNsPrefix(Constants.ONTMALIZER_BASE_URI_PREFIX, Constants.ONTMALIZER_BASE_URI);
		try {
			URI uri = new URI(schema.getTargetNamespace());
			if (uri.isAbsolute())
				mainURI = schema.getTargetNamespace();
			else 
				mainURI = "http://uri-not-absolute.com";
		} catch (URISyntaxException e) {
			mainURI = "http://uri-not-valid.com";
		}

		ontology.setNsPrefix("", mainURI + "#");
		
		hasValue = ontology.createProperty(Constants.ONTMALIZER_VALUE_PROP_NAME);
		
		abstractClasses = new ArrayList<OntClass>();
		mixedClasses = new ArrayList<OntClass>();
	}
	
	/**
	 * Converts the XML schema file to an ontology. 
	 */
	public void convertXSD2OWL() {
		Iterator<XSSimpleType> simpleTypes = schema.iterateSimpleTypes();
		while (simpleTypes.hasNext())
			convertSimpleType((XSSimpleType) simpleTypes.next(), null);
	
		Iterator<XSComplexType> complexTypes = schema.iterateComplexTypes();
		while (complexTypes.hasNext())
			convertComplexType((XSComplexType) complexTypes.next(), null);
	
		Iterator<XSElementDecl> elements = schema.iterateElementDecls();
		while (elements.hasNext())
			convertElement((XSElementDecl) elements.next(), null);
		
		Iterator<XSModelGroupDecl> groups = schema.iterateModelGroupDecls();
		while (groups.hasNext())
			convertModelGroupDecl(groups.next());
		
		Iterator<XSAttGroupDecl> attGroups = schema.iterateAttGroupDecls();
		while (attGroups.hasNext())
			convertAttributeGroup((XSAttGroupDecl) attGroups.next());
		
		createDefaultTextPropertyForMixedClasses();
	}
		
	/* This function creates RDFS Datatypes except for Enumerated Simple Types.
	 * If there are both enumeration facets and other facets, then other facets will be ignored.
	 */
	private OntClass convertSimpleType(XSSimpleType simple, String parentURI) {
		String NS 	= simple.getTargetNamespace() + "#";
		String URI 	= getURI(simple);
		
		if (simple.isGlobal()) {
			// TODO: Mustafa: Why would we define new simple types in the XSD namespace?
			// The following if should not evaluate to true...
			if ( NS.equals(XSD.getURI()) ) {
				// If element type is an XSD datatype
				// An example case:
				// <xs:element name="test" type="xs:string" />
				
				OntClass dataType = ontology.createOntResource( OntClass.class, 
																RDFS.Datatype, 
																parentURI + Constants.DATATYPE_SUFFIX );
				
				// Set super class to the element type
				Resource xsdResource = XSDUtil.getXSDResource(simple.getName());
				dataType.addSuperClass(xsdResource);
		
				// Set Equivalent Datatype
				OntClass eqDataType = ontology.createOntResource( OntClass.class, 
																  RDFS.Datatype, 
																  null );
				eqDataType.addProperty(OWL2.onDatatype, xsdResource);
				dataType.addEquivalentClass(eqDataType);
				
				addTextAnnotation(simple, dataType);
				return dataType;
			}
			else if (parentURI!=null) {
				// An example case:
				// <xs:element name="test" type="SimpleGlobalType1" />
				
				OntClass datatype = ontology.createOntResource( OntClass.class, 
																RDFS.Datatype, 
																parentURI + Constants.DATATYPE_SUFFIX );
				
				/**
				 * The following part adds equivalentClass to an enumarated class, if it is not added already. Like this:
				 * 
				 *  :simpletype1Datatype
				 *     a       rdfs:Datatype ;
				 *     rdfs:subClassOf :simpletype1;
				 *     owl:equivalentClass
				 *             [ a       rdfs:Datatype ;
				 *               owl:onDatatype :simpletype1
				 *             ] .
				 */
				Resource onDatatype = ontology.getEnumeratedClass(URI); // If Enumerated Class
				if(onDatatype != null) {
					Iterator<OntClass> prevEqClasses = datatype.listEquivalentClasses();
					boolean insertedBefore = false;
					while(prevEqClasses.hasNext()) {
						Resource prevOnDatatype = prevEqClasses.next().getProperty(OWL2.onDatatype).getResource();
						if(prevOnDatatype.getURI().equals(onDatatype.getURI()))
							insertedBefore = true;
					}
					
					if(!insertedBefore) {
						datatype.addSuperClass(onDatatype);
						OntClass equivClass = ontology.createOntResource( OntClass.class, 
																		  RDFS.Datatype, 
																		  null );
						equivClass.addProperty(OWL2.onDatatype, onDatatype);
						datatype.addEquivalentClass(equivClass);
					}
				}
				
				addTextAnnotation(simple, datatype);
				return datatype;
			}
			else if (simple.isList() || simple.isUnion()) {
				// To convert global union or global list simple type definitions.
				// It will create an rdfs:Datatype which is a subclass of xs:anySimpleType.			
				return convertListOrUnion(URI);
			}
			else if (simple.isRestriction()) {
				return convertRestriction(simple, URI);
			}
		}
		else if (simple.isLocal()) {
			if (simple.isList() || simple.isUnion()) {
				// It will create an rdfs:Datatype which is a subclass of xs:anySimpleType		
				return convertListOrUnion(parentURI);
			}
			else if (simple.isRestriction()) {
				return convertRestriction(simple, parentURI);
			}
		}
		
		return null;
	}
	
	private OntClass convertListOrUnion(String URI) {
		OntClass dataType = ontology.createOntResource( OntClass.class, 
														RDFS.Datatype, 
														URI + Constants.DATATYPE_SUFFIX);

		Resource anySimpleType = ontology.getResource(XSD.getURI() + "anySimpleType");
		dataType.addSuperClass(anySimpleType);
		
		OntClass eqDataType = ontology.createOntResource( OntClass.class, 
						  								  RDFS.Datatype, 
						  								  null);
		eqDataType.addProperty(OWL2.onDatatype, anySimpleType);
		dataType.addEquivalentClass(eqDataType);
		
		return dataType;
	}

	private OntClass convertEnumeration(EnumeratedClass enumClass, XSSimpleType simple, SimpleTypeRestriction facets) {		
		XSRestrictionSimpleType restriction = simple.asRestriction();
			
		XSType base 	= restriction.getBaseType();
		String baseNS	= base.getTargetNamespace() + "#";
		String baseURI 	= getURI(base);
		
		enumClass.addSuperClass(ontology.createClass(Constants.ONTMALIZER_ENUMERATEDVALUE_CLASS_NAME));

		if (baseNS.equals(XSD.getURI())) {
			enumClass.addSuperClass( ontology.createAllValuesFromRestriction( null, 
																			  hasValue, 
																			  XSDUtil.getXSDResource(base.getName()) ) );
			enumClass.addSuperClass( ontology.createMaxCardinalityRestriction( null, 
																			   hasValue, 
																			   1 ) );
		}
		else 
			enumClass.addSuperClass( ontology.createClass(baseURI) );
		
		OntClass enumSuperClass = ontology.createClass(Constants.ONTMALIZER_ENUMERATION_CLASS_NAME);
		//This statement should be added, but there is no Enumeration resource in any vocabulary. Only in LinkedModel
		//enumSuperClass.addSuperClass(DTYPE.Enumeration);
		//Individual enumResource = ontology.createIndividual(URI + "_Enumeration", enumSuperClass);
		Individual enumResource = ontology.createIndividual(enumClass.getURI() + "_Enumeration", enumSuperClass);
		
		for ( int i=0, length=facets.enumeration.length ; i<length ; i++ ) {
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
	
	private OntClass convertRestriction(XSSimpleType simple, String URI) {
		XSRestrictionSimpleType restriction = simple.asRestriction();
		
		SimpleTypeRestriction facets = new SimpleTypeRestriction();
		facets.initFacets(restriction);
		

			XSSimpleType baseType = restriction.getBaseType().asSimpleType();
			String baseURI = getURI(baseType);
			
			SimpleTypeRestriction baseFacets = new SimpleTypeRestriction();
			if (baseType.isRestriction())
				baseFacets.initFacets(baseType.asRestriction());
			
			OntClass datatype = ontology.createOntResource( OntClass.class, 
															RDFS.Datatype, 
															URI + Constants.DATATYPE_SUFFIX );
			Resource onDatatype = null;
			
			if (baseType.getTargetNamespace().equals(XSDDatatype.XSD))
				onDatatype = XSDUtil.getXSDResource(baseType.getName());
			else if (baseFacets.enumeration!=null)
				onDatatype = ontology.createResource(baseURI);
			else
				onDatatype = ontology.createOntResource( OntClass.class, 
														 RDFS.Datatype, 
														 baseURI + Constants.DATATYPE_SUFFIX );
			// I did not use getResource methods because the class I am looking for may not be created yet. 
			
			datatype.addSuperClass(onDatatype);

			OntClass equivClass = ontology.createOntResource( OntClass.class, 
															  RDFS.Datatype, 
															  null );
			equivClass.addProperty(OWL2.onDatatype, onDatatype);
			
			RDFList list = getFacetList(facets, restriction);
			if (list!=null)
				equivClass.addProperty(OWL2.withRestrictions, list);
			datatype.addEquivalentClass(equivClass);
			
			// If an enumeration facet is available, then we also generate an OWL:Class containing the EnumeratedValues.
			if (facets.enumeration!=null) {
				return convertEnumeration( ontology.createEnumeratedClass(URI, null), 
										   simple, 
									       facets );
			}
			else {
				addTextAnnotation(simple, datatype);
				return datatype;
			}

	}
	
	private OntClass convertComplexType(XSComplexType complex, String parentURI) {
		OntClass complexClass = null;
		
		if (complex.isGlobal()) { 
			complexClass = ontology.createClass( getURI(complex) );
			if (parentURI!=null) {
				OntClass element = ontology.createClass(parentURI);
				element.addSuperClass(complexClass);
				
				addTextAnnotation(complex, complexClass);
				return complexClass;
			}
		}
		else if (complex.isLocal())
			complexClass = ontology.createClass(parentURI);

		XSType baseType = complex.getBaseType();
		String baseURI = getURI(baseType);
		
		if (baseType.isSimpleType()) {		
			if (ontology.getOntResource(baseURI) != null && 
					!baseType.getTargetNamespace().equals(XSDDatatype.XSD) )
				// If base type is an enumeration simple type
				complexClass.addSuperClass( ontology.getOntResource(baseURI) );
			else {
				if ( baseType.getTargetNamespace().equals(XSDDatatype.XSD) )
					complexClass.addSuperClass( ontology.createAllValuesFromRestriction( null, 
																						 hasValue, 
																						 XSDUtil.getXSDResource(baseType.getName()) ) );
				
				else if (ontology.getOntResource(baseURI + Constants.DATATYPE_SUFFIX) != null)
					complexClass.addSuperClass( ontology.createAllValuesFromRestriction( null, 
																						 hasValue, 
																						 ontology.getOntResource(baseURI + Constants.DATATYPE_SUFFIX) ) );
				complexClass.addSuperClass( ontology.createMaxCardinalityRestriction( null, 
																					  hasValue, 
																					  1 ) );
			}
			
			Iterator<? extends XSAttributeUse> attributeUses = complex.getAttributeUses().iterator();
			while (attributeUses.hasNext()) {
				XSAttributeUse attributeUse = (XSAttributeUse) attributeUses.next();
				convertAttribute(attributeUse, complexClass);
			}
		}	
		else if (baseType.isComplexType()) {
			if (complex.getDerivationMethod()==XSType.EXTENSION) {
				complexClass.addSuperClass(ontology.createClass(baseURI));
				
				XSContentType expl = complex.getExplicitContent();
				if (expl!=null) {
					XSParticle particle = expl.asParticle();
					if (particle!=null) {
						XSTerm term = particle.getTerm();
						if (term.isModelGroup()) 
							convertGroup(term.asModelGroup(), complexClass);
						else if (term.isModelGroupDecl()) {
							XSModelGroupDecl group = term.asModelGroupDecl();
							OntClass groupClass = ontology.createClass(getURI(group));
							
							groupClass.addSubClass(complexClass);
						}
					}				
				}
			} 
			else if (complex.getDerivationMethod()==XSType.RESTRICTION && 
					baseURI.equals("http://www.w3.org/2001/XMLSchema#anyType")) {
				/* For the case
				 * <xs:complexType name="complex16">
				 * 	<xs:sequence>
				 * 		<xs:element name="element16_3" type="xs:string" />
				 * 	</xs:sequence>
				 * </xs:complexType>
				 */
	
				XSParticle particle = complex.getContentType().asParticle();
				if (particle!=null) {
					XSTerm term = particle.getTerm();
					if (term.isModelGroup())
						convertGroup(term.asModelGroup(), complexClass);
					else if (term.isModelGroupDecl()) {
						XSModelGroupDecl group = term.asModelGroupDecl();
						OntClass groupClass = ontology.createClass(getURI(group));
			
						groupClass.addSubClass(complexClass);
					} 
				}
			}
			else
				/* If this complex type is a restriction to its base. 
				 * Note that this part is slightly different from TopBraid. 
				 * I added the allowed restrictions by getDeclaredAttributeUses below
				 */
				complexClass.addSuperClass(ontology.createClass(baseURI));
				
			Iterator<? extends XSAttributeUse> attributeUses = complex.getDeclaredAttributeUses().iterator();
			while (attributeUses.hasNext()) {
				XSAttributeUse attributeUse = (XSAttributeUse) attributeUses.next();
				convertAttribute(attributeUse, complexClass);
			}
		}
		
		Iterator<? extends XSAttGroupDecl> attGroups = complex.iterateAttGroups();
		while (attGroups.hasNext()) {
			XSAttGroupDecl attGroup = (XSAttGroupDecl) attGroups
					.next();
			OntClass attgClass = ontology.createClass(getURI(attGroup));
			attgClass.addSubClass(complexClass);
		}
		
		if (complex.isMixed())		
			mixedClasses.add(complexClass);
		if (complex.isAbstract())
			abstractClasses.add(complexClass);
		
		addTextAnnotation(complex, complexClass);
		return complexClass;
	}
	
	private void convertElement(XSElementDecl element, OntClass parent) {
		XSType elementType = element.getType();
		String URI = getURI(element);
		
		OntClass elementClass = null;
		if (element.isGlobal()) {			
			if (elementType.isSimpleType())
				elementClass = convertSimpleType(elementType.asSimpleType(), URI);
			else if (elementType.isComplexType())
				elementClass = convertComplexType(elementType.asComplexType(), URI);
		}

		XSElementDecl subs = element.getSubstAffiliation();
		if (subs!=null && elementClass!=null) {
			OntClass subsClass = ontology.createClass(getURI(subs));
			subsClass.addSubClass(elementClass);
		}
	}
	
	private void convertAttribute(XSAttributeUse attributeUse, OntClass complexClass){
		XSAttributeDecl attribute = attributeUse.getDecl();
		
		String NS = attribute.getType().getTargetNamespace();
		String URI = null;
		
		if (attribute.getType().isGlobal())
			URI = getURI(attribute.getType());
		else 
			URI = mainURI + "#Class_" + attrLocalSimpleTypeCount;
		
		/**
		 * xsd:IDREFS, xsd:ENTITIES and xsd:NMTOKENS are sequence-valued datatypes
		 * which do not fit the RDF datatype model.
		 * http://mail-archives.apache.org/mod_mbox/jena-users/201206.mbox/%3CCAFq2biyPYPKt0mnsnEajqwrQjOYH_geaFbVXvOuVeeDVYDgs2A@mail.gmail.com%3E
		 */
		if (NS.equals(XSDDatatype.XSD)) {
			if(attribute.getType().getName().equals("IDREFS") ||
				attribute.getType().getName().equals("ENTITIES") ||
				attribute.getType().getName().equals("NMTOKENS"))
				return;
		}
			
		// Mustafa: All simple types are datatype properties now!
		Property prop = ontology.createDatatypeProperty(mainURI + "#" + NamingUtil.createPropertyName(dtpprefix, attribute.getName()));
		
		if (!NS.equals(XSDDatatype.XSD)) {
			convertSimpleType(attribute.getType(), URI);
			attrLocalSimpleTypeCount++;
		}
			
		if (attributeUse.isRequired())
			ontology.createCardinalityRestriction(null, prop, 1).addSubClass(complexClass);
		else
			ontology.createMaxCardinalityRestriction(null, prop, 1).addSubClass(complexClass);

		if (NS.equals(XSDDatatype.XSD))
			ontology.createAllValuesFromRestriction( null, 
													 prop, 
													 XSDUtil.getXSDResource(attribute.getType().getName())).addSubClass(complexClass);
		else if (ontology.getOntResource(URI + Constants.DATATYPE_SUFFIX)!=null)
			ontology.createAllValuesFromRestriction( null, 
													 prop, 
													 ontology.getOntResource(URI + Constants.DATATYPE_SUFFIX )).addSubClass(complexClass);
		else if (ontology.getOntResource(URI)!=null) 
			ontology.createAllValuesFromRestriction( null, 
													 prop, 
													 ontology.getOntResource(URI)).addSubClass(complexClass);
	}
	
	private void convertGroup(XSModelGroup group, OntClass parent) {
		XSParticle[] particles = group.getChildren();
		for (XSParticle p : particles ) {
			XSTerm term = p.getTerm();
			if (term.isElementDecl()) {
				XSElementDecl element = term.asElementDecl();
				Property prop = null;
				if ( element.getType().isSimpleType() ) {
															
					prop = ontology.createDatatypeProperty(mainURI + "#" + NamingUtil.createPropertyName(dtpprefix, element.getName()));

					if (element.getType().getTargetNamespace().equals(XSDDatatype.XSD)) {
						if (element.getType().getName().equals("anyType"))
							parent.addSuperClass(ontology.createAllValuesFromRestriction( null, 
									  													  prop, 
									  													  OWL.Thing ) );
						else
							parent.addSuperClass(ontology.createAllValuesFromRestriction( null, 
																						  prop, 
																						  XSDUtil.getXSDResource(element.getType().getName())));
					
					}
					else {
						parent.addSuperClass(ontology.createAllValuesFromRestriction( 	null, 
																						prop, 
																						ontology.getResource(getURI(element.getType()) + Constants.DATATYPE_SUFFIX)));
						
						convertSimpleType(element.getType().asSimpleType(), getURI(element.getType()));
					}
					
				}
				else if (element.getType().isComplexType()) {
					prop = ontology.createObjectProperty( mainURI + "#" + NamingUtil.createPropertyName(opprefix, element.getName()));

					// TODO: Mustafa: How will this be possible?
					if (element.getType().getTargetNamespace().equals(XSDDatatype.XSD)) {
						if (element.getType().getName().equals("anyType"))
							parent.addSuperClass(ontology.createAllValuesFromRestriction( null, 
																						  prop, 
																						  OWL.Thing ) );
						else 
							parent.addSuperClass(ontology.createAllValuesFromRestriction( null, 
																						  prop, 
																						  XSDUtil.getXSDResource(element.getType().getName() ) ) );
					}
					else {
						if (element.getType().isGlobal())
							parent.addSuperClass(ontology.createAllValuesFromRestriction( null, 
																						  prop, 
																						  ontology.createResource( getURI(element.getType()) ) ) );
						else if (element.getType().isLocal()) {
							OntClass anonClass = ontology.createClass( mainURI
																	   + "#" 
																	   + "Anon_" 
																	   + anonCount++ );
							anonClass.addSuperClass( ontology.createClass( mainURI 
																		   + "#" 
																		   + "Anon" ) );
							parent.addSuperClass( ontology.createAllValuesFromRestriction( null, 
																						   prop, 
																						   anonClass ) );
							
							if (element.getType().isSimpleType())
								convertSimpleType(element.getType().asSimpleType(), anonClass.getURI());
							else if (element.getType().isComplexType())
								convertComplexType(element.getType().asComplexType(), anonClass.getURI());
						}
					}

				}
				
				// Cardinality constraints for the elements
				int minOccurs = p.getMinOccurs().intValue();
				int maxOccurs = p.getMaxOccurs().intValue();
				if(maxOccurs == 1) {
					if(minOccurs == 1)
						parent.addSuperClass(ontology.createCardinalityRestriction(null, prop, 1));
					else // minOccurs can be 0 in this case, logically
						parent.addSuperClass(ontology.createMaxCardinalityRestriction(null, prop, 1));
				}
				else {
					parent.addSuperClass(ontology.createMinCardinalityRestriction(null, prop, minOccurs));
					if(maxOccurs == -1)
						parent.addSuperClass(ontology.createMaxCardinalityRestriction(null, prop, Integer.MAX_VALUE));
					else
						parent.addSuperClass(ontology.createMaxCardinalityRestriction(null, prop, maxOccurs));
				}

			}
			else if (term.isModelGroup()) {
				convertGroup(term.asModelGroup(), parent);
			}
			else if (term.isModelGroupDecl()) {
				XSModelGroupDecl groupDecl = term.asModelGroupDecl();
				OntClass groupClass = ontology.createClass(getURI(groupDecl));
	
				groupClass.addSubClass(parent);
			}
		}	
	}
	
	private void convertModelGroupDecl(XSModelGroupDecl group) {
		OntClass groupClass = ontology.createClass(getURI(group));

		convertGroup(group.getModelGroup(), groupClass);
	}
	
	private void convertAttributeGroup(XSAttGroupDecl attGroup) {
		OntClass attgClass = ontology.createClass(getURI(attGroup));
		
		Iterator<? extends XSAttributeUse> attributes = attGroup.iterateAttributeUses();
		while (attributes.hasNext())
			convertAttribute((XSAttributeUse) attributes.next(), attgClass);
	}
	
	private void createDefaultTextPropertyForMixedClasses() {
		if(mixedClasses.size() == 0)
			return;
		
		Property prop = ontology.createDatatypeProperty(mainURI + "#" + NamingUtil.createPropertyName(dtpprefix, Constants.MIXED_CLASS_DEFAULT_PROP_NAME));
		
		for(OntClass mixedClass: mixedClasses) {
			ontology.createAllValuesFromRestriction(null, prop, XSD.xstring).addSubClass(mixedClass);
			ontology.createMaxCardinalityRestriction(null, prop, 1).addSubClass(mixedClass);
		}
	}
	
	private void addTextAnnotation(XSType xsType, OntClass ontClass) {
		if(xsType.getAnnotation() != null && xsType.getAnnotation().getAnnotation() != null) {
			String textAnnotation = xsType.getAnnotation().getAnnotation().toString();
			ontClass.addProperty(RDFS.comment, textAnnotation);
		}
	}
	
	/**
	 * @return
	 * returns the ontology. Abstract complex types and mixed attributes cannot be represented in this ontology.
	 */
	public OntModel getOntology() {
		return ontology;
	}
	
	/**
	 * @param out
	 * @param format
	 * - Output format may be one of these values; "RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3".
	 */
	public void writeOntology(OutputStream out, String format) {
		ontology.write(out, format, null);
	}
	
	private RDFList getFacetList(SimpleTypeRestriction facets, XSRestrictionSimpleType restriction) {
		RDFList list = null;
		
		XSType temp = restriction.getBaseType();
		String NS 	= temp.getTargetNamespace();
		String name = temp.getName();
		
		while (!NS.equals(XSDDatatype.XSD)) {
			temp = temp.getBaseType();
			NS = temp.getTargetNamespace();
			name = temp.getName();
		}
		
		if (facets.hasFacet())
			list = ontology.createList();								

		if (facets.maxInclusive!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "maxInclusive"), facets.maxInclusive, XSDUtil.getXSDDatatype(name))
					);
			
		}
		if (facets.minInclusive!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "minInclusive"), facets.minInclusive, XSDUtil.getXSDDatatype(name))
					);
		}
		if (facets.maxExclusive!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "maxExclusive"), facets.maxExclusive, XSDUtil.getXSDDatatype(name))
					);					
		}
		if (facets.minExclusive!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "minExclusive"), facets.minExclusive, XSDUtil.getXSDDatatype(name))
					);					
		}
		if (facets.length!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "length"), facets.length, XSDUtil.getXSDDatatype(name))
					);					
		}
		if (facets.maxLength!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "maxLength"), facets.maxLength, XSDUtil.getXSDDatatype(name))
					);					
		}
		if (facets.minLength!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "minLength"), facets.minLength, XSDUtil.getXSDDatatype(name))
					);					
		}
		if (facets.pattern!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "pattern"), facets.pattern, XSDDatatype.XSDstring)
					);					
		}
		if (facets.totalDigits!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "totalDigits"), facets.totalDigits, XSDUtil.getXSDDatatype(name))
					);					
		}
		if (facets.fractionDigits!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "fractionDigits"), facets.fractionDigits, XSDUtil.getXSDDatatype(name))
					);					
		}				
		if (facets.whiteSpace!=null) {
			list = list.cons(
					ontology.createResource().addProperty(ontology.createProperty(XSD.getURI() + "whiteSpace"), facets.whiteSpace, XSDDatatype.XSDstring)
					);					
		}
		
		return list;
	}

	private String getURI(XSDeclaration decl) {
		if (decl.getTargetNamespace().equals(XSDDatatype.XSD))
			return  XSD.getURI() + decl.getName();
		return mainURI + "#" + decl.getName();
	}
	
	/**
	 * @return
	 * - returns a list of abstract classes in the ontology. These values cannot be represented in ontology file.
	 */
	public ArrayList<OntClass> getAbstractClasses() {
		return abstractClasses;
	}
	
	/**
	 * @return
	 * - returns a list of classes having mixed attribute in the ontology. These values cannot be represented in ontology file.
	 */
	public ArrayList<OntClass> getMixedClasses() {
		return mixedClasses;
	}

	/**
	 * @return
	 * - Prefix for object properties. 
	 * Default value is "has".
	 */
	public String getObjectPropPrefix() {
		return this.opprefix;
	}
	
	/**
	 * @return prefix for data type properties. 
	 * Default value is empty string.
	 */
	public String getDataTypePropPrefix() {
		return this.dtpprefix;
	}
	
	/**
	 * @param opprefix
	 * - Prefix for object properties. 
	 * Default value is "has".
	 */
	public void setObjectPropPrefix(String opprefix) {
		this.opprefix = opprefix;
	}
	
	/**
	 * @param dtpprefix
	 * - Prefix for data type properties. 
	 * Default value is empty string.
	 */
	public void setDataTypePropPrefix(String dtpprefix) {
		this.dtpprefix = dtpprefix;
	}
}
