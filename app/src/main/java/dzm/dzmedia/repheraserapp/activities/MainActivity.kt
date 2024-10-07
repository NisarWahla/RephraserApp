package dzm.dzmedia.repheraserapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.aemerse.iap.IapConnector
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.BuildConfig
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.adapter.BottomNavigationAdapter
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityMainBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.in_app_update.UpdateManager
import dzm.dzmedia.repheraserapp.in_app_update.UpdateManagerConstant
import dzm.dzmedia.repheraserapp.model.GetInfo.Data
import dzm.dzmedia.repheraserapp.model.GetInfo.GetInfoModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MainActivity : AppCompatActivity(), View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private var freeJson: String? = null
    private var paidJson: String? = null
    private val TAG = MainActivity::class.java.simpleName
    lateinit var activityMainBinding: ActivityMainBinding
    var mUpdateManager: UpdateManager? = null
    lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationAdapter: BottomNavigationAdapter
    var generated: Boolean = false
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var ip: String
    private lateinit var iapConnector: IapConnector
    private lateinit var dialog: Dialog
    private var androidId = ""


    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        dialog = Dialog(this@MainActivity)
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        if (Constants.isOnline(this@MainActivity)) {
            //User Device IP Address...
            ip = Constants.getIpAddress(this@MainActivity)
            //free user without login
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                getInfo(savedInstanceState)
            } else {
                //user with login
                userProfileApiResponce(savedInstanceState)
                /*if (!Prefs.getBoolean("subscription", false)) {
                    cancelRequest(savedInstanceState)
                } else {
                    userProfileApiResponce(savedInstanceState)
                }*/
            }
        } else {
            Toast.makeText(
                this@MainActivity,
                R.string.no_internet,
                Toast.LENGTH_SHORT
            ).show()
        }

       /* if (Prefs.getBoolean(Constants.IS_LOGIN, false)) {
            //paid
            paidJson = Prefs.getString(Constants.UserModel, "")
            userModel = App.gson.fromJson(paidJson, DataObjectModel::class.java)
        } else {
            //free
            freeJson = Prefs.getString(Constants.FreeInfo, "")
            freeInfo = App.gson.fromJson(freeJson, Data::class.java)
        }*/

        AdManger.loadBannerAds(activityMainBinding.admobBannerSize, this)
        AdManger.loadInterstial(this)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mUpdateManager = UpdateManager.Builder(this).mode(UpdateManagerConstant.IMMEDIATE)
        mUpdateManager!!.start()

        bottomNavigationAdapter = BottomNavigationAdapter(this)
        activityMainBinding.pager.adapter = bottomNavigationAdapter
        activityMainBinding.pager.isUserInputEnabled = false

        activityMainBinding.premiumBtn.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
            addEventToFirebaseAnalytics("click", "premium_activity")
        }

        activityMainBinding.rephrasrRLay1.setOnClickListener {
            RephraserIconSelected()
            GrammerCheckerNormal()
            SummarizeNormal()
            showHomeScreen()
            addEventToFirebaseAnalytics("click", "paraphraser")
        }

        activityMainBinding.grammerCheckerRLay3.setOnClickListener {
            RephraserNormal()
            GrammerCheckerIconSelected()
            SummarizeNormal()
            activityMainBinding.pager.currentItem = 1
            addEventToFirebaseAnalytics("click", "grammer_checker")
        }

        activityMainBinding.summarizeRLay4.setOnClickListener {
            RephraserNormal()
            GrammerCheckerNormal()
            SummarizeIconSelected()
            activityMainBinding.pager.currentItem = 2
            addEventToFirebaseAnalytics("click", "summarizer")
        }

        activityMainBinding.mainNavigationView.setNavigationItemSelectedListener(this@MainActivity)
        activityMainBinding.mainNavigationView.itemIconTintList = null
        activityMainBinding.navOpenButton.setOnClickListener(this)
        activityMainBinding.userProfile.setOnClickListener(this)
    }

    fun intializationAttributes(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            RephraserIconSelected()
            GrammerCheckerNormal()
            SummarizeNormal()
            showHomeScreen()
            addEventToFirebaseAnalytics("oncreate_main", "paraphraser")
        }
        if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
            hidemProfile()
            hidemHistory()
            hidemChangePassword()
            hidemLogoutButton()
            //hidemSubscriptionPlan()
            activityMainBinding.premiumBtn.visibility = View.VISIBLE
            activityMainBinding.admobBannerSize.visibility = View.VISIBLE
        } else {

            //check if user subscribed
            if (!userModel!!.subscribed && !userModel!!.verified) {
                activityMainBinding.userProfile.visibility = View.GONE
                activityMainBinding.premiumBtn.visibility = View.VISIBLE
                //hidemSubscriptionPlan()
            } else if (!userModel!!.subscribed && userModel!!.verified && userModel!!.subscription == "3 Days Trial") {
                activityMainBinding.userProfile.visibility = View.VISIBLE
                activityMainBinding.premiumBtn.visibility = View.GONE
            } else if (!userModel!!.subscribed && userModel!!.verified) {
                activityMainBinding.userProfile.visibility = View.GONE
                activityMainBinding.premiumBtn.visibility = View.VISIBLE
                //hidemSubscriptionPlan()
            } else {
                activityMainBinding.userProfile.visibility = View.VISIBLE
                activityMainBinding.premiumBtn.visibility = View.GONE
            }

            if (userModel!!.email == Constants.guestEmail) {
                hidemProfile()
                hidemHistory()
                hidemChangePassword()
                hidemLogoutButton()
                //hidemSubscriptionPlan()
                activityMainBinding.admobBannerSize.visibility = View.VISIBLE
            }
            if (userModel!!.email != Constants.guestEmail) {
                hidemLoginSignup()
            }

            if (userModel!!.word_limit == 250)
                activityMainBinding.admobBannerSize.visibility = View.VISIBLE
            else
                activityMainBinding.admobBannerSize.visibility = View.GONE


            if (Prefs.getBoolean(Constants.FROM_SOCIAL)) {
                hidemChangePassword()
            }
        }
    }

    private fun VerifyEmailPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.verify_email_dialog)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnCross = dialog.findViewById<ImageView>(R.id.cross_btn)
        val btnVerifyNow = dialog.findViewById<MaterialButton>(R.id.verifyNow)
        btnCross.setOnClickListener {
            dialog.dismiss()
        }
        btnVerifyNow.setOnClickListener {
            //verifyEmailApiResponce()
        }
        dialog.show()
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(this)
        Utils.loader(dialog)
        val email = userModel!!.email
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
                        val msg = response.body()?.message
                        Toast.makeText(this@MainActivity, "$msg", Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(
                                this@MainActivity,
                                VerifyEmailActivity::class.java
                            )
                        )
                        addEventToFirebaseAnalytics("click", "VerifyEmailActivity")

                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn,
                                this@MainActivity
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity, "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    fun showHomeScreen() {
        activityMainBinding.pager.currentItem = 0
        addEventToFirebaseAnalytics("click", "paraphraser")
    }

    fun RephraserIconSelected() {
        activityMainBinding.rephraserFragment.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.ic_rephraser_selected
            )
        )
        activityMainBinding.rephraserTxt.setTextColor(Color.parseColor("#4D7AFF"))
        activityMainBinding.bullet1.setTextColor(Color.parseColor("#4d7aff"))
    }

    fun GrammerCheckerIconSelected() {
        activityMainBinding.grammerCheckerFragment.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.ic_grammer_checker_selected
            )
        )
        activityMainBinding.grammarCheckerTxt.setTextColor(Color.parseColor("#4D7AFF"))
        activityMainBinding.bullet3.setTextColor(Color.parseColor("#4d7aff"))
    }

    fun SummarizeIconSelected() {
        activityMainBinding.summarizeFragment.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.ic_semmarize_selected
            )
        )
        activityMainBinding.summarizeTxt.setTextColor(Color.parseColor("#4D7AFF"))
        activityMainBinding.bullet4.setTextColor(Color.parseColor("#4d7aff"))
    }

    fun RephraserNormal() {
        activityMainBinding.rephraserFragment.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.ic_rephraser_unselected
            )
        )
        activityMainBinding.rephraserTxt.setTextColor(Color.parseColor("#000000"))
        activityMainBinding.bullet1.setTextColor(Color.parseColor("#000000"))
    }

    fun GrammerCheckerNormal() {
        activityMainBinding.grammerCheckerFragment.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.ic_grammer_checker_unslected
            )
        )
        activityMainBinding.grammarCheckerTxt.setTextColor(Color.parseColor("#000000"))
        activityMainBinding.bullet3.setTextColor(Color.parseColor("#000000"))
    }

    fun SummarizeNormal() {
        activityMainBinding.summarizeFragment.setImageDrawable(
            ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.ic_summarize_unselected
            )
        )
        activityMainBinding.summarizeTxt.setTextColor(Color.parseColor("#000000"))
        activityMainBinding.bullet4.setTextColor(Color.parseColor("#000000"))
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.clear()
    }

    override fun onBackPressed() {
        if (activityMainBinding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            activityMainBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else
            backPressedDialogue()
    }

    private fun backPressedDialogue() {
        var isStarSelected = 5
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.rating_exit_dialog)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val rateUs = dialog.findViewById<MaterialButton>(R.id.rate_us_on_google)
        val exit = dialog.findViewById<MaterialButton>(R.id.exit)
        val crossIcon = dialog.findViewById<ImageView>(R.id.cancel_dialog)

        val first = dialog.findViewById<ImageView>(R.id.firstStar)
        val second = dialog.findViewById<ImageView>(R.id.secondStar)
        val third = dialog.findViewById<ImageView>(R.id.thirdStar)
        val forth = dialog.findViewById<ImageView>(R.id.fourthStar)
        val fifth = dialog.findViewById<ImageView>(R.id.fifthStar)
        // set image Resource
        first.setImageResource(R.drawable.ylw_star)
        second.setImageResource(R.drawable.ylw_star)
        third.setImageResource(R.drawable.ylw_star)
        forth.setImageResource(R.drawable.ylw_star)
        fifth.setImageResource(R.drawable.ylw_star)
        first.setOnClickListener {
            isStarSelected = 1
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.given_star)
            third.setImageResource(R.drawable.given_star)
            forth.setImageResource(R.drawable.given_star)
            fifth.setImageResource(R.drawable.given_star)
        }
        second.setOnClickListener {
            isStarSelected = 2
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.given_star)
            forth.setImageResource(R.drawable.given_star)
            fifth.setImageResource(R.drawable.given_star)
        }
        third.setOnClickListener {
            isStarSelected = 3
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.ylw_star)
            forth.setImageResource(R.drawable.given_star)
            fifth.setImageResource(R.drawable.given_star)
        }
        forth.setOnClickListener {
            isStarSelected = 4
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.ylw_star)
            forth.setImageResource(R.drawable.ylw_star)
            fifth.setImageResource(R.drawable.given_star)
        }
        fifth.setOnClickListener {
            isStarSelected = 5
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.ylw_star)
            forth.setImageResource(R.drawable.ylw_star)
            fifth.setImageResource(R.drawable.ylw_star)
        }
        rateUs.setOnClickListener {
            dialog.dismiss()
            addEventToFirebaseAnalytics("click", "rateUs")
            if (isStarSelected == 1 || isStarSelected == 2 || isStarSelected == 3) {
                Utils.giveReview(this)
            } else {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                startActivity(openURL)
            }
        }
        crossIcon.setOnClickListener {
            dialog.dismiss()
        }
        exit.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }
        dialog.show()
    }

    override fun onClick(v: View?) {
        if (v?.id == activityMainBinding.navOpenButton.id) {
            hideKeyboard(this)
            activityMainBinding.mainDrawerLayout.openDrawer(GravityCompat.START)
            addEventToFirebaseAnalytics("click", "navDrawer")
        } else if (v?.id == activityMainBinding.userProfile.id) {
            if (userModel!!.email != Constants.guestEmail) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        ProfileActivity::class.java
                    )
                )
                addEventToFirebaseAnalytics("click", "ProfileActivity")
            } else {
                startActivity(
                    Intent(
                        this@MainActivity,
                        SignInActivity::class.java
                    )
                )
                addEventToFirebaseAnalytics("click", "SignInActivity")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mLoginSignup -> {
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                addEventToFirebaseAnalytics("click", "SignInActivity")
            }
            R.id.mSubscriptionPlan -> {
                startActivity(Intent(this@MainActivity, SubscriptionPlanActivity::class.java))
                addEventToFirebaseAnalytics("click", "SubscriptionPlanActivity")
            }
            R.id.mSavedFiles -> {
                startActivity(Intent(this@MainActivity, SavedFilesActivity::class.java))
                addEventToFirebaseAnalytics("click", "SavedFilesActivity")
            }

            R.id.mProfile -> {
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                addEventToFirebaseAnalytics("click", "ProfileActivity")
            }

            R.id.mChangePassword -> {
                addEventToFirebaseAnalytics("click", "ChangePasswordActivity")
                startActivity(Intent(this@MainActivity, ChangePasswordActivity::class.java))
            }

            R.id.mHistory -> {
                addEventToFirebaseAnalytics("click", "HistoryActivity")
                startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
            }

            R.id.mPrivacyPolicy -> {
                addEventToFirebaseAnalytics("click", "privacy_policy")
                val url = "https://www.dzinemedia.com/privacy-policy"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
            R.id.mMoreApps -> {
                addEventToFirebaseAnalytics("click", "more_apps")
                val url = "https://play.google.com/store/apps/dev?id=8194673071547521792"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
            R.id.mRateApps -> {
                showRateUsDialouge()
            }
            R.id.mSupportUs -> {
                addEventToFirebaseAnalytics("click", "support")
                Utils.startFeedbackEmail(this)
            }
            R.id.mLogout -> {
                addEventToFirebaseAnalytics("click", "Logout")
                if (Prefs.getBoolean(Constants.FROM_SOCIAL)) {
                    Prefs.clear()
                    Prefs.remove(Constants.PREF_TOKEN)
                    Prefs.putBoolean(Constants.IS_LOGIN, false)
                    Prefs.remove(Constants.UserModel)
                    signOut()
                } else if (Prefs.getBoolean(Constants.FROM_FACEBOOK)) {
                    Prefs.clear()
                    Prefs.remove(Constants.PREF_TOKEN)
                    Prefs.putBoolean(Constants.IS_LOGIN, false)
                    Prefs.remove(Constants.UserModel)
                    logoutFacebook()
                } else {
                    Prefs.clear()
                    Prefs.remove(Constants.PREF_TOKEN)
                    Prefs.putBoolean(Constants.IS_LOGIN, false)
                    Prefs.remove(Constants.UserModel)
                    startActivity(Intent(this, SignInActivity::class.java))
                    finishAffinity()
                }
            }
        }
        activityMainBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut() {
        Prefs.putBoolean(Constants.FROM_SOCIAL, false)
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(
                this
            ) {
                startActivity(Intent(this, SignInActivity::class.java))
                finishAffinity()
            }
    }

    private fun logoutFacebook() {
        Prefs.putBoolean(Constants.FROM_FACEBOOK, false)
        LoginManager.getInstance().logOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finishAffinity()
    }


    fun showRateUsDialouge() {
        var isStarSelected = 5
        val builder = AlertDialog.Builder(this)
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.rate_us_custom_dialog, viewGroup, false)
        builder.setView(dialogView)
        val changeEmoji = dialogView.findViewById<ImageView>(R.id.changeEmojiIv)
        val iV = dialogView.findViewById<ImageView>(R.id.dialougeImageV)
        val first = dialogView.findViewById<ImageView>(R.id.firstStar)
        val second = dialogView.findViewById<ImageView>(R.id.secondStar)
        val third = dialogView.findViewById<ImageView>(R.id.thirdStar)
        val forth = dialogView.findViewById<ImageView>(R.id.fourthStar)
        val fifth = dialogView.findViewById<ImageView>(R.id.fifthStar)
        val cancelDialouge = dialogView.findViewById<ImageView>(R.id.cancelDialougeiV)
        val textchangeAble = dialogView.findViewById<TextView>(R.id.changeTextView)
        val rate_us_on_google = dialogView.findViewById<TextView>(R.id.rate_us_on_googletv)
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // set image Resource
        first.setImageResource(R.drawable.ylw_star)
        second.setImageResource(R.drawable.ylw_star)
        third.setImageResource(R.drawable.ylw_star)
        forth.setImageResource(R.drawable.ylw_star)
        fifth.setImageResource(R.drawable.ylw_star)
        cancelDialouge.setOnClickListener {
            alertDialog.dismiss()
        }
        first.setOnClickListener {
            isStarSelected = 1
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.given_star)
            third.setImageResource(R.drawable.given_star)
            forth.setImageResource(R.drawable.given_star)
            fifth.setImageResource(R.drawable.given_star)
            textchangeAble.text = resources.getText(R.string.oops)
            changeEmoji.setImageResource(R.drawable.cry)
        }
        second.setOnClickListener {
            isStarSelected = 2
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.given_star)
            forth.setImageResource(R.drawable.given_star)
            fifth.setImageResource(R.drawable.given_star)
            textchangeAble.text = resources.getText(R.string.oops)
            changeEmoji.setImageResource(R.drawable.cryingone)
        }
        third.setOnClickListener {
            isStarSelected = 3
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.ylw_star)
            forth.setImageResource(R.drawable.given_star)
            fifth.setImageResource(R.drawable.given_star)
            textchangeAble.text = resources.getText(R.string.we_like_you_too)
            changeEmoji.setImageResource(R.drawable.happpy)
        }
        forth.setOnClickListener {
            isStarSelected = 4
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.ylw_star)
            forth.setImageResource(R.drawable.ylw_star)
            fifth.setImageResource(R.drawable.given_star)
            textchangeAble.text = resources.getText(R.string.we_like_you_too)
            changeEmoji.setImageResource(R.drawable.happpy)
        }
        fifth.setOnClickListener {
            isStarSelected = 5
            first.setImageResource(R.drawable.ylw_star)
            second.setImageResource(R.drawable.ylw_star)
            third.setImageResource(R.drawable.ylw_star)
            forth.setImageResource(R.drawable.ylw_star)
            fifth.setImageResource(R.drawable.ylw_star)
            textchangeAble.text = resources.getText(R.string.thanks_for_liking)
            changeEmoji.setImageResource(R.drawable.love)
        }
        rate_us_on_google.setOnClickListener {
            alertDialog.dismiss()
            if (isStarSelected == 1 || isStarSelected == 2 || isStarSelected == 3) {
                Utils.giveReview(this)
            } else {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                startActivity(openURL)
            }
        }
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

    private fun hidemLoginSignup() {
        navigationView = findViewById<View>(R.id.main_navigation_view) as NavigationView
        val nav_Menu: Menu = navigationView.menu
        nav_Menu.findItem(R.id.mLoginSignup).isVisible = false
    }

    /* private fun hidemSubscriptionPlan() {
         navigationView = findViewById<View>(R.id.main_navigation_view) as NavigationView
         val nav_Menu: Menu = navigationView.menu
         nav_Menu.findItem(R.id.mSubscriptionPlan).isVisible = false
     }*/

    private fun hidemProfile() {
        navigationView = findViewById<View>(R.id.main_navigation_view) as NavigationView
        val nav_Menu: Menu = navigationView.menu
        nav_Menu.findItem(R.id.mProfile).isVisible = false
    }

    private fun hidemChangePassword() {
        navigationView = findViewById<View>(R.id.main_navigation_view) as NavigationView
        val nav_Menu: Menu = navigationView.menu
        nav_Menu.findItem(R.id.mChangePassword).isVisible = false
    }

    private fun hidemHistory() {
        navigationView = findViewById<View>(R.id.main_navigation_view) as NavigationView
        val nav_Menu: Menu = navigationView.menu
        nav_Menu.findItem(R.id.mHistory).isVisible = false
    }

    private fun hidemLogoutButton() {
        navigationView = findViewById<View>(R.id.main_navigation_view) as NavigationView
        val nav_Menu: Menu = navigationView.menu
        nav_Menu.findItem(R.id.mLogout).isVisible = false
    }

    companion object {
        var userModel: DataObjectModel? = null
        var freeInfo: Data? = null
        var isPremiumFromFragment: String = ""

        fun addEventToFirebaseAnalytics(event_name: String, value: String) {
            try {
                val firebaseAnalytics = FirebaseAnalytics.getInstance(App.app!!)
                val params = Bundle()
                params.putString("click", value)
                firebaseAnalytics.logEvent(event_name, params)
                Log.d("MAIN_TAG", "addEventToFirebaseAnalytics: event-> $event_name")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getInfo(savedInstanceState: Bundle?) {
        val dialog = Dialog(this@MainActivity)
        Utils.loader(dialog)
        RetrofitAuthenticationClient.AuthClient()
            .getInfo(
                "asdf",
                ip
            )
            .enqueue(object : Callback<GetInfoModel> {
                override fun onResponse(
                    call: Call<GetInfoModel>,
                    response: Response<GetInfoModel>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        val freeInfo: GetInfoModel =
                            response.body() as GetInfoModel
                        val mInfo = freeInfo.data
                        val json = App.gson.toJson(mInfo)
                        Log.i(TAG, "onResponseFree: $json")
                        Prefs.putString(Constants.FreeInfo, json)
                        MainActivity.freeInfo = App.gson.fromJson(json, Data::class.java)
                        intializationAttributes(savedInstanceState)
                       /* startActivity(Intent(this@MainActivity, MainActivity::class.java))
                        finishAffinity()*/
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Internet connection or server error\n" +
                                    " plz restart your app.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GetInfoModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "Internet connection or server error\n plz restart your app." /*+ t.message*/,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })
    }

    fun userProfileApiResponce(savedInstanceState: Bundle?) {
        paidJson = Prefs.getString(Constants.UserModel, "")
        userModel = App.gson.fromJson(paidJson, DataObjectModel::class.java)
        val user_id = userModel?.user_id
        val token =
            "Bearer " + Prefs.getString(Constants.PREF_TOKEN, "")/*MainActivity.userModel!!.token*/
        Log.i(TAG, "userProfileApiResponce: token: $token \n user id: $user_id")

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
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        val userModel: AuthenticationBaseModel =
                            response.body() as AuthenticationBaseModel
                        val mCurrentUser = userModel.data
                        val json = App.gson.toJson(userModel.data)
                        Log.i(TAG, "onResponse: $json")
                        Prefs.putString(Constants.UserModel, json)
                        Prefs.putString(Constants.PREF_TOKEN, mCurrentUser.token)
                        Prefs.putBoolean(Constants.IS_LOGIN, true)
                        MainActivity.userModel = App.gson.fromJson(json, DataObjectModel::class.java)
                        intializationAttributes(savedInstanceState)
                        /*startActivity(Intent(this@MainActivity, MainActivity::class.java))
                        finishAffinity()*/
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Internet connection or server error\n" +
                                    " plz restart your app.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "Internet connection or server error\n plz restart your app." /*+ t.message*/,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })
    }

    private fun cancelRequest(savedInstanceState: Bundle?) {
        Utils.loader(dialog)
        paidJson = Prefs.getString(Constants.UserModel, "")
        userModel = App.gson.fromJson(paidJson, DataObjectModel::class.java)
        val user_id = userModel?.user_id
        val subscription_id = userModel?.subscription_id
        Log.i(TAG, "cancelRequest: $subscription_id")
        if (subscription_id != "" && !subscription_id.equals(null)) {
            val token =
                "Bearer " + Prefs.getString(Constants.PREF_TOKEN, "")
            Log.i(TAG, "userProfileApiResponce: token: $token \n user id: $user_id")

            RetrofitAuthenticationClient.AuthClient()
                .cancelSubscription(
                    token,
                    user_id,
                    subscription_id,
                    androidId
                )
                .enqueue(object : Callback<AuthenticationBaseModel> {
                    override fun onResponse(
                        call: Call<AuthenticationBaseModel>,
                        response: Response<AuthenticationBaseModel>
                    ) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            val userModel: AuthenticationBaseModel =
                                response.body() as AuthenticationBaseModel
                            val json = App.gson.toJson(userModel.data)
                            Prefs.putString(Constants.UserModel, json)
                            Prefs.putBoolean(Constants.IS_LOGIN, true)
                            MainActivity.userModel = App.gson.fromJson(json, DataObjectModel::class.java)
                            intializationAttributes(savedInstanceState)
                            /*startActivity(Intent(this@MainActivity, MainActivity::class.java))
                            finishAffinity()*/
                            Log.i(TAG, "cancelRequest: request cancelled")
                        } else {
                            try {
                                val errorBody = response.errorBody()!!.string()
                                Log.e("RetrofitError", "Error response: $errorBody")
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                        dialog.dismiss()
                        Log.i(TAG, "onResponse: Internet connection or server error")
                    }
                })
        } else {
            userProfileApiResponce(savedInstanceState)
        }
    }
}