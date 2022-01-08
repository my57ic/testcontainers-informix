package io.github.gltomasz;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@SpringBootTest(
    properties = {
      "spring.datasource.url = jdbc:informix-sqli://localhost:9088/test_database:INFORMIXSERVER=informix",
      "spring.datasource.username = informix",
      "spring.datasource.password =  in4mix"
    })
@ContextConfiguration(initializers = ManualStartTest.Initializer.class)
class ManualStartTest {

  /** Replace port number of spring.datasource.url as it's dynamically assigned */
  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
      final Integer mappedPort = informixContainer.getJdbcPort();
      final String jdbcUrl =
          MessageFormat.format(
              "spring.datasource.url=jdbc:informix-sqli://localhost:{0,number,#}/test_database:INFORMIXSERVER=informix",
              mappedPort);
      TestPropertyValues.of(jdbcUrl).applyTo(applicationContext.getEnvironment());
    }
  }

  @Container
  private static final InformixContainer informixContainer =
      new InformixContainer()
          .withInitFile(MountableFile.forClasspathResource("test-schema.sql"))
          .acceptLicense();

  @Autowired JdbcTemplate jdbcTemplate;

  @Test
  public void testContainerIsRunning() {
    assertAll(
        () -> assertEquals("informix", informixContainer.getUsername()),
        () -> assertEquals("in4mix", informixContainer.getPassword()),
        () -> assertEquals(2, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test_table")));
  }
}
