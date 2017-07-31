/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-06-10 13:54:48 -0500 (Sun, 10 Jun 2012) $
 * $Revision: 17269 $
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
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.api;

import java.util.BitSet;

import javax.vecmath.Point3f;

import org.jmol.util.Quadric;

public abstract class JmolAdapterAtomIterator {
	public abstract boolean hasNext();

	public int getAtomSetIndex() {
		return 0;
	}

	public BitSet getAtomSymmetry() {
		return null;
	}

	public int getAtomSite() {
		return Integer.MIN_VALUE;
	}

	abstract public Object getUniqueID();

	public short getElementNumber() {
		return -1;
	} // may be atomicNumber + isotopeNumber*128

	public String getAtomName() {
		return null;
	}

	public int getFormalCharge() {
		return 0;
	}

	public float getPartialCharge() {
		return Float.NaN;
	}

	public Quadric[] getEllipsoid() {
		return null;
	}

	public float getRadius() {
		return Float.NaN;
	}

	abstract public float getX();

	abstract public float getY();

	abstract public float getZ();

	abstract public Point3f getXYZ();

	public float getVectorX() {
		return Float.NaN;
	}

	public float getVectorY() {
		return Float.NaN;
	}

	public float getVectorZ() {
		return Float.NaN;
	}

	public float getBfactor() {
		return Float.NaN;
	}

	public int getOccupancy() {
		return 100;
	}

	public boolean getIsHetero() {
		return false;
	}

	public int getAtomSerial() {
		return Integer.MIN_VALUE;
	}

	public char getChainID() {
		return '\0';
	}

	public char getAlternateLocationID() {
		return '\0';
	}

	public String getGroup3() {
		return null;
	}

	public int getSequenceNumber() {
		return Integer.MIN_VALUE;
	}
	//added -hcf
	public int getChrScaleNumber() {
		return Integer.MIN_VALUE;
	}
	
	public int getLociScaleNumber() {
		return Integer.MIN_VALUE;
	}
	
	public int getFiberScaleNumber() {
		return Integer.MIN_VALUE;
	}
	
	public int getNucleoScaleNumber() {
		return Integer.MIN_VALUE;
	}
	public int getChrID() {
		return Integer.MIN_VALUE;
	}
	public int getFromPos() {
		return Integer.MIN_VALUE;
	}
	public int getEndPos() {
		return Integer.MIN_VALUE;
	}
	public String getSpName() {
		return null;
	}
	public String getEnsChr() {
		return null;
	}
	public String getLcChr() {
		return null;
	}
	//added end -hcf
	public char getInsertionCode() {
		return '\0';
	}
}
