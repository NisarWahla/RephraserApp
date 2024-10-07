package dzm.dzmedia.repheraserapp.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.adapter.ForgotPasswordFragmentAdapter
import dzm.dzmedia.repheraserapp.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var fragmentsAdapter: ForgotPasswordFragmentAdapter
    lateinit var binding: ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragmentsAdapter = ForgotPasswordFragmentAdapter(this)
        binding.viewPager.adapter = fragmentsAdapter
        binding.viewPager.isUserInputEnabled = false
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
        binding.backTOLoginBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}


