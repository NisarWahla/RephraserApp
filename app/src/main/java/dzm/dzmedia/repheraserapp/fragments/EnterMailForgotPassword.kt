package dzm.dzmedia.repheraserapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.activities.ForgotPasswordActivity
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.ErrorResponse
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityForgotPasswordBinding
import dzm.dzmedia.repheraserapp.databinding.FragmentEnterMailForgotPasswordBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EnterMailForgotPassword : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentEnterMailForgotPasswordBinding
    private lateinit var forgotPasswordActivity: ForgotPasswordActivity
    lateinit var forgotPasswordBinding: ActivityForgotPasswordBinding
    private lateinit var email: String
    private val TAG = EnterMailForgotPassword::class.simpleName
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEnterMailForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.getVerificationCodeBtn.setOnClickListener(this@EnterMailForgotPassword)
        forgotPasswordActivity = requireActivity() as ForgotPasswordActivity
        forgotPasswordBinding = forgotPasswordActivity.binding
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.getVerificationCodeBtn.id) {
            if (binding.enterMailET.text.toString().isEmpty()) {
                binding.enterMailET.error = "field cann't be empty"
            } else {
                verifyEmailApiResponce()
            }
        }
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val email = binding.enterMailET.text.toString().trim()
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
                            Toast.makeText(requireContext(), "Email not exist!", Toast.LENGTH_SHORT)
                                .show()
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