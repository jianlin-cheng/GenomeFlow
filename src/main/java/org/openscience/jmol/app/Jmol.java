/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-09 22:50:29 -0500 (Sun, 09 Sep 2012) $
 * $Revision: 17545 $
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
package org.openscience.jmol.app;

import java.awt.Point;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JFrame;

import org.openscience.jmol.app.jmolpanel.JmolPanel;
import org.openscience.jmol.app.jmolpanel.Splash;

public class Jmol extends JmolPanel {
	public Jmol(JmolApp jmolApp, Splash splash, JFrame frame, Jmol parent,
			int startupWidth, int startupHeight,
			Map<String, Object> viewerOptions, Point loc) {
		super(jmolApp, splash, frame, parent, startupWidth, startupHeight,
				viewerOptions, loc);
	}

	public static void main(String[] args) {
		JmolApp jmolApp = new JmolApp(args);
		startJmol(jmolApp);
	}

	public static Jmol getJmol(JFrame baseframe, int width, int height,
			Map<String, Object> viewerOptions) {
		JmolApp jmolApp = new JmolApp(new String[] {});
		jmolApp.startupHeight = height;
		jmolApp.startupWidth = width;
		jmolApp.info = (viewerOptions == null ? new Hashtable<String, Object>()
				: viewerOptions);
		return getJmol(jmolApp, baseframe);
	}

}
