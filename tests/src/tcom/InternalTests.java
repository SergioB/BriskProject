package tcom;

import junit.framework.Test;
import junit.framework.TestSuite;
import tcom.jslope.briskproject.networking.TestPropagationList;
import tcom.jslope.briskproject.networking.TestTossing;
import tcom.jslope.core.TestTreeElement;
import tcom.jslope.core.TestCloseDatabase;

/**
 * Date: 17.12.2005
 */
public class InternalTests {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestPropagationList.class);
        suite.addTestSuite(TestTreeElement.class);
        suite.addTestSuite(TestTossing.class);
        suite.addTestSuite(TestCloseDatabase.class);
        return suite;
    }

}
