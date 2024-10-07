package dzm.dzmedia.repheraserapp.activities

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityUpdateProfileBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            val firstName = MainActivity.userModel!!.firstName
            val lastName = MainActivity.userModel!!.last_name
            val email = MainActivity.userModel!!.email
            val contactNumber = MainActivity.userModel!!.contact_no
            val fullName = "$firstName $lastName"

            binding.userName.text = fullName
            binding.firstNameEt.setText(firstName)
            binding.lastNameEt.setText(lastName)
            binding.emailEt.setText(email)
            binding.mobileEt.setText(contactNumber)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.updateBtn.setOnClickListener {
            verifyEmailApiResponce()
        }

        binding.backBtn.setOnClickListener {
            super.onBackPressed()
        }
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(this@UpdateProfileActivity)
        Utils.loader(dialog)
        val token = "Bearer " + MainActivity.userModel!!.token
        val userId = MainActivity.userModel!!.user_id
        var firstName = binding.firstNameEt.text.toString().trim()
        var lastName = binding.lastNameEt.text.toString().trim()
        var mobile = binding.mobileEt.text.toString().trim()

        RetrofitAuthenticationClient.AuthClient()
            .userUpdate(
                token,
                userId, firstName, lastName, mobile
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
                        val mCurrentUser = userModel.data
                        val json = App.gson.toJson(userModel.data)
                        Prefs.putString(Constants.UserModel, json)
                        Prefs.putString(Constants.PREF_TOKEN, mCurrentUser.token)
                        Prefs.putBoolean(Constants.IS_LOGIN, true)

                        Toast.makeText(this@UpdateProfileActivity, "$msg", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(
                            Intent(
                                this@UpdateProfileActivity,
                                ProfileActivity::class.java
                            )
                        )
                        addEventToFirebaseAnalytics("click", "ProfileActivity")

                        finish()
                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn,
                                this@UpdateProfileActivity
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@UpdateProfileActivity, "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}