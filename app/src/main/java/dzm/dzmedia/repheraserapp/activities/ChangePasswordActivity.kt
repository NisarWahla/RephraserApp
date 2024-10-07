package dzm.dzmedia.repheraserapp.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.ErrorResponse
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityChangePasswordBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityChangePasswordBinding
    private val TAG = ChangePasswordActivity::class.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener(this)
        binding.resetPassBtn.setOnClickListener(this)

        if (MainActivity.userModel!!.email == Constants.guestEmail) {
            AdManger.loadBannerAds(binding.admobBannerSize, this)
            binding.admobBannerSize.visibility = View.GONE
        }

        binding.passwordET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.passwordET.text)) {
                    binding.profilePasswordTilLay.endIconDrawable =
                        ContextCompat.getDrawable(
                            this@ChangePasswordActivity,
                            R.drawable.custom_eye
                        )
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.newPasswordET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.newPasswordET.text)) {
                    binding.newprofilePasswordTilLay.endIconDrawable =
                        ContextCompat.getDrawable(
                            this@ChangePasswordActivity,
                            R.drawable.custom_eye
                        )
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


        binding.confirmPasswordET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.confirmPasswordET.text)) {
                    binding.newConfirmprofilePasswordTilLay.endIconDrawable =
                        ContextCompat.getDrawable(
                            this@ChangePasswordActivity,
                            R.drawable.custom_eye
                        )
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.btnBack.id) {
            startActivity(Intent(this@ChangePasswordActivity, MainActivity::class.java))
            addEventToFirebaseAnalytics("click", "HistoryActivity")

        } else if (v?.id == binding.resetPassBtn.id) {
            val newPassword = binding.newPasswordET.text.toString()
            val confirmPassword = binding.confirmPasswordET.text.toString()
            if (binding.passwordET.text.toString().trim() == "") {
                binding.passwordET.error =
                    getString(R.string.field_cannot_be_empty)
                binding.profilePasswordTilLay.endIconDrawable = null
            } else if (binding.newPasswordET.text.toString().trim() == "") {
                binding.newPasswordET.error = getString(R.string.field_cannot_be_empty)
                binding.newprofilePasswordTilLay.endIconDrawable = null
            } else if (binding.confirmPasswordET.text.toString().trim() == "") {
                binding.confirmPasswordET.error = getString(R.string.field_cannot_be_empty)
                binding.newConfirmprofilePasswordTilLay.endIconDrawable = null
            } else if (newPassword != confirmPassword) {
                Toast.makeText(
                    this@ChangePasswordActivity,
                    "The Password confirmation does not match.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else
                verifyEmailApiResponce()
        }
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(this)
        Utils.loader(dialog)
        val userId = MainActivity.userModel!!.user_id
        val oldPassword = binding.passwordET.text.toString().trim()
        val newPassword = binding.newPasswordET.text.toString().trim()
        val confirmPassword = binding.confirmPasswordET.text.toString().trim()

        RetrofitAuthenticationClient.AuthClient()
            .changePassword(
                "Bearer " + MainActivity.userModel!!.token,
                userId, oldPassword, newPassword, confirmPassword
            )
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>,
                    response: Response<AuthenticationBaseModel>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: $response")
                        dialog.dismiss()

                        val msg = response.body()?.message
                        Toast.makeText(this@ChangePasswordActivity, "$msg", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(
                            Intent(
                                this@ChangePasswordActivity,
                                MainActivity::class.java
                            )
                        )
                        addEventToFirebaseAnalytics("click", "MainActivity")
                    } else {
                        dialog.dismiss()
                        val errorJSOn = response.errorBody()?.string()
                        Constants.errorHandler(
                            errorJSOn, this@ChangePasswordActivity
                        )
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@ChangePasswordActivity, "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}