/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-11 19:29:26 -0500 (Tue, 11 Sep 2012) $
 * $Revision: 17556 $
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

package org.jmol.shape;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import org.jmol.constant.EnumPalette;
import org.jmol.modelset.Atom;
import org.jmol.modelset.LabelToken;
import org.jmol.util.ArrayUtil;
import org.jmol.util.BitSetUtil;
import org.jmol.util.Colix;
import org.jmol.util.Escape;
import org.jmol.util.JmolFont;
import org.jmol.viewer.ActionManager;
import org.jmol.viewer.JmolConstants;

public class Labels extends AtomShape {

  public String[] strings;
  public String[] formats;
  public short[] bgcolixes;
  public byte[] fids;
  public int[] offsets;

  private Map<Integer, Text> atomLabels = new Hashtable<Integer, Text>();
  private Text text;

  private Map<Integer, float[]> labelBoxes;

  private BitSet bsFontSet, bsBgColixSet;

  private int defaultOffset;
  private int defaultAlignment;
  private int defaultZPos;
  private byte defaultFontId;
  private short defaultColix;
  private short defaultBgcolix;
  private byte defaultPaletteID;
  private int defaultPointer;
  private static int zeroOffset = (JmolConstants.LABEL_DEFAULT_X_OFFSET << 8)
      | JmolConstants.LABEL_DEFAULT_Y_OFFSET;

  public byte zeroFontId;

  private boolean defaultsOnlyForNone = true;
  private boolean setDefaults = false;
  
  //labels

  @Override
  public void initShape() {
    super.initShape();
    defaultFontId = zeroFontId = gdata.getFont3D(JmolConstants.DEFAULT_FONTFACE,
        JmolConstants.DEFAULT_FONTSTYLE, JmolConstants.LABEL_DEFAULT_FONTSIZE).fid;
    defaultColix = 0; //"none" -- inherit from atom
    defaultBgcolix = 0; //"none" -- off
    defaultOffset = zeroOffset;
    defaultZPos = 0;
    translucentAllowed = false;
  }

  @Override
  public void setProperty(String propertyName, Object value, BitSet bsSelected) {
    isActive = true;
    
    //System.out.println(propertyName + " Labels " + value);

    if ("setDefaults" == propertyName) {
      setDefaults = ((Boolean) value).booleanValue();
      return;
    }

    if ("color" == propertyName) {
      isActive = true;
      byte pid = EnumPalette.pidOf(value);
      short colix = Colix.getColix(value);
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setColix(i, colix, pid);
      if (setDefaults || !defaultsOnlyForNone) {
        defaultColix = colix;
        defaultPaletteID = pid;
      }
      return;
    }

    if ("scalereference" == propertyName) {
      if (strings == null)
        return;
      float val = ((Float) value).floatValue();
      float scalePixelsPerMicron = (val == 0 ? 0 : 10000f / val);
      for (int i = atomCount; --i >= 0;)
        if (bsSelected.get(i)) {
          if (strings.length <= i)
            continue;
          text = getLabel(i);
          if (text == null) {
            text = new Text(gdata, null, strings[i], (short) 0, (short) 0, 0, 0,
                0, 0, 0, scalePixelsPerMicron);
            putLabel(i, text);
          } else {
            text.setScalePixelsPerMicron(scalePixelsPerMicron);
          }
        }
      return;
    }

    if ("label" == propertyName) {
      setScaling();
      String strLabel = (String) value;
      LabelToken[][] tokens = (strLabel == null || strLabel.length() == 0 ? nullToken : new LabelToken[][] { null });
      for (int i = atomCount; --i >= 0;)
        if (bsSelected.get(i)) 
          setLabel(tokens, strLabel, i);
      return;
    }

    if ("clearBoxes" == propertyName) {
      labelBoxes = null;
      return;
    }
    
    if ("translucency" == propertyName || "bgtranslucency" == propertyName) {
      // no translucency
      return;
    }

    if ("bgcolor" == propertyName) {
      isActive = true;
      if (bsBgColixSet == null)
        bsBgColixSet = new BitSet();
      short bgcolix = Colix.getColix(value);
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setBgcolix(i, bgcolix);
      if (setDefaults || !defaultsOnlyForNone)
        defaultBgcolix = bgcolix;
      return;
    }

    // the rest require bsFontSet setting

    if (bsFontSet == null)
      bsFontSet = new BitSet();

    if ("fontsize" == propertyName) {
      int fontsize = ((Integer) value).intValue();
      if (fontsize < 0) {
        fids = null;
        return;
      }
      byte fid = gdata.getFontFid(fontsize);
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setFont(i, fid);
      if (setDefaults || !defaultsOnlyForNone)
        defaultFontId = fid;
      return;
    }

    if ("font" == propertyName) {
      byte fid = ((JmolFont) value).fid;
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setFont(i, fid);
      if (setDefaults || !defaultsOnlyForNone)
        defaultFontId = fid;
      return;
    }

    if ("offset" == propertyName || "offsetexact" == propertyName) {
      int offset = ((Integer) value).intValue();
      // 0 must be the default, because we initialize the array
      // in segments and so there will be extra 0s.
      // but this "0" only means that "zero" offset; you 
      // can change the default to anything you want.
      boolean isExact = (propertyName == "offsetexact");
      if (offset == 0)
        offset = Short.MAX_VALUE;
      else if (offset == zeroOffset)
        offset = 0;
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setOffsets(i, offset, isExact);
      if (setDefaults || !defaultsOnlyForNone)
        defaultOffset = offset;
      return;
    }

    if ("align" == propertyName) {
      String type = (String) value;
      int alignment = Object2d.ALIGN_LEFT;
      if (type.equalsIgnoreCase("right"))
        alignment = Object2d.ALIGN_RIGHT;
      else if (type.equalsIgnoreCase("center"))
        alignment = Object2d.ALIGN_CENTER;
      for (int i = atomCount; --i >= 0;)
        if (bsSelected.get(i))
          setAlignment(i, alignment);
      if (setDefaults || !defaultsOnlyForNone)
        defaultAlignment = alignment;
      return;
    }

    if ("pointer" == propertyName) {
      int pointer = ((Integer) value).intValue();
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setPointer(i, pointer);
      if (setDefaults || !defaultsOnlyForNone)
        defaultPointer = pointer;
      return;
    }

    if ("front" == propertyName) {
      boolean TF = ((Boolean) value).booleanValue();
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setFront(i, TF);
      if (setDefaults || !defaultsOnlyForNone)
        defaultZPos = (TF ? FRONT_FLAG : 0);
      return;
    }

    if ("group" == propertyName) {
      boolean TF = ((Boolean) value).booleanValue();
      if (!setDefaults)
        for (int i = atomCount; --i >= 0;)
          if (bsSelected.get(i))
            setGroup(i, TF);
      if (setDefaults || !defaultsOnlyForNone)
        defaultZPos = (TF ? GROUP_FLAG : 0);
      return;
    }

    if ("display" == propertyName || "toggleLabel" == propertyName) {
      // toggle
      int mode = ("toggleLabel" == propertyName ? 0 
          : ((Boolean) value).booleanValue() ? 1 : -1);
      if (mads == null)
        mads = new short[atomCount];
      String strLabelPDB = null;
      LabelToken[] tokensPDB = null;
      String strLabelUNK = null;
      LabelToken[] tokensUNK = null;
      String strLabel;
      LabelToken[] tokens;
      for (int atomIndex = atomCount; --atomIndex >= 0;) {
        if (bsSelected.get(atomIndex)) {
          Atom atom = atoms[atomIndex];
          if (formats == null || atomIndex >= formats.length)
            formats = ArrayUtil.ensureLength(formats, atomIndex + 1);
          if (strings != null && strings.length > atomIndex
              && strings[atomIndex] != null) {
            mads[atomIndex] = (short) (mode == 0 && mads[atomIndex] < 0  
                || mode == 1 ? 1 : -1);
          } else {
            if (bsSizeSet == null)
              bsSizeSet = new BitSet();
            strings = ArrayUtil.ensureLength(strings, atomIndex + 1);
            if (atom.getGroup3(false).equals("UNK")) {
              if (strLabelUNK == null) {
                strLabelUNK = viewer.getStandardLabelFormat(1);
                tokensUNK = LabelToken.compile(viewer, strLabelUNK, '\0', null);
              }              
              strLabel = strLabelUNK;
              tokens = tokensUNK;
            } else {
              if (strLabelPDB == null) {
                strLabelPDB = viewer.getStandardLabelFormat(2);
                tokensPDB = LabelToken.compile(viewer, strLabelPDB, '\0', null);
              }
              strLabel = strLabelPDB;
              tokens = tokensPDB;
            }
            strings[atomIndex] = LabelToken.formatLabel(viewer, atom, tokens, '\0', null);
            formats[atomIndex] = strLabel;
            bsSizeSet.set(atomIndex);
            if ((bsBgColixSet == null || !bsBgColixSet.get(atomIndex))
                && defaultBgcolix != 0)
              setBgcolix(atomIndex, defaultBgcolix);
            mads[atomIndex] = (short) (mode >= 0 ? 1 : -1);
          }
          atom.setShapeVisibility(myVisibilityFlag, strings != null
              && atomIndex < strings.length && strings[atomIndex] != null
              && mads[atomIndex] >= 0);
          //        } else if (strings != null && atomIndex < strings.length) {
          //        strings[atomIndex] = null;          
        }
      }
      return;
    }

    if (propertyName.startsWith("label:")) {
      setScaling();
      setLabel(new LabelToken[1][], propertyName.substring(6), ((Integer)value).intValue());
      return;
    }

    if (propertyName == "deleteModelAtoms") {
      labelBoxes = null;
      int firstAtomDeleted = ((int[]) ((Object[]) value)[2])[1];
      int nAtomsDeleted = ((int[]) ((Object[]) value)[2])[2];
      fids = (byte[]) ArrayUtil.deleteElements(fids, firstAtomDeleted,
          nAtomsDeleted);
      bgcolixes = (short[]) ArrayUtil.deleteElements(bgcolixes,
          firstAtomDeleted, nAtomsDeleted);
      offsets = (int[]) ArrayUtil.deleteElements(offsets, firstAtomDeleted,
          nAtomsDeleted);
      formats = (String[]) ArrayUtil.deleteElements(formats, firstAtomDeleted,
          nAtomsDeleted);
      strings = (String[]) ArrayUtil.deleteElements(strings, firstAtomDeleted,
          nAtomsDeleted);
      BitSetUtil.deleteBits(bsFontSet, bsSelected);
      BitSetUtil.deleteBits(bsBgColixSet, bsSelected);
      // pass to super
    }
    
    super.setProperty(propertyName, value, bsSelected);

  }

  private final static LabelToken[][] nullToken = new LabelToken[][] { null };
  private boolean isScaled;
  private float scalePixelsPerMicron;
  
  private void setScaling() {
    isActive = true;
    if (bsSizeSet == null)
      bsSizeSet = new BitSet();
    isScaled = viewer.getFontScaling();
    scalePixelsPerMicron = (isScaled ? viewer
        .getScalePixelsPerAngstrom(false) * 10000f : 0);
  }
  
  private void setLabel(LabelToken[][] temp, String strLabel, int i) {
    Atom atom = atoms[i];
    LabelToken[] tokens = temp[0];
    if (tokens == null)
      tokens = temp[0] = LabelToken.compile(viewer, strLabel, '\0', null);
    String label = (tokens == null ? null : LabelToken.formatLabel(viewer,
        atom, tokens, '\0', null));
    atom.setShapeVisibility(myVisibilityFlag, label != null);
    if (strings == null || i >= strings.length)
      strings = ArrayUtil.ensureLength(strings, i + 1);
    if (formats == null || i >= formats.length)
      formats = ArrayUtil.ensureLength(formats, i + 1);
    strings[i] = label;
    if (i ==10) {
    	System.out.println("xx");
    }
    formats[i] = (strLabel != null && strLabel.indexOf("%{") >= 0 ? label
        : strLabel);
    bsSizeSet.set(i, (strLabel != null));
    text = getLabel(i);
    if (isScaled) {
      text = new Text(gdata, null, label, (short) 0, (short) 0, 0, 0, 0, 0, 0,
          scalePixelsPerMicron);
      putLabel(i, text);
    } else if (text != null) {
      text.setText(label);
    }
    if (defaultOffset != zeroOffset)
      setOffsets(i, defaultOffset, false);
    if (defaultAlignment != Object2d.ALIGN_LEFT)
      setAlignment(i, defaultAlignment);
    if ((defaultZPos & FRONT_FLAG) != 0)
      setFront(i, true);
    else if ((defaultZPos & GROUP_FLAG) != 0)
      setGroup(i, true);
    if (defaultPointer != Object2d.POINTER_NONE)
      setPointer(i, defaultPointer);
    if (defaultColix != 0 || defaultPaletteID != 0)
      setColix(i, defaultColix, defaultPaletteID);
    if (defaultBgcolix != 0)
      setBgcolix(i, defaultBgcolix);
    if (defaultFontId != zeroFontId)
      setFont(i, defaultFontId);
  }

  @Override
  public Object getProperty(String property, int index) {
    if (property.equals("offsets"))
      return offsets;
    if (property.equals("defaultState"))
      return getDefaultState();
    if (property.equals("label"))
      return (strings != null && index < strings.length && strings[index] != null 
          ? strings[index] : "");
    return null;
  }

  public void putLabel(int i, Text text) {
    if (text == null)
      atomLabels.remove(Integer.valueOf(i));
    else
      atomLabels.put(Integer.valueOf(i), text);
  }

  public Text getLabel(int i) {
    return atomLabels.get(Integer.valueOf(i));
  }

  public void putBox(int i, float[] boxXY) {
    if (labelBoxes == null)
      labelBoxes = new Hashtable<Integer, float[]>(); 
    labelBoxes.put(Integer.valueOf(i), boxXY);
  }

  public float[] getBox(int i) {
    if (labelBoxes == null)
      return null;
    return labelBoxes.get(Integer.valueOf(i));
  }
  
  private void setColix(int i, short colix, byte pid) {
    setColixAndPalette(colix, pid, i);
    // text is only created by labelsRenderer
    if (colixes != null && ((text = getLabel(i)) != null))
      text.setColix(colixes[i]);
  }

  private void setBgcolix(int i, short bgcolix) {
    if (bgcolixes == null || i >= bgcolixes.length) {
      if (bgcolix == 0)
        return;
      bgcolixes = ArrayUtil.ensureLength(bgcolixes, i + 1);
    }
    bgcolixes[i] = bgcolix;
    bsBgColixSet.set(i, bgcolix != 0);
    text = getLabel(i);
    if (text != null)
      text.setBgColix(bgcolix);
  }

  public final static int POINTER_FLAGS = 0x03;
  public final static int ALIGN_FLAGS   = 0x0C;
  public final static int ZPOS_FLAGS    = 0x30;
  public final static int GROUP_FLAG    = 0x10;
  public final static int FRONT_FLAG    = 0x20;
  public final static int SCALE_FLAG    = 0x40;
  public final static int EXACT_OFFSET_FLAG = 0x80;
  public final static int FLAGS         = 0xFF;
  public final static int FLAG_OFFSET   = 8;

  private void setOffsets(int i, int offset, boolean isExact) {
    //entry is just xxxxxxxxyyyyyyyy
    //  3         2         1        
    // 10987654321098765432109876543210
    //         xxxxxxxxyyyyyyyytsfgaabp
    //          x-align y-align||||| ||_pointer on
    //                         ||||| |_background pointer color
    //                         |||||_text alignment 0xC 
    //                         ||||_labels group 0x10
    //                         |||_labels front  0x20
    //                         ||_scaled
    //                         |_exact offset
    if (offsets == null || i >= offsets.length) {
      if (offset == 0)
        return;
      offsets = ArrayUtil.ensureLength(offsets, i + 1);
    }
    offsets[i] = (offsets[i] & FLAGS) | (offset << FLAG_OFFSET);
    if (isExact)
      offsets[i] |= EXACT_OFFSET_FLAG;
    text = getLabel(i);
    if (text != null)
      text.setOffset(offset);
  }

  private void setAlignment(int i, int alignment) {
    if (offsets == null || i >= offsets.length) {
      if (alignment == Object2d.ALIGN_LEFT)
        return;
      offsets = ArrayUtil.ensureLength(offsets, i + 1);
    }
    offsets[i] = (offsets[i] & ~ALIGN_FLAGS) | (alignment << 2);
    text = getLabel(i);
    if (text != null)
      text.setAlignment(alignment);
  }

  public static int getAlignment(int offsetFull) {
    return (offsetFull & ALIGN_FLAGS) >> 2;
  }
  
  private void setPointer(int i, int pointer) {
    if (offsets == null || i >= offsets.length) {
      if (pointer == Object2d.POINTER_NONE)
        return;
      offsets = ArrayUtil.ensureLength(offsets, i + 1);
    }
    offsets[i] = (offsets[i] & ~POINTER_FLAGS) + pointer;
    text = getLabel(i);
    if (text != null)
      text.setPointer(pointer);
  }

  private void setFront(int i, boolean TF) {
    if (offsets == null || i >= offsets.length) {
      if (!TF)
        return;
      offsets = ArrayUtil.ensureLength(offsets, i + 1);
    }
    offsets[i] = (offsets[i] & ~ZPOS_FLAGS) + (TF ? FRONT_FLAG : 0);
  }

  private void setGroup(int i, boolean TF) {
    if (offsets == null || i >= offsets.length) {
      if (!TF)
        return;
      offsets = ArrayUtil.ensureLength(offsets, i + 1);
    }
    offsets[i] = (offsets[i] & ~ZPOS_FLAGS) + (TF ? GROUP_FLAG : 0);
  }

  private void setFont(int i, byte fid) {
    if (fids == null || i >= fids.length) {
      if (fid == zeroFontId)
        return;
      fids = ArrayUtil.ensureLength(fids, i + 1);
    }
    fids[i] = fid;
    bsFontSet.set(i);
    text = getLabel(i);
    if (text != null) {
      text.setFid(fid);
    }
  }

  @Override
  public void setModelClickability() {
    if (strings == null)
      return;
    for (int i = strings.length; --i >= 0;) {
      String label = strings[i];
      if (label != null && modelSet.atoms.length > i
          && !modelSet.isAtomHidden(i))
        modelSet.atoms[i].setClickable(myVisibilityFlag);
    }
  }

  private String getDefaultState() {
    StringBuffer s = new StringBuffer("\n# label defaults;\n");
    appendCmd(s, "select none");
    appendCmd(s, getColorCommand("label", defaultPaletteID, defaultColix));
    appendCmd(s, "background label " + encodeColor(defaultBgcolix));
    appendCmd(s, "set labelOffset " + Object2d.getXOffset(defaultOffset) + " "
        + (-Object2d.getYOffset(defaultOffset)));
    String align = Object2d.getAlignment(defaultAlignment);
    appendCmd(s, "set labelAlignment " + (align.length() < 5 ? "left" : align));
    String pointer = Object2d.getPointer(defaultPointer);
    appendCmd(s, "set labelPointer " + (pointer.length() == 0 ? "off" : pointer));
    if ((defaultZPos & FRONT_FLAG) != 0)
      appendCmd(s, "set labelFront");
    else if ((defaultZPos & GROUP_FLAG) != 0)
      appendCmd(s, "set labelGroup");
    appendCmd(s, getFontCommand("label", JmolFont.getFont3D(defaultFontId)));
    return s.toString();
  }

  @Override
  public String getShapeState() {
    if (!isActive || bsSizeSet == null)
      return "";
    Map<String, BitSet> temp = new Hashtable<String, BitSet>();
    Map<String, BitSet> temp2 = new Hashtable<String, BitSet>();
    Map<String, BitSet> temp3 = new Hashtable<String, BitSet>();
    for (int i = bsSizeSet.nextSetBit(0); i >= 0; i = bsSizeSet
        .nextSetBit(i + 1)) {
      setStateInfo(temp, i, "label " + Escape.escapeStr(formats[i]));
      if (bsColixSet != null && bsColixSet.get(i))
        setStateInfo(temp2, i, getColorCommand("label", paletteIDs[i],
            colixes[i]));
      if (bsBgColixSet != null && bsBgColixSet.get(i))
        setStateInfo(temp2, i, "background label " + encodeColor(bgcolixes[i]));
      Text text = getLabel(i);
      float sppm = (text != null ? text.getScalePixelsPerMicron() : 0);
      if (sppm > 0)
        setStateInfo(temp2, i, "set labelScaleReference " + (10000f / sppm));
      if (offsets != null && offsets.length > i) {
        int offsetFull = offsets[i];
        setStateInfo(
            temp2,
            i,
            "set "
                + ((offsetFull & EXACT_OFFSET_FLAG) == EXACT_OFFSET_FLAG ? "labelOffsetExact "
                    : "labelOffset ")
                + Object2d.getXOffset(offsetFull >> FLAG_OFFSET) + " "
                + (-Object2d.getYOffset(offsetFull >> FLAG_OFFSET)));
        String align = Object2d.getAlignment(offsetFull >> 2);
        String pointer = Object2d.getPointer(offsetFull);
        if (pointer.length() > 0)
          setStateInfo(temp2, i, "set labelPointer " + pointer);
        if ((offsetFull & FRONT_FLAG) != 0)
          setStateInfo(temp2, i, "set labelFront");
        else if ((offsetFull & GROUP_FLAG) != 0)
          setStateInfo(temp2, i, "set labelGroup");
        // labelAlignment must come last, so we put it in a separate hash
        // table
        if (align.length() > 0)
          setStateInfo(temp3, i, "set labelAlignment " + align);
      }
      if (mads != null && mads[i] < 0)
        setStateInfo(temp2, i, "set toggleLabel");
      if (bsFontSet != null && bsFontSet.get(i))
        setStateInfo(temp2, i, getFontCommand("label", JmolFont
            .getFont3D(fids[i])));
    }
    return getShapeCommands(temp, temp2)
        + getShapeCommands(null, temp3);
  }

  private int pickedAtom = -1;
  private int pickedOffset = 0;
  private int pickedX;
  private int pickedY;
  
  @Override
  public synchronized boolean checkObjectDragged(int prevX, int prevY, int x,
                                                 int y, int modifiers,
                                                 BitSet bsVisible) {
    if (viewer.getPickingMode() != ActionManager.PICKING_LABEL || labelBoxes == null)
      return false;
    // mouse down ?
    if (prevX == Integer.MIN_VALUE) {
      int iAtom = findNearestLabel(x, y);
      if (iAtom >= 0) {
        pickedAtom = iAtom;
        pickedX = x;
        pickedY = y;
        pickedOffset = (offsets == null 
            || pickedAtom >= offsets.length ? 0 
                : offsets[pickedAtom]) >> FLAG_OFFSET;
        return true;
      }
      return false;
    }
    // mouse up ?
    if (prevX == Integer.MAX_VALUE) {
      pickedAtom = -1;
      return false;
    }
    if (pickedAtom < 0)
      return false;
    move2D(pickedAtom, x, y);
    return true;
  }
                         
  private int findNearestLabel(int x, int y) {
    if (labelBoxes == null)
      return -1;
    float dmin = Float.MAX_VALUE;
    int imin = -1;
    float zmin = Float.MAX_VALUE;
    for (Map.Entry<Integer, float[]> entry : labelBoxes.entrySet()) {
      if (!atoms[entry.getKey().intValue()].isVisible(myVisibilityFlag))
        continue;
      float[] boxXY = entry.getValue();
      float dx = x - boxXY[0];
      float dy = y - boxXY[1];
      if (dx <= 0 || dy <= 0 || dx >= boxXY[2] || dy >= boxXY[3] || boxXY[4] > zmin)
        continue;
      zmin = boxXY[4];
      float d = Math.min(Math.abs(dx - boxXY[2]/2), Math.abs(dy - boxXY[3]/2));
      if (d <= dmin) {
        dmin = d;
        imin = entry.getKey().intValue();
      }
    }
    return imin;
  }

  private void move2D(int pickedAtom, int x, int y) {
    int xOffset = Object2d.getXOffset(pickedOffset);
    int yOffset = -Object2d.getYOffset(pickedOffset);
    xOffset += x - pickedX;
    yOffset += pickedY - y;
    int offset = Object2d.getOffset(xOffset, yOffset);
    if (offset == 0)
      offset = Short.MAX_VALUE;
    else if (offset == zeroOffset)
      offset = 0;
    setOffsets(pickedAtom, offset, true);
  }
  
}
