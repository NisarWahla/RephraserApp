package dzm.dzmedia.repheraserapp.activities

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mukesh.mukeshotpview.completeListener.MukeshOtpCompleteListener
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityVerifyEmailBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyEmailBinding

    lateinit var otpVariable: String
    private val TAG = VerifyEmailActivity::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.verifyBtn.isEnabled = false

        binding.customOtpView.setOtpCompletionListener(object : MukeshOtpCompleteListener {
            override fun otpCompleteListener(otp: String?) {
                if (otp != null) {
                    otpVariable = otp
                }
                binding.verifyBtn.isEnabled = true
                verifyEmailApiResponce()
            }
        })

        binding.verifyBtn.setOnClickListener { verifyEmailApiResponce() }

        binding.backTOLoginBtn.setOnClickListener { super.onBackPressed() }
    }

    fun verifyEmailApiResponce() {
        try {
            val dialog = Dialog(this@VerifyEmailActivity)
            Utils.loader(dialog)
            val userId = MainActivity.userModel!!.user_id
            Log.d(TAG, "UserID:id $userId")
            Log.d(TAG, "verifyEmailApiResponce: $otpVariable")

            Log.d(TAG, "verifyEmailApiResponce: ${MainActivity.userModel!!.token}")
            RetrofitAuthenticationClient.AuthClient()
                .verifyEmail(
                    "Bearer " + MainActivity.userModel!!.token,
                    otpVariable,
                    userId
                )
                .enqueue(object : Callback<AuthenticationBaseModel> {
                    override fun onResponse(
                        call: Call<AuthenticationBaseModel>,
                        response: Response<AuthenticationBaseModel>
                    ) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            Log.d(TAG, "onResponse: $response")
                            dialog.dismiss()
                            val userModel: AuthenticationBaseModel =
                                response.body() as AuthenticationBaseModel
                            val json = App.gson.toJson(userModel.data)
                            Prefs.putString(Constants.PREF_TOKEN, userModel.data.token)
                            Prefs.putString(Constants.UserModel, json)
                            val msg =
                                userModel.message
                            Toast.makeText(this@VerifyEmailActivity, "$msg", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(
                                Intent(
                                    this@VerifyEmailActivity,
                                    MainActivity::class.java

                                )
                            )
                            addEventToFirebaseAnalytics("click", "MainActivity")
                            finish()
                        } else {
                            if (response.errorBody() != null) {
                                val errorJSOn = response.errorBody()?.string()
                                Constants.errorHandler(
                                    errorJSOn,
                                    this@VerifyEmailActivity
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                        dialog.dismiss()
                        Toast.makeText(this@VerifyEmailActivity, "" + t.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}