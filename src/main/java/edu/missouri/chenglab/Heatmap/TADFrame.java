package edu.missouri.chenglab.Heatmap;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jmol.viewer.Viewer;
import org.jmol.api.JmolViewer;
import org.jmol.export.dialog.Dialog;
import org.jmol.export.history.HistoryFile;

import edu.missouri.chenglab.ClusterTAD.ClusterTAD;
import edu.missouri.chenglab.ClusterTAD.Parameter;
import edu.missouri.chenglab.gmol.Constants;
import edu.missouri.chenglab.Structure3DMax.utility.Input;

public class TADFrame {
	
	  static HistoryFile historyFile;

	  public JmolViewer viewer;
	
	    public TADFrame() {
			    	
	    	
	        JTextField inputContactFileField = new JTextField();      
	        
	        JTextField outputGSSFileField = new JTextField();
	               
	        JButton openContactFileButton = new JButton("Browse File");
	        
	        openContactFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					String fileName ;
					JFileChooser fc = new JFileChooser();
		            fc.setCurrentDirectory(new java.io.File("C:/Users"));
		            fc.setDialogTitle("File Browser.");
		            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		            if (fc.showOpenDialog(openContactFileButton) == JFileChooser.APPROVE_OPTION){
		            	inputContactFileField .setText(fc.getSelectedFile().getAbsolutePath());
		            }
					
				
				}
			});
	        
	        
	        JButton outputGSSFileButton = new JButton("Browse File");
	        //openMappingFileButton.setPreferredSize(new Dimension(40, 20));
	        outputGSSFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
		            fc.setCurrentDirectory(new java.io.File("C:/Users"));
		            fc.setDialogTitle("File Browser.");
		            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		            if (fc.showOpenDialog(openContactFileButton) == JFileChooser.APPROVE_OPTION){
		            	outputGSSFileField .setText(fc.getSelectedFile().getAbsolutePath());
		            }
					
					
				}
			});
	        
	        	        	        
	        
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.insets = new Insets(5, 5, 5, 5);
	        
	        JPanel panel = new JPanel(){
	        	@Override
	            public Dimension getPreferredSize() {
	                return new Dimension(450, 350);
	            }	       
	        };	                
	        
	        panel.setLayout(new GridBagLayout());  	        
	        
	        int y = 0;
	        ////////////////////////////////////////////////	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	                
	        panel.add(new JLabel("Input contact file:",JLabel.LEFT), gbc);
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        inputContactFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(inputContactFileField, gbc);
	        
	        	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        panel.add(openContactFileButton, gbc);
	        	       
	                     
	        
	        ///////////////////////////////////////////////
	        y++;	        
	        gbc.gridx = 0;
	        gbc.gridy = y;	  
	        gbc.gridwidth = 1;
	        
	        JLabel resolution =  new JLabel("Data Resolution:",JLabel.LEFT);	
	        resolution.setVisible(false);
	        panel.add(resolution, gbc);	          
	        
	        JTextField IFResolutionField = new JTextField("40000"); 
	        IFResolutionField.setVisible(false);
	        IFResolutionField .addKeyListener(new KeyAdapter(){
	        	@Override
				public void keyReleased(KeyEvent e) {
	        		String currentTxt = IFResolutionField .getText();
					if (currentTxt.length() == 0) return;
					
	        		char chr = currentTxt.charAt(currentTxt.length() - 1);
					
					if ((!Character.isDigit(chr) && chr != '.') || (chr == '.' && currentTxt.substring(0, currentTxt.length() - 1).contains("."))){
						JOptionPane.showMessageDialog(null, "Please key in numbers only, 1000000 = 1MB, 10000 = 10KB");
						
						 IFResolutionField.setText(currentTxt.substring(0, currentTxt.length() - 1));
					}
				}	
	        });
	        
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;	
	        IFResolutionField.setPreferredSize(new Dimension(300, 21));
	        panel.add( IFResolutionField, gbc);
	        	      
	        ////////////////////////////////////////////////
	        y++;
	        gbc.gridx = 0;
	        gbc.gridy = y;	 
	        gbc.gridwidth = 1;
	        panel.add(new JLabel("Output folder:",JLabel.LEFT), gbc);	        
	
	        gbc.gridx = 1;
	        gbc.gridy = y;
	        gbc.gridwidth = 2;
	        outputGSSFileField.setPreferredSize(new Dimension(300, 21));
	        panel.add(outputGSSFileField, gbc);
	        
	        gbc.gridx = 3;
	        gbc.gridy = y;	
	        gbc.gridwidth = 1;
	        panel.add(outputGSSFileButton, gbc);
	
	            
	        
	      ///////////////////////////////////////////////	
			y++;
			JCheckBox isMatrix = new JCheckBox("Input IsMatrix ?");
			gbc.gridx = 0;
			gbc.gridy = y;
			gbc.gridwidth = 2;
			panel.add(isMatrix , gbc);
			isMatrix .addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					if (isMatrix.isSelected()){
						resolution.setVisible(true);
						IFResolutionField.setVisible(true);
					}
					
				}
			});   
	        /////////////////////////////////////////////// 
			
			//y++;
			JCheckBox clusterTAD = new JCheckBox("Run ClusterTAD Algorithm");
			gbc.gridx = 2;
			gbc.gridy = y;
			gbc.gridwidth = 2;
			
			clusterTAD.setSelected(true);
			panel.add(clusterTAD , gbc);
			        	        
	        //////////////////////////////////////////////
	        y++;
	        JButton runButton = new JButton("Run");
	        JButton stopButton = new JButton("Stop");
	       	        	     
	        gbc.gridx = 1;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;	   
	        runButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(runButton, gbc);
	        
	        gbc.gridx = 2;	        
	        gbc.gridy = y;
	        gbc.gridwidth = 1;
	        stopButton.setHorizontalAlignment(JLabel.CENTER);
	        panel.add(stopButton, gbc);
	        	        	        
	        
	        Frame Structure_3DMaxFrame = new JFrame();
	        Structure_3DMaxFrame.setSize(new Dimension(580,210));
	        Structure_3DMaxFrame.setLocation(400, 400);
	        
	        Structure_3DMaxFrame.add(panel);
	        Structure_3DMaxFrame.setVisible(true);
	        
	        
	        
	        
	        runButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {					
					Parameter.stoprunning = false;					
					String input = inputContactFileField.getText();
					String output = outputGSSFileField.getText();		
					int resolution = Integer.parseInt(IFResolutionField.getText());
					String inputdata_type = Parameter.inputtype_Tuple;
					String res = "";
					String algorithm= "";
					String startlocation = "0";
					String[] Input = new String[5];
						
					if (input == null || input.trim().equals("") || output == null || output.trim().equals("") ) {
						JOptionPane.showMessageDialog(null, "Input file or Output path Unspecified or Incorrect, Please make sure these fields are filled correctly !");						
						return;
					}
					
					if (IFResolutionField.isVisible()) {
						if ( resolution > 100000 ) {
							JOptionPane.showMessageDialog(null, "Resolution too High for TAD identification. Minimum Resolution = 100KB ");
							IFResolutionField.setText("50000");
							return;
						}
					}
					
					if (isMatrix.isSelected()){
						inputdata_type = Parameter.inputtype_Matrix;
						res = IFResolutionField.getText();
						
						
					} else {
						try {
							int Res = LoadHeatmap.Resolution(input);
							res = String.valueOf(Res);
							int sloc = LoadHeatmap.Startlocation;
							startlocation = String.valueOf(sloc);
							
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						
						
						resolution = Integer.parseInt(res);
						if ( resolution > 100000 ) {
							JOptionPane.showMessageDialog(null, "Resolution too High for TAD identification. Minimum Resolution = 100KB. Get new datasets ");							
							return;
						}
					}
					 Input[0] = inputContactFileField.getText();
					 Input[1] =  outputGSSFileField.getText();  
					 Input[2] = res;					
					 Input[3] = inputdata_type;			
					 Input[4] = startlocation;
		        	
		        if (!clusterTAD.isSelected()) {
		        		JOptionPane.showMessageDialog(null, "Please select atleast one algorithm");
		        		return;
		        	}
		        
		        if (clusterTAD.isSelected()) {
	        		algorithm = "FindTAD2D";	        		
		        }
		       
		        try{
					
					@SuppressWarnings("unused")
					ClusterTAD ctad = new ClusterTAD(Input);			
					
				}catch(Exception ex){
				    JOptionPane.showMessageDialog(null, "An error Occured!, Check File for Output");				
					ex.printStackTrace();
				}	    	
			    	
				}
			});
	        
	        stopButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
				// set stop running to true					
				Parameter.stoprunning = true;
				JOptionPane.showMessageDialog(null, "TAD Identification Stopped");
				}
			});
	        
	  }
}




