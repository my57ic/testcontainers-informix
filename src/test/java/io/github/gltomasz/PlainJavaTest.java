package io.github.gltomasz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public class PlainJavaTest {

  @Container
  private final JdbcDatabaseContainer<?> dbContainer =
      new InformixContainerProvider().newInstance().withInitScript("test-schema.sql");

  @Test
  public void plainJavaTest() throws Exception {

    final DataSource dataSource =
        DataSourceBuilder.create()
            .url(dbContainer.getJdbcUrl())
            .username(dbContainer.getUsername())
            .password(dbContainer.getPassword())
            .build();

    final List<String> schemas = new ArrayList<>();
    try (final Connection connection = dataSource.getConnection()) {
      connection.createStatement().execute("database test_database");
      try (final ResultSet resultSet = connection.getMetaData().getSchemas()) {
        while (resultSet.next()) {
          final String tableCatalog = resultSet.getString("TABLE_CATALOG");
          final String tableSchema = resultSet.getString("TABLE_SCHEM");
          schemas.add(tableCatalog + "/" + tableSchema);
        }
      }
    }
    System.out.println(schemas);
  }
}
