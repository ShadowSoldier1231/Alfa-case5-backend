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

    @Modifying
    @Transactional
    @Query(value = "UPDATE cases SET views_count = views_count + 1 WHERE id = :caseId", nativeQuery = true)
    void incrementViewsCount(@Param("caseId") Long caseId);
}