package io.github.gltomasz;

import java.util.Map;

import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class InformixContainerProvider extends JdbcDatabaseContainerProvider {

  public static final String FULL_IMAGE_NAME = "ibmcom/informix-developer-database";
  private static final String TC_INIT_IFX = "TC_INIT_IFX";
  private static final String TC_POSTINIT_IFX = "TC_POSTINIT_IFX";

  @Override
  public InformixContainer newInstance(final ConnectionUrl url) {
    final Map<String, String> containerParameters = url.getContainerParameters();
    final InformixContainer result = newInformixInstance(url.getImageTag().orElse("latest"));
    if (containerParameters.containsKey(TC_INIT_IFX)) {
      result.withInitFile(MountableFile.forClasspathResource(containerParameters.get(TC_INIT_IFX)));
    }
    if (containerParameters.containsKey(TC_POSTINIT_IFX)) {
      result.withPostInitFile(
          MountableFile.forClasspathResource(containerParameters.get(TC_POSTINIT_IFX)));
    }
    result.withDatabaseName(url.getDbHostString().replace("/", ""));
    result.withReuse(url.isReusable());
    return result;
  }

  @Override
  public InformixContainer newInstance(final String s) {
    return newInformixInstance(s);
  }

  @Override
  public boolean supports(final String databaseType) {
    return databaseType.equals("informix");
  }

  private InformixContainer newInformixInstance(final String tag) {
    return new InformixContainer(DockerImageName.parse(FULL_IMAGE_NAME).withTag(tag));
  }
}
