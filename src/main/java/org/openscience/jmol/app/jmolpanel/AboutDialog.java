/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-05-19 09:56:08 -0500 (Sat, 19 May 2012) $
 * $Revision: 17160 $
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
package org.openscience.jmol.app.jmolpanel;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jmol.api.JmolViewer;
import org.jmol.i18n.GT;
import org.jmol.util.Logger;

class AboutDialog extends JDialog implements HyperlinkListener {

  protected JEditorPane html;
  protected URL aboutURL;
  
  private JmolViewer viewer;
  
  private JScrollPane scroller;
  
/*  
  private JButton backButton;
  private List<URL> history = new ArrayList<URL>();
  private URL thisURL;

  private void addToHistory(URL url) {
    history.add(url);
    backButton.setEnabled(true);
    for (int i = history.size(); --i >= 0;)
      System.out.println(i + "\t" + history.get(i));
  }
*/
  
  
  AboutDialog(JFrame fr, JmolViewer viewer) {

    super(fr, GT._("About Jmol"), true);
    this.viewer = viewer;
    try {
      aboutURL = this.getClass().getClassLoader().getResource(
          JmolResourceHandler.getStringX("About.aboutURL"));
      if (aboutURL != null) {
        html = new JEditorPane();
        html.setContentType("text/html");
        html.setText(GuiMap.getResourceString(this, aboutURL.getPath()));
      } else {
        html = new JEditorPane("text/plain", GT._(
            "Unable to find url \"{0}\".", JmolResourceHandler
                .getStringX("About.aboutURL")));
      }
      html.setEditable(false);
      html.addHyperlinkListener(this);
    } catch (MalformedURLException e) {
      Logger.warn("Malformed URL: " + e);
    } catch (IOException e) {
      Logger.warn("IOException: " + e);
    }
    scroller = new JScrollPane() {

      @Override
      public Dimension getPreferredSize() {
        return new Dimension(500, 400);
      }

      @Override
      public float getAlignmentX() {
        return LEFT_ALIGNMENT;
      }
    };
    scroller.getViewport().add(html);

    JPanel htmlWrapper = new JPanel(new BorderLayout());
    htmlWrapper.setAlignmentX(LEFT_ALIGNMENT);
    htmlWrapper.add(scroller, BorderLayout.CENTER);
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.add(htmlWrapper, BorderLayout.CENTER);


/*    
    thisURL = aboutURL;
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    buttonPanel.setLayout(new BorderLayout());

    // create browser "back" button
    backButton = new JButton(GT._("back"));
    backButton.setEnabled(false);

    backButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        back();
      }
    });

    // create return-to-start dialog button
    JButton returnUserGuide = new JButton(GT._("back to About"));
    returnUserGuide.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        backTo(aboutURL);
      }
    });
    buttonPanel.add(returnUserGuide, BorderLayout.WEST);
    buttonPanel.add(backButton, BorderLayout.CENTER);
    getRootPane().setDefaultButton(returnUserGuide);

    JButton ok = new JButton(GT._("OK"));
    final AboutDialog aboutDialog = this;
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        aboutDialog.setVisible(false);
      }
    });
    buttonPanel.add(ok, BorderLayout.EAST);
    getRootPane().setDefaultButton(ok);
    //container.add(buttonPanel, BorderLayout.SOUTH);
*/
    getContentPane().add(container);
    pack();
    Dimension screenSize = this.getToolkit().getScreenSize();
    Dimension size = this.getSize();
    screenSize.height = screenSize.height / 2;
    screenSize.width = screenSize.width / 2;
    size.height = size.height / 2;
    size.width = size.width / 2;
    int y = screenSize.height - size.height;
    int x = screenSize.width - size.width;
    this.setLocation(x, y);
  }
/*
  private void visit(URL url) {
    Cursor cursor = html.getCursor();
    Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    html.setCursor(waitCursor);
    SwingUtilities.invokeLater(new PageLoader(url, cursor));
  }
*/
  @Override
public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      linkActivated(e.getURL());
    }
  }

  /**
   * Opens a web page in an external browser
   * 
   * @param url
   *        the URL to follow
   */
  protected void linkActivated(URL url) {
    viewer.showUrl(url.toString());
/*
    if (!url.getProtocol().equals("file")) {
      viewer.showUrl(url.toString());
      return;
    }
    addToHistory(thisURL);
    thisURL = url;
    visit(url);
*/    
  }
/*
  protected void back() {
    URL u = history.remove(history.size() - 1);
    backButton.setEnabled(history.size() > 0);
    visit(u);
  }

  protected void backTo(URL url) {
    try {
      html = new JEditorPane(url);
    } catch (IOException e) {
      return;
    }
    html.setEditable(false);
    html.addHyperlinkListener(this);
    thisURL = url;
    scroller.getViewport().add(html);
  }

*/  /*
   * temporary class that loads synchronously (although later than
   * the request so that a cursor change can be done).
   *
  class PageLoader implements Runnable {

    PageLoader(URL url, Cursor cursor) {
      this.url = url;
      this.cursor = cursor;
    }

    public void run() {

      if (url == null) {

        // restore the original cursor
        html.setCursor(cursor);

        // remove this hack when automatic validation is
        // activated.
        Container parent = html.getParent();
        parent.repaint();
      } else {
        Document doc = html.getDocument();
        try {
          html.setContentType("text/html");
          html.setPage(url);
        } catch (Exception ioe) {
          html.setDocument(doc);
          getToolkit().beep();
        } finally {

          // schedule the cursor to revert after the paint
          // has happended.
          url = null;
          SwingUtilities.invokeLater(this);
        }
      }
    }

    URL url;
    Cursor cursor;
  }
*/  
}
