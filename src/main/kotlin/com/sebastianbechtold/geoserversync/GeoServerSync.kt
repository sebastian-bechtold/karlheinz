package com.sebastianbechtold.geoserversync

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import java.io.File


// TODO: 2 Upload data file and create layer separately, in order to be able to
// set the layer name as identical to the data source name. This is necessary because
// the automatically generated layer name is in many situations different from the name
// of the uploaded file.


// TODO: 2 Understand why landkreise_polygone fails with HTTP 500

// TODO: 4 Implement filename-based ignoring of files and folders (e.g. with leading '_')

class GeoServerSync(var _gs: GeoServerRestClient) {

    fun syncDir(dir: File) : Boolean {

        if (!dir.exists() || !dir.isDirectory()) {
            return false
        }

        dir.listFiles().forEach {

            if (it.equals(dir)) {
                // Do nothing
            } else if (it.isDirectory()) {
                syncWorkspace(it, it.name)
            } else if (it.isFile()) {

                print("Publish file '${it.name}' ... ")

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

            var contentType: String = ""

            if (it.name.endsWith(".shp.zip")) {
                contentType = "shp"
            }
            else if (it.name.endsWith(".sld.zip")) {
                contentType = "sld"
            }
            else if (it.name.endsWith(".gpkg")) {
                contentType = "gpkg"
            }
            else if (it.name.endsWith(".sld")) {
                contentType = "sld"
            }

            print("Publish file '${it.name}' ... ")

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
            }
            */
        }
        //###################### END Upload data source files ############################

    }

}