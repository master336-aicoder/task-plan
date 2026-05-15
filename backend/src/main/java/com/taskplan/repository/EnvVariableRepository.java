package com.taskplan.repository;

import com.taskplan.entity.EnvVariable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvVariableRepository extends JpaRepository<EnvVariable, Long> {

    Page<EnvVariable> findByNameContaining(String name, Pageable pageable);

    List<EnvVariable> findByCategory(String category);

    List<EnvVariable> findAll();

    boolean existsByName(String name);
}
