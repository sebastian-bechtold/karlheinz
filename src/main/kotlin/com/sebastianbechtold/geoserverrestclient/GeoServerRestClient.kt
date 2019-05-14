package com.sebastianbechtold.geoserverrestclient

import java.io.*
import java.lang.Exception
import java.util.*


class GeoServerRestClient(private val _geoServerUrl: String, username: String, password: String) {

    val _basicAuth = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).toByteArray()).trim()

    val _authHeaders = mapOf("Authorization" to _basicAuth)

    val _mimeTypesMap = mapOf(
        "gpkg" to "application/gpkg",
        "sld" to "application/vnd.ogc.sld+xml",
        "zip" to "application/zip",
        "xml" to "application/xml"
    )


    val urlRest = _geoServerUrl + "rest"
    val urlWorkspaces = urlRest + "/workspaces"
    val urlLayers = urlRest + "/layers"


    fun createWorkspace(name: String): Int {

        var xml = "<workspace><name>" + name + "</name></workspace>"

        return gsHttpRequest(
            urlWorkspaces,
            "POST",
            ByteArrayInputStream(xml.toByteArray()),
            mapOf("Content-type" to "application/xml")
        );
    }


    fun deleteWorkspace(name : String, deleteContents : Boolean = false) : Int {

        var url = urlWorkspaces + "/" + name

        if (deleteContents) {
            url += "?recurse=true"
        }

        return gsHttpRequest(
            url,
            "DELETE",
            headers = mapOf("Content-type" to "application/xml")
        );
    }

    /*
    fun getContentTypeFromFileName(name: String): String {

        // TODO: 3 Replace this with loop over map (file ending -> content type)

        if (name.endsWith(".shp.zip")) {
            return "shp"
        } else if (name.endsWith(".gpkg")) {
            return "gpkg"
        } else if (name.endsWith(".sld")) {
            return "sld"
        } else if (name.endsWith(".sld.zip")) {
            return "sld.zip"
        } else if (name.endsWith(".xml")) {
            return "xml"
        }

        return ""
    }
    */



    fun getMimeTypeFromFileName(fileName: String): String? {
        var fileEnding = fileName.substring(fileName.lastIndexOf('.') + 1)

        return _mimeTypesMap[fileEnding]
    }


    fun gsHttpRequest(
        url: String,
        method: String,
        data: InputStream? = null,
        headers: Map<String, String> = mapOf()
    ): Int {

        var statusCode = 0

        try {
            statusCode =
                com.sebastianbechtold.nanohttp.httpRequest(url, method, data, headers + _authHeaders).statusCode;


        } catch (exception: Exception) {
            println("Exception: " + exception.message)
        }

        return statusCode
    }



    // TODO 3: Redesign this. A response code != 200 could mean something other than that the workspace does not exist!
    fun layerExists(layerName: String): Boolean {
        return gsHttpRequest(urlLayers + "/" + layerName, "GET") == 200;
    }


    fun uploadDataStore(wsName : String, file : File, overwrite: Boolean) : Int {
        // TODO: 3 Check existence of workspace before file upload

        //val contentType = getContentTypeFromFileName(file.name)
        val mimeType = getMimeTypeFromFileName(file.name)

        if (mimeType == null) {
            return 0
        }

        var url = ""
        var httpMethod = ""

        var resourceExists = true

        // NOTE: Setting of uploaded file type could also be done using the "accept" header. See
        // https://docs.geoserver.org/latest/en/api/#/latest/en/api/1.0.0/datastores.yaml

        // TODO: 2 This will probably not create the data store name we want
       // url = urlWorkspaces + "/" + wsName + "/" + "datastores/" + file.nameWithoutExtension + "/file." + contentType

        url = urlWorkspaces + "/" + wsName + "/" + "datastores/" + file.nameWithoutExtension + "/file." + file.extension
        httpMethod = "PUT"

        resourceExists = (gsHttpRequest(url, "GET") == 200)

        if (url == "") {
            return 0
        }

        if (resourceExists) {

            if (!overwrite) {
                println("Resource '${file.nameWithoutExtension}' already exists and overwrite is disabled!")
                return 0

            }

            println("Updating resource '${file.name}'")

        } else {
            println("Creating resource '${file.name}'")

        }

        return gsHttpRequest(url, httpMethod, FileInputStream(file), mapOf("Content-type" to mimeType))
    }


    fun uploadFeatureType(wsName : String, dataStoreName : String, it : File) : Int {


        val mimeType = getMimeTypeFromFileName(it.name)

        if (mimeType == null) {
            return 0
        }

        var url = urlWorkspaces + "/" + wsName + "/datastores/${dataStoreName}/featuretypes/"


        println("Uploading feature type definition '${it.name}'")

        var statusCode = gsHttpRequest(url, "POST", FileInputStream(it), mapOf("Content-type" to mimeType))

        return statusCode
    }


    fun uploadStyle(wsName : String, file : File, overwrite: Boolean) : Int {

        val mimeType = getMimeTypeFromFileName(file.name)

        if (mimeType == null) {
            return 0
        }

        var url = ""
        var httpMethod = ""

        var resourceExists = true

        var baseUrl = urlRest

        if (wsName != "") {
            baseUrl = urlWorkspaces + "/" + wsName
        }

        var url_create = baseUrl + "/styles?name=" + file.nameWithoutExtension
        var url_update = baseUrl + "/styles/" + file.nameWithoutExtension + ".sld"

        resourceExists = (gsHttpRequest(url_update, "GET") == 200)

        // If resource exists, update file with PUT:
        if (resourceExists) {
            url = url_update
            httpMethod = "PUT"
        }
        // Otherwise, create file with POST:
        else {
            url = url_create
            httpMethod = "POST"
        }

        if (url == "") {
            return 0
        }

        if (resourceExists) {

            if (!overwrite) {
                println("Resource '${file.nameWithoutExtension}' already exists and overwrite is disabled!")
                return 0

            }

            println("Updating resource '${file.name}'")

        } else {
            println("Creating resource '${file.name}'")

        }

        return gsHttpRequest(url, httpMethod, FileInputStream(file), mapOf("Content-type" to mimeType))
    }


    /*
    fun uploadFile(file: File, wsName: String, overwrite: Boolean): Int {

        // TODO: 3 Check existence of workspace before file upload

        val contentType = getContentTypeFromFileName(file.name)
        val mimeType = getMimeTypeFromFileName(file.name)

        if (mimeType == null) {
            return 0
        }

        var url = ""
        var httpMethod = ""

        var resourceExists = true

        when (contentType) {
            "shp", "gpkg" -> {
                // NOTE: Setting of uploaded file type could also be done using the "accept" header. See
                // https://docs.geoserver.org/latest/en/api/#/latest/en/api/1.0.0/datastores.yaml

                // TODO: 2 This will probably not create the data store name we want
                url = urlWorkspaces + "/" + wsName + "/" + "datastores/" + file.nameWithoutExtension + "/file." + contentType
                httpMethod = "PUT"

                resourceExists = (gsHttpRequest(url, "GET") == 200)
            }





            "xml" -> {

                var baseUrl = urlRest

                if (wsName != "") {
                    baseUrl = urlWorkspaces + "/" + wsName
                }

                var url_create = baseUrl + "/datastores"
                var url_update = baseUrl + "/datastores/" + file.nameWithoutExtension

                resourceExists = (gsHttpRequest(url_update, "GET") == 200)

                // If resource exists, update file with PUT:
                if (resourceExists) {
                    url = url_update
                    httpMethod = "PUT"
                }
                // Otherwise, create file with POST:
                else {
                    url = url_create
                    httpMethod = "POST"
                }
            }
        }


        if (url == "") {
            return 0
        }

        if (resourceExists) {

            if (!overwrite) {
                println("Resource '${file.nameWithoutExtension}' already exists and overwrite is disabled!")
                return 0

            }

            println("Updating resource '${file.name}'")

        } else {
            println("Creating resource '${file.name}'")

        }

        return gsHttpRequest(url, httpMethod, FileInputStream(file), mapOf("Content-type" to mimeType))
    }
     */

    fun setLayerDefaultStyle(layerName: String, styleName: String): Int {

        var url = urlLayers + "/" + layerName + ".xml"

        var xml = "<layer><defaultStyle><name>${styleName}</name></defaultStyle></layer>"

        return gsHttpRequest(
            url,
            "PUT",
            ByteArrayInputStream(xml.toByteArray()),
            mapOf("Content-type" to "application/xml")
        );
    }


    // TODO 3: Redesign this. A response code != 200 could mean something other than that the workspace does not exist!
    fun workspaceExists(wsName: String): Boolean {
        return gsHttpRequest(urlWorkspaces + "/" + wsName, "GET") == 200;
    }

}