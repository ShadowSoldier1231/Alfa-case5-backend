package com.project.main.repository;

import com.project.main.model.*;
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


    @Query(value = "SELECT * FROM cases WHERE is_active = true ORDER BY created_at DESC", nativeQuery = true)
    List<CaseEntity> findAllByIsActiveTrueOrderByCreatedAtDesc();


    @Query(value = "SELECT * FROM cases ORDER BY created_at DESC", nativeQuery = true)
    List<CaseEntity> findAllByOrderByCreatedAtDesc();


    @Query(value = "SELECT t.name, COUNT(c.id) " +
            "FROM tags t " +
            "LEFT JOIN case_tags ct ON t.id = ct.tag_id " +
            "LEFT JOIN cases c ON ct.case_id = c.id " +
            "WHERE t.is_active = true " +
            "GROUP BY t.id, t.name " +
            "ORDER BY COUNT(c.id) DESC", nativeQuery = true)
    List<Object[]> findActiveTagsWithCaseCount();


    @Query(value = "SELECT c.* FROM cases c " +
            "WHERE c.is_active = true " +
            "ORDER BY c.sort_order ASC, c.created_at DESC", nativeQuery = true)
    List<CaseEntity> findAllActiveWithTags();




    @Query(value = "SELECT COUNT(*) FROM cases WHERE is_active = true", nativeQuery = true)
    long countActiveCases();

    @Query(value = "SELECT difficulty, COUNT(*) FROM cases " +
            "WHERE is_active = true GROUP BY difficulty", nativeQuery = true)
    List<Object[]> countCasesByDifficulty();

    @Modifying
    @Transactional
    @Query(value = "UPDATE cases SET views_count = views_count + 1 WHERE id = :caseId", nativeQuery = true)
    void incrementViewsCount(@Param("caseId") Long caseId);


    @Query(value = "SELECT * FROM cases WHERE difficulty = :difficulty AND is_active = true", nativeQuery = true)
    List<CaseEntity> findByDifficultyAndIsActiveTrue(@Param("difficulty") String difficulty);


    @Query(value = "SELECT c.* FROM cases c " +
            "WHERE c.slug = :slug", nativeQuery = true)
    Optional<CaseEntity> findBySlugWithTags(@Param("slug") String slug);

    @Query(value = "SELECT DISTINCT c.* FROM cases c " +
            "JOIN case_tags ct ON c.id = ct.case_id " +
            "WHERE ct.tag_id = :tagId AND c.is_active = true", nativeQuery = true)
    List<CaseEntity> findByTagIdAndIsActiveTrue(@Param("tagId") Long tagId);

    @Query(value = "SELECT DISTINCT c.* FROM cases c " +
            "JOIN case_tags ct ON c.id = ct.case_id " +
            "JOIN tags t ON ct.tag_id = t.id " +
            "WHERE t.name IN (:tagNames) AND c.is_active = true " +
            "ORDER BY c.sort_order ASC, c.created_at DESC", nativeQuery = true)
    List<CaseEntity> findByTagsAndIsActiveTrue(@Param("tagNames") List<String> tagNames);


    Optional<CaseEntity> findBySlug(String slug);
    Optional<CaseEntity> findByIdAndIsActiveTrue(Long id);
    boolean existsBySlug(String slug);


}