package com.project.main.repository;

import com.project.main.model.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {

    @Transactional
    void deleteByUserId(Long userId);
}
