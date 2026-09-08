package com.project.main.repository.cases;

import com.project.main.model.cases.Tag;
import com.project.main.repository.projection.AdminTagRow;
import com.project.main.repository.projection.PublicTagRow;
import com.project.main.repository.projection.TagWithCountRow;
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


    boolean existsByName(String name);



    @Query(
            value = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.is_active AS is_active,
                    CAST(COUNT(ct.case_id) AS bigint) AS case_count,
                    t.created_at AS created_at
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
    Page<AdminTagRow> findAdminTagsWithCaseCount(
            @Param("search") String search,
            Pageable pageable
    );



    @Query(
            value = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    CAST(COUNT(ct.case_id) AS bigint) AS case_count
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
    Page<PublicTagRow> findPublicTagsWithCaseCount(
            @Param("search") String search,
            Pageable pageable
    );


    @Query(
            value = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.is_active AS is_active,
                    CAST(COUNT(ct.case_id) AS bigint) AS case_count
                FROM tags t
                LEFT JOIN case_tags ct ON t.id = ct.tag_id
                WHERE t.id IN (:ids)
                  AND t.is_active = true
                GROUP BY t.id, t.name, t.is_active
                """,
            nativeQuery = true
    )
    List<TagWithCountRow> findTagsWithCaseCountByIds(@Param("ids") List<Long> ids);

}