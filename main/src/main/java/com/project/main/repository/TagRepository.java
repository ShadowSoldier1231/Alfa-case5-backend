package com.project.main.repository;

import com.project.main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findByIdAndIsActiveTrue(Long id);

    boolean existsByName(String name);


    List<Tag> findByIsActiveTrueOrderByNameAsc();



    List<Tag> findByNameIn(List<String> names);

    List<Tag> findByIdIn(List<Long> ids);



    @Query("SELECT t, COUNT(ct) FROM Tag t " +
            "LEFT JOIN t.caseTags ct " +
            "WHERE t.isActive = true " +
            "GROUP BY t.id, t.name " +
            "ORDER BY COUNT(ct) DESC")
    List<Object[]> findTagsWithCaseCount();



    @Query("SELECT t FROM Tag t " +
            "LEFT JOIN t.caseTags ct " +
            "WHERE t.isActive = true " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(ct) DESC")
    List<Tag> findMostPopularTags(Pageable pageable);


    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) AND t.isActive = true")
    List<Tag> searchByName(@Param("query") String query);
}