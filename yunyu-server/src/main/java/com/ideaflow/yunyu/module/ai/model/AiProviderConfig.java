package com.ideaflow.yunyu.module.ai.model;

import java.util.List;
import lombok.Data;

/**
 * AI 提供商配置集合模型。
 * 作用：描述多配置集合与当前启用配置键，供配置中心统一持久化和读取。
 */
@Data
public class AiProviderConfig {

    private String activeProfileKey;
    private List<AiProviderProfile> profiles;
}
