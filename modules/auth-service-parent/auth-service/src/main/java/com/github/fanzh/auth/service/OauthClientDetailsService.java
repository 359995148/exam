package com.github.fanzh.auth.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.auth.api.module.OauthClientDetails;
import com.github.fanzh.auth.mapper.OauthClientDetailsMapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Oauth2客户端Service
 *
 * @author fanzh
 * @date 2019/3/30 16:48
 */
@Slf4j
@AllArgsConstructor
@Service
public class OauthClientDetailsService extends BaseService<OauthClientDetailsMapper, OauthClientDetails> {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 查询
     *
     * @param pageNum
     * @param pageSize
     * @param sort
     * @param order
     * @param oauthClientDetails
     * @return
     */
    public ExecResult<Page<OauthClientDetails>> listOrPage(Optional<Integer> pageNum, Optional<Integer> pageSize, Optional<String> sort, Optional<String> order, OauthClientDetails oauthClientDetails) {
        EntityWrapper<OauthClientDetails> ew = new EntityWrapper<>();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        if (ParamsUtil.isNotEmpty(oauthClientDetails.getId())) {
            ew.eq(SqlField.ID, oauthClientDetails.getId());
        }
        if (ParamsUtil.isNotEmpty(oauthClientDetails.getClientId())) {
            ew.eq("client_id", oauthClientDetails.getClientId());
        }
        if (ParamsUtil.isNotEmpty(oauthClientDetails.getTenantCode())) {
            ew.like("tenant_code", oauthClientDetails.getTenantCode());
        }
        if (sort.isPresent()) {
            if (Objects.equals(order.orElse(CommonConstant.PAGE_ORDER_DEFAULT), CommonConstant.PAGE_ORDER_DEFAULT)) {
                ew.orderDesc(Arrays.asList(sort.get()));
            } else {
                ew.orderAsc(Arrays.asList(sort.get()));
            }
        }
        Page<OauthClientDetails> page = new Page<>();
        if (pageNum.isPresent() && pageSize.isPresent()) {
            page = selectPage(new Page(pageNum.get(), pageSize.get()), ew);
        } else {
            page.setRecords(selectList(ew));
        }
        return ExecResultUtil.success(page);
    }

    /**
     * 新增
     *
     * @param oauthClientDetails
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> add(OauthClientDetails oauthClientDetails) {
        // 加密密钥
        oauthClientDetails.setClientSecret(bCryptPasswordEncoder.encode(oauthClientDetails.getClientSecretPlainText()));
        baseSave(oauthClientDetails);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新
     *
     * @param oauthClientDetails
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> update(OauthClientDetails oauthClientDetails) {
        OauthClientDetails exist = baseGetById(oauthClientDetails.getId());
        // 有调整过明文则重新加密密钥
        if (!Objects.equals(exist.getClientSecretPlainText(), oauthClientDetails.getClientSecretPlainText())) {
            oauthClientDetails.setClientSecret(bCryptPasswordEncoder.encode(oauthClientDetails.getClientSecretPlainText()));
        }
        baseSave(oauthClientDetails);
        return ExecResultUtil.success(true);
    }
}
