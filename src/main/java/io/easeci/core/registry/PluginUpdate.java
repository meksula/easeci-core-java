package io.easeci.core.registry;

import io.easeci.core.registry.dto.PluginUpdateCheckResponse;
import ratpack.exec.Promise;

public interface PluginUpdate {

    Promise<PluginUpdateCheckResponse> checkForUpdate(String pluginName, String pluginVersion);
}
