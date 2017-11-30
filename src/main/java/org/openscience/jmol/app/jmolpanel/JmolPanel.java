/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2009-06-26 23:35:44 -0500 (Fri, 26 Jun 2009) $
 * $Revision: 11131 $
 *
 * Copyright (C) 2000-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.jmol.app.jmolpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.NumberFormatter;

import org.broad.igv.feature.Chromosome;
import org.jmol.api.Interface;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.api.ScaleEventListener;//added -hcf
import org.jmol.console.JmolConsole;
import org.jmol.console.KeyJMenuItem;
import org.jmol.export.JmolFileDropper;
import org.jmol.export.dialog.Dialog;
import org.jmol.export.history.HistoryFile;
import org.jmol.export.image.ImageCreator;
import org.jmol.i18n.GT;
import org.jmol.util.Logger;
import org.jmol.util.Parser;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.Jmol;
import org.openscience.jmol.app.JmolApp;
import org.openscience.jmol.app.SplashInterface;
import org.openscience.jmol.app.jsonkiosk.BannerFrame;
import org.openscience.jmol.app.jsonkiosk.JsonNioClient;
import org.openscience.jmol.app.jsonkiosk.JsonNioServer;
import org.openscience.jmol.app.jsonkiosk.KioskFrame;
import org.openscience.jmol.app.surfacetool.SurfaceTool;
import org.openscience.jmol.app.webexport.WebExport;

import edu.missouri.chenglab.ClusterTAD.Parameter;
import edu.missouri.chenglab.Heatmap.LoadHeatmap;
import edu.missouri.chenglab.gmol.Constants;
import edu.missouri.chenglab.hicdata.PreProcessingHiC;
import edu.missouri.chenglab.hicdata.ReadHiCData;
import edu.missouri.chenglab.loopdetection.utility.CommonFunctions;
import edu.missouri.chenglab.struct3DMax.Input;
import edu.missouri.chenglab.swingutilities.ConvertToHiCWorker;
import edu.missouri.chenglab.swingutilities.ExtractHiCWorker;
import edu.missouri.chenglab.swingutilities.NormalizeHiCWorker;
import edu.missouri.chenglab.swingutilities.ReadHiCHeaderWorker;
import juicebox.HiC;
import juicebox.data.Dataset;
import juicebox.data.HiCFileTools;
import juicebox.tools.utils.original.Preprocessor;
import juicebox.windowui.HiCZoom;
import juicebox.windowui.MatrixType;
import juicebox.windowui.NormalizationType;

//added -hcf

/*import juicebox.data.Dataset;
import juicebox.data.HiCFileTools;
import juicebox.tools.clt.old.Dump;
*/


public class JmolPanel extends JPanel implements SplashInterface, JsonNioClient {

  static HistoryFile historyFile;

  public JmolViewer viewer;

  JmolAdapter modelAdapter;
  JmolApp jmolApp;
  StatusBar status;
  int startupWidth, startupHeight;
  JsonNioServer serverService;

  protected String appletContext;
  protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  protected DisplayPanel display;
  protected GaussianDialog gaussianDialog;
  protected RecentFilesDialog recentFiles;
  protected AtomSetChooser atomSetChooser;
  protected JFrame frame;
  protected SplashInterface splash;
  protected JFrame consoleframe;  
  protected JsonNioServer service;
  protected int qualityJPG = -1;
  protected int qualityPNG = -1;
  protected String imageType;

  protected GuiMap guimap = new GuiMap();
  private ExecuteScriptAction executeScriptAction;
  private PreferencesDialog preferencesDialog;
  private StatusListener myStatusListener;
  private SurfaceTool surfaceTool;
  private Map<String, Action> commands;
  private Map<String, JMenuItem> menuItems;
  private JMenuBar menubar;
  protected JToolBar toolbar;//privat->protected changed -hcf

  // --- action implementations -----------------------------------
  private ConvertPDB2GSSAction pdb2GSSAction = new ConvertPDB2GSSAction(); //Tuan added
  private LorDG3DModeller lorDGModellerAction = new LorDG3DModeller(); //Tuan added
  private LoopDetectorAction loopDetectAction = new LoopDetectorAction(); //Tuan added
  private AnnotationAction annotationAction = new AnnotationAction(); //Tuan added
  private ExtractHiCAction extractHiCAction = new ExtractHiCAction(); //Tuan added
  private ConvertToHiCAction convertToHiCAction = new ConvertToHiCAction(); //Tuan added
  private NormalizeHiCAction normalizeHiCAction = new NormalizeHiCAction(); //Tuan added
  
  private Structure_3DMaxModeller structure3DMaxAction = new Structure_3DMaxModeller(); //Tosin added
  private HeatmapVisualizeAction heatmap2DvisualizeAction = new HeatmapVisualizeAction(); //Tosin added
  private FindTADAction findTADAction = new FindTADAction(); //Tosin added
  
  private ExportAction exportAction = new ExportAction();
  private PovrayAction povrayAction = new PovrayAction();
  private extractPDBAction extractPDBAction = new extractPDBAction();//added -hcf
  private ToWebAction toWebAction = new ToWebAction();
  private WriteAction writeAction = new WriteAction();
  private PrintAction printAction = new PrintAction();
  private CopyImageAction copyImageAction = new CopyImageAction();
  private CopyScriptAction copyScriptAction = new CopyScriptAction();
  private SurfaceToolAction surfaceToolAction = new SurfaceToolAction();
  private PasteClipboardAction pasteClipboardAction = new PasteClipboardAction();
  private ViewMeasurementTableAction viewMeasurementTableAction = new ViewMeasurementTableAction();
  
  //added -hcf
  private searchGenomeSequenceTableAction searchGenomeSequenceTableAction = new searchGenomeSequenceTableAction();
  //added end -hcf
    
  private Map<String, Object> viewerOptions;

  private static int numWindows = 0;
  private static KioskFrame kioskFrame;
  private static BannerFrame bannerFrame;
  
  // Window names for the history file
  private final static String EDITOR_WINDOW_NAME = "ScriptEditor";
  private final static String SCRIPT_WINDOW_NAME = "ScriptWindow";
  private final static String FILE_OPEN_WINDOW_NAME = "FileOpen";
  private final static String WEB_MAKER_WINDOW_NAME = "JmolWebPageMaker";
  private final static String SURFACETOOL_WINDOW_NAME = "SurfaceToolWindow";
  private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  
  // these correlate with items xxx in GuiMap.java 
  // that have no associated xxxScript property listed
  // in org.openscience.jmol.Properties.Jmol-resources.properties
  private static final String convertPDB2GSSAction = "PDB2GSS";//Tuan added
  private static final String lorDG3DModellerAction = "LorDG";//Tuan added
  private static final String loopDetectorAction = "LoopDetector";//Tuan added
  private static final String annotateAction = "Annotate";//Tuan added
  private static final String extractHiC = "ExtractHiC";//Tuan added

  private static final String converToHiC = "ConvertToHiC";//Tuan added
  private static final String normalizeHiC = "NormalizeHiC";//Tuan added

  private static final String structure3DMAXAction = "3DMax";//Tosin added
  private static final String heatmap2DVisualizeAction = "Visualize"; //Tosin added
  private static final String findTadAction = "Find-TAD"; //Tosin added
  

  
  private static final String newwinAction = "newwin";
  private static final String openAction = "open";
  private static final String scaleDownAction = "scaleDown";//added -hcf
  private static final String scaleUpAction = "scaleUp";//added -hcf
  private static final String extractPDBProperty = "extractPDB";//added -hcf
  private static final String openurlAction = "openurl";
  private static final String openpdbAction = "openpdb";
  private static final String openmolAction = "openmol";
  private static final String newAction = "new";
  private static final String exportActionProperty = "export";
  private static final String closeAction = "close";
  private static final String exitAction = "exit";
  private static final String aboutAction = "about";
  private static final String whatsnewAction = "whatsnew";
  private static final String uguideAction = "uguide";
  private static final String printActionProperty = "print";
  private static final String recentFilesAction = "recentFiles";
  private static final String povrayActionProperty = "povray";
  private static final String writeActionProperty = "write";
  private static final String editorAction = "editor";
  private static final String consoleAction = "console";
  private static final String toWebActionProperty = "toweb";
  private static final String atomsetchooserAction = "atomsetchooser";
  private static final String copyImageActionProperty = "copyImage";
  private static final String copyScriptActionProperty = "copyScript";
  private static final String surfaceToolActionProperty = "surfaceTool";  private static final String pasteClipboardActionProperty = "pasteClipboard";
  private static final String gaussianAction = "gauss";
  private static final String resizeAction = "resize";
  //private static final String saveasAction = "saveas";
  //private static final String vibAction = "vibrate";

  public JmolPanel(JmolApp jmolApp, Splash splash, JFrame frame,
      JmolPanel parent, int startupWidth, int startupHeight,
      Map<String, Object> viewerOptions, Point loc) {
    super(true);
    this.jmolApp = jmolApp;
    this.frame = frame;
    this.startupWidth = startupWidth;
    this.startupHeight = startupHeight;
    historyFile = jmolApp.historyFile;
    numWindows++;

    try {
      say("history file is " + historyFile.getFile().getAbsolutePath());
    } catch (Exception e) {
    }

    frame.setTitle("Gmol"); //modified lxq35
    frame.getContentPane().setBackground(Color.lightGray);
    frame.getContentPane().setLayout(new BorderLayout());

    this.splash = splash;

    setBorder(BorderFactory.createEtchedBorder());
    setLayout(new BorderLayout());

    status = new StatusBar();
    say(GT._("Initializing 3D display..."));

    // only the SmarterJmolAdapter is allowed -- just send it in as null

    /*
     * String adapter = System.getProperty("model"); if (adapter == null ||
     * adapter.length() == 0) adapter = "smarter"; if
     * (adapter.equals("smarter")) { report("using Smarter Model Adapter");
     * modelAdapter = new SmarterJmolAdapter(); } else if
     * (adapter.equals("cdk")) {report(
     * "the CDK Model Adapter is currently no longer supported. Check out http://bioclipse.net/. -- using Smarter"
     * ); // modelAdapter = new CdkJmolAdapter(null); modelAdapter = new
     * SmarterJmolAdapter(); } else { report("unrecognized model adapter:" +
     * adapter + " -- using Smarter"); modelAdapter = new SmarterJmolAdapter();
     * }
     */

    /*
     * this version of Jmol needs to have a display so that it can 
     * construct JPG images -- if that is not needed, then you can
     * use JmolData.jar
     * 
     */
    display = new DisplayPanel(this);
    viewerOptions.put("display", display);
    myStatusListener = new StatusListener(this, display);
    viewerOptions.put("statusListener", myStatusListener);
    if (modelAdapter != null)
      viewerOptions.put("modelAdapter", modelAdapter);
    this.viewerOptions = viewerOptions;
    viewer = new Viewer(viewerOptions);

    viewer.setEventListener(new scaleButtonListener());
    display.setViewer(viewer);
    myStatusListener.setViewer(viewer);

    if (!jmolApp.haveDisplay)
      return;
    getDialogs();
    say(GT._("Initializing Script Window..."));
    viewer.getProperty("DATA_API", "getAppConsole", Boolean.TRUE);

    // Setup Plugin system
    // say(GT._("Loading plugins..."));
    // pluginManager = new CDKPluginManager(
    // System.getProperty("user.home") + System.getProperty("file.separator")
    // + ".jmol", new JmolEditBus(viewer)
    // );
    // pluginManager.loadPlugin("org.openscience.cdkplugin.dirbrowser.DirBrowserPlugin");
    // pluginManager.loadPlugin("org.openscience.cdkplugin.dirbrowser.DadmlBrowserPlugin");
    // pluginManager.loadPlugins(
    // System.getProperty("user.home") + System.getProperty("file.separator")
    // + ".jmol/plugins"
    // );
    // feature to allow for globally installed plugins
    // if (System.getProperty("plugin.dir") != null) {
    // pluginManager.loadPlugins(System.getProperty("plugin.dir"));
    // }

    // install the command table
    say(GT._("Building Command Hooks..."));
    commands = new Hashtable<String, Action>();
    if (display != null) {
      Action[] actions = getActions();
      for (int i = 0; i < actions.length; i++) {
        Action a = actions[i];
        commands.put(a.getValue(Action.NAME).toString(), a);
      }
    }

    if (jmolApp.isKiosk) {
      add("Center", display);
    } else {
      JPanel panel = new JPanel();
      menuItems = new Hashtable<String, JMenuItem>();
      say(GT._("Building Menubar..."));
      executeScriptAction = new ExecuteScriptAction();
      menubar = createMenubar();
      add("North", menubar);
      panel.setLayout(new BorderLayout());
      toolbar = createToolbar();
      panel.add("North", toolbar);
      JPanel ip = new JPanel();
      ip.setLayout(new BorderLayout());
      ip.add("Center", display);
      panel.add("Center", ip);
      add("Center", panel);
      add("South", status);
    }

    say(GT._("Starting display..."));
    display.start();

    if (jmolApp.isKiosk) {
      bannerFrame = new BannerFrame(jmolApp.startupWidth, 75);
    } else {
      // prevent new Jmol from covering old Jmol
      if (loc != null) {
        frame.setLocation(loc);
      } else if (parent == null) {
        loc = historyFile.getWindowPosition("Jmol");
        if (loc != null)
          frame.setLocation(loc);
      } else {
        loc = parent.frame.getLocationOnScreen();
        int maxX = screenSize.width - 50;
        int maxY = screenSize.height - 50;
        loc.x += 40;
        loc.y += 40;
        if (loc.x > maxX || loc.y > maxY)
          loc.setLocation(0, 0);
        frame.setLocation(loc);
      }
    }
    frame.getContentPane().add("Center", this);
    frame.addWindowListener(new AppCloser());
    frame.pack();
    frame.setSize(startupWidth, startupHeight);
    ImageIcon jmolIcon = JmolResourceHandler.getIconX("icon");
    Image iconImage = jmolIcon.getImage();
    frame.setIconImage(iconImage);

    // Repositioning windows

    //historyFile.repositionWindow("Jmol", getFrame(), 300, 300);

    AppConsole console = (AppConsole) viewer.getProperty("DATA_API",
        "getAppConsole", null);
    if (console != null && console.jcd != null) {
      historyFile.repositionWindow(SCRIPT_WINDOW_NAME, console.jcd, 200, 100,
          !jmolApp.isKiosk);
    }
    // this just causes problems
    //c = (Component) viewer.getProperty("DATA_API","getScriptEditor", null);
    //if (c != null)
    //historyFile.repositionWindow(EDITOR_WINDOW_NAME, c, 150, 50);

    say(GT._("Setting up Drag-and-Drop..."));
    new JmolFileDropper(myStatusListener, viewer);
    // it's important to set this up first, even though it consumes some memory
    // otherwise, loading a new model in a script that sets the vibration or vector parameters
    // can appear to skip those -- they aren't skipped, but creating the atomSetChooser
    // will run scripts as it loads.
    atomSetChooser = new AtomSetChooser(viewer, frame);
    pcs.addPropertyChangeListener(chemFileProperty, atomSetChooser);
    say(GT._("Launching main frame..."));
  }

  private void getDialogs() {
    say(GT._("Initializing Preferences..."));
    preferencesDialog = new PreferencesDialog(this, frame, guimap, viewer);
    say(GT._("Initializing Recent Files..."));
    recentFiles = new RecentFilesDialog(frame);
    if (jmolApp.haveDisplay) {
      if (display.measurementTable != null)
        display.measurementTable.dispose();
      display.measurementTable = new MeasurementTable(viewer, frame);
      display.sequenceTable = new SequenceTable(viewer, frame);
    }
  }

  protected static void startJmol(JmolApp jmolApp) {

    Dialog.setupUIManager();

    JFrame jmolFrame;

    if (jmolApp.isKiosk) {
      if (jmolApp.startupWidth < 100 || jmolApp.startupHeight < 100) {
        jmolApp.startupWidth = screenSize.width;
        jmolApp.startupHeight = screenSize.height - 75;
      }
      jmolFrame = kioskFrame = new KioskFrame(0, 75, jmolApp.startupWidth,
          jmolApp.startupHeight, null);
    } else {
      jmolFrame = new JFrame();
    }

    // now pass these to viewer

    Jmol jmol = null;

    try {
      if (jmolApp.jmolPosition != null) {
        jmolFrame.setLocation(jmolApp.jmolPosition);
      }

      jmol = getJmol(jmolApp, jmolFrame);

      // scripts are read and files are loaded now
 //     jmolApp.startViewer(jmol.viewer, jmol.splash, false);

    } catch (Throwable t) {
      Logger.error("uncaught exception: " + t);
      t.printStackTrace();
    }

    if (jmolApp.haveConsole)
      getJavaConsole(jmol);

    if (jmolApp.isKiosk) {
      kioskFrame.setPanel(jmol);
      bannerFrame.setLabel("click below and type exitJmol[enter] to quit");
      jmol.viewer.script("set allowKeyStrokes;set zoomLarge false;");
    }
    if (jmolApp.port > 0) {
      try {
        jmol.service = getJsonNioServer();
        jmol.service.startService(jmolApp.port, jmol, jmol.viewer, "-1", 1);
//        JsonNioService service2 = new JsonNioService();
//        service2.startService(jmolApp.port, jmol, null, "-2");
//        service2.sendMessage(null, "test", null);
      } catch (Throwable e) {
        e.printStackTrace();
        if (bannerFrame != null) {
          bannerFrame.setLabel("could not start NIO service on port " + jmolApp.port);
        }
        if (jmol.service != null)
          jmol.service.close();
      }

    }
  }

  private static void getJavaConsole(Jmol jmol) {
    // Adding console frame to grab System.out & System.err
    jmol.consoleframe = new JFrame(GT._("Java Console"));
    jmol.consoleframe.setIconImage(jmol.frame.getIconImage());
    try {
      final ConsoleTextArea consoleTextArea = new ConsoleTextArea(true);
      consoleTextArea.setFont(java.awt.Font.decode("monospaced"));
      jmol.consoleframe.getContentPane().add(new JScrollPane(consoleTextArea),
          java.awt.BorderLayout.CENTER);
        JButton buttonClear = jmol.guimap.newJButton("JavaConsole.Clear");
        buttonClear.addActionListener(new ActionListener() {
        @Override
		public void actionPerformed(ActionEvent e) {
          consoleTextArea.setText("");
        }
        });
        jmol.consoleframe.getContentPane().add(buttonClear,
            java.awt.BorderLayout.SOUTH);
    } catch (IOException e) {
      JTextArea errorTextArea = new JTextArea();
      errorTextArea.setFont(java.awt.Font.decode("monospaced"));
      jmol.consoleframe.getContentPane().add(new JScrollPane(errorTextArea),
          java.awt.BorderLayout.CENTER);
      errorTextArea.append(GT._("Could not create ConsoleTextArea: ") + e);
    }

    
    Point location = jmol.frame.getLocation();
    Dimension size = jmol.frame.getSize();
 
    // String name = CONSOLE_WINDOW_NAME;     

    //Dimension consoleSize = historyFile.getWindowSize(name);
    //Point consolePosition = historyFile.getWindowPosition(name);
    //if (consoleSize != null && consolePosition != null) {
    //  location = consolePosition;
    //  size = consoleSize;
    //} else {
      location.y += size.height;
      size.height = 200;
    //}
    if (size.height < 200 || size.height > 800)
      size.height = 200;
    if (size.width < 300 || size.width > 800)
      size.width = 300;
    if (location.y < 0 || location.y + size.height > screenSize.height)
      location.y = screenSize.height - size.height;
    if (location.x < 0 || location.x + size.width > screenSize.width)
      location.x = 0;
    jmol.consoleframe.setBounds(location.x, location.y, size.width, size.height);

    //Boolean consoleVisible = historyFile.getWindowVisibility(name);
    //if ((consoleVisible != null) && (consoleVisible.equals(Boolean.TRUE))) {
      //jmol.consoleframe.setVisible(true);
   // }

  }

  public static Jmol getJmol(JmolApp jmolApp, JFrame frame) {

    Splash splash = null;
    if (jmolApp.haveDisplay && jmolApp.splashEnabled) {
      ImageIcon splash_image = JmolResourceHandler.getIconX("splash");
      if (!jmolApp.isSilent)
        Logger.info("splash_image=" + splash_image);
      splash = new Splash(frame, splash_image);
      splash.setCursor(new Cursor(Cursor.WAIT_CURSOR));
      splash.showStatus(GT._("Creating main window..."));
      splash.showStatus(GT._("Initializing Swing..."));
    }
    if (jmolApp.haveDisplay)
      try {
        //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception exc) {
        System.err.println("Error loading L&F: " + exc);
      }

    if (splash != null)
      splash.showStatus(GT._("Initializing GMOL..."));

    Jmol window = new Jmol(jmolApp, splash, frame, null, jmolApp.startupWidth,
        jmolApp.startupHeight, jmolApp.info, null);
    if (jmolApp.haveDisplay)
      frame.setVisible(true);
    return window;
  }

  @Override
public void showStatus(String message) {
    splash.showStatus(message);    
  }

  private void report(String str) {
    if (jmolApp.isSilent)
      return;
    Logger.info(str);
  }

  private void say(String message) {
    if (jmolApp.haveDisplay)
      if (splash == null) {
    	  
        report(message);
      } else {
        splash.showStatus(message);
      }
  }

  /**
   * @return A list of Actions that is understood by the upper level
   * application
   */
  public Action[] getActions() {

    List<Action> actions = new ArrayList<Action>();
    actions.addAll(Arrays.asList(defaultActions));
    actions.addAll(Arrays.asList(display.getActions()));
    actions.addAll(Arrays.asList(preferencesDialog.getActions()));
    return actions.toArray(new Action[0]);
  }

  /**
   * To shutdown when run as an application.  This is a
   * fairly lame implementation.   A more self-respecting
   * implementation would at least check to see if a save
   * was needed.
   */
  protected final class AppCloser extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent e) {
      JmolPanel.this.doClose();
    }
  }

  protected void doClose() {
    dispose(this.frame);
  }

  private void dispose(JFrame f) {
    // Save window positions and status in the history
    if (webExport != null)
      WebExport.cleanUp();
    if (historyFile != null) {
      if (frame != null) {
        jmolApp.border.x = frame.getWidth() - display.dimSize.width;
        jmolApp.border.y = frame.getHeight() - display.dimSize.height;
        historyFile.addWindowInfo("Jmol", frame, jmolApp.border);
      }
      //historyFile.addWindowInfo(CONSOLE_WINDOW_NAME, consoleframe);
      AppConsole console = (AppConsole) viewer.getProperty("DATA_API","getAppConsole", null);
      if (console != null && console.jcd != null)
        historyFile.addWindowInfo(SCRIPT_WINDOW_NAME, console.jcd, null);
      Component c = (Component) viewer.getProperty("DATA_API","getScriptEditor", null);
      if (c != null)
        historyFile.addWindowInfo(EDITOR_WINDOW_NAME, c, null);
      if (historyFile.getProperty("clearHistory", "false").equals("true"))
        historyFile.clear();
    }
    if (service != null) {
      service.close();
      service = null;
    }
    if (serverService != null) {
      serverService.close();
      serverService = null;
    }
    if (numWindows <= 1) {
      // Close Jmol
      report(GT._("Closing Jmol..."));
      // pluginManager.closePlugins();
      System.exit(0);
    } else {
      numWindows--;
      viewer.setModeMouse(JmolConstants.MOUSE_NONE);
      try {
        f.dispose();
      } catch (Exception e) {
        Logger.error("frame disposal exception");
        // ignore
      }
    }
  }

//  protected void setupNewFrame(JmolViewer viewer) {
//    String state = viewer.getStateInfo();
//    JFrame newFrame = new JFrame();
//    JFrame f = this.frame;
//    Jmol j = new Jmol(jmolApp, null, newFrame, (Jmol) this, startupWidth, startupHeight,
//        "", (state == null ? null : f.getLocationOnScreen()));
//    newFrame.setVisible(true);
//    j.viewer.menuStructure = viewer.menuStructure;
//    if (state != null) {
//      dispose(f);
//      j.viewer.evalStringQuiet(state);
//    }
//  }

  /**
   * This is the hook through which all menu items are
   * created.  It registers the result with the menuitem
   * hashtable so that it can be fetched with getMenuItem().
   * @param cmd
   * @return Menu item created
   * @see #getMenuItem
   */
  protected JMenuItem createMenuItem(String cmd) {
	JMenuItem mi;
    if (cmd.endsWith("Check")) {
      mi = guimap.newJCheckBoxMenuItem(cmd, false);
    } else {
      mi = guimap.newJMenuItem(cmd);
    }

    ImageIcon f = JmolResourceHandler.getIconX(cmd + "Image");
    if (f != null) {
      mi.setHorizontalTextPosition(SwingConstants.RIGHT);
      mi.setIcon(f);
    }
    
    if (cmd.contains("3D")){
    	System.out.println();
    }

    if (cmd.endsWith("Script")) {
      mi.setActionCommand(JmolResourceHandler.getStringX(cmd));
      mi.addActionListener(executeScriptAction);
    } else {
      mi.setActionCommand(cmd);
      Action a = getAction(cmd);
      if (a != null) {
        mi.addActionListener(a);
        a.addPropertyChangeListener(new ActionChangedListener(mi));
        mi.setEnabled(a.isEnabled());
      } else {
        mi.setEnabled(false);
      }
    }
    menuItems.put(cmd, mi);
    return mi;
  }

  /**
   * Fetch the menu item that was created for the given
   * command.
   * @param cmd  Name of the action.
   * @return item created for the given command or null
   *  if one wasn't created.
   */
  protected JMenuItem getMenuItem(String cmd) {
    return menuItems.get(cmd);
  }

  /**
   * Fetch the action that was created for the given
   * command.
   * @param cmd  Name of the action.
   * @return The action
   */
  protected Action getAction(String cmd) {
    return commands.get(cmd);
  }

  /**
   * Create the toolbar.  By default this reads the
   * resource file for the definition of the toolbars.
   * @return The toolbar
   */
  private JToolBar createToolbar() {

    toolbar = new JToolBar();
    String[] tool1Keys = tokenize(JmolResourceHandler.getStringX("toolbar"));
    for (int i = 0; i < tool1Keys.length; i++) {
      if (tool1Keys[i].equals("-")) {
        toolbar.addSeparator();
      } else {
        toolbar.add(createTool(tool1Keys[i]));
      }
    }

    //Action handler implementation would go here.
    toolbar.add(Box.createHorizontalGlue());

    return toolbar;
  }

  /**
   * Hook through which every toolbar item is created.
   * @param key
   * @return Toolbar item
   */
  protected Component createTool(String key) {
    return createToolbarButton(key);
  }

  /**
   * Create a button to go inside of the toolbar.  By default this
   * will load an image resource.  The image filename is relative to
   * the classpath (including the '.' directory if its a part of the
   * classpath), and may either be in a JAR file or a separate file.
   *
   * @param key The key in the resource file to serve as the basis
   *  of lookups.
   * @return Button
   */
  protected AbstractButton createToolbarButton(String key) {
	  
    ImageIcon ii = JmolResourceHandler.getIconX(key + "Image");
    boolean isHoldButton = (key.startsWith("animatePrev") || key.startsWith("animateNext"));
    AbstractButton b = (isHoldButton ? new AnimButton(ii, JmolResourceHandler.getStringX(key)) : new JButton(ii));    
    String isToggleString = JmolResourceHandler.getStringX(key + "Toggle");
    if (isToggleString != null) {
      boolean isToggle = Boolean.valueOf(isToggleString).booleanValue();
      if (isToggle) {
        b = new JToggleButton(ii);
        if (key.equals("rotateScript")) {
          display.buttonRotate = b;
        }
        if (key.equals("modelkitScript")) {
          display.buttonModelkit = b;
        }
        display.toolbarButtonGroup.add(b);
        String isSelectedString = JmolResourceHandler.getStringX(key
            + "ToggleSelected");
        if (isSelectedString != null) {
          boolean isSelected = Boolean.valueOf(isSelectedString).booleanValue();
          b.setSelected(isSelected);
         
        }
      }
    }
    b.setRequestFocusEnabled(false);
    b.setMargin(new Insets(1, 1, 1, 1));

    Action a = null;
    String actionCommand = null;
    if (isHoldButton) {
      
    } else if (key.endsWith("Script")) {
      actionCommand = JmolResourceHandler.getStringX(key);
      a = executeScriptAction;
    } else {
      actionCommand = key;
      a = getAction(key);
    }
    if (a != null) {
      b.setActionCommand(actionCommand);
      b.addActionListener(a); 
      //added -hcf
      //Pattern scaleUpPattern = Pattern.compile("scaleUp");
      //Pattern scaleDownPattern = Pattern.compile("scaleDown");
      //Matcher scaleUpMatcher = scaleUpPattern.matcher(key);
      //Matcher scaleDownMatcher = scaleDownPattern.matcher(key);
      //if (scaleUpMatcher.find() || scaleDownMatcher.find()) {
      //	  b.addChangeListener(new scaleButtonListener());
      //}
      //added end -hcf
      
      a.addPropertyChangeListener(new ActionChangedListener(b));
      b.setEnabled(a.isEnabled());
    } else {
      b.setEnabled(isHoldButton);
    }

    String tip = guimap.getLabel(key + "Tip");
    if (tip != null) {
      guimap.map.put(key + "Tip", b);
      b.setToolTipText(tip);
    }

    return b;
  }
  
  //added -hcf for scale button
  public class scaleButtonListener implements ScaleEventListener {
	@Override
	public void handleEvent(boolean loadStatus) {
		if (loadStatus) {
			guimap.setEnabled("scaleUpTip", false);
			guimap.setEnabled("scaleDownTip", false);
		}
		else {
			guimap.setEnabled("scaleUpTip", true);
			guimap.setEnabled("scaleDownTip", true);
		}
	}

  }
  
  
  
  //added end - hcf
  
  
  
  

  /**
   * Take the given string and chop it up into a series
   * of strings on whitespace boundries.  This is useful
   * for trying to get an array of strings out of the
   * resource file.
   * @param input String to chop
   * @return Strings chopped on whitespace boundaries
   */
  protected String[] tokenize(String input) {
    List<String> v = new ArrayList<String>();
    StringTokenizer t = new StringTokenizer(input);
    while (t.hasMoreTokens())
      v.add(t.nextToken());
    return v.toArray(new String[v.size()]);
  }

  /**
   * Create the menubar for the app.  By default this pulls the
   * definition of the menu from the associated resource file.
   * @return Menubar
   */
  protected JMenuBar createMenubar() {
    JMenuBar mb = new JMenuBar();
    addNormalMenuBar(mb);
    // The Macros Menu
    //addMacrosMenuBar(mb); ignored --hcf
    // The Plugin Menu
    // if (pluginManager != null) {
    //     mb.add(pluginManager.getMenu());
    // }
    // The Help menu, right aligned
    //mb.add(Box.createHorizontalGlue());
    addHelpMenuBar(mb);
    return mb;
  }

  protected void addMacrosMenuBar(JMenuBar menuBar) {
    // ok, here needs to be added the funny stuff
    JMenu macroMenu = guimap.newJMenu("macros");
    File macroDir = new File(System.getProperty("user.home")
        + System.getProperty("file.separator") + ".jmol"
        + System.getProperty("file.separator") + "macros");
    report("User macros dir: " + macroDir);
    report("       exists: " + macroDir.exists());
    report("  isDirectory: " + macroDir.isDirectory());
    if (macroDir.exists() && macroDir.isDirectory()) {
      File[] macros = macroDir.listFiles();
      for (int i = 0; i < macros.length; i++) {
        // loop over these files and load them
        String macroName = macros[i].getName();
        if (macroName.endsWith(".macro")) {
          if (Logger.debugging) {
            Logger.debug("Possible macro found: " + macroName);
          }
          FileInputStream macro = null;
          try {
            macro = new FileInputStream(macros[i]);
            Properties macroProps = new Properties();
            macroProps.load(macro);
            String macroTitle = macroProps.getProperty("Title");
            String macroScript = macroProps.getProperty("Script");
            JMenuItem mi = new JMenuItem(macroTitle);
            mi.setActionCommand(macroScript);
            mi.addActionListener(executeScriptAction);
            macroMenu.add(mi);
          } catch (IOException exception) {
            System.err.println("Could not load macro file: ");
            System.err.println(exception);
          } finally {
            if (macro != null) {
              try {
                macro.close();
              } catch (IOException e) {
                // Nothing
              }
            }
          }
        }
      }
    }
    menuBar.add(macroMenu);
  }

  protected void addNormalMenuBar(JMenuBar menuBar) {
    String[] menuKeys = tokenize(JmolResourceHandler.getStringX("menubar"));
    for (int i = 0; i < menuKeys.length; i++) {
      if (menuKeys[i].equals("-")) {
        menuBar.add(Box.createHorizontalGlue());
      } else {
        JMenu m = createMenu(menuKeys[i]);
        if (m != null)
          menuBar.add(m);
      }
    }
  }

  protected void addHelpMenuBar(JMenuBar menuBar) {
    JMenu m = createMenu("help");
    if (m != null) {
      menuBar.add(m);
    }
  }

  /**
   * Create a menu for the app.  By default this pulls the
   * definition of the menu from the associated resource file.
   * @param key
   * @return Menu created
   */
  protected JMenu createMenu(String key) {

    // Get list of items from resource file:
    String[] itemKeys = tokenize(JmolResourceHandler.getStringX(key));
    // Get label associated with this menu:
    JMenu menu = guimap.newJMenu(key);
    //JMenu menu = new JMenu();
    ImageIcon f = JmolResourceHandler.getIconX(key + "Image");
    if (f != null) {
      menu.setHorizontalTextPosition(SwingConstants.RIGHT);
      menu.setIcon(f);
    }

    // Loop over the items in this menu:
    for (int i = 0; i < itemKeys.length; i++) {
      String item = itemKeys[i];
      if (item.equals("-")) {
        menu.addSeparator();
      } else if (item.endsWith("Menu")) {
        menu.add(createMenu(item));
      } else {
        JMenuItem mi = createMenuItem(item);        
        menu.add(mi);
      }
    }
    menu.addMenuListener(new MenuListener() {
        @Override
		public void menuSelected(MenuEvent e) {
          String menuKey = KeyJMenuItem.getKey(e.getSource());
          if (menuKey.equals("display") || menuKey.equals("tools"))
            setMenuState();
        }
        @Override
		public void menuDeselected(MenuEvent e) {
        }
        @Override
		public void menuCanceled(MenuEvent e) {
        }
    });
    return menu;
  }

  void setMenuState() {
    //guimap.setSelected("perspectiveCheck", viewer.getPerspectiveDepth());
    //guimap.setSelected("hydrogensCheck", viewer.getShowHydrogens()); -modified hcf
    guimap.setSelected("measurementsCheck", viewer.getShowMeasurements());
    guimap.setSelected("axesCheck", viewer.getShowAxes());
    guimap.setSelected("boundboxCheck", viewer.getShowBbcage());
    //guimap.setEnabled("openJSpecViewScript", display.isRotateMode()); -modified hcf
  }
  
  
  //added -hcf
  void setScaleButtonDisable(){
	  guimap.setEnabled("scaleDownTip",false);
	  guimap.setEnabled("scaleUpTip",false);
  }
  
  
  
  
  //added end -hcf
 
  private static class ActionChangedListener implements PropertyChangeListener {

    AbstractButton button;

    ActionChangedListener(AbstractButton button) {
      super();
      this.button = button;
    }

    @Override
	public void propertyChange(PropertyChangeEvent e) {

      String propertyName = e.getPropertyName();
      if (e.getPropertyName().equals(Action.NAME)) {
        String text = (String) e.getNewValue();
        if (button.getText() != null) {
          button.setText(text);
        }
      } else if (propertyName.equals("enabled")) {
        Boolean enabledState = (Boolean) e.getNewValue();
        button.setEnabled(enabledState.booleanValue());
      }
    }
  }

  /**
   * Actions defined by the Jmol class
   */
  private Action[] defaultActions = { new NewAction(), new NewwinAction(),
      new OpenAction(), new OpenUrlAction(), new OpenPdbAction(), new OpenMolAction(), printAction, exportAction,
      new CloseAction(), new ExitAction(), copyImageAction, copyScriptAction,
      pasteClipboardAction, new AboutAction(), new WhatsNewAction(),
      new UguideAction(), new ConsoleAction(),  
      new RecentFilesAction(), povrayAction, writeAction, toWebAction, 
      new ScriptWindowAction(), new ScriptEditorAction(),
      new AtomSetChooserAction(), viewMeasurementTableAction, 
      new GaussianAction(), new ResizeAction(), surfaceToolAction, new scaleDownAction(), new scaleUpAction(), 
      new searchGenomeSequenceTableAction(),  

      extractPDBAction, pdb2GSSAction, lorDGModellerAction, loopDetectAction, annotationAction, extractHiCAction, convertToHiCAction, normalizeHiCAction,////last four added -hcf, Tuan added pdb2GSSAction
      structure3DMaxAction, heatmap2DvisualizeAction,findTADAction}// [Tosin added: structure3DMaxAction,heatmap2Dvisualize]

  ;

  class CloseAction extends AbstractAction {
    CloseAction() {
      super(closeAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      JmolPanel.this.frame.setVisible(false);
      JmolPanel.this.doClose();
    }
  }

  class ConsoleAction extends AbstractAction {

    public ConsoleAction() {
      super("jconsole");
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      if (consoleframe != null)
        consoleframe.setVisible(true);
    }

  }

  class AboutAction extends AbstractAction {

    public AboutAction() {
      super(aboutAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      AboutDialog ad = new AboutDialog(frame, viewer);
      ad.setVisible(true);
    }

  }

  class WhatsNewAction extends AbstractAction {

    public WhatsNewAction() {
      super(whatsnewAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      WhatsNewDialog wnd = new WhatsNewDialog(frame);
      wnd.setVisible(true);
    }
  }

  class GaussianAction extends AbstractAction {
    public GaussianAction() {
      super(gaussianAction);
    }
    
    @Override
	public void actionPerformed(ActionEvent e) {
      if (gaussianDialog == null)
        gaussianDialog = new GaussianDialog(frame, viewer);
      gaussianDialog.setVisible(true);
    }
  }
    
  class NewwinAction extends AbstractAction {

    NewwinAction() {
      super(newwinAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      JFrame newFrame = new JFrame();
      new Jmol(jmolApp, null, newFrame, (Jmol) JmolPanel.this, startupWidth, startupHeight, null, null);
      newFrame.setVisible(true);
    }

  }

  class UguideAction extends AbstractAction {

    public UguideAction() {
      super(uguideAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      (new HelpDialog(frame)).setVisible(true);
    }
  }

  class PasteClipboardAction extends AbstractAction {

    public PasteClipboardAction() {
      super(pasteClipboardActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      viewer.loadInline(ImageCreator.getClipboardTextStatic(), false);
    }
  }

  /**
   * An Action to copy the current image into the clipboard. 
   */
  class CopyImageAction extends AbstractAction {

    public CopyImageAction() {
      super(copyImageActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      (new ImageCreator(viewer)).clipImage(null);
    }
  }

  class CopyScriptAction extends AbstractAction {

    public CopyScriptAction() {
      super(copyScriptActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      (new ImageCreator(viewer)).clipImage((String) viewer.getProperty(
          "string", "stateInfo", null));
    }
  }

  class PrintAction extends AbstractAction {

    public PrintAction() {
      super(printActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      print();
    }

  }

  /**
   * added print command, so that it can be used by RasmolScriptHandler
   **/
  public void print() {

    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(display);
    if (job.printDialog()) {
      try {
        job.print();
      } catch (PrinterException e) {
        Logger.error("Error while printing", e);
      }
    }
  }

  class OpenAction extends NewAction {

    OpenAction() {
      super(openAction);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      openFile();
    }
  }

 
  //added -hcf
  class scaleDownAction extends NewAction {
	  scaleDownAction() {
		  super(scaleDownAction);
	  }
	  
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	script = "scaledown";
	    	viewer.script(script);
	    }
  }

  class scaleUpAction extends NewAction {
	  scaleUpAction() {
		  super(scaleUpAction);
	  }
	  
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	//this.setEnabled(false);
	    	script = "scaleUp";
	    	viewer.script(script);
	    	//this.setEnabled(true);
	    }
  }
  

  
  class searchGenomeSequenceTableAction extends AbstractAction {
    public searchGenomeSequenceTableAction() {
        super("searchGenomeSequenceTable");
     }

    @Override
	public void actionPerformed(ActionEvent e) {
        display.sequenceTable.activate();
     } 
  }

  
  class extractPDBAction extends NewAction {
	  extractPDBAction() {
		  super(extractPDBProperty);
	  }
	  
	  @Override
	  public void actionPerformed(ActionEvent e) {
		  String fileName = (new Dialog()).getSaveFileNameFromDialog(viewer,
		          null, "SPT");
		  if (fileName != null)
		        Logger.info((String) viewer.createImage(fileName, "SPT", viewer.getStateInfo(),
		            Integer.MIN_VALUE, 0, 0));
		  viewer.callExtractPDB(fileName);
	  }
  }
  
  
  //added end -hcf

  
  class NormalizeHiCAction extends NewAction{
	  
	  //Pre dump = new Dump();
	  Dataset dataset = null;
	  
	  NormalizeHiCAction(){
		  super(normalizeHiC);
	  }
	  
	  @Override
	  public void actionPerformed(ActionEvent e) {
		  
		  GridBagConstraints gbc = new GridBagConstraints();
		  gbc.insets = new Insets(5, 5, 5, 5);
		
		  JPanel panel = new JPanel(){
			  @Override
			  public Dimension getPreferredSize() {
				  return new Dimension(800, 300);
			  }	       
		  };
		  
		  panel.setLayout(new GridBagLayout());  
		  
		  int y = 0;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.anchor = GridBagConstraints.WEST;
		  panel.add(new JLabel("Input File:"), gbc);
		  
		  JTextField inputField = new JTextField();
		  inputField.setPreferredSize(new Dimension(300,20));
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  panel.add(inputField, gbc);
		  
		  JButton browserFileButton = new JButton("Browse file");
		  
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  panel.add(browserFileButton, gbc);
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.anchor = GridBagConstraints.WEST;
		  panel.add(new JLabel("Output file:"), gbc);
		  
		  JTextField outputField = new JTextField();
		  outputField.setPreferredSize(new Dimension(300,20));
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  panel.add(outputField, gbc);
		  
		  JButton browserOutputFileButton = new JButton("Browse file");		  
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  panel.add(browserOutputFileButton, gbc);
		  
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.anchor = GridBagConstraints.WEST;
		  JLabel minResolutionLabel = new JLabel("Minimum resolution to normalize:");
		  minResolutionLabel.setVisible(false);
		  panel.add(minResolutionLabel, gbc);
		  
		  NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());			 
		  formatter.setValueClass(Integer.class);
		  formatter.setMinimum(0);
		  formatter.setMaximum(Integer.MAX_VALUE);
		  formatter.setAllowsInvalid(false);		  
		  
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  JFormattedTextField minResolutionField = new JFormattedTextField(formatter);		  
		  minResolutionField.setPreferredSize(new Dimension(100,20));		  
		  minResolutionField.setVisible(false);		  		  
		  panel.add(minResolutionField, gbc);
		  
		  gbc.gridx = 2;
		  gbc.gridy = y;
		  gbc.anchor = GridBagConstraints.WEST;
		  JLabel optionalResolutionLabel = new JLabel("(optional)");
		  optionalResolutionLabel.setVisible(false);
		  panel.add(optionalResolutionLabel, gbc);
		  
		  
		  browserFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					if (fileName == null) return;
					
					inputField.setText(fileName);
					
					if (fileName.endsWith(".hic")){
						outputField.setText(fileName);
						outputField.setEnabled(false);
						browserOutputFileButton.setEnabled(false);
						
						minResolutionLabel.setVisible(true);
						minResolutionField.setVisible(true);
						optionalResolutionLabel.setVisible(true);
						
					}else{
						outputField.setText(fileName.replace(".", "_norm."));
						outputField.setEnabled(true);
						browserOutputFileButton.setEnabled(true); 
						
						minResolutionLabel.setVisible(false);
						minResolutionField.setVisible(false);
						optionalResolutionLabel.setVisible(false);
					}
				}
		  });
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 4;
		  gbc.anchor = GridBagConstraints.CENTER;
		  JButton normalizeButton = new JButton("Normalize");
		  panel.add(normalizeButton, gbc);
		  
		  normalizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (inputField.getText().length() == 0){
					JOptionPane.showMessageDialog(null, "Please specify input file!");
					return;
				}
				if (outputField.getText().length() == 0){
					JOptionPane.showMessageDialog(null, "Please specify output file!");
					return;
				}
				String inputFile = inputField.getText();
				String outputFile = outputField.getText();
				int minRes = 0;
				if (minResolutionField.getText().length() > 0){
					minRes = Integer.parseInt(minResolutionField.getText());
				}
				

				Window win = SwingUtilities.getWindowAncestor((AbstractButton)e.getSource());
				final JDialog dialog = new JDialog(win, "Normalizing data ... please wait !", ModalityType.APPLICATION_MODAL);
				dialog.setPreferredSize(new Dimension(300,80));
				
				NormalizeHiCWorker normalizationWorker = new NormalizeHiCWorker(inputFile, outputFile, minRes);
				  
				normalizationWorker.addPropertyChangeListener(new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						switch (evt.getPropertyName()){
						case "progress":
							break;
						case "state":
							switch ((StateValue)evt.getNewValue()){
							case DONE:
								
								win.setEnabled(true);
								dialog.dispose();
								
								try {
									String msg = normalizationWorker.get();
									JOptionPane.showMessageDialog(null, msg);
								} catch (InterruptedException e) {									
									e.printStackTrace();
									JOptionPane.showMessageDialog(null, "Error while extracting data");
								} catch (ExecutionException e) {									
									e.printStackTrace();
									JOptionPane.showMessageDialog(null, "Error while extracting data");
								}
								
								
								break;
							case PENDING:								
								break;
							case STARTED:
								dialog.setVisible(true);
								win.setEnabled(false);								
								break;
							default:								
								break;
							}
						}
						
					}
				  });				  
				  
				normalizationWorker.execute();
				  
				JProgressBar progressBar = new JProgressBar();
			    progressBar.setIndeterminate(true);
			    JPanel panel = new JPanel(new BorderLayout());
			      
			    panel.add(progressBar, BorderLayout.CENTER);
			    panel.add(new JLabel(""), BorderLayout.PAGE_START);
			    dialog.add(panel);
			    dialog.pack();
			    dialog.setLocationRelativeTo(win);
			    dialog.setVisible(true);
				

			}
		  });
		  
		  
		  
		  JScrollPane scrollpane = new JScrollPane(panel);
		  scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		  scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		  
		  Frame subFrame = new JFrame();
		  subFrame.setSize(new Dimension(800, 400));
		  subFrame.setLocation(400, 400);
		
		  subFrame.add(scrollpane, BorderLayout.CENTER);
		  subFrame.setVisible(true);
		  subFrame.setTitle("Normalize HiC Data");
	  }
  }

  /**
   * 
   * @author Tuan
   *
   */
  class ConvertToHiCAction extends NewAction{
	  
	  //Pre dump = new Dump();
	  Dataset dataset = null;
	  
	  ConvertToHiCAction(){
		  super(converToHiC);
	  }
	  
	  @Override
	  public void actionPerformed(ActionEvent e) {
		  
		  GridBagConstraints gbc = new GridBagConstraints();
		  gbc.insets = new Insets(5, 5, 5, 5);
		
		  JPanel panel = new JPanel(){
			  @Override
			  public Dimension getPreferredSize() {
				  return new Dimension(800, 300);
			  }	       
		  };
		  
		  panel.setLayout(new GridBagLayout());  
		  
		  int y = 0;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.anchor = GridBagConstraints.WEST;
		  panel.add(new JLabel("Input File:"), gbc);
		  
		  JTextField inputField = new JTextField();
		  inputField.setPreferredSize(new Dimension(300,20));
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  panel.add(inputField, gbc);
		  
		  JButton browserFileButton = new JButton("Browse File");
		  browserFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					if (fileName == null) return;
					inputField.setText(fileName);					
				}
			});
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  panel.add(browserFileButton, gbc);
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.anchor = GridBagConstraints.WEST;
		  panel.add(new JLabel("Genome ID"), gbc);
		  
		  //JTextField genomeIDField = new JTextField();
		  //genomeIDField.setPreferredSize(new Dimension(100,20));
		  
//		  InputStream gmolResources;
//		  Properties prop = new Properties();
//		  try {
//			  gmolResources = new FileInputStream("Jmol-resources.properties");			  
//			  prop.load(gmolResources);
//		  } catch (FileNotFoundException e1) {			
//			  e1.printStackTrace();
//		  } catch (IOException e1) {
//			e1.printStackTrace();
//		}
		  		  
//		  String genomeIDs = prop.getProperty("genomeid");
		  
		  String genomeIDs = Constants.AVAILABLEGENOMEIDS;
		  
		  String[] ids = genomeIDs.split(",");
		  
		  JComboBox<String> genomeIDList = new JComboBox<String>();
		  
		  for(int i = 0; i < ids.length; i++){
			  if (ids[i].length() > 0){
				  genomeIDList.addItem(ids[i]);
			  }
		  }
		  
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  //panel.add(genomeIDField, gbc);
		  panel.add(genomeIDList, gbc);
		  
//		  genomeIDList.setInputVerifier(new InputVerifier() {
//			
//			@Override
//			public boolean verify(JComponent input) {
//				Set<String> validGenomeIDs = new HashSet<String>(Arrays.asList("hg18", "hg19", "hg38", "dMel", "mm9", "mm10", "anasPlat1", "bTaurus3",
//						"canFam3", "equCab2", "galGal4", "Pf3D7", "sacCer3", "sCerS288c", "susScr3", "TAIR10"));
//				
//				JTextField field = (JTextField) input;
//				if (validGenomeIDs.contains(field.getText())) return true;
//				
//				return false;
//			}
//		  });
		  
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  panel.add(new JLabel("Output File"), gbc);
		  
		  JTextField outputField = new JTextField();
		  outputField.setPreferredSize(new Dimension(300,20));
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  panel.add(outputField, gbc);
		  
		  JButton browserOutputFileButton = new JButton("Browse File");
		  browserOutputFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					if (fileName == null) return;
					outputField.setText(fileName);					
				}
			});
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  panel.add(browserOutputFileButton, gbc);
		  
		  List<JComponent> hiddenComponents = new ArrayList<JComponent>();
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 6;
		  JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		  separator.setPreferredSize(new Dimension(750,5));
		  hiddenComponents.add(separator);
		  separator.setVisible(false);
		  panel.add(separator, gbc);
		  
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  JLabel contactThresholdLabel = new JLabel("Contact Threshold");
		  contactThresholdLabel.setVisible(false);
		  hiddenComponents.add(contactThresholdLabel);
		  panel.add(contactThresholdLabel, gbc);
		  
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  JLabel mapqScoreThresholdLable = new JLabel("MAPQ Score Threshold");
		  mapqScoreThresholdLable.setVisible(false);
		  hiddenComponents.add(mapqScoreThresholdLable);
		  panel.add(mapqScoreThresholdLable, gbc);
		  
		  gbc.gridx = 2;
		  gbc.gridy = y;
		  JLabel chromLabel = new JLabel("Chromosomes (separated by ,)"); 
		  chromLabel.setVisible(false);
		  hiddenComponents.add(chromLabel);
		  panel.add(chromLabel, gbc);
		  
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  JLabel resLabel = new JLabel("Resolutions (separated by ,)");
		  resLabel.setVisible(false);
		  hiddenComponents.add(resLabel);
		  panel.add(resLabel, gbc);
		  
		  
		  NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());			 
		  formatter.setValueClass(Integer.class);
		  formatter.setMinimum(0);
		  formatter.setMaximum(Integer.MAX_VALUE);
		  formatter.setAllowsInvalid(false);
		  
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  JFormattedTextField contactThresholdField = new JFormattedTextField(formatter);
		  contactThresholdField.setText("0");
		  contactThresholdField.setPreferredSize(new Dimension(100,20));		  
		  contactThresholdField.setVisible(false);
		  hiddenComponents.add(contactThresholdField);		  
		  panel.add(contactThresholdField, gbc);
		  
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  JFormattedTextField mapqScoreThresholdField = new JFormattedTextField(formatter);
		  mapqScoreThresholdField.setText("0");
		  mapqScoreThresholdField.setPreferredSize(new Dimension(100,20));
		  mapqScoreThresholdField.setVisible(false);
		  hiddenComponents.add(mapqScoreThresholdField);
		  panel.add(mapqScoreThresholdField, gbc);
		  
		  gbc.gridx = 2;
		  gbc.gridy = y;
		  JTextField chromField = new JTextField();
		  chromField.setPreferredSize(new Dimension(100,20));
		  chromField.setVisible(false);		  
		  hiddenComponents.add(chromField);
		  panel.add(chromField, gbc);
		  chromField.setInputVerifier(new InputVerifier() {
			
			@Override
			public boolean verify(JComponent input) {
				JTextField field = (JTextField)input;
				String st = field.getText();
				for(int i = 0; i < st.length(); i++){
					if (st.charAt(i) == 'X' || st.charAt(i) == 'Y' || st.charAt(i) == 'x' || st.charAt(i) == 'y') continue;
					if (st.charAt(i) == 'M' && i < st.length() - 1 && st.charAt(i + 1) == 'T') {i++;continue;}
					if (st.charAt(i) == 'm' && i < st.length() - 1 && st.charAt(i + 1) == 't') {i++;continue;}
					
					if ((st.charAt(i) < '0' || st.charAt(i) > '9') && st.charAt(i) != ',') return false;
					if (i > 0 && st.charAt(i) == ',' && st.charAt(i - 1) == ',') return false;
				}				
								
				return true;
			}
		});
		  
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  JTextField resField = new JTextField();
		  resField.setPreferredSize(new Dimension(100,20));
		  resField.setText("2500000,1000000,500000,250000,100000,50000,25000,10000,5000");
		  resField.setVisible(false);
		  hiddenComponents.add(resField);
		  panel.add(resField, gbc);
		  
		  resField.setInputVerifier(new InputVerifier() {
				
				@Override
				public boolean verify(JComponent input) {
					JTextField field = (JTextField)input;
					String st = field.getText();
					for(int i = 0; i < st.length(); i++){
						if (st.charAt(i) == 'f' && (i == 0 || st.charAt(i - 1) < '0' || st.charAt(i) > '9')) return false;
						if ((st.charAt(i) < '0' || st.charAt(i) > '9') && st.charAt(i) != ',') return false;
						if (i > 0 && st.charAt(i) == ',' && st.charAt(i - 1) == ',') return false;
					}				
									
					return true;
				}
		  });
		  
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  JLabel restrictionSiteLabel = new JLabel("Restriction Site File");
		  restrictionSiteLabel.setVisible(false);
		  hiddenComponents.add(restrictionSiteLabel);
		  panel.add(restrictionSiteLabel, gbc);
		  
		  JTextField restrictionSiteField = new JTextField();
		  restrictionSiteField.setPreferredSize(new Dimension(300,20));
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;		  
		  restrictionSiteField.setVisible(false);
		  hiddenComponents.add(restrictionSiteField);
		  panel.add(restrictionSiteField, gbc);
		  
		  
		  JButton browserRestrictionSiteFileButton = new JButton("Browse File");
		  browserRestrictionSiteFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					if (fileName == null) return;
					restrictionSiteField.setText(fileName);					
				}
			});
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  browserRestrictionSiteFileButton.setVisible(false);
		  hiddenComponents.add(browserRestrictionSiteFileButton);
		  panel.add(browserRestrictionSiteFileButton, gbc);
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  JCheckBox optinalCheckBox = new JCheckBox("Advanced Options");
		  panel.add(optinalCheckBox, gbc);
		  
		  optinalCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (optinalCheckBox.isSelected()){
					for(JComponent com: hiddenComponents){
						com.setVisible(true);
					}
				}else{
					for(JComponent com: hiddenComponents){
						com.setVisible(false);
					}
				}
				
			}
		});
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 4;
		  gbc.anchor = GridBagConstraints.CENTER;
		  JButton processDataButton = new JButton("Convert to HiC");
		  panel.add(processDataButton, gbc);
		  
		  processDataButton.addActionListener(new ActionListener() {
				
			@Override
			public void actionPerformed(ActionEvent e) {
				Window win = SwingUtilities.getWindowAncestor((AbstractButton)e.getSource());
				final JDialog dialog = new JDialog(win, "Converting data ... please wait !", ModalityType.APPLICATION_MODAL);
				dialog.setPreferredSize(new Dimension(300,80));
				
				
				String inputFile = inputField.getText();
				String outputFile = outputField.getText();
				int countThreshold = 0;
				if (contactThresholdField.getText().length() > 0) countThreshold = Integer.parseInt(contactThresholdField.getText());
				
				int mapqThreshold = 0;
				if (mapqScoreThresholdField.getText().length() > 0) mapqThreshold = Integer.parseInt(mapqScoreThresholdField.getText());
				Set<String> includedChromosomes = new HashSet<String>();
				if (chromField.getText().length() > 0){
					String[] chroms = chromField.getText().split(",");
					includedChromosomes.addAll(Arrays.asList(chroms));
				}
				Set<String> resolutions = new HashSet<String>();
				if (resField.getText().length() > 0){
					String[] res = resField.getText().split(",");
					resolutions.addAll(Arrays.asList(res));
				}
				String restrictionSiteFile = restrictionSiteField.getText();
				
				//String genomeId = genomeIDField.getText();
				String genomeId = genomeIDList.getItemAt(genomeIDList.getSelectedIndex());
				long genomeLength = 0;
				List<Chromosome> chromosomes = HiCFileTools.loadChromosomes(genomeId);
		        for (Chromosome c : chromosomes) {
		            if (c != null)
		                genomeLength += c.getLength();
		        }
		        chromosomes.set(0, new Chromosome(0, HiCFileTools.ALL_CHROMOSOME, (int) (genomeLength / 1000)));
		        
		        Preprocessor preprocessor = new Preprocessor(new File(outputFile), genomeId, chromosomes);
		        preprocessor.setIncludedChromosomes(includedChromosomes);
		        preprocessor.setCountThreshold(countThreshold);
		        preprocessor.setMapqThreshold(mapqThreshold);		        
		        preprocessor.setFragmentFile(restrictionSiteFile);
		        if (resolutions.size() > 0) preprocessor.setResolutions(resolutions);
		        
				
				PreProcessingHiC processingHiC = new PreProcessingHiC();
				processingHiC.setPreprocessor(preprocessor);
				processingHiC.setInputFile(inputFile);
				processingHiC.setOutputFile(outputFile);
				
				ConvertToHiCWorker convertToHiCWorker = new ConvertToHiCWorker(processingHiC);
				  
				convertToHiCWorker.addPropertyChangeListener(new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						switch (evt.getPropertyName()){
						case "progress":
							break;
						case "state":
							switch ((StateValue)evt.getNewValue()){
							case DONE:
								
								win.setEnabled(true);
								dialog.dispose();
								
								try {
									String msg = convertToHiCWorker.get();
									JOptionPane.showMessageDialog(null, msg);
								} catch (InterruptedException e) {									
									e.printStackTrace();
									JOptionPane.showMessageDialog(null, "Error while converting data");
								} catch (ExecutionException e) {									
									e.printStackTrace();
									JOptionPane.showMessageDialog(null, "Error while converting data");
								}
								
								
								break;
							case PENDING:								
								break;
							case STARTED:
								dialog.setVisible(true);
								win.setEnabled(false);								
								break;
							default:								
								break;
							}
						}
						
					}
				  });				  
				  
				convertToHiCWorker.execute();
				  
				JProgressBar progressBar = new JProgressBar();
			    progressBar.setIndeterminate(true);
			    JPanel panel = new JPanel(new BorderLayout());
			      
			    panel.add(progressBar, BorderLayout.CENTER);
			    panel.add(new JLabel(""), BorderLayout.PAGE_START);
			    dialog.add(panel);
			    dialog.pack();
			    dialog.setLocationRelativeTo(win);
			    dialog.setVisible(true);	
				
			}
		});

		  
		  
		  
		  JScrollPane scrollpane = new JScrollPane(panel);
		  scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		  scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		  Frame subFrame = new JFrame();
		  subFrame.setSize(new Dimension(800, 400));
		  subFrame.setLocation(400, 400);
		
		  subFrame.add(scrollpane, BorderLayout.CENTER);
		  subFrame.setVisible(true);
		  subFrame.setTitle("Convert Mapped Reads to HiC Files");
		  
		  
	  }
  }
  /**
   * 
   * @author Tuan
   *
   */
  class ExtractHiCAction extends NewAction{
	  
	  //Dump dump = new Dump();
	  Dataset dataset = null;
	  
	  ExtractHiCAction(){
		  super(extractHiC);
	  }
	  
	  @Override
	  public void actionPerformed(ActionEvent e) {
		  
		  GridBagConstraints gbc = new GridBagConstraints();
		  gbc.insets = new Insets(5, 5, 5, 5);
		
		  JPanel panel = new JPanel(){
			  @Override
			  public Dimension getPreferredSize() {
				  return new Dimension(800, 300);
			  }	       
		  };
		  
		  panel.setLayout(new GridBagLayout());  
		  
		  int y=0;
		 
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.gridheight = 1;
		  panel.add(new JLabel("Path to .hic File"), gbc);
		  
		  JTextField pathField = new JTextField();
		  pathField.setPreferredSize(new Dimension(300,20));
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  panel.add(pathField, gbc);
		  
		  JButton browserFileButton = new JButton("Browse File (if locally)");
		  browserFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					if (fileName == null) return;
					pathField.setText(pathField.getText() + fileName + ";");					
				}
			});
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  panel.add(browserFileButton, gbc);
		  
		  
		  y++;
		  JButton loadFileButton = new JButton("Load");
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(loadFileButton, gbc);
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 6;
		  JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		  separator.setPreferredSize(new Dimension(750,5));
		  panel.add(separator, gbc);
		  
		  
		  /*
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(new JLabel("Genome:"), gbc);
		  
		  JTextField genomeField = new JTextField("---");
		  genomeField.setPreferredSize(new Dimension(100,20));
		  genomeField.setEnabled(false);
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(genomeField, gbc);
		  
		  gbc.gridx = 2;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(new JLabel("Resolution:"), gbc);
		  
		  JComboBox<String> resolutionList = new JComboBox<String>();
		  resolutionList.setPreferredSize(new Dimension(100,20));
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.WEST;
		  gbc.fill = GridBagConstraints.NONE;
		  panel.add(resolutionList, gbc);
		  
		  gbc.gridx = 4;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(new JLabel("Normalization:"), gbc);
		  
		  JComboBox<String> normalizationList = new JComboBox<String>();
		  normalizationList.setPreferredSize(new Dimension(100,20));
		  gbc.gridx = 5;
		  gbc.gridy = y;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(normalizationList, gbc);
		  */
		  
		  JComboBox<String> chrom1List = new JComboBox<String>();		  
		  JComboBox<String> chrom2List = new JComboBox<String>();
		  
		  chrom1List.setPreferredSize(new Dimension(100,20));
		  chrom2List.setPreferredSize(new Dimension(100,20));
		  
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(new JLabel("Genome"), gbc);
		  
		  
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(new JLabel("Chromosome"), gbc);
		  
		  gbc.gridx = 2;		  
		  panel.add(new JLabel("From"), gbc);
		  
		  gbc.gridx = 3;		  
		  panel.add(new JLabel("To"), gbc);
		  
		  gbc.gridx = 4;		  
		  panel.add(new JLabel("Resolution"), gbc);
		  
		  gbc.gridx = 5;
		  panel.add(new JLabel("Normalization"), gbc);
		  
		  /*
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  panel.add(new JLabel("Chromosome 2"), gbc);
		  
		  gbc.gridx = 4;
		  gbc.gridy = y;
		  panel.add(new JLabel("From"), gbc);
		  
		  gbc.gridx = 5;
		  gbc.gridy = y;
		  panel.add(new JLabel("To"), gbc);
		  */
		  
		  y++;		  
		  JTextField genomeField = new JTextField("---");
		  genomeField.setPreferredSize(new Dimension(100,20));
		  genomeField.setEnabled(false);
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  gbc.anchor = GridBagConstraints.CENTER;
		  panel.add(genomeField, gbc);
		  
		  
		  gbc.gridx = 1;		  		  
		  panel.add(chrom1List, gbc);
		  
		  /*
		  NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		  formatter.setValueClass(Integer.class);
		  //formatter.setMinimum(0);
		  formatter.setMaximum(Integer.MAX_VALUE);
		  formatter.setAllowsInvalid(true);
		  */
		  		  
		  
		  gbc.gridx = 2;
		  gbc.gridy = y;
		  JFormattedTextField chr1FromField = new JFormattedTextField();
		  chr1FromField.setPreferredSize(new Dimension(100,20));
		  chr1FromField.setEnabled(false);
		  panel.add(chr1FromField, gbc);
		  

		  gbc.gridx = 3;
		  gbc.gridy = y;
		  //JTextField chr1ToField = new JTextField();
		  JFormattedTextField chr1ToField = new JFormattedTextField();
		  chr1ToField.setPreferredSize(new Dimension(100,20));
		  chr1ToField.setEnabled(false);
		  panel.add(chr1ToField, gbc);
		  
		  JComboBox<String> resolutionList = new JComboBox<String>();
		  resolutionList.setPreferredSize(new Dimension(100,20));
		  gbc.gridx = 4;
		  panel.add(resolutionList, gbc);
		  
		  JComboBox<String> normalizationList = new JComboBox<String>();
		  normalizationList.setPreferredSize(new Dimension(100,20));
		  gbc.gridx = 5;
		  panel.add(normalizationList, gbc);
		  
		  
		  /*
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  panel.add(chrom2List, gbc);
		  
		  gbc.gridx = 4;
		  gbc.gridy = y;
		  //JTextField chr2FromField = new JTextField();
		  JFormattedTextField chr2FromField = new JFormattedTextField(formatter);
		  chr2FromField.setPreferredSize(new Dimension(100,20));
		  chr2FromField.setEnabled(false);
		  panel.add(chr2FromField, gbc);

		  gbc.gridx = 5;
		  gbc.gridy = y;
		  //JTextField chr2ToField = new JTextField();
		  JFormattedTextField chr2ToField = new JFormattedTextField(formatter);
		  chr2ToField.setPreferredSize(new Dimension(100,20));
		  chr2ToField.setEnabled(false);
		  panel.add(chr2ToField, gbc);
		  */
		  
		  
		  loadFileButton.addActionListener(new ActionListener() {
			
			  @Override
			  public void actionPerformed(ActionEvent e) {
				  
				  chrom1List.removeAllItems();
				  chrom2List.removeAllItems();
				  
				  loadFileButton.setEnabled(false);
				  
				  List<String> files = new ArrayList<String>();
				  for (String s : pathField.getText().split(";")){
					  files.add(s);
				  }
				
				  Window win = SwingUtilities.getWindowAncestor((AbstractButton)e.getSource());
				  JDialog dialog = new JDialog(win, "Loading file ...", ModalityType.APPLICATION_MODAL);
				  dialog.setPreferredSize(new Dimension(200,80));

				  			      
						  
				  ReadHiCHeaderWorker loadFileWorker = new ReadHiCHeaderWorker(files);
				  
				  loadFileWorker.addPropertyChangeListener(new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						switch (evt.getPropertyName()){
						case "progress":
							break;
						case "state":
							switch ((StateValue)evt.getNewValue()){
							case DONE:
								try{
									dataset = loadFileWorker.get();
								}catch(Exception ex){
									break;
								}
								
								genomeField.setText(dataset.getGenomeId());
								
								List<Chromosome> chroms = dataset.getChromosomes();
								chrom1List.removeAllItems();
								chrom2List.removeAllItems();
								for(int i = 1; i < chroms.size(); i++){
									Chromosome chrom = chroms.get(i);
									chrom1List.addItem(chrom.getName());
									chrom2List.addItem(chrom.getName());
								}
								chrom1List.addItem(chroms.get(0).getName());
								chrom2List.addItem(chroms.get(0).getName());
								
								
								resolutionList.removeAllItems();
								for(HiCZoom res: dataset.getBpZooms()){
									resolutionList.addItem(res.getKey());
								}
								for(HiCZoom res: dataset.getFragZooms()){
									resolutionList.addItem(res.getKey());
								}
								
								normalizationList.removeAllItems();
								for(NormalizationType norm : dataset.getNormalizationTypes()){									
									normalizationList.addItem(norm.getLabel());
								}
								
								chr1FromField.setEnabled(true);
								chr1ToField.setEnabled(true);
								//chr2FromField.setEnabled(true);
								//chr2ToField.setEnabled(true);
								
								loadFileButton.setEnabled(true);
								win.setEnabled(true);
								dialog.dispose();
								
								break;
							case PENDING:								
								break;
							case STARTED:
								dialog.setVisible(true);
								win.setEnabled(false);								
								break;
							default:								
								break;
							}
						}
						
					}
				  });				  
				  
				  loadFileWorker.execute();
				  
				  JProgressBar progressBar = new JProgressBar();
			      progressBar.setIndeterminate(true);
			      JPanel panel = new JPanel(new BorderLayout());
			      
			      panel.add(progressBar, BorderLayout.CENTER);
			      panel.add(new JLabel(""), BorderLayout.PAGE_START);
			      dialog.add(panel);
			      dialog.pack();
			      dialog.setLocationRelativeTo(win);
			      dialog.setVisible(true);
			      
			  }
		  });
		  
		  
		  /*
		  JRadioButton observedOption = new JRadioButton("Observed");
	      JRadioButton OEoption = new JRadioButton("Observed/Expected");	      
	      
	      ButtonGroup matrixTypeGroup = new ButtonGroup();	      
	      matrixTypeGroup.add(OEoption);
	      matrixTypeGroup.add(observedOption);
	      y++;
	      gbc.gridx = 0;
	      gbc.gridy = y;
	      gbc.gridwidth = 1;
	      panel.add(new JLabel("Matrix type:"), gbc);
	      
	      gbc.gridx = 1;
	      gbc.gridy = y;
	      panel.add(observedOption, gbc);
	      
	      gbc.gridx = 2;
	      gbc.gridy = y;
	      panel.add(OEoption, gbc);
	      */
	    		  
		  
		  
		  JTextField outputFileField = new JTextField("");
		  outputFileField.setPreferredSize(new Dimension(300,20));
		  
		  JButton outputFileButton = new JButton("Browse File");
		    //openPDBFileButton.setPreferredSize(new Dimension(40, 20));
		    
		  outputFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					if (fileName == null) return;
					if (CommonFunctions.isFolder(fileName)){
						fileName = fileName + "\\";
					}
					outputFileField.setText(fileName);
				}
		  });
		    
		  y++;
		  gbc.gridx = 0;
		  gbc.gridy = y;
		  panel.add(new JLabel("Output File"), gbc);
		  
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  gbc.gridwidth = 2;
		  panel.add(outputFileField, gbc);
		  
		  gbc.gridx = 3;
		  gbc.gridy = y;
		  gbc.gridwidth = 1;
		  panel.add(outputFileButton, gbc);
		  
		  
		  JButton extractButton = new JButton("Extract Contact Data");
		  extractButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
				if (outputFileField.getText().length() == 0) {
					JOptionPane.showMessageDialog(null, "Please Select Output File!");
					return;
				}
				
				ReadHiCData readHiCData = new ReadHiCData();				
				
				List<Chromosome> chromosomeList = dataset.getChromosomes();
			    Map<String, Chromosome> chromosomeMap = new HashMap<String, Chromosome>();
		        for (Chromosome c : chromosomeList) {
		            chromosomeMap.put(c.getName(), c);
		        }
		        
				Chromosome chr1 = chromosomeMap.get(chrom1List.getItemAt(chrom1List.getSelectedIndex())); 
				Chromosome chr2 = chr1;//chromosomeMap.get(chrom2List.getItemAt(chrom2List.getSelectedIndex()));
				NormalizationType norm = NormalizationType.enumValueFromString((String)normalizationList.getItemAt(normalizationList.getSelectedIndex()));
				
				String res = resolutionList.getItemAt(resolutionList.getSelectedIndex());
				HiC.Unit unit = HiC.Unit.BP;
				if (res.contains("FRAG")) unit =  HiC.Unit.FRAG;
				int binSize = Integer.parseInt(res.split("_")[1]);
				
				HiCZoom zoom = new HiCZoom(unit, binSize);
				
				String matrix = "Observed";
				//if (!matrix.equals("Observed")) matrix = "oe";
				MatrixType matrixType = MatrixType.enumValueFromString(matrix);
				String outputfile = outputFileField.getText();
				
				int chr1From = 0;
				int chr1To = chr1.getLength();
				
				int chr2From = 0;
				int chr2To = chr2.getLength();
				
				if (chr1FromField.getText().length() > 0 || chr1ToField.getText().length() > 0 /* ||
						chr2FromField.getText().length() > 0 || chr2ToField.getText().length() > 0*/){
					
					try{
						if (chr1FromField.getText().length() > 0) chr1From = Integer.max(chr1From, Integer.parseInt(chr1FromField.getText().replace(",", "")));
						if (chr1ToField.getText().length() > 0) chr1To = Integer.min(Integer.parseInt(chr1ToField.getText().replace(",", "")), chr1To);					
					}catch(Exception ex){
						JOptionPane.showMessageDialog(null, "Error! Please check if data in From or To fields are correct!");
						return;
					}
					
					//if (chr2FromField.getText().length() > 0) chr2From = Integer.max(chr2From, Integer.parseInt(chr2FromField.getText().replace(",", "")));
					//if (chr2ToField.getText().length() > 0) chr2To = Integer.min(Integer.parseInt(chr2ToField.getText().replace(",", "")), chr2To);
					
					
					ReadHiCData.setUseRegionIndices(true);				
				}else{
					ReadHiCData.setUseRegionIndices(false);
				}
				
				chr2From = chr1From;
				chr2To = chr1To;
				ReadHiCData.setRegionIndices(new int[]{chr1From, chr1To, chr2From, chr2To});
				
				readHiCData.setDataset(dataset);
				readHiCData.setMatrixType(matrixType);
				readHiCData.setNorm(norm);
				readHiCData.setZoom(zoom);
				readHiCData.setChrom1(chr1);
				readHiCData.setChrom2(chr2);
				readHiCData.setOutputFile(outputfile);
				
				
				Window win = SwingUtilities.getWindowAncestor((AbstractButton)e.getSource());
				final JDialog dialog = new JDialog(win, "Extracting data ... please wait !", ModalityType.APPLICATION_MODAL);
				dialog.setPreferredSize(new Dimension(300,80));

				ExtractHiCWorker extractDataWorker = new ExtractHiCWorker(readHiCData);
				  
				extractDataWorker.addPropertyChangeListener(new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						switch (evt.getPropertyName()){
						case "progress":
							break;
						case "state":
							switch ((StateValue)evt.getNewValue()){
							case DONE:
								
								win.setEnabled(true);
								dialog.dispose();
								
								try {
									String msg = extractDataWorker.get();
									JOptionPane.showMessageDialog(null, msg);
								} catch (InterruptedException e) {									
									e.printStackTrace();
									JOptionPane.showMessageDialog(null, "Error while extracting data");
								} catch (ExecutionException e) {									
									e.printStackTrace();
									JOptionPane.showMessageDialog(null, "Error while extracting data");
								}
								
								
								break;
							case PENDING:								
								break;
							case STARTED:
								dialog.setVisible(true);
								win.setEnabled(false);								
								break;
							default:								
								break;
							}
						}
						
					}
				  });				  
				  
				extractDataWorker.execute();
				  
				JProgressBar progressBar = new JProgressBar();
			    progressBar.setIndeterminate(true);
			    JPanel panel = new JPanel(new BorderLayout());
			      
			    panel.add(progressBar, BorderLayout.CENTER);
			    panel.add(new JLabel(""), BorderLayout.PAGE_START);
			    dialog.add(panel);
			    dialog.pack();
			    dialog.setLocationRelativeTo(win);
			    dialog.setVisible(true);			      
			}
		  });
		  
		  y++;
		  gbc.gridx = 1;
		  gbc.gridy = y;
		  panel.add(extractButton, gbc);
		  
		  
		
		  JScrollPane scrollpane = new JScrollPane(panel);
		  scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		  scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		  Frame subFrame = new JFrame();
		  subFrame.setSize(new Dimension(800, 400));
		  subFrame.setLocation(400, 400);
		
		  subFrame.add(scrollpane, BorderLayout.CENTER);
		  subFrame.setVisible(true);
		  
		  subFrame.setTitle("Extracting Contact Matrix from HiC Files");
		  
		  subFrame.addWindowListener(new WindowAdapter() {
			
			
			  @Override
			  public void windowClosing(WindowEvent e) {				  
				
			  }				
			
		  });
	  	}
  	}
  
  /**
   * 
   * @author Tuan
   *
   */
  class AnnotationAction extends NewAction{
	  int y = 0;
	  
	  AnnotationAction(){
		  super(annotateAction);
	  }
	  
	  private boolean isAlreadyDisplayedDomainTrack(Map<String, Boolean> trackStatusMap, Map<String, Boolean> trackDomainMap ){
		  for(String track:trackDomainMap.keySet()){
			if (trackDomainMap.get(track) && trackStatusMap.get(track)){
				return true;
			}
		  }
		  return false;
	  }
	  @Override
	  public void actionPerformed(ActionEvent e) {
		  	
		  	//viewer.evalString("restrict bonds not selected;select not selected;wireframe 5;color structure");
		  	
		  	Map<String, Color> trackColorMap = new HashMap<String, Color>();
		  	Map<String, String> trackFileNameMap = new HashMap<String, String>();
		  	Map<String, Boolean> trackStatusMap = new HashMap<String, Boolean>();
		  	Map<String, Boolean> trackDomainMap = new HashMap<String, Boolean>();
		  	
	    	GridBagConstraints gbc = new GridBagConstraints();
	        gbc.insets = new Insets(5, 5, 5, 5);
	        
	        JPanel panel = new JPanel(){
	        	@Override
	            public Dimension getPreferredSize() {
	                return new Dimension(650, 350);
	            }	       
	        };
	        panel.setLayout(new GridBagLayout());  
	        
	        
	        JScrollPane scrollpane = new JScrollPane(panel);
	        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	        scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	        
	        Frame subFrame = new JFrame();
	        subFrame.setSize(new Dimension(700, 400));
	        subFrame.setLocation(400, 400);
	        subFrame.setTitle("Annotate 3D Models");
	        
	        JButton colorChooserButton = new JButton("Choose color");
	        
		  	Random rd = new Random();
		  	Color defaulColor = new Color(rd.nextInt(256), rd.nextInt(256), rd.nextInt(256));		  	
		  	JLabel colorDisplay = new JLabel("Color to highlight");
		  	colorDisplay.setBackground(defaulColor);
		  	colorDisplay.setForeground(defaulColor);	
		  	
	        //colorDisplay.setEnabled(false);
		  
	    	JTextField trackNameField = new JTextField();
	    	
	    	
	    	JTextField trackFileField = new JTextField();
	    	
	    	JCheckBox isDomain = new JCheckBox("Is domain or loop?");
	    	
	    	isDomain.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (isDomain.isSelected()){
						colorChooserButton.setEnabled(false);
					}else{
						colorChooserButton.setEnabled(true);
					}					
				}
			});
	    		    	
	    	JButton openTrackFileButton = new JButton("Browse File");		    
		    
	    	openTrackFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					if (fileName == null) return;
					//if (trackNameField.getText().length() == 0) {
						String name = CommonFunctions.getFileNameFromPath(fileName);
						trackNameField.setText(name);
					//}
					
					trackFileField.setText(fileName);					
				}
			});
	    	
	    	
		    
	    	JButton runButton = new JButton("Annotate");
	    	runButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					if (viewer.getModelSetName() == null || viewer.getModelSetName().equals("Gmol")){
			    		JOptionPane.showMessageDialog(null, "Please load a model first!");
			    		return;
			    	}
					
					if (trackFileField.getText().length() == 0){
						JOptionPane.showMessageDialog(null, "Please specify the track file!");
			    		return;
					}
					if (trackNameField.getText().length() == 0){
						JOptionPane.showMessageDialog(null, "Please specify track name!");
			    		return;
					}else if (trackFileNameMap.keySet().contains(trackNameField.getText())){
						JOptionPane.showMessageDialog(null, trackNameField.getText() + " is already used, please specify a different track name!");
						return;
					}
					
					
					if (isDomain.isSelected()){
						if (isAlreadyDisplayedDomainTrack(trackStatusMap, trackDomainMap)){
							JOptionPane.showMessageDialog(null, "Only one domain or loop track can be displayed at a time, please deselect the current domain/loop track!");
							isDomain.setSelected(false);
							return;							
						}
					}
					
					Color color = colorDisplay.getBackground();
					
					if (!isDomain.isSelected() && trackColorMap.values().contains(color)){
						JOptionPane.showMessageDialog(null, "This color is already used, please choose a different color!");
						return;
					}
					
					
					if (!isDomain.isSelected()) trackColorMap.put(trackNameField.getText(), color);
					trackFileNameMap.put(trackNameField.getText(), trackFileField.getText());					
					trackStatusMap.put(trackNameField.getText(), true);
					trackDomainMap.put(trackNameField.getText(), isDomain.isSelected());
															
					viewer.setStringProperty(Constants.TRACKNAME, trackNameField.getText());					
			    	viewer.setStringProperty(Constants.TRACKFILENAME, trackFileField.getText());
			    	
			    	if (!isDomain.isSelected()) viewer.setStringProperty(Constants.ANNOTATIONCOLOR, "[" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "]");
			    	else viewer.setStringProperty(Constants.ANNOTATIONCOLOR, "");
			    	
					String script = "annotate";
					viewer.script(script);
					
										
					JCheckBox newCheckBox = new JCheckBox(trackNameField.getText());
					
					//if (!isDomain.isSelected()){
					JButton newColorChooser = new JButton("Change color");
					newColorChooser.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							Color color = JColorChooser.showDialog(null, "Choose color to highlight", defaulColor);	
							if (trackColorMap.values().contains(color)){
								JOptionPane.showMessageDialog(null, "This color is already used, please choose a different color!");
								return;
							}
					
							newCheckBox.setBackground(color);
							
							String track = newCheckBox.getText();
							trackColorMap.put(track, color);
							
							if (newCheckBox.isSelected()){
								newCheckBox.setSelected(false);
								newCheckBox.setSelected(true);
							}
						}
					});
					//}
					
					if (!isDomain.isSelected()) newCheckBox.setBackground(color);
					
					//newCheckBox.setForeground(color);
					newCheckBox.setSelected(true);
					
					
					//colorChooserButton.setEnabled(true);
					
					//newCheckBox.addActionListener(new ActionListener() {
					newCheckBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							String track = ((JCheckBox)e.getItem()).getText();
							String script = "annotate";
							if (newCheckBox.isSelected() && !trackStatusMap.get(track)){																
								
								if (trackDomainMap.get(track) && isAlreadyDisplayedDomainTrack(trackStatusMap, trackDomainMap)){
									JOptionPane.showMessageDialog(null, "Only one domain or loop track can be displayed at a time, please deselect the current domain/loop track!");
									newCheckBox.setSelected(false);
									return;
								}
								trackStatusMap.put(track, true);
								
								String trackFile = trackFileNameMap.get(track);
								Color color = trackColorMap.get(track);
								
								viewer.setStringProperty(Constants.TRACKNAME, track);
						    	viewer.setStringProperty(Constants.TRACKFILENAME, trackFile);
						    	
						    	if (!trackDomainMap.get(track)) viewer.setStringProperty(Constants.ANNOTATIONCOLOR, "[" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "]");
						    	else {
						    		viewer.setStringProperty(Constants.ANNOTATIONCOLOR, "");
						    	}
						    	
								viewer.script(script);
								
							}else{
								
								trackStatusMap.put(track, false);
								viewer.setStringProperty(Constants.TRACKNAME, "");
								viewer.script(script);
								try {
									Thread.sleep(500);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
								
								for(String trackName : trackFileNameMap.keySet()){
									if (trackStatusMap.get(trackName)){
										String trackFile = trackFileNameMap.get(trackName);
										Color color = trackColorMap.get(trackName);
										
										viewer.setStringProperty(Constants.TRACKNAME, trackName);
								    	viewer.setStringProperty(Constants.TRACKFILENAME, trackFile);
								    	
								    	if (!trackDomainMap.get(trackName))  viewer.setStringProperty(Constants.ANNOTATIONCOLOR, "[" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "]");
								    	else viewer.setStringProperty(Constants.ANNOTATIONCOLOR, "");
								    	
										viewer.script(script);
										
										try {
											Thread.sleep(500);
										} catch (InterruptedException e1) {											
											e1.printStackTrace();
										}
									}
								}								
							}
						}
					});
					
					y++;
					gbc.gridx = 0;
					gbc.gridy = y;
					gbc.gridwidth = 3;
					gbc.anchor = GridBagConstraints.WEST;
					panel.add(newCheckBox, gbc);
					
					if (!isDomain.isSelected()){
						gbc.gridx = 4;
						gbc.gridy = y;
						gbc.gridwidth = 1;
						gbc.anchor = GridBagConstraints.WEST;
						panel.add(newColorChooser, gbc);
					}
					
					//reset i
					isDomain.setSelected(false);
					
					//subFrame.setPreferredSize(preferredSize);
					
					subFrame.validate();
					subFrame.repaint();
					scrollpane.validate();
					scrollpane.repaint();
					
					//trackCheckBoxes.add(newCheckBox);
					
					
				}
			});
	    	

	        gbc.gridx = 0;
	        gbc.gridy = y;	
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Track File"), gbc);
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        trackFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(trackFileField, gbc);
	        
	        
	        gbc.gridx = 3;
	        gbc.gridy = y;
	        gbc.gridwidth = 1;	        
	        panel.add(openTrackFileButton, gbc);
	        
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	                
	        panel.add(new JLabel("Track Name"), gbc);
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        trackNameField.setPreferredSize(new Dimension(300, 21));
	        panel.add(trackNameField, gbc);
	        
	        
	        y++;
	        gbc.gridx = 1;
	        gbc.gridy = y;	        
	        panel.add(isDomain, gbc);
	        
	        
	        y++;	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 1;	        
	        panel.add(colorDisplay, gbc);
	        
	        gbc.gridx = 2;
	        gbc.gridy = y;
	        gbc.gridwidth = 1;
	        
	        
	        colorChooserButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Color color = JColorChooser.showDialog(null, "Choose color to highlight", defaulColor);	
					colorDisplay.setBackground(color);
					colorDisplay.setForeground(color);
				}
			});
	        panel.add(colorChooserButton, gbc);
	        
	        
	        y++;
	        gbc.gridx = 1;
	        gbc.gridy = y;	        
	        panel.add(runButton, gbc);
	        
	        
	        //////////////////////////////////////////////////////////////////////
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;
	        gbc.gridwidth = 4;
	        panel.add(new JLabel("------------------------------------------------ Highlighted tracks ------------------------------------------------"), gbc);
	                
	        
	        subFrame.add(scrollpane, BorderLayout.CENTER);
	        subFrame.setVisible(true);
	        
	        /*
	        subFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					viewer.setStringProperty(Constants.TRACKNAME, "");
					String script = "annotate";
					viewer.script(script);
					
				}								
			});
			*/
	        
	  }
  }
  

  /*
   * Tuan created a new button to convert PDB format file to GSS
   */
  class ConvertPDB2GSSAction extends NewAction {
	  ConvertPDB2GSSAction() {
		  super(convertPDB2GSSAction);
	  }
	  
		@Override
		public void actionPerformed(ActionEvent e) {
			//script = "pdb2GSS";
			//viewer.script(script);
			
			
		    JTextField pdbFileField = new JTextField();	        
		    
		    
		    JTextField mappingFileField = new JTextField();
		    //mappingFileField.setPreferredSize(new Dimension(400, 20));
		    
		    JTextField gssFileField = new JTextField();
		    //gssFileField.setPreferredSize(new Dimension(400, 20));
		    	        
		    JButton openPDBFileButton = new JButton("Browse File");
		    //openPDBFileButton.setPreferredSize(new Dimension(40, 20));
		    
		    openPDBFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					pdbFileField.setText(fileName);
					gssFileField.setText(fileName.replace(".pdb", ".gss"));
				}
			});
		    
		    
		    JButton openMappingFileButton = new JButton("Browse File");
		    //openMappingFileButton.setPreferredSize(new Dimension(40, 20));
		    openMappingFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					mappingFileField.setText(fileName);
				}
			});
		    
		    
		    
		    JButton openGSSFileButton = new JButton("Browse File");
		    //openGSSFileButton.setPreferredSize(new Dimension(40, 20));
		    openGSSFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					gssFileField.setText(fileName);
				}
			});
		    	        
		    
		    GridBagConstraints gbc = new GridBagConstraints();
		    gbc.insets = new Insets(5, 5, 5, 5);
		    
		    JPanel panel = new JPanel(){
		    	@Override
		        public Dimension getPreferredSize() {
		            return new Dimension(600, 120);
		        }	       
		    };
		            
		    
		    panel.setLayout(new GridBagLayout());
		    
		    int y = 0;
		    	        
		    gbc.gridx = 0;
		    gbc.gridy = y;	                
		    panel.add(new JLabel("Input PDB File"), gbc);
		    
		    gbc.gridx = 1;
		    gbc.gridy = y;
		    pdbFileField.setPreferredSize(new Dimension(300, 21));
		    panel.add(pdbFileField, gbc);
		    	        
		    
		    gbc.gridx = 2;
		    gbc.gridy = y;	        
		    panel.add(openPDBFileButton, gbc);
		    	        
		   	y++;
		    gbc.gridx = 0;
		    gbc.gridy = y;	        	        
		    panel.add(new JLabel("Input Mapping File"), gbc);	        
		
		    gbc.gridx = 1;
		    gbc.gridy = y;
		    mappingFileField.setPreferredSize(new Dimension(300, 21));
		    panel.add(mappingFileField, gbc);
		    
		    gbc.gridx = 2;
		    gbc.gridy = y;	
		    panel.add(openMappingFileButton, gbc);
		    
		    y++;
		    gbc.gridx = 0;
		    gbc.gridy = y;	
		    panel.add(new JLabel("Output GSS File"), gbc);
		    
		
		    gbc.gridx = 1;
		    gbc.gridy = y;
		    gssFileField.setPreferredSize(new Dimension(300, 21));
		    panel.add(gssFileField, gbc);
		    
		    gbc.gridx = 2;
		    gbc.gridy = y;
		    gbc.gridwidth = 1;	
		    panel.add(openGSSFileButton, gbc);
		    
		   
		    /*
		    int result = JOptionPane.showConfirmDialog(null, panel, "Convert PDB to GSS",
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE );
		    */
		    
		    Frame subFrame = new JFrame();
	        subFrame.setSize(new Dimension(600, 200));
	        subFrame.setLocation(400, 400);
	        
	        subFrame.add(panel);
	        subFrame.setVisible(true);
	        subFrame.setTitle("Converting .pdb Files to .gss Files");
	        
	        JButton runButton = new JButton("Convert");
	    	runButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					viewer.setStringProperty(Constants.INPUTPDBFILE, pdbFileField.getText());
			    	viewer.setStringProperty(Constants.INPUTMAPPINGFILE, mappingFileField.getText());
			    	viewer.setStringProperty(Constants.OUTPUTGSSFILE, gssFileField.getText());
			    	
			    	script = "pdb2GSS";
			    	viewer.script(script);	
					
			    	
				}
			});
	    	
	    	y++;
	    	gbc.gridx = 1;
			gbc.gridy = y;
			gbc.gridwidth = 1;
	    	panel.add(runButton, gbc);
	    	
		}
  }

  /*
   * Tuan created a new button to identify loops
   */
  class LoopDetectorAction extends NewAction {
	  LoopDetectorAction() {
		  super(loopDetectorAction);
	  }
	  
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        	

	    	
	    	JTextField outputFileField = new JTextField();
	    	JButton runButton = new JButton("Identify loops");
	    	runButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
			    	if (viewer.getModelSetName() == null || viewer.getModelSetName().equals("Gmol")){
			    		JOptionPane.showMessageDialog(null, "Please load a model first!");
			    		return;
			    	}
			    	
			    	viewer.setStringProperty(Constants.OUTPUTLOOPFILE, outputFileField.getText());
			    	
		        	script = "loopDetector";        	
			    	viewer.script(script);
					
				}
			});
	    	
	    	GridBagConstraints gbc = new GridBagConstraints();
	        gbc.insets = new Insets(5, 5, 5, 5);
	        
	        JPanel panel = new JPanel(){
	        	@Override
	            public Dimension getPreferredSize() {
	                return new Dimension(600, 120);
	            }	       
	        };
	        panel.setLayout(new GridBagLayout());  
	        
	        gbc.gridx = 0;
	        gbc.gridy = 0;	                
	        panel.add(new JLabel("Output File(optional)"), gbc);
	        
	        gbc.gridx = 1;
	        gbc.gridy = 0;
	        gbc.gridwidth = 2;
	        outputFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(outputFileField, gbc);
	        
	        
	        gbc.gridx = 3;
	        gbc.gridy = 0;
	        gbc.gridwidth = 1;
	        JButton openFileButton = new JButton("Browse File");	        
	        openFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					outputFileField.setText(fileName);
				}
			});
	        panel.add(openFileButton, gbc);
	        
	        
	        gbc.gridx = 1;
	        gbc.gridy = 1;
	        //gbc.gridwidth
	        panel.add(runButton, gbc);
	        
	        Frame subFrame = new JFrame();
	        subFrame.setSize(new Dimension(600, 200));
	        subFrame.setLocation(400, 400);
	        
	        subFrame.add(panel);
	        subFrame.setVisible(true);
	        subFrame.setTitle("Identify Chromatin Loops in 3D Models");
	        
	         
	    }
  }

  
  /*
   * Tuan created a new button for LorDG function
   */
  class LorDG3DModeller extends NewAction {
	  LorDG3DModeller() {
		  super(lorDG3DModellerAction);
	  }
	  
	    @Override
	    public void actionPerformed(ActionEvent e) {
	    	//script = "pdb2GSS";
	    	//viewer.script(script);
	    	
	    	
	        JTextField inputContactFileField = new JTextField();	        
	        
	        
	        JTextField outputGSSFileField = new JTextField();
	        //gssFileField.setPreferredSize(new Dimension(400, 20));
	        	        
	        JButton openContactFileButton = new JButton("Browse File");
	        //openPDBFileButton.setPreferredSize(new Dimension(40, 20));
	        
	        openContactFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					inputContactFileField.setText(fileName);
					//outputGSSFileField.setText(fileName.replace(".txt", ".gss"));
				}
			});
	        
	        
	        JButton outputGSSFileButton = new JButton("Browse File");
	        //openMappingFileButton.setPreferredSize(new Dimension(40, 20));
	        outputGSSFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					//viewerOptions.put(Constants.ISCHOOSINGFOLDER, true);
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					outputGSSFileField.setText(fileName);
					//viewerOptions.remove(Constants.ISCHOOSINGFOLDER);
				}
			});
	        
	        	        	        
	        
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.insets = new Insets(5, 5, 5, 5);
	        
	        JPanel panel = new JPanel(){
	        	@Override
	            public Dimension getPreferredSize() {
	                return new Dimension(450, 350);
	            }	       
	        };	                
	        
	        panel.setLayout(new GridBagLayout());  	        
	        
	        int y = 0;
	        ////////////////////////////////////////////////	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	                
	        panel.add(new JLabel("Input Contact File"), gbc);
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        inputContactFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(inputContactFileField, gbc);
	        	        
	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(openContactFileButton, gbc);
	        	        
	       	////////////////////////////////////////////////
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	 
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Output 3D Model Folder"), gbc);	        
	
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        outputGSSFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(outputGSSFileField, gbc);
	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	
	        gbc.gridwidth = 1;
	        panel.add(outputGSSFileButton, gbc);
	        ///////////////////////////////////////////////
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Conversion Factor"), gbc);	        
	        
	        
	        NumberFormatter doubleFormatter = new NumberFormatter(NumberFormat.getNumberInstance());			 
	        doubleFormatter.setValueClass(Double.class);
	        doubleFormatter.setMinimum(0.0);
	        doubleFormatter.setMaximum(4.0);
	        //doubleFormatter.setAllowsInvalid(false);
	        
			//JFormattedTextField conversionFactorField = new JFormattedTextField(doubleFormatter);
	        JTextField conversionFactorField = new JTextField();
			conversionFactorField.setText("1.0");
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        conversionFactorField.setPreferredSize(new Dimension(300, 21));
	        panel.add(conversionFactorField, gbc);
	        
	        ///////////////////////////////////////////////	  
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Initial Learning Rate:"), gbc);	        
	        
	        NumberFormatter learningRateFormatter = new NumberFormatter(NumberFormat.getNumberInstance());			 
	        learningRateFormatter.setValueClass(Double.class);
	        learningRateFormatter.setMinimum(0.0);
	        learningRateFormatter.setMaximum(10.0);
	        //learningRateFormatter.setAllowsInvalid(false);
	        
	        
	        JFormattedTextField learningRateField = new JFormattedTextField(learningRateFormatter);
	        learningRateField.setText("1.0");
	        
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        learningRateField.setPreferredSize(new Dimension(300, 21));
	        panel.add(learningRateField, gbc);
	        
	        ///////////////////////////////////////////////	
	        
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Max Number of Iteration"), gbc);	        
	        
	        NumberFormatter intFormatter = new NumberFormatter(NumberFormat.getNumberInstance());			 
	        intFormatter.setValueClass(Integer.class);
	        intFormatter.setMinimum(0);
	        intFormatter.setMaximum(1000000);
	        intFormatter.setAllowsInvalid(false);
	        
	        //JTextField maxIterationField = new JTextField("1000");
	        JFormattedTextField maxIterationField = new JFormattedTextField(intFormatter);
	        maxIterationField.setText("1000");
	        
	        	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        maxIterationField.setPreferredSize(new Dimension(300, 21));
	        panel.add(maxIterationField, gbc);
	        
			///////////////////////////////////////////
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Chromosome (Optional)"), gbc);	        

	        JTextField chromosomeField = new JTextField("X");	        
	        	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        chromosomeField.setPreferredSize(new Dimension(300, 21));
	        panel.add(chromosomeField, gbc);
	        
	        chromosomeField.setInputVerifier(new InputVerifier() {
				
				@Override
				public boolean verify(JComponent input) {
					Set<String> validGenomeIDs = new HashSet<String>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8","9","10","11","12","13","14","15","16","17",
							"18","19","20","21","22","23","X","Y"));
					
					JTextField field = (JTextField) input;
					if (field.getText().length() == 0 || validGenomeIDs.contains(field.getText())) return true;
					
					return false;
				}
			});

	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("(1,2,3,X or Y)"), gbc);	
	        
	        //////////////////////////////////////////
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Genome ID (Optional)"), gbc);	        

	        JTextField genomeField = new JTextField("hg19");
	               
	        	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        genomeField.setPreferredSize(new Dimension(300, 21));
	        panel.add(genomeField, gbc);
	        genomeField.setInputVerifier(new InputVerifier() {
				
				@Override
				public boolean verify(JComponent input) {
					Set<String> validGenomeIDs = new HashSet<String>(Arrays.asList("hg18", "hg19", "hg38", "dMel", "mm9", "mm10", "anasPlat1", "bTaurus3",
							"canFam3", "equCab2", "galGal4", "Pf3D7", "sacCer3", "sCerS288c", "susScr3", "TAIR10"));
					
					JTextField field = (JTextField) input;
					if (validGenomeIDs.contains(field.getText())) return true;
					
					return false;
				}
			});
	        
	        //////////
			
			y++;
			JCheckBox isMultipleChrom = new JCheckBox("Is Multiple-Chromosomes Structure?");
			gbc.gridx = 0;
			gbc.gridy = y;
			gbc.gridwidth = 2;
			panel.add(isMultipleChrom, gbc);
			
			JLabel chromLenLabel = new JLabel("Length of Chromosomes");
			JTextField chromLengthField = new JTextField("229,241,197,190,179,169,157,145,124,135,133,132,98,89,83,81,79,77,57,62,36,36,153,29");
			JLabel chromLenNoteLabel = new JLabel("Numbers separated by ,");
			
			isMultipleChrom.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					if (isMultipleChrom.isSelected()){
						chromLenLabel.setVisible(true);
						chromLengthField.setVisible(true);
						chromLenNoteLabel.setVisible(true);
					}else{
						chromLenLabel.setVisible(false);
						chromLengthField.setVisible(false);
						chromLenNoteLabel.setVisible(false);
					}
					
				}
			});

	        ///////////////////////////////////////////	        
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        
	        chromLenLabel.setVisible(false);
	        panel.add(chromLenLabel, gbc);	
	        	        
	        chromLengthField.setPreferredSize(new Dimension(300, 21));
	        
	       
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 2;
	        panel.add(chromLengthField, gbc);
	        
	        chromLengthField.setVisible(false);
	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        
	        chromLenNoteLabel.setVisible(false);
	        panel.add(chromLenNoteLabel, gbc);		 
	        

	        
	        ///////////////////////////////////////////////
	        y++;
	        JButton runButton = new JButton("Run");
	        JButton stopButton = new JButton("Stop");
	       
	        	     
	        gbc.gridx = 1;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;	   
	        runButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(runButton, gbc);
	        
	        gbc.gridx = 2;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;
	        stopButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(stopButton, gbc);
	        	        	        
	        
	        Frame lorDGFrame = new JFrame();
	        lorDGFrame.setSize(new Dimension(650, 400));
	        lorDGFrame.setLocation(400, 400);
	        
	        lorDGFrame.add(panel);
	        lorDGFrame.setVisible(true);
	        lorDGFrame.setTitle("Reconstruct 3D Models from Contact Matrices");
	        
	        
	        
	        runButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					int maxIteration = Integer.parseInt(maxIterationField.getText().replace(",", ""));
					if (maxIteration > 1e6){
						JOptionPane.showMessageDialog(null, "This is going to take a long time, please reset it!");
						maxIterationField.setText("1000");
						return;
					}
					
					double conversion = 0.0;
					double minConversion = 0.1, maxConversion = 3.0;
					if (conversionFactorField.getText().length() > 0){
						
						if (!conversionFactorField.getText().contains("-")){
							
							try{
								conversion = Double.parseDouble(conversionFactorField.getText().replace(",", ""));
							}catch(Exception ex){
								JOptionPane.showMessageDialog(null, "Please put a number!");
								conversionFactorField.setText("1.0");
								return;
							}
							
							if (conversion < 0.2 && conversion > 3.5){
								JOptionPane.showMessageDialog(null, "Please reconsider this conversion factor, it seems unrealistic!");
								conversionFactorField.setText("1.0");
								return;
							}
							
						}else{
							String[] st = conversionFactorField.getText().split("[-\\s+]+");
							if (st.length != 2){
								JOptionPane.showMessageDialog(null, "Please specify the range with this format: 0.5 - 1.0");
								conversionFactorField.setText("0.5 - 1.0");
								return;
							}
							
							try{
								minConversion = Double.parseDouble(st[0]);
								maxConversion = Double.parseDouble(st[1]);
							}catch(Exception ex){
								JOptionPane.showMessageDialog(null, "Please put numbers in the conversion factor range!");
								conversionFactorField.setText("0.5 - 1.0");
								return;
							}
						}
					}
					
					if (!CommonFunctions.isFile(inputContactFileField.getText())){
						JOptionPane.showMessageDialog(null, "Please specify a contact file as input");
						return;
					}
					if (outputGSSFileField.getText().length() == 0 || !CommonFunctions.isFolder(outputGSSFileField.getText())){
						JOptionPane.showMessageDialog(null, "Please specify an output folder");
						return;
					}
					
					if (isMultipleChrom.isSelected()){
						String st = chromLengthField.getText();
						for(int i = 0 ; i < st.length(); i++){
							if ((st.charAt(i) < '0' || st.charAt(i) > '9') && st.charAt(i) != ',') {
								JOptionPane.showMessageDialog(null, "Chromosome lengths field should contain numbers and/or , only! please correct it");
								return;
							};
						}					
					}
					
					viewer.setStringProperty(Constants.INPUTCONTACTFILE, inputContactFileField.getText());
		        	viewer.setStringProperty(Constants.OUTPUT3DFILE, outputGSSFileField.getText());
		        	
		        	if (conversion > 0){
		        		viewer.setStringProperty(Constants.CONVERSIONFACTOR, conversion + "");
		        		viewer.setStringProperty(Constants.MINCONVERSIONFACTOR, "");
		        		viewer.setStringProperty(Constants.MAXCONVERSIONFACTOR, "");
		        	}else{
		        		viewer.setStringProperty(Constants.CONVERSIONFACTOR, "");
		        		viewer.setStringProperty(Constants.MINCONVERSIONFACTOR, minConversion + "");
		        		viewer.setStringProperty(Constants.MAXCONVERSIONFACTOR, maxConversion + "");
		        	}
		        	
		        	viewer.setStringProperty(Constants.MAXITERATION, maxIterationField.getText().replace(",", ""));
		        	
		        	if (isMultipleChrom.isSelected()) viewer.setStringProperty(Constants.CHROMOSOME, "1");
		        	else {
		        		if (chromosomeField.getText().length() != 0) viewer.setStringProperty(Constants.CHROMOSOME, chromosomeField.getText());
		        		else if (chromosomeField.getText().length() != 0) viewer.setStringProperty(Constants.CHROMOSOME, "1");
		        	}
		        	
		        	viewer.setStringProperty(Constants.GENOMEID, genomeField.getText());
		        	
		        	if (isMultipleChrom.isSelected()) viewer.setStringProperty(Constants.CHROMOSOMELEN, chromLengthField.getText());
		        	else viewer.setStringProperty(Constants.CHROMOSOMELEN, "");
		        	
		        	viewer.setStringProperty(Constants.LEARNINGRATE, learningRateField.getText().replace(",", ""));
		        	
		        	script = "lorDG";
			    	viewer.script(script);	 
			    	
				}
			});
	        
	        stopButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					if (viewer.getInput3DModeller() != null){
						viewer.getInput3DModeller().setStopRunning(true);
					}
					
				}
			});
	        
	        
	        
	            
	    }
  }

  /*
   *  Tosin created a new button for 3DMax Modeller
   */
  class Structure_3DMaxModeller extends NewAction{
	  Structure_3DMaxModeller() {
		  super(structure3DMAXAction);
	  }
	  
	  @Override
	    public void actionPerformed(ActionEvent e) {
			    	
	    	
	        JTextField inputContactFileField = new JTextField();      
	        
	        JTextField outputGSSFileField = new JTextField();
	               
	        JButton openContactFileButton = new JButton("Browse File");
	        
	        openContactFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					inputContactFileField.setText(fileName);
					//outputGSSFileField.setText(fileName.replace(".txt", ".gss"));
				}
			});
	        
	        
	        JButton outputGSSFileButton = new JButton("Browse File");
	        //openMappingFileButton.setPreferredSize(new Dimension(40, 20));
	        outputGSSFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					//viewerOptions.put(Constants.ISCHOOSINGFOLDER, true);
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					outputGSSFileField.setText(fileName);
					//viewerOptions.remove(Constants.ISCHOOSINGFOLDER);
				}
			});
	        
	        	        	        
	        
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.insets = new Insets(5, 5, 5, 5);
	        
	        JPanel panel = new JPanel(){
	        	@Override
	            public Dimension getPreferredSize() {
	                return new Dimension(450, 350);
	            }	       
	        };	                
	        
	        panel.setLayout(new GridBagLayout());  	        
	        
	        int y = 0;
	        ////////////////////////////////////////////////	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	                
	        panel.add(new JLabel("Input contact file:",JLabel.LEFT), gbc);
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        inputContactFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(inputContactFileField, gbc);
	        
	        	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(openContactFileButton, gbc);
	        	        
	     
	        
	        ///////////////////////////////////////////////
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        
	        JLabel res = new JLabel("Data Resolution[1MB or 10KB]:",JLabel.LEFT);
	        
	        panel.add(res, gbc);	        
	        
	        JTextField IFResolutionField = new JTextField("1000000"); 
	      
	        IFResolutionField.addKeyListener(new KeyAdapter(){
	        	@Override
				public void keyReleased(KeyEvent e) {
	        		String currentTxt = IFResolutionField .getText();
					if (currentTxt.length() == 0) return;
					
	        		char chr = currentTxt.charAt(currentTxt.length() - 1);
					
					if ((!Character.isDigit(chr) && chr != '.') || (chr == '.' && currentTxt.substring(0, currentTxt.length() - 1).contains("."))){
						JOptionPane.showMessageDialog(null, "Please key in numbers only, 1000000 = 1MB, 10000 = 10KB");
						
						 IFResolutionField.setText(currentTxt.substring(0, currentTxt.length() - 1));
					}
				}	
	        });
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        IFResolutionField.setPreferredSize(new Dimension(300, 21));
	        panel.add( IFResolutionField, gbc);
	        
	        res.setVisible(false);
	        IFResolutionField.setVisible(false);     
	        ////////////////////////////////////////////////
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	 
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Output 3D model folder:"), gbc);	        
	
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        outputGSSFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(outputGSSFileField, gbc);
	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	
	        gbc.gridwidth = 1;
	        panel.add(outputGSSFileButton, gbc);
	        ///////////////////////////////////////////////
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        JLabel ConvFactormin =  new JLabel("Conversion Factor:");
	        panel.add(ConvFactormin, gbc);	        
	        
	        JTextField minconversionFactorField = new JTextField("0.4"); 
	        
	        minconversionFactorField.addKeyListener(new KeyAdapter(){
	        	@Override
				public void keyReleased(KeyEvent e) {
	        		String currentTxt = minconversionFactorField.getText();
					if (currentTxt.length() == 0) return;
					
	        		char chr = currentTxt.charAt(currentTxt.length() - 1);
					
					if ((!Character.isDigit(chr) && chr != '.') || (chr == '.' && currentTxt.substring(0, currentTxt.length() - 1).contains("."))){
						JOptionPane.showMessageDialog(null, "Please key in number only");
						
						minconversionFactorField.setText(currentTxt.substring(0, currentTxt.length() - 1));
					}
				}	
	        });
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        minconversionFactorField.setPreferredSize(new Dimension(300, 21));
	        panel.add(minconversionFactorField, gbc);
	       
	        
			////////////////////////////////////////////////
	        
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        JLabel ConvFactormax =  new JLabel("Conversion Factor (Max):");
	        panel.add(ConvFactormax, gbc);	         
	        
	        JTextField maxconversionFactorField = new JTextField("2.0"); 
	        
	        maxconversionFactorField.addKeyListener(new KeyAdapter(){
	        	@Override
				public void keyReleased(KeyEvent e) {
	        		String currentTxt = maxconversionFactorField.getText();
					if (currentTxt.length() == 0) return;
					
	        		char chr = currentTxt.charAt(currentTxt.length() - 1);
					
					if ((!Character.isDigit(chr) && chr != '.') || (chr == '.' && currentTxt.substring(0, currentTxt.length() - 1).contains("."))){
						JOptionPane.showMessageDialog(null, "Please key in number only");
						
						maxconversionFactorField.setText(currentTxt.substring(0, currentTxt.length() - 1));
					}
				}	
	        });
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        maxconversionFactorField.setPreferredSize(new Dimension(300, 21));
	        panel.add(maxconversionFactorField, gbc);        
	           
	        ConvFactormax.setVisible(false);
	        maxconversionFactorField.setVisible(false);
	        
	        
	        ///////////////////////////////////////////////	  
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Learning rate:"), gbc);	        
	        
	        JTextField learningRateField = new JTextField("1"); 
	        
	        learningRateField.addKeyListener(new KeyAdapter(){
	        	@Override
				public void keyReleased(KeyEvent e) {
	        		String currentTxt = learningRateField.getText();
					if (currentTxt.length() == 0) return;
					
	        		char chr = currentTxt.charAt(currentTxt.length() - 1);
					
					if ((!Character.isDigit(chr) && chr != '.') || (chr == '.' && currentTxt.substring(0, currentTxt.length() - 1).contains("."))){
						JOptionPane.showMessageDialog(null, "Please key in number only");
						
						learningRateField.setText(currentTxt.substring(0, currentTxt.length() - 1));
					}
				}	
	        });
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        learningRateField.setPreferredSize(new Dimension(300, 21));
	        panel.add(learningRateField, gbc);	        	        
	
	        ///////////////////////////////////////////////	
	        
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Max Number of Iteration:"), gbc);	        
	      	        
	        JTextField maxIterationField = new JTextField("1000"); 	
	        maxIterationField.addKeyListener(new KeyAdapter() {								
				@Override
				public void keyReleased(KeyEvent e) {
					
					String currentTxt = maxIterationField.getText();
					if (currentTxt.length() == 0) return;
					
	        		char chr = currentTxt.charAt(currentTxt.length() - 1);
	        		
					if (!Character.isDigit(chr)){
						JOptionPane.showMessageDialog(null, "Please key in number only");						
						maxIterationField.setText(currentTxt.substring(0, currentTxt.length() - 1));
					}
				}				
			});
	        	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        maxIterationField.setPreferredSize(new Dimension(300, 21));
	        panel.add(maxIterationField, gbc);             
	        
	      ///////////////////////////////////////////////	
		y++;
		JCheckBox isMatrix = new JCheckBox("Load input as a Normalized Contact Matrix(comma seperated)");
		gbc.gridx = 1;
		gbc.gridy = y;
		gbc.gridwidth = 2;
		panel.add(isMatrix , gbc);
				
		isMatrix .addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if (isMatrix.isSelected()){					
					  res.setVisible(true);
				      IFResolutionField.setVisible(true);
				}
				else {					
					  res.setVisible(false);
				      IFResolutionField.setVisible(false);
				}
				
				
			}
		});
	        
	        /////////////////////////////////////////////// 
			
			y++;
			JCheckBox clusterTAD = new JCheckBox("Specify a Minimum and Maximum Conversion Factor");
			gbc.gridx = 1;
			gbc.gridy = y;
			gbc.gridwidth = 2;
			panel.add(clusterTAD , gbc);
					
			clusterTAD .addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					if (clusterTAD.isSelected()){
						
						 ConvFactormin.setText("Conversion Factor (Min):");
						 ConvFactormax.setVisible(true);
					     maxconversionFactorField.setVisible(true);
					  
					}else{
						 ConvFactormin.setText("Conversion Factor:");
						 ConvFactormax.setVisible(false);
					     maxconversionFactorField.setVisible(false);
					}
					
				}
			});
	        	        
			JOptionPane.showMessageDialog(null, "On average for this algorithm, Best structures are produced between Min = 0.3 and Max = 0.6");
	        y++;
	        JButton runButton = new JButton("Run");
	        JButton stopButton = new JButton("Stop");
	       	        	     
	        gbc.gridx = 1;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;	   
	        runButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(runButton, gbc);
	        
	        gbc.gridx = 2;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;
	        stopButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(stopButton, gbc);
	        	        	        
	        
	        Frame Structure_3DMaxFrame = new JFrame();
	        Structure_3DMaxFrame.setSize(new Dimension(670,380));
	        Structure_3DMaxFrame.setLocation(400, 400);
	        
	        Structure_3DMaxFrame.add(panel);
	        Structure_3DMaxFrame.setVisible(true);
	        
	        
	        
	        
	        runButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {					
					Input.stoprunning = false;
					double minlearn = Double.parseDouble(minconversionFactorField.getText());
					double maxlearn = Double.parseDouble(maxconversionFactorField.getText());
					String input = inputContactFileField.getText();
					String output = outputGSSFileField.getText();
					double learningRate = Double.parseDouble(learningRateField.getText());
					int maxIteration = Integer.parseInt(maxIterationField.getText());
					String inputdata_type = Input.inputtype_Tuple;
					if (maxIteration > 10000){
						JOptionPane.showMessageDialog(null, "This is going to take a long time, please reset it!, Maximum is 10000");
						maxIterationField.setText("5000");
						return;
					}
					if (input == null || input.trim().equals("") || output == null || output.trim().equals("") ) {
						JOptionPane.showMessageDialog(null, "Input file or Output path Unspecified or Incorrect, Please make sure these fields are filled correctly !");						
						return;
					}
					
					if (maxlearn < minlearn) {
						JOptionPane.showMessageDialog(null, "Maximum conversion factor cannot be less than minimum!");
						maxconversionFactorField.setText("2.0");
						return;
					}
							
					if (minlearn < 0.1 || minlearn > 2 || maxlearn < 0.1 || maxlearn > 2) {
						JOptionPane.showMessageDialog(null, "The minimum or maximum conversion factor is out of range for this algorithm");
						JOptionPane.showMessageDialog(null, "Value Reset done, Min = 0.1 and Max = 2.0");
						minconversionFactorField.setText("0.1");
						maxconversionFactorField.setText("2.0");
						return;
					}
										
					
					if (learningRate > 1) {
						JOptionPane.showMessageDialog(null, "The learning Range is out of range for this algorithm. For this algorithm, learning Rate cannot be greater than 1");
						JOptionPane.showMessageDialog(null, "Value Reset done, Conversion factor = 1");
						learningRateField.setText("1");
						return;
					}
					
					if (isMatrix.isSelected()){
						inputdata_type = Input.inputtype_Matrix; // 1						
					} 
					
					
					viewer.setStringProperty(Constants.INPUTCONTACTFILE, inputContactFileField.getText());
		        	viewer.setStringProperty(Constants.OUTPUT3DFILE, outputGSSFileField.getText());		        	
		        	viewer.setStringProperty(Constants.MINCONVERSIONFACTOR, minconversionFactorField.getText());
		        	viewer.setStringProperty(Constants.MAXCONVERSIONFACTOR, minconversionFactorField.getText());
		        	viewer.setStringProperty(Constants.LEARNINGRATE, learningRateField.getText());
		        	viewer.setStringProperty(Constants.MAXITERATION, maxIterationField.getText());
		        	viewer.setStringProperty(Constants.IFRESOLUTION,  IFResolutionField.getText());
		        	viewer.setStringProperty(Constants.ISMATRIX, inputdata_type);
		        	if (clusterTAD.isSelected()) viewer.setStringProperty(Constants.MAXCONVERSIONFACTOR, maxconversionFactorField.getText());
		        	
		        	
		        	
		        	script = "struct_3DMax";
			    	viewer.script(script);	 
			    	
			    	
			    	
				}
			});
	        
	        stopButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
				// set stop running to true					
				Input.stoprunning = true;
				
				}
			});
	        
	  }
  }

  
  /*
   *  Tosin created a new button for Visualize HEatmap
   */
  class HeatmapVisualizeAction extends NewAction{
	  HeatmapVisualizeAction() {
		  super(heatmap2DVisualizeAction);
	  }
	  
	  @Override
	    public void actionPerformed(ActionEvent e) {
			 //Launch the Heatmap Interface   	
		    script = "Heatmap2D";
	    	viewer.script(script);	
	  }
  }
  
  
  
  
  /*
   *  Tosin created a new button for 3DMax Modeller
   */
  public class FindTADAction extends NewAction{
	  public FindTADAction() {
		  super(findTadAction);
	  }
	  
	  @Override
	    public void actionPerformed(ActionEvent e) {
			    	
	    	
	        JTextField inputContactFileField = new JTextField();      
	        
	        JTextField outputGSSFileField = new JTextField();
	               
	        JButton openContactFileButton = new JButton("Browse File");
	        
	        openContactFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					inputContactFileField.setText(fileName);
					//outputGSSFileField.setText(fileName.replace(".txt", ".gss"));
				}
			});
	        
	        
	        JButton outputGSSFileButton = new JButton("Browse File");
	        //openMappingFileButton.setPreferredSize(new Dimension(40, 20));
	        outputGSSFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					//viewerOptions.put(Constants.ISCHOOSINGFOLDER, true);
					String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
					        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
					
					outputGSSFileField.setText(fileName);
					//viewerOptions.remove(Constants.ISCHOOSINGFOLDER);
				}
			});
	        
	        	        	        
	        
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.insets = new Insets(5, 5, 5, 5);
	        
	        JPanel panel = new JPanel(){
	        	@Override
	            public Dimension getPreferredSize() {
	                return new Dimension(450, 350);
	            }	       
	        };	                
	        
	        panel.setLayout(new GridBagLayout());  	        
	        
	        int y = 0;
	        ////////////////////////////////////////////////	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	                
	        panel.add(new JLabel("Input contact file:",JLabel.LEFT), gbc);
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        inputContactFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(inputContactFileField, gbc);
	        
	        	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(openContactFileButton, gbc);
	        	       
	                     
	        
	        ///////////////////////////////////////////////
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        
	        JLabel resolution =  new JLabel("Data Resolution:",JLabel.LEFT);	
	        resolution.setVisible(false);
	        panel.add(resolution, gbc);	          
	        
	        JTextField IFResolutionField = new JTextField("40000"); 
	        IFResolutionField.setVisible(false);
	        IFResolutionField .addKeyListener(new KeyAdapter(){
	        	@Override
				public void keyReleased(KeyEvent e) {
	        		String currentTxt = IFResolutionField .getText();
					if (currentTxt.length() == 0) return;
					
	        		char chr = currentTxt.charAt(currentTxt.length() - 1);
					
					if ((!Character.isDigit(chr) && chr != '.') || (chr == '.' && currentTxt.substring(0, currentTxt.length() - 1).contains("."))){
						JOptionPane.showMessageDialog(null, "Please key in numbers only, 1000000 = 1MB, 10000 = 10KB","Alert!",JOptionPane.ERROR_MESSAGE);
						
						 IFResolutionField.setText(currentTxt.substring(0, currentTxt.length() - 1));
					}
				}	
	        });
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        IFResolutionField.setPreferredSize(new Dimension(300, 21));
	        panel.add( IFResolutionField, gbc);
	        	      
	        ////////////////////////////////////////////////
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	 
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Output folder:",JLabel.LEFT), gbc);	        
	
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        outputGSSFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(outputGSSFileField, gbc);
	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	
	        gbc.gridwidth = 1;
	        panel.add(outputGSSFileButton, gbc);
	
	            
	        
	      ///////////////////////////////////////////////	
			y++;
			JCheckBox isMatrix = new JCheckBox("Input IsMatrix ?");
			gbc.gridx = 0;
			gbc.gridy = y;
			gbc.gridwidth = 2;
			panel.add(isMatrix , gbc);
			isMatrix .addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					if (isMatrix.isSelected()){
						resolution.setVisible(true);
						IFResolutionField.setVisible(true);
					}
					
				}
			});   
	        /////////////////////////////////////////////// 
			
			//y++;
			JCheckBox clusterTAD = new JCheckBox("Run ClusterTAD Algorithm");
			gbc.gridx = 2;
			gbc.gridy = y;
			gbc.gridwidth = 2;
			
			clusterTAD.setSelected(true);
			panel.add(clusterTAD , gbc);
			        	        
	        //////////////////////////////////////////////
	        y++;
	        JButton runButton = new JButton("Run");
	        JButton stopButton = new JButton("Stop");
	       	        	     
	        gbc.gridx = 1;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;	   
	        runButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(runButton, gbc);
	        
	        gbc.gridx = 2;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;
	        stopButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(stopButton, gbc);
	        	        	        
	        
	        Frame Structure_3DMaxFrame = new JFrame();
	        Structure_3DMaxFrame.setSize(new Dimension(580,210));
	        Structure_3DMaxFrame.setLocation(400, 400);
	        
	        Structure_3DMaxFrame.add(panel);
	        Structure_3DMaxFrame.setVisible(true);
	        
	        
	        
	        
	        runButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {					
					Parameter.stoprunning = false;					
					String input = inputContactFileField.getText();
					String output = outputGSSFileField.getText();		
					int resolution = Integer.parseInt(IFResolutionField.getText());
					String inputdata_type = Parameter.inputtype_Tuple;
					String res = "";
					String algorithm= "";
					String startlocation = "0";
					if (input == null || input.trim().equals("") || output == null || output.trim().equals("") ) {
						JOptionPane.showMessageDialog(null, "Input file or Output path Unspecified or Incorrect, Please make sure these fields are filled correctly !","Alert!",JOptionPane.ERROR_MESSAGE);						
						return;
					}
					
					if (IFResolutionField.isVisible()) {
						if ( resolution > 100000 ) {
							JOptionPane.showMessageDialog(null, "Resolution too High for TAD identification. Minimum Resolution = 100KB ","Alert!",JOptionPane.ERROR_MESSAGE);
							IFResolutionField.setText("50000");
							return;
						}
					}
					
					if (isMatrix.isSelected()){
						inputdata_type = Input.inputtype_Matrix; // 1	
						viewer.setStringProperty(Constants.IFRESOLUTION,  IFResolutionField.getText());
						viewer.setStringProperty(Constants.STARTLOCATION,  startlocation);
					} else {
						try {
							int Res = LoadHeatmap.Resolution(input);
							res = String.valueOf(Res);
							int sloc = LoadHeatmap.Startlocation;
							startlocation = String.valueOf(sloc);
							
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						viewer.setStringProperty(Constants.IFRESOLUTION, res);
						viewer.setStringProperty(Constants.STARTLOCATION,  startlocation);
						
						resolution = Integer.parseInt(res);
						if ( resolution > 100000 ) {
							JOptionPane.showMessageDialog(null, "Resolution too High for TAD identification. Minimum Resolution = 100KB. Get new datasets ","Alert!",JOptionPane.ERROR_MESSAGE);							
							return;
						}
					}
					
					
					viewer.setStringProperty(Constants.INPUTCONTACTFILE, inputContactFileField.getText());
		        	viewer.setStringProperty(Constants.OUTPUT3DFILE, outputGSSFileField.getText());			        	
		        	viewer.setStringProperty(Constants.ISMATRIX, inputdata_type);
		        	
		        if (!clusterTAD.isSelected()) {
		        		JOptionPane.showMessageDialog(null, "Please select atleast one algorithm");
		        		return;
		        	}
		        
		        if (clusterTAD.isSelected()) {
	        		algorithm = "FindTAD2D";	        		
		        }
		       
		        	
		        script = algorithm;
			    viewer.script(script);	 
			    	
			    	
			    	
				}
			});
	        
	        stopButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
				// set stop running to true					
				Parameter.stoprunning = true;
				JOptionPane.showMessageDialog(null, "TAD Identification Stopped");
				}
			});
	        
	  }
  }

  
  
  
  
  
  
  //end
  
  
  
  
  
  
  
  class OpenUrlAction extends NewAction {

    String title;
    String prompt;

    OpenUrlAction() {
      super(openurlAction);
      title = GT._("Open URL");
      prompt = GT._("Enter URL of molecular model");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String url = JOptionPane.showInputDialog(frame, prompt, title,
          JOptionPane.PLAIN_MESSAGE);
      if (url != null) {
        if (url.indexOf("://") < 0) {
          if (url.length() == 4 && url.indexOf(".") < 0)
            url = "=" + url;
          if (!url.startsWith("="))
            url = "http://" + url;
        }
        viewer.openFileAsynchronously(url);
      }
    }
  }

  class OpenPdbAction extends NewAction {

    OpenPdbAction() {
      super(openpdbAction);
      script = "var x__id__ = _modelTitle; if (x__id__.length != 4) { x__id__ = '1crn'};x__id__ = prompt('" 
        + GT._("Enter a four-digit PDB model ID or \"=\" and a three-digit ligand ID") + "',x__id__);if (!x__id__) { quit }; load @{'=' + x__id__}";
    }
  }

  class OpenMolAction extends NewAction {

    OpenMolAction() {
      super(openmolAction);
      script = "var x__id__ = _smilesString; if (!x__id__) { x__id__ = 'tylenol'};x__id__ = prompt('" 
        + GT._("Enter the name or identifier (SMILES, InChI, CAS) of a compound. Preface with \":\" to load from PubChem; otherwise Jmol will use the NCI/NIH Resolver.")
        + "',x__id__);if (!x__id__) { quit }; load @{(x__id__[1]==':' ? x__id__ : '$' + x__id__)}";
    }

  }

  class NewAction extends AbstractAction {

    protected String script;
    
    NewAction() {
      super(newAction);
    }

    NewAction(String nm) {
      super(nm);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      if (script == null)
        revalidate();
      else
        viewer.script(script);
    }
  }
  
  /**
   * Really lame implementation of an exit command
   */
  class ExitAction extends AbstractAction {

    ExitAction() {
      super(exitAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      JmolPanel.this.doClose();
    }
  }

  final static String[] imageChoices = { "JPEG", "PNG", "GIF", "PPM", "PDF" };
  final static String[] imageExtensions = { "jpg", "png", "gif", "ppm", "pdf" };

  class ExportAction extends AbstractAction {

    ExportAction() {
      super(exportActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {

      Dialog sd = new Dialog();
      String fileName = sd.getImageFileNameFromDialog(viewer, null, imageType,
          imageChoices, imageExtensions, qualityJPG, qualityPNG);
      if (fileName == null)
        return;
      qualityJPG = sd.getQuality("JPG");
      qualityPNG = sd.getQuality("PNG");
      String sType = imageType = sd.getType();
      if (sType == null) {
        // file type changer was not touched
        sType = fileName;
        int i = sType.lastIndexOf(".");
        if (i < 0)
          return; // make no assumptions - require a type by extension
        sType = sType.substring(i + 1).toUpperCase();
      }
      if (fileName.indexOf(".") < 0)
        fileName += "." + (sType.equalsIgnoreCase("JPEG") ? "jpg" : sType.toLowerCase());
      Logger.info((String) viewer.createImage(fileName, sType, null, sd.getQuality(sType), 0, 0));
    }

  }
  

  class RecentFilesAction extends AbstractAction {

    public RecentFilesAction() {
      super(recentFilesAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {

      recentFiles.setVisible(true);
      String selection = recentFiles.getFile();
      if (selection != null)
        viewer.openFileAsynchronously(selection);
    }
  }

  class ScriptWindowAction extends AbstractAction {

    public ScriptWindowAction() {
      super(consoleAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      AppConsole console = (AppConsole) viewer.getProperty("DATA_API","getAppConsole", null);
      if (console != null)
        console.setVisible(true);
    }
  }

  class ScriptEditorAction extends AbstractAction {

    public ScriptEditorAction() {
      super(editorAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      Component c = (Component) viewer.getProperty("DATA_API","getScriptEditor", null);
      if (c != null)
        c.setVisible(true);
    }
  }

  class AtomSetChooserAction extends AbstractAction {
    public AtomSetChooserAction() {
      super(atomsetchooserAction);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      atomSetChooser.setVisible(true);
    }
  }

  class PovrayAction extends AbstractAction {

    public PovrayAction() {
      super(povrayActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      new PovrayDialog(frame, viewer);
    }

  }

  class WriteAction extends AbstractAction {

    public WriteAction() {
      super(writeActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      String fileName = (new Dialog()).getSaveFileNameFromDialog(viewer,
          null, "SPT");
      if (fileName != null)
        Logger.info((String) viewer.createImage(fileName, "SPT", viewer.getStateInfo(),
            Integer.MIN_VALUE, 0, 0));
    }
  }

  /**
   * 
   * Starting with Jmol 11.8.RC5, this is just informational
   * if type == null and null is returned, then it means "Jmol, you handle it"
   * 
   * @param fileName
   * @param type
   * @param text_or_bytes
   * @param quality
   * @return          null (you do it) or a message starting with OK or an error message
   */
  String createImageStatus(String fileName, String type, Object text_or_bytes,
                           int quality) {
    if (fileName != null && text_or_bytes != null)
      return null; // "Jmol, you do it."
    String msg = fileName;
    if (msg != null && !msg.startsWith("OK") && status != null) {
      status.setStatus(1, GT._("IO Exception:"));
      status.setStatus(2, msg);
    }
    return msg;
  }

  WebExport webExport;
  void createWebExport() {
    webExport = WebExport.createAndShowGUI(viewer, historyFile, WEB_MAKER_WINDOW_NAME);
  }


  class ToWebAction extends AbstractAction {

    public ToWebAction() {
      super(toWebActionProperty);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        @Override
		public void run() {
          createWebExport();
        }
      });
    }
  }

  class ViewMeasurementTableAction extends AbstractAction {

    public ViewMeasurementTableAction() {
      super("viewMeasurementTable");
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      display.measurementTable.activate();
    }
  }
  
  void createSurfaceTool(){
    //TODO check to see if it already exists, if so bring to front.
    if(surfaceTool!=null){
      surfaceTool.toFront();
    }else{
    surfaceTool = new SurfaceTool(viewer, historyFile, SURFACETOOL_WINDOW_NAME, true);
    }
  }
  
  class SurfaceToolAction extends AbstractAction {
    
    public SurfaceToolAction(){
      super(surfaceToolActionProperty);
    }
    
    @Override
	public void actionPerformed(ActionEvent e){
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        @Override
		public void run() {
          createSurfaceTool();
        }
      });
    }
  }
  

  
  /**
   * Returns a new File referenced by the property 'user.dir', or null
   * if the property is not defined.
   *
   * @return  a File to the user directory
   */
  public static File getUserDirectory() {
    String dir = System.getProperty("user.dir");
    return dir == null ? null : new File(System.getProperty("user.dir"));
  }

  void openFile() {
    String fileName = (new Dialog()).getOpenFileNameFromDialog(viewerOptions,
        viewer, null, historyFile, FILE_OPEN_WINDOW_NAME, true);
    if (fileName == null)
      return;
    boolean pdbCartoons = !fileName.startsWith("#NOC#;");
    if (!pdbCartoons)
      fileName = fileName.substring(6);
    if (fileName.startsWith("load append"))
      viewer.scriptWait(fileName);
    else
      viewer.openFileAsynchronously(fileName, pdbCartoons);
  }


  
  static final String chemFileProperty = "chemFile";

  void notifyFileOpen(String fullPathName, String title) {
    if (fullPathName == null || !fullPathName.equals("file[]")) {
      recentFiles.notifyFileOpen(fullPathName);
      frame.setTitle(title);
    }
    if (atomSetChooser == null) {
      atomSetChooser = new AtomSetChooser(viewer, frame);
      pcs.addPropertyChangeListener(chemFileProperty, atomSetChooser);
    }
    pcs.firePropertyChange(chemFileProperty, null, null);
  }

  class ExecuteScriptAction extends AbstractAction {
    public ExecuteScriptAction() {
      super("executeScriptAction");
    }

    @Override
	public void actionPerformed(ActionEvent e) {
      String script = e.getActionCommand();
      if (script.indexOf("#showMeasurementTable") >= 0)
        display.measurementTable.activate();
      //      viewer.script("set picking measure distance;set pickingstyle measure");
      viewer.evalStringQuiet(script);
    }
  }

  class ResizeAction extends AbstractAction {
    public ResizeAction() {
      super(resizeAction);
    }
    @Override
	public void actionPerformed(ActionEvent e) {
      resizeInnerPanel(null);
    } 
  }
  
  void resizeInnerPanel(String data) {
    String info = viewer.getScreenWidth() + " " + viewer.getScreenHeight();
    if (data == null) {
      data = info;
   } else {
     int pt = data.indexOf("preferredWidthHeight ");
     int pt2 = data.indexOf(";", pt + 1);
     if (pt >= 0 && pt2 > pt)
       data = data.substring(pt + 21, pt2).trim();
     if (data.equals(info))
       return;
   }
    info = JOptionPane.showInputDialog(GT._("width height?"), data);
    if (info == null)
      return;
    float[] dims = new float[2];
    int n = Parser.parseStringInfestedFloatArray(info, null, dims);
    if (n < 2)
      return;
    resizeDisplay((int) dims[0], (int) dims[1]);
  }

  void resizeDisplay(int width, int height) {
    Dimension d = new Dimension(width, height);
    display.setPreferredSize(d);
    d = new Dimension(width, 30);
    status.setPreferredSize(d);
    toolbar.setPreferredSize(d);
    JmolConsole.getWindow(this).pack();
  }

  void updateLabels() {
    if (atomSetChooser != null) {
      atomSetChooser.dispose();
      atomSetChooser = null;
    }
    if (gaussianDialog != null) {
      gaussianDialog.dispose();
      gaussianDialog = null;
    }
    boolean doTranslate = GT.getDoTranslate();
    GT.setDoTranslate(true);
    getDialogs();
    GT.setDoTranslate(doTranslate);
    guimap.updateLabels();
  }

  ////////// JSON/NIO SERVICE //////////
  
  @Override
public void nioRunContent(JsonNioServer jns) {
    // ignore
  }
  
  @Override
public void nioClosed(JsonNioServer jns) {
    if (bannerFrame != null) {
      viewer.scriptWait("delay 2");
      bannerFrame.dispose();
      viewer.setModeMouse(JmolConstants.MOUSE_NONE);
      // would not nec. have to close this....
      System.exit(0);
    }
    if (jns.equals(service))
      service = null;
    else if (jns.equals(serverService))
      serverService = null;
    
  }

  @Override
public void setBannerLabel(String label) {
    if (bannerFrame != null)
      bannerFrame.setLabel(label);
  }

  void sendNioMessage(int port, String strInfo) {
    try {
      if (port < 0) {
        if (serverService != null && strInfo != null && strInfo.equalsIgnoreCase("STOP")) {
          serverService.close();
        } else {
          serverService = getJsonNioServer();
          if (serverService != null)
            serverService.startService(port, this, viewer, "-1", 1);
        }
        if (serverService != null && serverService.getPort() == -port && strInfo != null) {
          if (service == null) {
            service = getJsonNioServer();
            if (service != null)
              service.startService(-port, this, viewer, null, 1);
          }
          if (service != null)
            service.send(-port, strInfo);
          return;
        }
        return;
      }
      if (strInfo == null)
        return;
      if (strInfo.equalsIgnoreCase("STOP"))
        strInfo = "{\"type\":\"quit\"}";
      if (service == null && serverService != null && serverService.getPort() == port) {
        serverService.send(port, strInfo);
        return;
      }
      if (service == null) {
        service = getJsonNioServer();
        if (service != null)
          service.startService(port, this, viewer, null, 1);
      }
      if (service != null)
        service.send(port, strInfo);
    } catch (IOException e) {
      // TODO
    }
  }

  public static JsonNioServer getJsonNioServer() {
    return (JsonNioServer) Interface
        .getApplicationInterface("jsonkiosk.JsonNioService");
  }

  private class AnimButton extends JButton implements MouseListener {

    private String script;

    protected AnimButton(ImageIcon ii, String script) {
      super(ii);
      this.script = script;
      addMouseListener(this);
    }

    @Override
	public void mousePressed(MouseEvent e) {
      viewer.evalStringQuiet(script);
      viewer.evalStringQuiet("timeout '__animBtn' -100 \"" + script + "\"");
    }

    @Override
	public void mouseReleased(MouseEvent e) {
      viewer.evalStringQuiet("timeout '__animBtn' OFF");
    }

    @Override
	public void mouseClicked(MouseEvent e) {
    }

    @Override
	public void mouseEntered(MouseEvent e) {
    }

    @Override
	public void mouseExited(MouseEvent e) {
    }

  }

  public void syncScript(String script) {
    viewer.syncScript(script, "~", 0);
  }

}
