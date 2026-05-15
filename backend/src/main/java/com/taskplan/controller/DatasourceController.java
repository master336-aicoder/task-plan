package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.Datasource;
import com.taskplan.service.DatasourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasources")
public class DatasourceController {

    @Autowired
    private DatasourceService datasourceService;

    @GetMapping
    public Result<Map<String, Object>> listDatasources(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Datasource> datasourcePage = datasourceService.listDatasources(name, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", datasourcePage.getContent());
        result.put("total", datasourcePage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<Datasource>> listAll() {
        return Result.success(datasourceService.listAll());
    }

    @GetMapping("/type/{type}")
    public Result<List<Datasource>> listByType(@PathVariable String type) {
        return Result.success(datasourceService.listByType(type));
    }

    @GetMapping("/{id}")
    public Result<Datasource> getDatasource(@PathVariable Long id) {
        return Result.success(datasourceService.getDatasource(id));
    }

    @PostMapping
    public Result<Datasource> createDatasource(@RequestBody Datasource datasource) {
        return Result.success(datasourceService.createDatasource(datasource));
    }

    @PutMapping("/{id}")
    public Result<Datasource> updateDatasource(@PathVariable Long id, @RequestBody Datasource datasource) {
        return Result.success(datasourceService.updateDatasource(id, datasource));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDatasource(@PathVariable Long id) {
        datasourceService.deleteDatasource(id);
        return Result.success();
    }

    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        Datasource datasource = datasourceService.getDatasource(id);
        boolean success = datasourceService.testConnection(datasource);
        return Result.success(success);
    }

    @PostMapping("/test-connection")
    public Result<Map<String, Object>> testConnectionDirect(@RequestBody Datasource datasource) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = datasourceService.testConnection(datasource);
            result.put("success", success);
            result.put("message", success ? "连接成功" : "连接失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return Result.success(result);
    }
}