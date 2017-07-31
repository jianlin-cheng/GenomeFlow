/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2009-07-31 09:22:19 -0500 (Fri, 31 Jul 2009) $
 * $Revision: 11291 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
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
package org.jmol.viewer;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3f;

import org.jmol.api.Event;
import org.jmol.i18n.GT;
import org.jmol.modelset.Atom;
import org.jmol.modelset.AtomCollection;
import org.jmol.modelset.MeasurementPending;
import org.jmol.script.ScriptEvaluator;
import org.jmol.script.Token;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Colix;
import org.jmol.util.Escape;
import org.jmol.util.Logger;
import org.jmol.util.Point3fi;
import org.jmol.util.Rectangle;
import org.jmol.util.TextFormat;
import org.jmol.viewer.binding.Binding;
import org.jmol.viewer.binding.DragBinding;
import org.jmol.viewer.binding.JmolBinding;
import org.jmol.viewer.binding.PfaatBinding;
import org.jmol.viewer.binding.RasmolBinding;

public class ActionManager {

  public final static int ACTION_center = 0;
  public final static int ACTION_translate = 1;  
  public final static int ACTION_rotate = 2;
  public final static int ACTION_rotateZ = 3;
  public final static int ACTION_rotateZorZoom = 4;
  public final static int ACTION_wheelZoom = 5;
  public final static int ACTION_slideZoom = 6;  
  public final static int ACTION_navTranslate = 7;
  public final static int ACTION_swipe = 8;
  public final static int ACTION_spinDrawObjectCW = 9;
  public final static int ACTION_spinDrawObjectCCW = 10;
  public final static int ACTION_slab = 11;
  public final static int ACTION_depth = 12;
  public final static int ACTION_slabAndDepth = 13;
  public final static int ACTION_popupMenu = 14;
  public final static int ACTION_clickFrank = 15;
  public final static int ACTION_select = 16;
  public final static int ACTION_selectNone = 17;
  public final static int ACTION_selectToggle = 18;  
  public final static int ACTION_selectAndNot = 19;
  public final static int ACTION_selectOr = 20;
  public final static int ACTION_selectToggleExtended = 21;
  public final static int ACTION_dragSelected = 22;
  public final static int ACTION_selectAndDrag = 23;
  public final static int ACTION_dragZ = 24;
  public final static int ACTION_rotateSelected = 25;
  public final static int ACTION_rotateBranch = 26;
  public final static int ACTION_dragAtom = 27;
  public final static int ACTION_dragMinimize = 28;
  public final static int ACTION_dragMinimizeMolecule = 29;
  public final static int ACTION_dragLabel = 30;
  public final static int ACTION_dragDrawPoint = 31;
  public final static int ACTION_dragDrawObject = 32;
  public final static int ACTION_pickAtom = 33;
  public final static int ACTION_pickPoint = 34;
  public final static int ACTION_pickLabel = 35;
  public final static int ACTION_pickMeasure = 36;
  public final static int ACTION_setMeasure = 37;
  public final static int ACTION_pickIsosurface = 38;
  public final static int ACTION_pickNavigate = 39;
  public final static int ACTION_deleteAtom = 40;
  public final static int ACTION_deleteBond = 41;
  public final static int ACTION_connectAtoms = 42;
  public final static int ACTION_assignNew = 43;
  public final static int ACTION_reset = 44;
  public final static int ACTION_stopMotion = 45;
  public final static int ACTION_multiTouchSimulation = 46;
  public final static int ACTION_count = 47;

  private final static String[] actionInfo = new String[ACTION_count];
  private final static String[] actionNames = new String[ACTION_count];

  static void newAction(int i, String name, String info) {
    actionInfo[i] = info;
    actionNames[i] = name;
  }
  
  static {
    newAction(ACTION_center,                "_center",               GT._("center"));
    newAction(ACTION_translate,             "_translate",            GT._("translate"));
    newAction(ACTION_rotate,                "_rotate",               GT._("rotate"));
    newAction(ACTION_rotateZ,               "_rotateZ",              GT._("rotate Z"));
    newAction(ACTION_rotateZorZoom,         "_rotateZorZoom",        GT._("rotate Z (horizontal motion of mouse) or zoom (vertical motion of mouse)"));
    newAction(ACTION_wheelZoom,             "_wheelZoom",            GT._("zoom"));
    newAction(ACTION_slideZoom,             "_slideZoom",            GT._("zoom (along right edge of window)"));
    newAction(ACTION_navTranslate,          "_navTranslate",         GT._("translate navigation point (requires {0} and {1})", new String[] {"set NAVIGATIONMODE", "set picking NAVIGATE"}));
    newAction(ACTION_swipe,                 "_swipe",                GT._("spin model (swipe and release button and stop motion simultaneously)"));
    newAction(ACTION_spinDrawObjectCW,      "_spinDrawObjectCW",     GT._("click on two points to spin around axis clockwise (requires {0})", "set picking SPIN"));
    newAction(ACTION_spinDrawObjectCCW,     "_spinDrawObjectCCW",    GT._("click on two points to spin around axis counterclockwise (requires {0})", "set picking SPIN"));
    newAction(ACTION_slab,                  "_slab",                 GT._("adjust slab (front plane; requires {0})", "SLAB ON"));
    newAction(ACTION_depth,                 "_depth",                GT._("adjust depth (back plane; requires {0})", "SLAB ON"));
    newAction(ACTION_slabAndDepth,          "_slabAndDepth",         GT._("move slab/depth window (both planes; requires {0})", "SLAB ON"));
    newAction(ACTION_popupMenu,             "_popupMenu",            GT._("pop up the full context menu"));
    newAction(ACTION_clickFrank,            "_clickFrank",           GT._("pop up recent context menu (click on Jmol frank)"));
    newAction(ACTION_select,                "_select",               GT._("select an atom (requires {0})", "set pickingStyle EXTENDEDSELECT"));
    newAction(ACTION_selectNone,            "_selectNone",           GT._("select NONE (requires {0})", "set pickingStyle EXTENDEDSELECT"));
    newAction(ACTION_selectToggle,          "_selectToggle",         GT._("toggle selection (requires {0})", "set pickingStyle DRAG/EXTENDEDSELECT/RASMOL"));
    newAction(ACTION_selectAndNot,          "_selectAndNot",         GT._("unselect this group of atoms (requires {0})", "set pickingStyle DRAG/EXTENDEDSELECT"));
    newAction(ACTION_selectOr,              "_selectOr",             GT._("add this group of atoms to the set of selected atoms (requires {0})", "set pickingStyle DRAG/EXTENDEDSELECT"));
    newAction(ACTION_selectToggleExtended,  "_selectToggleOr",       GT._("if all are selected, unselect all, otherwise add this group of atoms to the set of selected atoms (requires {0})", "set pickingStyle DRAG"));
    newAction(ACTION_dragSelected,          "_dragSelected",         GT._("move selected atoms (requires {0})", "set DRAGSELECTED"));
    newAction(ACTION_selectAndDrag,         "_selectAndDrag",        GT._("select and drag atoms (requires {0})", "set DRAGSELECTED"));
    newAction(ACTION_dragZ,                 "_dragZ",                GT._("drag atoms in Z direction (requires {0})", "set DRAGSELECTED"));
    newAction(ACTION_rotateSelected,        "_rotateSelected",       GT._("rotate selected atoms (requires {0})", "set DRAGSELECTED"));
    newAction(ACTION_rotateBranch,          "_rotateBranch",         GT._("rotate branch around bond (requires {0})", "set picking ROTATEBOND"));
    newAction(ACTION_dragAtom,              "_dragAtom",             GT._("move atom (requires {0})", "set picking DRAGATOM"));
    newAction(ACTION_dragMinimize,          "_dragMinimize",         GT._("move atom and minimize molecule (requires {0})", "set picking DRAGMINIMIZE"));
    newAction(ACTION_dragMinimizeMolecule,  "_dragMinimizeMolecule", GT._("move and minimize molecule (requires {0})", "set picking DRAGMINIMIZEMOLECULE"));
    newAction(ACTION_dragLabel,             "_dragLabel",            GT._("move label (requires {0})", "set picking LABEL"));
    newAction(ACTION_dragDrawPoint,         "_dragDrawPoint",        GT._("move specific DRAW point (requires {0})", "set picking DRAW"));
    newAction(ACTION_dragDrawObject,        "_dragDrawObject",       GT._("move whole DRAW object (requires {0})", "set picking DRAW"));
    newAction(ACTION_pickAtom,              "_pickAtom",             GT._("pick an atom"));
    newAction(ACTION_pickPoint,             "_pickPoint",            GT._("pick a DRAW point (for measurements) (requires {0}", "set DRAWPICKING"));
    newAction(ACTION_pickLabel,             "_pickLabel",            GT._("pick a label to toggle it hidden/displayed (requires {0})", "set picking LABEL"));
    newAction(ACTION_pickMeasure,           "_pickMeasure",          GT._("pick an atom to include it in a measurement (after starting a measurement or after {0})", "set picking DISTANCE/ANGLE/TORSION"));
    newAction(ACTION_setMeasure,            "_setMeasure",           GT._("pick an atom to initiate or conclude a measurement"));
    newAction(ACTION_pickIsosurface,        "_pickIsosurface",       GT._("pick an ISOSURFACE point (requires {0}", "set DRAWPICKING"));
    newAction(ACTION_pickNavigate,          "_pickNavigate",         GT._("pick a point or atom to navigate to (requires {0})", "set NAVIGATIONMODE"));
    newAction(ACTION_deleteAtom,            "_deleteAtom",           GT._("delete atom (requires {0})", "set picking DELETE ATOM"));
    newAction(ACTION_deleteBond,            "_deleteBond",           GT._("delete bond (requires {0})", "set picking DELETE BOND"));
    newAction(ACTION_connectAtoms,          "_pickConnect",          GT._("connect atoms (requires {0})", "set picking CONNECT"));
    newAction(ACTION_assignNew,             "_assignNew",            GT._("assign/new atom or bond (requires {0})", "set picking assignAtom_??/assignBond_?"));
    newAction(ACTION_reset,                 "_reset",                GT._("reset (when clicked off the model)"));
    newAction(ACTION_stopMotion,            "_stopMotion",           GT._("stop motion (requires {0})", "set waitForMoveTo FALSE"));
    newAction(ACTION_multiTouchSimulation,  "_multiTouchSimulation", GT._("simulate multi-touch using the mouse)"));
  }

  public static String getActionName(int i) {
    return (i < actionNames.length ? actionNames[i] : null);
  }
  
  public static int getActionFromName(String name) {
    for (int i = 0; i < actionNames.length; i++)
      if (actionNames[i].equalsIgnoreCase(name))
        return i;
    return -1;
  }
  
  public String getBindingInfo(String qualifiers) {
    return binding.getBindingInfo(actionInfo, qualifiers);  
  }

  /**
   * picking modes     set picking....
   */
  public final static int PICKING_OFF       = 0;
  public final static int PICKING_IDENTIFY  = 1;
  public final static int PICKING_LABEL     = 2;
  public final static int PICKING_CENTER    = 3;
  public final static int PICKING_DRAW      = 4;
  public final static int PICKING_SPIN      = 5;
  public final static int PICKING_SYMMETRY  = 6;
  public final static int PICKING_DELETE_ATOM      =  7;
  public final static int PICKING_DELETE_BOND      =  8;
  public final static int PICKING_SELECT_ATOM      =  9;
  public final static int PICKING_SELECT_GROUP     = 10;
  public final static int PICKING_SELECT_CHAIN     = 11;
  public final static int PICKING_SELECT_MOLECULE  = 12;
  public final static int PICKING_SELECT_POLYMER   = 13;
  public final static int PICKING_SELECT_STRUCTURE = 14;
  public final static int PICKING_SELECT_SITE      = 15;
  public final static int PICKING_SELECT_MODEL     = 16;
  public final static int PICKING_SELECT_ELEMENT   = 17;
  public final static int PICKING_MEASURE          = 18;
  public final static int PICKING_MEASURE_DISTANCE = 19;
  public final static int PICKING_MEASURE_ANGLE    = 20;
  public final static int PICKING_MEASURE_TORSION  = 21;
  public final static int PICKING_MEASURE_SEQUENCE = 22;
  public final static int PICKING_NAVIGATE         = 23;
  public final static int PICKING_CONNECT          = 24;
  public final static int PICKING_STRUTS           = 25;
  public final static int PICKING_DRAG_SELECTED    = 26;
  public final static int PICKING_DRAG_MOLECULE    = 27;
  public final static int PICKING_DRAG_ATOM        = 28;
  public final static int PICKING_DRAG_MINIMIZE    = 29;
  public final static int PICKING_DRAG_MINIMIZE_MOLECULE = 30; // for docking
  public final static int PICKING_INVERT_STEREO    = 31;
  public final static int PICKING_ASSIGN_ATOM      = 32;
  public final static int PICKING_ASSIGN_BOND      = 33;
  public final static int PICKING_ROTATE_BOND      = 34;
  public final static int PICKING_IDENTIFY_BOND    = 35;
  


  private final static String[] pickingModeNames = {
    "off", "identify", "label", "center", "draw", "spin",
    "symmetry", "deleteatom", "deletebond", 
    "atom", "group", "chain", "molecule", "polymer", "structure", 
    "site", "model", "element", 
    "measure", "distance", "angle", "torsion", "sequence", 
    "navigate", 
    "connect", "struts", 
    "dragselected", "dragmolecule", "dragatom", "dragminimize", "dragminimizemolecule",
    "invertstereo", "assignatom", "assignbond", "rotatebond", "identifybond"
  };
 
  public final static String getPickingModeName(int pickingMode) {
    return (pickingMode < 0 || pickingMode >= pickingModeNames.length ? "off"
        : pickingModeNames[pickingMode]);
  }
  
  public final static int getPickingMode(String str) {
    for (int i = pickingModeNames.length; --i >= 0; )
      if (str.equalsIgnoreCase(pickingModeNames[i]))
        return i;
    return -1;
  }
  /**
   * picking styles
   */
  public final static int PICKINGSTYLE_SELECT_JMOL = 0;
  public final static int PICKINGSTYLE_SELECT_CHIME = 0;
  public final static int PICKINGSTYLE_SELECT_RASMOL = 1;
  public final static int PICKINGSTYLE_SELECT_PFAAT = 2;
  public final static int PICKINGSTYLE_SELECT_DRAG = 3;
  public final static int PICKINGSTYLE_MEASURE_ON = 4;
  public final static int PICKINGSTYLE_MEASURE_OFF = 5;
  
  private final static String[] pickingStyleNames = {
    "toggle", "selectOrToggle", "extendedSelect", "drag",
    "measure", "measureoff"
  };

  public final static String getPickingStyleName(int pickingStyle) {
    return (pickingStyle < 0 || pickingStyle >= pickingStyleNames.length ? "toggle"
        : pickingStyleNames[pickingStyle]);
  }
  
  public final static int getPickingStyle(String str) {
    for (int i = pickingStyleNames.length; --i >= 0; )
      if (str.equalsIgnoreCase(pickingStyleNames[i]))
        return i;
    return -1;
  }

  public Map<String, Object> getMouseInfo() {
    Map<String, Object> info = new Hashtable<String, Object>();
    List<Object> vb = new ArrayList<Object>();
    Iterator<Object> e = binding.getBindings().values().iterator();
    while (e.hasNext()) {
      Object obj = e.next();
      if (obj instanceof Boolean)
        continue;
      if (obj instanceof int[]) {
        int[] binding = (int[]) obj;
        obj = new String[] { Binding.getMouseActionName(binding[0], false),
            getActionName(binding[1]) };
      }
      vb.add(obj);
    }
    info.put("bindings", vb);
    info.put("bindingName", binding.getName());
    info.put("actionNames", actionNames);
    info.put("actionInfo", actionInfo);
    info.put("bindingInfo", TextFormat.split(getBindingInfo(null), '\n'));
    return info;
  }

  private final static long MAX_DOUBLE_CLICK_MILLIS = 700;
  protected final static long MININUM_GESTURE_DELAY_MILLISECONDS = 5;
  private final static int SLIDE_ZOOM_X_PERCENT = 98;
  public final static float DEFAULT_MOUSE_DRAG_FACTOR = 1f;
  public final static float DEFAULT_MOUSE_WHEEL_FACTOR = 1.15f;
  public final static float DEFAULT_GESTURE_SWIPE_FACTOR = 1f;
 
  protected Viewer viewer;
  
  protected Binding binding;
  Binding jmolBinding;
  Binding pfaatBinding;
  Binding dragBinding;
  Binding rasmolBinding;
  Binding predragBinding;

  /**
   * 
   * @param viewer
   * @param commandOptions
   */
  public void setViewer(Viewer viewer, String commandOptions) {
    this.viewer = viewer;
    setBinding(jmolBinding = new JmolBinding());   
  }

  /**
   * @param groupID  
   * @param eventType 
   * @param touchID 
   * @param iData 
   * @param pt 
   * @param time 
   */
  public void processEvent(int groupID, int eventType, int touchID, int iData,
                           Point3f pt, long time) {
    // only for ActionManagerMT
  }

  boolean isBound(int gesture, int action) {
    return binding.isBound(gesture, action);
  }

  /**
   * 
   * @param desc
   * @param name
   * @param range1  currently ignored
   * @param range2  currently ignored
   */
  void bindAction(String desc, String name, Point3f range1,
                         Point3f range2) {
    int jmolAction = getActionFromName(name);
    int mouseAction = Binding.getMouseAction(desc);
    if (mouseAction == 0)
      return;
    if (jmolAction >= 0) {
      binding.bind(mouseAction, jmolAction);
    } else {
      binding.bind(mouseAction, name);
    }
  }

  protected void clearBindings() {
    setBinding(jmolBinding = new JmolBinding());
    pfaatBinding = null;
    dragBinding = null;
    rasmolBinding = null; 
  }
  
  void unbindAction(String desc, String name) {
    if (desc == null && name == null) {
      clearBindings();
      return;
    }
    int jmolAction = getActionFromName(name);
    int mouseAction = Binding.getMouseAction(desc);
    if (jmolAction >= 0)
      binding.unbind(mouseAction, jmolAction);
    else if (mouseAction != 0)
      binding.unbind(mouseAction, name);
    if (name == null)
      binding.unbindUserAction(desc);    
  }

  protected Thread hoverWatcherThread;
  protected boolean haveMultiTouchInput = false;

  protected int xyRange = 0;
  
  private float gestureSwipeFactor = DEFAULT_GESTURE_SWIPE_FACTOR;
  protected float mouseDragFactor = DEFAULT_MOUSE_DRAG_FACTOR;
  protected float mouseWheelFactor = DEFAULT_MOUSE_WHEEL_FACTOR;
  
  void setGestureSwipeFactor(float factor) {
    gestureSwipeFactor = factor;
  }
  
  void setMouseDragFactor(float factor) {
    mouseDragFactor = factor;
  }
  
  void setMouseWheelFactor(float factor) {
    mouseWheelFactor = factor;
  }
  
  protected class Mouse {
    protected int x = -1000;
    protected int y = -1000;
    protected int modifiers = 0;
    protected long time = -1;
    //private int type;
    
    /**
     * @param type  -- for debugging 
     */
    protected Mouse(int type) {
      //this.type = type;
    }
    
    protected void set(long time, int x, int y, int modifiers) {
      this.time = time;
      this.x = x;
      this.y = y;
      this.modifiers = modifiers;
    }

    /**
     * @param why  - for debugging purposes 
     */
    protected void setCurrent(int why) {
      time = current.time;
      x = current.x;
      y = current.y;
      modifiers = current.modifiers;
    }

    public boolean inRange(int x, int y) {
      return (Math.abs(this.x - x) <= xyRange && Math.abs(this.y - y) <= xyRange);
    }
    
    public boolean check(int x, int y, int modifiers, long time, long delayMax) {
      return (inRange(x, y) && this.modifiers == modifiers && (time - this.time) < delayMax);
    }
  }
  
  protected final Mouse current = new Mouse(0);
  protected final Mouse moved = new Mouse(1);
  private final Mouse clicked = new Mouse(2);
  private final Mouse pressed = new Mouse(3);
  private final Mouse dragged = new Mouse(4);

  protected void setCurrent(long time, int x, int y, int mods) {
    hoverOff();
    current.set(time, x, y, mods);
  }
  
  int getCurrentX() {
    return current.x;
  }
  int getCurrentY() {
    return current.y;
  }

  protected int pressedCount;
  private int pressedAtomIndex;
  
  protected int clickedCount;

  private boolean drawMode = false;
  private boolean labelMode = false;
  private boolean dragSelectedMode = false;
  private boolean measuresEnabled = true;

  public void setMouseMode() {
    drawMode = labelMode = false;
    dragSelectedMode = viewer.getDragSelected();
    measuresEnabled = !dragSelectedMode;
    if (!dragSelectedMode)
      switch (atomPickingMode) {
      default:
        return;
      case PICKING_ASSIGN_ATOM:
        measuresEnabled = !isPickAtomAssignCharge;
        return;
      case PICKING_DRAW:
        drawMode = true;
        // drawMode and dragSelectedMode are incompatible
        measuresEnabled = false;
        break;
      //other cases here?
      case PICKING_LABEL:
        labelMode = true;
        measuresEnabled = false;
        break;
      case PICKING_SELECT_ATOM:
        measuresEnabled = false;
        break;
      case PICKING_MEASURE_DISTANCE:
      case PICKING_MEASURE_SEQUENCE:
      case PICKING_MEASURE_ANGLE:
      case PICKING_MEASURE_TORSION:
        measuresEnabled = false;
        return;
        //break;
      }
    exitMeasurementMode();
  }
  
  protected void clearMouseInfo() {
    // when a second touch is made, this clears all record of first touch
    pressedCount = clickedCount = 0;
    dragGesture.setAction(0, 0);
    exitMeasurementMode();
  }

  private boolean hoverActive = false;

  private MeasurementPending measurementPending;
  private int dragAtomIndex = -1;

  private boolean rubberbandSelectionMode = false;
  private final Rectangle rectRubber = new Rectangle();

  private boolean isAltKeyReleased = true;  
  private boolean keyProcessing;

  protected boolean isMultiTouchClient;
  protected boolean isMultiTouchServer;

  public boolean isMTClient() {
    return isMultiTouchClient;
  }

  public boolean isMTServer() {
    return isMultiTouchServer;
  }

  public void dispose() {
    clear();
  }

  public void clear() {
    startHoverWatcher(false);
    if (predragBinding != null)
      binding = predragBinding;
    viewer.setPickingMode(null, PICKING_IDENTIFY);
    viewer.setPickingStyle(null, rootPickingStyle);
    eval = null;
    isAltKeyReleased = true;
  }

  synchronized public void startHoverWatcher(boolean isStart) {
    if (viewer.isPreviewOnly())
      return;
    try {
      if (isStart) {
        if (hoverWatcherThread != null)
          return;
        current.time = -1;
        hoverWatcherThread = new Thread(new HoverWatcher());
        hoverWatcherThread.setName("HoverWatcher");
        hoverWatcherThread.start();
      } else {
        if (hoverWatcherThread == null)
          return;
        current.time = -1;
        hoverWatcherThread.interrupt();
        hoverWatcherThread = null;
      }
    } catch (Exception e) {
      // is possible -- seen once hoverWatcherThread.start() had null pointer.
    }
  }

  /**
   * only NONE (-1) is implemented; it just stops the 
   * hoverWatcher thread so that the viewer references are all removed
   * 
   * @param modeMouse
   */
  public void setModeMouse(int modeMouse) {
    if (modeMouse == JmolConstants.MOUSE_NONE) {
      startHoverWatcher(false);
    }
  }

  /**
   * called by MouseManager.keyPressed
   * @param key
   * @param modifiers
   */
  public void keyPressed(int key, int modifiers) {
    if (keyProcessing)
      return;
    hoverOff();
    keyProcessing = true;
    switch(key) {
    case Event.VK_ALT:
      if (dragSelectedMode && isAltKeyReleased)
        viewer.moveSelected(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, null, false, false);
      isAltKeyReleased = false;
      moved.modifiers |= Binding.ALT;
      break;
    case Event.VK_SHIFT:
      dragged.modifiers |= Binding.SHIFT;
      moved.modifiers |= Binding.SHIFT;
      break;
    case Event.VK_CONTROL:
      moved.modifiers |= Binding.CTRL;
    }
    int action = Binding.LEFT+Binding.SINGLE_CLICK+moved.modifiers;
    if(!labelMode && !binding.isUserAction(action) && !isSelectAction(action))
      checkMotionRotateZoom(action, current.x, 0, 0, false);
    if (viewer.getNavigationMode()) {
      // if (viewer.getBooleanProperty("showKeyStrokes", false))
      // viewer.evalStringQuiet("!set echo bottom left;echo "
      // + (i == 0 ? "" : i + " " + m));
      switch (key) {
      case Event.VK_UP:
      case Event.VK_DOWN:
      case Event.VK_LEFT:
      case Event.VK_RIGHT:
      case Event.VK_SPACE:
      case Event.VK_PERIOD:
        viewer.navigate(key, modifiers);
        break;
      }
    }
    keyProcessing = false;
  }

  public void keyReleased(int key) {
    switch(key) {
    case Event.VK_ALT:
      if (dragSelectedMode)
        viewer.moveSelected(Integer.MAX_VALUE, 0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, null, false, false);
      isAltKeyReleased = true;
      moved.modifiers &= ~Binding.ALT;
      break;
    case Event.VK_SHIFT:
      moved.modifiers &= ~Binding.SHIFT;
      break;
    case Event.VK_CONTROL:
      moved.modifiers &= ~Binding.CTRL;
    }
    if (moved.modifiers == 0)
      viewer.setCursor(JmolConstants.CURSOR_DEFAULT);
    if (!viewer.getNavigationMode())
      return;
    //if (viewer.getBooleanProperty("showKeyStrokes", false))
      //viewer.evalStringQuiet("!set echo bottom left;echo;");
    switch (key) {
    case Event.VK_UP:
    case Event.VK_DOWN:
    case Event.VK_LEFT:
    case Event.VK_RIGHT:
      viewer.navigate(0, 0);
      break;
    }
  }

  public void mouseEntered(long time, int x, int y) {
    setCurrent(time, x, y, 0);
  }

  public void mouseExited(long time, int x, int y) {
    setCurrent(time, x, y, 0);
    exitMeasurementMode();
  }

  public void mouseAction(int action, long time, int x, int y, int count,
                          int modifiers) {
    if (!viewer.getMouseEnabled())
      return;
    //if (Logger.debugging)
      //Logger.info("ActionManager mouseAction action=" + action + " x y = " + x + " " + y + " count = " + count + " mods = " + modifiers);
    switch (action) {
    case Binding.MOVED:
      setCurrent(time, x, y, modifiers);
      moved.setCurrent(Binding.MOVED);
      if (measurementPending != null || hoverActive)
        checkPointOrAtomClicked(x, y, 0, 0, time, false, Binding.MOVED);
      else if (isZoomArea(x))
        checkMotionRotateZoom(Binding.getMouseAction(1, Binding.LEFT), 0, 0, 0,
            false);
      else if (viewer.getCursor() == JmolConstants.CURSOR_ZOOM)//if (dragSelectedMode)
        viewer.setCursor(JmolConstants.CURSOR_DEFAULT);
      return;
    case Binding.WHEELED:
      if (viewer.isApplet() && !viewer.hasFocus())
        return;
      // sun bug? noted by Charles Xie that wheeling on a Java page
      // effected inappropriate wheeling on this Java component
      setCurrent(time, current.x, current.y, modifiers);
      checkAction(Binding.getMouseAction(0, modifiers), current.x, current.y,
          0, y, time, Binding.WHEELED);
      return;
    case Binding.CLICKED:
      setMouseMode();
      setCurrent(time, x, y, modifiers);
      clickedCount = (count > 1 ? count : clicked.check(x, y, modifiers, time,
          MAX_DOUBLE_CLICK_MILLIS) ? clickedCount + 1 : 1);
      clicked.setCurrent(Binding.CLICKED);
      viewer.setFocus();
      if (atomPickingMode != PICKING_SELECT_ATOM
          && isBound(Binding.getMouseAction(Integer.MIN_VALUE, modifiers),
              ACTION_selectAndDrag))
        return;
      checkPointOrAtomClicked(x, y, modifiers, clickedCount, time, false,
          Binding.CLICKED);
      return;
    case Binding.DRAGGED:
      setMouseMode();
      int deltaX = x - dragged.x;
      int deltaY = y - dragged.y;
      setCurrent(time, x, y, modifiers);
      dragged.setCurrent(Binding.DRAGGED);
      if (atomPickingMode != PICKING_ASSIGN_ATOM)
        exitMeasurementMode();
      action = Binding.getMouseAction(pressedCount, modifiers);
      dragGesture.add(action, x, y, time);
      checkAction(action, x, y, deltaX, deltaY, time, Binding.DRAGGED);
      return;
    case Binding.PRESSED:
      setMouseMode();
      setCurrent(time, x, y, modifiers);
      pressedCount = (pressed.check(x, y, modifiers, time,
          MAX_DOUBLE_CLICK_MILLIS) ? pressedCount + 1 : 1);
      pressed.setCurrent(Binding.PRESSED);
      dragged.setCurrent(Binding.PRESSED);
      viewer.setFocus();
      boolean isSelectAndDrag = isBound(Binding.getMouseAction(
          Integer.MIN_VALUE, modifiers), ACTION_selectAndDrag);
      action = Binding.getMouseAction(pressedCount, modifiers);
      dragGesture.setAction(action, time);
      if (Binding.getModifiers(action) != 0) {
        action = viewer.notifyMouseClicked(x, y, action, Binding.PRESSED);
        if (action == 0)
          return;
      }
      pressedAtomIndex = Integer.MAX_VALUE;
      if (drawMode
          && (isBound(action, ACTION_dragDrawObject) || isBound(action,
              ACTION_dragDrawPoint)) || labelMode
          && isBound(action, ACTION_dragLabel)) {
        viewer.checkObjectDragged(Integer.MIN_VALUE, 0, x, y, action);
        return;
      }
      checkUserAction(action, x, y, 0, 0, time, Binding.PRESSED);
      boolean isBound = false;
      switch (atomPickingMode) {
      case PICKING_ASSIGN_ATOM:
        isBound = isBound(action, ACTION_assignNew);
        break;
      case PICKING_DRAG_ATOM:
        isBound = isBound(action, ACTION_dragAtom) 
            || isBound(action, ACTION_dragZ);
        break;
      case PICKING_DRAG_SELECTED:
      case PICKING_DRAG_MOLECULE:
        isBound = isBound(action, ACTION_dragAtom)
            || isBound(action, ACTION_rotateSelected)
            || isBound(action, ACTION_dragZ);
        break;
      case PICKING_DRAG_MINIMIZE:
        isBound = isBound(action, ACTION_dragMinimize)
            || isBound(action, ACTION_dragZ);
        break;
      case PICKING_DRAG_MINIMIZE_MOLECULE:
        isBound = isBound(action, ACTION_dragMinimizeMolecule)
            || isBound(action, ACTION_rotateSelected)
            || isBound(action, ACTION_dragZ);
        break;
      }
      if (isBound) {
        dragAtomIndex = viewer.findNearestAtomIndex(x, y, true);
        if (dragAtomIndex >= 0
            && (atomPickingMode == PICKING_ASSIGN_ATOM 
                || atomPickingMode == PICKING_INVERT_STEREO)
            && viewer.isAtomAssignable(dragAtomIndex)) {
          enterMeasurementMode(dragAtomIndex);
          measurementPending.addPoint(dragAtomIndex, null, false);
        }
        return;
      }
      if (isBound(action, ACTION_popupMenu)) {
        char type = 'j';
        if (viewer.getModelkitMode()) {
          Map<String, Object> t = viewer.checkObjectClicked(x, y, Binding.getMouseAction(1,
              Binding.LEFT));
          type = (t != null && "bond".equals(t.get("type")) ? 'b' : viewer
              .findNearestAtomIndex(x, y) >= 0 ? 'a' : 'm');
        }
        viewer.popupMenu(x, y, type);
        return;
      }
      if (dragSelectedMode) {
        haveSelection = true;
        if (isSelectAndDrag) {
          haveSelection = (viewer.findNearestAtomIndex(x, y, true) >= 0);
          // checkPointOrAtomClicked(x, y, mods, pressedCount, true);
        }
        if (!haveSelection)
          return;
        if (isBound(action, ACTION_dragSelected) || isBound(action, ACTION_dragZ))
          viewer.moveSelected(Integer.MIN_VALUE, 0, Integer.MIN_VALUE,
              Integer.MIN_VALUE, Integer.MIN_VALUE, null, false, false);
        return;
      }
      if (viewer.useArcBall())
        viewer.rotateArcBall(x, y, 0);
      checkMotionRotateZoom(action, x, 0, 0, true);
      return;
    case Binding.RELEASED:
      setCurrent(time, x, y, modifiers);
      viewer.spinXYBy(0, 0, 0);
      boolean dragRelease = !pressed.check(x, y, modifiers, time,
          Long.MAX_VALUE);
      viewer.setInMotion(false);
      viewer.setCursor(JmolConstants.CURSOR_DEFAULT);
      action = Binding.getMouseAction(pressedCount, modifiers);
      dragGesture.add(action, x, y, time);
      if (dragRelease)
        viewer.setRotateBondIndex(Integer.MIN_VALUE);
      if (dragAtomIndex >= 0) {
        if (atomPickingMode == PICKING_DRAG_MINIMIZE
            || atomPickingMode == PICKING_DRAG_MINIMIZE_MOLECULE)
          minimize(true);
      }
      if (atomPickingMode == PICKING_ASSIGN_ATOM
          && isBound(action, ACTION_assignNew)) {
        if (measurementPending == null || dragAtomIndex < 0)
          return;
        // H C + -, etc.
        // also check valence and add/remove H atoms as necessary?
        if (measurementPending.getCount() == 2) {
          viewer.undoMoveAction(-1, Token.save, true);
          viewer.script("assign connect "
              + measurementPending.getMeasurementScript(" ", false));
        } else if (pickAtomAssignType.equals("Xx")) {
          exitMeasurementMode();
          viewer.refresh(3, "bond dropped");
        } else {
          if (pressed.inRange(dragged.x, dragged.y)) {
            String s = "assign atom ({" + dragAtomIndex + "}) \""
                + pickAtomAssignType + "\"";
            if (isPickAtomAssignCharge) {
              s += ";{atomindex=" + dragAtomIndex + "}.label='%C'; ";
              viewer.undoMoveAction(dragAtomIndex,
                  AtomCollection.TAINT_FORMALCHARGE, true);
            } else {
              viewer.undoMoveAction(-1, Token.save, true);
            }
            viewer.script(s);
          } else if (!isPickAtomAssignCharge) {
            viewer.undoMoveAction(-1, Token.save, true);
            Atom a = viewer.getModelSet().atoms[dragAtomIndex];
            if (a.getElementNumber() == 1) {
              viewer.script("assign atom ({" + dragAtomIndex + "}) \"X\"");
            } else {
              Point3f ptNew = new Point3f(x, y, a.screenZ);
              viewer.unTransformPoint(ptNew, ptNew);
              viewer.script("assign atom ({" + dragAtomIndex + "}) \""
                  + pickAtomAssignType + "\" " + Escape.escapePt(ptNew));
            }
          }
        }
        exitMeasurementMode();
        return;
      }
      dragAtomIndex = -1;
      boolean isRbAction = isRubberBandSelect(action);
      if (isRbAction) {
        BitSet bs = viewer.findAtomsInRectangle(rectRubber);
        if (bs.length() > 0) {
          String s = Escape.escape(bs);
          if (isBound(action, ACTION_selectOr))
            viewer.script("selectionHalos on;select selected or " + s);
          else if (isBound(action, ACTION_selectAndNot))
            viewer.script("selectionHalos on;select selected and not " + s);
          else
            // ACTION_selectToggle
            viewer.script("selectionHalos on;select selected tog " + s);
        }
        viewer.refresh(3, "mouseReleased");
      }
      rubberbandSelectionMode = (binding.getName() == "drag");
      rectRubber.x = Integer.MAX_VALUE;
      if (dragRelease) {
        viewer.notifyMouseClicked(x, y,
            Binding.getMouseAction(pressedCount, 0), Binding.RELEASED);
      }
      if (drawMode
          && (isBound(action, ACTION_dragDrawObject) || isBound(action,
              ACTION_dragDrawPoint)) || labelMode
          && isBound(action, ACTION_dragLabel)) {
        viewer.checkObjectDragged(Integer.MAX_VALUE, 0, x, y, action);
        return;
      }
      if (dragSelectedMode && isBound(action, ACTION_dragSelected)
          && haveSelection)
        viewer.moveSelected(Integer.MAX_VALUE, 0, Integer.MIN_VALUE,
            Integer.MIN_VALUE, Integer.MIN_VALUE, null, false, false);

      if (dragRelease && checkUserAction(action, x, y, 0, 0, time, Binding.RELEASED))
        return;

      if (viewer.getAllowGestures()) {
        if (isBound(action, ACTION_swipe)) {
          float speed = getExitRate();
          if (speed > 0)
            viewer.spinXYBy(dragGesture.getDX(4, 2), dragGesture.getDY(4, 2),
                speed * 30 * gestureSwipeFactor);
          if (viewer.getLogGestures())
            viewer.log("$NOW$ swipe " + dragGesture + " " + speed);
          return;
        }

      }
      return;
    }
  }

  private boolean haveSelection;
  

  private void minimize(boolean dragDone) {
    viewer.stopMinimization();
    int iAtom = dragAtomIndex;
    if (dragDone)
      dragAtomIndex = -1;
    BitSet bs = (viewer.getMotionFixedAtoms().cardinality() == 0 ? viewer
        .getAtomBits((viewer.isAtomPDB(iAtom) ? Token.group
            : Token.molecule), BitSetUtil.setBit(iAtom)) : BitSetUtil
        .setAll(viewer.getAtomCount()));
    viewer.minimize(Integer.MAX_VALUE, 0, bs, null, 0, false, false, false);
  }

  protected float getExitRate() {
    long dt = dragGesture.getTimeDifference(2);
    return (dt > MININUM_GESTURE_DELAY_MILLISECONDS ? 0 : 
        dragGesture.getSpeedPixelsPerMillisecond(4, 2));
  }

  private boolean isRubberBandSelect(int action) {
    return rubberbandSelectionMode && 
        (  isBound(action, ACTION_selectToggle)
        || isBound(action, ACTION_selectOr)
        || isBound(action, ACTION_selectAndNot)
        );
  }

  Rectangle getRubberBand() {
    if (!rubberbandSelectionMode || rectRubber.x == Integer.MAX_VALUE)
      return null;
    return rectRubber;
  }

  private void calcRectRubberBand() {
    int factor = (viewer.isAntialiased() ? 2 : 1);
    if (current.x < pressed.x) {
      rectRubber.x = current.x * factor;
      rectRubber.width = (pressed.x - current.x) * factor;
    } else {
      rectRubber.x = pressed.x * factor;
      rectRubber.width = (current.x - pressed.x) * factor;
    }
    if (current.y < pressed.y) {
      rectRubber.y = current.y * factor;
      rectRubber.height = (pressed.y - current.y) * factor;
    } else {
      rectRubber.y = pressed.y * factor;
      rectRubber.height = (current.y - pressed.y) * factor;
    }
  }

  private void checkAction(int action, int x, int y, int deltaX, int deltaY,
                           long time, int mode) {
    int mods = Binding.getModifiers(action);
    if (Binding.getModifiers(action) != 0) {
      int newAction = viewer.notifyMouseClicked(x, y, Binding.getMouseAction(
          -pressedCount, mods), mode);
      if (newAction == 0)
        return;
      if (newAction > 0)
        action = newAction;
    }

    if (isRubberBandSelect(action)) {
      calcRectRubberBand();
      viewer.refresh(3, "rubberBand selection");
      return;
    }

    if (checkUserAction(action, x, y, deltaX, deltaY, time, mode))
      return;

    if (viewer.getRotateBondIndex() >= 0) {
      if (isBound(action, ACTION_rotateBranch)) {
        viewer.moveSelected(deltaX, deltaY, Integer.MIN_VALUE, x, y, null, false, false);
        return;
      }
      if (!isBound(action, ACTION_rotate))
        viewer.setRotateBondIndex(-1);
    }
    BitSet bs;
    if (dragAtomIndex >= 0) {
      switch (atomPickingMode) {
      case PICKING_DRAG_SELECTED:
        checkMotion(JmolConstants.CURSOR_MOVE);
        if (isBound(action, ACTION_rotateSelected) && viewer.allowRotateSelected()) {
          viewer.rotateSelected(getDegrees(deltaX, 0), getDegrees(deltaY, 1),
              null);
        } else {
          viewer.moveSelected(deltaX, deltaY, 
              (isBound(action, ACTION_dragZ) ? -deltaY : 
              Integer.MIN_VALUE), Integer.MIN_VALUE, Integer.MIN_VALUE,
              null, true, false);
        }
        return;
      case PICKING_DRAG_MOLECULE:
      case PICKING_DRAG_ATOM:
      case PICKING_DRAG_MINIMIZE:
      case PICKING_DRAG_MINIMIZE_MOLECULE:
        if (dragGesture.getPointCount() == 1)
          viewer.undoMoveAction(dragAtomIndex, AtomCollection.TAINT_COORD, true);
        checkMotion(JmolConstants.CURSOR_MOVE);
        if (isBound(action, ACTION_rotateSelected)) {
          bs = viewer.getAtomBits(Token.molecule, BitSetUtil
              .setBit(dragAtomIndex));
          viewer.rotateSelected(getDegrees(deltaX, 0), getDegrees(deltaY, 1),
              bs);
        } else {
          bs = null;
          switch (atomPickingMode) {
          case PICKING_DRAG_MOLECULE:
          case PICKING_DRAG_MINIMIZE_MOLECULE:
            bs = viewer.getAtomBits(Token.molecule, BitSetUtil
                .setBit(dragAtomIndex));
            viewer.select(bs, false, null, true);
            break;
          }
          viewer.moveAtomWithHydrogens(dragAtomIndex, deltaX, deltaY, 
              (isBound(action, ACTION_dragZ) ? -deltaY : Integer.MIN_VALUE), bs);
        }
        // NAH! if (atomPickingMode == PICKING_DRAG_MINIMIZE_MOLECULE && (dragGesture.getPointCount() % 5 == 0))
        //  minimize(false);
        return;
      }
    }

    if (dragAtomIndex >= 0 && isBound(action, ACTION_assignNew)
        && atomPickingMode == PICKING_ASSIGN_ATOM) {
      int nearestAtomIndex = viewer.findNearestAtomIndex(x, y, false);
      if (nearestAtomIndex >= 0) {
        if (measurementPending != null) {
          measurementPending.setCount(1);
        } else if (measuresEnabled) {
          enterMeasurementMode(nearestAtomIndex);
        }
        addToMeasurement(nearestAtomIndex, null, true);
        measurementPending.setColix(Colix.MAGENTA);
      } else if (measurementPending != null) {
        measurementPending.setCount(1);
        measurementPending.setColix(Colix.GOLD);
      }
      if (measurementPending == null)
        return;
      measurementPending.traceX = x;
      measurementPending.traceY = y;
      viewer.refresh(3, "assignNew");
      return;
    }

    if (!drawMode && !labelMode) {
      if (isBound(action, ACTION_translate)) {
        viewer.translateXYBy(deltaX, deltaY);
        return;
      }

      if (isBound(action, ACTION_center)) {
        if (pressedAtomIndex == Integer.MAX_VALUE)
          pressedAtomIndex = viewer.findNearestAtomIndex(pressed.x, pressed.y);
        Point3f pt = (pressedAtomIndex < 0 ? null : viewer
            .getAtomPoint3f(pressedAtomIndex));
        if (pt == null)
          viewer.translateXYBy(deltaX, deltaY);
        else
          viewer.centerAt(x, y, pt);
        return;
      }

    }

    if (dragSelectedMode && haveSelection
        && (isBound(action, ACTION_dragSelected) || isBound(action, ACTION_rotateSelected))) {
      int iatom = viewer.getSelectionSet(false).nextSetBit(0);
      if (iatom < 0)
        return;
      if (dragGesture.getPointCount() == 1)
        viewer.undoMoveAction(iatom, AtomCollection.TAINT_COORD, true);
      else
        viewer.moveSelected(Integer.MAX_VALUE, 0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, null, false, false);
      checkMotion(JmolConstants.CURSOR_MOVE);
      if (isBound(action, ACTION_rotateSelected) && viewer.allowRotateSelected())
        viewer.rotateSelected(getDegrees(deltaX, 0), getDegrees(deltaY, 1),
            null);
      else
        viewer.moveSelected(deltaX, deltaY, Integer.MIN_VALUE,
            Integer.MIN_VALUE, Integer.MIN_VALUE, null, true, false);
      return;
    }

    if (drawMode
        && (isBound(action, ACTION_dragDrawObject) || isBound(action,
            ACTION_dragDrawPoint)) || labelMode
        && isBound(action, ACTION_dragLabel)) {
      checkMotion(JmolConstants.CURSOR_MOVE);
      viewer.checkObjectDragged(dragged.x, dragged.y, x, y, action);
      return;
    }

    if (checkMotionRotateZoom(action, x, deltaX, deltaY, true)) {
      if (viewer.getSlabEnabled() && checkSlideZoom(action))
        viewer.slabDepthByPixels(deltaY);
      else
        viewer.zoomBy(deltaY);
      return;
    }
    if (isBound(action, ACTION_rotate)) {
      float degX = getDegrees(deltaX, 0);
      float degY = getDegrees(deltaY, 1);
      if (viewer.useArcBall())
        viewer.rotateArcBall(x, y, mouseDragFactor);
      else
        viewer.rotateXYBy(degX, degY);
      return;
    }
    if (isBound(action, ACTION_rotateZorZoom)) {
      if (Math.abs(deltaY) > 5 * Math.abs(deltaX)) {
        // if (deltaY < 0 && deltaX > deltaY || deltaY > 0 && deltaX < deltaY)
        checkMotion(JmolConstants.CURSOR_ZOOM);
        viewer.zoomBy(deltaY);
      } else if (Math.abs(deltaX) > 5 * Math.abs(deltaY)) {
        // if (deltaX < 0 && deltaY > deltaX || deltaX > 0 && deltaY < deltaX)
        checkMotion(JmolConstants.CURSOR_MOVE);
        viewer.rotateZBy(-deltaX, Integer.MAX_VALUE, Integer.MAX_VALUE);
      }
      return;
    } else if (isBound(action, ACTION_wheelZoom)) {
      zoomByFactor(deltaY, Integer.MAX_VALUE, Integer.MAX_VALUE);
      return;
    } else if (isBound(action, ACTION_rotateZ)) {
      checkMotion(JmolConstants.CURSOR_MOVE);
      viewer.rotateZBy(-deltaX, Integer.MAX_VALUE, Integer.MAX_VALUE);
      return;
    }
    if (viewer.getSlabEnabled()) {
      if (isBound(action, ACTION_depth)) {
        viewer.depthByPixels(deltaY);
        return;
      }
      if (isBound(action, ACTION_slab)) {
        viewer.slabByPixels(deltaY);
        return;
      }
      if (isBound(action, ACTION_slabAndDepth)) {
        viewer.slabDepthByPixels(deltaY);
        return;
      }
    }
  }

  protected float getDegrees(int delta, int i) {
    int dim = (i == 0 ? viewer.getScreenWidth() : viewer.getScreenHeight());
    if (dim > 500)
      dim = 500;
    return ((float) delta) / dim * 180 * mouseDragFactor;
  }

  protected void zoomByFactor(int dz, int x, int y) {
    if (dz == 0)
      return;
    checkMotion(JmolConstants.CURSOR_ZOOM);
    viewer.zoomByFactor((float) Math.pow(mouseWheelFactor, dz), x, y);
    viewer.setInMotion(false);
  }

  private boolean checkUserAction(int action, int x, int y, int deltaX,
                                  int deltaY, long time, int mode) {
    if (!binding.isUserAction(action))
      return false;
    Map<String, Object> ht = binding.getBindings();
    Iterator<String> e = ht.keySet().iterator();
    boolean ret = false;
    Object obj;
    while (e.hasNext()) {
      String key = e.next();
      if (key.indexOf(action + "\t") != 0
          || !((obj = ht.get(key)) instanceof String[]))
        continue;
      String script = ((String[]) obj)[1];
      Point3f nearestPoint = null;
      if (script.indexOf("_ATOM") >= 0) {
        int iatom = findNearestAtom(x, y, null, true);
        script = TextFormat.simpleReplace(script, "_ATOM", "({"
            + (iatom >= 0 ? "" + iatom : "") + "})");
        if (iatom >= 0)
          script = TextFormat.simpleReplace(script, "_POINT", Escape
              .escapePt(viewer.getModelSet().atoms[iatom]));
      }
      if (!drawMode
          && (script.indexOf("_POINT") >= 0 || script.indexOf("_OBJECT") >= 0 || script
              .indexOf("_BOND") >= 0)) {
        Map<String, Object> t = viewer.checkObjectClicked(x, y, action);
        if (t != null && (nearestPoint = (Point3f) t.get("pt")) != null) {
          boolean isBond = t.get("type").equals("bond");
          if (isBond)
            script = TextFormat.simpleReplace(script, "_BOND", "[{"
                + t.get("index") + "}]");
          script = TextFormat.simpleReplace(script, "_POINT", Escape
              .escapePt(nearestPoint));
          script = TextFormat
              .simpleReplace(script, "_OBJECT", Escape.escapeMap(t));
        }
        script = TextFormat.simpleReplace(script, "_BOND", "[{}]");
        script = TextFormat.simpleReplace(script, "_OBJECT", "{}");
      }
      script = TextFormat.simpleReplace(script, "_POINT", "{}");
      script = TextFormat.simpleReplace(script, "_ACTION", "" + action);
      script = TextFormat.simpleReplace(script, "_X", "" + x);
      script = TextFormat.simpleReplace(script, "_Y", ""
          + (viewer.getScreenHeight() - y));
      script = TextFormat.simpleReplace(script, "_DELTAX", "" + deltaX);
      script = TextFormat.simpleReplace(script, "_DELTAY", "" + deltaY);
      script = TextFormat.simpleReplace(script, "_TIME", "" + time);
      script = TextFormat.simpleReplace(script, "_MODE", "" + mode);
      viewer.evalStringQuiet(script);
      ret = true;
    }
    return ret;
  }

  /**
   * 
   * @param action
   * @param x
   * @param deltaX
   * @param deltaY
   * @param inMotion
   * @return TRUE if motion was a zoom
   */
  private boolean checkMotionRotateZoom(int action, int x, 
                                        int deltaX, int deltaY,
                                        boolean inMotion) {
    boolean isSlideZoom = checkSlideZoom(action);
    boolean isRotateXY = isBound(action, ACTION_rotate);
    boolean isRotateZorZoom = isBound(action, ACTION_rotateZorZoom);
    if (!isSlideZoom && !isRotateXY && !isRotateZorZoom) 
      return false;
    boolean isZoom = (isRotateZorZoom && (deltaX == 0 || Math.abs(deltaY) > 5 * Math.abs(deltaX)));
    int cursor = (isZoom || isZoomArea(moved.x) || isBound(action, ACTION_wheelZoom) ? JmolConstants.CURSOR_ZOOM 
        : isRotateXY || isRotateZorZoom ? JmolConstants.CURSOR_MOVE : JmolConstants.CURSOR_DEFAULT);
    if (viewer.getCursor() != JmolConstants.CURSOR_WAIT)
      viewer.setCursor(cursor);
    if (inMotion)
      viewer.setInMotion(true);
    return (isZoom || isSlideZoom);
  }

  private boolean checkSlideZoom(int action) {
    return isBound(action, ACTION_slideZoom) && isZoomArea(pressed.x);
  }

  private boolean isZoomArea(int x) {
    return x > viewer.getScreenWidth() * (viewer.isStereoDouble() ? 2 : 1)
        * SLIDE_ZOOM_X_PERCENT / 100f;
  }

  private boolean checkPointOrAtomClicked(int x, int y, int mods,
                                          int clickedCount, long time, 
                                          boolean atomOnly, int mode) {
    if (!viewer.haveModelSet())
      return false;
    // points are always picked up first, then atoms
    // so that atom picking can be superceded by draw picking
    int action = Binding.getMouseAction(clickedCount, mods);
    if (action != Binding.MOVED) {
      checkUserAction(action, x, y, 0, 0, time, mode);
      action = viewer.notifyMouseClicked(x, y, action, mode);
      if (action == Binding.MOVED)
        return false;
    }
    if (isBound(action, ACTION_clickFrank) && viewer.frankClicked(x, y)) {
      viewer.popupMenu(-x, y, 'j');
      return false;
    }
    if (isBound(action, ACTION_clickFrank) && viewer.frankClickedModelKit(x, y)) {
      viewer.popupMenu(0, 0, 'm');
      return false;
    }
    Point3fi nearestPoint = null;
    boolean isBond = false;
    boolean isIsosurface = false;
    Map<String, Object> t = null;
    // t.tok will let us know if this is an atom or a bond that was clicked
    if (!drawMode && !atomOnly) {
      t = viewer.checkObjectClicked(x, y, action);
      if (t != null) {
        isBond = "bond".equals(t.get("type"));
        isIsosurface = "isosurface".equals(t.get("type"));
        nearestPoint = getPoint(t);
      }
    }
    if (isBond)
      clickedCount = 1;

    if (nearestPoint != null && Float.isNaN(nearestPoint.x))
      return false;
    int nearestAtomIndex = findNearestAtom(x, y, nearestPoint, clickedCount > 0);
    
    if (clickedCount == 0 && atomPickingMode != PICKING_ASSIGN_ATOM) {
      // mouse move
      if (measurementPending == null)
        return (nearestAtomIndex >= 0);
      if (nearestPoint != null
          || measurementPending.getIndexOf(nearestAtomIndex) == 0)
        measurementPending.addPoint(nearestAtomIndex, nearestPoint, false);
      if (measurementPending.haveModified())
        viewer.setPendingMeasurement(measurementPending);
      viewer.refresh(3, "measurementPending");
      return (nearestAtomIndex >= 0);
    }
    setMouseMode();

    if (isBound(action, ACTION_stopMotion)) {
      viewer.stopMotion();
      // continue checking --- no need to exit here
    }

    if (viewer.getNavigationMode() && atomPickingMode == PICKING_NAVIGATE
        && isBound(action, ACTION_pickNavigate)) {
      viewer.navTranslatePercent(0f, x * 100f / viewer.getScreenWidth() - 50f,
          y * 100f / viewer.getScreenHeight() - 50f);
      return false;
    }

    // bond change by clicking on a bond
    // bond deletion by clicking a bond
    if (isBond) {
      if (isBound(action, bondPickingMode == PICKING_ROTATE_BOND
          || bondPickingMode == PICKING_ASSIGN_BOND ? ACTION_assignNew
          : ACTION_deleteBond)) {
        if (bondPickingMode == PICKING_ASSIGN_BOND)
          viewer.undoMoveAction(-1, Token.save, true);
        int index = ((Integer) t.get("index")).intValue();
        switch (bondPickingMode) {
        case PICKING_ASSIGN_BOND:
          viewer.script("assign bond [{" + index + "}] \""
              + pickBondAssignType + "\"");
          break;
        case PICKING_ROTATE_BOND:
          viewer.setRotateBondIndex(index);
          break;
        case PICKING_DELETE_BOND:
          viewer.deleteBonds(BitSetUtil.setBit(index));
        }
        return false;
      }
    } else if (isIsosurface) {
      return false;
    } else {
      if (atomPickingMode != PICKING_ASSIGN_ATOM && measurementPending != null
          && isBound(action, ACTION_pickMeasure)) {
        atomOrPointPicked(nearestAtomIndex, nearestPoint, action);
        if (addToMeasurement(nearestAtomIndex, nearestPoint, false) == 4)
          toggleMeasurement();
        return false;
      }

      if (isBound(action, ACTION_setMeasure)) {
        if (measurementPending != null) {
          addToMeasurement(nearestAtomIndex, nearestPoint, true);
          toggleMeasurement();
        } else if (!drawMode && !labelMode && !dragSelectedMode
            && measuresEnabled) {
          enterMeasurementMode(nearestAtomIndex);
          addToMeasurement(nearestAtomIndex, nearestPoint, true);
        }
        atomOrPointPicked(nearestAtomIndex, nearestPoint, action);
        return false;
      }
    }
    boolean isDragSelected = (dragSelectedMode && (isBound(action,
        ACTION_rotateSelected) || isBound(action, ACTION_dragSelected)));
    
    if (isDragSelected || isSelectAction(action)) {
      // TODO: in drawMode the binding changes
        if (!isIsosurface)
          atomOrPointPicked(nearestAtomIndex, nearestPoint, isDragSelected ? 0 : action);
      return (nearestAtomIndex >= 0);
    }
    if (isBound(action, ACTION_reset)) {
      if (nearestAtomIndex < 0)
        viewer.script("!reset");
      return false;
    }
    return (nearestAtomIndex >= 0);
  }

  private Point3fi getPoint(Map<String, Object> t) {
    Point3fi pt = new Point3fi((Point3f) t.get("pt"));
    pt.modelIndex = (short) ((Integer) t.get("modelIndex")).intValue();
    return pt;
  }

  private int findNearestAtom(int x, int y, Point3fi nearestPoint, boolean isClicked) {
    int index = (drawMode || nearestPoint != null ? -1 : viewer
        .findNearestAtomIndex(x, y, false));
    return (index >= 0
        && (isClicked || measurementPending == null)
        && !viewer.isInSelectionSubset(index) ? -1 : index);
  }

  private boolean isSelectAction(int action) {
    return (isBound(action, ACTION_pickAtom) 
        || isBound(action, ACTION_pickPoint)
        || isBound(action, ACTION_selectToggle)
        || isBound(action, ACTION_selectAndNot)
        || isBound(action, ACTION_selectOr)
        || isBound(action, ACTION_selectToggleExtended)
        || isBound(action, ACTION_select));
  }

  protected void checkMotion(int cursor) {
    if (viewer.getCursor() != JmolConstants.CURSOR_WAIT)
      viewer.setCursor(cursor);
    viewer.setInMotion(true);
  }

  private int addToMeasurement(int atomIndex, Point3fi nearestPoint,
                               boolean dblClick) {
    if (atomIndex == -1 && nearestPoint == null || measurementPending == null) {
      exitMeasurementMode();
      return 0;
    }
    int measurementCount = measurementPending.getCount();
    if (measurementPending.traceX != Integer.MIN_VALUE && measurementCount == 2)
      measurementPending.setCount(measurementCount = 1);
    return (measurementCount == 4 && !dblClick ? measurementCount
        : measurementPending.addPoint(atomIndex, nearestPoint, true));
  }

  private void enterMeasurementMode(int iAtom) {
    viewer.setPicked(-1);
    viewer.setPicked(iAtom);
    viewer.setCursor(JmolConstants.CURSOR_CROSSHAIR);
    viewer.setPendingMeasurement(measurementPending = new MeasurementPending(
        viewer.getModelSet()));
    measurementQueued = measurementPending;
  }

  private void exitMeasurementMode() {
    if (measurementPending == null)
      return;
    viewer.setPendingMeasurement(measurementPending = null);
    viewer.setCursor(JmolConstants.CURSOR_DEFAULT);
  }

  private void toggleMeasurement() {
    if (measurementPending == null)
      return;
    int measurementCount = measurementPending.getCount();
    if (measurementCount >= 2 && measurementCount <= 4)
      viewer.script("!measure " + measurementPending.getMeasurementScript(" ", true));
    exitMeasurementMode();
  }

  public void hoverOn(int atomIndex) {
    viewer.hoverOn(atomIndex, Binding.getMouseAction(clickedCount, moved.modifiers));
  }

  public void hoverOff() {
    try {
      viewer.hoverOff();
    } catch (Exception e) {
      // ignore
    }
  }

  class HoverWatcher implements Runnable {
    @Override
	public void run() {
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      int hoverDelay;
      try {
        while (Thread.currentThread().equals(hoverWatcherThread) && (hoverDelay = viewer.getHoverDelay()) > 0) {
          Thread.sleep(hoverDelay);
          if (current.x == moved.x && current.y == moved.y
              && current.time == moved.time) { // the last event was mouse
                                                  // move
            long currentTime = System.currentTimeMillis();
            int howLong = (int) (currentTime - moved.time);
            if (howLong > hoverDelay) {
              if (Thread.currentThread().equals(hoverWatcherThread) && !viewer.getInMotion()
                  && !viewer.getSpinOn() && !viewer.getNavOn()
                  && !viewer.checkObjectHovered(current.x, current.y)) {
                int atomIndex = viewer.findNearestAtomIndex(current.x, current.y);
                if (atomIndex >= 0) {
                  hoverOn(atomIndex);
                }
              }
            }
          }
        }
      } catch (InterruptedException ie) {
        Logger.debug("Hover interrupted");
      } catch (Exception ie) {
        Logger.debug("Hover Exception: " + ie);
      }
    }
  }
  
  //////////////// picking ///////////////////
  
  private MeasurementPending measurementQueued;
  
  private void resetMeasurement() {
    // doesn't reset the measurement that is being picked using
    // double-click, just the one using set picking measure.
    exitMeasurementMode();
    measurementQueued = new MeasurementPending(viewer.getModelSet());    
  }

  private int pickingStyle;
  private int atomPickingMode = PICKING_IDENTIFY;
  private int pickingStyleSelect = PICKINGSTYLE_SELECT_JMOL;
  private int pickingStyleMeasure = PICKINGSTYLE_MEASURE_OFF;
  private int rootPickingStyle = PICKINGSTYLE_SELECT_JMOL;
  private String pickAtomAssignType = "C";
  private char pickBondAssignType = 'p';
  private int bondPickingMode;
  private boolean isPickAtomAssignCharge;

  public String getPickingState() {
    // the pickingMode is not reported in the state. But when we do an UNDO,
    // we want to restore this.
    String script = ";set modelkitMode " + viewer.getModelkitMode()
        + ";set picking " + getPickingModeName(atomPickingMode);
    if (atomPickingMode == PICKING_ASSIGN_ATOM)
      script += "_" + pickAtomAssignType;
    script += ";";
    if (bondPickingMode != PICKING_OFF)
      script += "set picking " + getPickingModeName(bondPickingMode);
    if (bondPickingMode == PICKING_ASSIGN_BOND)
      script += "_" + pickBondAssignType;
    script += ";";
    return script;
  }
  
  int getAtomPickingMode() {
    return atomPickingMode;
  }
    
  void setPickingMode(int pickingMode) {
    switch (pickingMode) {
    case -1: // from  set modelkit OFF
      bondPickingMode = PICKING_IDENTIFY_BOND;
      pickingMode = PICKING_IDENTIFY;
      break;
    case PICKING_IDENTIFY_BOND:
    case PICKING_ROTATE_BOND:
    case PICKING_ASSIGN_BOND:
      viewer.setBooleanProperty("bondPicking", true);
      bondPickingMode = pickingMode;
      return;
    case PICKING_DELETE_BOND:
      bondPickingMode = pickingMode;
      if (viewer.getBondPicking())
        return;
      break;
      // if we have bondPicking mode, then we don't set atomPickingMode to this
    }
    atomPickingMode = pickingMode;
    resetMeasurement();
  }

  void setAtomPickingOption(String option) {
    switch (atomPickingMode) {
    case PICKING_ASSIGN_ATOM:
      pickAtomAssignType = option;
      isPickAtomAssignCharge = (pickAtomAssignType.equals("Pl") || pickAtomAssignType.equals("Mi"));
      break;
    }
  }
  
  void setBondPickingOption(String option) {
    switch (bondPickingMode) {
    case PICKING_ASSIGN_BOND:
      pickBondAssignType = Character.toLowerCase(option.charAt(0));
      break;
    }
  }
  
  int getPickingStyle() {
    return pickingStyle;
  }

  void setPickingStyle(int pickingStyle) {
    this.pickingStyle = pickingStyle;
    if (pickingStyle >= PICKINGSTYLE_MEASURE_ON) {
      pickingStyleMeasure = pickingStyle;
      resetMeasurement();
    } else {
      if (pickingStyle < PICKINGSTYLE_SELECT_DRAG)
        rootPickingStyle = pickingStyle;
      pickingStyleSelect = pickingStyle;
    }
    rubberbandSelectionMode = false;
    switch (pickingStyleSelect) {
    case PICKINGSTYLE_SELECT_PFAAT:
      if (binding.getName() != "extendedSelect") 
        setBinding(pfaatBinding = (pfaatBinding == null ? new PfaatBinding() : pfaatBinding));
      break;
    case PICKINGSTYLE_SELECT_DRAG:
      if (binding.getName() != "drag")
        setBinding(dragBinding = (dragBinding == null ? new DragBinding() : dragBinding));
      rubberbandSelectionMode = true;
      break;
    case PICKINGSTYLE_SELECT_RASMOL:
      if (binding.getName() != "selectOrToggle")
        setBinding(rasmolBinding = (rasmolBinding == null ? new RasmolBinding() : rasmolBinding));
      break;
    default:
      if (binding != jmolBinding)
        setBinding(jmolBinding);
    }
    if (binding.getName() != "drag")
      predragBinding = binding;
  }

  protected void setBinding(Binding newBinding) {
    binding = newBinding;
  }
  
  private void atomOrPointPicked(int atomIndex, Point3fi ptClicked, int action) {
    // atomIndex < 0 is off structure.
    // if picking spin or picking symmetry is on, then 
    // we need to enter this method to process those events.
    if (atomIndex < 0) {
      resetMeasurement(); // for set picking measure only
      if (isBound(action, ACTION_selectNone)) {
        viewer.script("select none");
        return;
      }
      if (atomPickingMode != PICKING_SPIN
          && atomPickingMode != PICKING_SYMMETRY)
        return;
    }
    int n = 2;
    switch (atomPickingMode) {
    case PICKING_DRAG_ATOM:
      // this is done in mouse drag, not mouse release
    case PICKING_DRAG_MINIMIZE:
      return;
    case PICKING_OFF:
      return;
    case PICKING_STRUTS:
    case PICKING_CONNECT:
    case PICKING_DELETE_BOND:
      boolean isDelete = (atomPickingMode == PICKING_DELETE_BOND);
      boolean isStruts = (atomPickingMode == PICKING_STRUTS);
      if (!isBound(action, (isDelete ? ACTION_deleteBond : ACTION_connectAtoms)))
        return;
      if (measurementQueued == null || measurementQueued.getCount() == 0 || measurementQueued.getCount() > 2) {
        resetMeasurement();
        enterMeasurementMode(atomIndex);
      }
      addToMeasurement(atomIndex, ptClicked, true);
      if (queueAtom(atomIndex, ptClicked) != 2)
        return;
      String cAction = (isDelete
          || measurementQueued.isConnected(viewer.getModelSet().atoms, 2) ? " DELETE"
          : isStruts ? "STRUTS" : "");
      viewer.script("connect "
          + measurementQueued.getMeasurementScript(" ", true) + cAction);
      resetMeasurement();
      return;
    case PICKING_MEASURE_TORSION:
      n++;
      //$FALL-THROUGH$
    case PICKING_MEASURE_ANGLE:
      n++;
      //$FALL-THROUGH$
    case PICKING_MEASURE:
    case PICKING_MEASURE_DISTANCE:
    case PICKING_MEASURE_SEQUENCE:
      if (!isBound(action, ACTION_pickMeasure))
        return;
      if (measurementQueued == null || measurementQueued.getCount() == 0 || measurementQueued.getCount() > n) {
        resetMeasurement();
        enterMeasurementMode(atomIndex);
      }
      addToMeasurement(atomIndex, ptClicked, true);
      queueAtom(atomIndex, ptClicked);
      int i = measurementQueued.getCount();
      if (i == 1) {
        viewer.setPicked(-1);
        viewer.setPicked(atomIndex);
      }
      if (i < n)
        return;
      if (atomPickingMode == PICKING_MEASURE_SEQUENCE) {
        getSequence();
      } else {
        viewer.setStatusMeasuring("measurePicked", n, measurementQueued
            .getStringDetail(), measurementQueued.getValue());
        if (atomPickingMode == PICKING_MEASURE
            || pickingStyleMeasure == PICKINGSTYLE_MEASURE_ON) {
          viewer.script("measure "
              + measurementQueued.getMeasurementScript(" ", true));
        }
      }
      resetMeasurement();
      return;
    }
    int mode = (measurementPending != null
        && atomPickingMode != PICKING_IDENTIFY ? PICKING_IDENTIFY
        : atomPickingMode);
    switch (mode) {
    case PICKING_CENTER:
      if (!isBound(action, ACTION_pickAtom))
        return;
      if (ptClicked == null) {
        viewer.script("zoomTo (atomindex=" + atomIndex + ")");
        viewer.setStatusAtomPicked(atomIndex, null);
      } else {
        viewer.script("zoomTo " + Escape.escapePt(ptClicked));
      }
      return;
    case PICKING_SPIN:
    case PICKING_SYMMETRY:
      checkTwoAtomAction(action, ptClicked, atomIndex);
    }
    if (ptClicked != null)
      return;
    // atoms only here:
    BitSet bs;
    String spec = "atomindex=" + atomIndex;
    switch (mode) {
    case PICKING_IDENTIFY:
      if (isBound(action, ACTION_pickAtom))
        viewer.setStatusAtomPicked(atomIndex, null);
      return;
    case PICKING_LABEL:
      if (isBound(action, ACTION_pickLabel)) {
        viewer.script("set labeltoggle {atomindex=" + atomIndex + "}");
        viewer.setStatusAtomPicked(atomIndex, null);
      }
      return;
    case PICKING_INVERT_STEREO:
      if (isBound(action, ACTION_assignNew)) {
        bs = viewer.getAtomBitSet("connected(atomIndex=" + atomIndex
            + ") and !within(SMARTS,'[r50,R]')");
        int nb = bs.cardinality();
        switch (nb) {
        case 0:
        case 1:
          // not enough non-ring atoms
          return;
        case 2:
          break;
        case 3:
        case 4:
          // three or four are not in a ring. So let's find the shortest two
          // branches and invert them.
          int[] lengths = new int[nb];
          int[] points = new int[nb];
          int ni = 0;
          for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1), ni++) {
            lengths[ni] = viewer.getBranchBitSet(i, atomIndex).cardinality();
            points[ni] = i;
          }
          for (int j = 0; j < nb - 2; j++) {
            int max = Integer.MIN_VALUE;
            int imax = 0;
            for (int i = 0; i < nb; i++)
              if (lengths[i] >= max && bs.get(points[i])) {
                imax = points[i];
                max = lengths[i];
              }
            bs.clear(imax);
          }
        }
        viewer.undoMoveAction(atomIndex, AtomCollection.TAINT_COORD, true);
        viewer.invertSelected(null, null, atomIndex, bs);
        viewer.setStatusAtomPicked(atomIndex, "inverted: " + Escape.escape(bs));
      }
      return;
    case PICKING_DELETE_ATOM:
      if (isBound(action, ACTION_deleteAtom)) {
        bs = getSelectionSet("(" + spec + ")");
        viewer.deleteAtoms(bs, false);
        viewer.setStatusAtomPicked(atomIndex, "deleted: " + Escape.escape(bs));
      }
      return;
    }
    // set picking select options:
    switch (atomPickingMode) {
    default:
      return;
    case PICKING_SELECT_ATOM:
      applySelectStyle(spec, action);
      break;
    case PICKING_SELECT_GROUP:
      applySelectStyle("within(group, " + spec + ")", action);
      break;
    case PICKING_SELECT_CHAIN:
      applySelectStyle("within(chain, " + spec + ")", action);
      break;
    case PICKING_SELECT_POLYMER:
      applySelectStyle("within(polymer, " + spec + ")", action);
      break;
    case PICKING_SELECT_STRUCTURE:
      applySelectStyle("within(structure, " + spec + ")", action);
      break;
    case PICKING_SELECT_MOLECULE:
      applySelectStyle("within(molecule, " + spec + ")", action);
      break;
    case PICKING_SELECT_MODEL:
      applySelectStyle("within(model, " + spec + ")", action);
      break;
    // only the next two use VISIBLE (as per the documentation)
    case PICKING_SELECT_ELEMENT:
      applySelectStyle("visible and within(element, " + spec + ")", action);
      break;
    case PICKING_SELECT_SITE:
      applySelectStyle("visible and within(site, " + spec + ")", action);
      break;
    }
    viewer.clearClickCount();
    viewer.setStatusAtomPicked(atomIndex, null);

  }

  private void getSequence() {
    int a1 = measurementQueued.getAtomIndex(1);
    int a2 = measurementQueued.getAtomIndex(2);
    if (a1 < 0 || a2 < 0)
      return;
    String sequence = viewer.getSmiles(a1, a2, null, true, false, false, false);
    viewer.setStatusMeasuring("measureSequence", -2, sequence, 0);
  }

  private void checkTwoAtomAction(int action, Point3fi ptClicked, int atomIndex) {
    if (!isBound(action, ACTION_pickAtom))
      return;
    boolean isSpin = (atomPickingMode == PICKING_SPIN);
    if (viewer.getSpinOn() || viewer.getNavOn() || viewer.getPendingMeasurement() != null) {
      resetMeasurement();
      if (viewer.getSpinOn())
        viewer.script("spin off");
      return;
    }
    if (measurementQueued.getCount() >= 2)
      resetMeasurement();
    int queuedAtomCount = measurementQueued.getCount(); 
    if (queuedAtomCount == 1) {
      if (ptClicked == null) {
        if (measurementQueued.getAtomIndex(1) == atomIndex)
          return;
      } else {
        if (measurementQueued.getAtom(1).distance(ptClicked) == 0)
          return;
      }
    }
    if (atomIndex >= 0 || ptClicked != null)
      queuedAtomCount = queueAtom(atomIndex, ptClicked);
    if (queuedAtomCount < 2) {
      if (isSpin)
      viewer.scriptStatus(queuedAtomCount == 1 ?
          GT._("pick one more atom in order to spin the model around an axis") :
          GT._("pick two atoms in order to spin the model around an axis"));
      else
        viewer.scriptStatus(queuedAtomCount == 1 ?
            GT._("pick one more atom in order to display the symmetry relationship") :
            GT._("pick two atoms in order to display the symmetry relationship between them"));
      return;
    }
    String s = measurementQueued.getMeasurementScript(" ", false);
    if (isSpin)
      viewer.script("spin" + s + " " + viewer.getPickingSpinRate());
    else  
      viewer.script("draw symop" + s + ";show symop" + s);
  }

  private int queueAtom(int atomIndex, Point3fi ptClicked) {
    int n = measurementQueued.addPoint(atomIndex, ptClicked, true);
    if (atomIndex >= 0)
      viewer.setStatusAtomPicked(atomIndex, "Unit #" + n + ":"//"Atom"->"Unit" -modified -hcf
          + viewer.getAtomInfo(atomIndex));
    return n;
  }

  private boolean selectionWorking = false;
  private ScriptEvaluator eval;
  private void applySelectStyle(String item, int action) {
    if (measurementPending != null || selectionWorking)
      return;
    selectionWorking = true;
    String s = (rubberbandSelectionMode || isBound(action,
            ACTION_selectToggle) ? "selected and not (" + item
            + ") or (not selected) and " 
            : isBound(action, ACTION_selectAndNot) ? "selected and not "
            : isBound(action, ACTION_selectOr) ? "selected or " 
            : action == 0 || isBound(action, ACTION_selectToggleExtended) ? "selected tog " 
            : isBound(action, ACTION_select) ? "" : null);
    if (s != null) {
      s += "(" + item + ")";
      if (Logger.debugging)
        Logger.debug(s);
      BitSet bs = getSelectionSet(s);
      if (bs != null) {
        viewer.select(bs, false, null, false);
        viewer.refresh(3, "selections set");
      }
    }
    selectionWorking = false;
  }

  private BitSet getSelectionSet(String script) {
    try {
      if (eval == null)
        eval = new ScriptEvaluator(viewer);
      return viewer.getAtomBitSet(eval, script);
    } catch (Exception e) {
      // ignore
    }
    return null;
  }

  protected class MotionPoint {
    int index;
    int x;
    int y;
    long time;

    void set(int index, int x, int y, long time) {
      this.index = index;
      this.x = x;
      this.y = y;
      this.time = time;
    }
    
    @Override
    public String toString() {
      return "[x = " + x + " y = " + y + " time = " + time + " ]";
    }
  }
  
  protected Gesture dragGesture = new Gesture(20);
  
  protected class Gesture {
    private int action;
    MotionPoint[] nodes;
    private int ptNext;
    private long time0;

    Gesture(int nPoints) {
      nodes = new MotionPoint[nPoints];
      for (int i = 0; i < nPoints; i++)
        nodes[i] = new MotionPoint();
    }
    
    void setAction(int action, long time) {
      this.action = action;
      ptNext = 0;
      time0 = time;
      for (int i = 0; i < nodes.length; i++)
        nodes[i].index = -1;
    }
    
    int getAction() {
      return action;
    }
  
    int add(int action, int x, int y, long time) {
      this.action = action;
      getNode(ptNext).set(ptNext, x, y, time - time0);
      ptNext++;
      return ptNext;
    }
    
    public long getTimeDifference(int nPoints) {
      nPoints = getPointCount(nPoints, 0);
      if (nPoints < 2)
        return 0;
      MotionPoint mp1 = getNode(ptNext - 1);
      MotionPoint mp0 = getNode(ptNext - nPoints);
      return mp1.time - mp0.time;
    }

    public float getSpeedPixelsPerMillisecond(int nPoints, int nPointsPrevious) {
      nPoints = getPointCount(nPoints, nPointsPrevious);
      if (nPoints < 2)
        return 0;
      MotionPoint mp1 = getNode(ptNext - 1 - nPointsPrevious);
      MotionPoint mp0 = getNode(ptNext - nPoints - nPointsPrevious);
      float dx = ((float) (mp1.x - mp0.x)) / viewer.getScreenWidth() * 360;
      float dy = ((float) (mp1.y - mp0.y)) / viewer.getScreenHeight() * 360;
      return (float) Math.sqrt(dx * dx + dy * dy) / (mp1.time - mp0.time);
    }

    int getDX(int nPoints, int nPointsPrevious) {
      nPoints = getPointCount(nPoints, nPointsPrevious);
      if (nPoints < 2)
        return 0;
      MotionPoint mp1 = getNode(ptNext - 1 - nPointsPrevious);
      MotionPoint mp0 = getNode(ptNext - nPoints - nPointsPrevious);
      return mp1.x - mp0.x;
    }

    int getDY(int nPoints, int nPointsPrevious) {
      nPoints = getPointCount(nPoints, nPointsPrevious);
      if (nPoints < 2)
        return 0;
      MotionPoint mp1 = getNode(ptNext - 1 - nPointsPrevious);
      MotionPoint mp0 = getNode(ptNext - nPoints - nPointsPrevious);
      return mp1.y - mp0.y;
    }

    int getPointCount() {
      return ptNext;
    }
    
    int getPointCount(int nPoints, int nPointsPrevious) {
      if (nPoints > nodes.length - nPointsPrevious)
        nPoints = nodes.length - nPointsPrevious;
      int n = nPoints + 1;
      for (; --n >= 0; )
        if (getNode(ptNext - n - nPointsPrevious).index >= 0)
          break;
      return n;
    }

    MotionPoint getNode(int i) {
      return nodes[(i + nodes.length + nodes.length) % nodes.length];
    }
    
    @Override
    public String toString() {
      if (nodes.length == 0) return "" + this;
      return Binding.getMouseActionName(action, false) + " nPoints = " + ptNext + " " + nodes[0];
    }
  }

} 
