package com.project.main.repository;


import com.project.main.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByCityName(String cityName);

    Optional<City> findByCityName(String cityName);

    boolean existsByRegionName(String regionName);

    List<City> findAllByCityName(String cityName);


    @Query(
            value = """
        SELECT c.*
        FROM city c
        WHERE LOWER(c.city_name) LIKE LOWER(CONCAT('%', CAST(:cityName AS text), '%')) ESCAPE '!'
        """,
            countQuery = """
        SELECT COUNT(c.id)
        FROM city c
        WHERE LOWER(c.city_name) LIKE LOWER(CONCAT('%', CAST(:cityName AS text), '%')) ESCAPE '!'
        """,
            nativeQuery = true
    )
    Page<City> searchByCityName(
            @Param("cityName") String cityName,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT c.*
        FROM city c
        WHERE CAST(:search AS text) IS NULL
           OR CAST(:search AS text) = ''
           OR LOWER(c.city_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
           OR LOWER(c.region_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
        """,
            countQuery = """
        SELECT COUNT(c.id)
        FROM city c
        WHERE CAST(:search AS text) IS NULL
           OR CAST(:search AS text) = ''
           OR LOWER(c.city_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
           OR LOWER(c.region_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
        """,
            nativeQuery = true
    )
    Page<City> findAllCities(
            @Param("search") String search,
            Pageable pageable
    );

    List<City> findByCityNameContainingIgnoreCase(String cityName);
}


