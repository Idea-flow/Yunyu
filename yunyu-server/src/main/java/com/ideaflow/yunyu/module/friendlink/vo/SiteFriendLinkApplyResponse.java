package com.ideaflow.yunyu.module.friendlink.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 前台友链申请响应类。
 * 作用：向友链申请页返回提交结果、当前状态与提示信息。
 */
@Data
@AllArgsConstructor
public class SiteFriendLinkApplyResponse {

    private final Long friendLinkId;
    private final String status;
    private final String message;
}
