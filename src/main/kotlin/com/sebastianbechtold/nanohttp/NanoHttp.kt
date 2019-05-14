package com.sebastianbechtold.nanohttp

import java.io.*
import java.net.HttpURLConnection
import java.net.URL


// Extent InputStream with method "myTransferTo" as in Java 9+:
fun InputStream.myTransferTo(os: OutputStream) {

    var i: Int
    // read byte by byte until end of stream
    while (true) {
        i = this.read()

        if (i < 0) {
            break
        }

        os.write(i)
    }

    this.close()
    os.close()
}


fun httpRequest(url: String, method: String = "GET", data: InputStream? = null, headers: Map<String, String> = mapOf()): HttpResponse {

    var conn: HttpURLConnection = URL(url).openConnection() as HttpURLConnection

    conn.doInput = true
    conn.doOutput = true

    conn.requestMethod = method

    // Set headers:
    for ((key, value) in headers) {
        conn.setRequestProperty(key, value)
    }

    // Send gsHttpRequest body:
    data?.myTransferTo(conn.outputStream)

    // Retrieve response body:
    var output = ByteArrayOutputStream()

    try {
        conn.inputStream.myTransferTo(output)
    } catch (e: Exception) {
    }

    // Return HttpResponse object:
    return HttpResponse(conn.responseCode, output)
}


class HttpResponse(val statusCode: Int, val data: ByteArrayOutputStream) {

}