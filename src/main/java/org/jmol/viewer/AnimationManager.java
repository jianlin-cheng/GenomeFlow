/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2009-06-26 10:56:39 -0500 (Fri, 26 Jun 2009) $
 * $Revision: 11127 $
 *
 * Copyright (C) 2003-2005  The Jmol Development Team
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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.viewer;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import org.jmol.constant.EnumAnimationMode;
import org.jmol.modelset.ModelSet;
import org.jmol.util.Escape;
import org.jmol.util.Logger;

class AnimationManager {

  protected Viewer viewer;
  
  AnimationManager(Viewer viewer) {
    this.viewer = viewer;
  }

  EnumAnimationMode animationReplayMode = EnumAnimationMode.ONCE;

  boolean animationOn;
  boolean animationPaused;
  boolean inMotion;
  
  int animationFps;  // set in stateManager
  int animationDirection = 1;
  int currentDirection = 1;
  int currentModelIndex;
  int firstModelIndex;
  int frameStep;
  int lastModelIndex;
   
  protected int firstFrameDelayMs;
  protected int lastFrameDelayMs;
  protected int lastModelPainted;
  
  private AnimationThread animationThread;
  private int backgroundModelIndex = -1;
  private final BitSet bsVisibleFrames = new BitSet();
  
  BitSet getVisibleFramesBitSet() {
    return bsVisibleFrames;
  }
  
  private float firstFrameDelay;
  private int intAnimThread;
  float lastFrameDelay = 1;

  void setCurrentModelIndex(int modelIndex) {
    setCurrentModelIndex(modelIndex, true);  
  }
  
  void setCurrentModelIndex(int modelIndex, boolean clearBackgroundModel) {
    if (modelIndex < 0)
      setAnimationOff(false);
    int formerModelIndex = currentModelIndex;
    ModelSet modelSet = viewer.getModelSet();
    int modelCount = (modelSet == null ? 0 : modelSet.getModelCount());
    if (modelCount == 1)
      currentModelIndex = modelIndex = 0;
    else if (modelIndex < 0 || modelIndex >= modelCount)
      modelIndex = -1;
    String ids = null;
    boolean isSameSource = false;
    if (currentModelIndex != modelIndex) {
      if (modelCount > 0) {
        boolean toDataFrame = viewer.isJmolDataFrame(modelIndex);
        boolean fromDataFrame = viewer.isJmolDataFrame(currentModelIndex);
        if (fromDataFrame)
          viewer.setJmolDataFrame(null, -1, currentModelIndex);
        if (currentModelIndex != -1)
          viewer.saveModelOrientation();
        if (fromDataFrame || toDataFrame) {
          ids = viewer.getJmolFrameType(modelIndex) 
          + " "  + modelIndex + " <-- " 
          + " " + currentModelIndex + " " 
          + viewer.getJmolFrameType(currentModelIndex);
          
          isSameSource = (viewer.getJmolDataSourceFrame(modelIndex) == viewer
              .getJmolDataSourceFrame(currentModelIndex));
        }
      }
      currentModelIndex = modelIndex;
      if (ids != null) {
        if (modelIndex >= 0)
          viewer.restoreModelOrientation(modelIndex);
        if (isSameSource && ids.indexOf("quaternion") >= 0 
            && ids.indexOf("plot") < 0
            && ids.indexOf("ramachandran") < 0
            && ids.indexOf(" property ") < 0) {
          viewer.restoreModelRotation(formerModelIndex);
        }
      }
    }
    viewer.setTrajectory(currentModelIndex);
    viewer.setFrameOffset(currentModelIndex);
    if (currentModelIndex == -1 && clearBackgroundModel)
      setBackgroundModelIndex(-1);  
    viewer.setTainted(true);
    setFrameRangeVisible();
    setStatusFrameChanged();
    if (modelSet != null) {
      if (!viewer.getSelectAllModels())
        viewer.setSelectionSubset(viewer.getModelUndeletedAtomsBitSet(currentModelIndex));
    }
  
  }

  void setBackgroundModelIndex(int modelIndex) {
    ModelSet modelSet = viewer.getModelSet();
    if (modelSet == null || modelIndex < 0 || modelIndex >= modelSet.getModelCount())
      modelIndex = -1;
    backgroundModelIndex = modelIndex;
    if (modelIndex >= 0)
      viewer.setTrajectory(modelIndex);
    viewer.setTainted(true);
    setFrameRangeVisible(); 
  }
  
  private void setStatusFrameChanged() {
    if (viewer.getModelSet() != null)
      viewer.setStatusFrameChanged(animationOn ? -2 - currentModelIndex
          : currentModelIndex);
  }
  
  private void setFrameRangeVisible() {
    bsVisibleFrames.clear();
    if (backgroundModelIndex >= 0)
      bsVisibleFrames.set(backgroundModelIndex);
    if (currentModelIndex >= 0) {
      bsVisibleFrames.set(currentModelIndex);
      return;
    }
    if (frameStep == 0)
      return;
    int nDisplayed = 0;
    int frameDisplayed = 0;
    for (int i = firstModelIndex; i != lastModelIndex; i += frameStep)
      if (!viewer.isJmolDataFrame(i)) {
        bsVisibleFrames.set(i);
        nDisplayed++;
        frameDisplayed = i;
      }
    if (firstModelIndex == lastModelIndex || !viewer.isJmolDataFrame(lastModelIndex)
        || nDisplayed == 0) {
      bsVisibleFrames.set(lastModelIndex);
      if (nDisplayed == 0)
        firstModelIndex = lastModelIndex;
      nDisplayed = 0;
    }
    if (nDisplayed == 1 && currentModelIndex < 0)
      setCurrentModelIndex(frameDisplayed);
  }

  void initializePointers(int frameStep) {
    firstModelIndex = 0;
    int modelCount = viewer.getModelCount();
    lastModelIndex = (frameStep == 0 ? 0 
        : modelCount) - 1;
    this.frameStep = frameStep;
    viewer.setFrameVariables();
  }

  void clear() {
    setAnimationOn(false);
    setCurrentModelIndex(0);
    currentDirection = 1;
    setAnimationDirection(1);
    setAnimationFps(10);
    setAnimationReplayMode(EnumAnimationMode.ONCE, 0, 0);
    initializePointers(0);
  }
  
  Map<String, Object> getAnimationInfo(){
    Map<String, Object> info = new Hashtable<String, Object>();
    info.put("firstModelIndex", Integer.valueOf(firstModelIndex));
    info.put("lastModelIndex", Integer.valueOf(lastModelIndex));
    info.put("animationDirection", Integer.valueOf(animationDirection));
    info.put("currentDirection", Integer.valueOf(currentDirection));
    info.put("displayModelIndex", Integer.valueOf(currentModelIndex));
    info.put("displayModelNumber", viewer.getModelNumberDotted(currentModelIndex));
    info.put("displayModelName", (currentModelIndex >=0 ? viewer.getModelName(currentModelIndex) : ""));
    info.put("animationFps", Integer.valueOf(animationFps));
    info.put("animationReplayMode", animationReplayMode.name());
    info.put("firstFrameDelay", new Float(firstFrameDelay));
    info.put("lastFrameDelay", new Float(lastFrameDelay));
    info.put("animationOn", Boolean.valueOf(animationOn));
    info.put("animationPaused", Boolean.valueOf(animationPaused));
    return info;
  }
 
  String getState(StringBuffer sfunc) {
    int modelCount = viewer.getModelCount();
    if (modelCount < 2)
      return "";
    StringBuffer commands = new StringBuffer();
    if (sfunc != null) {
      sfunc.append("  _setFrameState;\n");
      commands.append("function _setFrameState() {\n");
    }
    commands.append("# frame state;\n");
    
    commands.append("# modelCount ").append(modelCount)
        .append(";\n# first ").append(
             viewer.getModelNumberDotted(0)).append(";\n# last ").append(
             viewer.getModelNumberDotted(modelCount - 1)).append(";\n");
    if (backgroundModelIndex >= 0)
      StateManager.appendCmd(commands, "set backgroundModel " + 
          viewer.getModelNumberDotted(backgroundModelIndex));
    BitSet bs = viewer.getFrameOffsets();
    if (bs != null)
      StateManager.appendCmd(commands, "frame align " + Escape.escape(bs));
    StateManager.appendCmd(commands, 
        "frame RANGE " + viewer.getModelNumberDotted(firstModelIndex) + " "
            + viewer.getModelNumberDotted(lastModelIndex));
    StateManager.appendCmd(commands, 
        "animation DIRECTION " + (animationDirection == 1 ? "+1" : "-1"));
    StateManager.appendCmd(commands, "animation FPS " + animationFps);
    StateManager.appendCmd(commands, "animation MODE " + animationReplayMode.name()
        + " " + firstFrameDelay + " " + lastFrameDelay);
    StateManager.appendCmd(commands, "frame " + viewer.getModelNumberDotted(currentModelIndex));
    StateManager.appendCmd(commands, "animation "
            + (!animationOn ? "OFF" : currentDirection == 1 ? "PLAY"
                : "PLAYREV"));
    if (animationOn && animationPaused)
      StateManager.appendCmd(commands, "animation PAUSE");
    if (sfunc != null)
      commands.append("}\n\n");
    return commands.toString();
  }
  
  void setAnimationDirection(int animationDirection) {
    this.animationDirection = animationDirection;
    //if (animationReplayMode != ANIMATION_LOOP)
      //currentDirection = 1;
  }

  void setAnimationFps(int animationFps) {
    this.animationFps = animationFps;
  }

  // 0 = once
  // 1 = loop
  // 2 = palindrome
  
  void setAnimationReplayMode(EnumAnimationMode animationReplayMode,
                                     float firstFrameDelay,
                                     float lastFrameDelay) {
    this.firstFrameDelay = firstFrameDelay > 0 ? firstFrameDelay : 0;
    firstFrameDelayMs = (int)(this.firstFrameDelay * 1000);
    this.lastFrameDelay = lastFrameDelay > 0 ? lastFrameDelay : 0;
    lastFrameDelayMs = (int)(this.lastFrameDelay * 1000);
    this.animationReplayMode = animationReplayMode;
    viewer.setFrameVariables();
  }

  void setAnimationRange(int framePointer, int framePointer2) {
    int modelCount = viewer.getModelCount();
    if (framePointer < 0) framePointer = 0;
    if (framePointer2 < 0) framePointer2 = modelCount;
    if (framePointer >= modelCount) framePointer = modelCount - 1;
    if (framePointer2 >= modelCount) framePointer2 = modelCount - 1;
    firstModelIndex = framePointer;
    lastModelIndex = framePointer2;
    frameStep = (framePointer2 < framePointer ? -1 : 1);
    rewindAnimation();
  }

  private void animationOn(boolean TF) {
    animationOn = TF; 
    viewer.setBooleanProperty("_animating", TF);
  }
  
  void setAnimationOn(boolean animationOn) {
    if (!animationOn || !viewer.haveModelSet() || viewer.isHeadless()) {
      setAnimationOff(false);
      return;
    }
    if (!viewer.getSpinOn())
      viewer.refresh(3, "Viewer:setAnimationOn");
    setAnimationRange(-1, -1);
    resumeAnimation();
  }

  void setAnimationOff(boolean isPaused) {
    if (animationThread != null) {
      animationThread.interrupt();
      animationThread = null;
    }
    animationPaused = isPaused;
    if (!viewer.getSpinOn())
      viewer.refresh(3, "Viewer:setAnimationOff");
    animationOn(false);
    setStatusFrameChanged();
  }

  void pauseAnimation() {
    setAnimationOff(true);
  }
  
  void reverseAnimation() {
    currentDirection = -currentDirection;
    if (!animationOn)
      resumeAnimation();
  }
  
  void repaintDone() {
    lastModelPainted = currentModelIndex;
  }
  
  void resumeAnimation() {
    if(currentModelIndex < 0)
      setAnimationRange(firstModelIndex, lastModelIndex);
    if (viewer.getModelCount() <= 1) {
      animationOn(false);
      return;
    }
    animationOn(true);
    animationPaused = false;
    if (animationThread == null) {
      intAnimThread++;
      animationThread = new AnimationThread(firstModelIndex, lastModelIndex, intAnimThread);
      animationThread.start();
    }
  }
  
  boolean setAnimationNext() {
    return setAnimationRelative(animationDirection);
  }

  void setAnimationLast() {
    setCurrentModelIndex(animationDirection > 0 ? lastModelIndex : firstModelIndex);
  }

  void rewindAnimation() {
    setCurrentModelIndex(animationDirection > 0 ? firstModelIndex : lastModelIndex);
    currentDirection = 1;
    viewer.setFrameVariables();
  }
  
  boolean setAnimationPrevious() {
    return setAnimationRelative(-animationDirection);
  }

  boolean setAnimationRelative(int direction) {
    int frameStep = this.frameStep * direction * currentDirection;
    int modelIndexNext = currentModelIndex + frameStep;
    boolean isDone = (modelIndexNext > firstModelIndex
        && modelIndexNext > lastModelIndex || modelIndexNext < firstModelIndex
        && modelIndexNext < lastModelIndex);    
    if (isDone) {
      switch (animationReplayMode) {
      case ONCE:
        return false;
      case LOOP:
        modelIndexNext = (animationDirection == currentDirection ? firstModelIndex
            : lastModelIndex);
        break;
      case PALINDROME:
        currentDirection = -currentDirection;
        modelIndexNext -= 2 * frameStep;
      }
    }
    //Logger.debug("next="+modelIndexNext+" dir="+currentDirection+" isDone="+isDone);
    int nModels = viewer.getModelCount();
    if (modelIndexNext < 0 || modelIndexNext >= nModels)
      return false;
    setCurrentModelIndex(modelIndexNext);
    return true;
  }

  float getAnimRunTimeSeconds() {
    if (firstModelIndex == lastModelIndex
        || lastModelIndex < 0 || firstModelIndex < 0
        || lastModelIndex >= viewer.getModelCount()
        || firstModelIndex >= viewer.getModelCount())
      return  0;
    int i0 = Math.min(firstModelIndex, lastModelIndex);
    int i1 = Math.max(firstModelIndex, lastModelIndex);
    float nsec = 1f * (i1 - i0) / animationFps + firstFrameDelay
        + lastFrameDelay;
    for (int i = i0; i <= i1; i++)
      nsec += viewer.getFrameDelayMs(i) / 1000f;
    return nsec;
  }

  class AnimationThread extends Thread {
    final int framePointer;
    final int framePointer2;
    int intThread;

    AnimationThread(int framePointer, int framePointer2, int intAnimThread) {
      this.framePointer = framePointer;
      this.framePointer2 = framePointer2;
      this.setName("AnimationThread");
      intThread = intAnimThread;
    }

    @Override
    public void run() {
      long timeBegin = System.currentTimeMillis();
      int targetTime = 0;
      int sleepTime;
      //int holdTime = 0;
      if (Logger.debugging)
        Logger.debug("animation thread " + intThread + " running");
      viewer.requestRepaintAndWait();

      try {
        sleepTime = targetTime - (int) (System.currentTimeMillis() - timeBegin);
        if (sleepTime > 0)
          Thread.sleep(sleepTime);
        boolean isFirst = true;
        while (!isInterrupted() && animationOn) {
          if (currentModelIndex == framePointer) {
            targetTime += firstFrameDelayMs;
            sleepTime = targetTime
                - (int) (System.currentTimeMillis() - timeBegin);
            if (sleepTime > 0)
              Thread.sleep(sleepTime);
          }
          if (currentModelIndex == framePointer2) {
            targetTime += lastFrameDelayMs;
            sleepTime = targetTime
                - (int) (System.currentTimeMillis() - timeBegin);
            if (sleepTime > 0)
              Thread.sleep(sleepTime);
          }
          if (!isFirst && lastModelPainted == currentModelIndex && !setAnimationNext()) {
            Logger.debug("animation thread " + intThread + " exiting");
            setAnimationOff(false);
            return;
          }
          isFirst = false;
          targetTime += (int)((1000f / animationFps) + viewer.getFrameDelayMs(currentModelIndex));
          sleepTime = targetTime
              - (int) (System.currentTimeMillis() - timeBegin);

          while(!isInterrupted() && animationOn && !viewer.getRefreshing()) {
            Thread.sleep(10); 
          }
          if (!viewer.getSpinOn())
            viewer.refresh(1, "animationThread");
          sleepTime = targetTime
              - (int) (System.currentTimeMillis() - timeBegin);
          if (sleepTime > 0)
            Thread.sleep(sleepTime);
        }
      } catch (InterruptedException ie) {
        Logger.debug("animation thread interrupted!");
        try {
          setAnimationOn(false);
        } catch (Exception e) {
          // null pointer -- don't care;
        }
      }
    }
  }
 
}
