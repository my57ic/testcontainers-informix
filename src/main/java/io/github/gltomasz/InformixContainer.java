package io.github.gltomasz;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.nio.file.Paths;
import java.time.Duration;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class InformixContainer extends JdbcDatabaseContainer<InformixContainer> {

  private enum FileType {
    INIT_FILE,
    RUN_FILE_POST_INIT
  }

  private static final int INFORMIX_PORT = 9088;
  private static final String DEFAULT_USER = "informix";
  private static final String DEFAULT_PASSWORD = "in4mix";
  private static final String IFX_CONFIG_DIR = "/opt/ibm/config/";

  private String databaseName = "sysadmin";

  public InformixContainer() {
    this(DockerImageName.parse(InformixContainerProvider.FULL_IMAGE_NAME + ":latest"));
  }

  public InformixContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
    this.waitStrategy =
        new LogMessageWaitStrategy()
            .withRegEx(".*Maximum server connections 1.*")
            .withTimes(1)
            .withStartupTimeout(Duration.of(60, SECONDS));
    addExposedPort(INFORMIX_PORT);
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public String getDriverClassName() {
    return "com.informix.jdbc.IfxDriver";
  }

  public Integer getJdbcPort() {
    return getMappedPort(INFORMIX_PORT);
  }

  @Override
  public String getJdbcUrl() {
    final String additionalUrlParams = constructUrlParameters(";", ";");
    return String.format(
        "jdbc:informix-sqli://%s:%d/%s%s",
        getContainerIpAddress(), getJdbcPort(), getDatabaseName(), additionalUrlParams);
  }

  @Override
  public String getPassword() {
    return DEFAULT_PASSWORD;
  }

  @Override
  public String getUsername() {
    return DEFAULT_USER;
  }

  @Override
  public InformixContainer withDatabaseName(final String databaseName) {
    this.databaseName = databaseName;
    return self();
  }

  public InformixContainer withInitFile(final MountableFile mountableFile) {
    setEnvAndCopyFile(mountableFile, FileType.INIT_FILE);
    return self();
  }

  public InformixContainer withPostInitFile(final MountableFile mountableFile) {
    setEnvAndCopyFile(mountableFile, FileType.RUN_FILE_POST_INIT);
    return self();
  }

  @Override
  protected void configure() {
    addEnv("LICENSE", "accept");
  }

  @Override
  protected String getTestQueryString() {
    return "select count(*) from systables";
  }

  @Override
  protected void waitUntilContainerStarted() {
    getWaitStrategy().waitUntilReady(this);
  }

  private void setEnvAndCopyFile(final MountableFile mountableFile, final FileType fileType) {
    addEnv(
        fileType.toString(), Paths.get(mountableFile.getFilesystemPath()).getFileName().toString());
    withCopyFileToContainer(mountableFile, IFX_CONFIG_DIR);
  }
}
