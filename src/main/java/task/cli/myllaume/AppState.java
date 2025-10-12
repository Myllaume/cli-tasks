package task.cli.myllaume;

import java.time.Instant;
import task.cli.myllaume.config.AppConfigRepository;
import task.cli.myllaume.db.ProjectsRepository;

public class AppState {
  private final AppConfigRepository config;
  private final ProjectsRepository projectsRepository;

  public AppState(AppConfigRepository config, ProjectsRepository projectsRepository) {
    this.config = config;
    this.projectsRepository = projectsRepository;
    try {
      projectsRepository.initTables();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize database tables", e);
    }
  }

  public boolean isFirstLaunch() throws Exception {
    boolean configExists = config.fileExists();
    boolean hasProjects = !projectsRepository.isEmpty();

    if (!configExists && hasProjects) {
      throw new IllegalStateException("Config file is missing but projects exist in database.");
    }

    if (configExists && !hasProjects) {
      throw new IllegalStateException("Config file exists but no projects found in database.");
    }

    return !configExists && !hasProjects;
  }

  public void firstLaunchSetup() throws Exception {
    if (!isFirstLaunch()) {
      throw new IllegalStateException(
          "Cannot run first launch setup: application is already initialized.");
    }

    try {
      config.init();
      ProjectData defaultProjectData = ProjectData.of("default", Instant.now());
      projectsRepository.insertDefaultProjectIfNoneExists(defaultProjectData);
    } catch (Exception e) {
      throw new IllegalStateException(
          "First launch setup failed. The application may be in an inconsistent state. "
              + "Please check the config file and database manually.",
          e);
    }
  }
}
