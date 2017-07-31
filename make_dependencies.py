# -*- coding: utf-8 -*-
"""
Created on Sun Jul 30 12:20:13 2017

@author: Tuan
"""

from os import listdir
path = "C:/Users/Tuan/workspace/Gmol/lib/"
files = [f for f in listdir(path) if ".jar" in f]

     
      
with open("pom_temp.xml", "w") as fout:
    for f in files:
        fout.write("<dependency> \n")
        fout.write("\t<groupId>%s</groupId> \n" % f.replace(".jar","") )
        fout.write("\t<artifactId>%s</artifactId> \n" % f.replace(".jar","") )
        fout.write("\t<scope>system</scope> \n")
        fout.write("\t<version>1.0</version>\n")
        fout.write("\t<systemPath>${basedir}\lib\%s</systemPath> \n" % f)
        fout.write("</dependency>\n")
        
        
    