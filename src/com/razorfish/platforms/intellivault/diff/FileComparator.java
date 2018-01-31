package com.razorfish.platforms.intellivault.diff;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;

/**
 * Compares files to determine if the files are actually different or if the
 * differences are due to whitespace only.
 * 
 */
public class FileComparator {

    protected boolean ignoreWhitespace;

    /**
     * Constructs a new FileComparitor.
     * 
     * @param ignoreWhitespace
     *            if true ignore files where the only difference is whitepsace,
     *            if false do a binary check
     */
    public FileComparator(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
    }

    /**
     * Checks to see if the files are equal. If set to ignore whitespace, files
     * with only whitespace differences are considered equal.
     * 
     * @param f1
     *            the first file to compare
     * @param f2
     *            the second file to compare
     * @return true if the files are equal, false otherwise
     * @throws IOException
     */
    public boolean areEqual(File f1, VirtualFile f2) throws IOException {
        if ((f1.exists() && !f2.exists()) || (f2.exists() && !f1.exists())) {
            return false;
        }

        if (!ignoreWhitespace && f1.length() != f2.getLength()) {
            return false;
        }

        InputStream f1Is = null;
        InputStream f2Is = null;

        try {
            f1Is = new BufferedInputStream(new FileInputStream(f1));
            f2Is = f2.getInputStream();
            int f1IsByte = f1Is.read();
            while (f1IsByte != -1) {
                int f2IsByte = f2Is.read();
                if (f1IsByte != f2IsByte) {
                    if (ignoreWhitespace) {
                        char f1IsChar = (char) f1IsByte;

                        while (Character.isWhitespace(f1IsChar)) {
                            f1IsChar = (char) f1Is.read();
                        }

                        char f2IsChar = (char) f2IsByte;
                        while (Character.isWhitespace(f2IsChar)) {
                            f2IsChar = (char) f2Is.read();
                        }

                        if (f1IsChar != f2IsChar) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                f1IsByte = f1Is.read();
            }

            return true;
        } finally {
            if (f1Is != null) {
                f1Is.close();
            }

            if (f2Is != null) {
                f2Is.close();
            }

        }
    }
}
