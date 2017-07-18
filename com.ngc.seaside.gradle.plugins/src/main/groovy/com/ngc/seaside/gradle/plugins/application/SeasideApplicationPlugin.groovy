package com.ngc.seaside.gradle.plugins.application

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by J57467 on 7/18/2017.
 */
class SeasideApplicationPlugin implements Plugin<Project> {

   @Override
   void apply(Project p) {
      p.configure(p) {

         extensions.create("seasideApplication", SeasideApplicationPluginExtension)

         /**
          * This plugin requires the java and maven plugins
          */
         plugins.apply 'java'
         plugins.apply 'application'

         /**
          * Modify distZip task to include resources
          */
         distZip {

         }

         /**
          * Modify distZip task to include resources
          */
         distTar {

         }

         /**
          * Modify start scripts task to allow custom start scripts
          */
         startScripts {
            def scriptString = (String) seasideApplication.startScript
            shell('echo ' + scriptString)
            if (scriptString != null) {
               //do special script
               def startScript = new File(scriptString )
               def scriptsDirectory = startScript.getParentFile()
               println(scriptsDirectory.toString())
               scriptsDirectory.listFiles().each { file ->
                  if(file.name == startScript.name){ //not going to work for .sh scripts missing the .sh
                     //change name then copy
                     println(startScript.name)
                  } else {
                     //copy file
                  }
               }
            } else {
               //super.startScripts
            }
         }

         defaultTasks = ['build']
      }
   }
}