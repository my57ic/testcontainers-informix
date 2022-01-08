CREATE DATABASE test_database WITH LOG;

CREATE TABLE test_table (
    some_name varchar(50),
    some_value int
);

INSERT INTO test_table VALUES("testcontainers-1", 1);
INSERT INTO test_table VALUES("testcontainers-2", 2);