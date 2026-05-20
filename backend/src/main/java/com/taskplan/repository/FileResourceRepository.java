package com.taskplan.repository;

import com.taskplan.entity.FileResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
    Page<FileResource> findByNameContaining(String name, Pageable pageable);
    Page<FileResource> findByProjectId(Long projectId, Pageable pageable);
    List<FileResource> findByType(String type);
    boolean existsByName(String name);
    
    List<FileResource> findByParentId(Long parentId);
    
    List<FileResource> findByParentIdOrderByNameAsc(Long parentId);
    
    @Query("SELECT f FROM FileResource f WHERE f.isDirectory = 1 AND (f.parentId = :parentId OR (:parentId IS NULL AND f.parentId IS NULL)) ORDER BY f.name ASC")
    List<FileResource> findDirectoriesByParentId(Long parentId);
    
    @Query("SELECT f FROM FileResource f WHERE f.isDirectory = 0 AND (f.parentId = :parentId OR (:parentId IS NULL AND f.parentId IS NULL)) ORDER BY f.name ASC")
    List<FileResource> findFilesByParentId(Long parentId);
    
    @Query("SELECT f FROM FileResource f WHERE f.name = :name AND (f.parentId = :parentId OR (:parentId IS NULL AND f.parentId IS NULL))")
    FileResource findByNameAndParentId(String name, Long parentId);
}
