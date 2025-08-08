package com.taskhttpv1.demo.TaskTrackerPro.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<UserDb, Long> {
    Optional<UserDb> findByEmail(String email);
}
