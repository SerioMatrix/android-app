package com.gardion.android.family.client.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.model.GardionEvent
import kotlinx.coroutines.experimental.delay
import java.sql.Timestamp

class GardionServerEventManager(private val context: Context) {

    private val dataStore: DataStore
    private val api: GardionApi = GardionApi.instance
    private var job: Job = Job()

    init {
        val sharedPrefs = context.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)
    }

    enum class GardionEventType {
        ADMIN_DEACTIVATION, VPN_DISCONNECTED, VPN_REMOVED, APPLICATION_PASS, VPN_INTERVAL_CHECK,
        VPN_CONNECTED, VPN_DEACTIVATED, TEST_DEV, ADMIN_ACTIVATION
    }

    fun sendGardionEvent(type: GardionEventType) {
        sendEvent(type)
    }

    private fun getGardionEvent(type: GardionEventType): GardionEvent {
        val eventTimestamp = Timestamp(System.currentTimeMillis())
        val eventDescription = getEventDescription(type)
        val eventId = getEventId(type)
        val deviceId = dataStore.getConfigurationDeviceId()
        val event = GardionEvent.Event(eventTimestamp, eventDescription, eventId,
                deviceId.toString(), null)
        return GardionEvent(event)
    }

    private fun getEventId(type: GardionEventType): String {
        return when (type) {
            GardionEventType.ADMIN_DEACTIVATION -> context.getString(R.string.event_disable_admin_id)
            GardionEventType.VPN_DISCONNECTED -> context.getString(R.string.event_vpn_disconnected_id)
            GardionEventType.VPN_REMOVED -> context.getString(R.string.event_vpn_removed_id)
            GardionEventType.APPLICATION_PASS -> context.getString(R.string.event_application_pass_id)
            GardionEventType.VPN_INTERVAL_CHECK -> context.getString(R.string.event_interval_check_id)
            GardionEventType.VPN_CONNECTED -> context.getString(R.string.event_vpn_connected_id)
            GardionEventType.VPN_DEACTIVATED -> context.getString(R.string.event_vpn_deactivated_id)
            GardionEventType.TEST_DEV -> context.getString(R.string.event_test_dev_id)
            GardionEventType.ADMIN_ACTIVATION -> context.getString(R.string.event_enable_admin_id)
        }
    }

    private fun getEventDescription(type: GardionEventType): String {
        return when (type) {
            GardionEventType.ADMIN_DEACTIVATION -> context.getString(R.string.event_disable_admin_desc)
            GardionEventType.VPN_DISCONNECTED -> context.getString(R.string.event_vpn_disconnected_desc)
            GardionEventType.VPN_REMOVED -> context.getString(R.string.event_vpn_removed_desc)
            GardionEventType.APPLICATION_PASS -> context.getString(R.string.event_application_pass_desc)
            GardionEventType.VPN_INTERVAL_CHECK -> context.getString(R.string.event_interval_check_desc)
            GardionEventType.VPN_CONNECTED -> context.getString(R.string.event_vpn_connected_desc)
            GardionEventType.VPN_DEACTIVATED -> context.getString(R.string.event_vpn_deactivated)
            GardionEventType.TEST_DEV -> context.getString(R.string.event_test_dev_desc)
            GardionEventType.ADMIN_ACTIVATION -> context.getString(R.string.event_enable_admin_desc)
        }
    }

    //TODO - combine this with user pw event
    private fun sendEvent(type: GardionEventType) {
        Log.i("GARDION_EVENT", "trying to send out event...")
        job.cancel()
        job = launch(CommonPool) {
            try {
                //delaying execution a bit to avoid posting when the system is in transition from VPN to not-VPN
                //TODO - proper implementation could be: callback to when internet connection is available
                delay(3000)
                api.postEvent(getGardionEvent(type)).execute()
                Log.i("GARDION_EVENT", "sent out: ${getGardionEvent(type).toString()}")
            } catch (e: Exception) {
                Log.w("GARDION_EVENT", "Failed to post on gardion server: " + e.message)
            }
        }
    }
}