package task.cli.myllaume;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testNormalizeNull() {
        String result = StringUtils.normalizeString(null);
        assertNull(result);
    }

    @Test
    public void testNormalizeEmpty() {
        String result = StringUtils.normalizeString("");
        assertEquals("", result);
    }

    @Test
    public void testNormalizeRemoveSpace() {
        String result = StringUtils.normalizeString("Hello World");
        assertEquals("helloworld", result);
    }

    @Test
    public void testNormalizeRemoveAccents() {
        String result = StringUtils.normalizeString("Càféëñ");
        assertEquals("cafeen", result);
    }

    @Test
    public void testNormalizeKeepAlphanumerique() {
        String result = StringUtils.normalizeString("Hello-World,_123!");
        assertEquals("helloworld123", result);
    }

    @Test
    public void testNormalizeLowercase() {
        String result = StringUtils.normalizeString("UPPERCASE");
        assertEquals("uppercase", result);
    }

    @Test
    public void testNormalizeTrim() {
        String result = StringUtils.normalizeString("   ");
        assertEquals("", result);
    }

}
