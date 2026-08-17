package com.project.main.repository;

import com.project.main.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, Long> {






    @Query(value = "SELECT * FROM cases WHERE id = :id", nativeQuery = true)
    Optional<CaseEntity> findById(@Param("id") Long id);



    @Query(value = "UPDATE cases SET views_count = views_count + 1 WHERE id = :id", nativeQuery = true)
    @Modifying
    @Transactional
    void incrementViewsCount(@Param("id") Long id);



    @Query(value = "SELECT ct.case_id, t.id, t.name, " +
            "(SELECT COUNT(*) FROM case_tags ct2 WHERE ct2.tag_id = t.id) " +
            "FROM case_tags ct " +
            "JOIN tags t ON t.id = ct.tag_id " +
            "WHERE ct.case_id IN (:caseIds) " +
            "ORDER BY t.id ASC", nativeQuery = true)
    List<Object[]> findTagsByCaseIds(@Param("caseIds") List<Long> caseIds);



    @Query(
            value = """
        SELECT c.*
        FROM cases c
        WHERE CAST(:search AS text) IS NULL
           OR CAST(:search AS text) = ''
           OR LOWER(c.slug) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
           OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
           OR LOWER(c.title_en) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
           OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
        """,
            countQuery = """
        SELECT COUNT(c.id)
        FROM cases c
        WHERE CAST(:search AS text) IS NULL
           OR CAST(:search AS text) = ''
           OR LOWER(c.slug) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
           OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
           OR LOWER(c.title_en) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
           OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
        """,
            nativeQuery = true
    )
    Page<CaseEntity> findAdminCases(
            @Param("search") String search,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT c.*
        FROM cases c
        WHERE c.is_active = true
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
        SELECT COUNT(c.id)
        FROM cases c
        WHERE c.is_active = true
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
    Page<CaseEntity> findPublicCases(
            @Param("search") String search,
            Pageable pageable
    );

}