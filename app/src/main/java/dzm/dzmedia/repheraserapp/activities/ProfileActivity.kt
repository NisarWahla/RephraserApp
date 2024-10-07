package dzm.dzmedia.repheraserapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.databinding.ActivityProfileBinding
import dzm.dzmedia.repheraserapp.helpers.Constants

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        var json = Prefs.getString(Constants.UserModel, "")
        MainActivity.userModel = App.gson.fromJson(json, DataObjectModel::class.java)

        val firstName = MainActivity.userModel!!.firstName
        val lastName = MainActivity.userModel!!.last_name
        val email = MainActivity.userModel!!.email
        val contactNumber =
            MainActivity.userModel!!.contact_no
        val fullName = "$firstName $lastName"
        try {
            binding.userName.text = fullName
            binding.firstNameEt.setText(firstName)
            binding.lastNameEt.setText(lastName)
            binding.emailEt.setText(email)
            binding.mobileEt.setText(contactNumber)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        binding.backBtn.setOnClickListener {
            super.onBackPressed()
            addEventToFirebaseAnalytics("click", "MainActivity")
        }

        binding.updateProfile.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, UpdateProfileActivity::class.java))
            addEventToFirebaseAnalytics("click", "UpdateProfileActivity")
            finish()
        }

        binding.logout.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, UpdateProfileActivity::class.java))
            finish()
        }
    }
}