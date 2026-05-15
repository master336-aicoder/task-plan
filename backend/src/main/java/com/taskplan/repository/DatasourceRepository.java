package com.taskplan.repository;

import com.taskplan.entity.Datasource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasourceRepository extends JpaRepository<Datasource, Long> {
    Page<Datasource> findByNameContaining(String name, Pageable pageable);
    List<Datasource> findByType(String type);
    boolean existsByName(String name);
}