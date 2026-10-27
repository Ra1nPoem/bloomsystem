package com.bloomsystem;

import com.bloomsystem.network.BloomNetwork;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BloomSystem 主入口（服务端只负责注册命令/网络，不参与渲染）。
 */
public final class BloomSystem implements ModInitializer {
    public static final String MOD_ID = "bloomsystem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BloomNetwork.registerCommon();
        LOGGER.info("[BloomSystem] initialized (client renderer is client-side only)");
    }
}
