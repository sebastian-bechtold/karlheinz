package com.sebastianbechtold

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import com.sebastianbechtold.geoserversync.GeoServerSync
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class TestGeoServerSync {

    var geoserverUrl = "http://192.168.0.100:8080/geoserver/"
    var username = "admin"
    var password = "geoserver"

    var gs = GeoServerRestClient(geoserverUrl, username, password)

    var testUploadDir = File("testData/testUploadDir")


    @Test
    fun passingNonExistentUploadDirShouldReturnFalse() {


        var syncer = GeoServerSync(gs, false, false)

        var uploadDir = File("nonExistentDir")

        var result : Boolean = syncer.syncDir(uploadDir)

        assertEquals(false, result)
    }


    @Test
    fun passingExistentUploadDirShouldReturnTrue() {

        var syncer = GeoServerSync(gs, false, false)

        var result : Boolean = syncer.syncDir(testUploadDir)

        assertEquals(true, result)
    }


    @Test
    fun createWorkspace() {

        var testWsName = "test"

        var syncer = GeoServerSync(gs, false, false)

        if (gs.existsWorkspace(testWsName)) {
            assertEquals(200, gs.deleteWorkspace(testWsName))
        }


        assertEquals(true, syncer.syncDir(testUploadDir))
    }


    @Test
    fun uploadGpkg() {

    }
}