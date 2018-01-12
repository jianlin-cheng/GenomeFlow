/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2012-09-12 06:45:51 -0500 (Wed, 12 Sep 2012) $
 * $Revision: 17557 $
 *
 * Copyright (C) 2003-2006  Miguel, Jmol Development, www.jmol.org
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
package org.jmol.script;

import static org.biojava3.ws.alignment.qblast.BlastOutputParameterEnum.FORMAT_TYPE;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.io.util.IOUtils;
import org.biojava3.ws.alignment.qblast.BlastOutputFormatEnum;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;
import org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastService;
import org.jmol.api.MinimizerInterface;
import org.jmol.api.SymmetryInterface;
import org.jmol.atomdata.RadiusData;
import org.jmol.atomdata.RadiusData.EnumType;
import org.jmol.constant.EnumAnimationMode;
import org.jmol.constant.EnumPalette;
import org.jmol.constant.EnumStereoMode;
import org.jmol.constant.EnumStructure;
import org.jmol.constant.EnumVdw;
import org.jmol.i18n.GT;
import org.jmol.modelset.Atom;
import org.jmol.modelset.AtomCollection;
import org.jmol.modelset.Bond;
import org.jmol.modelset.Bond.BondSet;
import org.jmol.modelset.Group;
import org.jmol.modelset.LabelToken;
import org.jmol.modelset.MeasurementData;
import org.jmol.modelset.ModelCollection;
import org.jmol.modelset.ModelCollection.StateScript;
import org.jmol.modelset.ModelSet;
import org.jmol.modelset.TickInfo;
import org.jmol.shape.MeshCollection;
import org.jmol.shape.Object2d;
import org.jmol.shape.Shape;
import org.jmol.util.ArrayUtil;
import org.jmol.util.BitSetUtil;
import org.jmol.util.BoxInfo;
import org.jmol.util.Colix;
import org.jmol.util.ColorEncoder;
import org.jmol.util.ColorUtil;
import org.jmol.util.Elements;
import org.jmol.util.Escape;
import org.jmol.util.GData;
import org.jmol.util.JmolEdge;
import org.jmol.util.JmolFont;
import org.jmol.util.Logger;
import org.jmol.util.Measure;
import org.jmol.util.MeshSurface;
import org.jmol.util.Parser;
import org.jmol.util.Point3fi;
import org.jmol.util.Quaternion;
import org.jmol.util.SimpleUnitCell;
import org.jmol.util.TextFormat;
import org.jmol.util.ZipUtil;
import org.jmol.viewer.ActionManager;
import org.jmol.viewer.FileManager;
import org.jmol.viewer.JmolConstants;
import org.jmol.viewer.PropertyManager;
import org.jmol.viewer.ShapeManager;
import org.jmol.viewer.StateManager;
import org.jmol.viewer.Viewer;
import org.jmol.viewer.Viewer.ACCESS;

import edu.missouri.chenglab.ClusterTAD.ClusterTAD;
import edu.missouri.chenglab.Heatmap.HeatMapDemo;
import edu.missouri.chenglab.gmol.Constants;
import edu.missouri.chenglab.gmol.annotation.Annotator;
import edu.missouri.chenglab.gmol.filemodification.ConvertPDB2GSS;
import edu.missouri.chenglab.gmol.modelcomparison.Comparison;
import edu.missouri.chenglab.loopdetection.Detector;
import edu.missouri.chenglab.loopdetection.utility.CommonFunctions;
import edu.missouri.chenglab.lordg.noisy_mds.StructureGeneratorLorentz_HierarchicalModeling;
import edu.missouri.chenglab.lordg.utility.Helper;
import edu.missouri.chenglab.lordg.valueObject.GenomicLocation;
import edu.missouri.chenglab.lordg.valueObject.InputParameters;

//for gene sequence
import uk.ac.roslin.ensembl.config.DBConnection.DataSource;
import uk.ac.roslin.ensembl.dao.database.DBRegistry;
import uk.ac.roslin.ensembl.dao.database.DBSpecies;
import uk.ac.roslin.ensembl.exception.ConfigurationException;
import uk.ac.roslin.ensembl.exception.DAOException;
import uk.ac.roslin.ensembl.model.core.Chromosome;


//Tosin

import edu.missouri.chenglab.Heatmap.HeatMapDemo;
import edu.missouri.chenglab.Structure3DMax.algorithm.StructureGenerator3DMax;
import edu.missouri.chenglab.Structure3DMax.valueObject.InputParameters_3DMax;
import edu.missouri.chenglab.ClusterTAD.*;
import  edu.missouri.chenglab.compareTAD.TADComparison;

public class ScriptEvaluator {

	/*
	 * The ScriptEvaluator class, the Viewer, the xxxxManagers, the Graphics3D
	 * rendering engine, the ModelSet and Shape classes, and the Adapter file
	 * reader classes form the core of the Jmol molecular visualization
	 * framework.
	 * 
	 * The ScriptEvaluator has just a few entry points, which you will find
	 * immediately following this comment. They include:
	 * 
	 * public boolean compileScriptString(String script, boolean tQuiet)
	 * 
	 * public boolean compileScriptFile(String filename, boolean tQuiet)
	 * 
	 * public void evaluateCompiledScript(boolean isCmdLine_c_or_C_Option,
	 * boolean isCmdLine_C_Option, boolean historyDisabled, boolean
	 * listCommands)
	 * 
	 * Essentially ANYTHING can be done using these three methods. A variety of
	 * other methods are available via Viewer, which is the the true portal to
	 * Jmol (via the JmolViewer interface) for application developers who want
	 * faster, more direct processing.
	 * 
	 * A little Jmol history:
	 * 
	 * General history notes can be found at our ConfChem paper, which can be
	 * found at
	 * http://chemapps.stolaf.edu/jmol/presentations/confchem2006/jmol-confchem
	 * .htm
	 * 
	 * This ScriptEvaluator class was initially written by Michael (Miguel)
	 * Howard as Eval.java as an efficient means of reproducing the RasMol
	 * scripting language for Jmol. Key additions there included:
	 * 
	 * - tokenization of commands via the Compiler class (now ScriptCompiler and
	 * ScriptCompilationTokenParser) - ScriptException error handling - a
	 * flexible yet structured command parameter syntax - implementations of
	 * RasMol secondary structure visualizations - isosurfaces, dots, labels,
	 * polyhedra, draw, stars, pmesh, more
	 * 
	 * Other Miguel contributions include:
	 * 
	 * - the structural bases of the Adapter, ModelSet, and ModelSetBio classes
	 * - creation of Manager classes - absolutely amazing raw pixel bitmap
	 * rendering code (org.jmol.g3d) - popup context menu - inline model loading
	 * 
	 * Bob Hanson (St. Olaf College) found out about Jmol during the spring of
	 * 2004. After spending over a year working on developing online interactive
	 * documentation, he started actively writing code early in 2006. During the
	 * period 2006-2009 Bob completely reworked the script processor (and much
	 * of the rest of Jmol) to handle a much broader range of functionality.
	 * Notable improvements include:
	 * 
	 * - display/hide commands - dipole, ellipsoid, geosurface, lcaoCartoon
	 * visualizations - quaternion and ramachandran commands - much expanded
	 * isosurface / draw commands - configuration, disorder, and biomolecule
	 * support - broadly 2D- and 3D-positionable echos - translateSelected and
	 * rotateSelected commands - getProperty command, providing access to more
	 * file information - data and write commands - writing of high-resolution
	 * JPG, PNG, and movie-sequence JPG - generalized export to Maya and PovRay
	 * formats
	 * 
	 * - multiple file loading, including trajectories - minimization using the
	 * Universal Force Field (UFF) - atom/model deletion and addition - direct
	 * loading of properties such as partial charge or coordinates - several new
	 * file readers, including manifested zip file reading - default directory,
	 * CD command, and pop-up file open/save dialogs
	 * 
	 * - "internal" molecular coordinate-based rotations - full support for
	 * crystallographic formats, including space groups, symmetry, unit cells,
	 * and fractional coordinates - support for point groups and molecular
	 * symmetry - navigation mode - antialiasing of display and imaging -
	 * save/restore/write exact Jmol state - JVXL file format for compressed
	 * rapid generation of isosurfaces
	 * 
	 * - user-defined variables - addition of a Reverse Polish Notation (RPN)
	 * expression processor - extension of the RPN processor to user variables -
	 * user-defined functions - flow control commands if/else/endif, for, and
	 * while - JavaScript/Java-like brace syntax - key stroke-by-key stroke
	 * command syntax checking - integrated help command - user-definable popup
	 * menu - language switching
	 * 
	 * - fully functional signed applet - applet-applet synchronization,
	 * including two-applet geoWall stereo rendering - JSON format for property
	 * delivery to JavaScript - jmolScriptWait, dual-threaded queued JavaScript
	 * scripting interface - extensive callback development - script editor
	 * panel (work in progress, June 2009)
	 * 
	 * Several other people have contributed. Perhaps they will not be too shy
	 * to add their claim to victory here. Please add your contributions.
	 * 
	 * - Jmol application (Egon Willighagen) - smiles support (Nico Vervelle) -
	 * readers (Rene Kanter, Egon, several others) - initial VRML export work
	 * (Nico Vervelle) - WebExport (Jonathan Gutow) - internationalization
	 * (Nico, Egon, Angel Herriez) - Jmol Wiki and user guide book (Angel
	 * Herriez)
	 * 
	 * While this isn't necessarily the best place for such discussion, open
	 * source principles require proper credit given to those who have
	 * contributed. This core class seems to me a place to acknowledge this core
	 * work of the Jmol team.
	 * 
	 * Bob Hanson, 6/2009 hansonr@stolaf.edu
	 */

	public static final String SCRIPT_COMPLETED = "Script completed";
	protected BufferedReader reader;
	// Tosin added
	public HeatMapDemo hmd;
	
		//added lxq35
	public boolean threadStop = false; 

	public ScriptEvaluator(Viewer viewer) {
		this.viewer = viewer;
		this.compiler = (compiler == null ? viewer.compiler : compiler);
		definedAtomSets = viewer.definedAtomSets;
		currentThread = Thread.currentThread();
	}

	// //////////////// primary interfacing methods //////////////////

	/*
	 * see Viewer.evalStringWaitStatus for how these are implemented
	 */
	public boolean compileScriptString(String script, boolean tQuiet) {
		clearState(tQuiet);
		contextPath = "[script]";
		return compileScript(null, script, debugScript);
	}

	/**
	 * for the ISOSURFACE command
	 * 
	 * @param fname
	 * @param xyz
	 * @param ret
	 * @return [ ScriptFunction, Params ]
	 */
	private Object[] createFunction(String fname, String xyz, String ret) {
		ScriptEvaluator e = new ScriptEvaluator(viewer);
		try {
			e.compileScript(null, "function " + fname + "(" + xyz
					+ ") { return " + ret + "}", false);
			List<ScriptVariable> params = new ArrayList<ScriptVariable>();
			for (int i = 0; i < xyz.length(); i += 2)
				params.add(ScriptVariable.getVariable(Float.valueOf(0f))
						.setName(xyz.substring(i, i + 1)));
			return new Object[] { e.aatoken[0][1].value, params };
		} catch (Exception ex) {
			return null;
		}
	}

	public boolean compileScriptFile(String filename, boolean tQuiet) {
		clearState(tQuiet);
		contextPath = filename;
		return compileScriptFileInternal(filename, null, null, null);
	}

	//Tuan changed to repaint only
	public void evaluateCompiledScript(boolean isCmdLine_c_or_C_Option,
			boolean isCmdLine_C_Option, boolean historyDisabled,
			boolean listCommands, StringBuffer outputBuffer, boolean...isRepaint) {


		boolean tempOpen = this.isCmdLine_C_Option;
		this.isCmdLine_C_Option = isCmdLine_C_Option;
		
		//Tuan changed
		if (isRepaint == null || isRepaint.length == 0)
			viewer.pushHoldRepaint("runEval");
		
		interruptExecution = executionPaused = false;
		executionStepping = false;
		isExecuting = true;
		currentThread = Thread.currentThread();
		isSyntaxCheck = this.isCmdLine_c_or_C_Option = isCmdLine_c_or_C_Option;
		timeBeginExecution = System.currentTimeMillis();
		this.historyDisabled = historyDisabled;
		this.outputBuffer = outputBuffer;
		setErrorMessage(null);
		try {
			try {
				setScriptExtensions();
				instructionDispatchLoop(listCommands);
				String script = viewer.getInterruptScript();
				if (script != "")
					runScript(script, null);
			} catch (Error er) {
				viewer.handleError(er, false);
				setErrorMessage("" + er + " " + viewer.getShapeErrorState());
				errorMessageUntranslated = "" + er;
				scriptStatusOrBuffer(errorMessage);
			}
		} catch (ScriptException e) {
			setErrorMessage(e.toString());
			errorMessageUntranslated = e.getErrorMessageUntranslated();
			scriptStatusOrBuffer(errorMessage);
			viewer.notifyError(
					(errorMessage != null
							&& errorMessage
									.indexOf("java.lang.OutOfMemoryError") >= 0 ? "Error"
							: "ScriptException"), errorMessage,
					errorMessageUntranslated);
		}
		timeEndExecution = System.currentTimeMillis();
		this.isCmdLine_C_Option = tempOpen;
		if (errorMessage == null && interruptExecution)
			setErrorMessage("execution interrupted");
		else if (!tQuiet && !isSyntaxCheck)
			viewer.scriptStatus(SCRIPT_COMPLETED);
		isExecuting = isSyntaxCheck = isCmdLine_c_or_C_Option = historyDisabled = false;
		
		if (isRepaint == null || isRepaint.length == 0){
			viewer.setTainted(true);
			viewer.popHoldRepaint("runEval");
		}else{
			viewer.repaint();
		}
	}

	/**
	 * runs a script and sends selected output to a provided StringBuffer
	 * 
	 * @param script
	 * @param outputBuffer
	 * @throws ScriptException
	 */
	public void runScript(String script, StringBuffer outputBuffer)
			throws ScriptException {
		// a = script("xxxx")
		pushContext(null);
		contextPath += " >> script() ";

		this.outputBuffer = outputBuffer;
		if (compileScript(null, script + JmolConstants.SCRIPT_EDITOR_IGNORE,
				false))
			instructionDispatchLoop(false);
		popContext(false, false);
	}

	/**
	 * a method for just checking a script
	 * 
	 * @param script
	 * @return a ScriptContext that indicates errors and provides a tokenized
	 *         version of the script that has passed all syntax checking, both
	 *         in the compiler and the evaluator
	 * 
	 */
	public ScriptContext checkScriptSilent(String script) {
		ScriptContext sc = compiler.compile(null, script, false, true, false,
				true);
		if (sc.errorType != null)
			return sc;
		restoreScriptContext(sc, false, false, false);
		isSyntaxCheck = true;
		isCmdLine_c_or_C_Option = isCmdLine_C_Option = false;
		pc = 0;

		try {
			instructionDispatchLoop(false);
		} catch (ScriptException e) {
			setErrorMessage(e.toString());
			sc = getScriptContext();
		}

		isSyntaxCheck = false;
		return sc;
	}

	// //////////////////////// script execution /////////////////////

	private boolean tQuiet;
	protected boolean isSyntaxCheck;
	private boolean isCmdLine_C_Option;
	protected boolean isCmdLine_c_or_C_Option;
	private boolean historyDisabled;
	protected boolean logMessages;
	private boolean debugScript;

	public void setDebugging() {
		debugScript = viewer.getDebugScript();
		logMessages = (debugScript && Logger.debugging);
	}

	private boolean interruptExecution;
	private boolean executionPaused;
	private boolean executionStepping;
	private boolean isExecuting;

	private long timeBeginExecution;
	private long timeEndExecution;

	public int getExecutionWalltime() {
		return (int) (timeEndExecution - timeBeginExecution);
	}

	public void haltExecution() {
		resumePausedExecution();
		interruptExecution = true;
	}

	public void pauseExecution(boolean withDelay) {
		if (isSyntaxCheck || viewer.isHeadless())
			return;
		if (withDelay)
			delay(-100);
		viewer.popHoldRepaint("pauseExecution");
		executionStepping = false;
		executionPaused = true;
	}

	public void stepPausedExecution() {
		executionStepping = true;
		executionPaused = false;
		// releases a paused thread but
		// sets it to pause for the next command.
	}

	public void resumePausedExecution() {
		executionPaused = false;
		executionStepping = false;
	}

	public boolean isScriptExecuting() {
		return isExecuting && !interruptExecution;
	}

	public boolean isExecutionPaused() {
		return executionPaused;
	}

	public boolean isExecutionStepping() {
		return executionStepping;
	}

	/**
	 * when paused, indicates what statement will be next
	 * 
	 * @return a string indicating the statement
	 */
	public String getNextStatement() {
		return (pc < aatoken.length ? setErrorLineMessage(functionName,
				scriptFileName, getLinenumber(null), pc,
				statementAsString(aatoken[pc], -9999, logMessages)) : "");
	}

	/**
	 * used for recall of commands in the application console
	 * 
	 * @param pc
	 * @param allThisLine
	 * @param addSemi
	 * @return a string representation of the command
	 */
	private String getCommand(int pc, boolean allThisLine, boolean addSemi) {
		if (pc >= lineIndices.length)
			return "";
		if (allThisLine) {
			int pt0 = -1;
			int pt1 = script.length();
			for (int i = 0; i < lineNumbers.length; i++)
				if (lineNumbers[i] == lineNumbers[pc]) {
					if (pt0 < 0)
						pt0 = lineIndices[i][0];
					pt1 = lineIndices[i][1];
				} else if (lineNumbers[i] == 0
						|| lineNumbers[i] > lineNumbers[pc]) {
					break;
				}
			if (pt1 == script.length() - 1 && script.endsWith("}"))
				pt1++;
			return (pt0 == script.length() || pt1 < pt0 ? ""
					: script.substring(Math.max(pt0, 0),
							Math.min(script.length(), pt1)));
		}
		int ichBegin = lineIndices[pc][0];
		int ichEnd = lineIndices[pc][1];
		// (pc + 1 == lineIndices.length || lineIndices[pc + 1][0] == 0 ? script
		// .length()
		// : lineIndices[pc + 1]);
		String s = "";
		if (ichBegin < 0 || ichEnd <= ichBegin || ichEnd > script.length())
			return "";
		try {
			s = script.substring(ichBegin, ichEnd);
			if (s.indexOf("\\\n") >= 0)
				s = TextFormat.simpleReplace(s, "\\\n", "  ");
			if (s.indexOf("\\\r") >= 0)
				s = TextFormat.simpleReplace(s, "\\\r", "  ");
			// int i;
			// for (i = s.length(); --i >= 0 && !ScriptCompiler.eol(s.charAt(i),
			// 0);
			// ){
			// }
			// s = s.substring(0, i + 1);
			if (s.length() > 0 && !s.endsWith(";")/*
												 * && !s.endsWith("{") &&
												 * !s.endsWith("}")
												 */)
				s += ";";
		} catch (Exception e) {
			Logger.error("darn problem in Eval getCommand: ichBegin="
					+ ichBegin + " ichEnd=" + ichEnd + " len = "
					+ script.length() + "\n" + e);
		}
		return s;
	}

	private void logDebugScript(int ifLevel) {
		if (logMessages) {
			if (statement.length > 0)
				Logger.debug(statement[0].toString());
			for (int i = 1; i < statementLength; ++i)
				Logger.debug(statement[i].toString());
		}
		iToken = -9999;
		if (logMessages) {
			StringBuffer strbufLog = new StringBuffer(80);
			String s = (ifLevel > 0 ? "                          ".substring(0,
					ifLevel * 2) : "");
			strbufLog.append(s).append(
					statementAsString(statement, iToken, logMessages));
			viewer.scriptStatus(strbufLog.toString());
		} else {
			String cmd = getCommand(pc, false, false);
			if (cmd != "")
				viewer.scriptStatus(cmd);
		}

	}

	// /////////////// string-based evaluation support /////////////////////

	private final static String EXPRESSION_KEY = "e_x_p_r_e_s_s_i_o_n";

	/**
	 * a general-use method to evaluate a "SET" type expression.
	 * 
	 * @param viewer
	 * @param expr
	 * @return an object of one of the following types: Boolean, Integer, Float,
	 *         String, Point3f, BitSet
	 */

	public static Object evaluateExpression(Viewer viewer, Object expr) {
		// Text.formatText for MESSAGE and ECHO
		// prior to 12.[2/3].32 was not thread-safe for compilation.
		ScriptEvaluator e = new ScriptEvaluator(viewer);
		return (e.evaluate(expr));
	}

	private Object evaluate(Object expr) {
		try {
			if (expr instanceof String) {
				if (compileScript(null, EXPRESSION_KEY + " = " + expr, false)) {
					contextVariables = viewer.getContextVariables();
					setStatement(0);
					return parameterExpressionString(2, 0);
				}
			} else if (expr instanceof Token[]) {
				contextVariables = viewer.getContextVariables();
				return atomExpression((Token[]) expr, 0, 0, true, false, true,
						false);
			}
		} catch (Exception ex) {
			Logger.error("Error evaluating: " + expr + "\n" + ex);
		}
		return "ERROR";
	}

	ShapeManager shapeManager;

	public static boolean evaluateContext(Viewer viewer, ScriptContext context,
			ShapeManager shapeManager) {
		ScriptEvaluator e = new ScriptEvaluator(viewer);
		e.historyDisabled = true;
		e.compiler = new ScriptCompiler(e.compiler);
		e.shapeManager = shapeManager;
		try {
			e.restoreScriptContext(context, true, false, false);
			e.instructionDispatchLoop(false);
		} catch (Exception ex) {
			viewer.setStringProperty("_errormessage", "" + ex);
			Logger.error("Error evaluating context");
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * a general method to evaluate a string representing an atom set.
	 * 
	 * @param e
	 * @param atomExpression
	 * @return is a bitset indicating the selected atoms
	 * 
	 */
	public static BitSet getAtomBitSet(ScriptEvaluator e, Object atomExpression) {
		if (atomExpression instanceof BitSet)
			return (BitSet) atomExpression;
		BitSet bs = new BitSet();
		try {
			e.pushContext(null);
			String scr = "select (" + atomExpression + ")";
			scr = TextFormat.replaceAllCharacters(scr, "\n\r", "),(");
			scr = TextFormat.simpleReplace(scr, "()", "(none)");
			if (e.compileScript(null, scr, false)) {
				e.statement = e.aatoken[0];
				bs = e.atomExpression(e.statement, 1, 0, false, false, true,
						true);
			}
			e.popContext(false, false);
		} catch (Exception ex) {
			Logger.error("getAtomBitSet " + atomExpression + "\n" + ex);
		}
		return bs;
	}

	/**
	 * just provides a vector list of atoms in a string-based expression
	 * 
	 * @param e
	 * @param atomCount
	 * @param atomExpression
	 * @return vector list of selected atoms
	 */
	public static List<Integer> getAtomBitSetVector(ScriptEvaluator e,
			int atomCount, Object atomExpression) {
		List<Integer> V = new ArrayList<Integer>();
		BitSet bs = getAtomBitSet(e, atomExpression);
		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
			V.add(Integer.valueOf(i));
		}
		return V;
	}

	@SuppressWarnings("unchecked")
	private List<ScriptVariable> parameterExpressionList(int pt, int ptAtom,
			boolean isArrayItem) throws ScriptException {
		return (List<ScriptVariable>) parameterExpression(pt, -1, null, true,
				true, ptAtom, isArrayItem, null, null);
	}

	private String parameterExpressionString(int pt, int ptMax)
			throws ScriptException {
		return (String) parameterExpression(pt, ptMax, "", true, false, -1,
				false, null, null);
	}

	private boolean parameterExpressionBoolean(int pt, int ptMax)
			throws ScriptException {
		return ((Boolean) parameterExpression(pt, ptMax, null, true, false, -1,
				false, null, null)).booleanValue();
	}

	private ScriptVariable parameterExpressionToken(int pt)
			throws ScriptException {
		List<ScriptVariable> result = parameterExpressionList(pt, -1, false);
		return (result.size() > 0 ? result.get(0) : ScriptVariable
				.getVariable(""));
	}

	/**
	 * This is the primary driver of the RPN (reverse Polish notation)
	 * expression processor. It handles all math outside of a "traditional" Jmol
	 * SELECT/RESTRICT context. [Object atomExpression() takes care of that, and
	 * also uses the RPN class.]
	 * 
	 * @param pt
	 *            token index in statement start of expression
	 * @param ptMax
	 *            token index in statement end of expression
	 * @param key
	 *            variable name for debugging reference only -- null indicates
	 *            return Boolean -- "" indicates return String
	 * @param ignoreComma
	 *            TODO
	 * @param asVector
	 *            a flag passed on to RPN;
	 * @param ptAtom
	 *            this is a for() or select() function with a specific atom
	 *            selected
	 * @param isArrayItem
	 *            we are storing A[x] = ... so we need to deliver "x" as well
	 * @param localVars
	 *            see below -- lists all nested for(x, {exp}, select(y,
	 *            {ex},...)) variables
	 * @param localVar
	 *            x or y in above for(), select() examples
	 * @return either a vector or a value, caller's choice.
	 * @throws ScriptException
	 *             errors are thrown directly to the Eval error system.
	 */
	private Object parameterExpression(int pt, int ptMax, String key,
			boolean ignoreComma, boolean asVector, int ptAtom,
			boolean isArrayItem, Map<String, ScriptVariable> localVars,
			String localVar) throws ScriptException {

		/*
		 * localVar is a variable designated at the beginning of the
		 * select(x,...) or for(x,...) construct that will be implicitly used
		 * for properties. So, for example, "atomno" will become "x.atomno".
		 * That's all it is for. localVars provides a localized context variable
		 * set for a given nested set of for/select.
		 * 
		 * Note that localVars has nothing to do standard if/for/while flow
		 * contexts, just these specialized functions. Any variable defined in
		 * for or while is simply added to the context for a given script or
		 * function. These assignments are made by the compiler when seeing a
		 * VAR keyword.
		 */
		Object v, res;
		boolean isImplicitAtomProperty = (localVar != null);
		boolean isOneExpressionOnly = (pt < 0);
		boolean returnBoolean = (!asVector && key == null);
		boolean returnString = (!asVector && key != null && key.length() == 0);
		int nSquare = 0;
		if (isOneExpressionOnly)
			pt = -pt;
		int nParen = 0;
		ScriptMathProcessor rpn = new ScriptMathProcessor(this, isArrayItem,
				asVector, false);
		if (pt == 0 && ptMax == 0) // set command with v[...] = ....
			pt = 2;
		if (ptMax < pt)
			ptMax = statementLength;
		out: for (int i = pt; i < ptMax; i++) {
			v = null;
			int tok = getToken(i).tok;
			if (isImplicitAtomProperty && tokAt(i + 1) != Token.per) {
				ScriptVariable token = (localVars != null
						&& localVars.containsKey(theToken.value) ? null
						: getBitsetPropertySelector(i, false));
				if (token != null) {
					rpn.addXVar(localVars.get(localVar));
					if (!rpn.addOpAllowMath(token,
							(tokAt(i + 1) == Token.leftparen)))
						error(ERROR_invalidArgument);
					if ((token.intValue == Token.function || token.intValue == Token.parallel)
							&& tokAt(iToken + 1) != Token.leftparen) {
						rpn.addOp(Token.tokenLeftParen);
						rpn.addOp(Token.tokenRightParen);
					}
					i = iToken;
					continue;
				}
			}
			switch (tok) {
			case Token.define:
				// @{@x} or @{@{x}} or @{@1} -- also userFunction(@1)
				if (tokAt(++i) == Token.expressionBegin) {
					v = parameterExpressionToken(++i);
					i = iToken;
				} else if (tokAt(i) == Token.integer) {
					v = viewer.getAtomBits(Token.atomno,
							Integer.valueOf(statement[i].intValue));
					break;
				} else {
					v = getParameter(ScriptVariable.sValue(statement[i]),
							Token.variable);
				}
				v = getParameter(((ScriptVariable) v).asString(),
						Token.variable);
				break;
			case Token.ifcmd:
				if (getToken(++i).tok != Token.leftparen)
					error(ERROR_invalidArgument);
				if (localVars == null)
					localVars = new Hashtable<String, ScriptVariable>();
				res = parameterExpression(++i, -1, null, ignoreComma, false,
						-1, false, localVars, localVar);
				boolean TF = ((Boolean) res).booleanValue();
				int iT = iToken;
				if (getToken(iT++).tok != Token.semicolon)
					error(ERROR_invalidArgument);
				parameterExpressionBoolean(iT, -1);
				int iF = iToken;
				if (tokAt(iF++) != Token.semicolon)
					error(ERROR_invalidArgument);
				parameterExpression(-iF, -1, null, ignoreComma, false, 1,
						false, localVars, localVar);
				int iEnd = iToken;
				if (tokAt(iEnd) != Token.rightparen)
					error(ERROR_invalidArgument);
				v = parameterExpression(TF ? iT : iF, TF ? iF : iEnd, "XXX",
						ignoreComma, false, 1, false, localVars, localVar);
				i = iEnd;
				break;
			case Token.forcmd:
			case Token.select:
				boolean isFunctionOfX = (pt > 0);
				boolean isFor = (isFunctionOfX && tok == Token.forcmd);
				// it is important to distinguish between the select command:
				// select {atomExpression} (mathExpression)
				// and the select(dummy;{atomExpression};mathExpression)
				// function:
				// select {*.ca} (phi < select(y; {*.ca}; y.resno = _x.resno +
				// 1).phi)
				String dummy;
				// for(dummy;...
				// select(dummy;...
				if (isFunctionOfX) {
					if (getToken(++i).tok != Token.leftparen
							|| !Token.tokAttr(getToken(++i).tok,
									Token.identifier))
						error(ERROR_invalidArgument);
					dummy = parameterAsString(i);
					if (getToken(++i).tok != Token.semicolon)
						error(ERROR_invalidArgument);
				} else {
					dummy = "_x";
				}
				// for(dummy;{atom expr};...
				// select(dummy;{atom expr};...
				v = parameterExpressionToken(-(++i)).value;
				if (!(v instanceof BitSet))
					error(ERROR_invalidArgument);
				BitSet bsAtoms = (BitSet) v;
				i = iToken;
				if (isFunctionOfX && getToken(i++).tok != Token.semicolon)
					error(ERROR_invalidArgument);
				// for(dummy;{atom expr};math expr)
				// select(dummy;{atom expr};math expr)
				// bsX is necessary because there are a few operations that
				// still
				// are there for now that require it; could go, though.
				BitSet bsSelect = new BitSet();
				BitSet bsX = new BitSet();
				String[] sout = (isFor ? new String[BitSetUtil
						.cardinalityOf(bsAtoms)] : null);
				if (localVars == null)
					localVars = new Hashtable<String, ScriptVariable>();
				bsX.set(0);
				ScriptVariable t = new ScriptVariable(bsX, 0);
				localVars.put(dummy, t.setName(dummy));
				// one test just to check for errors and get iToken
				int pt2 = -1;
				if (isFunctionOfX) {
					pt2 = i - 1;
					int np = 0;
					int tok2;
					while (np >= 0 && ++pt2 < ptMax) {
						if ((tok2 = tokAt(pt2)) == Token.rightparen)
							np--;
						else if (tok2 == Token.leftparen)
							np++;
					}
				}
				int p = 0;
				int jlast = 0;
				int j = bsAtoms.nextSetBit(0);
				if (j < 0) {
					iToken = pt2 - 1;
				} else if (!isSyntaxCheck) {
					for (; j >= 0; j = bsAtoms.nextSetBit(j + 1)) {
						if (jlast >= 0)
							bsX.clear(jlast);
						jlast = j;
						bsX.set(j);
						t.index = j;
						res = parameterExpression(i, pt2,
								(isFor ? "XXX" : null), ignoreComma, isFor, j,
								false, localVars, isFunctionOfX ? null : dummy);
						if (isFor) {
							if (res == null || ((List<?>) res).size() == 0)
								error(ERROR_invalidArgument);
							sout[p++] = ((ScriptVariable) ((List<?>) res)
									.get(0)).asString();
						} else if (((Boolean) res).booleanValue()) {
							bsSelect.set(j);
						}
					}
				}
				if (isFor) {
					v = sout;
				} else if (isFunctionOfX) {
					v = bsSelect;
				} else {
					return bitsetVariableVector(bsSelect);
				}
				i = iToken + 1;
				break;
			case Token.semicolon: // for (i = 1; i < 3; i=i+1)
				break out;
			case Token.decimal:
				rpn.addXNum(ScriptVariable.getVariable(theToken.value));
				break;
			case Token.spec_seqcode:
			case Token.integer:
				rpn.addXNum(new ScriptVariableInt(theToken.intValue));
				break;
			// these next are for the within() command
			case Token.plane:
				if (tokAt(iToken + 1) == Token.leftparen) {
					if (!rpn.addOpAllowMath(theToken, true))
						error(ERROR_invalidArgument);
					break;
				}
				rpn.addXVar(new ScriptVariable(theToken));
				break;
			// for within:
			case Token.atomname:
			case Token.atomtype:
			case Token.branch:
			case Token.boundbox:
			case Token.chain:
			case Token.coord:
			case Token.element:
			case Token.group:
			case Token.model:
			case Token.molecule:
			case Token.sequence:
			case Token.site:
			case Token.search:
			case Token.smiles:
			case Token.substructure:
			case Token.structure:
				// //
			case Token.on:
			case Token.off:
			case Token.string:
			case Token.point3f:
			case Token.point4f:
			case Token.matrix3f:
			case Token.matrix4f:
			case Token.bitset:
			case Token.hash:
				rpn.addXVar(new ScriptVariable(theToken));
				break;
			case Token.dollarsign:
				ignoreError = true;
				Point3f ptc;
				try {
					ptc = centerParameter(i);
					rpn.addXVar(new ScriptVariable(Token.point3f, ptc));
				} catch (Exception e) {
					rpn.addXStr("");
				}
				ignoreError = false;
				i = iToken;
				break;
			case Token.leftbrace:
				if (tokAt(i + 1) == Token.string)
					v = getHash(i);
				else
					v = getPointOrPlane(i, false, true, true, false, 3, 4);
				i = iToken;
				break;
			case Token.expressionBegin:
				if (tokAt(i + 1) == Token.expressionEnd) {
					v = new Hashtable<String, Object>();
					i++;
					break;
				} else if (tokAt(i + 1) == Token.all
						&& tokAt(i + 2) == Token.expressionEnd) {
					tok = Token.all;
					iToken += 2;
				}
				//$FALL-THROUGH$
			case Token.all:
				if (tok == Token.all)
					v = viewer.getModelUndeletedAtomsBitSet(-1);
				else
					v = atomExpression(statement, i, 0, true, true, true, true);
				i = iToken;
				if (nParen == 0 && isOneExpressionOnly) {
					iToken++;
					return bitsetVariableVector(v);
				}
				break;
			case Token.spacebeforesquare:
				rpn.addOp(theToken);
				continue;
			case Token.expressionEnd:
				i++;
				break out;
			case Token.rightbrace:
				if (!ignoreComma && nParen == 0 && nSquare == 0)
					break out;
				error(ERROR_invalidArgument);
				break;
			case Token.comma: // ignore commas
				if (!ignoreComma && nParen == 0 && nSquare == 0) {
					break out;
				}
				if (!rpn.addOp(theToken))
					error(ERROR_invalidArgument);
				break;
			case Token.per:
				ScriptVariable token = getBitsetPropertySelector(i + 1, false);
				if (token == null)
					error(ERROR_invalidArgument);
				// check for added min/max modifier
				boolean isUserFunction = (token.intValue == Token.function);
				boolean allowMathFunc = true;
				int tok2 = tokAt(iToken + 2);
				if (tokAt(iToken + 1) == Token.per) {
					switch (tok2) {
					case Token.all:
						tok2 = Token.minmaxmask;
						if (tokAt(iToken + 3) == Token.per
								&& tokAt(iToken + 4) == Token.bin)
							tok2 = Token.selectedfloat;
						//$FALL-THROUGH$
					case Token.min:
					case Token.max:
					case Token.stddev:
					case Token.sum:
					case Token.sum2:
					case Token.average:
						allowMathFunc = (isUserFunction
								|| tok2 == Token.minmaxmask || tok2 == Token.selectedfloat);
						token.intValue |= tok2;
						getToken(iToken + 2);
					}
				}
				allowMathFunc &= (tokAt(iToken + 1) == Token.leftparen || isUserFunction);
				if (!rpn.addOpAllowMath(token, allowMathFunc))
					error(ERROR_invalidArgument);
				i = iToken;
				if (token.intValue == Token.function
						&& tokAt(i + 1) != Token.leftparen) {
					rpn.addOp(Token.tokenLeftParen);
					rpn.addOp(Token.tokenRightParen);
				}
				break;
			default:
				if (Token.tokAttr(theTok, Token.mathop)
						|| Token.tokAttr(theTok, Token.mathfunc)
						&& tokAt(iToken + 1) == Token.leftparen) {
					if (!rpn.addOp(theToken)) {
						if (ptAtom >= 0) {
							// this is expected -- the right parenthesis
							break out;
						}
						error(ERROR_invalidArgument);
					}
					switch (theTok) {
					case Token.leftparen:
						nParen++;
						break;
					case Token.rightparen:
						if (--nParen <= 0 && nSquare == 0
								&& isOneExpressionOnly) {
							iToken++;
							break out;
						}
						break;
					case Token.leftsquare:
						nSquare++;
						break;
					case Token.rightsquare:
						if (--nSquare == 0 && nParen == 0
								&& isOneExpressionOnly) {
							iToken++;
							break out;
						}
						break;
					}
				} else {
					// first check to see if the variable has been defined
					// already
					String name = parameterAsString(i).toLowerCase();
					boolean haveParens = (tokAt(i + 1) == Token.leftparen);
					if (isSyntaxCheck) {
						v = name;
					} else if (!haveParens
							&& (localVars == null || (v = localVars.get(name)) == null)) {
						v = getContextVariableAsVariable(name);
					}
					if (v == null) {
						if (Token.tokAttr(theTok, Token.identifier)
								&& viewer.isFunction(name)) {
							if (!rpn.addOp(new ScriptVariable(Token.function,
									theToken.value)))
								error(ERROR_invalidArgument);
							if (!haveParens) {
								rpn.addOp(Token.tokenLeftParen);
								rpn.addOp(Token.tokenRightParen);
							}
						} else {
							rpn.addXVar(viewer.getOrSetNewVariable(name, false));
						}
					}
				}
			}
			if (v != null) {
				if (v instanceof BitSet)
					rpn.addXBs((BitSet) v);
				else
					rpn.addXObj(v);
			}
		}
		ScriptVariable result = rpn.getResult(false);
		if (result == null) {
			if (!isSyntaxCheck)
				rpn.dumpStacks("null result");
			error(ERROR_endOfStatementUnexpected);
		}
		if (result.tok == Token.vector)
			return result.value;
		if (returnBoolean)
			return Boolean.valueOf(ScriptVariable.bValue(result));
		if (returnString) {
			if (result.tok == Token.string)
				result.intValue = Integer.MAX_VALUE;
			return result.asString();
		}
		switch (result.tok) {
		case Token.on:
		case Token.off:
			return Boolean.valueOf(result.intValue == 1);
		case Token.integer:
			return Integer.valueOf(result.intValue);
		case Token.bitset:
		case Token.decimal:
		case Token.string:
		case Token.point3f:
		default:
			return result.value;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getHash(int i) throws ScriptException {
		Map<String, Object> ht = new Hashtable<String, Object>();
		for (i = i + 1; i < statementLength; i++) {
			if (tokAt(i) == Token.rightbrace)
				break;
			String key = stringParameter(i++);
			if (tokAt(i++) != Token.colon)
				error(ERROR_invalidArgument);
			List<ScriptVariable> v = (List<ScriptVariable>) parameterExpression(
					i, 0, null, false, true, -1, false, null, null);
			ht.put(key, v.get(0));
			i = iToken;
			if (tokAt(i) != Token.comma)
				break;
		}
		iToken = i;
		if (tokAt(i) != Token.rightbrace)
			error(ERROR_invalidArgument);
		return ht;
	}

	List<ScriptVariable> bitsetVariableVector(Object v) {
		List<ScriptVariable> resx = new ArrayList<ScriptVariable>();
		if (v instanceof BitSet) {
			resx.add(new ScriptVariable(Token.bitset, v));
		}
		return resx;
	}

	Object getBitsetIdent(BitSet bs, String label, Object tokenValue,
			boolean useAtomMap, int index, boolean isExplicitlyAll) {
		boolean isAtoms = !(tokenValue instanceof BondSet);
		if (isAtoms) {
			if (label == null)
				label = viewer.getStandardLabelFormat(0);
			else if (label.length() == 0)
				label = "%[label]";
		}
		int pt = (label == null ? -1 : label.indexOf("%"));
		boolean haveIndex = (index != Integer.MAX_VALUE);
		if (bs == null || isSyntaxCheck || isAtoms && pt < 0) {
			if (label == null)
				label = "";
			return isExplicitlyAll ? new String[] { label } : (Object) label;
		}
		ModelSet modelSet = viewer.getModelSet();
		int n = 0;
		int[] indices = (isAtoms || !useAtomMap ? null : ((BondSet) tokenValue)
				.getAssociatedAtoms());
		if (indices == null && label != null && label.indexOf("%D") > 0)
			indices = viewer.getAtomIndices(bs);
		boolean asIdentity = (label == null || label.length() == 0);
		Map<String, Object> htValues = (isAtoms || asIdentity ? null
				: LabelToken.getBondLabelValues());
		LabelToken[] tokens = (asIdentity ? null : isAtoms ? LabelToken
				.compile(viewer, label, '\0', null) : LabelToken.compile(
				viewer, label, '\1', htValues));
		int nmax = (haveIndex ? 1 : BitSetUtil.cardinalityOf(bs));
		String[] sout = new String[nmax];
		for (int j = (haveIndex ? index : bs.nextSetBit(0)); j >= 0; j = bs
				.nextSetBit(j + 1)) {
			String str;
			if (isAtoms) {
				if (asIdentity)
					str = modelSet.atoms[j].getInfo();
				else
					str = LabelToken.formatLabel(viewer, modelSet.atoms[j],
							tokens, '\0', indices);
			} else {
				Bond bond = modelSet.getBondAt(j);
				if (asIdentity)
					str = bond.getIdentity();
				else
					str = LabelToken.formatLabel(viewer, bond, tokens,
							htValues, indices);
			}
			str = TextFormat.formatString(str, "#", (n + 1));
			sout[n++] = str;
			if (haveIndex)
				break;
		}
		return nmax == 1 && !isExplicitlyAll ? sout[0] : (Object) sout;
	}

	private ScriptVariable getBitsetPropertySelector(int i,
			boolean mustBeSettable) throws ScriptException {
		int tok = getToken(i).tok;
		switch (tok) {
		case Token.min:
		case Token.max:
		case Token.average:
		case Token.stddev:
		case Token.sum:
		case Token.sum2:
		case Token.property:
			break;
		default:
			if (Token.tokAttrOr(tok, Token.atomproperty, Token.mathproperty))
				break;
			if (tok != Token.opIf && !Token.tokAttr(tok, Token.identifier))
				return null;
			String name = parameterAsString(i);
			if (!mustBeSettable && viewer.isFunction(name)) {
				tok = Token.function;
				break;
			}
			if (!name.endsWith("?"))
				return null;
			tok = Token.identifier;
		}
		if (mustBeSettable && !Token.tokAttr(tok, Token.settable))
			return null;
		return new ScriptVariable(Token.propselector, tok, parameterAsString(i)
				.toLowerCase());
	}

	private float[] getBitsetPropertyFloat(BitSet bs, int tok, float min,
			float max) throws ScriptException {
		float[] data = (float[]) getBitsetProperty(bs, tok, null, null, null,
				null, false, Integer.MAX_VALUE, false);
		if (!Float.isNaN(min))
			for (int i = 0; i < data.length; i++)
				if (data[i] < min)
					data[i] = Float.NaN;
		if (!Float.isNaN(max))
			for (int i = 0; i < data.length; i++)
				if (data[i] > max)
					data[i] = Float.NaN;
		return data;
	}

	@SuppressWarnings("unchecked")
	protected Object getBitsetProperty(BitSet bs, int tok, Point3f ptRef,
			Point4f planeRef, Object tokenValue, Object opValue,
			boolean useAtomMap, int index, boolean asVectorIfAll)
			throws ScriptException {

		// index is a special argument set in parameterExpression that
		// indicates we are looking at only one atom within a for(...) loop
		// the bitset cannot be a BondSet in that case

		boolean haveIndex = (index != Integer.MAX_VALUE);

		boolean isAtoms = haveIndex || !(tokenValue instanceof BondSet);
		// check minmax flags:

		int minmaxtype = tok & Token.minmaxmask;
		boolean selectedFloat = (minmaxtype == Token.selectedfloat);
		int atomCount = viewer.getAtomCount();
		float[] fout = (minmaxtype == Token.allfloat ? new float[atomCount]
				: null);
		boolean isExplicitlyAll = (minmaxtype == Token.minmaxmask || selectedFloat);
		tok &= ~Token.minmaxmask;
		if (tok == Token.nada)
			tok = (isAtoms ? Token.atoms : Token.bonds);

		// determine property type:

		boolean isPt = false;
		boolean isInt = false;
		boolean isString = false;
		switch (tok) {
		case Token.xyz:
		case Token.vibxyz:
		case Token.fracxyz:
		case Token.fuxyz:
		case Token.unitxyz:
		case Token.color:
		case Token.screenxyz:
			isPt = true;
			break;
		case Token.function:
		case Token.distance:
			break;
		default:
			isInt = Token.tokAttr(tok, Token.intproperty)
					&& !Token.tokAttr(tok, Token.floatproperty);
			// occupancy and radius considered floats here
			isString = !isInt && Token.tokAttr(tok, Token.strproperty);
			// structure considered int; for the name, use
			// .label("%[structure]")
		}

		// preliminarty checks we only want to do once:

		Point3f zero = (minmaxtype == Token.allfloat ? new Point3f() : null);
		Point3f pt = (isPt || !isAtoms ? new Point3f() : null);
		if (isExplicitlyAll || isString && !haveIndex
				&& minmaxtype != Token.allfloat && minmaxtype != Token.min)
			minmaxtype = Token.all;
		List<Object> vout = (minmaxtype == Token.all ? new ArrayList<Object>()
				: null);
		BitSet bsNew = null;
		String userFunction = null;
		List<ScriptVariable> params = null;
		BitSet bsAtom = null;
		ScriptVariable tokenAtom = null;
		Point3f ptT = null;
		float[] data = null;

		switch (tok) {
		case Token.atoms:
		case Token.bonds:
			if (isSyntaxCheck)
				return bs;
			bsNew = (tok == Token.atoms ? (isAtoms ? bs : viewer.getAtomBits(
					Token.bonds, bs)) : (isAtoms ? (BitSet) new BondSet(
					viewer.getBondsForSelectedAtoms(bs)) : bs));
			int i;
			switch (minmaxtype) {
			case Token.min:
				i = bsNew.nextSetBit(0);
				break;
			case Token.max:
				i = bsNew.length() - 1;
				break;
			case Token.stddev:
			case Token.sum:
			case Token.sum2:
				return Float.valueOf(Float.NaN);
			default:
				return bsNew;
			}
			bsNew.clear();
			if (i >= 0)
				bsNew.set(i);
			return bsNew;
		case Token.identify:
			switch (minmaxtype) {
			case 0:
			case Token.all:
				return getBitsetIdent(bs, null, tokenValue, useAtomMap, index,
						isExplicitlyAll);
			}
			return "";
		case Token.function:
			userFunction = (String) ((Object[]) opValue)[0];
			params = (List<ScriptVariable>) ((Object[]) opValue)[1];
			bsAtom = new BitSet(atomCount);
			tokenAtom = new ScriptVariable(Token.bitset, bsAtom);
			break;
		case Token.straightness:
		case Token.surfacedistance:
		case Token.atomsequence:
			viewer.autoCalculate(tok);
			break;
		case Token.distance:
			if (ptRef == null && planeRef == null)
				return new Point3f();
			break;
		case Token.color:
			ptT = new Point3f();
			break;
		case Token.property:
			data = viewer.getDataFloat((String) opValue);
			break;
		}

		int n = 0;
		int ivvMinMax = 0;
		int ivMinMax = 0;
		float fvMinMax = 0;
		double sum = 0;
		double sum2 = 0;
		switch (minmaxtype) {
		case Token.min:
			ivMinMax = Integer.MAX_VALUE;
			fvMinMax = Float.MAX_VALUE;
			break;
		case Token.max:
			ivMinMax = Integer.MIN_VALUE;
			fvMinMax = -Float.MAX_VALUE;
			break;
		}
		ModelSet modelSet = viewer.getModelSet();
		int mode = (isPt ? 3 : isString ? 2 : isInt ? 1 : 0);
		if (isAtoms) {
			boolean haveBitSet = (bs != null);
			int iModel = -1;
			int i0, i1;
			if (haveIndex) {
				i0 = index;
				i1 = index + 1;
			} else if (haveBitSet) {
				i0 = bs.nextSetBit(0);
				i1 = Math.min(atomCount, bs.length());
			} else {
				i0 = 0;
				i1 = atomCount;
			}
			if (isSyntaxCheck)
				i1 = 0;
			for (int i = i0; i >= 0 && i < i1; i = (haveBitSet ? bs
					.nextSetBit(i + 1) : i + 1)) {
				n++;
				Atom atom = modelSet.atoms[i];
				switch (mode) {
				case 0: // float
					float fv = Float.MAX_VALUE;
					switch (tok) {
					case Token.function:
						bsAtom.set(i);
						fv = ScriptVariable.fValue(runFunction(null,
								userFunction, params, tokenAtom, true, true));
						bsAtom.clear(i);
						break;
					case Token.property:
						fv = (data == null ? 0 : data[i]);
						break;
					case Token.distance:
						if (planeRef != null)
							fv = Measure.distanceToPlane(planeRef, atom);
						else
							fv = atom.distance(ptRef);
						break;
					default:
						fv = Atom.atomPropertyFloat(viewer, atom, tok);
					}
					if (fv == Float.MAX_VALUE || Float.isNaN(fv)
							&& minmaxtype != Token.all) {
						n--; // don't count this one
						continue;
					}
					switch (minmaxtype) {
					case Token.min:
						if (fv < fvMinMax)
							fvMinMax = fv;
						break;
					case Token.max:
						if (fv > fvMinMax)
							fvMinMax = fv;
						break;
					case Token.allfloat:
						fout[i] = fv;
						break;
					case Token.all:
						vout.add(Float.valueOf(fv));
						break;
					case Token.sum2:
					case Token.stddev:
						sum2 += ((double) fv) * fv;
						//$FALL-THROUGH$
					case Token.sum:
					default:
						sum += fv;
					}
					break;
				case 1: // isInt
					int iv = 0;
					switch (tok) {
					case Token.symop:
						// a little weird:
						// First we determine how many operations we have in
						// this model.
						// Then we get the symmetry bitset, which shows the
						// assignments
						// of symmetry for this atom.
						if (atom.getModelIndex() != iModel)
							iModel = atom.getModelIndex();
						BitSet bsSym = atom.getAtomSymmetry();
						if (bsSym == null)
							break;
						int p = 0;
						switch (minmaxtype) {
						case Token.min:
							ivvMinMax = Integer.MAX_VALUE;
							break;
						case Token.max:
							ivvMinMax = Integer.MIN_VALUE;
							break;
						}
						for (int k = bsSym.nextSetBit(0); k >= 0; k = bsSym
								.nextSetBit(k + 1)) {
							iv += k + 1;
							switch (minmaxtype) {
							case Token.min:
								ivvMinMax = Math.min(ivvMinMax, k + 1);
								break;
							case Token.max:
								ivvMinMax = Math.max(ivvMinMax, k + 1);
								break;
							}
							p++;
						}
						switch (minmaxtype) {
						case Token.min:
						case Token.max:
							iv = ivvMinMax;
						}
						n += p - 1;
						break;
					case Token.configuration:
					case Token.cell:
						error(ERROR_unrecognizedAtomProperty, Token.nameOf(tok));
						break;
					default:
						iv = Atom.atomPropertyInt(atom, tok);
					}
					switch (minmaxtype) {
					case Token.min:
						if (iv < ivMinMax)
							ivMinMax = iv;
						break;
					case Token.max:
						if (iv > ivMinMax)
							ivMinMax = iv;
						break;
					case Token.allfloat:
						fout[i] = iv;
						break;
					case Token.all:
						vout.add(Integer.valueOf(iv));
						break;
					case Token.sum2:
					case Token.stddev:
						sum2 += ((double) iv) * iv;
						//$FALL-THROUGH$
					case Token.sum:
					default:
						sum += iv;
					}
					break;
				case 2: // isString
					String s = Atom.atomPropertyString(viewer, atom, tok);
					switch (minmaxtype) {
					case Token.allfloat:
						fout[i] = Parser.parseFloat(s);
						break;
					default:
						if (vout == null)
							return s;
						vout.add(s);
					}
					break;
				case 3: // isPt
					Tuple3f t = Atom.atomPropertyTuple(atom, tok);
					if (t == null)
						error(ERROR_unrecognizedAtomProperty, Token.nameOf(tok));
					switch (minmaxtype) {
					case Token.allfloat:
						fout[i] = (float) Math.sqrt(t.x * t.x + t.y * t.y + t.z
								* t.z);
						break;
					case Token.all:
						vout.add(new Point3f(t));
						break;
					default:
						pt.add(t);
					}
					break;
				}
				if (haveIndex)
					break;
			}
		} else { // bonds
			boolean isAll = (bs == null);
			int i0 = (isAll ? 0 : bs.nextSetBit(0));
			int i1 = viewer.getBondCount();
			for (int i = i0; i >= 0 && i < i1; i = (isAll ? i + 1 : bs
					.nextSetBit(i + 1))) {
				n++;

				Bond bond = modelSet.getBondAt(i);
				switch (tok) {
				case Token.length:
					float fv = bond.getAtom1().distance(bond.getAtom2());
					switch (minmaxtype) {
					case Token.min:
						if (fv < fvMinMax)
							fvMinMax = fv;
						break;
					case Token.max:
						if (fv > fvMinMax)
							fvMinMax = fv;
						break;
					case Token.all:
						vout.add(Float.valueOf(fv));
						break;
					case Token.sum2:
					case Token.stddev:
						sum2 += (double) fv * fv;
						//$FALL-THROUGH$
					case Token.sum:
					default:
						sum += fv;
					}
					break;
				case Token.xyz:
					switch (minmaxtype) {
					case Token.all:
						pt.set(bond.getAtom1());
						pt.add(bond.getAtom2());
						pt.scale(0.5f);
						vout.add(new Point3f(pt));
						break;
					default:
						pt.add(bond.getAtom1());
						pt.add(bond.getAtom2());
						n++;
					}
					break;
				case Token.color:
					ColorUtil.colorPointFromInt(
							viewer.getColorArgbOrGray(bond.getColix()), ptT);
					switch (minmaxtype) {
					case Token.all:
						vout.add(new Point3f(ptT));
						break;
					default:
						pt.add(ptT);
					}
					break;
				default:
					error(ERROR_unrecognizedBondProperty, Token.nameOf(tok));
				}
			}
		}
		if (minmaxtype == Token.allfloat)
			return fout;
		if (minmaxtype == Token.all) {
			if (asVectorIfAll)
				return vout;
			int len = vout.size();
			if (isString && !isExplicitlyAll && len == 1)
				return vout.get(0);
			if (selectedFloat) {
				fout = new float[len];
				for (int i = len; --i >= 0;) {
					Object v = vout.get(i);
					switch (mode) {
					case 0:
						fout[i] = ((Float) v).floatValue();
						break;
					case 1:
						fout[i] = ((Integer) v).floatValue();
						break;
					case 2:
						fout[i] = Parser.parseFloat((String) v);
						break;
					case 3:
						fout[i] = ((Point3f) v).distance(zero);
						break;
					}
				}
				return fout;
			}
			if (tok == Token.sequence) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < len; i++)
					sb.append((String) vout.get(i));
				return sb.toString();
			}
			String[] sout = new String[len];
			for (int i = len; --i >= 0;) {
				Object v = vout.get(i);
				if (v instanceof Point3f)
					sout[i] = Escape.escapePt((Point3f) v);
				else
					sout[i] = "" + vout.get(i);
			}
			return sout;
		}
		if (isPt)
			return (n == 0 ? pt : new Point3f(pt.x / n, pt.y / n, pt.z / n));
		if (n == 0 || n == 1 && minmaxtype == Token.stddev)
			return Float.valueOf(Float.NaN);
		if (isInt) {
			switch (minmaxtype) {
			case Token.min:
			case Token.max:
				return Integer.valueOf(ivMinMax);
			case Token.sum2:
			case Token.stddev:
				break;
			case Token.sum:
				return Integer.valueOf((int) sum);
			default:
				if (sum / n == (int) (sum / n))
					return Integer.valueOf((int) (sum / n));
				return Float.valueOf((float) (sum / n));
			}
		}
		switch (minmaxtype) {
		case Token.min:
		case Token.max:
			sum = fvMinMax;
			break;
		case Token.sum:
			break;
		case Token.sum2:
			sum = sum2;
			break;
		case Token.stddev:
			// because SUM (x_i - X_av)^2 = SUM(x_i^2) - 2X_av SUM(x_i) +
			// SUM(X_av^2)
			// = SUM(x_i^2) - 2nX_av^2 + nX_av^2
			// = SUM(x_i^2) - nX_av^2
			// = SUM(x_i^2) - [SUM(x_i)]^2 / n
			sum = Math.sqrt((sum2 - sum * sum / n) / (n - 1));
			break;
		default:
			sum /= n;
			break;
		}
		return Float.valueOf((float) sum);
	}

	private void setBitsetProperty(BitSet bs, int tok, int iValue,
			float fValue, Token tokenValue) throws ScriptException {
		if (isSyntaxCheck || BitSetUtil.cardinalityOf(bs) == 0)
			return;
		String[] list = null;
		String sValue = null;
		float[] fvalues = null;
		Point3f pt;
		List<ScriptVariable> sv = null;
		int nValues = 0;
		boolean isStrProperty = Token.tokAttr(tok, Token.strproperty);
		if (tokenValue.tok == Token.varray) {
			sv = ((ScriptVariable) tokenValue).getList();
			if ((nValues = sv.size()) == 0)
				return;
		}
		switch (tok) {
		case Token.xyz:
		case Token.fracxyz:
		case Token.fuxyz:
		case Token.vibxyz:
			switch (tokenValue.tok) {
			case Token.point3f:
				viewer.setAtomCoord(bs, tok, tokenValue.value);
				break;
			case Token.varray:
				theToken = tokenValue;
				viewer.setAtomCoord(bs, tok, getPointArray(-1, nValues));
				break;
			}
			return;
		case Token.color:
			Object value = null;
			String prop = "color";
			switch (tokenValue.tok) {
			case Token.varray:
				int[] values = new int[nValues];
				for (int i = nValues; --i >= 0;) {
					ScriptVariable svi = sv.get(i);
					pt = ScriptVariable.ptValue(svi);
					if (pt != null) {
						values[i] = ColorUtil.colorPtToInt(pt);
					} else if (svi.tok == Token.integer) {
						values[i] = svi.intValue;
					} else {
						values[i] = ColorUtil.getArgbFromString(svi.asString());
						if (values[i] == 0)
							values[i] = ScriptVariable.iValue(svi);
					}
					if (values[i] == 0)
						error(ERROR_unrecognizedParameter, "ARRAY",
								svi.asString());
				}
				value = values;
				prop = "colorValues";
				break;
			case Token.point3f:
				value = Integer.valueOf(ColorUtil
						.colorPtToInt((Point3f) tokenValue.value));
				break;
			case Token.string:
				value = tokenValue.value;
				break;
			default:
				value = Integer.valueOf(ScriptVariable.iValue(tokenValue));
				break;
			}
			setShapeProperty(JmolConstants.SHAPE_BALLS, prop, value, bs);
			return;
		case Token.label:
		case Token.format:
			if (tokenValue.tok != Token.varray)
				sValue = ScriptVariable.sValue(tokenValue);
			break;
		case Token.element:
		case Token.elemno:
			clearDefinedVariableAtomSets();
			isStrProperty = false;
			break;
		}
		switch (tokenValue.tok) {
		case Token.varray:
			if (isStrProperty)
				list = ScriptVariable.listValue(tokenValue);
			else
				fvalues = ScriptVariable.flistValue(tokenValue, nValues);
			break;
		case Token.string:
			if (sValue == null)
				list = Parser.getTokens(ScriptVariable.sValue(tokenValue));
			break;
		}
		if (list != null) {
			nValues = list.length;
			if (!isStrProperty) {
				fvalues = new float[nValues];
				for (int i = nValues; --i >= 0;)
					fvalues[i] = (tok == Token.element ? Elements
							.elementNumberFromSymbol(list[i], false) : Parser
							.parseFloat(list[i]));
			}
			if (tokenValue.tok != Token.varray && nValues == 1) {
				if (isStrProperty)
					sValue = list[0];
				else
					fValue = fvalues[0];
				iValue = (int) fValue;
				list = null;
				fvalues = null;
			}
		}
		viewer.setAtomProperty(bs, tok, iValue, fValue, sValue, fvalues, list);
	}

	// ///////////////////// general fields //////////////////////

	private final static int scriptLevelMax = 100;

	private Thread currentThread;
	protected Viewer viewer;
	protected ScriptCompiler compiler;
	private Map<String, Object> definedAtomSets;
	private StringBuffer outputBuffer;

	private String contextPath = "";
	private String scriptFileName;
	private String functionName;
	private boolean isStateScript;
	int scriptLevel;
	private int scriptReportingLevel = 0;
	private int commandHistoryLevelMax = 0;

	// created by Compiler:
	private Token[][] aatoken;
	private short[] lineNumbers;
	private int[][] lineIndices;
	private Map<String, ScriptVariable> contextVariables;

	public Map<String, ScriptVariable> getContextVariables() {
		return contextVariables;
	}

	private String script;

	public String getScript() {
		return script;
	}

	// specific to current statement
	protected int pc; // program counter
	private String thisCommand;
	private String fullCommand;
	private Token[] statement;
	private int statementLength;
	private int iToken;
	private int lineEnd;
	private int pcEnd;
	private String scriptExtensions;
	private boolean forceNoAddHydrogens;

	// ////////////////////// supporting methods for compilation and loading
	// //////////

	private boolean compileScript(String filename, String strScript,
			boolean debugCompiler) {
		scriptFileName = filename;
		strScript = fixScriptPath(strScript, filename);
		restoreScriptContext(compiler.compile(filename, strScript, false,
				false, debugCompiler, false), false, false, false);
		isStateScript = (script.indexOf(Viewer.STATE_VERSION_STAMP) >= 0);
		forceNoAddHydrogens = (isStateScript && script
				.indexOf("pdbAddHydrogens") < 0);
		String s = script;
		pc = setScriptExtensions();
		if (!isSyntaxCheck && viewer.isScriptEditorVisible()
				&& strScript.indexOf(JmolConstants.SCRIPT_EDITOR_IGNORE) < 0)
			viewer.scriptStatus("");
		script = s;
		return !error;
	}

	private String fixScriptPath(String strScript, String filename) {
		if (filename != null && strScript.indexOf("$SCRIPT_PATH$") >= 0) {
			String path = filename;
			// we first check for paths into ZIP files and adjust accordingly
			int pt = Math.max(filename.lastIndexOf("|"),
					filename.lastIndexOf("/"));
			path = path.substring(0, pt + 1);
			strScript = TextFormat.simpleReplace(strScript, "$SCRIPT_PATH$/",
					path);
			// now replace the variable itself
			strScript = TextFormat.simpleReplace(strScript, "$SCRIPT_PATH$",
					path);
		}
		return strScript;
	}

	private int setScriptExtensions() {
		String extensions = scriptExtensions;
		if (extensions == null)
			return 0;
		int pt = extensions.indexOf("##SCRIPT_STEP");
		if (pt >= 0) {
			executionStepping = true;
		}
		pt = extensions.indexOf("##SCRIPT_START=");
		if (pt < 0)
			return 0;
		pt = Parser.parseInt(extensions.substring(pt + 15));
		if (pt == Integer.MIN_VALUE)
			return 0;
		for (pc = 0; pc < lineIndices.length; pc++) {
			if (lineIndices[pc][0] > pt || lineIndices[pc][1] >= pt)
				break;
		}
		if (pc > 0 && pc < lineIndices.length && lineIndices[pc][0] > pt)
			--pc;
		return pc;
	}

	public void runScript(String script) throws ScriptException {
		if (!viewer.isPreviewOnly())
			runScript(script, outputBuffer);
	}

	private boolean compileScriptFileInternal(String filename,
			String localPath, String remotePath, String scriptPath) {
		// from "script" command, with push/pop surrounding or viewer
		if (filename.toLowerCase().indexOf("javascript:") == 0)
			return compileScript(filename,
					viewer.jsEval(filename.substring(11)), debugScript);
		String[] data = new String[2];
		data[0] = filename;
		if (!viewer.getFileAsString(data, Integer.MAX_VALUE, false)) { // first
																		// opening
			setErrorMessage("io error reading " + data[0] + ": " + data[1]);
			return false;
		}
		if (("\n" + data[1]).indexOf("\nJmolManifest.txt\n") >= 0) {
			String path;
			if (filename.endsWith(".all.pngj") || filename.endsWith(".all.png")) {
				path = "|state.spt";
				filename += "|";
			} else {
				data[0] = filename += "|JmolManifest.txt";
				if (!viewer.getFileAsString(data, Integer.MAX_VALUE, false)) { // second
																				// entry
					setErrorMessage("io error reading " + data[0] + ": "
							+ data[1]);
					return false;
				}
				path = ZipUtil.getManifestScriptPath(data[1]);
			}
			if (path != null && path.length() > 0) {
				data[0] = filename = filename.substring(0,
						filename.lastIndexOf("|"))
						+ path;
				if (!viewer.getFileAsString(data, Integer.MAX_VALUE, false)) { // third
																				// entry
					setErrorMessage("io error reading " + data[0] + ": "
							+ data[1]);
					return false;
				}
			}
		}
		scriptFileName = filename;
		data[1] = ScriptCompiler.getEmbeddedScript(data[1]);
		String script = fixScriptPath(data[1], data[0]);
		if (scriptPath == null) {
			scriptPath = viewer.getFilePath(filename, false);
			scriptPath = scriptPath.substring(
					0,
					Math.max(scriptPath.lastIndexOf("|"),
							scriptPath.lastIndexOf("/")));
		}
		script = FileManager.setScriptFileReferences(script, localPath,
				remotePath, scriptPath);
		return compileScript(filename, script, debugScript);
	}

	// ///////////// Jmol parameter / user variable / function support
	// ///////////////

	@SuppressWarnings("unchecked")
	private Object getParameter(String key, int tokType) {
		Object v = getContextVariableAsVariable(key);
		if (v == null)
			v = viewer.getParameter(key);
		switch (tokType) {
		case Token.variable:
			return ScriptVariable.getVariable(v);
		case Token.string:
			if (!(v instanceof List<?>))
				break;
			List<ScriptVariable> sv = (ArrayList<ScriptVariable>) v;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < sv.size(); i++)
				sb.append(sv.get(i).asString()).append('\n');
			return sb.toString();
		}
		return (v instanceof ScriptVariable ? ScriptVariable
				.oValue((ScriptVariable) v) : v);
	}

	private String getParameterEscaped(String var) {
		ScriptVariable v = getContextVariableAsVariable(var);
		return (v == null ? "" + viewer.getParameterEscaped(var) : v.escape());
	}

	private String getStringParameter(String var, boolean orReturnName) {
		ScriptVariable v = getContextVariableAsVariable(var);
		if (v != null)
			return v.asString();
		String val = "" + viewer.getParameter(var);
		return (val.length() == 0 && orReturnName ? var : val);
	}

	private Object getNumericParameter(String var) {
		if (var.equalsIgnoreCase("_modelNumber")) {
			int modelIndex = viewer.getCurrentModelIndex();
			return Integer.valueOf(modelIndex < 0 ? 0 : viewer
					.getModelFileNumber(modelIndex));
		}
		ScriptVariable v = getContextVariableAsVariable(var);
		if (v == null) {
			Object val = viewer.getParameter(var);
			if (!(val instanceof String))
				return val;
			v = new ScriptVariable(Token.string, val);
		}
		return ScriptVariable.nValue(v);
	}

	private ScriptVariable getContextVariableAsVariable(String var) {
		if (var.equals("expressionBegin"))
			return null;
		var = var.toLowerCase();
		if (contextVariables != null && contextVariables.containsKey(var))
			return contextVariables.get(var);
		ScriptContext context = thisContext;
		while (context != null) {
			if (context.isFunction == true)
				return null;
			if (context.contextVariables != null
					&& context.contextVariables.containsKey(var))
				return context.contextVariables.get(var);
			context = context.parentContext;
		}
		return null;
	}

	private Object getStringObjectAsVariable(String s, String key) {
		if (s == null || s.length() == 0)
			return s;
		Object v = ScriptVariable.unescapePointOrBitsetAsVariable(s);
		if (v instanceof String && key != null)
			v = viewer
					.setUserVariable(key, new ScriptVariable(Token.string, v));
		return v;
	}

	private ParallelProcessor parallelProcessor;

	@SuppressWarnings("unchecked")
	public float evalFunctionFloat(Object func, Object params, float[] values) {
		try {
			List<ScriptVariable> p = (List<ScriptVariable>) params;
			for (int i = 0; i < values.length; i++)
				p.get(i).value = new Float(values[i]);
			ScriptFunction f = (ScriptFunction) func;
			return ScriptVariable.fValue(runFunction(f, f.name, p, null, true,
					false));
		} catch (Exception e) {
			return Float.NaN;
		}

	}

	ScriptVariable runFunction(ScriptFunction function, String name,
			List<ScriptVariable> params, ScriptVariable tokenAtom,
			boolean getReturn, boolean setContextPath) throws ScriptException {
		if (function == null) {
			// general function call
			function = viewer.getFunction(name);
			if (function == null)
				return null;
			if (setContextPath)
				contextPath += " >> function " + name;
		} else if (setContextPath) {
			// "try"; not from evalFunctionFloat
			contextPath += " >> " + name;
		}
		pushContext(null);
		boolean isTry = (function.tok == Token.trycmd);
		thisContext.isFunction = !isTry;
		functionName = name;

		if (function instanceof ParallelProcessor) {
			synchronized (function) // can't do this -- too general
			{
				parallelProcessor = (ParallelProcessor) function;
				vProcess = null;
				runFunction(function, params, tokenAtom);

				ScriptContext sc = getScriptContext();
				if (isTry) {
					contextVariables.put("_breakval", new ScriptVariableInt(
							Integer.MAX_VALUE));
					contextVariables.put("_errorval",
							ScriptVariable.getVariable(""));
					viewer.resetError();
					parallelProcessor.addProcess("try", sc);
				}
				((ParallelProcessor) function).runAllProcesses(viewer, !isTry);
				if (isTry) {
					String err = (String) viewer.getParameter("_errormessage");
					if (err.length() > 0) {
						contextVariables.put("_errorval",
								ScriptVariable.getVariable(err));
						viewer.resetError();
					}
					contextVariables.put("_tryret",
							contextVariables.get("_retval"));
					contextVariables.put("_retval", new ScriptVariable(0,
							contextVariables));
				}
			}
		} else {
			runFunction(function, params, tokenAtom);
		}
		ScriptVariable v = (getReturn ? getContextVariableAsVariable("_retval")
				: null);
		popContext(false, false);
		return v;
	}

	private void runFunction(ScriptFunction function,
			List<ScriptVariable> params, ScriptVariable tokenAtom)
			throws ScriptException {
		aatoken = function.aatoken;
		lineNumbers = function.lineNumbers;
		lineIndices = function.lineIndices;
		script = function.script;
		pc = 0;
		if (function.names != null) {
			contextVariables = new Hashtable<String, ScriptVariable>();
			function.setVariables(contextVariables, params);
		}
		if (tokenAtom != null)
			contextVariables.put("_x", tokenAtom);
		if (function.tok != Token.trycmd)
			instructionDispatchLoop(false);
	}

	private void clearDefinedVariableAtomSets() {
		definedAtomSets.remove("# variable");
	}

	/**
	 * support for @xxx or define xxx commands
	 * 
	 */
	private void defineSets() {
		if (!definedAtomSets.containsKey("# static")) {
			for (int i = 0; i < JmolConstants.predefinedStatic.length; i++)
				defineAtomSet(JmolConstants.predefinedStatic[i]);
			defineAtomSet("# static");
		}
		if (definedAtomSets.containsKey("# variable"))
			return;
		for (int i = 0; i < JmolConstants.predefinedVariable.length; i++)
			defineAtomSet(JmolConstants.predefinedVariable[i]);
		// Now, define all the elements as predefined sets

		// name ==> elemno=n for all standard elements, isotope-blind
		// _Xx ==> elemno=n for of all elements, isotope-blind
		for (int i = Elements.elementNumberMax; --i >= 0;) {
			String definition = " elemno=" + i;
			defineAtomSet("@" + Elements.elementNameFromNumber(i) + definition);
			defineAtomSet("@_" + Elements.elementSymbolFromNumber(i)
					+ definition);
		}
		// name ==> _e=nn for each alternative element
		for (int i = Elements.firstIsotope; --i >= 0;) {
			String definition = "@" + Elements.altElementNameFromIndex(i)
					+ " _e=" + Elements.altElementNumberFromIndex(i);
			defineAtomSet(definition);
		}
		// these variables _e, _x can't be more than two characters
		// name ==> _isotope=iinn for each isotope
		// _T ==> _isotope=iinn for each isotope
		// _3H ==> _isotope=iinn for each isotope
		for (int i = Elements.altElementMax; --i >= Elements.firstIsotope;) {
			short ei = Elements.altElementNumberFromIndex(i);
			String def = " _e=" + ei;
			String definition = "@_" + Elements.altElementSymbolFromIndex(i);
			defineAtomSet(definition + def);

			definition = "@_" + Elements.altIsotopeSymbolFromIndex(i);
			defineAtomSet(definition + def);
			definition = "@_" + Elements.altIsotopeSymbolFromIndex2(i);
			defineAtomSet(definition + def);

			definition = "@" + Elements.altElementNameFromIndex(i);
			if (definition.length() > 1)
				defineAtomSet(definition + def);

			// @_12C _e=6
			// @_C12 _e=6
			short e = Elements.getElementNumber(ei);
			ei = (short) Elements.getNaturalIsotope(e);
			if (ei > 0) {
				def = Elements.elementSymbolFromNumber(e);
				defineAtomSet("@_" + def + ei + " _e=" + e);
				defineAtomSet("@_" + ei + def + " _e=" + e);
			}
		}
		defineAtomSet("# variable");
	}

	private void defineAtomSet(String script) {
		if (script.indexOf("#") == 0) {
			definedAtomSets.put(script, Boolean.TRUE);
			return;
		}
		ScriptContext sc = compiler.compile("#predefine", script, true, false,
				false, false);
		if (sc.errorType != null) {
			viewer.scriptStatus("JmolConstants.java ERROR: predefined set compile error:"
					+ script + "\ncompile error:" + sc.errorMessageUntranslated);
			return;
		}

		if (sc.aatoken.length != 1) {
			viewer.scriptStatus("JmolConstants.java ERROR: predefinition does not have exactly 1 command:"
					+ script);
			return;
		}
		Token[] statement = sc.aatoken[0];
		if (statement.length <= 2) {
			viewer.scriptStatus("JmolConstants.java ERROR: bad predefinition length:"
					+ script);
			return;
		}
		int tok = statement[1].tok;
		if (!Token.tokAttr(tok, Token.identifier)
				&& !Token.tokAttr(tok, Token.predefinedset)) {
			viewer.scriptStatus("JmolConstants.java ERROR: invalid variable name:"
					+ script);
			return;
		}
		String name = ((String) statement[1].value).toLowerCase();
		if (name.startsWith("dynamic_"))
			name = "!" + name.substring(8);
		definedAtomSets.put(name, statement);
	}

	private BitSet lookupIdentifierValue(String identifier)
			throws ScriptException {
		// all variables and possible residue names for PDB
		// or atom names for non-pdb atoms are processed here.

		// priority is given to a defined variable.

		BitSet bs = lookupValue(identifier, false);
		if (bs != null)
			return BitSetUtil.copy(bs);

		// next we look for names of groups (PDB) or atoms (non-PDB)
		bs = getAtomBits(Token.identifier, identifier);
		return (bs == null ? new BitSet() : bs);
	}

	private BitSet lookupValue(String setName, boolean plurals)
			throws ScriptException {
		if (isSyntaxCheck) {
			return new BitSet();
		}
		defineSets();
		setName = setName.toLowerCase();
		Object value = definedAtomSets.get(setName);
		boolean isDynamic = false;
		if (value == null) {
			value = definedAtomSets.get("!" + setName);
			isDynamic = (value != null);
		}
		if (value instanceof BitSet)
			return (BitSet) value;
		if (value instanceof Token[]) {
			pushContext(null);
			BitSet bs = atomExpression((Token[]) value, -2, 0, true, false,
					true, true);
			popContext(false, false);
			if (!isDynamic)
				definedAtomSets.put(setName, bs);
			return bs;
		}
		if (plurals)
			return null;
		int len = setName.length();
		if (len < 5) // iron is the shortest
			return null;
		if (setName.charAt(len - 1) != 's')
			return null;
		if (setName.endsWith("ies"))
			setName = setName.substring(0, len - 3) + 'y';
		else
			setName = setName.substring(0, len - 1);
		return lookupValue(setName, true);
	}

	public void deleteAtomsInVariables(BitSet bsDeleted) {
		for (Map.Entry<String, Object> entry : definedAtomSets.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof BitSet) {
				BitSetUtil.deleteBits((BitSet) value, bsDeleted);
				if (!entry.getKey().startsWith("!"))
					viewer.setUserVariable("@" + entry.getKey(),
							ScriptVariable.getVariable(value));
			}
		}
	}

	/**
	 * provides support for @x and @{....} in statements. The compiler passes on
	 * these, because they must be integrated with the statement dynamically.
	 * 
	 * @param pc
	 * @return a fixed token set -- with possible overrun of unused null tokens
	 * 
	 * @throws ScriptException
	 */
	@SuppressWarnings("unchecked")
	private boolean setStatement(int pc) throws ScriptException {
		statement = aatoken[pc];
		statementLength = statement.length;
		if (statementLength == 0)
			return true;
		Token[] fixed;
		int i;
		int tok;
		for (i = 1; i < statementLength; i++) {
			if (statement[i] == null) {
				statementLength = i;
				return true;
			}
			if (statement[i].tok == Token.define)
				break;
		}
		if (i == statementLength)// || isScriptCheck)
			return i == statementLength;
		switch (statement[0].tok) {
		case Token.parallel:
		case Token.function:
		case Token.identifier:
			if (tokAt(1) == Token.leftparen)
				return true;
		}
		fixed = new Token[statementLength];
		fixed[0] = statement[0];
		boolean isExpression = false;
		int j = 1;
		for (i = 1; i < statementLength; i++) {
			if (statement[i] == null)
				continue;
			switch (tok = getToken(i).tok) {
			default:
				fixed[j] = statement[i];
				break;
			case Token.expressionBegin:
			case Token.expressionEnd:
				// @ in expression will be taken as SELECT
				isExpression = (tok == Token.expressionBegin);
				fixed[j] = statement[i];
				break;
			case Token.define:
				if (++i == statementLength)
					error(ERROR_invalidArgument);
				Object v;
				// compiler can indicate that a definition MUST
				// be interpreted as a String
				boolean forceString = (theToken.intValue == Token.string);
				// Object var_set;
				String s;
				String var = parameterAsString(i);
				boolean isClauseDefine = (tokAt(i) == Token.expressionBegin);
				boolean isSetAt = (j == 1 && statement[0] == Token.tokenSetCmd);
				if (isClauseDefine) {
					ScriptVariable vt = parameterExpressionToken(++i);
					i = iToken;
					v = (vt.tok == Token.varray ? vt : ScriptVariable
							.oValue(vt));
				} else {
					if (tokAt(i) == Token.integer) {
						v = viewer.getAtomBits(Token.atomno,
								Integer.valueOf(statement[i].intValue));
					} else {
						v = getParameter(var, 0);
					}
					if (!isExpression && !isSetAt)
						isClauseDefine = true;
				}
				tok = tokAt(0);
				forceString |= (Token.tokAttr(tok, Token.implicitStringCommand) || tok == Token.script); // for
																											// the
																											// file
																											// names
				if (v instanceof ScriptVariable) {
					// select @{...}
					fixed[j] = (Token) v;
					if (isExpression && fixed[j].tok == Token.varray) {
						BitSet bs = ScriptVariable.getBitSet(
								(ScriptVariable) v, true);
						// I can't remember why we have to be checking list
						// variables
						// for atom names.
						fixed[j] = new ScriptVariable(Token.bitset,
								bs == null ? getAtomBitSet(this,
										ScriptVariable.sValue(fixed[j])) : bs);
					}
				} else if (v instanceof Boolean) {
					fixed[j] = (((Boolean) v).booleanValue() ? Token.tokenOn
							: Token.tokenOff);
				} else if (v instanceof Integer) {
					// if (isExpression && !isClauseDefine
					// && (var_set = getParameter(var + "_set", false)) != null)
					// fixed[j] = new Token(Token.define, "" + var_set);
					// else
					fixed[j] = new Token(Token.integer,
							((Integer) v).intValue(), v);

				} else if (v instanceof Float) {
					fixed[j] = new Token(Token.decimal, getFloatEncodedInt(""
							+ v), v);
				} else if (v instanceof String) {
					if (!forceString) {
						if ((tok != Token.set || j > 1
								&& statement[1].tok != Token.echo)
								&& Token.tokAttr(tok,
										Token.mathExpressionCommand)) {
							v = getParameter((String) v, Token.variable);
						}
						if (v instanceof String) {
							v = getStringObjectAsVariable((String) v, null);
						}
					}
					if (v instanceof ScriptVariable) {
						// was a bitset
						fixed[j] = (Token) v;
					} else {
						s = (String) v;
						if (isExpression && !forceString) {
							// select @x where x is "arg", for example
							fixed[j] = new Token(Token.bitset, getAtomBitSet(
									this, s));
						} else {
							if (!isExpression) {
								// print @x
							}

							// bit of a hack here....
							// identifiers cannot have periods; file names can,
							// though
							// TODO: this is still a hack
							// what we really need to know is what the compiler
							// expects here -- a string or an identifier,
							// because
							// they will be processed differently.
							// a filename with only letters and numbers will be
							// read incorrectly here as an identifier.

							// note that command keywords cannot be implemented
							// as variables
							// because they are not Token.identifiers in the
							// first place.
							// but the identifier tok is important here because
							// just below
							// there is a check for SET parameter name
							// assignments.
							// even those may not work...

							tok = (isSetAt ? Token.getTokFromName(s)
									: isClauseDefine || forceString
											|| s.length() == 0
											|| s.indexOf(".") >= 0
											|| s.indexOf(" ") >= 0
											|| s.indexOf("=") >= 0
											|| s.indexOf(";") >= 0
											|| s.indexOf("[") >= 0
											|| s.indexOf("{") >= 0 ? Token.string
											: Token.identifier);
							fixed[j] = new Token(tok, v);
						}
					}
				} else if (v instanceof BitSet) {
					fixed[j] = new ScriptVariable(Token.bitset, v);
				} else if (v instanceof Point3f) {
					fixed[j] = new ScriptVariable(Token.point3f, v);
				} else if (v instanceof Point4f) {
					fixed[j] = new ScriptVariable(Token.point4f, v);
				} else if (v instanceof Matrix3f) {
					fixed[j] = new ScriptVariable(Token.matrix3f, v);
				} else if (v instanceof Matrix4f) {
					fixed[j] = new ScriptVariable(Token.matrix4f, v);
				} else if (v instanceof Map<?, ?>) {
					fixed[j] = new ScriptVariable(Token.hash, v);
				} else if (v instanceof List<?>) {
					List<ScriptVariable> sv = (ArrayList<ScriptVariable>) v;
					BitSet bs = null;
					for (int k = 0; k < sv.size(); k++) {
						ScriptVariable svk = sv.get(k);
						if (svk.tok != Token.bitset) {
							bs = null;
							break;
						}
						if (bs == null)
							bs = new BitSet();
						bs.or((BitSet) svk.value);
					}
					fixed[j] = (bs == null ? ScriptVariable.getVariable(v)
							: new Token(Token.bitset, bs));
				} else {
					Point3f center = getObjectCenter(var, Integer.MIN_VALUE,
							Integer.MIN_VALUE);
					if (center == null)
						error(ERROR_invalidArgument);
					fixed[j] = new Token(Token.point3f, center);
				}
				if (isSetAt && !Token.tokAttr(fixed[j].tok, Token.setparam))
					error(ERROR_invalidArgument);
				break;
			}

			j++;
		}
		statement = fixed;
		for (i = j; i < statement.length; i++)
			statement[i] = null;
		statementLength = j;

		return true;
	}

	// ///////////////// Script context support //////////////////////

	private void clearState(boolean tQuiet) {
		thisContext = null;
		scriptLevel = 0;
		setErrorMessage(null);
		contextPath = "";
		this.tQuiet = tQuiet;
	}

	protected ScriptContext thisContext = null;

	private void pushContext(ContextToken token) throws ScriptException {
		if (scriptLevel == scriptLevelMax)
			error(ERROR_tooManyScriptLevels);
		thisContext = getScriptContext();
		thisContext.token = token;
		if (token == null) {
			scriptLevel = ++thisContext.scriptLevel;
		} else {
			thisContext.scriptLevel = -1;
			contextVariables = new Hashtable<String, ScriptVariable>();
			if (token.contextVariables != null)
				for (String key : token.contextVariables.keySet())
					ScriptCompiler.addContextVariable(contextVariables, key);
		}
		if (debugScript || isCmdLine_c_or_C_Option)
			Logger.info("-->>-------------".substring(0,
					Math.max(17, scriptLevel + 5))
					+ scriptLevel
					+ " "
					+ scriptFileName
					+ " "
					+ token
					+ " "
					+ thisContext);
	}

	public ScriptContext getScriptContext() {
		ScriptContext context = new ScriptContext();
		context.scriptLevel = scriptLevel;
		context.parentContext = thisContext;
		context.contextPath = contextPath;
		context.scriptFileName = scriptFileName;
		context.parallelProcessor = parallelProcessor;
		context.functionName = functionName;
		context.script = script;
		context.lineNumbers = lineNumbers;
		context.lineIndices = lineIndices;
		context.aatoken = aatoken;

		context.statement = statement;
		context.statementLength = statementLength;
		context.pc = pc;
		context.lineEnd = lineEnd;
		context.pcEnd = pcEnd;
		context.iToken = iToken;
		context.outputBuffer = outputBuffer;
		context.contextVariables = contextVariables;
		context.isStateScript = isStateScript;

		context.errorMessage = errorMessage;
		context.errorType = errorType;
		context.iCommandError = iCommandError;
		context.isSyntaxCheck = isSyntaxCheck;
		context.executionStepping = executionStepping;
		context.executionPaused = executionPaused;
		context.scriptExtensions = scriptExtensions;
		return context;
	}

	void popContext(boolean isFlowCommand, boolean statementOnly) {
		if (thisContext == null)
			return;
		if (thisContext.scriptLevel > 0)
			scriptLevel = thisContext.scriptLevel - 1;
		// we must save (and thus NOT restore) the current statement
		// business when doing push/pop for commands like FOR and WHILE
		ScriptContext scTemp = (isFlowCommand ? getScriptContext() : null);
		restoreScriptContext(thisContext, true, isFlowCommand, statementOnly);
		restoreScriptContext(scTemp, true, false, true);
		if (debugScript || isCmdLine_c_or_C_Option)
			Logger.info("--<<-------------".substring(0,
					Math.max(17, scriptLevel + 5))
					+ scriptLevel
					+ " "
					+ scriptFileName
					+ " "
					+ (thisContext == null ? "" : "" + thisContext.token)
					+ " "
					+ thisContext);
	}

	private void restoreScriptContext(ScriptContext context,
			boolean isPopContext, boolean isFlowCommand, boolean statementOnly) {

		if (context == null)
			return;
		if (!isFlowCommand) {
			statement = context.statement;
			statementLength = context.statementLength;
			pc = context.pc;
			lineEnd = context.lineEnd;
			pcEnd = context.pcEnd;
			if (statementOnly)
				return;
		}
		script = context.script;
		lineNumbers = context.lineNumbers;
		lineIndices = context.lineIndices;
		aatoken = context.aatoken;
		contextVariables = context.contextVariables;
		scriptExtensions = context.scriptExtensions;
		if (isPopContext) {
			contextPath = context.contextPath;
			scriptFileName = context.scriptFileName;
			parallelProcessor = context.parallelProcessor;
			functionName = context.functionName;
			iToken = context.iToken;
			outputBuffer = context.outputBuffer;
			isStateScript = context.isStateScript;
			thisContext = context.parentContext;
		} else {
			error = (context.errorType != null);
			errorMessage = context.errorMessage;
			errorMessageUntranslated = context.errorMessageUntranslated;
			iCommandError = context.iCommandError;
			errorType = context.errorType;
		}
	}

	private String getContext(boolean withVariables) {
		StringBuffer sb = new StringBuffer();
		ScriptContext context = thisContext;
		while (context != null) {
			if (withVariables) {
				if (context.contextVariables != null) {
					sb.append(getScriptID(context));
					sb.append(StateManager.getVariableList(
							context.contextVariables, 80, true, false));
				}
			} else {
				sb.append(setErrorLineMessage(
						context.functionName,
						context.scriptFileName,
						getLinenumber(context),
						context.pc,
						statementAsString(context.statement, -9999, logMessages)));
			}
			context = context.parentContext;
		}
		if (withVariables) {
			if (contextVariables != null) {
				sb.append(getScriptID(null));
				sb.append(StateManager.getVariableList(contextVariables, 80,
						true, false));
			}
		} else {
			sb.append(setErrorLineMessage(functionName, scriptFileName,
					getLinenumber(null), pc,
					statementAsString(statement, -9999, logMessages)));
		}

		return sb.toString();
	}

	private int getLinenumber(ScriptContext c) {
		return (c == null ? lineNumbers[pc] : c.lineNumbers[c.pc]);
	}

	private String getScriptID(ScriptContext context) {
		String fuName = (context == null ? functionName : "function "
				+ context.functionName);
		String fiName = (context == null ? scriptFileName
				: context.scriptFileName);
		return "\n# " + fuName + " (file " + fiName + ")\n";
	}

	// /////////////// error message support /////////////////

	private boolean error;
	private String errorMessage;
	protected String errorMessageUntranslated;
	protected String errorType;
	protected int iCommandError;

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorMessageUntranslated() {
		return errorMessageUntranslated == null ? errorMessage
				: errorMessageUntranslated;
	}

	private void setErrorMessage(String err) {
		errorMessageUntranslated = null;
		if (err == null) {
			error = false;
			errorType = null;
			errorMessage = null;
			iCommandError = -1;
			return;
		}
		error = true;
		if (errorMessage == null) // there could be a compiler error from a
									// script
			// command
			errorMessage = GT._("script ERROR: ");
		errorMessage += err;
	}

	private boolean ignoreError;

	private void planeExpected() throws ScriptException {
		error(ERROR_planeExpected, "{a b c d}",
				"\"xy\" \"xz\" \"yz\" \"x=...\" \"y=...\" \"z=...\"", "$xxxxx");
	}

	private void integerOutOfRange(int min, int max) throws ScriptException {
		error(ERROR_integerOutOfRange, "" + min, "" + max);
	}

	private void numberOutOfRange(float min, float max) throws ScriptException {
		error(ERROR_numberOutOfRange, "" + min, "" + max);
	}

	void error(int iError, int i) throws ScriptException {
		iToken = i;
		error(iError, null, null, null, false);
	}

	void error(int iError) throws ScriptException {
		error(iError, null, null, null, false);
	}

	void error(int iError, String value) throws ScriptException {
		error(iError, value, null, null, false);
	}

	void error(int iError, String value, String more) throws ScriptException {
		error(iError, value, more, null, false);
	}

	void error(int iError, String value, String more, String more2)
			throws ScriptException {
		error(iError, value, more, more2, false);
	}

	private void warning(int iError, String value, String more)
			throws ScriptException {
		error(iError, value, more, null, true);
	}

	void error(int iError, String value, String more, String more2,
			boolean warningOnly) throws ScriptException {
		String strError = ignoreError ? null : errorString(iError, value, more,
				more2, true);
		String strUntranslated = (!ignoreError && GT.getDoTranslate() ? errorString(
				iError, value, more, more2, false) : null);
		if (!warningOnly)
			evalError(strError, strUntranslated);
		showString(strError);
	}

	void evalError(String message, String strUntranslated)
			throws ScriptException {
		if (ignoreError)
			throw new NullPointerException();
		if (!isSyntaxCheck) {
			// String s = viewer.getSetHistory(1);
			// viewer.addCommand(s + CommandHistory.ERROR_FLAG);
			setCursorWait(false);
			viewer.setBooleanProperty("refreshing", true);
			viewer.setStringProperty("_errormessage", strUntranslated);
		}
		throw new ScriptException(message, strUntranslated);
	}

	final static int ERROR_axisExpected = 0;
	final static int ERROR_backgroundModelError = 1;
	final static int ERROR_badArgumentCount = 2;
	final static int ERROR_badMillerIndices = 3;
	final static int ERROR_badRGBColor = 4;
	final static int ERROR_booleanExpected = 5;
	final static int ERROR_booleanOrNumberExpected = 6;
	final static int ERROR_booleanOrWhateverExpected = 7;
	final static int ERROR_colorExpected = 8;
	final static int ERROR_colorOrPaletteRequired = 9;
	final static int ERROR_commandExpected = 10;
	final static int ERROR_coordinateOrNameOrExpressionRequired = 11;
	final static int ERROR_drawObjectNotDefined = 12;
	final static int ERROR_endOfStatementUnexpected = 13;
	final static int ERROR_expressionExpected = 14;
	final static int ERROR_expressionOrIntegerExpected = 15;
	final static int ERROR_filenameExpected = 16;
	final static int ERROR_fileNotFoundException = 17;
	final static int ERROR_incompatibleArguments = 18;
	final static int ERROR_insufficientArguments = 19;
	final static int ERROR_integerExpected = 20;
	final static int ERROR_integerOutOfRange = 21;
	final static int ERROR_invalidArgument = 22;
	final static int ERROR_invalidParameterOrder = 23;
	final static int ERROR_keywordExpected = 24;
	final static int ERROR_moCoefficients = 25;
	final static int ERROR_moIndex = 26;
	final static int ERROR_moModelError = 27;
	final static int ERROR_moOccupancy = 28;
	final static int ERROR_moOnlyOne = 29;
	final static int ERROR_multipleModelsDisplayedNotOK = 30;
	final static int ERROR_noData = 31;
	final static int ERROR_noPartialCharges = 32;
	final static int ERROR_noUnitCell = 33;
	final static int ERROR_numberExpected = 34;
	final static int ERROR_numberMustBe = 35;
	final static int ERROR_numberOutOfRange = 36;
	final static int ERROR_objectNameExpected = 37;
	final static int ERROR_planeExpected = 38;
	final static int ERROR_propertyNameExpected = 39;
	final static int ERROR_spaceGroupNotFound = 40;
	final static int ERROR_stringExpected = 41;
	final static int ERROR_stringOrIdentifierExpected = 42;
	final static int ERROR_tooManyPoints = 43;
	final static int ERROR_tooManyScriptLevels = 44;
	final static int ERROR_unrecognizedAtomProperty = 45;
	final static int ERROR_unrecognizedBondProperty = 46;
	final static int ERROR_unrecognizedCommand = 47;
	final static int ERROR_unrecognizedExpression = 48;
	final static int ERROR_unrecognizedObject = 49;
	final static int ERROR_unrecognizedParameter = 50;
	final static int ERROR_unrecognizedParameterWarning = 51;
	final static int ERROR_unrecognizedShowParameter = 52;
	final static int ERROR_what = 53;
	final static int ERROR_writeWhat = 54;
	final static int ERROR_multipleModelsNotOK = 55;
	final static int ERROR_cannotSet = 56;
	// added -hcf
	final static int ERROR_scaleDownFileNotExist = 57;
	final static int ERROR_scaleUpFileNotExist = 58;
	final static int ERROR_noSelectedUnits = 59;
	final static int ERROR_selectedTooMuchUnits = 60;
	final static int ERROR_onlyChrScaleAllowed = 61;
	final static int ERROR_wrongSelectCommand = 62;
	final static int ERROR_gssWrongFileName = 63;
	final static int ERROR_gpdbWrongFileName = 64;
	final static int ERROR_cannotFindGenomeSeq = 65;
	final static int ERROR_cannotFindSpecies = 66;
	final static int ERROR_xyzInvalid = 67;
	final static int ERROR_noGssForPDB = 68;
	final static int ERROR_notFASTAformat = 69;
	final static int ERROR_seqTooLong = 70;
	final static int ERROR_cannotSpecificSpecies = 71;

	// added end -hcf

	/**
	 * @param iError
	 * @param value
	 * @param more
	 * @param more2
	 * @param translated
	 * @return constructed error string
	 * 
	 */
	static String errorString(int iError, String value, String more,
			String more2, boolean translated) {
		boolean doTranslate = false;
		if (!translated && (doTranslate = GT.getDoTranslate()) == true)
			GT.setDoTranslate(false);
		String msg;
		switch (iError) {
		default:
			msg = "Unknown error message number: " + iError;
			break;
		case ERROR_axisExpected:
			msg = GT._("x y z axis expected");
			break;
		case ERROR_backgroundModelError:
			msg = GT._("{0} not allowed with background model displayed");
			break;
		case ERROR_badArgumentCount:
			msg = GT._("bad argument count");
			break;
		case ERROR_badMillerIndices:
			msg = GT._("Miller indices cannot all be zero.");
			break;
		case ERROR_badRGBColor:
			msg = GT._("bad [R,G,B] color");
			break;
		case ERROR_booleanExpected:
			msg = GT._("boolean expected");
			break;
		case ERROR_booleanOrNumberExpected:
			msg = GT._("boolean or number expected");
			break;
		case ERROR_booleanOrWhateverExpected:
			msg = GT._("boolean, number, or {0} expected");
			break;
		case ERROR_cannotSet:
			msg = GT._("cannot set value");
			break;
		case ERROR_colorExpected:
			msg = GT._("color expected");
			break;
		case ERROR_colorOrPaletteRequired:
			msg = GT._("a color or palette name (Jmol, Rasmol) is required");
			break;
		case ERROR_commandExpected:
			msg = GT._("command expected");
			break;
		case ERROR_coordinateOrNameOrExpressionRequired:
			msg = GT._("{x y z} or $name or (atom expression) required");
			break;
		case ERROR_drawObjectNotDefined:
			msg = GT._("draw object not defined");
			break;
		case ERROR_endOfStatementUnexpected:
			msg = GT._("unexpected end of script command");
			break;
		case ERROR_expressionExpected:
			msg = GT._("valid (atom expression) expected");
			break;
		case ERROR_expressionOrIntegerExpected:
			msg = GT._("(atom expression) or integer expected");
			break;
		case ERROR_filenameExpected:
			msg = GT._("filename expected");
			break;
		case ERROR_fileNotFoundException:
			msg = GT._("file not found");
			break;
		case ERROR_incompatibleArguments:
			msg = GT._("incompatible arguments");
			break;
		case ERROR_insufficientArguments:
			msg = GT._("insufficient arguments");
			break;
		case ERROR_integerExpected:
			msg = GT._("integer expected");
			break;
		case ERROR_integerOutOfRange:
			msg = GT._("integer out of range ({0} - {1})");
			break;
		case ERROR_invalidArgument:
			msg = GT._("invalid argument");
			break;
		// added -hcf
		case ERROR_scaleDownFileNotExist:
			msg = GT._("could not find file of specified level");
			break;
		case ERROR_scaleUpFileNotExist:
			msg = GT._("could not find file of specified level");
			break;
		case ERROR_noSelectedUnits:
			msg = GT._("please select the units");
			break;
		case ERROR_selectedTooMuchUnits:
			msg = GT._("please select only one unit");
			break;
		case ERROR_onlyChrScaleAllowed:
			msg = GT._("Too big for global visualization");
			break;
		case ERROR_wrongSelectCommand:
			msg = GT._("Wrong select command");
			break;
		case ERROR_gssWrongFileName:
			msg = GT._("Possibly GSS file name is not correct");
			break;
		case ERROR_gpdbWrongFileName:
			msg = GT._("Possibly GPDB file name is not correct");
			break;
		case ERROR_cannotFindGenomeSeq:
			msg = GT._("Cannot find specified genome sequence");
			break;
		case ERROR_cannotSpecificSpecies:
			msg = GT._("Cannot specify genome Species");
			break;
		case ERROR_cannotFindSpecies:
			msg = GT._("Cannot find specified species, check the name please");
			break;
		case ERROR_xyzInvalid:
			msg = GT._("Cannot transform to PDB format, as x/y/z coordinate is not with 1-8 long");
			break;
		case ERROR_noGssForPDB:
			msg = GT._("No GSS unit for extract PDB file");
			break;
		case ERROR_notFASTAformat:
			msg = GT._("Unrecognize genome sequence format, please use FASTA format.");
			break;
		//added lxq35
		case ERROR_seqTooLong:
			msg = GT._("Input sequence is too long to handle, please choose a shorter one.");
			break;
		// added end -hcf
		case ERROR_invalidParameterOrder:
			msg = GT._("invalid parameter order");
			break;
		case ERROR_keywordExpected:
			msg = GT._("keyword expected");
			break;
		case ERROR_moCoefficients:
			msg = GT._("no MO coefficient data available");
			break;
		case ERROR_moIndex:
			msg = GT._("An MO index from 1 to {0} is required");
			break;
		case ERROR_moModelError:
			msg = GT._("no MO basis/coefficient data available for this frame");
			break;
		case ERROR_moOccupancy:
			msg = GT._("no MO occupancy data available");
			break;
		case ERROR_moOnlyOne:
			msg = GT._("Only one molecular orbital is available in this file");
			break;
		case ERROR_multipleModelsDisplayedNotOK:
			msg = GT._("{0} require that only one model be displayed");
			break;
		case ERROR_multipleModelsNotOK:
			msg = GT._("{0} requires that only one model be loaded");
			break;
		case ERROR_noData:
			msg = GT._("No data available");
			break;
		case ERROR_noPartialCharges:
			msg = GT._("No partial charges were read from the file; Jmol needs these to render the MEP data.");
			break;
		case ERROR_noUnitCell:
			msg = GT._("No unit cell");
			break;
		case ERROR_numberExpected:
			msg = GT._("number expected");
			break;
		case ERROR_numberMustBe:
			msg = GT._("number must be ({0} or {1})");
			break;
		case ERROR_numberOutOfRange:
			msg = GT._("decimal number out of range ({0} - {1})");
			break;
		case ERROR_objectNameExpected:
			msg = GT._("object name expected after '$'");
			break;
		case ERROR_planeExpected:
			msg = GT._("plane expected -- either three points or atom expressions or {0} or {1} or {2}");
			break;
		case ERROR_propertyNameExpected:
			msg = GT._("property name expected");
			break;
		case ERROR_spaceGroupNotFound:
			msg = GT._("space group {0} was not found.");
			break;
		case ERROR_stringExpected:
			msg = GT._("quoted string expected");
			break;
		case ERROR_stringOrIdentifierExpected:
			msg = GT._("quoted string or identifier expected");
			break;
		case ERROR_tooManyPoints:
			msg = GT._("too many rotation points were specified");
			break;
		case ERROR_tooManyScriptLevels:
			msg = GT._("too many script levels");
			break;
		case ERROR_unrecognizedAtomProperty:
			msg = GT._("unrecognized atom property");
			break;
		case ERROR_unrecognizedBondProperty:
			msg = GT._("unrecognized bond property");
			break;
		case ERROR_unrecognizedCommand:
			msg = GT._("unrecognized command");
			break;
		case ERROR_unrecognizedExpression:
			msg = GT._("runtime unrecognized expression");
			break;
		case ERROR_unrecognizedObject:
			msg = GT._("unrecognized object");
			break;
		case ERROR_unrecognizedParameter:
			msg = GT._("unrecognized {0} parameter");
			break;
		case ERROR_unrecognizedParameterWarning:
			msg = GT._("unrecognized {0} parameter in Jmol state script (set anyway)");
			break;
		case ERROR_unrecognizedShowParameter:
			msg = GT._("unrecognized SHOW parameter --  use {0}");
			break;
		case ERROR_what:
			msg = "{0}";
			break;
		case ERROR_writeWhat:
			msg = GT._("write what? {0} or {1} \"filename\"");
			break;
		}
		if (msg.indexOf("{0}") < 0) {
			if (value != null)
				msg += ": " + value;
		} else {
			msg = TextFormat.simpleReplace(msg, "{0}", value);
			if (msg.indexOf("{1}") >= 0)
				msg = TextFormat.simpleReplace(msg, "{1}", more);
			else if (more != null)
				msg += ": " + more;
			if (msg.indexOf("{2}") >= 0)
				msg = TextFormat.simpleReplace(msg, "{2}", more);
		}
		if (doTranslate)
			GT.setDoTranslate(true);
		return msg;
	}

	static String setErrorLineMessage(String functionName, String filename,
			int lineCurrent, int pcCurrent, String lineInfo) {
		String err = "\n----";
		if (filename != null || functionName != null)
			err += "line "
					+ lineCurrent
					+ " command "
					+ (pcCurrent + 1)
					+ " of "
					+ (functionName == null ? filename : functionName
							.equals("try") ? "try" : "function " + functionName)
					+ ":";
		err += "\n         " + lineInfo;
		return err;
	}

	class ScriptException extends Exception {

		private String message;
		private String untranslated;

		ScriptException(String msg, String untranslated) {
			errorType = message = msg;
			iCommandError = pc;
			this.untranslated = (untranslated == null ? msg : untranslated);
			if (message == null) {
				message = "";
				return;
			}
			String s = getScriptContext().getContextTrace(null, true)
					.toString();
			while (thisContext != null)
				popContext(false, false);
			message += s;
			this.untranslated += s;
			if (isSyntaxCheck
					|| msg.indexOf("file recognized as a script file:") >= 0)
				return;
			Logger.error("eval ERROR: " + toString());
			if (viewer.autoExit)
				viewer.exitJmol();
		}

		protected String getErrorMessageUntranslated() {
			return untranslated;
		}

		@Override
		public String toString() {
			return message;
		}
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Eval\n pc:");
		str.append(pc);
		str.append("\n");
		str.append(aatoken.length);
		str.append(" statements\n");
		for (int i = 0; i < aatoken.length; ++i) {
			str.append("----\n");
			Token[] atoken = aatoken[i];
			for (int j = 0; j < atoken.length; ++j) {
				str.append(atoken[j]);
				str.append('\n');
			}
			str.append('\n');
		}
		str.append("END\n");
		return str.toString();
	}

	static String statementAsString(Token[] statement, int iTok,
			boolean doLogMessages) {
		if (statement.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		int tok = statement[0].tok;
		switch (tok) {
		case Token.nada:
			return (String) statement[0].value;
		case Token.end:
			if (statement.length == 2
					&& (statement[1].tok == Token.function || statement[1].tok == Token.parallel))
				return ((ScriptFunction) (statement[1].value)).toString();
		}
		boolean useBraces = true;// (!Token.tokAttr(tok,
		// Token.atomExpressionCommand));
		boolean inBrace = false;
		boolean inClauseDefine = false;
		boolean setEquals = (statement.length > 1
				&& tok == Token.set
				&& statement[0].value.equals("")
				&& (statement[0].intValue == '=' || statement[0].intValue == '#') && statement[1].tok != Token.expressionBegin);
		int len = statement.length;
		for (int i = 0; i < len; ++i) {
			Token token = statement[i];
			if (token == null) {
				len = i;
				break;
			}
			if (iTok == i - 1)
				sb.append(" <<");
			if (i != 0)
				sb.append(' ');
			if (i == 2 && setEquals) {
				if ((setEquals = (token.tok != Token.opEQ))
						|| statement[0].intValue == '#') {
					sb.append(setEquals ? "= " : "== ");
					if (!setEquals)
						continue;
				}
			}
			if (iTok == i && token.tok != Token.expressionEnd)
				sb.append(">> ");
			switch (token.tok) {
			case Token.expressionBegin:
				if (useBraces)
					sb.append("{");
				continue;
			case Token.expressionEnd:
				if (inClauseDefine && i == statement.length - 1)
					useBraces = false;
				if (useBraces)
					sb.append("}");
				continue;
			case Token.leftsquare:
			case Token.rightsquare:
				break;
			case Token.leftbrace:
			case Token.rightbrace:
				inBrace = (token.tok == Token.leftbrace);
				break;
			case Token.define:
				if (i > 0 && ((String) token.value).equals("define")) {
					sb.append("@");
					if (i + 1 < statement.length
							&& statement[i + 1].tok == Token.expressionBegin) {
						if (!useBraces)
							inClauseDefine = true;
						useBraces = true;
					}
					continue;
				}
				break;
			case Token.on:
				sb.append("true");
				continue;
			case Token.off:
				sb.append("false");
				continue;
			case Token.select:
				break;
			case Token.integer:
				sb.append(token.intValue);
				continue;
			case Token.point3f:
			case Token.point4f:
			case Token.bitset:
				sb.append(ScriptVariable.sValue(token)); // list
				continue;
			case Token.varray:
			case Token.hash:
				sb.append(((ScriptVariable) token).escape()); // list
				continue;
			case Token.seqcode:
				sb.append('^');
				continue;
			case Token.spec_seqcode_range:
				if (token.intValue != Integer.MAX_VALUE)
					sb.append(token.intValue);
				else
					sb.append(Group.getSeqcodeString(getSeqCode(token)));
				token = statement[++i];
				sb.append(' ');
				// if (token.intValue == Integer.MAX_VALUE)
				sb.append(inBrace ? "-" : "- ");
				//$FALL-THROUGH$
			case Token.spec_seqcode:
				if (token.intValue != Integer.MAX_VALUE)
					sb.append(token.intValue);
				else
					sb.append(Group.getSeqcodeString(getSeqCode(token)));
				continue;
			case Token.spec_chain:
				sb.append("*:");
				sb.append((char) token.intValue);
				continue;
			case Token.spec_alternate:
				sb.append("*%");
				if (token.value != null)
					sb.append(token.value.toString());
				continue;
			case Token.spec_model:
				sb.append("*/");
				//$FALL-THROUGH$
			case Token.spec_model2:
			case Token.decimal:
				if (token.intValue < Integer.MAX_VALUE) {
					sb.append(Escape.escapeModelFileNumber(token.intValue));
				} else {
					sb.append("" + token.value);
				}
				continue;
			case Token.spec_resid:
				sb.append('[');
				sb.append(Group.getGroup3((short) token.intValue));
				sb.append(']');
				continue;
			case Token.spec_name_pattern:
				sb.append('[');
				sb.append(token.value);
				sb.append(']');
				continue;
			case Token.spec_atom:
				sb.append("*.");
				break;
			case Token.cell:
				if (token.value instanceof Point3f) {
					Point3f pt = (Point3f) token.value;
					sb.append("cell=").append(Escape.escapePt(pt));
					continue;
				}
				break;
			case Token.string:
				sb.append("\"").append(token.value).append("\"");
				continue;
			case Token.opEQ:
			case Token.opLE:
			case Token.opGE:
			case Token.opGT:
			case Token.opLT:
			case Token.opNE:
				// not quite right -- for "inmath"
				if (token.intValue == Token.property) {
					sb.append((String) statement[++i].value).append(" ");
				} else if (token.intValue != Integer.MAX_VALUE)
					sb.append(Token.nameOf(token.intValue)).append(" ");
				break;
			case Token.trycmd:
				continue;
			case Token.end:
				sb.append("end");
				continue;
			default:
				if (Token.tokAttr(token.tok, Token.identifier)
						|| !doLogMessages)
					break;
				sb.append('\n').append(token.toString()).append('\n');
				continue;
			}
			if (token.value != null)
				sb.append(token.value.toString());
		}
		if (iTok >= len - 1 && iTok != 9999)
			sb.append(" <<");
		return sb.toString();
	}

	// /////////// shape get/set properties ////////////////

	private Object getShapeProperty(int shapeType, String propertyName) {
		return shapeManager.getShapeProperty(shapeType, propertyName,
				Integer.MIN_VALUE);
	}

	private boolean getShapeProperty(int shapeType, String propertyName,
			Object[] data) {
		return shapeManager.getShapeProperty(shapeType, propertyName, data);
	}

	private Object getShapeProperty(int shapeType, String propertyName,
			int index) {
		return shapeManager.getShapeProperty(shapeType, propertyName, index);
	}

	private void addShapeProperty(List<Object[]> propertyList, String key,
			Object value) {
		if (isSyntaxCheck)
			return;
		System.out.println("addshapeprop " + key + " " + value);
		propertyList.add(new Object[] { key, value });
	}

	private void setObjectMad(int iShape, String name, int mad) {
		if (isSyntaxCheck)
			return;
		viewer.setObjectMad(iShape, name, mad);
	}

	private void setObjectArgb(String str, int argb) {
		if (isSyntaxCheck)
			return;
		viewer.setObjectArgb(str, argb);
	}

	private void setShapeProperty(int shapeType, String propertyName,
			Object propertyValue) {
		if (isSyntaxCheck)
			return;
		shapeManager.setShapeProperty(shapeType, propertyName, propertyValue,
				null);
	}

	private void setShapeProperty(int iShape, String propertyName,
			Object propertyValue, BitSet bs) {
		if (isSyntaxCheck)
			return;
		shapeManager.setShapeProperty(iShape, propertyName, propertyValue, bs);
	}

	private void setShapeSize(int shapeType, int size, BitSet bs) {
		// stars, halos, balls only
		if (isSyntaxCheck)
			return;
		shapeManager.setShapeSize(shapeType, size, null, bs);
	}

	private void setShapeSize(int shapeType, RadiusData rd) {
		if (isSyntaxCheck)
			return;
		shapeManager.setShapeSize(shapeType, 0, rd, null);
	}

	// ////////////////// setting properties ////////////////////////

	private void setBooleanProperty(String key, boolean value) {
		if (!isSyntaxCheck)
			viewer.setBooleanProperty(key, value);
	}

	private boolean setIntProperty(String key, int value) {
		if (!isSyntaxCheck)
			viewer.setIntProperty(key, value);
		return true;
	}

	private boolean setFloatProperty(String key, float value) {
		if (!isSyntaxCheck)
			viewer.setFloatProperty(key, value);
		return true;
	}

	private void setStringProperty(String key, String value) {
		if (!isSyntaxCheck)
			viewer.setStringProperty(key, value);
	}

	// ////////////////// showing strings /////////////////
	private void showString(String str) {
		showString(str, false);
	}

	private void showString(String str, boolean isPrint) {
		if (isSyntaxCheck || str == null)
			return;
		if (outputBuffer != null)
			outputBuffer.append(str).append('\n');
		else
			viewer.showString(str, isPrint);
	}

	private void scriptStatusOrBuffer(String s) {
		if (isSyntaxCheck)
			return;
		if (outputBuffer != null) {
			outputBuffer.append(s).append('\n');
			return;
		}
		viewer.scriptStatus(s);
	}

	// /////////////// expression processing ///////////////////

	private Token[] tempStatement;
	private boolean isBondSet;
	private Object expressionResult;

	private BitSet atomExpressionAt(int index) throws ScriptException {
		if (!checkToken(index))
			error(ERROR_badArgumentCount, index);
		return atomExpression(statement, index, 0, true, false, true, true);
	}

	/**
	 * @param code
	 * @param pcStart
	 * @param pcStop
	 * @param allowRefresh
	 * @param allowUnderflow
	 * @param mustBeBitSet
	 * @param andNotDeleted
	 *            IGNORED
	 * @return atom bitset
	 * @throws ScriptException
	 */
	@SuppressWarnings("unchecked")
	private BitSet atomExpression(Token[] code, int pcStart, int pcStop,
			boolean allowRefresh, boolean allowUnderflow, boolean mustBeBitSet,
			boolean andNotDeleted) throws ScriptException {
		// note that this is general -- NOT just statement[]
		// errors reported would improperly access statement/line context
		// there should be no errors anyway, because this is for
		// predefined variables, but it is conceivable that one could
		// have a problem.

		isBondSet = false;
		if (code != statement) {
			tempStatement = statement;
			statement = code;
		}
		/*
		 * statement = new Token[5]; statement[0] = new Token(Token.select,
		 * "select"); statement[1] = new Token(Token.expressionBegin,
		 * "implicitExpressionBegin"); statement[2] = new
		 * Token(Token.spec_seqcode, 101, Integer.MAX_VALUE); statement[3] = new
		 * Token(Token.spec_seqcode, 156, Integer.MAX_VALUE); statement[4] = new
		 * Token(Token.expressionEnd, "expressionEnd"); code = statement;
		 */
		ScriptMathProcessor rpn = new ScriptMathProcessor(this, false, false,
				mustBeBitSet);
		Object val;
		int comparisonValue = Integer.MAX_VALUE;
		boolean refreshed = false;
		iToken = 1000;
		boolean ignoreSubset = (pcStart < 0);
		boolean isInMath = false;
		int nExpress = 0;
		int atomCount = viewer.getAtomCount();
		if (ignoreSubset)
			pcStart = -pcStart;
		ignoreSubset |= isSyntaxCheck;
		if (pcStop == 0 && code.length > pcStart)
			pcStop = pcStart + 1;
		// if (logMessages)
		// viewer.scriptStatus("start to evaluate expression");
		expression_loop: for (int pc = pcStart; pc < pcStop; ++pc) {
			iToken = pc;
			Token instruction = code[pc];
			if (instruction == null)
				break;
			Object value = instruction.value;
			// if (logMessages)
			// viewer.scriptStatus("instruction=" + instruction);
			switch (instruction.tok) {
			case Token.expressionBegin:
				pcStart = pc;
				pcStop = code.length;
				nExpress++;
				break;
			case Token.expressionEnd:
				nExpress--;
				if (nExpress > 0)
					continue;
				break expression_loop;
			case Token.leftbrace:
				if (isPoint3f(pc)) {
					Point3f pt = getPoint3f(pc, true);
					if (pt != null) {
						rpn.addXPt(pt);
						pc = iToken;
						break;
					}
				}
				break; // ignore otherwise
			case Token.rightbrace:
				if (pc > 0 && code[pc - 1].tok == Token.leftbrace)
					rpn.addXBs(new BitSet());
				break;
			case Token.leftsquare:
				isInMath = true;
				rpn.addOp(instruction);
				break;
			case Token.rightsquare:
				isInMath = false;
				rpn.addOp(instruction);
				break;
			case Token.define:
				rpn.addXBs(getAtomBitSet(this, value));
				break;
			case Token.hkl:
				rpn.addXVar(new ScriptVariable(instruction));
				rpn.addXVar(new ScriptVariable(Token.point4f,
						hklParameter(pc + 2)));
				pc = iToken;
				break;
			case Token.plane:
				rpn.addXVar(new ScriptVariable(instruction));
				rpn.addXVar(new ScriptVariable(Token.point4f,
						planeParameter(pc + 2)));
				pc = iToken;
				break;
			case Token.coord:
				rpn.addXVar(new ScriptVariable(instruction));
				rpn.addXPt(getPoint3f(pc + 2, true));
				pc = iToken;
				break;
			case Token.string:
				String s = (String) value;
				if (s.indexOf("({") == 0) {
					BitSet bs = Escape.unescapeBitset(s);
					if (bs != null) {
						rpn.addXBs(bs);
						break;
					}
				}
				rpn.addXVar(new ScriptVariable(instruction));
				// note that the compiler has changed all within() types to
				// strings.
				if (s.equals("hkl")) {
					rpn.addXVar(new ScriptVariable(Token.point4f,
							hklParameter(pc + 2)));
					pc = iToken;
				}
				break;
			case Token.smiles:
			case Token.search:
			case Token.substructure:
			case Token.within:
			case Token.contact:
			case Token.connected:
			case Token.comma:
				rpn.addOp(instruction);
				break;
			case Token.all:
				rpn.addXBs(viewer.getModelUndeletedAtomsBitSet(-1));
				break;
			case Token.none:
				rpn.addXBs(new BitSet());
				break;
			case Token.on:
			case Token.off:
				rpn.addXVar(new ScriptVariable(instruction));
				break;
			case Token.selected:
				rpn.addXBs(BitSetUtil.copy(viewer.getSelectionSet(false)));
				break;
			case Token.subset:
				BitSet bsSubset = viewer.getSelectionSubset();
				rpn.addXBs(bsSubset == null ? viewer
						.getModelUndeletedAtomsBitSet(-1) : BitSetUtil
						.copy(bsSubset));
				break;
			case Token.hidden:
				rpn.addXBs(BitSetUtil.copy(viewer.getHiddenSet()));
				break;
			case Token.fixed:
				rpn.addXBs(BitSetUtil.copy(viewer.getMotionFixedAtoms()));
				break;
			case Token.displayed:
				rpn.addXBs(BitSetUtil.copyInvert(viewer.getHiddenSet(),
						atomCount));
				break;
			case Token.basemodel:
				rpn.addXBs(viewer.getBaseModelBitSet());
				break;
			case Token.visible:
				if (!isSyntaxCheck && !refreshed)
					viewer.setModelVisibility();
				refreshed = true;
				rpn.addXBs(viewer.getVisibleSet());
				break;
			case Token.clickable:
				// a bit different, because it requires knowing what got slabbed
				if (!isSyntaxCheck && allowRefresh)
					refresh();
				rpn.addXBs(viewer.getClickableSet());
				break;
			case Token.spec_atom:
				if (viewer.allowSpecAtom()) {
					int atomID = instruction.intValue;
					if (atomID > 0)
						rpn.addXBs(compareInt(Token.atomid, Token.opEQ, atomID));
					else
						rpn.addXBs(getAtomBits(instruction.tok, value));
				} else {
					// Chime legacy hack. *.C for _C
					rpn.addXBs(lookupIdentifierValue("_" + value));
				}
				break;
			case Token.carbohydrate:
			case Token.dna:
			case Token.hetero:
			case Token.isaromatic:
			case Token.nucleic:
			case Token.protein:
			case Token.purine:
			case Token.pyrimidine:
			case Token.rna:
			case Token.spec_name_pattern:
			case Token.spec_alternate:
			case Token.specialposition:
			case Token.symmetry:
			case Token.unitcell:
				rpn.addXBs(getAtomBits(instruction.tok, value));
				break;
			case Token.spec_model:
				// from select */1002 or */1000002 or */1.2
				// */1002 is equivalent to 1.2 when more than one file is
				// present
			case Token.spec_model2:
				// from just using the number 1.2
				int iModel = instruction.intValue;
				if (iModel == Integer.MAX_VALUE && value instanceof Integer) {
					// from select */n
					iModel = ((Integer) value).intValue();
					if (!viewer.haveFileSet()) {
						rpn.addXBs(getAtomBits(Token.spec_model,
								Integer.valueOf(iModel)));
						break;
					}
					if (iModel <= 2147) // file number
						iModel = iModel * 1000000;
				}
				rpn.addXBs(bitSetForModelFileNumber(iModel));
				break;
			case Token.spec_resid:
			case Token.spec_chain:
				rpn.addXBs(getAtomBits(instruction.tok, new Integer(
						instruction.intValue)));
				break;
			case Token.spec_seqcode:
				if (isInMath)
					rpn.addXNum(new ScriptVariableInt(instruction.intValue));
				else
					rpn.addXBs(getAtomBits(Token.spec_seqcode, new Integer(
							getSeqCode(instruction))));
				break;
			case Token.spec_seqcode_range:
				if (isInMath) {
					rpn.addXNum(new ScriptVariableInt(instruction.intValue));
					rpn.addXObj(Token.tokenMinus);
					rpn.addXNum(new ScriptVariableInt(code[++pc].intValue));
					break;
				}
				int chainID = (pc + 3 < code.length
						&& code[pc + 2].tok == Token.opAND
						&& code[pc + 3].tok == Token.spec_chain ? code[pc + 3].intValue
						: '\t');
				rpn.addXBs(getAtomBits(Token.spec_seqcode_range, new int[] {
						getSeqCode(instruction), getSeqCode(code[++pc]),
						chainID }));
				if (chainID != '\t')
					pc += 2;
				break;
			case Token.cell:
				Point3f pt = (Point3f) value;
				rpn.addXBs(getAtomBits(Token.cell, new int[] {
						(int) (pt.x * 1000), (int) (pt.y * 1000),
						(int) (pt.z * 1000) }));
				break;
			case Token.thismodel:
				rpn.addXBs(viewer.getModelUndeletedAtomsBitSet(viewer
						.getCurrentModelIndex()));
				break;
			case Token.hydrogen:
			case Token.amino:
			case Token.backbone:
			case Token.solvent:
			case Token.helix:
			case Token.helixalpha:
			case Token.helix310:
			case Token.helixpi:
			case Token.sidechain:
			case Token.surface:
				rpn.addXBs(lookupIdentifierValue((String) value));
				break;
			case Token.opLT:
			case Token.opLE:
			case Token.opGE:
			case Token.opGT:
			case Token.opEQ:
			case Token.opNE:
				if (pc + 1 == code.length)
					error(ERROR_invalidArgument);
				val = code[++pc].value;
				int tokOperator = instruction.tok;
				int tokWhat = instruction.intValue;
				String property = (tokWhat == Token.property ? (String) val
						: null);
				if (property != null) {
					if (pc + 1 == code.length)
						error(ERROR_invalidArgument);
					val = code[++pc].value;
				}
				if (tokWhat == Token.configuration && tokOperator != Token.opEQ)
					error(ERROR_invalidArgument);
				if (isSyntaxCheck) {
					rpn.addXBs(new BitSet());
					break;
				}
				boolean isModel = (tokWhat == Token.model);
				boolean isIntProperty = Token.tokAttr(tokWhat,
						Token.intproperty);
				boolean isFloatProperty = Token.tokAttr(tokWhat,
						Token.floatproperty);
				boolean isIntOrFloat = isIntProperty && isFloatProperty;
				boolean isStringProperty = !isIntProperty
						&& Token.tokAttr(tokWhat, Token.strproperty);
				if (tokWhat == Token.element)
					isIntProperty = !(isStringProperty = false);
				int tokValue = code[pc].tok;
				comparisonValue = code[pc].intValue;
				float comparisonFloat = Float.NaN;
				if (val instanceof Point3f) {
					if (tokWhat == Token.color) {
						comparisonValue = ColorUtil.colorPtToInt((Point3f) val);
						tokValue = Token.integer;
						isIntProperty = true;
					}
				} else if (val instanceof String) {
					if (tokWhat == Token.color) {
						comparisonValue = ColorUtil
								.getArgbFromString((String) val);
						if (comparisonValue == 0
								&& Token.tokAttr(tokValue, Token.identifier)) {
							val = getStringParameter((String) val, true);
							if (((String) val).startsWith("{")) {
								val = Escape.unescapePoint((String) val);
								if (val instanceof Point3f)
									comparisonValue = ColorUtil
											.colorPtToInt((Point3f) val);
								else
									comparisonValue = 0;
							} else {
								comparisonValue = ColorUtil
										.getArgbFromString((String) val);
							}
						}
						tokValue = Token.integer;
						isIntProperty = true;
					} else if (isStringProperty) {
						if (Token.tokAttr(tokValue, Token.identifier))
							val = getStringParameter((String) val, true);
					} else {
						if (Token.tokAttr(tokValue, Token.identifier))
							val = getNumericParameter((String) val);
						if (val instanceof String) {
							if (tokWhat == Token.structure
									|| tokWhat == Token.substructure
									|| tokWhat == Token.element)
								isStringProperty = !(isIntProperty = (comparisonValue != Integer.MAX_VALUE));
							else
								val = ScriptVariable.nValue(code[pc]);
						}
						if (val instanceof Integer)
							comparisonFloat = comparisonValue = ((Integer) val)
									.intValue();
						else if (val instanceof Float && isModel)
							comparisonValue = ModelCollection
									.modelFileNumberFromFloat(((Float) val)
											.floatValue());
					}
				}
				if (isStringProperty && !(val instanceof String)) {
					val = "" + val;
				}
				if (val instanceof Integer || tokValue == Token.integer) {
					if (isModel) {
						if (comparisonValue >= 1000000)
							tokWhat = -Token.model;
					} else if (isIntOrFloat) {
						isFloatProperty = false;
					} else if (isFloatProperty) {
						comparisonFloat = comparisonValue;
					}
				} else if (val instanceof Float) {
					if (isModel) {
						tokWhat = -Token.model;
					} else {
						comparisonFloat = ((Float) val).floatValue();
						if (isIntOrFloat) {
							isIntProperty = false;
						} else if (isIntProperty) {
							comparisonValue = (int) comparisonFloat;
						}
					}
				} else if (!isStringProperty) {
					iToken++;
					error(ERROR_invalidArgument);
				}
				if (isModel && comparisonValue >= 1000000
						&& comparisonValue % 1000000 == 0) {
					comparisonValue /= 1000000;
					tokWhat = Token.file;
					isModel = false;
				}
				if (tokWhat == -Token.model && tokOperator == Token.opEQ) {
					rpn.addXBs(bitSetForModelFileNumber(comparisonValue));
					break;
				}
				if (value != null && ((String) value).indexOf("-") >= 0) {
					if (isIntProperty)
						comparisonValue = -comparisonValue;
					else if (!Float.isNaN(comparisonFloat))
						comparisonFloat = -comparisonFloat;
				}
				float[] data = (tokWhat == Token.property ? viewer
						.getDataFloat(property) : null);
				rpn.addXBs(isIntProperty ? compareInt(tokWhat, tokOperator,
						comparisonValue) : isStringProperty ? compareString(
						tokWhat, tokOperator, (String) val) : compareFloat(
						tokWhat, data, tokOperator, comparisonFloat));
				break;
			case Token.decimal:
			case Token.integer:
				rpn.addXNum(new ScriptVariable(instruction));
				break;
			case Token.bitset:
				BitSet bs1 = BitSetUtil.copy((BitSet) value);
				// System.out.println(Escape.escape(bs1));
				// if (isStateScript && viewer.getTestFlag(1))
				// BitSetUtil.deleteBits(bs1, (BitSet)
				// viewer.getModelSetAuxiliaryInfo("bsDeletedAtoms"));
				// System.out.println(Escape.escape(bs1));
				rpn.addXBs(bs1);
				break;
			case Token.point3f:
				rpn.addXObj(value);
				break;
			default:
				if (Token.tokAttr(instruction.tok, Token.mathop)) {
					if (!rpn.addOp(instruction))
						error(ERROR_invalidArgument);
					break;
				}
				if (!(value instanceof String)) {
					// catch-all: point4f, hash, list, etc.
					rpn.addXObj(value);
					break;
				}
				val = getParameter((String) value, 0);
				if (isInMath) {
					rpn.addXObj(val);
					break;
				}
				if (val instanceof String)
					val = getStringObjectAsVariable((String) val, null);
				if (val instanceof List<?>) {
					BitSet bs = ScriptVariable.unEscapeBitSetArray(
							(ArrayList<ScriptVariable>) val, true);
					if (bs == null)
						val = value;
					else
						val = bs;
				}
				if (val instanceof String)
					val = lookupIdentifierValue((String) value);
				rpn.addXObj(val);
				break;
			}
		}
		expressionResult = rpn.getResult(allowUnderflow);
		if (expressionResult == null) {
			if (allowUnderflow)
				return null;
			if (!isSyntaxCheck)
				rpn.dumpStacks("after getResult");
			error(ERROR_endOfStatementUnexpected);
		}
		expressionResult = ((ScriptVariable) expressionResult).value;
		if (expressionResult instanceof String
				&& (mustBeBitSet || ((String) expressionResult)
						.startsWith("({"))) {
			// allow for select @{x} where x is a string that can evaluate to a
			// bitset
			expressionResult = (isSyntaxCheck ? new BitSet() : getAtomBitSet(
					this, expressionResult));
		}
		if (!mustBeBitSet && !(expressionResult instanceof BitSet))
			return null; // because result is in expressionResult in that case
		BitSet bs = (expressionResult instanceof BitSet ? (BitSet) expressionResult
				: new BitSet());
		isBondSet = (expressionResult instanceof BondSet);
		if (!isBondSet) {
			viewer.excludeAtoms(bs, ignoreSubset);
			if (bs.length() > viewer.getAtomCount())
				bs.clear();
		}
		if (tempStatement != null) {
			statement = tempStatement;
			tempStatement = null;
		}
		return bs;
	}

	private BitSet compareFloat(int tokWhat, float[] data, int tokOperator,
			float comparisonFloat) {
		BitSet bs = new BitSet();
		int atomCount = viewer.getAtomCount();
		ModelSet modelSet = viewer.getModelSet();
		Atom[] atoms = modelSet.atoms;
		float propertyFloat = 0;
		viewer.autoCalculate(tokWhat);
		for (int i = atomCount; --i >= 0;) {
			boolean match = false;
			Atom atom = atoms[i];
			switch (tokWhat) {
			default:
				propertyFloat = Atom.atomPropertyFloat(viewer, atom, tokWhat);
				break;
			case Token.property:
				if (data == null || data.length <= i)
					continue;
				propertyFloat = data[i];
			}
			match = compareFloat(tokOperator, propertyFloat, comparisonFloat);
			if (match)
				bs.set(i);
		}
		return bs;
	}

	private BitSet compareString(int tokWhat, int tokOperator,
			String comparisonString) throws ScriptException {
		BitSet bs = new BitSet();
		Atom[] atoms = viewer.getModelSet().atoms;
		int atomCount = viewer.getAtomCount();
		boolean isCaseSensitive = (tokWhat == Token.chain && viewer
				.getChainCaseSensitive());
		if (!isCaseSensitive)
			comparisonString = comparisonString.toLowerCase();
		for (int i = atomCount; --i >= 0;) {
			String propertyString = Atom.atomPropertyString(viewer, atoms[i],
					tokWhat);
			if (!isCaseSensitive)
				propertyString = propertyString.toLowerCase();
			if (compareString(tokOperator, propertyString, comparisonString))
				bs.set(i);
		}
		return bs;
	}

	protected BitSet compareInt(int tokWhat, int tokOperator,
			int comparisonValue) {
		int propertyValue = Integer.MAX_VALUE;
		BitSet propertyBitSet = null;
		int bitsetComparator = tokOperator;
		int bitsetBaseValue = comparisonValue;
		int atomCount = viewer.getAtomCount();
		ModelSet modelSet = viewer.getModelSet();
		Atom[] atoms = modelSet.atoms;
		int imax = -1;
		int imin = 0;
		int iModel = -1;
		int[] cellRange = null;
		int nOps = 0;
		BitSet bs;
		// preliminary setup
		switch (tokWhat) {
		case Token.symop:
			switch (bitsetComparator) {
			case Token.opGE:
			case Token.opGT:
				imax = Integer.MAX_VALUE;
				break;
			}
			break;
		case Token.atomindex:
			try {
				switch (tokOperator) {
				case Token.opLT:
					return BitSetUtil.newBitSet(0, comparisonValue);
				case Token.opLE:
					return BitSetUtil.newBitSet(0, comparisonValue + 1);
				case Token.opGE:
					return BitSetUtil.newBitSet(comparisonValue, atomCount);
				case Token.opGT:
					return BitSetUtil.newBitSet(comparisonValue + 1, atomCount);
				case Token.opEQ:
					return (comparisonValue < atomCount ? BitSetUtil.newBitSet(
							comparisonValue, comparisonValue + 1)
							: new BitSet());
				case Token.opNE:
				default:
					bs = BitSetUtil.setAll(atomCount);
					if (comparisonValue >= 0)
						bs.clear(comparisonValue);
					return bs;
				}
			} catch (Exception e) {
				return new BitSet();
			}
		}
		bs = new BitSet(atomCount);
		for (int i = 0; i < atomCount; ++i) {
			boolean match = false;
			Atom atom = atoms[i];
			switch (tokWhat) {
			default:
				propertyValue = Atom.atomPropertyInt(atom, tokWhat);
				break;
			case Token.configuration:
				// these are all-inclusive; no need to do a by-atom comparison
				return BitSetUtil.copy(viewer.getConformation(-1,
						comparisonValue - 1, false));
			case Token.symop:
				propertyBitSet = atom.getAtomSymmetry();
				if (propertyBitSet == null)
					continue;
				if (atom.getModelIndex() != iModel) {
					iModel = atom.getModelIndex();
					cellRange = modelSet.getModelCellRange(iModel);
					nOps = modelSet.getModelSymmetryCount(iModel);
				}
				if (bitsetBaseValue >= 200) {
					if (cellRange == null)
						continue;
					/*
					 * symop>=1000 indicates symop*1000 +
					 * lattice_translation(555) for this the comparision is only
					 * with the translational component; the symop itself must
					 * match thus: select symop!=1655 selects all symop=1 and
					 * translation !=655 select symop>=2555 selects all symop=2
					 * and translation >555 symop >=200 indicates any symop in
					 * the specified translation (a few space groups have > 100
					 * operations)
					 * 
					 * Note that when normalization is not done, symop=1555 may
					 * not be in the base unit cell. Everything is relative to
					 * wherever the base atoms ended up, usually in 555, but not
					 * necessarily.
					 * 
					 * The reason this is tied together an atom may have one
					 * translation for one symop and another for a different
					 * one.
					 * 
					 * Bob Hanson - 10/2006
					 */
					comparisonValue = bitsetBaseValue % 1000;
					int symop = bitsetBaseValue / 1000 - 1;
					if (symop < 0) {
						match = true;
					} else if (nOps == 0 || symop >= 0
							&& !(match = propertyBitSet.get(symop))) {
						continue;
					}
					bitsetComparator = Token.none;
					if (symop < 0)
						propertyValue = atom.getCellTranslation(
								comparisonValue, cellRange, nOps);
					else
						propertyValue = atom.getSymmetryTranslation(symop,
								cellRange, nOps);
				} else if (nOps > 0) {
					if (comparisonValue > nOps) {
						if (bitsetComparator != Token.opLT
								&& bitsetComparator != Token.opLE)
							continue;
					}
					if (bitsetComparator == Token.opNE) {
						if (comparisonValue > 0 && comparisonValue <= nOps
								&& !propertyBitSet.get(comparisonValue)) {
							bs.set(i);
						}
						continue;
					}
				}
				switch (bitsetComparator) {
				case Token.opLT:
					imax = comparisonValue - 1;
					break;
				case Token.opLE:
					imax = comparisonValue;
					break;
				case Token.opGE:
					imin = comparisonValue - 1;
					break;
				case Token.opGT:
					imin = comparisonValue;
					break;
				case Token.opEQ:
					imax = comparisonValue;
					imin = comparisonValue - 1;
					break;
				case Token.opNE:
					match = !propertyBitSet.get(comparisonValue);
					break;
				}
				if (imin < 0)
					imin = 0;
				if (imin < imax) {
					int pt = propertyBitSet.nextSetBit(imin);
					if (pt >= 0 && pt < imax)
						match = true;
				}
				// note that a symop property can be both LE and GT !
				if (!match || propertyValue == Integer.MAX_VALUE)
					tokOperator = Token.none;
			}
			switch (tokOperator) {
			case Token.none:
				break;
			case Token.opLT:
				match = (propertyValue < comparisonValue);
				break;
			case Token.opLE:
				match = (propertyValue <= comparisonValue);
				break;
			case Token.opGE:
				match = (propertyValue >= comparisonValue);
				break;
			case Token.opGT:
				match = (propertyValue > comparisonValue);
				break;
			case Token.opEQ:
				match = (propertyValue == comparisonValue);
				break;
			case Token.opNE:
				match = (propertyValue != comparisonValue);
				break;
			}
			if (match)
				bs.set(i);
		}
		return bs;
	}

	private boolean compareString(int tokOperator, String propertyValue,
			String comparisonValue) throws ScriptException {
		switch (tokOperator) {
		case Token.opEQ:
		case Token.opNE:
			return (TextFormat.isMatch(propertyValue, comparisonValue, true,
					true) == (tokOperator == Token.opEQ));
		default:
			error(ERROR_invalidArgument);
		}
		return false;
	}

	private static boolean compareFloat(int tokOperator, float propertyFloat,
			float comparisonFloat) {
		switch (tokOperator) {
		case Token.opLT:
			return propertyFloat < comparisonFloat;
		case Token.opLE:
			return propertyFloat <= comparisonFloat;
		case Token.opGE:
			return propertyFloat >= comparisonFloat;
		case Token.opGT:
			return propertyFloat > comparisonFloat;
		case Token.opEQ:
			return propertyFloat == comparisonFloat;
		case Token.opNE:
			return propertyFloat != comparisonFloat;
		}
		return false;
	}

	private BitSet getAtomBits(int tokType, Object specInfo) {
		return (isSyntaxCheck ? new BitSet() : viewer.getAtomBits(tokType,
				specInfo));
	}

	private static int getSeqCode(Token instruction) {
		return (instruction.intValue != Integer.MAX_VALUE ? Group.getSeqcode(
				instruction.intValue, ' ') : ((Integer) instruction.value)
				.intValue());
	}

	/*
	 * ****************************************************************************
	 * ============================================================== checks and
	 * parameter retrieval
	 * ==============================================================
	 */

	private int checkLast(int i) throws ScriptException {
		return checkLength(i + 1) - 1;
	}

	private int checkLength(int length) throws ScriptException {
		if (length >= 0)
			return checkLength(length, 0);
		// max
		if (statementLength > -length) {
			iToken = -length;
			error(ERROR_badArgumentCount);
		}
		return statementLength;
	}

	private int checkLength(int length, int errorPt) throws ScriptException {
		if (statementLength != length) {
			iToken = errorPt > 0 ? errorPt : statementLength;
			error(errorPt > 0 ? ERROR_invalidArgument : ERROR_badArgumentCount);
		}
		return statementLength;
	}

	private int checkLength23() throws ScriptException {
		iToken = statementLength;
		if (statementLength != 2 && statementLength != 3)
			error(ERROR_badArgumentCount);
		return statementLength;
	}

	private int checkLength34() throws ScriptException {
		iToken = statementLength;
		if (statementLength != 3 && statementLength != 4)
			error(ERROR_badArgumentCount);
		return statementLength;
	}

	private int theTok;
	private Token theToken;

	private Token getToken(int i) throws ScriptException {
		if (!checkToken(i))
			error(ERROR_endOfStatementUnexpected);
		theToken = statement[i];
		theTok = theToken.tok;
		return theToken;
	}

	private int tokAt(int i) {
		return (i < statementLength && statement[i] != null ? statement[i].tok
				: Token.nada);
	}

	private int tokAt(int i, Token[] args) {
		return (i < args.length && args[i] != null ? args[i].tok : Token.nada);
	}

	private Token tokenAt(int i, Token[] args) {
		return (i < args.length ? args[i] : null);
	}

	private boolean checkToken(int i) {
		return (iToken = i) < statementLength;
	}

	private int modelNumberParameter(int index) throws ScriptException {
		int iFrame = 0;
		boolean useModelNumber = false;
		switch (tokAt(index)) {
		case Token.integer:
			useModelNumber = true;
			//$FALL-THROUGH$
		case Token.decimal:
			iFrame = getToken(index).intValue; // decimal Token intValue is
			// model/frame number encoded
			break;
		case Token.string:
			iFrame = getFloatEncodedInt(stringParameter(index));
			break;
		default:
			error(ERROR_invalidArgument);
		}
		return viewer.getModelNumberIndex(iFrame, useModelNumber, true);
	}

	private String optParameterAsString(int i) throws ScriptException {
		if (i >= statementLength)
			return "";
		return parameterAsString(i);
	}

	private String parameterAsString(int i) throws ScriptException {
		getToken(i);
		if (theToken == null)
			error(ERROR_endOfStatementUnexpected);
		return ScriptVariable.sValue(theToken);
	}

	private int intParameter(int index) throws ScriptException {
		if (checkToken(index))
			if (getToken(index).tok == Token.integer)
				return theToken.intValue;
		error(ERROR_integerExpected);
		return 0;
	}

	private int intParameter(int i, int min, int max) throws ScriptException {
		int val = intParameter(i);
		if (val < min || val > max)
			integerOutOfRange(min, max);
		return val;
	}

	private boolean isFloatParameter(int index) {
		switch (tokAt(index)) {
		case Token.integer:
		case Token.decimal:
			return true;
		}
		return false;
	}

	private float floatParameter(int i, float min, float max)
			throws ScriptException {
		float val = floatParameter(i);
		if (val < min || val > max)
			numberOutOfRange(min, max);
		return val;
	}

	private float floatParameter(int index) throws ScriptException {
		if (checkToken(index)) {
			getToken(index);
			switch (theTok) {
			case Token.spec_seqcode_range:
				return -theToken.intValue;
			case Token.spec_seqcode:
			case Token.integer:
				return theToken.intValue;
			case Token.spec_model2:
			case Token.decimal:
				return ((Float) theToken.value).floatValue();
			}
		}
		error(ERROR_numberExpected);
		return 0;
	}

	/**
	 * process a general string or set of parameters as an array of floats,
	 * allowing for relatively free form input
	 * 
	 * @param i
	 * @param nMin
	 * @param nMax
	 * @return array of floats
	 * @throws ScriptException
	 */
	private float[] floatParameterSet(int i, int nMin, int nMax)
			throws ScriptException {
		int tok = tokAt(i);
		if (tok == Token.spacebeforesquare)
			tok = tokAt(++i);
		boolean haveBrace = (tok == Token.leftbrace);
		boolean haveSquare = (tok == Token.leftsquare);
		float[] fparams = null;
		List<Float> v = new ArrayList<Float>();
		int n = 0;
		if (haveBrace || haveSquare)
			i++;
		Point3f pt;
		String s = null;
		switch (tokAt(i)) {
		case Token.string:
			s = ScriptVariable.sValue(statement[i]);
			s = TextFormat.replaceAllCharacters(s, "{},[]\"'", ' ');
			fparams = Parser.parseFloatArray(s);
			n = fparams.length;
			break;
		case Token.varray:
			fparams = ScriptVariable.flistValue(statement[i++], 0);
			n = fparams.length;
			break;
		default:
			while (n < nMax) {
				tok = tokAt(i);
				if (haveBrace && tok == Token.rightbrace || haveSquare
						&& tok == Token.rightsquare)
					break;
				switch (tok) {
				case Token.comma:
				case Token.leftbrace:
				case Token.rightbrace:
					break;
				case Token.string:
					break;
				case Token.point3f:
					pt = getPoint3f(i, false);
					v.add(Float.valueOf(pt.x));
					v.add(Float.valueOf(pt.y));
					v.add(Float.valueOf(pt.z));
					n += 3;
					break;
				case Token.point4f:
					Point4f pt4 = getPoint4f(i);
					v.add(Float.valueOf(pt4.x));
					v.add(Float.valueOf(pt4.y));
					v.add(Float.valueOf(pt4.z));
					v.add(Float.valueOf(pt4.w));
					n += 4;
					break;
				default:
					v.add(Float.valueOf(floatParameter(i)));
					n++;
					if (n == nMax && haveSquare
							&& tokAt(i + 1) == Token.rightbrace)
						i++;
				}
				i++;
			}
		}
		if (haveBrace && tokAt(i++) != Token.rightbrace || haveSquare
				&& tokAt(i++) != Token.rightsquare)
			error(ERROR_invalidArgument);
		iToken = i - 1;
		if (n < nMin || n > nMax)
			error(ERROR_invalidArgument);
		if (fparams == null) {
			fparams = new float[n];
			for (int j = 0; j < n; j++)
				fparams[j] = v.get(j).floatValue();
		}
		return fparams;
	}

	private boolean isArrayParameter(int i) {
		switch (tokAt(i)) {
		case Token.varray:
		case Token.matrix3f:
		case Token.matrix4f:
		case Token.spacebeforesquare:
		case Token.leftsquare:
			return true;
		}
		return false;
	}

	private Point3f[] getPointArray(int i, int nPoints) throws ScriptException {
		Point3f[] points = (nPoints < 0 ? null : new Point3f[nPoints]);
		List<Point3f> vp = (nPoints < 0 ? new ArrayList<Point3f>() : null);
		int tok = (i < 0 ? Token.varray : getToken(i++).tok);
		switch (tok) {
		case Token.varray:
			List<ScriptVariable> v = ((ScriptVariable) theToken).getList();
			if (nPoints >= 0 && v.size() != nPoints)
				error(ERROR_invalidArgument);
			nPoints = v.size();
			if (points == null)
				points = new Point3f[nPoints];
			for (int j = 0; j < nPoints; j++)
				if ((points[j] = ScriptVariable.ptValue(v.get(j))) == null)
					error(ERROR_invalidArgument);
			return points;
		case Token.spacebeforesquare:
			tok = tokAt(i++);
			break;
		}
		if (tok != Token.leftsquare)
			error(ERROR_invalidArgument);
		int n = 0;
		while (tok != Token.rightsquare && tok != Token.nada) {
			tok = getToken(i).tok;
			switch (tok) {
			case Token.nada:
			case Token.rightsquare:
				break;
			case Token.comma:
				i++;
				break;
			default:
				if (nPoints >= 0 && n == nPoints) {
					tok = Token.nada;
					break;
				}
				Point3f pt = getPoint3f(i, true);
				if (points == null)
					vp.add(pt);
				else
					points[n] = pt;
				n++;
				i = iToken + 1;
			}
		}
		if (tok != Token.rightsquare)
			error(ERROR_invalidArgument);
		if (points == null)
			points = vp.toArray(new Point3f[vp.size()]);
		return points;
	}

	private float[][] floatArraySet(int i, int nX, int nY)
			throws ScriptException {
		int tok = tokAt(i++);
		if (tok == Token.spacebeforesquare)
			tok = tokAt(i++);
		if (tok != Token.leftsquare)
			error(ERROR_invalidArgument);
		float[][] fparams = new float[nX][];
		int n = 0;
		while (tok != Token.rightsquare) {
			tok = getToken(i).tok;
			switch (tok) {
			case Token.spacebeforesquare:
			case Token.rightsquare:
				continue;
			case Token.comma:
				i++;
				break;
			case Token.leftsquare:
				i++;
				float[] f = new float[nY];
				fparams[n++] = f;
				for (int j = 0; j < nY; j++) {
					f[j] = floatParameter(i++);
					if (tokAt(i) == Token.comma)
						i++;
				}
				if (tokAt(i++) != Token.rightsquare)
					error(ERROR_invalidArgument);
				tok = Token.nada;
				if (n == nX && tokAt(i) != Token.rightsquare)
					error(ERROR_invalidArgument);
				break;
			default:
				error(ERROR_invalidArgument);
			}
		}
		return fparams;
	}

	private float[][][] floatArraySet(int i, int nX, int nY, int nZ)
			throws ScriptException {
		int tok = tokAt(i++);
		if (tok == Token.spacebeforesquare)
			tok = tokAt(i++);
		if (tok != Token.leftsquare || nX <= 0)
			error(ERROR_invalidArgument);
		float[][][] fparams = new float[nX][][];
		int n = 0;
		while (tok != Token.rightsquare) {
			tok = getToken(i).tok;
			switch (tok) {
			case Token.spacebeforesquare:
			case Token.rightsquare:
				continue;
			case Token.comma:
				i++;
				break;
			case Token.leftsquare:
				fparams[n++] = floatArraySet(i, nY, nZ);
				i = ++iToken;
				tok = Token.nada;
				if (n == nX && tokAt(i) != Token.rightsquare)
					error(ERROR_invalidArgument);
				break;
			default:
				error(ERROR_invalidArgument);
			}
		}
		return fparams;
	}

	private String stringParameter(int index) throws ScriptException {
		if (!checkToken(index) || getToken(index).tok != Token.string)
			error(ERROR_stringExpected);
		return (String) theToken.value;
	}

	private String[] stringParameterSet(int i) throws ScriptException {
		switch (tokAt(i)) {
		case Token.string:
			String s = stringParameter(i);
			if (s.startsWith("[\"")) {
				Object o = viewer.evaluateExpression(s);
				if (o instanceof String)
					return TextFormat.split((String) o, '\n');
			}
			return new String[] { s };
		case Token.spacebeforesquare:
			i += 2;
			break;
		case Token.leftsquare:
			++i;
			break;
		case Token.varray:
			return ScriptVariable.listValue(getToken(i));
		default:
			error(ERROR_invalidArgument);
		}
		int tok;
		List<String> v = new ArrayList<String>();
		while ((tok = tokAt(i)) != Token.rightsquare) {
			switch (tok) {
			case Token.comma:
				break;
			case Token.string:
				v.add(stringParameter(i));
				break;
			default:
			case Token.nada:
				error(ERROR_invalidArgument);
			}
			i++;
		}
		iToken = i;
		int n = v.size();
		String[] sParams = new String[n];
		for (int j = 0; j < n; j++) {
			sParams[j] = v.get(j);
		}
		return sParams;
	}

	private String objectNameParameter(int index) throws ScriptException {
		if (!checkToken(index))
			error(ERROR_objectNameExpected);
		return parameterAsString(index);
	}

	private boolean booleanParameter(int i) throws ScriptException {
		if (statementLength == i)
			return true;
		switch (getToken(checkLast(i)).tok) {
		case Token.on:
			return true;
		case Token.off:
			return false;
		default:
			error(ERROR_booleanExpected);
		}
		return false;
	}

	private Point3f atomCenterOrCoordinateParameter(int i)
			throws ScriptException {
		switch (getToken(i).tok) {
		case Token.bitset:
		case Token.expressionBegin:
			BitSet bs = atomExpression(statement, i, 0, true, false, false,
					true);
			if (bs != null)
				return viewer.getAtomSetCenter(bs);
			if (expressionResult instanceof Point3f)
				return (Point3f) expressionResult;
			error(ERROR_invalidArgument);
			break;
		case Token.leftbrace:
		case Token.point3f:
			return getPoint3f(i, true);
		}
		error(ERROR_invalidArgument);
		// impossible return
		return null;
	}

	private boolean isCenterParameter(int i) {
		int tok = tokAt(i);
		return (tok == Token.dollarsign || tok == Token.leftbrace
				|| tok == Token.expressionBegin || tok == Token.point3f || tok == Token.bitset);
	}

	private Point3f centerParameter(int i) throws ScriptException {
		return centerParameter(i, Integer.MIN_VALUE);
	}

	private Point3f centerParameter(int i, int modelIndex)
			throws ScriptException {
		Point3f center = null;
		expressionResult = null;
		if (checkToken(i)) {
			switch (getToken(i).tok) {
			case Token.dollarsign:
				String id = objectNameParameter(++i);
				int index = Integer.MIN_VALUE;
				// allow for $pt2.3 -- specific vertex
				if (tokAt(i + 1) == Token.leftsquare) {
					index = ScriptVariable.iValue(parameterExpressionList(
							-i - 1, -1, true).get(0));
					if (getToken(--iToken).tok != Token.rightsquare)
						error(ERROR_invalidArgument);
				}
				if (isSyntaxCheck)
					return new Point3f();
				if (tokAt(i + 1) == Token.per
						&& (tokAt(i + 2) == Token.length || tokAt(i + 2) == Token.size)) {
					index = Integer.MAX_VALUE;
					iToken = i + 2;
				}
				if ((center = getObjectCenter(id, index, modelIndex)) == null)
					error(ERROR_drawObjectNotDefined, id);
				break;
			case Token.bitset:
			case Token.expressionBegin:
			case Token.leftbrace:
			case Token.point3f:
				center = atomCenterOrCoordinateParameter(i);
				break;
			}
		}
		if (center == null)
			error(ERROR_coordinateOrNameOrExpressionRequired);
		return center;
	}

	private Point4f planeParameter(int i) throws ScriptException {
		Vector3f vAB = new Vector3f();
		Vector3f vAC = new Vector3f();
		Point4f plane = null;
		boolean isNegated = (tokAt(i) == Token.minus);
		if (isNegated)
			i++;
		if (i < statementLength)
			switch (getToken(i).tok) {
			case Token.point4f:
				plane = new Point4f((Point4f) theToken.value);
				break;
			case Token.dollarsign:
				String id = objectNameParameter(++i);
				if (isSyntaxCheck)
					return new Point4f();
				int shapeType = shapeManager.getShapeIdFromObjectName(id);
				switch (shapeType) {
				case JmolConstants.SHAPE_DRAW:
					setShapeProperty(JmolConstants.SHAPE_DRAW, "thisID", id);
					Point3f[] points = (Point3f[]) getShapeProperty(
							JmolConstants.SHAPE_DRAW, "vertices");
					if (points == null || points.length < 3
							|| points[0] == null || points[1] == null
							|| points[2] == null)
						break;
					Measure.getPlaneThroughPoints(points[0], points[1],
							points[2], new Vector3f(), vAB, vAC,
							plane = new Point4f());
					break;
				case JmolConstants.SHAPE_ISOSURFACE:
					setShapeProperty(JmolConstants.SHAPE_ISOSURFACE, "thisID",
							id);
					plane = (Point4f) getShapeProperty(
							JmolConstants.SHAPE_ISOSURFACE, "plane");
					break;
				}
				break;
			case Token.x:
				if (!checkToken(++i) || getToken(i++).tok != Token.opEQ)
					evalError("x=?", null);
				plane = new Point4f(1, 0, 0, -floatParameter(i));
				break;
			case Token.y:
				if (!checkToken(++i) || getToken(i++).tok != Token.opEQ)
					evalError("y=?", null);
				plane = new Point4f(0, 1, 0, -floatParameter(i));
				break;
			case Token.z:
				if (!checkToken(++i) || getToken(i++).tok != Token.opEQ)
					evalError("z=?", null);
				plane = new Point4f(0, 0, 1, -floatParameter(i));
				break;
			case Token.identifier:
			case Token.string:
				String str = parameterAsString(i);
				if (str.equalsIgnoreCase("xy"))
					return new Point4f(0, 0, 1, 0);
				if (str.equalsIgnoreCase("xz"))
					return new Point4f(0, 1, 0, 0);
				if (str.equalsIgnoreCase("yz"))
					return new Point4f(1, 0, 0, 0);
				iToken += 2;
				break;
			case Token.leftbrace:
				if (!isPoint3f(i)) {
					plane = getPoint4f(i);
					break;
				}
				//$FALL-THROUGH$
			case Token.bitset:
			case Token.expressionBegin:
				Point3f pt1 = atomCenterOrCoordinateParameter(i);
				if (getToken(++iToken).tok == Token.comma)
					++iToken;
				Point3f pt2 = atomCenterOrCoordinateParameter(iToken);
				if (getToken(++iToken).tok == Token.comma)
					++iToken;
				Point3f pt3 = atomCenterOrCoordinateParameter(iToken);
				i = iToken;
				Vector3f norm = new Vector3f();
				float w = Measure.getNormalThroughPoints(pt1, pt2, pt3, norm,
						vAB, vAC);
				plane = new Point4f(norm.x, norm.y, norm.z, w);
				if (!isSyntaxCheck && Logger.debugging)
					Logger.debug("points: " + pt1 + pt2 + pt3
							+ " defined plane: " + plane);
				break;
			}
		if (plane == null)
			planeExpected();
		if (isNegated) {
			plane.scale(-1);
		}
		return plane;
	}

	private Point4f hklParameter(int i) throws ScriptException {
		if (!isSyntaxCheck && viewer.getCurrentUnitCell() == null)
			error(ERROR_noUnitCell);
		Point3f pt = (Point3f) getPointOrPlane(i, false, true, false, true, 3,
				3);
		Point4f p = getHklPlane(pt);
		if (p == null)
			error(ERROR_badMillerIndices);
		if (!isSyntaxCheck && Logger.debugging)
			Logger.info("defined plane: " + p);
		return p;
	}

	protected Point4f getHklPlane(Point3f pt) {
		Vector3f vAB = new Vector3f();
		Vector3f vAC = new Vector3f();
		Point3f pt1 = new Point3f(pt.x == 0 ? 1 : 1 / pt.x, 0, 0);
		Point3f pt2 = new Point3f(0, pt.y == 0 ? 1 : 1 / pt.y, 0);
		Point3f pt3 = new Point3f(0, 0, pt.z == 0 ? 1 : 1 / pt.z);
		// trick for 001 010 100 is to define the other points on other edges
		if (pt.x == 0 && pt.y == 0 && pt.z == 0) {
			return null;
		} else if (pt.x == 0 && pt.y == 0) {
			pt1.set(1, 0, pt3.z);
			pt2.set(0, 1, pt3.z);
		} else if (pt.y == 0 && pt.z == 0) {
			pt2.set(pt1.x, 0, 1);
			pt3.set(pt1.x, 1, 0);
		} else if (pt.z == 0 && pt.x == 0) {
			pt3.set(0, pt2.y, 1);
			pt1.set(1, pt2.y, 0);
		} else if (pt.x == 0) {
			pt1.set(1, pt2.y, 0);
		} else if (pt.y == 0) {
			pt2.set(0, 1, pt3.z);
		} else if (pt.z == 0) {
			pt3.set(pt1.x, 0, 1);
		}
		// base this one on the currently defined unit cell
		viewer.toCartesian(pt1, false);
		viewer.toCartesian(pt2, false);
		viewer.toCartesian(pt3, false);
		Vector3f plane = new Vector3f();
		float w = Measure
				.getNormalThroughPoints(pt1, pt2, pt3, plane, vAB, vAC);
		return new Point4f(plane.x, plane.y, plane.z, w);
	}

	private int getMadParameter() throws ScriptException {
		// wireframe, ssbond, hbond, struts
		int mad = 1;
		switch (getToken(1).tok) {
		case Token.only:
			restrictSelected(false, false);
			break;
		case Token.on:
			break;
		case Token.off:
			mad = 0;
			break;
		case Token.integer:
			int radiusRasMol = intParameter(1, 0, 750);
			mad = radiusRasMol * 4 * 2;
			break;
		case Token.decimal:
			mad = (int) (floatParameter(1, -3, 3) * 1000 * 2);
			if (mad < 0) {
				restrictSelected(false, false);
				mad = -mad;
			}
			break;
		default:
			error(ERROR_booleanOrNumberExpected);
		}
		return mad;
	}

	private int getSetAxesTypeMad(int index) throws ScriptException {
		if (index == statementLength)
			return 1;
		switch (getToken(checkLast(index)).tok) {
		case Token.on:
			return 1;
		case Token.off:
			return 0;
		case Token.dotted:
			return -1;
		case Token.integer:
			return intParameter(index, -1, 19);
		case Token.decimal:
			float angstroms = floatParameter(index, 0, 2);
			return (int) (angstroms * 1000 * 2);
		}
		error(ERROR_booleanOrWhateverExpected, "\"DOTTED\"");
		return 0;
	}

	private boolean isColorParam(int i) {
		int tok = tokAt(i);
		return (tok == Token.navy || tok == Token.spacebeforesquare
				|| tok == Token.leftsquare || tok == Token.varray
				|| tok == Token.point3f || isPoint3f(i) || (tok == Token.string || Token
				.tokAttr(tok, Token.identifier))
				&& ColorUtil.getArgbFromString((String) statement[i].value) != 0);
	}

	private int getArgbParam(int index) throws ScriptException {
		return getArgbParam(index, false);
	}

	private int getArgbParamLast(int index, boolean allowNone)
			throws ScriptException {
		int icolor = getArgbParam(index, allowNone);
		checkLast(iToken);
		return icolor;
	}

	private int getArgbParam(int index, boolean allowNone)
			throws ScriptException {
		Point3f pt = null;
		if (checkToken(index)) {
			switch (getToken(index).tok) {
			default:
				if (!Token.tokAttr(theTok, Token.identifier))
					break;
				//$FALL-THROUGH$
			case Token.navy:
			case Token.string:
				return ColorUtil.getArgbFromString(parameterAsString(index));
			case Token.spacebeforesquare:
				return getColorTriad(index + 2);
			case Token.leftsquare:
				return getColorTriad(++index);
			case Token.varray:
				float[] rgb = ScriptVariable.flistValue(theToken, 3);
				if (rgb != null && rgb.length != 3)
					pt = new Point3f(rgb[0], rgb[1], rgb[2]);
				break;
			case Token.point3f:
				pt = (Point3f) theToken.value;
				break;
			case Token.leftbrace:
				pt = getPoint3f(index, false);
				break;
			case Token.none:
				if (allowNone)
					return 0;
			}
		}
		if (pt == null)
			error(ERROR_colorExpected);
		return ColorUtil.colorPtToInt(pt);
	}

	private int getColorTriad(int i) throws ScriptException {
		float[] colors = new float[3];
		int n = 0;
		String hex = "";
		getToken(i);
		Point3f pt = null;
		float val = 0;
		out: switch (theTok) {
		case Token.integer:
		case Token.spec_seqcode:
		case Token.decimal:
			for (; i < statementLength; i++) {
				switch (getToken(i).tok) {
				case Token.comma:
					continue;
				case Token.identifier:
					if (n != 1 || colors[0] != 0)
						error(ERROR_badRGBColor);
					hex = "0" + parameterAsString(i);
					break out;
				case Token.decimal:
					if (n > 2)
						error(ERROR_badRGBColor);
					val = floatParameter(i);
					break;
				case Token.integer:
					if (n > 2)
						error(ERROR_badRGBColor);
					val = theToken.intValue;
					break;
				case Token.spec_seqcode:
					if (n > 2)
						error(ERROR_badRGBColor);
					val = ((Integer) theToken.value).intValue() % 256;
					break;
				case Token.rightsquare:
					if (n != 3)
						error(ERROR_badRGBColor);
					--i;
					pt = new Point3f(colors[0], colors[1], colors[2]);
					break out;
				default:
					error(ERROR_badRGBColor);
				}
				colors[n++] = val;
			}
			error(ERROR_badRGBColor);
			break;
		case Token.point3f:
			pt = (Point3f) theToken.value;
			break;
		case Token.identifier:
			hex = parameterAsString(i);
			break;
		default:
			error(ERROR_badRGBColor);
		}
		if (getToken(++i).tok != Token.rightsquare)
			error(ERROR_badRGBColor);
		if (pt != null)
			return ColorUtil.colorPtToInt(pt);
		if ((n = ColorUtil.getArgbFromString("[" + hex + "]")) == 0)
			error(ERROR_badRGBColor);
		return n;
	}

	private boolean coordinatesAreFractional;

	private boolean isPoint3f(int i) {
		// first check for simple possibilities:
		boolean isOK;
		if ((isOK = (tokAt(i) == Token.point3f)) || tokAt(i) == Token.point4f
				|| isFloatParameter(i + 1) && isFloatParameter(i + 2)
				&& isFloatParameter(i + 3) && isFloatParameter(i + 4))
			return isOK;
		ignoreError = true;
		int t = iToken;
		isOK = true;
		try {
			getPoint3f(i, true);
		} catch (Exception e) {
			isOK = false;
		}
		ignoreError = false;
		iToken = t;
		return isOK;
	}

	private Point3f getPoint3f(int i, boolean allowFractional)
			throws ScriptException {
		return (Point3f) getPointOrPlane(i, false, allowFractional, true,
				false, 3, 3);
	}

	private Point4f getPoint4f(int i) throws ScriptException {
		return (Point4f) getPointOrPlane(i, false, false, false, false, 4, 4);
	}

	private Point3f fractionalPoint;

	private Object getPointOrPlane(int index, boolean integerOnly,
			boolean allowFractional, boolean doConvert,
			boolean implicitFractional, int minDim, int maxDim)
			throws ScriptException {
		// { x y z } or {a/b c/d e/f} are encoded now as seqcodes and model
		// numbers
		// so we decode them here. It's a bit of a pain, but it isn't too bad.
		float[] coord = new float[6];
		int n = 0;
		coordinatesAreFractional = implicitFractional;
		if (tokAt(index) == Token.point3f) {
			if (minDim <= 3 && maxDim >= 3)
				return /* Point3f */getToken(index).value;
			error(ERROR_invalidArgument);
		}
		if (tokAt(index) == Token.point4f) {
			if (minDim <= 4 && maxDim >= 4)
				return /* Point4f */getToken(index).value;
			error(ERROR_invalidArgument);
		}
		int multiplier = 1;
		out: for (int i = index; i < statement.length; i++) {
			switch (getToken(i).tok) {
			case Token.leftbrace:
			case Token.comma:
			case Token.opAnd:
			case Token.opAND:
				break;
			case Token.rightbrace:
				break out;
			case Token.minus:
				multiplier = -1;
				break;
			case Token.spec_seqcode_range:
				if (n == 6)
					error(ERROR_invalidArgument);
				coord[n++] = theToken.intValue;
				multiplier = -1;
				break;
			case Token.integer:
			case Token.spec_seqcode:
				if (n == 6)
					error(ERROR_invalidArgument);
				coord[n++] = theToken.intValue * multiplier;
				multiplier = 1;
				break;
			case Token.divide:
			case Token.spec_model: // after a slash
				if (!allowFractional)
					error(ERROR_invalidArgument);
				if (theTok == Token.divide)
					getToken(++i);
				n--;
				if (n < 0 || integerOnly)
					error(ERROR_invalidArgument);
				if (theToken.value instanceof Integer
						|| theTok == Token.integer) {
					coord[n++] /= (theToken.intValue == Integer.MAX_VALUE ? ((Integer) theToken.value)
							.intValue() : theToken.intValue);
				} else if (theToken.value instanceof Float) {
					coord[n++] /= ((Float) theToken.value).floatValue();
				}
				coordinatesAreFractional = true;
				break;
			case Token.decimal:
			case Token.spec_model2:
				if (integerOnly)
					error(ERROR_invalidArgument);
				if (n == 6)
					error(ERROR_invalidArgument);
				coord[n++] = ((Float) theToken.value).floatValue();
				break;
			default:
				error(ERROR_invalidArgument);
			}
		}
		if (n < minDim || n > maxDim)
			error(ERROR_invalidArgument);
		if (n == 3) {
			Point3f pt = new Point3f(coord[0], coord[1], coord[2]);
			if (coordinatesAreFractional && doConvert) {
				fractionalPoint = new Point3f(pt);
				if (!isSyntaxCheck)
					viewer.toCartesian(pt, !viewer.getFractionalRelative());
			}
			return pt;
		}
		if (n == 4) {
			if (coordinatesAreFractional) // no fractional coordinates for
											// planes (how
				// to convert?)
				error(ERROR_invalidArgument);
			Point4f plane = new Point4f(coord[0], coord[1], coord[2], coord[3]);
			return plane;
		}
		return coord;
	}

	private Point3f xypParameter(int index) throws ScriptException {
		// [x y] or [x,y] refers to an xy point on the screen
		// return a Point3f with z = Float.MAX_VALUE
		// [x y %] or [x,y %] refers to an xy point on the screen
		// as a percent
		// return a Point3f with z = -Float.MAX_VALUE

		int tok = tokAt(index);
		if (tok == Token.spacebeforesquare)
			tok = tokAt(++index);
		if (tok != Token.leftsquare || !isFloatParameter(++index))
			return null;
		Point3f pt = new Point3f();
		pt.x = floatParameter(index);
		if (tokAt(++index) == Token.comma)
			index++;
		if (!isFloatParameter(index))
			return null;
		pt.y = floatParameter(index);
		boolean isPercent = (tokAt(++index) == Token.percent);
		if (isPercent)
			++index;
		if (tokAt(index) != Token.rightsquare)
			return null;
		iToken = index;
		pt.z = (isPercent ? -1 : 1) * Float.MAX_VALUE;
		return pt;
	}

	/*
	 * ****************************************************************
	 * =============== command dispatch ===============================
	 */

	/**
	 * provides support for the script editor
	 * 
	 * @param i
	 * @return true if displayable
	 */
	private boolean isCommandDisplayable(int i) {
		if (i >= aatoken.length || i >= pcEnd || aatoken[i] == null)
			return false;
		return (lineIndices[i][1] > lineIndices[i][0]);
	}

	/**
	 * checks to see if there is a pause condition, during which commands can
	 * still be issued, but with the ! first.
	 * 
	 * @return false if there was a problem
	 */
	private boolean checkContinue() {
		if (interruptExecution)
			return false;

		if (executionStepping && isCommandDisplayable(pc)) {
			viewer.scriptStatus("Next: " + getNextStatement(),
					"stepping -- type RESUME to continue", 0, null);
			executionPaused = true;
		} else if (!executionPaused) {
			return true;
		}

		if (Logger.debugging) {
			Logger.info("script execution paused at command " + (pc + 1)
					+ " level " + scriptLevel + ": " + thisCommand);
		}

		try {
			refresh();
			while (executionPaused) {
				viewer.popHoldRepaint("pause"); // does not actually do a
												// repaint
				Thread.sleep(100);
				String script = viewer.getInterruptScript();
				if (script != "") {
					resumePausedExecution();
					setErrorMessage(null);
					ScriptContext scSave = getScriptContext();
					pc--; // in case there is an error, we point to the PAUSE
							// command
					try {
						runScript(script);
					} catch (Exception e) {
						setErrorMessage("" + e);
					} catch (Error er) {
						setErrorMessage("" + er);
					}
					if (error) {
						scriptStatusOrBuffer(errorMessage);
						setErrorMessage(null);
					}
					restoreScriptContext(scSave, true, false, false);
					pauseExecution(false);
				}
				viewer.pushHoldRepaint("pause");
			}
			if (!isSyntaxCheck && !interruptExecution && !executionStepping) {
				viewer.scriptStatus("script execution "
						+ (error || interruptExecution ? "interrupted"
								: "resumed"));
			}
		} catch (Exception e) {
			viewer.pushHoldRepaint("pause");
		}
		Logger.debug("script execution resumed");
		// once more to trap quit during pause
		return !error && !interruptExecution;
	}

	/**
	 * here we go -- everything else in this class is called by this method or
	 * one of its subsidiary methods.
	 * 
	 * 
	 * @param doList
	 * @throws ScriptException
	 */
	private void instructionDispatchLoop(boolean doList) throws ScriptException {
		long timeBegin = 0;
		vProcess = null;
		boolean isForCheck = false; // indicates the stage of the for command
									// loop
		if (shapeManager == null)
			shapeManager = viewer.getShapeManager();
		debugScript = logMessages = false;
		if (!isSyntaxCheck)
			setDebugging();
		if (logMessages) {
			timeBegin = System.currentTimeMillis();
			viewer.scriptStatus("Eval.instructionDispatchLoop():" + timeBegin);
			viewer.scriptStatus(script);
		}
		if (pcEnd == 0)
			pcEnd = Integer.MAX_VALUE;
		if (lineEnd == 0)
			lineEnd = Integer.MAX_VALUE;
		String lastCommand = "";
		if (aatoken == null)
			return;
		for (; pc < aatoken.length && pc < pcEnd; pc++) {
			if (!isSyntaxCheck && !checkContinue())
				break;
			if (lineNumbers[pc] > lineEnd)
				break;
			theToken = (aatoken[pc].length == 0 ? null : aatoken[pc][0]);
			// when checking scripts, we can't check statments
			// containing @{...}
			if (!historyDisabled && !isSyntaxCheck
					&& scriptLevel <= commandHistoryLevelMax && !tQuiet) {
				String cmdLine = getCommand(pc, true, true);
				if (theToken != null
						&& cmdLine.length() > 0
						&& !cmdLine.equals(lastCommand)
						&& (theToken.tok == Token.function
								|| theToken.tok == Token.parallel || !Token
									.tokAttr(theToken.tok, Token.flowCommand)))
					viewer.addCommand(lastCommand = cmdLine);
			}
			if (!isSyntaxCheck) {
				String script = viewer.getInterruptScript();
				if (script != "")
					runScript(script);
			}
			if (!setStatement(pc)) {
				Logger.info(getCommand(pc, true, false)
						+ " -- STATEMENT CONTAINING @{} SKIPPED");
				continue;
			}
			thisCommand = getCommand(pc, false, true);
			fullCommand = thisCommand + getNextComment();
			getToken(0);
			iToken = 0;
			if (doList || !isSyntaxCheck) {
				int milliSecDelay = viewer.getScriptDelay();
				if (doList || milliSecDelay > 0 && scriptLevel > 0) {
					if (milliSecDelay > 0)
						delay(-(long) milliSecDelay);
					viewer.scriptEcho("$[" + scriptLevel + "."
							+ lineNumbers[pc] + "." + (pc + 1) + "] "
							+ thisCommand);
				}
			}
			if (vProcess != null
					&& (theTok != Token.end || statementLength < 2 || statement[1].tok != Token.process)) {
				vProcess.add(statement);
				continue;
			}
			if (isSyntaxCheck) {
				if (isCmdLine_c_or_C_Option)
					Logger.info(thisCommand);
				if (statementLength == 1 && statement[0].tok != Token.function
						&& statement[0].tok != Token.parallel)
					continue;
			} else {
				if (debugScript)
					logDebugScript(0);
				if (scriptLevel == 0 && viewer.logCommands())
					viewer.log(thisCommand);
				if (logMessages && theToken != null)
					Logger.debug(theToken.toString());
			}
			if (theToken == null)
				continue;
			if (Token.tokAttr(theToken.tok, Token.shapeCommand))
				processShapeCommand(theToken.tok);
			else
				switch (theToken.tok) {
				case Token.nada:
					if (isSyntaxCheck || !viewer.getMessageStyleChime())
						break;
					String s = (String) theToken.value;
					if (s == null)
						break;
					if (outputBuffer == null)
						viewer.showMessage(s);
					scriptStatusOrBuffer(s);
					break;
				case Token.push:
					pushContext((ContextToken) theToken);
					break;
				case Token.pop:
					popContext(true, false);
					break;
				case Token.colon:
					break;
				case Token.gotocmd:
				case Token.loop:
					if (viewer.isHeadless())
						break;
					//$FALL-THROUGH$
				case Token.catchcmd:
				case Token.breakcmd:
				case Token.continuecmd:
				case Token.elsecmd:
				case Token.elseif:
				case Token.end:
				case Token.endifcmd:
				case Token.forcmd:
				case Token.ifcmd:
				case Token.switchcmd:
				case Token.casecmd:
				case Token.defaultcmd:
				case Token.process:
				case Token.whilecmd:
					isForCheck = flowControl(theToken.tok, isForCheck);
					break;
				case Token.animation:
					animation();
					break;
				case Token.assign:
					assign();
					break;
				case Token.background:
					background(1);
					break;
				case Token.bind:
					bind();
					break;
				case Token.bondorder:
					bondorder();
					break;
				case Token.calculate:
					calculate();
					break;
				case Token.cache:
					cache();
					break;
				case Token.cd:
					cd();
					break;
				case Token.center:
					center(1);
					break;
				case Token.centerAt:
					centerAt();
					break;
				case Token.color:
					color();
					break;
				case Token.compare:
					compare();
					break;
				case Token.configuration:
					configuration();
					break;
				case Token.connect:
					connect(1);
					break;
				case Token.console:
					console();
					break;
				case Token.data:
					data();
					break;
				case Token.define:
					define();
					break;
				case Token.delay:
					delay();
					break;
				case Token.delete:
					delete();
					break;
				case Token.depth:
					slab(true);
					break;
				case Token.display:
					display(true);
					break;
				case Token.exit: // flush the queue and...
				case Token.quit: // quit this only if it isn't the first command
					if (isSyntaxCheck)
						break;
					if (pc > 0 && theToken.tok == Token.exit)
						viewer.clearScriptQueue();
					interruptExecution = (pc > 0 || !viewer.usingScriptQueue());
					break;
				case Token.exitjmol:
					if (isSyntaxCheck)
						return;
					viewer.exitJmol();
					break;
				case Token.file:
					file();
					break;
				case Token.fixed:
					fixed();
					break;
				case Token.font:
					font(-1, 0);
					break;
				case Token.frame:
				case Token.model:
					frame(1);
					break;
				case Token.parallel: // not actually found
				case Token.function:
				case Token.identifier:
					function(); // when a function is a command
					break;
				case Token.getproperty:
					getProperty();
					break;
				case Token.help:
					help();
					break;
				case Token.hide:
					display(false);
					break;
				case Token.hbond:
					hbond();
					break;
				case Token.history:
					history(1);
					break;
				case Token.hover:
					hover();
					break;
				case Token.initialize:
					if (!isSyntaxCheck)
						viewer.initialize(!isStateScript);
					break;
				case Token.invertSelected:
					invertSelected();
					break;
				case Token.javascript:
					script(Token.javascript, null, false);
					break;
				case Token.load:
					load();
					break;
				// added -hcf
				case Token.scaledown:
					scaledown();
					break;
				case Token.scaleup:
					scaleup();
					break;
				case Token.gselect:
					gselect();
					break;
				case Token.sselect:
					sselect();
					break;
				case Token.getseqEnsembl:
					gseqEnsembl();
					break;
				case Token.killThread:
					kilThread();
					break;
				case Token.getseqUCSCGB:
					gseqUCSCGB();
					break;
				case Token.getseqLocal:
					gseqLocal();
					break;
				case Token.getseqBlast:
					gseqBlast();
					break;
				case Token.getseqTranscribe:
					gseqTranscribe();
					break;
				case Token.getseqGene:
					gseqGene();
					break;
				case Token.getseqProperties:
					gseqProp();
					break;
				case Token.extractPDB:
					extractPDB();
					break;
				// added end -hcf
				case Token.log:
					log();
					break;
				case Token.mapProperty:
					mapProperty();
					break;
				case Token.message:
					message();
					break;
				case Token.minimize:
					minimize();
					break;
				case Token.move:
					move();
					break;
				case Token.moveto:
					moveto();
					break;
				case Token.navigate:
					navigate();
					break;
				case Token.pause: // resume is done differently
					pause();
					break;
				case Token.plot:
				case Token.quaternion:
				case Token.ramachandran:
					plot(statement);
					break;
				case Token.print:
					print();
					break;
				case Token.prompt:
					prompt();
					break;
				case Token.redomove:
				case Token.undomove:
					undoRedoMove();
					break;
				case Token.refresh:
					refresh();
					break;
				case Token.reset:
					reset();
					break;
				case Token.restore:
					restore();
					break;
				case Token.restrict:
					restrict();
					break;
				case Token.resume:
					if (!isSyntaxCheck)
						resumePausedExecution();
					break;
				case Token.returncmd:
					returnCmd(null);
					break;
				case Token.rotate:
					rotate(false, false);
					break;
				case Token.rotateSelected:
					rotate(false, true);
					break;
				case Token.save:
					save();
					break;
				case Token.set:
					set();
					break;
				case Token.script:
					script(Token.script, null, doList);
					break;
				case Token.select:
					select(1);
					break;
				case Token.selectionhalos:
					selectionHalo(1);
					break;
				case Token.show:
					show();
					break;
				case Token.slab:
					slab(false);
					break;
				// case Token.slice:
				// slice();
				// break;
				case Token.spin:
					rotate(true, false);
					break;
				case Token.ssbond:
					ssbond();
					break;
				case Token.step:
					if (pause())
						stepPausedExecution();
					break;
				case Token.stereo:
					stereo();
					break;
				case Token.structure:
					structure();
					break;
				case Token.subset:
					subset();
					break;
				case Token.sync:
					sync();
					break;
				case Token.timeout:
					timeout(1);
					break;
				case Token.translate:
					translate(false);
					break;
				case Token.translateSelected:
					translate(true);
					break;
				case Token.unbind:
					unbind();
					break;
				case Token.vibration:
					vibration();
					break;
				case Token.write:
					write(null);
					break;
				case Token.zap:
					zap(true);
					break;
				case Token.zoom:
					zoom(false);
					break;
				case Token.zoomTo:
					zoom(true);
					break;
				//Tuan added for 3D genome functions
				case Token.pdb2gss:
					convertPDB2GSS();
					break;				
				case Token.lorDG:
					lorDG3DModeller();
					break;
				case Token.loopDetector:
					loopIdentifier();
					break;
				case Token.annotate:
					annotate();
					break;
					

					
				//end
				// Tosin added for 3D genome functions	
				case Token.struct_3DMax:
					Structure_3DMax();
					break;
				case Token.Heatmap2D:
					Heatmap_Visualization();
					break;
				case Token.FindTAD2D:
					Find_TAD();
					break;				
				default:
					error(ERROR_unrecognizedCommand);
				}
			setCursorWait(false);
			// at end because we could use continue to avoid it
			if (executionStepping) {
				executionPaused = (isCommandDisplayable(pc + 1));
			}
		}
	}

	/**
	 * @author Tuan
	 */
	private void annotate(){
		Annotator annotator = new Annotator();
		try{
			
			if (viewer.getParameter(Constants.TRACKNAME) == null || ((String)viewer.getParameter(Constants.TRACKNAME)).length() == 0){
				annotator.deannotate((Viewer)viewer);
				return;
			}
			
			String trackName = (String) viewer.getParameter(Constants.TRACKNAME);
			String trackFileName = (String) viewer.getParameter(Constants.TRACKFILENAME);
			String color = (String) viewer.getParameter(Constants.ANNOTATIONCOLOR);
			
			String probeGeneFile = (String) viewer.getParameter(Constants.PROBECOORDINATEFILE);
			
			if (trackFileName.endsWith(".gct") && probeGeneFile.length() > 0){
				annotator.annotateGeneExpression(trackName, trackFileName, probeGeneFile, color, 15, viewer);
			}else if (color.length() > 0){
				annotator.annotate(trackName, trackFileName, color, 15, viewer);
			}else{
				annotator.annotateDomain(trackName, trackFileName, viewer);
			}
			
		}catch(Exception ex){
			viewer.displayMessage(new String[]{ex.getMessage()});
			ex.printStackTrace();
		}
	}

	
	/**
	 * @author Tuan
	 * To convert a pdb format file to a gss format file
	 */
	private void convertPDB2GSS(){
		String pdbFile = (String) viewer.getParameter(Constants.INPUTPDBFILE);
		String mappingFile = (String) viewer.getParameter(Constants.INPUTMAPPINGFILE);
		String gssFile = (String) viewer.getParameter(Constants.OUTPUTGSSFILE);
		
		ConvertPDB2GSS pdb2GSSConverter = new ConvertPDB2GSS();
		try{
			pdb2GSSConverter.convertToGSS(pdbFile, mappingFile, gssFile);
			viewer.displayMessage(new String[]{"File converted successfully!"});
		}catch(Exception ex){
			viewer.displayMessage(new String[]{ex.getMessage()});
			ex.printStackTrace();
		}
    	
	}
	
	/**
	 * @author Tuan
	 */
	private void loopIdentifier(){
		Detector loopDetector = new Detector();
		try{
			loopDetector.identifyLoop(viewer.getModelSet().atoms, viewer);
		}catch(Exception ex){
			viewer.displayMessage(new String[]{ex.getMessage()});
		}
	}
	/**
	 * @author Tuan
	 * To reconstruct 3D model using LorDG
	 */
	private void lorDG3DModeller(){
		
		String contactFile = (String) viewer.getParameter(Constants.INPUTCONTACTFILE);		
		String outputFolder = (String) viewer.getParameter(Constants.OUTPUT3DFILE);
		
		
		
		double conversionFactor = 0, minConversionFactor = 0.1, maxConversionFactor = 3.0;
		
		String conversionFactorStr = (String)viewer.getParameter(Constants.CONVERSIONFACTOR);
		String minConversion = (String) viewer.getParameter(Constants.MINCONVERSIONFACTOR);
		String maxConversion = (String) viewer.getParameter(Constants.MAXCONVERSIONFACTOR);
		
		if (conversionFactorStr.length() > 0) conversionFactor = Double.parseDouble(conversionFactorStr);
		
		if (minConversion.length() > 0) minConversionFactor = Double.parseDouble(minConversion);
		if (maxConversion.length() > 0) maxConversionFactor = Double.parseDouble(maxConversion);
		
		
		
		
		double learningRate = Double.parseDouble((String)viewer.getParameter(Constants.LEARNINGRATE));
		int maxIteration = Integer.parseInt((String)viewer.getParameter(Constants.MAXITERATION));
		
		InputParameters inputParameter = new InputParameters();
		
		String[] st;
		
		String chromLen = (String) viewer.getParameter(Constants.CHROMOSOMELEN);
		if (chromLen != null && chromLen.length() > 0){
			st = chromLen.split("[\\s,]");
			int[] chrLen = new int[st.length];
			for(int i = 0; i < st.length; i++){
				chrLen[i] = Integer.parseInt(st[i]);
			}
			
			if (chrLen.length > 1) inputParameter.setChr_lens(chrLen);
		}
		
		String chrom = (String) viewer.getParameter(Constants.CHROMOSOME);
		String genomeID = (String) viewer.getParameter(Constants.GENOMEID);
		
		inputParameter.setNum(1);
		inputParameter.setOutput_folder(outputFolder);
		inputParameter.setInput_file(contactFile);
		inputParameter.setLearning_rate(learningRate);
		inputParameter.setNumber_threads(1);
		
		if (conversionFactor > 0.001) inputParameter.setConvert_factor(conversionFactor);
		
		inputParameter.setMinConversionFactor(minConversionFactor);
		inputParameter.setMaxConversionFactor(maxConversionFactor);
		
		inputParameter.setMax_iteration(maxIteration);
		
		inputParameter.setChrom(chrom);
		inputParameter.setGenomeID(genomeID);
		
		inputParameter.setVerbose(false);
		
		st = contactFile.split("[\\/\\.\\\\]");
		
		if (contactFile.contains(".")){
			inputParameter.setFile_prefix(st[st.length - 2]);
		}else{
			inputParameter.setFile_prefix(st[st.length - 1]);
		}
		
		inputParameter.setViewer(viewer);
		inputParameter.setTmpFolder(outputFolder + "/tmp/");
		
		Helper helper = Helper.getHelperInstance();
		if (!helper.isExist(inputParameter.getTmpFolder())){
			try {
				helper.make_folder(inputParameter.getTmpFolder());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String genomicLocationFile = (String) viewer.getParameter(Constants.GENOMICLOCATIONFILE);
		if (genomicLocationFile.length() > 0) {
			try {
				Map<Integer,GenomicLocation> map = helper.readGenomicLocation(genomicLocationFile);
				inputParameter.setIdToGenomLocation(map);
				
			} catch (Exception e) {
				viewer.displayMessage(new String[]{e.getMessage()});
				e.printStackTrace();
			}
		}
		
		
		viewer.setInput3DModeller(inputParameter);
		
		StructureGeneratorLorentz_HierarchicalModeling generator = new StructureGeneratorLorentz_HierarchicalModeling(inputParameter);
		try{
			generator.generateStructure();
		}catch(Exception ex){
			
			viewer.displayMessage(new String[]{ex.getMessage()});
			ex.printStackTrace();
		}

    	
	}

	
	/**
	 * @author-Tosin
	 * To reconstruct 3D model using 3DMax
	 */
	private void Structure_3DMax() {
		
		
		 String contactFile = (String) viewer.getParameter(Constants.INPUTCONTACTFILE);		
		 String outputFolder = (String) viewer.getParameter(Constants.OUTPUT3DFILE);
			
			
			
		double conversionFactor = 0, minConversionFactor = 0.1, maxConversionFactor = 3.0;
		
		String conversionFactorStr = (String)viewer.getParameter(Constants.CONVERSIONFACTOR);
		String minConversion = (String) viewer.getParameter(Constants.MINCONVERSIONFACTOR);
		String maxConversion = (String) viewer.getParameter(Constants.MAXCONVERSIONFACTOR);
		
		if (conversionFactorStr.length() > 0) conversionFactor = Double.parseDouble(conversionFactorStr);
		
		if (minConversion.length() > 0) minConversionFactor = Double.parseDouble(minConversion);
		if (maxConversion.length() > 0) maxConversionFactor = Double.parseDouble(maxConversion);
		
		
		
		
		double learningRate = Double.parseDouble((String)viewer.getParameter(Constants.LEARNINGRATE));
		int maxIteration = Integer.parseInt((String)viewer.getParameter(Constants.MAXITERATION));
		
		InputParameters_3DMax inputParameter = new InputParameters_3DMax();
		
		String[] st;
		
		String chromLen = (String) viewer.getParameter(Constants.CHROMOSOMELEN);
		if (chromLen != null && chromLen.length() > 0){
			st = chromLen.split("[\\s,]");
			int[] chrLen = new int[st.length];
			for(int i = 0; i < st.length; i++){
				chrLen[i] = Integer.parseInt(st[i]);
			}
			
			if (chrLen.length > 1) inputParameter.setChr_lens(chrLen);
		}
		
		String chrom = (String) viewer.getParameter(Constants.CHROMOSOME);
		String genomeID = (String) viewer.getParameter(Constants.GENOMEID);
		
		inputParameter.setNum(1);
		inputParameter.setOutput_folder(outputFolder);
		inputParameter.setInput_file(contactFile);
		inputParameter.setLearning_rate(learningRate);
		inputParameter.setNumber_threads(1);
		
		if (conversionFactor > 0.001) inputParameter.setConvert_factor(conversionFactor);
		
		inputParameter.setMinConversionFactor(minConversionFactor);
		inputParameter.setMaxConversionFactor(maxConversionFactor);
		
		inputParameter.setMax_iteration(maxIteration);
		
		inputParameter.setChrom(chrom);
		inputParameter.setGenomeID(genomeID);
		
		inputParameter.setVerbose(false);
		
		st = contactFile.split("[\\/\\.\\\\]");
		
		if (contactFile.contains(".")){
			inputParameter.setFile_prefix(st[st.length - 2]);
		}else{
			inputParameter.setFile_prefix(st[st.length - 1]);
		}
		
		inputParameter.setViewer(viewer);
		inputParameter.setTmpFolder(outputFolder + "/tmp/");
		
		Helper helper = Helper.getHelperInstance();
		if (!helper.isExist(inputParameter.getTmpFolder())){
			try {
				helper.make_folder(inputParameter.getTmpFolder());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		 
		viewer.setInput3DModeller_3DMax(inputParameter);
		
		
		 // Call the Structure_3DMax	
		
		StructureGenerator3DMax  generator = new StructureGenerator3DMax(inputParameter);
		try{
			generator.generateStructure();
		}catch(Exception ex){
			
			viewer.displayMessage(new String[]{ex.getMessage()});
			ex.printStackTrace();
		}
		 
	
		
	}
	
	/**
	 * @author-Tosin
	 * To Find the TAD
	 */
	private void Find_TAD() {
		String[] Input = new String[6];
		 Input[0] = (String) viewer.getParameter(Constants.INPUTCONTACTFILE);		
		 Input[1] = (String) viewer.getParameter(Constants.OUTPUT3DFILE);			
		 Input[2] = (String)viewer.getParameter(Constants.IFRESOLUTION);	
		 Input[3] = (String)viewer.getParameter(Constants.ISMATRIX);	
		 Input[4] = (String)viewer.getParameter(Constants.STARTLOCATION);	
		 Input[5] = (String) viewer.getParameter(Constants.CHROMOSOME);	
		 
		// Call the ClusteTAD
		 
		try{
						
			@SuppressWarnings("unused")
			ClusterTAD ctad = new ClusterTAD(Input,viewer);			
			
		}catch(Exception ex){
		    JOptionPane.showMessageDialog(null, "An error Occured!, Check File for Output","Alert!",JOptionPane.ERROR_MESSAGE);
			viewer.displayMessage(new String[]{ex.getMessage()});
			ex.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * @author Tosin
	 * To visualize the HeatMap
	 */
	
	private void Heatmap_Visualization() {
		
		 SwingUtilities.invokeLater(new Runnable()
	        {
	            public void run()
	            {
	                try
	                {	 
	                	// get the screen size as a java dimension
	                	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	                	// get 2/3 of the height, and 2/3 of the width
	                	int height = screenSize.height* 7 / 10;
	                	int width = screenSize.width* 7 / 10;
	                	

	                	 hmd = new HeatMapDemo(); //Tosin added Filename
	                     hmd.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);	                  
	                    // hmd.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
	                     hmd.setSize(width, height);
	                    // hmd.setMinimumSize(new Dimension(800, 900));
	                     hmd.setVisible(true);
	                }
	                catch (Exception e)
	                {
	                    System.err.println(e);
	                    e.printStackTrace();
	                }
	            }
	        });
	}
	
	
	///
	private void cache() throws ScriptException {
		checkLength(3);
		int tok = tokAt(1);
		String fileName = parameterAsString(2);
		switch (tok) {
		case Token.add:
		case Token.remove:
			if (!isSyntaxCheck) {
				if (tok == Token.remove && tokAt(2) == Token.all)
					fileName = null;
				int nBytes = viewer.cacheFileByName(fileName, tok == Token.add);
				showString(nBytes < 0 ? "cache cleared" : nBytes + " bytes "
						+ (tok == Token.add ? " cached" : " removed"));
			}
			break;
		default:
			error(ERROR_invalidArgument);
		}
	}

	private void setCursorWait(boolean TF) {
		if (!isSyntaxCheck)
			viewer.setCursor(TF ? JmolConstants.CURSOR_WAIT
					: JmolConstants.CURSOR_DEFAULT);
	}

	private void processShapeCommand(int tok) throws ScriptException {
		int iShape = 0;
		switch (tok) {
		case Token.axes:
			iShape = JmolConstants.SHAPE_AXES;
			break;
		case Token.backbone:
			iShape = JmolConstants.SHAPE_BACKBONE;
			break;
		case Token.boundbox:
			iShape = JmolConstants.SHAPE_BBCAGE;
			break;
		case Token.cartoon:
			iShape = JmolConstants.SHAPE_CARTOON;
			break;
		case Token.contact:
			iShape = JmolConstants.SHAPE_CONTACT;
			break;
		case Token.dipole:
			iShape = JmolConstants.SHAPE_DIPOLES;
			break;
		case Token.dots:
			iShape = JmolConstants.SHAPE_DOTS;
			break;
		case Token.draw:
			iShape = JmolConstants.SHAPE_DRAW;
			break;
		case Token.echo:
			iShape = JmolConstants.SHAPE_ECHO;
			break;
		case Token.ellipsoid:
			iShape = JmolConstants.SHAPE_ELLIPSOIDS;
			break;
		case Token.frank:
			iShape = JmolConstants.SHAPE_FRANK;
			break;
		case Token.geosurface:
			iShape = JmolConstants.SHAPE_GEOSURFACE;
			break;
		case Token.halo:
			iShape = JmolConstants.SHAPE_HALOS;
			break;
		case Token.isosurface:
			iShape = JmolConstants.SHAPE_ISOSURFACE;
			break;
		case Token.label:
			iShape = JmolConstants.SHAPE_LABELS;
			break;
		case Token.lcaocartoon:
			iShape = JmolConstants.SHAPE_LCAOCARTOON;
			break;
		case Token.measurements:
		case Token.measure:
			iShape = JmolConstants.SHAPE_MEASURES;
			break;
		case Token.meshRibbon:
			iShape = JmolConstants.SHAPE_MESHRIBBON;
			break;
		case Token.mo:
			iShape = JmolConstants.SHAPE_MO;
			break;
		case Token.plot3d:
			iShape = JmolConstants.SHAPE_PLOT3D;
			break;
		case Token.pmesh:
			iShape = JmolConstants.SHAPE_PMESH;
			break;
		case Token.polyhedra:
			iShape = JmolConstants.SHAPE_POLYHEDRA;
			break;
		case Token.ribbon:
			iShape = JmolConstants.SHAPE_RIBBONS;
			break;
		case Token.rocket:
			iShape = JmolConstants.SHAPE_ROCKETS;
			break;
		case Token.spacefill: // aka cpk
			iShape = JmolConstants.SHAPE_BALLS;
			break;
		case Token.star:
			iShape = JmolConstants.SHAPE_STARS;
			break;
		case Token.strands:
			iShape = JmolConstants.SHAPE_STRANDS;
			break;
		case Token.struts:
			iShape = JmolConstants.SHAPE_STRUTS;
			break;
		case Token.trace:
			iShape = JmolConstants.SHAPE_TRACE;
			break;
		case Token.unitcell:
			iShape = JmolConstants.SHAPE_UCCAGE;
			break;
		case Token.vector:
			iShape = JmolConstants.SHAPE_VECTORS;
			break;
		case Token.wireframe:
			iShape = JmolConstants.SHAPE_STICKS;
			break;
		default:
			error(ERROR_unrecognizedCommand);
		}

		// atom objects:

		switch (tok) {
		case Token.backbone:
		case Token.cartoon:
		case Token.meshRibbon:
		case Token.ribbon:
		case Token.rocket:
		case Token.strands:
		case Token.trace:
			proteinShape(iShape);
			return;
		case Token.dots:
		case Token.geosurface:
			dots(iShape);
			return;
		case Token.ellipsoid:
			ellipsoid();
			return;
		case Token.halo:
		case Token.spacefill: // aka cpk
		case Token.star:
			setAtomShapeSize(iShape, (tok == Token.halo ? -1f : 1f));
			return;
		case Token.label:
			label(1);
			return;
		case Token.lcaocartoon:
			lcaoCartoon();
			return;
		case Token.polyhedra:
			polyhedra();
			return;
		case Token.struts:
			struts();
			return;
		case Token.vector:
			vector();
			return;
		case Token.wireframe:
			wireframe();
			return;
		}

		// other objects:

		switch (tok) {
		case Token.axes:
			axes(1);
			return;
		case Token.boundbox:
			boundbox(1);
			return;
		case Token.contact:
			contact();
			return;
		case Token.dipole:
			dipole();
			return;
		case Token.draw:
			draw();
			return;
		case Token.echo:
			echo(1, false);
			return;
		case Token.frank:
			frank(1);
			return;
		case Token.isosurface:
		case Token.plot3d:
		case Token.pmesh:
			isosurface(iShape);
			return;
		case Token.measurements:
		case Token.measure:
			measure();
			return;
		case Token.mo:
			mo(false);
			return;
		case Token.unitcell:
			unitcell(1);
			return;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean flowControl(int tok, boolean isForCheck)
			throws ScriptException {
		ContextToken ct;
		switch (tok) {
		case Token.gotocmd:
			gotoCmd(parameterAsString(checkLast(1)));
			return isForCheck;
		case Token.loop:
			// back to the beginning of this script
			delay();
			if (!isSyntaxCheck)
				pc = -1;
			return isForCheck;
		}
		int pt = statement[0].intValue;
		boolean isDone = (pt < 0 && !isSyntaxCheck);
		boolean isOK = true;
		int ptNext = 0;
		switch (tok) {
		case Token.catchcmd:
			ct = (ContextToken) theToken;
			pushContext(ct);
			if (!isDone && ct.name0 != null)
				contextVariables.put(ct.name0,
						ct.contextVariables.get(ct.name0));
			isOK = !isDone;
			break;
		case Token.process:
			pushContext((ContextToken) theToken);
			isDone = isOK = true;
			addProcess(pc, pt, true);
			break;
		case Token.switchcmd:
		case Token.defaultcmd:
		case Token.casecmd:
			ptNext = Math.abs(aatoken[Math.abs(pt)][0].intValue);
			switch (isDone ? 0 : switchCmd((ContextToken) theToken, tok)) {
			case 0:
				// done
				ptNext = -ptNext;
				isOK = false;
				break;
			case -1:
				// skip this case
				isOK = false;
				break;
			case 1:
				// do this one
			}
			aatoken[pc][0].intValue = Math.abs(pt);
			theToken = aatoken[Math.abs(pt)][0];
			if (theToken.tok != Token.end)
				theToken.intValue = ptNext;
			break;
		case Token.ifcmd:
		case Token.elseif:
			isOK = (!isDone && ifCmd());
			if (isSyntaxCheck)
				break;
			ptNext = Math.abs(aatoken[Math.abs(pt)][0].intValue);
			ptNext = (isDone || isOK ? -ptNext : ptNext);
			aatoken[Math.abs(pt)][0].intValue = ptNext;
			if (tok == Token.catchcmd)
				aatoken[pc][0].intValue = -pt; // reset to "done" state
			break;
		case Token.elsecmd:
			checkLength(1);
			if (pt < 0 && !isSyntaxCheck)
				pc = -pt - 1;
			break;
		case Token.endifcmd:
			checkLength(1);
			break;
		case Token.whilecmd:
			if (!isForCheck)
				pushContext((ContextToken) theToken);
			isForCheck = false;
			if (!ifCmd() && !isSyntaxCheck) {
				pc = pt;
				popContext(true, false);
			}
			break;
		case Token.breakcmd:
			if (!isSyntaxCheck) {
				breakCmd(pt);
				break;
			}
			if (statementLength == 1)
				break;
			int n = intParameter(checkLast(1));
			if (isSyntaxCheck)
				break;
			for (int i = 0; i < n; i++)
				popContext(true, false);
			break;
		case Token.continuecmd:
			isForCheck = true;
			if (!isSyntaxCheck)
				pc = pt - 1;
			if (statementLength > 1)
				intParameter(checkLast(1));
			break;
		case Token.forcmd:
			// for (i = 1; i < 3; i = i + 1);
			// for (var i = 1; i < 3; i = i + 1);
			// for (;;;);
			// for (var x in {...}) { xxxxx }
			// for (var x in y) { xxxx }
			Token token = theToken;
			int[] pts = new int[2];
			int j = 0;
			Object bsOrList = null;
			for (int i = 1, nSkip = 0; i < statementLength && j < 2; i++) {
				switch (tokAt(i)) {
				case Token.semicolon:
					if (nSkip > 0)
						nSkip--;
					else
						pts[j++] = i;
					break;
				case Token.in:
					nSkip -= 2;
					if (tokAt(++i) == Token.expressionBegin
							|| tokAt(i) == Token.bitset) {
						bsOrList = atomExpressionAt(i);
						if (isBondSet)
							bsOrList = new BondSet((BitSet) bsOrList);
					} else {
						List<ScriptVariable> what = parameterExpressionList(-i,
								1, false);
						if (what == null || what.size() < 1)
							error(ERROR_invalidArgument);
						ScriptVariable vl = what.get(0);
						switch (vl.tok) {
						case Token.bitset:
							bsOrList = ScriptVariable.getBitSet(vl, false);
							break;
						case Token.varray:
							bsOrList = vl.getList();
							break;
						default:
							error(ERROR_invalidArgument);
						}
					}
					i = iToken;
					break;
				case Token.select:
					nSkip += 2;
					break;
				}

			}
			if (isForCheck) {
				j = (bsOrList == null ? pts[1] + 1 : 2);
			} else {
				pushContext((ContextToken) token);
				j = 2;
			}
			if (tokAt(j) == Token.var)
				j++;
			String key = parameterAsString(j);
			boolean isMinusMinus = key.equals("--") || key.equals("++");
			if (isMinusMinus) {
				key = parameterAsString(++j);
			}
			ScriptVariable v = null;
			if (Token.tokAttr(tokAt(j), Token.misc)
					|| (v = getContextVariableAsVariable(key)) != null) {
				if (bsOrList == null && !isMinusMinus
						&& getToken(++j).tok != Token.opEQ)
					error(ERROR_invalidArgument);
				if (bsOrList == null) {
					if (isMinusMinus)
						j -= 2;
					setVariable(++j, statementLength - 1, key, 0);
				} else {
					// for (var x in {xx})....
					isOK = true;
					String key_incr = (key + "_incr");
					if (v == null)
						v = getContextVariableAsVariable(key_incr);
					if (v == null) {
						if (key.startsWith("_"))
							error(ERROR_invalidArgument);
						v = viewer.getOrSetNewVariable(key_incr, true);
					}
					if (!isForCheck || v.tok != Token.bitset
							&& v.tok != Token.varray
							|| v.intValue == Integer.MAX_VALUE) {
						if (isForCheck) {
							// someone messed with this variable -- do not
							// continue!
							isOK = false;
						} else {
							v.set(ScriptVariable.getVariable(bsOrList), false);
							v.intValue = 1;
						}
					} else {
						v.intValue++;
					}
					isOK = isOK
							&& (bsOrList instanceof BitSet ? ScriptVariable
									.bsSelectVar(v).cardinality() == 1
									: v.intValue <= v.getList().size());
					if (isOK) {
						v = ScriptVariable.selectItemVar(v);
						ScriptVariable t = getContextVariableAsVariable(key);
						if (t == null)
							t = viewer.getOrSetNewVariable(key, true);
						t.set(v, false);
					}
				}
			}
			if (bsOrList == null)
				isOK = parameterExpressionBoolean(pts[0] + 1, pts[1]);
			pt++;
			if (!isOK)
				popContext(true, false);
			isForCheck = false;
			break;
		case Token.end: // function, if, for, while, catch, switch
			switch (getToken(checkLast(1)).tok) {
			case Token.trycmd:
				ScriptFunction trycmd = (ScriptFunction) getToken(1).value;
				if (isSyntaxCheck)
					return false;
				Map<String, ScriptVariable> cv = (Map<String, ScriptVariable>) runFunction(
						trycmd, "try", null, null, true, true).value;
				ScriptVariable ret = cv.get("_tryret");
				if (ret.value != null || ret.intValue != Integer.MAX_VALUE) {
					returnCmd(ret);
					return false;
				}
				String errMsg = (String) (cv.get("_errorval")).value;
				if (errMsg.length() == 0) {
					int iBreak = (cv.get("_breakval")).intValue;
					if (iBreak != Integer.MAX_VALUE) {
						breakCmd(pc - iBreak);
						return false;
					}
				}
				// normal return will skip the catch
				if (pc + 1 < aatoken.length
						&& aatoken[pc + 1][0].tok == Token.catchcmd) {
					// set the intValue positive to indicate "not done" for the
					// IF evaluation
					ct = (ContextToken) aatoken[pc + 1][0];
					if (ct.contextVariables != null && ct.name0 != null)
						ct.contextVariables.put(ct.name0,
								ScriptVariable.getVariable(errMsg));
					ct.intValue = (errMsg.length() > 0 ? 1 : -1)
							* Math.abs(ct.intValue);
				}
				return false;
			case Token.catchcmd:
				popContext(true, false);
				break;
			case Token.function:
			case Token.parallel:
				viewer.addFunction((ScriptFunction) theToken.value);
				return isForCheck;
			case Token.process:
				addProcess(pt, pc, false);
				popContext(true, false);
				break;
			case Token.switchcmd:
				if (pt > 0 && switchCmd((ContextToken) aatoken[pt][0], 0) == -1) {
					// check for the default position
					for (; pt < pc; pt++)
						if ((tok = aatoken[pt][0].tok) != Token.defaultcmd
								&& tok != Token.casecmd)
							break;
					isOK = (pc == pt);
				}
				break;
			}
			if (isOK)
				isOK = (theTok == Token.catchcmd || theTok == Token.process
						|| theTok == Token.ifcmd || theTok == Token.switchcmd);
			isForCheck = (theTok == Token.forcmd || theTok == Token.whilecmd);
			break;
		}
		if (!isOK && !isSyntaxCheck)
			pc = Math.abs(pt) - 1;
		return isForCheck;
	}

	private void gotoCmd(String strTo) throws ScriptException {
		int pcTo = (strTo == null ? aatoken.length - 1 : -1);
		String s = null;
		for (int i = pcTo + 1; i < aatoken.length; i++) {
			Token[] tokens = aatoken[i];
			int tok = tokens[0].tok;
			switch (tok) {
			case Token.message:
			case Token.nada:
				s = (String) tokens[tokens.length - 1].value;
				if (tok == Token.nada)
					s = s.substring(s.startsWith("#") ? 1 : 2);
				break;
			default:
				continue;
			}
			if (s.equalsIgnoreCase(strTo)) {
				pcTo = i;
				break;
			}
		}
		if (pcTo < 0)
			error(ERROR_invalidArgument);
		if (strTo == null)
			pcTo = 0;
		int di = (pcTo < pc ? 1 : -1);
		int nPush = 0;
		for (int i = pcTo; i != pc; i += di) {
			switch (aatoken[i][0].tok) {
			case Token.push:
			case Token.process:
			case Token.forcmd:
			case Token.catchcmd:
			case Token.whilecmd:
				nPush++;
				break;
			case Token.pop:
				nPush--;
				break;
			case Token.end:
				switch (aatoken[i][1].tok) {
				case Token.process:
				case Token.forcmd:
				case Token.catchcmd:
				case Token.whilecmd:
					nPush--;
				}
				break;
			}
		}
		if (strTo == null) {
			pcTo = Integer.MAX_VALUE;
			for (; nPush > 0; --nPush)
				popContext(false, false);
		}
		if (nPush != 0)
			error(ERROR_invalidArgument);
		if (!isSyntaxCheck)
			pc = pcTo - 1; // ... resetting the program counter
	}

	private void breakCmd(int pt) {
		// pt is a backward reference
		if (pt < 0) {
			// this is a break within a try{...} block
			getContextVariableAsVariable("_breakval").intValue = -pt;
			pcEnd = pc;
			return;
		}
		pc = Math.abs(aatoken[pt][0].intValue);
		int tok = aatoken[pt][0].tok;
		if (tok == Token.casecmd || tok == Token.defaultcmd) {
			theToken = aatoken[pc--][0];
			int ptNext = Math.abs(theToken.intValue);
			if (theToken.tok != Token.end)
				theToken.intValue = -ptNext;
		} else {
			while (thisContext != null
					&& !ScriptCompiler
							.isBreakableContext(thisContext.token.tok))
				popContext(true, false);
			popContext(true, false);
		}
	}

	private List<Token[]> vProcess;
	static int iProcess;

	private void addProcess(int pc, int pt, boolean isStart) {
		if (parallelProcessor == null)
			return;
		if (isStart) {
			vProcess = new ArrayList<Token[]>();
		} else {

			Token[][] statements = new Token[pt][];
			for (int i = 0; i < vProcess.size(); i++)
				statements[i + 1 - pc] = vProcess.get(i);
			ScriptContext context = getScriptContext();
			context.aatoken = statements;
			context.pc = 1 - pc;
			context.pcEnd = pt;
			parallelProcessor.addProcess("p" + (++iProcess), context);
			vProcess = null;
		}
	}

	private int switchCmd(ContextToken c, int tok) throws ScriptException {
		if (tok == Token.switchcmd)
			c.addName("_var");
		ScriptVariable var = c.contextVariables.get("_var");
		if (var == null)
			return 1; // OK, case found -- no more testing
		if (tok == 0) {
			// end: remove variable and do default
			// this causes all other cases to
			// skip
			c.contextVariables.remove("_var");
			return -1;
		}
		if (tok == Token.defaultcmd) // never do the default one directly
			return -1;
		ScriptVariable v = parameterExpressionToken(1);
		if (tok == Token.casecmd) {
			boolean isOK = ScriptVariable.areEqual(var, v);
			if (isOK)
				c.contextVariables.remove("_var");
			return isOK ? 1 : -1;
		}
		c.contextVariables.put("_var", v);
		return 1;
	}

	private boolean ifCmd() throws ScriptException {
		return parameterExpressionBoolean(1, 0);
	}

	private void returnCmd(ScriptVariable tv) throws ScriptException {
		ScriptVariable t = getContextVariableAsVariable("_retval");
		if (t == null) {
			if (!isSyntaxCheck)
				gotoCmd(null);
			return;
		}
		ScriptVariable v = (tv != null || statementLength == 1 ? null
				: parameterExpressionToken(1));
		if (isSyntaxCheck)
			return;
		if (tv == null)
			tv = (v == null ? new ScriptVariableInt(0) : v);
		t.value = tv.value;
		t.intValue = tv.intValue;
		t.tok = tv.tok;
		gotoCmd(null);
	}

	private void help() throws ScriptException {
		if (isSyntaxCheck)
			return;
		String what = optParameterAsString(1).toLowerCase();
		int pt = 0;
		if (what.startsWith("mouse") && (pt = what.indexOf(" ")) >= 0
				&& pt == what.lastIndexOf(" ")) {
			showString(viewer.getBindingInfo(what.substring(pt + 1)));
			return;
		}
		if (Token.tokAttr(Token.getTokFromName(what), Token.scriptCommand))
			what = "?command=" + what;
		viewer.getHelp(what);
	}

	private void move() throws ScriptException {
		if (statementLength > 11)
			error(ERROR_badArgumentCount);
		// rotx roty rotz zoom transx transy transz slab seconds fps
		Vector3f dRot = new Vector3f(floatParameter(1), floatParameter(2),
				floatParameter(3));
		float dZoom = floatParameter(4);
		Vector3f dTrans = new Vector3f(intParameter(5), intParameter(6),
				intParameter(7));
		float dSlab = floatParameter(8);
		float floatSecondsTotal = floatParameter(9);
		int fps = (statementLength == 11 ? intParameter(10) : 30);
		if (isSyntaxCheck)
			return;
		refresh();
		viewer.move(dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps);
	}

	private void moveto() throws ScriptException {
		// moveto time
		// moveto [time] { x y z deg} zoom xTrans yTrans (rotCenter)
		// rotationRadius
		// (navCenter) xNav yNav navDepth
		// moveto [time] { x y z deg} 0 xTrans yTrans (rotCenter) [zoom factor]
		// (navCenter) xNav yNav navDepth
		// moveto [time] { x y z deg} (rotCenter) [zoom factor] (navCenter) xNav
		// yNav navDepth
		// where [zoom factor] is [0|n|+n|-n|*n|/n|IN|OUT]
		// moveto [time] front|back|left|right|top|bottom
		if (statementLength == 2 && tokAt(1) == Token.stop) {
			if (!isSyntaxCheck)
				viewer.stopMotion();
			return;
		}
		if (statementLength == 2 && isFloatParameter(1)) {
			float f = floatParameter(1);
			if (isSyntaxCheck)
				return;
			if (f > 0)
				refresh();
			viewer.moveTo(f, null, JmolConstants.axisZ, 0, null, 100, 0, 0, 0,
					null, Float.NaN, Float.NaN, Float.NaN);
			return;
		}
		Vector3f axis = new Vector3f(Float.NaN, 0, 0);
		Point3f center = null;
		int i = 1;
		float floatSecondsTotal = (isFloatParameter(i) ? floatParameter(i++)
				: 2.0f);
		float degrees = 90;
		BitSet bsCenter = null;
		switch (getToken(i).tok) {
		case Token.quaternion:
			Quaternion q;
			boolean isMolecular = false;
			if (tokAt(++i) == Token.molecular) {
				// see comment below
				isMolecular = true;
				i++;
			}
			if (tokAt(i) == Token.bitset || tokAt(i) == Token.expressionBegin) {
				isMolecular = true;
				center = centerParameter(i);
				if (!(expressionResult instanceof BitSet))
					error(ERROR_invalidArgument);
				bsCenter = (BitSet) expressionResult;
				q = (isSyntaxCheck ? new Quaternion() : viewer
						.getAtomQuaternion(bsCenter.nextSetBit(0)));
			} else {
				q = getQuaternionParameter(i);
			}
			i = iToken + 1;
			if (q == null)
				error(ERROR_invalidArgument);
			AxisAngle4f aa = q.toAxisAngle4f();
			axis.set(aa.x, aa.y, aa.z);
			/*
			 * The quaternion angle for an atom represents the angle by which
			 * the reference frame must be rotated to match the frame defined
			 * for the residue.
			 * 
			 * However, to "moveTo" this frame as the REFERENCE frame, what we
			 * have to do is take that quaternion frame and rotate it BACKWARD
			 * by that many degrees. Then it will match the reference frame,
			 * which is ultimately our window frame.
			 * 
			 * We only apply this for molecular-type quaternions, because in
			 * general the orientation quaternion refers to how the reference
			 * plane has been changed (the orientation matrix)
			 */
			degrees = (isMolecular ? -1 : 1)
					* (float) (aa.angle * 180.0 / Math.PI);
			break;
		case Token.point4f:
		case Token.point3f:
		case Token.leftbrace:
			// {X, Y, Z} deg or {x y z deg}
			if (isPoint3f(i)) {
				axis.set(getPoint3f(i, true));
				i = iToken + 1;
				degrees = floatParameter(i++);
			} else {
				Point4f pt4 = getPoint4f(i);
				i = iToken + 1;
				axis.set(pt4.x, pt4.y, pt4.z);
				degrees = (pt4.x == 0 && pt4.y == 0 && pt4.z == 0 ? Float.NaN
						: pt4.w);
			}
			break;
		case Token.front:
			axis.set(1, 0, 0);
			degrees = 0f;
			checkLength(++i);
			break;
		case Token.back:
			axis.set(0, 1, 0);
			degrees = 180f;
			checkLength(++i);
			break;
		case Token.left:
			axis.set(0, 1, 0);
			checkLength(++i);
			break;
		case Token.right:
			axis.set(0, -1, 0);
			checkLength(++i);
			break;
		case Token.top:
			axis.set(1, 0, 0);
			checkLength(++i);
			break;
		case Token.bottom:
			axis.set(-1, 0, 0);
			checkLength(++i);
			break;
		default:
			// X Y Z deg
			axis = new Vector3f(floatParameter(i++), floatParameter(i++),
					floatParameter(i++));
			degrees = floatParameter(i++);
		}
		if (Float.isNaN(axis.x) || Float.isNaN(axis.y) || Float.isNaN(axis.z))
			axis.set(0, 0, 0);
		else if (axis.length() == 0 && degrees == 0)
			degrees = Float.NaN;
		boolean isChange = !viewer.isInPosition(axis, degrees);
		// optional zoom
		float zoom = (isFloatParameter(i) ? floatParameter(i++) : Float.NaN);
		// optional xTrans yTrans
		float xTrans = 0;
		float yTrans = 0;
		if (isFloatParameter(i) && !isCenterParameter(i)) {
			xTrans = floatParameter(i++);
			yTrans = floatParameter(i++);
			if (!isChange
					&& Math.abs(xTrans - viewer.getTranslationXPercent()) >= 1)
				isChange = true;
			if (!isChange
					&& Math.abs(yTrans - viewer.getTranslationYPercent()) >= 1)
				isChange = true;
		}
		if (bsCenter == null && i != statementLength) {
			// if any more, required (center)
			center = centerParameter(i);
			if (expressionResult instanceof BitSet)
				bsCenter = (BitSet) expressionResult;
			i = iToken + 1;
		}
		float rotationRadius = Float.NaN;
		float zoom0 = viewer.getZoomSetting();
		if (center != null) {
			if (!isChange && center.distance(viewer.getRotationCenter()) >= 0.1)
				isChange = true;
			// optional {center} rotationRadius
			if (isFloatParameter(i))
				rotationRadius = floatParameter(i++);
			if (!isCenterParameter(i)) {
				if ((rotationRadius == 0 || Float.isNaN(rotationRadius))
						&& (zoom == 0 || Float.isNaN(zoom))) {
					// alternative (atom expression) 0 zoomFactor
					float newZoom = Math.abs(getZoom(0, i, bsCenter,
							(zoom == 0 ? 0 : zoom0)));
					i = iToken + 1;
					zoom = newZoom;
				} else {
					if (!isChange
							&& Math.abs(rotationRadius
									- viewer.getRotationRadius()) >= 0.1)
						isChange = true;
				}
			}
		}
		if (zoom == 0 || Float.isNaN(zoom))
			zoom = 100;
		if (Float.isNaN(rotationRadius))
			rotationRadius = 0;

		if (!isChange && Math.abs(zoom - zoom0) >= 1)
			isChange = true;
		// (navCenter) xNav yNav navDepth

		Point3f navCenter = null;
		float xNav = Float.NaN;
		float yNav = Float.NaN;
		float navDepth = Float.NaN;

		if (i != statementLength) {
			navCenter = centerParameter(i);
			i = iToken + 1;
			if (i != statementLength) {
				xNav = floatParameter(i++);
				yNav = floatParameter(i++);
			}
			if (i != statementLength)
				navDepth = floatParameter(i++);
		}

		if (i != statementLength)
			error(ERROR_badArgumentCount);

		if (isSyntaxCheck)
			return;
		if (!isChange)
			floatSecondsTotal = 0;
		if (floatSecondsTotal > 0)
			refresh();
		viewer.moveTo(floatSecondsTotal, center, axis, degrees, null, zoom,
				xTrans, yTrans, rotationRadius, navCenter, xNav, yNav, navDepth);
	}

	private void navigate() throws ScriptException {
		/*
		 * navigation on/off navigation depth p # would be as a depth value,
		 * like slab, in percent, but could be negative navigation nSec
		 * translate X Y # could be percentages navigation nSec translate
		 * $object # could be a draw object navigation nSec translate (atom
		 * selection) #average of values navigation nSec center {x y z}
		 * navigation nSec center $object navigation nSec center (atom
		 * selection) navigation nSec path $object navigation nSec path {x y z
		 * theta} {x y z theta}{x y z theta}{x y z theta}... navigation nSec
		 * trace (atom selection)
		 */
		if (statementLength == 1) {
			setBooleanProperty("navigationMode", true);
			return;
		}
		Vector3f rotAxis = new Vector3f(0, 1, 0);
		Point3f pt;
		if (statementLength == 2) {
			switch (getToken(1).tok) {
			case Token.on:
			case Token.off:
				if (isSyntaxCheck)
					return;
				setObjectMad(JmolConstants.SHAPE_AXES, "axes", 1);
				setShapeProperty(JmolConstants.SHAPE_AXES, "position",
						new Point3f(50, 50, Float.MAX_VALUE));
				setBooleanProperty("navigationMode", true);
				viewer.setNavOn(theTok == Token.on);
				return;
			case Token.stop:
				if (!isSyntaxCheck)
					viewer.setNavXYZ(0, 0, 0);
				return;
			case Token.point3f:
				break;
			default:
				error(ERROR_invalidArgument);
			}
		}
		if (!viewer.getNavigationMode())
			setBooleanProperty("navigationMode", true);
		for (int i = 1; i < statementLength; i++) {
			float timeSec = (isFloatParameter(i) ? floatParameter(i++) : 2f);
			if (timeSec < 0)
				error(ERROR_invalidArgument);
			if (!isSyntaxCheck && timeSec > 0)
				refresh();
			switch (getToken(i).tok) {
			case Token.point3f:
			case Token.leftbrace:
				// navigate {x y z}
				pt = getPoint3f(i, true);
				iToken++;
				if (iToken != statementLength)
					error(ERROR_invalidArgument);
				if (isSyntaxCheck)
					return;
				viewer.setNavXYZ(pt.x, pt.y, pt.z);
				return;
			case Token.depth:
				float depth = floatParameter(++i);
				if (!isSyntaxCheck)
					viewer.setNavigationDepthPercent(timeSec, depth);
				continue;
			case Token.center:
				pt = centerParameter(++i);
				i = iToken;
				if (!isSyntaxCheck)
					viewer.navigate(timeSec, pt);
				continue;
			case Token.rotate:
				switch (getToken(++i).tok) {
				case Token.x:
					rotAxis.set(1, 0, 0);
					i++;
					break;
				case Token.y:
					rotAxis.set(0, 1, 0);
					i++;
					break;
				case Token.z:
					rotAxis.set(0, 0, 1);
					i++;
					break;
				case Token.point3f:
				case Token.leftbrace:
					rotAxis.set(getPoint3f(i, true));
					i = iToken + 1;
					break;
				case Token.identifier:
					error(ERROR_invalidArgument); // for now
					break;
				}
				float degrees = floatParameter(i);
				if (!isSyntaxCheck)
					viewer.navigate(timeSec, rotAxis, degrees);
				continue;
			case Token.translate:
				float x = Float.NaN;
				float y = Float.NaN;
				if (isFloatParameter(++i)) {
					x = floatParameter(i);
					y = floatParameter(++i);
				} else {
					switch (tokAt(i)) {
					case Token.x:
						x = floatParameter(++i);
						break;
					case Token.y:
						y = floatParameter(++i);
						break;
					default:
						pt = centerParameter(i);
						i = iToken;
						if (!isSyntaxCheck)
							viewer.navTranslate(timeSec, pt);
						continue;
					}
				}
				if (!isSyntaxCheck)
					viewer.navTranslatePercent(timeSec, x, y);
				continue;
			case Token.divide:
				continue;
			case Token.trace:
				Point3f[][] pathGuide;
				List<Point3f[]> vp = new ArrayList<Point3f[]>();
				BitSet bs = atomExpressionAt(++i);
				i = iToken;
				if (isSyntaxCheck)
					return;
				viewer.getPolymerPointsAndVectors(bs, vp);
				int n;
				if ((n = vp.size()) > 0) {
					pathGuide = new Point3f[n][];
					for (int j = 0; j < n; j++) {
						pathGuide[j] = vp.get(j);
					}
					viewer.navigate(timeSec, pathGuide);
					continue;
				}
				break;
			case Token.surface:
				if (i != 1)
					error(ERROR_invalidArgument);
				if (isSyntaxCheck)
					return;
				viewer.navigateSurface(timeSec, optParameterAsString(2));
				continue;
			case Token.path:
				Point3f[] path;
				float[] theta = null; // orientation; null for now
				if (getToken(i + 1).tok == Token.dollarsign) {
					i++;
					// navigate timeSeconds path $id indexStart indexEnd
					String pathID = objectNameParameter(++i);
					if (isSyntaxCheck)
						return;
					setShapeProperty(JmolConstants.SHAPE_DRAW, "thisID", pathID);
					path = (Point3f[]) getShapeProperty(
							JmolConstants.SHAPE_DRAW, "vertices");
					refresh();
					if (path == null)
						error(ERROR_invalidArgument);
					int indexStart = (int) (isFloatParameter(i + 1) ? floatParameter(++i)
							: 0);
					int indexEnd = (int) (isFloatParameter(i + 1) ? floatParameter(++i)
							: Integer.MAX_VALUE);
					if (!isSyntaxCheck)
						viewer.navigate(timeSec, path, theta, indexStart,
								indexEnd);
					continue;
				}
				List<Point3f> v = new ArrayList<Point3f>();
				while (isCenterParameter(i + 1)) {
					v.add(centerParameter(++i));
					i = iToken;
				}
				if (v.size() > 0) {
					path = v.toArray(new Point3f[v.size()]);
					if (!isSyntaxCheck)
						viewer.navigate(timeSec, path, theta, 0,
								Integer.MAX_VALUE);
					continue;
				}
				//$FALL-THROUGH$
			default:
				error(ERROR_invalidArgument);
			}
		}
	}

	private void bondorder() throws ScriptException {
		checkLength(-3);
		int order = 0;
		switch (getToken(1).tok) {
		case Token.integer:
		case Token.decimal:
			if ((order = JmolEdge.getBondOrderFromFloat(floatParameter(1))) == JmolEdge.BOND_ORDER_NULL)
				error(ERROR_invalidArgument);
			break;
		default:
			if ((order = getBondOrderFromString(parameterAsString(1))) == JmolEdge.BOND_ORDER_NULL)
				error(ERROR_invalidArgument);
			// generic partial can be indicated by "partial n.m"
			if (order == JmolEdge.BOND_PARTIAL01 && tokAt(2) == Token.decimal) {
				order = getPartialBondOrderFromFloatEncodedInt(statement[2].intValue);
			}
		}
		setShapeProperty(JmolConstants.SHAPE_STICKS, "bondOrder",
				Integer.valueOf(order));
	}

	private void console() throws ScriptException {
		switch (getToken(1).tok) {
		case Token.off:
			if (!isSyntaxCheck)
				viewer.showConsole(false);
			break;
		case Token.on:
			if (isSyntaxCheck)
				break;
			viewer.showConsole(true);
			break;
		default:
			error(ERROR_invalidArgument);
		}
	}

	private void centerAt() throws ScriptException {
		String relativeTo = null;
		switch (getToken(1).tok) {
		case Token.absolute:
			relativeTo = "absolute";
			break;
		case Token.average:
			relativeTo = "average";
			break;
		case Token.boundbox:
			relativeTo = "boundbox";
			break;
		default:
			error(ERROR_invalidArgument);
		}
		Point3f pt = new Point3f(0, 0, 0);
		if (statementLength == 5) {
			// centerAt xxx x y z
			pt.x = floatParameter(2);
			pt.y = floatParameter(3);
			pt.z = floatParameter(4);
		} else if (isCenterParameter(2)) {
			pt = centerParameter(2);
			checkLast(iToken);
		} else {
			checkLength(2);
		}
		if (!isSyntaxCheck)
			viewer.setCenterAt(relativeTo, pt);
	}

	private void stereo() throws ScriptException {
		EnumStereoMode stereoMode = EnumStereoMode.DOUBLE;
		// see www.usm.maine.edu/~rhodes/0Help/StereoViewing.html
		// stereo on/off
		// stereo color1 color2 6
		// stereo redgreen 5

		float degrees = EnumStereoMode.DEFAULT_STEREO_DEGREES;
		boolean degreesSeen = false;
		int[] colors = null;
		int colorpt = 0;
		for (int i = 1; i < statementLength; ++i) {
			if (isColorParam(i)) {
				if (colorpt > 1)
					error(ERROR_badArgumentCount);
				if (colorpt == 0)
					colors = new int[2];
				if (!degreesSeen)
					degrees = 3;
				colors[colorpt] = getArgbParam(i);
				if (colorpt++ == 0)
					colors[1] = ~colors[0];
				i = iToken;
				continue;
			}
			switch (getToken(i).tok) {
			case Token.on:
				checkLast(iToken = 1);
				iToken = 1;
				break;
			case Token.off:
				checkLast(iToken = 1);
				stereoMode = EnumStereoMode.NONE;
				break;
			case Token.integer:
			case Token.decimal:
				degrees = floatParameter(i);
				degreesSeen = true;
				break;
			case Token.identifier:
				if (!degreesSeen)
					degrees = 3;
				stereoMode = EnumStereoMode.getStereoMode(parameterAsString(i));
				if (stereoMode != null)
					break;
				//$FALL-THROUGH$
			default:
				error(ERROR_invalidArgument);
			}
		}
		if (isSyntaxCheck)
			return;
		viewer.setStereoMode(colors, stereoMode, degrees);
	}

	private void compare() throws ScriptException {
		// compare {model1} {model2} [atoms] {bsAtoms1} {bsAtoms2}
		// compare {model1} {model2} orientations
		// compare {model1} {model2} orientations {bsAtoms1} {bsAtoms2}
		// compare {model1} {model2} [orientations] [quaternionList1]
		// [quaternionList2]
		// compare {model1} {model2} SMILES "....."
		// compare {model1} {model2} SMARTS "....."
		// compare {model1} {model2} FRAMES

		boolean isQuaternion = false;
		boolean doRotate = false;
		boolean doTranslate = false;
		boolean doAnimate = false;
		float nSeconds = Float.NaN;
		Quaternion[] data1 = null, data2 = null;
		BitSet bsAtoms1 = null, bsAtoms2 = null;
		List<BitSet[]> vAtomSets = null;
		List<Object[]> vQuatSets = null;
		BitSet bsFrom = (tokAt(1) == Token.subset ? null : atomExpressionAt(1));
		BitSet bsTo = (tokAt(++iToken) == Token.subset ? null
				: atomExpressionAt(iToken));
		if (bsFrom == null || bsTo == null)
			error(ERROR_invalidArgument);
		BitSet bsSubset = null;
		boolean isSmiles = false;
		String strSmiles = null;
		BitSet bs = BitSetUtil.copy(bsFrom);
		bs.or(bsTo);
		boolean isToSubsetOfFrom = bs.equals(bsFrom);
		boolean isFrames = isToSubsetOfFrom;
		for (int i = iToken + 1; i < statementLength; ++i) {
			switch (getToken(i).tok) {
			case Token.frame:
				isFrames = true;
				break;
			case Token.smiles:
				isSmiles = true;
				//$FALL-THROUGH$
			case Token.search:
				strSmiles = stringParameter(++i);
				break;
			case Token.decimal:
			case Token.integer:
				nSeconds = Math.abs(floatParameter(i));
				if (nSeconds > 0)
					doAnimate = true;
				break;
			case Token.comma:
				break;
			case Token.subset:
				bsSubset = atomExpressionAt(++i);
				i = iToken;
				break;
			case Token.bitset:
			case Token.expressionBegin:
				if (vQuatSets != null)
					error(ERROR_invalidArgument);
				bsAtoms1 = atomExpressionAt(iToken);
				int tok = (isToSubsetOfFrom ? 0 : tokAt(iToken + 1));
				bsAtoms2 = (tok == Token.bitset || tok == Token.expressionBegin ? atomExpressionAt(++iToken)
						: BitSetUtil.copy(bsAtoms1));
				if (bsSubset != null) {
					bsAtoms1.and(bsSubset);
					bsAtoms2.and(bsSubset);
				}
				bsAtoms2.and(bsTo);
				if (vAtomSets == null)
					vAtomSets = new ArrayList<BitSet[]>();
				vAtomSets.add(new BitSet[] { bsAtoms1, bsAtoms2 });
				i = iToken;
				break;
			case Token.varray:
				if (vAtomSets != null)
					error(ERROR_invalidArgument);
				isQuaternion = true;
				data1 = ScriptMathProcessor
						.getQuaternionArray(((ScriptVariable) theToken)
								.getList());
				getToken(++i);
				data2 = ScriptMathProcessor
						.getQuaternionArray(((ScriptVariable) theToken)
								.getList());
				if (vQuatSets == null)
					vQuatSets = new ArrayList<Object[]>();
				vQuatSets.add(new Object[] { data1, data2 });
				break;
			case Token.orientation:
				isQuaternion = true;
				break;
			case Token.point:
			case Token.atoms:
				isQuaternion = false;
				break;
			case Token.rotate:
				doRotate = true;
				break;
			case Token.translate:
				doTranslate = true;
				break;
			default:
				error(ERROR_invalidArgument);
			}
		}
		if (isSyntaxCheck)
			return;

		if (isFrames)
			nSeconds = 0;
		if (Float.isNaN(nSeconds) || nSeconds < 0)
			nSeconds = 1;
		else if (!doRotate && !doTranslate)
			doRotate = doTranslate = true;
		doAnimate = (nSeconds != 0);

		boolean isAtoms = (!isQuaternion && strSmiles == null);
		if (vAtomSets == null && vQuatSets == null) {
			if (bsSubset == null) {
				bsAtoms1 = (isAtoms ? viewer.getAtomBitSet("spine")
						: new BitSet());
				if (bsAtoms1.nextSetBit(0) < 0) {
					bsAtoms1 = bsFrom;
					bsAtoms2 = bsTo;
				} else {
					bsAtoms2 = BitSetUtil.copy(bsAtoms1);
					bsAtoms1.and(bsFrom);
					bsAtoms2.and(bsTo);
				}
			} else {
				bsAtoms1 = BitSetUtil.copy(bsFrom);
				bsAtoms2 = BitSetUtil.copy(bsTo);
				bsAtoms1.and(bsSubset);
				bsAtoms2.and(bsSubset);
				bsAtoms1.and(bsFrom);
				bsAtoms2.and(bsTo);
			}
			vAtomSets = new ArrayList<BitSet[]>();
			vAtomSets.add(new BitSet[] { bsAtoms1, bsAtoms2 });
		}

		BitSet[] bsFrames;
		if (isFrames) {
			BitSet bsModels = viewer.getModelBitSet(bsFrom, false);
			bsFrames = new BitSet[bsModels.cardinality()];
			for (int i = 0, iModel = bsModels.nextSetBit(0); iModel >= 0; iModel = bsModels
					.nextSetBit(iModel + 1), i++)
				bsFrames[i] = viewer.getModelUndeletedAtomsBitSet(iModel);
		} else {
			bsFrames = new BitSet[] { bsFrom };
		}
		for (int iFrame = 0; iFrame < bsFrames.length; iFrame++) {
			bsFrom = bsFrames[iFrame];
			float[] retStddev = new float[2]; // [0] final, [1] initial for
												// atoms
			Quaternion q = null;
			List<Quaternion> vQ = new ArrayList<Quaternion>();
			Point3f[][] centerAndPoints = null;
			List<BitSet[]> vAtomSets2 = (isFrames ? new ArrayList<BitSet[]>()
					: vAtomSets);
			for (int i = 0; i < vAtomSets.size(); ++i) {
				BitSet[] bss = vAtomSets.get(i);
				if (isFrames)
					vAtomSets2.add(bss = new BitSet[] {
							BitSetUtil.copy(bss[0]), bss[1] });
				bss[0].and(bsFrom);
			}
			if (isAtoms) {
				centerAndPoints = viewer.getCenterAndPoints(vAtomSets2, true);
				q = Measure.calculateQuaternionRotation(centerAndPoints,
						retStddev, true);
				float r0 = (Float.isNaN(retStddev[1]) ? Float.NaN
						: (int) (retStddev[0] * 100) / 100f);
				float r1 = (Float.isNaN(retStddev[1]) ? Float.NaN
						: (int) (retStddev[1] * 100) / 100f);
				showString("RMSD " + r0 + " --> " + r1 + " Angstroms");
			} else if (isQuaternion) {
				if (vQuatSets == null) {
					for (int i = 0; i < vAtomSets2.size(); i++) {
						BitSet[] bss = vAtomSets2.get(i);
						data1 = viewer.getAtomGroupQuaternions(bss[0],
								Integer.MAX_VALUE);
						data2 = viewer.getAtomGroupQuaternions(bss[1],
								Integer.MAX_VALUE);
						for (int j = 0; j < data1.length && j < data2.length; j++) {
							vQ.add(data2[j].div(data1[j]));
						}
					}
				} else {
					for (int j = 0; j < data1.length && j < data2.length; j++) {
						vQ.add(data2[j].div(data1[j]));
					}
				}
				retStddev[0] = 0;
				data1 = vQ.toArray(new Quaternion[vQ.size()]);
				q = Quaternion.sphereMean(data1, retStddev, 0.0001f);
				showString("RMSD = " + retStddev[0] + " degrees");
			} else {
				// SMILES
				/*
				 * not sure why this was like this: if (vAtomSets == null) {
				 * vAtomSets = new ArrayList<BitSet[]>(); } bsAtoms1 =
				 * BitSetUtil.copy(bsFrom); bsAtoms2 = BitSetUtil.copy(bsTo);
				 * vAtomSets.add(new BitSet[] { bsAtoms1, bsAtoms2 });
				 */

				Matrix4f m4 = new Matrix4f();
				float stddev = getSmilesCorrelation(bsFrom, bsTo, strSmiles,
						null, null, m4, null, !isSmiles, false);
				if (Float.isNaN(stddev))
					error(ERROR_invalidArgument);
				Vector3f translation = new Vector3f();
				m4.get(translation);
				Matrix3f m3 = new Matrix3f();
				m4.get(m3);
				q = new Quaternion(m3);
			}
			if (centerAndPoints == null)
				centerAndPoints = viewer.getCenterAndPoints(vAtomSets2, true);
			Point3f pt1 = new Point3f();
			float endDegrees = Float.NaN;
			Vector3f translation = null;
			if (doTranslate) {
				translation = new Vector3f(centerAndPoints[1][0]);
				translation.sub(centerAndPoints[0][0]);
				endDegrees = 0;
			}
			if (doRotate) {
				if (q == null)
					evalError("option not implemented", null);
				pt1.set(centerAndPoints[0][0]);
				pt1.add(q.getNormal());
				endDegrees = q.getTheta();
			}
			if (Float.isNaN(endDegrees) || Float.isNaN(pt1.x))
				continue;
			List<Point3f> ptsB = null;
			if (doRotate && doTranslate && nSeconds != 0) {
				List<Point3f> ptsA = viewer.getAtomPointVector(bsFrom);
				Matrix4f m4 = ScriptMathProcessor.getMatrix4f(q.getMatrix(),
						translation);
				ptsB = Measure.transformPoints(ptsA, m4, centerAndPoints[0][0]);
			}
			viewer.rotateAboutPointsInternal(centerAndPoints[0][0], pt1,
					endDegrees / nSeconds, endDegrees, doAnimate, bsFrom,
					translation, ptsB);
		}
	}

	float getSmilesCorrelation(BitSet bsA, BitSet bsB, String smiles,
			List<Point3f> ptsA, List<Point3f> ptsB, Matrix4f m,
			List<BitSet> vReturn, boolean isSmarts, boolean asMap)
			throws ScriptException {
		float tolerance = 0.1f; // TODO
		try {
			if (ptsA == null) {
				ptsA = new ArrayList<Point3f>();
				ptsB = new ArrayList<Point3f>();
			}
			if (m == null)
				m = new Matrix4f();

			Atom[] atoms = viewer.getModelSet().atoms;
			int atomCount = viewer.getAtomCount();
			int[][] maps = viewer.getSmilesMatcher().getCorrelationMaps(smiles,
					atoms, atomCount, bsA, isSmarts, true);
			if (maps == null)
				evalError(viewer.getSmilesMatcher().getLastException(), null);
			if (maps.length == 0)
				return Float.NaN;
			for (int i = 0; i < maps[0].length; i++)
				ptsA.add(atoms[maps[0][i]]);
			maps = viewer.getSmilesMatcher().getCorrelationMaps(smiles, atoms,
					atomCount, bsB, isSmarts, false);
			if (maps == null)
				evalError(viewer.getSmilesMatcher().getLastException(), null);
			if (maps.length == 0)
				return Float.NaN;
			if (asMap) {
				for (int i = 0; i < maps.length; i++)
					for (int j = 0; j < maps[i].length; j++)
						ptsB.add(atoms[maps[i][j]]);
				return 0;
			}
			float lowestStdDev = Float.MAX_VALUE;
			int[] mapB = null;
			for (int i = 0; i < maps.length; i++) {
				ptsB.clear();
				for (int j = 0; j < maps[i].length; j++)
					ptsB.add(atoms[maps[i][j]]);
				float stddev = Measure.getTransformMatrix4(ptsA, ptsB, m, null);
				Logger.info("getSmilesCorrelation stddev=" + stddev);
				if (vReturn != null) {
					if (stddev < tolerance) {
						BitSet bs = new BitSet();
						for (int j = 0; j < maps[i].length; j++)
							bs.set(maps[i][j]);
						vReturn.add(bs);
					}
				}
				if (stddev < lowestStdDev) {
					mapB = maps[i];
					lowestStdDev = stddev;
				}
			}
			for (int i = 0; i < mapB.length; i++)
				ptsB.add(atoms[mapB[i]]);
			return lowestStdDev;
		} catch (Exception e) {
			// e.printStackTrace();
			evalError(e.getMessage(), null);
			return 0; // unattainable
		}
	}

	Object getSmilesMatches(String pattern, String smiles, BitSet bsSelected,
			BitSet bsMatch3D, boolean isSmarts, boolean asOneBitset)
			throws ScriptException {
		if (isSyntaxCheck) {
			if (asOneBitset)
				return new BitSet();
			return new String[] { "({})" };
		}

		// just retrieving the SMILES or bioSMILES string

		if (pattern.length() == 0) {
			boolean isBioSmiles = (!asOneBitset);
			Object ret = viewer.getSmiles(0, 0, bsSelected, isBioSmiles, false,
					true, true);
			if (ret == null)
				evalError(viewer.getSmilesMatcher().getLastException(), null);
			return ret;
		}

		boolean asAtoms = true;
		BitSet[] b;
		if (bsMatch3D == null) {

			// getting a BitSet or BitSet[] from a set of atoms or a pattern.

			asAtoms = (smiles == null);
			if (asAtoms)
				b = viewer.getSmilesMatcher().getSubstructureSetArray(pattern,
						viewer.getModelSet().atoms, viewer.getAtomCount(),
						bsSelected, null, isSmarts, false);
			else
				b = viewer.getSmilesMatcher().find(pattern, smiles, isSmarts,
						false);

			if (b == null) {
				showString(viewer.getSmilesMatcher().getLastException(), false);
				if (!asAtoms && !isSmarts)
					return Integer.valueOf(-1);
				return "?";
			}
		} else {

			// getting a correlation

			List<BitSet> vReturn = new ArrayList<BitSet>();
			float stddev = getSmilesCorrelation(bsMatch3D, bsSelected, pattern,
					null, null, null, vReturn, isSmarts, false);
			if (Float.isNaN(stddev)) {
				if (asOneBitset)
					return new BitSet();
				return new String[] {};
			}
			showString("RMSD " + stddev + " Angstroms");
			b = vReturn.toArray(new BitSet[vReturn.size()]);
		}
		if (asOneBitset) {
			// sum total of all now, not just first
			BitSet bs = new BitSet();
			for (int j = 0; j < b.length; j++)
				bs.or(b[j]);
			if (asAtoms)
				return bs;
			if (!isSmarts)
				return Integer.valueOf(bs.cardinality());
			int[] iarray = new int[bs.cardinality()];
			int pt = 0;
			for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
				iarray[pt++] = i + 1;
			return iarray;
		}
		String[] matches = new String[b.length];
		for (int j = 0; j < b.length; j++)
			matches[j] = Escape.escapeBs(b[j], asAtoms);
		return matches;
	}

	private void connect(int index) throws ScriptException {

		final float[] distances = new float[2];
		BitSet[] atomSets = new BitSet[2];
		atomSets[0] = atomSets[1] = viewer.getSelectionSet(false);
		float radius = Float.NaN;
		int color = Integer.MIN_VALUE;
		int distanceCount = 0;
		int bondOrder = JmolEdge.BOND_ORDER_NULL;
		int bo;
		int operation = Token.modifyorcreate;
		boolean isDelete = false;
		boolean haveType = false;
		boolean haveOperation = false;
		String translucency = null;
		float translucentLevel = Float.MAX_VALUE;
		boolean isColorOrRadius = false;
		int nAtomSets = 0;
		int nDistances = 0;
		BitSet bsBonds = new BitSet();
		boolean isBonds = false;
		int expression2 = 0;
		int ptColor = 0;
		float energy = 0;
		boolean addGroup = false;
		/*
		 * connect [<=2 distance parameters] [<=2 atom sets] [<=1 bond type]
		 * [<=1 operation]
		 */

		if (statementLength == 1) {
			if (!isSyntaxCheck)
				viewer.rebond(isStateScript);
			return;
		}

		for (int i = index; i < statementLength; ++i) {
			switch (getToken(i).tok) {
			case Token.on:
			case Token.off:
				checkLength(2);
				if (!isSyntaxCheck)
					viewer.rebond(isStateScript);
				return;
			case Token.integer:
			case Token.decimal:
				if (nAtomSets > 0) {
					if (haveType || isColorOrRadius)
						error(ERROR_invalidParameterOrder);
					bo = JmolEdge.getBondOrderFromFloat(floatParameter(i));
					if (bo == JmolEdge.BOND_ORDER_NULL)
						error(ERROR_invalidArgument);
					bondOrder = bo;
					haveType = true;
					break;
				}
				if (++nDistances > 2)
					error(ERROR_badArgumentCount);
				float dist = floatParameter(i);
				if (tokAt(i + 1) == Token.percent) {
					dist = -dist / 100f;
					i++;
				}
				distances[distanceCount++] = dist;
				break;
			case Token.bitset:
			case Token.expressionBegin:
				if (nAtomSets > 2 || isBonds && nAtomSets > 0)
					error(ERROR_badArgumentCount);
				if (haveType || isColorOrRadius)
					error(ERROR_invalidParameterOrder);
				atomSets[nAtomSets++] = atomExpressionAt(i);
				isBonds = isBondSet;
				if (nAtomSets == 2) {
					int pt = iToken;
					for (int j = i; j < pt; j++)
						if (tokAt(j) == Token.identifier
								&& parameterAsString(j).equals("_1")) {
							expression2 = i;
							break;
						}
					iToken = pt;
				}
				i = iToken;
				break;
			case Token.group:
				addGroup = true;
				break;
			case Token.color:
				int tok = tokAt(i + 1);
				if (tok != Token.translucent && tok != Token.opaque)
					ptColor = i + 1;
				continue;
			case Token.translucent:
			case Token.opaque:
				if (translucency != null)
					error(ERROR_invalidArgument);
				isColorOrRadius = true;
				translucency = parameterAsString(i);
				if (theTok == Token.translucent && isFloatParameter(i + 1))
					translucentLevel = getTranslucentLevel(++i);
				ptColor = i + 1;
				break;
			case Token.pdb:
				boolean isAuto = (tokAt(2) == Token.auto);
				checkLength(isAuto ? 3 : 2);
				if (!isSyntaxCheck)
					viewer.setPdbConectBonding(isAuto, isStateScript);
				return;
			case Token.adjust:
			case Token.auto:
			case Token.create:
			case Token.modify:
			case Token.modifyorcreate:
				// must be an operation and must be last argument
				haveOperation = true;
				if (++i != statementLength)
					error(ERROR_invalidParameterOrder);
				operation = theTok;
				if (theTok == Token.auto
						&& !(bondOrder == JmolEdge.BOND_ORDER_NULL
								|| bondOrder == JmolEdge.BOND_H_REGULAR || bondOrder == JmolEdge.BOND_AROMATIC))
					error(ERROR_invalidArgument);
				break;
			case Token.struts:
				if (!isColorOrRadius) {
					color = 0xFFFFFF;
					translucency = "translucent";
					translucentLevel = 0.5f;
					radius = viewer.getStrutDefaultRadius();
					isColorOrRadius = true;
				}
				if (!haveOperation)
					operation = Token.modifyorcreate;
				haveOperation = true;
				//$FALL-THROUGH$
			case Token.identifier:
			case Token.aromatic:
			case Token.hbond:
				if (i > 0) {
					if (ptColor == i)
						break;
					// I know -- should have required the COLOR keyword
					if (isColorParam(i)) {
						ptColor = -i;
						break;
					}
				}
				String cmd = parameterAsString(i);
				if ((bo = getBondOrderFromString(cmd)) == JmolEdge.BOND_ORDER_NULL) {
					error(ERROR_invalidArgument);
				}
				// must be bond type
				if (haveType)
					error(ERROR_incompatibleArguments);
				haveType = true;
				switch (bo) {
				case JmolEdge.BOND_PARTIAL01:
					switch (tokAt(i + 1)) {
					case Token.decimal:
						bo = getPartialBondOrderFromFloatEncodedInt(statement[++i].intValue);
						break;
					case Token.integer:
						bo = (short) intParameter(++i);
						break;
					}
					break;
				case JmolEdge.BOND_H_REGULAR:
					if (tokAt(i + 1) == Token.integer) {
						bo = (short) (intParameter(++i) << JmolEdge.BOND_HBOND_SHIFT);
						energy = floatParameter(++i);
					}
					break;
				}
				bondOrder = bo;
				break;
			case Token.radius:
				radius = floatParameter(++i);
				isColorOrRadius = true;
				break;
			case Token.none:
			case Token.delete:
				if (++i != statementLength)
					error(ERROR_invalidParameterOrder);
				operation = Token.delete;
				// if (isColorOrRadius) / for struts automatic color
				// error(ERROR_invalidArgument);
				isDelete = true;
				isColorOrRadius = false;
				break;
			default:
				ptColor = i;
				break;
			}
			// now check for color -- -i means we've already checked
			if (i > 0) {
				if (ptColor == -i || ptColor == i && isColorParam(i)) {
					color = getArgbParam(i);
					i = iToken;
					isColorOrRadius = true;
				} else if (ptColor == i) {
					error(ERROR_invalidArgument);
				}
			}
		}
		if (isSyntaxCheck)
			return;
		if (distanceCount < 2) {
			if (distanceCount == 0)
				distances[0] = JmolConstants.DEFAULT_MAX_CONNECT_DISTANCE;
			distances[1] = distances[0];
			distances[0] = JmolConstants.DEFAULT_MIN_CONNECT_DISTANCE;
		}
		if (translucency != null || !Float.isNaN(radius)
				|| color != Integer.MIN_VALUE) {
			if (!haveType)
				bondOrder = JmolEdge.BOND_ORDER_ANY;
			if (!haveOperation)
				operation = Token.modify;
		}
		int nNew = 0;
		int nModified = 0;
		int[] result;
		if (expression2 > 0) {
			BitSet bs = new BitSet();
			definedAtomSets.put("_1", bs);
			BitSet bs0 = atomSets[0];
			for (int atom1 = bs0.nextSetBit(0); atom1 >= 0; atom1 = bs0
					.nextSetBit(atom1 + 1)) {
				bs.set(atom1);
				result = viewer.makeConnections(distances[0], distances[1],
						bondOrder, operation, bs,
						atomExpressionAt(expression2), bsBonds, isBonds, false,
						0);
				nNew += Math.abs(result[0]);
				nModified += result[1];
				bs.clear(atom1);
			}
		} else {
			result = viewer.makeConnections(distances[0], distances[1],
					bondOrder, operation, atomSets[0], atomSets[1], bsBonds,
					isBonds, addGroup, energy);
			nNew += Math.abs(result[0]);
			nModified += result[1];
		}
		if (isDelete) {
			if (!(tQuiet || scriptLevel > scriptReportingLevel))
				scriptStatusOrBuffer(GT._("{0} connections deleted", nModified));
			return;
		}
		if (isColorOrRadius) {
			viewer.selectBonds(bsBonds);
			if (!Float.isNaN(radius))
				setShapeSize(JmolConstants.SHAPE_STICKS, (int) (radius * 2000),
						null);
			if (color != Integer.MIN_VALUE)
				setShapeProperty(JmolConstants.SHAPE_STICKS, "color",
						Integer.valueOf(color), bsBonds);
			if (translucency != null) {
				if (translucentLevel == Float.MAX_VALUE)
					translucentLevel = viewer.getDefaultTranslucent();
				setShapeProperty(JmolConstants.SHAPE_STICKS,
						"translucentLevel", Float.valueOf(translucentLevel));
				setShapeProperty(JmolConstants.SHAPE_STICKS, "translucency",
						translucency, bsBonds);
			}
			viewer.selectBonds(null);
		}
		if (!(tQuiet || scriptLevel > scriptReportingLevel))
			scriptStatusOrBuffer(GT._(
					"{0} new bonds; {1} modified",
					new Object[] { Integer.valueOf(nNew),
							Integer.valueOf(nModified) }));
	}

	private float getTranslucentLevel(int i) throws ScriptException {
		float f = floatParameter(i);
		return (theTok == Token.integer && f > 0 && f < 9 ? f + 1 : f);
	}

	private void getProperty() throws ScriptException {
		if (isSyntaxCheck)
			return;
		String retValue = "";
		String property = optParameterAsString(1);
		String name = property;
		if (name.indexOf(".") >= 0)
			name = name.substring(0, name.indexOf("."));
		if (name.indexOf("[") >= 0)
			name = name.substring(0, name.indexOf("["));
		int propertyID = PropertyManager.getPropertyNumber(name);
		String param = optParameterAsString(2);
		int tok = tokAt(2);
		BitSet bs = (tok == Token.expressionBegin || tok == Token.bitset ? atomExpressionAt(2)
				: null);
		if (property.length() > 0 && propertyID < 0) {
			// no such property
			property = ""; // produces a list from Property Manager
			param = "";
		} else if (propertyID >= 0 && statementLength < 3) {
			param = PropertyManager.getDefaultParam(propertyID);
			if (param.equals("(visible)")) {
				viewer.setModelVisibility();
				bs = viewer.getVisibleSet();
			}
		} else if (propertyID == PropertyManager.PROP_FILECONTENTS_PATH) {
			for (int i = 3; i < statementLength; i++)
				param += parameterAsString(i);
		}
		retValue = (String) viewer.getProperty("readable", property,
				(bs == null ? (Object) param : (Object) bs));
		showString(retValue);
	}

	private void background(int i) throws ScriptException {
		getToken(i);
		int argb;
		if (theTok == Token.image) {
			// background IMAGE "xxxx.jpg"
			String file = parameterAsString(checkLast(++i));
			if (isSyntaxCheck)
				return;
			String[] retFileName = new String[1];
			Object image = null;
			if (!file.equalsIgnoreCase("none") && file.length() > 0) {
				image = viewer.getFileAsImage(file, retFileName);
				if (image == null)
					evalError(retFileName[0], null);
			}
			viewer.setBackgroundImage(retFileName[0], image);
			return;
		}
		if (isColorParam(i) || theTok == Token.none) {
			argb = getArgbParamLast(i, true);
			if (isSyntaxCheck)
				return;
			setObjectArgb("background", argb);
			viewer.setBackgroundImage(null, null);
			return;
		}
		int iShape = getShapeType(theTok);
		colorShape(iShape, i + 1, true);
	}

	private void center(int i) throws ScriptException {
		// from center (atom) or from zoomTo under conditions of not
		// windowCentered()
		if (statementLength == 1) {
			viewer.setNewRotationCenter(null);
			return;
		}
		Point3f center = centerParameter(i);
		if (center == null)
			error(ERROR_invalidArgument);
		if (!isSyntaxCheck)
			viewer.setNewRotationCenter(center);

	}

	private String setObjectProperty() throws ScriptException {
		String s = "";
		String id = getShapeNameParameter(2);
		Object[] data = new Object[] { id, null };
		if (isSyntaxCheck)
			return "";
		int iTok = iToken;
		int tokCommand = tokAt(0);
		boolean isWild = TextFormat.isWild(id);
		for (int iShape = JmolConstants.SHAPE_DIPOLES;;) {
			if (iShape != JmolConstants.SHAPE_MO
					&& getShapeProperty(iShape, "checkID", data)) {
				setShapeProperty(iShape, "thisID", id);
				switch (tokCommand) {
				case Token.delete:
					setShapeProperty(iShape, "delete", null);
					break;
				case Token.hide:
				case Token.display:
					setShapeProperty(iShape, "hidden",
							tokCommand == Token.display ? Boolean.FALSE
									: Boolean.TRUE);
					break;
				case Token.show:
					// if (iShape == JmolConstants.SHAPE_ISOSURFACE && !isWild)
					// return getIsosurfaceJvxl(false,
					// JmolConstants.SHAPE_ISOSURFACE);
					// else if (iShape == JmolConstants.SHAPE_PMESH && !isWild)
					// return getIsosurfaceJvxl(true,
					// JmolConstants.SHAPE_PMESH);
					s += (String) getShapeProperty(iShape, "command") + "\n";
					break;
				case Token.color:
					colorShape(iShape, iTok + 1, false);
					break;
				}
				if (!isWild)
					break;
			}
			if (iShape == JmolConstants.SHAPE_DIPOLES)
				iShape = JmolConstants.SHAPE_MAX_HAS_ID;
			if (--iShape < JmolConstants.SHAPE_MIN_HAS_ID)
				break;
		}
		return s;
	}

	private void color() throws ScriptException {

		int i = 1;
		boolean xx = isColorParam(1);
		if (isColorParam(1)) {
			theTok = Token.atoms;
		} else {
			int argb = 0;
			i = 2;
			int tok = getToken(1).tok;
			switch (tok) {
			case Token.dollarsign:
				setObjectProperty();
				return;
			case Token.altloc:
			case Token.amino:
			case Token.chain:
			case Token.fixedtemp:
			case Token.formalcharge:
			case Token.group:
			case Token.hydrophobic:
			case Token.insertion:
			case Token.jmol:
			case Token.molecule:
			case Token.monomer:
			case Token.none:
			case Token.opaque:
			case Token.partialcharge:
			case Token.polymer:
			case Token.property:
			case Token.rasmol:
			case Token.spacefill:
			case Token.shapely:
			case Token.straightness:
			case Token.structure:
			case Token.surfacedistance:
			case Token.atomsequence:
			case Token.temperature:
			case Token.translucent:
			case Token.user:
			case Token.vanderwaals:
				theTok = Token.atoms;
				i = 1;
				break;
			case Token.string:
				i = 1;
				String strColor = stringParameter(i++);
				if (isArrayParameter(i)) {
					strColor = strColor += "="
							+ ScriptVariable
									.sValue(ScriptVariable
											.getVariable(stringParameterSet(i)))
									.replace('\n', ' ');
					i = iToken + 1;
				}
				boolean isTranslucent = (tokAt(i) == Token.translucent);
				if (!isSyntaxCheck)
					viewer.setPropertyColorScheme(strColor, isTranslucent, true);
				if (isTranslucent)
					++i;
				if (tokAt(i) == Token.range || tokAt(i) == Token.absolute) {
					float min = floatParameter(++i);
					float max = floatParameter(++i);
					if (!isSyntaxCheck)
						viewer.setCurrentColorRange(min, max);
				}
				return;
			case Token.range:
			case Token.absolute:
				float min = floatParameter(2);
				float max = floatParameter(checkLast(3));
				if (!isSyntaxCheck)
					viewer.setCurrentColorRange(min, max);
				return;
			case Token.background:
				argb = getArgbParamLast(2, true);
				if (!isSyntaxCheck)
					setObjectArgb("background", argb);
				return;
			case Token.bitset:
			case Token.expressionBegin:
				i = -1;
				theTok = Token.atoms;
				break;
			case Token.rubberband:
				argb = getArgbParamLast(2, false);
				if (!isSyntaxCheck)
					viewer.setRubberbandArgb(argb);
				return;
			case Token.highlight:
			case Token.selectionhalos:
				i = 2;
				if (tokAt(2) == Token.opaque)
					i++;
				argb = getArgbParamLast(i, true);
				if (isSyntaxCheck)
					return;
				shapeManager.loadShape(JmolConstants.SHAPE_HALOS);
				setShapeProperty(JmolConstants.SHAPE_HALOS,
						(tok == Token.selectionhalos ? "argbSelection"
								: "argbHighlight"), Integer.valueOf(argb));
				return;
			case Token.axes:
			case Token.boundbox:
			case Token.unitcell:
			case Token.identifier:
			case Token.hydrogen:
				// color element
				String str = parameterAsString(1);
				if (checkToken(2)) {
					switch (getToken(2).tok) {
					case Token.rasmol:
						argb = Token.rasmol;
						break;
					case Token.none:
					case Token.jmol:
						argb = Token.jmol;
						break;
					default:
						argb = getArgbParam(2);
					}
				}
				if (argb == 0)
					error(ERROR_colorOrPaletteRequired);
				checkLast(iToken);
				if (str.equalsIgnoreCase("axes")
						|| StateManager.getObjectIdFromName(str) >= 0) {
					setObjectArgb(str, argb);
					return;
				}
				if (changeElementColor(str, argb))
					return;
				error(ERROR_invalidArgument);
				break;
			case Token.isosurface:
			case Token.contact:
				setShapeProperty(JmolConstants.shapeTokenIndex(tok), "thisID",
						MeshCollection.PREVIOUS_MESH_ID);
				break;
			}
		}

		colorShape(getShapeType(theTok), i, false);
	}

	private boolean changeElementColor(String str, int argb) {
		for (int i = Elements.elementNumberMax; --i >= 0;) {
			if (str.equalsIgnoreCase(Elements.elementNameFromNumber(i))) {
				if (!isSyntaxCheck)
					viewer.setElementArgb(i, argb);
				return true;
			}
		}
		for (int i = Elements.altElementMax; --i >= 0;) {
			if (str.equalsIgnoreCase(Elements.altElementNameFromIndex(i))) {
				if (!isSyntaxCheck)
					viewer.setElementArgb(
							Elements.altElementNumberFromIndex(i), argb);
				return true;
			}
		}
		if (str.charAt(0) != '_')
			return false;
		for (int i = Elements.elementNumberMax; --i >= 0;) {
			if (str.equalsIgnoreCase("_" + Elements.elementSymbolFromNumber(i))) {
				if (!isSyntaxCheck)
					viewer.setElementArgb(i, argb);
				return true;
			}
		}
		for (int i = Elements.altElementMax; --i >= Elements.firstIsotope;) {
			if (str.equalsIgnoreCase("_"
					+ Elements.altElementSymbolFromIndex(i))) {
				if (!isSyntaxCheck)
					viewer.setElementArgb(
							Elements.altElementNumberFromIndex(i), argb);
				return true;
			}
			if (str.equalsIgnoreCase("_"
					+ Elements.altIsotopeSymbolFromIndex(i))) {
				if (!isSyntaxCheck)
					viewer.setElementArgb(
							Elements.altElementNumberFromIndex(i), argb);
				return true;
			}
		}
		return false;
	}

	private void colorShape(int shapeType, int index, boolean isBackground)
			throws ScriptException {
		String translucency = null;
		Object colorvalue = null;
		Object colorvalue1 = null;
		BitSet bs = null;
		String prefix = "";
		boolean isColor = false;
		boolean isIsosurface = (shapeType == JmolConstants.SHAPE_ISOSURFACE || shapeType == JmolConstants.SHAPE_CONTACT);
		int typeMask = 0;
		boolean doClearBondSet = false;
		float translucentLevel = Float.MAX_VALUE;
		if (index < 0) {
			bs = atomExpressionAt(-index);
			index = iToken + 1;
			if (isBondSet) {
				doClearBondSet = true;
				shapeType = JmolConstants.SHAPE_STICKS;
			}
		}
		if (isBackground)
			getToken(index);
		else if ((isBackground = (getToken(index).tok == Token.background)) == true)
			getToken(++index);
		if (isBackground)
			prefix = "bg";
		else if (isIsosurface) {
			switch (theTok) {
			case Token.mesh:
				getToken(++index);
				prefix = "mesh";
				break;
			case Token.phase:
				int argb = getArgbParam(++index, false);
				colorvalue1 = (argb == 0 ? null : Integer.valueOf(argb));
				getToken(index = iToken + 1);
				break;
			case Token.bitset:
			case Token.expressionBegin:
				if (theToken.value instanceof BondSet) {
					bs = (BondSet) theToken.value;
					prefix = "vertex";
				} else {
					bs = atomExpressionAt(index);
					// don't allow isosurface partial translucency (yet)
					prefix = "atom";
				}
				translucentLevel = Float.MIN_VALUE;
				getToken(index = iToken + 1);
				break;
			}
		}
		if (!isSyntaxCheck && shapeType == JmolConstants.SHAPE_MO && !mo(true))
			return;
		boolean isTranslucent = (theTok == Token.translucent);
		if (isTranslucent || theTok == Token.opaque) {
			if (translucentLevel == Float.MIN_VALUE)
				error(ERROR_invalidArgument);
			translucency = parameterAsString(index++);
			if (isTranslucent && isFloatParameter(index))
				translucentLevel = getTranslucentLevel(index++);
		}
		int tok = 0;
		if (index < statementLength && tokAt(index) != Token.on
				&& tokAt(index) != Token.off) {
			isColor = true;
			tok = getToken(index).tok;
			if ((!isIsosurface || tokAt(index + 1) != Token.to)
					&& isColorParam(index)) {
				int argb = getArgbParam(index, false);
				colorvalue = (argb == 0 ? null : Integer.valueOf(argb));
				if (translucency == null
						&& tokAt(index = iToken + 1) != Token.nada) {
					getToken(index);
					isTranslucent = (theTok == Token.translucent);
					if (isTranslucent || theTok == Token.opaque) {
						translucency = parameterAsString(index);
						if (isTranslucent && isFloatParameter(index + 1))
							translucentLevel = getTranslucentLevel(++index);
					}
					// checkLength(index + 1);
					// iToken = index;
				}
			} else if (shapeType == JmolConstants.SHAPE_LCAOCARTOON) {
				iToken--; // back up one
			} else {
				// must not be a color, but rather a color SCHEME
				// this could be a problem for properties, which can't be
				// checked later -- they must be turned into a color NOW.

				// "cpk" value would be "spacefill"
				String name = parameterAsString(index).toLowerCase();
				boolean isByElement = (name
						.indexOf(ColorEncoder.BYELEMENT_PREFIX) == 0);
				boolean isColorIndex = (isByElement || name
						.indexOf(ColorEncoder.BYRESIDUE_PREFIX) == 0);
				EnumPalette pal = (isColorIndex || isIsosurface ? EnumPalette.PROPERTY
						: tok == Token.spacefill ? EnumPalette.CPK
								: EnumPalette.getPalette(name));
				// color atoms "cpkScheme"
				if (pal == EnumPalette.UNKNOWN
						|| (pal == EnumPalette.TYPE || pal == EnumPalette.ENERGY)
						&& shapeType != JmolConstants.SHAPE_HSTICKS)
					error(ERROR_invalidArgument);
				Object data = null;
				BitSet bsSelected = (pal != EnumPalette.PROPERTY
						&& pal != EnumPalette.VARIABLE
						|| !viewer.isRangeSelected() ? null : viewer
						.getSelectionSet(false));
				if (pal == EnumPalette.PROPERTY) {
					if (isColorIndex) {
						if (!isSyntaxCheck) {
							data = getBitsetPropertyFloat(
									bsSelected,
									(isByElement ? Token.elemno : Token.groupid)
											| Token.allfloat, Float.NaN,
									Float.NaN);
						}
					} else {
						if (!isColorIndex && !isIsosurface)
							index++;
						if (name.equals("property")
								&& Token.tokAttr((tok = getToken(index).tok),
										Token.atomproperty)
								&& !Token.tokAttr(tok, Token.strproperty)) {
							if (!isSyntaxCheck) {
								data = getBitsetPropertyFloat(bsSelected,
										getToken(index++).tok | Token.allfloat,
										Float.NaN, Float.NaN);
							}
						}
					}
				} else if (pal == EnumPalette.VARIABLE) {
					index++;
					name = parameterAsString(index++);
					data = new float[viewer.getAtomCount()];
					Parser.parseStringInfestedFloatArray(
							"" + getParameter(name, Token.string), null,
							(float[]) data);
					pal = EnumPalette.PROPERTY;
				}
				if (pal == EnumPalette.PROPERTY) {
					String scheme = null;
					if (tokAt(index) == Token.string) {
						scheme = parameterAsString(index++).toLowerCase();
						if (isArrayParameter(index)) {
							scheme += "="
									+ ScriptVariable
											.sValue(ScriptVariable
													.getVariable(stringParameterSet(index)))
											.replace('\n', ' ');
							index = iToken + 1;
						}
					} else if (isIsosurface && isColorParam(index)) {
						scheme = getColorRange(index);
						index = iToken + 1;
					}
					if (scheme != null && !isIsosurface) {
						setStringProperty(
								"propertyColorScheme",
								(isTranslucent
										&& translucentLevel == Float.MAX_VALUE ? "translucent "
										: "")
										+ scheme);
						isColorIndex = (scheme
								.indexOf(ColorEncoder.BYELEMENT_PREFIX) == 0 || scheme
								.indexOf(ColorEncoder.BYRESIDUE_PREFIX) == 0);
					}
					float min = 0;
					float max = Float.MAX_VALUE;
					if (!isColorIndex
							&& (tokAt(index) == Token.absolute || tokAt(index) == Token.range)) {
						min = floatParameter(index + 1);
						max = floatParameter(index + 2);
						index += 3;
						if (min == max && isIsosurface) {
							float[] range = (float[]) getShapeProperty(
									shapeType, "dataRange");
							if (range != null) {
								min = range[0];
								max = range[1];
							}
						} else if (min == max) {
							max = Float.MAX_VALUE;
						}
					}
					if (!isSyntaxCheck) {
						if (isIsosurface) {
						} else if (data == null) {
							viewer.setCurrentColorRange(name);
						} else {
							viewer.setCurrentColorRange((float[]) data,
									bsSelected);
						}
						if (isIsosurface) {
							checkLength(index);
							isColor = false;
							ColorEncoder ce = viewer.getColorEncoder(scheme);
							if (ce == null)
								return;
							ce.isTranslucent = (isTranslucent && translucentLevel == Float.MAX_VALUE);
							ce.setRange(min, max, min > max);
							if (max == Float.MAX_VALUE)
								ce.hi = max;
							setShapeProperty(shapeType, "remapColor", ce);
							showString(getIsosurfaceDataRange(shapeType, ""));
							if (translucentLevel == Float.MAX_VALUE)
								return;
						} else if (max != Float.MAX_VALUE) {
							viewer.setCurrentColorRange(min, max);
						}
					}
				} else {
					index++;
				}
				checkLength(index);
				colorvalue = pal;
			}
		}
		if (isSyntaxCheck || shapeType < 0)
			return;
		switch (shapeType) {
		case JmolConstants.SHAPE_STRUTS:
			typeMask = JmolEdge.BOND_STRUT;
			break;
		case JmolConstants.SHAPE_HSTICKS:
			typeMask = JmolEdge.BOND_HYDROGEN_MASK;
			break;
		case JmolConstants.SHAPE_SSSTICKS:
			typeMask = JmolEdge.BOND_SULFUR_MASK;
			break;
		case JmolConstants.SHAPE_STICKS:
			typeMask = JmolEdge.BOND_COVALENT_MASK;
			break;
		default:
			typeMask = 0;
		}
		if (typeMask == 0) {
			shapeManager.loadShape(shapeType);
			if (shapeType == JmolConstants.SHAPE_LABELS)
				setShapeProperty(JmolConstants.SHAPE_LABELS, "setDefaults",
						viewer.getNoneSelected());
		} else {
			if (bs != null) {
				viewer.selectBonds(bs);
				bs = null;
			}
			shapeType = JmolConstants.SHAPE_STICKS;
			setShapeProperty(shapeType, "type", Integer.valueOf(typeMask));
		}
		if (isColor) {
			// ok, the following five options require precalculation.
			// the state must not save them as paletteIDs, only as pure
			// color values.
			switch (tok) {
			case Token.surfacedistance:
			case Token.straightness:
			case Token.atomsequence:
				viewer.autoCalculate(tok);
				break;
			case Token.temperature:
				if (viewer.isRangeSelected())
					viewer.clearBfactorRange();
				break;
			case Token.group:
				viewer.calcSelectedGroupsCount();
				break;
			case Token.polymer:
			case Token.monomer:
				viewer.calcSelectedMonomersCount();
				break;
			case Token.molecule:
				viewer.calcSelectedMoleculesCount();
				break;
			}
			if (isIsosurface && colorvalue1 != null)
				setShapeProperty(shapeType, "colorPhase", new Object[] {
						colorvalue1, colorvalue });
			else if (bs == null)
				setShapeProperty(shapeType, prefix + "color", colorvalue);
			else
				setShapeProperty(shapeType, prefix + "color", colorvalue, bs);
		}
		if (translucency != null)
			setShapeTranslucency(shapeType, prefix, translucency,
					translucentLevel, bs);
		if (typeMask != 0)
			setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
					Integer.valueOf(JmolEdge.BOND_COVALENT_MASK));
		if (doClearBondSet)
			viewer.selectBonds(null);
	}

	private void colorShape(int shapeType, int typeMask, int argb,
			String translucency, float translucentLevel, BitSet bs) {

		if (typeMask != 0) {
			setShapeProperty(shapeType = JmolConstants.SHAPE_STICKS, "type",
					Integer.valueOf(typeMask));
		}
		setShapeProperty(shapeType, "color", Integer.valueOf(argb), bs);
		if (translucency != null)
			setShapeTranslucency(shapeType, "", translucency, translucentLevel,
					bs);
		if (typeMask != 0)
			setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
					Integer.valueOf(JmolEdge.BOND_COVALENT_MASK));
	}

	private void setShapeTranslucency(int shapeType, String prefix,
			String translucency, float translucentLevel, BitSet bs) {
		if (translucentLevel == Float.MAX_VALUE)
			translucentLevel = viewer.getDefaultTranslucent();
		setShapeProperty(shapeType, "translucentLevel",
				Float.valueOf(translucentLevel));
		if (prefix == null)
			return;
		if (bs == null)
			setShapeProperty(shapeType, prefix + "translucency", translucency);
		else if (!isSyntaxCheck)
			setShapeProperty(shapeType, prefix + "translucency", translucency,
					bs);
	}

	private void cd() throws ScriptException {
		if (isSyntaxCheck)
			return;
		String dir = (statementLength == 1 ? null : parameterAsString(1));
		showString(viewer.cd(dir));
	}

	private Object[] data;

	private void mapProperty() throws ScriptException {
		// map {1.1}.straightness {2.1}.property_x resno
		BitSet bsFrom, bsTo;
		String property1, property2, mapKey;
		int tokProp1 = 0;
		int tokProp2 = 0;
		int tokKey = 0;
		while (true) {
			if (tokAt(1) == Token.selected) {
				bsFrom = viewer.getSelectionSet(false);
				bsTo = atomExpressionAt(2);
				property1 = property2 = "selected";
			} else {
				bsFrom = atomExpressionAt(1);
				if (tokAt(++iToken) != Token.per
						|| !Token.tokAttr(tokProp1 = tokAt(++iToken),
								Token.atomproperty))
					break;
				property1 = parameterAsString(iToken);
				bsTo = atomExpressionAt(++iToken);
				if (tokAt(++iToken) != Token.per
						|| !Token.tokAttr(tokProp2 = tokAt(++iToken),
								Token.settable))
					break;
				property2 = parameterAsString(iToken);
			}
			if (Token.tokAttr(tokKey = tokAt(iToken + 1), Token.atomproperty))
				mapKey = parameterAsString(++iToken);
			else
				mapKey = Token.nameOf(tokKey = Token.atomno);
			checkLast(iToken);
			if (isSyntaxCheck)
				return;
			BitSet bsOut = null;
			showString("mapping " + property1.toUpperCase() + " for "
					+ bsFrom.cardinality() + " atoms to "
					+ property2.toUpperCase() + " for " + bsTo.cardinality()
					+ " atoms using " + mapKey.toUpperCase());
			if (Token.tokAttrOr(tokProp1, Token.intproperty,
					Token.floatproperty)
					&& Token.tokAttrOr(tokProp2, Token.intproperty,
							Token.floatproperty)
					&& Token.tokAttrOr(tokKey, Token.intproperty,
							Token.floatproperty)) {
				float[] data1 = getBitsetPropertyFloat(bsFrom, tokProp1
						| Token.selectedfloat, Float.NaN, Float.NaN);
				float[] data2 = getBitsetPropertyFloat(bsFrom, tokKey
						| Token.selectedfloat, Float.NaN, Float.NaN);
				float[] data3 = getBitsetPropertyFloat(bsTo, tokKey
						| Token.selectedfloat, Float.NaN, Float.NaN);
				boolean isProperty = (tokProp2 == Token.property);
				float[] dataOut = new float[isProperty ? viewer.getAtomCount()
						: data3.length];
				bsOut = new BitSet();
				if (data1.length == data2.length) {
					Map<Float, Float> ht = new Hashtable<Float, Float>();
					for (int i = 0; i < data1.length; i++) {
						ht.put(Float.valueOf(data2[i]), Float.valueOf(data1[i]));
					}
					int pt = -1;
					int nOut = 0;
					for (int i = 0; i < data3.length; i++) {
						pt = bsTo.nextSetBit(pt + 1);
						Float F = ht.get(Float.valueOf(data3[i]));
						if (F == null)
							continue;
						bsOut.set(pt);
						dataOut[(isProperty ? pt : nOut)] = F.floatValue();
						nOut++;
					}
					if (isProperty)
						viewer.setData(property2, new Object[] { property2,
								dataOut, bsOut }, viewer.getAtomCount(), 0, 0,
								Integer.MAX_VALUE, 0);
					else
						viewer.setAtomProperty(bsOut, tokProp2, 0, 0, null,
								dataOut, null);
				}
			}
			if (bsOut == null) {
				String format = "{" + mapKey + "=%[" + mapKey + "]}."
						+ property2 + " = %[" + property1 + "]";
				String[] data = (String[]) getBitsetIdent(bsFrom, format, null,
						false, Integer.MAX_VALUE, false);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < data.length; i++)
					if (data[i].indexOf("null") < 0)
						sb.append(data[i]).append('\n');
				if (Logger.debugging)
					Logger.info(sb.toString());
				BitSet bsSubset = BitSetUtil.copy(viewer.getSelectionSubset());
				viewer.setSelectionSubset(bsTo);
				try {
					runScript(sb.toString());
				} catch (Exception e) {
					viewer.setSelectionSubset(bsSubset);
					error(-1, "Error: " + e.getMessage());
				} catch (Error er) {
					viewer.setSelectionSubset(bsSubset);
					error(-1, "Error: " + er.getMessage());
				}
				viewer.setSelectionSubset(bsSubset);
			}
			showString("DONE");
			return;
		}
		error(ERROR_invalidArgument);
	}

	private void data() throws ScriptException {
		String dataString = null;
		String dataLabel = null;
		boolean isOneValue = false;
		int i;
		switch (iToken = statementLength) {
		case 5:
			// parameters 3 and 4 are just for the ride: [end] and ["key"]
			dataString = parameterAsString(2);
			//$FALL-THROUGH$
		case 4:
		case 2:
			dataLabel = parameterAsString(1);
			if (dataLabel.equalsIgnoreCase("clear")) {
				if (!isSyntaxCheck)
					viewer.setData(null, null, 0, 0, 0, 0, 0);
				return;
			}
			if ((i = dataLabel.indexOf("@")) >= 0) {
				dataString = ""
						+ getParameter(dataLabel.substring(i + 1), Token.string);
				dataLabel = dataLabel.substring(0, i).trim();
			} else if (dataString == null && (i = dataLabel.indexOf(" ")) >= 0) {
				dataString = dataLabel.substring(i + 1).trim();
				dataLabel = dataLabel.substring(0, i).trim();
				isOneValue = true;
			}
			break;
		default:
			error(ERROR_badArgumentCount);
		}
		String dataType = dataLabel + " ";
		dataType = dataType.substring(0, dataType.indexOf(" ")).toLowerCase();
		if (dataType.equals("model") || dataType.equals("append")) {
			load();
			return;
		}
		if (isSyntaxCheck)
			return;
		boolean isDefault = (dataLabel.toLowerCase().indexOf("(default)") >= 0);
		data = new Object[3];
		if (dataType.equals("element_vdw")) {
			// vdw for now
			data[0] = dataType;
			data[1] = dataString.replace(';', '\n');
			int n = Elements.elementNumberMax;
			int[] eArray = new int[n + 1];
			for (int ie = 1; ie <= n; ie++)
				eArray[ie] = ie;
			data[2] = eArray;
			viewer.setData("element_vdw", data, n, 0, 0, 0, 0);
			return;
		}
		if (dataType.equals("connect_atoms")) {
			viewer.connect(Parser.parseFloatArray2d(dataString));
			return;
		}
		if (dataType.indexOf("ligand_") == 0) {
			// ligand structure for pdbAddHydrogen
			viewer.setLigandModel(dataLabel.substring(7), dataString.trim());
			return;
		}
		if (dataType.indexOf("data2d_") == 0) {
			// data2d_someName
			data[0] = dataLabel;
			data[1] = Parser.parseFloatArray2d(dataString);
			viewer.setData(dataLabel, data, 0, 0, 0, 0, 0);
			return;
		}
		if (dataType.indexOf("data3d_") == 0) {
			// data3d_someName
			data[0] = dataLabel;
			data[1] = Parser.parseFloatArray3d(dataString);
			viewer.setData(dataLabel, data, 0, 0, 0, 0, 0);
			return;
		}
		String[] tokens = Parser.getTokens(dataLabel);
		if (dataType.indexOf("property_") == 0
				&& !(tokens.length == 2 && tokens[1].equals("set"))) {
			BitSet bs = viewer.getSelectionSet(false);
			data[0] = dataType;
			int atomNumberField = (isOneValue ? 0 : ((Integer) viewer
					.getParameter("propertyAtomNumberField")).intValue());
			int atomNumberFieldColumnCount = (isOneValue ? 0
					: ((Integer) viewer
							.getParameter("propertyAtomNumberColumnCount"))
							.intValue());
			int propertyField = (isOneValue ? Integer.MIN_VALUE
					: ((Integer) viewer.getParameter("propertyDataField"))
							.intValue());
			int propertyFieldColumnCount = (isOneValue ? 0 : ((Integer) viewer
					.getParameter("propertyDataColumnCount")).intValue());
			if (!isOneValue && dataLabel.indexOf(" ") >= 0) {
				if (tokens.length == 3) {
					// DATA "property_whatever [atomField] [propertyField]"
					dataLabel = tokens[0];
					atomNumberField = Parser.parseInt(tokens[1]);
					propertyField = Parser.parseInt(tokens[2]);
				}
				if (tokens.length == 5) {
					// DATA
					// "property_whatever [atomField] [atomFieldColumnCount] [propertyField] [propertyDataColumnCount]"
					dataLabel = tokens[0];
					atomNumberField = Parser.parseInt(tokens[1]);
					atomNumberFieldColumnCount = Parser.parseInt(tokens[2]);
					propertyField = Parser.parseInt(tokens[3]);
					propertyFieldColumnCount = Parser.parseInt(tokens[4]);
				}
			}
			if (atomNumberField < 0)
				atomNumberField = 0;
			if (propertyField < 0)
				propertyField = 0;
			int atomCount = viewer.getAtomCount();
			int[] atomMap = null;
			BitSet bsTemp = new BitSet(atomCount);
			if (atomNumberField > 0) {
				atomMap = new int[atomCount + 2];
				for (int j = 0; j <= atomCount; j++)
					atomMap[j] = -1;
				for (int j = bs.nextSetBit(0); j >= 0; j = bs.nextSetBit(j + 1)) {
					int atomNo = viewer.getAtomNumber(j);
					if (atomNo > atomCount + 1 || atomNo < 0
							|| bsTemp.get(atomNo))
						continue;
					bsTemp.set(atomNo);
					atomMap[atomNo] = j;
				}
				data[2] = atomMap;
			} else {
				data[2] = BitSetUtil.copy(bs);
			}
			data[1] = dataString;
			viewer.setData(dataType, data, atomCount, atomNumberField,
					atomNumberFieldColumnCount, propertyField,
					propertyFieldColumnCount);
			return;
		}
		int userType = AtomCollection.getUserSettableType(dataType);
		if (userType >= 0) {
			// this is a known settable type or "property_xxxx"
			viewer.setAtomData(userType, dataType, dataString, isDefault);
			return;
		}
		// this is just information to be stored.
		data[0] = dataLabel;
		data[1] = dataString;
		viewer.setData(dataType, data, 0, 0, 0, 0, 0);
	}

	private void define() throws ScriptException {
		// note that the standard definition depends upon the
		// current state. Once defined, a setName is the set
		// of atoms that matches the definition at that time.
		// adding DYMAMIC_ to the beginning of the definition
		// allows one to create definitions that are recalculated
		// whenever they are used. When used, "DYNAMIC_" is dropped
		// so, for example:
		// define DYNAMIC_what selected and visible
		// and then
		// select what
		// will return different things at different times depending
		// upon what is selected and what is visible
		// but
		// define what selected and visible
		// will evaluate the moment it is defined and then represent
		// that set of atoms forever.

		if (statementLength < 3 || !(getToken(1).value instanceof String))
			error(ERROR_invalidArgument);
		String setName = ((String) getToken(1).value).toLowerCase();
		if (Parser.parseInt(setName) != Integer.MIN_VALUE)
			error(ERROR_invalidArgument);
		if (isSyntaxCheck)
			return;
		boolean isSite = setName.startsWith("site_");
		boolean isDynamic = (setName.indexOf("dynamic_") == 0);
		if (isDynamic || isSite) {
			Token[] code = new Token[statementLength];
			for (int i = statementLength; --i >= 0;)
				code[i] = statement[i];
			definedAtomSets.put(
					"!" + (isSite ? setName : setName.substring(8)), code);
			// if (!isSite)
			// viewer.addStateScript(thisCommand, false, true); removed for
			// 12.1.16
		} else {
			BitSet bs = atomExpressionAt(2);
			definedAtomSets.put(setName, bs);
			if (!isSyntaxCheck)
				viewer.setUserVariable("@" + setName,
						ScriptVariable.getVariable(bs));
		}
	}

	private void echo(int index, boolean isImage) throws ScriptException {
		if (isSyntaxCheck)
			return;
		String text = optParameterAsString(index);
		if (viewer.getEchoStateActive()) {
			if (isImage) {
				String[] retFileName = new String[1];
				Object image = viewer.getFileAsImage(text, retFileName);
				if (image == null) {
					text = retFileName[0];
				} else {
					setShapeProperty(JmolConstants.SHAPE_ECHO, "text",
							retFileName[0]);
					setShapeProperty(JmolConstants.SHAPE_ECHO, "image", image);
					text = null;
				}
			} else if (text.startsWith("\1")) {
				// no reporting, just screen echo, from mouseManager key press
				text = text.substring(1);
				isImage = true;
			}
			if (text != null)
				setShapeProperty(JmolConstants.SHAPE_ECHO, "text", text);
		}
		if (!isImage && viewer.getRefreshing())
			showString(viewer.formatText(text));
	}

	private void message() throws ScriptException {
		String text = parameterAsString(checkLast(1));
		if (isSyntaxCheck)
			return;
		String s = viewer.formatText(text);
		if (outputBuffer == null)
			viewer.showMessage(s);
		if (!s.startsWith("_"))
			scriptStatusOrBuffer(s);
	}

	private void log() throws ScriptException {
		if (statementLength == 1)
			error(ERROR_badArgumentCount);
		if (isSyntaxCheck)
			return;
		String s = parameterExpressionString(1, 0);
		if (tokAt(1) == Token.off)
			setStringProperty("logFile", "");
		else
			viewer.log(s);
	}

	private void label(int index) throws ScriptException {
		if (isSyntaxCheck)
			return;
		shapeManager.loadShape(JmolConstants.SHAPE_LABELS);
		String strLabel = null;

		switch (getToken(index).tok) {
		case Token.on:
			strLabel = viewer.getStandardLabelFormat(0);
			break;
		case Token.off:
			break;
		case Token.hide:
		case Token.display:
			setShapeProperty(JmolConstants.SHAPE_LABELS, "display",
					theTok == Token.display ? Boolean.TRUE : Boolean.FALSE);
			return;
		default:
			strLabel = parameterAsString(index);
		}

		shapeManager.setLabel(strLabel, viewer.getSelectionSet(false));

	}

	private void hover() throws ScriptException {
		if (isSyntaxCheck)
			return;
		String strLabel = parameterAsString(1);
		if (strLabel.equalsIgnoreCase("on"))
			strLabel = "%U";
		else if (strLabel.equalsIgnoreCase("off"))
			strLabel = null;
		viewer.setHoverLabel(strLabel);
	}

	// added -hcf
	// for recording current selected unit
	int[] selectedPath = { 1, 0, 0, 0, 0 }; // for recording the current
											// selected Path

	String spName = "Homo sapiens"; // default genome species
	String genomeChr = "1"; // default genome chromosome number
	int genomeStart = 1; // default genome start position
	int genomeEnd = 1000; // default genome end position

	Chromosome selectedChrSeq = null;

	private boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	// extract sequence from local dataset
	// private String seqFilePath = "";
	private void gseqLocal() throws ScriptException {
		boolean checkGseqCommand = checkGseqCommandLocal();// should be fixed
		if (!checkGseqCommand) {
			error(ERROR_cannotFindGenomeSeq);
			return;
		} else {
			String currentStrucFile = viewer.getFullPathName();
			String currentSeqFile = (String) statement[10].value;

			int chrNum = statement[4].intValue;
			int fromPos = statement[6].intValue;
			int endPos = statement[8].intValue;			
			//added lxq35
			if (endPos - fromPos > 190000000) {
				error(ERROR_seqTooLong);
			}
			
			String currentSeqFileFullPath = getSeqFullPath(currentStrucFile,
					currentSeqFile);
			showString("Loading sequence from local database:"
					+ currentSeqFileFullPath);
			String seqTAG = ">" + spName + " | CHR" + chrNum + " : " + fromPos
					+ "-" + endPos;
			showString(seqTAG);
			showString(formFASTA(getBaseSeq(currentSeqFileFullPath, fromPos,
					endPos + 1)));
			showString("Loading complete");
			showString("-------------------------");
		}
	}

	private boolean checkGseqCommandLocal() {
		// Token[] xx = statement;
		if (statement.length != 12) {
			return false;
		} else {
			if (statement[3].value.equals(",") && statement[4].intValue >= 0
					&& statement[5].value.equals(",")
					&& statement[6].intValue >= 0
					&& statement[7].value.equals(",")
					&& statement[8].intValue >= 0
					&& statement[8].intValue > statement[6].intValue
					&& statement[9].value.equals(",")
					&& statement[10].value != "") {
				return true;
			} else {
				return false;
			}
		}
	}

	private String getSeqFullPath(String currentStrucFile, String currentSeqFile) {
		String currentSeqFileFullPath = "";
		String[] strucFilePathSplit = currentStrucFile.split("\\\\");
		if (strucFilePathSplit.length > 0) {
			strucFilePathSplit[strucFilePathSplit.length - 1] = currentSeqFile;
			strucFilePathSplit[strucFilePathSplit.length - 2] = "sequence";
		}
		for (int ii = 0; ii <= strucFilePathSplit.length - 1; ii++) {
			String fileSplit = strucFilePathSplit[ii];
			currentSeqFileFullPath = currentSeqFileFullPath + fileSplit;
			if (ii != strucFilePathSplit.length - 1) {
				currentSeqFileFullPath = currentSeqFileFullPath + "\\";
			}
		}
		return currentSeqFileFullPath;
	}

	// extract sequence from local dataset
	private String getBaseSeq(String currentSeqFileFullPath, int fromPos,
			int endPos) throws ScriptException {
		// File file = new File(currentSeqFileFullPath);
		String chrSeq = "";
		String selectedSeq = "";
		int selectedSeqLength = endPos - fromPos;
		int seekPos = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(currentSeqFileFullPath,
					"r");
			Pattern patternHeader = Pattern.compile("^>(.*)");
			Pattern patternSeq = Pattern.compile("[ATGCUNatgcun]+");
			String firstLine = raf.readLine();
			String secLine = raf.readLine();
			int singleLineLen = 0;
			Matcher matcherHeader = patternHeader.matcher(firstLine);
			Matcher matcherSeq = patternSeq.matcher(secLine);
			if (matcherHeader.matches() && matcherSeq.matches()) {
				seekPos = seekPos + firstLine.getBytes().length + 1;
				singleLineLen = secLine.length();
			} else {
				error(ERROR_notFASTAformat);
			}

			// calculate seek position based on file format and fromPos
			long selectedSeqStartPos = seekPos
					+ ((int) Math.floor((fromPos - 1) / singleLineLen))
					+ fromPos - 1;
			raf.seek(selectedSeqStartPos);
			String tmpString = "";
			while ((tmpString = raf.readLine()) != null) {
				selectedSeq = selectedSeq + tmpString;
				if (selectedSeq.length() > selectedSeqLength) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			error(ERROR_cannotFindGenomeSeq);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		// showString(selectedSeq);
		selectedSeq = selectedSeq.substring(0, selectedSeqLength);

		return selectedSeq;
	}
	
	//added lxq35
	private void kilThread(){
		//stop the excution of BLAST
		threadStop = true;
	}
	
	// extract sequence from Ensembl dataset
	private void gseqEnsembl() throws ScriptException {
		boolean checkGseqCommand = checkGseqCommandEnsembl();
		// boolean checkGseqCommand = true;
		if (!checkGseqCommand) {
			error(ERROR_cannotFindGenomeSeq);
			return;
		} else {
			// refresh parameters
			spName = (String) statement[2].value;
			genomeChr = String.valueOf(statement[4].intValue);
			genomeStart = statement[6].intValue;
			genomeEnd = statement[8].intValue;

			try {
				// check start and end position
				if (genomeStart == 0)
					genomeStart = 1;
				if (genomeStart < 0 || genomeEnd <= 0
						|| genomeStart > genomeEnd) {
					error(ERROR_cannotFindGenomeSeq);
				}
				//added lxq35
				if (genomeEnd - genomeStart > 190000000) {
					error(ERROR_seqTooLong);
				}
				
				showString("Loading genome sequence from Ensembl...");
				
				
				DBRegistry enReg = new DBRegistry(DataSource.ENSEMBLDB);
				;
				DBSpecies sp = enReg.getSpeciesByAlias(spName);
				// check species name
				if (sp == null) {
					error(ERROR_cannotFindSpecies);
				}
				selectedChrSeq = sp.getChromosomeByName(genomeChr);

				// check chromosome sequence
				if (selectedChrSeq == null) {
					error(ERROR_cannotFindGenomeSeq);
				}
				// for the endPos, if endPos larger than chrom length, then
				// endPos = length
				if (genomeEnd > selectedChrSeq.getDBSeqLength()) {
					genomeEnd = selectedChrSeq.getDBSeqLength();
				}
				String selectedGenomeSeq = selectedChrSeq.getSequenceAsString(
						genomeStart, genomeEnd);
				String formGenomeSeq = formFASTA(selectedGenomeSeq);
				String seqTAG = ">" + spName + " | CHR" + genomeChr + " : "
						+ genomeStart + "-" + genomeEnd;
				showString(seqTAG);
				showString(formGenomeSeq);
				showString("Loading completed.");
				showString("-------------------------");
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				error(ERROR_cannotFindGenomeSeq);
			} catch(ConfigurationException e) {
				error(ERROR_cannotFindGenomeSeq);
			}

		}

	}

	private boolean checkGseqCommandEnsembl() {
		if (statement.length != 10) {
			return false;
		} else {
			if (statement[3].value.equals(",") && statement[4].intValue >= 0
					&& statement[5].value.equals(",")
					&& statement[6].intValue >= 0
					&& statement[7].value.equals(",")
					&& statement[8].intValue >= 0
					&& statement[8].intValue > statement[6].intValue) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	private String gseqUCSC(String spName, String genomeChr, int genomeStart, int genomeEnd) throws ScriptException{
		boolean b = false;
		String inputLine = null;
		String seqLine = "";
		Pattern charp = Pattern.compile("<"+".*"+">");
		Matcher seq = null;
		// boolean checkGseqCommand = true;
		try {
				// check start and end position
				if (genomeStart == 0)
					genomeStart = 1;
				if (genomeStart < 0 || genomeEnd <= 0
						|| genomeStart > genomeEnd) {
					error(ERROR_cannotFindGenomeSeq);
				}
				//added lxq35
				if (genomeEnd - genomeStart > 190000000) {
					error(ERROR_seqTooLong);
				}
				
				switch (spName) {
					case "Homo_sapiens":
						spName = "hg19";
						break;
					case "Bos_taurus":
						spName = "bosTau6";
						break;
					case "Caenorhabditis_elegans":
						spName = "ce10";
						break;
					case "Callithrix_jacchus":
						spName = "calJac3";
						break;
					case "Ciona_intestinalis":
						spName = "ci2";
						break;
					case "Danio_rerio":
						spName = "danRer7";
						break;
					case "Drosophila_melanogaster":
						spName = "dm3";
						break;
					case "Equus_caballus":
						spName = "equCab2";
						break;
					case "Gallus_gallus":
						spName = "galGal4";
						break;
					case "Gasterosteus_aculeatus":
						spName = "gasAcu1";
						break;
					case "Meleagris_gallopavo":
						spName = "melGal1";
						break;
					case "Monodelphis_domestica":
						spName = "monDom5";
						break;
					case "Mus_musculus":
						spName = "micMur1";
						break;
					case "Ornithorhynchus_anatinus":
						spName = "ornAna1";
						break;
					case "Oryctolagus_cuniculus":
						spName = "oryCun2";
						break;
					case "Oryzias_latipes":
						spName = "oryLat2";
						break;
					case "Pan_troglodytes":
						spName = "panTro4";
						break;
					case "Pongo_abelii":
						spName = "ponAbe2";
						break;
					case "Rattus_norvegicus":
						spName = "rn6";
						break;
					case "Saccharomyces_cerevisiae":
						spName = "sacCer3";
						break;
					case "Sus_scrofa":
						spName = "susScr3";
						break;
					case "Taeniopygia_guttata":
						spName = "taeGut2";
						break;
					case "Tetraodon_nigroviridis":
						spName = "tetNig2";
						break;
						
					default:
						spName = "Not Exist";
						break;
				}
				
				if (spName != "Not Exist"){
					URL ucscgb = new URL("http://genome.cse.ucsc.edu/cgi-bin/das/"+spName+"/dna?segment=chr"+genomeChr+":"+genomeStart+","+genomeEnd);
					
					InputStreamReader inputPage = new InputStreamReader(ucscgb.openStream());
					BufferedReader in = new BufferedReader(inputPage);
					while ((inputLine = in.readLine()) != null){
						seq = charp.matcher(inputLine);
						b = seq.matches();
						if(!b){
							seqLine = seqLine + inputLine.toUpperCase();
						}
					} 
					in.close();
					
				} else {
					showString("The species name is not recognized by UCSC Genome Browser, please check again.");
				}		
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
		return seqLine;
		}
	
	private void gseqUCSCGB() throws ScriptException{
		boolean checkGseqCommand = checkGseqCommandEnsembl();
		boolean b = false;
		Pattern chrp = Pattern.compile("[a-zA-Z]+");
		Matcher chrstyle = null;
		// boolean checkGseqCommand = true;
		if (!checkGseqCommand) {
			error(ERROR_cannotFindGenomeSeq);
			return;
		} else {
			// refresh parameters
			spName = (String) statement[2].value;
			chrstyle = chrp.matcher(String.valueOf(statement[4].value));
			b = chrstyle.matches();
			if (b)
				genomeChr = String.valueOf(statement[4].value);  
			else
				genomeChr = String.valueOf(statement[4].intValue);
			b = false;
			genomeStart = statement[6].intValue;
			genomeEnd = statement[8].intValue;
			
			//added lxq35
			if (genomeEnd - genomeStart > 190000000) {
				error(ERROR_seqTooLong);
			}
			
			String seqTAG = ">" + spName + " | CHR" + genomeChr + " : "
					+ genomeStart + "-" + genomeEnd;
			showString(seqTAG);
			showString("Loading genome sequence from UCSC Genome Database...");
			showString(formFASTA(gseqUCSC(spName, genomeChr, genomeStart, genomeEnd)));
			showString("Loading completed.");
			showString("-------------------------");
		}
	}
	
	private void gseqBlast() throws ScriptException{
		new BLASTThread();
		threadStop = false;
	}
	
	class BLASTThread implements Runnable {
		Thread BLASt;
		BLASTThread() {
		   BLASt = new Thread(this, "BLAST Thread");
		   BLASt.start();
		   }
		
		@Override
		public void run(){
			   String seqLine = null;
			   boolean checkGseqCommand = checkGseqCommandEnsembl();
			   boolean b = false;
			   Pattern chrp = Pattern.compile("[a-zA-Z]+");
			   Matcher chrstyle = null;
			   if (!checkGseqCommand) {
				   showString("Cannot find specified genome sequence");
				   return;
			   } else {
	  			// refresh parameters
				   spName = (String) statement[2].value;
				   chrstyle = chrp.matcher(String.valueOf(statement[4].value));
				   b = chrstyle.matches();
				   if (b)
					   genomeChr = String.valueOf(statement[4].value);  
				   else
					   genomeChr = String.valueOf(statement[4].intValue);
				   b = false;
				   genomeStart = statement[6].intValue;
				   genomeEnd = statement[8].intValue;
	  			
	  			//added lxq35
				   if (genomeEnd - genomeStart > 10000) {
					   showString("Input sequence is too long to handle, please choose a shorter one.");
					   return;
				   }
				 String BLAST_OUTPUT_FILE = "blastOutput."+"_" + spName+ "_" + genomeChr+"_"+genomeStart+ "_"+genomeEnd+"_" + "txt";
	  			 try {
					 seqLine = gseqUCSC(spName, genomeChr, genomeStart, genomeEnd);
					 showString(formFASTA(seqLine));
	  			 } catch (ScriptException e1) {
	  				 // TODO Auto-generated catch block
	  				 e1.printStackTrace();
	  			 }
	  			
	  			 NCBIQBlastService service = new NCBIQBlastService();
	  					
	  			// set alignment options
	  			 NCBIQBlastAlignmentProperties props = new NCBIQBlastAlignmentProperties();
	  			 props.setBlastProgram(BlastProgramEnum.blastn);
	  			 props.setBlastDatabase("nr");
	  			
	  			 // set output options
	  			 NCBIQBlastOutputProperties outputProps = new NCBIQBlastOutputProperties();
	  			 outputProps.setOutputOption(FORMAT_TYPE, BlastOutputFormatEnum.Text.name());			 
	  			 String rid = null;          // blast request ID
	  			 FileWriter writer = null;
	  			 BufferedReader reader = null;
	  			 try {
	  				 // send blast request and save request id
	  				 rid = service.sendAlignmentRequest(seqLine, props);
	  				 // wait until results become available. Alternatively, one can do other computations/send other alignment requests
	  				 while (!service.isReady(rid)) {
	  					 if(threadStop == true) {
	  						 showString("NCBI Blast"+ spName+ "_" + genomeChr+"_"+genomeStart+ "_"+genomeEnd+ "stopped.");
	  						 return;
	  					 }
	  					Thread.sleep(10000);
//	  					showString("Have been Waiting for Blasting results for " + 10  + " seconds.");
	  					 
	  				}
	  				// read results when they are ready
	  				InputStream blastresult = service.getAlignmentResults(rid, outputProps);
	  				reader = new BufferedReader(new InputStreamReader(blastresult));
	  				// write blast output to specified file
	  				File f = new File(BLAST_OUTPUT_FILE);
	  				showString("Saving blast results in file " + f.getAbsolutePath());
	  				writer = new FileWriter(f);
	  				String line;
	  				while ((line = reader.readLine()) != null) {
	  					writer.write(line + System.getProperty("line.separator"));
	  				}
	  				showString("");
	  				showString("Blast completed.");
	  				showString("-------------------------");
	  			 	} catch (Exception e) {
	  				System.out.println(e.getMessage());
	  				e.printStackTrace();
	  				} finally {
	  				// clean up
	  				IOUtils.close(writer);
	  				IOUtils.close(reader);
	  				// delete given alignment results from blast server (optional operation)
	  				service.sendDeleteRequest(rid);
	  				}
	  			 }
			   }
	}
	
	private void gseqTranscribe() throws ScriptException{
		String seqLine = null;
		boolean checkGseqCommand = checkGseqCommandEnsembl();
		boolean b = false;
		Pattern chrp = Pattern.compile("[a-zA-Z]+");
		Matcher chrstyle = null;
		// boolean checkGseqCommand = true;
		if (!checkGseqCommand) {
			error(ERROR_cannotFindGenomeSeq);
			return;
		} else {
			// refresh parameters
			spName = (String) statement[2].value;
			chrstyle = chrp.matcher(String.valueOf(statement[4].value));
			b = chrstyle.matches();
			if (b)
				genomeChr = String.valueOf(statement[4].value);  
			else
				genomeChr = String.valueOf(statement[4].intValue);
			b = false;
			genomeStart = statement[6].intValue;
			genomeEnd = statement[8].intValue;
			
			seqLine = gseqUCSC(spName, genomeChr, genomeStart, genomeEnd);
			showString("DNA Sequence: " + formFASTA(seqLine));		
			DNASequence seqDNA = new DNASequence(seqLine);
			RNASequence seqRNA =(seqDNA).getRNASequence();
			showString("RNA Sequence: " + formFASTA(seqRNA.getSequenceAsString()));
			ProteinSequence seqProtein = seqRNA.getProteinSequence();
			showString("Protein Sequence: " + formFASTA(seqProtein.getSequenceAsString()));
			showString("");
			showString("Transcribe completed.");
			showString("-------------------------");
		}
	}
	
	private void gseqGene() throws ScriptException, NumberFormatException{
		boolean checkGseqCommand = checkGseqCommandEnsembl();
		boolean b = false;
		Pattern chrp = Pattern.compile("[a-zA-Z]+");
		Matcher chrstyle = null;
		if (!checkGseqCommand) {
			error(ERROR_cannotFindGenomeSeq);
			return;
		} else {
			// refresh parameters
			spName = (String) statement[2].value;
			if(!spName.equals("Homo_sapiens")){
				 error(ERROR_cannotSpecificSpecies);
				 return;
			}
			chrstyle = chrp.matcher(String.valueOf(statement[4].value));
			b = chrstyle.matches();
			if (b)
				genomeChr = String.valueOf(statement[4].value);  
			else
				genomeChr = String.valueOf(statement[4].intValue);
			b = false;
			genomeStart = statement[6].intValue;
			genomeEnd = statement[8].intValue;
			
			showString(formFASTA(gseqUCSC(spName, genomeChr, genomeStart, genomeEnd)));

			String tmp = "";
			int geneNum = 0;
			BufferedReader br = null;
//			br = new BufferedReader(new FileReader("src/org/openscience/jmol/app/sources/knownGene.txt"));
//			br = new BufferedReader(new FileReader("src\\org\\openscience\\jmol\\app\\sources\\knownGene.txt"));
//			URL url = this.getClass().getResource("/knownGene");			
//			URL url = this.getClass().getClassLoader().getResource("knownGene");
			
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("knownGene");
			InputStreamReader reader = new InputStreamReader(in);
			br = new BufferedReader(reader);
			
			reader = null;
			in = null;
//			String path = (url.getPath());
//			showString(path);
//			path = path.replace("!", "");
//			showString(path);
//			path = path.replace("file:", "");
//			showString(path);
////			showString(url.getPath());
			try {
				while ((tmp = br.readLine()) != null) {
					String[] x = tmp.split("\t");
					int exonsize = Integer.parseInt(x[7]);
					String[] exonStart = x[8].split(",");
					String[] exonEnd = x[9].split(",");
					String geneInfo = x[0];
					String chr = x[1];
					int txStart = Integer.parseInt(x[3]);
					int txEnd = Integer.parseInt(x[4]);
					int cdsStart = Integer.parseInt(x[5]);
					int cdsEnd = Integer.parseInt(x[6]);
					String getScript = null;
					if (((txStart > genomeStart && txStart < genomeEnd) || (txEnd > genomeStart && txEnd < genomeEnd))
							&& (chr.equals("chr"+ genomeChr))) {
						showString("Gene Name " + geneInfo + ":");
						geneNum++;
						for (int i = 0; i < exonsize; i++) {
							if (x[2].equals("+")) {// forward strand
								showString(exonStart[i] + "\t" + exonEnd[i]
										+ "\t" + "Exon" + (i + 1));
								if (exonsize > 0 && i < exonsize - 1) {// having
																		// intron
									showString(exonEnd[i] + "\t"
											+ exonStart[i + 1] + "\t" + "Intron"
											+ (i + 1));
								}
							} else {
								showString(exonStart[i] + "\t" + exonEnd[i]
										+ "\t" + "Exon" + (exonsize - i));
								if (exonsize > 0 && i < exonsize - 1) {// having
																		// intron
									showString(exonEnd[i] + "\t"
											+ exonStart[i + 1] + "\t" + "Intron"
											+ (exonsize - i - 1));
								}
							}
						}
						
						showString("Transcription start position: "
								+ txStart);
						showString("Transcription end position: " + txEnd);
						
						getScript = "sselect "+ genomeChr + "," + txStart + "," + txStart + ";" + "label TranscriptionStartPosition" + ";" + "color label red" + ";";
						viewer.script(getScript);
						
						getScript = "sselect "+ genomeChr + "," + txEnd + "," + txEnd + ";" + "label TranscriptionEndPosition" + ";" + "color label red" + ";";
						viewer.script(getScript);
						
						getScript = "sselect "+ genomeChr + "," + txStart + "," + txEnd + ";" + "color red" + ";";
						viewer.script(getScript);
						
						if (cdsStart != cdsEnd) { // having 3'UTR or 5'UTR
							if (x[2].equals("+")) {
								if (txStart != cdsStart){
									showString("Coding region start:  "+ x[3] + "\t" + (cdsStart - 1) + "\t" + "5'UTR");
									getScript = "sselect "+ genomeChr + ","+ x[3]+ "," + x[3] + ";" + "label 5'UTR" + ";" + "color label yellow" + ";";
									viewer.script(getScript);
									getScript = "sselect "+ genomeChr + "," + x[3] + "," + (cdsStart - 1) + ";" + "color yellow" + ";";
									viewer.script(getScript);
								}
								
								if (txEnd != cdsEnd){
									showString("Coding region end:  " + (cdsEnd + 1) + "\t" + x[4] + "\t" + "3'UTR");						
									getScript = "sselect "+ genomeChr + "," + (cdsEnd + 1) + "," + (cdsEnd + 1) + ";" + "label 3'UTR" + ";" + "color label yellow" + ";";
									viewer.script(getScript);
									getScript = "sselect "+ genomeChr + "," + (cdsEnd + 1) + "," + x[4] + ";" + "color yellow" + ";";
									viewer.script(getScript);
								}
												

							} else {
								if (txStart != cdsStart){
									showString("Coding region start:  " + x[3] + "\t" + (cdsStart - 1) + "\t" + "3'UTR");
									getScript = "sselect "+ genomeChr + "," + x[3] + "," + x[3] + ";" + "label 3'UTR" + ";" + "color label yellow" + ";";
									viewer.script(getScript);
									getScript = "sselect "+ genomeChr + "," + x[3] + "," + (cdsStart - 1) + ";" + "color yellow" + ";";
									viewer.script(getScript);
								}
								if (txEnd != cdsEnd){
									showString("Coding region end:  " + (cdsEnd + 1) + "\t" +  x[4] + "\t" + "5'UTR");
									getScript = "sselect " + genomeChr + "," + (cdsEnd + 1) + "," + (cdsEnd + 1) + ";" + "label 5'UTR" + ";" + "color label yellow" + ";";
									viewer.script(getScript);
									getScript = "sselect "+ genomeChr + "," + (cdsEnd + 1) + "," + x[4] + ";" + "color yellow" + ";";
									viewer.script(getScript);
								}		
							}
						}
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			showString("There are " + geneNum
					+ " Genes in this sequence");
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			showString("Loading completed.");
			showString("-------------------------");
		}
	}
	
	private void gseqProp() throws ScriptException{
		String seqLine = null;
		boolean checkGseqCommand = checkGseqCommandEnsembl();
		boolean b = false;
		Pattern chrp = Pattern.compile("[a-zA-Z]+");
		Matcher chrstyle = null;
		// boolean checkGseqCommand = true;
		if (!checkGseqCommand) {
			error(ERROR_cannotFindGenomeSeq);
			return;
		} else {
			// refresh parameters
			spName = (String) statement[2].value;
			chrstyle = chrp.matcher(String.valueOf(statement[4].value));
			b = chrstyle.matches();
			if (b)
				genomeChr = String.valueOf(statement[4].value);  
			else
				genomeChr = String.valueOf(statement[4].intValue);
			b = false;
			genomeStart = statement[6].intValue;
			genomeEnd = statement[8].intValue;
			
			seqLine = gseqUCSC(spName, genomeChr, genomeStart, genomeEnd);
			showString("DNA Sequence: " + formFASTA(seqLine));
			
			int a = 0, c = 0, g = 0, t = 0;
			double mw = 0, tm = 0, satm = 0;
			long ec = 0;
			int AA = 0,  AC= 0,  AT= 0,  AG= 0,  CC= 0,  CA= 0,  CT= 0,  CG= 0,  TT= 0,  TA= 0,  TC= 0,  TG= 0,  GA= 0,  GG= 0,  GC= 0,  GT= 0;
			if (seqLine.length() <= 0){
				showString("Selected input sequence length equal or less than 0");
				return;
			}
			for(int i=0; i<seqLine.length(); i++){
				char nucleotide  = seqLine.charAt(i);
				switch (nucleotide){
					case 'A': a++; break;
					case 'C': c++; break;
					case 'G': g++; break;
					case 'T': t++; break;
					default: break;
				}
			}
			
			for(int i=0; i<seqLine.length() - 1; i++){
				char nucleotide1  = seqLine.charAt(i);
				char nucleotide2  = seqLine.charAt(i+1);
				if (nucleotide1 == 'A' && nucleotide2 == 'A'){
					AA++;
				}else if (nucleotide1 == 'A' && nucleotide2 == 'C'){
					AC++;
				}else if (nucleotide1 == 'A' && nucleotide2 == 'G'){
					AG++;
				}else if (nucleotide1 == 'A' && nucleotide2 == 'T'){
					AT++;
				}else if (nucleotide1 == 'C' && nucleotide2 == 'A'){
					CA++;
				}else if (nucleotide1 == 'C' && nucleotide2 == 'G'){
					CG++;
				}else if (nucleotide1 == 'C' && nucleotide2 == 'C'){
					CC++;
				}else if (nucleotide1 == 'C' && nucleotide2 == 'T'){
					CT++;
				}else if (nucleotide1 == 'T' && nucleotide2 == 'T'){
					TT++;
				}else if (nucleotide1 == 'T' && nucleotide2 == 'G'){
					TG++;
				}else if (nucleotide1 == 'T' && nucleotide2 == 'A'){
					TA++;
				}else if (nucleotide1 == 'T' && nucleotide2 == 'C'){
					TC++;
				}else if (nucleotide1 == 'G' && nucleotide2 == 'G'){
					GG++;
				}else if (nucleotide1 == 'G' && nucleotide2 == 'A'){
					GA++;
				}else if (nucleotide1 == 'G' && nucleotide2 == 'C'){
					GC++;
				}else if (nucleotide1 == 'G' && nucleotide2 == 'T'){
					GT++;
				}
			}
			
			//length
			showString("Sequence Length: " + seqLine.length());
			
			//Molecular Weight
			mw = (a*313.21) + (t*304.2) + (c*289.18) + (g*329.21) - 61.96;
			showString("Molecular Weight: " + mw + " g/mole");
			
			//GC content
			showString("GC content: " + (100*(c+g)/(a+c+t+g)) + "%");
			
			//Melting Temperature
			if(seqLine.length() < 14){
				tm = (a + t)*2 + (c + g)*4;
				satm = (a+t)*2 + (g+c)*4 - 16.6*(Math.log10(0.050)) + 16.6*Math.log10(0.05);
			} else{
				tm = 64.9 + 41*(g + c -16.4)/(a + t + g + c);
				satm= 100.5 + (41 * (g+c)/(a+t+g+c)) - (820/(a+t+g+c)) + 16.6*Math.log10(0.05);
			}
			showString("Melting Temperature: " + ((int)(tm*100))/(float)100 + " C");
			showString("Salt Adjusted Melting Temperature: " + ((int)(satm*100))/(float)100 + " C");

			//Extinction Coefficient
			ec = (27400*AA + 21200*AC + 25000*AG + 22800*AT + 21200 *CA + 14600*CC + 18000*CG + 15200*CT + 25200*GA + 17600*GC + 21600*GG + 20000*GT + 23400*TA + 16200*TC + 19000*TG + 16800*TT);
			char start = seqLine.charAt(0);
			char end = seqLine.charAt(seqLine.length() - 1);
			if (start == 'A'){
				a--;			
			}else if(start == 'C'){
				c--;
			}else if(start == 'T'){
				t--;
			}else if(start == 'G'){
				g--;
			}
			if (end == 'A'){
				a--;			
			}else if(end == 'C'){
				c--;
			}else if(end == 'T'){
				t--;
			}else if(end == 'G'){
				g--;
			}
			ec = ec - 15400*a - 7400*c - 8700*t - 11500*g;
			showString("Extinction Coefficient: " + ec + " L/(mole·cm)");
			
			//nmole/OD260
			//µg/OD260
			
			showString("nmole/OD260: " + ((int)((Math.pow(10,6)/ec)*100))/(float)100 + " nmole");	
			showString("ug/OD260: " + ((int)((mw*Math.pow(10,3)/ec)*100))/(float)100 + " ug");	
			showString("");
			showString("Properties displayed.");
			showString("-------------------------");
			
			a = 0; c = 0; g = 0; t = 0; mw = 0; tm = 0; satm = 0; ec = 0;
			AA = 0;  AC= 0;  AT= 0; AG= 0; CC= 0; CA= 0; CT= 0; CG= 0; TT= 0; TA= 0; TC= 0; TG= 0; GA= 0; GG= 0; GC= 0; GT= 0;
			
		}
	}
	
	private String formFASTA(String genomeSeq) throws ScriptException {
		int fa_len = 60;
		genomeSeq.replace("\n", "");
		int allLen = genomeSeq.length();
		String newString = "";
		if (allLen == 0) {
			return "Loading sequence failed!\n";
		} else if (allLen <= fa_len) {
			return genomeSeq;
		} else if (allLen > fa_len) {
			while (genomeSeq.length() > fa_len) {
				newString = newString + genomeSeq.substring(0, fa_len) + "\n";
				genomeSeq = genomeSeq.substring(fa_len, genomeSeq.length());
			}
			newString = newString + genomeSeq + "\n";
			return newString;
		} else {
			return null;
		}
	}

	private void extractPDB() throws ScriptException {
		if (statementLength != 2)
			return;
		Atom[] currentUnits = viewer.getModelSet().atoms;
		String atomHead = "ATOM  ";
		int serialNum = 1;
		String atomName = "  C   ";
		String resName = "UNK ";
		char chainChar = 'A';
		int resCount = 0;
		// xpos, ypos, zpos
		String occupacy = "  1.00";
		String tempFactor = " 86.08";
		String lineEnd = "           C ";
		List<String> pdbList = new ArrayList<String>();
		for (int i = 0; i < currentUnits.length; i++) {
			resCount++;
			Atom oneUnit = currentUnits[i];
			BitSet selectedBS = viewer.getSelectedBS();
			if ((selectedBS.cardinality() > 0 && selectedBS.get(i))
					|| selectedBS.cardinality() == 0) {
				if (resCount > 9999) {
					resCount = 1;
					serialNum++;
					int chainNum = chainChar;
					chainNum++;
					chainChar = (char) chainNum;
				}
				float unitX = oneUnit.x;
				float unitY = oneUnit.y;
				float unitZ = oneUnit.z;
				// get formated resCount, xPos, yPos, zPos
				int resCountLen = Integer.toString(resCount).length();
				String resCountString = Integer.toString(resCount);
				if (resCountLen < 4) {
					for (int j = 1; j <= 4 - resCountLen; j++) {
						resCountString = " " + resCountString;
					}
				}
				String formatX = xyzPDBFormat(unitX);
				String formatY = xyzPDBFormat(unitY);
				String formatZ = xyzPDBFormat(unitZ);

				String PDBLine = atomHead + "    " + String.valueOf(serialNum)
						+ atomName + resName + chainChar + resCountString
						+ "    " + formatX + formatY + formatZ + occupacy
						+ tempFactor + lineEnd;
				if (PDBLine != "")
					pdbList.add(PDBLine);
			}
		}
		// write out PDBLine List into a specified file
		if (pdbList.size() > 0) {
			File pdbFile = new File(String.valueOf(statement[1].value));
			if (!pdbFile.exists()) {
				try {
					pdbFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				FileWriter fileWriter = new FileWriter(pdbFile);
				for (String pdbLine : pdbList) {
					fileWriter.write(pdbLine);
					fileWriter.write("\n");
				}
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			showString("Extracting PDB done!");
		} else {

		}
	}

	// format the double number into %8.3 format
	private String xyzPDBFormat(float p) throws ScriptException {
		int pLen = String.valueOf(p).length();
		int pIntLen = String.valueOf(new Double(p).intValue()).length();
		if (pLen <= 8 && pLen > 0) {
			if (pLen == 8) {
				return String.valueOf(p);
			} else {
				String formatXYZ = String.valueOf(p);
				for (int i = 1; i <= 8 - pLen; i++) {
					formatXYZ = " " + formatXYZ;
				}
				return formatXYZ;
			}
		} else if (pIntLen <= 8 && pIntLen > 0) {
			String formatXYZ = String.valueOf(p).substring(8);
			return formatXYZ;
		} else {
			error(ERROR_xyzInvalid);
			return null;
		}
	}

	private void scaledown() throws ScriptException {
		Map<String, Object> htParams = new Hashtable<String, Object>();
		boolean isAppend = false;
		int currentSelectedScale = 0;

		// load current atomsetcollection
		Atom[] currentUnits = viewer.getModelSet().atoms;

		String fileName = viewer.getFullPathName();
		String downFileName = null;

		Pattern patternGssType = Pattern.compile("\\.gss$");
		Matcher checkGssType = patternGssType.matcher(fileName);

		//Pattern patternGpdbType = Pattern.compile("\\.pdb$");
		//Matcher checkGpdbType = patternGpdbType.matcher(fileName);

		setCursorWait(true);// for mouse to wait
		// for Gss type files
		if (checkGssType.find()) {
			Pattern patternGss = Pattern.compile("\\.gs\\.gss");
			Pattern patternCrs = Pattern.compile("\\.cs\\.gss");
			Pattern patternLcs = Pattern.compile("\\.ls\\.gss");
			Pattern patternFbs = Pattern.compile("(\\.chr)([a-z\\d]+)(\\.fs\\.gss)");

			Matcher gssMatcher = patternGss.matcher(fileName);
			Matcher crsMatcher = patternCrs.matcher(fileName);
			Matcher lcsMatcher = patternLcs.matcher(fileName);
			Matcher fbsMatcher = patternFbs.matcher(fileName);

			// locate next-scale file name
			if (gssMatcher.find()) {
				downFileName = gssMatcher.replaceAll(".cs.gss");
				downFileName = downFileName.replace("global", "chromosome");
				currentSelectedScale = 1;
			} else if (crsMatcher.find()) {
				downFileName = crsMatcher.replaceAll(".ls.gss");
				downFileName = downFileName.replace("chromosome", "loci");
				currentSelectedScale = 2;
			} else if (lcsMatcher.find()) {
				downFileName = lcsMatcher.replaceAll(".fs.gss");
				downFileName = downFileName.replace("loci", "fiber");
				currentSelectedScale = 3;
			} else if (fbsMatcher.find()) {
				downFileName = fbsMatcher.replaceAll(".ns.gss");//needs to be modified
				downFileName = downFileName.replace("fiber", "ncp");
				currentSelectedScale = 4;
			}
			
			BitSet selectedBS = viewer.getSelectedBS();
			if (selectedBS.cardinality() == 1) {
				int currentSelectedUnit = 0;
				for (int i = 0; i < selectedBS.length(); i++) {
					if (selectedBS.get(i)) {
						currentSelectedUnit = i + 1;
					}
				}
				Atom currentSelectedUnitInfo = currentUnits[currentSelectedUnit - 1];

				// record the selected history
				switch (currentSelectedScale) {
				case 1:
					selectedPath[1] = currentSelectedUnitInfo.chrScaleNumber;
					selectedPath[2] = 0;
					selectedPath[3] = 0;
					selectedPath[4] = 0;
					break;
				case 2:
					selectedPath[1] = currentSelectedUnitInfo.chrScaleNumber;
					selectedPath[2] = currentSelectedUnitInfo.lociScaleNumber;
					selectedPath[3] = 0;
					selectedPath[4] = 0;
					break;
				case 3:
					// find the path to current selected unit
					selectedPath[1] = currentSelectedUnitInfo.chrScaleNumber;
					selectedPath[2] = currentSelectedUnitInfo.lociScaleNumber;
					selectedPath[3] = currentSelectedUnitInfo.fiberScaleNumber;
					selectedPath[4] = 0;
					break;
				case 4:
					selectedPath[1] = currentSelectedUnitInfo.chrScaleNumber;
					selectedPath[2] = currentSelectedUnitInfo.lociScaleNumber;
					selectedPath[3] = currentSelectedUnitInfo.fiberScaleNumber;
					selectedPath[4] = currentSelectedUnitInfo.nucleoScaleNumber;
					break;
				}

				if (currentSelectedScale == 1) {
					// for global structure->chromosome structure, name special
					//String chrNum = String.valueOf(currentSelectedUnit);
					String chrNum = String.valueOf(currentSelectedUnitInfo.chrScaleNumber);
					String speChr = ".chr" + chrNum + ".cs.gss";
					downFileName = downFileName.replace(".cs.gss", speChr);
				} else if (currentSelectedScale == 2) {
					String chrNum = String.valueOf(selectedPath[1]);
					Pattern chrPattern = Pattern.compile("(\\.chr)([a-z\\d]+)(\\.ls\\.gss)");
					Matcher chrMatcher = chrPattern.matcher(downFileName);
					String speChr = "";
					if (chrMatcher.find()) {
						speChr = chrMatcher.group(1) + chrNum + chrMatcher.group(3);
						downFileName = chrMatcher.replaceAll(speChr);
					}
				} else if (currentSelectedScale == 3) {
					String chrNum = String.valueOf(selectedPath[1]);
					Pattern chrPattern = Pattern.compile("(\\.chr)([a-z\\d]+)(\\.fs\\.gss)");
					Matcher chrMatcher = chrPattern.matcher(downFileName);
					String speChr = "";
					if (chrMatcher.find()) {
						speChr = chrMatcher.group(1) + chrNum + chrMatcher.group(3);
						downFileName = chrMatcher.replaceAll(speChr);
					}
				} else if (currentSelectedScale == 4) {
					//for fiber -> NCP scale, name special
					Pattern chrPattern = Pattern.compile("(\\.chr)([a-z\\d]+)(\\.ns\\.gss)");
					Matcher chrMatcher = chrPattern.matcher(downFileName);
					if (chrMatcher.find()) {
						downFileName = chrMatcher.replaceAll(".ns.gss");
					}					
				}

				// check the downscale file
				//downFileName = downFileName.replace("file:", "");
				
				if (downFileName == null) {
					error(ERROR_scaleDownFileNotExist);
					return;
				} else {
					boolean isExist = new File(downFileName).exists();
					if (!isExist) {
						error(ERROR_scaleDownFileNotExist);
						return;
					}
				}

				if (currentSelectedScale >= 1 && currentSelectedScale <= 4) {
					StringBuffer loadScript = new StringBuffer(
							"load /*file*/$FILENAME$");
					htParams.put("loadScript", loadScript);
					int tokType = 0;
					viewer.setLoadStatus(true);
					viewer.loadModelFromFile(null, downFileName, null, null,
							isAppend, htParams, loadScript, tokType,
							currentSelectedScale, currentSelectedUnit,
							selectedPath, true, "down", currentUnits);
					if (currentSelectedScale == 4) {
						System.out.println("xx");
					}
					viewer.setLoadStatus(false);
				}
				else {
					error(ERROR_scaleDownFileNotExist);
					return;
				}
			} else if (selectedBS.cardinality() > 1) {
				error(ERROR_selectedTooMuchUnits);
				return;
			} else if (selectedBS.cardinality() == 0) {
				// check the downscale file if selected chrom is unkown - all
				// chroms
				if (selectedPath[1] != 0) {
					if (downFileName == null) {
						error(ERROR_scaleDownFileNotExist);
						return;
					} else {
						boolean isExist = new File(downFileName).exists();
						if (!isExist) {
							error(ERROR_scaleDownFileNotExist);
							return;
						}
					}
					if (currentSelectedScale >= 1 && currentSelectedScale <= 4) {
						StringBuffer loadScript = new StringBuffer(
								"load /*file*/$FILENAME$");
						htParams.put("loadScript", loadScript);
						int tokType = 0;
						viewer.setLoadStatus(true);
						viewer.loadModelFromFile(null, downFileName, null,
								null, isAppend, htParams, loadScript, tokType,
								currentSelectedScale, 0, selectedPath, true,
								"down", currentUnits);
						viewer.setLoadStatus(false);
					} else {
						error(ERROR_scaleDownFileNotExist);
					}
				} else {
					// for global visualization
					if (currentSelectedScale == 1) {
						Pattern chrPattern = Pattern
								.compile("(.*)(\\.cs\\.gss)");
						Matcher chrMatcher = chrPattern.matcher(downFileName);
						if (chrMatcher.find()) {
							ArrayList multiFileList = new ArrayList();
							for (Atom goThrUnit : currentUnits) {
								int chrNum = goThrUnit.sequenceNumber;
								String speChr = chrMatcher.group(1) + ".chr"
										+ chrNum + chrMatcher.group(2);
								multiFileList.add(speChr);
							}
							String[] multidownFiles = (String[]) multiFileList
									.toArray(new String[0]);
							StringBuffer loadScript = new StringBuffer(
									"load /*file*/$FILENAME$");
							htParams.put("loadScript", loadScript);
							int tokType = 0;
							viewer.setLoadStatus(true);
							viewer.loadModelFromFile(null, multidownFiles,
									null, null, isAppend, htParams, loadScript,
									tokType, currentSelectedScale, 0,
									selectedPath, true, "down", currentUnits);
							viewer.setLoadStatus(false);
						} else {
							error(ERROR_scaleDownFileNotExist);
						}
					} else if (currentSelectedScale == 2) {
						Pattern lociPattern = Pattern
								.compile("(.*chr)(\\d+)(\\.ls\\.gss)");
						Matcher lociMatcher = lociPattern.matcher(downFileName);
						if (lociMatcher.find()) {
							ArrayList multiFileList = new ArrayList();
							for (Atom goThrUnit : currentUnits) {
								int chrNum = goThrUnit.chrScaleNumber;
								String speChr = lociMatcher.group(1) + chrNum
										+ lociMatcher.group(3);
								boolean haveSame = false;
								for (String tmpFileName : (String[]) multiFileList
										.toArray(new String[0])) {
									if (tmpFileName.equals(speChr)) {
										haveSame = true;
										break;
									}
								}
								if (!haveSame) {
									multiFileList.add(speChr);
								}
							}
							String[] multidownFiles = (String[]) multiFileList
									.toArray(new String[0]);
							StringBuffer loadScript = new StringBuffer(
									"load /*file*/$FILENAME$");
							htParams.put("loadScript", loadScript);
							int tokType = 0;
							viewer.setLoadStatus(true);
							viewer.loadModelFromFile(null, multidownFiles,
									null, null, isAppend, htParams, loadScript,
									tokType, currentSelectedScale, 0,
									selectedPath, true, "down", currentUnits);
							viewer.setLoadStatus(false);
						}

					} else if (currentSelectedScale == 3) {
						Pattern lociPattern = Pattern
								.compile("(.*chr)(\\d+)(\\.fs\\.gss)");
						Matcher lociMatcher = lociPattern.matcher(downFileName);
						if (lociMatcher.find()) {
							ArrayList multiFileList = new ArrayList();
							for (Atom goThrUnit : currentUnits) {
								int chrNum = goThrUnit.chrScaleNumber;
								String speChr = lociMatcher.group(1) + chrNum
										+ lociMatcher.group(3);
								boolean haveSame = false;
								for (String tmpFileName : (String[]) multiFileList
										.toArray(new String[0])) {
									if (tmpFileName.equals(speChr)) {
										haveSame = true;
										break;
									}
								}
								if (!haveSame) {
									multiFileList.add(speChr);
								}
							}
							String[] multidownFiles = (String[]) multiFileList
									.toArray(new String[0]);
							StringBuffer loadScript = new StringBuffer(
									"load /*file*/$FILENAME$");
							htParams.put("loadScript", loadScript);
							int tokType = 0;
							viewer.setLoadStatus(true);
							viewer.loadModelFromFile(null, multidownFiles,
									null, null, isAppend, htParams, loadScript,
									tokType, currentSelectedScale, 0,
									selectedPath, true, "down", currentUnits);
							viewer.setLoadStatus(false);
						}

					}
					else if (currentSelectedScale > 3) {
						error(ERROR_scaleDownFileNotExist);
					}

				}
				return;
			}
		} else {
			error(ERROR_scaleDownFileNotExist);
			return;
		}
	}

	private void scaleup() throws ScriptException {
		Map<String, Object> htParams = new Hashtable<String, Object>();
		boolean isAppend = false;
		int currentSelectedScale = 0;

		String fileName = viewer.getFullPathName();
		String upFileName = null;

		Atom[] currentUnits = viewer.getModelSet().atoms;

		Pattern patternGssType = Pattern.compile("\\.gss$");
		Matcher checkGssType = patternGssType.matcher(fileName);

		//Pattern patternGpdbType = Pattern.compile("\\.pdb$");
		//Matcher checkGpdbType = patternGpdbType.matcher(fileName);
		setCursorWait(true);// for mouse to wait

		if (checkGssType.find()) {
			Pattern patternCrs = Pattern.compile("(\\.chr)([a-z\\d]+)(\\.cs\\.gss)");
			Pattern patternLcs = Pattern.compile("\\.ls\\.gss");
			Pattern patternFbs = Pattern.compile("\\.fs\\.gss");
			Pattern patternNus = Pattern.compile("\\.ns\\.gss");//-to modify

			Matcher crsMatcher = patternCrs.matcher(fileName);
			Matcher lcsMatcher = patternLcs.matcher(fileName);
			Matcher fbsMatcher = patternFbs.matcher(fileName);
			Matcher nusMatcher = patternNus.matcher(fileName);

			if (nusMatcher.find()) {
				upFileName = nusMatcher.replaceAll(".fs.gss");
				upFileName = upFileName.replace("ncp", "fiber");
				currentSelectedScale = 5;
			} else if (fbsMatcher.find()) {
				upFileName = fbsMatcher.replaceAll(".ls.gss");
				upFileName = upFileName.replace("fiber", "loci");
				currentSelectedScale = 4;
			} else if (lcsMatcher.find()) {
				upFileName = lcsMatcher.replaceAll(".cs.gss");
				upFileName = upFileName.replace("loci", "chromosome");
				currentSelectedScale = 3;
			} else if (crsMatcher.find()) {
				upFileName = crsMatcher.replaceAll(".gs.gss");
				upFileName = upFileName.replace("chromosome", "global");
				currentSelectedScale = 2;
			}
			BitSet selectedBS = viewer.getSelectedBS();
			if (currentSelectedScale == 5) { 
				//for nucleosome level, just return to the previous structure - no matter whether selected - use previous selectedPath
				//first step - get the upfile Name
				selectedPath[4] = 0;
				String chrNum = String.valueOf(selectedPath[1]);
				Pattern chrPattern = Pattern.compile("\\.fs\\.gss");
				Matcher chrMatcher = chrPattern.matcher(upFileName);
				String speChr = "";
				if (chrMatcher.find()) {
					speChr = ".chr" + chrNum + ".fs.gss";
					upFileName = chrMatcher.replaceAll(speChr); 
				}
				
				//second step - load structure based on previous selectedPath
				StringBuffer loadScript = new StringBuffer(
						"load /*file*/$FILENAME$");
				htParams.put("loadScript", loadScript);
				int tokType = 0;
				viewer.setLoadStatus(true);
				viewer.loadModelFromFile(null, upFileName, null, null,
						isAppend, htParams, loadScript, tokType,
						currentSelectedScale, 0, selectedPath, true, "up",
						currentUnits);
				viewer.setLoadStatus(false);
				
			}
			else if (selectedBS.cardinality() == 1) {
				int currentSelectedUnit = 0;
				for (int i = 0; i < selectedBS.length(); i++) {
					if (selectedBS.get(i)) {
						currentSelectedUnit = i + 1;
					}
				}
				Atom currentSelectedUnitInfo = currentUnits[currentSelectedUnit - 1];

				switch (currentSelectedScale) {
				case 5:
					selectedPath[4] = 0;
					selectedPath[3] = currentSelectedUnitInfo.fiberScaleNumber;
					selectedPath[2] = currentSelectedUnitInfo.lociScaleNumber;
					selectedPath[1] = currentSelectedUnitInfo.chrScaleNumber;
					break;
				case 4:
					selectedPath[4] = 0;
					selectedPath[3] = 0;
					selectedPath[2] = currentSelectedUnitInfo.lociScaleNumber;
					selectedPath[1] = currentSelectedUnitInfo.chrScaleNumber;
					break;
				case 3:
					selectedPath[4] = 0;
					selectedPath[3] = 0;
					selectedPath[2] = 0;
					selectedPath[1] = currentSelectedUnitInfo.chrScaleNumber;
					
					break;
				case 2:
					selectedPath[4] = 0;
					selectedPath[3] = 0;
					selectedPath[2] = 0;
					selectedPath[1] = 0;
					break;
				}
				
				//for global visualization - change the upfilename based on unit selected
				if (currentSelectedScale == 4) {
					String chrNum = String.valueOf(selectedPath[1]);
					Pattern chrPattern = Pattern
							.compile("(\\.chr)([a-z\\d]+)(\\.ls\\.gss)");
					Matcher chrMatcher = chrPattern.matcher(upFileName);
					String speChr = "";
					if (chrMatcher.find()) {
						speChr = chrMatcher.group(1) + chrNum
								+ chrMatcher.group(3);
						upFileName = chrMatcher.replaceAll(speChr); 
					}
				}
				else if (currentSelectedScale == 3) {
					String chrNum = String.valueOf(selectedPath[1]);
					Pattern chrPattern = Pattern
							.compile("(\\.chr)([a-z\\d]+)(\\.cs\\.gss)");
					Matcher chrMatcher = chrPattern.matcher(upFileName);
					String speChr = "";
					if (chrMatcher.find()) {
						speChr = chrMatcher.group(1) + chrNum
								+ chrMatcher.group(3);
						upFileName = chrMatcher.replaceAll(speChr);
					}
				}
				
				// check the upscale file
				//upFileName = upFileName.replace("file:", "");
				
				if (upFileName == null) {
					error(ERROR_scaleUpFileNotExist);
					viewer.setLoadStatus(false);
					return;
				} else {
					boolean isExist = new File(upFileName).exists();
					if (!isExist) {
						error(ERROR_scaleUpFileNotExist);
						return;
					}
				}

				if (currentSelectedScale >= 2 && currentSelectedScale <= 4) {
					StringBuffer loadScript = new StringBuffer(
							"load /*file*/$FILENAME$");
					htParams.put("loadScript", loadScript);
					int tokType = 0;
					viewer.setLoadStatus(true);
					viewer.loadModelFromFile(null, upFileName, null, null,
							isAppend, htParams, loadScript, tokType,
							currentSelectedScale, currentSelectedUnit,
							selectedPath, true, "up", currentUnits);
					viewer.setLoadStatus(false);
				}
				else {
					error(ERROR_scaleUpFileNotExist);
				}

				switch (currentSelectedScale) {
				// case 5:
				// selectedPath[4] = 0;
				// break;
				case 4:
					selectedPath[4] = 0;
					selectedPath[3] = 0;
					break;
				case 3:
					selectedPath[4] = 0;
					selectedPath[3] = 0;
					selectedPath[2] = 0;
					break;
				case 2:
					selectedPath[4] = 0;
					selectedPath[3] = 0;
					selectedPath[2] = 0;
					selectedPath[1] = 0;
					break;
				}
			} else if (selectedBS.cardinality() > 1) {
				error(ERROR_selectedTooMuchUnits);
				return;
			} else if (selectedBS.cardinality() == 0) {
				// check the upscale file
				if (upFileName == null) {
					error(ERROR_scaleUpFileNotExist);
					return;
				} else {
					boolean isExist = new File(upFileName).exists();
					if (!isExist) {
						error(ERROR_scaleUpFileNotExist);
						return;
					}
				}

				// no selection: means user want to see the whole chrom/genome
				if (currentSelectedScale == 5) {
					//TODO -hcf
				}
				else if (currentSelectedScale == 4) {
					Pattern lociPattern = Pattern
							.compile("(.*chr)(\\d+)(\\.ls\\.gss)");
					Matcher lociMatcher = lociPattern.matcher(upFileName);
					if (lociMatcher.find()) {
						ArrayList multiFileList = new ArrayList();
						for (Atom goThrUnit : currentUnits) {
							int chrNum = goThrUnit.chrScaleNumber;
							String speChr = lociMatcher.group(1) + chrNum
									+ lociMatcher.group(3);
							boolean haveSame = false;
							for (String tmpFileName : (String[]) multiFileList
									.toArray(new String[0])) {
								if (tmpFileName.equals(speChr)) {
									haveSame = true;
									break;
								}
							}
							if (!haveSame) {
								multiFileList.add(speChr);
							}
						}
						String[] multiUpFiles = (String[]) multiFileList
								.toArray(new String[0]);
						StringBuffer loadScript = new StringBuffer(
								"load /*file*/$FILENAME$");
						htParams.put("loadScript", loadScript);
						selectedPath[2] = 0;
						selectedPath[3] = 0;
						selectedPath[4] = 0;
						int tokType = 0;
						viewer.setLoadStatus(true);
						viewer.loadModelFromFile(null, multiUpFiles, null,
								null, isAppend, htParams, loadScript, tokType,
								currentSelectedScale, 0, selectedPath, true,
								"up", currentUnits);
						viewer.setLoadStatus(false);
					}
				} else if (currentSelectedScale == 3) {
					Pattern chrPattern = Pattern
							.compile("(.*chr)(\\d+)(\\.cs\\.gss)");
					Matcher chrMatcher = chrPattern.matcher(upFileName);
					if (chrMatcher.find()) {
						ArrayList multiFileList = new ArrayList();
						for (Atom goThrUnit : currentUnits) {
							int chrNum = goThrUnit.chrScaleNumber;
							String speChr = chrMatcher.group(1) + chrNum
									+ chrMatcher.group(3);
							boolean haveSame = false;
							for (String tmpFileName : (String[]) multiFileList
									.toArray(new String[0])) {
								if (tmpFileName.equals(speChr)) {
									haveSame = true;
									break;
								}
							}
							if (!haveSame) {
								multiFileList.add(speChr);
							}
						}
						String[] multiUpFiles = (String[]) multiFileList
								.toArray(new String[0]);
						StringBuffer loadScript = new StringBuffer(
								"load /*file*/$FILENAME$");
						htParams.put("loadScript", loadScript);
						int tokType = 0;
						selectedPath[2] = 0;
						selectedPath[3] = 0;
						selectedPath[4] = 0;
						viewer.setLoadStatus(true);
						viewer.loadModelFromFile(null, multiUpFiles, null,
								null, isAppend, htParams, loadScript, tokType,
								currentSelectedScale, 0, selectedPath, true,
								"up", currentUnits);
						viewer.setLoadStatus(false);
						
					}
				} else if (currentSelectedScale == 2) {
					selectedPath[1] = 0;
					selectedPath[2] = 0;
					selectedPath[3] = 0;
					selectedPath[4] = 0;
					StringBuffer loadScript = new StringBuffer(
							"load /*file*/$FILENAME$");
					htParams.put("loadScript", loadScript);
					int tokType = 0;
					viewer.setLoadStatus(true);
					viewer.loadModelFromFile(null, upFileName, null, null,
							isAppend, htParams, loadScript, tokType,
							currentSelectedScale, 0, selectedPath, true, "up",
							currentUnits);
					viewer.setLoadStatus(false);
				}
				return;
			}
		}else {
			error(ERROR_scaleUpFileNotExist);
			return;
		}
	}

	// added end -hcf

	private void load() throws ScriptException {
		boolean doLoadFiles = (!isSyntaxCheck || isCmdLine_C_Option);
		boolean isAppend = false;
		boolean isInline = false;
		boolean isSmiles = false;
		boolean isData = false;
		BitSet bsModels;
		int i = (tokAt(0) == Token.data ? 0 : 1);
		boolean appendNew = viewer.getAppendNew();
		String filter = null;
		List<Object> firstLastSteps = null;
		int modelCount0 = viewer.getModelCount()
				- (viewer.getFileName().equals("zapped") ? 1 : 0);
		int atomCount0 = viewer.getAtomCount();
		StringBuffer loadScript = new StringBuffer("load");
		int nFiles = 1;
		Map<String, Object> htParams = new Hashtable<String, Object>();
		// ignore optional file format
		if (isStateScript && forceNoAddHydrogens)
			htParams.put("doNotAddHydrogens", Boolean.TRUE);
		String modelName = null;
		String[] filenames = null;
		String[] tempFileInfo = null;
		String errMsg = null;
		String sOptions = "";
		int tokType = 0;
		int tok;

		// check for special parameters

		if (statementLength == 1) {
			i = 0;
		} else {
			modelName = parameterAsString(i);
			if (statementLength == 2 && !isSyntaxCheck) {
				// spt, png, and pngj files may be
				// run using the LOAD command, but
				// we transfer them to the script command
				// if it is just LOAD "xxxx.xxx"
				// so as to avoid the ZAP in case these
				// do not contain a full state script
				if (modelName.endsWith(".spt") || modelName.endsWith(".png")
						|| modelName.endsWith(".pngj")) {
					script(0, modelName, false);
					return;
				}
			}

			// load MENU
			// load DATA "xxx" ...(data here)...END "xxx"
			// load DATA "append xxx" ...(data here)...END "append xxx"
			// load DATA "@varName"
			// load APPEND (moves pointer forward)
			// load XYZ
			// load VXYZ
			// load VIBRATION
			// load TEMPERATURE
			// load OCCUPANCY
			// load PARTIALCHARGE
			switch (tok = tokAt(i)) {
			case Token.menu:
				String m = parameterAsString(checkLast(2));
				if (!isSyntaxCheck)
					viewer.setMenu(m, true);
				return;
			case Token.data:
				isData = true;
				loadScript.append(" /*data*/ data");
				String key = stringParameter(++i).toLowerCase();
				loadScript.append(" ").append(Escape.escapeStr(key));
				isAppend = key.startsWith("append");
				String strModel = (key.indexOf("@") >= 0 ? ""
						+ getParameter(key.substring(key.indexOf("@") + 1),
								Token.string) : parameterAsString(++i));
				strModel = viewer.fixInlineString(strModel,
						viewer.getInlineChar());
				htParams.put("fileData", strModel);
				htParams.put("isData", Boolean.TRUE);
				// note: ScriptCompiler will remove an initial \n if present
				loadScript.append('\n');
				loadScript.append(strModel);
				if (key.indexOf("@") < 0) {
					loadScript.append(" end ").append(Escape.escapeStr(key));
					i += 2; // skip END "key"
				}
				break;
			case Token.append:
				isAppend = true;
				loadScript.append(" append");
				modelName = optParameterAsString(++i);
				tok = Token.getTokFromName(modelName);
				break;
			case Token.identifier:
				i++;
				loadScript.append(" " + modelName);
				tokType = (tok == Token.identifier
						&& Parser.isOneOf(modelName.toLowerCase(),
								JmolConstants.LOAD_ATOM_DATA_TYPES) ? Token
						.getTokFromName(modelName) : Token.nada);
				if (tokType != Token.nada) {
					// loading just some data here
					// xyz vxyz vibration temperature occupancy partialcharge
					htParams.put("atomDataOnly", Boolean.TRUE);
					htParams.put("modelNumber", Integer.valueOf(1));
					if (tokType == Token.vibration)
						tokType = Token.vibxyz;
					tempFileInfo = viewer.getFileInfo();
					isAppend = true;
				}
			}
			// LOAD [[APPEND]] FILE
			// LOAD [[APPEND]] INLINE
			// LOAD [[APPEND]] SMILES
			// LOAD [[APPEND]] TRAJECTORY
			// LOAD [[APPEND]] MODEL
			// LOAD [[APPEND]] "fileNameInQuotes"

			switch (tok) {
			case Token.file:
			case Token.inline:
				isInline = (tok == Token.inline);
				i++;
				loadScript.append(" " + modelName);
				break;
			case Token.smiles:
				isSmiles = true;
				i++;
				break;
			case Token.trajectory:
			case Token.model:
				i++;
				loadScript.append(" " + modelName);
				if (tok == Token.trajectory)
					htParams.put("isTrajectory", Boolean.TRUE);
				if (isPoint3f(i)) {
					Point3f pt = getPoint3f(i, false);
					i = iToken + 1;
					// first last stride
					htParams.put("firstLastStep", new int[] { (int) pt.x,
							(int) pt.y, (int) pt.z });
					loadScript.append(" " + Escape.escapePt(pt));
				} else if (tokAt(i) == Token.bitset) {
					bsModels = (BitSet) getToken(i++).value;
					htParams.put("bsModels", bsModels);
					loadScript.append(" " + Escape.escape(bsModels));
				} else {
					htParams.put("firstLastStep", new int[] { 0, -1, 1 });
				}
				break;
			case Token.identifier:
				// i has been incremented; continue...
				break;
			default:
				modelName = "fileset";
			}
			if (getToken(i).tok != Token.string)
				error(ERROR_filenameExpected);
		}
		// long timeBegin = System.currentTimeMillis();

		// file name is next

		// LOAD ... "xxxx"
		// LOAD ... "xxxx" AS "yyyy"

		int filePt = i;
		String localName = null;
		if (tokAt(filePt + 1) == Token.as) {
			localName = stringParameter(i = i + 2);
			if (viewer.getPathForAllFiles() != "") {
				// we use the LOCAL name when reading from a local path only (in
				// the case of JMOL files)
				localName = null;
				filePt = i;
			}
		}

		String filename = null;
		if (statementLength == i + 1) {

			// end-of-command options:
			// LOAD SMILES "xxxx" --> load "$xxxx"
			// LOAD "['xxxxx','yyyyy','zzzzz']"

			if (i == 0 || (filename = parameterAsString(filePt)).length() == 0)
				filename = viewer.getFullPathName();
			if (filename == null) {
				zap(false);
				return;
			}
			if (isSmiles) {
				filename = "$" + filename;
			} else if (!isInline) {
				if (filename.indexOf("[]") >= 0)
					return;
				if (filename.indexOf("[") == 0) {
					filenames = Escape.unescapeStringArray(filename);
					if (filenames != null) {
						if (i == 1)
							loadScript.append(" files");
						if (loadScript.indexOf(" files") < 0)
							error(ERROR_invalidArgument);
						for (int j = 0; j < filenames.length; j++)
							loadScript.append(" /*file*/").append(
									Escape.escapeStr(filenames[j]));
					}
				}
			}

		} else if (getToken(i + 1).tok == Token.manifest
				// model/vibration index or list of model indices
				|| theTok == Token.integer || theTok == Token.varray
				|| theTok == Token.leftsquare
				|| theTok == Token.spacebeforesquare
				// {i j k} (lattice)
				|| theTok == Token.leftbrace || theTok == Token.point3f
				// PACKED/CENTROID, either order
				|| theTok == Token.packed || theTok == Token.centroid
				// SUPERCELL {i j k}
				|| theTok == Token.supercell
				// RANGE x.x or RANGE -x.x
				|| theTok == Token.range
				// SPACEGROUP "nameOrNumber"
				// or SPACEGROUP "IGNOREOPERATORS"
				// or SPACEGROUP "" (same as current)
				|| theTok == Token.spacegroup
				// UNITCELL [a b c alpha beta gamma]
				// or UNITCELL [ax ay az bx by bz cx cy cz]
				// or UNITCELL "" (same as current)
				// UNITCELL "..." or UNITCELL ""
				|| theTok == Token.unitcell
				// OFFSET {x y z}
				|| theTok == Token.offset
				// FILTER "..."
				|| theTok == Token.filter && tokAt(i + 3) != Token.coord
				// don't remember what this is:
				|| theTok == Token.identifier && tokAt(i + 3) != Token.coord

		) {

			// more complicated command options, in order
			// (checking the tokens after "....")

			// LOAD "" --> prevous file

			if ((filename = parameterAsString(filePt)).length() == 0
					&& (filename = viewer.getFullPathName()) == null) {
				// no previously loaded file
				zap(false);
				return;
			}
			if (filePt == i)
				i++;

			// for whatever reason, we don't allow a filename with [] in it.
			if (filename.indexOf("[]") >= 0)
				return;

			// MANIFEST "..."

			if ((tok = tokAt(i)) == Token.manifest) {
				String manifest = stringParameter(++i);
				htParams.put("manifest", manifest);
				sOptions += " MANIFEST " + Escape.escapeStr(manifest);
				tok = tokAt(++i);
			}
			// n >= 0: model number
			// n < 0: vibration number
			// [index1, index2, index3,...]

			switch (tok) {
			case Token.integer:
				int n = intParameter(i);
				sOptions += " " + n;
				if (n < 0)
					htParams.put("vibrationNumber", Integer.valueOf(-n));
				else
					htParams.put("modelNumber", Integer.valueOf(n));
				tok = tokAt(++i);
				break;
			case Token.varray:
			case Token.leftsquare:
			case Token.spacebeforesquare:
				float[] data = floatParameterSet(i, 1, Integer.MAX_VALUE);
				i = iToken;
				BitSet bs = new BitSet();
				for (int j = 0; j < data.length; j++)
					if (data[j] >= 1 && data[j] == (int) data[j])
						bs.set((int) data[j] - 1);
				htParams.put("bsModels", bs);
				int[] iArray = new int[bs.cardinality()];
				for (int pt = 0, j = bs.nextSetBit(0); j >= 0; j = bs
						.nextSetBit(j + 1))
					iArray[pt++] = j + 1;
				sOptions += " " + Escape.escapeArray(iArray);
				tok = tokAt(i);
				break;
			}

			// {i j k}

			Point3f lattice = null;
			if (tok == Token.leftbrace || tok == Token.point3f) {
				lattice = getPoint3f(i, false);
				i = iToken + 1;
				tok = tokAt(i);
			}

			// default lattice {555 555 -1} (packed)
			// for PACKED, CENTROID, SUPERCELL, RANGE, SPACEGROUP, UNITCELL

			switch (tok) {
			case Token.packed:
			case Token.centroid:
			case Token.supercell:
			case Token.range:
			case Token.spacegroup:
			case Token.unitcell:
				if (lattice == null)
					lattice = new Point3f(555, 555, -1);
				iToken = i - 1;
			}
			Point3f offset = null;
			if (lattice != null) {
				htParams.put("lattice", lattice);
				i = iToken + 1;
				sOptions += " {" + (int) lattice.x + " " + (int) lattice.y
						+ " " + (int) lattice.z + "}";

				// {i j k} PACKED, CENTROID -- either or both; either order

				if (tokAt(i) == Token.packed) {
					htParams.put("packed", Boolean.TRUE);
					sOptions += " PACKED";
					i++;
				}
				if (tokAt(i) == Token.centroid) {
					htParams.put("centroid", Boolean.TRUE);
					sOptions += " CENTROID";
					i++;
					if (tokAt(i) == Token.packed
							&& !htParams.containsKey("packed")) {
						htParams.put("packed", Boolean.TRUE);
						sOptions += " PACKED";
						i++;
					}
				}

				// {i j k} ... SUPERCELL {i' j' k'}

				if (tokAt(i) == Token.supercell) {
					Object supercell;
					if (isPoint3f(++i)) {
						Point3f pt = getPoint3f(i, false);
						if (pt.x != (int) pt.x || pt.y != (int) pt.y
								|| pt.z != (int) pt.z || pt.x < 1 || pt.y < 1
								|| pt.z < 1) {
							iToken = i;
							error(ERROR_invalidArgument);
						}
						supercell = pt;
						i = iToken + 1;
					} else {
						supercell = stringParameter(i++);
					}
					htParams.put("supercell", supercell);
				}

				// {i j k} ... RANGE x.y (from full unit cell set)
				// {i j k} ... RANGE -x.y (from non-symmetry set)

				float distance = 0;
				if (tokAt(i) == Token.range) {
					/*
					 * # Jmol 11.3.9 introduces the capability of visualizing
					 * the close contacts around a crystalline protein (or any
					 * other cyrstal structure) that are to atoms that are in
					 * proteins in adjacent unit cells or adjacent to the
					 * protein itself. The option RANGE x, where x is a distance
					 * in angstroms, placed right after the braces containing
					 * the set of unit cells to load does this. The distance, if
					 * a positive number, is the maximum distance away from the
					 * closest atom in the {1 1 1} set. If the distance x is a
					 * negative number, then -x is the maximum distance from the
					 * {not symmetry} set. The difference is that in the first
					 * case the primary unit cell (555) is first filled as
					 * usual, using symmetry operators, and close contacts to
					 * this set are found. In the second case, only the
					 * file-based atoms ( Jones-Faithful operator x,y,z) are
					 * initially included, then close contacts to that set are
					 * found. Depending upon the application, one or the other
					 * of these options may be desirable.
					 */
					i++;
					distance = floatParameter(i++);
					sOptions += " range " + distance;
				}
				htParams.put("symmetryRange", Float.valueOf(distance));

				// {i j k} ... SPACEGROUP "nameOrNumber"
				// {i j k} ... SPACEGROUP "IGNOREOPERATORS"
				// {i j k} ... SPACEGROUP ""

				String spacegroup = null;
				SymmetryInterface sg;
				int iGroup = Integer.MIN_VALUE;
				if (tokAt(i) == Token.spacegroup) {
					++i;
					spacegroup = TextFormat.simpleReplace(
							parameterAsString(i++), "''", "\"");
					sOptions += " spacegroup " + Escape.escapeStr(spacegroup);
					if (spacegroup.equalsIgnoreCase("ignoreOperators")) {
						iGroup = -999;
					} else {
						if (spacegroup.length() == 0) {
							sg = viewer.getCurrentUnitCell();
							if (sg != null)
								spacegroup = sg.getSpaceGroupName();
						} else {
							if (spacegroup.indexOf(",") >= 0) // Jones Faithful
								if ((lattice.x < 9 && lattice.y < 9 && lattice.z == 0))
									spacegroup += "#doNormalize=0";
						}
						htParams.put("spaceGroupName", spacegroup);
						iGroup = -2;
					}
				}

				// {i j k} ... UNITCELL [a b c alpha beta gamma]
				// {i j k} ... UNITCELL [ax ay az bx by bz cx cy cz]
				// {i j k} ... UNITCELL "" // same as current

				float[] fparams = null;
				if (tokAt(i) == Token.unitcell) {
					++i;
					if (optParameterAsString(i).length() == 0) {
						// unitcell "" -- use current unit cell
						sg = viewer.getCurrentUnitCell();
						if (sg != null) {
							fparams = sg.getUnitCellAsArray(true);
							offset = sg.getCartesianOffset();
						}
					} else {
						fparams = floatParameterSet(i, 6, 9);
					}
					if (fparams == null || fparams.length != 6
							&& fparams.length != 9)
						error(ERROR_invalidArgument);
					sOptions += " unitcell {";
					for (int j = 0; j < fparams.length; j++)
						sOptions += (j == 0 ? "" : " ") + fparams[j];
					sOptions += "}";
					htParams.put("unitcell", fparams);
					if (iGroup == Integer.MIN_VALUE)
						iGroup = -1;
				}
				i = iToken + 1;
				if (iGroup != Integer.MIN_VALUE)
					htParams.put("spaceGroupIndex", Integer.valueOf(iGroup));
			}

			// OFFSET {x y z} (fractional or not) (Jmol 12.1.17)

			if (offset != null)
				coordinatesAreFractional = false;
			else if (tokAt(i) == Token.offset)
				offset = getPoint3f(++i, true);
			if (offset != null) {
				if (coordinatesAreFractional) {
					offset.set(fractionalPoint);
					htParams.put("unitCellOffsetFractional",
							(coordinatesAreFractional ? Boolean.TRUE
									: Boolean.FALSE));
					sOptions += " offset {" + offset.x + " " + offset.y + " "
							+ offset.z + "/1}";
				} else {
					sOptions += " offset " + Escape.escapePt(offset);
				}
				htParams.put("unitCellOffset", offset);
				i = iToken + 1;
			}

			// FILTER

			if (tokAt(i) == Token.filter)
				filter = stringParameter(++i);

		} else {

			// list of file names
			// or COORD {i j k} "fileName"
			// or COORD ({bitset}) "fileName"
			// or FILTER "xxxx"

			if (i == 1) {
				i++;
				loadScript.append(" " + modelName);
			}

			Point3f pt = null;
			BitSet bs = null;
			List<String> fNames = new ArrayList<String>();
			while (i < statementLength) {
				switch (tokAt(i)) {
				case Token.filter:
					filter = stringParameter(++i);
					++i;
					continue;
				case Token.coord:
					htParams.remove("isTrajectory");
					if (firstLastSteps == null) {
						firstLastSteps = new ArrayList<Object>();
						pt = new Point3f(0, -1, 1);
					}
					if (isPoint3f(++i)) {
						pt = getPoint3f(i, false);
						i = iToken + 1;
					} else if (tokAt(i) == Token.bitset) {
						bs = (BitSet) getToken(i).value;
						pt = null;
						i = iToken + 1;
					}
					break;
				case Token.identifier:
					error(ERROR_invalidArgument);
				}
				fNames.add(filename = parameterAsString(i++));
				if (pt != null) {
					firstLastSteps.add(new int[] { (int) pt.x, (int) pt.y,
							(int) pt.z });
					loadScript.append(" COORD " + Escape.escapePt(pt));
				} else if (bs != null) {
					firstLastSteps.add(bs);
					loadScript.append(" COORD " + Escape.escape(bs));
				}
				loadScript.append(" /*file*/$FILENAME" + fNames.size() + "$");
			}
			if (firstLastSteps != null) {
				htParams.put("firstLastSteps", firstLastSteps);
			}
			nFiles = fNames.size();
			filenames = new String[nFiles];
			for (int j = 0; j < nFiles; j++)
				filenames[j] = fNames.get(j);
			filename = "fileSet";
		}

		// end of parsing

		if (!doLoadFiles)
			return;

		// get default filter if necessary

		if (filter == null)
			filter = viewer.getDefaultLoadFilter();
		if (filter.length() > 0) {
			htParams.put("filter", filter);
			if (filter.equalsIgnoreCase("2d")) // MOL file hack
				filter = "2D-noMin";
			sOptions += " FILTER " + Escape.escapeStr(filter);
		}

		// store inline data or variable data in htParams

		boolean isVariable = false;
		if (filenames == null) {
			if (isInline) {
				htParams.put("fileData", filename);
			} else if (filename.startsWith("@") && filename.length() > 1) {
				isVariable = true;
				String s = getStringParameter(filename.substring(1), false);
				htParams.put("fileData", s);
				loadScript = new StringBuffer("{\n    var "
						+ filename.substring(1) + " = " + Escape.escapeStr(s)
						+ ";\n    " + loadScript);
			}
		}

		// set up the output stream from AS keyword

		OutputStream os = null;
		if (localName != null) {
			if (localName.equals("."))
				localName = viewer.getFilePath(filename, true);
			if (localName.length() == 0
					|| viewer.getFilePath(localName, false).equalsIgnoreCase(
							viewer.getFilePath(filename, false)))
				error(ERROR_invalidArgument);
			String[] fullPath = new String[] { localName };
			os = viewer.getOutputStream(localName, fullPath);
			if (os == null)
				Logger.error("Could not create output stream for "
						+ fullPath[0]);
			else
				htParams.put("OutputStream", os);
		}

		if (filenames == null && tokType == 0) {
			// a single file or string -- complete the loadScript
			loadScript.append(" ");
			if (isVariable || isInline) {
				loadScript.append(Escape.escapeStr(filename));
			} else if (!isData) {
				if (!filename.equals("string") && !filename.equals("string[]"))
					loadScript.append("/*file*/");
				if (localName != null)
					localName = viewer.getFilePath(localName, false);
				loadScript.append((localName != null ? Escape
						.escapeStr(localName) : "$FILENAME$"));
			}
			if (sOptions.length() > 0)
				loadScript.append(" /*options*/ ").append(sOptions);
			if (isVariable)
				loadScript.append("\n  }");
			htParams.put("loadScript", loadScript);
		}
		setCursorWait(true);

		//tuan changed on 08/23/2017 to visualize pdb file
		if (filename.endsWith(".pdb")){
			
		errMsg = viewer.loadModelFromFile(null, filename, filenames, null,
		 isAppend, htParams, loadScript, tokType);
		
		}else{
		


			Atom[] currentUnits = viewer.getModelSet().atoms;
	
			// first load
			int loadInfo = toCheckAndFind(filename);
			if (loadInfo > 0) {
				errMsg = viewer.loadModelFromFile(null, filename, filenames, null,
						isAppend, htParams, loadScript, tokType, loadInfo, 0,
						selectedPath, true, "none", currentUnits);
			} else if (loadInfo == 0) {
				errMsg = viewer.loadModelFromFile(null, filename, filenames, null,
						isAppend, htParams, loadScript, tokType);
			} else if (loadInfo == -1) {
				error(ERROR_gssWrongFileName);
			} else if (loadInfo == -2) {
				error(ERROR_gpdbWrongFileName);
			}
			// modified end -hcf		
		}
		
		if (os != null)
			try {
				viewer.setFileInfo(new String[] { localName, localName,
						localName });
				Logger.info(GT._("file {0} created", localName));
				showString(viewer.getFilePath(localName, false) + " created");
				os.close();
			} catch (IOException e) {
				Logger.error("error closing file " + e.getMessage());
			}
		if (tokType > 0) {
			// we are just loading an atom property
			// reset the file info in FileManager, check for errors, and return
			viewer.setFileInfo(tempFileInfo);
			if (errMsg != null && !isCmdLine_c_or_C_Option)
				evalError(errMsg, null);
			return;
		}
		if (errMsg != null && !isCmdLine_c_or_C_Option) {
			if (errMsg.indexOf(JmolConstants.NOTE_SCRIPT_FILE) == 0) {
				filename = errMsg.substring(
						JmolConstants.NOTE_SCRIPT_FILE.length()).trim();
				script(0, filename, false);
				return;
			}
			evalError(errMsg, null);
		}
		if (isAppend && (appendNew || nFiles > 1)) {
			viewer.setAnimationRange(-1, -1);
			viewer.setCurrentModelIndex(modelCount0);
		}
		if (scriptLevel == 0 && !isAppend && nFiles < 2)
			showString((String) viewer
					.getModelSetAuxiliaryInfo("modelLoadNote"));
		if (logMessages)
			scriptStatusOrBuffer("Successfully loaded:"
					+ (filenames == null ? htParams.get("fullPathName")
							: modelName));
		Map<String, Object> info = viewer.getModelSetAuxiliaryInfo();
		if (info != null && info.containsKey("centroidMinMax")
				&& viewer.getAtomCount() > 0)
			viewer.setCentroid(isAppend ? atomCount0 : 0,
					viewer.getAtomCount() - 1,
					(int[]) info.get("centroidMinMax"));
		String script = viewer.getDefaultLoadScript();
		String msg = "";
		if (script.length() > 0)
			msg += "\nUsing defaultLoadScript: " + script;
		if (info != null && viewer.getAllowEmbeddedScripts()) {
			String embeddedScript = (String) info.remove("jmolscript");
			if (embeddedScript != null && embeddedScript.length() > 0) {
				msg += "\nAdding embedded #jmolscript: " + embeddedScript;
				script += ";" + embeddedScript;
				setStringProperty("_loadScript", script);
				script = "allowEmbeddedScripts = false;try{" + script
						+ "} allowEmbeddedScripts = true;";
			}

		}
		logLoadInfo(msg);

		String siteScript = (info == null ? null : (String) info
				.remove("sitescript"));
		if (siteScript != null)
			script = siteScript + ";" + script;
		
		//Tuan added to visualize pdb file 
		if (filename.endsWith(".pdb") || filename.endsWith(".gss")){
			if (script.length() > 0) script += ";wireframe 5;";
			else script += "restrict bonds not selected;select not selected;wireframe 5;";
			
			int numberOfChain = CommonFunctions.countChain(viewer.getModelSet());
			if (numberOfChain > 1) script += "color chain;";
			
		}
		//End 
		if (script.length() > 0 && !isCmdLine_c_or_C_Option){

			// NOT checking embedded scripts in some cases

			runScript(script);
		}
	}

	// added -hcf
	private int toCheckAndFind(String fileName) {
		int currentScale = 2;//tuan debug
		Pattern patternGss = Pattern.compile("\\.([a-z]+s)(\\.gss)$");
		Matcher gssMatcher = patternGss.matcher(fileName);

		Pattern patternGpdb = Pattern.compile("([a-z]+s)(\\.pdb)$");
		Matcher gpdbMatcher = patternGpdb.matcher(fileName);

		if (gssMatcher.find()) {
			String scaleInfo = gssMatcher.group(1);
			Pattern patternCrs = Pattern
					.compile("(\\.chr)(\\d+)(\\.[a-z]+s)(\\.gss)$");
			Matcher crsMatcher = patternCrs.matcher(fileName);

			if (scaleInfo.equals("gs")) {
				selectedPath[0] = 1;
				selectedPath[1] = 0;
				selectedPath[2] = 0;
				selectedPath[3] = 0;
				selectedPath[4] = 0;
				currentScale = 1;
			} else if (scaleInfo.equals("cs") || scaleInfo.equals("ls")
					|| scaleInfo.equals("fs")) {
				if (crsMatcher.find()) {
					selectedPath[0] = 1;
					selectedPath[1] = Integer.parseInt(crsMatcher.group(2));
					selectedPath[2] = 0;
					selectedPath[3] = 0;
					selectedPath[4] = 0;
					if (scaleInfo.equals("cs")) {
						currentScale = 2;
					} else if (scaleInfo.equals("ls")) {
						currentScale = 3;
					} else if (scaleInfo.equals("fs")) {
						currentScale = 4;
					}
				}
				else {
					currentScale = -1;
				}
			} else if (scaleInfo.equals("ns")) {
				currentScale = 5;
			}
			else {
				currentScale = -1;
			}
		} 
		else if (gpdbMatcher.find()) {
			String scaleInfo = gssMatcher.group(1);
			Pattern patternCrs = Pattern
					.compile("(\\.chr)(\\d+)(\\.[a-z]+s)(\\.pdb)$");
			Matcher crsMatcher = patternCrs.matcher(fileName);

			if (scaleInfo.equals("gs")) {
				selectedPath[0] = 1;
				selectedPath[1] = 0;
				selectedPath[2] = 0;
				selectedPath[3] = 0;
				selectedPath[4] = 0;
				currentScale = 1;
			} else if (scaleInfo.equals("cs") || scaleInfo.equals("ls")
					|| scaleInfo.equals("fs")) {
				if (crsMatcher.find()) {
					selectedPath[0] = 1;
					selectedPath[1] = Integer.parseInt(crsMatcher.group(2));
					selectedPath[2] = 0;
					selectedPath[3] = 0;
					selectedPath[4] = 0;
					if (scaleInfo.equals("cs")) {
						currentScale = 2;
					} else if (scaleInfo.equals("ls")) {
						currentScale = 3;
					} else if (scaleInfo.equals("ls")) {
						currentScale = 4;
					}
				} else {
					currentScale = -2;
				}
			} else {
				currentScale = -2;
			}
		}
		
		return currentScale;
	}

	// added end -hcf

	@SuppressWarnings("unchecked")
	private void logLoadInfo(String msg) {
		if (msg.length() > 0)
			Logger.info(msg);
		StringBuffer sb = new StringBuffer();
		int modelCount = viewer.getModelCount();
		if (modelCount > 1)
			sb.append(modelCount).append(" models\n");
		for (int i = 0; i < modelCount; i++) {
			Map<String, Object> moData = (Map<String, Object>) viewer
					.getModelAuxiliaryInfo(i, "moData");
			if (moData == null)
				continue;
			sb.append(((List<Map<String, Object>>) moData.get("mos")).size())
					.append(" molecular orbitals in model ")
					.append(viewer.getModelNumberDotted(i)).append("\n");
		}
		if (sb.length() > 0)
			showString(sb.toString());
	}

	private String getFullPathName() throws ScriptException {
		String filename = (!isSyntaxCheck || isCmdLine_C_Option ? viewer
				.getFullPathName() : "test.xyz");
		if (filename == null)
			error(ERROR_invalidArgument);
		return filename;
	}

	private void measure() throws ScriptException {
		if (tokAt(1) == Token.search) {
			String smarts = stringParameter(statementLength == 3 ? 2 : 4);
			if (isSyntaxCheck)
				return;
			Atom[] atoms = viewer.getModelSet().atoms;
			int atomCount = viewer.getAtomCount();
			int[][] maps = viewer.getSmilesMatcher().getCorrelationMaps(smarts,
					atoms, atomCount, viewer.getSelectionSet(false), true,
					false);
			if (maps == null)
				return;
			setShapeProperty(JmolConstants.SHAPE_MEASURES, "maps", maps);
			return;
		}
		switch (statementLength) {
		case 1:
		case 2:
			switch (getToken(1).tok) {
			case Token.nada:
			case Token.on:
				setShapeProperty(JmolConstants.SHAPE_MEASURES, "hideAll",
						Boolean.FALSE);
				return;
			case Token.off:
				setShapeProperty(JmolConstants.SHAPE_MEASURES, "hideAll",
						Boolean.TRUE);
				return;
			case Token.list:
				if (!isSyntaxCheck)
					showString(viewer.getMeasurementInfoAsString(), false);
				return;
			case Token.delete:
				if (!isSyntaxCheck)
					viewer.clearAllMeasurements();
				return;
			case Token.string:
				setShapeProperty(JmolConstants.SHAPE_MEASURES, "setFormats",
						stringParameter(1));
				return;
			}
			error(ERROR_keywordExpected, "ON, OFF, DELETE");
			break;
		case 3: // measure delete N
			// search "smartsString"
			switch (getToken(1).tok) {
			case Token.delete:
				if (getToken(2).tok == Token.all) {
					if (!isSyntaxCheck)
						viewer.clearAllMeasurements();
				} else {
					int i = intParameter(2) - 1;
					if (!isSyntaxCheck)
						viewer.deleteMeasurement(i);
				}
				return;
			}
		}

		int nAtoms = 0;
		int expressionCount = 0;
		int modelIndex = -1;
		int atomIndex = -1;
		int ptFloat = -1;
		int[] countPlusIndexes = new int[5];
		float[] rangeMinMax = new float[] { Float.MAX_VALUE, Float.MAX_VALUE };
		boolean isAll = false;
		boolean isAllConnected = false;
		boolean isNotConnected = false;
		boolean isRange = true;
		RadiusData rd = null;
		Boolean intramolecular = null;
		int tokAction = Token.opToggle;
		String strFormat = null;
		List<Object> points = new ArrayList<Object>();
		BitSet bs = new BitSet();
		Object value = null;
		TickInfo tickInfo = null;
		int nBitSets = 0;
		for (int i = 1; i < statementLength; ++i) {
			switch (getToken(i).tok) {
			case Token.identifier:
				error(ERROR_keywordExpected, "ALL, ALLCONNECTED, DELETE");
				break;
			default:
				error(ERROR_expressionOrIntegerExpected);
				break;
			case Token.opNot:
				if (tokAt(i + 1) != Token.connected)
					error(ERROR_invalidArgument);
				i++;
				isNotConnected = true;
				break;
			case Token.connected:
			case Token.allconnected:
			case Token.all:
				isAllConnected = (theTok == Token.allconnected);
				atomIndex = -1;
				isAll = true;
				if (isAllConnected && isNotConnected)
					error(ERROR_invalidArgument);
				break;
			case Token.decimal:
				if (rd != null)
					error(ERROR_invalidArgument);
				isAll = true;
				isRange = true;
				ptFloat = (ptFloat + 1) % 2;
				rangeMinMax[ptFloat] = floatParameter(i);
				break;
			case Token.delete:
				if (tokAction != Token.opToggle)
					error(ERROR_invalidArgument);
				tokAction = Token.delete;
				break;
			case Token.integer:
				int iParam = intParameter(i);
				if (isAll) {
					isRange = true; // irrelevant if just four integers
					ptFloat = (ptFloat + 1) % 2;
					rangeMinMax[ptFloat] = iParam;
				} else {
					atomIndex = viewer.getAtomIndexFromAtomNumber(iParam);
					if (!isSyntaxCheck && atomIndex < 0)
						return;
					if (value != null)
						error(ERROR_invalidArgument);
					if ((countPlusIndexes[0] = ++nAtoms) > 4)
						error(ERROR_badArgumentCount);
					countPlusIndexes[nAtoms] = atomIndex;
				}
				break;
			case Token.modelindex:
				modelIndex = intParameter(++i);
				break;
			case Token.off:
				if (tokAction != Token.opToggle)
					error(ERROR_invalidArgument);
				tokAction = Token.off;
				break;
			case Token.on:
				if (tokAction != Token.opToggle)
					error(ERROR_invalidArgument);
				tokAction = Token.on;
				break;
			case Token.range:
				isAll = true;
				isRange = true; // unnecessary
				atomIndex = -1;
				break;
			case Token.intramolecular:
			case Token.intermolecular:
				intramolecular = Boolean
						.valueOf(theTok == Token.intramolecular);
				isAll = true;
				isNotConnected = (theTok == Token.intermolecular);
				break;
			case Token.vanderwaals:
				if (ptFloat >= 0)
					error(ERROR_invalidArgument);
				rd = encodeRadiusParameter(i, false, true);
				rd.values = rangeMinMax;
				i = iToken;
				isNotConnected = true;
				isAll = true;
				intramolecular = Boolean.valueOf(false);
				if (nBitSets == 1) {
					nBitSets++;
					nAtoms++;
					BitSet bs2 = BitSetUtil.copy(bs);
					BitSetUtil.invertInPlace(bs2, viewer.getAtomCount());
					bs2.and(viewer.getAtomsWithin(5, bs, false, null));
					points.add(bs2);
				}
				break;
			case Token.bitset:
			case Token.expressionBegin:
			case Token.leftbrace:
			case Token.point3f:
			case Token.dollarsign:
				if (theTok == Token.bitset || theTok == Token.expressionBegin)
					nBitSets++;
				if (atomIndex >= 0)
					error(ERROR_invalidArgument);
				expressionResult = Boolean.FALSE;
				value = centerParameter(i);
				if (expressionResult instanceof BitSet) {
					value = bs = (BitSet) expressionResult;
					if (!isSyntaxCheck && bs.length() == 0)
						return;
				}
				if (value instanceof Point3f) {
					Point3fi v = new Point3fi();
					v.set((Point3f) value);
					v.modelIndex = (short) modelIndex;
					value = v;
				}
				if ((nAtoms = ++expressionCount) > 4)
					error(ERROR_badArgumentCount);
				i = iToken;
				points.add(value);
				break;
			case Token.string:
				// measures "%a1 %a2 %v %u"
				strFormat = stringParameter(i);
				break;
			case Token.ticks:
				tickInfo = checkTicks(i, false, true, true);
				i = iToken;
				tokAction = Token.define;
				break;
			}
		}
		if (rd != null && (ptFloat >= 0 || nAtoms != 2) || nAtoms < 2
				&& (tickInfo == null || nAtoms == 1))
			error(ERROR_badArgumentCount);
		if (strFormat != null && strFormat.indexOf(nAtoms + ":") != 0)
			strFormat = nAtoms + ":" + strFormat;
		if (isRange) {
			if (rangeMinMax[1] < rangeMinMax[0]) {
				rangeMinMax[1] = rangeMinMax[0];
				rangeMinMax[0] = (rangeMinMax[1] == Float.MAX_VALUE ? Float.MAX_VALUE
						: -200);
			}
		}
		if (isSyntaxCheck)
			return;
		if (value != null || tickInfo != null) {
			if (rd == null)
				rd = new RadiusData(rangeMinMax);
			if (value == null)
				tickInfo.id = "default";
			if (value != null && strFormat != null
					&& tokAction == Token.opToggle)
				tokAction = Token.define;
			setShapeProperty(JmolConstants.SHAPE_MEASURES, "measure",
					new MeasurementData(viewer, points, tokAction, rd,
							strFormat, null, tickInfo, isAllConnected,
							isNotConnected, intramolecular, isAll));
			return;
		}
		switch (tokAction) {
		case Token.delete:
			setShapeProperty(JmolConstants.SHAPE_MEASURES, "delete",
					countPlusIndexes);
			break;
		case Token.on:
			setShapeProperty(JmolConstants.SHAPE_MEASURES, "show",
					countPlusIndexes);
			break;
		case Token.off:
			setShapeProperty(JmolConstants.SHAPE_MEASURES, "hide",
					countPlusIndexes);
			break;
		default:
			setShapeProperty(JmolConstants.SHAPE_MEASURES,
					(strFormat == null ? "toggle" : "toggleOn"),
					countPlusIndexes);
			if (strFormat != null)
				setShapeProperty(JmolConstants.SHAPE_MEASURES, "setFormats",
						strFormat);
		}
	}

	private String plot(Token[] args) throws ScriptException {
		// also used for draw [quaternion, helix, ramachandran]
		// and write quaternion, ramachandran, plot, ....
		// and plot property propertyX, propertyY, propertyZ //
		int modelIndex = viewer.getCurrentModelIndex();
		if (modelIndex < 0)
			error(ERROR_multipleModelsDisplayedNotOK, "plot");
		modelIndex = viewer.getJmolDataSourceFrame(modelIndex);
		int pt = args.length - 1;
		boolean isReturnOnly = (args != statement);
		Token[] statementSave = statement;
		if (isReturnOnly)
			statement = args;
		int tokCmd = (isReturnOnly ? Token.show : args[0].tok);
		int pt0 = (isReturnOnly || tokCmd == Token.quaternion
				|| tokCmd == Token.ramachandran ? 0 : 1);
		String filename = null;
		boolean makeNewFrame = true;
		boolean isDraw = false;
		switch (tokCmd) {
		case Token.plot:
		case Token.quaternion:
		case Token.ramachandran:
			break;
		case Token.draw:
			makeNewFrame = false;
			isDraw = true;
			break;
		case Token.show:
			makeNewFrame = false;
			break;
		case Token.write:
			makeNewFrame = false;
			if (tokAt(pt, args) == Token.string) {
				filename = stringParameter(pt--);
			} else if (tokAt(pt - 1, args) == Token.per) {
				filename = parameterAsString(pt - 2) + "."
						+ parameterAsString(pt);
				pt -= 3;
			} else {
				statement = statementSave;
				iToken = statement.length;
				error(ERROR_endOfStatementUnexpected);
			}
			break;
		}
		String qFrame = "";
		Object[] parameters = null;
		String stateScript = "";
		boolean isQuaternion = false;
		boolean isDerivative = false;
		boolean isSecondDerivative = false;
		boolean isRamachandranRelative = false;
		int propertyX = 0, propertyY = 0, propertyZ = 0;
		BitSet bs = BitSetUtil.copy(viewer.getSelectionSet(false));
		String preSelected = "; select " + Escape.escape(bs) + ";\n ";
		String type = optParameterAsString(pt).toLowerCase();
		Point3f minXYZ = null;
		Point3f maxXYZ = null;
		int tok = tokAt(pt0, args);
		if (tok == Token.string)
			tok = Token.getTokFromName((String) args[pt0].value);
		switch (tok) {
		default:
			iToken = 1;
			error(ERROR_invalidArgument);
			break;
		case Token.data:
			iToken = 1;
			type = "data";
			preSelected = "";
			break;
		case Token.property:
			iToken = pt0 + 1;
			if (!Token.tokAttr(propertyX = tokAt(iToken++), Token.atomproperty)
					|| !Token.tokAttr(propertyY = tokAt(iToken++),
							Token.atomproperty))
				error(ERROR_invalidArgument);
			if (Token.tokAttr(propertyZ = tokAt(iToken), Token.atomproperty))
				iToken++;
			else
				propertyZ = 0;
			if (tokAt(iToken) == Token.min) {
				minXYZ = getPoint3f(++iToken, false);
				iToken++;
			}
			if (tokAt(iToken) == Token.max) {
				maxXYZ = getPoint3f(++iToken, false);
				iToken++;
			}
			type = "property " + Token.nameOf(propertyX) + " "
					+ Token.nameOf(propertyY)
					+ (propertyZ == 0 ? "" : " " + Token.nameOf(propertyZ));
			if (bs.nextSetBit(0) < 0)
				bs = viewer.getModelUndeletedAtomsBitSet(modelIndex);
			stateScript = "select " + Escape.escape(bs) + ";\n ";
			break;
		case Token.ramachandran:
			if (type.equalsIgnoreCase("draw")) {
				isDraw = true;
				type = optParameterAsString(--pt).toLowerCase();
			}
			isRamachandranRelative = (pt > pt0 && type.startsWith("r"));
			type = "ramachandran" + (isRamachandranRelative ? " r" : "")
					+ (tokCmd == Token.draw ? " draw" : "");
			break;
		case Token.quaternion:
		case Token.helix:
			qFrame = " \"" + viewer.getQuaternionFrame() + "\"";
			stateScript = "set quaternionFrame" + qFrame + ";\n  ";
			isQuaternion = true;
			// working backward this time:
			if (type.equalsIgnoreCase("draw")) {
				isDraw = true;
				type = optParameterAsString(--pt).toLowerCase();
			}
			isDerivative = (type.startsWith("deriv") || type.startsWith("diff"));
			isSecondDerivative = (isDerivative && type.indexOf("2") > 0);
			if (isDerivative)
				pt--;
			if (type.equalsIgnoreCase("helix") || type.equalsIgnoreCase("axis")) {
				isDraw = true;
				isDerivative = true;
				pt = -1;
			}
			type = ((pt <= pt0 ? "" : optParameterAsString(pt)) + "w")
					.substring(0, 1);
			if (type.equals("a") || type.equals("r"))
				isDerivative = true;
			if (!Parser.isOneOf(type, "w;x;y;z;r;a")) // a absolute; r relative
				evalError("QUATERNION [w,x,y,z,a,r] [difference][2]", null);
			type = "quaternion " + type + (isDerivative ? " difference" : "")
					+ (isSecondDerivative ? "2" : "") + (isDraw ? " draw" : "");
			break;
		}
		statement = statementSave;
		if (isSyntaxCheck) // just in case we later add parameter options to
							// this
			return "";

		// if not just drawing check to see if there is already a plot of this
		// type

		if (makeNewFrame) {
			stateScript += "plot " + type;
			int ptDataFrame = viewer.getJmolDataFrameIndex(modelIndex,
					stateScript);
			if (ptDataFrame > 0 && tokCmd != Token.write
					&& tokCmd != Token.show) {
				// no -- this is that way we switch frames.
				// viewer.deleteAtoms(viewer.getModelUndeletedAtomsBitSet(ptDataFrame),
				// true);
				// data frame can't be 0.
				viewer.setCurrentModelIndex(ptDataFrame, true);
				// BitSet bs2 = viewer.getModelAtomBitSet(ptDataFrame);
				// bs2.and(bs);
				// need to be able to set data directly as well.
				// viewer.display(BitSetUtil.setAll(viewer.getAtomCount()), bs2,
				// tQuiet);
				return "";
			}
		}

		// prepare data for property plotting

		float[] dataX = null, dataY = null, dataZ = null;
		Point3f factors = new Point3f(1, 1, 1);
		if (tok == Token.property) {
			dataX = getBitsetPropertyFloat(bs, propertyX | Token.selectedfloat,
					(minXYZ == null ? Float.NaN : minXYZ.x),
					(maxXYZ == null ? Float.NaN : maxXYZ.x));
			dataY = getBitsetPropertyFloat(bs, propertyY | Token.selectedfloat,
					(minXYZ == null ? Float.NaN : minXYZ.y),
					(maxXYZ == null ? Float.NaN : maxXYZ.y));
			if (propertyZ != 0)
				dataZ = getBitsetPropertyFloat(bs, propertyZ
						| Token.selectedfloat, (minXYZ == null ? Float.NaN
						: minXYZ.z), (maxXYZ == null ? Float.NaN : maxXYZ.z));
			if (minXYZ == null)
				minXYZ = new Point3f(getMinMax(dataX, false, propertyX),
						getMinMax(dataY, false, propertyY), getMinMax(dataZ,
								false, propertyZ));
			if (maxXYZ == null)
				maxXYZ = new Point3f(getMinMax(dataX, true, propertyX),
						getMinMax(dataY, true, propertyY), getMinMax(dataZ,
								true, propertyZ));
			Logger.info("plot min/max: " + minXYZ + " " + maxXYZ);
			Point3f center = new Point3f(maxXYZ);
			center.add(minXYZ);
			center.scale(0.5f);
			factors.set(maxXYZ);
			factors.sub(minXYZ);
			factors.set(factors.x / 200, factors.y / 200, factors.z / 200);
			if (Token.tokAttr(propertyX, Token.intproperty)) {
				factors.x = 1;
				center.x = 0;
			} else if (factors.x > 0.1 && factors.x <= 10) {
				factors.x = 1;
			}
			if (Token.tokAttr(propertyY, Token.intproperty)) {
				factors.y = 1;
				center.y = 0;
			} else if (factors.y > 0.1 && factors.y <= 10) {
				factors.y = 1;
			}
			if (Token.tokAttr(propertyZ, Token.intproperty)) {
				factors.z = 1;
				center.z = 0;
			} else if (factors.z > 0.1 && factors.z <= 10) {
				factors.z = 1;
			}
			if (propertyZ == 0)
				center.z = minXYZ.z = maxXYZ.z = factors.z = 0;
			for (int i = 0; i < dataX.length; i++)
				dataX[i] = (dataX[i] - center.x) / factors.x;
			for (int i = 0; i < dataY.length; i++)
				dataY[i] = (dataY[i] - center.y) / factors.y;
			if (propertyZ != 0)
				for (int i = 0; i < dataZ.length; i++)
					dataZ[i] = (dataZ[i] - center.z) / factors.z;
			parameters = new Object[] { bs, dataX, dataY, dataZ, minXYZ,
					maxXYZ, factors, center };
		}

		// all set...

		if (tokCmd == Token.write)
			return viewer.streamFileData(filename, "PLOT", type, modelIndex,
					parameters);

		String data = (type.equals("data") ? "1 0 H 0 0 0 # Jmol PDB-encoded data"
				: viewer.getPdbData(modelIndex, type, parameters));

		if (tokCmd == Token.show)
			return data;

		if (Logger.debugging)
			Logger.info(data);

		if (tokCmd == Token.draw) {
			runScript(data);
			return "";
		}

		// create the new model

		String[] savedFileInfo = viewer.getFileInfo();
		boolean oldAppendNew = viewer.getAppendNew();
		viewer.setAppendNew(true);
		boolean isOK = (data != null && viewer.loadInline(data, true) == null);
		viewer.setAppendNew(oldAppendNew);
		viewer.setFileInfo(savedFileInfo);
		if (!isOK)
			return "";
		int modelCount = viewer.getModelCount();
		viewer.setJmolDataFrame(stateScript, modelIndex, modelCount - 1);
		if (tok != Token.property)
			stateScript += ";\n" + preSelected;
		StateScript ss = viewer.addStateScript(stateScript, true, false);

		// get post-processing script

		float radius = 150;
		String script;
		switch (tok) {
		default:
			script = "frame 0.0; frame last; reset;select visible;wireframe only;";
			radius = 10;
			break;
		case Token.property:
			viewer.setFrameTitle(modelCount - 1, type + " plot for model "
					+ viewer.getModelNumberDotted(modelIndex));
			float f = 3;
			script = "frame 0.0; frame last; reset;"
					+ "select visible; spacefill " + f + "; wireframe 0;"
					+ "draw plotAxisX" + modelCount
					+ " {100 -100 -100} {-100 -100 -100} \""
					+ Token.nameOf(propertyX) + "\";" + "draw plotAxisY"
					+ modelCount + " {-100 100 -100} {-100 -100 -100} \""
					+ Token.nameOf(propertyY) + "\";";
			if (propertyZ != 0)
				script += "draw plotAxisZ" + modelCount
						+ " {-100 -100 100} {-100 -100 -100} \""
						+ Token.nameOf(propertyZ) + "\";";
			break;
		case Token.ramachandran:
			viewer.setFrameTitle(modelCount - 1, "ramachandran plot for model "
					+ viewer.getModelNumberDotted(modelIndex));
			script = "frame 0.0; frame last; reset;"
					+ "select visible; color structure; spacefill 3.0; wireframe 0;"
					+ "draw ramaAxisX" + modelCount
					+ " {100 0 0} {-100 0 0} \"phi\";" + "draw ramaAxisY"
					+ modelCount + " {0 100 0} {0 -100 0} \"psi\";";
			break;
		case Token.quaternion:
		case Token.helix:
			viewer.setFrameTitle(
					modelCount - 1,
					type.replace('w', ' ') + qFrame + " for model "
							+ viewer.getModelNumberDotted(modelIndex));
			String color = (Colix.getHexCode(viewer
					.getColixBackgroundContrast()));
			script = "frame 0.0; frame last; reset;"
					+ "select visible; wireframe 0; spacefill 3.0; "
					+ "isosurface quatSphere" + modelCount + " color " + color
					+ " sphere 100.0 mesh nofill frontonly translucent 0.8;"
					+ "draw quatAxis" + modelCount
					+ "X {100 0 0} {-100 0 0} color red \"x\";"
					+ "draw quatAxis" + modelCount
					+ "Y {0 100 0} {0 -100 0} color green \"y\";"
					+ "draw quatAxis" + modelCount
					+ "Z {0 0 100} {0 0 -100} color blue \"z\";"
					+ "color structure;" + "draw quatCenter" + modelCount
					+ "{0 0 0} scale 0.02;";
			break;
		}

		// run the post-processing script and set rotation radius and display
		// frame title
		runScript(script + preSelected);
		ss.setModelIndex(viewer.getCurrentModelIndex());
		viewer.setRotationRadius(radius, true);
		shapeManager.loadShape(JmolConstants.SHAPE_ECHO);
		showString("frame "
				+ viewer.getModelNumberDotted(modelCount - 1)
				+ (type.length() > 0 ? " created: " + type
						+ (isQuaternion ? qFrame : "") : ""));
		return "";
	}

	private static float getMinMax(float[] data, boolean isMax, int tok) {
		if (data == null)
			return 0;
		switch (tok) {
		case Token.omega:
		case Token.phi:
		case Token.psi:
			return (isMax ? 180 : -180);
		case Token.eta:
		case Token.theta:
			return (isMax ? 360 : 0);
		case Token.straightness:
			return (isMax ? 1 : -1);
		}
		float fmax = (isMax ? -1E10f : 1E10f);
		for (int i = data.length; --i >= 0;) {
			float f = data[i];
			if (Float.isNaN(f))
				continue;
			if (isMax == (f > fmax))
				fmax = f;
		}
		return fmax;
	}

	private boolean pause() throws ScriptException {
		if (isSyntaxCheck)
			return false;
		String msg = optParameterAsString(1);
		if (!viewer.getBooleanProperty("_useCommandThread")) {
			// showString("Cannot pause thread when _useCommandThread = FALSE: "
			// +
			// msg);
			// return;
		}
		if (viewer.autoExit || !viewer.haveDisplay)
			return false;
		if (scriptLevel == 0 && pc == aatoken.length - 1) {
			viewer.scriptStatus("nothing to pause: " + msg);
			return false;
		}
		msg = (msg.length() == 0 ? ": RESUME to continue." : ": "
				+ viewer.formatText(msg));
		pauseExecution(true);
		viewer.scriptStatus("script execution paused" + msg,
				"script paused for RESUME");
		return true;
	}

	private void print() throws ScriptException {
		if (statementLength == 1)
			error(ERROR_badArgumentCount);
		showString(parameterExpressionString(1, 0), true);
	}

	private void prompt() throws ScriptException {
		String msg = null;
		if (statementLength == 1) {
			if (!isSyntaxCheck)
				msg = getScriptContext().getContextTrace(null, true).toString();
		} else {
			msg = parameterExpressionString(1, 0);
		}
		if (!isSyntaxCheck)
			viewer.prompt(msg, "OK", null, true);
	}

	private void refresh() {
		if (isSyntaxCheck)
			return;
		viewer.setTainted(true);
		viewer.requestRepaintAndWait();
	}

	private void reset() throws ScriptException {
		if (statementLength == 3 && tokAt(1) == Token.function) {
			if (!isSyntaxCheck)
				viewer.removeFunction(stringParameter(2));
			return;
		}
		checkLength(-2);
		if (isSyntaxCheck)
			return;
		if (statementLength == 1) {
			viewer.reset(false);
			return;
		}
		// possibly "all"
		switch (tokAt(1)) {
		case Token.cache:
			viewer.cacheClear();
			return;
		case Token.error:
			viewer.resetError();
			return;
		case Token.shape:
			viewer.resetShapes(true);
			return;
		case Token.function:
			viewer.clearFunctions();
			return;
		case Token.structure:
			BitSet bsAllAtoms = new BitSet();
			runScript(viewer.getDefaultStructure(null, bsAllAtoms));
			viewer.resetBioshapes(bsAllAtoms);
			return;
		case Token.vanderwaals:
			viewer.setData("element_vdw", new Object[] { null, "" }, 0, 0, 0,
					0, 0);
			return;
		case Token.aromatic:
			viewer.resetAromatic();
			return;
		case Token.spin:
			viewer.reset(true);
			return;
		}
		String var = parameterAsString(1);
		if (var.charAt(0) == '_')
			error(ERROR_invalidArgument);
		viewer.unsetProperty(var);
	}

	private void restrict() throws ScriptException {
		boolean isBond = (tokAt(1) == Token.bonds);
		select(isBond ? 2 : 1);
		restrictSelected(isBond, true);
	}

	private void restrictSelected (boolean isBond, boolean doInvert) {
		if (isSyntaxCheck)
			return;
		BitSet bsSelected = BitSetUtil.copy(viewer.getSelectionSet(true));
		if (doInvert) {
			viewer.invertSelection();
			BitSet bsSubset = viewer.getSelectionSubset();
			if (bsSubset != null) {
				bsSelected = BitSetUtil.copy(viewer.getSelectionSet(true));
				bsSelected.and(bsSubset);
				viewer.select(bsSelected, false, null, true);
				BitSetUtil.invertInPlace(bsSelected, viewer.getAtomCount());
				bsSelected.and(bsSubset);
			}
		}
		BitSetUtil.andNot(bsSelected, viewer.getDeletedAtoms());
		boolean bondmode = viewer.getBondSelectionModeOr();

		if (!isBond)
			setBooleanProperty("bondModeOr", true);
		setShapeSize(JmolConstants.SHAPE_STICKS, 0, null);
		// wireframe will not operate on STRUTS even though they are
		// a form of bond order (see BondIteratoSelected)
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_STRUT));
		setShapeSize(JmolConstants.SHAPE_STICKS, 0, null);
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_COVALENT_MASK));
		// also need to turn off backbones, ribbons, strands, cartoons
		for (int shapeType = JmolConstants.SHAPE_MAX_SIZE_ZERO_ON_RESTRICT; --shapeType >= 0;)
			if (shapeType != JmolConstants.SHAPE_MEASURES)
				setShapeSize(shapeType, 0, null);
		setShapeProperty(JmolConstants.SHAPE_POLYHEDRA, "delete", null);
		shapeManager.setLabel(null, viewer.getSelectionSet(true));

		if (!isBond)
			setBooleanProperty("bondModeOr", bondmode);
		viewer.select(bsSelected, false, null, true);
	}

	private void rotate(boolean isSpin, boolean isSelected)
			throws ScriptException {

		// rotate is a full replacement for spin
		// spin is DEPRECATED

		/*
		 * The Chime spin method:
		 * 
		 * set spin x 10;set spin y 30; set spin z 10; spin | spin ON spin OFF
		 * 
		 * Jmol does these "first x, then y, then z" I don't know what Chime
		 * does.
		 * 
		 * spin and rotate are now consolidated here.
		 * 
		 * far simpler is
		 * 
		 * spin x 10 spin y 10
		 * 
		 * these are pure x or y spins or
		 * 
		 * spin axisangle {1 1 0} 10
		 * 
		 * this is the same as the old "spin x 10; spin y 10" -- or is it?
		 * anyway, it's better!
		 * 
		 * note that there are many defaults
		 * 
		 * spin # defaults to spin y 10 spin 10 # defaults to spin y 10 spin x #
		 * defaults to spin x 10
		 * 
		 * and several new options
		 * 
		 * spin -x spin axisangle {1 1 0} 10 spin 10 (atomno=1)(atomno=2) spin
		 * 20 {0 0 0} {1 1 1}
		 * 
		 * spin MOLECULAR {0 0 0} 20
		 * 
		 * The MOLECULAR keyword indicates that spins or rotations are to be
		 * carried out in the internal molecular coordinate frame, not the fixed
		 * room frame.
		 * 
		 * In the case of rotateSelected, all rotations are molecular and the
		 * absense of the MOLECULAR keyword indicates to rotate about the
		 * geometric center of the molecule, not {0 0 0}
		 * 
		 * Fractional coordinates may be indicated:
		 * 
		 * spin 20 {0 0 0/} {1 1 1/}
		 * 
		 * In association with this, TransformManager and associated functions
		 * are TOTALLY REWRITTEN and consolideated. It is VERY clean now - just
		 * two methods here -- one fixed and one molecular, two in Viewer, and
		 * two in TransformManager. All the centering stuff has been carefully
		 * inspected are reorganized as well.
		 * 
		 * Bob Hanson 5/21/06
		 */

		if (statementLength == 2)
			switch (getToken(1).tok) {
			case Token.on:
				if (!isSyntaxCheck)
					viewer.setSpinOn(true);
				return;
			case Token.off:
				if (!isSyntaxCheck)
					viewer.setSpinOn(false);
				return;
			}

		BitSet bsAtoms = null;
		float degreesPerSecond = Float.MIN_VALUE;
		int nPoints = 0;
		float endDegrees = Float.MAX_VALUE;
		boolean isMolecular = false;
		boolean haveRotation = false;
		List<Point3f> ptsA = null;
		Point3f[] points = new Point3f[2];
		Vector3f rotAxis = new Vector3f(0, 1, 0);
		Vector3f translation = null;
		Matrix4f m4 = null;
		Matrix3f m3 = null;
		int direction = 1;
		int tok;
		Quaternion q = null;
		boolean helicalPath = false;
		List<Point3f> ptsB = null;
		BitSet bsCompare = null;
		Point3f invPoint = null;
		Point4f invPlane = null;
		boolean axesOrientationRasmol = viewer.getAxesOrientationRasmol();
		for (int i = 1; i < statementLength; ++i) {
			switch (tok = getToken(i).tok) {
			case Token.bitset:
			case Token.expressionBegin:
			case Token.leftbrace:
			case Token.point3f:
			case Token.dollarsign:
				if (tok == Token.bitset || tok == Token.expressionBegin) {
					if (translation != null || q != null || nPoints == 2) {
						bsAtoms = atomExpressionAt(i);
						ptsB = null;
						isSelected = true;
						break;
					}
				}
				haveRotation = true;
				if (nPoints == 2)
					nPoints = 0;
				// {X, Y, Z}
				// $drawObject[n]
				Point3f pt1 = centerParameter(i, viewer.getCurrentModelIndex());
				if (!isSyntaxCheck && tok == Token.dollarsign
						&& tokAt(i + 2) != Token.leftsquare) {
					// rotation about an axis such as $line1
					isMolecular = true;
					rotAxis = getDrawObjectAxis(objectNameParameter(++i),
							viewer.getCurrentModelIndex());
				}
				points[nPoints++] = pt1;
				break;
			case Token.spin:
				isSpin = true;
				continue;
			case Token.internal:
			case Token.molecular:
				isMolecular = true;
				continue;
			case Token.selected:
				isSelected = true;
				break;
			case Token.comma:
				continue;
			case Token.integer:
			case Token.decimal:
				if (isSpin) {
					// rotate spin ... [degreesPerSecond]
					// rotate spin ... [endDegrees] [degreesPerSecond]
					if (degreesPerSecond == Float.MIN_VALUE) {
						degreesPerSecond = floatParameter(i);
						continue;
					} else if (endDegrees == Float.MAX_VALUE) {
						endDegrees = degreesPerSecond;
						degreesPerSecond = floatParameter(i);
						continue;
					}
				} else {
					// rotate ... [endDegrees]
					// rotate ... [endDegrees] [degreesPerSecond]
					if (endDegrees == Float.MAX_VALUE) {
						endDegrees = floatParameter(i);
						continue;
					} else if (degreesPerSecond == Float.MIN_VALUE) {
						degreesPerSecond = floatParameter(i);
						isSpin = true;
						continue;
					}
				}
				error(ERROR_invalidArgument);
				break;
			case Token.minus:
				direction = -1;
				continue;
			case Token.x:
				haveRotation = true;
				rotAxis.set(direction, 0, 0);
				continue;
			case Token.y:
				haveRotation = true;
				rotAxis.set(0, direction, 0);
				continue;
			case Token.z:
				haveRotation = true;
				rotAxis.set(0, 0,
						(axesOrientationRasmol && !isMolecular ? -direction
								: direction));
				continue;

				// 11.6 options

			case Token.point4f:
			case Token.quaternion:
				if (tok == Token.quaternion)
					i++;
				haveRotation = true;
				q = getQuaternionParameter(i);
				rotAxis.set(q.getNormal());
				endDegrees = q.getTheta();
				break;
			case Token.axisangle:
				haveRotation = true;
				if (isPoint3f(++i)) {
					rotAxis.set(centerParameter(i));
					break;
				}
				Point4f p4 = getPoint4f(i);
				rotAxis.set(p4.x, p4.y, p4.z);
				endDegrees = p4.w;
				q = new Quaternion(rotAxis, endDegrees);
				break;
			case Token.branch:
				haveRotation = true;
				int iAtom1 = atomExpressionAt(++i).nextSetBit(0);
				int iAtom2 = atomExpressionAt(++iToken).nextSetBit(0);
				if (iAtom1 < 0 || iAtom2 < 0)
					return;
				bsAtoms = viewer.getBranchBitSet(iAtom2, iAtom1);
				isSelected = true;
				isMolecular = true;
				points[0] = viewer.getAtomPoint3f(iAtom1);
				points[1] = viewer.getAtomPoint3f(iAtom2);
				nPoints = 2;
				break;

			// 12.0 options

			case Token.translate:
				translation = new Vector3f(centerParameter(++i));
				isMolecular = isSelected = true;
				break;
			case Token.helix:
				// screw motion, for quaternion-based operations
				helicalPath = true;
				continue;
			case Token.symop:
				int symop = intParameter(++i);
				if (isSyntaxCheck)
					continue;
				Map<String, Object> info = viewer.getSpaceGroupInfo(null);
				Object[] op = (info == null ? null : (Object[]) info
						.get("operations"));
				if (symop == 0 || op == null || op.length < Math.abs(symop))
					error(ERROR_invalidArgument);
				op = (Object[]) op[Math.abs(symop) - 1];
				translation = (Vector3f) op[5];
				invPoint = (Point3f) op[6];
				points[0] = (Point3f) op[7];
				if (op[8] != null)
					rotAxis = (Vector3f) op[8];
				endDegrees = ((Integer) op[9]).intValue();
				if (symop < 0) {
					endDegrees = -endDegrees;
					if (translation != null)
						translation.scale(-1);
				}
				if (endDegrees == 0 && points[0] != null) {
					// glide plane
					rotAxis.normalize();
					Measure.getPlaneThroughPoint(points[0], rotAxis,
							invPlane = new Point4f());
				}
				q = new Quaternion(rotAxis, endDegrees);
				nPoints = (points[0] == null ? 0 : 1);
				isMolecular = true;
				haveRotation = true;
				isSelected = true;
				continue;

			case Token.compare:
			case Token.matrix4f:
			case Token.matrix3f:
				haveRotation = true;
				if (tok == Token.compare) {
					bsCompare = atomExpressionAt(++i);
					ptsA = viewer.getAtomPointVector(bsCompare);
					if (ptsA == null)
						error(ERROR_invalidArgument, i);
					i = iToken;
					ptsB = getPointVector(getToken(++i), i);
					if (ptsB == null || ptsA.size() != ptsB.size())
						error(ERROR_invalidArgument, i);
					m4 = new Matrix4f();
					points[0] = new Point3f();
					nPoints = 1;
					float stddev = (isSyntaxCheck ? 0 : Measure
							.getTransformMatrix4(ptsA, ptsB, m4, points[0]));
					// if the standard deviation is very small, we leave ptsB
					// because it will be used to set the absolute final
					// positions
					if (stddev > 0.001)
						ptsB = null;
				} else if (tok == Token.matrix4f) {
					m4 = (Matrix4f) theToken.value;
				}

				m3 = new Matrix3f();
				if (m4 != null) {
					translation = new Vector3f();
					m4.get(translation);
					m4.get(m3);
				} else {
					m3 = (Matrix3f) theToken.value;
				}
				q = (isSyntaxCheck ? new Quaternion() : new Quaternion(m3));
				rotAxis.set(q.getNormal());
				endDegrees = q.getTheta();
				isMolecular = true;
				break;
			default:
				error(ERROR_invalidArgument);
			}
			i = iToken;
		}
		if (isSyntaxCheck)
			return;
		if (isSelected && bsAtoms == null)
			bsAtoms = viewer.getSelectionSet(false);
		if (bsCompare != null) {
			isSelected = true;
			if (bsAtoms == null)
				bsAtoms = bsCompare;
		}
		float rate = (degreesPerSecond == Float.MIN_VALUE ? 10
				: endDegrees == Float.MAX_VALUE ? degreesPerSecond
						: (degreesPerSecond < 0) == (endDegrees > 0) ?
						// -n means number of seconds, not degreesPerSecond
						-endDegrees / degreesPerSecond
								: degreesPerSecond);
		if (q != null) {
			// only when there is a translation (4x4 matrix or TRANSLATE)
			// do we set the rotation to be the center of the selected atoms or
			// model
			if (nPoints == 0 && translation != null)
				points[0] = viewer.getAtomSetCenter(bsAtoms != null ? bsAtoms
						: isSelected ? viewer.getSelectionSet(false) : viewer
								.getModelUndeletedAtomsBitSet(-1));
			if (helicalPath && translation != null) {
				points[1] = new Point3f(points[0]);
				points[1].add(translation);
				Object[] ret = (Object[]) Measure.computeHelicalAxis(null,
						Token.array, points[0], points[1], q);
				points[0] = (Point3f) ret[0];
				float theta = ((Point3f) ret[3]).x;
				if (theta != 0) {
					translation = (Vector3f) ret[1];
					rotAxis = new Vector3f(translation);
					if (theta < 0)
						rotAxis.scale(-1);
				}
				m4 = null;
			}
			if (isSpin && m4 == null)
				m4 = ScriptMathProcessor
						.getMatrix4f(q.getMatrix(), translation);
			if (points[0] != null)
				nPoints = 1;
		}
		if (invPoint != null) {
			viewer.invertAtomCoord(invPoint, bsAtoms);
			if (rotAxis == null)
				return;
		}
		if (invPlane != null) {
			viewer.invertAtomCoord(invPlane, bsAtoms);
			if (rotAxis == null)
				return;
		}
		if (nPoints < 2) {
			if (!isMolecular) {
				// fixed-frame rotation
				// rotate x 10 # Chime-like
				// rotate axisangle {0 1 0} 10
				// rotate x 10 (atoms) # point-centered
				// rotate x 10 $object # point-centered
				viewer.rotateAxisAngleAtCenter(points[0], rotAxis, rate,
						endDegrees, isSpin, bsAtoms);
				return;
			}
			if (nPoints == 0)
				points[0] = new Point3f();
			// rotate MOLECULAR
			// rotate MOLECULAR (atom1)
			// rotate MOLECULAR x 10 (atom1)
			// rotate axisangle MOLECULAR (atom1)
			points[1] = new Point3f(points[0]);
			points[1].add(rotAxis);
			nPoints = 2;
		}
		if (nPoints == 0)
			points[0] = new Point3f();
		if (nPoints < 2 || points[0].distance(points[1]) == 0) {
			points[1] = new Point3f(points[0]);
			points[1].y += 1.0;
		}
		if (endDegrees == Float.MAX_VALUE)
			endDegrees = 0;
		if (endDegrees != 0 && translation != null && !haveRotation)
			translation.scale(endDegrees / translation.length());
		if (isSpin && translation != null
				&& (endDegrees == 0 || degreesPerSecond == 0)) {
			// need a token rotation
			endDegrees = 0.01f;
			rate = (degreesPerSecond == Float.MIN_VALUE ? 0.01f
					: degreesPerSecond < 0 ?
					// -n means number of seconds, not degreesPerSecond
					-endDegrees / degreesPerSecond
							: degreesPerSecond * 0.01f / translation.length());
			degreesPerSecond = 0.01f;
		}
		if (bsAtoms != null && isSpin && ptsB == null && m4 != null) {
			ptsA = viewer.getAtomPointVector(bsAtoms);
			ptsB = Measure.transformPoints(ptsA, m4, points[0]);
		}
		if (bsAtoms != null && !isSpin && ptsB != null)
			viewer.setAtomCoord(bsAtoms, Token.xyz, ptsB);
		else
			viewer.rotateAboutPointsInternal(points[0], points[1], rate,
					endDegrees, isSpin, bsAtoms, translation, ptsB);
	}

	private Quaternion getQuaternionParameter(int i) throws ScriptException {
		if (tokAt(i) == Token.varray) {
			List<ScriptVariable> sv = ((ScriptVariable) getToken(i)).getList();
			Point4f p4 = null;
			if (sv.size() == 0
					|| (p4 = ScriptVariable.pt4Value(sv.get(0))) == null)
				error(ERROR_invalidArgument);
			return new Quaternion(p4);
		}
		return new Quaternion(getPoint4f(i));
	}

	List<Point3f> getPointVector(Token t, int i) throws ScriptException {
		switch (t.tok) {
		case Token.bitset:
			return viewer.getAtomPointVector((BitSet) t.value);
		case Token.varray:
			List<Point3f> data = new ArrayList<Point3f>();
			Point3f pt;
			List<ScriptVariable> pts = ((ScriptVariable) t).getList();
			for (int j = 0; j < pts.size(); j++)
				if ((pt = ScriptVariable.ptValue(pts.get(j))) != null)
					data.add(pt);
				else
					return null;
			return data;
		}
		if (i > 0)
			return viewer.getAtomPointVector(atomExpressionAt(i));
		return null;
	}

	private Point3f getObjectCenter(String axisID, int index, int modelIndex) {
		Object[] data = new Object[] { axisID, Integer.valueOf(index),
				Integer.valueOf(modelIndex) };
		return (getShapeProperty(JmolConstants.SHAPE_DRAW, "getCenter", data)
				|| getShapeProperty(JmolConstants.SHAPE_ISOSURFACE,
						"getCenter", data)
				|| getShapeProperty(JmolConstants.SHAPE_CONTACT, "getCenter",
						data)
				|| getShapeProperty(JmolConstants.SHAPE_MO, "getCenter", data) ? (Point3f) data[2]
				: null);
	}

	private Point3f[] getObjectBoundingBox(String id) {
		Object[] data = new Object[] { id, null, null };
		return (getShapeProperty(JmolConstants.SHAPE_ISOSURFACE,
				"getBoundingBox", data)
				|| getShapeProperty(JmolConstants.SHAPE_CONTACT,
						"getBoundingBox", data)
				|| getShapeProperty(JmolConstants.SHAPE_MO, "getBoundingBox",
						data) ? (Point3f[]) data[2] : null);
	}

	private Vector3f getDrawObjectAxis(String axisID, int index) {
		Object[] data = new Object[] { axisID, Integer.valueOf(index), null };
		return (getShapeProperty(JmolConstants.SHAPE_DRAW, "getSpinAxis", data) ? (Vector3f) data[2]
				: null);
	}

	private void script(int tok, String filename, boolean listCommands)
			throws ScriptException {
		boolean loadCheck = true;
		boolean isCheck = false;
		boolean doStep = false;
		int lineNumber = 0;
		int pc = 0;
		int lineEnd = 0;
		int pcEnd = 0;
		int i = 2;
		String theScript = null;
		String localPath = null;
		String remotePath = null;
		String scriptPath = null;
		List<ScriptVariable> params = null;

		if (tok == Token.javascript) {
			checkLength(2);
			if (!isSyntaxCheck)
				viewer.jsEval(parameterAsString(1));
			return;
		}
		if (filename == null) {
			tok = tokAt(1);
			if (tok != Token.string)
				error(ERROR_filenameExpected);
			filename = parameterAsString(1);
			if (filename.equalsIgnoreCase("applet")) {
				// script APPLET x "....."
				String appID = parameterAsString(2);
				theScript = parameterExpressionString(3, 0); // had _script
																// variable??
				checkLast(iToken);
				if (isSyntaxCheck)
					return;
				if (appID.length() == 0 || appID.equals("all"))
					appID = "*";
				if (!appID.equals(".")) {
					viewer.jsEval(appID + "\1" + theScript);
					if (!appID.equals("*"))
						return;
				}
			} else {
				tok = tokAt(statementLength - 1);
				doStep = (tok == Token.step);
				if (filename.equalsIgnoreCase("inline")) {
					theScript = parameterExpressionString(2,
							(doStep ? statementLength - 1 : 0));
					i = iToken + 1;
				}
				while (filename.equalsIgnoreCase("localPath")
						|| filename.equalsIgnoreCase("remotePath")
						|| filename.equalsIgnoreCase("scriptPath")) {
					if (filename.equalsIgnoreCase("localPath"))
						localPath = parameterAsString(i++);
					else if (filename.equalsIgnoreCase("scriptPath"))
						scriptPath = parameterAsString(i++);
					else
						remotePath = parameterAsString(i++);
					filename = parameterAsString(i++);
				}
				if ((tok = tokAt(i)) == Token.check) {
					isCheck = true;
					tok = tokAt(++i);
				}
				if (tok == Token.noload) {
					loadCheck = false;
					tok = tokAt(++i);
				}
				if (tok == Token.line || tok == Token.lines) {
					i++;
					lineEnd = lineNumber = Math.max(intParameter(i++), 0);
					if (checkToken(i)) {
						if (getToken(i).tok == Token.minus)
							lineEnd = (checkToken(++i) ? intParameter(i++) : 0);
						else
							lineEnd = -intParameter(i++);
						if (lineEnd <= 0)
							error(ERROR_invalidArgument);
					}
				} else if (tok == Token.command || tok == Token.commands) {
					i++;
					pc = Math.max(intParameter(i++) - 1, 0);
					pcEnd = pc + 1;
					if (checkToken(i)) {
						if (getToken(i).tok == Token.minus)
							pcEnd = (checkToken(++i) ? intParameter(i++) : 0);
						else
							pcEnd = -intParameter(i++);
						if (pcEnd <= 0)
							error(ERROR_invalidArgument);
					}
				}
				if (tokAt(i) == Token.leftparen) {
					params = parameterExpressionList(i, -1, false);
					i = iToken + 1;
				}
				checkLength(doStep ? i + 1 : i);
			}
		}

		// processing

		if (isSyntaxCheck && !isCmdLine_c_or_C_Option)
			return;
		if (isCmdLine_c_or_C_Option)
			isCheck = true;
		boolean wasSyntaxCheck = isSyntaxCheck;
		boolean wasScriptCheck = isCmdLine_c_or_C_Option;
		if (isCheck)
			isSyntaxCheck = isCmdLine_c_or_C_Option = true;
		pushContext(null);
		contextPath += " >> " + filename;
		if (theScript == null ? compileScriptFileInternal(filename, localPath,
				remotePath, scriptPath) : compileScript(null, theScript, false)) {
			this.pcEnd = pcEnd;
			this.lineEnd = lineEnd;
			while (pc < lineNumbers.length && lineNumbers[pc] < lineNumber)
				pc++;
			this.pc = pc;
			boolean saveLoadCheck = isCmdLine_C_Option;
			isCmdLine_C_Option &= loadCheck;
			executionStepping |= doStep;

			contextVariables = new Hashtable<String, ScriptVariable>();
			contextVariables.put("_arguments", ScriptVariable
					.getVariable(params == null ? new int[] {} : params));

			instructionDispatchLoop(isCheck || listCommands);
			if (debugScript && viewer.getMessageStyleChime())
				viewer.scriptStatus("script <exiting>");
			isCmdLine_C_Option = saveLoadCheck;
			popContext(false, false);
		} else {
			Logger.error(GT._("script ERROR: ") + errorMessage);
			popContext(false, false);
			if (wasScriptCheck) {
				setErrorMessage(null);
			} else {
				evalError(null, null);
			}
		}

		isSyntaxCheck = wasSyntaxCheck;
		isCmdLine_c_or_C_Option = wasScriptCheck;
	}

	private void function() throws ScriptException {
		if (isSyntaxCheck && !isCmdLine_c_or_C_Option)
			return;
		String name = (String) getToken(0).value;
		if (!viewer.isFunction(name))
			error(ERROR_commandExpected);
		List<ScriptVariable> params = (statementLength == 1
				|| statementLength == 3 && tokAt(1) == Token.leftparen
				&& tokAt(2) == Token.rightparen ? null
				: parameterExpressionList(1, -1, false));
		if (isSyntaxCheck)
			return;
		runFunction(null, name, params, null, false, true);
	}

	private void sync() throws ScriptException {
		// new 11.3.9
		checkLength(-3);
		String text = "";
		String applet = "";
		switch (statementLength) {
		case 1:
			applet = "*";
			text = "ON";
			break;
		case 2:
			applet = parameterAsString(1);
			if (applet.indexOf("jmolApplet") == 0
					|| Parser.isOneOf(applet, "*;.;^")) {
				text = "ON";
				if (!isSyntaxCheck)
					viewer.syncScript(text, applet, 0);
				applet = ".";
				break;
			}
			if (tokAt(1) == Token.integer) {
				// start/stop server on port <nnnn>
				if (!isSyntaxCheck)
					viewer.syncScript(null, null, intParameter(1));
				return;
			}
			text = applet;
			applet = "*";
			break;
		case 3:
			if (isSyntaxCheck)
				return;
			applet = parameterAsString(1);
			text = (tokAt(2) == Token.stereo ? Viewer.SYNC_GRAPHICS_MESSAGE
					: parameterAsString(2));
			if (tokAt(1) == Token.integer) {
				// send to server on port <nnnn>
				viewer.syncScript(text, null, intParameter(1));
				return;
			}
			break;
		}
		if (isSyntaxCheck)
			return;
		viewer.syncScript(text, applet, 0);
	}

	private void history(int pt) throws ScriptException {
		// history or set history
		if (statementLength == 1) {
			// show it
			showString(viewer.getSetHistory(Integer.MAX_VALUE));
			return;
		}
		if (pt == 2) {
			// set history n; n' = -2 - n; if n=0, then set history OFF
			int n = intParameter(checkLast(2));
			if (n < 0)
				error(ERROR_invalidArgument);
			if (!isSyntaxCheck)
				viewer.getSetHistory(n == 0 ? 0 : -2 - n);
			return;
		}
		switch (getToken(checkLast(1)).tok) {
		// pt = 1 history ON/OFF/CLEAR
		case Token.on:
		case Token.clear:
			if (!isSyntaxCheck)
				viewer.getSetHistory(Integer.MIN_VALUE);
			return;
		case Token.off:
			if (!isSyntaxCheck)
				viewer.getSetHistory(0);
			break;
		default:
			error(ERROR_keywordExpected, "ON, OFF, CLEAR");
		}
	}

	private void display(boolean isDisplay) throws ScriptException {
		BitSet bs = null;
		Boolean addRemove = null;
		int i = 1;
		int tok;
		switch (tok = tokAt(1)) {
		case Token.add:
		case Token.remove:
			addRemove = Boolean.valueOf(tok == Token.add);
			tok = tokAt(++i);
			break;
		}
		boolean isGroup = (tok == Token.group);
		if (isGroup)
			tok = tokAt(++i);
		switch (tok) {
		case Token.dollarsign:
			setObjectProperty();
			return;
		case Token.nada:
			break;
		default:
			if (statementLength == 4 && tokAt(2) == Token.bonds)
				bs = new BondSet(BitSetUtil.newBitSet(0, viewer.getModelSet()
						.getBondCount()));
			else
				bs = atomExpressionAt(i);
		}
		if (isSyntaxCheck)
			return;
		if (bs instanceof BondSet) {
			viewer.displayBonds((BondSet) bs, isDisplay);
			return;
		}
		viewer.display(bs, isDisplay, isGroup, addRemove, tQuiet);
	}

	private void delete() throws ScriptException {
		if (statementLength == 1) {
			zap(true);
			return;
		}
		if (tokAt(1) == Token.dollarsign) {
			setObjectProperty();
			return;
		}
		BitSet bs = atomExpression(statement, 1, 0, true, false, true, false);
		if (isSyntaxCheck)
			return;
		int nDeleted = viewer.deleteAtoms(bs, false);
		if (!(tQuiet || scriptLevel > scriptReportingLevel))
			scriptStatusOrBuffer(GT._("{0} atoms deleted", nDeleted));
	}

	private void minimize() throws ScriptException {
		BitSet bsSelected = null;
		int steps = Integer.MAX_VALUE;
		float crit = 0;
		boolean addHydrogen = false;
		boolean isSilent = false;
		BitSet bsFixed = null;
		MinimizerInterface minimizer = viewer.getMinimizer(false);
		// may be null
		for (int i = 1; i < statementLength; i++)
			switch (getToken(i).tok) {
			case Token.addhydrogens:
				addHydrogen = true;
				continue;
			case Token.cancel:
			case Token.stop:
				checkLength(2);
				if (isSyntaxCheck || minimizer == null)
					return;
				minimizer.setProperty(parameterAsString(i), null);
				return;
			case Token.clear:
				checkLength(2);
				if (isSyntaxCheck || minimizer == null)
					return;
				minimizer.setProperty("clear", null);
				return;
			case Token.constraint:
				if (i != 1)
					error(ERROR_invalidArgument);
				int n = 0;
				float targetValue = 0;
				int[] aList = new int[5];
				if (tokAt(++i) == Token.clear) {
					checkLength(3);
				} else {
					while (n < 4 && !isFloatParameter(i)) {
						aList[++n] = atomExpressionAt(i).nextSetBit(0);
						i = iToken + 1;
					}
					aList[0] = n;
					if (n == 1)
						error(ERROR_invalidArgument);
					targetValue = floatParameter(checkLast(i));
				}
				if (!isSyntaxCheck)
					viewer.getMinimizer(true).setProperty(
							"constraint",
							new Object[] { aList, new int[n],
									Float.valueOf(targetValue) });
				return;
			case Token.criterion:
				crit = floatParameter(++i);
				continue;
			case Token.energy:
				steps = 0;
				continue;
			case Token.fixed:
				if (i != 1)
					error(ERROR_invalidArgument);
				bsFixed = atomExpressionAt(++i);
				if (bsFixed.nextSetBit(0) < 0)
					bsFixed = null;
				i = iToken;
				if (!isSyntaxCheck)
					viewer.getMinimizer(true).setProperty("fixed", bsFixed);
				if (i + 1 == statementLength)
					return;
				continue;
			case Token.select:
				bsSelected = atomExpressionAt(++i);
				i = iToken;
				continue;
			case Token.silent:
				isSilent = true;
				break;
			case Token.step:
				steps = intParameter(++i);
				continue;
			default:
				error(ERROR_invalidArgument);
				break;
			}
		if (!isSyntaxCheck)
			viewer.minimize(steps, crit, bsSelected, bsFixed, 0, addHydrogen,
					isSilent, false);
	}

	// added -hcf

	private boolean checkGselectCommand() {
		if (statement.length != 10) {
			return false;
		} else {
			if (statement[2].intValue >= 0 && statement[3].value.equals("or")
					&& statement[4].intValue >= 0
					&& statement[5].value.equals("or")
					&& statement[6].intValue >= 0
					&& statement[7].value.equals("or")
					&& statement[8].intValue >= 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	// "gselect" command calls "select" command to complete its function.
	private void gselect() throws ScriptException {

		Atom[] currentUnits = viewer.getModelSet().atoms;

		// NOTE this is called by restrict()
		if (statementLength == 1 || currentUnits == null) {
			viewer.select(null, false, null, tQuiet
					|| scriptLevel > scriptReportingLevel);
			return;
		}
		// Check the input gselect command
		boolean checkGselectCommand = checkGselectCommand();
		if (!checkGselectCommand) {
			error(ERROR_wrongSelectCommand);
			return;
		}

		String fileName = viewer.getFullPathName();

		Pattern patternGssType = Pattern.compile("\\.gss$");
		Matcher checkGssType = patternGssType.matcher(fileName);

		// for Gss type files
		if (checkGssType.find()) {
			Pattern patternGss = Pattern.compile("\\.gs\\.gss");
			Pattern patternCrs = Pattern.compile("\\.cs\\.gss");
			Pattern patternLcs = Pattern.compile("\\.ls\\.gss");
			Pattern patternFbs = Pattern.compile("\\.fs\\.gss");

			Matcher gssMatcher = patternGss.matcher(fileName);
			Matcher crsMatcher = patternCrs.matcher(fileName);
			Matcher lcsMatcher = patternLcs.matcher(fileName);
			Matcher fbsMatcher = patternFbs.matcher(fileName);

			int currentSelectedScale = 0;
			// locate next-scale file name
			if (gssMatcher.find()) {
				currentSelectedScale = 1;
			} else if (crsMatcher.find()) {
				currentSelectedScale = 2;
			} else if (lcsMatcher.find()) {
				currentSelectedScale = 3;
			} else if (fbsMatcher.find()) {
				currentSelectedScale = 4;
			} else {
				// -TODO
			}

			BitSet gbs = gselectExpressionAt(currentSelectedScale,
					currentUnits, statement);

			// generate "select command" based on gbs => change the "statement"
			if (generateSelectStatement(gbs)) {
				statementLength = statement.length;
				select(1);
			} else {
				error(ERROR_wrongSelectCommand);
				return;
			}
		}
	}

	// transform gselect command into select command & refine the statement
	private boolean generateSelectStatement(BitSet gbs) {
		int selectedUnitNum = gbs.cardinality();
		if (selectedUnitNum == 0) {
			statement = new Token[4];
			statement[0] = new Token(Token.select, "select");
			statement[1] = new Token(Token.expressionBegin,
					"implicitExpressionBegin");
			statement[2] = new Token(Token.none, "none");
			statement[3] = new Token(Token.expressionEnd, "expressionEnd");
			return true;
		} else if (selectedUnitNum > 0) {
			List<String> idList = new ArrayList<String>();
			int preI = Integer.MIN_VALUE;
			for (int i = 0; i < gbs.length(); i++) {
				if (gbs.get(i)) {
					if (preI == Integer.MIN_VALUE) {
						preI = i;
						idList.add(String.valueOf(i));
					} else if (i - preI == 1) {
						idList.add(String.valueOf(i));
					} else if (i - preI != 1) {
						idList.add(",");
						idList.add(String.valueOf(i));
					}
					preI = i;
				}
			}
			List<String> argList = new ArrayList<String>();
			List<String> tempList = new ArrayList<String>();
			for (int i = 0; i < idList.size(); i++) {
				if (idList.get(i).equals(",") || i == idList.size() - 1) {
					if (i == idList.size() - 1) {
						tempList.add(idList.get(i));
					}
					if (tempList.size() == 1) {
						argList.add(tempList.get(0));
					} else if (tempList.size() > 1) {
						argList.add(tempList.get(0));
						argList.add("-");
						argList.add(tempList.get(tempList.size() - 1));
						tempList.clear();
					}
				} else if (!idList.get(i).equals(",")) {
					tempList.add(idList.get(i));
				}
			}

			// generate the statement
			String[] argParas = argList.toArray(new String[0]);
			// count '-'
			int countH = 0;
			for (int i = 0; i < argParas.length; i++) {
				if (argParas[i].equals("-")) {
					countH++;
				}
			}
			statement = new Token[argParas.length - countH + 3];
			statement[0] = new Token(Token.select, "select");
			statement[1] = new Token(Token.expressionBegin,
					"implicitExpressionBegin");
			if (countH > 0) {
				for (int i = 0; i < argParas.length; i++) {
					if (isNumeric(argParas[i]) && argParas[i + 1].equals("-")
							&& isNumeric(argParas[i + 2])) {
						statement[2 + i] = new Token(Token.spec_seqcode_range,
								Integer.parseInt(argParas[i]) + 1,
								Integer.MAX_VALUE);
						statement[2 + i + 1] = new Token(Token.spec_seqcode,
								Integer.parseInt(argParas[i + 2]) + 1,
								Integer.MAX_VALUE);
						i = i + 2;
					} else if (isNumeric(argParas[i])) {
						if (i > 0) {
							statement[2 + i] = Token.tokenOr;
							statement[2 + i + 1] = new Token(
									Token.spec_seqcode,
									Integer.parseInt(argParas[i]) + 1,
									Integer.MAX_VALUE);
						} else if (i == 0) {
							statement[2 + i] = new Token(Token.spec_seqcode,
									Integer.parseInt(argParas[i]) + 1,
									Integer.MAX_VALUE);
						}
					}
				}
			} else {
				for (int i = 0; i < argParas.length; i++) {
					if (isNumeric(argParas[i])) {
						if (i > 0) {
							statement[2 + i] = Token.tokenOr;
							statement[2 + i + 1] = new Token(
									Token.spec_seqcode,
									Integer.parseInt(argParas[i]) + 1,
									Integer.MAX_VALUE);
						} else if (i == 0) {
							statement[2 + i] = new Token(Token.spec_seqcode,
									Integer.parseInt(argParas[i]) + 1,
									Integer.MAX_VALUE);
						}
					}
				}
			}

			statement[argParas.length - countH + 2] = new Token(
					Token.expressionEnd, "expressionEnd");

			return true;
		}
		return false;
	}

	// calculate the selected units based on "currenSelectedScale","statement" &
	// current "AtomSetCollection";
	private BitSet gselectExpressionAt(int currentSelectedScale,
			Atom[] currentUnits, Token[] code) {
		BitSet selectedUnits = new BitSet();
		if (code != statement) {
			statement = code;
		}

		switch (currentSelectedScale) {
		case 1:
			if (statement[2].intValue >= 0 && statement[4].intValue == 0
					&& statement[6].intValue == 0 && statement[8].intValue == 0) {
				int selectedChr = statement[2].intValue;
				for (Atom singleUnit : currentUnits) {
					if ((selectedChr != 0 && singleUnit.chrScaleNumber == selectedChr)
							|| selectedChr == 0) {
						selectedUnits.set(singleUnit.index);
					}
				}
				return selectedUnits;
			} else {
				return selectedUnits;
			}
		case 2:
			if (statement[2].intValue >= 0 && statement[4].intValue >= 0
					&& statement[6].intValue == 0 && statement[8].intValue == 0) {
				int selectedChr = statement[2].intValue;
				int selectedLoci = statement[4].intValue;
				for (Atom singleUnit : currentUnits) {
					if ((selectedChr != 0 && singleUnit.chrScaleNumber == selectedChr)
							|| selectedChr == 0) {
						if ((selectedLoci != 0 && singleUnit.lociScaleNumber == selectedLoci)
								|| selectedLoci == 0) {
							selectedUnits.set(singleUnit.index);
						}
					}
				}
				return selectedUnits;
			} else {
				return selectedUnits;
			}
		case 3:
			if (statement[2].intValue >= 0 && statement[4].intValue >= 0
					&& statement[6].intValue >= 0 && statement[8].intValue == 0) {
				int selectedChr = statement[2].intValue;
				int selectedLoci = statement[4].intValue;
				int selectedFiber = statement[6].intValue;
				for (Atom singleUnit : currentUnits) {
					if ((selectedChr != 0 && singleUnit.chrScaleNumber == selectedChr)
							|| selectedChr == 0) {
						if ((selectedLoci != 0 && singleUnit.lociScaleNumber == selectedLoci)
								|| selectedLoci == 0) {
							if ((selectedFiber != 0 && singleUnit.fiberScaleNumber == selectedFiber)
									|| selectedFiber == 0) {
								selectedUnits.set(singleUnit.index);
							}
						}
					}
				}
				return selectedUnits;
			} else {
				return selectedUnits;
			}
		case 4:
			if (statement[2].intValue >= 0 && statement[4].intValue >= 0
					&& statement[6].intValue >= 0 && statement[8].intValue >= 0) {
				int selectedChr = statement[2].intValue;
				int selectedLoci = statement[4].intValue;
				int selectedFiber = statement[6].intValue;
				int selectedNucleo = statement[8].intValue;
				for (Atom singleUnit : currentUnits) {
					if ((selectedChr != 0 && singleUnit.chrScaleNumber == selectedChr)
							|| selectedChr == 0) {
						if ((selectedLoci != 0 && singleUnit.lociScaleNumber == selectedLoci)
								|| selectedLoci == 0) {
							if ((selectedFiber != 0 && singleUnit.fiberScaleNumber == selectedFiber)
									|| selectedFiber == 0) {
								if ((selectedNucleo != 0 && singleUnit.nucleoScaleNumber == selectedNucleo)
										|| selectedNucleo == 0) {
									selectedUnits.set(singleUnit.index);
								}
							}
						}
					}
				}
				return selectedUnits;
			}
		}
		return selectedUnits;
	}

	private boolean checkSselectCommand() {
		if (statement.length != 8) {
			return false;
		} else {
			if (statement[2].intValue > 0 && statement[3].value.equals("or")
					&& statement[4].intValue > 0
					&& statement[5].value.equals("or")
					&& statement[6].intValue > 0
					&& statement[6].intValue >= statement[4].intValue) {
				return true;
			} else {
				return false;
			}
		}
	}

	// sselect also calls select command
	private void sselect() throws ScriptException {
		Atom[] currentUnits = viewer.getModelSet().atoms;
		// NOTE this is called by restrict()
		if (statementLength == 1 || currentUnits == null) {
			viewer.select(null, false, null, tQuiet
					|| scriptLevel > scriptReportingLevel);
			return;
		}
		// Check the input gselect command
		boolean checkSselectCommand = checkSselectCommand();
		if (!checkSselectCommand) {
			error(ERROR_wrongSelectCommand);
			return;
		}

		String fileName = viewer.getFullPathName();

		Pattern patternGssType = Pattern.compile("\\.gss$");
		Matcher checkGssType = patternGssType.matcher(fileName);

		if (checkGssType.find()) {
			BitSet gbs = sselectExpressionAt(currentUnits, statement);
			// generate "select command" based on gbs => change the "statement"
			if (generateSelectStatement(gbs)) {
				statementLength = statement.length;
				select(1);
			} else {
				error(ERROR_wrongSelectCommand);
				return;
			}
		}
	}

	private BitSet sselectExpressionAt(Atom[] currentUnits, Token[] code) {
		BitSet selectedUnits = new BitSet();
		if (code != statement) {
			statement = code;
		}
		int selectedChr = statement[2].intValue;
		int selectedFrom = statement[4].intValue;
		int selectedEnd = statement[6].intValue;
		for (Atom singleUnit : currentUnits) {
			if (selectedChr == singleUnit.chrScaleNumber
					&& selectedFrom <= singleUnit.getEndPos()
					&& selectedEnd >= singleUnit.getFromPos()) {
				selectedUnits.set(singleUnit.index);
			}
		}

		return selectedUnits;
	}
	
	// added end -hcf
	private void select(int i) throws ScriptException {
		// NOTE this is called by restrict()		
		if (statementLength == 1) {
			viewer.select(null, false, null, tQuiet
					|| scriptLevel > scriptReportingLevel);
			return;
		}
		if (statementLength == 2 && tokAt(1) == Token.only)
			return; // coming from "cartoon only"
		// select beginexpr none endexpr
		viewer.setNoneSelected(statementLength == 4 && tokAt(2) == Token.none);
		// select beginexpr bonds ( {...} ) endex pr
		if (tokAt(2) == Token.bitset && getToken(2).value instanceof BondSet
				|| getToken(2).tok == Token.bonds
				&& getToken(3).tok == Token.bitset) {
			if (statementLength == iToken + 2) {
				if (!isSyntaxCheck)
					viewer.selectBonds((BitSet) theToken.value);
				return;
			}
			error(ERROR_invalidArgument);
		}
		if (getToken(2).tok == Token.measure) {
			if (statementLength == 5 && getToken(3).tok == Token.bitset) {
				if (!isSyntaxCheck)
					setShapeProperty(JmolConstants.SHAPE_MEASURES, "select",
							theToken.value);
				return;
			}
			error(ERROR_invalidArgument);
		}

		BitSet bs = null;
		Boolean addRemove = null;
		boolean isGroup = false;
		if (getToken(1).intValue == 0) {
			Object v = parameterExpressionToken(0).value;
			if (!(v instanceof BitSet))
				error(ERROR_invalidArgument);
			checkLast(iToken);
			bs = (BitSet) v;
		} else {
			int tok = tokAt(i);
			switch (tok) {
			case Token.add:
			case Token.remove:
				addRemove = Boolean.valueOf(tok == Token.add);
				tok = tokAt(++i);
			}
			isGroup = (tok == Token.group);
			if (isGroup)
				tok = tokAt(++i);
			bs = atomExpressionAt(i);
		}
		if (isSyntaxCheck)
			return;
		if (isBondSet) {
			viewer.selectBonds(bs);
		} else {
			if (bs.length() > viewer.getAtomCount()) {
				BitSet bs1 = viewer.getModelUndeletedAtomsBitSet(-1);
				bs1.and(bs);
				bs = bs1;
			}
			viewer.select(bs, isGroup, addRemove, tQuiet
					|| scriptLevel > scriptReportingLevel);

		}

		/*
		 * String fileName = viewer.getFullPathName();
		 * 
		 * Pattern pattern = Pattern.compile(".scale\\d+."); Matcher m1 =
		 * pattern.matcher(fileName); if (m1.find()) { String s1 = m1.group();
		 * String ss1 = s1.substring(6,7); currentSelectedScale =
		 * Integer.parseInt(ss1); }
		 */
		// add end
	}

	private void subset() throws ScriptException {
		BitSet bs = null;
		if (!isSyntaxCheck)
			viewer.setSelectionSubset(null);
		if (statementLength != 1
				&& (statementLength != 4 || !getToken(2).value.equals("off")))
			bs = atomExpressionAt(1);
		if (!isSyntaxCheck)
			viewer.setSelectionSubset(bs);
	}

	private void invertSelected() throws ScriptException {
		// invertSelected POINT
		// invertSelected PLANE
		// invertSelected HKL
		// invertSelected STEREO {sp3Atom} {one or two groups)
		Point3f pt = null;
		Point4f plane = null;
		BitSet bs = null;
		int iAtom = Integer.MIN_VALUE;
		switch (tokAt(1)) {
		case Token.nada:
			if (isSyntaxCheck)
				return;
			bs = viewer.getSelectionSet(false);
			pt = viewer.getAtomSetCenter(bs);
			viewer.invertAtomCoord(pt, bs);
			return;
		case Token.stereo:
			iAtom = atomExpressionAt(2).nextSetBit(0);
			// and only these:
			bs = atomExpressionAt(iToken + 1);
			break;
		case Token.point:
			pt = centerParameter(2);
			break;
		case Token.plane:
			plane = planeParameter(2);
			break;
		case Token.hkl:
			plane = hklParameter(2);
			break;
		}
		checkLength(iToken + 1, 1);
		if (plane == null && pt == null && iAtom == Integer.MIN_VALUE)
			error(ERROR_invalidArgument);
		if (isSyntaxCheck)
			return;
		if (iAtom == -1)
			return;
		viewer.invertSelected(pt, plane, iAtom, bs);
	}

	private void translate(boolean isSelected) throws ScriptException {
		// translate [selected] X|Y|Z x.x [NM|ANGSTROMS]
		// translate [selected] X|Y x.x%
		// translate [selected] X|Y|Z x.x [NM|ANGSTROMS]
		// translate [selected] X|Y x.x%
		// translate {x y z} [{atomExpression}]
		BitSet bs = null;
		int i = 1;
		int i0 = 0;
		if (tokAt(1) == Token.selected) {
			isSelected = true;
			i0 = 1;
			i = 2;
		}
		if (isPoint3f(i)) {
			Point3f pt = getPoint3f(i, true);
			bs = (!isSelected && iToken + 1 < statementLength ? atomExpressionAt(++iToken)
					: null);
			checkLast(iToken);
			if (!isSyntaxCheck)
				viewer.setAtomCoordRelative(pt, bs);
			return;
		}
		char xyz = parameterAsString(i).toLowerCase().charAt(0);
		if ("xyz".indexOf(xyz) < 0)
			error(ERROR_axisExpected);
		float amount = floatParameter(++i);
		char type;
		switch (tokAt(++i)) {
		case Token.nada:
		case Token.bitset:
		case Token.expressionBegin:
			type = '\0';
			break;
		default:
			type = (optParameterAsString(i).toLowerCase() + '\0').charAt(0);
		}
		if (amount == 0 && type != '\0')
			return;
		iToken = i0 + (type == '\0' ? 2 : 3);
		bs = (isSelected ? viewer.getSelectionSet(false)
				: iToken + 1 < statementLength ? atomExpressionAt(++iToken)
						: null);
		checkLast(iToken);
		if (!isSyntaxCheck)
			viewer.translate(xyz, amount, type, bs);
	}

	private void zap(boolean isZapCommand) throws ScriptException {
		if (statementLength == 1 || !isZapCommand) {
			viewer.zap(true, isZapCommand && !isStateScript, true);
			refresh();
			return;
		}
		BitSet bs = atomExpressionAt(1);
		if (isSyntaxCheck)
			return;
		int nDeleted = viewer.deleteAtoms(bs, true);
		boolean isQuiet = (tQuiet || scriptLevel > scriptReportingLevel);
		if (!isQuiet)
			scriptStatusOrBuffer(GT._("{0} atoms deleted", nDeleted));
		viewer.select(null, false, null, isQuiet);
	}

	private void zoom(boolean isZoomTo) throws ScriptException {
		if (!isZoomTo) {
			// zoom
			// zoom on|off
			int tok = (statementLength > 1 ? getToken(1).tok : Token.on);
			switch (tok) {
			case Token.in:
			case Token.out:
				break;
			case Token.on:
			case Token.off:
				if (statementLength > 2)
					error(ERROR_badArgumentCount);
				if (!isSyntaxCheck)
					setBooleanProperty("zoomEnabled", tok == Token.on);
				return;
			}
		}
		Point3f center = null;
		// Point3f currentCenter = viewer.getRotationCenter();
		int i = 1;
		// zoomTo time-sec
		float time = (isZoomTo ? (isFloatParameter(i) ? floatParameter(i++)
				: 2f) : 0f);
		if (time < 0) {
			// zoom -10
			i--;
			time = 0;
		}
		// zoom {x y z} or (atomno=3)
		int ptCenter = 0;
		BitSet bsCenter = null;
		if (isCenterParameter(i)) {
			ptCenter = i;
			center = centerParameter(i);
			if (expressionResult instanceof BitSet)
				bsCenter = (BitSet) expressionResult;
			i = iToken + 1;
		} else if (tokAt(i) == Token.integer && getToken(i).intValue == 0) {
			bsCenter = viewer.getAtomBitSet("visible");
			center = viewer.getAtomSetCenter(bsCenter);
		}

		// disabled sameAtom stuff -- just too weird
		boolean isSameAtom = false;// && (center != null &&
									// currentCenter.distance(center) < 0.1);
		// zoom/zoomTo [0|n|+n|-n|*n|/n|IN|OUT]
		// zoom/zoomTo percent|-factor|+factor|*factor|/factor | 0
		float zoom = viewer.getZoomSetting();

		float newZoom = getZoom(ptCenter, i, bsCenter, zoom);
		i = iToken + 1;
		float xTrans = Float.NaN;
		float yTrans = Float.NaN;
		if (i != statementLength) {
			xTrans = floatParameter(i++);
			yTrans = floatParameter(i++);
		}
		if (i != statementLength)
			error(ERROR_invalidArgument);
		if (newZoom < 0) {
			newZoom = -newZoom; // currentFactor
			if (isZoomTo) {
				// no factor -- check for no center (zoom out) or same center
				// (zoom in)
				if (statementLength == 1 || isSameAtom)
					newZoom *= 2;
				else if (center == null)
					newZoom /= 2;
			}
		}
		float max = viewer.getMaxZoomPercent();
		if (newZoom < 5 || newZoom > max)
			numberOutOfRange(5, max);
		if (!viewer.isWindowCentered()) {
			// do a smooth zoom only if not windowCentered
			if (center != null) {
				BitSet bs = atomExpressionAt(ptCenter);
				if (!isSyntaxCheck)
					viewer.setCenterBitSet(bs, false);
			}
			center = viewer.getRotationCenter();
			if (Float.isNaN(xTrans))
				xTrans = viewer.getTranslationXPercent();
			if (Float.isNaN(yTrans))
				yTrans = viewer.getTranslationYPercent();
		}
		if (isSyntaxCheck)
			return;
		if (Float.isNaN(xTrans))
			xTrans = 0;
		if (Float.isNaN(yTrans))
			yTrans = 0;
		if (isSameAtom && Math.abs(zoom - newZoom) < 1)
			time = 0;
		viewer.moveTo(time, center, JmolConstants.center, Float.NaN, null,
				newZoom, xTrans, yTrans, Float.NaN, null, Float.NaN, Float.NaN,
				Float.NaN);
	}

	private float getZoom(int ptCenter, int i, BitSet bs, float currentZoom)
			throws ScriptException {
		// where [zoom factor] is [0|n|+n|-n|*n|/n|IN|OUT]

		float zoom = (isFloatParameter(i) ? floatParameter(i++) : Float.NaN);
		if (zoom == 0 || currentZoom == 0) {
			// moveTo/zoom/zoomTo {center} 0
			float r = Float.NaN;
			if (bs == null) {
				if (tokAt(ptCenter) == Token.dollarsign) {
					Point3f[] bbox = getObjectBoundingBox(objectNameParameter(ptCenter + 1));
					if (bbox == null
							|| (r = bbox[0].distance(bbox[1]) / 2) == 0)
						error(ERROR_invalidArgument);
				}
			} else {
				r = viewer.calcRotationRadius(bs);
			}
			if (Float.isNaN(r))
				error(ERROR_invalidArgument);
			currentZoom = viewer.getRotationRadius() / r * 100;
			zoom = Float.NaN;
		}
		if (zoom < 0) {
			// moveTo/zoom/zoomTo -factor
			zoom += currentZoom;
		} else if (Float.isNaN(zoom)) {
			// moveTo/zoom/zoomTo [optional {center}]
			// percent|+factor|*factor|/factor
			// moveTo/zoom/zoomTo {center} 0 [optional
			// -factor|+factor|*factor|/factor]
			int tok = tokAt(i);
			switch (tok) {
			case Token.out:
			case Token.in:
				zoom = currentZoom * (tok == Token.out ? 0.5f : 2f);
				i++;
				break;
			case Token.divide:
			case Token.times:
			case Token.plus:
				float value = floatParameter(++i);
				i++;
				switch (tok) {
				case Token.divide:
					zoom = currentZoom / value;
					break;
				case Token.times:
					zoom = currentZoom * value;
					break;
				case Token.plus:
					zoom = currentZoom + value;
					break;
				}
				break;
			default:
				// indicate no factor indicated
				zoom = (bs == null ? -currentZoom : currentZoom);
			}
		}
		iToken = i - 1;
		return zoom;
	}

	private void delay() throws ScriptException {
		long millis = 0;
		switch (getToken(1).tok) {
		case Token.on: // this is auto-provided as a default
			millis = 1;
			break;
		case Token.integer:
			millis = intParameter(1) * 1000;
			break;
		case Token.decimal:
			millis = (long) (floatParameter(1) * 1000);
			break;
		default:
			error(ERROR_numberExpected);
		}
		if (!isSyntaxCheck)
			delay(millis);
	}

	private void delay(long millis) {
		if (viewer.isHeadless())
			return;
		long timeBegin = System.currentTimeMillis();
		refresh();
		int delayMax;
		if (millis < 0)
			millis = -millis;
		else if ((delayMax = viewer.getDelayMaximum()) > 0 && millis > delayMax)
			millis = delayMax;
		millis -= System.currentTimeMillis() - timeBegin;
		int seconds = (int) millis / 1000;
		millis -= seconds * 1000;
		if (millis <= 0)
			millis = 1;
		while (seconds >= 0 && millis > 0 && !interruptExecution
				&& currentThread == Thread.currentThread()) {
			viewer.popHoldRepaint("delay");
			try {
				Thread.sleep((seconds--) > 0 ? 1000 : millis);
			} catch (InterruptedException e) {
			}
			viewer.pushHoldRepaint("delay");
		}
	}

	private void slab(boolean isDepth) throws ScriptException {
		boolean TF = false;
		Point4f plane = null;
		String str;
		if (isCenterParameter(1) || tokAt(1) == Token.point4f)
			plane = planeParameter(1);
		else
			switch (getToken(1).tok) {
			case Token.integer:
				int percent = intParameter(checkLast(1));
				if (!isSyntaxCheck)
					if (isDepth)
						viewer.depthToPercent(percent);
					else
						viewer.slabToPercent(percent);
				return;
			case Token.on:
				checkLength(2);
				TF = true;
				//$FALL-THROUGH$
			case Token.off:
				checkLength(2);
				setBooleanProperty("slabEnabled", TF);
				return;
			case Token.reset:
				checkLength(2);
				if (isSyntaxCheck)
					return;
				viewer.slabReset();
				setBooleanProperty("slabEnabled", true);
				return;
			case Token.set:
				checkLength(2);
				if (isSyntaxCheck)
					return;
				viewer.setSlabDepthInternal(isDepth);
				setBooleanProperty("slabEnabled", true);
				return;
			case Token.minus:
				str = parameterAsString(2);
				if (str.equalsIgnoreCase("hkl"))
					plane = hklParameter(3);
				else if (str.equalsIgnoreCase("plane"))
					plane = planeParameter(3);
				if (plane == null)
					error(ERROR_invalidArgument);
				plane.scale(-1);
				break;
			case Token.plane:
				switch (getToken(2).tok) {
				case Token.none:
					break;
				default:
					plane = planeParameter(2);
				}
				break;
			case Token.hkl:
				plane = (getToken(2).tok == Token.none ? null : hklParameter(2));
				break;
			case Token.reference:
				// only in 11.2; deprecated
				return;
			default:
				error(ERROR_invalidArgument);
			}
		if (!isSyntaxCheck)
			viewer.slabInternal(plane, isDepth);
	}

	/*
	 * private void slice() throws ScriptException{ if(!isSyntaxCheck &&
	 * viewer.slicer==null){ viewer.createSlicer(); } int tok1 =
	 * getToken(1).tok; if(tok1==Token.left||tok1==Token.right){ switch
	 * (getToken(2).tok){ case Token.on: if(isSyntaxCheck) return;
	 * viewer.slicer.drawSlicePlane(tok1, true); return; case Token.off:
	 * if(isSyntaxCheck) return; viewer.slicer.drawSlicePlane(tok1, false);
	 * return; default: error(ERROR_invalidArgument); break; } }else{//command
	 * to slice object, not show slice planes String name =
	 * (String)getToken(1).value; //TODO - should accept "all" for now "all"
	 * will fail silently. // Should check it is a valid isosurface name
	 * //Should be followed by two angles, and two percents (float values)
	 * float[] param = new float[4]; for (int i=2;i<6;++i){ if(getToken(i).tok
	 * == Token.decimal){ param[i-2]=floatParameter(i); } else{
	 * error(ERROR_invalidArgument); } } if(!isSyntaxCheck){
	 * viewer.slicer.setSlice(param[0], param[1], param[2], param[3]);
	 * viewer.slicer.sliceObject(name); } return; } }
	 */

	private void ellipsoid() throws ScriptException {
		int mad = 0;
		int i = 1;
		switch (getToken(1).tok) {
		case Token.on:
			mad = 50;
			break;
		case Token.off:
			break;
		case Token.integer:
			mad = intParameter(1);
			break;
		case Token.set:
			checkLength(3);
			shapeManager.loadShape(JmolConstants.SHAPE_ELLIPSOIDS);
			setShapeProperty(JmolConstants.SHAPE_ELLIPSOIDS, "select",
					Integer.valueOf(intParameter(2, 1, 3)));
			return;
		case Token.id:
		case Token.times:
		case Token.identifier:
			shapeManager.loadShape(JmolConstants.SHAPE_ELLIPSOIDS);
			if (theTok == Token.id)
				i++;
			setShapeId(JmolConstants.SHAPE_ELLIPSOIDS, i, false);
			i = iToken;
			while (++i < statementLength) {
				String key = parameterAsString(i);
				Object value = null;
				switch (tokAt(i)) {
				case Token.axes:
					Vector3f[] axes = new Vector3f[3];
					for (int j = 0; j < 3; j++) {
						axes[j] = new Vector3f();
						axes[j].set(centerParameter(++i));
						i = iToken;
					}
					value = axes;
					break;
				case Token.center:
					value = centerParameter(++i);
					i = iToken;
					break;
				case Token.color:
					float translucentLevel = Float.NaN;
					if (tokAt(i) == Token.color)
						i++;
					if ((theTok = tokAt(i)) == Token.translucent) {
						value = "translucent";
						if (isFloatParameter(++i))
							translucentLevel = getTranslucentLevel(i++);
						else
							translucentLevel = viewer.getDefaultTranslucent();
					} else if (theTok == Token.opaque) {
						value = "opaque";
						i++;
					}
					if (isColorParam(i)) {
						setShapeProperty(JmolConstants.SHAPE_ELLIPSOIDS,
								"color", Integer.valueOf(getArgbParam(i)));
						i = iToken;
					}
					if (value == null)
						continue;
					if (!Float.isNaN(translucentLevel))
						setShapeProperty(JmolConstants.SHAPE_ELLIPSOIDS,
								"translucentLevel",
								Float.valueOf(translucentLevel));
					key = "translucency";
					break;
				case Token.delete:
					value = Boolean.TRUE;
					checkLength(3);
					break;
				case Token.modelindex:
					value = Integer.valueOf(intParameter(++i));
					break;
				case Token.on:
					value = Boolean.TRUE;
					break;
				case Token.off:
					key = "on";
					value = Boolean.FALSE;
					break;
				case Token.scale:
					value = Float.valueOf(floatParameter(++i));
					break;
				}
				if (value == null)
					error(ERROR_invalidArgument);
				setShapeProperty(JmolConstants.SHAPE_ELLIPSOIDS,
						key.toLowerCase(), value);
			}
			setShapeProperty(JmolConstants.SHAPE_ELLIPSOIDS, "thisID", null);
			return;
		default:
			error(ERROR_invalidArgument);
		}
		setShapeSize(JmolConstants.SHAPE_ELLIPSOIDS, mad, null);
	}

	private String getShapeNameParameter(int i) throws ScriptException {
		String id = parameterAsString(i);
		boolean isWild = id.equals("*");
		if (id.length() == 0)
			error(ERROR_invalidArgument);
		if (isWild) {
			switch (tokAt(i + 1)) {
			case Token.nada:
			case Token.on:
			case Token.off:
			case Token.displayed:
			case Token.hidden:
			case Token.color:
			case Token.delete:
				break;
			default:
				if (setMeshDisplayProperty(-1, 0, tokAt(i + 1)))
					break;
				id += optParameterAsString(++i);
			}
		}
		if (tokAt(i + 1) == Token.times)
			id += parameterAsString(++i);
		iToken = i;
		return id;
	}

	private String setShapeId(int iShape, int i, boolean idSeen)
			throws ScriptException {
		if (idSeen)
			error(ERROR_invalidArgument);
		String name = getShapeNameParameter(i).toLowerCase();
		setShapeProperty(iShape, "thisID", name);
		return name;
	}

	private void setAtomShapeSize(int shape, float scale)
			throws ScriptException {
		// halo star spacefill
		RadiusData rd = null;
		int tok = tokAt(1);
		boolean isOnly = false;
		switch (tok) {
		case Token.only:
			restrictSelected(false, false);
			break;
		case Token.on:
			break;
		case Token.off:
			scale = 0;
			break;
		case Token.decimal:
			isOnly = (floatParameter(1) < 0);
			//$FALL-THROUGH$
		case Token.integer:
		default:
			rd = encodeRadiusParameter(1, isOnly, true);
			if (Float.isNaN(rd.value))
				error(ERROR_invalidArgument);
		}
		if (rd == null)
			rd = new RadiusData(scale, EnumType.FACTOR, EnumVdw.AUTO);
		if (isOnly)
			restrictSelected(false, false);
		setShapeSize(shape, rd);
	}

	/*
	 * Based on the form of the parameters, returns and encoded radius as
	 * follows:
	 * 
	 * script meaning range
	 * 
	 * +1.2 offset [0 - 10] -1.2 offset 0) 1.2 absolute (0 - 10] -30% 70% (-100
	 * - 0) +30% 130% (0 80% percent (0
	 */

	private RadiusData encodeRadiusParameter(int index, boolean isOnly,
			boolean allowAbsolute) throws ScriptException {

		float value = Float.NaN;
		EnumType factorType = EnumType.ABSOLUTE;
		EnumVdw vdwType = null;

		int tok = (index == -1 ? Token.vanderwaals : getToken(index).tok);
		switch (tok) {
		case Token.adpmax:
		case Token.adpmin:
		case Token.ionic:
		case Token.hydrophobic:
		case Token.temperature:
		case Token.vanderwaals:
			value = 1;
			factorType = EnumType.FACTOR;
			vdwType = (tok == Token.vanderwaals ? null : EnumVdw
					.getVdwType2(Token.nameOf(tok)));
			tok = tokAt(++index);
			break;
		}
		switch (tok) {
		case Token.reset:
			return viewer.getDefaultRadiusData();
		case Token.auto:
		case Token.rasmol:
		case Token.babel:
		case Token.babel21:
		case Token.jmol:
			value = 1;
			factorType = EnumType.FACTOR;
			iToken = index - 1;
			break;
		case Token.plus:
		case Token.integer:
		case Token.decimal:
			if (tok == Token.plus) {
				index++;
			} else if (tokAt(index + 1) == Token.percent) {
				value = Math.round(floatParameter(index));
				iToken = ++index;
				factorType = EnumType.FACTOR;
				if (value < 0 || value > 200)
					integerOutOfRange(0, 200);
				value /= 100;
				break;
			} else if (tok == Token.integer) {
				value = intParameter(index);
				// rasmol 250-scale if positive or percent (again), if negative
				// (deprecated)
				if (value > 749 || value < -200)
					integerOutOfRange(-200, 749);
				if (value > 0) {
					value /= 250;
					factorType = EnumType.ABSOLUTE;
				} else {
					value /= -100;
					factorType = EnumType.FACTOR;
				}
				break;
			}
			value = floatParameter(index,
					(isOnly || !allowAbsolute ? -Atom.RADIUS_MAX : 0),
					Atom.RADIUS_MAX);
			if (tok == Token.plus || !allowAbsolute) {
				factorType = EnumType.OFFSET;
			} else {
				factorType = EnumType.ABSOLUTE;
				vdwType = EnumVdw.NADA;
			}
			if (isOnly)
				value = -value;
			break;
		default:
			if (value == 1)
				index--;
		}
		if (vdwType == null) {
			vdwType = EnumVdw.getVdwType(optParameterAsString(++iToken));
			if (vdwType == null) {
				iToken = index;
				vdwType = EnumVdw.AUTO;
			}
		}
		return new RadiusData(value, factorType, vdwType);
	}

	private void structure() throws ScriptException {
		EnumStructure type = EnumStructure
				.getProteinStructureType(parameterAsString(1));
		if (type == EnumStructure.NOT)
			error(ERROR_invalidArgument);
		BitSet bs = null;
		switch (tokAt(2)) {
		case Token.bitset:
		case Token.expressionBegin:
			bs = atomExpressionAt(2);
			checkLast(iToken);
			break;
		default:
			checkLength(2);
		}
		if (isSyntaxCheck)
			return;
		clearDefinedVariableAtomSets();
		viewer.setProteinType(type, bs);
	}

	private void wireframe() throws ScriptException {
		int mad = Integer.MIN_VALUE;
		if (tokAt(1) == Token.reset)
			checkLast(1);
		else
			mad = getMadParameter();
		if (isSyntaxCheck)
			return;
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_COVALENT_MASK));
		setShapeSize(
				JmolConstants.SHAPE_STICKS,
				mad == Integer.MIN_VALUE ? 2 * JmolConstants.DEFAULT_BOND_MILLIANGSTROM_RADIUS
						: mad, null);
	}

	private void ssbond() throws ScriptException {
		int mad = getMadParameter();
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_SULFUR_MASK));
		setShapeSize(JmolConstants.SHAPE_STICKS, mad, null);
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_COVALENT_MASK));
	}

	private void struts() throws ScriptException {
		boolean defOn = (tokAt(1) == Token.only || tokAt(1) == Token.on || statementLength == 1);
		int mad = getMadParameter();
		if (defOn)
			mad = (int) (viewer.getStrutDefaultRadius() * 2000f);
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_STRUT));
		setShapeSize(JmolConstants.SHAPE_STICKS, mad, null);
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_COVALENT_MASK));
	}

	private void hbond() throws ScriptException {
		if (statementLength == 2 && getToken(1).tok == Token.calculate) {
			if (isSyntaxCheck)
				return;
			int n = viewer.autoHbond(null, null, false);
			scriptStatusOrBuffer(GT._("{0} hydrogen bonds", Math.abs(n)));
			return;
		}
		if (statementLength == 2 && getToken(1).tok == Token.delete) {
			if (isSyntaxCheck)
				return;
			connect(0);
			return;
		}
		int mad = getMadParameter();
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_HYDROGEN_MASK));
		setShapeSize(JmolConstants.SHAPE_STICKS, mad, null);
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_COVALENT_MASK));
	}

	private void configuration() throws ScriptException {
		// if (!isSyntaxCheck && viewer.getDisplayModelIndex() <= -2)
		// error(ERROR_backgroundModelError, "\"CONFIGURATION\"");
		BitSet bsAtoms;
		if (statementLength == 1) {
			bsAtoms = viewer.setConformation();
			viewer.addStateScript("select", null,
					viewer.getSelectionSet(false), null, "configuration", true,
					false);
		} else {
			int n = intParameter(checkLast(1));
			if (isSyntaxCheck)
				return;
			bsAtoms = viewer.getConformation(viewer.getCurrentModelIndex(),
					n - 1, true);
			viewer.addStateScript("configuration " + n + ";", true, false);
		}
		if (isSyntaxCheck)
			return;
		setShapeProperty(JmolConstants.SHAPE_STICKS, "type",
				Integer.valueOf(JmolEdge.BOND_HYDROGEN_MASK));
		setShapeSize(JmolConstants.SHAPE_STICKS, 0, bsAtoms);
		viewer.autoHbond(bsAtoms, bsAtoms, true);
		viewer.select(bsAtoms, false, null, tQuiet);
	}

	private void vector() throws ScriptException {
		EnumType type = EnumType.SCREEN;
		float value = 1;
		checkLength(-3);
		switch (iToken = statementLength) {
		case 1:
			break;
		case 2:
			switch (getToken(1).tok) {
			case Token.on:
				break;
			case Token.off:
				value = 0;
				break;
			case Token.integer:
				// diameter Pixels
				value = intParameter(1, 0, 19);
				break;
			case Token.decimal:
				// radius angstroms
				type = EnumType.ABSOLUTE;
				value = floatParameter(1, 0, 3);
				break;
			default:
				error(ERROR_booleanOrNumberExpected);
			}
			break;
		case 3:
			if (tokAt(1) == Token.scale) {
				setFloatProperty("vectorScale", floatParameter(2, -100, 100));
				return;
			}
		}
		setShapeSize(JmolConstants.SHAPE_VECTORS, new RadiusData(value, type,
				null));
	}

	private void dipole() throws ScriptException {
		// dipole intWidth floatMagnitude OFFSET floatOffset {atom1} {atom2}
		String propertyName = null;
		Object propertyValue = null;
		boolean iHaveAtoms = false;
		boolean iHaveCoord = false;
		boolean idSeen = false;

		shapeManager.loadShape(JmolConstants.SHAPE_DIPOLES);
		if (tokAt(1) == Token.list
				&& listIsosurface(JmolConstants.SHAPE_DIPOLES))
			return;
		setShapeProperty(JmolConstants.SHAPE_DIPOLES, "init", null);
		if (statementLength == 1) {
			setShapeProperty(JmolConstants.SHAPE_DIPOLES, "thisID", null);
			return;
		}
		for (int i = 1; i < statementLength; ++i) {
			propertyName = null;
			propertyValue = null;
			switch (getToken(i).tok) {
			case Token.on:
				propertyName = "on";
				break;
			case Token.off:
				propertyName = "off";
				break;
			case Token.delete:
				propertyName = "delete";
				break;
			case Token.integer:
			case Token.decimal:
				propertyName = "value";
				propertyValue = Float.valueOf(floatParameter(i));
				break;
			case Token.bitset:
				propertyName = "atomBitset";
				//$FALL-THROUGH$
			case Token.expressionBegin:
				if (propertyName == null)
					propertyName = (iHaveAtoms || iHaveCoord ? "endSet"
							: "startSet");
				propertyValue = atomExpressionAt(i);
				i = iToken;
				iHaveAtoms = true;
				break;
			case Token.leftbrace:
			case Token.point3f:
				// {X, Y, Z}
				Point3f pt = getPoint3f(i, true);
				i = iToken;
				propertyName = (iHaveAtoms || iHaveCoord ? "endCoord"
						: "startCoord");
				propertyValue = pt;
				iHaveCoord = true;
				break;
			case Token.bonds:
				propertyName = "bonds";
				break;
			case Token.calculate:
				propertyName = "calculate";
				break;
			case Token.id:
				setShapeId(JmolConstants.SHAPE_DIPOLES, ++i, idSeen);
				i = iToken;
				break;
			case Token.cross:
				propertyName = "cross";
				propertyValue = Boolean.TRUE;
				break;
			case Token.nocross:
				propertyName = "cross";
				propertyValue = Boolean.FALSE;
				break;
			case Token.offset:
				float v = floatParameter(++i);
				if (theTok == Token.integer) {
					propertyName = "offsetPercent";
					propertyValue = Integer.valueOf((int) v);
				} else {
					propertyName = "offset";
					propertyValue = Float.valueOf(v);
				}
				break;
			case Token.offsetside:
				propertyName = "offsetSide";
				propertyValue = Float.valueOf(floatParameter(++i));
				break;

			case Token.val:
				propertyName = "value";
				propertyValue = Float.valueOf(floatParameter(++i));
				break;
			case Token.width:
				propertyName = "width";
				propertyValue = Float.valueOf(floatParameter(++i));
				break;
			default:
				if (theTok == Token.times
						|| Token.tokAttr(theTok, Token.identifier)) {
					setShapeId(JmolConstants.SHAPE_DIPOLES, i, idSeen);
					i = iToken;
					break;
				}
				error(ERROR_invalidArgument);
			}
			idSeen = (theTok != Token.delete && theTok != Token.calculate);
			if (propertyName != null)
				setShapeProperty(JmolConstants.SHAPE_DIPOLES, propertyName,
						propertyValue);
		}
		if (iHaveCoord || iHaveAtoms)
			setShapeProperty(JmolConstants.SHAPE_DIPOLES, "set", null);
	}

	private void animationMode() throws ScriptException {
		float startDelay = 1, endDelay = 1;
		if (statementLength > 5)
			error(ERROR_badArgumentCount);
		EnumAnimationMode animationMode = null;
		switch (getToken(2).tok) {
		case Token.once:
			animationMode = EnumAnimationMode.ONCE;
			startDelay = endDelay = 0;
			break;
		case Token.loop:
			animationMode = EnumAnimationMode.LOOP;
			break;
		case Token.palindrome:
			animationMode = EnumAnimationMode.PALINDROME;
			break;
		default:
			error(ERROR_invalidArgument);
		}
		if (statementLength >= 4) {
			startDelay = endDelay = floatParameter(3);
			if (statementLength == 5)
				endDelay = floatParameter(4);
		}
		if (!isSyntaxCheck)
			viewer.setAnimationReplayMode(animationMode, startDelay, endDelay);
	}

	private void vibration() throws ScriptException {
		checkLength(-3);
		float period = 0;
		switch (getToken(1).tok) {
		case Token.on:
			checkLength(2);
			period = viewer.getVibrationPeriod();
			break;
		case Token.off:
			checkLength(2);
			period = 0;
			break;
		case Token.integer:
		case Token.decimal:
			checkLength(2);
			period = floatParameter(1);
			break;
		case Token.scale:
			setFloatProperty("vibrationScale", floatParameter(2, -10, 10));
			return;
		case Token.period:
			setFloatProperty("vibrationPeriod", floatParameter(2));
			return;
		case Token.identifier:
			error(ERROR_invalidArgument);
			break;
		default:
			period = -1;
		}
		if (period < 0)
			error(ERROR_invalidArgument);
		if (isSyntaxCheck)
			return;
		if (period == 0) {
			viewer.setVibrationOff();
			return;
		}
		viewer.setVibrationPeriod(-period);
	}

	private void animationDirection() throws ScriptException {
		int i = 2;
		int direction = 0;
		switch (tokAt(i)) {
		case Token.minus:
			direction = -intParameter(++i);
			break;
		case Token.plus:
			direction = intParameter(++i);
			break;
		case Token.integer:
			direction = intParameter(i);
			if (direction > 0)
				direction = 0;
			break;
		default:
			error(ERROR_invalidArgument);
		}
		checkLength(++i);
		if (direction != 1 && direction != -1)
			error(ERROR_numberMustBe, "-1", "1");
		if (!isSyntaxCheck)
			viewer.setAnimationDirection(direction);
	}

	private void calculate() throws ScriptException {
		boolean isSurface = false;
		boolean asDSSP = false;
		BitSet bs;
		BitSet bs2 = null;
		int n = Integer.MIN_VALUE;
		if ((iToken = statementLength) >= 2) {
			clearDefinedVariableAtomSets();
			switch (getToken(1).tok) {
			case Token.identifier:
				checkLength(2);
				break;
			case Token.aromatic:
				checkLength(2);
				if (!isSyntaxCheck)
					viewer.assignAromaticBonds();
				return;
			case Token.hbond:
				if (statementLength == 2) {
					if (!isSyntaxCheck) {
						n = viewer.autoHbond(null, null, false);
						break;
					}
					return;
				}
				BitSet bs1 = null;
				// calculate hbonds STRUCTURE -- only the DSSP
				// structurally-defining H bonds
				asDSSP = (tokAt(++iToken) == Token.structure);
				if (asDSSP)
					bs1 = viewer.getSelectionSet(false);
				else
					bs1 = atomExpressionAt(iToken);
				if (!asDSSP && !(asDSSP = (tokAt(++iToken) == Token.structure)))
					bs2 = atomExpressionAt(iToken);
				if (!isSyntaxCheck) {
					n = viewer.autoHbond(bs1, bs2, false);
					break;
				}
				return;
			case Token.hydrogen:
				bs = (statementLength == 2 ? null : atomExpressionAt(2));
				checkLast(iToken);
				if (!isSyntaxCheck)
					viewer.addHydrogens(bs, false, false);
				return;
			case Token.partialcharge:
				iToken = 1;
				bs = (statementLength == 2 ? null : atomExpressionAt(2));
				checkLast(iToken);
				if (!isSyntaxCheck)
					viewer.calculatePartialCharges(bs);
				return;
			case Token.pointgroup:
				pointGroup();
				return;
			case Token.straightness:
				checkLength(2);
				if (!isSyntaxCheck) {
					viewer.calculateStraightness();
					viewer.addStateScript(
							"set quaternionFrame '"
									+ viewer.getQuaternionFrame()
									+ "'; calculate straightness", false, true);
				}
				return;
			case Token.structure:
				bs = (statementLength < 4 ? null : atomExpressionAt(2));
				switch (tokAt(++iToken)) {
				case Token.ramachandran:
					break;
				case Token.dssp:
					asDSSP = true;
					break;
				case Token.nada:
					asDSSP = viewer.getDefaultStructureDSSP();
					break;
				default:
					error(ERROR_invalidArgument);
				}
				if (!isSyntaxCheck)
					showString(viewer.calculateStructures(bs, asDSSP, true));
				return;
			case Token.struts:
				bs = (iToken + 1 < statementLength ? atomExpressionAt(++iToken)
						: null);
				bs2 = (iToken + 1 < statementLength ? atomExpressionAt(++iToken)
						: null);
				checkLength(++iToken);
				if (!isSyntaxCheck) {
					n = viewer.calculateStruts(bs, bs2);
					if (n > 0)
						colorShape(JmolConstants.SHAPE_STRUTS,
								JmolEdge.BOND_STRUT, 0x0FFFFFF, "translucent",
								0.5f, null);
					showString(GT._("{0} struts added", n));
				}
				return;
			case Token.surface:
				isSurface = true;
				// deprecated
				//$FALL-THROUGH$
			case Token.surfacedistance:
			case Token.atomsequence:
				/*
				 * preferred:
				 * 
				 * calculate surfaceDistance FROM {...} calculate
				 * surfaceDistance WITHIN {...}
				 */
				boolean isFrom = false;
				switch (tokAt(2)) {
				case Token.within:
					iToken++;
					break;
				case Token.nada:
					isFrom = !isSurface;
					break;
				case Token.from:
					isFrom = true;
					iToken++;
					break;
				default:
					isFrom = true;
				}
				bs = (iToken + 1 < statementLength ? atomExpressionAt(++iToken)
						: viewer.getSelectionSet(false));
				checkLength(++iToken);
				if (!isSyntaxCheck)
					viewer.calculateSurface(bs, (isFrom ? Float.MAX_VALUE : -1));
				return;
				// Removed in Jmol 13.0.RC4
				// case Token.volume:
				// checkLength(2);
				// if (!isSyntaxCheck) {
				// float val = viewer.getVolume(null, null);
				// showString("" + Math.round(val * 10) / 10f + " A^3; "
				// + Math.round(val * 6.02) / 10f + " cm^3/mole (VDW "
				// + viewer.getDefaultVdwTypeNameOrData(Integer.MIN_VALUE, null)
				// + ")");
				// }
				// return;
			}
			if (n != Integer.MIN_VALUE) {
				scriptStatusOrBuffer(GT._("{0} hydrogen bonds", Math.abs(n)));
				return;
			}
		}
		error(ERROR_what,
				"CALCULATE",
				"aromatic? hbonds? hydrogen? partialCharge? pointgroup? straightness? structure? struts? surfaceDistance FROM? surfaceDistance WITHIN?");
	}

	private void pointGroup() throws ScriptException {
		switch (tokAt(0)) {
		case Token.calculate:
			if (!isSyntaxCheck)
				showString(viewer.calculatePointGroup());
			return;
		case Token.show:
			if (!isSyntaxCheck)
				showString(viewer.getPointGroupAsString(false, null, 0, 0));
			return;
		}
		// draw pointgroup [C2|C3|Cs|Ci|etc.] [n] [scale x]
		int pt = 2;
		String type = (tokAt(pt) == Token.scale ? "" : optParameterAsString(pt));
		float scale = 1;
		int index = 0;
		if (type.length() > 0) {
			if (isFloatParameter(++pt))
				index = intParameter(pt++);
		}
		if (tokAt(pt) == Token.scale)
			scale = floatParameter(++pt);
		if (!isSyntaxCheck)
			runScript(viewer.getPointGroupAsString(true, type, index, scale));
	}

	private void dots(int iShape) throws ScriptException {
		if (!isSyntaxCheck)
			shapeManager.loadShape(iShape);
		setShapeProperty(iShape, "init", null);
		float value = Float.NaN;
		EnumType type = EnumType.ABSOLUTE;
		int ipt = 1;
		switch (getToken(ipt).tok) {
		case Token.only:
			restrictSelected(false, false);
			value = 1;
			type = EnumType.FACTOR;
			break;
		case Token.on:
			value = 1;
			type = EnumType.FACTOR;
			break;
		case Token.off:
			value = 0;
			break;
		case Token.integer:
			int dotsParam = intParameter(ipt);
			if (tokAt(ipt + 1) == Token.radius) {
				ipt++;
				setShapeProperty(iShape, "atom", Integer.valueOf(dotsParam));
				setShapeProperty(iShape, "radius",
						Float.valueOf(floatParameter(++ipt)));
				if (tokAt(++ipt) == Token.color) {
					setShapeProperty(iShape, "colorRGB",
							Integer.valueOf(getArgbParam(++ipt)));
					ipt++;
				}
				if (getToken(ipt).tok != Token.bitset)
					error(ERROR_invalidArgument);
				setShapeProperty(iShape, "dots", statement[ipt].value);
				return;
			}
			break;
		}
		RadiusData rd = (Float.isNaN(value) ? encodeRadiusParameter(ipt, false,
				true) : new RadiusData(value, type, EnumVdw.AUTO));
		if (Float.isNaN(rd.value))
			error(ERROR_invalidArgument);
		setShapeSize(iShape, rd);
	}

	private void proteinShape(int shapeType) throws ScriptException {
		int mad = 0;
		// token has ondefault1
		switch (getToken(1).tok) {
		case Token.only:
			if (isSyntaxCheck)
				return;
			restrictSelected(false, false);
			mad = -1;
			break;
		case Token.on:
			mad = -1; // means take default
			break;
		case Token.off:
			break;
		case Token.structure:
			mad = -2;
			break;
		case Token.temperature:
		case Token.displacement:
			mad = -4;
			break;
		case Token.integer:
			mad = (intParameter(1, 0, 1000) * 8);
			break;
		case Token.decimal:
			mad = (int) (floatParameter(1, -Shape.RADIUS_MAX, Shape.RADIUS_MAX) * 2000);
			if (mad < 0) {
				restrictSelected(false, false);
				mad = -mad;
			}
			break;
		case Token.bitset:
			if (!isSyntaxCheck)
				shapeManager.loadShape(shapeType);
			setShapeProperty(shapeType, "bitset", theToken.value);
			return;
		default:
			error(ERROR_booleanOrNumberExpected);
		}
		setShapeSize(shapeType, mad, null);
	}

	private void animation() throws ScriptException {
		boolean animate = false;
		switch (getToken(1).tok) {
		case Token.on:
			animate = true;
			//$FALL-THROUGH$
		case Token.off:
			if (!isSyntaxCheck)
				viewer.setAnimationOn(animate);
			break;
		case Token.frame:
			frame(2);
			break;
		case Token.mode:
			animationMode();
			break;
		case Token.direction:
			animationDirection();
			break;
		case Token.fps:
			setIntProperty("animationFps", intParameter(checkLast(2)));
			break;
		default:
			frameControl(1);
		}
	}

	private void assign() throws ScriptException {
		int atomsOrBonds = tokAt(1);
		int index = atomExpressionAt(2).nextSetBit(0);
		int index2 = -1;
		String type = null;
		if (index < 0)
			error(ERROR_invalidArgument);
		if (atomsOrBonds == Token.connect) {
			index2 = atomExpressionAt(++iToken).nextSetBit(0);
		} else {
			type = parameterAsString(++iToken);
		}
		Point3f pt = (++iToken < statementLength ? centerParameter(iToken)
				: null);
		if (isSyntaxCheck)
			return;
		switch (atomsOrBonds) {
		case Token.atoms:
			viewer.assignAtom(index, pt, type);
			break;
		case Token.bonds:
			viewer.assignBond(index, (type + "p").charAt(0));
			break;
		case Token.connect:
			viewer.assignConnect(index, index2);
		}
	}

	private void file() throws ScriptException {
		int file = intParameter(checkLast(1));
		if (isSyntaxCheck)
			return;
		int modelIndex = viewer.getModelNumberIndex(file * 1000000 + 1, false,
				false);
		int modelIndex2 = -1;
		if (modelIndex >= 0) {
			modelIndex2 = viewer.getModelNumberIndex((file + 1) * 1000000 + 1,
					false, false);
			if (modelIndex2 < 0)
				modelIndex2 = viewer.getModelCount();
			modelIndex2--;
		}
		viewer.setAnimationOn(false);
		viewer.setAnimationDirection(1);
		viewer.setAnimationRange(modelIndex, modelIndex2);
		viewer.setCurrentModelIndex(-1);
	}

	private void fixed() throws ScriptException {
		BitSet bs = (statementLength == 1 ? null : atomExpressionAt(1));
		if (isSyntaxCheck)
			return;
		viewer.setMotionFixedAtoms(bs);
	}

	private void frame(int offset) throws ScriptException {
		boolean useModelNumber = true;
		// for now -- as before -- remove to implement
		// frame/model difference
		if (statementLength == 1 && offset == 1) {
			int modelIndex = viewer.getCurrentModelIndex();
			int m;
			if (!isSyntaxCheck && modelIndex >= 0
					&& (m = viewer.getJmolDataSourceFrame(modelIndex)) >= 0)
				viewer.setCurrentModelIndex(m == modelIndex ? Integer.MIN_VALUE
						: m);
			return;
		}
		switch (tokAt(1)) {
		case Token.expressionBegin:
		case Token.bitset:
			int i = atomExpressionAt(1).nextSetBit(0);
			checkLength(iToken + 1);
			if (isSyntaxCheck || i < 0)
				return;
			BitSet bsa = new BitSet();
			bsa.set(i);
			viewer.setCurrentModelIndex(viewer.getModelBitSet(bsa, false)
					.nextSetBit(0));
			return;
		case Token.id:
			checkLength(3);
			String id = stringParameter(2);
			if (!isSyntaxCheck)
				viewer.setCurrentModelID(id);
			return;
		case Token.delay:
			long millis = 0;
			checkLength(3);
			switch (getToken(2).tok) {
			case Token.integer:
			case Token.decimal:
				millis = (long) (floatParameter(2) * 1000);
				break;
			default:
				error(ERROR_integerExpected);
			}
			if (!isSyntaxCheck)
				viewer.setFrameDelayMs(millis);
			return;
		case Token.title:
			if (checkLength23() > 0)
				if (!isSyntaxCheck)
					viewer.setFrameTitle(statementLength == 2 ? "@{_modelName}"
							: (tokAt(2) == Token.varray ? ScriptVariable
									.listValue(statement[2])
									: parameterAsString(2)));
			return;
		case Token.align:
			BitSet bs = (statementLength == 2 || tokAt(2) == Token.none ? null
					: atomExpressionAt(2));
			if (!isSyntaxCheck)
				viewer.setFrameOffsets(bs);
			return;
		}
		if (getToken(offset).tok == Token.minus) {
			++offset;
			if (getToken(checkLast(offset)).tok != Token.integer
					|| intParameter(offset) != 1)
				error(ERROR_invalidArgument);
			if (!isSyntaxCheck)
				viewer.setAnimation(Token.prev);
			return;
		}
		boolean isPlay = false;
		boolean isRange = false;
		boolean isAll = false;
		boolean isHyphen = false;
		int[] frameList = new int[] { -1, -1 };
		int nFrames = 0;
		float fFrame = 0;
		boolean haveFileSet = viewer.haveFileSet();

		for (int i = offset; i < statementLength; i++) {
			switch (getToken(i).tok) {
			case Token.all:
			case Token.times:
				checkLength(offset + (isRange ? 2 : 1));
				isAll = true;
				break;
			case Token.minus: // ignore
				if (nFrames != 1)
					error(ERROR_invalidArgument);
				isHyphen = true;
				break;
			case Token.none:
				checkLength(offset + 1);
				break;
			case Token.decimal:
				useModelNumber = false;
				if ((fFrame = floatParameter(i)) < 0)
					error(ERROR_invalidArgument);
				//$FALL-THROUGH$
			case Token.integer:
			case Token.string:
				if (nFrames == 2)
					error(ERROR_invalidArgument);
				int iFrame = (theTok == Token.string ? getFloatEncodedInt((String) theToken.value)
						: theToken.intValue);
				if (iFrame < 0 && nFrames == 1) {
					isHyphen = true;
					iFrame = -iFrame;
					if (haveFileSet && iFrame < 1000000)
						iFrame *= 1000000;
				}
				if (theTok == Token.decimal && haveFileSet
						&& fFrame == (int) fFrame)
					iFrame = (int) fFrame * 1000000;
				if (iFrame == Integer.MAX_VALUE) {
					if (i == 1) {
						String id = theToken.value.toString();
						int modelIndex = (isSyntaxCheck ? -1 : viewer
								.getModelIndexFromId(id));
						if (modelIndex >= 0) {
							checkLength(2);
							viewer.setCurrentModelIndex(modelIndex);
							return;
						}
					}
					iFrame = 0; // frame 0.0
				}
				if (iFrame == -1) {
					checkLength(offset + 1);
					if (!isSyntaxCheck)
						viewer.setAnimation(Token.prev);
					return;
				}
				if (iFrame >= 1000 && iFrame < 1000000 && haveFileSet)
					iFrame = (iFrame / 1000) * 1000000 + (iFrame % 1000); // initial
																			// way
				if (!useModelNumber && iFrame == 0 && nFrames == 0)
					isAll = true; // 0.0 means ALL; 0 means "all in this range
				if (iFrame >= 1000000)
					useModelNumber = false;
				frameList[nFrames++] = iFrame;
				break;
			case Token.play:
				isPlay = true;
				break;
			case Token.range:
				isRange = true;
				break;
			default:
				frameControl(offset);
				return;
			}
		}
		if (isRange && nFrames == 0)
			isAll = true;
		if (isSyntaxCheck)
			return;
		if (isAll) {
			viewer.setAnimationOn(false);
			viewer.setAnimationRange(-1, -1);
			if (!isRange)
				viewer.setCurrentModelIndex(-1);
			return;
		}
		if (nFrames == 2 && !isRange)
			isHyphen = true;
		if (haveFileSet)
			useModelNumber = false;
		else if (useModelNumber)
			for (int i = 0; i < nFrames; i++)
				if (frameList[i] >= 0)
					frameList[i] %= 1000000;
		int modelIndex = viewer.getModelNumberIndex(frameList[0],
				useModelNumber, false);
		int modelIndex2 = -1;
		if (haveFileSet && modelIndex < 0 && frameList[0] != 0) {
			// may have frame 2.0 or frame 2 meaning the range of models in file
			// 2
			// or frame 2.0 - 3.1 or frame 2.0 - 3.0
			if (frameList[0] < 1000000)
				frameList[0] *= 1000000;
			if (nFrames == 2 && frameList[1] < 1000000)
				frameList[1] *= 1000000;
			if (frameList[0] % 1000000 == 0) {
				frameList[0]++;
				modelIndex = viewer.getModelNumberIndex(frameList[0], false,
						false);
				if (modelIndex >= 0) {
					int i2 = (nFrames == 1 ? frameList[0] + 1000000
							: frameList[1] == 0 ? -1
									: frameList[1] % 1000000 == 0 ? frameList[1] + 1000001
											: frameList[1] + 1);
					modelIndex2 = viewer.getModelNumberIndex(i2, false, false);
					if (modelIndex2 < 0)
						modelIndex2 = viewer.getModelCount();
					modelIndex2--;
					if (isRange)
						nFrames = 2;
					else if (!isHyphen && modelIndex2 != modelIndex)
						isHyphen = true;
					isRange = isRange || modelIndex == modelIndex2;// (isRange
																	// ||
					// !isHyphen &&
					// modelIndex2 !=
					// modelIndex);
				}
			} else {
				// must have been a bad frame number. Just return.
				return;
			}
		}

		if (!isPlay && !isRange || modelIndex >= 0)
			viewer.setCurrentModelIndex(modelIndex, false);
		if (isPlay && nFrames == 2 || isRange || isHyphen) {
			if (modelIndex2 < 0)
				modelIndex2 = viewer.getModelNumberIndex(frameList[1],
						useModelNumber, false);
			viewer.setAnimationOn(false);
			viewer.setAnimationDirection(1);
			viewer.setAnimationRange(modelIndex, modelIndex2);
			viewer.setCurrentModelIndex(isHyphen && !isRange ? -1
					: modelIndex >= 0 ? modelIndex : 0, false);
		}
		if (isPlay)
			viewer.setAnimation(Token.resume);
	}

	BitSet bitSetForModelFileNumber(int m) {
		// where */1.0 or */1.1 or just 1.1 is processed
		BitSet bs = new BitSet(viewer.getAtomCount());
		if (isSyntaxCheck)
			return bs;
		int modelCount = viewer.getModelCount();
		boolean haveFileSet = viewer.haveFileSet();
		if (m < 1000000 && haveFileSet)
			m *= 1000000;
		int pt = m % 1000000;
		if (pt == 0) {
			int model1 = viewer.getModelNumberIndex(m + 1, false, false);
			if (model1 < 0)
				return bs;
			int model2 = (m == 0 ? modelCount : viewer.getModelNumberIndex(
					m + 1000001, false, false));
			if (model1 < 0)
				model1 = 0;
			if (model2 < 0)
				model2 = modelCount;
			if (viewer.isTrajectory(model1))
				model2 = model1 + 1;
			for (int j = model1; j < model2; j++)
				bs.or(viewer.getModelUndeletedAtomsBitSet(j));
		} else {
			int modelIndex = viewer.getModelNumberIndex(m, false, true);
			if (modelIndex >= 0)
				bs.or(viewer.getModelUndeletedAtomsBitSet(modelIndex));
		}
		return bs;
	}

	private void frameControl(int i) throws ScriptException {
		switch (getToken(checkLast(i)).tok) {
		case Token.playrev:
		case Token.play:
		case Token.resume:
		case Token.pause:
		case Token.next:
		case Token.prev:
		case Token.rewind:
		case Token.first:
		case Token.last:
			if (!isSyntaxCheck)
				viewer.setAnimation(theTok);
			return;
		}
		error(ERROR_invalidArgument);
	}

	private int getShapeType(int tok) throws ScriptException {
		int iShape = JmolConstants.shapeTokenIndex(tok);
		if (iShape < 0)
			error(ERROR_unrecognizedObject);
		return iShape;
	}

	private void font(int shapeType, float fontsize) throws ScriptException {
		String fontface = "SansSerif";
		String fontstyle = "Plain";
		int sizeAdjust = 0;
		float scaleAngstromsPerPixel = -1;
		switch (iToken = statementLength) {
		case 6:
			scaleAngstromsPerPixel = floatParameter(5);
			if (scaleAngstromsPerPixel >= 5) // actually a zoom value
				scaleAngstromsPerPixel = viewer.getZoomSetting()
						/ scaleAngstromsPerPixel
						/ viewer.getScalePixelsPerAngstrom(false);
			//$FALL-THROUGH$
		case 5:
			if (getToken(4).tok != Token.identifier)
				error(ERROR_invalidArgument);
			fontstyle = parameterAsString(4);
			//$FALL-THROUGH$
		case 4:
			if (getToken(3).tok != Token.identifier)
				error(ERROR_invalidArgument);
			fontface = parameterAsString(3);
			if (!isFloatParameter(2))
				error(ERROR_numberExpected);
			fontsize = floatParameter(2);
			shapeType = getShapeType(getToken(1).tok);
			break;
		case 3:
			if (!isFloatParameter(2))
				error(ERROR_numberExpected);
			if (shapeType == -1) {
				shapeType = getShapeType(getToken(1).tok);
				fontsize = floatParameter(2);
			} else {// labels --- old set fontsize N
				if (fontsize >= 1)
					fontsize += (sizeAdjust = 5);
			}
			break;
		case 2:
		default:
			if (shapeType == JmolConstants.SHAPE_LABELS) {
				// set fontsize
				fontsize = JmolConstants.LABEL_DEFAULT_FONTSIZE;
				break;
			}
			error(ERROR_badArgumentCount);
		}
		if (shapeType == JmolConstants.SHAPE_LABELS) {
			if (fontsize < 0
					|| fontsize >= 1
					&& (fontsize < JmolConstants.LABEL_MINIMUM_FONTSIZE || fontsize > JmolConstants.LABEL_MAXIMUM_FONTSIZE))
				integerOutOfRange(JmolConstants.LABEL_MINIMUM_FONTSIZE
						- sizeAdjust, JmolConstants.LABEL_MAXIMUM_FONTSIZE
						- sizeAdjust);
			setShapeProperty(JmolConstants.SHAPE_LABELS, "setDefaults",
					viewer.getNoneSelected());
		}
		if (isSyntaxCheck)
			return;
		if (GData.getFontStyleID(fontface) >= 0) {
			fontstyle = fontface;
			fontface = "SansSerif";
		}
		JmolFont font3d = viewer.getFont3D(fontface, fontstyle, fontsize);
		shapeManager.loadShape(shapeType);
		setShapeProperty(shapeType, "font", font3d);
		if (scaleAngstromsPerPixel >= 0)
			setShapeProperty(shapeType, "scalereference",
					Float.valueOf(scaleAngstromsPerPixel));
	}

	private void set() throws ScriptException {
		/*
		 * The SET command now allows only the following:
		 * 
		 * SET SET xxx? SET [valid Jmol Token.setparam keyword] SET labelxxxx
		 * SET xxxxCallback
		 * 
		 * All other variables must be assigned using
		 * 
		 * x = ....
		 * 
		 * The processing goes as follows:
		 * 
		 * check for SET check for SET xx? check for SET xxxx where xxxx is a
		 * command --- deprecated (all other settings may alternatively start
		 * with x = y) check for SET xxxx where xxxx requires special checking
		 * (all other settings may alternatively start with x = (math
		 * expression) check for context variables var x = ... check for
		 * deprecated SET words such as "radius"
		 */
		if (statementLength == 1) {
			showString(viewer.getAllSettings(null));
			return;
		}
		boolean isJmolSet = (parameterAsString(0).equals("set"));
		String key = optParameterAsString(1);
		if (isJmolSet && statementLength == 2 && key.indexOf("?") >= 0) {
			showString(viewer
					.getAllSettings(key.substring(0, key.indexOf("?"))));
			return;
		}
		int tok = getToken(1).tok;

		int newTok = 0;
		String sval;
		int ival = Integer.MAX_VALUE;

		boolean showing = (!isSyntaxCheck && !tQuiet
				&& scriptLevel <= scriptReportingLevel && !((String) statement[0].value)
				.equals("var"));

		// THESE FIRST ARE DEPRECATED AND HAVE THEIR OWN COMMAND
		// anything in this block MUST RETURN

		switch (tok) {
		case Token.axes:
			axes(2);
			return;
		case Token.background:
			background(2);
			return;
		case Token.boundbox:
			boundbox(2);
			return;
		case Token.frank:
			frank(2);
			return;
		case Token.history:
			history(2);
			return;
		case Token.label:
			label(2);
			return;
		case Token.unitcell:
			unitcell(2);
			return;
		case Token.highlight:
			shapeManager.loadShape(JmolConstants.SHAPE_HALOS);
			setShapeProperty(JmolConstants.SHAPE_HALOS, "highlight",
					(tokAt(2) == Token.off ? null : atomExpressionAt(2)));
			return;
		case Token.display:// deprecated
		case Token.selectionhalos:
			selectionHalo(2);
			return;
		case Token.timeout:
			timeout(2);
			return;
		}

		// THESE HAVE MULTIPLE CONTEXTS AND
		// SO DO NOT ALLOW CALCULATIONS xxx = a + b...
		// and are thus "setparam" only

		// anything in this block MUST RETURN

		switch (tok) {
		case Token.structure:
			EnumStructure type = EnumStructure
					.getProteinStructureType(parameterAsString(2));
			if (type == EnumStructure.NOT)
				error(ERROR_invalidArgument);
			float[] data = floatParameterSet(3, 0, Integer.MAX_VALUE);
			if (data.length % 4 != 0)
				error(ERROR_invalidArgument);
			viewer.setStructureList(data, type);
			checkLast(iToken);
			return;
		case Token.axescolor:
			ival = getArgbParam(2);
			if (!isSyntaxCheck)
				setObjectArgb("axes", ival);
			return;
		case Token.bondmode:
			setBondmode();
			return;
		case Token.debug:
			if (isSyntaxCheck)
				return;
			int iLevel = (tokAt(2) == Token.off || tokAt(2) == Token.integer
					&& intParameter(2) == 0 ? 4 : 5);
			Logger.setLogLevel(iLevel);
			setIntProperty("logLevel", iLevel);
			if (iLevel == 4) {
				viewer.setDebugScript(false);
				if (showing)
					viewer.showParameter("debugScript", true, 80);
			}
			setDebugging();
			if (showing)
				viewer.showParameter("logLevel", true, 80);
			return;
		case Token.echo:
			setEcho();
			return;
		case Token.fontsize:
			font(JmolConstants.SHAPE_LABELS, checkLength23() == 2 ? 0
					: floatParameter(2));
			return;
		case Token.hbond:
			setHbond();
			return;
		case Token.measure:
		case Token.measurements:
			setMonitor();
			return;
		case Token.ssbond: // ssBondsBackbone
			setSsbond();
			return;
		case Token.togglelabel:
			setLabel("toggle");
			return;
		case Token.usercolorscheme:
			setUserColors();
			return;
		case Token.zslab:
			setZslab();
			return;
		}

		// these next may report a value
		// require special checks
		// math expressions are allowed in most cases.

		boolean justShow = true;

		switch (tok) {
		case Token.backgroundmodel:
			if (statementLength > 2) {
				String modelDotted = stringSetting(2, false);
				int modelNumber;
				boolean useModelNumber = false;
				if (modelDotted.indexOf(".") < 0) {
					modelNumber = Parser.parseInt(modelDotted);
					useModelNumber = true;
				} else {
					modelNumber = getFloatEncodedInt(modelDotted);
				}
				if (isSyntaxCheck)
					return;
				int modelIndex = viewer.getModelNumberIndex(modelNumber,
						useModelNumber, true);
				viewer.setBackgroundModelIndex(modelIndex);
				return;
			}
			break;
		case Token.vanderwaals:
			if (isSyntaxCheck)
				return;
			viewer.setAtomProperty(viewer.getModelUndeletedAtomsBitSet(-1),
					Token.vanderwaals, -1, Float.NaN, null, null, null);
			switch (tokAt(2)) {
			case Token.probe:
				runScript(Elements.VdwPROBE);
				return;
			}
			newTok = Token.defaultvdw;
			//$FALL-THROUGH$
		case Token.defaultvdw:
			// allows unquoted string for known vdw type
			if (statementLength > 2) {
				sval = (statementLength == 3
						&& EnumVdw.getVdwType(parameterAsString(2)) == null ? stringSetting(
						2, false) : parameterAsString(2));
				if (EnumVdw.getVdwType(sval) == null)
					error(ERROR_invalidArgument);
				setStringProperty(key, sval);
			}
			break;
		case Token.defaultlattice:
			if (statementLength > 2) {
				Point3f pt;
				ScriptVariable var = parameterExpressionToken(2);
				if (var.tok == Token.point3f)
					pt = (Point3f) var.value;
				else {
					int ijk = ScriptVariable.iValue(var);
					if (ijk < 555)
						pt = new Point3f();
					else
						pt = viewer.getSymmetry().ijkToPoint3f(ijk + 111);
				}
				if (!isSyntaxCheck)
					viewer.setDefaultLattice(pt);
			}
			break;
		case Token.defaults:
		case Token.defaultcolorscheme:
			// allows unquoted "jmol" or "rasmol"
			if (statementLength > 2) {
				if ((theTok = tokAt(2)) == Token.jmol || theTok == Token.rasmol) {
					sval = parameterAsString(checkLast(2));
				} else {
					sval = stringSetting(2, false);
				}
				setStringProperty(key, sval);
			}
			break;
		case Token.formalcharge:
			ival = intSetting(2);
			if (ival == Integer.MIN_VALUE)
				error(ERROR_invalidArgument);
			if (!isSyntaxCheck)
				viewer.setFormalCharges(ival);
			return;
		case Token.historylevel:
			// save value locally as well
			ival = intSetting(2);
			if (!isSyntaxCheck) {
				if (ival != Integer.MIN_VALUE)
					commandHistoryLevelMax = ival;
				setIntProperty(key, ival);
			}
			break;
		case Token.language:
			// language can be used without quotes in a SET context
			// set language en
			if (statementLength > 2)
				setStringProperty(key, stringSetting(2, isJmolSet));
			break;
		case Token.measurementunits:
		case Token.energyunits:
			if (statementLength > 2)
				setUnits(stringSetting(2, isJmolSet), tok);
			break;
		case Token.picking:
			if (!isSyntaxCheck)
				viewer.setPicked(-1);
			if (statementLength > 2) {
				setPicking();
				return;
			}
			break;
		case Token.pickingstyle:
			if (statementLength > 2) {
				setPickingStyle();
				return;
			}
			break;
		case Token.property: // compiler may give different values to this token
			// set property_xxxx will be handled in setVariable
			break;
		case Token.scriptreportinglevel:
			// save value locally as well
			ival = intSetting(2);
			if (!isSyntaxCheck && ival != Integer.MIN_VALUE)
				setIntProperty(key, scriptReportingLevel = ival);
			break;
		case Token.specular:
			ival = intSetting(2);
			if (ival == Integer.MIN_VALUE || ival == 0 || ival == 1) {
				justShow = false;
				break;
			}
			tok = Token.specularpercent;
			key = "specularPercent";
			setIntProperty(key, ival);
			break;
		case Token.strands:
			tok = Token.strandcount;
			key = "strandCount";
			setIntProperty(key, intSetting(2));
			break;
		default:
			justShow = false;
		}

		if (justShow && !showing)
			return;

		// var xxxx = xxx can supercede set xxxx

		boolean isContextVariable = (!justShow && !isJmolSet && getContextVariableAsVariable(key) != null);

		if (!justShow && !isContextVariable) {

			// THESE NEXT are deprecated:

			switch (tok) {
			case Token.bonds:
				newTok = Token.showmultiplebonds;
				break;
			case Token.hetero:
				newTok = Token.selecthetero;
				break;
			case Token.hydrogen:
				newTok = Token.selecthydrogen;
				break;
			case Token.measurementnumbers:
				newTok = Token.measurementlabels;
				break;
			case Token.radius:
				newTok = Token.solventproberadius;
				setFloatProperty("solventProbeRadius", floatSetting(2));
				justShow = true;
				break;
			case Token.scale3d:
				newTok = Token.scaleangstromsperinch;
				break;
			case Token.solvent:
				newTok = Token.solventprobe;
				break;
			case Token.color:
				newTok = Token.defaultcolorscheme;
				break;
			case Token.spin:
				sval = parameterAsString(2).toLowerCase();
				switch ("x;y;z;fps".indexOf(sval + ";")) {
				case 0:
					newTok = Token.spinx;
					break;
				case 2:
					newTok = Token.spiny;
					break;
				case 4:
					newTok = Token.spinz;
					break;
				case 6:
					newTok = Token.spinfps;
					break;
				default:
					error(ERROR_unrecognizedParameter, "set SPIN ", sval);
				}
				if (!isSyntaxCheck)
					viewer.setSpin(sval, (int) floatParameter(checkLast(3)));
				justShow = true;
				break;
			}
		}

		if (newTok != 0) {
			key = Token.nameOf(tok = newTok);
		} else if (!justShow && !isContextVariable) {
			// special cases must be checked
			if (key.length() == 0 || key.charAt(0) == '_') // these cannot be
															// set by user
				error(ERROR_cannotSet);

			// these next are not reported and do not allow calculation xxxx = a
			// + b

			String lckey = key.toLowerCase();
			if (lckey.indexOf("label") == 0
					&& Parser
							.isOneOf(key.substring(5).toLowerCase(),
									"front;group;atom;offset;offsetexact;pointer;alignment;toggle;scalereference")) {
				if (setLabel(key.substring(5)))
					return;
			}
			if (lckey.endsWith("callback"))
				tok = Token.setparam;
		}
		if (isJmolSet && !Token.tokAttr(tok, Token.setparam)) {
			iToken = 1;
			if (!isStateScript)
				error(ERROR_unrecognizedParameter, "SET", key);
			warning(ERROR_unrecognizedParameterWarning, "SET", key);
		}

		if (!justShow && isJmolSet) {
			// simple cases
			switch (statementLength) {
			case 2:
				// set XXXX;
				// too bad we allow this...
				setBooleanProperty(key, true);
				justShow = true;
				break;
			case 3:
				// set XXXX val;
				// check for int and NONE just in case
				if (ival != Integer.MAX_VALUE) {
					// keep it simple
					setIntProperty(key, ival);
					justShow = true;
				}
				break;
			}
		}

		if (!justShow && !isJmolSet && tokAt(2) == Token.none) {
			if (!isSyntaxCheck)
				viewer.removeUserVariable(key.toLowerCase());
			justShow = true;
		}

		if (!justShow) {
			int tok2 = (tokAt(1) == Token.expressionBegin ? 0 : tokAt(2));
			int setType = statement[0].intValue;
			// recasted by compiler:
			// var c.xxx =
			// c.xxx =
			// {...}[n].xxx =
			// not supported:
			// a[...][...].xxx =
			// var a[...][...].xxx =

			int pt = (tok2 == Token.opEQ ? 3
			// set x = ...
					: setType == '=' && !key.equals("return")
							&& tok2 != Token.opEQ ? 0
					// {c}.xxx =
					// {...}.xxx =
					// {{...}[n]}.xxx =
							: 2
			// var a[...].xxx =
			// a[...].xxx =
			// var c = ...
			// var c = [
			// c = [
			// c = ...
			// set x ...
			// a[...] =
			);

			setVariable(pt, 0, key, setType);
			if (!isJmolSet)
				return;
		}
		if (showing)
			viewer.showParameter(key, true, 80);
	}

	private void setZslab() throws ScriptException {
		// sets zSlab either based on a percent value or an atom position
		Point3f pt = null;
		if (isFloatParameter(2)) {
			checkLength(3);
			setIntProperty("zSlab", (int) floatParameter(2));
		} else {
			if (!isCenterParameter(2))
				error(ERROR_invalidArgument);
			pt = centerParameter(2);
			checkLength(iToken + 1);
		}
		if (!isSyntaxCheck)
			viewer.setZslabPoint(pt);
	}

	private void setBondmode() throws ScriptException {
		boolean bondmodeOr = false;
		switch (getToken(checkLast(2)).tok) {
		case Token.opAnd:
			break;
		case Token.opOr:
			bondmodeOr = true;
			break;
		default:
			error(ERROR_invalidArgument);
		}
		setBooleanProperty("bondModeOr", bondmodeOr);
	}

	private void setEcho() throws ScriptException {
		String propertyName = null;
		Object propertyValue = null;
		String id = null;
		boolean echoShapeActive = true;
		// set echo xxx
		int pt = 2;

		// check for ID name or just name
		// also check simple OFF, NONE
		switch (getToken(2).tok) {
		case Token.off:
			id = propertyName = "allOff";
			checkLength(++pt);
			break;
		case Token.none:
			echoShapeActive = false;
			//$FALL-THROUGH$
		case Token.all:
			// all and none get NO additional parameters;
			id = parameterAsString(2);
			checkLength(++pt);
			break;
		case Token.left:
		case Token.center:
		case Token.right:
		case Token.top:
		case Token.middle:
		case Token.bottom:
		case Token.identifier:
		case Token.string:
		case Token.id:
			if (theTok == Token.id)
				pt++;
			id = parameterAsString(pt++);
			break;
		}

		if (!isSyntaxCheck) {
			viewer.setEchoStateActive(echoShapeActive);
			shapeManager.loadShape(JmolConstants.SHAPE_ECHO);
			if (id != null)
				setShapeProperty(JmolConstants.SHAPE_ECHO,
						propertyName == null ? "target" : propertyName, id);
		}

		if (pt < statementLength) {
			// set echo name xxx
			// pt is usually 3, but could be 4 if ID used
			switch (getToken(pt++).tok) {
			case Token.align:
				propertyName = "align";
				switch (getToken(pt).tok) {
				case Token.left:
				case Token.right:
				case Token.center:
					propertyValue = parameterAsString(pt++);
					break;
				default:
					error(ERROR_invalidArgument);
				}
				break;
			case Token.center:
			case Token.left:
			case Token.right:
				propertyName = "align";
				propertyValue = parameterAsString(pt - 1);
				break;
			case Token.depth:
				propertyName = "%zpos";
				propertyValue = Integer.valueOf((int) floatParameter(pt++));
				break;
			case Token.display:
			case Token.displayed:
			case Token.on:
				propertyName = "hidden";
				propertyValue = Boolean.FALSE;
				break;
			case Token.hide:
			case Token.hidden:
				propertyName = "hidden";
				propertyValue = Boolean.TRUE;
				break;
			case Token.model:
				int modelIndex = (isSyntaxCheck ? 0
						: modelNumberParameter(pt++));
				if (modelIndex >= viewer.getModelCount())
					error(ERROR_invalidArgument);
				propertyName = "model";
				propertyValue = Integer.valueOf(modelIndex);
				break;
			case Token.leftsquare:
			case Token.spacebeforesquare:
				// [ x y ] with or without %
				propertyName = "xypos";
				propertyValue = xypParameter(--pt);
				if (propertyValue == null)
					pt--;
				else
					pt = iToken + 1;
				break;
			case Token.integer:
				// x y without brackets
				pt--;
				int posx = intParameter(pt++);
				String namex = "xpos";
				if (tokAt(pt) == Token.percent) {
					namex = "%xpos";
					pt++;
				}
				propertyName = "ypos";
				propertyValue = Integer.valueOf(intParameter(pt++));
				if (tokAt(pt) == Token.percent) {
					propertyName = "%ypos";
					pt++;
				}
				checkLength(pt);
				setShapeProperty(JmolConstants.SHAPE_ECHO, namex,
						Integer.valueOf(posx));
				break;
			case Token.off:
				propertyName = "off";
				break;
			case Token.scale:
				propertyName = "scale";
				propertyValue = Float.valueOf(floatParameter(pt++));
				break;
			case Token.script:
				propertyName = "script";
				propertyValue = parameterAsString(pt++);
				break;
			case Token.string:
			case Token.image:
				if (theTok == Token.image)
					pt++;
				checkLength(pt);
				echo(pt - 1, theTok == Token.image);
				return;
			default:
				if (isCenterParameter(pt - 1)) {
					propertyName = "xyz";
					propertyValue = centerParameter(pt - 1);
					pt = iToken + 1;
					break;
				}
				error(ERROR_invalidArgument);
			}
		}
		checkLength(pt);
		if (!isSyntaxCheck && propertyName != null)
			setShapeProperty(JmolConstants.SHAPE_ECHO, propertyName,
					propertyValue);
	}

	private int intSetting(int pt) throws ScriptException {
		if (pt == statementLength)
			return Integer.MIN_VALUE;
		return ScriptVariable.iValue(parameterExpressionToken(pt));
	}

	private float floatSetting(int pt) throws ScriptException {
		if (pt == statementLength)
			return Float.NaN;
		return ScriptVariable.fValue(parameterExpressionToken(pt));
	}

	private String stringSetting(int pt, boolean isJmolSet)
			throws ScriptException {
		if (isJmolSet && statementLength == pt + 1)
			return parameterAsString(pt);
		return parameterExpressionToken(pt).asString();
	}

	private boolean setLabel(String str) throws ScriptException {
		shapeManager.loadShape(JmolConstants.SHAPE_LABELS);
		Object propertyValue = null;
		setShapeProperty(JmolConstants.SHAPE_LABELS, "setDefaults",
				viewer.getNoneSelected());
		while (true) {
			if (str.equals("scalereference")) {
				float scaleAngstromsPerPixel = floatParameter(2);
				if (scaleAngstromsPerPixel >= 5) // actually a zoom value
					scaleAngstromsPerPixel = viewer.getZoomSetting()
							/ scaleAngstromsPerPixel
							/ viewer.getScalePixelsPerAngstrom(false);
				propertyValue = Float.valueOf(scaleAngstromsPerPixel);
				break;
			}
			if (str.equals("offset") || str.equals("offsetexact")) {
				int xOffset = intParameter(2, -127, 127);
				int yOffset = intParameter(3, -127, 127);
				propertyValue = Integer.valueOf(Object2d.getOffset(xOffset,
						yOffset));
				break;
			}
			if (str.equals("alignment")) {
				switch (getToken(2).tok) {
				case Token.left:
				case Token.right:
				case Token.center:
					str = "align";
					propertyValue = theToken.value;
					break;
				default:
					error(ERROR_invalidArgument);
				}
				break;
			}
			if (str.equals("pointer")) {
				int flags = Object2d.POINTER_NONE;
				switch (getToken(2).tok) {
				case Token.off:
				case Token.none:
					break;
				case Token.background:
					flags |= Object2d.POINTER_BACKGROUND;
					//$FALL-THROUGH$
				case Token.on:
					flags |= Object2d.POINTER_ON;
					break;
				default:
					error(ERROR_invalidArgument);
				}
				propertyValue = Integer.valueOf(flags);
				break;
			}
			if (str.equals("toggle")) {
				iToken = 1;
				BitSet bs = (statementLength == 2 ? null : atomExpressionAt(2));
				checkLast(iToken);
				if (!isSyntaxCheck)
					viewer.togglePickingLabel(bs);
				return true;
			}
			iToken = 1;
			boolean TF = (statementLength == 2 || getToken(2).tok == Token.on);
			if (str.equals("front") || str.equals("group")) {
				if (!TF && tokAt(2) != Token.off)
					error(ERROR_invalidArgument);
				if (!TF)
					str = "front";
				propertyValue = (TF ? Boolean.TRUE : Boolean.FALSE);
				break;
			}
			if (str.equals("atom")) {
				if (!TF && tokAt(2) != Token.off)
					error(ERROR_invalidArgument);
				str = "front";
				propertyValue = (TF ? Boolean.FALSE : Boolean.TRUE);
				break;
			}
			return false;
		}
		BitSet bs = (iToken + 1 < statementLength ? atomExpressionAt(++iToken)
				: null);
		checkLast(iToken);
		if (isSyntaxCheck)
			return true;
		if (bs == null)
			setShapeProperty(JmolConstants.SHAPE_LABELS, str, propertyValue);
		else
			setShapeProperty(JmolConstants.SHAPE_LABELS, str, propertyValue, bs);
		return true;
	}

	private void setMonitor() throws ScriptException {
		// on off here incompatible with "monitor on/off" so this is just a SET
		// option.
		int tok = tokAt(checkLast(2));
		switch (tok) {
		case Token.on:
		case Token.off:
			setBooleanProperty("measurementlabels", tok == Token.on);
			return;
		case Token.dotted:
		case Token.integer:
		case Token.decimal:
			setShapeSize(JmolConstants.SHAPE_MEASURES, getSetAxesTypeMad(2),
					null);
			return;
		}
		setUnits(parameterAsString(2), Token.measurementunits);
	}

	private boolean setUnits(String units, int tok) throws ScriptException {
		if (tok == Token.measurementunits
				&& Parser
						.isOneOf(units.toLowerCase(),
								"angstroms;au;bohr;nanometers;nm;picometers;pm;vanderwaals;vdw")) {
			if (!isSyntaxCheck)
				viewer.setUnits(units, true);
		} else if (tok == Token.energyunits
				&& Parser.isOneOf(units.toLowerCase(), "kcal;kj")) {
			if (!isSyntaxCheck)
				viewer.setUnits(units, false);
		} else {
			error(ERROR_unrecognizedParameter, "set " + Token.nameOf(tok),
					units);
		}
		return true;
	}

	/*
	 * private void setProperty() throws ScriptException { // what possible good
	 * is this? // set property foo bar is identical to // set foo bar if
	 * (getToken(2).tok != Token.identifier) error(ERROR_propertyNameExpected);
	 * String propertyName = parameterAsString(2); switch
	 * (getToken(checkLast(3)).tok) { case Token.on:
	 * setBooleanProperty(propertyName, true); break; case Token.off:
	 * setBooleanProperty(propertyName, false); break; case Token.integer:
	 * setIntProperty(propertyName, intParameter(3)); break; case Token.decimal:
	 * setFloatProperty(propertyName, floatParameter(3)); break; case
	 * Token.string: setStringProperty(propertyName, stringParameter(3)); break;
	 * default: error(ERROR_unrecognizedParameter, "SET " +
	 * propertyName.toUpperCase(), parameterAsString(3)); } }
	 */

	private void setSsbond() throws ScriptException {
		boolean ssbondsBackbone = false;
		// shapeManager.loadShape(JmolConstants.SHAPE_SSSTICKS);
		switch (tokAt(checkLast(2))) {
		case Token.backbone:
			ssbondsBackbone = true;
			break;
		case Token.sidechain:
			break;
		default:
			error(ERROR_invalidArgument);
		}
		setBooleanProperty("ssbondsBackbone", ssbondsBackbone);
	}

	private void setHbond() throws ScriptException {
		boolean bool = false;
		switch (tokAt(checkLast(2))) {
		case Token.backbone:
			bool = true;
			//$FALL-THROUGH$
		case Token.sidechain:
			setBooleanProperty("hbondsBackbone", bool);
			break;
		case Token.solid:
			bool = true;
			//$FALL-THROUGH$
		case Token.dotted:
			setBooleanProperty("hbondsSolid", bool);
			break;
		default:
			error(ERROR_invalidArgument);
		}
	}

	private void setPicking() throws ScriptException {
		// set picking
		if (statementLength == 2) {
			setStringProperty("picking", "identify");
			return;
		}
		// set picking @{"xxx"} or some large length, ignored
		if (statementLength > 4 || tokAt(2) == Token.string) {
			setStringProperty("picking", stringSetting(2, false));
			return;
		}
		int i = 2;
		// set picking select ATOM|CHAIN|GROUP|MOLECULE|MODEL|SITE
		// set picking measure ANGLE|DISTANCE|TORSION
		// set picking spin fps
		String type = "SELECT";
		switch (getToken(2).tok) {
		case Token.select:
		case Token.measure:
		case Token.spin:
			if (checkLength34() == 4) {
				type = parameterAsString(2).toUpperCase();
				if (type.equals("SPIN"))
					setIntProperty("pickingSpinRate", intParameter(3));
				else
					i = 3;
			}
			break;
		case Token.delete:
			break;
		default:
			checkLength(3);
		}

		// set picking on
		// set picking normal
		// set picking identify
		// set picking off
		// set picking select
		// set picking bonds
		// set picking dragselected

		String str = parameterAsString(i);
		switch (getToken(i).tok) {
		case Token.on:
		case Token.normal:
			str = "identify";
			break;
		case Token.off:
		case Token.none:
			str = "off";
			break;
		case Token.select:
			str = "atom";
			break;
		case Token.label:
			str = "label";
			break;
		case Token.bonds: // not implemented
			str = "bond";
			break;
		case Token.delete:
			checkLength(4);
			if (tokAt(3) != Token.bonds)
				error(ERROR_invalidArgument);
			str = "deleteBond";
			break;
		}
		int mode = ((mode = str.indexOf("_")) >= 0 ? mode : str.length());
		mode = ActionManager.getPickingMode(str.substring(0, mode));
		if (mode < 0)
			error(ERROR_unrecognizedParameter, "SET PICKING " + type, str);
		setStringProperty("picking", str);
	}

	private void setPickingStyle() throws ScriptException {
		if (statementLength > 4 || tokAt(2) == Token.string) {
			setStringProperty("pickingStyle", stringSetting(2, false));
			return;
		}
		int i = 2;
		boolean isMeasure = false;
		String type = "SELECT";
		switch (getToken(2).tok) {
		case Token.measure:
			isMeasure = true;
			type = "MEASURE";
			//$FALL-THROUGH$
		case Token.select:
			if (checkLength34() == 4)
				i = 3;
			break;
		default:
			checkLength(3);
		}
		String str = parameterAsString(i);
		switch (getToken(i).tok) {
		case Token.none:
		case Token.off:
			str = (isMeasure ? "measureoff" : "toggle");
			break;
		case Token.on:
			if (isMeasure)
				str = "measure";
			break;
		}
		if (ActionManager.getPickingStyle(str) < 0)
			error(ERROR_unrecognizedParameter, "SET PICKINGSTYLE " + type, str);
		setStringProperty("pickingStyle", str);
	}

	private void timeout(int index) throws ScriptException {
		// timeout ID "mytimeout" mSec "script"
		// msec < 0 --> repeat indefinitely
		// timeout ID "mytimeout" 1000 // milliseconds
		// timeout ID "mytimeout" 0.1 // seconds
		// timeout ID "mytimeout" OFF
		// timeout ID "mytimeout" // flag to trigger waiting timeout repeat
		// timeout OFF
		String name = null;
		String script = null;
		int mSec = 0;
		if (statementLength == index) {
			showString(viewer.showTimeout(null));
			return;
		}
		for (int i = index; i < statementLength; i++)
			switch (getToken(i).tok) {
			case Token.id:
				name = parameterAsString(++i);
				if (statementLength == 3) {
					if (!isSyntaxCheck)
						viewer.triggerTimeout(name);
					return;
				}
				break;
			case Token.off:
				break;
			case Token.integer:
				mSec = intParameter(i);
				break;
			case Token.decimal:
				mSec = (int) (floatParameter(i) * 1000);
				break;
			default:
				if (name == null)
					name = parameterAsString(i);
				else if (script == null)
					script = parameterAsString(i);
				else
					error(ERROR_invalidArgument);
				break;
			}
		if (!isSyntaxCheck && !viewer.isHeadless())
			viewer.setTimeout(name, mSec, script);
	}

	private void setUserColors() throws ScriptException {
		List<Integer> v = new ArrayList<Integer>();
		for (int i = 2; i < statementLength; i++) {
			int argb = getArgbParam(i);
			v.add(Integer.valueOf(argb));
			i = iToken;
		}
		if (isSyntaxCheck)
			return;
		int n = v.size();
		int[] scale = new int[n];
		for (int i = n; --i >= 0;)
			scale[i] = v.get(i).intValue();
		viewer.setUserScale(scale);
	}

	/**
	 * 
	 * @param pt
	 * @param ptMax
	 * @param key
	 * @param setType
	 * @throws ScriptException
	 */
	@SuppressWarnings("unchecked")
	private void setVariable(int pt, int ptMax, String key, int setType)
			throws ScriptException {
		BitSet bs = null;
		String propertyName = "";
		int tokProperty = Token.nada;
		boolean isArrayItem = (setType == '[');
		boolean settingProperty = false;
		boolean isExpression = false;
		boolean settingData = (key.startsWith("property_"));
		ScriptVariable t = (settingData ? null
				: getContextVariableAsVariable(key));
		boolean isUserVariable = (t != null);
		if (pt > 0 && tokAt(pt - 1) == Token.expressionBegin) {
			bs = atomExpressionAt(pt - 1);
			pt = iToken + 1;
			isExpression = true;
		}
		if (tokAt(pt) == Token.per) {
			settingProperty = true;
			ScriptVariable token = getBitsetPropertySelector(++pt, true);
			if (token == null)
				error(ERROR_invalidArgument);
			if (tokAt(++pt) != Token.opEQ)
				error(ERROR_invalidArgument);
			pt++;
			tokProperty = token.intValue;
			propertyName = (String) token.value;
		}
		if (isExpression && !settingProperty)
			error(ERROR_invalidArgument);

		// get value

		List<ScriptVariable> v = (List<ScriptVariable>) parameterExpression(pt,
				ptMax, key, true, true, -1, isArrayItem, null, null);
		int nv = v.size();
		if (nv == 0 || !isArrayItem && nv > 1 || isArrayItem
				&& (nv < 3 || nv % 2 != 1))
			error(ERROR_invalidArgument);
		if (isSyntaxCheck)
			return;
		// x[3][4] = ??
		ScriptVariable tv = v.get(isArrayItem ? v.size() - 1 : 0);

		// create user variable if needed for list now, so we can do the copying

		boolean needVariable = (!isUserVariable && !isExpression
				&& !settingData && (isArrayItem || settingProperty || !(tv.value instanceof String
				|| tv.tok == Token.integer
				|| tv.value instanceof Integer
				|| tv.value instanceof Float || tv.value instanceof Boolean)));

		if (needVariable) {
			if (key.startsWith("_"))
				error(ERROR_invalidArgument, key);
			t = viewer.getOrSetNewVariable(key, true);
			isUserVariable = true;
		}

		if (isArrayItem) {
			ScriptVariable tnew = (new ScriptVariable()).set(tv, false);
			int nParam = v.size() / 2;
			for (int i = 0; i < nParam; i++) {
				boolean isLast = (i + 1 == nParam);
				ScriptVariable vv = v.get(i * 2);
				// stack is selector [ selector [ selector [ ... VALUE
				if (t.tok == Token.bitset) {
					t.tok = Token.hash;
					t.value = new Hashtable<String, ScriptVariable>();
				}
				if (t.tok == Token.hash) {
					String hkey = vv.asString();
					Map<String, ScriptVariable> tmap = (Map<String, ScriptVariable>) t.value;
					if (isLast) {
						tmap.put(hkey, tnew);
						break;
					}
					t = tmap.get(hkey);
				} else {
					int ipt = ScriptVariable.iValue(vv);
					// in the case of for (x in y) where y is an array, we need
					// to select the item before continuing
					if (t.tok == Token.varray)
						t = ScriptVariable.selectItemVar(t);
					switch (t.tok) {
					case Token.varray:
						List<ScriptVariable> list = t.getList();
						if (ipt > list.size() || isLast)
							break;
						if (ipt <= 0)
							ipt = list.size() + ipt;
						if (--ipt < 0)
							ipt = 0;
						t = list.get(ipt);
						continue;
					case Token.matrix3f:
					case Token.matrix4f:
						// check for row/column replacement
						int dim = (t.tok == Token.matrix3f ? 3 : 4);
						if (nParam == 1 && Math.abs(ipt) >= 1
								&& Math.abs(ipt) <= dim
								&& tnew.tok == Token.varray
								&& tnew.getList().size() == dim)
							break;
						if (nParam == 2) {
							int ipt2 = ScriptVariable.iValue(v.get(2));
							if (ipt2 >= 1
									&& ipt2 <= dim
									&& (tnew.tok == Token.integer || tnew.tok == Token.decimal)) {
								i++;
								ipt = ipt * 10 + ipt2;
								break;
							}
						}
						// change to an array and continue;
						t.toArray();
						--i;
						continue;
					}
					t.setSelectedValue(ipt, tnew);
					break;
				}
			}
			return;
		}
		if (settingProperty) {
			if (!isExpression) {
				bs = ScriptVariable.getBitSet(t, true);
				if (bs == null)
					error(ERROR_invalidArgument);
			}
			if (propertyName.startsWith("property_")) {
				viewer.setData(
						propertyName,
						new Object[] {
								propertyName,
								tv.tok == Token.varray ? ScriptVariable.flistValue(
										tv,
										((List<?>) tv.value).size() == bs
												.cardinality() ? bs
												.cardinality() : viewer
												.getAtomCount())
										: tv.asString(), BitSetUtil.copy(bs) },
						viewer.getAtomCount(), 0, 0,
						tv.tok == Token.varray ? Integer.MAX_VALUE
								: Integer.MIN_VALUE, 0);
				return;
			}
			setBitsetProperty(bs, tokProperty, ScriptVariable.iValue(tv),
					ScriptVariable.fValue(tv), tv);
			return;
		}

		if (isUserVariable) {
			t.set(tv, false);
			return;
		}

		Object vv = ScriptVariable.oValue(tv);

		if (key.startsWith("property_")) {
			if (tv.tok == Token.varray)
				vv = tv.asString();
			viewer.setData(
					key,
					new Object[] { key, "" + vv,
							BitSetUtil.copy(viewer.getSelectionSet(false)) },
					viewer.getAtomCount(), 0, 0, Integer.MIN_VALUE, 0);
			return;
		}

		if (vv instanceof Boolean) {
			setBooleanProperty(key, ((Boolean) vv).booleanValue());
		} else if (vv instanceof Integer) {
			setIntProperty(key, ((Integer) vv).intValue());
		} else if (vv instanceof Float) {
			setFloatProperty(key, ((Float) vv).floatValue());
		} else if (vv instanceof String) {
			setStringProperty(key, (String) vv);
		} else if (vv instanceof BondSet) {
			setStringProperty(key, Escape.escapeBs((BitSet) vv, false));
		} else if (vv instanceof BitSet || vv instanceof Point3f
				|| vv instanceof Point4f) {
			setStringProperty(key, Escape.escape(vv));
		} else {
			Logger.error("ERROR -- return from propertyExpression was " + vv);
		}
	}

	private void axes(int index) throws ScriptException {
		// axes (index==1) or set axes (index==2)
		TickInfo tickInfo = checkTicks(index, true, true, false);
		index = iToken + 1;
		int tok = tokAt(index);
		String type = optParameterAsString(index).toLowerCase();
		if (statementLength == index + 1
				&& Parser.isOneOf(type, "window;unitcell;molecular")) {
			setBooleanProperty("axes" + type, true);
			return;
		}
		switch (tok) {
		case Token.center:
			Point3f center = centerParameter(index + 1);
			setShapeProperty(JmolConstants.SHAPE_AXES, "origin", center);
			checkLast(iToken);
			return;
		case Token.scale:
			setFloatProperty("axesScale", floatParameter(checkLast(++index)));
			return;
		case Token.label:
			switch (tok = tokAt(index + 1)) {
			case Token.off:
			case Token.on:
				checkLength(index + 2);
				setShapeProperty(JmolConstants.SHAPE_AXES, "labels"
						+ (tok == Token.on ? "On" : "Off"), null);
				return;
			}
			if (statementLength == index + 7) {
				// axes labels "X" "Y" "Z" "-X" "-Y" "-Z"
				setShapeProperty(JmolConstants.SHAPE_AXES, "labels",
						new String[] { parameterAsString(++index),
								parameterAsString(++index),
								parameterAsString(++index),
								parameterAsString(++index),
								parameterAsString(++index),
								parameterAsString(++index) });
			} else {
				checkLength(index + 4);
				setShapeProperty(JmolConstants.SHAPE_AXES, "labels",
						new String[] { parameterAsString(++index),
								parameterAsString(++index),
								parameterAsString(++index) });
			}
			return;
		}
		// axes position [x y %]
		if (type.equals("position")) {
			Point3f xyp;
			if (tokAt(++index) == Token.off) {
				xyp = new Point3f();
			} else {
				xyp = xypParameter(index);
				if (xyp == null)
					error(ERROR_invalidArgument);
				index = iToken;
			}
			setShapeProperty(JmolConstants.SHAPE_AXES, "position", xyp);
			return;
		}
		int mad = getSetAxesTypeMad(index);
		if (isSyntaxCheck)
			return;
		setObjectMad(JmolConstants.SHAPE_AXES, "axes", mad);
		if (tickInfo != null)
			setShapeProperty(JmolConstants.SHAPE_AXES, "tickInfo", tickInfo);
	}

	private void boundbox(int index) throws ScriptException {
		TickInfo tickInfo = checkTicks(index, false, true, false);
		index = iToken + 1;
		float scale = 1;
		if (tokAt(index) == Token.scale) {
			scale = floatParameter(++index);
			if (!isSyntaxCheck && scale == 0)
				error(ERROR_invalidArgument);
			index++;
			if (index == statementLength) {
				if (!isSyntaxCheck)
					viewer.setBoundBox(null, null, true, scale);
				return;
			}
		}
		boolean byCorner = (tokAt(index) == Token.corners);
		if (byCorner)
			index++;
		if (isCenterParameter(index)) {
			expressionResult = null;
			int index0 = index;
			Point3f pt1 = centerParameter(index);
			index = iToken + 1;
			if (byCorner || isCenterParameter(index)) {
				// boundbox CORNERS {expressionOrPoint1} {expressionOrPoint2}
				// boundbox {expressionOrPoint1} {vector}
				Point3f pt2 = (byCorner ? centerParameter(index) : getPoint3f(
						index, true));
				index = iToken + 1;
				if (!isSyntaxCheck)
					viewer.setBoundBox(pt1, pt2, byCorner, scale);
			} else if (expressionResult != null
					&& expressionResult instanceof BitSet) {
				// boundbox {expression}
				if (!isSyntaxCheck)
					viewer.calcBoundBoxDimensions((BitSet) expressionResult,
							scale);
			} else if (expressionResult == null
					&& tokAt(index0) == Token.dollarsign) {
				if (isSyntaxCheck)
					return;
				Point3f[] bbox = getObjectBoundingBox(objectNameParameter(++index0));
				if (bbox == null)
					error(ERROR_invalidArgument);
				viewer.setBoundBox(bbox[0], bbox[1], true, scale);
				index = iToken + 1;
			} else {
				error(ERROR_invalidArgument);
			}
			if (index == statementLength)
				return;
		}
		int mad = getSetAxesTypeMad(index);
		if (isSyntaxCheck)
			return;
		if (tickInfo != null)
			setShapeProperty(JmolConstants.SHAPE_BBCAGE, "tickInfo", tickInfo);
		setObjectMad(JmolConstants.SHAPE_BBCAGE, "boundbox", mad);
	}

	/**
	 * 
	 * @param index
	 * @param allowUnitCell
	 *            IGNORED
	 * @param allowScale
	 * @param allowFirst
	 * @return TickInfo
	 * @throws ScriptException
	 */
	private TickInfo checkTicks(int index, boolean allowUnitCell,
			boolean allowScale, boolean allowFirst) throws ScriptException {
		iToken = index - 1;
		if (tokAt(index) != Token.ticks)
			return null;
		TickInfo tickInfo;
		String str = " ";
		switch (tokAt(index + 1)) {
		case Token.x:
		case Token.y:
		case Token.z:
			str = parameterAsString(++index).toLowerCase();
			break;
		case Token.identifier:
			error(ERROR_invalidArgument);
		}
		if (tokAt(++index) == Token.none) {
			tickInfo = new TickInfo(null);
			tickInfo.type = str;
			iToken = index;
			return tickInfo;
		}
		tickInfo = new TickInfo((Point3f) getPointOrPlane(index, false, true,
				false, false, 3, 3));
		if (coordinatesAreFractional || tokAt(iToken + 1) == Token.unitcell) {
			tickInfo.scale = new Point3f(Float.NaN, Float.NaN, Float.NaN);
			allowScale = false;
		}
		if (tokAt(iToken + 1) == Token.unitcell)
			iToken++;
		tickInfo.type = str;
		if (tokAt(iToken + 1) == Token.format)
			tickInfo.tickLabelFormats = stringParameterSet(iToken + 2);
		if (!allowScale)
			return tickInfo;
		if (tokAt(iToken + 1) == Token.scale) {
			if (isFloatParameter(iToken + 2)) {
				float f = floatParameter(iToken + 2);
				tickInfo.scale = new Point3f(f, f, f);
			} else {
				tickInfo.scale = getPoint3f(iToken + 2, true);
			}
		}
		if (allowFirst)
			if (tokAt(iToken + 1) == Token.first)
				tickInfo.first = floatParameter(iToken + 2);
		// POINT {x,y,z} reference point not implemented
		// if (tokAt(iToken + 1) == Token.point)
		// tickInfo.reference = centerParameter(iToken + 2);
		return tickInfo;
	}

	private void unitcell(int index) throws ScriptException {
		int icell = Integer.MAX_VALUE;
		int mad = Integer.MAX_VALUE;
		Point3f pt = null;
		TickInfo tickInfo = checkTicks(index, true, false, false);
		index = iToken;
		String id = null;
		Point3f[] points = null;
		switch (tokAt(index + 1)) {
		case Token.string:
			id = objectNameParameter(++index);
			break;
		case Token.dollarsign:
			index++;
			id = objectNameParameter(++index);
			break;
		default:
			if (isArrayParameter(index + 1)) {
				points = getPointArray(++index, 4);
				index = iToken;
			} else if (statementLength == index + 2) {
				if (getToken(index + 1).tok == Token.integer
						&& intParameter(index + 1) >= 111)
					icell = intParameter(++index);
			} else if (statementLength > index + 1) {
				pt = (Point3f) getPointOrPlane(++index, false, true, false,
						true, 3, 3);
				index = iToken;
			}
		}
		mad = getSetAxesTypeMad(++index);
		checkLast(iToken);
		if (isSyntaxCheck)
			return;
		if (icell != Integer.MAX_VALUE)
			viewer.setCurrentUnitCellOffset(icell);
		else if (id != null)
			viewer.setCurrentUnitCell(id);
		else if (points != null)
			viewer.setCurrentUnitCell(points);
		setObjectMad(JmolConstants.SHAPE_UCCAGE, "unitCell", mad);
		if (pt != null)
			viewer.setCurrentUnitCellOffset(pt);
		if (tickInfo != null)
			setShapeProperty(JmolConstants.SHAPE_UCCAGE, "tickInfo", tickInfo);
	}

	private void frank(int index) throws ScriptException {
		setBooleanProperty("frank", booleanParameter(index));
	}

	private void selectionHalo(int pt) throws ScriptException {
		boolean showHalo = false;
		switch (pt == statementLength ? Token.on : getToken(pt).tok) {
		case Token.on:
		case Token.selected:
			showHalo = true;
			//$FALL-THROUGH$
		case Token.off:
		case Token.none:
		case Token.normal:
			setBooleanProperty("selectionHalos", showHalo);
			break;
		default:
			error(ERROR_invalidArgument);
		}
	}

	private void save() throws ScriptException {
		if (statementLength > 1) {
			String saveName = optParameterAsString(2);
			switch (tokAt(1)) {
			case Token.rotation:
				if (!isSyntaxCheck)
					viewer.saveOrientation(saveName);
				return;
			case Token.orientation:
				if (!isSyntaxCheck)
					viewer.saveOrientation(saveName);
				return;
			case Token.bonds:
				if (!isSyntaxCheck)
					viewer.saveBonds(saveName);
				return;
			case Token.state:
				if (!isSyntaxCheck)
					viewer.saveState(saveName);
				return;
			case Token.structure:
				if (!isSyntaxCheck)
					viewer.saveStructure(saveName);
				return;
			case Token.coord:
				if (!isSyntaxCheck)
					viewer.saveCoordinates(saveName,
							viewer.getSelectionSet(false));
				return;
			case Token.selection:
				if (!isSyntaxCheck)
					viewer.saveSelection(saveName);
				return;
			}
		}
		error(ERROR_what, "SAVE",
				"bonds? coordinates? orientation? selection? state? structure?");
	}

	private void restore() throws ScriptException {
		// restore orientation name time
		if (statementLength > 1) {
			String saveName = optParameterAsString(2);
			if (getToken(1).tok != Token.orientation)
				checkLength23();
			float timeSeconds;
			switch (getToken(1).tok) {
			case Token.rotation:
				timeSeconds = (statementLength > 3 ? floatParameter(3) : 0);
				if (timeSeconds < 0)
					error(ERROR_invalidArgument);
				if (!isSyntaxCheck)
					viewer.restoreRotation(saveName, timeSeconds);
				return;
			case Token.orientation:
				timeSeconds = (statementLength > 3 ? floatParameter(3) : 0);
				if (timeSeconds < 0)
					error(ERROR_invalidArgument);
				if (!isSyntaxCheck)
					viewer.restoreOrientation(saveName, timeSeconds);
				return;
			case Token.bonds:
				if (!isSyntaxCheck)
					viewer.restoreBonds(saveName);
				return;
			case Token.coord:
				if (isSyntaxCheck)
					return;
				String script = viewer.getSavedCoordinates(saveName);
				if (script == null)
					error(ERROR_invalidArgument);
				runScript(script);
				viewer.checkCoordinatesChanged();
				return;
			case Token.state:
				if (isSyntaxCheck)
					return;
				String state = viewer.getSavedState(saveName);
				if (state == null)
					error(ERROR_invalidArgument);
				runScript(state);
				return;
			case Token.structure:
				if (isSyntaxCheck)
					return;
				String shape = viewer.getSavedStructure(saveName);
				if (shape == null)
					error(ERROR_invalidArgument);
				runScript(shape);
				return;
			case Token.selection:
				if (!isSyntaxCheck)
					viewer.restoreSelection(saveName);
				return;
			}
		}
		error(ERROR_what, "RESTORE",
				"bonds? coords? orientation? selection? state? structure?");
	}

	String write(Token[] args) throws ScriptException {
		int pt = 0, pt0 = 0;
		boolean isCommand, isShow;
		if (args == null) {
			args = statement;
			pt = pt0 = 1;
			isCommand = true;
			isShow = (viewer.isApplet() && !viewer.isSignedApplet()
					|| !viewer.isRestricted(ACCESS.ALL) || viewer
					.getPathForAllFiles().length() > 0);
		} else {
			isCommand = false;
			isShow = true;
		}
		int argCount = (isCommand ? statementLength : args.length);
		int len = 0;
		int nVibes = 0;
		int width = -1;
		int height = -1;
		int quality = Integer.MIN_VALUE;
		String driverList = viewer.getExportDriverList();
		String sceneType = "PNGJ";
		String data = null;
		String type2 = "";
		String fileName = null;
		String localPath = null;
		String remotePath = null;
		String val = null;
		String msg = null;
		String[] fullPath = new String[1];
		boolean isCoord = false;
		boolean isExport = false;
		boolean isImage = false;
		BitSet bsFrames = null;
		String[] scripts = null;
		String type = "SPT";
		int tok = (isCommand && args.length == 1 ? Token.clipboard : tokAt(pt,
				args));
		switch (tok) {
		case Token.nada:
			break;
		case Token.string:
			Token t = Token.getTokenFromName(ScriptVariable.sValue(args[pt])
					.toLowerCase());
			if (t != null) {
				tok = t.tok;
				type = ScriptVariable.sValue(t).toUpperCase();
			}
			break;
		case Token.script:
			if (isArrayParameter(pt + 1)) {
				scripts = stringParameterSet(++pt);
				localPath = ".";
				remotePath = ".";
				pt0 = pt = iToken + 1;
				tok = tokAt(pt);
			}
			break;
		default:
			type = ScriptVariable.sValue(tokenAt(pt, args)).toUpperCase();
		}
		switch (tok) {
		case Token.nada:
			break;
		case Token.quaternion:
		case Token.ramachandran:
		case Token.property:
			msg = plot(args);
			if (!isCommand)
				return msg;
			break;
		case Token.inline:
			type = "INLINE";
			data = ScriptVariable.sValue(tokenAt(++pt, args));
			pt++;
			break;
		case Token.pointgroup:
			type = "PGRP";
			pt++;
			type2 = ScriptVariable.sValue(tokenAt(pt, args)).toLowerCase();
			if (type2.equals("draw"))
				pt++;
			break;
		case Token.coord:
			pt++;
			isCoord = true;
			break;
		case Token.state:
		case Token.script:
			val = ScriptVariable.sValue(tokenAt(++pt, args)).toLowerCase();
			while (val.equals("localpath") || val.equals("remotepath")) {
				if (val.equals("localpath"))
					localPath = ScriptVariable.sValue(tokenAt(++pt, args));
				else
					remotePath = ScriptVariable.sValue(tokenAt(++pt, args));
				val = ScriptVariable.sValue(tokenAt(++pt, args)).toLowerCase();
			}
			type = "SPT";
			break;
		case Token.file:
		case Token.function:
		case Token.history:
		case Token.isosurface:
		case Token.menu:
		case Token.mesh:
		case Token.mo:
		case Token.pmesh:
			pt++;
			break;
		case Token.jmol:
			type = "ZIPALL";
			pt++;
			break;
		case Token.var:
			type = "VAR";
			pt += 2;
			break;
		case Token.frame:
		case Token.identifier:
		case Token.image:
		case Token.scene:
		case Token.string:
		case Token.vibration:
			switch (tok) {
			case Token.image:
				pt++;
				break;
			case Token.vibration:
				nVibes = intParameter(++pt, 1, 10);
				if (!isSyntaxCheck) {
					viewer.setVibrationOff();
					delay(100);
				}
				pt++;
				break;
			case Token.frame:
				BitSet bsAtoms;
				if (pt + 1 < argCount
						&& args[++pt].tok == Token.expressionBegin
						|| args[pt].tok == Token.bitset) {
					bsAtoms = atomExpression(args, pt, 0, true, false, true,
							true);
					pt = iToken + 1;
				} else {
					bsAtoms = viewer.getModelUndeletedAtomsBitSet(-1);
				}
				if (!isSyntaxCheck)
					bsFrames = viewer.getModelBitSet(bsAtoms, true);
				break;
			case Token.scene:
				val = ScriptVariable.sValue(tokenAt(++pt, args)).toUpperCase();
				if (Parser.isOneOf(val, "PNG;PNGJ")) {
					sceneType = val;
					pt++;
				}
				break;
			default:
				if (Parser.isOneOf(type, driverList.toUpperCase())) {
					// povray, maya, vrml, idtf
					pt++;
					type = type.substring(0, 1).toUpperCase()
							+ type.substring(1).toLowerCase();
					// Povray, Maya, Vrml, Idtf
					isExport = true;
					if (isCommand)
						fileName = "Jmol." + type;
				} else if (type.equals("ZIP")) {
					pt++;
				} else if (type.equals("ZIPALL")) {
					pt++;
				} else {
					type = "(image)";
				}
				break;
			}
			if (tokAt(pt, args) == Token.integer) {
				width = ScriptVariable.iValue(tokenAt(pt++, args));
				height = ScriptVariable.iValue(tokenAt(pt++, args));
			}
			break;
		}

		if (msg == null) {
			val = ScriptVariable.sValue(tokenAt(pt, args));
			if (val.equalsIgnoreCase("clipboard")) {
				if (isSyntaxCheck)
					return "";
				// if (isApplet)
				// evalError(GT._("The {0} command is not available for the applet.",
				// "WRITE CLIPBOARD"));
			} else if (Parser.isOneOf(val.toLowerCase(),
					"png;pngj;pngt;jpg;jpeg;jpg64;jpeg64")
					&& tokAt(pt + 1, args) == Token.integer) {
				quality = ScriptVariable.iValue(tokenAt(++pt, args));
			} else if (Parser.isOneOf(val.toLowerCase(),
					"xyz;xyzrn;xyzvib;mol;sdf;v2000;v3000;cd;pdb;pqr;cml")) {
				type = val.toUpperCase();
				if (pt + 1 == argCount)
					pt++;
			}

			// write [image|history|state] clipboard

			// write [optional image|history|state] [JPG quality|JPEG
			// quality|JPG64
			// quality|PNG|PPM|SPT] "filename"
			// write script "filename"
			// write isosurface t.jvxl

			if (type.equals("(image)")
					&& Parser.isOneOf(val.toUpperCase(),
							"GIF;JPG;JPG64;JPEG;JPEG64;PNG;PNGJ;PNGT;PPM")) {
				type = val.toUpperCase();
				pt++;
			}

			if (pt + 2 == argCount) {
				data = ScriptVariable.sValue(tokenAt(++pt, args));
				if (data.length() > 0 && data.charAt(0) != '.')
					type = val.toUpperCase();
			}
			switch (tokAt(pt, args)) {
			case Token.nada:
				isShow = true;
				break;
			case Token.clipboard:
				break;
			case Token.identifier:
			case Token.string:
				fileName = ScriptVariable.sValue(tokenAt(pt, args));
				if (pt == argCount - 3 && tokAt(pt + 1, args) == Token.per) {
					// write filename.xxx gets separated as filename .spt
					// write isosurface filename.xxx also
					fileName += "."
							+ ScriptVariable.sValue(tokenAt(pt + 2, args));
				}
				if (type != "VAR" && pt == pt0)
					type = "IMAGE";
				else if (fileName.length() > 0 && fileName.charAt(0) == '.'
						&& (pt == pt0 + 1 || pt == pt0 + 2)) {
					fileName = ScriptVariable.sValue(tokenAt(pt - 1, args))
							+ fileName;
					if (type != "VAR" && pt == pt0 + 1)
						type = "IMAGE";
				}
				if (fileName.equalsIgnoreCase("clipboard")
						|| !viewer.isRestricted(ACCESS.ALL))
					fileName = null;
				break;
			default:
				error(ERROR_invalidArgument);
			}
			if (type.equals("IMAGE") || type.equals("FRAME")
					|| type.equals("VIBRATION")) {
				type = (fileName != null && fileName.indexOf(".") >= 0 ? fileName
						.substring(fileName.lastIndexOf(".") + 1).toUpperCase()
						: "JPG");
				if (type.equals("MNU"))
					type = "MENU";
				else if (type.equals("WRL") || type.equals("VRML")) {
					type = "Vrml";
					isExport = true;
				} else if (type.equals("X3D")) {
					type = "X3d";
					isExport = true;
				} else if (type.equals("IDTF")) {
					type = "Idtf";
					isExport = true;
				} else if (type.equals("MA")) {
					type = "Maya";
					isExport = true;
				} else if (type.equals("JS")) {
					type = "Js";
					isExport = true;
				} else if (type.equals("OBJ")) {
					type = "Obj";
					isExport = true;
				} else if (type.equals("JVXL")) {
					type = "ISOSURFACE";
				} else if (type.equals("XJVXL")) {
					type = "ISOSURFACE";
				} else if (type.equals("JMOL")) {
					type = "ZIPALL";
				} else if (type.equals("HIS")) {
					type = "HISTORY";
				}
			}
			if (type.equals("COORD"))
				type = (fileName != null && fileName.indexOf(".") >= 0 ? fileName
						.substring(fileName.lastIndexOf(".") + 1).toUpperCase()
						: "XYZ");
			isImage = Parser.isOneOf(type,
					"GIF;JPEG64;JPEG;JPG64;JPG;PPM;PNG;PNGJ;PNGT;SCENE");
			if (scripts != null) {
				if (type.equals("PNG"))
					type = "PNGJ";
				if (!type.equals("PNGJ") && !type.equals("ZIPALL"))
					error(ERROR_invalidArgument);
			}
			if (isImage && isShow)
				type = "JPG64";
			else if (!isImage
					&& !isExport
					&& !Parser
							.isOneOf(
									type,
									"SCENE;JMOL;ZIP;ZIPALL;SPT;HISTORY;MO;ISOSURFACE;MESH;PMESH;VAR;FILE;FUNCTION;CD;CML;XYZ;XYZRN;XYZVIB;MENU;MOL;PDB;PGRP;PQR;QUAT;RAMA;SDF;V2000;V3000;INLINE"))
				error(ERROR_writeWhat,
						"COORDS|FILE|FUNCTIONS|HISTORY|IMAGE|INLINE|ISOSURFACE|JMOL|MENU|MO|POINTGROUP|QUATERNION [w,x,y,z] [derivative]"
								+ "|RAMACHANDRAN|SPT|STATE|VAR x|ZIP|ZIPALL  CLIPBOARD",
						"CML|GIF|JPG|JPG64|JMOL|JVXL|MESH|MOL|PDB|PMESH|PNG|PNGJ|PNGT|PPM|PQR|SDF|V2000|V3000|SPT|XJVXL|XYZ|XYZRN|XYZVIB|ZIP"
								+ driverList.toUpperCase().replace(';', '|'));
			if (isSyntaxCheck)
				return "";
			Object bytes = null;
			boolean doDefer = false;
			if (data == null) {
				data = type.intern();
				if (isExport) {
					// POV-Ray uses a BufferedWriter instead of a StringBuffer.
					// todo -- there's no reason this data has to be done this
					// way.
					// we could send all of them out to file directly
					fullPath[0] = fileName;
					data = viewer.generateOutput(data, isCommand
							|| fileName != null ? fullPath : null, width,
							height);
					if (data == null || data.length() == 0)
						return "";
					if (!isCommand)
						return data;
					if ((type.equals("Povray") || type.equals("Idtf"))
							&& fullPath[0] != null) {
						String ext = (type.equals("Idtf") ? ".tex" : ".ini");
						fileName = fullPath[0] + ext;
						msg = viewer.createImage(fileName, ext, data, null,
								Integer.MIN_VALUE, 0, 0, null, 0, fullPath);
						if (type.equals("Idtf"))
							data = data.substring(0,
									data.indexOf("\\begin{comment}"));
						data = "Created " + fullPath[0] + ":\n\n" + data;
					} else {
						msg = data;
					}
					if (msg != null) {
						if (!msg.startsWith("OK"))
							evalError(msg, null);
						scriptStatusOrBuffer(data);
					}
					return "";
				} else if (data == "MENU") {
					data = viewer.getMenu("");
				} else if (data == "PGRP") {
					data = viewer.getPointGroupAsString(type2.equals("draw"),
							null, 0, 1.0f);
				} else if (data == "PDB" || data == "PQR") {
					if (isShow) {
						data = viewer.getPdbData(null, null);
					} else {
						doDefer = true;
						/*
						 * OutputStream os = viewer.getOutputStream(fileName,
						 * fullPath); msg = viewer.getPdbData(null, new
						 * BufferedOutputStream(os)); if (msg != null) msg =
						 * "OK " + msg + " " + fullPath[0]; try { os.close(); }
						 * catch (IOException e) { // TODO }
						 */
					}
				} else if (data == "FILE") {
					if (isShow)
						data = viewer.getCurrentFileAsString();
					else
						doDefer = true;
					if ("?".equals(fileName))
						fileName = "?Jmol." + viewer.getParameter("_fileType");
				} else if ((data == "SDF" || data == "MOL" || data == "V2000"
						|| data == "V3000" || data == "CD")
						&& isCoord) {
					data = viewer.getModelExtract("selected", true, data);
					if (data.startsWith("ERROR:"))
						bytes = data;
				} else if (data == "XYZ" || data == "XYZRN" || data == "XYZVIB"
						|| data == "MOL" || data == "SDF" || data == "V2000"
						|| data == "V3000" || data == "CML" || data == "CD") {
					data = viewer.getData("selected", data);
					if (data.startsWith("ERROR:"))
						bytes = data;
				} else if (data == "FUNCTION") {
					data = viewer.getFunctionCalls(null);
					type = "TXT";
				} else if (data == "VAR") {
					data = ((ScriptVariable) getParameter(
							ScriptVariable.sValue(tokenAt(isCommand ? 2 : 1,
									args)), Token.variable)).asString();
					type = "TXT";
				} else if (data == "SPT") {
					if (isCoord) {
						BitSet tainted = viewer
								.getTaintedAtoms(AtomCollection.TAINT_COORD);
						viewer.setAtomCoordRelative(new Point3f(0, 0, 0), null);
						data = (String) viewer.getProperty("string",
								"stateInfo", null);
						viewer.setTaintedAtoms(tainted,
								AtomCollection.TAINT_COORD);
					} else {
						data = (String) viewer.getProperty("string",
								"stateInfo", null);
						if (localPath != null || remotePath != null)
							data = FileManager.setScriptFileReferences(data,
									localPath, remotePath, null);
					}
				} else if (data == "ZIP" || data == "ZIPALL") {

					data = (String) viewer.getProperty("string", "stateInfo",
							null);
					bytes = viewer.createZip(fileName, type, data, scripts);
				} else if (data == "HISTORY") {
					data = viewer.getSetHistory(Integer.MAX_VALUE);
					type = "SPT";
				} else if (data == "MO") {
					data = getMoJvxl(Integer.MAX_VALUE);
					type = "XJVXL";
				} else if (data == "PMESH") {
					if ((data = getIsosurfaceJvxl(true,
							JmolConstants.SHAPE_PMESH)) == null)
						error(ERROR_noData);
					type = "XJVXL";
				} else if (data == "ISOSURFACE" || data == "MESH") {
					if ((data = getIsosurfaceJvxl(data == "MESH",
							JmolConstants.SHAPE_ISOSURFACE)) == null)
						error(ERROR_noData);
					type = (data.indexOf("<?xml") >= 0 ? "XJVXL" : "JVXL");
					if (!isShow)
						showString((String) getShapeProperty(
								JmolConstants.SHAPE_ISOSURFACE, "jvxlFileInfo"));
				} else {
					// image
					len = -1;
					if (quality < 0)
						quality = -1;
				}
				if (data == null && !doDefer)
					data = "";
				if (len == 0 && !doDefer)
					len = (bytes == null ? data.length()
							: bytes instanceof String ? ((String) bytes)
									.length() : ((byte[]) bytes).length);
				if (isImage) {
					refresh();
					if (width < 0)
						width = viewer.getScreenWidth();
					if (height < 0)
						height = viewer.getScreenHeight();
				}
			}
			if (!isCommand)
				return data;
			if (isShow) {
				showString(data, true);
				return "";
			}
			if (bytes != null && bytes instanceof String) {
				// load error or completion message here
				scriptStatusOrBuffer((String) bytes);
				return (String) bytes;
			}
			if (type.equals("SCENE"))
				bytes = sceneType;
			else if (bytes == null && (!isImage || fileName != null))
				bytes = data;
			if (doDefer)
				msg = viewer.streamFileData(fileName, type, type2, 0, null);
			else
				msg = viewer.createImage(fileName, type, bytes, scripts,
						quality, width, height, bsFrames, nVibes, fullPath);
		}
		if (!isSyntaxCheck && msg != null) {
			if (!msg.startsWith("OK"))
				evalError(msg, null);
			scriptStatusOrBuffer(msg
					+ (isImage ? "; width=" + width + "; height=" + height : ""));
			return msg;
		}
		return "";
	}

	private void show() throws ScriptException {
		String value = null;
		String str = parameterAsString(1);
		String msg = null;
		String name = null;
		int len = 2;
		Token token = getToken(1);
		int tok = (token instanceof ScriptVariable ? Token.nada : token.tok);
		if (tok == Token.string) {
			token = Token.getTokenFromName(str.toLowerCase());
			if (token != null)
				tok = token.tok;
		}
		if (tok != Token.symop && tok != Token.state)
			checkLength(-3);
		if (statementLength == 2 && str.indexOf("?") >= 0) {
			showString(viewer
					.getAllSettings(str.substring(0, str.indexOf("?"))));
			return;
		}
		switch (tok) {
		case Token.nada:
			msg = ((ScriptVariable) theToken).escape();
			break;
		case Token.cache:
			msg = Escape.escape(viewer.cacheList());
			break;
		case Token.dssp:
			checkLength(2);
			if (!isSyntaxCheck)
				msg = viewer.calculateStructures(null, true, false);
			break;
		case Token.pathforallfiles:
			checkLength(2);
			if (!isSyntaxCheck)
				msg = viewer.getPathForAllFiles();
			break;
		case Token.nmr:
		case Token.smiles:
		case Token.drawing:
		case Token.chemical:
			checkLength(tok == Token.chemical ? 3 : 2);
			if (isSyntaxCheck)
				return;
			msg = viewer.getSmiles(0, 0, viewer.getSelectionSet(false), false,
					true, false, false);
			switch (tok) {
			case Token.drawing:
				if (msg.length() > 0) {
					viewer.show2D(msg);
					return;
				}
				msg = "Could not show drawing -- Either insufficient atoms are selected or the model is a PDB file.";
				break;
			case Token.nmr:
				if (msg.length() > 0) {
					viewer.showNMR(msg);
					return;
				}
				msg = "Could not show nmr -- Either insufficient atoms are selected or the model is a PDB file.";
				break;
			case Token.chemical:
				len = 3;
				String info = null;
				if (msg.length() > 0) {
					char type = '/';
					switch (getToken(2).tok) {
					case Token.inchi:
						type = 'I';
						break;
					case Token.inchikey:
						type = 'K';
						break;
					case Token.name:
						type = 'N';
						break;
					default:
						info = parameterAsString(2);
					}
					msg = viewer.getChemicalInfo(msg, type, info);
					if (msg.indexOf("FileNotFound") >= 0)
						msg = "?";
				} else {
					msg = "Could not show name -- Either insufficient atoms are selected or the model is a PDB file.";
				}
			}
			break;
		case Token.symop:
			if (statementLength > 3) {
				Point3f pt1 = centerParameter(2);
				Point3f pt2 = centerParameter(++iToken);
				if (!isSyntaxCheck)
					msg = viewer.getSymmetryOperation(null, 0, pt1, pt2, false);
				len = ++iToken;
			} else {
				int iop = (checkLength23() == 2 ? 0 : intParameter(2));
				if (!isSyntaxCheck)
					msg = viewer.getSymmetryOperation(null, iop, null, null,
							false);
				len = -3;
			}
			break;
		case Token.vanderwaals:
			EnumVdw vdwType = null;
			if (statementLength > 2) {
				vdwType = EnumVdw.getVdwType(parameterAsString(2));
				if (vdwType == null)
					error(ERROR_invalidArgument);
			}
			if (!isSyntaxCheck)
				showString(viewer.getDefaultVdwTypeNameOrData(0, vdwType));
			return;
		case Token.function:
			checkLength23();
			if (!isSyntaxCheck)
				showString(viewer.getFunctionCalls(optParameterAsString(2)));
			return;
		case Token.set:
			checkLength(2);
			if (!isSyntaxCheck)
				showString(viewer.getAllSettings(null));
			return;
		case Token.url:
			// in a new window
			if ((len = statementLength) == 2) {
				if (!isSyntaxCheck)
					viewer.showUrl(getFullPathName());
				return;
			}
			name = parameterAsString(2);
			if (!isSyntaxCheck)
				viewer.showUrl(name);
			return;
		case Token.color:
			str = "defaultColorScheme";
			break;
		case Token.scale3d:
			str = "scaleAngstromsPerInch";
			break;
		case Token.quaternion:
		case Token.ramachandran:
			if (isSyntaxCheck)
				return;
			int modelIndex = viewer.getCurrentModelIndex();
			if (modelIndex < 0)
				error(ERROR_multipleModelsDisplayedNotOK, "show "
						+ theToken.value);
			msg = plot(statement);
			len = statementLength;
			break;
		case Token.trace:
			if (!isSyntaxCheck)
				msg = getContext(false);
			break;
		case Token.colorscheme:
			name = optParameterAsString(2);
			if (name.length() > 0)
				len = 3;
			if (!isSyntaxCheck)
				value = viewer.getColorSchemeList(name);
			break;
		case Token.variables:
			if (!isSyntaxCheck)
				msg = viewer.getVariableList() + getContext(true);
			break;
		case Token.trajectory:
			if (!isSyntaxCheck)
				msg = viewer.getTrajectoryInfo();
			break;
		case Token.historylevel:
			value = "" + commandHistoryLevelMax;
			break;
		case Token.loglevel:
			value = "" + Logger.getLogLevel();
			break;
		case Token.debugscript:
			value = "" + viewer.getDebugScript();
			break;
		case Token.strandcount:
			msg = "set strandCountForStrands "
					+ viewer.getStrandCount(JmolConstants.SHAPE_STRANDS)
					+ "; set strandCountForMeshRibbon "
					+ viewer.getStrandCount(JmolConstants.SHAPE_MESHRIBBON);
			break;
		case Token.timeout:
			msg = viewer.showTimeout((len = statementLength) == 2 ? null
					: parameterAsString(2));
			break;
		case Token.defaultlattice:
			value = Escape.escapePt(viewer.getDefaultLattice());
			break;
		case Token.minimize:
			if (!isSyntaxCheck)
				msg = viewer.getMinimizationInfo();
			break;
		case Token.axes:
			switch (viewer.getAxesMode()) {
			case UNITCELL:
				msg = "set axesUnitcell";
				break;
			case BOUNDBOX:
				msg = "set axesWindow";
				break;
			default:
				msg = "set axesMolecular";
			}
			break;
		case Token.bondmode:
			msg = "set bondMode "
					+ (viewer.getBondSelectionModeOr() ? "OR" : "AND");
			break;
		case Token.strands:
			if (!isSyntaxCheck)
				msg = "set strandCountForStrands "
						+ viewer.getStrandCount(JmolConstants.SHAPE_STRANDS)
						+ "; set strandCountForMeshRibbon "
						+ viewer.getStrandCount(JmolConstants.SHAPE_MESHRIBBON);
			break;
		case Token.hbond:
			msg = "set hbondsBackbone " + viewer.getHbondsBackbone()
					+ ";set hbondsSolid " + viewer.getHbondsSolid();
			break;
		case Token.spin:
			if (!isSyntaxCheck)
				msg = viewer.getSpinState();
			break;
		case Token.ssbond:
			msg = "set ssbondsBackbone " + viewer.getSsbondsBackbone();
			break;
		case Token.display:// deprecated
		case Token.selectionhalos:
			msg = "selectionHalos "
					+ (viewer.getSelectionHaloEnabled(false) ? "ON" : "OFF");
			break;
		case Token.hetero:
			msg = "set selectHetero " + viewer.getRasmolSetting(tok);
			break;
		case Token.addhydrogens:
			msg = Escape.escapeArray(viewer.getAdditionalHydrogens(null, true,
					true, null));
			break;
		case Token.hydrogen:
			msg = "set selectHydrogens " + viewer.getRasmolSetting(tok);
			break;
		case Token.ambientpercent:
		case Token.diffusepercent:
		case Token.specular:
		case Token.specularpower:
		case Token.specularexponent:
		case Token.lighting:
			if (!isSyntaxCheck)
				msg = viewer.getSpecularState();
			break;
		case Token.save:
			if (!isSyntaxCheck)
				msg = viewer.listSavedStates();
			break;
		case Token.unitcell:
			if (!isSyntaxCheck)
				msg = viewer.getUnitCellInfoText();
			break;
		case Token.coord:
			if ((len = statementLength) == 2) {
				if (!isSyntaxCheck)
					msg = viewer.getCoordinateState(viewer
							.getSelectionSet(false));
				break;
			}
			String nameC = parameterAsString(2);
			if (!isSyntaxCheck)
				msg = viewer.getSavedCoordinates(nameC);
			break;
		case Token.state:
			if ((len = statementLength) == 2) {
				if (!isSyntaxCheck)
					msg = viewer.getStateInfo();
				break;
			}
			name = parameterAsString(2);
			if (name.equals("/") && (len = statementLength) == 4) {
				name = parameterAsString(3).toLowerCase();
				if (!isSyntaxCheck) {
					String[] info = TextFormat.split(viewer.getStateInfo(),
							'\n');
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < info.length; i++)
						if (info[i].toLowerCase().indexOf(name) >= 0)
							sb.append(info[i]).append('\n');
					msg = sb.toString();
				}
				break;
			} else if (tokAt(2) == Token.file && (len = statementLength) == 4) {
				if (!isSyntaxCheck)
					msg = viewer.getEmbeddedFileState(parameterAsString(3));
				break;
			}
			len = 3;
			if (!isSyntaxCheck)
				msg = viewer.getSavedState(name);
			break;
		case Token.structure:
			if ((len = statementLength) == 2) {
				if (!isSyntaxCheck)
					msg = viewer.getProteinStructureState();
				break;
			}
			String shape = parameterAsString(2);
			if (!isSyntaxCheck)
				msg = viewer.getSavedStructure(shape);
			break;
		case Token.data:
			String type = ((len = statementLength) == 3 ? parameterAsString(2)
					: null);
			if (!isSyntaxCheck) {
				Object[] data = (type == null ? this.data : viewer
						.getData(type));
				msg = (data == null ? "no data" : "data  \""
						+ data[0]
						+ "\"\n"
						+ (data[1] instanceof float[] ? Escape.escapeFloatA(
								(float[]) data[1], false)
								: data[1] instanceof float[][] ? Escape
										.escapeFloatAA((float[][]) data[1],
												false) : "" + data[1])
						+ "\nend \"" + data[0] + "\";");
			}
			break;
		case Token.spacegroup:
			Map<String, Object> info = null;
			if ((len = statementLength) == 2) {
				if (!isSyntaxCheck) {
					info = viewer.getSpaceGroupInfo(null);
				}
			} else {
				String sg = parameterAsString(2);
				if (!isSyntaxCheck)
					info = viewer.getSpaceGroupInfo(TextFormat.simpleReplace(
							sg, "''", "\""));
			}
			if (info != null)
				msg = "" + info.get("spaceGroupInfo")
						+ info.get("symmetryInfo");
			break;
		case Token.dollarsign:
			len = 3;
			msg = setObjectProperty();
			break;
		case Token.boundbox:
			if (!isSyntaxCheck) {
				msg = viewer.getBoundBoxCommand(true);
			}
			break;
		case Token.center:
			if (!isSyntaxCheck)
				msg = "center " + Escape.escapePt(viewer.getRotationCenter());
			break;
		case Token.draw:
			if (!isSyntaxCheck)
				msg = (String) getShapeProperty(JmolConstants.SHAPE_DRAW,
						"command");
			break;
		case Token.file:
			// as a string
			if (statementLength == 2) {
				if (!isSyntaxCheck)
					msg = viewer.getCurrentFileAsString();
				if (msg == null)
					msg = "<unavailable>";
				break;
			}
			len = 3;
			value = parameterAsString(2);
			if (!isSyntaxCheck)
				msg = viewer.getFileAsString(value);
			break;
		case Token.frame:
			if (tokAt(2) == Token.all && (len = 3) > 0)
				msg = viewer.getModelFileInfoAll();
			else
				msg = viewer.getModelFileInfo();
			break;
		case Token.history:
			int n = ((len = statementLength) == 2 ? Integer.MAX_VALUE
					: intParameter(2));
			if (n < 1)
				error(ERROR_invalidArgument);
			if (!isSyntaxCheck) {
				viewer.removeCommand();
				msg = viewer.getSetHistory(n);
			}
			break;
		case Token.isosurface:
			if (!isSyntaxCheck)
				msg = (String) getShapeProperty(JmolConstants.SHAPE_ISOSURFACE,
						"jvxlDataXml");
			break;
		case Token.mo:
			if (optParameterAsString(2).equalsIgnoreCase("list")) {
				msg = viewer.getMoInfo(-1);
				len = 3;
			} else {
				int ptMO = ((len = statementLength) == 2 ? Integer.MIN_VALUE
						: intParameter(2));
				if (!isSyntaxCheck)
					msg = getMoJvxl(ptMO);
			}
			break;
		case Token.model:
			if (!isSyntaxCheck)
				msg = viewer.getModelInfoAsString();
			break;
		case Token.measurements:
			if (!isSyntaxCheck)
				msg = viewer.getMeasurementInfoAsString();
			break;
		case Token.translation:
		case Token.rotation:
		case Token.moveto:
			if (!isSyntaxCheck)
				msg = viewer.getOrientationText(tok, null);
			break;
		case Token.orientation:
			len = 2;
			if (statementLength > 3)
				break;
			switch (tok = tokAt(2)) {
			case Token.translation:
			case Token.rotation:
			case Token.moveto:
			case Token.nada:
				if (!isSyntaxCheck)
					msg = viewer.getOrientationText(tok, null);
				break;
			default:
				name = optParameterAsString(2);
				msg = viewer.getOrientationText(0, name);
			}
			len = statementLength;
			break;
		case Token.pdbheader:
			if (!isSyntaxCheck)
				msg = viewer.getPDBHeader();
			break;
		case Token.pointgroup:
			pointGroup();
			return;
		case Token.symmetry:
			if (!isSyntaxCheck)
				msg = viewer.getSymmetryInfoAsString();
			break;
		case Token.transform:
			if (!isSyntaxCheck)
				msg = "transform:\n" + viewer.getTransformText();
			break;
		case Token.zoom:
			msg = "zoom "
					+ (viewer.getZoomEnabled() ? ("" + viewer.getZoomSetting())
							: "off");
			break;
		case Token.frank:
			msg = (viewer.getShowFrank() ? "frank ON" : "frank OFF");
			break;
		case Token.radius:
			str = "solventProbeRadius";
			break;
		// Chime related
		case Token.basepair:
		case Token.chain:
		case Token.sequence:
		case Token.residue:
		case Token.selected:
		case Token.group:
		case Token.atoms:
		case Token.info:
		case Token.bonds:
			msg = viewer.getChimeInfo(tok);
			break;
		// not implemented
		case Token.echo:
		case Token.fontsize:
		case Token.property: // huh? why?
		case Token.help:
		case Token.solvent:
			value = "?";
			break;
		case Token.identifier:
			if (str.equalsIgnoreCase("fileHeader")) {
				if (!isSyntaxCheck)
					msg = viewer.getPDBHeader();
			} else if (str.equalsIgnoreCase("menu")) {
				if (!isSyntaxCheck)
					value = viewer.getMenu("");
			} else if (str.equalsIgnoreCase("mouse")) {
				String qualifiers = ((len = statementLength) == 2 ? null
						: parameterAsString(2));
				if (!isSyntaxCheck)
					msg = viewer.getBindingInfo(qualifiers);
			}
			break;
		}
		checkLength(len);
		if (isSyntaxCheck)
			return;
		if (msg != null)
			showString(msg);
		else if (value != null)
			showString(str + " = " + value);
		else if (str != null) {
			if (str.indexOf(" ") >= 0)
				showString(str);
			else
				showString(str + " = " + getParameterEscaped(str));
		}
	}

	private String getIsosurfaceJvxl(boolean asMesh, int iShape) {
		if (isSyntaxCheck)
			return "";
		return (String) getShapeProperty(iShape, asMesh ? "jvxlMeshX"
				: "jvxlDataXml");
	}

	@SuppressWarnings("unchecked")
	private String getMoJvxl(int ptMO) throws ScriptException {
		// 0: all; Integer.MAX_VALUE: current;
		shapeManager.loadShape(JmolConstants.SHAPE_MO);
		int modelIndex = viewer.getCurrentModelIndex();
		if (modelIndex < 0)
			error(ERROR_multipleModelsDisplayedNotOK, "MO isosurfaces");
		Map<String, Object> moData = (Map<String, Object>) viewer
				.getModelAuxiliaryInfo(modelIndex, "moData");
		if (moData == null)
			error(ERROR_moModelError);
		Integer n = (Integer) getShapeProperty(JmolConstants.SHAPE_MO,
				"moNumber");
		if (n == null || n.intValue() == 0) {
			setShapeProperty(JmolConstants.SHAPE_MO, "init",
					Integer.valueOf(modelIndex));
		} else if (ptMO == Integer.MAX_VALUE) {
		}
		setShapeProperty(JmolConstants.SHAPE_MO, "moData", moData);
		return (String) getShapeProperty(JmolConstants.SHAPE_MO, "showMO", ptMO);
	}

	private void draw() throws ScriptException {
		shapeManager.loadShape(JmolConstants.SHAPE_DRAW);
		switch (tokAt(1)) {
		case Token.list:
			if (listIsosurface(JmolConstants.SHAPE_DRAW))
				return;
			break;
		case Token.pointgroup:
			pointGroup();
			return;
		case Token.helix:
		case Token.quaternion:
		case Token.ramachandran:
			plot(statement);
			return;
		}
		boolean havePoints = false;
		boolean isInitialized = false;
		boolean isSavedState = false;
		boolean isTranslucent = false;
		boolean isIntersect = false;
		boolean isFrame = false;
		Point4f plane;
		int tokIntersect = 0;
		float translucentLevel = Float.MAX_VALUE;
		int colorArgb = Integer.MIN_VALUE;
		int intScale = 0;
		String swidth = "";
		int iptDisplayProperty = 0;
		Point3f center = null;
		String thisId = initIsosurface(JmolConstants.SHAPE_DRAW);
		boolean idSeen = (thisId != null);
		boolean isWild = (idSeen && getShapeProperty(JmolConstants.SHAPE_DRAW,
				"ID") == null);
		int[] connections = null;
		int iConnect = 0;
		for (int i = iToken; i < statementLength; ++i) {
			String propertyName = null;
			Object propertyValue = null;
			switch (getToken(i).tok) {
			case Token.unitcell:
			case Token.boundbox:
				if (isSyntaxCheck)
					break;
				List<Object> vp = viewer.getPlaneIntersection(theTok, null,
						intScale / 100f, 0);
				intScale = 0;
				propertyName = "polygon";
				propertyValue = vp;
				havePoints = true;
				break;
			case Token.connect:
				connections = new int[4];
				iConnect = 4;
				float[] farray = floatParameterSet(++i, 4, 4);
				i = iToken;
				for (int j = 0; j < 4; j++)
					connections[j] = (int) farray[j];
				havePoints = true;
				break;
			case Token.bonds:
			case Token.atoms:
				if (connections == null
						|| iConnect > (theTok == Token.bondcount ? 2 : 3)) {
					iConnect = 0;
					connections = new int[] { -1, -1, -1, -1 };
				}
				connections[iConnect++] = atomExpressionAt(++i).nextSetBit(0);
				i = iToken;
				connections[iConnect++] = (theTok == Token.bonds ? atomExpressionAt(
						++i).nextSetBit(0)
						: -1);
				i = iToken;
				havePoints = true;
				break;
			case Token.slab:
				switch (getToken(++i).tok) {
				case Token.dollarsign:
					propertyName = "slab";
					propertyValue = objectNameParameter(++i);
					i = iToken;
					havePoints = true;
					break;
				default:
					error(ERROR_invalidArgument);
				}
				break;
			case Token.intersection:
				switch (getToken(++i).tok) {
				case Token.unitcell:
				case Token.boundbox:
					tokIntersect = theTok;
					isIntersect = true;
					continue;
				case Token.dollarsign:
					propertyName = "intersect";
					propertyValue = objectNameParameter(++i);
					i = iToken;
					isIntersect = true;
					havePoints = true;
					break;
				default:
					error(ERROR_invalidArgument);
				}
				break;
			case Token.polygon:
				propertyName = "polygon";
				havePoints = true;
				List<Object> v = new ArrayList<Object>();
				int nVertices = 0;
				int nTriangles = 0;
				Point3f[] points = null;
				List<ScriptVariable> vpolygons = null;
				if (isArrayParameter(++i)) {
					points = getPointArray(i, -1);
					nVertices = points.length;
				} else {
					nVertices = Math.max(0, intParameter(i));
					points = new Point3f[nVertices];
					for (int j = 0; j < nVertices; j++)
						points[j] = centerParameter(++iToken);
				}
				switch (getToken(++iToken).tok) {
				case Token.matrix3f:
				case Token.matrix4f:
					ScriptVariable sv = new ScriptVariable(theToken);
					sv.toArray();
					vpolygons = sv.getList();
					nTriangles = vpolygons.size();
					break;
				case Token.varray:
					vpolygons = ((ScriptVariable) theToken).getList();
					nTriangles = vpolygons.size();
					break;
				default:
					nTriangles = Math.max(0, intParameter(iToken));
				}
				int[][] polygons = new int[nTriangles][];
				for (int j = 0; j < nTriangles; j++) {
					float[] f = (vpolygons == null ? floatParameterSet(
							++iToken, 3, 4) : ScriptVariable.flistValue(
							vpolygons.get(j), 0));
					if (f.length < 3 || f.length > 4)
						error(ERROR_invalidArgument);
					polygons[j] = new int[] { (int) f[0], (int) f[1],
							(int) f[2], (f.length == 3 ? 7 : (int) f[3]) };
				}
				if (nVertices > 0) {
					v.add(points);
					v.add(polygons);
				} else {
					v = null;
				}
				propertyValue = v;
				i = iToken;
				break;
			case Token.symop:
				String xyz = null;
				int iSym = 0;
				plane = null;
				Point3f target = null;
				switch (tokAt(++i)) {
				case Token.string:
					xyz = stringParameter(i);
					break;
				case Token.matrix4f:
					xyz = ScriptVariable.sValue(getToken(i));
					break;
				case Token.integer:
				default:
					if (!isCenterParameter(i))
						iSym = intParameter(i++);
					if (isCenterParameter(i))
						center = centerParameter(i);
					if (isCenterParameter(iToken + 1))
						target = centerParameter(++iToken);
					if (isSyntaxCheck)
						return;
					i = iToken;
				}
				BitSet bsAtoms = null;
				if (center == null && i + 1 < statementLength) {
					center = centerParameter(++i);
					// draw ID xxx symop [n or "x,-y,-z"] [optional {center}]
					// so we also check here for the atom set to get the right
					// model
					bsAtoms = (tokAt(i) == Token.bitset
							|| tokAt(i) == Token.expressionBegin ? atomExpressionAt(i)
							: null);
					i = iToken + 1;
				}
				checkLast(iToken);
				if (!isSyntaxCheck)
					runScript((String) viewer.getSymmetryInfo(bsAtoms, xyz,
							iSym, center, target, thisId, Token.draw));
				return;
			case Token.frame:
				isFrame = true;
				// draw ID xxx frame {center} {q1 q2 q3 q4}
				continue;
			case Token.leftbrace:
			case Token.point4f:
			case Token.point3f:
				// {X, Y, Z}
				if (theTok == Token.point4f || !isPoint3f(i)) {
					propertyValue = getPoint4f(i);
					if (isFrame) {
						checkLast(iToken);
						if (!isSyntaxCheck)
							runScript((new Quaternion((Point4f) propertyValue))
									.draw((thisId == null ? "frame" : thisId),
											" " + swidth,
											(center == null ? new Point3f()
													: center), intScale / 100f));
						return;
					}
					propertyName = "planedef";
				} else {
					propertyValue = center = getPoint3f(i, true);
					propertyName = "coord";
				}
				i = iToken;
				havePoints = true;
				break;
			case Token.hkl:
			case Token.plane:
				if (!havePoints && !isIntersect && tokIntersect == 0
						&& theTok != Token.hkl) {
					propertyName = "plane";
					break;
				}
				if (theTok == Token.plane) {
					plane = planeParameter(++i);
				} else {
					plane = hklParameter(++i);
				}
				i = iToken;
				if (tokIntersect != 0) {
					if (isSyntaxCheck)
						break;
					List<Object> vpc = viewer.getPlaneIntersection(
							tokIntersect, plane, intScale / 100f, 0);
					intScale = 0;
					propertyName = "polygon";
					propertyValue = vpc;
				} else {
					propertyValue = plane;
					propertyName = "planedef";
				}
				havePoints = true;
				break;
			case Token.linedata:
				propertyName = "lineData";
				propertyValue = floatParameterSet(++i, 0, Integer.MAX_VALUE);
				i = iToken;
				havePoints = true;
				break;
			case Token.bitset:
			case Token.expressionBegin:
				propertyName = "atomSet";
				propertyValue = atomExpressionAt(i);
				if (isFrame)
					center = centerParameter(i);
				i = iToken;
				havePoints = true;
				break;
			case Token.varray:
				propertyName = "modelBasedPoints";
				propertyValue = ScriptVariable.listValue(theToken);
				havePoints = true;
				break;
			case Token.spacebeforesquare:
			case Token.comma:
				break;
			case Token.leftsquare:
				// [x y] or [x y %]
				propertyValue = xypParameter(i);
				if (propertyValue != null) {
					i = iToken;
					propertyName = "coord";
					havePoints = true;
					break;
				}
				if (isSavedState)
					error(ERROR_invalidArgument);
				isSavedState = true;
				break;
			case Token.rightsquare:
				if (!isSavedState)
					error(ERROR_invalidArgument);
				isSavedState = false;
				break;
			case Token.reverse:
				propertyName = "reverse";
				break;
			case Token.string:
				propertyValue = stringParameter(i);
				propertyName = "title";
				break;
			case Token.vector:
				propertyName = "vector";
				break;
			case Token.length:
				propertyValue = Float.valueOf(floatParameter(++i));
				propertyName = "length";
				break;
			case Token.decimal:
				// $drawObject
				propertyValue = Float.valueOf(floatParameter(i));
				propertyName = "length";
				break;
			case Token.modelindex:
				propertyName = "modelIndex";
				propertyValue = Integer.valueOf(intParameter(++i));
				break;
			case Token.integer:
				if (isSavedState) {
					propertyName = "modelIndex";
					propertyValue = Integer.valueOf(intParameter(i));
				} else {
					intScale = intParameter(i);
				}
				break;
			case Token.scale:
				if (++i >= statementLength)
					error(ERROR_numberExpected);
				switch (getToken(i).tok) {
				case Token.integer:
					intScale = intParameter(i);
					continue;
				case Token.decimal:
					intScale = (int) (floatParameter(i) * 100);
					continue;
				}
				error(ERROR_numberExpected);
				break;
			case Token.id:
				thisId = setShapeId(JmolConstants.SHAPE_DRAW, ++i, idSeen);
				isWild = (getShapeProperty(JmolConstants.SHAPE_DRAW, "ID") == null);
				i = iToken;
				break;
			case Token.modelbased:
				propertyName = "fixed";
				propertyValue = Boolean.FALSE;
				break;
			case Token.fixed:
				propertyName = "fixed";
				propertyValue = Boolean.TRUE;
				break;
			case Token.offset:
				Point3f pt = getPoint3f(++i, true);
				i = iToken;
				propertyName = "offset";
				propertyValue = pt;
				break;
			case Token.crossed:
				propertyName = "crossed";
				break;
			case Token.width:
				propertyValue = Float.valueOf(floatParameter(++i));
				propertyName = "width";
				swidth = propertyName + " " + propertyValue;
				break;
			case Token.line:
				propertyName = "line";
				propertyValue = Boolean.TRUE;
				break;
			case Token.curve:
				propertyName = "curve";
				break;
			case Token.arc:
				propertyName = "arc";
				break;
			case Token.arrow:
				propertyName = "arrow";
				break;
			case Token.circle:
				propertyName = "circle";
				break;
			case Token.cylinder:
				propertyName = "cylinder";
				break;
			case Token.vertices:
				propertyName = "vertices";
				break;
			case Token.nohead:
				propertyName = "nohead";
				break;
			case Token.barb:
				propertyName = "isbarb";
				break;
			case Token.rotate45:
				propertyName = "rotate45";
				break;
			case Token.perpendicular:
				propertyName = "perp";
				break;
			case Token.radius:
			case Token.diameter:
				boolean isRadius = (theTok == Token.radius);
				float f = floatParameter(++i);
				if (isRadius)
					f *= 2;
				propertyValue = Float.valueOf(f);
				propertyName = (isRadius || tokAt(i) == Token.decimal ? "width"
						: "diameter");
				swidth = propertyName
						+ (tokAt(i) == Token.decimal ? " " + f : " "
								+ ((int) f));
				break;
			case Token.dollarsign:
				// $drawObject[m]
				if ((tokAt(i + 2) == Token.leftsquare || isFrame)) {
					Point3f pto = center = centerParameter(i);
					i = iToken;
					propertyName = "coord";
					propertyValue = pto;
					havePoints = true;
					break;
				}
				// $drawObject
				propertyValue = objectNameParameter(++i);
				propertyName = "identifier";
				havePoints = true;
				break;
			case Token.color:
			case Token.translucent:
			case Token.opaque:
				if (theTok != Token.color)
					--i;
				if (tokAt(i + 1) == Token.translucent) {
					i++;
					isTranslucent = true;
					if (isFloatParameter(i + 1))
						translucentLevel = getTranslucentLevel(++i);
				} else if (tokAt(i + 1) == Token.opaque) {
					i++;
					isTranslucent = true;
					translucentLevel = 0;
				}
				if (isColorParam(i + 1)) {
					colorArgb = getArgbParam(++i);
					i = iToken;
				} else if (!isTranslucent) {
					error(ERROR_invalidArgument);
				}
				idSeen = true;
				continue;
			default:
				if (!setMeshDisplayProperty(JmolConstants.SHAPE_DRAW, 0, theTok)) {
					if (theTok == Token.times
							|| Token.tokAttr(theTok, Token.identifier)) {
						thisId = setShapeId(JmolConstants.SHAPE_DRAW, i, idSeen);
						i = iToken;
						break;
					}
					error(ERROR_invalidArgument);
				}
				if (iptDisplayProperty == 0)
					iptDisplayProperty = i;
				i = iToken;
				continue;
			}
			idSeen = (theTok != Token.delete);
			if (havePoints && !isInitialized && !isFrame) {
				setShapeProperty(JmolConstants.SHAPE_DRAW, "points",
						Integer.valueOf(intScale));
				isInitialized = true;
				intScale = 0;
			}
			if (havePoints && isWild)
				error(ERROR_invalidArgument);
			if (propertyName != null)
				setShapeProperty(JmolConstants.SHAPE_DRAW, propertyName,
						propertyValue);
		}
		if (havePoints) {
			setShapeProperty(JmolConstants.SHAPE_DRAW, "set", connections);
		}
		if (colorArgb != Integer.MIN_VALUE)
			setShapeProperty(JmolConstants.SHAPE_DRAW, "color",
					Integer.valueOf(colorArgb));
		if (isTranslucent)
			setShapeTranslucency(JmolConstants.SHAPE_DRAW, "", "translucent",
					translucentLevel, null);
		if (intScale != 0) {
			setShapeProperty(JmolConstants.SHAPE_DRAW, "scale",
					Integer.valueOf(intScale));
		}
		if (iptDisplayProperty > 0) {
			if (!setMeshDisplayProperty(JmolConstants.SHAPE_DRAW,
					iptDisplayProperty, 0))
				error(ERROR_invalidArgument);
		}
	}

	private void polyhedra() throws ScriptException {
		/*
		 * needsGenerating:
		 * 
		 * polyhedra [number of vertices and/or basis] [at most two selection
		 * sets] [optional type and/or edge] [optional design parameters]
		 * 
		 * OR else:
		 * 
		 * polyhedra [at most one selection set] [type-and/or-edge or
		 * on/off/delete]
		 */
		boolean needsGenerating = false;
		boolean onOffDelete = false;
		boolean typeSeen = false;
		boolean edgeParameterSeen = false;
		boolean isDesignParameter = false;
		int lighting = 0;
		int nAtomSets = 0;
		shapeManager.loadShape(JmolConstants.SHAPE_POLYHEDRA);
		setShapeProperty(JmolConstants.SHAPE_POLYHEDRA, "init", null);
		String setPropertyName = "centers";
		String decimalPropertyName = "radius_";
		boolean isTranslucent = false;
		float translucentLevel = Float.MAX_VALUE;
		int color = Integer.MIN_VALUE;
		for (int i = 1; i < statementLength; ++i) {
			String propertyName = null;
			Object propertyValue = null;
			switch (getToken(i).tok) {
			case Token.delete:
			case Token.on:
			case Token.off:
				if (i + 1 != statementLength || needsGenerating
						|| nAtomSets > 1 || nAtomSets == 0
						&& "to".equals(setPropertyName))
					error(ERROR_incompatibleArguments);
				propertyName = (theTok == Token.off ? "off"
						: theTok == Token.on ? "on" : "delete");
				onOffDelete = true;
				break;
			case Token.opEQ:
			case Token.comma:
				continue;
			case Token.bonds:
				if (nAtomSets > 0)
					error(ERROR_invalidParameterOrder);
				needsGenerating = true;
				propertyName = "bonds";
				break;
			case Token.radius:
				decimalPropertyName = "radius";
				continue;
			case Token.integer:
			case Token.decimal:
				if (nAtomSets > 0 && !isDesignParameter)
					error(ERROR_invalidParameterOrder);
				if (theTok == Token.integer) {
					if (decimalPropertyName == "radius_") {
						propertyName = "nVertices";
						propertyValue = Integer.valueOf(intParameter(i));
						needsGenerating = true;
						break;
					}
				}
				propertyName = (decimalPropertyName == "radius_" ? "radius"
						: decimalPropertyName);
				propertyValue = Float.valueOf(floatParameter(i));
				decimalPropertyName = "radius_";
				isDesignParameter = false;
				needsGenerating = true;
				break;
			case Token.bitset:
			case Token.expressionBegin:
				if (typeSeen)
					error(ERROR_invalidParameterOrder);
				if (++nAtomSets > 2)
					error(ERROR_badArgumentCount);
				if ("to".equals(setPropertyName))
					needsGenerating = true;
				propertyName = setPropertyName;
				setPropertyName = "to";
				propertyValue = atomExpressionAt(i);
				i = iToken;
				break;
			case Token.to:
				if (nAtomSets > 1)
					error(ERROR_invalidParameterOrder);
				if (tokAt(i + 1) == Token.bitset
						|| tokAt(i + 1) == Token.expressionBegin
						&& !needsGenerating) {
					propertyName = "toBitSet";
					propertyValue = atomExpressionAt(++i);
					i = iToken;
					needsGenerating = true;
					break;
				} else if (!needsGenerating) {
					error(ERROR_insufficientArguments);
				}
				setPropertyName = "to";
				continue;
			case Token.facecenteroffset:
				if (!needsGenerating)
					error(ERROR_insufficientArguments);
				decimalPropertyName = "faceCenterOffset";
				isDesignParameter = true;
				continue;
			case Token.distancefactor:
				if (!needsGenerating)
					error(ERROR_insufficientArguments);
				decimalPropertyName = "distanceFactor";
				isDesignParameter = true;
				continue;
			case Token.color:
			case Token.translucent:
			case Token.opaque:
				isTranslucent = false;
				if (theTok != Token.color)
					--i;
				if (tokAt(i + 1) == Token.translucent) {
					i++;
					isTranslucent = true;
					if (isFloatParameter(i + 1))
						translucentLevel = getTranslucentLevel(++i);
				} else if (tokAt(i + 1) == Token.opaque) {
					i++;
					isTranslucent = true;
					translucentLevel = 0;
				}
				if (isColorParam(i + 1)) {
					color = getArgbParam(i);
					i = iToken;
				} else if (!isTranslucent)
					error(ERROR_invalidArgument);
				continue;
			case Token.collapsed:
			case Token.flat:
				propertyName = "collapsed";
				propertyValue = (theTok == Token.collapsed ? Boolean.TRUE
						: Boolean.FALSE);
				if (typeSeen)
					error(ERROR_incompatibleArguments);
				typeSeen = true;
				break;
			case Token.noedges:
			case Token.edges:
			case Token.frontedges:
				if (edgeParameterSeen)
					error(ERROR_incompatibleArguments);
				propertyName = parameterAsString(i);
				edgeParameterSeen = true;
				break;
			case Token.fullylit:
				lighting = theTok;
				continue;
			default:
				if (isColorParam(i)) {
					color = getArgbParam(i);
					i = iToken;
					continue;
				}
				error(ERROR_invalidArgument);
			}
			setShapeProperty(JmolConstants.SHAPE_POLYHEDRA, propertyName,
					propertyValue);
			if (onOffDelete)
				return;
		}
		if (!needsGenerating && !typeSeen && !edgeParameterSeen
				&& lighting == 0)
			error(ERROR_insufficientArguments);
		if (needsGenerating)
			setShapeProperty(JmolConstants.SHAPE_POLYHEDRA, "generate", null);
		if (color != Integer.MIN_VALUE)
			setShapeProperty(JmolConstants.SHAPE_POLYHEDRA, "colorThis",
					Integer.valueOf(color));
		if (isTranslucent)
			setShapeTranslucency(JmolConstants.SHAPE_POLYHEDRA, "",
					"translucentThis", translucentLevel, null);
		if (lighting != 0)
			setShapeProperty(JmolConstants.SHAPE_POLYHEDRA, "token",
					Integer.valueOf(lighting));
		setShapeProperty(JmolConstants.SHAPE_POLYHEDRA, "init", null);
	}

	private void contact() throws ScriptException {
		shapeManager.loadShape(JmolConstants.SHAPE_CONTACT);
		if (tokAt(1) == Token.list
				&& listIsosurface(JmolConstants.SHAPE_CONTACT))
			return;
		int iptDisplayProperty = 0;
		iToken = 1;
		String thisId = initIsosurface(JmolConstants.SHAPE_CONTACT);
		boolean idSeen = (thisId != null);
		boolean isWild = (idSeen && getShapeProperty(
				JmolConstants.SHAPE_CONTACT, "ID") == null);
		BitSet bsA = null;
		BitSet bsB = null;
		BitSet bs = null;
		RadiusData rd = null;
		float[] params = null;
		boolean colorDensity = false;
		StringBuffer sbCommand = new StringBuffer();
		int minSet = Integer.MAX_VALUE;
		int displayType = Token.plane;
		int contactType = Token.nada;
		float distance = Float.NaN;
		float saProbeRadius = Float.NaN;
		boolean localOnly = true;
		Boolean intramolecular = null;
		Object userSlabObject = null;
		int colorpt = 0;
		boolean colorByType = false;
		int tok;
		boolean okNoAtoms = (iToken > 1);
		for (int i = iToken; i < statementLength; ++i) {
			switch (tok = getToken(i).tok) {
			// these first do not need atoms defined
			default:
				okNoAtoms = true;
				if (!setMeshDisplayProperty(JmolConstants.SHAPE_CONTACT, 0,
						theTok)) {
					if (theTok != Token.times
							&& !Token.tokAttr(theTok, Token.identifier))
						error(ERROR_invalidArgument);
					thisId = setShapeId(JmolConstants.SHAPE_CONTACT, i, idSeen);
					i = iToken;
					break;
				}
				if (iptDisplayProperty == 0)
					iptDisplayProperty = i;
				i = iToken;
				continue;
			case Token.id:
				okNoAtoms = true;
				setShapeId(JmolConstants.SHAPE_CONTACT, ++i, idSeen);
				isWild = (getShapeProperty(JmolConstants.SHAPE_CONTACT, "ID") == null);
				i = iToken;
				break;
			case Token.color:
				switch (tokAt(i + 1)) {
				case Token.density:
					tok = Token.nada;
					colorDensity = true;
					sbCommand.append(" color density");
					i++;
					break;
				case Token.type:
					tok = Token.nada;
					colorByType = true;
					sbCommand.append(" color type");
					i++;
					break;
				}
				if (tok == Token.nada)
					break;
				//$FALL-THROUGH$ to translucent
			case Token.translucent:
			case Token.opaque:
				okNoAtoms = true;
				if (colorpt == 0)
					colorpt = i;
				setMeshDisplayProperty(JmolConstants.SHAPE_CONTACT, i, theTok);
				i = iToken;
				break;
			case Token.slab:
				okNoAtoms = true;
				userSlabObject = getCapSlabObject(i, false);
				setShapeProperty(JmolConstants.SHAPE_CONTACT, "slab",
						userSlabObject);
				i = iToken;
				break;

			// now after this you need atoms

			case Token.density:
				colorDensity = true;
				sbCommand.append(" density");
				if (isFloatParameter(i + 1)) {
					if (params == null)
						params = new float[1];
					params[0] = -Math.abs(floatParameter(++i));
					sbCommand.append(" " + -params[0]);
				}
				break;
			case Token.resolution:
				float resolution = floatParameter(++i);
				if (resolution > 0) {
					sbCommand.append(" resolution ").append(resolution);
					setShapeProperty(JmolConstants.SHAPE_CONTACT, "resolution",
							Float.valueOf(resolution));
				}
				break;
			case Token.within:
			case Token.distance:
				distance = floatParameter(++i);
				sbCommand.append(" within ").append(distance);
				break;
			case Token.plus:
			case Token.integer:
			case Token.decimal:
				rd = encodeRadiusParameter(i, false, false);
				sbCommand.append(" ").append(rd);
				i = iToken;
				break;
			case Token.intermolecular:
			case Token.intramolecular:
				intramolecular = (tok == Token.intramolecular ? Boolean.TRUE
						: Boolean.FALSE);
				sbCommand.append(" ").append(theToken.value);
				break;
			case Token.minset:
				minSet = intParameter(++i);
				break;
			case Token.hbond:
			case Token.clash:
			case Token.vanderwaals:
				contactType = tok;
				sbCommand.append(" ").append(theToken.value);
				break;
			case Token.sasurface:
				if (isFloatParameter(i + 1))
					saProbeRadius = floatParameter(++i);
				//$FALL-THROUGH$
			case Token.cap:
			case Token.nci:
			case Token.surface:
				localOnly = false;
				//$FALL-THROUGH$
			case Token.trim:
			case Token.full:
			case Token.plane:
			case Token.connect:
				displayType = tok;
				sbCommand.append(" ").append(theToken.value);
				if (tok == Token.sasurface)
					sbCommand.append(" ").append(saProbeRadius);
				break;
			case Token.parameters:
				params = floatParameterSet(++i, 1, 10);
				i = iToken;
				break;
			case Token.bitset:
			case Token.expressionBegin:
				if (isWild || bsB != null)
					error(ERROR_invalidArgument);
				bs = BitSetUtil.copy(atomExpressionAt(i));
				i = iToken;
				if (bsA == null)
					bsA = bs;
				else
					bsB = bs;
				sbCommand.append(" ").append(Escape.escape(bs));
				break;
			}
			idSeen = (theTok != Token.delete);
		}
		if (!okNoAtoms && bsA == null)
			error(ERROR_endOfStatementUnexpected);
		if (isSyntaxCheck)
			return;

		if (bsA != null) {
			// bond mode, intramolec set here
			RadiusData rd1 = (rd == null ? new RadiusData(0.26f,
					EnumType.OFFSET, EnumVdw.AUTO) : rd);
			if (displayType == Token.nci && bsB == null
					&& intramolecular != null && intramolecular.booleanValue())
				bsB = bsA;
			else
				bsB = setContactBitSets(bsA, bsB, localOnly, distance, rd1,
						true);
			switch (displayType) {
			case Token.cap:
			case Token.sasurface:
				BitSet bsSolvent = lookupIdentifierValue("solvent");
				bsA.andNot(bsSolvent);
				bsB.andNot(bsSolvent);
				bsB.andNot(bsA);
				break;
			case Token.surface:
				bsB.andNot(bsA);
				break;
			case Token.nci:
				if (minSet == Integer.MAX_VALUE)
					minSet = 100;
				setShapeProperty(JmolConstants.SHAPE_CONTACT, "minset",
						Integer.valueOf(minSet));
				sbCommand.append(" minSet ").append(minSet);
				if (params == null)
					params = new float[] { 0.5f, 2 };
			}

			if (intramolecular != null) {
				params = (params == null ? new float[2] : ArrayUtil
						.ensureLength(params, 2));
				params[1] = (intramolecular.booleanValue() ? 1 : 2);
			}

			if (params != null)
				sbCommand.append(" parameters ").append(Escape.escape(params));

			// now adjust for type -- HBOND or HYDROPHOBIC or MISC
			// these are just "standard shortcuts" they are not necessary at all
			setShapeProperty(
					JmolConstants.SHAPE_CONTACT,
					"set",
					new Object[] { Integer.valueOf(contactType),
							Integer.valueOf(displayType),
							Boolean.valueOf(colorDensity),
							Boolean.valueOf(colorByType), bsA, bsB, rd,
							Float.valueOf(saProbeRadius), params,
							sbCommand.toString() });
			if (colorpt > 0)
				setMeshDisplayProperty(JmolConstants.SHAPE_CONTACT, colorpt, 0);
		}
		if (iptDisplayProperty > 0) {
			if (!setMeshDisplayProperty(JmolConstants.SHAPE_CONTACT,
					iptDisplayProperty, 0))
				error(ERROR_invalidArgument);
		}
		if (userSlabObject != null && bsA != null)
			setShapeProperty(JmolConstants.SHAPE_CONTACT, "slab",
					userSlabObject);
		if (bsA != null && (displayType == Token.nci || localOnly)) {
			Object volume = getShapeProperty(JmolConstants.SHAPE_CONTACT,
					"volume");
			if (volume instanceof double[]) {
				double[] vs = (double[]) volume;
				double v = 0;
				for (int i = 0; i < vs.length; i++)
					v += Math.abs(vs[i]);
				volume = Float.valueOf((float) v);
			}
			int nsets = ((Integer) getShapeProperty(
					JmolConstants.SHAPE_CONTACT, "nSets")).intValue();

			if (colorDensity || displayType != Token.trim) {
				showString((nsets == 0 ? "" : nsets + " contacts with ")
						+ "net volume " + volume + " A^3");
			}
		}
	}

	BitSet setContactBitSets(BitSet bsA, BitSet bsB, boolean localOnly,
			float distance, RadiusData rd, boolean warnMultiModel) {
		boolean withinAllModels;
		BitSet bs;
		if (bsB == null) {
			// default is within just one model when {B} is missing
			bsB = BitSetUtil.setAll(viewer.getAtomCount());
			BitSetUtil.andNot(bsB, viewer.getDeletedAtoms());
			bsB.andNot(bsA);
			withinAllModels = false;
		} else {
			// two atom sets specified; within ALL MODELS here
			bs = BitSetUtil.copy(bsA);
			bs.or(bsB);
			int nModels = viewer.getModelBitSet(bs, false).cardinality();
			withinAllModels = (nModels > 1);
			if (warnMultiModel && nModels > 1 && !tQuiet)
				showString(GT
						._("Note: More than one model is involved in this contact!"));
		}
		// B always within some possibly extended VDW of A or just A itself
		if (!bsA.equals(bsB)) {
			boolean setBfirst = (!localOnly || bsA.cardinality() < bsB
					.cardinality());
			if (setBfirst) {
				bs = viewer.getAtomsWithin(distance, bsA, withinAllModels,
						(Float.isNaN(distance) ? rd : null));
				bsB.and(bs);
			}
			if (localOnly) {
				// we can just get the near atoms for A as well.
				bs = viewer.getAtomsWithin(distance, bsB, withinAllModels,
						(Float.isNaN(distance) ? rd : null));
				bsA.and(bs);
				if (!setBfirst) {
					bs = viewer.getAtomsWithin(distance, bsA, withinAllModels,
							(Float.isNaN(distance) ? rd : null));
					bsB.and(bs);
				}
				// If the two sets are not the same,
				// we AND them and see if that is A.
				// If so, then the smaller set is
				// removed from the larger set.
				bs = BitSetUtil.copy(bsB);
				bs.and(bsA);
				if (bs.equals(bsA))
					bsB.andNot(bsA);
				else if (bs.equals(bsB))
					bsA.andNot(bsB);
			}
		}
		return bsB;
	}

	private void lcaoCartoon() throws ScriptException {
		shapeManager.loadShape(JmolConstants.SHAPE_LCAOCARTOON);
		if (tokAt(1) == Token.list
				&& listIsosurface(JmolConstants.SHAPE_LCAOCARTOON))
			return;
		setShapeProperty(JmolConstants.SHAPE_LCAOCARTOON, "init", fullCommand);
		if (statementLength == 1) {
			setShapeProperty(JmolConstants.SHAPE_LCAOCARTOON, "lcaoID", null);
			return;
		}
		boolean idSeen = false;
		String translucency = null;
		for (int i = 1; i < statementLength; i++) {
			String propertyName = null;
			Object propertyValue = null;
			switch (getToken(i).tok) {
			case Token.cap:
			case Token.slab:
				propertyName = (String) theToken.value;
				if (tokAt(i + 1) == Token.off)
					iToken = i + 1;
				propertyValue = getCapSlabObject(i, true);
				i = iToken;
				break;
			case Token.center:
				// serialized lcaoCartoon in isosurface format
				isosurface(JmolConstants.SHAPE_LCAOCARTOON);
				return;
			case Token.rotate:
				float degx = 0;
				float degy = 0;
				float degz = 0;
				switch (getToken(++i).tok) {
				case Token.x:
					degx = floatParameter(++i) * JmolConstants.radiansPerDegree;
					break;
				case Token.y:
					degy = floatParameter(++i) * JmolConstants.radiansPerDegree;
					break;
				case Token.z:
					degz = floatParameter(++i) * JmolConstants.radiansPerDegree;
					break;
				default:
					error(ERROR_invalidArgument);
				}
				propertyName = "rotationAxis";
				propertyValue = new Vector3f(degx, degy, degz);
				break;
			case Token.on:
			case Token.display:
			case Token.displayed:
				propertyName = "on";
				break;
			case Token.off:
			case Token.hide:
			case Token.hidden:
				propertyName = "off";
				break;
			case Token.delete:
				propertyName = "delete";
				break;
			case Token.bitset:
			case Token.expressionBegin:
				propertyName = "select";
				propertyValue = atomExpressionAt(i);
				i = iToken;
				break;
			case Token.color:
				translucency = setColorOptions(null, i + 1,
						JmolConstants.SHAPE_LCAOCARTOON, -2);
				if (translucency != null)
					setShapeProperty(JmolConstants.SHAPE_LCAOCARTOON,
							"settranslucency", translucency);
				i = iToken;
				idSeen = true;
				continue;
			case Token.translucent:
			case Token.opaque:
				setMeshDisplayProperty(JmolConstants.SHAPE_LCAOCARTOON, i,
						theTok);
				i = iToken;
				idSeen = true;
				continue;
			case Token.spacefill:
			case Token.string:
				propertyValue = parameterAsString(i).toLowerCase();
				if (propertyValue.equals("spacefill"))
					propertyValue = "cpk";
				propertyName = "create";
				if (optParameterAsString(i + 1).equalsIgnoreCase("molecular")) {
					i++;
					propertyName = "molecular";
				}
				break;
			case Token.select:
				if (tokAt(i + 1) == Token.bitset
						|| tokAt(i + 1) == Token.expressionBegin) {
					propertyName = "select";
					propertyValue = atomExpressionAt(i + 1);
					i = iToken;
				} else {
					propertyName = "selectType";
					propertyValue = parameterAsString(++i);
					if (propertyValue.equals("spacefill"))
						propertyValue = "cpk";
				}
				break;
			case Token.scale:
				propertyName = "scale";
				propertyValue = Float.valueOf(floatParameter(++i));
				break;
			case Token.lonepair:
			case Token.lp:
				propertyName = "lonePair";
				break;
			case Token.radical:
			case Token.rad:
				propertyName = "radical";
				break;
			case Token.molecular:
				propertyName = "molecular";
				break;
			case Token.create:
				propertyValue = parameterAsString(++i);
				propertyName = "create";
				if (optParameterAsString(i + 1).equalsIgnoreCase("molecular")) {
					i++;
					propertyName = "molecular";
				}
				break;
			case Token.id:
				propertyValue = getShapeNameParameter(++i);
				i = iToken;
				if (idSeen)
					error(ERROR_invalidArgument);
				propertyName = "lcaoID";
				break;
			default:
				if (theTok == Token.times
						|| Token.tokAttr(theTok, Token.identifier)) {
					if (theTok != Token.times)
						propertyValue = parameterAsString(i);
					if (idSeen)
						error(ERROR_invalidArgument);
					propertyName = "lcaoID";
					break;
				}
				break;
			}
			if (theTok != Token.delete)
				idSeen = true;
			if (propertyName == null)
				error(ERROR_invalidArgument);
			setShapeProperty(JmolConstants.SHAPE_LCAOCARTOON, propertyName,
					propertyValue);
		}
		setShapeProperty(JmolConstants.SHAPE_LCAOCARTOON, "clear", null);
	}

	private Object getCapSlabObject(int i, boolean isLcaoCartoon)
			throws ScriptException {
		if (i < 0) {
			// standard range -100 to 0
			return MeshSurface.getSlabWithinRange(i, 0);
		}
		Object data = null;
		int tok0 = tokAt(i);
		boolean isSlab = (tok0 == Token.slab);
		int tok = tokAt(i + 1);
		Point4f plane = null;
		Point3f[] pts = null;
		float d, d2;
		BitSet bs = null;
		Short slabColix = null;
		Integer slabMeshType = null;
		if (tok == Token.translucent) {
			float slabTranslucency = (isFloatParameter(++i + 1) ? floatParameter(++i)
					: 0.5f);
			if (isColorParam(i + 1)) {
				slabColix = Short.valueOf(Colix.getColixTranslucent(
						Colix.getColix(getArgbParam(i + 1)),
						slabTranslucency != 0, slabTranslucency));
				i = iToken;
			} else {
				slabColix = Short.valueOf(Colix.getColixTranslucent(
						Colix.INHERIT_COLOR, slabTranslucency != 0,
						slabTranslucency));
			}
			switch (tok = tokAt(i + 1)) {
			case Token.mesh:
			case Token.fill:
				slabMeshType = Integer.valueOf(tok);
				tok = tokAt(++i + 1);
				break;
			default:
				slabMeshType = Integer.valueOf(Token.fill);
				break;
			}
		}
		// TODO: check for compatibility with LCAOCARTOONS
		switch (tok) {
		case Token.bitset:
		case Token.expressionBegin:
			data = atomExpressionAt(i + 1);
			tok = Token.decimal;
			iToken++;
			break;
		case Token.off:
			iToken = i + 1;
			return new Integer(Integer.MIN_VALUE);
		case Token.none:
			iToken = i + 1;
			break;
		case Token.dollarsign:
			// do we need distance here? "-" here?
			i++;
			data = new Object[] { Float.valueOf(1), parameterAsString(++i) };
			tok = Token.mesh;
			break;
		case Token.within:
			// isosurface SLAB WITHIN RANGE f1 f2
			i++;
			if (tokAt(++i) == Token.range) {
				d = floatParameter(++i);
				d2 = floatParameter(++i);
				data = new Object[] { Float.valueOf(d), Float.valueOf(d2) };
				tok = Token.range;
			} else if (isFloatParameter(i)) {
				// isosurface SLAB WITHIN distance {atomExpression}|[point
				// array]
				d = floatParameter(i);
				if (isCenterParameter(++i)) {
					Point3f pt = centerParameter(i);
					if (isSyntaxCheck || !(expressionResult instanceof BitSet)) {
						pts = new Point3f[] { pt };
					} else {
						Atom[] atoms = viewer.getModelSet().atoms;
						bs = (BitSet) expressionResult;
						pts = new Point3f[bs.cardinality()];
						for (int k = 0, j = bs.nextSetBit(0); j >= 0; j = bs
								.nextSetBit(j + 1), k++)
							pts[k] = atoms[j];
					}
				} else {
					pts = getPointArray(i, -1);
				}
				if (pts.length == 0) {
					iToken = i;
					error(ERROR_invalidArgument);
				}
				data = new Object[] { Float.valueOf(d), pts, bs };
			} else {
				data = getPointArray(i, 4);
				tok = Token.boundbox;
			}
			break;
		case Token.boundbox:
			iToken = i + 1;
			data = BoxInfo
					.getCriticalPoints(viewer.getBoundBoxVertices(), null);
			break;
		// case Token.slicebox:
		// data =
		// BoxInfo.getCriticalPoints(((JmolViewer)(viewer)).slicer.getSliceVert(),
		// null);
		// iToken = i + 1;
		// break;
		case Token.brillouin:
		case Token.unitcell:
			iToken = i + 1;
			SymmetryInterface unitCell = viewer.getCurrentUnitCell();
			if (unitCell == null) {
				if (tok == Token.unitcell)
					error(ERROR_invalidArgument);
			} else {
				pts = BoxInfo.getCriticalPoints(unitCell.getUnitCellVertices(),
						unitCell.getCartesianOffset());
				int iType = (int) unitCell
						.getUnitCellInfo(SimpleUnitCell.INFO_DIMENSIONS);
				Vector3f v1 = null;
				Vector3f v2 = null;
				switch (iType) {
				case 3:
					break;
				case 1: // polymer
					v2 = new Vector3f(pts[2]);
					v2.sub(pts[0]);
					v2.scale(1000f);
					//$FALL-THROUGH$
				case 2: // slab
					// "a b c" is really "z y x"
					v1 = new Vector3f(pts[1]);
					v1.sub(pts[0]);
					v1.scale(1000f);
					pts[0].sub(v1);
					pts[1].scale(2000f);
					if (iType == 1) {
						pts[0].sub(v2);
						pts[2].scale(2000f);
					}
					break;
				}
				data = pts;
			}
			break;
		default:
			// isosurface SLAB n
			// isosurface SLAB -100. 0. as "within range"
			if (!isLcaoCartoon && isSlab && isFloatParameter(i + 1)) {
				d = floatParameter(++i);
				if (!isFloatParameter(i + 1))
					return new Integer((int) d);
				d2 = floatParameter(++i);
				data = new Object[] { Float.valueOf(d), Float.valueOf(d2) };
				tok = Token.range;
				break;
			}
			// isosurface SLAB [plane]
			plane = planeParameter(++i);
			float off = (isFloatParameter(iToken + 1) ? floatParameter(++iToken)
					: Float.NaN);
			if (!Float.isNaN(off))
				plane.w -= off;
			data = plane;
			tok = Token.plane;
		}
		Object colorData = (slabMeshType == null ? null : new Object[] {
				slabMeshType, slabColix });
		return MeshSurface.getSlabObject(tok, data, !isSlab, colorData);
	}

	private boolean mo(boolean isInitOnly) throws ScriptException {
		int offset = Integer.MAX_VALUE;
		boolean isNegOffset = false;
		BitSet bsModels = viewer.getVisibleFramesBitSet();
		List<Object[]> propertyList = new ArrayList<Object[]>();
		int i0 = 1;
		if (tokAt(1) == Token.model || tokAt(1) == Token.frame) {
			i0 = modelNumberParameter(2);
			if (i0 < 0)
				error(ERROR_invalidArgument);
			bsModels.clear();
			bsModels.set(i0);
			i0 = 3;
		}
		for (int iModel = bsModels.nextSetBit(0); iModel >= 0; iModel = bsModels
				.nextSetBit(iModel + 1)) {
			shapeManager.loadShape(JmolConstants.SHAPE_MO);
			int i = i0;
			if (tokAt(i) == Token.list
					&& listIsosurface(JmolConstants.SHAPE_MO))
				return true;
			setShapeProperty(JmolConstants.SHAPE_MO, "init",
					Integer.valueOf(iModel));
			String title = null;
			int moNumber = ((Integer) getShapeProperty(JmolConstants.SHAPE_MO,
					"moNumber")).intValue();
			float[] linearCombination = (float[]) getShapeProperty(
					JmolConstants.SHAPE_MO, "moLinearCombination");
			if (isInitOnly)
				return true;// (moNumber != 0);
			if (moNumber == 0)
				moNumber = Integer.MAX_VALUE;
			String propertyName = null;
			Object propertyValue = null;

			switch (getToken(i).tok) {
			case Token.cap:
			case Token.slab:
				propertyName = (String) theToken.value;
				propertyValue = getCapSlabObject(i, false);
				i = iToken;
				break;
			case Token.integer:
				moNumber = intParameter(i);
				linearCombination = (moNumber >= 0 ? null : new float[] { -100,
						-moNumber });
				break;
			case Token.minus:
				switch (tokAt(++i)) {
				case Token.homo:
				case Token.lumo:
					break;
				default:
					error(ERROR_invalidArgument);
				}
				isNegOffset = true;
				//$FALL-THROUGH$
			case Token.homo:
			case Token.lumo:
				linearCombination = null;
				if ((offset = moOffset(i)) == Integer.MAX_VALUE)
					error(ERROR_invalidArgument);
				moNumber = 0;
				break;
			case Token.next:
				linearCombination = null;
				moNumber = Token.next;
				break;
			case Token.prev:
				linearCombination = null;
				moNumber = Token.prev;
				break;
			case Token.color:
				setColorOptions(null, i + 1, JmolConstants.SHAPE_MO, 2);
				break;
			case Token.plane:
				// plane {X, Y, Z, W}
				propertyName = "plane";
				propertyValue = planeParameter(i + 1);
				break;
			case Token.point:
				addShapeProperty(
						propertyList,
						"randomSeed",
						tokAt(i + 2) == Token.integer ? Integer
								.valueOf(intParameter(i + 2)) : null);
				propertyName = "monteCarloCount";
				propertyValue = Integer.valueOf(intParameter(i + 1));
				break;
			case Token.scale:
				propertyName = "scale";
				propertyValue = Float.valueOf(floatParameter(i + 1));
				break;
			case Token.cutoff:
				if (tokAt(i + 1) == Token.plus) {
					propertyName = "cutoffPositive";
					propertyValue = Float.valueOf(floatParameter(i + 2));
				} else {
					propertyName = "cutoff";
					propertyValue = Float.valueOf(floatParameter(i + 1));
				}
				break;
			case Token.debug:
				propertyName = "debug";
				break;
			case Token.noplane:
				propertyName = "plane";
				break;
			case Token.pointsperangstrom:
			case Token.resolution:
				propertyName = "resolution";
				propertyValue = Float.valueOf(floatParameter(i + 1));
				break;
			case Token.squared:
				propertyName = "squareData";
				propertyValue = Boolean.TRUE;
				break;
			case Token.titleformat:
				if (i + 1 < statementLength && tokAt(i + 1) == Token.string) {
					propertyName = "titleFormat";
					propertyValue = parameterAsString(i + 1);
				}
				break;
			case Token.identifier:
				error(ERROR_invalidArgument);
				break;
			default:
				if (isArrayParameter(i)) {
					linearCombination = floatParameterSet(i, 2,
							Integer.MAX_VALUE);
					break;
				}
				int ipt = iToken;
				if (!setMeshDisplayProperty(JmolConstants.SHAPE_MO, 0, theTok))
					error(ERROR_invalidArgument);
				setShapeProperty(JmolConstants.SHAPE_MO, "setProperties",
						propertyList);
				setMeshDisplayProperty(JmolConstants.SHAPE_MO, ipt, tokAt(ipt));
				return true;
			}
			if (propertyName != null)
				addShapeProperty(propertyList, propertyName, propertyValue);
			if (moNumber != Integer.MAX_VALUE || linearCombination != null) {
				if (tokAt(i + 1) == Token.string)
					title = parameterAsString(i + 1);
				setCursorWait(true);
				setMoData(propertyList, moNumber, linearCombination, offset,
						isNegOffset, iModel, title);
				addShapeProperty(propertyList, "finalize", null);
			}
			if (propertyList.size() > 0)
				setShapeProperty(JmolConstants.SHAPE_MO, "setProperties",
						propertyList);
			propertyList.clear();
		}
		return true;
	}

	private String setColorOptions(StringBuffer sb, int index, int iShape,
			int nAllowed) throws ScriptException {
		getToken(index);
		String translucency = "opaque";
		if (theTok == Token.translucent) {
			translucency = "translucent";
			if (nAllowed < 0) {
				float value = (isFloatParameter(index + 1) ? floatParameter(++index)
						: Float.MAX_VALUE);
				setShapeTranslucency(iShape, null, "translucent", value, null);
				if (sb != null) {
					sb.append(" translucent");
					if (value != Float.MAX_VALUE)
						sb.append(" ").append(value);
				}
			} else {
				setMeshDisplayProperty(iShape, index, theTok);
			}
		} else if (theTok == Token.opaque) {
			if (nAllowed >= 0)
				setMeshDisplayProperty(iShape, index, theTok);
		} else {
			iToken--;
		}
		nAllowed = Math.abs(nAllowed);
		for (int i = 0; i < nAllowed; i++) {
			if (isColorParam(iToken + 1)) {
				int color = getArgbParam(++iToken);
				setShapeProperty(iShape, "colorRGB", Integer.valueOf(color));
				if (sb != null)
					sb.append(" ").append(Escape.escapeColor(color));
			} else if (iToken < index) {
				error(ERROR_invalidArgument);
			} else {
				break;
			}
		}
		return translucency;
	}

	private int moOffset(int index) throws ScriptException {
		boolean isHomo = (getToken(index).tok == Token.homo);
		int offset = (isHomo ? 0 : 1);
		int tok = tokAt(++index);
		if (tok == Token.integer && intParameter(index) < 0)
			offset += intParameter(index);
		else if (tok == Token.plus)
			offset += intParameter(++index);
		else if (tok == Token.minus)
			offset -= intParameter(++index);
		return offset;
	}

	@SuppressWarnings("unchecked")
	private void setMoData(List<Object[]> propertyList, int moNumber,
			float[] linearCombination, int offset, boolean isNegOffset,
			int modelIndex, String title) throws ScriptException {
		if (isSyntaxCheck)
			return;
		if (modelIndex < 0) {
			modelIndex = viewer.getCurrentModelIndex();
			if (modelIndex < 0)
				error(ERROR_multipleModelsDisplayedNotOK, "MO isosurfaces");
		}
		int firstMoNumber = moNumber;
		Map moData = (Map) viewer.getModelAuxiliaryInfo(modelIndex, "moData");
		if (linearCombination == null) {
			if (moData == null)
				error(ERROR_moModelError);
			int lastMoNumber = (moData.containsKey("lastMoNumber") ? ((Integer) moData
					.get("lastMoNumber")).intValue() : 0);
			if (moNumber == Token.prev)
				moNumber = lastMoNumber - 1;
			else if (moNumber == Token.next)
				moNumber = lastMoNumber + 1;
			List<Map<String, Object>> mos = (List<Map<String, Object>>) (moData
					.get("mos"));
			int nOrb = (mos == null ? 0 : mos.size());
			if (nOrb == 0)
				error(ERROR_moCoefficients);
			if (nOrb == 1 && moNumber > 1)
				error(ERROR_moOnlyOne);
			if (offset != Integer.MAX_VALUE) {
				// 0: HOMO;
				if (moData.containsKey("HOMO")) {
					moNumber = ((Integer) moData.get("HOMO")).intValue()
							+ offset;
				} else {
					moNumber = -1;
					Float f;
					for (int i = 0; i < nOrb; i++) {
						Map<String, Object> mo = mos.get(i);
						if ((f = (Float) mo.get("occupancy")) != null) {
							if (f.floatValue() < 0.5f) {
								// go for LUMO = first unoccupied
								moNumber = i;
								break;
							}
							continue;
						} else if ((f = (Float) mo.get("energy")) != null) {
							if (f.floatValue() > 0) {
								// go for LUMO = first positive
								moNumber = i;
								break;
							}
							continue;
						}
						break;
					}
					if (moNumber < 0)
						error(ERROR_moOccupancy);
					moNumber += offset;
				}
				Logger.info("MO " + moNumber);
			}
			if (moNumber < 1 || moNumber > nOrb)
				error(ERROR_moIndex, "" + nOrb);
		}
		moData.put("lastMoNumber", Integer.valueOf(moNumber));
		if (isNegOffset)
			linearCombination = new float[] { -100, moNumber };
		addShapeProperty(propertyList, "moData", moData);
		if (title != null)
			addShapeProperty(propertyList, "title", title);
		if (firstMoNumber < 0)
			addShapeProperty(propertyList, "charges", viewer.getAtomicCharges());
		addShapeProperty(
				propertyList,
				"molecularOrbital",
				linearCombination != null ? linearCombination : Integer
						.valueOf(firstMoNumber < 0 ? -moNumber : moNumber));
		addShapeProperty(propertyList, "clear", null);
	}

	private String initIsosurface(int iShape) throws ScriptException {

		// handle isosurface/mo/pmesh delete and id delete here

		setShapeProperty(iShape, "init", fullCommand);
		iToken = 0;
		int tok1 = tokAt(1);
		int tok2 = tokAt(2);
		if (tok1 == Token.delete || tok2 == Token.delete
				&& tokAt(++iToken) == Token.all) {
			setShapeProperty(iShape, "delete", null);
			iToken += 2;
			if (statementLength > iToken) {
				setShapeProperty(iShape, "init", fullCommand);
				setShapeProperty(iShape, "thisID",
						MeshCollection.PREVIOUS_MESH_ID);
			}
			return null;
		}
		iToken = 1;
		if (!setMeshDisplayProperty(iShape, 0, tok1)) {
			setShapeProperty(iShape, "thisID", MeshCollection.PREVIOUS_MESH_ID);
			if (iShape != JmolConstants.SHAPE_DRAW)
				setShapeProperty(iShape, "title", new String[] { thisCommand });
			if (tok1 != Token.id
					&& (tok2 == Token.times || tok1 == Token.times
							&& setMeshDisplayProperty(iShape, 0, tok2))) {
				String id = setShapeId(iShape, 1, false);
				iToken++;
				return id;
			}
		}
		return null;
	}

	private String getNextComment() {

		String nextCommand = getCommand(pc + 1, false, true);
		return (nextCommand.startsWith("#") ? nextCommand : "");
	}

	private boolean listIsosurface(int iShape) throws ScriptException {
		checkLength23();
		if (!isSyntaxCheck)
			showString((String) getShapeProperty(iShape, "list"
					+ (tokAt(2) == Token.nada ? "" : " " + getToken(2).value)));
		return true;
	}

	private void isosurface(int iShape) throws ScriptException {
		// also called by lcaoCartoon
		shapeManager.loadShape(iShape);
		if (tokAt(1) == Token.list && listIsosurface(iShape))
			return;
		int iptDisplayProperty = 0;
		boolean isIsosurface = (iShape == JmolConstants.SHAPE_ISOSURFACE);
		boolean isPmesh = (iShape == JmolConstants.SHAPE_PMESH);
		boolean isPlot3d = (iShape == JmolConstants.SHAPE_PLOT3D);
		boolean isLcaoCartoon = (iShape == JmolConstants.SHAPE_LCAOCARTOON);
		boolean surfaceObjectSeen = false;
		boolean planeSeen = false;
		boolean isMapped = false;
		boolean isBicolor = false;
		boolean isPhased = false;
		boolean doCalcArea = false;
		boolean doCalcVolume = false;
		boolean isCavity = false;
		boolean haveRadius = false;
		boolean toCache = false;
		boolean isFxy = false;
		boolean haveSlab = false;
		boolean haveIntersection = false;
		float[] data = null;
		String cmd = null;
		int thisSetNumber = -1;
		int nFiles = 0;
		int nX, nY, nZ, ptX, ptY;
		float sigma = Float.NaN;
		float cutoff = Float.NaN;
		int ptWithin = 0;
		Boolean smoothing = null;
		int smoothingPower = Integer.MAX_VALUE;
		BitSet bs = null;
		BitSet bsSelect = null;
		BitSet bsIgnore = null;
		StringBuffer sbCommand = new StringBuffer();
		Point3f pt;
		Point4f plane = null;
		Point3f lattice = null;
		Point3f[] pts;
		String str = null;
		int modelIndex = (isSyntaxCheck ? 0 : Integer.MIN_VALUE);
		setCursorWait(true);
		boolean idSeen = (initIsosurface(iShape) != null);
		boolean isWild = (idSeen && getShapeProperty(iShape, "ID") == null);
		boolean isColorSchemeTranslucent = false;
		boolean isInline;
		Object onlyOneModel = null;
		String translucency = null;
		String colorScheme = null;
		String mepOrMlp = null;
		short[] discreteColixes = null;
		List<Object[]> propertyList = new ArrayList<Object[]>();
		boolean defaultMesh = false;
		if (isPmesh || isPlot3d)
			addShapeProperty(propertyList, "fileType", "Pmesh");
		for (int i = iToken; i < statementLength; ++i) {
			String propertyName = null;
			Object propertyValue = null;
			getToken(i);
			if (theTok == Token.identifier)
				str = parameterAsString(i);
			switch (theTok) {
			// settings only
			case Token.isosurfacepropertysmoothing:
				smoothing = (getToken(++i).tok == Token.on ? Boolean.TRUE
						: theTok == Token.off ? Boolean.FALSE : null);
				if (smoothing == null)
					error(ERROR_invalidArgument);
				continue;
			case Token.isosurfacepropertysmoothingpower:
				smoothingPower = intParameter(++i);
				continue;
				// offset, rotate, and scale3d don't need to be saved in
				// sbCommand
				// because they are display properties
			case Token.move: // Jmol 13.0.RC2 -- required for state saving after
								// coordinate-based translate/rotate
				propertyName = "moveIsosurface";
				if (tokAt(++i) != Token.matrix4f)
					error(ERROR_invalidArgument);
				propertyValue = getToken(i++).value;
				break;
			case Token.offset:
				propertyName = "offset";
				propertyValue = centerParameter(++i);
				i = iToken;
				break;
			case Token.rotate:
				propertyName = "rotate";
				propertyValue = (tokAt(iToken = ++i) == Token.none ? null
						: getPoint4f(i));
				i = iToken;
				break;
			case Token.scale3d:
				propertyName = "scale3d";
				propertyValue = Float.valueOf(floatParameter(++i));
				break;
			case Token.period:
				sbCommand.append(" periodic");
				propertyName = "periodic";
				break;
			case Token.origin:
			case Token.step:
			case Token.point:
				propertyName = theToken.value.toString();
				sbCommand.append(" ").append(theToken.value);
				propertyValue = centerParameter(++i);
				sbCommand.append(" ").append(Escape.escape(propertyValue));
				i = iToken;
				break;
			case Token.boundbox:
				if (fullCommand.indexOf("# BBOX=") >= 0) {
					String[] bbox = TextFormat.split(
							Parser.getQuotedAttribute(fullCommand, "# BBOX"),
							',');
					pts = new Point3f[] {
							(Point3f) Escape.unescapePoint(bbox[0]),
							(Point3f) Escape.unescapePoint(bbox[1]) };
				} else if (isCenterParameter(i + 1)) {
					pts = new Point3f[] { getPoint3f(i + 1, true),
							getPoint3f(iToken + 1, true) };
					i = iToken;
				} else {
					pts = viewer.getBoundBoxVertices();
				}
				sbCommand.append(" boundBox " + Escape.escapePt(pts[0]) + " "
						+ Escape.escapePt(pts[pts.length - 1]));
				propertyName = "boundingBox";
				propertyValue = pts;
				break;
			case Token.pmesh:
				isPmesh = true;
				sbCommand.append(" pmesh");
				propertyName = "fileType";
				propertyValue = "Pmesh";
				break;
			case Token.intersection:
				// isosurface intersection {A} {B} VDW....
				// isosurface intersection {A} {B} function "a-b" VDW....
				bsSelect = atomExpressionAt(++i);
				if (isSyntaxCheck) {
					bs = new BitSet();
				} else if (tokAt(iToken + 1) == Token.expressionBegin
						|| tokAt(iToken + 1) == Token.bitset) {
					bs = atomExpressionAt(++iToken);
					bs.and(viewer.getAtomsWithin(5.0f, bsSelect, false, null));
				} else {
					// default is
					// "within(5.0, selected) and not within(molecule,selected)"
					bs = viewer.getAtomsWithin(5.0f, bsSelect, true, null);
					bs.andNot(viewer.getAtomBits(Token.molecule, bsSelect));
				}
				bs.andNot(bsSelect);
				sbCommand.append(" intersection ")
						.append(Escape.escape(bsSelect)).append(" ")
						.append(Escape.escape(bs));
				i = iToken;
				if (tokAt(i + 1) == Token.function) {
					i++;
					String f = (String) getToken(++i).value;
					sbCommand.append(" function ").append(Escape.escapeStr(f));
					if (!isSyntaxCheck)
						addShapeProperty(propertyList, "func",
								(f.equals("a+b") || f.equals("a-b") ? f
										: createFunction("__iso__", "a,b", f)));
				} else {
					haveIntersection = true;
				}
				propertyName = "intersection";
				propertyValue = new BitSet[] { bsSelect, bs };
				break;
			case Token.display:
			case Token.within:
				boolean isDisplay = (theTok == Token.display);
				if (isDisplay) {
					sbCommand.append(" display");
					iptDisplayProperty = i;
					int tok = tokAt(i + 1);
					if (tok == Token.nada)
						continue;
					i++;
					addShapeProperty(propertyList, "token",
							Integer.valueOf(Token.on));
					if (tok == Token.bitset || tok == Token.all) {
						propertyName = "bsDisplay";
						if (tok == Token.all) {
							sbCommand.append(" all");
						} else {
							propertyValue = statement[i].value;
							sbCommand.append(" ").append(
									Escape.escape(propertyValue));
						}
						checkLast(i);
						break;
					} else if (tok != Token.within) {
						iToken = i;
						error(ERROR_invalidArgument);
					}
				} else {
					ptWithin = i;
				}
				float distance;
				Point3f ptc = null;
				bs = null;
				boolean havePt = false;
				if (tokAt(i + 1) == Token.expressionBegin) {
					// within ( x.x , .... )
					distance = floatParameter(i + 3);
					if (isPoint3f(i + 4)) {
						ptc = centerParameter(i + 4);
						havePt = true;
						iToken = iToken + 2;
					} else if (isPoint3f(i + 5)) {
						ptc = centerParameter(i + 5);
						havePt = true;
						iToken = iToken + 2;
					} else {
						bs = atomExpression(statement, i + 5, statementLength,
								true, false, false, true);
						if (bs == null)
							error(ERROR_invalidArgument);
					}
				} else {
					distance = floatParameter(++i);
					ptc = centerParameter(++i);
				}
				if (isDisplay)
					checkLast(iToken);
				i = iToken;
				if (fullCommand.indexOf("# WITHIN=") >= 0)
					bs = Escape.unescapeBitset(Parser.getQuotedAttribute(
							fullCommand, "# WITHIN"));
				else if (!havePt)
					bs = (expressionResult instanceof BitSet ? (BitSet) expressionResult
							: null);
				if (!isSyntaxCheck) {
					if (bs != null && modelIndex >= 0) {
						bs.and(viewer.getModelUndeletedAtomsBitSet(modelIndex));
					}
					if (ptc == null)
						ptc = viewer.getAtomSetCenter(bs);

					getWithinDistanceVector(propertyList, distance, ptc, bs,
							isDisplay);
					sbCommand
							.append(" within ")
							.append(distance)
							.append(" ")
							.append(bs == null ? Escape.escapePt(ptc) : Escape
									.escape(bs));
				}
				continue;
			case Token.parameters:
				propertyName = "parameters";
				// if > 1 parameter, then first is assumed to be the cutoff.
				float[] fparams = floatParameterSet(++i, 1, 10);
				i = iToken;
				propertyValue = fparams;
				sbCommand.append(" parameters ").append(Escape.escape(fparams));
				break;
			case Token.property:
			case Token.variable:
				onlyOneModel = theToken.value;
				boolean isVariable = (theTok == Token.variable);
				if (mepOrMlp == null) { // not mlp or mep
					if (!surfaceObjectSeen && !isMapped && !planeSeen) {
						addShapeProperty(propertyList, "sasurface",
								Float.valueOf(0));
						// if (surfaceObjectSeen)
						sbCommand.append(" vdw");
						surfaceObjectSeen = true;
					}
					propertyName = "property";
					if (smoothing == null)
						smoothing = viewer
								.getIsosurfacePropertySmoothing(false) == 1 ? Boolean.TRUE
								: Boolean.FALSE;
					addShapeProperty(propertyList, "propertySmoothing",
							smoothing);
					sbCommand.append(" isosurfacePropertySmoothing "
							+ smoothing);
					if (smoothingPower == Integer.MAX_VALUE)
						smoothingPower = viewer
								.getIsosurfacePropertySmoothing(true);
					addShapeProperty(propertyList, "propertySmoothingPower",
							Integer.valueOf(smoothingPower));
					if (smoothing == Boolean.TRUE)
						sbCommand.append(" isosurfacePropertySmoothingPower "
								+ smoothingPower);
					if (viewer.isRangeSelected())
						addShapeProperty(propertyList, "rangeSelected",
								Boolean.TRUE);
				} else {
					propertyName = mepOrMlp;
				}
				str = parameterAsString(i);
				// if (surfaceObjectSeen)
				sbCommand.append(" ").append(str);

				if (str.toLowerCase().indexOf("property_") == 0) {
					data = new float[viewer.getAtomCount()];
					if (isSyntaxCheck)
						continue;
					data = viewer.getDataFloat(str);
					if (data == null)
						error(ERROR_invalidArgument);
					addShapeProperty(propertyList, propertyName, data);
					continue;
				}

				int atomCount = viewer.getAtomCount();
				data = new float[atomCount];

				if (isVariable) {
					String vname = parameterAsString(++i);
					if (vname.length() == 0) {
						data = floatParameterSet(i, atomCount, atomCount);
					} else {
						data = new float[atomCount];
						if (!isSyntaxCheck)
							Parser.parseStringInfestedFloatArray(""
									+ getParameter(vname, Token.string), null,
									data);
					}
					if (!isSyntaxCheck/* && (surfaceObjectSeen) */)
						sbCommand.append(" \"\" ").append(Escape.escape(data));
				} else {
					int tokProperty = getToken(++i).tok;
					if (!isSyntaxCheck) {
						sbCommand.append(" " + theToken.value);
						Atom[] atoms = viewer.getModelSet().atoms;
						viewer.autoCalculate(tokProperty);
						for (int iAtom = atomCount; --iAtom >= 0;)
							data[iAtom] = Atom.atomPropertyFloat(viewer,
									atoms[iAtom], tokProperty);
					}
					if (tokProperty == Token.color)
						colorScheme = "colorRGB";
					if (tokAt(i + 1) == Token.within) {
						float d = floatParameter(i = i + 2);
						sbCommand.append(" within " + d);
						addShapeProperty(propertyList, "propertyDistanceMax",
								Float.valueOf(d));
					}
				}
				propertyValue = data;
				break;
			case Token.model:
				if (surfaceObjectSeen)
					error(ERROR_invalidArgument);
				modelIndex = modelNumberParameter(++i);
				sbCommand.append(" model " + modelIndex);
				if (modelIndex < 0) {
					propertyName = "fixed";
					propertyValue = Boolean.TRUE;
					break;
				}
				propertyName = "modelIndex";
				propertyValue = Integer.valueOf(modelIndex);
				break;
			case Token.select:
				// in general, viewer.getCurrentSelection() is used, but we may
				// override that here. But we have to be careful that
				// we PREPEND the selection to the command if no surface object
				// has been seen yet, and APPEND it if it has.
				propertyName = "select";
				BitSet bs1 = atomExpressionAt(++i);
				propertyValue = bs1;
				i = iToken;
				boolean isOnly = (tokAt(i + 1) == Token.only);
				if (isOnly) {
					i++;
					BitSet bs2 = BitSetUtil.copy(bs1);
					BitSetUtil.invertInPlace(bs2, viewer.getAtomCount());
					addShapeProperty(propertyList, "ignore", bs2);
					sbCommand.append(" ignore ").append(Escape.escape(bs2));
				}
				if (surfaceObjectSeen || isMapped) {
					sbCommand.append(" select " + Escape.escape(propertyValue));
				} else {
					bsSelect = (BitSet) propertyValue;
					if (modelIndex < 0 && bsSelect.nextSetBit(0) >= 0)
						modelIndex = viewer.getAtomModelIndex(bsSelect
								.nextSetBit(0));
				}
				break;
			case Token.set:
				thisSetNumber = intParameter(++i);
				break;
			case Token.center:
				propertyName = "center";
				propertyValue = centerParameter(++i);
				sbCommand.append(" center " + Escape.escape(propertyValue));
				i = iToken;
				break;
			case Token.sign:
			case Token.color:
				int color;
				idSeen = true;
				boolean isSign = (theTok == Token.sign);
				if (isSign) {
					sbCommand.append(" sign");
					addShapeProperty(propertyList, "sign", Boolean.TRUE);
				} else {
					if (tokAt(i + 1) == Token.density) {
						i++;
						propertyName = "colorDensity";
						sbCommand.append(" color density");
						break;
					}
					/*
					 * "color" now is just used as an equivalent to "sign" and
					 * as an introduction to "absolute" any other use is
					 * superfluous; it has been replaced with MAP for indicating
					 * "use the current surface" because the term COLOR is too
					 * general.
					 */

					if (getToken(i + 1).tok == Token.string) {
						colorScheme = parameterAsString(++i);
						if (colorScheme.indexOf(" ") > 0) {
							discreteColixes = Colix.getColixArray(colorScheme);
							if (discreteColixes == null)
								error(ERROR_badRGBColor);
						}
					} else if (theTok == Token.mesh) {
						i++;
						sbCommand.append(" color mesh");
						color = getArgbParam(++i);
						addShapeProperty(propertyList, "meshcolor",
								Integer.valueOf(color));
						sbCommand.append(" ").append(Escape.escapeColor(color));
						i = iToken;
						continue;
					}
					if ((theTok = tokAt(i + 1)) == Token.translucent
							|| theTok == Token.opaque) {
						sbCommand.append(" color");
						translucency = setColorOptions(sbCommand, i + 1,
								JmolConstants.SHAPE_ISOSURFACE, -2);
						i = iToken;
						continue;
					}
					switch (tokAt(i + 1)) {
					case Token.absolute:
					case Token.range:
						getToken(++i);
						sbCommand.append(" color range");
						addShapeProperty(propertyList, "rangeAll", null);
						if (tokAt(i + 1) == Token.all) {
							i++;
							sbCommand.append(" all");
							continue;
						}
						float min = floatParameter(++i);
						float max = floatParameter(++i);
						addShapeProperty(propertyList, "red",
								Float.valueOf(min));
						addShapeProperty(propertyList, "blue",
								Float.valueOf(max));
						sbCommand.append(" ").append(min).append(" ")
								.append(max);
						continue;
					}
					if (isColorParam(i + 1)) {
						color = getArgbParam(i + 1);
						if (tokAt(i + 2) == Token.to) {
							colorScheme = getColorRange(i + 1);
							i = iToken;
							break;
						}
					}
					sbCommand.append(" color");
				}
				if (isColorParam(i + 1)) {
					color = getArgbParam(++i);
					sbCommand.append(" ").append(Escape.escapeColor(color));
					i = iToken;
					addShapeProperty(propertyList, "colorRGB",
							Integer.valueOf(color));
					idSeen = true;
					if (isColorParam(i + 1)) {
						color = getArgbParam(++i);
						i = iToken;
						addShapeProperty(propertyList, "colorRGB",
								Integer.valueOf(color));
						sbCommand.append(" ").append(Escape.escapeColor(color));
						isBicolor = true;
					} else if (isSign) {
						error(ERROR_invalidParameterOrder);
					}
				} else if (!isSign && discreteColixes == null) {
					error(ERROR_invalidParameterOrder);
				}
				continue;
			case Token.cache:
				if (!isIsosurface)
					error(ERROR_invalidArgument);
				toCache = !isSyntaxCheck;
				continue;
			case Token.file:
				if (tokAt(i + 1) != Token.string)
					error(ERROR_invalidParameterOrder);
				continue;
			case Token.ionic:
			case Token.vanderwaals:
				// if (surfaceObjectSeen)
				sbCommand.append(" ").append(theToken.value);
				RadiusData rd = encodeRadiusParameter(i, false, true);
				// if (surfaceObjectSeen)
				sbCommand.append(" ").append(rd);
				if (Float.isNaN(rd.value))
					rd.value = 100;
				propertyValue = rd;
				propertyName = "radius";
				haveRadius = true;
				if (isMapped)
					surfaceObjectSeen = false;
				i = iToken;
				break;
			case Token.plane:
				// plane {X, Y, Z, W}
				planeSeen = true;
				propertyName = "plane";
				propertyValue = planeParameter(++i);
				i = iToken;
				// if (surfaceObjectSeen)
				sbCommand.append(" plane ")
						.append(Escape.escape(propertyValue));
				break;
			case Token.scale:
				propertyName = "scale";
				propertyValue = Float.valueOf(floatParameter(++i));
				sbCommand.append(" scale ").append(propertyValue);
				break;
			case Token.all:
				if (idSeen)
					error(ERROR_invalidArgument);
				propertyName = "thisID";
				break;
			case Token.ellipsoid:
				// ellipsoid {xc yc zc f} where a = b and f = a/c
				// OR ellipsoid {u11 u22 u33 u12 u13 u23}
				surfaceObjectSeen = true;
				++i;
				try {
					propertyValue = getPoint4f(i);
					propertyName = "ellipsoid";
					i = iToken;
					sbCommand.append(" ellipsoid ").append(
							Escape.escape(propertyValue));
					break;
				} catch (ScriptException e) {
				}
				try {
					propertyName = "ellipsoid";
					propertyValue = floatParameterSet(i, 6, 6);
					i = iToken;
					sbCommand.append(" ellipsoid ").append(
							Escape.escape(propertyValue));
					break;
				} catch (ScriptException e) {
				}
				bs = atomExpressionAt(i);
				sbCommand.append(" ellipsoid ").append(Escape.escape(bs));
				int iAtom = bs.nextSetBit(0);
				Atom[] atoms = viewer.getModelSet().atoms;
				if (iAtom >= 0)
					propertyValue = atoms[iAtom].getEllipsoid();
				if (propertyValue == null)
					return;
				i = iToken;
				propertyName = "ellipsoid";
				if (!isSyntaxCheck)
					addShapeProperty(propertyList, "center",
							viewer.getAtomPoint3f(iAtom));
				break;
			case Token.hkl:
				// miller indices hkl
				planeSeen = true;
				propertyName = "plane";
				propertyValue = hklParameter(++i);
				i = iToken;
				sbCommand.append(" plane ")
						.append(Escape.escape(propertyValue));
				break;
			case Token.lcaocartoon:
				surfaceObjectSeen = true;
				String lcaoType = parameterAsString(++i);
				addShapeProperty(propertyList, "lcaoType", lcaoType);
				sbCommand.append(" lcaocartoon ").append(
						Escape.escapeStr(lcaoType));
				switch (getToken(++i).tok) {
				case Token.bitset:
				case Token.expressionBegin:
					// automatically selects just the model of the first atom in
					// the set.
					propertyName = "lcaoCartoon";
					bs = atomExpressionAt(i);
					i = iToken;
					if (isSyntaxCheck)
						continue;
					int atomIndex = bs.nextSetBit(0);
					if (atomIndex < 0)
						error(ERROR_expressionExpected);
					sbCommand.append(" ({").append(atomIndex).append("})");
					modelIndex = viewer.getAtomModelIndex(atomIndex);
					addShapeProperty(propertyList, "modelIndex",
							Integer.valueOf(modelIndex));
					Vector3f[] axes = { new Vector3f(), new Vector3f(),
							new Vector3f(viewer.getAtomPoint3f(atomIndex)),
							new Vector3f() };
					if (!lcaoType.equalsIgnoreCase("s")
							&& viewer.getHybridizationAndAxes(atomIndex,
									axes[0], axes[1], lcaoType) == null)
						return;
					propertyValue = axes;
					break;
				default:
					error(ERROR_expressionExpected);
				}
				break;
			case Token.mo:
				// mo 1-based-index
				int moNumber = Integer.MAX_VALUE;
				int offset = Integer.MAX_VALUE;
				boolean isNegOffset = (tokAt(i + 1) == Token.minus);
				if (isNegOffset)
					i++;
				float[] linearCombination = null;
				switch (tokAt(++i)) {
				case Token.nada:
					error(ERROR_badArgumentCount);
					break;
				case Token.homo:
				case Token.lumo:
					offset = moOffset(i);
					moNumber = 0;
					i = iToken;
					// if (surfaceObjectSeen) {
					sbCommand.append(" mo " + (isNegOffset ? "-" : "")
							+ "HOMO ");
					if (offset > 0)
						sbCommand.append("+");
					if (offset != 0)
						sbCommand.append(offset);
					// }
					break;
				case Token.integer:
					moNumber = intParameter(i);
					// if (surfaceObjectSeen)
					sbCommand.append(" mo ").append(moNumber);
					break;
				default:
					if (isArrayParameter(i)) {
						linearCombination = floatParameterSet(i, 2,
								Integer.MAX_VALUE);
						i = iToken;
					}
				}
				if (tokAt(i + 1) == Token.point) {
					++i;
					int monteCarloCount = intParameter(++i);
					int seed = (tokAt(i + 1) == Token.integer ? intParameter(++i)
							: ((int) -System.currentTimeMillis()) % 10000);
					addShapeProperty(propertyList, "monteCarloCount",
							Integer.valueOf(monteCarloCount));
					addShapeProperty(propertyList, "randomSeed",
							Integer.valueOf(seed));
					sbCommand.append(" points ").append(monteCarloCount)
							.append(' ').append(seed);
				}
				setMoData(propertyList, moNumber, linearCombination, offset,
						isNegOffset, modelIndex, null);
				surfaceObjectSeen = true;
				continue;
			case Token.nci:
				propertyName = "nci";
				// if (surfaceObjectSeen)
				sbCommand.append(" " + propertyName);
				int tok = tokAt(i + 1);
				boolean isPromolecular = (tok != Token.file
						&& tok != Token.string && tok != Token.mrc);
				propertyValue = Boolean.valueOf(isPromolecular);
				if (isPromolecular)
					surfaceObjectSeen = true;
				break;
			case Token.mep:
			case Token.mlp:
				boolean isMep = (theTok == Token.mep);
				propertyName = (isMep ? "mep" : "mlp");
				// if (surfaceObjectSeen)
				sbCommand.append(" " + propertyName);
				String fname = null;
				int calcType = -1;
				surfaceObjectSeen = true;
				if (tokAt(i + 1) == Token.integer) {
					calcType = intParameter(++i);
					sbCommand.append(" " + calcType);
					addShapeProperty(propertyList, "mepCalcType",
							Integer.valueOf(calcType));
				}
				if (tokAt(i + 1) == Token.string) {
					fname = stringParameter(++i);
					// if (surfaceObjectSeen)
					sbCommand.append(" /*file*/" + Escape.escapeStr(fname));
				} else if (tokAt(i + 1) == Token.property) {
					mepOrMlp = propertyName;
					continue;
				}
				if (!isSyntaxCheck)
					try {
						data = (fname == null && isMep ? viewer
								.getPartialCharges() : viewer
								.getAtomicPotentials(isMep, bsSelect, bsIgnore,
										fname));
					} catch (Exception e) {
						// ignore
					}
				if (!isSyntaxCheck && data == null)
					error(ERROR_noPartialCharges);
				propertyValue = data;
				break;
			case Token.volume:
				doCalcVolume = !isSyntaxCheck;
				sbCommand.append(" volume");
				break;
			case Token.id:
				setShapeId(iShape, ++i, idSeen);
				isWild = (getShapeProperty(iShape, "ID") == null);
				i = iToken;
				break;
			case Token.colorscheme:
				// either order NOT OK -- documented for TRANSLUCENT "rwb"
				if (tokAt(i + 1) == Token.translucent) {
					isColorSchemeTranslucent = true;
					i++;
				}
				colorScheme = parameterAsString(++i).toLowerCase();
				if (colorScheme.equals("sets")) {
					sbCommand.append(" colorScheme \"sets\"");
				} else if (isColorParam(i)) {
					colorScheme = getColorRange(i);
					i = iToken;
				}
				break;
			case Token.addhydrogens:
				propertyName = "addHydrogens";
				propertyValue = Boolean.TRUE;
				sbCommand.append(" addHydrogens");
				break;
			case Token.angstroms:
				propertyName = "angstroms";
				sbCommand.append(" angstroms");
				break;
			case Token.anisotropy:
				propertyName = "anisotropy";
				propertyValue = getPoint3f(++i, false);
				sbCommand.append(" anisotropy").append(
						Escape.escapePt((Point3f) propertyValue));
				i = iToken;
				break;
			case Token.area:
				doCalcArea = !isSyntaxCheck;
				sbCommand.append(" area");
				break;
			case Token.atomicorbital:
			case Token.orbital:
				surfaceObjectSeen = true;
				if (isBicolor && !isPhased) {
					sbCommand.append(" phase \"_orb\"");
					addShapeProperty(propertyList, "phase", "_orb");
				}
				float[] nlmZprs = new float[7];
				nlmZprs[0] = intParameter(++i);
				nlmZprs[1] = intParameter(++i);
				nlmZprs[2] = intParameter(++i);
				nlmZprs[3] = (isFloatParameter(i + 1) ? floatParameter(++i)
						: 6f);
				// if (surfaceObjectSeen)
				sbCommand.append(" atomicOrbital ").append((int) nlmZprs[0])
						.append(" ").append((int) nlmZprs[1]).append(" ")
						.append((int) nlmZprs[2]).append(" ")
						.append(nlmZprs[3]);
				if (tokAt(i + 1) == Token.point) {
					i += 2;
					nlmZprs[4] = intParameter(i);
					nlmZprs[5] = (tokAt(i + 1) == Token.decimal ? floatParameter(++i)
							: 0);
					nlmZprs[6] = (tokAt(i + 1) == Token.integer ? intParameter(++i)
							: ((int) -System.currentTimeMillis()) % 10000);
					// if (surfaceObjectSeen)
					sbCommand.append(" points ").append((int) nlmZprs[4])
							.append(' ').append(nlmZprs[5]).append(' ')
							.append((int) nlmZprs[6]);
				}
				propertyName = "hydrogenOrbital";
				propertyValue = nlmZprs;
				break;
			case Token.binary:
				sbCommand.append(" binary");
				// for PMESH, specifically
				// ignore for now
				continue;
			case Token.blockdata:
				sbCommand.append(" blockData");
				propertyName = "blockData";
				propertyValue = Boolean.TRUE;
				break;
			case Token.cap:
			case Token.slab:
				haveSlab = true;
				propertyName = (String) theToken.value;
				propertyValue = getCapSlabObject(i, false);
				i = iToken;
				break;
			case Token.cavity:
				if (!isIsosurface)
					error(ERROR_invalidArgument);
				isCavity = true;
				if (isSyntaxCheck)
					continue;
				float cavityRadius = (isFloatParameter(i + 1) ? floatParameter(++i)
						: 1.2f);
				float envelopeRadius = (isFloatParameter(i + 1) ? floatParameter(++i)
						: 10f);
				if (envelopeRadius > 10f)
					integerOutOfRange(0, 10);
				sbCommand.append(" cavity ").append(cavityRadius).append(" ")
						.append(envelopeRadius);
				addShapeProperty(propertyList, "envelopeRadius",
						Float.valueOf(envelopeRadius));
				addShapeProperty(propertyList, "cavityRadius",
						Float.valueOf(cavityRadius));
				propertyName = "cavity";
				break;
			case Token.contour:
			case Token.contours:
				propertyName = "contour";
				sbCommand.append(" contour");
				switch (tokAt(i + 1)) {
				case Token.discrete:
					propertyValue = floatParameterSet(i + 2, 1,
							Integer.MAX_VALUE);
					sbCommand.append(" discrete ").append(
							Escape.escape(propertyValue));
					i = iToken;
					break;
				case Token.increment:
					pt = getPoint3f(i + 2, false);
					if (pt.z <= 0 || pt.y < pt.x)
						error(ERROR_invalidArgument); // from to step
					if (pt.z == (int) pt.z && pt.z > (pt.y - pt.x))
						pt.z = (pt.y - pt.x) / pt.z;
					propertyValue = pt;
					i = iToken;
					sbCommand.append(" increment ").append(Escape.escapePt(pt));
					break;
				default:
					propertyValue = Integer
							.valueOf(tokAt(i + 1) == Token.integer ? intParameter(++i)
									: 0);
					sbCommand.append(" ").append(propertyValue);
				}
				break;
			case Token.decimal:
			case Token.integer:
			case Token.plus:
			case Token.cutoff:
				sbCommand.append(" cutoff ");
				if (theTok == Token.cutoff)
					i++;
				if (tokAt(i) == Token.plus) {
					propertyName = "cutoffPositive";
					propertyValue = Float.valueOf(cutoff = floatParameter(++i));
					sbCommand.append("+").append(propertyValue);
				} else if (isFloatParameter(i)) {
					propertyName = "cutoff";
					propertyValue = Float.valueOf(cutoff = floatParameter(i));
					sbCommand.append(propertyValue);
				} else {
					propertyName = "cutoffRange";
					propertyValue = floatParameterSet(i, 2, 2);
					addShapeProperty(propertyList, "cutoff", Float.valueOf(0));
					sbCommand.append(Escape.escape(propertyValue));
					i = iToken;
				}
				break;
			case Token.downsample:
				propertyName = "downsample";
				propertyValue = Integer.valueOf(intParameter(++i));
				// if (surfaceObjectSeen)
				sbCommand.append(" downsample ").append(propertyValue);
				break;
			case Token.eccentricity:
				propertyName = "eccentricity";
				propertyValue = getPoint4f(++i);
				// if (surfaceObjectSeen)
				sbCommand.append(" eccentricity ").append(
						Escape.escape(propertyValue));
				i = iToken;
				break;
			case Token.ed:
				sbCommand.append(" ed");
				// electron density - never documented
				setMoData(propertyList, -1, null, 0, false, modelIndex, null);
				surfaceObjectSeen = true;
				continue;
			case Token.debug:
			case Token.nodebug:
				sbCommand.append(" ").append(theToken.value);
				propertyName = "debug";
				propertyValue = (theTok == Token.debug ? Boolean.TRUE
						: Boolean.FALSE);
				break;
			case Token.fixed:
				sbCommand.append(" fixed");
				propertyName = "fixed";
				propertyValue = Boolean.TRUE;
				break;
			case Token.fullplane:
				sbCommand.append(" fullPlane");
				propertyName = "fullPlane";
				propertyValue = Boolean.TRUE;
				break;
			case Token.functionxy:
			case Token.functionxyz:
				// isosurface functionXY "functionName"|"data2d_xxxxx"
				// isosurface functionXYZ "functionName"|"data3d_xxxxx"
				// {origin} {ni ix iy iz} {nj jx jy jz} {nk kx ky kz}
				// or
				// isosurface origin.. step... count... functionXY[Z] =
				// "x + y + z"
				boolean isFxyz = (theTok == Token.functionxyz);
				propertyName = "" + theToken.value;
				List<Object> vxy = new ArrayList<Object>();
				propertyValue = vxy;
				isFxy = surfaceObjectSeen = true;
				// if (surfaceObjectSeen)
				sbCommand.append(" ").append(propertyName);
				String name = parameterAsString(++i);
				if (name.equals("=")) {
					// if (surfaceObjectSeen)
					sbCommand.append(" =");
					name = parameterAsString(++i);
					// if (surfaceObjectSeen)
					sbCommand.append(" ").append(Escape.escapeStr(name));
					vxy.add(name);
					if (!isSyntaxCheck)
						addShapeProperty(propertyList, "func",
								createFunction("__iso__", "x,y,z", name));
					// surfaceObjectSeen = true;
					break;
				}
				// override of function or data name when saved as a state
				String dName = Parser.getQuotedAttribute(fullCommand, "# DATA"
						+ (isFxy ? "2" : ""));
				if (dName == null)
					dName = "inline";
				else
					name = dName;
				boolean isXYZ = (name.indexOf("data2d_") == 0);
				boolean isXYZV = (name.indexOf("data3d_") == 0);
				isInline = name.equals("inline");
				// if (!surfaceObjectSeen)
				sbCommand.append(" inline");
				vxy.add(name); // (0) = name
				Point3f pt3 = getPoint3f(++i, false);
				// if (!surfaceObjectSeen)
				sbCommand.append(" ").append(Escape.escapePt(pt3));
				vxy.add(pt3); // (1) = {origin}
				Point4f pt4;
				ptX = ++iToken;
				vxy.add(pt4 = getPoint4f(ptX)); // (2) = {ni ix iy iz}
				// if (!surfaceObjectSeen)
				sbCommand.append(" ").append(Escape.escape(pt4));
				nX = (int) pt4.x;
				ptY = ++iToken;
				vxy.add(pt4 = getPoint4f(ptY)); // (3) = {nj jx jy jz}
				// if (!surfaceObjectSeen)
				sbCommand.append(" ").append(Escape.escape(pt4));
				nY = (int) pt4.x;
				vxy.add(pt4 = getPoint4f(++iToken)); // (4) = {nk kx ky kz}
				// if (!surfaceObjectSeen)
				sbCommand.append(" ").append(Escape.escape(pt4));
				nZ = (int) pt4.x;

				if (nX == 0 || nY == 0 || nZ == 0)
					error(ERROR_invalidArgument);
				if (!isSyntaxCheck) {
					float[][] fdata = null;
					float[][][] xyzdata = null;
					if (isFxyz) {
						if (isInline) {
							nX = Math.abs(nX);
							nY = Math.abs(nY);
							nZ = Math.abs(nZ);
							xyzdata = floatArraySet(++iToken, nX, nY, nZ);
						} else if (isXYZV) {
							xyzdata = viewer.getDataFloat3D(name);
						} else {
							xyzdata = viewer.functionXYZ(name, nX, nY, nZ);
						}
						nX = Math.abs(nX);
						nY = Math.abs(nY);
						nZ = Math.abs(nZ);
						if (xyzdata == null) {
							iToken = ptX;
							error(ERROR_what, "xyzdata is null.");
						}
						if (xyzdata.length != nX || xyzdata[0].length != nY
								|| xyzdata[0][0].length != nZ) {
							iToken = ptX;
							error(ERROR_what, "xyzdata[" + xyzdata.length
									+ "][" + xyzdata[0].length + "]["
									+ xyzdata[0][0].length
									+ "] is not of size [" + nX + "][" + nY
									+ "][" + nZ + "]");
						}
						vxy.add(xyzdata); // (5) = float[][][] data
						// if (!surfaceObjectSeen)
						sbCommand.append(" ").append(Escape.escape(xyzdata));
					} else {
						if (isInline) {
							nX = Math.abs(nX);
							nY = Math.abs(nY);
							fdata = floatArraySet(++iToken, nX, nY);
						} else if (isXYZ) {
							fdata = viewer.getDataFloat2D(name);
							nX = (fdata == null ? 0 : fdata.length);
							nY = 3;
						} else {
							fdata = viewer.functionXY(name, nX, nY);
							nX = Math.abs(nX);
							nY = Math.abs(nY);
						}
						if (fdata == null) {
							iToken = ptX;
							error(ERROR_what, "fdata is null.");
						}
						if (fdata.length != nX && !isXYZ) {
							iToken = ptX;
							error(ERROR_what, "fdata length is not correct: "
									+ fdata.length + " " + nX + ".");
						}
						for (int j = 0; j < nX; j++) {
							if (fdata[j] == null) {
								iToken = ptY;
								error(ERROR_what, "fdata[" + j + "] is null.");
							}
							if (fdata[j].length != nY) {
								iToken = ptY;
								error(ERROR_what, "fdata[" + j
										+ "] is not the right length: "
										+ fdata[j].length + " " + nY + ".");
							}
						}
						vxy.add(fdata); // (5) = float[][] data
						// if (!surfaceObjectSeen)
						sbCommand.append(" ").append(Escape.escape(fdata));
					}
				}
				i = iToken;
				break;
			case Token.gridpoints:
				propertyName = "gridPoints";
				sbCommand.append(" gridPoints");
				break;
			case Token.ignore:
				propertyName = "ignore";
				propertyValue = bsIgnore = atomExpressionAt(++i);
				sbCommand.append(" ignore ").append(
						Escape.escape(propertyValue));
				i = iToken;
				break;
			case Token.insideout:
				propertyName = "insideOut";
				sbCommand.append(" insideout");
				break;
			case Token.internal:
			case Token.interior:
			case Token.pocket:
				// if (!surfaceObjectSeen)
				sbCommand.append(" ").append(theToken.value);
				propertyName = "pocket";
				propertyValue = (theTok == Token.pocket ? Boolean.TRUE
						: Boolean.FALSE);
				break;
			case Token.lobe:
				// lobe {eccentricity}
				propertyName = "lobe";
				propertyValue = getPoint4f(++i);
				i = iToken;
				// if (!surfaceObjectSeen)
				sbCommand.append(" lobe ").append(Escape.escape(propertyValue));
				surfaceObjectSeen = true;
				break;
			case Token.lonepair:
			case Token.lp:
				// lp {eccentricity}
				propertyName = "lp";
				propertyValue = getPoint4f(++i);
				i = iToken;
				// if (!surfaceObjectSeen)
				sbCommand.append(" lp ").append(Escape.escape(propertyValue));
				surfaceObjectSeen = true;
				break;
			case Token.mapProperty:
				if (isMapped || statementLength == i + 1)
					error(ERROR_invalidArgument);
				isMapped = true;
				if ((isCavity || haveRadius || haveIntersection)
						&& !surfaceObjectSeen) {
					surfaceObjectSeen = true;
					addShapeProperty(propertyList, "bsSolvent", (haveRadius
							|| haveIntersection ? new BitSet()
							: lookupIdentifierValue("solvent")));
					addShapeProperty(propertyList, "sasurface",
							Float.valueOf(0));
				}
				if (sbCommand.length() == 0) {
					plane = (Point4f) getShapeProperty(
							JmolConstants.SHAPE_ISOSURFACE, "plane");
					if (plane == null) {
						if (getShapeProperty(JmolConstants.SHAPE_ISOSURFACE,
								"contours") != null) {
							addShapeProperty(propertyList, "nocontour", null);
						}
					} else {
						addShapeProperty(propertyList, "plane", plane);
						sbCommand.append("plane ").append(Escape.escape(plane));
						planeSeen = true;
						plane = null;
					}
				} else if (!surfaceObjectSeen && !planeSeen) {
					error(ERROR_invalidArgument);
				}
				sbCommand.append("; isosurface map");
				addShapeProperty(propertyList, "map",
						(surfaceObjectSeen ? Boolean.TRUE : Boolean.FALSE));
				break;
			case Token.maxset:
				propertyName = "maxset";
				propertyValue = Integer.valueOf(intParameter(++i));
				sbCommand.append(" maxSet ").append(propertyValue);
				break;
			case Token.minset:
				propertyName = "minset";
				propertyValue = Integer.valueOf(intParameter(++i));
				sbCommand.append(" minSet ").append(propertyValue);
				break;
			case Token.radical:
				// rad {eccentricity}
				surfaceObjectSeen = true;
				propertyName = "rad";
				propertyValue = getPoint4f(++i);
				i = iToken;
				// if (!surfaceObjectSeen)
				sbCommand.append(" radical ").append(
						Escape.escape(propertyValue));
				break;
			case Token.modelbased:
				propertyName = "fixed";
				propertyValue = Boolean.FALSE;
				sbCommand.append(" modelBased");
				break;
			case Token.molecular:
			case Token.sasurface:
			case Token.solvent:
				onlyOneModel = theToken.value;
				float radius;
				if (theTok == Token.molecular) {
					propertyName = "molecular";
					// if (!surfaceObjectSeen)
					sbCommand.append(" molecular");
					radius = 1.4f;
				} else {
					addShapeProperty(propertyList, "bsSolvent",
							lookupIdentifierValue("solvent"));
					propertyName = (theTok == Token.sasurface ? "sasurface"
							: "solvent");
					// if (!surfaceObjectSeen)
					sbCommand.append(" ").append(theToken.value);
					radius = (isFloatParameter(i + 1) ? floatParameter(++i)
							: viewer.getSolventProbeRadius());
					// if (!surfaceObjectSeen)
					sbCommand.append(" ").append(radius);
				}
				propertyValue = Float.valueOf(radius);
				if (tokAt(i + 1) == Token.full) {
					addShapeProperty(propertyList, "doFullMolecular", null);
					// if (!surfaceObjectSeen)
					sbCommand.append(" full");
					i++;
				}
				surfaceObjectSeen = true;
				break;
			case Token.mrc:
				addShapeProperty(propertyList, "fileType", "MRC");
				// if (!surfaceObjectSeen)
				sbCommand.append(" mrc");
				continue;
			case Token.object:
			case Token.obj:
				addShapeProperty(propertyList, "fileType", "Obj");
				// if (!surfaceObjectSeen)
				sbCommand.append(" obj");
				continue;
			case Token.msms:
				addShapeProperty(propertyList, "fileType", "Msms");
				// if (!surfaceObjectSeen)
				sbCommand.append(" msms");
				continue;
			case Token.phase:
				if (surfaceObjectSeen)
					error(ERROR_invalidArgument);
				propertyName = "phase";
				isPhased = true;
				propertyValue = (tokAt(i + 1) == Token.string ? stringParameter(++i)
						: "_orb");
				sbCommand.append(" phase ")
						.append(Escape.escape(propertyValue));
				break;
			case Token.pointsperangstrom:
			case Token.resolution:
				propertyName = "resolution";
				propertyValue = Float.valueOf(floatParameter(++i));
				sbCommand.append(" resolution ").append(propertyValue);
				break;
			case Token.reversecolor:
				propertyName = "reverseColor";
				propertyValue = Boolean.TRUE;
				sbCommand.append(" reversecolor");
				break;
			case Token.sigma:
				propertyName = "sigma";
				propertyValue = Float.valueOf(sigma = floatParameter(++i));
				sbCommand.append(" sigma ").append(propertyValue);
				break;
			case Token.sphere:
				// sphere [radius]
				propertyName = "sphere";
				propertyValue = Float.valueOf(floatParameter(++i));
				// if (!surfaceObjectSeen)
				sbCommand.append(" sphere ").append(propertyValue);
				surfaceObjectSeen = true;
				break;
			case Token.squared:
				propertyName = "squareData";
				propertyValue = Boolean.TRUE;
				sbCommand.append(" squared");
				break;
			case Token.inline:
			case Token.string:
				String filename = parameterAsString(i);
				String sType = null;
				isInline = filename.equalsIgnoreCase("inline");
				if (tokAt(i + 1) == Token.string) {
					sType = stringParameter(++i);
					if (!isInline)
						addShapeProperty(propertyList, "calculationType", sType);
				}
				boolean firstPass = (!surfaceObjectSeen && !planeSeen);
				propertyName = (firstPass ? "readFile" : "mapColor");
				if (isInline) {
					if (sType == null)
						error(ERROR_invalidArgument);
					// inline PMESH data
					if (isPmesh)
						sType = TextFormat.replaceAllCharacters(sType, "{,}|",
								' ');
					if (logMessages)
						Logger.debug("pmesh inline data:\n" + sType);
					propertyValue = (isSyntaxCheck ? null : sType);
					addShapeProperty(propertyList, "fileName", "");
					// if (!surfaceObjectSeen)
					sbCommand.append(" INLINE");
					surfaceObjectSeen = true;
				} else {
					if (filename.startsWith("=") && filename.length() > 1) {
						// Uppsala Electron Density Server (default, at least)
						String[] info = (String[]) viewer.setLoadFormat(
								filename, '_', false);
						filename = info[0];
						String strCutoff = (!firstPass || !Float.isNaN(cutoff) ? null
								: info[1]);
						if (strCutoff != null && !isSyntaxCheck) {
							cutoff = ScriptVariable.fValue(ScriptVariable
									.getVariable(viewer
											.evaluateExpression(strCutoff)));
							if (cutoff > 0) {
								if (!Float.isNaN(sigma)) {
									cutoff *= sigma;
									sigma = Float.NaN;
									addShapeProperty(propertyList, "sigma",
											Float.valueOf(sigma));
								}
								addShapeProperty(propertyList, "cutoff",
										Float.valueOf(cutoff));
								// if (!surfaceObjectSeen)
								sbCommand.append(" cutoff ").append(cutoff);
							}
						}
						if (ptWithin == 0) {
							onlyOneModel = "=xxxx";
							if (modelIndex < 0)
								modelIndex = viewer.getCurrentModelIndex();
							bs = viewer
									.getModelUndeletedAtomsBitSet(modelIndex);
							getWithinDistanceVector(propertyList, 2.0f, null,
									bs, false);
							// if (!surfaceObjectSeen)
							sbCommand.append(" within 2.0 ").append(
									Escape.escape(bs));
						}
						if (firstPass)
							defaultMesh = true;
					}
					if (firstPass
							&& viewer.getParameter("_fileType").equals("Pdb")
							&& Float.isNaN(sigma) && Float.isNaN(cutoff)) {
						// negative sigma just indicates that
						addShapeProperty(propertyList, "sigma",
								Float.valueOf(-1));
						// if (!surfaceObjectSeen)
						sbCommand.append(" sigma -1.0");
					}
					/*
					 * a file name, optionally followed by an integer file
					 * index. OR empty. In that case, if the model auxiliary
					 * info has the data stored in it, we use that. There are
					 * two possible structures:
					 * 
					 * jmolSurfaceInfo jmolMappedDataInfo
					 * 
					 * Both can be present, but if jmolMappedDataInfo is
					 * missing, then jmolSurfaceInfo is used by default.
					 */

					if (filename.equals("TESTDATA") && testData != null) {
						propertyValue = testData;
						break;
					}
					if (filename.equals("TESTDATA2") && testData2 != null) {
						propertyValue = testData2;
						break;
					}
					if (filename.length() == 0) {
						// ""
						if (modelIndex < 0)
							modelIndex = viewer.getCurrentModelIndex();
						if (surfaceObjectSeen || planeSeen)
							propertyValue = viewer.getModelAuxiliaryInfo(
									modelIndex, "jmolMappedDataInfo");
						if (propertyValue == null)
							propertyValue = viewer.getModelAuxiliaryInfo(
									modelIndex, "jmolSurfaceInfo");
						if (propertyValue != null) {
							surfaceObjectSeen = true;
							break;
						}
						filename = getFullPathName();
					}
					int fileIndex = -1;
					if (tokAt(i + 1) == Token.integer)
						addShapeProperty(propertyList, "fileIndex",
								Integer.valueOf(fileIndex = intParameter(++i)));
					if (!isSyntaxCheck) {
						String[] fullPathNameOrError;
						String localName = null;
						if (fullCommand.indexOf("# FILE" + nFiles + "=") >= 0) {
							filename = Parser.getQuotedAttribute(fullCommand,
									"# FILE" + nFiles);
							if (tokAt(i + 1) == Token.as)
								i += 2; // skip that
						} else if (tokAt(i + 1) == Token.as) {
							localName = viewer.getFilePath(
									stringParameter(iToken = (i = i + 2)),
									false);
							fullPathNameOrError = viewer
									.getFullPathNameOrError(localName);
							localName = fullPathNameOrError[0];
							if (viewer.getPathForAllFiles() != "") {
								// we use the LOCAL name when reading from a
								// local path only (in the case of JMOL files)
								filename = localName;
								localName = null;
							} else {
								addShapeProperty(propertyList, "localName",
										localName);
								viewer.setPrivateKeyForShape(iShape); // for the
																		// "AS"
																		// keyword
																		// to
																		// work
							}
						}
						// just checking here, and getting the full path name
						if (!filename.startsWith("cache://")) {
							fullPathNameOrError = viewer
									.getFullPathNameOrError(filename);
							filename = fullPathNameOrError[0];
							if (fullPathNameOrError[1] != null)
								error(ERROR_fileNotFoundException, filename
										+ ":" + fullPathNameOrError[1]);
						}
						Logger.info("reading isosurface data from " + filename);
						addShapeProperty(propertyList, "fileName", filename);
						if (localName != null)
							filename = localName;
						// if (!surfaceObjectSeen)
						sbCommand.append(" /*file*/").append(
								Escape.escapeStr(filename));
						// null propertyValue indicates that we need a reader
						// based on the fileName
					}
					// if (!surfaceObjectSeen)
					if (fileIndex >= 0)
						sbCommand.append(" ").append(fileIndex);
				}
				// if (!surfaceObjectSeen)
				if (sType != null)
					sbCommand.append(" ").append(Escape.escapeStr(sType));
				surfaceObjectSeen = true;
				break;
			case Token.connect:
				propertyName = "connections";
				switch (tokAt(++i)) {
				case Token.bitset:
				case Token.expressionBegin:
					propertyValue = new int[] { atomExpressionAt(i).nextSetBit(
							0) };
					break;
				default:
					propertyValue = new int[] { (int) floatParameterSet(i, 1, 1)[0] };
					break;
				}
				i = iToken;
				break;
			case Token.link:
				propertyName = "link";
				// if (!surfaceObjectSeen)
				sbCommand.append(" link");
				break;
			case Token.lattice:
				if (iShape != JmolConstants.SHAPE_ISOSURFACE)
					error(ERROR_invalidArgument);
				pt = getPoint3f(iToken + 1, false);
				i = iToken;
				if (pt.x <= 0 || pt.y <= 0 || pt.z <= 0)
					break;
				pt.x = (int) pt.x;
				pt.y = (int) pt.y;
				pt.z = (int) pt.z;
				sbCommand.append(" lattice ").append(Escape.escapePt(pt));
				if (isMapped) {
					propertyName = "mapLattice";
					propertyValue = pt;
				} else {
					lattice = pt;
				}
				break;
			default:
				if (theTok == Token.identifier) {
					propertyName = "thisID";
					propertyValue = str;
				}
				/*
				 * I have no idea why this is here.... if (planeSeen &&
				 * !surfaceObjectSeen) { addShapeProperty(propertyList, "nomap",
				 * Float.valueOf(0)); surfaceObjectSeen = true; }
				 */
				if (!setMeshDisplayProperty(iShape, 0, theTok)) {
					if (Token.tokAttr(theTok, Token.identifier) && !idSeen) {
						setShapeId(iShape, i, idSeen);
						i = iToken;
						break;
					}
					error(ERROR_invalidArgument);
				}
				if (iptDisplayProperty == 0)
					iptDisplayProperty = i;
				i = statementLength - 1;
				break;
			}
			idSeen = (theTok != Token.delete);
			if (isWild && surfaceObjectSeen)
				error(ERROR_invalidArgument);
			if (propertyName != null)
				addShapeProperty(propertyList, propertyName, propertyValue);
		}

		// OK, now send them all

		if (!isSyntaxCheck) {
			if ((isCavity || haveRadius) && !surfaceObjectSeen) {
				surfaceObjectSeen = true;
				addShapeProperty(propertyList, "bsSolvent",
						(haveRadius ? new BitSet()
								: lookupIdentifierValue("solvent")));
				addShapeProperty(propertyList, "sasurface", Float.valueOf(0));
			}
			if (planeSeen && !surfaceObjectSeen && !isMapped) {
				// !isMapped added 6/14/2012 12.3.30
				// because it was preventing planes from being mapped properly
				addShapeProperty(propertyList, "nomap", Float.valueOf(0));
				surfaceObjectSeen = true;
			}
			if (thisSetNumber >= 0)
				addShapeProperty(propertyList, "getSurfaceSets",
						Integer.valueOf(thisSetNumber - 1));
			if (discreteColixes != null) {
				addShapeProperty(propertyList, "colorDiscrete", discreteColixes);
			} else if ("sets".equals(colorScheme)) {
				addShapeProperty(propertyList, "setColorScheme", null);
			} else if (colorScheme != null) {
				ColorEncoder ce = viewer.getColorEncoder(colorScheme);
				if (ce != null) {
					ce.isTranslucent = isColorSchemeTranslucent;
					ce.hi = Float.MAX_VALUE;
					addShapeProperty(propertyList, "remapColor", ce);
				}
			}
			if (surfaceObjectSeen && !isLcaoCartoon
					&& sbCommand.indexOf(";") != 0) {
				propertyList.add(0, new Object[] { "newObject", null });
				boolean needSelect = (bsSelect == null);
				if (needSelect)
					bsSelect = BitSetUtil.copy(viewer.getSelectionSet(false));
				if (modelIndex < 0)
					modelIndex = viewer.getCurrentModelIndex();
				bsSelect.and(viewer.getModelUndeletedAtomsBitSet(modelIndex));
				if (onlyOneModel != null) {
					BitSet bsModels = viewer.getModelBitSet(bsSelect, false);
					if (bsModels.cardinality() != 1)
						error(ERROR_multipleModelsDisplayedNotOK, "ISOSURFACE "
								+ onlyOneModel);
					if (needSelect) {
						propertyList
								.add(0, new Object[] { "select", bsSelect });
						if (sbCommand.indexOf("; isosurface map") == 0) {
							sbCommand = new StringBuffer(
									"; isosurface map select "
											+ Escape.escape(bsSelect)
											+ sbCommand.substring(16));
						}
					}
				}
			}
			if (haveIntersection && !haveSlab) {
				if (!surfaceObjectSeen)
					addShapeProperty(propertyList, "sasurface",
							Float.valueOf(0));
				if (!isMapped) {
					addShapeProperty(propertyList, "map", Boolean.TRUE);
					addShapeProperty(propertyList, "select", bs);
					addShapeProperty(propertyList, "sasurface",
							Float.valueOf(0));
				}
				addShapeProperty(propertyList, "slab",
						getCapSlabObject(-100, false));
			}

			setShapeProperty(iShape, "setProperties", propertyList);

			if (defaultMesh) {
				setShapeProperty(iShape, "token", Integer.valueOf(Token.mesh));
				setShapeProperty(iShape, "token", Integer.valueOf(Token.nofill));
				setShapeProperty(iShape, "token",
						Integer.valueOf(Token.frontonly));
				sbCommand.append(" mesh nofill frontOnly");
			}
		}
		if (lattice != null) // before MAP, this is a display option
			setShapeProperty(JmolConstants.SHAPE_ISOSURFACE, "lattice", lattice);
		if (iptDisplayProperty > 0) {
			if (!setMeshDisplayProperty(iShape, iptDisplayProperty, 0))
				error(ERROR_invalidArgument);
		}

		if (isSyntaxCheck)
			return;
		Object area = null;
		Object volume = null;
		if (doCalcArea) {
			area = getShapeProperty(iShape, "area");
			if (area instanceof Float)
				viewer.setFloatProperty("isosurfaceArea",
						((Float) area).floatValue());
			else
				viewer.setUserVariable("isosurfaceArea",
						ScriptVariable.getVariable(area));
		}
		if (doCalcVolume) {
			volume = (doCalcVolume ? getShapeProperty(iShape, "volume") : null);
			if (volume instanceof Float)
				viewer.setFloatProperty("isosurfaceVolume",
						((Float) volume).floatValue());
			else
				viewer.setUserVariable("isosurfaceVolume",
						ScriptVariable.getVariable(volume));
		}
		if (!isLcaoCartoon) {
			if (isMapped && !surfaceObjectSeen) {
				setShapeProperty(iShape, "finalize", sbCommand.toString());
			} else if (surfaceObjectSeen) {
				cmd = sbCommand.toString();
				setShapeProperty(iShape, "finalize",
						(cmd.indexOf("; isosurface map") == 0 ? "" : " select "
								+ Escape.escape(bsSelect) + " ")
								+ cmd);
				String s = (String) getShapeProperty(iShape, "ID");
				if (s != null && !tQuiet) {
					cutoff = ((Float) getShapeProperty(iShape, "cutoff"))
							.floatValue();
					if (Float.isNaN(cutoff) && !Float.isNaN(sigma)) {
						Logger.error("sigma not supported");
					}
					s += " created";
					if (isIsosurface)
						s += " with cutoff=" + cutoff;
					float[] minMax = (float[]) getShapeProperty(iShape,
							"minMaxInfo");
					if (minMax[0] != Float.MAX_VALUE)
						s += " min=" + minMax[0] + " max=" + minMax[1];
					s += "; "
							+ JmolConstants.shapeClassBases[iShape]
									.toLowerCase() + " count: "
							+ getShapeProperty(iShape, "count");
					s += getIsosurfaceDataRange(iShape, "\n");
					if (doCalcArea)
						s += "\nisosurfaceArea = " + Escape.escapeArray(area);
					if (doCalcVolume)
						s += "\nisosurfaceVolume = "
								+ Escape.escapeArray(volume);
					showString(s);
				}
			} else if (doCalcArea || doCalcVolume) {
				if (doCalcArea)
					showString("isosurfaceArea = " + Escape.escapeArray(area));
				if (doCalcVolume)
					showString("isosurfaceVolume = "
							+ Escape.escapeArray(volume));
			}
		}
		if (translucency != null)
			setShapeProperty(iShape, "translucency", translucency);
		setShapeProperty(iShape, "clear", null);
		if (toCache) {
			String id = (String) getShapeProperty(iShape, "ID");
			viewer.cachePut("cache://isosurface_" + id,
					getShapeProperty(iShape, "jvxlDataXml"));
			runScript("isosurface ID \"" + id + "\" delete;isosurface ID \""
					+ id + "\""
					+ (modelIndex >= 0 ? " model " + modelIndex : "")
					+ " \"cache://isosurface_" + getShapeProperty(iShape, "ID")
					+ "\"");
		}
	}

	private String getColorRange(int i) throws ScriptException {
		int color1 = getArgbParam(i);
		if (tokAt(++iToken) != Token.to)
			error(ERROR_invalidArgument);
		int color2 = getArgbParam(++iToken);
		int nColors = (tokAt(iToken + 1) == Token.integer ? intParameter(++iToken)
				: 0);
		return ColorEncoder.getColorSchemeList(ColorEncoder.getPaletteAtoB(
				color1, color2, nColors));
	}

	private String getIsosurfaceDataRange(int iShape, String sep) {
		float[] dataRange = (float[]) getShapeProperty(iShape, "dataRange");
		return (dataRange != null && dataRange[0] != Float.MAX_VALUE
				&& dataRange[0] != dataRange[1] ? sep + "isosurface"
				+ " full data range " + dataRange[0] + " to " + dataRange[1]
				+ " with color scheme spanning " + dataRange[2] + " to "
				+ dataRange[3] : "");
	}

	private static Object testData; // for isosurface
	private static Object testData2; // for isosurface

	private void getWithinDistanceVector(List<Object[]> propertyList,
			float distance, Point3f ptc, BitSet bs, boolean isShow) {
		List<Point3f> v = new ArrayList<Point3f>();
		Point3f[] pts = new Point3f[2];
		if (bs == null) {
			Point3f pt1 = new Point3f(distance, distance, distance);
			Point3f pt0 = new Point3f(ptc);
			pt0.sub(pt1);
			pt1.add(ptc);
			pts[0] = pt0;
			pts[1] = pt1;
			v.add(ptc);
		} else {
			BoxInfo bbox = viewer.getBoxInfo(bs, -Math.abs(distance));
			pts[0] = bbox.getBboxVertices()[0];
			pts[1] = bbox.getBboxVertices()[7];
			if (bs.cardinality() == 1)
				v.add(viewer.getAtomPoint3f(bs.nextSetBit(0)));
		}
		if (v.size() == 1 && !isShow) {
			addShapeProperty(propertyList, "withinDistance",
					Float.valueOf(distance));
			addShapeProperty(propertyList, "withinPoint", v.get(0));
		}
		addShapeProperty(propertyList, (isShow ? "displayWithin"
				: "withinPoints"), new Object[] { Float.valueOf(distance), pts,
				bs, v });
	}

	/**
	 * @param shape
	 * @param i
	 * @param tok
	 * @return true if successful
	 * @throws ScriptException
	 */
	private boolean setMeshDisplayProperty(int shape, int i, int tok)
			throws ScriptException {
		String propertyName = null;
		Object propertyValue = null;
		boolean allowCOLOR = (shape == JmolConstants.SHAPE_CONTACT);
		boolean checkOnly = (i == 0);
		// these properties are all processed in MeshCollection.java
		if (!checkOnly)
			tok = getToken(i).tok;
		switch (tok) {
		case Token.color:
			if (allowCOLOR)
				iToken++;
			else
				break;
			//$FALL-THROUGH$
		case Token.opaque:
		case Token.translucent:
			if (!checkOnly)
				colorShape(shape, iToken, false);
			return true;
		case Token.nada:
		case Token.delete:
		case Token.on:
		case Token.off:
		case Token.hide:
		case Token.hidden:
		case Token.display:
		case Token.displayed:
			if (iToken == 1 && shape >= 0 && tokAt(2) == Token.nada)
				setShapeProperty(shape, "thisID", null);
			if (tok == Token.nada)
				return (iToken == 1);
			if (checkOnly)
				return true;
			switch (tok) {
			case Token.delete:
				setShapeProperty(shape, "delete", null);
				return true;
			case Token.hidden:
			case Token.hide:
				tok = Token.off;
				break;
			case Token.displayed:
				tok = Token.on;
				break;
			case Token.display:
				if (i + 1 == statementLength)
					tok = Token.on;
				break;
			}
			//$FALL-THROUGH$ for on/off/display
		case Token.frontlit:
		case Token.backlit:
		case Token.fullylit:
		case Token.contourlines:
		case Token.nocontourlines:
		case Token.dots:
		case Token.nodots:
		case Token.mesh:
		case Token.nomesh:
		case Token.fill:
		case Token.nofill:
		case Token.triangles:
		case Token.notriangles:
		case Token.frontonly:
		case Token.notfrontonly:
			propertyName = "token";
			propertyValue = Integer.valueOf(tok);
			break;
		}
		if (propertyName == null)
			return false;
		if (checkOnly)
			return true;
		setShapeProperty(shape, propertyName, propertyValue);
		if ((tokAt(iToken + 1)) != Token.nada) {
			if (!setMeshDisplayProperty(shape, ++iToken, 0))
				--iToken;
		}
		return true;
	}

	private void bind() throws ScriptException {
		/*
		 * bind "MOUSE-ACTION" actionName bind "MOUSE-ACTION" "script" not
		 * implemented: range [xyrange] [xyrange]
		 */
		String mouseAction = stringParameter(1);
		String name = parameterAsString(2);
		Point3f range1 = null;
		Point3f range2 = null;
		// if (tokAt(3) == Token.range) {
		// range1 = xypParameter(4);
		// range2 = xypParameter(++iToken);
		// checkLast(iToken);
		// } else {
		checkLength(3);
		// }
		if (!isSyntaxCheck)
			viewer.bindAction(mouseAction, name, range1, range2);
	}

	private void unbind() throws ScriptException {
		/*
		 * unbind "MOUSE-ACTION"|all ["...script..."|actionName|all]
		 */
		if (statementLength != 1)
			checkLength23();
		String mouseAction = optParameterAsString(1);
		String name = optParameterAsString(2);
		if (mouseAction.length() == 0 || tokAt(1) == Token.all)
			mouseAction = null;
		if (name.length() == 0 || tokAt(2) == Token.all)
			name = null;
		if (name == null && mouseAction != null
				&& ActionManager.getActionFromName(mouseAction) >= 0) {
			name = mouseAction;
			mouseAction = null;
		}
		if (!isSyntaxCheck)
			viewer.unBindAction(mouseAction, name);
	}

	private void undoRedoMove() throws ScriptException {
		// Jmol 12.1.46
		int n = 1;
		int len = 2;
		switch (tokAt(1)) {
		case Token.nada:
			len = 1;
			break;
		case Token.all:
			n = 0;
			break;
		case Token.integer:
			n = intParameter(1);
			break;
		default:
			error(ERROR_invalidArgument);
		}
		checkLength(len);
		if (!isSyntaxCheck)
			viewer.undoMoveAction(tokAt(0), n);
	}

	BitSet getAtomsNearSurface(float distance, String surfaceId) {
		Object[] data = new Object[] { surfaceId, null, null };
		if (isSyntaxCheck)
			return new BitSet();
		if (getShapeProperty(JmolConstants.SHAPE_ISOSURFACE, "getVertices",
				data))
			return viewer.getAtomsWithin(distance, (Point3f[]) data[1],
					(BitSet) data[2]);
		data[1] = Integer.valueOf(0);
		data[2] = Integer.valueOf(-1);
		if (getShapeProperty(JmolConstants.SHAPE_DRAW, "getCenter", data))
			return viewer.getAtomsWithin(distance, (Point3f) data[2]);
		return new BitSet();
	}

	/**
	 * Encodes a string such as "2.10" as an integer instead of a float so as to
	 * distinguish "2.1" from "2.10" used for model numbers and partial bond
	 * orders. 2147483647 is maxvalue, so this allows loading simultaneously up
	 * to 2147 files, each with 999999 models (or trajectories)
	 * 
	 * @param strDecimal
	 * @return float encoded as an integer
	 */
	static int getFloatEncodedInt(String strDecimal) {
		int pt = strDecimal.indexOf(".");
		if (pt < 1 || strDecimal.charAt(0) == '-' || strDecimal.endsWith(".")
				|| strDecimal.contains(".0"))
			return Integer.MAX_VALUE;
		int i = 0;
		int j = 0;
		if (pt > 0) {
			try {
				i = Integer.parseInt(strDecimal.substring(0, pt));
				if (i < 0)
					i = -i;
			} catch (NumberFormatException e) {
				i = -1;
			}
		}
		if (pt < strDecimal.length() - 1)
			try {
				j = Integer.parseInt(strDecimal.substring(pt + 1));
			} catch (NumberFormatException e) {
				// not a problem
			}
		i = i * 1000000 + j;
		return (i < 0 ? Integer.MAX_VALUE : i);
	}

	/**
	 * reads standard n.m float-as-integer n*1000000 + m and returns (n % 6) <<
	 * 5 + (m % 0x1F)
	 * 
	 * @param bondOrderInteger
	 * @return Bond order partial mask
	 */
	static int getPartialBondOrderFromFloatEncodedInt(int bondOrderInteger) {
		return (((bondOrderInteger / 1000000) % 6) << 5)
				+ ((bondOrderInteger % 1000000) & 0x1F);
	}

	static int getBondOrderFromString(String s) {
		return (s.indexOf(' ') < 0 ? JmolEdge.getBondOrderFromString(s)
				: s.toLowerCase().indexOf("partial ") == 0 ? getPartialBondOrderFromString(s
						.substring(8).trim()) : JmolEdge.BOND_ORDER_NULL);
	}

	private static int getPartialBondOrderFromString(String s) {
		return getPartialBondOrderFromFloatEncodedInt(getFloatEncodedInt(s));
	}

}
