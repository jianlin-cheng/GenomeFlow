------------------------------------------------------------------------------------------------------------------------------------
# GenomeFlow : A Graphical Tool for Modeling and Analyzing 3D Genome Structure 
------------------------------------------------------------------------------------------------------------------------------------
A comprehensive graphical tool to facilitate the entire process of 3D genome organization, modeling and analysis of Hi-C data. 

GenomeFlow is capable of creating index for reference genome, generate maps from raw fastq data files, and support the reconstruction of  two-dimensional (2D) and 
three- Dimensional (3D) chromosome and genome models with our graphical user interface.

If you have further difficulties using GenomeFlow, please do not hesitate to contact us (chengji@missouri.edu) 

--------------------------------------------------------------------		

# Documentation

Please see [the wiki](https://github.com/jianlin-cheng/GenomeFlow/wiki) for an extensive documentation, or download the [user guide](https://github.com/jianlin-cheng/GenomeFlow/raw/master/user_guide/UserGuide_4.2.docx)

--------------------------------------------------------------------	

# Distribution

In this repository, we include the folowing
* **executable**: contains the GenomeFlow .jar executable file and sample. Download latest version from [here](https://github.com/jianlin-cheng/GenomeFlow/releases)
* examples: contains example data and outputs generated from ClusterTAD for these datasets 
* src: contains the  source codes
* user_guide: contains user guide
* lib: contains the external libraries used in source code

--------------------------------------------------------------------	
# Hardware and Software Requirements

GenomeFlow consists of three parts: the pipeline that takes raw fastq files to create a formatted text files called the 1D-Function, the analysis of binned HiC file called 2D-Functions. The third part Reconstruct and visualize three- Dimensional (3D) of chromosome and genome models with our 3D-Function tools.  

## Requirements
GenomeFlow requires the use of a computer, with ideally >= 4 cores (min 1 core) and >= 4 GB RAM (min 2 GB RAM)


## GenomeFlow tools requirements
The minimum software requirement to run GenomeFlow is a working Java installation (version >= 1.7) on Windows, Linux, and Mac OSX. We recommend using the latest Java version available, but please do not use the Java Beta Version. Minimum system requirements for running Java can be found at https://java.com/en/download/help/sysreq.xml

To download and install the latest Java Runtime Environment (JRE), please go to https://www.java.com/download


## Burrows-Wheeler Aligner (BWA) for 1D-Function
The latest version of BWA should be installed from http://bio-bwa.sourceforge.net/


## Bowtie2 for 1D-Function 

The latest version of Bowtie2  should be installed from  http://bowtie-bio.sourceforge.net/index.shtml

--------------------------------------------------------------------	

# Building from source code
The project uses JUnit for testing and Maven to manage dependencies and builds. It can be imported into Eclipse as a Maven project.

## Requirements
* Maven: manage builds and dependencies. <br>
If using Eclipse, there is no need to install Maven. Otherwise, Maven needs to be installed to compile the code. 

<br>

* Example commands <br>
`mvn compile`: to compile <br>
`mvn test`: to run test <br>
`mvn package`: to compile, test and package <br>


## Compiling Source code
Requires Java JDK 1.8 (not JRE but JDK, set JAVA_HOME to JDK folder)


--------------------------------------------------------------------	