package io.github.gltomasz;

import org.assertj.core.data.MapEntry;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
@SpringBootTest(properties = { "spring.datasource.url = jdbc:tc:informix:latest:///test_database?TC_INIT_IFX=test-schema.sql" })
class AutomaticStartTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void test() {
        List<MapEntry<String, Integer>> result = selectAllFromTestTable();
        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("testcontainers-1", result.get(0).key),
                () -> assertEquals("testcontainers-2", result.get(1).key)
        );
    }

    @NotNull
    private List<MapEntry<String, Integer>> selectAllFromTestTable() {
        return jdbcTemplate.query("select * from test_table", (resultSet, i) -> MapEntry.entry(resultSet.getString("some_name"), resultSet.getInt("some_value")));
    }
}
