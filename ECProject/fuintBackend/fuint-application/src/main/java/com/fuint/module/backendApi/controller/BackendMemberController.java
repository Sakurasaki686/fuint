package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.GroupMemberDto;
import com.fuint.common.dto.UserDto;
import com.fuint.common.dto.UserGroupDto;
import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.UserSettingEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.PhoneFormatCheckUtils;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;

/**
 * 会员管理类controller
 *
 * 
 * 
 */
@Api(tags="管理端-会员相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/member")
public class BackendMemberController extends BaseController {

    /**
     * 会员服务接口
     */
    private MemberService memberService;

    /**
     * 配置服务接口
     * */
    private SettingService settingService;

    /**
     * 后台账户服务接口
     */
    private AccountService accountService;

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 会员分组服务接口
     */
    private MemberGroupService memberGroupService;

    /**
     * 查询会员列表
     *
     * @param request  HttpServletRequest对象
     * @return 会员列表
     */
    @ApiOperation(value = "查询会员列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String mobile = request.getParameter("mobile");
        String userId = request.getParameter("id");
        String name = request.getParameter("name");
        String birthday = request.getParameter("birthday");
        String userNo = request.getParameter("userNo");
        String gradeId = request.getParameter("gradeId");
        String startTime = request.getParameter("startTime") == null ? "" : request.getParameter("startTime");
        String endTime = request.getParameter("endTime") == null ? "" : request.getParameter("endTime");
        String status = request.getParameter("status");
        String storeIds = request.getParameter("storeIds");
        String groupIds = request.getParameter("groupIds");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotEmpty(userId)) {
            params.put("id", userId);
        }
        if (StringUtil.isNotEmpty(name)) {
            params.put("name", name);
        }
        if (StringUtil.isNotEmpty(mobile)) {
            params.put("mobile", mobile);
        }
        if (StringUtil.isNotEmpty(birthday)) {
            params.put("birthday", birthday);
        }
        if (StringUtil.isNotEmpty(userNo)) {
            params.put("userNo", userNo);
        }
        if (StringUtil.isNotEmpty(gradeId)) {
            params.put("gradeId", gradeId);
        }
        if (StringUtil.isNotEmpty(status)) {
            params.put("status", status);
        }
        if (StringUtil.isNotEmpty(storeIds)) {
            params.put("storeIds", storeIds);
        }
        if (StringUtil.isNotEmpty(groupIds)) {
            params.put("groupIds", groupIds);
        }
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }
        TAccount account = accountService.getAccountInfoById(accountInfo.getId());
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("merchantId", accountInfo.getMerchantId());
        }
        if (StringUtil.isNotEmpty(startTime)) {
            params.put("startTime", startTime);
        }
        if (StringUtil.isNotEmpty(endTime)) {
            params.put("endTime", endTime);
        }
        paginationRequest.setSearchParams(params);
        PaginationResponse<UserDto> paginationResponse = memberService.queryMemberListByPagination(paginationRequest);

        // 会员等级列表
        Map<String, Object> param = new HashMap<>();
        param.put("status", StatusEnum.ENABLED.getKey());
        if (account.getMerchantId() != null && account.getMerchantId() > 0) {
            param.put("MERCHANT_ID", account.getMerchantId());
        }
        List<MtUserGrade> userGradeList = memberService.queryMemberGradeByParams(param);

        // 店铺列表
        Map<String, Object> paramsStore = new HashMap<>();
        paramsStore.put("status", StatusEnum.ENABLED.getKey());
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            paramsStore.put("merchantId", accountInfo.getMerchantId());
        }
        List<MtStore> storeList = storeService.queryStoresByParams(paramsStore);

        // 会员分组
        List<UserGroupDto> groupList = new ArrayList<>();
        Map<String, Object> searchParams = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            searchParams.put("merchantId", accountInfo.getMerchantId());
        }
        PaginationRequest groupRequest = new PaginationRequest();
        groupRequest.setCurrentPage(1);
        groupRequest.setPageSize(Constants.MAX_ROWS);
        groupRequest.setSearchParams(searchParams);
        PaginationResponse<UserGroupDto> groupResponse = memberGroupService.queryMemberGroupListByPagination(groupRequest);
        if (groupResponse != null && groupResponse.getContent() != null) {
            groupList = groupResponse.getContent();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);
        result.put("userGradeList", userGradeList);
        result.put("storeList", storeList);
        result.put("groupList", groupList);

        return getSuccessResult(result);
    }

    /**
     * 更新会员状态
     *
     * @return
     */
    @ApiOperation(value = "更新会员状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer userId = param.get("userId") == null ? 0 : Integer.parseInt(param.get("userId").toString());
        String status = param.get("status") == null ? StatusEnum.ENABLED.getKey() : param.get("status").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null) {
            return getFailureResult(201, "会员不存在");
        }

        userInfo.setStatus(status);
        memberService.updateMember(userInfo);

        return getSuccessResult(true);
    }

    /**
     * 删除会员
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "删除会员")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject delete(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        String operator = accountInfo.getAccountName();
        memberService.deleteMember(id, operator);

        return getSuccessResult(true);
    }

    /**
     * 保存会员信息
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存会员信息")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:add')")
    public ResponseObject save(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException, ParseException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        String id = param.get("id").toString();
        String name = param.get("name") == null ? "" : param.get("name").toString();
        String gradeId = param.get("gradeId") == null ? "0" :param.get("gradeId").toString();
        String groupId = param.get("groupId") == null ? "0" :param.get("groupId").toString();
        String userNo = param.get("userNo") == null ? "" : param.get("userNo").toString();
        String mobile = param.get("mobile") == null ? "" : param.get("mobile").toString();
        String sex = param.get("sex") == null ? "0" : param.get("sex").toString();
        String idCard = param.get("idcard") == null ? "" : param.get("idcard").toString();
        String birthday = param.get("birthday") == null ? "" : param.get("birthday").toString();
        String address = param.get("address") == null ? "" : param.get("address").toString();
        String description = param.get("description") == null ? "" : param.get("description").toString();
        String status = param.get("status") == null ? StatusEnum.ENABLED.getKey() : param.get("status").toString();
        String startTime = param.get("startTime") == null ? "" : param.get("startTime").toString();
        String endTime = param.get("endTime") == null ? "" : param.get("endTime").toString();

        if (PhoneFormatCheckUtils.isChinaPhoneLegal(mobile)) {
            // 重置该手机号
            memberService.resetMobile(mobile, StringUtil.isEmpty(id) ? 0 : Integer.parseInt(id));
        }

        MtUser memberInfo;
        if (StringUtil.isEmpty(id)) {
            memberInfo = new MtUser();
        } else {
            memberInfo = memberService.queryMemberById(Integer.parseInt(id));
        }

        memberInfo.setMerchantId(accountInfo.getMerchantId());
        memberInfo.setName(name);
        memberInfo.setStatus(status);
        if (StringUtil.isNotEmpty(groupId)) {
            memberInfo.setGroupId(Integer.parseInt(groupId));
        }
        memberInfo.setGradeId(gradeId);
        memberInfo.setUserNo(userNo);
        if (PhoneFormatCheckUtils.isChinaPhoneLegal(mobile)) {
            memberInfo.setMobile(mobile);
        }
        memberInfo.setSex(Integer.parseInt(sex));
        memberInfo.setIdcard(idCard);
        memberInfo.setBirthday(birthday);
        memberInfo.setAddress(address);
        memberInfo.setDescription(description);
        memberInfo.setStartTime(DateUtil.parseDate(startTime));
        memberInfo.setEndTime(DateUtil.parseDate(endTime));
        if (StringUtil.isEmpty(id)) {
            TAccount account = accountService.getAccountInfoById(accountInfo.getId());
            Integer storeId = account.getStoreId();
            memberInfo.setStoreId(storeId);
            memberService.addMember(memberInfo);
        } else {
            memberService.updateMember(memberInfo);
        }
        return getSuccessResult(true);
    }

    /**
     * 获取会员详情
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取会员详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        MtUser mtUser = memberService.queryMemberById(id);
        if (mtUser == null) {
            return getFailureResult(201, "会员信息有误");
        }

        UserDto memberInfo = new UserDto();
        BeanUtils.copyProperties(mtUser, memberInfo);

        MtUserGroup mtUserGroup = memberGroupService.queryMemberGroupById(memberInfo.getGroupId());
        if (mtUserGroup != null) {
            UserGroupDto userGroupDto = new UserGroupDto();
            BeanUtils.copyProperties(mtUserGroup, userGroupDto);
            memberInfo.setGroupInfo(userGroupDto);
        }

        // 隐藏手机号中间四位
        String phone = memberInfo.getMobile();
        if (phone != null && StringUtil.isNotEmpty(phone) && phone.length() == 11) {
            memberInfo.setMobile(phone.substring(0, 3) + "****" + phone.substring(7));
        }

        Map<String, Object> param = new HashMap<>();
        List<MtUserGrade> userGradeList = memberService.queryMemberGradeByParams(param);

        Map<String, Object> result = new HashMap<>();
        result.put("userGradeList", userGradeList);
        result.put("memberInfo", memberInfo);

        return getSuccessResult(result);
    }

    /**
     * 获取会员设置
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取会员设置")
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:setting')")
    public ResponseObject setting(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        List<MtSetting> settingList = settingService.getSettingList(accountInfo.getMerchantId(), SettingTypeEnum.USER.getKey());

        String getCouponNeedPhone = "false";
        String submitOrderNeedPhone = "false";
        String loginNeedPhone = "false";

        for (MtSetting setting : settingList) {
             if (setting.getName().equals("getCouponNeedPhone")) {
                 getCouponNeedPhone = setting.getValue();
             } else if (setting.getName().equals("submitOrderNeedPhone")) {
                 submitOrderNeedPhone = setting.getValue();
             } else if (setting.getName().equals("loginNeedPhone")) {
                 loginNeedPhone = setting.getValue();
             }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("getCouponNeedPhone", getCouponNeedPhone);
        result.put("submitOrderNeedPhone", submitOrderNeedPhone);
        result.put("loginNeedPhone", loginNeedPhone);

        return getSuccessResult(result);
    }

    /**
     * 保存设置
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存设置")
    @RequestMapping(value = "/saveSetting", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:setting')")
    public ResponseObject saveSetting(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String getCouponNeedPhone = param.get("getCouponNeedPhone") != null ? param.get("getCouponNeedPhone").toString() : "false";
        String submitOrderNeedPhone = param.get("submitOrderNeedPhone") != null ? param.get("submitOrderNeedPhone").toString() : "false";
        String loginNeedPhone = param.get("loginNeedPhone") != null ? param.get("loginNeedPhone").toString() : "false";

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        String operator = accountInfo.getAccountName();

        UserSettingEnum[] settingList = UserSettingEnum.values();
        for (UserSettingEnum setting : settingList) {
            MtSetting info = new MtSetting();
            info.setType(SettingTypeEnum.USER.getKey());
            info.setName(setting.getKey());

            if (setting.getKey().equals("getCouponNeedPhone")) {
                info.setValue(getCouponNeedPhone);
            } else if (setting.getKey().equals("submitOrderNeedPhone")) {
                info.setValue(submitOrderNeedPhone);
            } else if (setting.getKey().equals("loginNeedPhone")) {
                info.setValue(loginNeedPhone);
            }

            info.setDescription(setting.getValue());
            info.setOperator(operator);
            info.setUpdateTime(new Date());
            info.setMerchantId(accountInfo.getMerchantId());
            info.setStoreId(0);

            settingService.saveSetting(info);
        }

        return getSuccessResult(true);
    }

    /**
     * 获取会员分组
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取会员分组")
    @RequestMapping(value = "/groupList", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:group:index')")
    public ResponseObject groupList(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        // 会员分组
        List<UserGroupDto> groupList = new ArrayList<>();
        Map<String, Object> searchParams = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            searchParams.put("merchantId", accountInfo.getMerchantId());
        }
        PaginationRequest groupRequest = new PaginationRequest();
        groupRequest.setCurrentPage(1);
        groupRequest.setPageSize(Constants.MAX_ROWS);
        groupRequest.setSearchParams(searchParams);
        PaginationResponse<UserGroupDto> groupResponse = memberGroupService.queryMemberGroupListByPagination(groupRequest);
        if (groupResponse != null && groupResponse.getContent() != null) {
            groupList = groupResponse.getContent();
        }

        return getSuccessResult(groupList);
    }

    /**
     * 查找会员列表
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "查找会员列表")
    @RequestMapping(value = "/searchMembers", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject searchMembers(HttpServletRequest request) {
        String token = request.getHeader("Access-Token");
        String groupIds = request.getParameter("groupIds") != null ? request.getParameter("groupIds") : "";
        String keyword = request.getParameter("keyword") != null ? request.getParameter("keyword") : "";
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }
        List<GroupMemberDto> memberList = memberService.searchMembers(accountInfo.getMerchantId(), keyword, groupIds,1, Constants.MAX_ROWS);
        return getSuccessResult(memberList);
    }
}
