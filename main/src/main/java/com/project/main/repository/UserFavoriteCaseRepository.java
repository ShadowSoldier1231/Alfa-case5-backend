package com.project.main.repository;

import com.project.main.model.UserFavoriteCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserFavoriteCaseRepository extends JpaRepository<UserFavoriteCase, Long> {

    boolean existsByUserIdAndCaseId(Long userid, Long caseId);
    void deleteByUserIdAndCaseId(Long userid, Long caseId);

    @Query(
            value = """
    SELECT c.id, c.slug, c.title, c.title_en, c.description, c.full_description,
           c.difficulty, c.average_solve_min, c.pdf_url, c.icon_url,
           c.views_count, c.created_at, c.updated_at, ufc.added_at
    FROM favourite_cases ufc
    INNER JOIN cases c ON ufc.case_id = c.id
    WHERE ufc.user_id = :userId
      AND c.is_active = true
      AND (
            CAST(:search AS text) IS NULL
         OR CAST(:search AS text) = ''
         OR LOWER(c.slug) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
         OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
         OR LOWER(c.title_en) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
         OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
      )
    """,
            countQuery = """
    SELECT COUNT(ufc.id)
    FROM favourite_cases ufc
    INNER JOIN cases c ON ufc.case_id = c.id
    WHERE ufc.user_id = :userId
      AND c.is_active = true
      AND (
            CAST(:search AS text) IS NULL
         OR CAST(:search AS text) = ''
         OR LOWER(c.slug) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
         OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
         OR LOWER(c.title_en) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
         OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
      )
    """,
            nativeQuery = true
    )
    Page<Object[]> findFavoriteCases(
            @Param("search") String search,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM UserFavoriteCase u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}
