package tcom.jslope.core;

import com.jslope.persistence.sql.SqlUtil;
import junit.framework.TestCase;

/**
 * Date: 16.01.2006
 */
public class TestCloseDatabase extends TestCase {
    /**
     * this is designed to close database, so that next tests run will not have database errorrs.
     */
    public void testCloseDatabase() {
        SqlUtil.closeDatabase();
    }
}
