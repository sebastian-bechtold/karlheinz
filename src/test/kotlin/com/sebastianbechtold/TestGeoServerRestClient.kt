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

        assert(gs.existsWorkspace(testWsName))

        assertEquals(200, gs.deleteWorkspace(testWsName, true))

        assertFalse(gs.existsWorkspace(testWsName))
    }


    @Test
    fun uploadFeatureType() {

        gs.deleteWorkspace(testWsName, true)

        // Create workspace:
        assertEquals(201,gs.createWorkspace(testWsName))

        // Upload data store:
        assertEquals(201,gs.uploadDataStore(testWsName, File(testDataDir + "test_gpkg.gpkg"), false))

        // Create:
        assertEquals(201, gs.uploadFeatureTypeXml(testWsName, "test_gpkg", File(testDataDir + "raumeinheiten_32632.xml"), false))

        // Update:
        assertEquals(200, gs.uploadFeatureTypeXml(testWsName, "test_gpkg", File(testDataDir + "raumeinheiten_32632.xml"), true))

        // Cleanup:
        assertEquals(200, gs.deleteWorkspace(testWsName, true))
    }


    @Test
    fun uploadGpkg() {

        gs.deleteWorkspace(testWsName, true)
        assertEquals(201, gs.createWorkspace(testWsName))

        // Upload GPKG:
        assertEquals(201, gs.uploadDataStore(testWsName, File(testDataDir + "test_gpkg.gpkg"), true))

        // Cleanup:
        assertEquals(200, gs.deleteWorkspace(testWsName, true))
    }


    @Test
    fun uploadShpZip() {

        gs.deleteWorkspace(testWsName, true)
        assertEquals(201, gs.createWorkspace(testWsName))

        // Create:
        assertEquals(201, gs.uploadDataStore(testWsName, File(testDataDir + "test_shp.zip"), false))

        // Upload shp.zip:
        assertEquals(201, gs.uploadDataStore(testWsName, File(testDataDir + "test_shp.zip"), true))

        // Cleanup:
        assertEquals(200, gs.deleteDataStore(testWsName, "test_shp", true))

        assertEquals(200, gs.deleteWorkspace(testWsName,true))
    }


    @Test
    fun uploadGlobalSld() {

        gs.deleteStyle("", "heatmap")

        // Create:
        assertEquals(201, gs.uploadStyle("", File(testDataDir + "heatmap.sld"), false))

        // Update:
        assertEquals(200, gs.uploadStyle("", File(testDataDir + "heatmap.sld"), true))

        // Cleanup:
        assertEquals(200,gs.deleteStyle("", "heatmap"))
    }


    @Test
    fun uploadWorkspaceSld() {

        gs.deleteWorkspace(testWsName, true)

        assertEquals(201, gs.createWorkspace(testWsName))

        // Create style:
        assertEquals(201, gs.uploadStyle(testWsName, File(testDataDir + "heatmap.sld"), false))

        // Update style:
        assertEquals(200, gs.uploadStyle(testWsName, File(testDataDir + "heatmap.sld"), true))

        // Cleanup:
        assertEquals(200, gs.deleteWorkspace(testWsName, true))
    }
}