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
public interface CaseTagRepository extends JpaRepository<CaseTag, CaseTagId> {


    @Query(value = "SELECT COUNT(*) > 0 FROM case_tags WHERE case_id = :caseId AND tag_id = :tagId", nativeQuery = true)
    boolean existsByCaseEntityIdAndTagId(@Param("caseId") Long caseId, @Param("tagId") Long tagId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM case_tags WHERE case_id = :caseId AND tag_id = :tagId", nativeQuery = true)
    void deleteByCaseEntityIdAndTagId(@Param("caseId") Long caseId, @Param("tagId") Long tagId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM case_tags WHERE case_id = :caseId", nativeQuery = true)
    void deleteByCaseEntityId(@Param("caseId") Long caseId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM case_tags WHERE tag_id = :tagId", nativeQuery = true)
    void deleteByTagId(@Param("tagId") Long tagId);


    @Query(value = "SELECT t.* FROM tags t " +
            "JOIN case_tags ct ON t.id = ct.tag_id " +
            "WHERE ct.case_id = :caseId", nativeQuery = true)
    List<Tag> findTagsByCaseId(@Param("caseId") Long caseId);


    @Query(value = "SELECT c.* FROM cases c " +
            "JOIN case_tags ct ON c.id = ct.case_id " +
            "WHERE ct.tag_id = :tagId", nativeQuery = true)
    List<CaseEntity> findCasesByTagId(@Param("tagId") Long tagId);

    @Query(value = "SELECT ct.* FROM case_tags ct " +
            "WHERE ct.case_id = :caseId AND ct.tag_id = :tagId", nativeQuery = true)
    Optional<CaseTag> findByCaseIdAndTagId(@Param("caseId") Long caseId, @Param("tagId") Long tagId);
}