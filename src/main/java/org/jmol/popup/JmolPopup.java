/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2011-10-14 11:28:38 -0500 (Fri, 14 Oct 2011) $
 * $Revision: 16354 $
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

import org.jmol.api.JmolPopupInterface;
import org.jmol.i18n.GT;
import org.jmol.viewer.Viewer;

public class JmolPopup extends SwingPopup implements JmolPopupInterface {
  
  /*
   * If adding a custom popup menu to an application, simply subclass 
   * initialize() and specify a different resource bundle. 
   * 
   * If you are not using Java awt/Swing, then you need to also overload 
   * Swingpopup.java and extend it as desired. Or completely omit this package 
   * 
   * Note that changes here should also be reflected in org.jmol.modelkit.ModelKitPopup.
   * 
   */
  public JmolPopup() {
    // required by reflection
  }

  @Override
public void initialize(Viewer viewer, String menu) {
    isModelKit = false;
    asPopup = true;
    boolean doTranslate = GT.getDoTranslate();
    GT.setDoTranslate(true);
    initialize(viewer, new MainPopupResourceBundle(strMenuStructure = menu,
        menuText));
    GT.setDoTranslate(doTranslate);
  }

}
