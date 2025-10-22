package task.cli.myllaume;

import java.io.IOException;

public interface WebServer {

  void start() throws IOException;

  void stop(int delaySeconds);

  String getUrl();
}
