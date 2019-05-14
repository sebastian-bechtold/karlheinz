package com.sebastianbechtold.geoserverrestclient

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
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


    fun deleteDataStore(wsName: String, dataStoreName: String, deleteNonEmpty: Boolean): Int {

        var url = urlWorkspaces + "/" + wsName + "/" + "datastores/" + dataStoreName

        if (deleteNonEmpty) {
            url += "?recurse=true"
        }

        return gsHttpRequest(url, "DELETE")
    }


    fun deleteStyle(wsName: String, styleName: String): Int {

        var baseUrl = urlRest

        if (wsName != "") {
            baseUrl = urlWorkspaces + "/" + wsName
        }

        var url = baseUrl + "/styles/" + styleName

        return gsHttpRequest(url, "DELETE")

    }


    fun deleteWorkspace(name: String, deleteNonEmpty: Boolean): Int {

        var url = urlWorkspaces + "/" + name

        if (deleteNonEmpty) {
            url += "?recurse=true"
        }

        return gsHttpRequest(url, "DELETE");
    }


    fun existsDataStore(wsName: String, dataStoreName: String): Boolean {

        var url = urlWorkspaces + "/" + wsName + "/" + "datastores/" + dataStoreName

        return (gsHttpRequest(url, "GET") == 200)
    }


    fun existsLayer(name: String): Boolean {
        return gsHttpRequest(urlLayers + "/" + name, "GET") == 200;
    }


    fun existsStyle(wsName: String, styleName: String): Boolean {

        var baseUrl = urlRest

        if (wsName != "") {
            baseUrl = urlWorkspaces + "/" + wsName
        }

        return gsHttpRequest(baseUrl + "/styles/" + styleName, "GET") == 200;
    }


    fun existsWorkspace(wsName: String): Boolean {
        return gsHttpRequest(urlWorkspaces + "/" + wsName, "GET") == 200;
    }


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

        return com.sebastianbechtold.nanohttp.httpRequest(url, method, data, headers + _authHeaders).statusCode;
    }


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


    fun upload(url: String, url_get: String, httpMethod: String, file: File, overwrite: Boolean): Int {

        val mimeType = getMimeTypeFromFileName(file.name)

        if (mimeType == null) {
            return 0
        }


        var resourceExists = (gsHttpRequest(url_get, "GET") == 200)

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


    fun uploadDataStore(wsName: String, file: File, overwrite: Boolean): Int {

        // NOTE: Setting of uploaded file type could also be done using the "accept" header. See
        // https://docs.geoserver.org/latest/en/api/#/latest/en/api/1.0.0/datastores.yaml

        var url =
            urlWorkspaces + "/" + wsName + "/" + "datastores/" + file.nameWithoutExtension + "/file." + file.extension
        var httpMethod = "PUT"


        if (url == "") {
            return 0
        }

        return upload(url, url, httpMethod, file, overwrite)
    }


    fun uploadDataStoreXml(wsName: String, file: File, overwrite: Boolean): Int {

        var url = urlWorkspaces + "/" + wsName + "/datastores"
        var url_get = urlWorkspaces + "/" + wsName + "/datastores/" + file.nameWithoutExtension
        var httpMethod = "POST"

        println("Uploading data store definition '${file.name}'")

        return upload(url, url_get, httpMethod, file, overwrite)
    }


    fun uploadFeatureTypeXml(wsName: String, dataStoreName: String, file: File, overwrite: Boolean): Int {

        var url = urlWorkspaces + "/" + wsName + "/datastores/${dataStoreName}/featuretypes"
        var url_get =
            urlWorkspaces + "/" + wsName + "/datastores/${dataStoreName}/featuretypes/" + file.nameWithoutExtension

        var httpMethod = "POST"


        println("Uploading feature type definition '${file.name}'")

        return upload(url, url_get, httpMethod, file, overwrite)
    }


    fun uploadStyle(wsName: String, file: File, overwrite: Boolean): Int {

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

        return upload(url, url_update, httpMethod, file, overwrite)
    }
}