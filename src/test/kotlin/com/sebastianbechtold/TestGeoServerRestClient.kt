package com.sebastianbechtold

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import org.junit.Test
import kotlin.test.assertEquals


class TestGeoServerRestClient {

    var geoserverUrl = "http://192.168.0.100:8080/geoserver/"
    var username = "admin"
    var password = "geoserver"

    var gs = GeoServerRestClient(geoserverUrl, username, password)

    var testDataDir = "testData/"

    var testWsName = "test"


    @Test
    fun createWorkspace() {

        if (gs.workspaceExists(testWsName)) {
            assertEquals(200, gs.deleteWorkspace(testWsName))
        }

        gs.createWorkspace(testWsName)

        assertEquals(true, gs.workspaceExists(testWsName))
    }


    @Test
    fun deleteWorkspace() {

        if (!gs.workspaceExists(testWsName)) {
            assertEquals(200, gs.createWorkspace(testWsName))
        }

        assertEquals(200, gs.deleteWorkspace(testWsName))
    }


    @Test
    fun uploadGpkg() {

    }
}