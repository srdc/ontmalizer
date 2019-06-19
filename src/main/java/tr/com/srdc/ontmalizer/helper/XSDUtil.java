/**
 *
 */
package tr.com.srdc.ontmalizer.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mustafa
 *
 */
public class XSDUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(XSDUtil.class);
    
    
    public static final String EOL_UNIX = "\n"; 
    
    public static Resource getXSDResource(String type) {
        switch (type) {
            case "anyURI":
                return XSD.anyURI;
            case "base64Binary":
                return XSD.base64Binary;
            case "date":
                return XSD.date;
            case "dateTime":
                return XSD.dateTime;
            case "decimal":
                return XSD.decimal;
            case "duration":
                return XSD.duration;
            case "ENTITIES":
                return XSD.ENTITIES;
            case "ENTITY":
                return XSD.ENTITY;
            case "gDay":
                return XSD.gDay;
            case "gMonth":
                return XSD.gMonth;
            case "gMonthDay":
                return XSD.gMonthDay;
            case "gYear":
                return XSD.gYear;
            case "gYearMonth":
                return XSD.gYearMonth;
            case "hexBinary":
                return XSD.hexBinary;
            case "ID":
                return XSD.ID;
            //		else if (type.equals("IDREFS"))
            case "IDREF":
                return XSD.IDREF;
            case "integer":
                return XSD.integer;
            case "language":
                return XSD.language;
            case "Name":
                return XSD.Name;
            case "NCName":
                return XSD.NCName;
            case "negativeInteger":
                return XSD.negativeInteger;
            //		else if (type.equals("NMTOKENS"))
            case "NMTOKEN":
                return XSD.NMTOKEN;
            case "nonNegativeInteger":
                return XSD.nonNegativeInteger;
            case "nonPositiveInteger":
                return XSD.nonPositiveInteger;
            case "normalizedString":
                return XSD.normalizedString;
            case "NOTATION":
                return XSD.NOTATION;
            case "positiveInteger":
                return XSD.positiveInteger;
            case "QName":
                return XSD.QName;
            case "time":
                return XSD.time;
            case "token":
                return XSD.token;
            case "unsignedByte":
                return XSD.unsignedByte;
            case "unsignedInt":
                return XSD.unsignedInt;
            case "unsignedLong":
                return XSD.unsignedLong;
            case "unsignedShort":
                return XSD.unsignedShort;
            case "boolean":
                return XSD.xboolean;
            case "byte":
                return XSD.xbyte;
            case "double":
                return XSD.xdouble;
            case "float":
                return XSD.xfloat;
            case "int":
                return XSD.xint;
            case "long":
                return XSD.xlong;
            case "short":
                return XSD.xshort;
            case "string":
                return XSD.xstring;
            case "anyType":
            case "anySimpleType":
                return OWL.Thing;
            default:
                break;
        }

        return null;
    }

    public static XSDDatatype getXSDDatatype(String name) {
        switch (name) {
            case "anyURI":
                return XSDDatatype.XSDanyURI;
            case "base64Binary":
                return XSDDatatype.XSDbase64Binary;
            case "boolean":
                return XSDDatatype.XSDboolean;
            case "byte":
                return XSDDatatype.XSDbyte;
            case "date":
                return XSDDatatype.XSDdate;
            case "dateTime":
                return XSDDatatype.XSDdateTime;
            case "decimal":
                return XSDDatatype.XSDdecimal;
            case "double":
                return XSDDatatype.XSDdouble;
            case "duration":
                return XSDDatatype.XSDduration;
            case "ENTITY":
                return XSDDatatype.XSDENTITY;
            case "float":
                return XSDDatatype.XSDfloat;
            case "gDay":
                return XSDDatatype.XSDgDay;
            case "gMonth":
                return XSDDatatype.XSDgMonth;
            case "gMonthDay":
                return XSDDatatype.XSDgMonthDay;
            case "gYear":
                return XSDDatatype.XSDgYear;
            case "gYearMonth":
                return XSDDatatype.XSDgYearMonth;
            case "hexBinary":
                return XSDDatatype.XSDhexBinary;
            case "ID":
                return XSDDatatype.XSDID;
            case "IDREF":
                return XSDDatatype.XSDIDREF;
            case "int":
                return XSDDatatype.XSDint;
            case "integer":
                return XSDDatatype.XSDinteger;
            case "language":
                return XSDDatatype.XSDlanguage;
            case "long":
                return XSDDatatype.XSDlong;
            case "Name":
                return XSDDatatype.XSDName;
            case "NCName":
                return XSDDatatype.XSDNCName;
            case "negativeInteger":
                return XSDDatatype.XSDnegativeInteger;
            case "NMTOKEN":
                return XSDDatatype.XSDNMTOKEN;
            case "nonNegativeInteger":
                return XSDDatatype.XSDnonNegativeInteger;
            case "nonPositiveInteger":
                return XSDDatatype.XSDnonPositiveInteger;
            case "normalizedString":
                return XSDDatatype.XSDnormalizedString;
            case "NOTATION":
                return XSDDatatype.XSDNOTATION;
            case "positiveInteger":
                return XSDDatatype.XSDpositiveInteger;
            case "QName":
                return XSDDatatype.XSDQName;
            case "short":
                return XSDDatatype.XSDshort;
            case "string":
                return XSDDatatype.XSDstring;
            case "time":
                return XSDDatatype.XSDtime;
            case "token":
                return XSDDatatype.XSDtoken;
            case "unsignedByte":
                return XSDDatatype.XSDunsignedByte;
            case "unsignedInt":
                return XSDDatatype.XSDunsignedInt;
            case "unsignedLong":
                return XSDDatatype.XSDunsignedLong;
            case "unsignedShort":
                return XSDDatatype.XSDunsignedShort;
            default:
                break;
        }

        return null;
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
            fis =  XSDUtil.class.getResourceAsStream(filename);
        }
        
        File temp = null;
        if (fis != null && !EOL_UNIX.equals(System.getProperty( "line.separator" ))){
        	temp = new File( file.getAbsolutePath() + ".unixEndings" );
            temp.createNewFile();
        	BufferedReader bufferIn = new BufferedReader( new InputStreamReader( fis ) );

			FileOutputStream fileOut = new FileOutputStream( temp );
			BufferedWriter bufferOut = new BufferedWriter( new OutputStreamWriter( fileOut ) );

			String line;
			while ( ( line = bufferIn.readLine() ) != null )
			{
			    bufferOut.write( line );
			    bufferOut.write( EOL_UNIX ); // write EOL marker
			}
			bufferIn.close();
			bufferOut.close();
			fis.close();
			fis = new FileInputStream(temp);
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
            if (temp!=null) {
            	temp.delete();
            }
            return complete.digest();
        }
    }
    
    
    public static String getMD5Checksum(String filename) throws NoSuchAlgorithmException, IOException {
        byte[] b = createChecksum(filename);
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    
    }

    public static boolean isSchemaQualified(String filename) {
        //actually lets us get the file (if it isn't in a jar)
        if (filename.startsWith("file:")) {
            filename = filename.replaceFirst("file:", "");
        }
        final String attrString = "elementFormDefault=";
        try {
            List<String> lines = Files.readLines(new File(filename), Charset.defaultCharset());
            for (String line : lines) {
                int index = line.indexOf(attrString);
                if (index == -1) {
                    // default is unqualified
                    continue;
                }
                // Should be either q for qualified or u for unqualified
                // add one for opening "
                char uOrQ = line.charAt(index + attrString.length() + 1);
                if (uOrQ == 'q' || uOrQ == 'Q') {
                    return true;
                } else if (uOrQ == 'u' || uOrQ == 'U') {
                    return false;
                }else {
                    LOGGER.warn("Unrecognized {} in line {}. Char={}", attrString, line, uOrQ);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error reading file {}", filename, e);
        }
        // default is unqualified
        return false;
    }
    
    
    

}
