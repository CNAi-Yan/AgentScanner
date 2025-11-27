package com.agentscanner.gateway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.agentscanner.gateway.entity.Log;
import com.agentscanner.gateway.mapper.LogMapper;
import com.agentscanner.gateway.service.LogService;
import com.agentscanner.gateway.dto.LogDTO;
import com.agentscanner.gateway.vo.LogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {
    
    @Override
    public LogVO createLog(LogDTO logDTO) {
        Log log = new Log();
        BeanUtils.copyProperties(logDTO, log);
        
        // 设置默认值
        log.setLogTime(new Date());
        
        save(log);
        
        return convertToVO(log);
    }
    
    @Override
    public boolean batchCreateLog(List<LogDTO> logDTOList) {
        List<Log> logList = logDTOList.stream().map(logDTO -> {
            Log log = new Log();
            BeanUtils.copyProperties(logDTO, log);
            log.setLogTime(new Date());
            return log;
        }).collect(Collectors.toList());
        
        return saveBatch(logList);
    }
    
    @Override
    public LogVO getLogById(Long id) {
        Log log = getById(id);
        return log != null ? convertToVO(log) : null;
    }
    
    @Override
    public List<LogVO> getLogList(Map<String, Object> params) {
        List<Log> logList = list();
        return logList.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public Long countLogs(Map<String, Object> params) {
        return count();
    }
    
    @Override
    public boolean deleteLog(Long id) {
        return removeById(id);
    }
    
    @Override
    public boolean batchDeleteLog(List<Long> ids) {
        return removeByIds(ids);
    }
    
    @Override
    public boolean deleteLogByCondition(Map<String, Object> params) {
        // 这里简化处理，实际应该根据params构建查询条件
        return true;
    }
    
    @Override
    public List<LogVO> exportLogs(Map<String, Object> params) {
        // 这里简化处理，实际应该根据params构建查询条件
        return getLogList(params);
    }
    
    @Override
    public Map<String, Object> getLogStatistics(Map<String, Object> params) {
        // 这里简化处理，实际应该根据params构建统计条件
        Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalLogs", count());
        statistics.put("accessLogs", count(lambdaQuery().eq(Log::getType, 1)));
        statistics.put("auditLogs", count(lambdaQuery().eq(Log::getType, 2)));
        statistics.put("errorLogs", count(lambdaQuery().gt(Log::getResponseStatus, 499)));
        return statistics;
    }
    
    // 转换为VO对象
    private LogVO convertToVO(Log log) {
        LogVO vo = new LogVO();
        BeanUtils.copyProperties(log, vo);
        
        // 设置日志类型描述
        vo.setTypeDesc(log.getType() == 1 ? "访问日志" : "审计日志");
        
        return vo;
    }
}