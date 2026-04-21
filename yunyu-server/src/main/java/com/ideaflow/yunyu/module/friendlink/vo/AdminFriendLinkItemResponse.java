package com.ideaflow.yunyu.module.friendlink.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台友链条目响应类。
 * 作用：向后台友链管理页返回单条友链的站点信息、联系信息、排序与审核状态。
 */
@Data
public class AdminFriendLinkItemResponse {

    private Long id;
    private String siteName;
    private String siteUrl;
    private String logoUrl;
    private String description;
    private String contactName;
    private String contactEmail;
    private String contactMessage;
    private String themeColor;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
