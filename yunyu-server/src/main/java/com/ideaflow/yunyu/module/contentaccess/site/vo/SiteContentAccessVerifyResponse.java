package com.ideaflow.yunyu.module.contentaccess.site.vo;

import com.ideaflow.yunyu.module.post.site.vo.SiteContentAccessStateResponse;
import lombok.Data;

/**
 * 前台内容访问校验响应类。
 * 作用：在校验访问码成功后，返回最新的内容访问状态，便于前台立即刷新解锁结果。
 */
@Data
public class SiteContentAccessVerifyResponse {

    private Boolean granted;
    private SiteContentAccessStateResponse contentAccessState;
}
