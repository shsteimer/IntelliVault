package com.razorfish.platforms.intellivault.config;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/15/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntelliVaultConfigDefaults {


    /**
     * Private default constructor to prevent creation of instances of this class.
     */
    private IntelliVaultConfigDefaults() {
        //no instances
    }

    public static final String REPO_NAME = "new repository";
    public static final String REPO_URL = "http://localhost:4502";
    public static final String REPO_USER = "admin";
    public static final String REPO_PASSWORD = "admin";

    public static final String ROOT_FOLDER = "jcr_root";
    public static final String IGNORE_PATTERNS = ".svn,.vlt,CVS,.DS_Store";

    public static final boolean VERBOSE = true;
    public static final boolean CONSOLE_LOG = false;
    public static final boolean DEBUG = false;

    public static final boolean SHOW_MESSAGE_DIALOG = true;

    public static final String VAULT_PATH = "";
}
