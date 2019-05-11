package com.sebastianbechtold

import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import com.sebastianbechtold.geoserversync.GeoServerSync
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class TestGeoServerSync {

    @Test
    fun passingNonExistentDirShouldReturnFalse() {

        var syncer = GeoServerSync(GeoServerRestClient("","",""), false, false)

        var dir = File("nonExistentDir")

        var result : Boolean = syncer.syncDir(dir)

        assertEquals(false, result)
    }
}