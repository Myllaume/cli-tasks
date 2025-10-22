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
    assertTrue(html.contains("<ul></ul>"));
  }

  @Test
  public void testRenderHomeWithMultipleTasks() {
    TaskHtmlRenderer renderer = new TaskHtmlRenderer(3000);
    List<Task> tasks = new ArrayList<>();

    Task task1 =
        Task.of(
            1,
            "Faire les courses",
            false,
            "fairelescourses",
            TaskPriority.LOW,
            Instant.now(),
            null,
            null,
            null);
    Task task2 =
        Task.of(
            2,
            "Appeler le dentiste",
            false,
            "appelerleedentiste",
            TaskPriority.HIGH,
            Instant.now(),
            null,
            null,
            null);
    tasks.add(task1);
    tasks.add(task2);

    String html = renderer.renderHome(tasks);

    assertNotNull(html);
    assertTrue(html.contains("<li>Faire les courses</li>"));
    assertTrue(html.contains("<li>Appeler le dentiste</li>"));
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

    String html = renderer.render500();

    assertNotNull(html);
    assertTrue(html.contains("<!DOCTYPE html>"));
    assertTrue(html.contains("<title>Erreur</title>"));
    assertTrue(html.contains("500 - Erreur serveur"));
  }
}
