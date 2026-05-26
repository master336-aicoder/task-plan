package com.taskplan.service;

import com.taskplan.entity.ServerNode;
import com.taskplan.exception.BusinessException;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SshService {

    public Map<String, Object> executeCommand(ServerNode server, String command) {
        Map<String, Object> result = new HashMap<>();
        Session session = null;
        ChannelExec channel = null;
        
        try {
            JSch jsch = new JSch();
            
            session = jsch.getSession(server.getUsername(), server.getHostname(), server.getPort());
            session.setPassword(server.getPassword());
            
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            session.connect(5000);
            log.info("SSH连接成功: {}:{}", server.getHostname(), server.getPort());
            
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            
            channel.setOutputStream(outputStream);
            channel.setErrStream(errorStream);
            
            channel.connect(30000);
            
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }
            
            int exitCode = channel.getExitStatus();
            String output = outputStream.toString("UTF-8");
            String error = errorStream.toString("UTF-8");
            
            result.put("success", exitCode == 0);
            result.put("exitCode", exitCode);
            result.put("output", output);
            result.put("error", error);
            
            log.info("命令执行完成: exitCode={}, output={}", exitCode, output);
            
        } catch (JSchException e) {
            log.error("SSH连接失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "SSH连接失败: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("命令执行中断: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "命令执行中断: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.error("编码转换失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "编码转换失败: " + e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        
        return result;
    }

    public Map<String, Object> executeScript(ServerNode server, String scriptContent, String scriptType) {
        String tempFileName = "/tmp/taskplan_script_" + System.currentTimeMillis();
        
        if ("PYTHON".equalsIgnoreCase(scriptType)) {
            tempFileName += ".py";
        } else {
            tempFileName += ".sh";
        }
        
        String uploadCommand = "cat > " + tempFileName + " << 'EOF'\n" + scriptContent + "\nEOF\nchmod +x " + tempFileName;
        
        Map<String, Object> uploadResult = executeCommand(server, uploadCommand);
        if (!(Boolean) uploadResult.get("success")) {
            return uploadResult;
        }
        
        String executeCommand;
        if ("PYTHON".equalsIgnoreCase(scriptType)) {
            executeCommand = "python " + tempFileName;
        } else {
            executeCommand = "bash " + tempFileName;
        }
        
        Map<String, Object> executeResult = executeCommand(server, executeCommand);
        
        executeCommand(server, "rm -f " + tempFileName);
        
        return executeResult;
    }

    public boolean testSshConnection(ServerNode server) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(server.getUsername(), server.getHostname(), server.getPort());
            session.setPassword(server.getPassword());
            
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            session.connect(5000);
            session.disconnect();
            return true;
        } catch (JSchException e) {
            log.error("SSH连接测试失败: {}", e.getMessage());
            return false;
        }
    }
}