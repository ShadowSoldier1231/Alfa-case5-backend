package com.project.main.repository;


import com.project.main.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByCityName(String cityName);

    Optional<City> findByCityName(String cityName);

    boolean existsByRegionName(String regionName);

    List<City> findAllByCityName(String cityName);

    List<City> findByCityNameContainingIgnoreCase(String cityName);
}


