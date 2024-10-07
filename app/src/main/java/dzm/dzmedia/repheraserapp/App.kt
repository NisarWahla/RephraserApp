package dzm.dzmedia.repheraserapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContextWrapper
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.aemerse.iap.BuildConfig
import com.aemerse.iap.DataWrappers
import com.aemerse.iap.IapConnector
import com.aemerse.iap.SubscriptionServiceListener
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.activities.SplashActivity
import dzm.dzmedia.repheraserapp.admob.AdManger
import kotlinx.coroutines.delay

class App : Application() {
    private val TAG = "Connector"
    private lateinit var iapConnector: IapConnector
    companion object {
        var gson = Gson()
        var app: App? = null
    }

    override fun onCreate() {
        super.onCreate()

        app = this
        AdManger.init(this)
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}
