package com.sebastianbechtold

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse


// TODO: 2 Test upload of zipped SLD (global and workspace)

class TestGeoServerRestClient {

    var geoserverUrl = "http://localhost:8080/geoserver/"
    var username = "admin"
    var password = "geoserver"

    var gs = GeoServerRestClient(geoserverUrl, username, password)

    var testDataDir = "testData/"

    var testWsName = "test"


    @Test
    fun createAndDeleteWorkspace() {

        gs.deleteWorkspace(testWsName, true)

        gs.createWorkspace(testWsName)

        assert(gs.workspaceExists(testWsName))

        assertEquals(200, gs.deleteWorkspace(testWsName, true))

        assertFalse(gs.workspaceExists(testWsName))
    }


    @Test
    fun uploadGpkg() {

        gs.createWorkspace(testWsName)

        assertEquals(201, gs.uploadDataStore(testWsName, File(testDataDir + "test.gpkg"), true))

        assertEquals(200, gs.deleteDataStore(testWsName, "test", true))
    }


    @Test
    fun uploadGlobalSld() {

        gs.deleteStyle("", "heatmap")

        assertEquals(201, gs.uploadStyle("", File(testDataDir + "heatmap.sld"), true))

        assertEquals(200, gs.deleteStyle("", "heatmap"))

    }


    @Test
    fun uploadWorkspaceSld() {

        gs.createWorkspace(testWsName)

        gs.deleteStyle(testWsName, "heatmap")

        assertEquals(201, gs.uploadStyle(testWsName, File(testDataDir + "heatmap.sld"), true))

        assertEquals(200, gs.deleteWorkspace(testWsName, true))

    }
}