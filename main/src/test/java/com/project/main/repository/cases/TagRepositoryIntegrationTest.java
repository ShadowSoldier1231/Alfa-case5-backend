package com.project.main.repository.cases;

import com.project.main.model.cases.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class TagRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TagRepository tagRepository;

    @Test
    void savedTagIsFoundByName() {
        Tag tag = new Tag();
        tag.setName("Spring Boot");
        tag.setActive(true);
        tagRepository.saveAndFlush(tag);

        assertThat(tagRepository.existsByName("Spring Boot")).isTrue();
    }
}