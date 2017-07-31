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

import org.jmol.constant.EnumStructure;

public abstract class JmolAdapterStructureIterator {
	public abstract boolean hasNext();

	public abstract int getModelIndex();

	public abstract EnumStructure getStructureType();

	public abstract EnumStructure getSubstructureType();

	public abstract String getStructureID();

	public abstract int getSerialID();

	public abstract int getStrandCount();

	public abstract char getStartChainID();

	public abstract int getStartSequenceNumber();

	public abstract char getStartInsertionCode();

	public abstract char getEndChainID();

	public abstract int getEndSequenceNumber();

	public abstract char getEndInsertionCode();
}
