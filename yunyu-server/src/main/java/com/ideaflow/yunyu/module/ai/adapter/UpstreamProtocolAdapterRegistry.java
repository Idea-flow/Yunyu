package com.ideaflow.yunyu.module.ai.adapter;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.ai.model.UpstreamProtocolType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 上游协议适配器注册表。
 * 作用：集中维护协议类型到适配器实例的映射，避免调用方编写分支路由逻辑。
 */
@Component
public class UpstreamProtocolAdapterRegistry {

    private final Map<UpstreamProtocolType, UpstreamProtocolAdapter> adapterMap = new EnumMap<>(UpstreamProtocolType.class);

    /**
     * 创建适配器注册表。
     *
     * @param adapters 系统内所有协议适配器
     */
    public UpstreamProtocolAdapterRegistry(List<UpstreamProtocolAdapter> adapters) {
        for (UpstreamProtocolAdapter adapter : adapters) {
            adapterMap.put(adapter.protocolType(), adapter);
        }
    }

    /**
     * 按协议类型获取适配器。
     *
     * @param protocolType 协议类型
     * @return 协议适配器
     */
    public UpstreamProtocolAdapter getAdapter(UpstreamProtocolType protocolType) {
        UpstreamProtocolAdapter adapter = adapterMap.get(protocolType);
        if (adapter == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "未找到可用的上游协议适配器：" + protocolType);
        }
        return adapter;
    }
}
