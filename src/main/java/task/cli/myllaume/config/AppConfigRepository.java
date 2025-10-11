package task.cli.myllaume.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

// @TODO Add default and init project to config

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

        List<TaskConfig> tasks = new ArrayList<TaskConfig>();
        org.w3c.dom.Node tasksContainer = doc.getElementsByTagName("tasks").item(0);
        org.w3c.dom.NodeList taskNodes = ((Element) tasksContainer).getElementsByTagName("task");

        for (int i = 0; i < taskNodes.getLength(); i++) {
            org.w3c.dom.Node taskNode = taskNodes.item(i);
            NamedNodeMap attributes = taskNode.getAttributes();
            String filePath = attributes.getNamedItem("file").getNodeValue();
            int index = Integer.parseInt(attributes.getNamedItem("index").getNodeValue());
            TaskConfig taskConfig = new TaskConfig(filePath, index);
            tasks.add(taskConfig);
        }

        return new AppConfig(version, tasks);
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

        Element tasksElement = doc.createElement("tasks");
        rootElement.appendChild(tasksElement);

        List<TaskConfig> tasks = config.getTasksFiles();
        for (TaskConfig taskConfig : tasks) {
            Element taskPath = doc.createElement("task");
            taskPath.setAttribute("file", taskConfig.getFilePath());
            taskPath.setAttribute("index", Integer.toString(taskConfig.getIndex()));
            tasksElement.appendChild(taskPath);
        }

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

        AppConfig config = new AppConfig("1.0", new ArrayList<TaskConfig>());
        writeFile(config);
    }

    public TaskConfig getTaskConfig(String filePath) throws Exception {
        AppConfig config = read();
        List<TaskConfig> tasks = config.getTasksFiles();
        return tasks.stream()
                .filter(t -> t.getFilePath().equals(filePath))
                .findFirst()
                .orElse(null);
    }

    public void addTaskConfig(TaskConfig taskConfig) throws Exception {
        AppConfig config = read();
        List<TaskConfig> tasks = config.getTasksFiles();
        tasks.add(taskConfig);
        writeFile(new AppConfig(config.getVersion(), tasks));
    }

    public void removeTaskConfig(String taskFilePath) throws Exception {
        AppConfig config = read();
        List<TaskConfig> tasks = config.getTasksFiles();

        TaskConfig taskToRemove = tasks.stream()
                .filter(t -> t.getFilePath().equals(taskFilePath))
                .findFirst()
                .orElse(null);

        if (taskToRemove != null) {
            tasks.remove(taskToRemove);
        }

        writeFile(new AppConfig(config.getVersion(), tasks));
    }

    public void updateTaskConfig(TaskConfig taskConfig) throws Exception {
        AppConfig config = read();
        List<TaskConfig> tasks = config.getTasksFiles();

        TaskConfig taskToUpdate = tasks.stream()
                .filter(t -> t.getFilePath().equals(taskConfig.getFilePath()))
                .findFirst()
                .orElse(null);

        if (taskToUpdate != null) {
            tasks.remove(taskToUpdate);
            tasks.add(taskConfig);
        }

        writeFile(new AppConfig(config.getVersion(), tasks));
    }

    public String getAppVersion() throws Exception {
        AppConfig config = read();
        return config.getVersion();
    }

    public void setAppVersion(String version) throws Exception {
        AppConfig config = read();
        writeFile(new AppConfig(version, config.getTasksFiles()));
    }

}
