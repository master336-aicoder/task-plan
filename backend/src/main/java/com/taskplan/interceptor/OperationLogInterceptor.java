package com.taskplan.interceptor;

import com.taskplan.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            long requestTime = System.currentTimeMillis() - startTime.get();
            startTime.remove();

            String requestUrl = request.getRequestURI();
            if (requestUrl.startsWith("/api/") && !requestUrl.contains("/login-logs") && !requestUrl.contains("/operation-logs")) {
                Long userId = getUserIdFromRequest(request);
                String username = getUsernameFromRequest(request);

                String requestMethod = request.getMethod();
                String clientIp = getClientIp(request);
                int clientPort = request.getRemotePort();
                String httpHeader = getHeaders(request);
                String userAgent = request.getHeader("User-Agent");
                String browserInfo = parseBrowserInfo(userAgent);
                String requestParams = getRequestParams(request);
                int statusCode = response.getStatus();

                operationLogService.saveLogAsync(userId, username, requestMethod, requestUrl,
                        requestParams, null, clientIp, clientPort, httpHeader,
                        browserInfo, userAgent, statusCode, requestTime);
            }
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage());
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        if (username instanceof String) {
            return (String) username;
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            if (!name.toLowerCase().contains("authorization") && !name.toLowerCase().contains("cookie")) {
                headers.put(name, value);
            }
        }
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            return headers.toString();
        }
    }

    private String getRequestParams(HttpServletRequest request) {
        String method = request.getMethod();
        
        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            String queryString = request.getQueryString();
            return queryString != null ? queryString : "";
        }
        
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String body = sb.toString();
                
                if (body.length() > 2000) {
                    return body.substring(0, 2000);
                }
                return body;
            } catch (Exception e) {
                return "";
            }
        }
        
        return "";
    }

    private String parseBrowserInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari";
        }
        if (userAgent.contains("Edge")) {
            return "Edge";
        }
        if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            return "Opera";
        }
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        }
        
        return "Unknown";
    }
}
