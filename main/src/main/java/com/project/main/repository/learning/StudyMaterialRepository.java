package com.project.main.repository.learning;

import com.project.main.model.learning.StudyMaterial;
import com.project.main.repository.projection.AdminMaterialFullRow;
import com.project.main.repository.projection.AdminMaterialSummaryRow;
import com.project.main.repository.projection.MaterialSummaryRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {


    @Query(
            value = """
                SELECT
                    sm.id AS id,
                    sm.title AS title,
                    sm.position AS position
                FROM study_material sm
                WHERE sm.case_id = :caseId
                  AND sm.is_active = true
                ORDER BY sm.position ASC, sm.id ASC
                """,
            nativeQuery = true
    )
    List<MaterialSummaryRow> findActiveByCaseIdSorted(@Param("caseId") Long caseId);



    @Query(
            value = """
                SELECT
                    sm.id AS id,
                    sm.title AS title,
                    sm.position AS position,
                    sm.is_active AS active
                FROM study_material sm
                WHERE sm.case_id = :caseId
                ORDER BY sm.position ASC, sm.id ASC
                """,
            nativeQuery = true
    )
    List<AdminMaterialSummaryRow> findAllByCaseIdOrdered(@Param("caseId") Long caseId);


    @Query(
            value = """
                SELECT sm.*
                FROM study_material sm
                JOIN cases c ON c.id = sm.case_id
                WHERE sm.id = :id
                  AND sm.is_active = true
                  AND c.is_active = true
                LIMIT 1
                """,
            nativeQuery = true
    )
    Optional<StudyMaterial> findActiveByIdAndActiveCase(@Param("id") Long id);


    @Query(
            value = """
                SELECT
                    sm.id AS id,
                    sm.case_id AS case_id,
                    sm.title AS title,
                    sm.position AS position,
                    sm.text AS text,
                    sm.is_active AS active
                FROM study_material sm
                WHERE sm.id = :id
                LIMIT 1
                """,
            nativeQuery = true
    )
    List<AdminMaterialFullRow> findAdminMaterialById(@Param("id") Long id);

    @Query(
            value = """
                SELECT EXISTS(
                    SELECT 1
                    FROM study_material
                    WHERE case_id = :caseId
                      AND position = :position
                )
                """,
            nativeQuery = true
    )
    boolean existsByCaseIdAndPosition(
            @Param("caseId") Long caseId,
            @Param("position") Integer position
    );

    @Query(
            value = """
                SELECT EXISTS(
                    SELECT 1
                    FROM study_material
                    WHERE case_id = :caseId
                      AND position = :position
                      AND id <> :materialId
                )
                """,
            nativeQuery = true
    )
    boolean existsByCaseIdAndPositionAndIdNot(
            @Param("caseId") Long caseId,
            @Param("position") Integer position,
            @Param("materialId") Long materialId
    );


}