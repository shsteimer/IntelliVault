package com.razorfish.platforms.intellivault.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.razorfish.platforms.intellivault.diff.FileComparator;
import com.razorfish.platforms.intellivault.exceptions.IntelliVaultException;
import com.razorfish.platforms.intellivault.filter.Filter;

import java.io.*;

/**
 * FileUtiles is a set of static utility methods for itneracting with files.
 * Both files on the file system before/after a vault import/export operation,
 * and virtual files within the intelliJ IDEA Project file system.
 */
public class FileUtils {
    private static final String VAULTCLIPSE_TEMP_DIR_NAME = "IntelliVault";
    public static final int BUFFER_LENGTH = 1024;
    private static final boolean IGNORE_WHITESPACE = false;

    /**
     * Private default constructor, prevents creating instances
     */
    private FileUtils() {

    }

    /**
     * Create a temporary directory on the file system, which will be used as
     * the base directory for a vault operation.
     * 
     * @param userTempDir
     *            the user's temporary directory, where the tempdir will be
     *            created.
     * @return a java.io.File instance representing the newly created directory.
     */
    public static File createTempDirectory(String userTempDir) {
        File baseDir = new File(userTempDir + File.separator + VAULTCLIPSE_TEMP_DIR_NAME + File.separator
                + System.currentTimeMillis());
        baseDir.mkdirs();

        return baseDir;
    }

    /**
     * Copy the contents of an import operation from the IDEA project directory
     * to the vault temp directory.
     * 
     * @param importBaseDir
     *            the file system directory, which serves as the root of the
     *            copy target
     * @param importDir
     *            the PsiDirectory (IDEA virtual directory) containing the
     *            contents to be copied
     * @param path
     *            the jcr path representing the root of the import
     * @param filter
     *            A list of Filters specifying which files should be ignored
     *            (not imported).
     * 
     * @throws com.razorfish.platforms.intellivault.exceptions.IntelliVaultException
     *             if an error occurs during copy
     */
    public static void copyImportContents(File importBaseDir, PsiDirectory importDir, String path,
            Filter<VirtualFile> filter) throws IntelliVaultException {
        File copyRootDir = new File(importBaseDir.getAbsolutePath() + File.separator + IntelliVaultConstants.JCR_ROOT
                + path.replace(IntelliVaultConstants.JCR_PATH_SEPERATOR, File.separator));
        copyRootDir.mkdirs();

        try {
            copyImportContents(copyRootDir, importDir.getVirtualFile(), filter);
        } catch (IOException e) {
            throw new IntelliVaultException("Failed copying contents.", e);
        }
    }

    /**
     * Recursive method called for copying import contents from the IDEA project
     * directory to the vault temp directory. For each child of virtualFile, if
     * it is a directory, a new directory is created in fsDir, and this method
     * is called recursively. If the child is a file, then that file is created
     * and the content copied.
     * 
     * @param fsDir
     *            th file system directory where contents will be copied
     * @param virtualFile
     *            the VirtualFile in the IDEA project being copied to fsDir. In
     *            practice this is always a directory.
     * @param filter
     *            A list of Filters specifying which files should be ignored
     *            (not imported).
     * @throws IOException
     *             if an error occurs during copy
     */
    private static void copyImportContents(File fsDir, VirtualFile virtualFile, Filter<VirtualFile> filter)
            throws IOException {
        VirtualFile[] contents = virtualFile.getChildren();
        for (int i = 0; i < contents.length; i++) {
            VirtualFile file = contents[i];
            if (filter.allows(file)) {
                if (file.isDirectory()) {
                    File newDir = new File(fsDir.getAbsolutePath() + File.separator + file.getName());
                    newDir.mkdir();
                    copyImportContents(newDir, file, filter);
                } else {
                    InputStream ins = null;
                    try {
                        ins = file.getInputStream();
                        writeFile(file.getInputStream(), fsDir.getAbsolutePath() + File.separator + file.getName());
                    } finally {
                        if(ins!=null){
                            ins.close();;
                        }
                    }
                }
            }
        }
    }

    public static void copyExportContents(final PsiDirectory exportDir, final File exportBaseDir, final String path)
            throws IntelliVaultException {
        final File copyRootDir = new File(exportBaseDir.getAbsolutePath() + File.separator
                + IntelliVaultConstants.JCR_ROOT
                + path.replace(IntelliVaultConstants.JCR_PATH_SEPERATOR, File.separator));
        if (!copyRootDir.exists()) {
            throw new IntelliVaultException("Failed copying contents.");
        }

        try {
            copyExportContents(exportDir, copyRootDir);
        } catch (IOException e) {
            throw new IntelliVaultException("Error Copying Exported contents to IDEA.", e);
        }
        exportDir.getVirtualFile().refresh(false, true);
    }

    /**
     * Delete a directory and all of it's contents in a depth-first recursive manner.
     * @param f the directory to delete
     * @throws IOException if an error occurs
     */
    public static void deleteDirectoryRecursive(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteDirectoryRecursive(c);
            }
        }

        //once we have deleted all of a directories contents, we delete the directory
        delete(f);

    }

    /**
     * Delete a file.  This method will retry the delete up to 5 times, sleeping for one second between retries.
     * This is because sometimes, especially with smaller imports, the cleanup starts happening before vault has closed
     * all of it's file locks.  The retry behavior makes a best effort to properly cleanup when that happens.
     * @param f the file to delete
     * @throws IOException if the file can't be deleted.
     */
    private static void delete(File f) throws IOException {
        boolean isDeleted = f.delete();
        for(int i=0;i<4 && !isDeleted;i++){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new IOException("Failed to delete file: " + f, e);
            }
            isDeleted = f.delete();
        }

        if(!isDeleted) {
            throw new IOException("Failed to delete file: " + f);
        }
    }

    private static void copyExportContents(final PsiDirectory exportDir, File copyRootDir) throws IOException {
        File[] contents = copyRootDir.listFiles();
        FileComparator comparator = new FileComparator(IGNORE_WHITESPACE);
        for (int i = 0; i < contents.length; i++) {
            final File f = contents[i];
            if (f.isDirectory()) {
                PsiDirectory subdir = exportDir.findSubdirectory(f.getName());

                if (subdir == null) {
                    subdir = ApplicationManager.getApplication().runWriteAction(new Computable<PsiDirectory>() {
                        @Override
                        public PsiDirectory compute() {
                            return exportDir.createSubdirectory(f.getName());
                        }
                    });
                }
                copyExportContents(subdir, f);
            } else {
                copyFile(exportDir, f, comparator);
            }
        }
    }

    private static void copyFile(final PsiDirectory exportDir, final File f, FileComparator comparator) throws IOException {
        if (f.getName().equals(".vlt")) {
            return;
        }

        PsiFile file = exportDir.findFile(f.getName());

        if (file == null) {
            file = ApplicationManager.getApplication().runWriteAction(new Computable<PsiFile>() {
                @Override
                public PsiFile compute() {
                    return exportDir.createFile(f.getName());
                }
            });
        }

        if(!comparator.areEqual(f,file.getVirtualFile())) {
            copyFileContents(file, f);
        }

    }

    private static void copyFileContents(final PsiFile file, final File f) throws IOException {
        final VirtualFile vf = file.getVirtualFile();

        ApplicationManager.getApplication().runWriteAction(new ThrowableComputable<Void, IOException>() {
            @Override
            public Void compute() throws IOException {
                OutputStream out = null;
                InputStream in = null;
                try {
                    out = vf.getOutputStream(ApplicationManager.getApplication());
                    in = new FileInputStream(f);
                    write(in, out);
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }

                    if (in != null) {
                        in.close();
                    }
                }
                return null;
            }
        });

    }

    public static void writeFile(String inputResourcePath, String filePath) throws IOException {
        InputStream ins = null;
        try {
            ins = FileUtils.class.getClassLoader().getResourceAsStream(inputResourcePath);
            writeFile(ins, filePath);
        } finally {
            if (ins != null) {
                ins.close();
            }
        }
    }

    public static void writeFile(InputStream inputStream, String filePath) throws IOException {
        File f = new File(filePath);
        OutputStream os = null;
        try {
            os = new FileOutputStream(f);
            write(inputStream, os);
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }

    private static void write(InputStream inputStream, OutputStream os) throws IOException {
        byte[] buf = new byte[BUFFER_LENGTH];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
    }

}
