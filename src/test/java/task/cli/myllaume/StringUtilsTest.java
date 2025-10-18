package task.cli.myllaume;

import static org.junit.Assert.*;

import org.junit.Test;
import task.cli.myllaume.utils.StringUtils;

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

  @Test
  public void testEscapeHtml() {
    assertNull(StringUtils.escapeHtml(null));

    assertEquals("", StringUtils.escapeHtml(""));

    assertEquals("Hello World", StringUtils.escapeHtml("Hello World"));

    assertEquals("&lt;", StringUtils.escapeHtml("<"));
    assertEquals("&gt;", StringUtils.escapeHtml(">"));
    assertEquals("&amp;", StringUtils.escapeHtml("&"));
    assertEquals("&quot;", StringUtils.escapeHtml("\""));
    assertEquals("&#x27;", StringUtils.escapeHtml("'"));
    assertEquals("&#x2F;", StringUtils.escapeHtml("/"));
    assertEquals("&#x60;", StringUtils.escapeHtml("`"));

    assertEquals("&lt;&gt;&amp;&quot;&#x27;&#x2F;&#x60;", StringUtils.escapeHtml("<>&\"'/`"));

    assertEquals(
        "&lt;script&gt;alert(&#x27;XSS&#x27;)&lt;&#x2F;script&gt;",
        StringUtils.escapeHtml("<script>alert('XSS')</script>"));

    assertEquals(
        "Hello &lt;div class=&quot;test&quot;&gt;World &amp; Friends&lt;&#x2F;div&gt;",
        StringUtils.escapeHtml("Hello <div class=\"test\">World & Friends</div>"));
  }
}
