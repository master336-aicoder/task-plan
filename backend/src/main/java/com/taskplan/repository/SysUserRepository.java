package com.taskplan.repository;

import com.taskplan.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    
    Optional<SysUser> findByUsernameAndIsDeleted(String username, Integer isDeleted);
    
    @Query("SELECT u FROM SysUser u WHERE u.username = :username AND u.isDeleted = 0 AND u.status = 1")
    Optional<SysUser> findActiveUserByUsername(String username);
    
    List<SysUser> findByIsDeleted(Integer isDeleted);
    
    boolean existsByUsername(String username);
}
