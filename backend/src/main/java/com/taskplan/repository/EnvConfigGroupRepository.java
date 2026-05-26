package com.taskplan.repository;

import com.taskplan.entity.EnvConfigGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvConfigGroupRepository extends JpaRepository<EnvConfigGroup, Long> {
    
    Optional<EnvConfigGroup> findByName(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
}