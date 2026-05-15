package com.taskplan.repository;

import com.taskplan.entity.FileResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
    Page<FileResource> findByNameContaining(String name, Pageable pageable);
    Page<FileResource> findByProjectId(Long projectId, Pageable pageable);
    List<FileResource> findByType(String type);
    boolean existsByName(String name);
}