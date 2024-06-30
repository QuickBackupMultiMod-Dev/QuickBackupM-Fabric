package io.github.skydynamic.quickbackupmulti.api;

import java.nio.file.Path;

public interface ServerPathGetter {
    Path getConfigPath();
    Path getGamePath();
}
