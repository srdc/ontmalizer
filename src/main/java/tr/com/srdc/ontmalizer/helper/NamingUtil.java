/**
 * 
 */
package tr.com.srdc.ontmalizer.helper;

/**
 * @author Mustafa
 *
 */
public class NamingUtil {

	public static String createPropertyName(String prefix, String propName) {
		
		if(prefix == null || prefix.equals(""))
			return propName;
		else {
			StringBuilder sb = new StringBuilder();
			
			// Mustafa: Use Character.toLowerCase() or Character.toUpperCase() to derive a locale-independent case-insensitive String value.
			// http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug
			sb.append(prefix).append(Character.toUpperCase(propName.charAt(0)))
					.append(propName.substring(1));
			return sb.toString();
		}
	}
	
}
