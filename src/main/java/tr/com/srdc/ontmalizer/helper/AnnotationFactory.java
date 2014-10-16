/**
 * 
 */
package tr.com.srdc.ontmalizer.helper;

import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;

/**
 * @author mustafa
 *
 */
public class AnnotationFactory implements AnnotationParserFactory {

	/* (non-Javadoc)
	 * @see com.sun.xml.xsom.parser.AnnotationParserFactory#create()
	 */
	@Override
	public AnnotationParser create() {
		return new XsdAnnotationParser();
	}

}
