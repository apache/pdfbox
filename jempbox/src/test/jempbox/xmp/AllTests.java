package test.jempbox.xmp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite for all tests in test.jempbox.xmp.
 * 
 * @author $Author: coezbek $
 * @version $Revision: 1.1 $ ($Date: 2006/12/30 17:27:46 $)
 * 
 */
public class AllTests
{

    /**
     * Hide constructor.
     */
    protected AllTests()
    {
    }

    /**
     * Method returns a test representing all tests in the package
     * test.jempbox.xmp.
     * 
     * @return The test representing all tests in the current package.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for test.jempbox.xmp");
        // $JUnit-BEGIN$
        suite.addTestSuite(XMPSchemaTest.class);
        // $JUnit-END$
        return suite;
    }

}
