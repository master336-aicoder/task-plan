package com.taskplan.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProcessDefinitionVersionDiff {
    
    private Long processDefinitionId;
    private Integer sourceVersion;
    private Integer targetVersion;
    private Integer currentVersion;
    
    private String nameChanged;
    private String descriptionChanged;
    private String processTypeChanged;
    
    private NodeDiff nodesDiff;
    private ConnectionDiff connectionsDiff;
    
    private List<Map<String, Object>> currentNodes;
    private List<Map<String, Object>> targetNodes;
    private List<Map<String, Object>> currentConnections;
    private List<Map<String, Object>> targetConnections;
    
    @Data
    public static class NodeDiff {
        private List<NodeChange> added;
        private List<NodeChange> removed;
        private List<NodeChange> modified;
    }
    
    @Data
    public static class NodeChange {
        private String id;
        private String name;
        private String taskType;
        private Map<String, Object> properties;
        private boolean isAdded;
        private boolean isRemoved;
        private boolean isModified;
    }
    
    @Data
    public static class ConnectionDiff {
        private List<ConnectionChange> added;
        private List<ConnectionChange> removed;
    }
    
    @Data
    public static class ConnectionChange {
        private String id;
        private String sourceNodeId;
        private String targetNodeId;
        private String sourceHandle;
        private String targetHandle;
        private boolean isAdded;
        private boolean isRemoved;
    }
}