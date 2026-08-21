package com.project.main.repository.cases;

import com.project.main.model.cases.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '!' " +
            "AND t.is_active = true", nativeQuery = true)
    List<Tag> searchByName(@Param("query") String query);


    @Query(
            value = """
                SELECT t.id,
                       t.name,
                       t.is_active,
                       COUNT(ct.case_id) AS case_count,
                       t.created_at
                FROM tags t
                LEFT JOIN case_tags ct ON t.id = ct.tag_id
                WHERE CAST(:search AS text) IS NULL
                   OR CAST(:search AS text) = ''
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                GROUP BY t.id, t.name, t.is_active, t.created_at
                """,
            countQuery = """
                SELECT COUNT(t.id)
                FROM tags t
                WHERE CAST(:search AS text) IS NULL
                   OR CAST(:search AS text) = ''
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                """,
            nativeQuery = true
    )
    Page<Object[]> findAdminTagsWithCaseCount(
            @Param("search") String search,
            Pageable pageable
    );



    @Query(
            value = """
        SELECT t.id,
               t.name,
               COUNT(ct.case_id) AS case_count
        FROM tags t
        LEFT JOIN case_tags ct ON t.id = ct.tag_id
        WHERE t.is_active = true
          AND (
                CAST(:search AS text) IS NULL
             OR CAST(:search AS text) = ''
             OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
          )
        GROUP BY t.id, t.name
        """,
            countQuery = """
        SELECT COUNT(t.id)
        FROM tags t
        WHERE t.is_active = true
          AND (
                CAST(:search AS text) IS NULL
             OR CAST(:search AS text) = ''
             OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
          )
        """,
            nativeQuery = true
    )
    Page<Object[]> findPublicTagsWithCaseCount(
            @Param("search") String search,
            Pageable pageable
    );


    @Query(value = """
    SELECT t.id, t.name, t.is_active, COUNT(ct.case_id) AS case_count
    FROM tags t
    LEFT JOIN case_tags ct ON t.id = ct.tag_id
    WHERE t.id IN (:ids) AND t.is_active = true
    GROUP BY t.id, t.name, t.is_active
    """, nativeQuery = true)
    List<Object[]> findTagsWithCaseCountByIds(@Param("ids") List<Long> ids);

}