package com.gardion.android.family.client.network

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.model.GardionEvent

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
        VPN_CONNECTED, VPN_DEACTIVATED
    }

    fun sendGardionEvent(type: GardionEventType) {
        sendEvent(type)
    }

    fun sendPasswordEvent(userPass: String) {
        job.cancel()
        job = launch(CommonPool) {
            try {
                api.postEvent(getGardionEvent(userPass)).execute()
            } catch (e: Exception) {
                Log.w("EventManager", "Failed to post on gardion server: " + e.message)
            }
        }
    }

    private fun getGardionEvent(type: GardionEventType): GardionEvent {
        val device = GardionEvent.Event.Device(dataStore.getConfigurationDeviceId() ?: Build.ID,
                dataStore.getConfigurationDeviceName() ?: Build.DEVICE)
        val eventDescription = getEventDescription(type)
        val eventId = getEventId(type)
        val event = GardionEvent.Event(eventDescription, eventId, device)
        return GardionEvent(event)
    }

    private fun getGardionEvent(userPassword: String): GardionEvent {
        val device = GardionEvent.Event.Device(dataStore.getConfigurationDeviceId() ?: Build.ID,
                dataStore.getConfigurationDeviceName() ?: Build.DEVICE)
        val eventDescription = getEventDescription(GardionEventType.APPLICATION_PASS)
        val eventId = getEventId(GardionEventType.APPLICATION_PASS)
        val event = GardionEvent.Event(eventDescription + userPassword, eventId, device)
        return GardionEvent(event)
    }

    private fun getEventId(type: GardionEventType): String {
        return when (type) {
            GardionEventType.ADMIN_DEACTIVATION -> context.getString(R.string.event_error_disable_admin_id)
            GardionEventType.VPN_DISCONNECTED -> context.getString(R.string.event_error_vpn_disconnected_id)
            GardionEventType.VPN_REMOVED -> context.getString(R.string.event_error_vpn_removed_id)
            GardionEventType.APPLICATION_PASS -> context.getString(R.string.event_application_pass_id)
            GardionEventType.VPN_INTERVAL_CHECK -> context.getString(R.string.event_interval_check_id)
            GardionEventType.VPN_CONNECTED -> context.getString(R.string.event_vpn_connected_id)
            GardionEventType.VPN_DEACTIVATED -> context.getString(R.string.event_vpn_deactivated_id)
        }
    }

    private fun getEventDescription(type: GardionEventType): String {
        return when (type) {
            GardionEventType.ADMIN_DEACTIVATION -> context.getString(R.string.event_error_disable_admin_desc)
            GardionEventType.VPN_DISCONNECTED -> context.getString(R.string.event_error_vpn_disconnected_desc)
            GardionEventType.VPN_REMOVED -> context.getString(R.string.event_error_vpn_removed_desc)
            GardionEventType.APPLICATION_PASS -> context.getString(R.string.event_application_pass_desc)
            GardionEventType.VPN_INTERVAL_CHECK -> context.getString(R.string.event_interval_check_desc)
            GardionEventType.VPN_CONNECTED -> context.getString(R.string.event_vpn_connected_desc)
            GardionEventType.VPN_DEACTIVATED -> context.getString(R.string.event_vpn_deactivated)
        }
    }

    private fun sendEvent(type: GardionEventType) {
        job.cancel()
        job = launch(CommonPool) {
            try {
                api.postEvent(getGardionEvent(type)).execute()
            } catch (e: Exception) {
                Log.w("EventManager", "Failed to post on gardion server: " + e.message)
            }
        }
    }
}