package com.example.android.wearable.visualScheduleWatchface.calendar

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import at.bitfire.dav4jvm.BasicDigestAuthHandler
import at.bitfire.dav4jvm.DavCalendar
import at.bitfire.dav4jvm.DavCalendar.Companion.CALENDAR_QUERY
import at.bitfire.dav4jvm.DavCalendar.Companion.COMP_FILTER
import at.bitfire.dav4jvm.DavCalendar.Companion.COMP_FILTER_NAME
import at.bitfire.dav4jvm.DavCalendar.Companion.FILTER
import at.bitfire.dav4jvm.DavCalendar.Companion.TIME_RANGE
import at.bitfire.dav4jvm.DavCalendar.Companion.TIME_RANGE_END
import at.bitfire.dav4jvm.DavCalendar.Companion.TIME_RANGE_START
import at.bitfire.dav4jvm.DavResource.Companion.MIME_XML
import at.bitfire.dav4jvm.DavResource.Companion.PROP
import at.bitfire.dav4jvm.MultiResponseCallback
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.XmlUtils
import at.bitfire.dav4jvm.XmlUtils.insertTag
import at.bitfire.dav4jvm.XmlUtils.propertyName
import at.bitfire.dav4jvm.exception.DavException
import at.bitfire.dav4jvm.property.CalendarData
import at.bitfire.dav4jvm.property.GetETag
import at.bitfire.dav4jvm.property.SyncToken
import com.example.android.wearable.visualScheduleWatchface.R
import java.io.EOFException
import java.io.Reader
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

class AsyncDav() : AsyncTask<Context, Unit, Unit>() {
    private var events = mutableListOf<EventItem>();

    override fun doInBackground(vararg applicationContext: Context) {
        try {
            val authHandler = BasicDigestAuthHandler(
                domain = "posteo.de", // Optional, to only authenticate against hosts with this domain.
                username = "wier.adam@posteo.com",
                password = applicationContext.get(0).getString(R.string.posteo_password)
            )
            val httpClient = OkHttpClient.Builder()
                .followRedirects(false)
                .authenticator(authHandler)
                .addNetworkInterceptor(authHandler)
                .build()

            val start = null
            val end = null

            val serializer = XmlUtils.newSerializer()
            val writer = StringWriter()
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", null)
            serializer.setPrefix("", XmlUtils.NS_WEBDAV)
            serializer.setPrefix("CAL", XmlUtils.NS_CALDAV)
            serializer.insertTag(CALENDAR_QUERY) {
                insertTag(PROP) {
                    insertTag(GetETag.NAME)
                    insertTag(CalendarData.NAME)
                }
                insertTag(FILTER) {
                    insertTag(COMP_FILTER) {
                        attribute(null, COMP_FILTER_NAME, "VCALENDAR")
                        insertTag(COMP_FILTER) {
                            attribute(null, COMP_FILTER_NAME, "VEVENT")
                            if (start != null || end != null) {
                                insertTag(TIME_RANGE) {
                                    if (start != null)
                                        attribute(null, TIME_RANGE_START, SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT).format(start))
                                    if (end != null)
                                        attribute(null, TIME_RANGE_END, SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT).format(end))
                                }
                            }
                        }
                    }
                }
            }
            serializer.endDocument()
            var events = mutableListOf<EventItem>();
val callback: (Response, Response.HrefRelation) -> Unit = { response: Response, relation: Response.HrefRelation ->

    Log.d("caldav", response.properties.get(0).toString())
    val calendarData = response.properties.get(0) as CalendarData
    val splits = calendarData.iCalendar.toString().lines().map {
        it.split(":")
    }.map { it.get(0) to it.get(1) }.associate { it }
Log.d("caldav2", splits.get("SUMMARY")!!);
    val combined1 = splits.get("DTSTART") + splits.get("X-WR-Timezone")
    val combined2 = splits.get("DTSTART") + splits.get("X-WR-Timezone")
    val pattern = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z' z");
    val start = ZonedDateTime.parse( combined1, pattern)
    val end = ZonedDateTime.parse( combined2, pattern)
    events.add(EventItem(start =  start, end = end, summary = splits.get("SUMMARY")!!))

}
            val location = "https://posteo.de:8443/calendars/wier.adam/default/"

                val result = httpClient.newCall(
                    Request.Builder()
                    .url(location)
                    .method("REPORT", writer.toString().toRequestBody(MIME_XML))
                    .header("Depth", "1")
                    .build()).execute()
                val result2 = processMultiStatus(result.body!!.charStream(), location, callback)
    }catch (e: IOException) {
        Log.d("CALDAV", e.toString())
    }
    }

    protected fun processMultiStatus(reader: Reader, location: String, callback: MultiResponseCallback): List<Property> {
        val responseProperties = mutableListOf<Property>()
        val parser = XmlUtils.newPullParser()

        fun parseMultiStatus(): List<Property> {
            // <!ELEMENT multistatus (response*, responsedescription?,
            //                        sync-token?) >
            val depth = parser.depth
            var eventType = parser.eventType
            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType == XmlPullParser.START_TAG && parser.depth == depth + 1)
                    when (parser.propertyName()) {
                        Response.RESPONSE ->
                            at.bitfire.dav4jvm.Response.parse(parser, location.toHttpUrl(), callback)
                        SyncToken.NAME ->
                            XmlUtils.readText(parser)?.let {
                                responseProperties += SyncToken(it)
                            }
                    }
                eventType = parser.next()
            }

            return responseProperties
        }

        try {
            parser.setInput(reader)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.depth == 1)
                    if (parser.propertyName() == Response.MULTISTATUS)
                        return parseMultiStatus()
                // ignore further <multistatus> elements
                eventType = parser.next()
            }

            throw DavException("Multi-Status response didn't contain multistatus XML element")

        } catch (e: EOFException) {
            throw DavException("Incomplete multistatus XML element", e)
        } catch (e: XmlPullParserException) {
            throw DavException("Couldn't parse multistatus XML element", e)
        }
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        Log.d("CALDAVRESULT", events.toString())
    }


}
