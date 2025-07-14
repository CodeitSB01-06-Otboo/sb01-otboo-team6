package com.codeit.sb01otbooteam06.domain.profile.repository;

import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;

import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

  Optional<Profile> findById(UUID id);

  /**
   * Profile 테이블에서 (lat, lon, x, y) 조합을 DISTINCT 로 추출해
   * Location 객체로 바로 매핑한다.
   */
  @Query("""
        select distinct new com.codeit.sb01otbooteam06.domain.weather.entity.Location(
                              p.latitude, p.longitude, p.x, p.y)
                          from Profile p
    """)
  List<Location> findDistinctLocations();
}
