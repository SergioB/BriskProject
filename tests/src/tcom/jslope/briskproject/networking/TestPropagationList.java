package tcom.jslope.briskproject.networking;

import junit.framework.TestCase;
import com.jslope.briskproject.networking.PropagationList;

import java.util.Map;
import java.util.HashMap;

/**
 * Date: 17.12.2005
 */
public class TestPropagationList extends TestCase {
    public void testConversion() {
        Map<String, Integer> array = new HashMap<String, Integer>();
        array.put("a1", 5);
        arrayTest(array);
        array = new HashMap<String,  Integer>();
        array.put("b1", 7);
        array.put("b2", 5);
        array.put("b3", 594935894);
        arrayTest(array);
    }

    private void arrayTest(Map<String, Integer> array) {
        String str = PropagationList.mapToString(array);
        Map<String, Integer> result = PropagationList.stringToMap(str);
        assert array.size() == result.size();
        for (String key: result.keySet()) {
            assert array.containsKey(key);
            assert array.get(key).equals(result.get(key));
        }
    }
}
