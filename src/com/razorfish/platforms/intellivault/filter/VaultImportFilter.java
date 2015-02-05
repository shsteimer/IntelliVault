package com.razorfish.platforms.intellivault.filter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: sean.steimer Date: 4/3/13 Time: 2:05 PM To change this template use File | Settings
 * | File Templates.
 */
public class VaultImportFilter implements Filter<VirtualFile> {
    private List<String> ignorePatterns;

    private static final Logger log = Logger.getInstance(VaultImportFilter.class);

    /**
     * Create a new VaultImportFilter instance based on a List of ignore patterns
     *
     * @param ignorePatterns the ignore patterns for this filter
     */
    public VaultImportFilter(List<String> ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

    @Override
    public boolean allows(VirtualFile file) {
        for (String ignorePattern : ignorePatterns) {
            boolean matches = file.getName().equals(ignorePattern) ||
                    Pattern.compile(ignorePattern).matcher(file.getName()).matches();
            if (matches) {
                log.info(String.format("Skipping file %s, matched ignore pattern %s.",
                        file.getCanonicalPath(), ignorePattern));
                return false;
            }
        }

        return true;
    }
}
