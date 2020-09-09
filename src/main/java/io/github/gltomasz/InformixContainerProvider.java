package io.github.gltomasz;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.jdbc.ConnectionUrl;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Map;

public class InformixContainerProvider extends JdbcDatabaseContainerProvider {

    public static final String FULL_IMAGE_NAME = "ibmcom/informix-developer-database";
    private static final String IFX_CONFIG_DIR = "/opt/ibm/config/";
    private static final String TC_INIT_IFX = "TC_INIT_IFX";
    private static final String TC_POSTINIT_IFX = "TC_POSTINIT_IFX";

    @Override
    public boolean supports(String databaseType) {
        return databaseType.equals("informix");
    }

    @Override
    public JdbcDatabaseContainer newInstance(String s) {
        return newInformixInstance(s);
    }

    @Override
    public JdbcDatabaseContainer newInstance(ConnectionUrl url) {
        Map<String, String> containerParameters = url.getContainerParameters();
        InformixContainer result = newInformixInstance(url.getImageTag().orElse("latest"));
        if (containerParameters.containsKey(TC_INIT_IFX)) {
            result.withCopyFileToContainer(MountableFile.forClasspathResource(containerParameters.get(TC_INIT_IFX)), IFX_CONFIG_DIR)
                    .withInitFile(containerParameters.get(TC_INIT_IFX));
        }
        if (containerParameters.containsKey(TC_POSTINIT_IFX)) {
            result.withCopyFileToContainer(MountableFile.forClasspathResource(containerParameters.get(TC_POSTINIT_IFX)), IFX_CONFIG_DIR)
                    .withPostInitFile(containerParameters.get(TC_POSTINIT_IFX));
        }
        result.withDatabaseName(url.getDbHostString().replace("/", ""));
        result.withReuse(url.isReusable());
        return result;
    }

    private InformixContainer newInformixInstance(String tag) {
        return new InformixContainer(DockerImageName.parse(FULL_IMAGE_NAME).withTag(tag));
    }
}
