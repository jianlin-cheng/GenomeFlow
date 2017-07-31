package org.jmol.api;


/*
 * This interface can be used by any applet to register itself
 * with Jmol and thus allow direct applet-applet scripting and 
 * syncing operations.
 *  
 */
public interface JmolScriptInterface extends JmolSyncInterface {

  public Object setStereoGraphics(boolean isStereo);
  public void script(String script);
  public String scriptCheck(String script);
  public String scriptWait(String script);
  public String scriptWaitOutput(String script);
  public String scriptWait(String script, String statusParams);
  public String scriptNoWait(String script);

}
