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

import org.jmol.api.JmolAdapterBondIterator;

class BondIterator extends JmolAdapterBondIterator {
	private BitSet bsAtoms;
	private Bond[] bonds;
	private int ibond;
	private Bond bond;
	private int bondCount;

	BondIterator(AtomSetCollection atomSetCollection) {
		bsAtoms = atomSetCollection.bsAtoms;
		bonds = atomSetCollection.getBonds();
		bondCount = atomSetCollection.getBondCount();
		ibond = 0;
	}

	@Override
	public boolean hasNext() {
		if (ibond == bondCount)
			return false;
		while ((bond = bonds[ibond++]) == null
				|| (bsAtoms != null && (!bsAtoms.get(bond.atomIndex1) || !bsAtoms
						.get(bond.atomIndex2))))
			if (ibond == bondCount)
				return false;
		return true;
	}

	@Override
	public Object getAtomUniqueID1() {
		return Integer.valueOf(bond.atomIndex1);
	}

	@Override
	public Object getAtomUniqueID2() {
		return Integer.valueOf(bond.atomIndex2);
	}

	@Override
	public int getEncodedOrder() {
		return bond.order;
	}
}
