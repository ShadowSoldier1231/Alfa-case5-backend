package com.project.main.repository;


import com.project.main.model.UserSetup;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;



public interface UserRepository extends JpaRepository<UserSetup, Long> {

    boolean existsByEmail(String email);
    Optional<UserSetup> findByUsername(String username);
    boolean existsByUsername(String username);

}