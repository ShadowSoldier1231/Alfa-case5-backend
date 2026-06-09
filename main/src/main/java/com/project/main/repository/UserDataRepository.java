package com.project.main.repository;

import com.project.main.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserDataRepository extends JpaRepository<UserData, Long>{


}