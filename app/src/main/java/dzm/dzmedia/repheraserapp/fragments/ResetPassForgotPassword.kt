package dzm.dzmedia.repheraserapp.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.SignInActivity
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.ErrorResponse
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.FragmentResetPassForgotPasswordBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPassForgotPassword : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentResetPassForgotPasswordBinding
    private val TAG = ResetPassForgotPassword::class.simpleName
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResetPassForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resetPassBtn.setOnClickListener(this@ResetPassForgotPassword)

        binding.passET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.passET.text)) {
                    binding.passwordTilLay.endIconDrawable =
                        requireContext().getDrawable(R.drawable.custom_eye)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.confirmPassET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.confirmPassET.text)) {
                    binding.confirmPasswordTilLay.endIconDrawable =
                        requireContext().getDrawable(R.drawable.custom_eye)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

    }

    override fun onClick(v: View?) {
        if (v?.id == binding.resetPassBtn.id) {
            val newPassword = binding.passET.text.toString()
            val confirmPassword = binding.confirmPassET.text.toString()
            if (binding.passET.text.toString().trim() == "") {
                binding.passET.error = "This field cannot be empty!"
                binding.passwordTilLay.endIconDrawable = null

            } else if (binding.confirmPassET.text.toString().trim() == "") {
                binding.confirmPassET.error = "This field cannot be empty!"
                binding.confirmPasswordTilLay.endIconDrawable = null
            } else if (newPassword != confirmPassword) {
                Toast.makeText(
                    requireActivity(),
                    "The Password confirmation does not match.",
                    Toast.LENGTH_SHORT
                ).show()
            } else
                verifyEmailApiResponce()
        }
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val email = Prefs.getString(Constants.EMAIL)
        val oldPassword = binding.passET.text.toString().trim()
        val newPassword = binding.confirmPassET.text.toString().trim()

        RetrofitAuthenticationClient.AuthClient()
            .resetPassword(
                email, oldPassword, newPassword
            )
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>,
                    response: Response<AuthenticationBaseModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        val msg = response.body()?.message

                        Toast.makeText(requireContext(), "$msg", Toast.LENGTH_SHORT)
                            .show()
                        requireContext().startActivity(
                            Intent(
                                requireContext(),
                                SignInActivity::class.java
                            )
                        )
                        requireActivity().finishAffinity()
                    } else {
                        if (response.errorBody() != null) {
                            Toast.makeText(
                                context,
                                "Validation error",
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