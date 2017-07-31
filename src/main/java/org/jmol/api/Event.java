/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2006  The Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

package org.jmol.api;

public class Event {

  public static final int MOUSE_LEFT = 16;
  public static final int MOUSE_WHEEL = 32;
  public static final int MOUSE_DOWN = 501;//InputEvent.MOUSE_DOWN;
  public static final int MOUSE_UP = 502; //Event.MOUSE_UP;
  public static final int MOUSE_DRAG = 506; //Event.MOUSE_DRAG;
  
  
  public static final int SHIFT_MASK =  1;//InputEvent.SHIFT_MASK;
  public static final int ALT_MASK =    8;//InputEvent.ALT_MASK;
  public static final int CTRL_MASK =   2;//InputEvent.CTRL_MASK;
  public static final int META_MASK =   4;//InputEvent.META_MASK;
  public static final int VK_SHIFT =   16;//KeyEvent.VK_SHIFT;
  public static final int VK_ALT =     18;//KeyEvent.VK_ALT;
  public static final int VK_CONTROL = 17;//KeyEvent.VK_CONTROL;
  public static final int VK_LEFT =    37;//KeyEvent.VK_LEFT;
  public static final int VK_RIGHT =   39;//KeyEvent.VK_RIGHT;
  public static final int VK_PERIOD =  46;//KeyEvent.VK_PERIOD;
  public static final int VK_SPACE =   32;//KeyEvent.VK_SPACE;
  public static final int VK_DOWN =    40;//KeyEvent.VK_DOWN;
  public static final int VK_UP =      38;//KeyEvent.VK_UP;

}
