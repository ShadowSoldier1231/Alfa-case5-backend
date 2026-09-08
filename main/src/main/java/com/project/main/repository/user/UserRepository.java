package com.project.main.repository.user;


import com.project.main.model.user.UserSetup;
import com.project.main.repository.projection.AdminUserRow;
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

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user_setup WHERE id = :userId)", nativeQuery = true)
    boolean existsUserById(@Param("userId") Long userId);

    @Query(
            value = """
                SELECT
                    u.id AS id,
                    u.username AS username,
                    u.email AS email,
                    d.nick_name AS nick_name,
                    u.role AS role,
                    d.status AS status,
                    u.is_verified AS is_verified,
                    u.banned_until AS banned_until
                FROM user_setup u
                LEFT JOIN user_data d ON u.id = d.id
                    WHERE (
                        CAST(:search AS text) IS NULL
                        OR CAST(:search AS text) = ''
                        OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                        OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                        OR LOWER(d.nick_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                    )
                """,
            countQuery = """
                SELECT COUNT(u.id)
                FROM user_setup u
                LEFT JOIN user_data d ON u.id = d.id
                    WHERE (
                        CAST(:search AS text) IS NULL
                        OR CAST(:search AS text) = ''
                        OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                        OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                        OR LOWER(d.nick_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) ESCAPE '!'
                    )
                """,
            nativeQuery = true
    )
    Page<AdminUserRow> findUsersForAdmin(
            @Param("search") String search,
            Pageable pageable
    );


    @Query(value = "SELECT * FROM user_setup WHERE email = :email", nativeQuery = true)
    Optional<UserSetup> findByEmail(@Param("email") String email);
}