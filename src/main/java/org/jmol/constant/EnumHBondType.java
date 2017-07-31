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

package org.jmol.constant;

import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;
import org.jmol.modelset.Group;
import org.jmol.viewer.JmolConstants;

/**
 * Enum for hydrogen bonding donor/acceptor type
 */
public enum EnumHBondType {
  NOT, ACCEPTOR, DONOR, UNKNOWN;

  public static EnumHBondType getType(Atom atom) {
    Group group = atom.getGroup();
    int groupID = group.getGroupID();
    boolean considerHydrogens = !atom.isHetero();
    switch (atom.getElementNumber()) {
    default:
      return NOT;
    case 1:
      if (atom.getCovalentBondCount() == 0)
        return DONOR;
      Bond[] bonds = atom.getBonds();
      if (bonds == null)
        return NOT;
      switch (bonds[0].getOtherAtom(atom).getElementNumber()) {
      case 7:
      case 8:
      case 16:
        return DONOR;
      }
      return NOT;
    case 7:
      if (atom == group.getNitrogenAtom())
        return DONOR;
      if (groupID == JmolConstants.GROUPID_HISTIDINE)
        return UNKNOWN;
      if (atom.getCovalentHydrogenCount() > 0)
        return DONOR;
      if (considerHydrogens)
        return ACCEPTOR;
      switch (groupID) {
      case JmolConstants.GROUPID_ARGININE:
      case JmolConstants.GROUPID_ASPARAGINE:
      case JmolConstants.GROUPID_LYSINE:
      case JmolConstants.GROUPID_GLUTAMINE:
      case JmolConstants.GROUPID_TRYPTOPHAN:
        return DONOR;
      }
      return UNKNOWN;
    case 8:
      if (atom == group.getCarbonylOxygenAtom() || atom.getFormalCharge() == -1)
        return ACCEPTOR;
      if (atom.getCovalentBondCount() == 0 || atom.getCovalentHydrogenCount() > 0)
        return UNKNOWN;
      if (considerHydrogens)
        return ACCEPTOR;       
      switch (groupID) {
      case JmolConstants.GROUPID_ASPARTATE:
      case JmolConstants.GROUPID_GLUTAMATE:
        return ACCEPTOR;
      }
      return UNKNOWN;
    }
  }

  public static boolean isPossibleHBond(EnumHBondType typeA, EnumHBondType typeB) {
    return (typeA == NOT || typeB == NOT ? false
        : typeA == UNKNOWN || typeA != typeB);
  }
  
}
