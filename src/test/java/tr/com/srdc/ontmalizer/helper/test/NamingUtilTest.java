package tr.com.srdc.ontmalizer.helper.test;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Test;

import tr.com.srdc.ontmalizer.helper.NamingUtil;

/**
 * @author rahmivolkan
 */
public class NamingUtilTest {
    private static Locale defaultLocale = Locale.getDefault();
    
	@Test
	public void testCreatePropertyName() {
		final String prefix = "pre";
		final String propName = "problemDate";

		// with no prefix
		assertEquals(propName, NamingUtil.createPropertyName(null, propName));
		assertEquals(propName, NamingUtil.createPropertyName("", propName));
		
		// with prefix
		assertEquals("preProblemDate", NamingUtil.createPropertyName(prefix, propName));
	}
	
	@Test
	public void testCreatePropertyNameForDifferentLocales() {
		// Danish locale
		Locale.setDefault(new Locale("dk", "DK"));
		assertEquals("preÆtesting", NamingUtil.createPropertyName("pre", "ætesting"));
		assertEquals("preTesting", NamingUtil.createPropertyName("pre", "testing"));
		
		// Turkish locale (for i)
		Locale.setDefault(new Locale("tr", "TR"));
		assertEquals("preIlaç", NamingUtil.createPropertyName("pre", "ilaç"));
		assertEquals("preIlaç", NamingUtil.createPropertyName("pre", "ılaç"));
	}

    @AfterClass
    public static void restoreLocale() {
        Locale.setDefault(defaultLocale);
    }

}
