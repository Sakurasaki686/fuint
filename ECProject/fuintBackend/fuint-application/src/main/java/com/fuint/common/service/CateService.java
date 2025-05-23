package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.GoodsCateDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtGoodsCate;
import java.util.List;
import java.util.Map;

/**
 * 商品分类业务接口
 *
 *
 *
 */
public interface CateService extends IService<MtGoodsCate> {

    /**
     * 分页查询列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<GoodsCateDto> queryCateListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 添加商品分类
     *
     * @param  reqDto
     * @throws BusinessCheckException
     */
    MtGoodsCate addCate(MtGoodsCate reqDto) throws BusinessCheckException;

    /**
     * 根据ID获取商品分类信息
     *
     * @param  id ID
     * @throws BusinessCheckException
     */
    MtGoodsCate queryCateById(Integer id) throws BusinessCheckException;

    /**
     * 根据ID删除
     *
     * @param  id       ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     */
    void deleteCate(Integer id, String operator) throws BusinessCheckException;

    /**
     * 更新分类
     * @param  reqDto
     * @throws BusinessCheckException
     * */
    MtGoodsCate updateCate(MtGoodsCate reqDto) throws BusinessCheckException;

    /**
     * 根据条件搜索分类
     * */
    List<MtGoodsCate> queryCateListByParams(Map<String, Object> params) throws BusinessCheckException;
}
