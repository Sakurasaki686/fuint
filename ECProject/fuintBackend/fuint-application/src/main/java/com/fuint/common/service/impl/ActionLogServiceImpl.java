package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.service.ActionLogService;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.mapper.TActionLogMapper;
import com.fuint.repository.model.TActionLog;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 日志服务实现类
 */
@Service
@AllArgsConstructor
public class ActionLogServiceImpl extends ServiceImpl<TActionLogMapper, TActionLog> implements ActionLogService {

    private TActionLogMapper tActionLogMapper;

    public void saveActionLog(TActionLog actionLog) {
        tActionLogMapper.insert(actionLog);
    }

    public PaginationResponse<TActionLog> findLogsByPagination(PaginationRequest paginationRequest) {
        Page<TActionLog> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<TActionLog> lambdaQueryWrapper = Wrappers.lambdaQuery();
        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(TActionLog::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(TActionLog::getStoreId, storeId);
        }
        String module = paginationRequest.getSearchParams().get("module") == null ? "" : paginationRequest.getSearchParams().get("module").toString();
        if (StringUtils.isNotBlank(module)) {
            lambdaQueryWrapper.like(TActionLog::getModule, module);
        }
        String name = paginationRequest.getSearchParams().get("name") == null ? "" : paginationRequest.getSearchParams().get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.eq(TActionLog::getAcctName, name);
        }
        String startTime = paginationRequest.getSearchParams().get("startTime") == null ? "" : paginationRequest.getSearchParams().get("startTime").toString();
        if (StringUtils.isNotBlank(startTime)) {
            lambdaQueryWrapper.gt(TActionLog::getActionTime, startTime);
        }
        String endTime = paginationRequest.getSearchParams().get("endTime") == null ? "" : paginationRequest.getSearchParams().get("endTime").toString();
        if (StringUtils.isNotBlank(endTime)) {
            lambdaQueryWrapper.lt(TActionLog::getActionTime, endTime);
        }
        String ip = paginationRequest.getSearchParams().get("ip") == null ? "" : paginationRequest.getSearchParams().get("ip").toString();
        if (StringUtils.isNotBlank(ip)) {
            lambdaQueryWrapper.eq(TActionLog::getClientIp, ip);
        }

        lambdaQueryWrapper.orderByDesc(TActionLog::getId);
        List<TActionLog> dataList = tActionLogMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<TActionLog> paginationResponse = new PaginationResponse(pageImpl, TActionLog.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }
}
