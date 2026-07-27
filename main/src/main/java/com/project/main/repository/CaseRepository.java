package com.project.main.repository;

import com.project.main.enums.Difficulty;
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



    Optional<CaseEntity> findBySlug(String slug);

    Optional<CaseEntity> findByIdAndIsActiveTrue(Long id);

    boolean existsBySlug(String slug);


    List<CaseEntity> findByIsActiveTrueOrderBySortOrderAscCreatedAtDesc();

    Page<CaseEntity> findByIsActiveTrue(Pageable pageable);


    List<CaseEntity> findByDifficultyAndIsActiveTrue(Difficulty difficulty);

    Page<CaseEntity> findByDifficultyAndIsActiveTrue(Difficulty difficulty, Pageable pageable);



    @Query("SELECT DISTINCT c FROM CaseEntity c " +
            "JOIN c.caseTags ct " +
            "JOIN ct.tag t " +
            "WHERE t.name IN :tagNames AND c.isActive = true " +
            "ORDER BY c.sortOrder ASC, c.createdAt DESC")
    List<CaseEntity> findByTagsAndIsActiveTrue(@Param("tagNames") List<String> tagNames);

    @Query("SELECT DISTINCT c FROM CaseEntity c " +
            "JOIN c.caseTags ct " +
            "JOIN ct.tag t " +
            "WHERE t.id = :tagId AND c.isActive = true")
    List<CaseEntity> findByTagIdAndIsActiveTrue(@Param("tagId") Long tagId);



    @Modifying
    @Transactional
    @Query("UPDATE CaseEntity c SET c.viewsCount = c.viewsCount + 1 WHERE c.id = :caseId")
    void incrementViewsCount(@Param("caseId") Long caseId);



    @Query("SELECT c FROM CaseEntity c " +
            "LEFT JOIN FETCH c.caseTags ct " +
            "LEFT JOIN FETCH ct.tag " +
            "WHERE c.slug = :slug")
    Optional<CaseEntity> findBySlugWithTags(@Param("slug") String slug);

    @Query("SELECT c FROM CaseEntity c " +
            "LEFT JOIN FETCH c.caseTags ct " +
            "LEFT JOIN FETCH ct.tag " +
            "WHERE c.isActive = true " +
            "ORDER BY c.sortOrder ASC, c.createdAt DESC")
    List<CaseEntity> findAllActiveWithTags();



    @Query("SELECT COUNT(c) FROM CaseEntity c WHERE c.isActive = true")
    long countActiveCases();

    @Query("SELECT c.difficulty, COUNT(c) FROM CaseEntity c " +
            "WHERE c.isActive = true GROUP BY c.difficulty")
    List<Object[]> countCasesByDifficulty();
}