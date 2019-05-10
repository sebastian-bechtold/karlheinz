package com.sebastianbechtold.geoserversync

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import java.io.File


// TODO: 4 Implement filename-based ignoring of files and folders (e.g. with leading '_')

class GeoServerSync(var _gs: GeoServerRestClient) {

    fun getContentTypeFromFileName(name: String): String {

        // TODO: 3 Replace this with loop over map (file ending -> content type)

        if (name.endsWith(".shp.zip")) {
            return "shp"
        } else if (name.endsWith(".sld.zip")) {
            return "sld"
        } else if (name.endsWith(".gpkg")) {
            return "gpkg"
        } else if (name.endsWith(".sld")) {
            return "sld"
        }
        else if (name.endsWith(".ftype.xml")) {
            return "featureType"
        }

        return ""
    }


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

                var contentType = getContentTypeFromFileName(it.name)

                if (contentType != "sld") {
                    println("Ignoring file with invalid content type in upload root folder: '$it.name'. In the root folder, only style files (.sld and .sld.zip) are processed.")
                    return@forEach
                }

                println("HTTP " + _gs.uploadFile(it, "sld", ""))
            }
        }

        return true
    }


    fun syncWorkspace(dir: File, wsName: String = dir.name) {

        if (!_gs.existsWorkspace(wsName)) {
            println("Creating workspace '${wsName}' ... ")
            println("HTTP " + _gs.createWorkspace(wsName))
        } else {
            println("Workspace '${wsName}' already exists, no need to create it.")
        }


        var featureTypeFiles = ArrayList<File>()

        val styleFiles = ArrayList<File>()

        //###################### BEGIN Upload data source files ############################
        dir.listFiles().forEach {

            if (!it.isFile()) {
                return@forEach
            }


            var contentType = getContentTypeFromFileName(it.name)

            if (contentType == "") {
                println("Ignoring file with unknown content type: " + it.name)
                return@forEach
            }
            else if (contentType == "sld") {
                styleFiles.add(it)
            }
            else if (contentType == "featureType") {
                featureTypeFiles.add(it)
            }
            else {
                var status = _gs.uploadFile(it, contentType, wsName)
                println("HTTP " + status)
            }
        }
        //###################### END Upload data source files ############################


        //################ BEGIN Upload feature type definitions ###############
        for(ftFile in featureTypeFiles) {
            var contentType = getContentTypeFromFileName(ftFile.name)
            var status = _gs.uploadFile(ftFile, contentType, wsName)
            println("HTTP " + status)

        }
        //################ END Upload feature type definitions ###############


        //####### BEGIN Try to set each uploaded style file as default style of layer with same name ########

        // NOTE: We do this in a separate loop after all data and style files were uploaded in order to make
        // sure that all layers which are created from the uploaded data files already exist.

        for(styleFile in styleFiles) {

            // First, upload the style file:
            var contentType = getContentTypeFromFileName(styleFile.name)
            var status = _gs.uploadFile(styleFile, contentType, wsName)
            println("HTTP " + status)


            // Then, try to auto-assign it as default style for existing layer with same name:

            var fileNameBase = ""

            if (styleFile.name.endsWith("sld")) {
                fileNameBase = styleFile.name.substring(0, styleFile.name.length - 4)
            } else if (styleFile.name.endsWith("sld.zip")) {
                fileNameBase = styleFile.name.substring(0, styleFile.name.length - 8)
            }

            val layerName = wsName + ":" + fileNameBase

            if (_gs.existsLayer(layerName)) {

                println("Setting uploaded style '${styleFile.name}' as default style for layer '${layerName}'.")

                val status = _gs.setLayerDefaultStyle(layerName, styleFile.name)

                println("HTTP " + status)
            }
        }
        //####### END Try to set each uploaded style file as default style of layer with same name ########
    }
}