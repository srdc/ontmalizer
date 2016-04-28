/**
 *
 */
package tr.com.srdc.ontmalizer.helper;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;

/**
 * @author Mustafa
 *
 */
public class XSDUtil {

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

}
