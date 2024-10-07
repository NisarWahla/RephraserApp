package dzm.dzmedia.repheraserapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityPaymentBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentActivity : AppCompatActivity() {
    lateinit var binding: ActivityPaymentBinding
    var userModel: DataObjectModel? = null
    var url = ""
    var value = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = Prefs.getString(Constants.UserModel, "")
        userModel = App.gson.fromJson(json, DataObjectModel::class.java)
        binding.webView.webViewClient = MyWebViewClient(this)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        // if you want to enable zoom feature
        binding.webView.settings.setSupportZoom(true)
        val intent = intent
        if (intent.extras != null) {
            value = intent.getIntExtra("plan", 0)
            when (value) {
                1 -> {
                    addEventToFirebaseAnalytics("click", "webView")
                    binding.webView.loadUrl(getString(R.string.monthly) + "" + userModel!!.user_id)

                }
                2 -> {
                    addEventToFirebaseAnalytics("click", "webView")
                    binding.webView.loadUrl(getString(R.string.quarterly) + "" + userModel!!.user_id)
                }
                else -> {
                    addEventToFirebaseAnalytics("click", "webView")
                    binding.webView.loadUrl(getString(R.string.yearly) + "" + userModel!!.user_id)
                }
            }
        }
    }

    class MyWebViewClient internal constructor(private val activity: Activity) : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url: String = request?.url.toString();
            view?.loadUrl(url)
            Log.d("URL", "shouldOverrideUrlLoading: $url")
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("URL", "onPageFinished: $url")
            if (url!!.contains("https://www.rephraser.co/thank-you")) {
                userProfileApiResponce()
            }
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            webView.loadUrl(url)
            return true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            Toast.makeText(activity, "Got Error! $error", Toast.LENGTH_SHORT).show()
        }

        fun userProfileApiResponce() {
            val user_id = MainActivity.userModel?.user_id
            val token = "Bearer " + Prefs.getString(Constants.PREF_TOKEN, "")
            RetrofitAuthenticationClient.AuthClient()
                .userProfile(
                    token,
                    user_id
                )
                .enqueue(object : Callback<AuthenticationBaseModel> {
                    override fun onResponse(
                        call: Call<AuthenticationBaseModel>,
                        response: Response<AuthenticationBaseModel>
                    ) {
                        if (response.isSuccessful) {
                            val userModel: AuthenticationBaseModel =
                                response.body() as AuthenticationBaseModel
                            val json = App.gson.toJson(userModel.data)
                            Prefs.putString(Constants.UserModel, json)
                            Prefs.putBoolean(Constants.IS_LOGIN, true)
                            activity.startActivity(Intent(activity, MainActivity::class.java))
                            addEventToFirebaseAnalytics("click", "HistoryActivity")
                        } else {
                            Toast.makeText(
                                activity,
                                R.string.no_internet,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                        Toast.makeText(activity, "" + t.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }
    }
}