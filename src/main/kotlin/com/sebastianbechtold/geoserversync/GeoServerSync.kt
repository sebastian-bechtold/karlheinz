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

                /*
                var url = _gs.urlWorkspaces + "/" + wsName + "/datastores/${dir.name}/featuretypes/"

                var mimeType = "application/xml"

                println("Uploading feature type definition '${it.name}'")

                var statusCode = _gs.gsHttpRequest(url, "POST", FileInputStream(it), mapOf("Content-type" to mimeType))
                */
                println("HTTP " + statusCode)
            }
        }
    }


    /*
    fun uploadStyles(wsName : String, styleFiles: ArrayList<File>) {

        //####### BEGIN Try to set each uploaded style file as default style of layer with same name ########

        // NOTE: We do this in a separate loop after all data and style files were uploaded in order to make
        // sure that all layers which are created from the uploaded data files already exist.

        for (it in styleFiles) {

            // First, upload the style file:
            var status = _gs.uploadFile(it, wsName, overwriteStyles)
            println("HTTP " + status)

            // Then, try to auto-assign it as default style for existing layer with same name:

            var fileNameBase = ""

            if (it.name.endsWith("sld")) {
                //fileNameBase = styleFile.name.substring(0, styleFile.name.length - 4)
                fileNameBase = it.nameWithoutExtension
            } else if (it.name.endsWith("sld.zip")) {
                fileNameBase = it.nameWithoutExtension.substring(0, it.nameWithoutExtension.length - 4)
            }

            val layerName = wsName + ":" + fileNameBase

            if (_gs.layerExists(layerName)) {

                println("Setting uploaded style '${it.name}' as default style for layer '${layerName}'.")

                val status = _gs.setLayerDefaultStyle(layerName, fileNameBase)

                println("HTTP " + status)
            }
        }
        //####### END Try to set each uploaded style file as default style of layer with same name ########
    }
    */


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