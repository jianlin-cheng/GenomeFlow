//added -hcf
//for genome sequence searching in Ensembl database(currently)

package org.openscience.jmol.app.jmolpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.jmol.api.JmolViewer;
import org.jmol.i18n.GT;

public class SequenceTable extends JDialog {
	JmolViewer viewer;
	private JTable sequenceTable;
	private SequenceTableModel sequenceTableModel;
	JPanel container = new JPanel();
	JButton okButton = new JButton(GT._("OK"));
	JButton cancelButton = new JButton(GT._("Cancel"));
	JTextField spText1 = null, chrText1 = null;
	JTextField fromText1 = null, endText1 = null;
	JTextField fromText2 = null, endText2 = null;
	JTextField fromText3 = null, endText3 = null;
	JTextField fromText4 = null, endText4 = null;
	JTextField fromText5 = null, endText5 = null;
	JTextField fromText6 = null, endText6 = null;
	JTextField fromText7 = null, endText7 = null;
	private ResourceBundle generalEnsemblBundle;
	int speInitialId = 1;
	String spName1 = "";
	String spName = "Bos_taurus";//for initial displaying-ensembl
	int chrInitialId = 0;
	String chrName1 = "";
	String chrName2 = "1";
	String chrName3 = "1";
	String chrName4 = "1";
	String chrName5 = "1";
	String chrName6 = "1";
	String chrName7 = "1";
	BitSet selectedBS = new BitSet();
	int fromPos1 = 0;
	int endPos1 = 1;
	int fromPos2 = 0;
	int endPos2 = 1;
	int fromPos3 = 0;
	int endPos3 = 1;
	int fromPos4 = 0;
	int endPos4 = 1;
	int fromPos5 = 0;
	int endPos5 = 1;
	int fromPos6 = 0;
	int endPos6 = 1;
	int fromPos7 = 0;
	int endPos7 = 1;
	String selectedChrFile = "";
	
	//for local genome database
	String localBaseDIR = "";

	//recording database type: 0-not specified; 1-Local Database; 2-Ensembl Database
	int dbType = 1;
	
	// initial the species hash - include all the ensembl species name/alias and
	// their relationship with sequencetable spChoice.
	private HashMap<String, Integer> initialSp() {
		HashMap<String, Integer> spMap = new HashMap<String, Integer>();
		spMap.put("Anolis carolinensis", 1);
		spMap.put("Anole lizard", 1);
		spMap.put("Anole_lizard", 1);
		spMap.put("Anolis_carolinensis", 1);
		spMap.put("Bos taurus", 2);
		spMap.put("Cow", 2);
		spMap.put("Bos_taurus", 2);
		spMap.put("Caenorhabditis elegans", 3);
		spMap.put("Caenorhabditis_elegans", 3);
		spMap.put("Callithrix jacchus", 4);
		spMap.put("Marmoset", 4);
		spMap.put("Callithrix_jacchus", 4);
		spMap.put("Ciona intestinalis", 6);
		spMap.put("Ciona_intestinalis", 6);
		spMap.put("Danio rerio", 7);
		spMap.put("Zebrafish", 7);
		spMap.put("Danio_rerio", 7);
		spMap.put("Drosophila melanogaster", 8);
		spMap.put("Fruitfly", 8);
		spMap.put("Drosophila_melanogaster", 8);
		spMap.put("Equus caballus", 9);
		spMap.put("Horse", 9);
		spMap.put("Equus_caballus", 9);
		spMap.put("Gallus gallus", 10);
		spMap.put("Chicken", 10);
		spMap.put("Gallus_gallus", 10);
		spMap.put("Gasterosteus aculeatus", 11);
		spMap.put("Stickleback", 11);
		spMap.put("Gasterosteus_aculeatus", 11);
		spMap.put("Homo sapiens", 13);
		spMap.put("Human", 13);
		spMap.put("Homo_sapiens", 13);
		spMap.put("Macaca mulatta", 14);
		spMap.put("Macaque", 14);
		spMap.put("Macaca_mulatta", 14);
		spMap.put("Meleagris gallopavo", 15);
		spMap.put("Turkey", 15);
		spMap.put("Meleagris_gallopavo", 15);
		spMap.put("Monodelphis domestica", 16);
		spMap.put("Opossum", 16);
		spMap.put("Monodelphis_domestica", 16);
		spMap.put("Mus musculus", 17);
		spMap.put("Mouse", 17);
		spMap.put("Mus_musculus", 17);
		spMap.put("Ornithorhynchus anatinus", 18);
		spMap.put("Platypus", 18);
		spMap.put("Ornithorhynchus_anatinus", 18);
		spMap.put("Oryctolagus cuniculus", 19);
		spMap.put("Rabbit", 19);
		spMap.put("Oryctolagus_cuniculus", 19);
		spMap.put("Oryzias latipes", 20);
		spMap.put("Medaka", 20);
		spMap.put("Oryzias_latipes", 20);
		spMap.put("Pan troglodytes", 21);
		spMap.put("Chimpanzee", 21);
		spMap.put("Pan_troglodytes", 21);
		spMap.put("Pongo abelii", 22);
		spMap.put("Orangutan", 22);
		spMap.put("Pongo_abelii", 22);
		spMap.put("Rattus norvegicus", 23);
		spMap.put("Rat", 23);
		spMap.put("Rattus_norvegicus", 23);
		spMap.put("Saccharomyces cerevisiae", 24);
		spMap.put("Saccharomyces_cerevisiae", 24);
		spMap.put("Sus scrofa", 25);
		spMap.put("Pig", 25);
		spMap.put("Sus_scrofa", 25);
		spMap.put("Taeniopygia guttata", 26);
		spMap.put("Zebra Finch", 26);
		spMap.put("Zebra_Finch", 26);
		spMap.put("Taeniopygia_guttata", 26);
		spMap.put("Tetraodon nigroviridis", 27);
		spMap.put("Tetraodon", 27);
		spMap.put("Tetraodon_nigroviridis", 27);
		return spMap;
	}
	
	public SequenceTable(JmolViewer viewer, JFrame parentFrame) {

		super(parentFrame, GT._("GenomeSequenceViewer"), false);
		this.viewer = viewer;

		container.setLayout(new BorderLayout());

		// add first line components
		container.add(constructUpPanel(), BorderLayout.NORTH);
		// add second line components
		JPanel sdPanel = new JPanel();
		sdPanel.setLayout(new BorderLayout());
		container.add(sdPanel, BorderLayout.CENTER);

		// add bottom line components
		container.add(constructBtPanel(), BorderLayout.SOUTH);

		// addWindowListener(new MeasurementListWindowListener());
		getContentPane().add(container);
		pack();
		centerDialog();
	}
	
	class SequenceTableModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	JComponent constructUpPanel() {
		final JPanel upPanel = new JPanel();
		upPanel.setLayout(new BorderLayout());
		
		//for first panel - for local database sequence extracting
		final JPanel fstPanel = new JPanel();
		fstPanel.setLayout(new BorderLayout());
		JLabel spLabel1 = new JLabel(GT._("Species:"));
		spText1 = new JTextField(spName1,10);
		JLabel chrLabel1 = new JLabel(GT._("chrom:"));
		chrText1 = new JTextField(chrName1,5);
		JLabel fromLabel1 = new JLabel(GT._("From:"));
		fromText1 = new JTextField(String.valueOf(fromPos1),7);
		JLabel endLabel1 = new JLabel(GT._("End:"));
		endText1 = new JTextField(String.valueOf(endPos1),8);
		
		JPanel onePanel = new JPanel();
		onePanel.add(spLabel1);
		onePanel.add(spText1);
		spText1.setEditable(false);
		fstPanel.add(onePanel,BorderLayout.NORTH);
		
		JPanel fst1Panel = new JPanel();
		fst1Panel.add(chrLabel1);
		fst1Panel.add(chrText1);
		chrText1.setEditable(false);
		fst1Panel.add(fromLabel1);
		fst1Panel.add(fromText1);
		fromText1.setEditable(false);
		fst1Panel.add(endLabel1);
		fst1Panel.add(endText1);
		endText1.setEditable(false);
		fstPanel.add(fst1Panel,BorderLayout.SOUTH);
		
		//generate second panel - for Ensembl database sequence extracting
		final JPanel secPanel = new JPanel();
		secPanel.setLayout(new BorderLayout());
		
		JLabel spLabel2 = new JLabel(GT._("Species:"));
		final JComboBox spChoice2 = new JComboBox();
		JLabel chrLabel2 = new JLabel(GT._("chrom:"));
		final JComboBox chrChoice2 = new JComboBox();
		JLabel fromLabel2 = new JLabel(GT._("From:"));
		fromText2 = new JTextField(String.valueOf(fromPos2),7);
		JLabel endLabel2 = new JLabel(GT._("End:"));
		endText2 = new JTextField(String.valueOf(endPos2),8);
		
		// generate spcombobox - from properties file - Drop-down box
		// generate chrText - from properties file - Drop-down box
		try {
			String t = "/org/openscience/jmol/app/jmolpanel/Properties/ensembl-genomes.properties";
			generalEnsemblBundle = new PropertyResourceBundle(getClass()
					.getResourceAsStream(t));
			// initial construct
			String[] ensemblSps = tokenize(generalEnsemblBundle
					.getString("species"));

			for (String sp : ensemblSps) {
				spChoice2.addItem(sp);
			}
			spChoice2.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if (ie.getStateChange() == 1) {
						chrChoice2.removeAllItems();
						String[] ensemblChr = tokenize(generalEnsemblBundle
								.getString(ie.getItem().toString()));
						for (String chr : ensemblChr) {
							chrChoice2.addItem(chr);
						}
						spName = (String)spChoice2.getSelectedItem();
					}
				}
			});
			spChoice2.setSelectedIndex(speInitialId);
			
			chrChoice2.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == 1) {
						chrName2 = (String)chrChoice2.getSelectedItem();
					}
				}
			});
			chrChoice2.setSelectedIndex(chrInitialId);
		} catch (IOException ex) {
			throw new RuntimeException(ex.toString());
		}
		
		JPanel sec1Panel = new JPanel();
		sec1Panel.add(spLabel2);
		sec1Panel.add(spChoice2);
		secPanel.add(sec1Panel,BorderLayout.NORTH);
		
		JPanel sec2Panel = new JPanel();
		sec2Panel.add(chrLabel2);
		sec2Panel.add(chrChoice2);
		sec2Panel.add(fromLabel2);
		sec2Panel.add(fromText2);
		sec2Panel.add(endLabel2);
		sec2Panel.add(endText2);
		secPanel.add(sec2Panel,BorderLayout.SOUTH);
		
		//generate third panel - for UCSC Genome database sequence extracting
		final JPanel thrPanel = new JPanel();
		thrPanel.setLayout(new BorderLayout());
		
		JLabel spLabel3 = new JLabel(GT._("Species:"));
		final JComboBox spChoice3 = new JComboBox();
		JLabel chrLabel3 = new JLabel(GT._("chrom:"));
		final JComboBox chrChoice3 = new JComboBox();
		JLabel fromLabel3 = new JLabel(GT._("From:"));
		fromText3 = new JTextField(String.valueOf(fromPos3),7);
		JLabel endLabel3 = new JLabel(GT._("End:"));
		endText3 = new JTextField(String.valueOf(endPos3),8);
		
		// generate spcombobox - from properties file - Drop-down box
		// generate chrText - from properties file - Drop-down box
		try {
			String t2 = "/org/openscience/jmol/app/jmolpanel/Properties/ensembl-genomes.properties";
			generalEnsemblBundle = new PropertyResourceBundle(getClass()
					.getResourceAsStream(t2));
			// initial construct
			String[] ensemblSps2 = tokenize(generalEnsemblBundle
					.getString("species"));

			for (String sp2 : ensemblSps2) {
				spChoice3.addItem(sp2);
			}
			spChoice3.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if (ie.getStateChange() == 1) {
						chrChoice3.removeAllItems();
						String[] ensemblChr = tokenize(generalEnsemblBundle
								.getString(ie.getItem().toString()));
						for (String chr : ensemblChr) {
							chrChoice3.addItem(chr);
						}
						spName = (String)spChoice3.getSelectedItem();
					}
				}
			});
			spChoice3.setSelectedIndex(speInitialId);
			
			chrChoice3.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == 1) {
						chrName3 = (String)chrChoice3.getSelectedItem();
					}
				}
			});
			chrChoice3.setSelectedIndex(chrInitialId);
		} catch (IOException ex) {
			throw new RuntimeException(ex.toString());
		}
		
		JPanel thr1Panel = new JPanel();
		thr1Panel.add(spLabel3);
		thr1Panel.add(spChoice3);
		thrPanel.add(thr1Panel,BorderLayout.NORTH);
		
		
		JPanel thr2Panel = new JPanel();
		thr2Panel.add(chrLabel3);
		thr2Panel.add(chrChoice3);
		thr2Panel.add(fromLabel3);
		thr2Panel.add(fromText3);
		thr2Panel.add(endLabel3);
		thr2Panel.add(endText3);
		thrPanel.add(thr2Panel,BorderLayout.SOUTH);

		//generate forth panel - for NCBI BLast
		final JPanel forthPanel = new JPanel();
		forthPanel.setLayout(new BorderLayout());
		
		JLabel spLabel4 = new JLabel(GT._("Species:"));
		final JComboBox spChoice4 = new JComboBox();
		JLabel chrLabel4 = new JLabel(GT._("chrom:"));
		final JComboBox chrChoice4 = new JComboBox();
		JLabel fromLabel4 = new JLabel(GT._("From:"));
		fromText4 = new JTextField(String.valueOf(fromPos4),7);
		JLabel endLabel4 = new JLabel(GT._("End:"));
		endText4 = new JTextField(String.valueOf(endPos4),8);
		
		// generate spcombobox - from properties file - Drop-down box
		// generate chrText - from properties file - Drop-down box
		try {
			String t2 = "/org/openscience/jmol/app/jmolpanel/Properties/ensembl-genomes.properties";
			generalEnsemblBundle = new PropertyResourceBundle(getClass()
					.getResourceAsStream(t2));
			// initial construct
			String[] ensemblSps2 = tokenize(generalEnsemblBundle
					.getString("species"));

			for (String sp2 : ensemblSps2) {
				spChoice4.addItem(sp2);
			}
			spChoice4.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if (ie.getStateChange() == 1) {
						chrChoice4.removeAllItems();
						String[] ensemblChr = tokenize(generalEnsemblBundle
								.getString(ie.getItem().toString()));
						for (String chr : ensemblChr) {
							chrChoice4.addItem(chr);
						}
						spName = (String)spChoice4.getSelectedItem();
					}
				}
			});
			spChoice4.setSelectedIndex(speInitialId);
			
			chrChoice4.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == 1) {
						chrName4 = (String)chrChoice4.getSelectedItem();
					}
				}
			});
			chrChoice4.setSelectedIndex(chrInitialId);
		} catch (IOException ex) {
			throw new RuntimeException(ex.toString());
		}
		
		JPanel forth1Panel = new JPanel();
		forth1Panel.add(spLabel4);
		forth1Panel.add(spChoice4);
		forthPanel.add(forth1Panel,BorderLayout.NORTH);
		
		
		JPanel forth2Panel = new JPanel();
		forth2Panel.add(chrLabel4);
		forth2Panel.add(chrChoice4);
		forth2Panel.add(fromLabel4);
		forth2Panel.add(fromText4);
		forth2Panel.add(endLabel4);
		forth2Panel.add(endText4);
		forthPanel.add(forth2Panel,BorderLayout.SOUTH);
		
		//generate fifth panel - for NCBI BLast
		final JPanel fifthPanel = new JPanel();
		fifthPanel.setLayout(new BorderLayout());
		
		JLabel spLabel5 = new JLabel(GT._("Species:"));
		final JComboBox spChoice5 = new JComboBox();
		JLabel chrLabel5 = new JLabel(GT._("chrom:"));
		final JComboBox chrChoice5 = new JComboBox();
		JLabel fromLabel5 = new JLabel(GT._("From:"));
		fromText5 = new JTextField(String.valueOf(fromPos5),7);
		JLabel endLabel5 = new JLabel(GT._("End:"));
		endText5 = new JTextField(String.valueOf(endPos5),8);
		
		// generate spcombobox - from properties file - Drop-down box
		// generate chrText - from properties file - Drop-down box
		try {
			String t2 = "/org/openscience/jmol/app/jmolpanel/Properties/ensembl-genomes.properties";
			generalEnsemblBundle = new PropertyResourceBundle(getClass()
					.getResourceAsStream(t2));
			// initial construct
			String[] ensemblSps2 = tokenize(generalEnsemblBundle
					.getString("species"));

			for (String sp2 : ensemblSps2) {
				spChoice5.addItem(sp2);
			}
			spChoice5.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if (ie.getStateChange() == 1) {
						chrChoice5.removeAllItems();
						String[] ensemblChr = tokenize(generalEnsemblBundle
								.getString(ie.getItem().toString()));
						for (String chr : ensemblChr) {
							chrChoice5.addItem(chr);
						}
						spName = (String)spChoice5.getSelectedItem();
					}
				}
			});
			spChoice5.setSelectedIndex(speInitialId);
			
			chrChoice5.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == 1) {
						chrName5 = (String)chrChoice5.getSelectedItem();
					}
				}
			});
			chrChoice5.setSelectedIndex(chrInitialId);
		} catch (IOException ex) {
			throw new RuntimeException(ex.toString());
		}
		
		JPanel fifth1Panel = new JPanel();
		fifth1Panel.add(spLabel5);
		fifth1Panel.add(spChoice5);
		fifthPanel.add(fifth1Panel,BorderLayout.NORTH);
		
		
		JPanel fifth2Panel = new JPanel();
		fifth2Panel.add(chrLabel5);
		fifth2Panel.add(chrChoice5);
		fifth2Panel.add(fromLabel5);
		fifth2Panel.add(fromText5);
		fifth2Panel.add(endLabel5);
		fifth2Panel.add(endText5);
		fifthPanel.add(fifth2Panel,BorderLayout.SOUTH);
		
		
		
		//generate sixth panel - for Sequence Properties 
		final JPanel sixthPanel = new JPanel();
		sixthPanel.setLayout(new BorderLayout());
		
		JLabel spLabel6 = new JLabel(GT._("Species:"));
		final JComboBox spChoice6 = new JComboBox();
		JLabel chrLabel6 = new JLabel(GT._("chrom:"));
		final JComboBox chrChoice6 = new JComboBox();
		JLabel fromLabel6 = new JLabel(GT._("From:"));
		fromText6 = new JTextField(String.valueOf(fromPos6),7);
		JLabel endLabel6 = new JLabel(GT._("End:"));
		endText6 = new JTextField(String.valueOf(endPos6),8);
		
		// generate spcombobox - from properties file - Drop-down box
		// generate chrText - from properties file - Drop-down box
		try {
			String t2 = "/org/openscience/jmol/app/jmolpanel/Properties/ensembl-genomes.properties";
			generalEnsemblBundle = new PropertyResourceBundle(getClass()
					.getResourceAsStream(t2));
			// initial construct
			String[] ensemblSps2 = tokenize(generalEnsemblBundle
					.getString("species"));

			for (String sp2 : ensemblSps2) {
				spChoice6.addItem(sp2);
			}
			spChoice6.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if (ie.getStateChange() == 1) {
						chrChoice6.removeAllItems();
						String[] ensemblChr = tokenize(generalEnsemblBundle
								.getString(ie.getItem().toString()));
						for (String chr : ensemblChr) {
							chrChoice6.addItem(chr);
						}
						spName = (String)spChoice6.getSelectedItem();
					}
				}
			});
			spChoice6.setSelectedIndex(speInitialId);
			
			chrChoice6.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == 1) {
						chrName6 = (String)chrChoice6.getSelectedItem();
					}
				}
			});
			chrChoice6.setSelectedIndex(chrInitialId);
		} catch (IOException ex) {
			throw new RuntimeException(ex.toString());
		}
		
		JPanel sixth1Panel = new JPanel();
		sixth1Panel.add(spLabel6);
		sixth1Panel.add(spChoice6);
		sixthPanel.add(sixth1Panel,BorderLayout.NORTH);
		
		
		JPanel sixth2Panel = new JPanel();
		sixth2Panel.add(chrLabel6);
		sixth2Panel.add(chrChoice6);
		sixth2Panel.add(fromLabel6);
		sixth2Panel.add(fromText6);
		sixth2Panel.add(endLabel6);
		sixth2Panel.add(endText6);
		sixthPanel.add(sixth2Panel,BorderLayout.SOUTH);

		//generate seventh panel - for Exon and Inxons 
		final JPanel seventhPanel = new JPanel();
		seventhPanel.setLayout(new BorderLayout());
		
		JLabel spLabel7 = new JLabel(GT._("Species:"));
		final JComboBox spChoice7 = new JComboBox();
		JLabel chrLabel7 = new JLabel(GT._("chrom:"));
		final JComboBox chrChoice7 = new JComboBox();
		JLabel fromLabel7 = new JLabel(GT._("From:"));
		fromText7 = new JTextField(String.valueOf(fromPos7),7);
		JLabel endLabel7 = new JLabel(GT._("End:"));
		endText7 = new JTextField(String.valueOf(endPos7),8);
		
		// generate spcombobox - from properties file - Drop-down box
		// generate chrText - from properties file - Drop-down box
		try {
			String t2 = "/org/openscience/jmol/app/jmolpanel/Properties/ensembl-genomes.properties";
			generalEnsemblBundle = new PropertyResourceBundle(getClass()
					.getResourceAsStream(t2));
			// initial construct
			String[] ensemblSps2 = tokenize(generalEnsemblBundle
					.getString("species"));

			for (String sp2 : ensemblSps2) {
				spChoice7.addItem(sp2);
			}
			spChoice7.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if (ie.getStateChange() == 1) {
						chrChoice7.removeAllItems();
						String[] ensemblChr = tokenize(generalEnsemblBundle
								.getString(ie.getItem().toString()));
						for (String chr : ensemblChr) {
							chrChoice7.addItem(chr);
						}
						spName = (String)spChoice7.getSelectedItem();
					}
				}
			});
			spChoice7.setSelectedIndex(speInitialId);
			
			chrChoice7.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == 1) {
						chrName7 = (String)chrChoice7.getSelectedItem();
					}
				}
			});
			chrChoice7.setSelectedIndex(chrInitialId);
		} catch (IOException ex) {
			throw new RuntimeException(ex.toString());
		}
		
		JPanel seventh1Panel = new JPanel();
		seventh1Panel.add(spLabel7);
		seventh1Panel.add(spChoice7);
		seventhPanel.add(seventh1Panel,BorderLayout.NORTH);
		
		
		JPanel seventh2Panel = new JPanel();
		seventh2Panel.add(chrLabel7);
		seventh2Panel.add(chrChoice7);
		seventh2Panel.add(fromLabel7);
		seventh2Panel.add(fromText7);
		seventh2Panel.add(endLabel7);
		seventh2Panel.add(endText7);
		seventhPanel.add(seventh2Panel,BorderLayout.SOUTH);

		final JPanel resPanel = new JPanel();
		final JLabel resourceLabel = new JLabel(GT._("Choose Function:"));
		JComboBox resourceChoice = new JComboBox();
		resourceChoice.addItem(GT._("Local_Database_Sequence_Search"));
		resourceChoice.addItem(GT._("Ensembl_Sequence_Search"));
		resourceChoice.addItem(GT._("UCSC_Genome_Browser_Sequence_Search"));
		resourceChoice.addItem(GT._("NCBI_Blast"));
		resourceChoice.addItem(GT._("Sequence_Transcribe"));
		resourceChoice.addItem(GT._("Sequence_Properties"));
		resourceChoice.addItem(GT._("Exons_Introns"));
		resPanel.add(resourceLabel);
		resPanel.add(resourceChoice);
		upPanel.add(resPanel,BorderLayout.NORTH);

		upPanel.add(fstPanel);

		resourceChoice.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				if (ie.getStateChange() == 1) {
					if (ie.getItem().toString().equals("Ensembl_Sequence_Search")) {
						upPanel.remove(fstPanel);
						upPanel.remove(thrPanel);
						upPanel.remove(forthPanel);
						upPanel.remove(fifthPanel);
						upPanel.remove(sixthPanel);
						upPanel.remove(seventhPanel);
						upPanel.add(secPanel);
						upPanel.repaint();
						upPanel.revalidate();
						dbType = 2;
					}
					else if (ie.getItem().toString().equals("Local_Database_Sequence_Search")) {
						localBaseDIR = viewer.getLocalSeqPath();
						upPanel.remove(secPanel);
						upPanel.remove(thrPanel);
						upPanel.remove(forthPanel);
						upPanel.remove(fifthPanel);
						upPanel.remove(sixthPanel);
						upPanel.remove(seventhPanel);
						upPanel.add(fstPanel);
						upPanel.repaint();
						upPanel.revalidate();
						dbType = 1;
					}
					else if (ie.getItem().toString().equals("UCSC_Genome_Browser_Sequence_Search")) {
						localBaseDIR = viewer.getLocalSeqPath();
						upPanel.remove(fstPanel);
						upPanel.remove(secPanel);
						upPanel.remove(forthPanel);
						upPanel.remove(fifthPanel);
						upPanel.remove(sixthPanel);
						upPanel.remove(seventhPanel);
						upPanel.add(thrPanel);
						upPanel.repaint();
						upPanel.revalidate();
						dbType = 3;
					}					
					else if (ie.getItem().toString().equals("NCBI_Blast")) {
						localBaseDIR = viewer.getLocalSeqPath();
						upPanel.remove(fstPanel);
						upPanel.remove(secPanel);
						upPanel.remove(thrPanel);
						upPanel.remove(fifthPanel);
						upPanel.remove(sixthPanel);
						upPanel.remove(seventhPanel);
						upPanel.add(forthPanel);
						upPanel.repaint();
						upPanel.revalidate();
						dbType = 4;
					}
					else if (ie.getItem().toString().equals("Sequence_Transcribe")) {
						localBaseDIR = viewer.getLocalSeqPath();
						upPanel.remove(fstPanel);
						upPanel.remove(secPanel);
						upPanel.remove(thrPanel);
						upPanel.remove(forthPanel);
						upPanel.remove(sixthPanel);
						upPanel.remove(seventhPanel);
						upPanel.add(fifthPanel);
						upPanel.repaint();
						upPanel.revalidate();
						dbType = 5;
					}
					else if (ie.getItem().toString().equals("Sequence_Properties")) {
						localBaseDIR = viewer.getLocalSeqPath();
						upPanel.remove(fstPanel);
						upPanel.remove(secPanel);
						upPanel.remove(thrPanel);
						upPanel.remove(forthPanel);
						upPanel.remove(fifthPanel);
						upPanel.remove(seventhPanel);
						upPanel.add(sixthPanel);
						upPanel.repaint();
						upPanel.revalidate();
						dbType = 6;
					}
					else if (ie.getItem().toString().equals("Exons_Introns")) {
						localBaseDIR = viewer.getLocalSeqPath();
						upPanel.remove(fstPanel);
						upPanel.remove(secPanel);
						upPanel.remove(thrPanel);
						upPanel.remove(forthPanel);
						upPanel.remove(fifthPanel);
						upPanel.remove(sixthPanel);
						upPanel.add(seventhPanel);
						upPanel.repaint();
						upPanel.revalidate();
						dbType = 7;
					}
				}
			}
		});
		
		return upPanel;
	}
	
	
	//construct the initial panel for local sequence - to use local database, user need to load and select one unit
	

	// refresh info based on unit selected
	void updateSequenceTableData() {
		okButton.setEnabled(true);
		selectedBS = viewer.getSelectionSet(false);
		// generate species,chrom,from,end
		if (selectedBS.cardinality() == 1) {
			int selectedUnitIndex = 0;
			for (int i = 0; i < selectedBS.length(); i++) {
				if (selectedBS.get(i)) {
					selectedUnitIndex = i;
				}
			}
			String selectedUnitInfo = viewer.getAtomInfo(selectedUnitIndex,true);//returned info: speceis name, local chrom number, ensembl chrom number, genome sequence from, genome sequence end, local sequence filename
			
			//initial sp name - local
			String lcSpName = findSpLc(selectedUnitInfo);
			spName1 = lcSpName;
			
			// initial sp name - Ensembl
			int selectedSpId = findSpEns(selectedUnitInfo);
			if (selectedSpId > 0) {
				setInitialSp(selectedSpId - 1);
			} else {
				setInitialSp(2);
			}
			
			//initial chr Num - local
			String selectedChrLc = findChrLc(selectedUnitInfo);
			chrName1 = selectedChrLc;
			
			//initial local database file name
			selectedChrFile = findLcSeqFile(selectedUnitInfo);
			
			
			// initial chr Num -Ensembl & local
			String selectedChr = findChrEns(selectedUnitInfo);
			if (isNumeric(selectedChr)) {
				try {
					setInitialChr(Integer.parseInt(selectedChr) - 1);
				} finally {
					// no action
				}
			} else {
				setInitialChr(1);
			}
			
			//initial frompos and endpos
			fromPos1 = findFR(selectedUnitInfo);
			endPos1 = findEN(selectedUnitInfo);
			fromPos2 = fromPos1;
			endPos2 = endPos1;
			fromPos3 = fromPos1;
			endPos3 = endPos1;
			fromPos4 = fromPos1;
			endPos4 = endPos1;
			fromPos5 = fromPos1;
			endPos5 = endPos1;
			fromPos6 = fromPos1;
			endPos6 = endPos1;
			fromPos7 = fromPos1;
			endPos7 = endPos1;
			
			//update panel-fstPanel && secPanel
			container.removeAll();
			container.setLayout(new BorderLayout());
			container.add(constructUpPanel(), BorderLayout.NORTH);
			JPanel sdPanel = new JPanel();
			sdPanel.setLayout(new BorderLayout());
			container.add(sdPanel, BorderLayout.CENTER);
			container.add(constructBtPanel(), BorderLayout.SOUTH);
			
			pack();
			centerDialog();
			//sequenceTable.fire
		}

	}
	
	//find species name- local
	private String findSpLc(String unitInfo) {
		String[] unitSeria = unitInfo.split(",");
		String spName = unitSeria[0];
		return spName;
	}
	
	// find default species - ensembl
	private int findSpEns(String unitInfo) {
		String[] unitSeria = unitInfo.split(",");
		String spName = unitSeria[0];
		HashMap<String, Integer> spMap = initialSp();
		Object spId = null;

		Iterator iter = spMap.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object value = spMap.get(key);
			if (key.toString().toUpperCase().equals(spName.toUpperCase())) {
				spId = value;
				break;
			}
		}
		if (spId == null) {
			spId = 0;
		}
		return (Integer) spId;
	}
	
	//find default chr-local
	private String findChrLc(String unitInfo) {
		String[] unitSeria = unitInfo.split(",");
		String chrNum = unitSeria[1];
		return chrNum;
	}
	//find default sequence file name
	private String findLcSeqFile(String unitInfo) {
		String[] unitSeria = unitInfo.split(",");
		String chrNum = unitSeria[5];
		return chrNum;
	}
	
	// find default chr-ensembl
	private String findChrEns(String unitInfo) {
		String[] unitSeria = unitInfo.split(",");
		String chrNum = unitSeria[2];
		return chrNum;
	}

	//find default frompos - both
	private int findFR (String unitInfo) {
		String[] unitSeria = unitInfo.split(",");
		int fromPos = Integer.parseInt(unitSeria[3]);
		return fromPos;
	}
	
	//find default endpos - both
	private int findEN (String unitInfo) {
		String[] unitSeria = unitInfo.split(",");
		int endPos = Integer.parseInt(unitSeria[4]);
		return endPos;
	}
	
	private boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Take the given string and chop it up into a series of strings on
	 * whitespace boundries. This is useful for trying to get an array of
	 * strings out of the resource file.
	 * 
	 * @param input
	 *            String to chop
	 * @return Strings chopped on whitespace boundaries
	 */
	protected String[] tokenize(String input) {
		List<String> v = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(input);
		while (t.hasMoreTokens())
			v.add(t.nextToken());
		return v.toArray(new String[v.size()]);
	}

	JComponent constructBtPanel() {
		JPanel btPanel = new JPanel();
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//chrName1 = chromNum.getText();
				String fromPosString1 = fromText1.getText();
				String endPosString1  = endText1.getText();
				String fromPosString2 = fromText2.getText();
				String endPosString2  = endText2.getText();
				String fromPosString3 = fromText3.getText();
				String endPosString3  = endText3.getText();
				String fromPosString4 = fromText4.getText();
				String endPosString4  = endText4.getText();
				String fromPosString5 = fromText5.getText();
				String endPosString5  = endText5.getText();
				String fromPosString6 = fromText6.getText();
				String endPosString6  = endText6.getText();
				String fromPosString7 = fromText7.getText();
				String endPosString7  = endText7.getText();
				
				if (dbType == 2 && isNumeric(fromPosString2) && isNumeric(endPosString2)) {
					String getSeqScript = "getseqEnsembl" + " \"" + spName + "\"" + "," + chrName2 + "," + fromPosString2 + "," + endPosString2;
					viewer.script(getSeqScript);
					close();
					//reset the database to local
					dbType = 1;				
				}
				else if (dbType == 3 && isNumeric(fromPosString3) && isNumeric(endPosString3)) {
					String getSeqScript = "getseqUCSCGB" + " \"" + spName + "\"" + "," + chrName3 + "," + fromPosString3 + "," + endPosString3;
					viewer.script(getSeqScript);
					close();
					//reset the database to local
					dbType = 1;				
				}
				else if (dbType == 4 && isNumeric(fromPosString4) && isNumeric(endPosString4)) {
					String getSeqScript = "getseqBlast" + " \"" + spName + "\"" + "," + chrName4 + "," + fromPosString4 + "," + endPosString4;
					viewer.script(getSeqScript);
					close();
					//reset the database to local
					dbType = 1;				
				}
				else if (dbType == 5 && isNumeric(fromPosString5) && isNumeric(endPosString5)) {
					String getSeqScript = "getseqTranscribe" + " \"" + spName + "\"" + "," + chrName5 + "," + fromPosString5 + "," + endPosString5;
					viewer.script(getSeqScript);
					close();
					//reset the database to local
					dbType = 1;				
				}
				else if (dbType == 6 && isNumeric(fromPosString6) && isNumeric(endPosString6)) {
					String getSeqScript = "getseqProperties" + " \"" + spName + "\"" + "," + chrName6 + "," + fromPosString6 + "," + endPosString6;
					viewer.script(getSeqScript);
					close();
					//reset the database to local
					dbType = 1;				
				}
				else if (dbType == 7 && isNumeric(fromPosString7) && isNumeric(endPosString7)) {
					String getSeqScript = "getseqGene" + " \"" + spName + "\"" + "," + chrName7 + "," + fromPosString7 + "," + endPosString7;
					viewer.script(getSeqScript);
					close();
					//reset the database to local
					dbType = 1;				
				}
				else if (dbType == 1 && chrName1 != "" && isNumeric(fromPosString1) && isNumeric(endPosString1)) {
					String getSeqScript = "getseqLocal" + " \"" + spName + "\"" + "," + chrName1 + "," + fromPosString1 + "," + endPosString1 + "," + "\"" + selectedChrFile + "\"";
					viewer.script(getSeqScript);
					close();
				}				
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		btPanel.setLayout(new FlowLayout());
		btPanel.add(okButton);
		btPanel.add(cancelButton);

		return btPanel;
	}
	
	
	private void cleanData() {
		spText1 = null;
		chrText1 = null;
		fromText1 = null;
		endText1 = null;
		fromText2 = null;
		endText2 = null;
		fromText3 = null;
		endText3 = null;
		fromText4 = null;
		endText4 = null;
		fromText5 = null;
		endText5 = null;
		fromText6 = null;
		endText6 = null;
		fromText7 = null;
		endText7 = null;
		generalEnsemblBundle = null;
		speInitialId = 1;
		spName1 = "";
		spName = "Bos_taurus";
		chrInitialId = 0;
		chrName1 = "";
		chrName2 = "1";
		chrName3 = "1";
		chrName4 = "1";
		chrName5 = "1";
		chrName6 = "1";
		chrName7 = "1";
		fromPos1 = 0;
		endPos1 = 1;
		fromPos2 = 0;
		endPos2 = 1;
		fromPos3 = 0;
		endPos3 = 1;
		fromPos4 = 0;
		endPos4 = 1;
		fromPos5 = 0;
		endPos5 = 1;
		fromPos6 = 0;
		endPos6 = 1;
		fromPos7 = 0;
		endPos7 = 1;
		selectedChrFile = "";
	}
	
	public void close() {
		this.setVisible(false);
		cleanData();
	}

	public void activate() {
		updateSequenceTableData();
		setVisible(true);
		System.out.println("Yes");
	}

	protected void centerDialog() {
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

	private void setInitialSp(int i) {
		if (i < 0)
			return;
		this.speInitialId = i;
	}

	private void setInitialChr(int i) {
		if (i < 0)
			return;
		this.chrInitialId = i;
	}

	
}
