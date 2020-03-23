package com.razorfish.platforms.intellivault.vo;

import com.intellij.psi.PsiDirectory;
import com.razorfish.platforms.intellivault.utils.IntelliVaultConstants;

public class VaultOperationDirectory {
    private PsiDirectory psiDir;
    private String jcrPath;

    /**
     * Create a VaultOperationDirectory instance.
     *
     * @param psiDir the selected psiDirectory for the vault operation to be executed.
     * @param jcrRootFolderName the name of the jcr root foler, to be used for computing the jcr path
     */
    public VaultOperationDirectory(PsiDirectory psiDir, String jcrRootFolderName) {
        this.psiDir=psiDir;
        this.jcrPath=computePathFromPsiDirectory(psiDir,jcrRootFolderName);
    }

    public PsiDirectory getPsiDir() {
        return psiDir;
    }

    public void setPsiDir(PsiDirectory psiDir) {
        this.psiDir = psiDir;
    }

    public String getJcrPath() {
        return jcrPath;
    }

    public void setJcrPath(String jcrPath) {
        this.jcrPath = jcrPath;
    }

    /**
     * Compute the operative jcr path that the psiDirectory represents.
     * The method calls itself recursively to get the parent path, and then appends the name of the current directory.
     * @param psiDir the PsiDirectory used to compute the jcr path
     * @param rootDirectoryName the name of the directory representing the jcr root.
     * @return the computer jcr path for the folder
     */
    private String computePathFromPsiDirectory(PsiDirectory psiDir, String rootDirectoryName) {

        String directoryName = psiDir.getName();
        if(directoryName.equals(rootDirectoryName)) {
            return "";
        }

        String parentName = computePathFromPsiDirectory(psiDir.getParentDirectory(),rootDirectoryName);

        return parentName + IntelliVaultConstants.JCR_PATH_SEPERATOR + directoryName;

    }
}
