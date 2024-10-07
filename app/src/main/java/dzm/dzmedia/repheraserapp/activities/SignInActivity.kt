package dzm.dzmedia.repheraserapp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivitySignInBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Constants.Companion.guestEmail
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.GetInfo.GetInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignInActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySignInBinding
    private val TAG = SignInActivity::class.simpleName
    var gson = Gson()
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
    private val EMAIL = "email"
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = Dialog(this@SignInActivity)

        binding.WithOutLogin.setOnClickListener(this)
        binding.moveToSIgnUp.setOnClickListener(this)
        binding.signIpBtn.setOnClickListener(this)
        binding.forgotPassword.setOnClickListener(this)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        /*facebook sdk implementation*/
        callbackManager = CallbackManager.Factory.create()
        binding.logInWithFacebook.setOnClickListener {
            addEventToFirebaseAnalytics("click", "logInWithFacebook")

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"));
        }

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {

                    Prefs.putBoolean(Constants.FROM_FACEBOOK, true)
                    val accessToken = AccessToken.getCurrentAccessToken()
                    /*facebook graph api for user data*/
                    val request = GraphRequest.newMeRequest(
                        accessToken
                    ) { `object`, response ->
                        try {
                            val facebookId = `object`!!.getString("id")
                            val facebookName = `object`!!.getString("name")
                            val password = "12345678"
                            val ip: String = Constants.getIpAddress(this@SignInActivity)
                            Utils.loader(dialog)
                            RetrofitAuthenticationClient.AuthClient().UerRegisterationWithFacebook(
                                facebookName,
                                facebookName,
                                "$facebookId@gmail.com",
                                password,
                                password,
                                ip,
                                facebookId
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
                                        Log.d(TAG, "onResponse:signin ${userModel.data.token}")
                                        Prefs.putString(Constants.UserModel, json)
                                        Prefs.remove(Constants.FreeInfo)
                                        Prefs.putBoolean(Constants.IS_LOGIN, true)
                                        Prefs.putBoolean(Constants.FROM_SOCIAL, true)
                                        Toast.makeText(
                                            this@SignInActivity,
                                            "${userModel.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        if (Prefs.getBoolean(Constants.GUEST_USER_INTENT)) {
                                            startActivity(
                                                Intent(
                                                    this@SignInActivity, PremiumActivity::class.java
                                                )
                                            )
                                            addEventToFirebaseAnalytics("click", "PremiumActivity")
                                            finish()
                                        } else startActivity(
                                            Intent(
                                                this@SignInActivity, MainActivity::class.java
                                            )
                                        )
                                        addEventToFirebaseAnalytics("click", "MainActivity")

                                    } else {
                                        if (response.errorBody() != null) {
                                            val errorJSOn = response.errorBody()?.string()
                                            Constants.errorHandler(
                                                errorJSOn, this@SignInActivity
                                            )
                                        }
                                    }
                                }

                                override fun onFailure(
                                    call: Call<AuthenticationBaseModel>, t: Throwable
                                ) {
                                    dialog.dismiss()
                                    Toast.makeText(
                                        this@SignInActivity, "" + t.message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
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

        binding.logInWithGoogle.setOnClickListener {
            signIn()
            addEventToFirebaseAnalytics("click", "logInWithGoogle")

        }

        binding.passswordEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!TextUtils.isEmpty(binding.passswordEt.text)) {
                    binding.profilePasswordTilLay.endIconDrawable =
                        getDrawable(R.drawable.custom_eye)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    fun ApiResponce(email: String, password: String) {
        Utils.loader(dialog)
        RetrofitAuthenticationClient.AuthClient().UserLogin(email, password)
            .enqueue(object : Callback<AuthenticationBaseModel> {
                override fun onResponse(
                    call: Call<AuthenticationBaseModel>, response: Response<AuthenticationBaseModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d(TAG, "onResponse: $response")
                            var data = response.body()?.`data`
                            Log.d(TAG, "onResponse: $data")
                            /* if (data!!.email == guestEmail) {
                                 Prefs.putBoolean(Constants.GUEST_USER, true)
                                 Prefs.putBoolean(Constants.IS_LOGIN, false)
                             } else {
                                 Prefs.putBoolean(Constants.IS_LOGIN, true)
                                 Prefs.putBoolean(Constants.GUEST_USER, false)
                             }*/
                            val userModel: AuthenticationBaseModel =
                                response.body() as AuthenticationBaseModel
                            val mCurrentUser = userModel.data
                            val json = App.gson.toJson(userModel.data)
                            Prefs.putString(Constants.UserModel, json)
                            Prefs.remove(Constants.FreeInfo)
                            Prefs.putString(Constants.PREF_TOKEN, mCurrentUser.token)
                            Prefs.putBoolean(Constants.IS_LOGIN, true)
                            Prefs.putBoolean(Constants.GUEST_USER, false)
                            /*if (email != "test@rephraser.co") {
                                Toast.makeText(
                                    this@SignInActivity, "${userModel.message}", Toast.LENGTH_SHORT
                                ).show()
                            }*/
                            addEventToFirebaseAnalytics("click", "MainActivity")
                            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn, this@SignInActivity
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@SignInActivity, "" + t.message, Toast.LENGTH_SHORT).show()
                }
            })
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
            Log.d(TAG, "handleResult:aa $e")
            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
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
        val ip: String = Constants.getIpAddress(this@SignInActivity)

        Utils.loader(dialog)
        RetrofitAuthenticationClient.AuthClient().UerRegisterationWithGoogle(
            googleName, googleName, googleEmail, googlePassword, googlePassword, ip, googleId
        ).enqueue(object : Callback<AuthenticationBaseModel> {
            override fun onResponse(
                call: Call<AuthenticationBaseModel>, response: Response<AuthenticationBaseModel>
            ) {
                dialog.dismiss()
                if (response.isSuccessful) {//200
                    val userModel: AuthenticationBaseModel =
                        response.body() as AuthenticationBaseModel
                    val json = App.gson.toJson(userModel.data)
                    Prefs.putString(Constants.PREF_TOKEN, userModel.data.token)
                    Prefs.putString(Constants.UserModel, json)
                    Prefs.putBoolean(Constants.IS_LOGIN, true)
                    Prefs.putBoolean(Constants.FROM_SOCIAL, true)
                    Prefs.remove(Constants.FreeInfo)
                    Toast.makeText(
                        this@SignInActivity, "${userModel.message}", Toast.LENGTH_SHORT
                    ).show()

                    if (Prefs.getBoolean(Constants.GUEST_USER_INTENT)) {
                        addEventToFirebaseAnalytics("click", "PremiumActivity")
                        startActivity(Intent(this@SignInActivity, PremiumActivity::class.java))
                        finish()
                    } else addEventToFirebaseAnalytics("click", "MainActivity")
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))

                } else {
                    if (response.errorBody() != null) {
                        val errorJSOn = response.errorBody()?.string()
                        Constants.errorHandler(
                            errorJSOn, this@SignInActivity
                        )
                    }
                }
            }

            override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(this@SignInActivity, "" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.WithOutLogin.id) {
            GlobalScope.launch(Dispatchers.Main) {
                /*val email = guestEmail
                val password = "12345678"
                ApiResponce(email, password)*/
                if (!Constants.isOnline(this@SignInActivity)) {
                    Toast.makeText(
                        this@SignInActivity, R.string.no_internet, Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (Prefs.getString(Constants.FreeInfo, null) != null) {
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    } else {
                        val ip: String = Constants.getIpAddress(this@SignInActivity)
                        getInfo(ip)
                    }
                }
                addEventToFirebaseAnalytics("click", "WithOutLogin")

            }
        } else if (v?.id == binding.forgotPassword.id) {
            addEventToFirebaseAnalytics("click", "ForgotPasswordActivity")
            startActivity(Intent(this@SignInActivity, ForgotPasswordActivity::class.java))
        } else if (v?.id == binding.moveToSIgnUp.id) {
            addEventToFirebaseAnalytics("click", "SignUpActivity")
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
        } else if (v?.id == binding.signIpBtn.id) {
            hideKeyboard(this)
            if (binding.EmailEt.text.toString().trim() == "") {
                binding.EmailEt.error = getString(R.string.field_cannot_be_empty)
                /*"This field cannot be empty!"*/
            } else if (binding.passswordEt.text.toString().trim() == "") {
                binding.passswordEt.error = getString(R.string.field_cannot_be_empty)
                binding.profilePasswordTilLay.endIconDrawable = null
            } else if (!Constants.isOnline(this)) {
                Log.d(TAG, "onClick: Internet " + Constants.isOnline(this))
                Toast.makeText(
                    this, R.string.no_internet, Toast.LENGTH_SHORT
                ).show()
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    val email = binding.EmailEt.text.toString().trim()
                    val password = binding.passswordEt.text.toString().trim()
                    ApiResponce(email, password)
                }
            }
        }
    }

    fun getInfo(ip: String) {
        val dialog = Dialog(this)
        Utils.loader(dialog)
        dialog.show()
        RetrofitAuthenticationClient.AuthClient()
            .getInfo(
                Constants.FreeUserKey,
                ip
            )
            .enqueue(object : Callback<GetInfoModel> {
                override fun onResponse(
                    call: Call<GetInfoModel>,
                    response: Response<GetInfoModel>
                ) {
                    try {
                        if (dialog != null && dialog.isShowing) dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    if (response.isSuccessful) {
                        val freeInfo: GetInfoModel =
                            response.body() as GetInfoModel
                        val mInfo = freeInfo.data
                        val json = App.gson.toJson(mInfo)
                        Prefs.putString(Constants.FreeInfo, json)
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(
                            this@SignInActivity,
                            "Internet connection or server error\n" +
                                    " plz restart your app.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GetInfoModel>, t: Throwable) {
                    try {
                        dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    Toast.makeText(
                        this@SignInActivity,
                        "Internet connection or server error\n plz restart your app." /*+ t.message*/,
                        Toast.LENGTH_LONG
                    )
                        .show()
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
}