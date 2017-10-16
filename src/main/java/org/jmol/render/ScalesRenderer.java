/* added -hcf
 * to render the Scales Information
 */
package org.jmol.render;
import org.jmol.shape.Scales;
import org.jmol.util.Colix;

public class ScalesRenderer extends ShapeRenderer {
    
  @Override
  protected void render() {
    Scales scales = (Scales) shape;
    String[] scaleString = generateScaleString();
    boolean allowKeys = viewer.getBooleanProperty("allowKeyStrokes");
    boolean modelKitMode = viewer.isModelKitMode();
    colix = (modelKitMode ? Colix.MAGENTA 
        : viewer.isSignedApplet() ? (allowKeys ? Colix.ORANGE : Colix.RED) : allowKeys ? Colix.BLUE : Colix.GRAY);
    float imageFontScaling = viewer.getImageFontScaling();
    scales.getFont(imageFontScaling);
    
    for (int i = 0; i <= 4; i++){
    	String ii = scaleString[i];
    	
    	//Tuan fixed bug
    	if (ii == null) continue;
    	//End
    	
        int width = scales.font3d.stringWidth(ii);
        int dx = (int) (width + Scales.margin * imageFontScaling);
        int dy = scales.ascent;
    	g3d.drawStringNoSlab(ii, scales.font3d,
    	        g3d.getRenderWidth() - dx, (i+1)*dy, 0);

      //  if (modelKitMode) {
         //g3d.setColix(GData.GRAY);
       //   g3d.fillRect(0, 0, 0, 0, dy * 2, dx * 3 / 2);      
    //    }
    }
    

  }


  private String[] generateScaleString() {
	  String[] scaleStrings = new String[5];
	  int currentScale = modelSet.currentScale;//NEED TO DISPLAY CURRENT SCALE INFO
	  int chrScale = modelSet.chrScaleNumber;
	  int lociScale = modelSet.lociScaleNumber;
	  int fiberScale = modelSet.fiberScaleNumber;
	  int nucleoScale = modelSet.nucleoScaleNumber;
	
	  if (chrScale != 0) {		  
		  scaleStrings[1] = "CHROM" + ": " + Integer.toString(chrScale);
	  }
	  if (lociScale != 0) {
		  scaleStrings[2] = "LOCI" + ": " + Integer.toString(lociScale);
	  }
	  if (fiberScale != 0) {
		  scaleStrings[3] = "FIBER" + ": " + Integer.toString(fiberScale);
	  }
	  if (nucleoScale != 0) {
		  scaleStrings[4] = "NUCLEO" + ": " + Integer.toString(nucleoScale);
	  }
	  
	  //generate current scale String
	  String currentScaleInfo = "CURRENT SCALE: ";
	  boolean checkScale = false;
	  switch(currentScale) {
	    case 1:
		  currentScaleInfo = currentScaleInfo + "GENOME";
		  checkScale = true;
		break;
	    case 2:
	      currentScaleInfo = currentScaleInfo + "CHROM";
	      checkScale = true;
		break;
	    case 3:
		  currentScaleInfo = currentScaleInfo + "LOCI";
		  checkScale = true;
		break;
	    case 4:
		  currentScaleInfo = currentScaleInfo + "FIBER";
		  checkScale = true;
		break;
	    case 5:
		  currentScaleInfo = currentScaleInfo + "NUCLEO";
		  checkScale = true;	    
		break;
		default:
		  checkScale = false;
	  }
	  
	  if (checkScale) {
		  scaleStrings[0] = currentScaleInfo;
	  }
	  
	  
	  return scaleStrings;
  }

}