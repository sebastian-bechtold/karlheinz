package com.sebastianbechtold

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import com.sebastianbechtold.geoserversync.GeoServerSync
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class TestGeoServerSync {

    var geoserverUrl = "http://localhost:8080/geoserver/"
    var username = "admin"
    var password = "geoserver"

    var gs = GeoServerRestClient(geoserverUrl, username, password)

    var testUploadDir = File("testData/testUploadDir")


    @Test
    fun passingNonExistentUploadDirShouldReturnFalse() {

        var syncer = GeoServerSync(gs, false, false)

        var uploadDir = File("nonExistentDir")

        var result : Boolean = syncer.uploadDir(uploadDir)

        assertEquals(false, result)
    }


    @Test
    fun passingExistentUploadDirShouldReturnTrue() {

        var syncer = GeoServerSync(gs, false, false)

        var result : Boolean = syncer.uploadDir(testUploadDir)

        assertEquals(true, result)
    }

}