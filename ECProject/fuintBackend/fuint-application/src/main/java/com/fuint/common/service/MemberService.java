package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alibaba.fastjson.JSONObject;
import com.fuint.common.dto.GroupMemberDto;
import com.fuint.common.dto.MemberTopDto;
import com.fuint.common.dto.UserDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtUser;
import com.fuint.repository.model.MtUserGrade;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 会员服务接口
 *
 *
 *
 */
public interface MemberService extends IService<MtUser> {

    /**
     * 更新活跃时间
     * @param userId 会员ID
     * @return
     * */
    Boolean updateActiveTime(Integer userId) throws BusinessCheckException;

    /**
     * 获取当前操作会员信息
     * @param userId
     * @param accessToken
     * @return
     * */
    MtUser getCurrentUserInfo(HttpServletRequest request, Integer userId, String accessToken) throws BusinessCheckException;

    /**
     * 分页查询会员列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<UserDto> queryMemberListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 添加会员
     *
     * @param  reqUserDto
     * @throws BusinessCheckException
     */
    MtUser addMember(MtUser reqUserDto) throws BusinessCheckException;

    /**
     * 编辑会员
     *
     * @param  reqUserDto
     * @throws BusinessCheckException
     */
    MtUser updateMember(MtUser reqUserDto) throws BusinessCheckException;

    /**
     * 通过手机号添加会员
     *
     * @param  merchantId
     * @param  mobile
     * @throws BusinessCheckException
     */
    MtUser addMemberByMobile(Integer merchantId, String mobile) throws BusinessCheckException;

    /**
     * 根据会员ID获取会员信息
     *
     * @param  id 会员ID
     * @throws BusinessCheckException
     */
    MtUser queryMemberById(Integer id) throws BusinessCheckException;

    /**
     * 根据会员名称获取会员信息
     *
     * @param  merchantId
     * @param  name 会员名称
     * @throws BusinessCheckException
     */
    MtUser queryMemberByName(Integer merchantId, String name) throws BusinessCheckException;

    /**
     * 根据会员ID获取会员信息
     *
     * @param  merchantId
     * @param  openId 微信openId
     * @throws BusinessCheckException
     */
    MtUser queryMemberByOpenId(Integer merchantId, String openId, JSONObject userInfo) throws BusinessCheckException;

    /**
     * 根据会员组ID获取会员组信息
     *
     * @param  id 会员组ID
     * @throws BusinessCheckException
     */
    MtUserGrade queryMemberGradeByGradeId(Integer id) throws BusinessCheckException;

    /**
     * 根据会员手机获取会员信息
     *
     * @param merchantId
     * @param  mobile 会员手机
     * @throws BusinessCheckException
     */
    MtUser queryMemberByMobile(Integer merchantId, String mobile) throws BusinessCheckException;

    /**
     * 根据会员号获取会员信息
     *
     * @param  merchantId
     * @param  userNo 会员号
     * @throws BusinessCheckException
     */
    MtUser queryMemberByUserNo(Integer merchantId, String userNo) throws BusinessCheckException;

    /**
     * 根据会员ID 删除店铺信息
     *
     * @param  id      会员ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     */
    Integer deleteMember(Integer id, String operator) throws BusinessCheckException;

    /**
     * 根据条件搜索会员分组
     * */
    List<MtUserGrade> queryMemberGradeByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 获取会员数量
     * */
    Long getUserCount(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 获取会员数量
     * */
    Long getUserCount(Integer merchantId, Integer storeId, Date beginTime, Date endTime) throws BusinessCheckException;

    /**
     * 获取活跃会员数量
     * */
    Long getActiveUserCount(Integer merchantId, Integer storeId, Date beginTime, Date endTime) throws BusinessCheckException;

    /**
     * 重置手机号
     *
     * @param  mobile 手机号码
     * @param  userId 会员ID
     * @throws BusinessCheckException
     */
    void resetMobile(String mobile, Integer userId) throws BusinessCheckException;

    /**
     * 获取会员消费排行榜
     *
     * @param merchantId
     * @param storeId
     * @param startTime
     * @param endTime
     * @return
     * */
    List<MemberTopDto> getMemberConsumeTopList(Integer merchantId, Integer storeId, Date startTime, Date endTime);

    /**
     * 查找会员列表
     *
     * @param merchantId
     * @param keyword
     * @param groupIds
     * @param page
     * @param pageSize
     * @return
     * */
    List<GroupMemberDto> searchMembers(Integer merchantId, String keyword, String groupIds, Integer page, Integer pageSize);

    /**
     * 设定安全的密码
     *
     * @param password
     * @param salt
     * @return
     */
    String enCodePassword(String password, String salt);

    /**
     * 获取加密密码
     *
     * @param password
     * @param salt
     * @return
     * */
    String deCodePassword(String password, String salt);

    /**
     * 获取会员ID列表
     *
     * @param merchantId
     * @param storeId
     * @return
     * */
    List<Integer> getUserIdList(Integer merchantId, Integer storeId);
}
