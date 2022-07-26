package com.webrtc.localserver

import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.IStatus
import java.io.ByteArrayInputStream

internal class HttpServer(port: Int) : NanoHTTPD(null as String?, port) {
    private fun endWithDesignatedExtensions(var1: String): Boolean {
        return true
    }

    private fun handleMediaFile(
        var1: String,
        var2: String,
        var3: String,
        var4: Map<String, String>
    ): Response {
        var var96: Response
        return newChunkedResponse(Response.Status.INTERNAL_ERROR, "", ByteArrayInputStream(ByteArray(0)))
    }

    private fun handleOtherFile(
        param1: String,
        param2: String,
        param3: Map<String, String>
    ): Response {
        return newChunkedResponse(Response.Status.INTERNAL_ERROR, "", ByteArrayInputStream(ByteArray(0)))
    }

    private fun requestPlaylistFromPeer(var1: String, var2: String): ResponseData {
        return ResponseData("", Response.Status.INTERNAL_ERROR, "", ByteArray(0))

    }
    private fun requestFromNetwork(var1: String, var2: String): ResponseData
    {
        return ResponseData("", Response.Status.INTERNAL_ERROR, "", ByteArray(0))
    }


    // ==================================================
    // API parts
    // ==================================================

    /**
     * Override this to customize the server.<p>
     *
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
     * @param method	"GET", "POST" etc.
     * @param parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
     * @param header	Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    override fun serve(param1: IHTTPSession): Response {
        println("xxxxxx ${Gson().toJson(param1)}")
        return newChunkedResponse(Response.Status.INTERNAL_ERROR, "", ByteArrayInputStream(ByteArray(0)))
    }

     inner class ResponseData(
        val responseUrl: String,
        val status: IStatus,
        val contentType: String,
        val data: ByteArray
    )

    init {
        this.start(50000)
    }
}