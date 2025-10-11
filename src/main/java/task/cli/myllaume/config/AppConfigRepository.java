package task.cli.myllaume.config;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AppConfigRepository {
  public final String filePath;

  public AppConfigRepository(String dirPath) {
    this.filePath = dirPath + "/config.xml";
  }

  private AppConfig read() throws Exception {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(new File(filePath));
    doc.getDocumentElement().normalize();

    org.w3c.dom.Node versionNode = doc.getElementsByTagName("version").item(0);
    String version = versionNode.getTextContent();

    return new AppConfig(version);
  }

  private void writeFile(AppConfig config) throws Exception {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

    Document doc = dBuilder.newDocument();
    Element rootElement = doc.createElement("config");
    doc.appendChild(rootElement);

    Element version = doc.createElement("version");
    version.setTextContent(config.getVersion());
    rootElement.appendChild(version);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(filePath));
    transformer.transform(source, result);
  }

  public void init() throws Exception {
    if (new File(filePath).exists()) {
      return;
    }

    AppConfig config = new AppConfig("1.0");
    writeFile(config);
  }

  public String getAppVersion() throws Exception {
    AppConfig config = read();
    return config.getVersion();
  }

  public void setAppVersion(String version) throws Exception {
    AppConfig config = new AppConfig(version);
    writeFile(config);
  }
}
