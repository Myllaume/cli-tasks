package task.cli.myllaume;

import static org.junit.Assert.*;

import java.util.Map;
import org.junit.Test;
import task.cli.myllaume.utils.FormDataParser;

public class FormDataParserTest {

  @Test
  public void shouldParseSingleKeyValuePair() {
    String formData = "name=John";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(1, result.size());
    assertEquals("John", result.get("name"));
  }

  @Test
  public void shouldParseMultipleKeyValuePairs() {
    String formData = "name=John&age=30&city=Paris";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(3, result.size());
    assertEquals("John", result.get("name"));
    assertEquals("30", result.get("age"));
    assertEquals("Paris", result.get("city"));
  }

  @Test
  public void shouldReturnEmptyMapForNullInput() {
    Map<String, String> result = FormDataParser.parse(null);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldReturnEmptyMapForEmptyString() {
    Map<String, String> result = FormDataParser.parse("");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldDecodeUrlEncodedCharacters() {
    String formData = "name=John+Doe&message=Hello%20World%21";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(2, result.size());
    assertEquals("John Doe", result.get("name"));
    assertEquals("Hello World!", result.get("message"));
  }

  @Test
  public void shouldHandleSpecialEncodedCharacters() {
    String formData = "email=test%40example.com&url=https%3A%2F%2Fexample.com";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals("test@example.com", result.get("email"));
    assertEquals("https://example.com", result.get("url"));
  }

  @Test
  public void shouldIgnoreMalformedPairsWithoutEquals() {
    String formData = "name=John&invalidpair&city=Paris";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(2, result.size());
    assertEquals("John", result.get("name"));
    assertEquals("Paris", result.get("city"));
    assertNull(result.get("invalidpair"));
  }

  @Test
  public void shouldHandleEmptyValues() {
    String formData = "name=John&middleName=&age=30";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(3, result.size());
    assertEquals("John", result.get("name"));
    assertEquals("", result.get("middleName"));
    assertEquals("30", result.get("age"));
  }

  @Test
  public void shouldIgnoreEmptyKeys() {
    String formData = "=value&name=John";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(1, result.size());
    assertEquals("John", result.get("name"));
  }

  @Test
  public void shouldHandleValuesWithMultipleEquals() {
    String formData = "equation=a=b&name=John";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(2, result.size());
    assertEquals("a=b", result.get("equation"));
    assertEquals("John", result.get("name"));
  }

  @Test
  public void shouldOverwriteDuplicateKeysWithLastValue() {
    String formData = "name=John&name=Jane&name=Bob";

    try {
      FormDataParser.parse(formData);
      fail("Should have thrown IllegalArgumentException for duplicated keys");
    } catch (IllegalArgumentException e) {
      assertEquals("Key \"name\" is duplicated.", e.getMessage());
    }
  }

  @Test
  public void shouldHandleAccentedCharacters() {
    String formData = "ville=%C3%89vry&pr%C3%A9nom=Jos%C3%A9";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(2, result.size());
    assertEquals("Évry", result.get("ville"));
    assertEquals("José", result.get("prénom"));
  }

  @Test
  public void shouldHandleOnlyAmpersands() {
    String formData = "&&&";
    Map<String, String> result = FormDataParser.parse(formData);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldHandleSingleAmpersand() {
    String formData = "&";
    Map<String, String> result = FormDataParser.parse(formData);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldHandleComplexFormData() {
    String formData =
        "username=john.doe&email=john%40example.com&age=25&city=New+York&subscribe=true";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(5, result.size());
    assertEquals("john.doe", result.get("username"));
    assertEquals("john@example.com", result.get("email"));
    assertEquals("25", result.get("age"));
    assertEquals("New York", result.get("city"));
    assertEquals("true", result.get("subscribe"));
  }

  @Test
  public void shouldHandleNumericKeys() {
    String formData = "1=first&2=second&3=third";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals(3, result.size());
    assertEquals("first", result.get("1"));
    assertEquals("second", result.get("2"));
    assertEquals("third", result.get("3"));
  }

  @Test
  public void shouldHandleWhitespaceInValues() {
    String formData = "name=John%20%20Doe&title=Software%20Engineer";
    Map<String, String> result = FormDataParser.parse(formData);

    assertEquals("John  Doe", result.get("name"));
    assertEquals("Software Engineer", result.get("title"));
  }
}
