/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2011  The Jmol Development Team
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

package org.jmol.viewer;

import org.jmol.util.Logger;

class TimeoutThread extends Thread {
  String name;
  private int ms;
  private long targetTime;
  private int status;
  String script;
  private boolean triggered = true;
  private Viewer viewer;
  
  TimeoutThread(Viewer viewer, String name, int ms, String script) {
    this.viewer = viewer;
    this.name = name;
    set(ms, script);
  }
  
  void set(int ms, String script) {
    this.ms = ms;
    targetTime = System.currentTimeMillis() + Math.abs(ms);
    if (script != null)
      this.script = script; 
  }

  void trigger() {
    triggered = (ms < 0);
  }
  
  @Override
  public String toString() {
    return "timeout name=" + name + " executions=" + status + " mSec=" + ms 
    + " secRemaining=" + (targetTime - System.currentTimeMillis())/1000f + " script=" + script + " thread=" + Thread.currentThread().getName();      
  }
  
  @Override
  public void run() {
    if (script == null || script.length() == 0 || ms == 0)
      return;
    Thread.currentThread().setName("timeout " + name);
    //if (true || Logger.debugging) 
    //Logger.info(toString());
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    try {
      while (true) {
        Thread.sleep(26);
        if (targetTime > System.currentTimeMillis())
          continue;
        status++;
        boolean looping = (ms < 0);
        targetTime += Math.abs(ms);
        if (viewer.timeouts.get(name) == null)
          break;
        if (!looping)
          viewer.timeouts.remove(name);
        if (triggered) {
          triggered = false;
          viewer.evalStringQuiet((looping ? script + ";\ntimeout ID \"" + name + "\";" : script));
        } else {
        }
        if (!looping)
          break;
      }
    } catch (InterruptedException ie) {
      //Logger.info("Timeout " + this + " interrupted");
    } catch (Exception ie) {
      Logger.info("Timeout " + name + " Exception: " + ie);
    }
    viewer.timeouts.remove(name);
  }
}