package edu.missouri.chenglab.Heatmap;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.missouri.chenglab.ClusterTAD.ClusterTAD;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import edu.missouri.chenglab.Heatmap.TADFrame;
import edu.missouri.chenglab.swingutilities.ExtractHiCWorker;


/**
 * <p>This class shows the various options of the HeatMap.</p>
 *
 * <hr />
 * <p><strong>Copyright:</strong> Copyright (c) 2007, 2008</p>
 *
 * <p>HeatMap is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.</p>
 *
 * <p>HeatMap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU General Public License
 * along with HeatMap; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA</p>
 *
 * @author Matthew Beckler (matthew@mbeckler.org)
 * @author Josh Hayes-Sheen (grey@grevian.org), Converted to use BufferedImage.
 * @author J. Keller (jpaulkeller@gmail.com), Added transparency (alpha) support, data ordering bug fix.
 * @version 1.6
 */

public class HeatMapDemo extends JFrame implements ItemListener, FocusListener
{
	String Filename = null;
    HeatMap panel;
    JCheckBox drawLegend;
    JCheckBox drawTitle;
    JCheckBox drawXTitle;
    JCheckBox drawXTicks;
    JCheckBox drawYTitle;
    JCheckBox drawYTicks;
    JTextField textTitle;
    JTextField textXTitle;
    JTextField textYTitle;
    JTextField textXMin;
    JTextField textXMax;
    JTextField textYMin;
    JTextField textYMax;
    JTextField textXMinG;
    JTextField textXMaxG;
    JTextField textYMinG;
    JTextField textYMaxG;
    JTextField textResolution;
    JComboBox gradientComboBox;
    //Tosin Added
    JCheckBox runClusterTAD;
    JComboBox ShowDataCombo;
   
    JCheckBox drawtadshowall;
   JCheckBox drawdirection;
    
    
   JLabel imageCanvas;
   Dimension size;
   double scale = 1.0;
   JFrame frame ;
   BufferedImage img;
   
   JPanel listPane;
   JCheckBox Zoom;  
   JScrollPane scroll_pane;   // Add listpane to scrollpane
   
   
    
    JCheckBox isMatrix;
    JLabel labelr;
    JTextField textnumUnits;
    JTextField textnumMUnits;
    JTextField textResdet;
    JTextField textstart;
    JTextField textend;
    
    
    static String sep = "\\s+";
    JTextField inputContactFileField ;
    static JTextField inputtadFileField ;
    JButton openContactFileButton;
    JButton loadtadFileButton;
    JButton findtadButton;
    JButton displayheatmapButton;
    JButton exitButton;
    
    private final static String FILE_OPEN_WINDOW_NAME = "FileOpen";
    public static double[][] data = null;
    public static double[][] default_data = null;
   
    static JEditorPane pane = new JEditorPane();
    static String[] TAD = null;
   
    public  boolean useGraphicsYAxis = true;  //Determines the direction of y axis Data arrangement
    static int Resolution = 40000; // Assigned a constant resolution of 40,000
    
    static int GenomeSL = 0; //Genomic start Location
    static int GenomeEL = 0; //Genomic start Location
	static final int KB = 1000;
	static final long MILLION = 1000000;
	public  boolean useTuple = false;
	public static double minrow,mincol,maxrow,maxcol;
    public int Transform = 0;
    public double constant_minrow, constant_mincol, constant_maxrow, constant_maxcol;
   
  
    public  static double[][] tanh_data = null;
    public  static double[][] pearson_data = null;
    public  static double[][] spearman_data = null;

    
    ImageIcon[] icons;
    String[] names = {
    		 		 
    				  "GRADIENT_BLACK_TO_WHITE",
                      "GRADIENT_BLUE_TO_RED",
                      "GRADIENT_GREEN_YELLOW_ORANGE_RED",  
                      "GRADIENT_HEAT",
                      "GRADIENT_HOT",
                      "GRADIENT_MAROON_TO_GOLD",
                      "GRADIENT_RAINBOW",
                      "GRADIENT_RED_TO_GREEN",
                      "GRADIENT_ROY"};
    Color[][] gradients = {Gradient.GRADIENT_BLACK_TO_WHITE,
                           Gradient.GRADIENT_BLUE_TO_RED,
                           Gradient.GRADIENT_GREEN_YELLOW_ORANGE_RED,
                           Gradient.GRADIENT_HEAT,
                           Gradient.GRADIENT_HOT,
                           Gradient.GRADIENT_MAROON_TO_GOLD,
                           Gradient.GRADIENT_RAINBOW,
                           Gradient.GRADIENT_RED_TO_GREEN,
                           Gradient.GRADIENT_ROY};
    
    String [] datashow_names = { "TANH",
    							 "RAW_DATA",
    							 "PEARSON",
    							 "SPEARMAN"
    							 };


    public HeatMapDemo() throws Exception
    {
        super("Genomeflow 2D Visualization");
        
        // gui stuff to demonstrate options
        listPane = new JPanel();
        listPane.setLayout(new GridBagLayout());
        listPane.setBorder(BorderFactory.createTitledBorder("Display Control"));
        
      
               
        GridBagConstraints gbc;        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(2, 1, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LINE_START;
        
        
       
       
        
        drawTitle = new JCheckBox("Draw Title");
        drawTitle.setSelected(true);
        drawTitle.addItemListener(this);
        listPane.add(drawTitle, gbc);
        gbc.gridy = GridBagConstraints.RELATIVE;
        
        drawLegend = new JCheckBox("Draw Legend");
        drawLegend.setSelected(true);
        drawLegend.addItemListener(this);
        listPane.add(drawLegend, gbc);
        
        drawXTitle = new JCheckBox("Draw X-Axis Title");
        drawXTitle.setSelected(true);
        drawXTitle.addItemListener(this);
        listPane.add(drawXTitle, gbc);
        
        drawXTicks = new JCheckBox("Draw X-Axis Ticks");
        drawXTicks.setSelected(true);
        drawXTicks.addItemListener(this);
        listPane.add(drawXTicks, gbc);
        
        drawYTitle = new JCheckBox("Draw Y-Axis Title");
        drawYTitle.setSelected(true);
        drawYTitle.addItemListener(this);
        listPane.add(drawYTitle, gbc);
        
        drawYTicks = new JCheckBox("Draw Y-Axis Ticks");
        drawYTicks.setSelected(true);
        drawYTicks.addItemListener(this);
        listPane.add(drawYTicks, gbc);
        
        drawdirection = new JCheckBox("Heatmap Direction(Left/Right):");
        drawdirection.setSelected(true);
        drawdirection.addItemListener(this);
        listPane.add(drawdirection, gbc);
        
            
        Zoom = new JCheckBox("Enable Zoom Mode");
        Zoom.setSelected(false);
        Zoom .addItemListener(this);
        listPane.add( Zoom , gbc);
             
        listPane.add(Box.createVerticalStrut(20), gbc);
        

        isMatrix= new JCheckBox("Is SquareMatrix? (Input contact file )");
        isMatrix.setSelected(false);
        isMatrix.addItemListener(this);
        listPane.add(isMatrix, gbc);
        

        labelr = new JLabel("Specify Resolution:");
        labelr.setVisible(false);
        listPane.add(labelr, gbc);
        textResolution = new JTextField();     
        textResolution.setVisible(false);
        listPane.add(textResolution, gbc);
        
       
        JLabel labelx = new JLabel("Input contact file:");
        listPane.add(labelx, gbc);
        inputContactFileField = new JTextField(2);  
        inputContactFileField .setMaximumSize(  inputContactFileField .getPreferredSize());
        inputContactFileField .setMinimumSize(  inputContactFileField .getPreferredSize());
        listPane.add(inputContactFileField, gbc);
        
        
        openContactFileButton = new JButton("Browse File & Load");
        openContactFileButton.setBackground(Color.YELLOW);
        openContactFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {             
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new java.io.File("C:/Users"));
            fc.setDialogTitle("File Browser.");
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fc.showOpenDialog(openContactFileButton) == JFileChooser.APPROVE_OPTION){
            	inputContactFileField .setText(fc.getSelectedFile().getAbsolutePath());
            }
            
            
           // JOptionPane.showMessageDialog(null, "Click on the DRAW HEATMAP button below to Draw !", "GenomeFlow-2D", JOptionPane.INFORMATION_MESSAGE);	
            	
      	  Window win = SwingUtilities.getWindowAncestor((AbstractButton)e.getSource());
            final JDialog dialog = new JDialog(win, "Loading data ... please wait !", ModalityType.APPLICATION_MODAL);
    		dialog.setPreferredSize(new Dimension(300,80));
    		          		
      	//Draw HeatMap for 2D Visualization
      	// DrawHeatMap();          
    		
    		Draw drawinbackground= new Draw();
      	  
    		drawinbackground.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					switch (evt.getPropertyName()){
					case "progress":
						break;
					case "state":
						switch ((StateValue)evt.getNewValue()){
						case DONE:
							
							win.setEnabled(true);
							dialog.dispose();
							
							try {
								String msg = drawinbackground.get();
								//	JOptionPane.showMessageDialog(null, msg); // returns loading successful
							} catch (InterruptedException e) {									
								e.printStackTrace();
								JOptionPane.showMessageDialog(null, "Error while extracting data");
							} catch (ExecutionException e) {									
								e.printStackTrace();
								JOptionPane.showMessageDialog(null, "Error while extracting data");
							}
							
							
							break;
						case PENDING:								
							break;
						case STARTED:
							dialog.setVisible(true);
							win.setEnabled(false);								
							break;
						default:								
							break;
						}
					}
					
				}
			  });				  
			  
    		drawinbackground.execute();
    		
    		
    		
			JProgressBar progressBar = new JProgressBar();
		    progressBar.setIndeterminate(true);
		    JPanel panel = new JPanel(new BorderLayout());
		      
		    panel.add(progressBar, BorderLayout.CENTER);
		    panel.add(new JLabel(""), BorderLayout.PAGE_START);
		    dialog.add(panel);
		    dialog.pack();
		    dialog.setLocationRelativeTo(win);
		    dialog.setVisible(true);	
            
          }
    });
        listPane.add(openContactFileButton, gbc);
        
                
        listPane.add(Box.createVerticalStrut(10), gbc);
        
        JLabel label = new JLabel("Title:");
        listPane.add(label, gbc);
        
        textTitle = new JTextField();
        textTitle.addFocusListener(this);
        listPane.add(textTitle, gbc);
        
        label = new JLabel("X-Axis Title:");
        listPane.add(label, gbc);
        
        textXTitle = new JTextField();
        textXTitle.addFocusListener(this);
        listPane.add(textXTitle, gbc);

        label = new JLabel("Y-Axis Title:");
        listPane.add(label, gbc);

        textYTitle = new JTextField();
        textYTitle.addFocusListener(this);
        listPane.add(textYTitle, gbc);
        
        listPane.add(Box.createVerticalStrut(20), gbc);
               
        label = new JLabel("Specify Matrix Index:");
        listPane.add(label, gbc);
        
        //25 is next row number
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        label = new JLabel("X min:");
        gbc.gridx = 0;
        gbc.gridy = 25;
        listPane.add(label, gbc);
        textXMin = new JTextField();
        textXMin.addFocusListener(this);
        gbc.gridy = 26;
        listPane.add(textXMin, gbc);
        
        label = new JLabel("X max:");
        gbc.gridx = 1;
        gbc.gridy = 25;
        listPane.add(label, gbc);
        textXMax = new JTextField();
        textXMax.addFocusListener(this);
        gbc.gridy = 26;
        listPane.add(textXMax, gbc);
        
        label = new JLabel("Y min:");
        gbc.gridx = 2;
        gbc.gridy = 25;
        listPane.add(label, gbc);
        textYMin = new JTextField();
        textYMin.addFocusListener(this);
        gbc.gridy = 26;
        listPane.add(textYMin, gbc);
        
        label = new JLabel("Y max:");
        gbc.gridx = 3;
        gbc.gridy = 25;
        listPane.add(label, gbc);
        textYMax = new JTextField();
        textYMax.addFocusListener(this);
        gbc.gridy = 26;
        listPane.add(textYMax, gbc);
        
        //line 27        
        gbc.gridx = 0;
        gbc.gridy = 27;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        listPane.add(Box.createVerticalStrut(20), gbc);
        
        label = new JLabel("Genome Location Equivalent (Bin):");
        label.setFont(new Font("Arial", Font.BOLD, 10));
        listPane.add(label, gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        label = new JLabel("X min:");
        gbc.gridx = 0;
        gbc.gridy = 28;
        listPane.add(label, gbc);
        textXMinG = new JTextField();
        textXMinG.addFocusListener(this);
        textXMinG.setEditable(false);
        gbc.gridy = 29;
        listPane.add(textXMinG, gbc);
        
        label = new JLabel("X max:");
        gbc.gridx = 1;
        gbc.gridy = 28;
        listPane.add(label, gbc);
        textXMaxG = new JTextField();
        textXMaxG.addFocusListener(this);
        textXMaxG.setEditable(false);
        gbc.gridy = 29;
        listPane.add(textXMaxG, gbc);
        
        label = new JLabel("Y min:");
        gbc.gridx = 2;
        gbc.gridy = 28;
        listPane.add(label, gbc);
        textYMinG = new JTextField();
        textYMinG.addFocusListener(this);
        textYMinG.setEditable(false);
        gbc.gridy = 29;
        listPane.add(textYMinG, gbc);
        
        label = new JLabel("Y max:");
        gbc.gridx = 3;
        gbc.gridy = 28;
        listPane.add(label, gbc);
        textYMaxG = new JTextField();
        textYMaxG.addFocusListener(this);
        textYMaxG.setEditable(false);
        gbc.gridy = 29;
        listPane.add(textYMaxG, gbc);
        
        
        labelx = new JLabel("Number of Units detected:");
        labelx.setFont(new Font("Arial", Font.BOLD, 10));
        gbc.gridx = 0;
        gbc.gridy = 30;
        listPane.add(labelx, gbc);
        
        textnumUnits = new JTextField();
        textnumUnits.addFocusListener(this);
        gbc.gridx = 0;
        gbc.gridy = 31;
        gbc.gridwidth = 2;
        listPane.add(textnumUnits, gbc);
        
        
        labelx = new JLabel("Number of Missing Units:");
        labelx.setFont(new Font("Arial", Font.BOLD, 10));
        gbc.gridx = 2;
        gbc.gridy = 30;
        listPane.add(labelx, gbc);
        
        textnumMUnits = new JTextField();
        textnumMUnits.addFocusListener(this);
        gbc.gridx = 2;
        gbc.gridy = 31;
        gbc.gridwidth = 2;
        listPane.add(textnumMUnits, gbc);
        
        
        labelx = new JLabel("Resolution detected:");
        labelx.setFont(new Font("Arial", Font.BOLD, 10));
        gbc.gridx = 0;
        gbc.gridy = 32;       
        listPane.add(labelx, gbc);
        
        textResdet = new JTextField();
        textResdet.addFocusListener(this);
        gbc.gridx = 0;
        gbc.gridy = 33;
        gbc.gridwidth = 2;
        listPane.add(textResdet, gbc);
        
        
        labelx = new JLabel("Initial Start Position:");
        labelx.setFont(new Font("Arial", Font.BOLD, 10));
        gbc.gridx = 0;
        gbc.gridy = 34;
        listPane.add(labelx, gbc);
        
        textstart = new JTextField();
        textstart.addFocusListener(this);
        gbc.gridx = 0;
        gbc.gridy = 35;
        gbc.gridwidth = 2;      
        listPane.add(textstart, gbc);
        
        labelx = new JLabel("Initial End Position:");
        labelx.setFont(new Font("Arial", Font.BOLD, 10));
        gbc.gridx = 2;
        gbc.gridy = 34;       
        listPane.add(labelx, gbc);
        
        textend = new JTextField();
        textend.addFocusListener(this);      
        gbc.gridx = 2;
        gbc.gridy = 35;
        gbc.gridwidth = 2;
        listPane.add(textend, gbc);
        
                
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        listPane.add(Box.createVerticalStrut(15), gbc);
             
                
        //----------------------------------------------------------------------
        
        label = new JLabel("Gradient:");
        listPane.add(label, gbc);
        
        icons = new ImageIcon[names.length];
        Integer[] intArray = new Integer[names.length];
        for (int i = 0; i < names.length; i++)
        {
            intArray[i] = new Integer(i);
            icons[i] = createImageIcon("images/" + names[i] + ".gif");
        }
        
        gradientComboBox = new JComboBox(intArray);
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        gradientComboBox.setRenderer(renderer);
        gradientComboBox.addItemListener(this);
        listPane.add(gradientComboBox, gbc);
        //----------------------------------------------------------------------
        // Tosin Added
        listPane.add(Box.createVerticalStrut(5), gbc);
        label = new JLabel("Data Type:");
        listPane.add(label, gbc);
                
        ShowDataCombo = new JComboBox(datashow_names);
        MyComboBoxRenderer myrenderer = new MyComboBoxRenderer();
        ShowDataCombo.setRenderer(myrenderer);
        ShowDataCombo .setMaximumRowCount(4);
        ShowDataCombo.addItemListener(this);        
        listPane.add(ShowDataCombo, gbc);        
              
        listPane.add(Box.createVerticalStrut(20), gbc);
        
        
        JLabel tlabel = new JLabel("TAD Annotation");
        gbc.gridy = 44;
        tlabel.setFont(new Font("Arial", Font.BOLD, 14));       
        listPane.add(tlabel, gbc);
        gbc.gridy = GridBagConstraints.RELATIVE;
        
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        listPane.add(Box.createVerticalStrut(10), gbc);
        
        int y = 46;        
        
        gbc.gridx = 0;
        gbc.gridy = y++;  
        labelx = new JLabel("Load TAD file:");
        listPane.add(labelx, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = y++;  
        inputtadFileField = new JTextField(2);
        inputtadFileField .addFocusListener(this);
        inputtadFileField.setMaximumSize( inputtadFileField.getPreferredSize());
        inputtadFileField.setMinimumSize( inputtadFileField.getPreferredSize());
        listPane.add(inputtadFileField, gbc);
        
        
        gbc.gridx = 0;
        gbc.gridy = y++;  
        loadtadFileButton = new JButton("Browse & Load File ");
        loadtadFileButton.setBackground(Color.YELLOW);
        loadtadFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {             
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new java.io.File("C:/Users"));
            fc.setDialogTitle("File Browser.");
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fc.showOpenDialog(openContactFileButton) == JFileChooser.APPROVE_OPTION){
            	inputtadFileField .setText(fc.getSelectedFile().getAbsolutePath());
            }
            
          //Disable zoom
        	if (Zoom.isSelected()) {
        		Zoom.setSelected(false);
        	}
        	            
            
            //load file
            String name = inputtadFileField.getText();
            
        	int [][] TD;
			try {
				TD = LoadHeatmap.readTADFile(name,sep);
				
				// Clear the editor text
        		pane.setText(null);
        		appendtoEditorfromFile(TD);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		
		
        }
    });
        listPane.add (loadtadFileButton, gbc);
        
        
        gbc.gridx = 0;
        gbc.gridy = y++;  
        listPane .add(Box.createVerticalStrut(10), gbc);
               
        gbc.gridx = 0;
        gbc.gridy = y++;  
        
       //line 12
        gbc.gridx = 0;
        gbc.gridy = y++;  
        JLabel showlabel = new JLabel("Identified TAD:");
        listPane.add(showlabel, gbc);
              
        gbc.gridx = 0;
        gbc.gridy = y++;  
        listPane .add(Box.createVerticalStrut(10), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;  
        pane.setEditable(false);  // prevents the user from editting it.
        // programmatically put this text in the TextPane
       
        JScrollPane editorScrollPane = new JScrollPane(pane);
        editorScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(150, 80));
        //editorScrollPane.setMinimumSize(new Dimension(5, 5));
        listPane.add( editorScrollPane );
        
        //Append Identified TAD		
        int [][] TD  = {{2,10}}; //Identified TAD
       // appendtoEditor(TD);                                
                        
        gbc.gridx = 0;
        gbc.gridy = y++;    
        drawtadshowall = new JCheckBox("Show TAD on Heatmap");
        drawtadshowall.setSelected(false);
        drawtadshowall .addItemListener(this);
        listPane.add( drawtadshowall , gbc);
        
        gbc.gridx = 0;
        gbc.gridy = y++;  
        listPane.add(Box.createVerticalStrut(30), gbc);
        gbc.gridx = 0;
        gbc.gridy = y++;  
        
        
          
        //----------------------------------------------------------------------
        
   
        default_data =  HeatMap.generateSinCosData(200);
        data = default_data;        
                 
        // you can use a pre-defined gradient:
        panel = new HeatMap(data, useGraphicsYAxis, Gradient.GRADIENT_HOT);
        gradientComboBox.setSelectedIndex(0);
        
        // set miscelaneous settings
        panel.setDrawLegend(true);

        panel.setTitle("HeatMap Display");
        textTitle.setText("HeatMap Display");
        panel.setDrawTitle(true);

        panel.setXAxisTitle("Genome bin Resolution (bp) ");
        textXTitle.setText("Genome bin Resolution (bp)");
        panel.setDrawXAxisTitle(true);

        panel.setYAxisTitle("Genome bin Resolution (bp)");
        textYTitle.setText("Genome bin Resolution (bp)");
        panel.setDrawYAxisTitle(true);
        
        //Tosin added    
        int [] mat = LoadHeatmap.MatSize(data);
        int row_max = mat[0];
        int col_max = mat[1];
        int minimum = 0;
        int  MissingUnits = (col_max - minimum) - mat[1];
        //Assign to global variables
    	minrow = minimum; mincol = minimum;
  	    maxrow = row_max; maxcol = col_max;
        
        panel.setCoordinateBounds(minimum ,row_max, col_max, minimum);        
        textXMin.setText( String.valueOf(minimum));
        textXMax.setText(String.valueOf(row_max));
        textYMin.setText( String.valueOf(minimum));
        textYMax.setText(String.valueOf(col_max));
        
      //initial limits
	     constant_minrow =  minimum; constant_mincol =  minimum; constant_maxrow = row_max; constant_maxcol = col_max; 
        // Assign value to the text for Genome location
        int min = minimum;
        int r_max = Location(row_max,GenomeSL);
        int c_max = Location(col_max ,GenomeSL);
        
        textXMinG.setText( String.valueOf(min));
        textXMaxG.setText(String.valueOf(r_max));
        textYMinG.setText( String.valueOf(min));
        textYMaxG.setText(String.valueOf(c_max));
        textnumUnits.setText(String.valueOf(mat[1]));        
        textnumUnits.setEditable(false);
        textnumMUnits.setText(String.valueOf(MissingUnits ));        
        textnumMUnits.setEditable(false);

        textResdet.setText(String.valueOf(Resolution));
        textResdet.setEditable(false);
        textend.setEditable(false);
        textstart.setEditable(false);
        textstart.setText( String.valueOf(min));
        textend.setText( String.valueOf(c_max));
        
        
        panel.setDrawXTicks(true);
        panel.setDrawYTicks(true);
        //Tosin added
        panel.setrunClusterTAD(true);
        
        
        panel.setColorForeground(Color.black);
        panel.setColorBackground(Color.white);
     
        
      
         scroll_pane = new JScrollPane( listPane);
         scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        
      //   this.getContentPane().add(showpane, BorderLayout.WEST);
        this.getContentPane().add(panel, BorderLayout.CENTER);  
	    this.getContentPane().add( scroll_pane, BorderLayout.EAST);
	    
    }
    
    
   /**
    * allows to be run in background
    * Function to Draw Heatmap
    */
    
    public void DrawHeatMap() {

    	
    	//Disable zoom
    	if (Zoom.isSelected()) {
    		Zoom.setSelected(false);
    	}
    	
    	
    	 int [] mat ;
    	 int row_max=0,col_max=0,minimum = 0;
    	 int min = 0,r_max = 0, c_max = 0;
    	 int MissingUnits = 0;    
    	 //load the file in the textbox - Tuple format
    	Filename = inputContactFileField.getText();
    	if (!Filename.isEmpty() && !Filename.equals("")) {
    		
    		
    		try {
        			if (isMatrix.isSelected()) {
        				
        				
        				if (textResolution.getText().equals("") ||  isMatrix.isSelected()==false) {
        					JOptionPane.showMessageDialog(null, "Please Specify Matrix Resolution", "GenomeFlow-2D", JOptionPane.INFORMATION_MESSAGE);	            					
        				}
        				// Input is a Matrix format
        				default_data =  LoadHeatmap.readFile(Filename, sep);
        				//update the row and column
        				mat = LoadHeatmap.MatSize(default_data);            	
                  	    row_max = mat[0];
                  	    col_max = mat[1];
                  	    minimum = 0;
                  	    MissingUnits = (col_max - minimum) - mat[1];
                  	    
                  	    Resolution = Integer.valueOf(textResolution.getText()); // Recieve resolution from Text 
                        min= Location(minimum,GenomeSL);
                        r_max = Location(row_max,GenomeSL);
                        c_max = Location(col_max ,GenomeSL);
                      
                        int genomelocation = Resolution *  c_max ;
              	        
              	        
          				//update the row and column	          				
                        panel.setXAxisTitle("Number of Bins");
               	        textXTitle.setText("Number of Bins");
               	        panel.setDrawXAxisTitle(true);

               	        panel.setYAxisTitle("Number of Bins");
               	        textYTitle.setText("Number of Bins");
               	        panel.setDrawYAxisTitle(true);
               	        useTuple = false;   // Define if the input is Tuple input or not
               	        
                   	     minrow = minimum; mincol = minimum;
                	     maxrow = row_max; maxcol = col_max;
                  	       
        			}
        			else {
        				
        				useTuple  = true; //Specify input as Tuple input
        				//Input is in Tuple format
        				default_data =  LoadHeatmap.readTupleFile(Filename, sep);       
        				// Detect the Resolution, detect the Starting Index
        				mat = LoadHeatmap.MatSize(default_data);            				
        				 
        				Resolution = LoadHeatmap.Resolution(Filename);
        				int genomelocation = LoadHeatmap.Endlocation;
        					                   	     
               	        panel.setXAxisTitle("Genome bin Location (" + ResolutionString(genomelocation)+ ") ");
               	        textXTitle.setText("Genome bin Location (" + ResolutionString(genomelocation)+")" );
               	        panel.setDrawXAxisTitle(true);

               	        panel.setYAxisTitle("Genome Bin Location (" + ResolutionString(genomelocation)+ ") ");
               	        textYTitle.setText("Genome Bin Location  (" + ResolutionString(genomelocation)+")" );
               	        panel.setDrawYAxisTitle(true);
               	        	
               	        
               	         minimum = truncateNumber(LoadHeatmap.Startlocation);
              	         row_max =truncateNumber( LoadHeatmap.Endlocation);
              	         col_max = truncateNumber(LoadHeatmap.Endlocation);
                  	     
              	        GenomeSL = LoadHeatmap.Startlocation;
              	        GenomeEL = LoadHeatmap.Endlocation;
              	        
              	        MissingUnits = ((GenomeEL - GenomeSL)/Resolution)- (mat[1]);
              	        
              	        min=  GenomeSL;
                        r_max = GenomeEL;
                        c_max = GenomeEL;
              	        
                        minrow = minimum; mincol = minimum;
                	    maxrow = row_max; maxcol = col_max;
        				
        			}
        			
        			 data = LoadHeatmap.Tanh(default_data); //Tanh as the default representation
        			 tanh_data = data; //copy tanh into data 
        			 Transform = 3;
        			 
        			 
            		 panel.updateData(data, useGraphicsYAxis);            		 	
          	        textXMin.setText( String.valueOf(minimum));
          	        textXMax.setText(String.valueOf(row_max));
          	        textYMin.setText( String.valueOf(minimum));
          	        textYMax.setText(String.valueOf(col_max));
          	        //Genome Location
              	      textXMinG.setText( String.valueOf(min));
                      textXMaxG.setText(String.valueOf(r_max));
                      textYMinG.setText( String.valueOf(min));
                      textYMaxG.setText(String.valueOf(c_max));
                      textnumUnits.setText(String.valueOf(mat[1]+1));
                      textnumMUnits.setText(String.valueOf(MissingUnits ));    
                      textResdet.setText(String.valueOf(Resolution));
                      
                      textstart.setText( String.valueOf(min));
                      textend.setText( String.valueOf(c_max));
                      
             	     panel.setCoordinateBounds(minimum ,row_max, col_max, minimum); 
            	 
            	     
            	   //initial limits
            	     constant_minrow =  minimum; constant_mincol =  minimum; constant_maxrow = row_max; constant_maxcol = col_max; 
            	     
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    
    	}
    }
    
    
    /**
     * Append text to editor
     * @param s
     */
    public static void append(String s) {
    	   try {
    	      Document doc = pane.getDocument();
    	      
    	      doc.insertString(doc.getLength(), s, null);
    	   } catch(BadLocationException exc) {
    	      exc.printStackTrace();
    	   }
    }
    
    
    
    
    /**
     * Receives Result from ClusterTAD and send to Editor from File, [if already ransformed to identify based on genomic location]
     * @throws FileNotFoundException
     */
	public static void appendtoEditorfromFile(int[][]x) throws FileNotFoundException {
		TAD = new String[x.length];		
		for (int i=0;i<x.length;i++) {
			int first = (int)x[i][0] ;
			int second = (int) x[i][1] ;
			String str = String.valueOf(first) + "\t" + String.valueOf(second) + "\n";
			String str1 = String.valueOf(first) + "       " + String.valueOf(second);
			TAD[i]=str1;
			append(str);
		}
	}
    
    
    /**
     * Receives Result from ClusterTAD and send to Editor. [transforms index to the location identification]
     * @throws FileNotFoundException
     */
	public static void appendtoEditor(int [][]x) throws FileNotFoundException {
		TAD = new String[x.length];		
		for (int i=0;i<x.length;i++) {
			int first =    Location((int)x[i][0] ,  GenomeSL );
			int second =  Location((int)x[i][1] ,  GenomeSL );
			String str = String.valueOf(first) + "\t" + String.valueOf(second) + "\n";
			String str1 = String.valueOf(first) + "       " + String.valueOf(second);
			TAD[i]=str1;
			append(str);
		}
	}
	
	
	
	
	/**
     * Return the Resolution, KB or MB
     * @param x
     * @return
     */
	public static String ResolutionString(double x) {
	    return x < KB ?  String.valueOf(x) :
	           x < MILLION ?   "KB" :
	        	   			   "MB" ; 
	                        
	}
    
    /**
     * Return the string
     * @param x
     * @return
     */
	public static int truncateNumber(double x) {
	    return x < MILLION ?  (int) (x / KB) :
	        	   			  (int) (x / MILLION); 
	                        
	}
	
	 /**
     * Return the Resolution
     * @param x
     * @return
     */
	public static int ResNumber(String reslabel) {
		int m;
		if (reslabel.equals("KB")) {
			m = KB;
		}
		else
		{
			
			m = (int) MILLION;
		}
	   
	      return m;                  
	}
	
    /**
     * Return the specified Genomic location
     * @param index
     * @param Start
     * @return
     */
    public static int Location(double index, int Start) {
    	int loc = (int) (index) * Resolution;
    	loc =  loc + Start;
    	return loc;
    }
    
    /**
     * This function takes an identified location, and converts to index
     * @param location
     * @return
     */
    public static int convert2index(double location, int Start) {
    	// The start is the current datastart
    	int ind=  (int) location - Start;
    	ind = (int) ind/Resolution;
    	
    	return ind;
    }
    
    /**
     * Returns the TAD in index like format instead of genomic location
     * @param Start
     * @return
     * @throws FileNotFoundException
     */
    public static int [][] TADindex(int Start) throws FileNotFoundException {
    	String name = inputtadFileField.getText();	
    	int [][] TD;
    
		//TD = LoadHeatmap.readFile(name,sep);
    	TD = LoadHeatmap.ExtractedTAD;
		int row = TD.length; 
		int [][] TADIndex  =  new int[row][2];
		
		for (int x= 0; x <row; x++) {
			
			TADIndex[x][0] = convert2index(TD[x][0],Start);
			TADIndex[x][1] = convert2index(TD[x][1],Start);
		}
		
		return TADIndex;
    }
    
    public void focusGained(FocusEvent e)
    {}
    
    public void focusLost(FocusEvent e)
    {
    	   	
    	 double[][] new_data = null; 
        Object source = e.getSource();
        double min_x,max_x,min_y,max_y;    
        
        if (source == textTitle)
        {
            panel.setTitle(textTitle.getText());
        }
        else if (source == textXTitle)
        {
            panel.setXAxisTitle(textXTitle.getText());
        }
        else if (source == textYTitle)
        {
            panel.setYAxisTitle(textYTitle.getText());
        }
        else if (source == textXMin)
        {
        	int [] val = new int[4];
            double d;
            try
            {
                d = Double.parseDouble(textXMin.getText());               
                //Re-Draw Heatmap
                min_x = d; max_x = Double.parseDouble(textXMax.getText());
                min_y = Double.parseDouble(textYMin.getText()); max_y = Double.parseDouble(textYMax.getText());
                if (useTuple) {               
                	if (min_x < constant_minrow ) {min_x = constant_minrow ; textXMin.setText(String.valueOf(min_x));}
                	val = updateindex(min_x,  max_x, min_y,  max_y, LoadHeatmap.Startlocation);
                	min_x = val[0] ;  max_x = val[1];  min_y = val[2];  max_y = val[3];
                	minrow = min_x;
                }
                else
                {
                	if (min_x < constant_minrow ) {min_x = constant_minrow ; textXMin.setText(String.valueOf(min_x)); }
                	 minrow = min_x;
                }
                
             
                data = LoadHeatmap.MatrixReform(default_data, min_x, max_x, min_y, max_y);  //default data
             
              
               //Genome Location
        	    textXMinG.setText( String.valueOf( Location(minrow,GenomeSL)));    
        	    new_data = LoadHeatmap.TransformedData(Transform);
        	    new_data = LoadHeatmap.MatrixReform(new_data, min_x, max_x, min_y, max_y);  //Transformed data
                panel.updateData(new_data, useGraphicsYAxis);   
                panel.setXMinCoordinateBounds(d);
            }
            catch (Exception ex){}
        }
        else if (source == textXMax)
        {
        	int [] val = new int[4];
            double d;
            try
            {
                d = Double.parseDouble(textXMax.getText());
                //Re-Draw Heatmap
                min_x = Double.parseDouble(textXMin.getText());  ; max_x = d;
                min_y = Double.parseDouble(textYMin.getText()); max_y = Double.parseDouble(textYMax.getText());
                if (useTuple) {                	
                	if (max_x > constant_maxrow ) {max_x = constant_maxrow ; textXMax.setText(String.valueOf(max_x));}
                	val = updateindex(min_x,  max_x, min_y,  max_y, LoadHeatmap.Startlocation);
                	min_x = val[0] ;  max_x = val[1];  min_y = val[2];  max_y = val[3];
                 	maxrow = max_x;
                }
                else
                {
                	if (max_x > constant_maxrow ) {max_x = constant_maxrow ;textXMax.setText(String.valueOf(max_x)); }
               	
                	 maxrow = max_x;
                }
                data = LoadHeatmap.MatrixReform(default_data, min_x, max_x, min_y, max_y);
                
               
                //Genome Location        
                textXMaxG.setText(String.valueOf(Location(maxrow,GenomeSL)));
                new_data = LoadHeatmap.TransformedData(Transform);
        	    new_data = LoadHeatmap.MatrixReform(new_data, min_x, max_x, min_y, max_y);  //Transformed data
                panel.updateData(new_data, useGraphicsYAxis);   
                panel.setXMaxCoordinateBounds(d);
            }
            catch (Exception ex){}
        }
        else if (source == textYMin)
        {
        	int [] val = new int[4];
            double d;
            try
            {
                d = Double.parseDouble(textYMin.getText());
              //Re-Draw Heatmap
                min_x = Double.parseDouble(textXMin.getText());  ; max_x = Double.parseDouble(textXMax.getText());
                min_y = d; max_y = Double.parseDouble(textYMax.getText());
                if (useTuple) {
                	if (min_y < constant_mincol ) {min_y = constant_mincol ; textYMin.setText(String.valueOf(min_y)); }
                	val = updateindex(min_x,  max_x, min_y,  max_y, LoadHeatmap.Startlocation);
                	min_x = val[0] ;  max_x = val[1];  min_y = val[2];  max_y = val[3];
                	  mincol = min_y ;
                }else {
                	if (min_y < constant_mincol ) {min_y = constant_mincol ; textYMin.setText(String.valueOf(min_y));}
                	  mincol = min_y;
                }
                data = LoadHeatmap.MatrixReform(default_data, min_x, max_x, min_y, max_y);
              
               //Genome Location        	     
                textYMinG.setText( String.valueOf(Location(mincol,GenomeSL)));             
                new_data = LoadHeatmap.TransformedData(Transform);
        	    new_data = LoadHeatmap.MatrixReform(new_data, min_x, max_x, min_y, max_y);  //Transformed data
                panel.updateData(new_data, useGraphicsYAxis);   
                panel.setYMinCoordinateBounds(d);
            }
            catch (Exception ex){}
        }
        else if (source == textYMax)
        {
        	int [] val = new int[4];
            double d;
            try
            {
            	
                d = Double.parseDouble(textYMax.getText());
                //Re-Draw Heatmap
                min_x = Double.parseDouble(textXMin.getText());  ; max_x = Double.parseDouble(textXMax.getText());
                min_y = Double.parseDouble(textYMin.getText()); max_y = d;
                if (useTuple) {
                	if (max_y > constant_maxcol ) {max_y = constant_maxcol ; textYMax.setText(String.valueOf(max_y)); }
                	val = updateindex(min_x,  max_x, min_y,  max_y, LoadHeatmap.Startlocation);
                	min_x = val[0] ;  max_x = val[1];  min_y = val[2];  max_y = val[3];
                	maxcol = max_y;
                }else {
                	if (max_y > constant_maxcol ) {max_y = constant_maxcol ; textYMax.setText(String.valueOf(max_y)); }
                	 maxcol = max_y;
                }
                data = LoadHeatmap.MatrixReform(default_data, min_x, max_x, min_y, max_y);
               
               //Genome Location
        	    textYMaxG.setText(String.valueOf(Location(maxcol,GenomeSL)));
        	    new_data = LoadHeatmap.TransformedData(Transform);
        	    new_data = LoadHeatmap.MatrixReform(new_data, min_x, max_x, min_y, max_y);  //Transformed data
                panel.updateData(new_data, useGraphicsYAxis);       
                
                panel.setYMaxCoordinateBounds(d);
            }
            catch (Exception ex){}
        }
                    
        
        //update the x and y ticks        
        XYaxisTicks();
        
      //Disable zoom
    	if (Zoom.isSelected()) {
    		Zoom.setSelected(false);
    	}
    	
    	
    }
    
    private Color colorFromHex(String c)
    {
        try
        {
            int r = Integer.parseInt(c.substring(0, 2), 16);
            int g = Integer.parseInt(c.substring(2, 4), 16);
            int b = Integer.parseInt(c.substring(4, 6), 16);
            
            float rd = r / 255.0f;
            float gd = g / 255.0f;
            float bd = b / 255.0f;
            
            return new Color(rd, gd, bd);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
   
    /**
     * Returns if the current label for X and Y
     */
    public  void XYaxisTicks() {
    	
    	double   min_x, min_y, max_x, max_y;
        //Re-Draw Heatmap
        min_x = Double.parseDouble(textXMin.getText());  ; max_x = Double.parseDouble(textXMax.getText());
        min_y = Double.parseDouble(textYMin.getText()); max_y = Double.parseDouble(textYMax.getText());  
    	if(drawdirection.isSelected()) {       		
    		
    		if (useTuple) {    			
    			
    			panel.setCoordinateBounds(min_x ,max_x, max_y,min_y); 
    		}
    		
    		else {
    		
    			panel.setCoordinateBounds(minrow ,maxrow, maxcol,mincol);  
    		}
    	}
    	else
    	{
    		
    		if (useTuple) {
    			panel.setCoordinateBounds(min_x ,max_x, min_y,max_y); 
    		}
    		
    		else {
    		
    			panel.setCoordinateBounds(minrow ,maxrow, mincol,maxcol);    
    		}
    	}
    
    	
    }
    
    
    
    @SuppressWarnings("unchecked")
	public void itemStateChanged(ItemEvent e)
    {
    	
    	
    	Object source = e.getItemSelectable();

        if (source == drawLegend)
        {
            panel.setDrawLegend(e.getStateChange() == ItemEvent.SELECTED);
        }
        else if (source == drawTitle)
        {
            panel.setDrawTitle(e.getStateChange() == ItemEvent.SELECTED);
        }
        else if (source == drawXTitle)
        {
            panel.setDrawXAxisTitle(e.getStateChange() == ItemEvent.SELECTED);
        }
        else if (source == drawXTicks)
        {
            panel.setDrawXTicks(e.getStateChange() == ItemEvent.SELECTED);
        }
        
        else if (source == drawYTitle)
        {
            panel.setDrawYAxisTitle(e.getStateChange() == ItemEvent.SELECTED);
        }
        
        
        else if (source == Zoom)
        {
           if (Zoom.isSelected()) {
        	   panel.app.gui=null;
        	   this.setContentPane(panel.app.getGui());
        	   panel.app.setImage(panel.app.image);  
          	 	this.getContentPane().add(panel.app.getControl(), "Last");
                
          	   scroll_pane = new JScrollPane( listPane);
               scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);              
              
      	         this.getContentPane().add( scroll_pane, BorderLayout.EAST);        
          	 	 //this.getContentPane().add(showpane, BorderLayout.WEST);
                
        	    
        	    this.revalidate();
        	    this.repaint(); 
           }
           
           if(!Zoom.isSelected())  {
        	   	
        	 //  panel.updateData(data, true);
        	   
        	   this.getContentPane().removeAll();
        	   this.revalidate();
        	   this.repaint(); 
        	   
        	 
               scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);              
              //  this.getContentPane().add(showpane, BorderLayout.WEST);      	        
                this.getContentPane().add(panel, BorderLayout.CENTER);      
                this.getContentPane().add( scroll_pane, BorderLayout.EAST);  
        	       
           }
        }   
        
        
        
        else if(source == isMatrix) 
        {
        	if(isMatrix.isSelected()) {
        	labelr.setVisible(true);
			textResolution.setVisible(true);
        	}
        	else
        	{
        		labelr.setVisible(false);
    			textResolution.setVisible(false);
        	}
        }
        else if (source == drawYTicks)
        {
            panel.setDrawYTicks(e.getStateChange() == ItemEvent.SELECTED);
        }
        else if (source == drawdirection) //Tosin added
        {
        	
        	double   min_x, min_y, max_x, max_y;
            //Re-Draw Heatmap
            min_x = Double.parseDouble(textXMin.getText());  ; max_x = Double.parseDouble(textXMax.getText());
            min_y = Double.parseDouble(textYMin.getText()); max_y = Double.parseDouble(textYMax.getText());  
        	if(drawdirection.isSelected()) {       		
        		useGraphicsYAxis = true;          		
        		if (useTuple) {
        			panel.setCoordinateBounds(min_x ,max_x, max_y,min_y); 
        		}
        		
        		else {
        		
        			panel.setCoordinateBounds(minrow ,maxrow, maxcol,mincol);  
        		}
        	}
        	if (!drawdirection.isSelected()) 
        	
        	{
        		useGraphicsYAxis = false;
        		if (useTuple) {
        			panel.setCoordinateBounds(min_x ,max_x, min_y,max_y); 
        		}
        		
        		else {
        		
        			panel.setCoordinateBounds(minrow ,maxrow, mincol,maxcol);    
        		}
        	}
        
        	double[][] new_data;
			try {				
				  new_data = LoadHeatmap.TransformData(data, Transform);
				  panel.updateData(new_data, useGraphicsYAxis);     
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
                  
        }     
        else if (source == drawtadshowall)
        {
        	double[][] new_data = null;
			try {
				new_data = LoadHeatmap.TransformedData( Transform);
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}   //Load already transformed data
			
        	if (drawtadshowall.isSelected()) {
	        	double [][] tad_data = null; int rwstart,rwend;   
	        	
	        	try {
					int [][] TDindex = TADindex(LoadHeatmap.Startlocation);
					// Convert the index to create a square matrix			
					// double[][] new_data = LoadHeatmap.TransformData(default_data , Transform);
					panel.updateData_ext(new_data ,TDindex,useGraphicsYAxis);				
					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}     
	        	
	        	// JOptionPane.showMessageDialog(null, "Task done Successfully!");	
        	}
        	else {
        			// when unchecked 
        		  panel.updateData(new_data, useGraphicsYAxis);       
        	}
        }
        
        else if (source == ShowDataCombo)
        {
        	  double[][] new_data = null; // data to use to draw heatmap
        	 String ix = (String) e.getItem();
             if (e.getStateChange() == ItemEvent.SELECTED)
             {
            	 if (ix.equals("RAW_DATA")) { 
            		 new_data = default_data;  Transform = 0;} 
            	 else if (ix.equals("PEARSON")) { 
             		try {
             			 Transform = 1;
             			 if (pearson_data==null) {
	             			new_data = LoadHeatmap.Pearson(default_data);
	             			pearson_data = new_data;
             			 }
             			 else {
             				 new_data = pearson_data;
             			 }
             				 
             			
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();		}  
             		} 
            	 else if (ix.equals("SPEARMAN")) {
                 	try {
                 		Transform = 2;
                 		if (pearson_data==null) {
                 			new_data = LoadHeatmap.Spearman(default_data);
	             			spearman_data = new_data;
             			 }
             			 else {
             				 new_data = spearman_data;
             			 }
                 		
                 	 	} catch (FileNotFoundException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace(); 		  }   }
            	 else if (ix.equals("TANH")) {
                  	Transform = 3;
					//new_data = (data);
                  	try {
                  	if (tanh_data==null) {
             			new_data = LoadHeatmap.Tanh(default_data);
             			tanh_data = new_data;
         			 }
         			 else {
         				 new_data = data; // data is already in Tanh
         			 }
                  	}
                  	catch (FileNotFoundException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace(); 		  }   
					
					}
            	             	
            		panel.updateData(new_data, useGraphicsYAxis);
             
            		
            }
        	        	
        }
        else
        {
            // must be from the combo box
            Integer ix = (Integer) e.getItem();
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                panel.updateGradient(gradients[ix]);
            }
        }
        
    
     
    }
    
    
 
    
    public static int [] updateindex(double min_x, double max_x,double min_y, double max_y, int loc) {
    	int [] val = new int[4];
    	int locc = ResNumber(ResolutionString(loc));
    	val =  LoadHeatmap.FindIndex( min_x,  max_x, min_y, max_y, locc);
    	return val;
    }
    
      
    // this function will be run from the EDT
    private static void createAndShowGUI() throws Exception
    {
    	
        HeatMapDemo hmd = new HeatMapDemo(); 
        hmd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        hmd.setSize(1200, 800);
        hmd.setVisible(true);
    }

   
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path)
    {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null)
        {
            return new ImageIcon(imgURL);
        }
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    
   
    
    
    /**
     * Class for progressbar to draw 
     * @author Tosin
     *
     */
    public class Draw extends SwingWorker<String,Void>{
    	
    	@Override
    	protected String doInBackground() throws Exception {
    		
    		try{
    			 
    			DrawHeatMap();
    		}catch(Exception ex){
    			ex.printStackTrace();
    			return ex.getMessage();
    		}
    		
    		return "Loading Successful!";
    	}
    }

    
    
    
    
    class ComboBoxRenderer extends JLabel implements ListCellRenderer
    {
        public ComboBoxRenderer()
        {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }
        
        public Component getListCellRendererComponent(
                                                JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus)
        {
            int selectedIndex = ((Integer)value).intValue();
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            ImageIcon icon = icons[selectedIndex];
            setIcon(icon);
            setText(names[selectedIndex].substring(9));
            return this;
        }
    }
    
    /**
     * Renderrer for Show Data for different data format display
     * @author Tosin
     *
     */
    class MyComboBoxRenderer extends JLabel   implements ListCellRenderer {
    	
    	public MyComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
           
        }
    	/*
         * This method finds the data and performs a pearson,spearman 
         * or tanh correlation of Input Data
         *       
         */
    	public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
    	{
    		
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }     
       
        setText(value.toString());
        return this;
    	}
    }
    

    
}
