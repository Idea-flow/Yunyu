package com.ideaflow.yunyu.module.tag.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台标签条目响应类。
 * 作用：向后台标签管理页返回单个标签的展示信息，便于前端列表和表单复用。
 */
@Data
public class AdminTagItemResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String coverUrl;
    private String status;
    private Integer sortOrder;
    private Long relatedPostCount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
