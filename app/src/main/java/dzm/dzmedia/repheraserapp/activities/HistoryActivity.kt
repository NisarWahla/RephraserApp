package dzm.dzmedia.repheraserapp.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.adapter.HistoryFragmentAdapter
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.databinding.ActivityHistoryBinding
import dzm.dzmedia.repheraserapp.helpers.Constants

val tabsArray = arrayOf(
    "Usage History",
    "Payment History"
)

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val json = Prefs.getString(Constants.UserModel, "")
        MainActivity.userModel = App.gson.fromJson(json, DataObjectModel::class.java)
        AdManger.loadBannerAds(binding.admobBannerSize, this)

        val viewPager = binding.viewpager
        val tabLayout = binding.tabLayout
        val adapter = HistoryFragmentAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabsArray[position]
        }.attach()

        binding.backBtn.setOnClickListener {
            super.onBackPressed()
            addEventToFirebaseAnalytics("click", "HistoryActivity")
        }
        if (MainActivity.userModel!!.email == Constants.guestEmail) {
            binding.admobBannerSize.visibility = View.VISIBLE
        }
        if (!MainActivity.userModel!!.verified) {
            binding.admobBannerSize.visibility = View.VISIBLE
        }
    }
}