package com.nextinnomind.agro_speak_backend.repository;

import com.nextinnomind.agro_speak_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
