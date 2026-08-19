package com.project.main.repository;


import com.project.main.model.UserSetup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserSetup, Long> {

    boolean existsByEmail(String email);
    Optional<UserSetup> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByTelegramId(Long telegramId);


    @Query(value = "SELECT u.id, u.username, u.email, d.nick_name, u.role, d.status, u.is_verified, u.banned_until " +
            "FROM user_setup u " +
            "LEFT JOIN user_data d ON u.id = d.id " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) ESCAPE '!' OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) ESCAPE '!' OR " +
            "LOWER(d.nick_name) LIKE LOWER(CONCAT('%', :search, '%')) ESCAPE '!')",
            countQuery = "SELECT COUNT(u.id) FROM user_setup u " +
                    "LEFT JOIN user_data d ON u.id = d.id " +
                    "WHERE (:search IS NULL OR :search = '' OR " +
                    "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) ESCAPE '!' OR " +
                    "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) ESCAPE '!' OR " +
                    "LOWER(d.nick_name) LIKE LOWER(CONCAT('%', :search, '%')) ESCAPE '!')",
            nativeQuery = true)
    Page<Object[]> findUsersForAdmin(@Param("search") String search, Pageable pageable);

}