package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.SmsTemplateDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.SmsTemplateService;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.mapper.MtSmsTemplateMapper;
import com.fuint.repository.model.MtSmsTemplate;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信模板业务实现类
 *
 *
 *
 */
@Service
@AllArgsConstructor
public class SmsTemplateServiceImpl extends ServiceImpl<MtSmsTemplateMapper, MtSmsTemplate> implements SmsTemplateService {

    private MtSmsTemplateMapper mtSmsTemplateMapper;

    /**
     * 分页查询模板列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<MtSmsTemplate> querySmsTemplateListByPagination(PaginationRequest paginationRequest) {
        Page<MtSmsTemplate> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtSmsTemplate> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtSmsTemplate::getStatus, StatusEnum.DISABLE.getKey());

        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.like(MtSmsTemplate::getMerchantId, merchantId);
        }
        String name = paginationRequest.getSearchParams().get("name") == null ? "" : paginationRequest.getSearchParams().get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtSmsTemplate::getName, name);
        }
        String uname = paginationRequest.getSearchParams().get("uname") == null ? "" : paginationRequest.getSearchParams().get("uname").toString();
        if (StringUtils.isNotBlank(uname)) {
            lambdaQueryWrapper.eq(MtSmsTemplate::getUname, uname);
        }
        String code = paginationRequest.getSearchParams().get("code") == null ? "" : paginationRequest.getSearchParams().get("code").toString();
        if (StringUtils.isNotBlank(code)) {
            lambdaQueryWrapper.eq(MtSmsTemplate::getCode, code);
        }
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtSmsTemplate::getStatus, status);
        }

        lambdaQueryWrapper.orderByDesc(MtSmsTemplate::getId);
        List<MtSmsTemplate> dataList = mtSmsTemplateMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtSmsTemplate> paginationResponse = new PaginationResponse(pageImpl, MtSmsTemplate.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 保存模板信息
     *
     * @param mtSmsTemplateDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "保存短信模板")
    public MtSmsTemplate saveSmsTemplate(SmsTemplateDto mtSmsTemplateDto) throws BusinessCheckException {
        MtSmsTemplate mtSmsTemplate = new MtSmsTemplate();
        mtSmsTemplate.setMerchantId(mtSmsTemplateDto.getMerchantId());
        mtSmsTemplate.setCode(mtSmsTemplateDto.getCode());
        mtSmsTemplate.setName(mtSmsTemplateDto.getName());
        mtSmsTemplate.setUname(mtSmsTemplateDto.getUname());
        mtSmsTemplate.setContent(mtSmsTemplateDto.getContent());
        mtSmsTemplate.setStatus(mtSmsTemplateDto.getStatus());
        mtSmsTemplate.setOperator(mtSmsTemplate.getOperator());

        if (mtSmsTemplateDto.getId() == null) {
            mtSmsTemplate.setCreateTime(new Date());
            mtSmsTemplate.setUpdateTime(new Date());
            mtSmsTemplateMapper.insert(mtSmsTemplate);
        } else {
            MtSmsTemplate oldSmsTemplate = getById(mtSmsTemplateDto.getId());
            if (oldSmsTemplate == null) {
                throw new BusinessCheckException("该短信模板不存在");
            }
            mtSmsTemplate.setMerchantId(oldSmsTemplate.getMerchantId());
            mtSmsTemplate.setId(mtSmsTemplateDto.getId());
            mtSmsTemplate.setUpdateTime(new Date());
            this.updateById(mtSmsTemplate);
        }

        return mtSmsTemplate;
    }

    /**
     * 根据ID删除数据
     *
     * @param id 模板ID
     * @param operator 操作人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "删除短信模板")
    public void deleteTemplate(Integer id, String operator) {
        MtSmsTemplate mtTemplate = mtSmsTemplateMapper.selectById(id);
        if (null == mtTemplate) {
            return;
        }

        mtTemplate.setStatus(StatusEnum.DISABLE.getKey());
        mtTemplate.setUpdateTime(new Date());

        mtSmsTemplateMapper.updateById(mtTemplate);
    }

    /**
     * 根据D获取信息
     *
     * @param  id 模板ID
     */
    @Override
    public MtSmsTemplate querySmsTemplateById(Integer id) {
        return mtSmsTemplateMapper.selectById(id);
    }

    @Override
    public List<MtSmsTemplate> querySmsTemplateByParams(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        List<MtSmsTemplate> result = mtSmsTemplateMapper.selectByMap(params);
        return result;
    }
}
