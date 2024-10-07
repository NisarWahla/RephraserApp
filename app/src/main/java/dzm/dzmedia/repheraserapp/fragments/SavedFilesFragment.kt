package dzm.dzmedia.repheraserapp.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.databinding.ActivityMainBinding
import dzm.dzmedia.repheraserapp.databinding.FragmentSavedFilesBinding
import dzm.dzmedia.repheraserapp.model.SavedFilesModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SavedFilesFragment : androidx.fragment.app.Fragment() {
    lateinit var savedFilesBinding: FragmentSavedFilesBinding
    var list: ArrayList<SavedFilesModel> = ArrayList()
    private val TAG = "SavedFilesFragment"
    private lateinit var mainActivity: MainActivity
    lateinit var activityMainBinding: ActivityMainBinding

    private var adLoader: AdLoader? = null
    private var adLoaded = false
    var template: TemplateView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        savedFilesBinding = FragmentSavedFilesBinding.inflate(inflater, container, false)
        return savedFilesBinding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        activityMainBinding = mainActivity.activityMainBinding

        GlobalScope.launch(Dispatchers.IO) {
            setData()
            GlobalScope.launch(Dispatchers.Main) {
                initializeAdapter()
            }
        }

        MobileAds.initialize(requireContext())
        adLoader =
            AdLoader.Builder(requireContext(), requireContext().getString(R.string.admob_native_id))
                .forNativeAd { nativeAd ->
                    val styles =
                        NativeTemplateStyle.Builder().build()
                    template = savedFilesBinding.nativeTemplateViewSF
                    template!!.setStyles(styles)
                    template!!.setNativeAd(nativeAd)
                }
                .build()

        adLoader?.loadAd(AdRequest.Builder().build())

        savedFilesBinding.btnBack.setOnClickListener {
            moveToRephraseFragment()
        }

        savedFilesBinding.contactSupport.setOnClickListener {
            moveToContactSupportFragment()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun initializeAdapter() {
        savedFilesBinding.savedFilesRecycler.setHasFixedSize(true)
        savedFilesBinding.savedFilesRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        list.sortByDescending {
            it.cDate
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setData() {
        list.clear()
        val path: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = requireActivity().getExternalFilesDir("Rephraser")!!.absolutePath/*toString()*/
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
            if (files != null ) {
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

    private fun moveToRephraseFragment() {
        val trans: FragmentTransaction = parentFragmentManager.beginTransaction()
        trans.replace(activityMainBinding.pager.id, RephraseParagraphFragment())
        trans.commit()
    }

    private fun moveToContactSupportFragment() {
        val trans: FragmentTransaction = parentFragmentManager.beginTransaction()
        trans.replace(activityMainBinding.pager.id, ContactSupportFragment())
        trans.addToBackStack(null)
        trans.commit()
    }
}