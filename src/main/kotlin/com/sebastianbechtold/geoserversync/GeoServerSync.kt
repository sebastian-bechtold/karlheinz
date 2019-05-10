package com.sebastianbechtold.geoserversync

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import java.io.File


// TODO: 2 Upload data file and create layer separately, in order to be able to
// set the layer name as identical to the data source name. This is necessary because
// the automatically generated layer name is in many situations different from the name
// of the uploaded file.


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
                    println("Ignoring file with invalid content type in upload root folder: " + it.name + ". In the root folder, only style files (.sld and .sld.zip) are processed.")
                    return@forEach
                }

                //print("Publishing file '${it.name}' ... ")

                println("HTTP " + _gs.uploadFile(it, "sld", ""))
            }
        }

        return true
    }


    fun syncWorkspace(dir: File, wsName: String = dir.name) {

        if (!_gs.existsWorkspace(wsName)) {
            print("Creating workspace '${wsName}' ... ")
            println("HTTP " + _gs.createWorkspace(wsName))
        } else {
            println("Workspace '${wsName}' already exists, no need to create it.")
        }

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

            //print("Publish file '${it.name}' ... ")

            var status = _gs.uploadFile(it, contentType, wsName)

            println("HTTP " + status)


            // Try to set style as default style of layer with same name:

            /*
            var fileNameBase = ""

            if (it.name.endsWith("sld")) {
                fileNameBase = it.name.substring(0, it.name.length - 4)
            } else if (it.name.endsWith("sld.zip")) {
                fileNameBase = it.name.substring(0, it.name.length - 8)
            }

            if (_gs.existsLayer(wsName + ":" + fileNameBase)) {
                _gs.setLayerDefaultStyle(fileNameBase, it.name)

                println("Set uploaded style '" + it.name + "' as default style for layer '" + fileNameBase + "'.")
            }
            else {
                println("No layer named '" + fileNameBase + "' found.")
            }
            */


        }
        //###################### END Upload data source files ############################

    }

}