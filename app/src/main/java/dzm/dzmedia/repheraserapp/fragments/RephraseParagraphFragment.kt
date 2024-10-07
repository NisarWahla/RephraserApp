package dzm.dzmedia.repheraserapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.TransactionTooLargeException
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.material.button.MaterialButton
import com.permissionx.guolindev.PermissionX
import com.pixplicity.easyprefs.library.Prefs
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.activities.PremiumActivity
import dzm.dzmedia.repheraserapp.activities.VerifyEmailActivity
import dzm.dzmedia.repheraserapp.adapter.CustomDropDownAdapter
import dzm.dzmedia.repheraserapp.adapter.FrozenWordsAdapter
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.authentication_api.ErrorResponse
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.ActivityMainBinding
import dzm.dzmedia.repheraserapp.databinding.FragmentRephraseParagraphBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.PathUtils
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.CustomSpinnerModel
import dzm.dzmedia.repheraserapp.model.GetInfo.Data
import dzm.dzmedia.repheraserapp.rephraser_api_responce.RephraserApiResponce
import dzm.dzmedia.repheraserapp.retrofit.RetroClientRephraseText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class RephraseParagraphFragment : Fragment(), View.OnClickListener {
    private val TAG = RephraseParagraphFragment::class.simpleName
    private lateinit var rephraseParagraphBinding: FragmentRephraseParagraphBinding
    private lateinit var mainActivity: MainActivity
    lateinit var activityMainBinding: ActivityMainBinding
    private var adLoader: AdLoader? = null
    var template: TemplateView? = null
    var encrypted = false
    private val OPEN_PDF_FILE_REQUEST_CODE = 1
    private val OPEN_DOCX_FILE_REQUEST_CODE = 2
    private val OPEN_TEXT_FILE_REQUEST_CODE = 3
    var intentOpenDocxfile: Intent? = null
    var intentOpenPDFfile: Intent? = null
    var intentOpenTextfile: Intent? = null
    var mCounts: Int = 0
    var fileUri: Uri? = null
    var isFirstTime = false
    lateinit var mode: String
    var frozenWordsList: ArrayList<String> = ArrayList()
    lateinit var stringFromFreezeArray: String
    lateinit var customDropDownAdapter: CustomDropDownAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        rephraseParagraphBinding =
            FragmentRephraseParagraphBinding.inflate(inflater, container, false)
        return rephraseParagraphBinding.root
    }

    @SuppressLint("FragmentBackPressedCallback")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity
        activityMainBinding = mainActivity.activityMainBinding


        PDFBoxResourceLoader.init(requireContext())

        val drawerList: ArrayList<CustomSpinnerModel> = ArrayList()
        drawerList.add(CustomSpinnerModel("Creative", "your title"))
        drawerList.add(CustomSpinnerModel("Anti Plagiarism", "your title"))
        drawerList.add(CustomSpinnerModel("Fluency", "your title"))
        drawerList.add(CustomSpinnerModel("Academic", "your title"))
        drawerList.add(CustomSpinnerModel("Blog", "your title"))
        drawerList.add(CustomSpinnerModel("Formal", "your title"))

        customDropDownAdapter = CustomDropDownAdapter(requireContext(), drawerList)
        rephraseParagraphBinding.modesSpinner.adapter = customDropDownAdapter
        rephraseParagraphBinding.modesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, position: Int, p3: Long
                ) {
                    val data = drawerList[position]
                    mode = data.name
                    if (mode == "Creative") {
                        mode = "Standard"
                    } else if (mode == "Anti Plagiarism") {
                        mode = "General"
                    }
                    if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                        // do logic for free or not login user
                        if (mode == "Academic" || mode == "Blog" || mode == "Formal") {
                            upGradeToPremium()
                        }
                    } else {
                        //paid or login user
                        if (mode == "Academic" && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.subscription == "") {
                            upGradeToPremium()
                            rephraseParagraphBinding.modesSpinner.setSelection(0)
                        } else if (mode == "Blog" && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.subscription == "") {
                            upGradeToPremium()
                            rephraseParagraphBinding.modesSpinner.setSelection(0)
                        } else if (mode == "Formal" && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.subscription == "") {
                            upGradeToPremium()
                            rephraseParagraphBinding.modesSpinner.setSelection(0)
                        } else if (mode == "Academic" && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.subscription == "3 Days Trial") {

                        } else if (mode == "Blog" && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.subscription == "3 Days Trial") {
                            rephraseParagraphBinding.freezeButton.visibility = View.VISIBLE
                        } else if (mode == "Formal" && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.subscription == "3 Days Trial") {
                        } else if (mode == "Blog" && MainActivity.userModel!!.subscribed) {
                            rephraseParagraphBinding.freezeButton.visibility = View.VISIBLE
                        } else if (mode == "Academic" && MainActivity.userModel!!.subscribed) {
                            rephraseParagraphBinding.freezeButton.visibility = View.GONE
                        } else {
                            rephraseParagraphBinding.freezeButton.visibility = View.GONE
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        mode = rephraseParagraphBinding.modesSpinner.selectedItem.toString()
        rephraseParagraphBinding.btnUpload.setOnClickListener(this)
        rephraseParagraphBinding.freezeButton.setOnClickListener(this)
        rephraseParagraphBinding.copyRephrasedText.setOnClickListener(this)
        rephraseParagraphBinding.placeTextImg.setOnClickListener(this)
        rephraseParagraphBinding.delText.setOnClickListener(this)
        rephraseParagraphBinding.rpExport.setOnClickListener(this)
        rephraseParagraphBinding.generteText.setOnClickListener(this)

        rephraseParagraphBinding.rephraserET.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                rephraseParagraphBinding.placeTextImg.visibility = View.GONE
                rephraseParagraphBinding.placeText.visibility = View.GONE

                if (s.isEmpty()) {
                    rephraseParagraphBinding.placeTextImg.visibility = View.VISIBLE
                    rephraseParagraphBinding.placeText.visibility = View.VISIBLE
                }
                try {
                    val words: String = s.toString().trim()
                    mCounts = if (words.isEmpty()) 0 else words.split("\\s+".toRegex())
                        .toTypedArray().size
                    rephraseParagraphBinding.writenWords.text = "$mCounts"
                    if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                        //do logic for free and without login user
                        if (mCounts >= MainActivity.freeInfo!!.free_max_limit + 1) {
                            rephraseParagraphBinding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.red_clr
                                )
                            )
                        } else {
                            rephraseParagraphBinding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.word_count_color
                                )
                            )
                        }
                    } else {
                        //do logic for paid and login user
                        //change color if word limit excceeds.
                        if (mCounts >= MainActivity.userModel!!.word_limit + 1 && !MainActivity.userModel!!.subscribed && !MainActivity.userModel!!.verified) {
                            rephraseParagraphBinding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.red_clr
                                )
                            )
                        } else if (mCounts >= MainActivity.userModel!!.word_limit + 1 && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.verified) {
                            rephraseParagraphBinding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.red_clr
                                )
                            )
                        } else if (mCounts >= MainActivity.userModel!!.word_limit + 1 && MainActivity.userModel!!.subscribed) {
                            rephraseParagraphBinding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.red_clr
                                )
                            )
                        } else {
                            rephraseParagraphBinding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.word_count_color
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (a: ArrayIndexOutOfBoundsException) {
                    a.printStackTrace()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        rephraseParagraphBinding.rephrasedET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                rephraseParagraphBinding.noResult.visibility = View.GONE

                if (s.isEmpty()) {
                    rephraseParagraphBinding.noResult.visibility = View.VISIBLE
                }
                try {
                    val words: String = s.toString().trim()
                    mCounts = if (words.isEmpty()) 0 else words.split("\\s+".toRegex())
                        .toTypedArray().size
                    rephraseParagraphBinding.rephrasedWritenWords.text = "$mCounts"
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (a: ArrayIndexOutOfBoundsException) {
                    a.printStackTrace()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        MobileAds.initialize(requireContext())
        /*adLoader =
            AdLoader.Builder(requireContext(), requireContext().getString(R.string.admob_native_id))
                .forNativeAd { nativeAd ->
                    val styles = NativeTemplateStyle.Builder().build()
                    template = rephraseParagraphBinding.nativeTemplateViewRP
                    template!!.setStyles(styles)
                    template!!.setNativeAd(nativeAd)
                }.build()
        adLoader?.loadAd(AdRequest.Builder().build())*/
        try {
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                //do logic for free and without login user
                val wordLimit = MainActivity.freeInfo!!.free_max_limit
                rephraseParagraphBinding.totalWords.text = "/$wordLimit"
            } else {
                //do logic for paid and login user
                val wordLimit: Int = MainActivity.userModel!!.word_limit
                //set words limit according to payment.
                if (MainActivity.userModel!!.email == Prefs.getString(Constants.guestEmail)) {
                    rephraseParagraphBinding.totalWords.text = "/$wordLimit"
                } else if (!MainActivity.userModel!!.subscribed && !MainActivity.userModel!!.verified) {
                    rephraseParagraphBinding.totalWords.text = "/$wordLimit"
                } else if (!MainActivity.userModel!!.subscribed && MainActivity.userModel!!.verified) {
                    rephraseParagraphBinding.totalWords.text = "/$wordLimit"
                } else {
                    rephraseParagraphBinding.totalWords.text = "/$wordLimit"
                }

                if (MainActivity.userModel!!.word_limit == 250) {
                    activityMainBinding.admobBannerSize.visibility = View.VISIBLE
                }
                Log.d(TAG, "onViewCreated: ${MainActivity.userModel!!.word_limit}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyText(text: String) {
        val myClipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
    }

    fun ApiResponce(ip: String) {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        dialog.show()
        if (mode != "Blog") {
            //entries
            var inpputText = rephraseParagraphBinding.rephraserET.text.toString()
            val userID = MainActivity.userModel!!.user_id

            RetroClientRephraseText.APIClient().sendToRephraser(
                "Bearer " + MainActivity.userModel!!.token,
                inpputText,
                Constants.REPHRASER_ACTION_TYPE,
                userID,
                ip,
                mode,
                ""
            ).enqueue(object : Callback<RephraserApiResponce> {
                override fun onResponse(
                    call: Call<RephraserApiResponce>, response: Response<RephraserApiResponce>
                ) {
                    try {
                        if (dialog != null && dialog.isShowing) dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: $response")
                        val data = response.body()?.data
                        Log.d(TAG, "onResponse: ")
                        if (MainActivity.userModel!!.word_limit == 250) {
                            if (AdManger.isInterstialLoaded()) {
                                AdManger.showInterstial(requireActivity(),
                                    requireContext(),
                                    object : FullScreenContentCallback() {
                                        override fun onAdDismissedFullScreenContent() {
                                            super.onAdDismissedFullScreenContent()
                                            rephraseParagraphBinding.rephrasedET.setText(
                                                data!!.output_text
                                            )
                                        }

                                        override fun onAdShowedFullScreenContent() {
                                            super.onAdShowedFullScreenContent()
                                            rephraseParagraphBinding.rephrasedET.setText(
                                                data!!.output_text
                                            )
                                        }

                                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                            super.onAdFailedToShowFullScreenContent(p0)
                                            rephraseParagraphBinding.rephrasedET.setText(
                                                data!!.output_text
                                            )
                                        }
                                    })
                            }
                        } else {
                            rephraseParagraphBinding.rephrasedET.setText(
                                data!!.output_text
                            )
                        }
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val errorJSOn = response.errorBody()?.string()
                                val errorResponse =
                                    App.gson.fromJson(errorJSOn, ErrorResponse::class.java)
                                if (errorResponse.data == "Usages limit reached!") {
                                    upGradeToPremium()
                                } else {
                                    //Constants.errorHandler(errorJSOn, requireContext())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<RephraserApiResponce>, t: Throwable) {
                    try {
                        dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    Log.d(TAG, "onFailure: ${t.message}")
                }
            })
        } else {
            //entries
            val inputText = rephraseParagraphBinding.rephraserET.text.toString()
            val userID = MainActivity.userModel!!.user_id

            RetroClientRephraseText.APIClient().sendToRephraser(
                "Bearer " + MainActivity.userModel!!.token,
                inputText,
                Constants.REPHRASER_ACTION_TYPE,
                userID,
                ip,
                mode,
                stringFromFreezeArray
            ).enqueue(object : Callback<RephraserApiResponce> {
                override fun onResponse(
                    call: Call<RephraserApiResponce>, response: Response<RephraserApiResponce>
                ) {
                    try {
                        if (dialog != null && dialog.isShowing) dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: $response")
                        val data = response.body()?.data
                        Log.d(TAG, "onResponse: ")
                        if (dialog != null && dialog.isShowing) dialog.dismiss()
                        rephraseParagraphBinding.rephrasedET.setText(
                            data!!.output_text
                        )
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val errorJSOn = response.errorBody()?.string()
                                //Constants.errorHandler(errorJSOn, requireContext())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<RephraserApiResponce>, t: Throwable) {
                    dialog.dismiss()
                    Log.d(TAG, "onFailure: ${t.message}")
                }
            })
        }
    }

    private fun getRephraserFreeAttempt(ip: String) {
        val inputText = rephraseParagraphBinding.rephraserET.text.toString()
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        dialog.show()
        RetrofitAuthenticationClient.AuthClient()
            .getrephraser(
                Constants.FreeUserKey,
                inputText,
                Constants.REPHRASER_ACTION_TYPE,
                ip,
                mode.lowercase()
            )
            .enqueue(object : Callback<RephraserApiResponce> {
                override fun onResponse(
                    call: Call<RephraserApiResponce>,
                    response: Response<RephraserApiResponce>
                ) {
                    try {
                        if (dialog != null && dialog.isShowing) dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    if (response.isSuccessful) {
                        val freeInfo: RephraserApiResponce =
                            response.body() as RephraserApiResponce
                        val mInfo = freeInfo.data
                        if (AdManger.isInterstialLoaded()) {
                            AdManger.showInterstial(requireActivity(),
                                requireContext(),
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        rephraseParagraphBinding.rephrasedET.setText(
                                            mInfo.output_text
                                        )
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                        rephraseParagraphBinding.rephrasedET.setText(
                                            mInfo.output_text
                                        )
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        rephraseParagraphBinding.rephrasedET.setText(
                                            mInfo.output_text
                                        )
                                    }
                                })
                        }
                    } else {
                        if (response.errorBody() != null) {
                            try {
                                val errorJSOn = response.errorBody()?.string()
                                val errorResponse =
                                    App.gson.fromJson(errorJSOn, ErrorResponse::class.java)
                                if (errorResponse.data == "Usages limit reached!") {
                                    upGradeToPremium()
                                } else {
                                    Constants.errorHandler(errorJSOn, requireContext())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<RephraserApiResponce>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        requireActivity(),
                        "Internet connection or server error\n plz restart your app." /*+ t.message*/,
                        Toast.LENGTH_LONG
                    ).show()
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

    override fun onClick(v: View?) {
        if (v?.id == rephraseParagraphBinding.copyRephrasedText.id) {
            if (rephraseParagraphBinding.rephrasedET.text.toString().trim() == "") {
                Toast.makeText(requireContext(), "No text available to copy!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                copyText(rephraseParagraphBinding.rephrasedET.text.toString())
                Toast.makeText(requireContext(), "Text copied", Toast.LENGTH_SHORT).show()
            }
        } else if (v?.id == rephraseParagraphBinding.placeTextImg.id) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            val item = clip?.getItemAt(0)
            val textToPaste = item?.text
            rephraseParagraphBinding.rephraserET.setText(textToPaste.toString())
            rephraseParagraphBinding.rephraserET.setSelection(rephraseParagraphBinding.rephraserET.text.length)
            isFirstTime = true
        } else if (v?.id == rephraseParagraphBinding.delText.id) {
            val containsOnlySpaces =
                rephraseParagraphBinding.rephraserET.text.toString().trim().isEmpty()
            if (rephraseParagraphBinding.rephraserET.text.toString().trim().isNotEmpty()) {
                rephraseParagraphBinding.rephraserET.text.clear()
            } else if (containsOnlySpaces && rephraseParagraphBinding.rephraserET.text.toString()
                    .isNotEmpty()
            ) {
                rephraseParagraphBinding.rephraserET.text.clear()
            } else {
                Toast.makeText(requireActivity(), "No text to delete!", Toast.LENGTH_SHORT).show()
            }
        } else if (v?.id == rephraseParagraphBinding.btnUpload.id) {
            val newText = rephraseParagraphBinding.rephraserET.text.toString()
            if (isFirstTime) {
                if (newText == "") {
                    openPdfPasteDialog()
                } else {
                    Toast.makeText(
                        requireActivity(), "Please Delete Original Text!", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                openPdfPasteDialog()
            }
        } else if (v?.id == rephraseParagraphBinding.rpExport.id) {
            hideKeyboard(requireActivity())
            if (rephraseParagraphBinding.rephrasedET.text.toString().trim() == "") {
                Toast.makeText(
                    requireContext(), "Please generate some text", Toast.LENGTH_SHORT
                ).show()
            } else {
                val exportTextDialogFragment = ExportTextDialogFragment(
                    rephraseParagraphBinding.rephrasedET.text.toString().trim(),
                    rephraseParagraphBinding.rephraserET.text.toString().trim()
                )
                Utils.openDialog(
                    requireActivity().supportFragmentManager, exportTextDialogFragment
                )
            }
        } else if (v?.id == rephraseParagraphBinding.generteText.id) {
            hideKeyboard(requireActivity())
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                //do logic for free and without login user
                if (rephraseParagraphBinding.rephraserET.text.toString().trim() == "") {
                    Toast.makeText(
                        requireContext(), "Upload file or write text!", Toast.LENGTH_SHORT
                    ).show()
                } else if (mCounts >= MainActivity.freeInfo!!.free_max_limit) {
                    upGradeToPremium()
                } else {
                    if (mode.equals("standard", ignoreCase = true) || mode.equals(
                            "general",
                            ignoreCase = true
                        ) || mode.equals("fluency", ignoreCase = true)
                    ) {
                        if (Constants.isOnline(requireActivity())) {
                            //User Device IP Address...
                            val ip: String = Constants.getIpAddress(requireActivity())
                            getRephraserFreeAttempt(ip)
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                R.string.no_internet,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        upGradeToPremium()
                    }
                }

            } else {
                // do logic for paid and login user
                if (rephraseParagraphBinding.rephraserET.text.toString().trim() == "") {
                    Toast.makeText(
                        requireContext(), "Upload file or write text!", Toast.LENGTH_SHORT
                    ).show()
                } else if (!MainActivity.userModel!!.verified && MainActivity.userModel!!.email != Constants.guestEmail && mCounts >= MainActivity.userModel!!.word_limit + 1) {
                    //VerifyEmailPopup()
                    upGradeToPremium()
                } else if (mCounts >= MainActivity.userModel!!.word_limit + 1 && !MainActivity.userModel!!.subscribed && !MainActivity.userModel!!.verified) {
                    upGradeToPremium()
                } else if (mCounts >= MainActivity.userModel!!.word_limit + 1 && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.verified) {
                    upGradeToPremium()
                } else if (mCounts >= 1501 && MainActivity.userModel!!.subscribed) {
                    Toast.makeText(
                        requireContext(), "Words can't be more than 1500", Toast.LENGTH_SHORT
                    ).show()
                } else if (MainActivity.userModel!!.email == Prefs.getString(Constants.guestEmail) && mCounts >= MainActivity.userModel!!.word_limit + 1) {
                    Toast.makeText(
                        requireContext(),
                        "register yourself to get more features",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!isOnline(requireContext())) {
                    Toast.makeText(
                        requireContext(), "No internet connection", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        val stringBuilder: StringBuilder = StringBuilder()
                        for (i in 0 until frozenWordsList.size) {
                            if (i == 0) {
                                stringBuilder.append(frozenWordsList[i])
                            } else {
                                stringBuilder.append("," + frozenWordsList[i])
                            }
                        }
                        stringFromFreezeArray = stringBuilder.toString()
                        Log.d(TAG, "FreezeWordsPopup: $stringFromFreezeArray")
                        val ip: String = Constants.getIpAddress(requireActivity())
                        ApiResponce(ip)
                    }
                }
            }
        } else if (v?.id == rephraseParagraphBinding.freezeButton.id) {
            FreezeWordsPopup()
        }
    }

    private fun VerifyEmailPopup() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.verify_email_dialog)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnCross = dialog.findViewById<ImageView>(R.id.cross_btn)
        val btnVerifyNow = dialog.findViewById<MaterialButton>(R.id.verifyNow)
        btnCross.setOnClickListener {
            dialog.dismiss()
        }
        btnVerifyNow.setOnClickListener {
            verifyEmailApiResponce()
        }
        dialog.show()
    }

    private fun FreezeWordsPopup() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.freeze_words_dialog)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val freezeWordET = dialog.findViewById<EditText>(R.id.freezeWordET)
        val frozenWordsRecycler = dialog.findViewById<RecyclerView>(R.id.frozenWordsRecycler)
        val btnFreeze = dialog.findViewById<TextView>(R.id.btnFreeze)
        val btnClear = dialog.findViewById<TextView>(R.id.btnlearAll)
        val btnCancel = dialog.findViewById<ImageView>(R.id.btnCancel)

        frozenWordsRecycler.setHasFixedSize(true)
        frozenWordsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = FrozenWordsAdapter(requireContext(), frozenWordsList)
        frozenWordsRecycler.adapter = adapter
        adapter.notifyDataSetChanged()


        btnFreeze.setOnClickListener {
            if (freezeWordET.text.isNotEmpty()) {
                if (frozenWordsList.size < 1 && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.verified) {
                    frozenWordsList.add(freezeWordET.text.toString())
                    Log.d(TAG, "FreezeWordsPopup: ${frozenWordsList.size}")
                    freezeWordET.setText("")
                    adapter.notifyDataSetChanged()
                } else if (frozenWordsList.size >= 1 && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.verified) {
                    dialog.dismiss()
                    upGradeToPremium()
                } else {
                    frozenWordsList.add(freezeWordET.text.toString())
                    Log.d(TAG, "FreezeWordsPopup: ${frozenWordsList.size}")
                    freezeWordET.setText("")
                    adapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(requireContext(), R.string.no_word_to_freeze, Toast.LENGTH_SHORT)
                    .show()
            }

            val stringBuilder: StringBuilder = StringBuilder()
            for (i in 0 until frozenWordsList.size) {
                if (i == 0) {
                    stringBuilder.append(frozenWordsList[i])
                } else {
                    stringBuilder.append("," + frozenWordsList[i])
                }
            }
            stringFromFreezeArray = stringBuilder.toString()
            Log.d(TAG, "FreezeWordsPopup: $stringFromFreezeArray")
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnClear.setOnClickListener {
            if (frozenWordsList.isNotEmpty()) {
                frozenWordsList.clear()
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(requireContext(), R.string.no_word_to_delete, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        dialog.show()
    }

    private fun upGradeToPremium() {
        startActivity(Intent(requireContext(), PremiumActivity::class.java))
        /*val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.upgrade_to_premium_dialog)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnCross = dialog.findViewById<ImageView>(R.id.cross_btn)
        val btnVerifyNow = dialog.findViewById<AppCompatButton>(R.id.verifyNow)

        btnCross.setOnClickListener {
            dialog.dismiss()
        }

        btnVerifyNow.setOnClickListener {
            startActivity(Intent(requireContext(), PremiumActivity::class.java))
            dialog.dismiss()
        }
        dialog.show()*/
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val email = MainActivity.userModel!!.email
        RetrofitAuthenticationClient.AuthClient().sendOTP(
            email
        ).enqueue(object : Callback<AuthenticationBaseModel> {
            override fun onResponse(
                call: Call<AuthenticationBaseModel>, response: Response<AuthenticationBaseModel>
            ) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    val msg = response.body()?.message
                    Toast.makeText(requireContext(), "$msg", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(
                            requireContext(), VerifyEmailActivity::class.java
                        )
                    )
                } else {
                    if (response.errorBody() != null) {
                        val errorJSOn = response.errorBody()?.string()
                        Constants.errorHandler(
                            errorJSOn, requireContext()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun moveToSaveFragment() {
        val trans: FragmentTransaction = parentFragmentManager.beginTransaction()
        trans.replace(activityMainBinding.pager.id, SavedFilesFragment())
        trans.addToBackStack(null)
        trans.commit()
    }

    private fun openPdfPasteDialog() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.fragment_dialog_main)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnPDF = dialog.findViewById<ImageView>(R.id.msPdfRelLay)
        val btnDocx = dialog.findViewById<ImageView>(R.id.msDocxRelLay)
        val btnText = dialog.findViewById<ImageView>(R.id.msTextRelLay)
        val cancelBtn = dialog.findViewById<ImageView>(R.id.cancelBtn)

        btnPDF.setOnClickListener {
            intentOpenPDFfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentOpenPDFfile!!.type = "application/pdf"
            startActivityForResult(intentOpenPDFfile!!, OPEN_PDF_FILE_REQUEST_CODE)
            dialog.dismiss()
        }

        btnDocx.setOnClickListener {
            intentOpenDocxfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentOpenDocxfile!!.addCategory(Intent.CATEGORY_OPENABLE)
            intentOpenDocxfile!!.type = "*/*"
            val mimetypes = arrayOf(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword"
            )
            intentOpenDocxfile!!.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            startActivityForResult(intentOpenDocxfile!!, OPEN_DOCX_FILE_REQUEST_CODE)
            dialog.dismiss()
        }

        btnText.setOnClickListener {
            intentOpenTextfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentOpenTextfile!!.type = "text/plain"
            startActivityForResult(intentOpenTextfile!!, OPEN_TEXT_FILE_REQUEST_CODE)
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            try {
                fileUri = data!!.data
                val path = PathUtils.getPath(fileUri, requireContext())
                Log.d(TAG, "onActivityResult:path $path")
                val file = File(path)
                val length = file.length()
                Log.d(TAG, "length: $length")

                val cr: ContentResolver = requireActivity().contentResolver
                val mime: String = cr.getType(fileUri!!).toString()

                Log.d(TAG, "onActivityResult: mime:$mime")
                if (length <= 1572864/*(1.5MB)*/ && !Prefs.getBoolean("subscription", false)) {
                    if (requestCode == OPEN_PDF_FILE_REQUEST_CODE) {

                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                            rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                            GlobalScope.launch(Dispatchers.IO) {
                                var document: PDDocument? = null
                                var parsedText: String? = null
                                try {
                                    val inputStream: InputStream? =
                                        requireContext().contentResolver.openInputStream(fileUri!!)
                                    document = PDDocument.load(inputStream)
                                    if (document.isEncrypted) {
                                        encrypted = true
                                        document.close()
                                        GlobalScope.launch(Dispatchers.Main) {
                                            Toast.makeText(
                                                requireContext(),
                                                "pdf is encrypted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        val pdfStripper = PDFTextStripper()
                                        pdfStripper.startPage = 0
                                        pdfStripper.endPage = 5
                                        parsedText = pdfStripper.getText(document)
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    System.gc()
                                    encrypted = true
                                    e.printStackTrace()
                                } finally {
                                    try {
                                        document?.close()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (parsedText != null) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        rephraseParagraphBinding.rephraserET.setText(parsedText)
                                        isFirstTime = true
                                    } else {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "Please choose valid pdf",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                    }
                                }
                            }
                        }
                    } else if (requestCode == OPEN_TEXT_FILE_REQUEST_CODE) {
                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {

                            rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                            GlobalScope.launch(Dispatchers.IO) {
                                fileUri = data.data

                                val inputStream: InputStream? =
                                    requireContext().contentResolver.openInputStream(fileUri!!)
                                val br = BufferedReader(InputStreamReader(inputStream))
                                var line: String
                                var entireFile = ""
                                try {
                                    while (br.readLine().also { line = it } != null) {
                                        entireFile += """ $line """.trimIndent()
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    System.gc()
                                    e.printStackTrace()
                                } finally {
                                    try {
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (entireFile != null) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        rephraseParagraphBinding.rephraserET.setText(entireFile)
                                        isFirstTime = true
                                    } else {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "Please choose valid text file",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                    }
                                }
                            }
                        }
                    } else if (requestCode == OPEN_DOCX_FILE_REQUEST_CODE) {
                        val uri = fileUri.toString()

                        Log.d(TAG, "onActivityResult: path${fileUri!!.path}")
                        Log.d(TAG, "onActivityResult: $uri")

                        if (path.contains(".docx")) {
                            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                                rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                                GlobalScope.launch(Dispatchers.IO) {
                                    Log.d(TAG, "onActivityResult: $fileUri")
                                    var document: XWPFDocument? = null
                                    var parsedText: String? = null
                                    try {
                                        val inputStream: InputStream? =
                                            requireContext().contentResolver.openInputStream(fileUri!!)
                                        XWPFDocument(
                                            inputStream
                                        ).use { doc ->
                                            val xwpfWordExtractor = XWPFWordExtractor(doc)
                                            parsedText = xwpfWordExtractor.text
                                            GlobalScope.launch(Dispatchers.Main) {
                                                rephraseParagraphBinding.rephraserET.setText(
                                                    parsedText
                                                )
                                            }
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } finally {
                                        try {
                                            document?.close()
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            rephraseParagraphBinding.rephraserET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                "Please choose valid docx file",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                        }
                                    }
                                }
                            }
                        } else if (path.contains(".doc")) {
                            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                                rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                                GlobalScope.launch(Dispatchers.IO) {
                                    Log.d(TAG, "onActivityResult: $fileUri")
                                    var document: XWPFDocument? = null
                                    var parsedText: String? = null
                                    try {
                                        val inputStream: InputStream? =
                                            requireContext().contentResolver.openInputStream(fileUri!!)
                                        val wd = WordExtractor(inputStream)
                                        parsedText = wd.text
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.rephraserET.setText(parsedText)
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } catch (e: IllegalArgumentException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.choose_another_file,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } catch (e: TransactionTooLargeException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.choose_another_file,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } finally {
                                        try {
                                            document?.close()
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            rephraseParagraphBinding.rephraserET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                "Please choose valid doc file",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (Prefs.getBoolean("subscription", false)) {
                    if (requestCode == OPEN_PDF_FILE_REQUEST_CODE) {
                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                            rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                            GlobalScope.launch(Dispatchers.IO) {
                                var document: PDDocument? = null
                                var parsedText: String? = null
                                try {
                                    val inputStream: InputStream? =
                                        requireContext().contentResolver.openInputStream(fileUri!!)
                                    document = PDDocument.load(inputStream)
                                    if (document!!.isEncrypted) {
                                        encrypted = true
                                        document!!.close()
                                        GlobalScope.launch(Dispatchers.Main) {
                                            Toast.makeText(
                                                requireContext(),
                                                "pdf is encrypted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        val pdfStripper = PDFTextStripper()
                                        pdfStripper.startPage = 0
                                        pdfStripper.endPage = 5
                                        parsedText = pdfStripper.getText(document)
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    System.gc()
                                    encrypted = true
                                    e.printStackTrace()
                                } finally {
                                    try {
                                        document?.close()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (parsedText != null) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        rephraseParagraphBinding.rephraserET.setText(parsedText)
                                        isFirstTime = true
                                    } else {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "Please choose valid pdf",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                    }
                                }
                            }
                        }
                    } else if (requestCode == OPEN_TEXT_FILE_REQUEST_CODE) {
                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {

                            rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                            GlobalScope.launch(Dispatchers.IO) {
                                fileUri = data.data

                                val inputStream: InputStream? =
                                    requireContext().contentResolver.openInputStream(fileUri!!)
                                val br = BufferedReader(InputStreamReader(inputStream))
                                var line: String
                                var entireFile = ""
                                try {
                                    while (br.readLine().also { line = it } != null) {
                                        entireFile += """ $line """.trimIndent()
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                    }
                                    System.gc()
                                    e.printStackTrace()
                                } finally {
                                    try {
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (entireFile != null) {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        rephraseParagraphBinding.rephraserET.setText(entireFile)
                                        isFirstTime = true
                                    } else {
                                        rephraseParagraphBinding.progressRelative.visibility =
                                            View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "Please choose valid text file",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                    }
                                }
                            }
                        }
                    } else if (requestCode == OPEN_DOCX_FILE_REQUEST_CODE) {
                        val uri = fileUri.toString()

                        Log.d(TAG, "onActivityResult: path${fileUri!!.path}")
                        Log.d(TAG, "onActivityResult: $uri")

                        if (path.contains(".docx")) {
                            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                                rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                                GlobalScope.launch(Dispatchers.IO) {
                                    Log.d(TAG, "onActivityResult: $fileUri")
                                    var document: XWPFDocument? = null
                                    var parsedText: String? = null
                                    try {
                                        val inputStream: InputStream? =
                                            requireContext().contentResolver.openInputStream(fileUri!!)
                                        XWPFDocument(
                                            inputStream
                                        ).use { doc ->
                                            val xwpfWordExtractor = XWPFWordExtractor(doc)
                                            parsedText = xwpfWordExtractor.text
                                            GlobalScope.launch(Dispatchers.Main) {
                                                rephraseParagraphBinding.rephraserET.setText(
                                                    parsedText
                                                )
                                            }
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } finally {
                                        try {
                                            document?.close()
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            rephraseParagraphBinding.rephraserET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                "Please choose valid docx file",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                        }
                                    }
                                }
                            }
                        } else if (path.contains(".doc")) {
                            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                                rephraseParagraphBinding.progressRelative.visibility = View.VISIBLE
                                GlobalScope.launch(Dispatchers.IO) {
                                    Log.d(TAG, "onActivityResult: $fileUri")
                                    var document: XWPFDocument? = null
                                    var parsedText: String? = null
                                    try {
                                        val inputStream: InputStream? =
                                            requireContext().contentResolver.openInputStream(fileUri!!)
                                        val wd = WordExtractor(inputStream)
                                        parsedText = wd.text
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.rephraserET.setText(parsedText)
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } catch (e: IllegalArgumentException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.choose_another_file,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } catch (e: TransactionTooLargeException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.choose_another_file,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        System.gc()
                                        e.printStackTrace()
                                    } finally {
                                        try {
                                            document?.close()
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                        GlobalScope.launch(Dispatchers.Main) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            rephraseParagraphBinding.rephraserET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            rephraseParagraphBinding.progressRelative.visibility =
                                                View.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                "Please choose valid doc file",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.file_size_larger, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (nE: NullPointerException) {
                nE.printStackTrace()
                Toast.makeText(
                    requireContext(), R.string.choose_file_from_other_folder, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun takePermissionForReadData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionX.init(this).permissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "Core fundamental are based on these permissions", "OK", "Cancel"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }.request { allGranted, _, _ ->
                if (allGranted) {
                    moveToSaveFragment()
                } else {
                    permissionDenyDialog()
                }
            }
        } else {
            PermissionX.init(this).permissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "Core fundamental are based on these permissions", "OK", "Cancel"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }.request { allGranted, _, _ ->
                if (allGranted) {
                    moveToSaveFragment()
                } else {
                    permissionDenyDialog()
                }
            }
        }
    }

    private fun permissionDenyDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.permission_deny_dialog)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val okBtn = dialog.findViewById<Button>(R.id.ok_btn)
        okBtn.setOnClickListener {
            takePermissionForReadData()
            dialog.dismiss()
        }
        dialog.show()
    }
}