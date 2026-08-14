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
    List<CaseEntity> findAllActive();
    @Query(value = "SELECT * FROM cases ORDER BY created_at DESC", nativeQuery = true)
    List<CaseEntity> findAllForAdmin();

    @Query(value = "SELECT * FROM cases WHERE id = :id", nativeQuery = true)
    Optional<CaseEntity> findById(@Param("id") Long id);



    @Query(value = "UPDATE cases SET views_count = views_count + 1 WHERE id = :id", nativeQuery = true)
    @Modifying
    @Transactional
    void incrementViewsCount(@Param("id") Long id);

    @Query(value = "SELECT t.id, t.name, COUNT(ct.case_id) FROM tags t " +
            "LEFT JOIN case_tags ct ON t.id = ct.tag_id " +
            "WHERE t.is_active = true " +
            "GROUP BY t.id, t.name ORDER BY COUNT(ct.case_id) DESC, t.name ASC", nativeQuery = true)
    List<Object[]> getActiveTagsWithCaseCount();

    @Query(value = "SELECT ct.case_id, t.id, t.name, " +
            "(SELECT COUNT(*) FROM case_tags ct2 WHERE ct2.tag_id = t.id) " +
            "FROM case_tags ct " +
            "JOIN tags t ON t.id = ct.tag_id " +
            "WHERE ct.case_id IN (:caseIds) " +
            "ORDER BY t.id ASC", nativeQuery = true)
    List<Object[]> findTagsByCaseIds(@Param("caseIds") List<Long> caseIds);




}