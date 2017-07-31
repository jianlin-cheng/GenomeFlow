/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2010-05-11 15:47:18 -0500 (Tue, 11 May 2010) $
 * $Revision: 13064 $
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
package org.jmol.popup;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.jmol.i18n.GT;
import org.jmol.util.Elements;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;


/**
 * all popup-related awt/swing class references are in this file.
 */
abstract public class SwingPopup extends GenericPopup {

  
  @Override
  public void finalize() {
    System.out.println("SwingPopup Finalize " + this);
  }
  
  private final static int MENUITEM_HEIGHT = 20;

  private MenuItemListener mil;
  private CheckboxMenuItemListener cmil;
  private MenuMouseListener mfl;

  public SwingPopup() {
    // required by reflection
  }

  public void dispose() {
    clearListeners(popupMenu);
    clearListeners(frankPopup);
    popupMenu = null;
    frankPopup = null;
  }
  
  private void clearListeners(Object menu) {
    if (menu == null)
      return;
    Component[] subMenus = (menu instanceof JPopupMenu ? ((JPopupMenu) menu)
        .getComponents() : ((JMenu) menu).getPopupMenu().getComponents());
    for (int i = 0; i < subMenus.length; i++) {
      Component m = subMenus[i];
      if (m instanceof JMenu) {
        clearListeners(((JMenu) m).getPopupMenu());
      } else {
        try {
          m.removeMouseListener(mfl);
          ((AbstractButton) m).removeActionListener(mil);
          ((AbstractButton) m).removeItemListener(cmil);
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }

  public void show(int x, int y) {
    // main entry point from Viewer
    // called via JmolPopupInterface
    if (!viewer.haveDisplay)
      return;
    show(x, y, false);
    if (x < 0) {
      getViewerData();
      setFrankMenu(currentMenuItemId);
      thisx = -x - 50;
      if (nFrankList > 1) {
        thisy = y - nFrankList * MENUITEM_HEIGHT;
        showPopupMenu(frankPopup, thisx, thisy);
        return;
      }
    }
    restorePopupMenu();
    if (asPopup)
      showPopupMenu(popupMenu, thisx, thisy);
  }  

  @Override
  protected void set(Viewer viewer) {
    super.set(viewer);
  }

  protected void initialize(Viewer viewer, PopupResource bundle) {
    String title = bundle.getMenuName();
    set(viewer);
    popupMenu = new JPopupMenu(title);
    menuName = title;
    build(title, popupMenu, bundle);

  }

  /**
   * update the button depending upon its type
   * 
   * @param b
   * @param entry
   * @param script
   */
  private void updateButton(AbstractButton b, String entry, String script) {
    String[] ret = new String[] { entry };    
    Object icon = getEntryIcon(ret);
    entry = ret[0];
    if (icon != null)
      b.setIcon((ImageIcon) icon);
    if (entry != null)
      b.setText(entry);
    if (script != null)
      b.setActionCommand(script);
  }

  ////////////////////////////////////////////////////////////////

  /// required abstract classes ///

  @Override
  protected void addButtonGroupItem(Object newMenu) {
    if (buttonGroup == null)
      buttonGroup = new ButtonGroup();
    ((ButtonGroup) buttonGroup).add((JMenuItem) newMenu);
  }

  @Override
  protected Object addCheckboxMenuItem(Object menu, String entry,
                                       String basename, String id,
                                       boolean state, boolean isRadio) {
    JMenuItem jm;
    if (isRadio) {
      JRadioButtonMenuItem jr = new JRadioButtonMenuItem(entry);
      jm = jr;
      jr.setArmed(state);
    } else {
      JCheckBoxMenuItem jcmi = new JCheckBoxMenuItem(entry);
      jm = jcmi;
      jcmi.setState(state);
    }
    jm.setSelected(state);
    jm.addItemListener(cmil);
    jm.setActionCommand(basename);
    updateButton(jm, entry, basename);
    if (id != null && id.startsWith("Focus")) {
      jm.addMouseListener(mfl);
      id = ((Component) menu).getName() + "." + id;
    }
    jm.setName(id == null ? ((Component) menu).getName() + "." : id);
    addToMenu(menu, jm);
    return jm;
  }

  @Override
  protected Object addMenuItem(Object menu, String entry, String script,
                               String id) {
    JMenuItem jmi = new JMenuItem(entry);
    updateButton(jmi, entry, script);
    jmi.addActionListener(mil);
    if (id != null && id.startsWith("Focus")) {
      jmi.addMouseListener(mfl);
      id = ((Component) menu).getName() + "." + id;
    }
    jmi.setName(id == null ? ((Component) menu).getName() + "." : id);
    addToMenu(menu, jmi);
    return jmi;
  }

  @Override
  protected void addMenuSeparator(Object menu) {
    if (menu instanceof JPopupMenu)
      ((JPopupMenu) menu).addSeparator();
    else
      ((JMenu) menu).addSeparator();
  }

  @Override
  protected void addMenuSubMenu(Object menu, Object subMenu) {
    addToMenu(menu, subMenu);
  }

  @Override
  protected void addToMenu(Object menu, Object item) {
    if (menu instanceof JPopupMenu) {
      ((JPopupMenu) menu).add((JComponent) item);
    } else if (menu instanceof JMenu) {
      ((JMenu) menu).add((JComponent) item);
    } else {
      Logger.warn("cannot add object to menu: " + menu);
    }
  }

  @Override
  protected void enableMenu(Object menu, boolean enable) {
    if (menu instanceof JMenuItem) {
      enableMenuItem(menu, enable);
      return;
    }
    try {
      ((JMenu) menu).setEnabled(enable);
    } catch (Exception e) {
      //no menu item;
    }
  }

  @Override
  protected void enableMenuItem(Object item, boolean enable) {
    try {
      ((JMenuItem) item).setEnabled(enable);
    } catch (Exception e) {
      //no menu item;
    }
  }

  @Override
  protected String getId(Object menu) {
    return ((Component) menu).getName();
  }

  @Override
  protected Object getImageIcon(URL imageUrl) {
    return new ImageIcon(imageUrl);
  }

  @Override
  protected int getMenuItemCount(Object menu) {
    return ((JMenu) menu).getItemCount();
  }

  @Override
  protected Object newMenu(String entry, String id) {
    JMenu jm = new JMenu(entry);
    updateButton(jm, entry, null);
    jm.setName(id);
    jm.setAutoscrolls(true);
    return jm;
  }

  @Override
  protected void removeMenuItem(Object menu, int index) {
    ((JMenu) menu).remove(index);
  }

  @Override
  protected void removeAll(Object menu) {
    ((JMenu) menu).removeAll();
  }

  @Override
  protected void renameMenu(Object menu, String entry) {
    ((JMenu) menu).setText(entry);
  }

  @Override
  protected void setAutoscrolls(Object menu) {
    ((JMenu) menu).setAutoscrolls(true);
  }

  @Override
  protected void setCheckBoxState(Object item, boolean state) {
    if (item instanceof JCheckBoxMenuItem)
      ((JCheckBoxMenuItem) item).setState(state);
    else
      ((JRadioButtonMenuItem) item).setArmed(state);
    ((JMenuItem) item).setSelected(state);
  }

  @Override
  protected void setCheckBoxValue(Object source) {
    JMenuItem jcmi = (JMenuItem) source;
    setCheckBoxValue(jcmi, jcmi.getActionCommand(), jcmi.isSelected());
  }

  @Override
  protected void setLabel(Object menu, String entry) {
    if (menu instanceof JMenuItem)
      ((JMenuItem) menu).setText(entry);
    else
      ((JMenu) menu).setText(entry);
  }

  @Override
  protected void setMenuListeners() {
    mil = new MenuItemListener();
    cmil = new CheckboxMenuItemListener();
    mfl = new MenuMouseListener();
  }
  
  class MenuItemListener implements ActionListener {
    @Override
	public void actionPerformed(ActionEvent e) {
      checkMenuClick(e.getSource(), e.getActionCommand());
    }
  }

  class MenuMouseListener implements MouseListener {

    @Override
	public void mouseClicked(MouseEvent e) {
    }

    @Override
	public void mouseEntered(MouseEvent e) {
      checkMenuFocus(e.getSource(), true);
    }

    @Override
	public void mouseExited(MouseEvent e) {
      checkMenuFocus(e.getSource(), false);
    }

    @Override
	public void mousePressed(MouseEvent e) {
    }

    @Override
	public void mouseReleased(MouseEvent e) {
    }

  }

  class CheckboxMenuItemListener implements ItemListener {
    @Override
	public void itemStateChanged(ItemEvent e) {
      restorePopupMenu();
      setCheckBoxValue(e.getSource());
      String id = getId(e.getSource());
      if (id != null) {
        currentMenuItemId = id;
      }
      //Logger.debug("CheckboxMenuItemListener() " + e.getSource());
    }
  }

  @Override
  protected void checkMenuClick(Object source, String script) {
    if (script.equals("clearQ")) {
      for (Object o : htCheckbox.values()) {
        JMenuItem item = (JMenuItem) o;
        if (item.getActionCommand().indexOf(":??") < 0)
          continue;        
        setLabel(item, "??");
        item.setActionCommand("_??P!:");
        item.setSelected(false);
        item.setArmed(false);
      }
      viewer.evalStringQuiet("set picking assignAtom_C");
      return;
    }
    super.checkMenuClick(source, script);  
  }
  

  //////////////// JmolPopup methods ///////////
    
  public void checkMenuFocus(Object source, boolean isFocus) {
    if (source instanceof JMenuItem) {
      String name = ((JMenuItem) source).getName();
      if (name.indexOf("Focus") < 0)
        return;
      if (isFocus) {
        viewer.script("selectionHalos ON;" + ((JMenuItem) source).getActionCommand());
      } else {
        viewer.script("selectionHalos OFF");
      }
    }
  }

  @Override
  protected void insertMenuSubMenu(Object menu, Object subMenu, int index) {
    if (menu instanceof JPopupMenu)
      ((JPopupMenu) menu).insert((JMenu) subMenu, index);
    else
      ((JMenu) menu).insert((JMenu) subMenu, index);
  }

  @Override
  protected void createFrankPopup() {
    frankPopup = new JPopupMenu("Frank");
  }

  @Override
  protected void resetFrankMenu() {
    ((JPopupMenu)frankPopup).removeAll();
  }


  @Override
  protected void getMenuAsText(StringBuffer sb, int level, Object menu,
                                String menuName) {
    String name = menuName;
    Component[] subMenus = (menu instanceof JPopupMenu ? ((JPopupMenu) menu)
        .getComponents() : ((JMenu) menu).getPopupMenu().getComponents());
    for (int i = 0; i < subMenus.length; i++) {
      Object m = subMenus[i];
      String flags;
      if (m instanceof JMenu) {
        JMenu jm = (JMenu) m;
        name = jm.getName();
        flags = "enabled:" + jm.isEnabled();
        addItemText(sb, 'M', level, name, jm.getText(), null, flags);
        getMenuAsText(sb, level + 1, ((JMenu) m).getPopupMenu(), name);
      } else if (m instanceof JMenuItem) {
        JMenuItem jmi = (JMenuItem) m;
        flags = "enabled:" + jmi.isEnabled();
        if (m instanceof JCheckBoxMenuItem)
          flags += ";checked:" + ((JCheckBoxMenuItem) m).getState();
        String script = fixScript(jmi.getName(), jmi.getActionCommand());
        addItemText(sb, 'I', level, jmi.getName(), jmi.getText(), script,
            flags);
      } else {
        addItemText(sb, 'S', level, name, null, null, null);
      }
    }
  }
  
  @Override
  protected Object getParent(Object menu) {
    return ((JMenu) menu).getParent();
  }

  @Override
  protected int getPosition(Object menu) {
    Object p = getParent(menu);
    if (p instanceof JPopupMenu) {
      for (int i = ((JPopupMenu) p).getComponentCount(); --i >= 0;)
        if (((JPopupMenu) p).getComponent(i) == menu)
          return i;
    } else {
      for (int i = ((JMenu) p).getItemCount(); --i >= 0;)
        if (((JMenu) p).getItem(i) == menu)
          return i;
    }
    return -1;
  }

  @Override
  protected String setCheckBoxOption(Object item, String name, String what) {
    if (isModelKit) {
      // atom type
      String element = JOptionPane.showInputDialog(GT._("Element?"), "");
      if (element == null
          || Elements.elementNumberFromSymbol(element, true) == 0)
        return null;
      setLabel(item, element);
      ((JMenuItem) item).setActionCommand("assignAtom_" + element + "P!:??");
      return "set picking assignAtom_" + element;
    }
    return null;
  }

  @Override
  protected void showPopupMenu(Object popup, int x, int y) {
    try {
      ((JPopupMenu)popup).show((Component) viewer.getDisplay(), x, y);
    } catch (Exception e) {
      // ignore
    }
  }

}
