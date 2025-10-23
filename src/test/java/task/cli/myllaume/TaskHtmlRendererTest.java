package task.cli.myllaume;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TaskHtmlRendererTest {

  @Test
  public void testRenderHomeWithEmptyTasks() {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(8080);
    List<Task> tasks = new ArrayList<>();

    String html = renderer.renderHome(tasks);

    assertNotNull(html);
    assertTrue(html.contains("<!DOCTYPE html>"));
    assertTrue(html.contains("<title>Tasks</title>"));
    assertTrue(html.contains("<tbody></tbody>"));
  }

  @Test
  public void testRenderHomeWithMultipleTasks() {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(3000);
    List<Task> tasks = new ArrayList<>();

    tasks.add(
        Task.of(
            1,
            "Faire les courses",
            false,
            "fairelescourses",
            TaskPriority.LOW,
            Instant.now(),
            null,
            null,
            null));

    String html = renderer.renderHome(tasks);

    assertNotNull(html);
    assertTrue(html.contains("<tr><td>Faire les courses</td><td>faible</td></tr>"));
  }

  @Test
  public void testRenderHomeEscapesHtml() {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(8080);
    List<Task> tasks = new ArrayList<>();
    Task task =
        Task.of(
            1,
            "<script>alert('XSS')</script>",
            false,
            "xss",
            TaskPriority.LOW,
            Instant.now(),
            null,
            null,
            null);
    tasks.add(task);

    String html = renderer.renderHome(tasks);

    assertNotNull(html);
    assertFalse(html.contains("<script>"));
    assertTrue(html.contains("&lt;script&gt;"));
    assertTrue(html.contains("alert(&#x27;XSS&#x27;)"));
  }

  @Test
  public void testRenderFormWithPriorities() {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(8080);

    String html = renderer.renderForm();

    assertNotNull(html);
    assertFalse(html.contains("<option value=\"1\">faible</option>"));
  }

  @Test
  public void testRender404() {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(8080);

    String html = renderer.render404();

    assertNotNull(html);
    assertTrue(html.contains("<!DOCTYPE html>"));
    assertTrue(html.contains("<title>404 Not Found</title>"));
    assertTrue(html.contains("404 - Page non trouvée"));
  }

  @Test
  public void testRender500() {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(8080);

    String html = renderer.render500("message");

    assertNotNull(html);
    assertTrue(html.contains("<!DOCTYPE html>"));
    assertTrue(html.contains("<title>Erreur</title>"));
    assertTrue(html.contains("500 - Erreur serveur"));
    assertTrue(html.contains("<pre>message</pre>"));
  }
}
