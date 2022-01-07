package io.github.gltomasz;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Duration;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

class InformixContainer<SELF extends InformixContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

  private enum FileType {
    INIT_FILE,
    RUN_FILE_POST_INIT
  }

  public static final int INFORMIX_PORT = 9088;
  static final String DEFAULT_USER = "informix";
  static final String DEFAULT_PASSWORD = "in4mix";
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

  @Override
  public String getJdbcUrl() {
    final String additionalUrlParams = constructUrlParameters(";", ";");
    return MessageFormat.format(
        "jdbc:informix-sqli://{0}:{1}/{2}:INFORMIXSERVER=informix{3}",
        getContainerIpAddress(),
        String.valueOf(getMappedPort(INFORMIX_PORT)),
        getDatabaseName(),
        additionalUrlParams);
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
  public SELF withDatabaseName(final String databaseName) {
    this.databaseName = databaseName;
    return self();
  }

  public SELF withInitFile(final MountableFile mountableFile) {
    setEnvAndCopyFile(mountableFile, FileType.INIT_FILE);
    return self();
  }

  public SELF withPostInitFile(final MountableFile mountableFile) {
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
