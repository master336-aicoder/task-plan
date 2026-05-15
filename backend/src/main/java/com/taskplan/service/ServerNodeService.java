package com.taskplan.service;

import com.taskplan.entity.ServerNode;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.ServerNodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ServerNodeService {

    @Autowired
    private ServerNodeRepository serverNodeRepository;

    @Transactional
    public ServerNode createServer(ServerNode server) {
        validateServer(server);
        if (serverNodeRepository.existsByName(server.getName())) {
            throw new BusinessException("服务器名称已存在");
        }
        if (serverNodeRepository.existsByHostname(server.getHostname())) {
            throw new BusinessException("主机地址已存在");
        }
        return serverNodeRepository.save(server);
    }

    @Transactional
    public ServerNode updateServer(Long id, ServerNode server) {
        ServerNode existing = getServer(id);
        
        if (!existing.getName().equals(server.getName()) && 
            serverNodeRepository.existsByName(server.getName())) {
            throw new BusinessException("服务器名称已存在");
        }
        if (!existing.getHostname().equals(server.getHostname()) && 
            serverNodeRepository.existsByHostname(server.getHostname())) {
            throw new BusinessException("主机地址已存在");
        }
        
        existing.setName(server.getName());
        existing.setHostname(server.getHostname());
        existing.setPort(server.getPort());
        existing.setUsername(server.getUsername());
        existing.setPassword(server.getPassword());
        existing.setGroupName(server.getGroupName());
        
        return serverNodeRepository.save(existing);
    }

    @Transactional
    public void deleteServer(Long id) {
        if (!serverNodeRepository.existsById(id)) {
            throw new BusinessException("服务器不存在");
        }
        serverNodeRepository.deleteById(id);
    }

    public ServerNode getServer(Long id) {
        return serverNodeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("服务器不存在"));
    }

    public Page<ServerNode> listServers(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) {
            return serverNodeRepository.findByNameContaining(name, pageable);
        }
        return serverNodeRepository.findAll(pageable);
    }

    public List<ServerNode> listByGroup(String groupName) {
        return serverNodeRepository.findByGroupName(groupName);
    }

    public List<ServerNode> listAll() {
        return serverNodeRepository.findAll();
    }

    public List<ServerNode> listOnline() {
        return serverNodeRepository.findByStatus("ONLINE");
    }

    public boolean pingServer(Long id) {
        ServerNode server = getServer(id);
        boolean success = testConnection(server);
        server.setStatus(success ? "ONLINE" : "OFFLINE");
        serverNodeRepository.save(server);
        return success;
    }

    public boolean testConnection(ServerNode server) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(server.getHostname(), server.getPort()), 5000);
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to server: {}", e.getMessage());
            return false;
        }
    }

    private void validateServer(ServerNode server) {
        if (server.getName() == null || server.getName().trim().isEmpty()) {
            throw new BusinessException("服务器名称不能为空");
        }
        if (server.getHostname() == null || server.getHostname().trim().isEmpty()) {
            throw new BusinessException("主机地址不能为空");
        }
        if (server.getPort() == null) {
            server.setPort(22);
        }
        if (server.getUsername() == null || server.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
    }
}