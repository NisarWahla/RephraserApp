package dzm.dzmedia.repheraserapp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Password
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivitySignUpBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = SignUpActivity::class.simpleName
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var ip: String
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 100
    lateinit var googleName: String
    lateinit var googleEmail: String
    lateinit var googlePhotoUrl: String
    lateinit var googleId: String
    lateinit var googleGivenName: String
    lateinit var googleIdToken: String
    lateinit var googlePassword: String
    lateinit var callbackManager: CallbackManager

    @Email
    lateinit var email: EditText

    @NotEmpty(trim = true)
    //@Length(min = 3, max = 30, message = "Length up to 30 character")
    lateinit var fName: EditText

    @NotEmpty(trim = true)
    //@Length(min = 3, max = 30, message = "Length up to 30 character")
    lateinit var lName: EditText

    @Password
    lateinit var passowrd: EditText

    @ConfirmPassword
    lateinit var cPassowrd: EditText
    lateinit var validator: Validator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        validator = Validator(this)
        email = binding.profileEmailEt
        fName = binding.firstNameEt
        lName = binding.lastNameEt
        passowrd = binding.profilePasswordEt
        cPassowrd = binding.profileNewPasswordEt

        validator.setValidationListener(object : Validator.ValidationListener {
            override fun onValidationSucceeded() {
                if (binding.firstNameEt.text.toString().trim() == "") {
                    binding.firstNameEt.error = getString(R.string.field_cannot_be_empty)
                } else if (binding.lastNameEt.text.toString().trim() == "") {
                    binding.lastNameEt.error = getString(R.string.field_cannot_be_empty)
                } else if (binding.profileEmailEt.text.toString().trim() == "") {
                    binding.profileEmailEt.error = getString(R.string.field_cannot_be_empty)
                } else if (binding.profilePasswordEt.text.toString().trim() == "") {
                    binding.profilePasswordEt.error = getString(R.string.field_cannot_be_empty)
                } else if (binding.profilePasswordEt.length() < 8) {
                    Toast.makeText(
                        this@SignUpActivity,
                        R.string.password_must_be_8char,
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.profileNewPasswordEt.text.toString().trim() == "") {
                    binding.profileNewPasswordEt.error = getString(R.string.field_cannot_be_empty)
                } else if (binding.profileNewPasswordEt.length() < 8) {
                    Toast.makeText(
                        this@SignUpActivity,
                        R.string.password_must_be_8char,
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!isOnline(this@SignUpActivity)) {
                    Toast.makeText(
                        this@SignUpActivity,
                        R.string.no_internet,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        ApiResponce()
                    }
                }
            }

            override fun onValidationFailed(errors: MutableList<ValidationError>) {
                for (error in errors) {
                    val view = error.view
                    val message = error.getCollatedErrorMessage(this@SignUpActivity)
                    if (view is EditText) {
                        view.error = message
                    } else {
                        Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

        /*google sdk implementation*/
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        /*facebook sdk implementation*/
        callbackManager = CallbackManager.Factory.create()
        binding.logInWithFacebook.setOnClickListener {
            addEventToFirebaseAnalytics("click", "logInWithFacebook")
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("public_profile"));
        }

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {

                    val accessToken = AccessToken.getCurrentAccessToken()
                    /*facebook graph api for user data*/
                    val request = GraphRequest.newMeRequest(
                        accessToken
                    ) { `object`, _ ->


                        val facebookId = `object`!!.getString("id")
                        val facebookName = `object`!!.getString("name")
                        val facebookFName = `object`!!.getString("first_name")
                        val facebookLName = `object`!!.getString("last_name")
                        val email = `object`!!.getString("id") + "@facebook.com"
                        Log.d(TAG, "onSuccess: signup $email")
                        val password = "12345678"

                        val dialog = Dialog(this@SignUpActivity)
                        Utils.loader(dialog)
                        RetrofitAuthenticationClient.AuthClient()
                            .UerRegisterationWithFacebook(
                                facebookName,
                                facebookName,
                                "$facebookId@facebook.com",
                                password,
                                password,
                                ip, facebookId
                            ).enqueue(object : Callback<AuthenticationBaseModel> {
                                override fun onResponse(
                                    call: Call<AuthenticationBaseModel>,
                                    response: Response<AuthenticationBaseModel>
                                ) {
                                    dialog.dismiss()
                                    if (response.isSuccessful) {//200
                                        val userModel: AuthenticationBaseModel =
                                            response.body() as AuthenticationBaseModel
                                        val json = App.gson.toJson(userModel.data)
                                        Prefs.putString(Constants.PREF_TOKEN, userModel.data.token)
                                        Prefs.putString(Constants.UserModel, json)
                                        Prefs.remove(Constants.FreeInfo)
                                        Prefs.putBoolean(Constants.IS_LOGIN, true)
                                        Prefs.putBoolean(Constants.FROM_SOCIAL, true)
                                        Prefs.putBoolean(Constants.GUEST_USER, false)
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "${userModel.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        if (Prefs.getBoolean(Constants.GUEST_USER_INTENT)) {
                                            startActivity(
                                                Intent(
                                                    this@SignUpActivity,
                                                    PremiumActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else
                                            startActivity(
                                                Intent(
                                                    this@SignUpActivity,
                                                    MainActivity::class.java
                                                )
                                            )
                                    } else {
                                        if (response.errorBody() != null) {
                                            val errorJSOn = response.errorBody()?.string()
                                            Constants.errorHandler(
                                                errorJSOn,
                                                this@SignUpActivity
                                            )
                                        }
                                    }
                                }

                                override fun onFailure(
                                    call: Call<AuthenticationBaseModel>,
                                    t: Throwable
                                ) {
                                    dialog.dismiss()
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "" + t.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,link,first_name,last_name")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                }

                override fun onError(exception: FacebookException) {
                }
            })

        binding.profileNewPasswordEt.setOnFocusChangeListener { _, hasFocus ->
            val color = if (hasFocus) Color.BLUE else Color.GRAY
            binding.profileNewPasswordTilLay.setStartIconTintList(ColorStateList.valueOf(color))
        }

        binding.login.setOnClickListener(this)
        binding.signupBtn.setOnClickListener(this)
        binding.privacyPolicyTxt.setOnClickListener(this)
        binding.termsAndConditionsTxt.setOnClickListener(this)
        binding.logInWithGoogle.setOnClickListener(this)

        //User Device IP Address...
        val wm: WifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)


        binding.profilePasswordEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.profilePasswordEt.text)) {
                    binding.profilePasswordTilLay.endIconDrawable =
                        getDrawable(R.drawable.custom_eye)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.profileNewPasswordEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.profileNewPasswordEt.text)) {
                    binding.profilePasswordTilLay.endIconDrawable =
                        getDrawable(R.drawable.custom_eye)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    override fun onClick(v: View) {
        if (v.id == binding.login.id) {
            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            addEventToFirebaseAnalytics("click", "SignInActivity")
            finish()
        } else if (v.id == binding.signupBtn.id) {
            hideKeyboard(this)
            /*validator.validate()*/
            /*if (BuildConfig.DEBUG) {
                binding.firstNameEt.setText("Random")
                binding.lastNameEt.setText("Random")
                binding.profileEmailEt.setText("iamRandom@facebook.com")
                binding.profilePasswordEt.setText("12345678")
                binding.profileNewPasswordEt.setText("12345678")
            }*/

            if (binding.firstNameEt.text.toString().trim() == "") {
                binding.firstNameEt.error = getString(R.string.field_cannot_be_empty)
            } else if (binding.lastNameEt.text.toString().trim() == "") {
                binding.lastNameEt.error = getString(R.string.field_cannot_be_empty)
            } else if (binding.profileEmailEt.text.toString().trim() == "") {
                binding.profileEmailEt.error = getString(R.string.field_cannot_be_empty)
            } else if (binding.profilePasswordEt.text.toString().trim() == "") {
                binding.profilePasswordEt.error = getString(R.string.field_cannot_be_empty)
                binding.profilePasswordTilLay.endIconDrawable = null
            } else if (binding.profilePasswordEt.length() < 8) {
                Toast.makeText(
                    this,
                    R.string.password_must_be_8char,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.profileNewPasswordEt.text.toString().trim() == "") {
                binding.profileNewPasswordEt.error = getString(R.string.field_cannot_be_empty)
                binding.profileNewPasswordTilLay.endIconDrawable = null

            } else if (binding.profileNewPasswordEt.length() < 8) {
                Toast.makeText(
                    this,
                    R.string.password_must_be_8char,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!isOnline(this)) {
                Toast.makeText(
                    this,
                    R.string.no_internet,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    ApiResponce()
                }
            }
        } else if (v?.id == binding.privacyPolicyTxt.id) {
            val url = "https://www.dzinemedia.com/privacy-policy"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        } else if (v?.id == binding.termsAndConditionsTxt.id) {
            val url = "https://www.dzinemedia.com/terms-and-conditions"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        } else if (v?.id == binding.logInWithGoogle.id) {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
                Log.d(TAG, "handleResult:aa $account")
            }
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount) {
        googleName = account.displayName.toString()
        googleEmail = account.email.toString()
        googlePhotoUrl = account.photoUrl.toString()
        googleId = account.id.toString()
        googleGivenName = account.givenName.toString()
        googleIdToken = account.idToken.toString()
        googlePassword = "12345678"

        val dialog = Dialog(this@SignUpActivity)
        Utils.loader(dialog)
        RetrofitAuthenticationClient.AuthClient()
            .UerRegisterationWithGoogle(
                googleName,
                googleName,
                googleEmail,
                googlePassword,
                googlePassword,
                ip, googleId
            )
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>,
                    response: Response<AuthenticationBaseModel>
                ) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "$googleId",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(TAG, "UpdateUI:id $googleId")

                    dialog.dismiss()
                    if (response.isSuccessful) {//200
                        val userModel: AuthenticationBaseModel =
                            response.body() as AuthenticationBaseModel
                        val json = App.gson.toJson(userModel.data)
                        Prefs.putString(Constants.PREF_TOKEN, userModel.data.token)
                        Prefs.putString(Constants.UserModel, json)
                        Prefs.remove(Constants.FreeInfo)
                        Prefs.putBoolean(Constants.IS_LOGIN, true)
                        Prefs.putBoolean(Constants.GUEST_USER, false)
                        Prefs.putBoolean(Constants.FROM_SOCIAL, true)
                        Prefs.putString(Constants.GOOGLE_EMAIL, googleEmail)
                        Prefs.putString(Constants.GOOGLE_PASSWORD, googlePassword)
                        Toast.makeText(
                            this@SignUpActivity,
                            "${userModel.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (Prefs.getBoolean(Constants.GUEST_USER_INTENT)) {
                            startActivity(Intent(this@SignUpActivity, PremiumActivity::class.java))
                            addEventToFirebaseAnalytics("click", "PremiumActivity")
                            finish()
                        } else
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                        addEventToFirebaseAnalytics("click", "MainActivity")

                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn,
                                this@SignUpActivity
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@SignUpActivity, "" + t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    fun ApiResponce() {
        val dialog = Dialog(this@SignUpActivity)
        Utils.loader(dialog)
        val firstName = binding.firstNameEt.text.toString().trim()
        val lastName = binding.lastNameEt.text.toString().trim()
        val email = binding.profileEmailEt.text.toString().trim()
        val password = binding.profilePasswordEt.text.toString().trim()
        val passwordConfirmation = binding.profileNewPasswordEt.text.toString().trim()

        RetrofitAuthenticationClient.AuthClient()
            .UerRegisteration(
                firstName,
                lastName,
                email,
                password,
                passwordConfirmation,
                ip
            )
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>,
                    response: Response<AuthenticationBaseModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {//200
                        val userModel: AuthenticationBaseModel =
                            response.body() as AuthenticationBaseModel
                        val json = App.gson.toJson(userModel.data)
                        Prefs.putString(Constants.PREF_TOKEN, userModel.data.token)
                        Prefs.putString(Constants.UserModel, json)
                        Prefs.remove(Constants.FreeInfo)
                        Prefs.putBoolean(Constants.IS_LOGIN, true)
                        Prefs.putBoolean(Constants.GUEST_USER, false)
                        Toast.makeText(
                            this@SignUpActivity,
                            "${userModel.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (Prefs.getBoolean(
                                Constants.GUEST_USER_INTENT
                            )
                        ) {
                            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                            addEventToFirebaseAnalytics("click", "SignInActivity")
                            finishAffinity()
                        } else
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                        addEventToFirebaseAnalytics("click", "MainActivity")
                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn,
                                this@SignUpActivity
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@SignUpActivity, "" + t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}