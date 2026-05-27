package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.DatasourceProbeLog;
import com.taskplan.entity.ServerProbeLog;
import com.taskplan.service.ProbeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/probe")
public class ProbeController {

    @Autowired
    private ProbeService probeService;

    @GetMapping("/server/status")
    public ResponseEntity<Map<String, Object>> getServerProbeStatus() {
        List<ServerProbeLog> probes = probeService.getLatestServerProbes();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "success");
        response.put("data", probes);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/datasource/status")
    public ResponseEntity<Map<String, Object>> getDatasourceProbeStatus() {
        List<DatasourceProbeLog> probes = probeService.getLatestDatasourceProbes();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "success");
        response.put("data", probes);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/server/trigger")
    public ResponseEntity<Map<String, Object>> triggerServerProbe() {
        probeService.probeAllServers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "服务器探活已触发");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/datasource/trigger")
    public ResponseEntity<Map<String, Object>> triggerDatasourceProbe() {
        probeService.probeAllDatasources();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "数据源探活已触发");
        
        return ResponseEntity.ok(response);
    }
}
