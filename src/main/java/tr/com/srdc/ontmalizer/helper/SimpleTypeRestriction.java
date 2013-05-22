package tr.com.srdc.ontmalizer.helper;

import java.util.Iterator;
import java.util.Vector;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSRestrictionSimpleType;

public class SimpleTypeRestriction {
    public String[] enumeration = null;
    public String maxInclusive = null;
    public String minInclusive = null;
    public String maxExclusive = null;
    public String minExclusive = null;
    public String length = null;
    public String maxLength = null;
    public String minLength = null;
    public String pattern = null;
    public String totalDigits = null;
    public String fractionDigits = null;
    public String whiteSpace = null;
    
    public boolean hasFacet() {
		if (maxInclusive!=null)
			return true;
		if (minInclusive!=null)
			return true;
		if (maxExclusive!=null)
			return true;
		if (minExclusive!=null)
			return true;
		if (length!=null)
			return true;
		if (maxLength!=null)
			return true;
		if (minLength!=null)
			return true;
		if (pattern!=null)
			return true;
		if (totalDigits!=null)
			return true;
		if (fractionDigits!=null)
			return true;
		if (whiteSpace!=null)
			return true;		
		return false;
	}
    
    public void initFacets(XSRestrictionSimpleType restriction){
        Vector<String> en = new Vector<String>();
        Iterator<? extends XSFacet> i = restriction.getDeclaredFacets().iterator();
        while(i.hasNext()){
            XSFacet facet = i.next();
            if(facet.getName().equals(XSFacet.FACET_ENUMERATION))
                en.add(facet.getValue().value);
            if(facet.getName().equals(XSFacet.FACET_MAXINCLUSIVE))
            	maxInclusive = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_MININCLUSIVE))
                minInclusive = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE))
                maxExclusive = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE))
                minExclusive = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_LENGTH))
                length = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_MAXLENGTH))
                maxLength = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_MINLENGTH))
                minLength = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_PATTERN))
                pattern = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_TOTALDIGITS))
                totalDigits = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_FRACTIONDIGITS))
            	fractionDigits = facet.getValue().value;
            if(facet.getName().equals(XSFacet.FACET_WHITESPACE))
            	whiteSpace = facet.getValue().value;
        }
        if(en.size() > 0){
            enumeration = en.toArray(new String[]{});
        }
	}
}
