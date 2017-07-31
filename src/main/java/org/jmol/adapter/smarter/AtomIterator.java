/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-08-09 08:31:30 -0500 (Thu, 09 Aug 2012) $
 * $Revision: 17434 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
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

package org.jmol.adapter.smarter;

import java.util.BitSet;

import javax.vecmath.Point3f;

import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolAdapterAtomIterator;
import org.jmol.util.Quadric;

/* **************************************************************
 * the frame iterators
 * **************************************************************/
class AtomIterator extends JmolAdapterAtomIterator {
	private int iatom;
	private Atom atom;
	private int atomCount;
	private Atom[] atoms;
	private BitSet bsAtoms;

	AtomIterator(AtomSetCollection atomSetCollection) {
		atomCount = atomSetCollection.getAtomCount();
		atoms = atomSetCollection.getAtoms();
		bsAtoms = atomSetCollection.bsAtoms;
		iatom = 0;
	}

	@Override
	public boolean hasNext() {
		if (iatom == atomCount)
			return false;
		while ((atom = atoms[iatom++]) == null
				|| (bsAtoms != null && !bsAtoms.get(atom.atomIndex)))
			if (iatom == atomCount)
				return false;
		atoms[iatom - 1] = null; // single pass
		return true;
	}

	@Override
	public int getAtomSetIndex() {
		return atom.atomSetIndex;
	}

	@Override
	public BitSet getAtomSymmetry() {
		return atom.bsSymmetry;
	}

	@Override
	public int getAtomSite() {
		return atom.atomSite + 1;
	}

	@Override
	public Object getUniqueID() {
		return Integer.valueOf(atom.atomIndex);
	}

	@Override
	public short getElementNumber() {
		return (atom.elementNumber > 0 ? atom.elementNumber : JmolAdapter
				.getElementNumber(atom.getElementSymbol()));
	}

	@Override
	public String getAtomName() {
		return atom.atomName;
	}

	@Override
	public int getFormalCharge() {
		return atom.formalCharge;
	}

	@Override
	public float getPartialCharge() {
		return atom.partialCharge;
	}

	@Override
	public Quadric[] getEllipsoid() {
		return atom.ellipsoid;
	}

	@Override
	public float getRadius() {
		return atom.radius;
	}

	@Override
	public float getX() {
		return atom.x;
	}

	@Override
	public float getY() {
		return atom.y;
	}

	@Override
	public float getZ() {
		return atom.z;
	}

	@Override
	public float getVectorX() {
		return atom.vectorX;
	}

	@Override
	public float getVectorY() {
		return atom.vectorY;
	}

	@Override
	public float getVectorZ() {
		return atom.vectorZ;
	}

	@Override
	public float getBfactor() {
		return Float.isNaN(atom.bfactor) && atom.anisoBorU != null ? atom.anisoBorU[7] * 100f
				: atom.bfactor;
	}

	@Override
	public int getOccupancy() {
		return atom.occupancy;
	}

	@Override
	public boolean getIsHetero() {
		return atom.isHetero;
	}

	@Override
	public int getAtomSerial() {
		return atom.atomSerial;
	}

	@Override
	public char getChainID() {
		return JmolAdapter.canonizeChainID(atom.chainID);
	}

	@Override
	public char getAlternateLocationID() {
		return JmolAdapter.canonizeAlternateLocationID(atom.alternateLocationID);
	}

	@Override
	public String getGroup3() {
		return atom.group3;
	}
//added -hcf
	@Override
	public int getChrScaleNumber() {
		return atom.chrScaleNumber;
	}
	
	@Override
	public int getLociScaleNumber() {
		return atom.lociScaleNumber;
	}

	@Override
	public int getFiberScaleNumber() {
		return atom.fiberScaleNumber;
	}
	
	@Override
	public int getNucleoScaleNumber() {
		return atom.nucleoScaleNumber;
	}
	
	@Override
	public int getChrID() {
		return atom.chrID;
	}
	
	@Override
	public int getFromPos() {
		return atom.fromPos;
	}
	@Override
	public int getEndPos() {
		return atom.endPos;
	}
	@Override
	public String getSpName() {
		return atom.spName;
	}
	@Override
	public String getEnsChr() {
		return atom.ensChr;
	}
	@Override 
	public String getLcChr() {
		return atom.lcChr;
	}
	
//added end -hcf
	@Override
	public int getSequenceNumber() {
		return atom.sequenceNumber;
	}
	@Override
	public char getInsertionCode() {
		return JmolAdapter.canonizeInsertionCode(atom.insertionCode);
	}

	@Override
	public Point3f getXYZ() {
		return atom;
	}

}
