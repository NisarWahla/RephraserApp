package dzm.dzmedia.repheraserapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mukesh.mukeshotpview.completeListener.MukeshOtpCompleteListener
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.activities.ForgotPasswordActivity
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.ErrorResponse
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityForgotPasswordBinding
import dzm.dzmedia.repheraserapp.databinding.FragmentEnterVerificationCodeForgotPasswordBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EnterVerificationCodeForgotPassword : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentEnterVerificationCodeForgotPasswordBinding
    private lateinit var forgotPasswordActivity: ForgotPasswordActivity
    lateinit var forgotPasswordBinding: ActivityForgotPasswordBinding
    lateinit var otpVariable: String
    private val TAG = EnterVerificationCodeForgotPassword::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEnterVerificationCodeForgotPasswordBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueBtn.isEnabled = false
        binding.continueBtn.setOnClickListener(this)
        binding.resendCode.setOnClickListener(this)
        forgotPasswordActivity = requireActivity() as ForgotPasswordActivity
        forgotPasswordBinding = forgotPasswordActivity.binding

        binding.customOtpView.setOtpCompletionListener(object : MukeshOtpCompleteListener {
            override fun otpCompleteListener(otp: String?) {
                if (otp != null) {
                    otpVariable = otp
                    verifyEmailApiResponce()
                }
                binding.continueBtn.isEnabled = true
            }
        })

        if (binding.customOtpView.text!!.isEmpty()) {
            binding.continueBtn.isEnabled = false
        }
        binding.continueBtn.isEnabled = binding.customOtpView.text!!.length >= 4
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.continueBtn.id) {
            verifyEmailApiResponce()
        } else if (v?.id == binding.resendCode.id) {
            resendCodeApiResponce()
        }
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val email = Prefs.getString(Constants.EMAIL)
        Log.d(TAG, "verifyEmailApiResponce: $email")
        RetrofitAuthenticationClient.AuthClient()
            .verifyOTP(
                "Bearer " + Prefs.getString(Constants.PREF_TOKEN, ""),
                email,
                otpVariable
            )
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>,
                    response: Response<AuthenticationBaseModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: $response")
                        val msg = response.body()?.message
                        Toast.makeText(requireContext(), "$msg", Toast.LENGTH_SHORT)
                            .show()
                        forgotPasswordBinding.viewPager.currentItem = 2
                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn,
                                requireContext()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    fun resendCodeApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val email = Prefs.getString(Constants.EMAIL)
        RetrofitAuthenticationClient.AuthClient()
            .sendOTP(
                email
            )
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>,
                    response: Response<AuthenticationBaseModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: $response")
                        val msg = response.body()?.message
                        forgotPasswordBinding.viewPager.currentItem = 1
                        Prefs.putString(Constants.EMAIL, email)
                        Toast.makeText(requireContext(), "$msg", Toast.LENGTH_SHORT).show()
                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            val errorResponse =
                                App.gson.fromJson(errorJSOn, ErrorResponse::class.java)
                            Toast.makeText(
                                context,
                                "${errorResponse.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}