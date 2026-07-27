package com.project.main.repository;

import com.project.main.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseTagRepository extends JpaRepository<CaseTag, CaseTagId> {


    @Query("SELECT ct FROM CaseTag ct WHERE ct.caseEntity.id = :caseId AND ct.tag.id = :tagId")
    Optional<CaseTag> findByCaseIdAndTagId(@Param("caseId") Long caseId, @Param("tagId") Long tagId);

    boolean existsByCaseEntityIdAndTagId(Long caseId, Long tagId);



    void deleteByCaseEntityId(Long caseId);



    void deleteByTagId(Long tagId);



    @Query("SELECT ct.tag FROM CaseTag ct WHERE ct.caseEntity.id = :caseId")
    List<Tag> findTagsByCaseId(@Param("caseId") Long caseId);


    @Query("SELECT ct.caseEntity FROM CaseTag ct WHERE ct.tag.id = :tagId")
    List<CaseEntity> findCasesByTagId(@Param("tagId") Long tagId);
}