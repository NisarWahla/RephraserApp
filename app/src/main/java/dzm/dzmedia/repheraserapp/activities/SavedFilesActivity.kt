package dzm.dzmedia.repheraserapp.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.pixplicity.easyprefs.library.Prefs
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity.Companion.addEventToFirebaseAnalytics
import dzm.dzmedia.repheraserapp.adapter.SavedFilesAdapter
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.databinding.ActivitySavedFilesBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.model.SavedFilesModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SavedFilesActivity : AppCompatActivity() {

    lateinit var binding: ActivitySavedFilesBinding
    var list: ArrayList<SavedFilesModel> = ArrayList()
    private val TAG = "SavedFilesActivity"
    private var adLoader: AdLoader? = null
    var template: TemplateView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GlobalScope.launch(Dispatchers.IO) {
            setData()
            GlobalScope.launch(Dispatchers.Main) {
                initializeAdapter()
            }
        }
        AdManger.loadBannerAds(binding.admobBannerSize, this)

        MobileAds.initialize(this)
        adLoader =
            AdLoader.Builder(this, getString(R.string.admob_native_id))
                .forNativeAd { nativeAd ->
                    val styles =
                        NativeTemplateStyle.Builder().build()
                    template = binding.nativeTemplateViewSF
                    template!!.setStyles(styles)
                    template!!.setNativeAd(nativeAd)
                }
                .build()

        adLoader?.loadAd(AdRequest.Builder().build())

        binding.btnBack.setOnClickListener {
            super.onBackPressed()
            addEventToFirebaseAnalytics("click", "MainActivity")

        }
        try {
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                //do logic for free and without login
            } else  {
                //do logic for paid and logged in user
                if (MainActivity.userModel!!.email == Constants.guestEmail) {
                    binding.admobBannerSize.visibility = View.VISIBLE
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
            //do logic for free and without login
        } else  {
            //do logic for paid and logged in user
            if ((!MainActivity.userModel!!.verified)) {
                binding.admobBannerSize.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun initializeAdapter() {
        binding.savedFilesRecycler.setHasFixedSize(true)
        binding.savedFilesRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list.sortByDescending {
            it.cDate
        }
        val adapter = SavedFilesAdapter(this, list, binding)
        binding.savedFilesRecycler.adapter = adapter
        adapter.notifyDataSetChanged()

        if (adapter.itemCount > 0) {
            binding.savedFilesRecycler.visibility = View.VISIBLE
            binding.mianScrEmptyIcon.visibility = View.GONE
            binding.nothingShow.visibility = View.GONE
        } else {
            binding.savedFilesRecycler.visibility = View.GONE
            binding.mianScrEmptyIcon.visibility = View.VISIBLE
            binding.nothingShow.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setData() {
        list.clear()
        val path: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = getExternalFilesDir("Rephraser")!!.absolutePath
            Log.d("elevenAndroid", path)
        } else {
            path =
                Environment.getExternalStorageDirectory().path + "/Rephraser/"
            Log.d("beforeelevenAndroid", path)
        }
        try {
            Log.d("Files", path)
            val directory = File(path)
            val files = directory.listFiles()
            if (files != null) {
                for (i in files.indices) {
                    val file = File(files[i].absolutePath)
                    Log.d("filePath", file.toString())
                    if (file.isDirectory) {
                        Log.d("FileDirectory", "FileName:" + files[i].name)

                        val sdf = SimpleDateFormat("HH:mm a MMMM dd, yyyy", Locale.getDefault())
                        val date = file.lastModified()
                        val currentDateString = sdf.format(date)
                        val currentDate = sdf.parse(currentDateString)!!

                        list.add(
                            SavedFilesModel(
                                files[i].name,
                                "folder",
                                "folder",
                                file.toString(), currentDate
                            )
                        )
                    } else {
                        Log.d("formatedDate", "onCreate: "/*$formattedDate*/)
                        Log.d("FileNameeee", "FileNameeeee:" + files[i].name)
                        val extension =
                            files[i].absolutePath.substring(files[i].absolutePath.lastIndexOf("."))
                        Log.d("extension", extension)

                        val sdf = SimpleDateFormat("HH:mm a MMMM dd, yyyy", Locale.getDefault())
                        val date = file.lastModified()
                        Log.i(TAG, "setData: $date")
                        val currentDateString = sdf.format(date)
                        val currentDate = sdf.parse(currentDateString)!!

                        list.add(
                            SavedFilesModel(
                                files[i].name,
                                extension,
                                "file",
                                file.toString(), currentDate
                            )
                        )
                    }
                }
            } else {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}