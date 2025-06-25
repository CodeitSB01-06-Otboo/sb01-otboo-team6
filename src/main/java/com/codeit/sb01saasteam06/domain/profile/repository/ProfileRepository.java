package com.codeit.sb01saasteam06.domain.profile.repository;

import com.codeit.sb01saasteam06.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findById(UUID id);
}
