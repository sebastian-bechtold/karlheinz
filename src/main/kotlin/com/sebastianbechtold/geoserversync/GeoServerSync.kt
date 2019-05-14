package com.sebastianbechtold.geoserversync

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import java.io.File
import java.io.FileInputStream


// TODO: 4 Implement filename-based ignoring of files and folders (e.g. with leading '_')

class GeoServerSync(var _gs: GeoServerRestClient, var overwriteDataStores : Boolean, val overwriteStyles : Boolean) {


    fun uploadDir(dir: File): Boolean {

        if (!dir.exists() || !dir.isDirectory()) {
            return false
        }

        dir.listFiles().forEach {

            if (it.equals(dir)) {
                // Do nothing
            } else if (it.isDirectory()) {
                syncWorkspace(it, it.name)
            } else if (it.isFile()) {

                when(it.extension) {
                    "sld", "xml", "zip" -> {
                        println("HTTP " + _gs.uploadStyle("", it, overwriteStyles))
                    }
                }
            }
        }

        return true
    }



    fun syncWorkspace(workspaceDir: File, wsName: String = workspaceDir.name) {

        if (!_gs.workspaceExists(wsName)) {
            println("Creating workspace '${wsName}' ... ")
            println("HTTP " + _gs.createWorkspace(wsName))
        } else {
            println("Workspace '${wsName}' already exists, no need to create it.")
        }


        val dataStoreFolders = ArrayList<File>()
        val dataStoreFiles = ArrayList<File>()


        //###################### BEGIN Upload data source files ############################
        workspaceDir.listFiles().forEach {

            if (it.isDirectory) {

                if (it.name != "styles") {
                    dataStoreFolders.add(it)
                }
            }
            else {
                when (it.extension) {

                    "shp", "gpkg" -> {
                        dataStoreFiles.add(it)
                    }
                }
            }
        }
        //###################### END Upload data source files ############################


        uploadDataStores(wsName, dataStoreFiles)

        uploadFeatureTypes(wsName, dataStoreFolders)


        // Upload styles:

        // NOTE: WorkspaceDir is a local variable in this function. That's why we need to perform the existence
        // check for the styles folder here and can not move it into uploadStyles() (unless we pass workspaceDir
        // to uploadStyles()).

        var stylesFolder = File(workspaceDir.path + "/styles")

        if (stylesFolder.exists()) {
            uploadStyles(wsName, stylesFolder)
        }
    }


    fun uploadDataStores(wsName : String, dataStoreFiles : ArrayList<File>) {

        for(it in dataStoreFiles) {
            var status = _gs.uploadDataStore(wsName,it,overwriteDataStores)
            println("HTTP " + status)
        }
    }


    fun uploadFeatureTypes(wsName : String, datasetFolders : ArrayList<File>) {

        for (dir in datasetFolders) {
            dir.listFiles().forEach {

                if (!it.isFile()) {
                    return@forEach
                }

                var statusCode = _gs.uploadFeatureType(wsName, dir.name, it)

                println("HTTP " + statusCode)
            }
        }
    }


    fun uploadStyles(wsName : String, stylesFolder : File) {

        stylesFolder.listFiles().forEach {

            when(it.extension) {
                "xml", "sld", "zip" -> { } // carry on

                else -> {
                    return@forEach
                }
            }


            var status = _gs.uploadStyle(wsName, it, overwriteStyles)
            println("HTTP " + status)


            //####### BEGIN Try to set style file as default style of layer with same name ########
            val layerName = wsName + ":" +  it.nameWithoutExtension

            if (_gs.layerExists(layerName)) {

                println("Setting uploaded style '${it.name}' as default style for layer '${layerName}'.")

                val status = _gs.setLayerDefaultStyle(layerName,  it.nameWithoutExtension)

                println("HTTP " + status)
            }
            //####### END Try to set style file as default style of layer with same name ########

        }
    }
}