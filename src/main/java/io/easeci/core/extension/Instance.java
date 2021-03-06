package io.easeci.core.extension;

import io.easeci.extension.ExtensionType;
import io.easeci.extension.Standalone;
import lombok.*;

import java.time.LocalDateTime;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.PLUGIN_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.FOUR;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.ONE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;
import static java.util.Objects.nonNull;

@Builder
@ToString(exclude = "instance")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PACKAGE)
public class Instance {
    public Thread thread;
    public LocalDateTime instantiateDateTime;
    private Plugin plugin;
    private Object instance;
    private int identityHashCode;

    @Setter
    private boolean isStarted;

    public void assignThread(Thread thread) {
        if (this.thread == null) {
            this.thread = thread;
            logit(PLUGIN_EVENT, "Thread " + thread.getName() + " is assigned now to plugin: " + plugin.toShortString(), ONE);
        }
        else logit(PLUGIN_EVENT, "Instance has thread assigned! Cannot change.", FOUR);
    }

    public boolean isRunning() {
        ExtensionType extensionType = ExtensionType.toEnum(plugin.getJarArchive().getExtensionManifest().getImplementsProperty());
        if (ExtensionType.STANDALONE_PLUGIN.equals(extensionType)) {
            return isStarted;
        }
        if (ExtensionType.EXTENSION_PLUGIN.equals(extensionType)) {
            return nonNull(this.getInstance()) &&
                    nonNull(this.getInstantiateDateTime()) &&
                    this.identityHashCode != 0;
        }
        return false;
    }

    public boolean clear() {
        this.isStarted = false;
        this.instance = null;
        return this.instance == null;
    }

    public Standalone toStandalone() throws ClassCastException {
        if (this.instance instanceof Standalone) {
            return (Standalone) this.instance;
        }
        throw new PluginSystemIntegrityViolated("This instance with hashCode: " + this.identityHashCode + " is not instance of Standalone.class");
    }

    public boolean isStandalone() {
        return this.instance instanceof Standalone;
    }

    @Override
    public boolean equals(Object obj) {
        return this.plugin.equals(((Instance) obj).plugin);
    }

    @Override
    public int hashCode() {
        return this.plugin.hashCode();
    }
}
