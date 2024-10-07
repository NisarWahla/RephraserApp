package dzm.dzmedia.repheraserapp.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap.Config
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.iap.DataWrappers
import com.aemerse.iap.IapConnector
import com.aemerse.iap.SubscriptionServiceListener
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.BuildConfig
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivitySplashBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.GetInfo.Data
import dzm.dzmedia.repheraserapp.model.GetInfo.GetInfoModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    lateinit var activitySplashBinding: ActivitySplashBinding
    private val TAG = SplashActivity::class.simpleName
    private lateinit var iapConnector: IapConnector

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activitySplashBinding.root)
        try {
            Prefs.putBoolean("subscription", false)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        checkSubscribedUser()
        Handler(Looper.getMainLooper()).postDelayed({ //Do something after 100ms
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finishAffinity()
        }, 3000)

    }

    private fun checkSubscribedUser() {
        //in app purchase code to see user has an active subscription
        val nonConsumablesList = listOf("lifetime")
        val consumablesList = listOf(
            resources.getString(R.string.weekly_id),
            resources.getString(R.string.monthly_id),
            resources.getString(R.string.yearly_id)
        )
        val subscriptionList = listOf(
            resources.getString(R.string.weekly_id),
            resources.getString(R.string.monthly_id),
            resources.getString(R.string.yearly_id)
        )
        iapConnector = IapConnector(
            context = this,
            nonConsumableKeys = nonConsumablesList,
            consumableKeys = consumablesList,
            subscriptionKeys = subscriptionList,
            key = resources.getString(R.string.rephraser_key),
            enableLogging = com.aemerse.iap.BuildConfig.DEBUG
        )

        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            @SuppressLint("LogNotTimber")
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
                Log.e(TAG, "onSubscriptionRestored: ${purchaseInfo.sku}\n")
                Prefs.putBoolean("subscription", true)
                Prefs.putString("package", purchaseInfo.packageName)
                val tt = Prefs.putBoolean("subscription", true)
                Log.i(TAG, "onSubscriptionRestored: $tt")
            }

            @SuppressLint("LogNotTimber")
            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
            }

            @SuppressLint("SetTextI18n", "LogNotTimber")
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
                Log.e(TAG, "onPricesUpdated: ${iapKeyPrices.entries}\n")
            }
        })

    }
}