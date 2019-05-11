

package com.sebastianbechtold.geoserverrestclient

import java.io.*
import java.lang.Exception
import java.util.*


class GeoServerRestClient(private val _geoServerUrl: String, username: String, password: String) {

    val _basicAuth = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).toByteArray()).trim()

    val _authHeaders = mapOf("Authorization" to _basicAuth)

    val _mimeTypesMap = mapOf(  "gpkg" to "application/gpkg",
                                "sld" to "application/vnd.ogc.sld+xml",
                                "zip" to "application/zip",
                                "xml" to "application/xml")


    val urlRest = _geoServerUrl + "rest"
    val urlWorkspaces = urlRest + "/workspaces"
    val urlLayers = urlRest + "/layers"
    val urlStyles = urlRest + "/styles"


    fun createWorkspace(name: String): Int {

        var xml = "<workspace><name>" + name + "</name></workspace>"

        return gsHttpRequest(urlWorkspaces, "POST", ByteArrayInputStream(xml.toByteArray()), mapOf("Content-type" to "application/xml"));
    }


    // TODO 3: Redesign this. A response code != 200 could mean something other than that the workspace does not exist!
    fun existsLayer(layerName: String): Boolean {
        return gsHttpRequest(urlLayers + "/" + layerName, "GET") == 200;
    }


    // TODO 3: Redesign this. A response code != 200 could mean something other than that the workspace does not exist!
    fun existsWorkspace(wsName: String): Boolean {
        return gsHttpRequest(urlWorkspaces + "/" + wsName, "GET") == 200;
    }


    fun getContentTypeFromFileName(name: String): String {

        // TODO: 3 Replace this with loop over map (file ending -> content type)

        if (name.endsWith(".shp.zip")) {
            return "shp"
        } else if (name.endsWith(".gpkg")) {
            return "gpkg"
        } else if (name.endsWith(".sld")) {
            return "sld"
        } else if (name.endsWith(".sld.zip")) {
            return "sld"
        }

        return ""
    }


    fun getMimeTypeFromFileName(fileName : String) : String? {
        var fileEnding = fileName.substring(fileName.lastIndexOf('.') + 1)

       return _mimeTypesMap[fileEnding]
    }


    fun gsHttpRequest(url : String, method : String, data : InputStream? = null, headers : Map<String, String> = mapOf()) : Int {

        var statusCode = 0

        try {
            statusCode = com.sebastianbechtold.nanohttp.httpRequest(url, method, data, headers + _authHeaders).statusCode;


        }
        catch(exception : Exception) {
            println("Exception: " + exception.message)
        }

        return statusCode
    }


    fun uploadFile(file : File, wsName : String, overwrite : Boolean) : Int {

        // TODO: 3 Check existence of workspace before file upload

        val contentType = getContentTypeFromFileName(file.name)
        val mimeType = getMimeTypeFromFileName(file.name)

        if (mimeType == null) {
            return 0
        }

        when(contentType) {
            "shp", "gpkg" ->
            {
                // NOTE: Setting of uploaded file type could also be done using the "accept" header. See
                // https://docs.geoserver.org/latest/en/api/#/latest/en/api/1.0.0/datastores.yaml

                var url = urlWorkspaces + "/" + wsName + "/" + "datastores/" + file.name + "/file." + contentType

                val resourceExists = (gsHttpRequest(url, "GET") == 200)

                if (resourceExists && !overwrite) {
                    println("Data set '${file.name}' already exists and overwrite is disabled!")
                    return 0
                }

                println("Uploading geodataset '${file.name}'")

                return gsHttpRequest(url, "PUT", FileInputStream(file), mapOf("Content-type" to mimeType))
            }


            "sld" -> {
                var baseUrl = urlRest

                if (wsName != "") {
                    baseUrl = urlWorkspaces + "/" + wsName
                }

                var url_create = baseUrl + "/styles?name=" + file.name
                var url_update = baseUrl + "/styles/" + file.name + ".sld"

                val resourceExists = (gsHttpRequest(url_update, "GET") == 200)

                if (resourceExists && !overwrite) {
                    println("Style '${file.name}' already exists and overwrite is disabled!")
                    return 0
                }

                println("Uploading style '${file.name}'")

                // If resource exists, update file with PUT:
                if (resourceExists) {
                    return gsHttpRequest(url_update, "PUT", FileInputStream(file), mapOf("Content-type" to mimeType))
                }
                // Otherwise, create file with POST:
                else {
                    return gsHttpRequest(url_create, "POST", FileInputStream(file), mapOf("Content-type" to mimeType))
                }
            }
        }



        return 0
    }


    fun setLayerDefaultStyle(layerName: String, styleName: String): Int {

        var url = urlLayers + "/" + layerName + ".xml"

        var xml = "<layer><defaultStyle><name>${styleName}</name></defaultStyle></layer>"

        return gsHttpRequest(url, "PUT", ByteArrayInputStream(xml.toByteArray()), mapOf("Content-type" to "application/xml"));
    }
}