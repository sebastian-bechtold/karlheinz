package com.sebastianbechtold.geoserversync

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import java.io.File
import java.io.FileInputStream
import java.net.URLEncoder


// TODO: 4 Implement filename-based ignoring of files and folders (e.g. with leading '_')

class GeoServerSync(var _gs: GeoServerRestClient, var overwriteDataStores : Boolean, val overwriteStyles : Boolean) {


    fun syncDir(dir: File): Boolean {

        if (!dir.exists() || !dir.isDirectory()) {
            return false
        }

        dir.listFiles().forEach {

            if (it.equals(dir)) {
                // Do nothing
            } else if (it.isDirectory()) {
                syncWorkspace(it, it.name)
            } else if (it.isFile()) {

                var contentType = _gs.getContentTypeFromFileName(it.name)

                if (contentType != "sld") {
                    println("Ignoring file with invalid content type in upload root folder: '$it.name'. In the root folder, only style files (.sld and .sld.zip) are processed.")
                    return@forEach
                }

                println("HTTP " + _gs.uploadFile(it, "sld", overwriteStyles))
            }
        }

        return true
    }


    fun createFeatureTypes(wsName: String, dir: File) {

        dir.listFiles().forEach {

            if (!it.isFile()) {
                return@forEach
            }

            var datastoreName = URLEncoder.encode(dir.name.substring(1))

            var url = _gs.urlWorkspaces + "/" + wsName + "/datastores/${datastoreName}/featuretypes/"


            var mimeType = "application/xml"

            println("Uploading feature type definition '${it.name}'")

            var statusCode = _gs.gsHttpRequest(url, "POST", FileInputStream(it), mapOf("Content-type" to mimeType))

            println("HTTP " + statusCode)
        }
    }


    fun syncWorkspace(dir: File, wsName: String = dir.name) {

        if (!_gs.existsWorkspace(wsName)) {
            println("Creating workspace '${wsName}' ... ")
            println("HTTP " + _gs.createWorkspace(wsName))
        } else {
            println("Workspace '${wsName}' already exists, no need to create it.")
        }


        val styleFiles = ArrayList<File>()
        val folders = ArrayList<File>()
        val datasets = ArrayList<File>()

        //###################### BEGIN Upload data source files ############################
        dir.listFiles().forEach {

            if (it.isDirectory) {
                folders.add(it)
            }

            if (!it.isFile()) {
                return@forEach
            }


            var contentType = _gs.getContentTypeFromFileName(it.name)

            when (contentType) {
                "sld", "sld.zip" -> {
                    styleFiles.add(it)
                }

                "shp", "gpkg" -> {
                    datasets.add(it)
                }

                "xml" -> {
                    var status = _gs.uploadFile(it, wsName, overwriteDataStores)
                    println("HTTP " + status)
                }
            }
        }
        //###################### END Upload data source files ############################


        uploadDatasets(wsName, datasets)

        uploadFeatureTypes(wsName, folders)

        uploadStyles(wsName, styleFiles)

    }


    fun uploadDatasets(wsName : String, datasetFiles : ArrayList<File>) {

        for(it in datasetFiles) {
            var status = _gs.uploadFile(it, wsName, overwriteDataStores)
            println("HTTP " + status)
        }
    }


    fun uploadFeatureTypes(wsName : String, datasetFolders : ArrayList<File>) {

        for (folder in datasetFolders) {
            createFeatureTypes(wsName, folder)
        }
    }


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

            if (_gs.existsLayer(layerName)) {

                println("Setting uploaded style '${it.name}' as default style for layer '${layerName}'.")

                val status = _gs.setLayerDefaultStyle(layerName, fileNameBase)

                println("HTTP " + status)
            }
        }
        //####### END Try to set each uploaded style file as default style of layer with same name ########
    }
}