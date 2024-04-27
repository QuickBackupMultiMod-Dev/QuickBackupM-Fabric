package dev.skydynamic.quickbackupmulti.utils.filefilter;

import org.apache.commons.io.filefilter.IOFileFilter;
import java.io.File;

public class NonRecursiveDirFilter implements IOFileFilter {
    @Override
    public boolean accept(File file) {
        return false;  // 不递归子目录
    }

    @Override
    public boolean accept(File dir, String name) {
        return false;  // 不递归子目录
    }
}
