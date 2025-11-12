package com.as.authservice.repositories;

import com.as.authservice.models.Rider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiderRepository extends JpaRepository<Rider, Long> {
    Optional<Object> findByEmail(String email);
}
