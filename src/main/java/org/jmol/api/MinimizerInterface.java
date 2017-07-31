package org.jmol.api;

import java.util.BitSet;

import org.jmol.modelset.Atom;
import org.jmol.modelset.Bond;



public interface MinimizerInterface {

  public abstract boolean minimize(int steps, double crit, BitSet bsSelected, 
                                   BitSet bsFixed, boolean haveFixed, 
                                   boolean isSilent, String ff) throws Exception;
  public abstract void setProperty(String propertyName, Object propertyValue);
  public abstract Object getProperty(String propertyName, int param);
  public abstract void calculatePartialCharges(Bond[] bonds, int bondCount, Atom[] atoms, BitSet bsAtoms);
}
