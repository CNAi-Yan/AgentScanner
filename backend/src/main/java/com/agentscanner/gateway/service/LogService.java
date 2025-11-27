package com.agentscanner.gateway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.agentscanner.gateway.entity.Log;
import com.agentscanner.gateway.dto.LogDTO;
import com.agentscanner.gateway.vo.LogVO;

import java.util.List;
import java.util.Map;

public interface LogService extends IService<Log> {
    
    // 创建日志
    LogVO createLog(LogDTO logDTO);
    
    // 批量创建日志
    boolean batchCreateLog(List<LogDTO> logDTOList);
    
    // 获取日志详情
    LogVO getLogById(Long id);
    
    // 获取日志列表
    List<LogVO> getLogList(Map<String, Object> params);
    
    // 统计日志数量
    Long countLogs(Map<String, Object> params);
    
    // 删除日志
    boolean deleteLog(Long id);
    
    // 批量删除日志
    boolean batchDeleteLog(List<Long> ids);
    
    // 按条件删除日志
    boolean deleteLogByCondition(Map<String, Object> params);
    
    // 导出日志
    List<LogVO> exportLogs(Map<String, Object> params);
    
    // 获取日志统计信息
    Map<String, Object> getLogStatistics(Map<String, Object> params);
}