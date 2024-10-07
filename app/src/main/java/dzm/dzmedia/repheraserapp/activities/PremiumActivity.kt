package dzm.dzmedia.repheraserapp.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.aemerse.iap.*
import com.denzcoskun.imageslider.models.SlideModel
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityPremiumBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.ArrayList

class PremiumActivity : AppCompatActivity() {
    var defaultSelected = 1
    lateinit var binding: ActivityPremiumBinding
    private lateinit var iapConnector: IapConnector
    private val isBillingClientConnected: MutableLiveData<Boolean> = MutableLiveData()
    var weeklyPrice: String = ""
    var monthlyPrice: String = ""
    var yearlyPrice: String = ""
    var amount: Int = 0
    var apiWeekPrice = 0
    var apiMonthPrice = 0
    var apiYearPrice = 0

    companion object {
        const val TAG = "GoogleBillingService"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*val json = Prefs.getString(Constants.UserModel, "")
        MainActivity.userModel = App.gson.fromJson(json, DataObjectModel::class.java)*/

        binding.back.setOnClickListener {
            finish()
        }
        sliderImages()
        isBillingClientConnected.value = false
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
            }

            @SuppressLint("LogNotTimber")
            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
                var subscriptionType: String = ""
                when (purchaseInfo.sku) {
                    resources.getString(R.string.weekly_id) -> {
                        amount = apiWeekPrice
                        subscriptionType = "weekly"
                        purchaseInfo.orderId
                        Log.e(
                            TAG,
                            "onSubscriptionPurchased: ${"OrderId: " + purchaseInfo.orderId + "SKU: " + purchaseInfo.sku}\n"
                        )
                    }
                    resources.getString(R.string.monthly_id) -> {
                        amount = apiMonthPrice
                        subscriptionType = "monthly"
                        purchaseInfo.orderId
                        Log.e(
                            TAG,
                            "onSubscriptionPurchased: ${purchaseInfo.orderId + " " + purchaseInfo.sku}\n"
                        )
                    }
                    resources.getString(R.string.yearly_id) -> {
                        amount = apiYearPrice
                        subscriptionType = "yearly"
                        purchaseInfo.orderId
                        Log.e(
                            TAG,
                            "onSubscriptionPurchased: ${purchaseInfo.orderId + " " + purchaseInfo.sku}\n"
                        )
                    }
                }
                paymentApi(purchaseInfo.orderId, subscriptionType, amount)
            }

            @SuppressLint("SetTextI18n", "LogNotTimber")
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
                Log.e(TAG, "onPricesUpdated: ${iapKeyPrices.entries}\n")
                if (iapKeyPrices.entries.isNotEmpty()) {
                    try {
                        apiWeekPrice =
                            iapKeyPrices[resources.getString(R.string.weekly_id)]?.priceAmount?.toInt()!!
                        apiMonthPrice =
                            iapKeyPrices[resources.getString(R.string.monthly_id)]?.priceAmount?.toInt()!!
                        apiYearPrice =
                            iapKeyPrices[resources.getString(R.string.yearly_id)]?.priceAmount?.toInt()!!
                        weeklyPrice =
                            "" + iapKeyPrices[resources.getString(R.string.weekly_id)]?.price.toString()
                        monthlyPrice =
                            "" + iapKeyPrices[resources.getString(R.string.monthly_id)]?.price.toString()
                        yearlyPrice =
                            "" + iapKeyPrices[resources.getString(R.string.yearly_id)]?.price.toString()
                        binding.basicPrice.text = weeklyPrice
                        binding.standardPrice.text = monthlyPrice
                        binding.premiumPrice.text = yearlyPrice
                        binding.weekBelowValue.text = "$weeklyPrice /week"
                        binding.monthBelowValue.text = "$monthlyPrice /month"
                        val yearlyDividedValue =
                            iapKeyPrices[resources.getString(R.string.yearly_id)]?.priceAmount?.div(
                                12
                            )
                        val formattedValue = DecimalFormat("##.##").format(yearlyDividedValue)
                        binding.yearBelowValue.text = "$formattedValue /month"
                    } catch (e: Exception) {

                    } catch (e: NullPointerException) {

                    }
                }
            }
        })

        binding.monthlyPlan.setOnClickListener {
            defaultSelected = 1
            checkCondition()
        }

        binding.quarterlyPlan.setOnClickListener {
            defaultSelected = 2
            checkCondition()
        }

        binding.yearlyPlan.setOnClickListener {
            defaultSelected = 3
            checkCondition()
        }

        binding.btn.setOnClickListener {
            if (Constants.isOnline(this)) {
                if (Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                    when (defaultSelected) {
                        1 -> {
                            //Toast.makeText(this, "Clicked 0", Toast.LENGTH_SHORT).show()
                            iapConnector.subscribe(
                                this@PremiumActivity,
                                resources.getString(R.string.weekly_id)
                            )
                        }
                        2 -> {
                            //Toast.makeText(this, "Clicked 0", Toast.LENGTH_SHORT).show()
                            iapConnector.subscribe(
                                this@PremiumActivity,
                                resources.getString(R.string.monthly_id)
                            )
                        }
                        3 -> {
                            //Toast.makeText(this, "Clicked 1", Toast.LENGTH_SHORT).show()
                            iapConnector.subscribe(
                                this@PremiumActivity,
                                resources.getString(R.string.yearly_id)
                            )
                        }
                    }
                } else {
                    val intent = Intent(
                        this@PremiumActivity,
                        SignInActivity::class.java
                    )
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Please check your internet connection!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        checkCondition()
    }

    fun sliderImages() {
        val imageList = ArrayList<SlideModel>() // Create image list
        imageList.add(SlideModel(R.drawable.img_1, true))
        imageList.add(SlideModel(R.drawable.img_2, true))
        imageList.add(SlideModel(R.drawable.img_3, true))
        binding.imageSlider.setImageList(imageList)
    }

    fun paymentApi(
        paymentId: String,
        subscriptionType: String,
        amount: Int
    ) {
        val dialog = Dialog(this@PremiumActivity)
        try {
            Utils.loader(dialog)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        val token = "Bearer " + MainActivity.userModel!!.token
        val userId = MainActivity.userModel!!.user_id
        val isTest: Int = if (BuildConfig.DEBUG) {
            1
        } else {
            1
        }

        RetrofitAuthenticationClient.AuthClient()
            .subscribedUser(
                token,
                Constants.FreeUserKey,
                paymentId,
                subscriptionType,
                amount,
                "card",
                userId,
                1234,
                isTest
            )
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>,
                    response: Response<AuthenticationBaseModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        val msg = response.body()?.message
                        val userModel: AuthenticationBaseModel =
                            response.body() as AuthenticationBaseModel
                        userModel.data.token = Prefs.getString(Constants.PREF_TOKEN, token)
                        val json = App.gson.toJson(userModel.data)
                        Prefs.putString(Constants.UserModel, json)
                        Prefs.putBoolean(Constants.IS_LOGIN, true)
                        Prefs.putBoolean("subscription", true)
                        Toast.makeText(this@PremiumActivity, "$msg", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this@PremiumActivity, MainActivity::class.java))
                        finishAffinity()
                        /*Prefs.putBoolean("subscription", true)
                        Prefs.putString("package", purchaseInfo.toString())
                        val intent = Intent()
                        intent.putExtra("isSubscribed", true)
                        setResult(RESULT_OK, intent)
                        finish()*/
                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            /*Constants.errorHandler(
                                errorJSOn,
                                this@PremiumActivity
                            )*/
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@PremiumActivity, "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()
        //val json = Prefs.getString(Constants.UserModel, "")
        //MainActivity.userModel = App.gson.fromJson(json, DataObjectModel::class.java)
    }

    private fun checkCondition() {
        when (defaultSelected) {
            1 -> {
                if (Constants.isOnline(this)) {
                    binding.monthlyPlan.setBackgroundResource(R.drawable.monthly_plan_selected_rel_bg)
                    binding.quarterlyPlan.setBackgroundResource(R.drawable.monthly_plan_unselected_rel_bg)
                    binding.yearlyPlan.setBackgroundResource(R.drawable.monthly_plan_unselected_rel_bg)
                    binding.radio.setBackgroundResource(R.drawable.ic_selected)
                    binding.radio1.setBackgroundResource(R.drawable.ic_radio_bg)
                    binding.radio2.setBackgroundResource(R.drawable.ic_radio_bg)
                    binding.check.visibility = View.VISIBLE
                    binding.check1.visibility = View.GONE
                    binding.check2.visibility = View.GONE
                    binding.planTxt.text = getString(R.string.get_weekly_plan)
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
            }
            2 -> {
                if (Constants.isOnline(this)) {
                    binding.quarterlyPlan.setBackgroundResource(R.drawable.monthly_plan_selected_rel_bg)
                    binding.monthlyPlan.setBackgroundResource(R.drawable.monthly_plan_unselected_rel_bg)
                    binding.yearlyPlan.setBackgroundResource(R.drawable.monthly_plan_unselected_rel_bg)
                    binding.radio1.setBackgroundResource(R.drawable.ic_selected)
                    binding.radio.setBackgroundResource(R.drawable.ic_radio_bg)
                    binding.radio2.setBackgroundResource(R.drawable.ic_radio_bg)
                    binding.check.visibility = View.GONE
                    binding.check1.visibility = View.VISIBLE
                    binding.check2.visibility = View.GONE
                    binding.planTxt.text = getString(R.string.get_monthly_plan)
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
            }
            3 -> {
                if (Constants.isOnline(this)) {
                    binding.yearlyPlan.setBackgroundResource(R.drawable.monthly_plan_selected_rel_bg)
                    binding.quarterlyPlan.setBackgroundResource(R.drawable.monthly_plan_unselected_rel_bg)
                    binding.monthlyPlan.setBackgroundResource(R.drawable.monthly_plan_unselected_rel_bg)
                    binding.radio2.setBackgroundResource(R.drawable.ic_selected)
                    binding.radio1.setBackgroundResource(R.drawable.ic_radio_bg)
                    binding.radio.setBackgroundResource(R.drawable.ic_radio_bg)
                    binding.check.visibility = View.GONE
                    binding.check1.visibility = View.GONE
                    binding.check2.visibility = View.VISIBLE
                    binding.planTxt.text = getString(R.string.get_yearly_plan)
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}