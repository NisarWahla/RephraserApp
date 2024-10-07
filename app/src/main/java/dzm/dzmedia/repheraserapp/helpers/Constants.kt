package dzm.dzmedia.repheraserapp.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import android.widget.Toast
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.BuildConfig
import dzm.dzmedia.repheraserapp.authentication_api.ErrorResponse

class Constants {
    companion object {
        fun errorHandler(errorJSOn: String?, context: Context) {
            val errorResponse =
                App.gson.fromJson(errorJSOn, ErrorResponse::class.java)
            Toast.makeText(
                context,
                "${errorResponse.data}",
                Toast.LENGTH_SHORT
            ).show()
        }

        fun getIpAddress(context: Context): String {
            val ip = if (BuildConfig.DEBUG) {
                REPHRASER_IP_ADDRESS
            } else {
                val wm: WifiManager =
                    context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
            }
            return ip
        }

        fun isOnline(context: Context?): Boolean {
            if (context == null) return false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            }
            return false
        }

        const val FROM_SOCIAL = "FROM_SOCIAL"
        const val FROM_FACEBOOK = "FROM_FACEBOOK"
        const val PREF_TOKEN = "PREF_TOKEN"
        const val GOOGLE_EMAIL = "GOOGLE_EMAIL"
        const val GOOGLE_PASSWORD = "GOOGLE_PASSWORD"
        const val EMAIL = "EMAIL"
        var IS_LOGIN = "IS_LOGIN"
        var GUEST_USER = "GUEST_USER"
        var GUEST_USER_INTENT = "GUEST_USER_INTENT"
        var guestEmail = "test@rephraser.co"
        const val UserModel = "UserModel"
        const val FreeInfo = "FreeInfo"
        const val REPHRASER_ACTION_TYPE = "rephraser"
        const val GRAMMAR_CHECKER_ACTION_TYPE = "Grammer Checker"
        const val Summarizer_ACTION_TYPE = "Summarizer"
        const val REPHRASER_IP_ADDRESS = "127.0.0.0095"
        const val GRAMMER_CHECKER_IP_ADDRESS = "127.0.0.1"
        const val Summarizer_IP_ADDRESS = "127.0.0.1"
        const val FreeUserKey = "asdf"
        const val REPHRASER_MODE_TYPE = "General"
    }
}