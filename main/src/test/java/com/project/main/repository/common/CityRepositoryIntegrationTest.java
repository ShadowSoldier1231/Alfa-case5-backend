package com.project.main.repository.common;

import com.project.main.model.common.City;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class CityRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CityRepository cityRepository;

    @Test
    void savedCityIsFoundByCaseInsensitiveNameSearch() {
        cityRepository.save(new City(null, "Москва", "Московская область"));

        Page<City> page = cityRepository.searchByCityName("моск", PageRequest.of(0, 25));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getCityName()).isEqualTo("Москва");
    }

    @Test
    void findAllCitiesReturnsEverythingWhenSearchIsNull() {
        cityRepository.saveAndFlush(new City(null, "Казань", "Республика Татарстан"));
        cityRepository.saveAndFlush(new City(null, "Калининград", "Калининградская область"));

        Page<City> page = cityRepository.findAllCities(null, PageRequest.of(0, 25));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}