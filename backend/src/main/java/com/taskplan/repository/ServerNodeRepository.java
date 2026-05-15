package com.taskplan.repository;

import com.taskplan.entity.ServerNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerNodeRepository extends JpaRepository<ServerNode, Long> {
    Page<ServerNode> findByNameContaining(String name, Pageable pageable);
    List<ServerNode> findByGroupName(String groupName);
    List<ServerNode> findByStatus(String status);
    boolean existsByName(String name);
    boolean existsByHostname(String hostname);
}