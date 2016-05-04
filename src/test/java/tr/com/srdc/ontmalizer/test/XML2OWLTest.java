/**
 *
 */
package tr.com.srdc.ontmalizer.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Writer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.ontmalizer.XML2OWLMapper;
import tr.com.srdc.ontmalizer.XSD2OWLMapper;

/**
 * @author Mustafa
 *
 */
public class XML2OWLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(XML2OWLTest.class);

    @Test
    public void createCDAOntologyInstance() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/CDA/CDA.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part converts XML instance to RDF data model.
        XML2OWLMapper generator = new XML2OWLMapper(new File("src/test/resources/CDA/SALUS-sample-full-CDA-instance.xml"), mapping);
        generator.convertXML2OWL();

        // This part prints the RDF data model to the specified file.
        try {
            File f = new File("src/test/resources/output/salus-cda-instance.n3");
            f.getParentFile().mkdirs();
            FileOutputStream fout = new FileOutputStream(f);
            generator.writeModel(fout, "N3");
            fout.close();

        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    @Test
    public void createFirstPrototypeCDAInstances() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/CDA/CDA.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        File folder = new File("src/test/resources/CDA/first-prot");
        for (File child : folder.listFiles()) {
            String inName = child.getName();
            String outName = inName.substring(0, inName.lastIndexOf(".")) + "-cda.n3";

            // This part converts XML instance to RDF data model.
            XML2OWLMapper generator = new XML2OWLMapper(child, mapping);
            generator.convertXML2OWL();

            // This part prints the RDF data model to the specified file.
            try {
                File f = new File("src/test/resources/output/first-prot/" + outName);
                f.getParentFile().mkdirs();
                FileOutputStream fout = new FileOutputStream(f);
                generator.writeModel(fout, "N3");
                fout.close();

            } catch (Exception e) {
                LOGGER.error("{}", e.getMessage());
            }
        }

    }

    @Test
    public void createSALUSCommonOntologyInstance() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/salus-common-xsd/salus-cim.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part converts XML instance to RDF data model.
        XML2OWLMapper generator = new XML2OWLMapper(new File("src/test/resources/salus-common-xsd/salus-cim-instance.xml"), mapping);
        generator.convertXML2OWL();

        // This part prints the RDF data model to the specified file.
        try {
            File f = new File("src/test/resources/output/salus-cim-instance.n3");
            f.getParentFile().mkdirs();
            FileOutputStream fout = new FileOutputStream(f);
            generator.writeModel(fout, "N3");
            fout.close();

        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    @Test
    public void createSALUSEligibilityInstance() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/salus-common-xsd/salus-cim.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part converts XML instance to RDF data model.
        XML2OWLMapper generator = new XML2OWLMapper(new File("src/test/resources/salus-common-xsd/salus-eligibility-instance.xml"), mapping);
        generator.convertXML2OWL();

        // This part prints the RDF data model to the specified file.
        try {
            File f = new File("src/test/resources/output/salus-eligibility-instance.n3");
            f.getParentFile().mkdirs();
            FileOutputStream fout = new FileOutputStream(f);
            generator.writeModel(fout, "N3");
            fout.close();

        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    @Test
    public void createTestOntologyInstance() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/test/test.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part converts XML instance to RDF data model.
        XML2OWLMapper generator = new XML2OWLMapper(new File("src/test/resources/test/test.xml"), mapping);
        generator.convertXML2OWL();

        // This part prints the RDF data model to the specified file.
        try {
            File f = new File("src/test/resources/output/test-instance.rdf");
            f.getParentFile().mkdirs();
            FileOutputStream fout = new FileOutputStream(f);
            generator.writeModel(fout, "RDF/XML-ABBREV");
            fout.close();

        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    @Test
    public void testWriter() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/test/test.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part converts XML instance to RDF data model.
        XML2OWLMapper generator = new XML2OWLMapper(new File("src/test/resources/test/test.xml"), mapping);
        generator.convertXML2OWL();

        // This part prints the RDF data model to the specified file.
        try {
            File f = new File("src/test/resources/output/test-instance.rdf");
            f.getParentFile().mkdirs();
            Writer writer = new FileWriter(f);
            generator.writeModel(writer, "RDF/XML-ABBREV");
            writer.close();

        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }
}
