package com.outsera.razzies;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Boots the whole application (real CSV load, JPA, H2, MVC) for an integration test.
 * <p>
 * Every Spring context gets its own in-memory database: contexts with different CSVs are cached
 * side by side by the test framework and must not share (and overwrite) each other's data.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid}")
@AutoConfigureMockMvc
public @interface IntegrationTest {
}
