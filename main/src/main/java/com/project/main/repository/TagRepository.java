package com.project.main.repository;

import com.project.main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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

    @Query(value = "SELECT t.name, COUNT(ct.case_id) " +
            "FROM tags t " +
            "LEFT JOIN case_tags ct ON t.id = ct.tag_id " +
            "WHERE t.is_active = true " +
            "GROUP BY t.id, t.name " +
            "ORDER BY COUNT(ct.case_id) DESC", nativeQuery = true)
    List<Object[]> findTagsWithCaseCount();


    @Query(value = "SELECT t.* FROM tags t " +
            "LEFT JOIN case_tags ct ON t.id = ct.tag_id " +
            "WHERE t.is_active = true " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(ct.case_id) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Tag> findMostPopularTags(@Param("limit") int limit);


    @Query(value = "SELECT * FROM tags t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND t.is_active = true", nativeQuery = true)
    List<Tag> searchByName(@Param("query") String query);
}