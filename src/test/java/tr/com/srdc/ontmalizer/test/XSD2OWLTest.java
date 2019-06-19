/**
 *
 */
package tr.com.srdc.ontmalizer.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Writer;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.ontmalizer.XSD2OWLMapper;

/**
 * @author Mustafa
 *
 */
public class XSD2OWLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(XSD2OWLTest.class);

    @Test
    public void createCDAOntology() {

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
            LOGGER.error("{}", e.getMessage());
        }
    }

    @Test
    public void createSALUSCommonOntology() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/salus-common-xsd/salus-cim.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part prints the ontology to the specified file.
        FileOutputStream ont;
        try {
            File f = new File("src/test/resources/output/salus-cim-ontology.n3");
            f.getParentFile().mkdirs();
            ont = new FileOutputStream(f);
            mapping.writeOntology(ont, "N3");
            ont.close();
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    @Test
    public void createTestOntology() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/test/test.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part prints the ontology to the specified file.
        FileOutputStream ont;
        try {
            File f = new File("src/test/resources/output/test.n3");
            f.getParentFile().mkdirs();
            ont = new FileOutputStream(f);
            mapping.writeOntology(ont, "N3");
            ont.close();
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    @Test
    public void writerTest() {

        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("src/test/resources/test/test.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();

        // This part prints the ontology to the specified file.
        try {
            File f = new File("src/test/resources/output/test.n3");
            f.getParentFile().mkdirs();
            Writer w = new FileWriter(f);
            mapping.writeOntology(w, "N3");
            w.close();
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

}
