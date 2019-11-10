/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import apps.AppsBase;
import apps.CreateButtonModel;
import apps.gui3.tabbedpreferences.TabbedPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.ShutDownManager;
import jmri.implementation.AbstractShutDownTask;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrit.display.layoutEditor.BlockValueFile;
import jmri.jmrit.revhistory.FileHistory;
import jmri.managers.DefaultShutDownManager;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import jmri.util.FileUtilSupport;
import jmri.util.Log4JUtil;
import jmri.util.ThreadingUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sdedic
 */
public class AppsBaseBridge implements Runnable {
    private final static String CONFIG_FILENAME = System.getProperty("org.jmri.Apps.configFilename", "/JmriConfig3.xml");
    private static final RequestProcessor INIT_RP = new RequestProcessor(AppsBaseBridge.class);
    protected boolean configOK;
    protected boolean configDeferredLoadOK;
    protected boolean preferenceFileExists;
    static boolean preInit = false;
    private final static Logger log = LoggerFactory.getLogger(AppsBase.class);
    
    private static final AppsBaseBridge INSTANCE = new AppsBaseBridge();
    
    private RequestProcessor.Task initTask;
    
    private volatile boolean loaded;
    
    public static AppsBaseBridge getInstance() {
        return INSTANCE;
    }
    
    private AppsBaseBridge() {
        initTask = INIT_RP.post(() -> initJMRI());
    }
    
    public boolean isJMRILoaded() {
        return loaded;
    }
    
    public void whenReady(Runnable exec, boolean inAWT) {
        initTask.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(Task task) {
                if (inAWT && !SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(exec);
                } else {
                    exec.run();
                }
            }
        });
    }

    /**
     * Initial actions before frame is created, invoked in the applications
     * main() routine.
     * <ul>
     * <li> Initialize logging
     * <li> Set application name
     * </ul>
     *
     * @param applicationName The application name as presented to the user
     */
    static public void preInit(String applicationName) {
        Log4JUtil.initLogging();

        try {
            Application.setApplicationName(applicationName);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            log.error("Unable to set application name", ex);
        }

        log.info(Log4JUtil.startupInfo(applicationName));

        preInit = true;
    }
    
    public void initJMRI() {
        // override shutdown manager with NetBeans-friendly version
        InstanceManager.store(new JmriDefaultShutDownManager(), ShutDownManager.class);
        
        File embeddedJMRIDir = InstalledFileLocator.getDefault().locate("jmri", "one.dedic.decodernext.main", false);
        FileUtilSupport.getDefault().setProgramPath(embeddedJMRIDir);

        /*
        if (!preInit) {
            preInit(applicationName);
            setConfigFilename(configFileDef, args);
        }

        */
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;

//        Log4JUtil.initLogging();
        
        System.setOut(origOut);
        System.setErr(origErr);

        // configureProfile();

        installConfigurationManager();

        addDefaultShutDownTasks();

        installManagers();

        setAndLoadPreferenceFile();

        FileUtil.logFilePaths();

        // all loaded, initialize objects as necessary
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        loaded = true;
        
        JmriServicesBridge br = Lookup.getDefault().lookup(JmriServicesBridge.class);
        br.run();
    }
    
    protected void installConfigurationManager() {
        ConfigureManager cm = new JmriConfigurationManager();
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        InstanceManager.store(cm, ConfigureManager.class);
        InstanceManager.setDefault(ConfigureManager.class, cm);
        log.debug("config manager installed");
    }

    protected void installManagers() {
        // record startup
        InstanceManager.getDefault(FileHistory.class).addOperation("app", Application.getApplicationName(), null);

        // install the abstract action model that allows items to be added to the, both
        // CreateButton and Perform Action Model use a common Abstract class
        InstanceManager.store(new CreateButtonModel(), CreateButtonModel.class);
    }

    /**
     * Invoked to load the preferences information, and in the process configure
     * the system. The high-level steps are:
     * <ul>
     * <li>Locate the preferences file based through
     * {@link FileUtil#getFile(String)}
     * <li>See if the preferences file exists, and handle it if it doesn't
     * <li>Obtain a {@link jmri.ConfigureManager} from the
     * {@link jmri.InstanceManager}
     * <li>Ask that ConfigureManager to load the file, in the process loading
     * information into existing and new managers.
     * <li>Do any deferred loads that are needed
     * <li>If needed, migrate older formats
     * </ul>
     * (There's additional handling for shared configurations)
     */
    protected void setAndLoadPreferenceFile() {
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        final File file;
        File sharedConfig = null;
        try {
            sharedConfig = FileUtil.getFile(FileUtil.PROFILE + Profile.SHARED_CONFIG);
            if (!sharedConfig.canRead()) {
                sharedConfig = null;
            }
        } catch (FileNotFoundException ex) {
            // ignore - this only means that sharedConfig does not exist.
        }
        if (sharedConfig != null) {
            file = sharedConfig;
        } else if (!new File(getConfigFileName()).isAbsolute()) {
            // must be relative, but we want it to
            // be relative to the preferences directory
            file = new File(FileUtil.getUserFilesPath() + getConfigFileName());
        } else {
            file = new File(getConfigFileName());
        }
        // don't try to load if doesn't exist, but mark as not OK
        if (!file.exists()) {
            preferenceFileExists = false;
            configOK = false;
            log.info("No pre-existing config file found, searched for '{}'", file.getPath());
            return;
        }
        preferenceFileExists = true;

        // ensure the UserPreferencesManager has loaded. Done on GUI
        // thread as it can modify GUI objects
        ThreadingUtil.runOnGUI(() -> {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        });

        // now (attempt to) load the config file
        try {
            ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cm != null) {
                configOK = cm.load(file);
            } else {
                configOK = false;
            }
            log.debug("end load config file {}, OK={}", file.getName(), configOK);
        } catch (JmriException e) {
            configOK = false;
        }

        if (sharedConfig != null) {
            // sharedConfigs do not need deferred loads
            configDeferredLoadOK = true;
        } else if (SwingUtilities.isEventDispatchThread()) {
            // To avoid possible locks, deferred load should be
            // performed on the Swing thread
            configDeferredLoadOK = doDeferredLoad(file);
        } else {
            try {
                // Use invokeAndWait method as we don't want to
                // return until deferred load is completed
                SwingUtilities.invokeAndWait(() -> {
                    configDeferredLoadOK = doDeferredLoad(file);
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                log.error("Exception creating system console frame:", ex);
            }
        }
        if (sharedConfig == null && configOK == true && configDeferredLoadOK == true) {
            log.info("Migrating preferences to new format...");
            // migrate preferences
            InstanceManager.getOptionalDefault(TabbedPreferences.class).ifPresent(tp -> {
                //tp.init();
                tp.saveContents();
                InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> {
                    cm.storePrefs();
                });
                // notify user of change
                log.info("Preferences have been migrated to new format.");
                log.info("New preferences format will be used after JMRI is restarted.");
            });
        }
    }

    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config file {}", file.getName());
        try {
            ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cm != null) {
                result = cm.loadDeferred(file);
            } else {
                log.error("Failed to get default configure manager");
                result = false;
            }
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration:", e);
            result = false;
        }
        log.debug("end deferred load from config file {}, OK={}", file.getName(), result);
        return result;
    }

    /**
     * @deprecated for removal since 4.17.2 without replacement
     */
    @Deprecated
    protected void installShutDownManager() {
        // nothing to do
    }

    protected void addDefaultShutDownTasks() {
        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.getDefault(jmri.ShutDownManager.class).
                register(new AbstractShutDownTask("Writing Blocks") {

                    @Override
                    public boolean execute() {
                        // Save block values prior to exit, if necessary
                        log.debug("Start writing block info");
                        try {
                            new BlockValueFile().writeBlockValues();
                        } //catch (org.jdom2.JDOMException jde) { log.error("Exception writing blocks: "+jde); }
                        catch (java.io.IOException ioe) {
                            log.error("Exception writing blocks:", ioe);
                        }

                        // continue shutdown
                        return true;
                    }
                });
    }

    /**
     * Final actions before releasing control of the application to the user,
     * invoked explicitly after object has been constructed in main().
     */
    protected void start() {
        log.debug("main initialization done");
    }

    /**
     * Set up the configuration file name at startup.
     * <p>
     * The Configuration File name variable holds the name used to load the
     * configuration file during later startup processing. Applications invoke
     * this method to handle the usual startup hierarchy:
     * <ul>
     * <li>If an absolute filename was provided on the command line, use it
     * <li>If a filename was provided that's not absolute, consider it to be in
     * the preferences directory
     * <li>If no filename provided, use a default name (that's application specific)
     * </ul>
     * This name will be used for reading and writing the preferences. It need
     * not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em>.
     *
     * @param def  Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String[] args) {
        // skip if org.jmri.Apps.configFilename is set
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            return;
        }
        // save the configuration filename if present on the command line
        if (args.length >= 1 && args[0] != null && !args[0].equals("") && !args[0].contains("=")) {
            def = args[0];
            log.debug("Config file was specified as: {}", args[0]);
        }
        for (String arg : args) {
            String[] split = arg.split("=", 2);
            if (split[0].equalsIgnoreCase("config")) {
                def = split[1];
                log.debug("Config file was specified as: {}", arg);
            }
        }
        if (def != null) {
            setJmriSystemProperty("configFilename", def);
            log.debug("Config file set to: {}", def);
        }
    }

    // We will use the value stored in the system property
    static public String getConfigFileName() {
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            return System.getProperty("org.jmri.Apps.configFilename");
        }
        return CONFIG_FILENAME;
    }

    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps." + key);
            if (current == null) {
                System.setProperty("org.jmri.Apps." + key, value);
            } else if (!current.equals(value)) {
                log.warn("JMRI property {} already set to {}, skipping reset to {}", key, current, value);
            }
        } catch (Exception e) {
            log.error("Unable to set JMRI property {} to {}due to exception: {}", key, value, e);
        }
    }

    /**
     * The application decided to quit, handle that.
     *
     * @return true if successfully ran all shutdown tasks and can quit; false
     *         otherwise
     */
    static public boolean handleQuit() {
        log.debug("Start handleQuit");
        try {
            return InstanceManager.getDefault(jmri.ShutDownManager.class).shutdown();
        } catch (Exception e) {
            log.error("Continuing after error in handleQuit", e);
        }
        return false;
    }

    /**
     * The application decided to restart, handle that.
     *
     * @return true if successfully ran all shutdown tasks and can quit; false
     *         otherwise
     */
    static public boolean handleRestart() {
        log.debug("Start handleRestart");
        try {
            return InstanceManager.getDefault(jmri.ShutDownManager.class).restart();
        } catch (Exception e) {
            log.error("Continuing after error in handleRestart", e);
        }
        return false;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
