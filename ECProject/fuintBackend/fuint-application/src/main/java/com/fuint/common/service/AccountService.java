package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.AccountDto;
import com.fuint.common.dto.AccountInfo;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.TAccount;
import com.fuint.repository.model.TDuty;
import java.util.List;

/**
 * 后台账号接口
 *
 *
 *
 */
public interface AccountService extends IService<TAccount> {

    /**
     * 分页查询账号列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<AccountDto> getAccountListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 根据用户名获取用户对象
     *
     * @param userName
     * @return
     */
    AccountInfo getAccountByName(String userName);

    /**
     * 获取用户信息
     *
     * @param id
     * @return
     */
    TAccount getAccountInfoById(Integer id);

    /**
     * 创建账号信息
     *
     * @param  accountInfo
     * @param  duties
     * @return
     * */
    TAccount createAccountInfo(TAccount accountInfo, List<TDuty> duties);

    /**
     * 获取账号角色ID
     *
     * @param accountId
     * @return
     * */
    List<Long> getRoleIdsByAccountId(Integer accountId);

    /**
     * 修改账户
     *
     * @param tAccount 账户实体
     * @throws BusinessCheckException
     */
    void editAccount(TAccount tAccount, List<TDuty> duties) throws BusinessCheckException;

    /**
     * 根据账户名称获取账户所分配的角色ID集合
     *
     * @param accountId 账户
     * @return 角色ID集合
     */
    List<Long> getDutyIdsByAccountId(long accountId);

    /**
     * 更新账户信息
     *
     * @param tAccount
     * @return
     */
    void updateAccount(TAccount tAccount);

    /**
     * 删除后台账号
     *
     * @param userId
     * @return
     * */
    void deleteAccount(Long userId);

    /**
     * 密码加密
     *
     * @param user
     * @return
     * */
    void entryptPassword(TAccount user);

    /**
     * 获取加密密码
     *
     * @param password
     * @param salt
     * @return
     * */
    String getEntryptPassword(String password, String salt);
}
