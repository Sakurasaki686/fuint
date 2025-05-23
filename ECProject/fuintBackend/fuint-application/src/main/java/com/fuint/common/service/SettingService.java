package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.ParamDto;
import com.fuint.repository.model.MtSetting;
import com.fuint.framework.exception.BusinessCheckException;
import java.util.List;

/**
 * 配置业务接口
 *
 *
 * 
 */
public interface SettingService extends IService<MtSetting> {

    /**
     * 删除配置
     *
     * @param  merchantId
     * @param  name
     * @throws BusinessCheckException
     */
    void removeSetting(Integer merchantId, String name) throws BusinessCheckException;

    /**
     * 保存配置
     *
     * @param  reqDto
     * @throws BusinessCheckException
     */
    MtSetting saveSetting(MtSetting reqDto) throws BusinessCheckException;

    /**
     * 获取配置列表
     *
     * @param  type
     * @throws BusinessCheckException
     */
    List<MtSetting> getSettingList(Integer merchantId, String type) throws BusinessCheckException;

    /**
     * 根据配置名称获取配置信息
     *
     * @param  merchantId
     * @param  name name
     * @throws BusinessCheckException
     */
    MtSetting querySettingByName(Integer merchantId, String name) throws BusinessCheckException;

    /**
     * 获取系统上传文件的根路径
     * @return 本地配置或阿里云的oss域名
     * */
    String getUploadBasePath();

    /**
     * 获取支付方式列表
     * @param platform 平台
     * @return
     * */
    List<ParamDto> getPayTypeList(String platform);
}
