package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.razorfish.platforms.intellivault.actions.VaultOperationDirectory;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultOperationConfig;
import com.razorfish.platforms.intellivault.exceptions.IntelliVaultException;
import com.razorfish.platforms.intellivault.filter.VaultImportFilter;
import com.razorfish.platforms.intellivault.services.IntelliVaultService;
import com.razorfish.platforms.intellivault.services.VaultInvokerService;
import com.razorfish.platforms.intellivault.utils.FileUtils;
import com.razorfish.platforms.intellivault.utils.IntelliVaultConstants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The IntelliVault service handles all of the logic around calling vault such as copying files, settting up vault
 * configurations, etc.
 */
public class IntelliVaultServiceImpl implements IntelliVaultService {

    public static final String CHECKOUT = "co";
    private static final String IMPORT = "import";
    private static final String EXPORT = "export";
    public static final String DEBUG = "debug";

    public static final String FILTER = "--filter";
    public static final String CREDENTIALS = "--credentials";
    public static final String VERBOSE = "--verbose";
    public static final String FORCE = "--force";
    public static final String LOG_LEVEL = "--log-level";

    public static final String WORKSPACE_ROOT_PATH = "/crx/server";
    public static final String CREDENTIALS_SEPERATOR = ":";
    public static final String NEW_LINE_CHAR = "\n";
    private static final List<String> TOP_LEVEL_JCR_PATHS = new ArrayList<String>() {
        {
            add("/");
            add("/apps");
            add("/libs");
            add("/etc");
            add("/home");
            add("/var");
            add("/bin");
            add("/tmp");
            add("/content");
        }
    };

    // private PrintStream sysOut;
    private OutputStream logOut;
    // private File logFile;
    private boolean isError;
    private String errorMsg;

    private static final Logger log = Logger.getInstance(IntelliVaultServiceImpl.class);

    @Override
    public void vaultExport(final IntelliVaultCRXRepository repository, final IntelliVaultOperationConfig opConf,
                            final VaultOperationDirectory exportOpDir, final ProgressIndicator progressIndicator, ConsoleView console)
            throws IntelliVaultException {
        progressIndicator.setText2("Preparing export");

        if (TOP_LEVEL_JCR_PATHS.contains(exportOpDir.getJcrPath())) {
            throw new IntelliVaultException("Cannot export top level directory " + exportOpDir.getJcrPath() +
                    ".  Please select a valid sub-path.");
        }

        isError = false;
        errorMsg = null;

        final File exportBaseDir = FileUtils.createTempDirectory(opConf.getTempDirectory());

        try {
            final List<String> jcrPaths = new ArrayList<String>();
            jcrPaths.add(exportOpDir.getJcrPath());
            File filterFile = createFilterFile(exportBaseDir, jcrPaths);

            progressIndicator.setText2("Running VLT Export");
            final String[] args = prepareExportArgsList(repository, opConf, exportBaseDir, filterFile);

            progressIndicator.startNonCancelableSection();
            invokeVault(opConf, args, console);
            progressIndicator.finishNonCancelableSection();

            progressIndicator.setText2("Copying export contents to IDEA");

            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.copyExportContents(exportOpDir.getPsiDir(), exportBaseDir, exportOpDir.getJcrPath());
                    } catch (IntelliVaultException e) {
                        // TODO work on this
                        log.error("Error copying contents.", e);
                        Messages.showErrorDialog(e.getLocalizedMessage(), "IntelliVault Error!");
                    }
                }
            }, ModalityState.any());

        } finally {
            if (!opConf.isDebug()) {
                try {
                    FileUtils.deleteDirectoryRecursive(exportBaseDir);
                } catch (IOException e) {
                    throw new IntelliVaultException("Error while deleting temp contents.", e);
                }
            }
        }

    }

    @Override
    public void vaultImport(final IntelliVaultCRXRepository repository, final IntelliVaultOperationConfig opConf,
                            final VaultOperationDirectory importOpDir, ProgressIndicator progressIndicator, ConsoleView console)
            throws IntelliVaultException {
        progressIndicator.setText2("Preparing import");

        if (TOP_LEVEL_JCR_PATHS.contains(importOpDir.getJcrPath())) {
            throw new IntelliVaultException("Cannot import top level directory " + importOpDir.getJcrPath() +
                    ".  Please select a valid sub-path.");
        }

        isError = false;
        errorMsg = null;

        final File importBaseDir = FileUtils.createTempDirectory(opConf.getTempDirectory());

        try {
            final List<String> jcrPaths = new ArrayList<String>();
            jcrPaths.add(importOpDir.getJcrPath());
            File filterFile = createFilterFile(importBaseDir, jcrPaths);

            try {
                FileUtils.writeFile("com/razorfish/platforms/intellivault/settings.xml", filterFile.getParentFile()
                        .getAbsoluteFile() + File.separator + "settings.xml");
                FileUtils.writeFile("com/razorfish/platforms/intellivault/config.xml", filterFile.getParentFile()
                        .getAbsoluteFile() + File.separator + "config.xml");
            } catch (IOException e) {
                throw new IntelliVaultException("Error creating import config files.", e);
            }

            progressIndicator.setText2("Copying import contents to Temp Directory");
            FileUtils.copyImportContents(importBaseDir, importOpDir.getPsiDir(), importOpDir.getJcrPath(),
                    new VaultImportFilter(opConf.getFileIgnorePatterns()));

            progressIndicator.setText2("Running VLT Import");
            String[] args = prepareImportArgsList(repository, opConf, importBaseDir);

            progressIndicator.startNonCancelableSection();
            invokeVault(opConf, args, console);
            progressIndicator.finishNonCancelableSection();
        } finally {
            if (!opConf.isDebug()) {
                try {
                    FileUtils.deleteDirectoryRecursive(importBaseDir);
                } catch (IOException e) {
                    throw new IntelliVaultException("Error while deleting temp contents.", e);
                }
            }
        }

    }

    private void invokeVault(final IntelliVaultOperationConfig opConf, final String[] args, ConsoleView console)
            throws IntelliVaultException {
        PrintStream sysOut = null;
        try {
            sysOut = redirectSysOut(console);
        } catch (IOException e) {
            log.error("Error redirecting sysout, ignore.", e);
            // do nothing, just let it go to sys out
        }

        VaultInvokerService vlt = ServiceManager.getService(VaultInvokerService.class);
        vlt.invokeVault(opConf.getVaultPath(), args);

        try {
            restoreSysOut(sysOut);
        } catch (IOException e) {
            log.error("Error restoring sysout, ignore.", e);
            // do nothing, just let it go to sys out
        }

        if (isError) {
            throw new IntelliVaultException(errorMsg);
        }
    }

    private String[] prepareImportArgsList(final IntelliVaultCRXRepository repository,
            final IntelliVaultOperationConfig opConf, final File importBaseDir) {
        List<String> argsList = new ArrayList<String>();

        if (opConf.isDebug()) {
            argsList.add(LOG_LEVEL);
            argsList.add(DEBUG);
        }

        argsList.add(IMPORT);

        argsList.add(repository.getRepoUrl());

        argsList.add(importBaseDir.getAbsolutePath());

        argsList.add(IntelliVaultConstants.JCR_PATH_SEPERATOR);

        argsList.add(CREDENTIALS);
        argsList.add(repository.getUsername() + CREDENTIALS_SEPERATOR + repository.getPassword());

        if (opConf.isVerbose()) {
            argsList.add(VERBOSE);
        }

        return argsList.toArray(new String[argsList.size()]);
    }

    private String[] prepareExportArgsList(final IntelliVaultCRXRepository repository,
            final IntelliVaultOperationConfig opConf, final File exportBaseDir, final File filterFile) {
        List<String> argsList = new ArrayList<String>();

        if (opConf.isDebug()) {
            argsList.add(LOG_LEVEL);
            argsList.add(DEBUG);
        }

        argsList.add(CHECKOUT);

        argsList.add(FILTER);
        argsList.add(filterFile.getAbsolutePath());

        argsList.add(repository.getRepoUrl() + WORKSPACE_ROOT_PATH);

        argsList.add(exportBaseDir.getAbsolutePath());

        argsList.add(CREDENTIALS);
        argsList.add(repository.getUsername() + CREDENTIALS_SEPERATOR + repository.getPassword());

        if (opConf.isVerbose()) {
            argsList.add(VERBOSE);
        }

        return argsList.toArray(new String[argsList.size()]);
    }

    /**
     * Create filter.xml file and populate it's content from the list of jcr paths handled by this operation.
     *
     * @param baseDir the base directory for the vault operation (export or import)
     * @param paths   the List of jcr paths to be handled by the operation
     * @return File representing the filter.xml that was created
     * @throws IntelliVaultException if an error occurs preventing creation of the file
     */
    private File createFilterFile(final File baseDir, final List<String> paths) throws IntelliVaultException {
        File filterFile = null;
        FileOutputStream os = null;
        try {
            filterFile = createFilterFile(baseDir);
            os = new FileOutputStream(filterFile);
            os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<workspaceFilter version=\"1.0\">\n")
                    .getBytes("UTF-8"));
            for (String path : paths) {
                os.write(("\t<filter root=\"" + path + "\"/>\n").getBytes("UTF-8"));
            }

            os.write(("</workspaceFilter>").getBytes());
        } catch (IOException e) {
            throw new IntelliVaultException(e);
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    throw new IntelliVaultException(e);
                }
            }
        }

        return filterFile;
    }

    /**
     * Create an empty filter.xml file.
     *
     * @param baseDir the base directory for the vault operation (export or import)
     * @return File representing the filter.xml that was created
     * @throws IOException if an IOError occurs preventing creation of the file
     */
    private File createFilterFile(File baseDir) throws IOException {
        File filterConfigDir =
                new File(baseDir.getAbsolutePath() + File.separator + "META-INF" + File.separator + "vault");
        filterConfigDir.mkdirs();

        File filterFile = new File(filterConfigDir.getAbsolutePath() + File.separator + "filter.xml");

        if (!filterFile.exists()) {
            filterFile.createNewFile();
        }

        return filterFile;
    }

    /**
     * Redirect the system output for vault to another, custom output stream. Also starts a thread to read that stream
     * to identify underlying vault errors.
     *
     * @param console the console to log messages to
     * @return PrintStream that formerly was System.out, so that it can be restored later
     * @throws IOException if an error occurs
     */
    private PrintStream redirectSysOut(final ConsoleView console) throws IOException {
        PrintStream sysOut = System.out;

        final PipedInputStream in = new PipedInputStream();
        // TODO close logOut
        logOut = new PipedOutputStream(in);

        System.setOut(new PrintStream(logOut));

        final Thread writerThread = new Thread() {
            public void run() {
                log.debug("Starting input listener");
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        if (line.contains("[ERROR]") ||
                                (isError && (line.contains("caused by") || line.contains("at")))) {
                            isError = true;
                            errorMsg += NEW_LINE_CHAR + line;

                            console.print(line + NEW_LINE_CHAR, ConsoleViewContentType.ERROR_OUTPUT);
                            log.error(line);
                        } else {
                            console.print(line + NEW_LINE_CHAR, ConsoleViewContentType.NORMAL_OUTPUT);
                            log.info(line);
                        }

                    }
                } catch (IOException e) {
                    log.error("Exception reading output stream", e);
                }
                log.debug("Input reader closing");
            }

        };
        writerThread.start();

        return sysOut;
    }

    /**
     * Restore the system output stream
     *
     * @param sysOut the output stream to restore to System.out
     * @throws IOException if an error occurs while restoring
     */
    private void restoreSysOut(PrintStream sysOut) throws IOException {
        System.setOut(sysOut);

        logOut.flush();
        logOut.close();
    }
}
