package com.project.main.repository.cases;

import com.project.main.model.cases.CaseEntity;
import com.project.main.model.cases.CaseTag;
import com.project.main.model.cases.CaseTagId;
import com.project.main.model.cases.Tag;
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

}