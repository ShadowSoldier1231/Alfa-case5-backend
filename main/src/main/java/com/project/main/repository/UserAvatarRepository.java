package com.project.main.repository;

import com.project.main.model.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {
}
