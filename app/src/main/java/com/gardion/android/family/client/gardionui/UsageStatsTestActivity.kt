package com.gardion.android.family.client.gardionui

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import com.gardion.android.family.client.R
import com.gardion.android.family.client.toast
import kotlinx.android.synthetic.main.activity_usage_stats_test.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class UsageStatsTestActivity : AppCompatActivity() {

    val tag = "GARDION_STATS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage_stats_test)
        button_usage_stats.setOnClickListener {showStats()}
        button_usage_stats_aggregated.setOnClickListener {showStatsAggregated()}

        //TODO - move this were it belongs, i.e. initial setup
        //startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

        //TODO - add check if permission has been granted (where? how often?), protect against turning
        //TODO - also implement queryEventStats to compare with results (on API 28)?
        //TODO - should probably be done by a worker
    }

    private fun showStats() {
        val usageStatsQuery = usageStats()
        val builder = StringBuilder()
        var statString: String

        builder.append("Usage Stats Test\n")
        Log.i(tag, "showing stats now... \n\n")

        Log.i(tag, usageStatsQuery.size.toString())

        for (details in usageStatsQuery) {
            statString =
                    Date(details.firstTimeStamp).toString() + " " +
                    Date(details.lastTimeStamp).toString() +  " " +
                    details.packageName + ": " +
                    details.totalTimeInForeground/1000 + " | " +
                    Date(details.lastTimeUsed).toString()
            Log.i(tag, statString)
        }
        usage_stats_textView.text = builder.toString()
    }

    private fun showStatsAggregated() {
        val usageStatsQuery = usageStatsAggregated()
        val builder = StringBuilder()
        var timeInForegroundMinutes: Double
        var lastTimeUsed: Date
        var statString: String

        builder.append("Usage Stats Test Aggregated\n")

        usageStatsQuery?.forEach {
            statString = it.key + " " + Date(it.value.firstTimeStamp) + " " + Date(it.value.lastTimeStamp)
            Log.i(tag, statString)
        }
        usage_stats_textView.text = builder.toString()
    }

    private fun showEvents() {
        val eventsQuery = usageEvents()
        while(eventsQuery?.hasNextEvent()!!) {
            //val event = eventsQuery.getNextEvent()
            //Log.i(tag, "s")
        }
    }


    private fun usageStats() : MutableList<UsageStats> {
        val statsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var list : MutableList<UsageStats>

        val timestamp = java.sql.Timestamp(System.currentTimeMillis())

        var calStart = Calendar.getInstance()
        calStart.add(Calendar.DAY_OF_YEAR, -4)
        val startTime = calStart.timeInMillis

        var calEnd = Calendar.getInstance()
        calEnd.add(Calendar.DAY_OF_YEAR, +1)
        val endTime = calEnd.timeInMillis

        list = statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                startTime, endTime)
        return(list)
    }

    private fun usageStatsAggregated(): MutableMap<String, UsageStats>? {
        val statsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val timestamp = java.sql.Timestamp(System.currentTimeMillis())
        Log.i(tag, "timestamp to time" + Date(timestamp.time).toString())
        val beginQuery: Long
        beginQuery = startOfDay(timestamp).timeInMillis

        val endQuery = System.currentTimeMillis()

        toast(beginQuery.toString())

        val query = statsManager.queryAndAggregateUsageStats(beginQuery, endQuery)
        //return query.toList()
        return query
    }

    private fun startOfDay(time: java.sql.Timestamp): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time.time
        cal.set(Calendar.HOUR_OF_DAY, 0) //set hours to zero
        cal.set(Calendar.MINUTE, 0) // set minutes to zero
        cal.set(Calendar.SECOND, 0) //set seconds to zero
        Log.i(tag, "startOfDay" + cal.time.toString())
        //return cal.timeInMillis as Int / 1000
        return cal
    }

    private fun usageEvents(): UsageEvents? {
        val statsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        var calStart = Calendar.getInstance()
        calStart.add(Calendar.DAY_OF_YEAR, -4)
        val startTime = calStart.timeInMillis

        var calEnd = Calendar.getInstance()
        calEnd.add(Calendar.DAY_OF_YEAR, +1)
        val endTime = calEnd.timeInMillis

        val list = statsManager.queryEvents(startTime, endTime)
        return(list)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun testToday(): Long {
        val z = ZoneId.of("Europe/Berlin")
        val secondsSinceEpoch = ZonedDateTime.now(z).toLocalDate().atStartOfDay(z).toEpochSecond()
        Log.i(tag, "test " + secondsSinceEpoch.toString())
        return secondsSinceEpoch
    }
}
