package com.project.main.repository.learning;


import com.project.main.model.learning.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query(value = "SELECT * FROM quiz WHERE material_id = :materialId AND is_active = true ORDER BY id ASC", nativeQuery = true)
    List<Quiz> findActiveByMaterialId(@Param("materialId") Long materialId);

    @Query(value = "SELECT * FROM quiz WHERE material_id = :materialId ORDER BY id ASC", nativeQuery = true)
    List<Quiz> findAllByMaterialId(@Param("materialId") Long materialId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM quiz WHERE material_id = :materialId", nativeQuery = true)
    void deleteByMaterialId(@Param("materialId") Long materialId);
}