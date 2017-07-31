/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2006-09-12 00:46:22 -0500 (Tue, 12 Sep 2006) $
 * $Revision: 5501 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
 * Copyright (C) 2005  Peter Knowles
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
package org.jmol.adapter.readers.xml;

import java.util.Map;

import org.xml.sax.Attributes;

/**
 * A Molpro 2005 reader
 */

public class XmlMolproReader extends XmlCmlReader {

  XmlMolproReader() {  
  }
  
  @Override
  protected String[] getImplementedAttributes() {
    return new String[] { "id", "length", "type", //general
        "x3", "y3", "z3", "elementType", //atoms
        "name", //variable
        "groups", "cartesianLength", "primitives", // basisSet and
        "minL", "maxL", "angular", "contractions", //   basisGroup
        "occupation", "energy", "symmetryID", // orbital 
        "wavenumber", "units", // normalCoordinate
    };
  }

  public void processStartElement2(String localName, Map<String, String> atts) {
    if (localName.equals("normalCoordinate")) {
      keepChars = false;
      if (!parent.doGetVibration(++vibrationNumber))
        return;
      try {
        atomSetCollection.cloneLastAtomSet();
      } catch (Exception e) {
        e.printStackTrace();
        atomSetCollection.errorMessage = "Error processing normalCoordinate: " + e.getMessage();
        vibrationNumber = 0;
        return;
      }
      if (atts.containsKey("wavenumber")) {
        String wavenumber = atts.get("wavenumber");
        String units = "cm^-1";
        if (atts.containsKey("units")) {
          units = atts.get("units");
          if (units.startsWith("inverseCent"))
            units = "cm^-1";
        }
        atomSetCollection.setAtomSetFrequency(null, null, wavenumber, units);
        keepChars = true;
      }
      return;
    }

    if (localName.equals("vibrations")) {
      vibrationNumber = 0;
      return;
    }
  }

  public void processEndElement2(String localName) {
    if (localName.equals("normalCoordinate")) {
      if (!keepChars)
        return;
      int atomCount = atomSetCollection.getLastAtomSetAtomCount();
      int baseAtomIndex = atomSetCollection.getLastAtomSetAtomIndex();
      tokens = getTokens(chars);
      for (int offset = tokens.length - atomCount * 3, i = 0; i < atomCount; i++) {
        atomSetCollection.addVibrationVector(i + baseAtomIndex,
            parseFloat(tokens[offset++]),
            parseFloat(tokens[offset++]),
            parseFloat(tokens[offset++])
        );
      }
    }
  }

  @Override
  protected JmolXmlHandler getHandler(Object xmlReader) {
    return new MolproHandler(xmlReader);
  }

  class MolproHandler extends JmolXmlHandler {
 
    public MolproHandler(Object xmlReader) {
      super(xmlReader);
    }

    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attributes) {
      super.startElement(namespaceURI, localName, qName, attributes);
      processStartElement2(localName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      processEndElement2(localName);
      super.endElement(uri, localName, qName);
    }
  }
}
