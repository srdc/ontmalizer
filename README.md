<!--
Copyright (C) 2013 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. Ltd. Sti.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

Ontmalizer [![License Info](http://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/srdc/ontmalizer/blob/master/LICENSE.txt)
===

Ontmalizer performs comprehensive transformations of XML Schemas (XSD) and XML data to RDF/OWL automatically. Through this tool, it is possible to create RDF/OWL representation of XML Schemas, and XML instances that comply with such XML Schemas.

The state of the art open source and/or free tools for RDFizing XSD and XML are not able to handle complex schemas and XML instances such as HL7 Clinical Document Architecture (CDA) R2. Only a few commercial tools such as TopBraid Composer are successfully able to do so. However, we do not want to use commercial tools in our SALUS Project: http://www.srdc.com.tr/projects/salus/. As a result, we implemented our own solution. We make use of Sun's XSOM library for processing XML Schemas, Apache Xerces for processing XML data and Apache Jena for managing RDF data.

Further information and technical details can be found in our blog post accessible at http://www.srdc.com.tr/projects/salus/blog/?p=189.

## Installation

Apache Maven is required to build the Ontmalizer. Please visit http://maven.apache.org/ in order to install Maven on your system.

Under the root directory of the Ontmalizer project run the following:

	$ ontmalizer> mvn install

In order to make a clean install run the following:

	$ ontmalizer> mvn clean install

These will build the Ontmalizer and also run a number of test cases, which will transform some XML Schemas (e.g. HL7 CDA R2, SALUS Common Information Model) and corresponding XML instances to RDF/OWL. 

## Transforming XSD to RDF/OWL

XSD2OWLMapper is the main class to transform XML Schemas to RDF/OWL. The constructor of this class gets the root XSD file to be transformed. Configuration of the transformation operation is quite simple: the caller can set the prefixes for the object property and datatype property names to be created. Then, the call to the convertXSD2OWL() method performs the transformation. 

XSD2OWLMapper is able to print the output ontology in one of these formats: RDF/XML, RDF/XML-ABBREV, N-TRIPLE and N3. An example transformation routine is provided below for the HL7 CDA R2 XML Schema:

```java
    // This part converts XML schema to OWL ontology.
    XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/CDA/CDA.xsd"));
    mapping.setObjectPropPrefix("");
    mapping.setDataTypePropPrefix("");
    mapping.convertXSD2OWL();

    // This part prints the ontology to the specified file.
    FileOutputStream ont;
    try {
        File f = new File("src/test/resources/output/cda-ontology.n3");
        f.getParentFile().mkdirs();
        ont = new FileOutputStream(f);
        mapping.writeOntology(ont, "N3");
        ont.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
```

## Transforming XML to RDF/OWL

XML2OWLMapper is the main class to transform XML data to RDF/OWL by creating instances of the necessary OWL classes, RDFS datatypes, OWL datatype and object properties. The constructor of this class gets the XML file to be transformed together with an instance of XSD2OWLMapper that is already initialized with the corresponding XML Schema of the XML data. No other configuration is necessary for the transformation operation; the prefixes for the object property and datatype property names to be created are gathered from the XSD2OWLMapper configuration. Then, the call to the convertXML2OWL() method performs the transformation.

Similar to XSD2OWLMapper, XML2OWLMapper is able to print the output ontology instance in one of these formats: RDF/XML, RDF/XML-ABBREV, N-TRIPLE and N3. An example transformation routine is provided below for a complete HL7 CDA R2 instance, which is compliant with the HL7/ASTM Continuity of Care Document (CCD) and IHE Patient Care Coordination (PCC) templates:

```java
    // This part converts XML schema to OWL ontology.
    XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/CDA/CDA.xsd"));
    mapping.setObjectPropPrefix("");
    mapping.setDataTypePropPrefix("");
    mapping.convertXSD2OWL();

    // This part converts XML instance to RDF data model.
    XML2OWLMapper generator = new XML2OWLMapper(
        new File("src/test/resources/CDA/SALUS-sample-full-CDA-instance.xml"), mapping);
    generator.convertXML2OWL();
    
    // This part prints the RDF data model to the specified file.
    try{
        File f = new File("src/test/resources/output/salus-cda-instance.n3");
        f.getParentFile().mkdirs();
        FileOutputStream fout = new FileOutputStream(f);
        generator.writeModel(fout, "N3");
        fout.close();

    } catch (Exception e){
        e.printStackTrace();
    }
```

Please refer to our blog post (http://www.srdc.com.tr/projects/salus/blog/?p=189) for further details.
