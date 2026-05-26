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

    List<EnvVariable> findAll();

    boolean existsByNameAndGroupId(String name, Long groupId);

    List<EnvVariable> findByGroupId(Long groupId);

    List<EnvVariable> findByGroupIdIn(List<Long> groupIds);

    void deleteByGroupId(Long groupId);

long countByGroupId(Long groupId);
}