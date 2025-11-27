package com.agentscanner.gateway.controller;

import com.agentscanner.gateway.service.LogService;
import com.agentscanner.gateway.dto.LogDTO;
import com.agentscanner.gateway.vo.LogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log")
@Tag(name = "日志管理", description = "日志管理相关接口")
public class LogController {

    @Autowired
    private LogService logService;

    @Operation(summary = "创建日志", description = "创建新的日志记录")
    @PostMapping
    public ResponseEntity<LogVO> createLog(@RequestBody LogDTO logDTO) {
        LogVO logVO = logService.createLog(logDTO);
        return ResponseEntity.ok(logVO);
    }

    @Operation(summary = "批量创建日志", description = "批量创建日志记录")
    @PostMapping("/batch")
    public ResponseEntity<Boolean> batchCreateLog(@RequestBody List<LogDTO> logDTOList) {
        boolean result = logService.batchCreateLog(logDTOList);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取日志详情", description = "获取指定ID的日志详情")
    @GetMapping("/{id}")
    public ResponseEntity<LogVO> getLogById(@PathVariable Long id) {
        LogVO logVO = logService.getLogById(id);
        return ResponseEntity.ok(logVO);
    }

    @Operation(summary = "获取日志列表", description = "获取日志列表，支持分页和筛选")
    @GetMapping
    public ResponseEntity<List<LogVO>> getLogList(@RequestParam Map<String, Object> params) {
        List<LogVO> logList = logService.getLogList(params);
        return ResponseEntity.ok(logList);
    }

    @Operation(summary = "统计日志数量", description = "统计日志数量，支持条件筛选")
    @GetMapping("/count")
    public ResponseEntity<Long> countLogs(@RequestParam Map<String, Object> params) {
        Long count = logService.countLogs(params);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "删除日志", description = "删除指定ID的日志")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteLog(@PathVariable Long id) {
        boolean result = logService.deleteLog(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "批量删除日志", description = "批量删除指定ID列表的日志")
    @DeleteMapping("/batch")
    public ResponseEntity<Boolean> batchDeleteLog(@RequestBody List<Long> ids) {
        boolean result = logService.batchDeleteLog(ids);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "按条件删除日志", description = "根据条件删除日志")
    @DeleteMapping
    public ResponseEntity<Boolean> deleteLogByCondition(@RequestParam Map<String, Object> params) {
        boolean result = logService.deleteLogByCondition(params);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "导出日志", description = "导出日志，支持条件筛选")
    @GetMapping("/export")
    public ResponseEntity<List<LogVO>> exportLogs(@RequestParam Map<String, Object> params) {
        List<LogVO> logList = logService.exportLogs(params);
        return ResponseEntity.ok(logList);
    }

    @Operation(summary = "获取日志统计信息", description = "获取日志统计信息，支持条件筛选")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLogStatistics(@RequestParam Map<String, Object> params) {
        Map<String, Object> statistics = logService.getLogStatistics(params);
        return ResponseEntity.ok(statistics);
    }
}