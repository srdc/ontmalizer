/**
 * 
 */
package tr.com.srdc.ontmalizer.helper;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Mustafa
 *
 */
public class XSDUtil {
	
	public static Resource getXSDResource(String type) {
		if (type.equals("anyURI"))
			return XSD.anyURI;
		else if (type.equals("base64Binary"))
			return XSD.base64Binary;
		else if (type.equals("date"))
			return XSD.date;
		else if (type.equals("dateTime"))
			return XSD.dateTime;
		else if (type.equals("decimal"))
			return XSD.decimal;
		else if (type.equals("duration"))
			return XSD.duration;
		else if (type.equals("ENTITIES"))
			return XSD.ENTITIES;
		else if (type.equals("ENTITY"))
			return XSD.ENTITY;
		else if (type.equals("gDay"))
			return XSD.gDay;
		else if (type.equals("gMonth"))
			return XSD.gMonth;
		else if (type.equals("gMonthDay"))
			return XSD.gMonthDay;
		else if (type.equals("gYear"))
			return XSD.gYear;
		else if (type.equals("gYearMonth"))
			return XSD.gYearMonth;
		else if (type.equals("hexBinary"))
			return XSD.hexBinary;
		else if (type.equals("ID"))
			return XSD.ID;
		else if (type.equals("IDREF"))
			return XSD.IDREF;
//		else if (type.equals("IDREFS"))
//			return XSD.IDREFS;
		else if (type.equals("integer"))
			return XSD.integer;
		else if (type.equals("language"))
			return XSD.language;
		else if (type.equals("Name"))
			return XSD.Name;		
		else if (type.equals("NCName"))
			return XSD.NCName;
		else if (type.equals("negativeInteger"))
			return XSD.negativeInteger;
		else if (type.equals("NMTOKEN"))
			return XSD.NMTOKEN;		
//		else if (type.equals("NMTOKENS"))
//			return XSD.NMTOKENS;
		else if (type.equals("nonNegativeInteger"))
			return XSD.nonNegativeInteger;
		else if (type.equals("nonPositiveInteger"))
			return XSD.nonPositiveInteger;		
		else if (type.equals("normalizedString"))
			return XSD.normalizedString;
		else if (type.equals("NOTATION"))
			return XSD.NOTATION;
		else if (type.equals("positiveInteger"))
			return XSD.positiveInteger;		
		else if (type.equals("QName"))
			return XSD.QName;
		else if (type.equals("time"))
			return XSD.time;
		else if (type.equals("token"))
			return XSD.token;		
		else if (type.equals("unsignedByte"))
			return XSD.unsignedByte;
		else if (type.equals("unsignedInt"))
			return XSD.unsignedInt;
		else if (type.equals("unsignedLong"))
			return XSD.unsignedLong;		
		else if (type.equals("unsignedShort"))
			return XSD.unsignedShort;
		else if (type.equals("boolean"))
			return XSD.xboolean;
		else if (type.equals("byte"))
			return XSD.xbyte;		
		else if (type.equals("double"))
			return XSD.xdouble;
		else if (type.equals("float"))
			return XSD.xfloat;		
		else if (type.equals("int"))
			return XSD.xint;
		else if (type.equals("long"))
			return XSD.xlong;		
		else if (type.equals("short"))
			return XSD.xshort;	
		else if (type.equals("string"))
			return XSD.xstring;		
		
		return null;
	}
	
	
	public static XSDDatatype getXSDDatatype(String name) {
		if (name.equals("anyURI")) 
			return XSDDatatype.XSDanyURI;
		else if (name.equals("base64Binary"))
			return XSDDatatype.XSDbase64Binary;
		else if (name.equals("boolean"))
			return XSDDatatype.XSDboolean;
		else if (name.equals("byte"))
			return XSDDatatype.XSDbyte;
		else if (name.equals("date"))
			return XSDDatatype.XSDdate;
		else if (name.equals("dateTime"))
			return XSDDatatype.XSDdateTime;
		else if (name.equals("decimal"))
			return XSDDatatype.XSDdecimal;
		else if (name.equals("double"))
			return XSDDatatype.XSDdouble;
		else if (name.equals("duration"))
			return XSDDatatype.XSDduration;
		else if (name.equals("ENTITY"))
			return XSDDatatype.XSDENTITY;
		else if (name.equals("float"))
			return XSDDatatype.XSDfloat;
		else if (name.equals("gDay"))
			return XSDDatatype.XSDgDay;
		else if (name.equals("gMonth"))
			return XSDDatatype.XSDgMonth;
		else if (name.equals("gMonthDay"))
			return XSDDatatype.XSDgMonthDay;
		else if (name.equals("gYear"))
			return XSDDatatype.XSDgYear;
		else if (name.equals("gYearMonth"))
			return XSDDatatype.XSDgYearMonth;
		else if (name.equals("hexBinary"))
			return XSDDatatype.XSDhexBinary;
		else if (name.equals("ID"))
			return XSDDatatype.XSDID;
		else if (name.equals("IDREF"))
			return XSDDatatype.XSDIDREF;
		else if (name.equals("int"))
			return XSDDatatype.XSDint;
		else if (name.equals("integer"))
			return XSDDatatype.XSDinteger;
		else if (name.equals("language"))
			return XSDDatatype.XSDlanguage;
		else if (name.equals("long"))
			return XSDDatatype.XSDlong;
		else if (name.equals("Name"))
			return XSDDatatype.XSDName;
		else if (name.equals("NCName"))
			return XSDDatatype.XSDNCName;
		else if (name.equals("negativeInteger"))
			return XSDDatatype.XSDnegativeInteger;
		else if (name.equals("NMTOKEN"))
			return XSDDatatype.XSDNMTOKEN;
		else if (name.equals("nonNegativeInteger"))
			return XSDDatatype.XSDnonNegativeInteger;
		else if (name.equals("nonPositiveInteger"))
			return XSDDatatype.XSDnonPositiveInteger;
		else if (name.equals("normalizedString"))
			return XSDDatatype.XSDnormalizedString;
		else if (name.equals("NOTATION"))
			return XSDDatatype.XSDNOTATION;
		else if (name.equals("positiveInteger"))
			return XSDDatatype.XSDpositiveInteger;
		else if (name.equals("QName"))
			return XSDDatatype.XSDQName;
		else if (name.equals("short"))
			return XSDDatatype.XSDshort;	
		else if (name.equals("string"))
			return XSDDatatype.XSDstring;
		else if (name.equals("time"))
			return XSDDatatype.XSDtime;
		else if (name.equals("token"))
			return XSDDatatype.XSDtoken;
		else if (name.equals("unsignedByte"))
			return XSDDatatype.XSDunsignedByte;
		else if (name.equals("unsignedInt"))
			return XSDDatatype.XSDunsignedInt;
		else if (name.equals("unsignedLong"))
			return XSDDatatype.XSDunsignedLong;
		else if (name.equals("unsignedShort"))
			return XSDDatatype.XSDunsignedShort;

		return null;
	}

}
