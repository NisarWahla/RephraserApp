package dzm.dzmedia.repheraserapp.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.iap.BuildConfig
import com.aemerse.iap.DataWrappers
import com.aemerse.iap.IapConnector
import com.aemerse.iap.SubscriptionServiceListener
import com.google.android.material.button.MaterialButton
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivitySubscriptionPlanBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubscriptionPlanActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySubscriptionPlanBinding
    private val TAG = SubscriptionPlanActivity::class.simpleName
    private lateinit var iapConnector: IapConnector
    private var skuKey = ""
    private var androidId = ""


    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val json = Prefs.getString(Constants.UserModel, "")
        MainActivity.userModel = App.gson.fromJson(json, DataObjectModel::class.java)

        try {
            binding.subscriptionType.text = MainActivity.userModel!!.subscription
            binding.renewalOn.text = MainActivity.userModel!!.subscription_end_at

            if (MainActivity.userModel!!.subscription == "3 Days Trial")
                binding.cancelRequest.setTextColor(Color.parseColor("#808080"))
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
            enableLogging = BuildConfig.DEBUG
        )

        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            @SuppressLint("LogNotTimber")
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
                Log.e(TAG, "onSubscriptionRestored: ${purchaseInfo.sku}\n")
                Prefs.putBoolean("subscription", true)
                Prefs.putString("package", purchaseInfo.packageName)
                skuKey = purchaseInfo.sku
            }

            @SuppressLint("LogNotTimber")
            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
                when (purchaseInfo.sku) {
                    resources.getString(R.string.monthly_id) -> {
                        purchaseInfo.orderId
                        Log.e(
                            TAG,
                            "onSubscriptionPurchased: ${purchaseInfo.orderId + " " + purchaseInfo.sku}\n"
                        )
                    }
                    resources.getString(R.string.yearly_id) -> {
                        purchaseInfo.orderId
                        Log.e(
                            TAG,
                            "onSubscriptionPurchased: ${purchaseInfo.orderId + " " + purchaseInfo.sku}\n"
                        )
                    }
                }
                skuKey = purchaseInfo.sku
                Prefs.putBoolean("subscription", true)
                Prefs.putString("package", purchaseInfo.toString())
                finish()
            }

            @SuppressLint("SetTextI18n", "LogNotTimber")
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
                Log.e(TAG, "onPricesUpdated: ${iapKeyPrices.entries}\n")
            }
        })

        binding.upgradePlanBtn.setOnClickListener(this)
        binding.backButton.setOnClickListener(this)
        binding.cancelRequest.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.upgradePlanBtn.id) {
            startActivity(
                Intent(
                    this@SubscriptionPlanActivity, PremiumActivity::class.java
                )
            )
        } else if (v?.id == binding.backButton.id) {
            super.onBackPressed()
            finish()
        } else if (v?.id == binding.cancelRequest.id) {
            /*if (MainActivity.userModel!!.subscription != "3 Days Trial") {
                cancelSubscriptionPopup()
            }*/
            iapConnector.unsubscribe(this, skuKey)
        }
    }

    private fun cancelSubscriptionPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.cancel_subsciption_dialog)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnCross = dialog.findViewById<MaterialButton>(R.id.noBtn)
        val btnVerifyNow = dialog.findViewById<MaterialButton>(R.id.yesBtn)
        btnCross.setOnClickListener {
            dialog.dismiss()
        }
        btnVerifyNow.setOnClickListener {
            cancelSubscriptionAPi()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    fun cancelSubscriptionAPi() {
        val dialog = Dialog(this)
        Utils.loader(dialog)
        val user_id = MainActivity.userModel?.user_id
        val token = "Bearer " + MainActivity.userModel?.token
        val subscription_id = MainActivity.userModel?.subscription_id
        RetrofitAuthenticationClient.AuthClient().cancelSubscription(
            token, user_id, subscription_id, androidId
        ).enqueue(object : Callback<AuthenticationBaseModel> {
            override fun onResponse(
                call: Call<AuthenticationBaseModel>, response: Response<AuthenticationBaseModel>
            ) {
                if (response.isSuccessful) {
                    dialog.dismiss()
                    val userModel: AuthenticationBaseModel =
                        response.body() as AuthenticationBaseModel
                    val mCurrentUser = userModel.data
                    val json = App.gson.toJson(userModel.data)
                    Prefs.putString(Constants.UserModel, json)
                    Log.d(TAG, "onResponse:subsplanac ${mCurrentUser.token}")
                    Prefs.putBoolean(Constants.IS_LOGIN, true)
                    Toast.makeText(
                        this@SubscriptionPlanActivity,
                        "${userModel.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(
                        Intent(
                            this@SubscriptionPlanActivity, MainActivity::class.java
                        )
                    )
                    finish()
                } else {
                    dialog.dismiss()
                    if (response.errorBody() != null) {
                        val errorJSOn = response.errorBody()?.string()
                        Constants.errorHandler(
                            errorJSOn, this@SubscriptionPlanActivity
                        )
                    }
                }
            }

            override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(
                    this@SubscriptionPlanActivity, "" + t.message, Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}