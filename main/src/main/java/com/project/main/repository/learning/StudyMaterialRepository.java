package com.project.main.repository.learning;

import com.project.main.model.learning.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StudyMaterialRepository  extends JpaRepository<StudyMaterial, Long> {

    @Query(value = "SELECT * FROM study_material WHERE case_id = :caseId AND is_active = true ORDER BY id ASC", nativeQuery = true)
    List<StudyMaterial> findActiveByCaseId(@Param("caseId") Long caseId);

    @Query(value = "SELECT * FROM study_material WHERE case_id = :caseId ORDER BY id ASC", nativeQuery = true)
    List<StudyMaterial> findAllByCaseId(@Param("caseId") Long caseId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE study_material SET is_active = false WHERE case_id = :caseId", nativeQuery = true)
    void deactivateByCaseId(@Param("caseId") Long caseId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM study_material WHERE case_id = :caseId", nativeQuery = true)
    void deleteByCaseId(@Param("caseId") Long caseId);
}
