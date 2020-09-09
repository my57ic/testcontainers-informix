# testcontainers-informix
Informix module for testcontainers (https://github.com/testcontainers)

### Usage

Adding as maven dependency:
```xml
<dependency>
    <groupId>io.github.gltomasz</groupId>
    <artifactId>testcontainers-informix</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

Adding as gradle dependency:
```groovy
testImplementation "io.github.gltomasz:testcontainers-informix:1.0.0"
```

Using Spring (version >= 2.3.0):
```properties
spring.datasource.url: jdbc:tc:informix:version:///databasename
```
where `version` is the docker image tag.

Additional datasource URL parameters:
- TC_INIT_IFX - init script called before database start
- TC_POSTINIT_IFX=name_of_file.sh - post init shell script called after database start

Datasoruce URL with parameters can look like:

```properties
spring.datasource.url: jdbc:tc:informix:latest:///databasename?TC_INIT_IFX=example_schema.sql&TC_POSTINIT_IFX=example_script.sh
``` 

Testcontainers-informix also needs standard testcontainers dependency, like other modules.

```groovy
testImplementation "org.testcontainers:testcontainers:1.15.0-rc1"
``` 
 