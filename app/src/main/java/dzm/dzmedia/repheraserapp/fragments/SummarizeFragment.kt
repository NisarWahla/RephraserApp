package dzm.dzmedia.repheraserapp.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.material.button.MaterialButton
import com.pixplicity.easyprefs.library.Prefs
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dzm.dzmedia.repheraserapp.App
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.activities.PremiumActivity
import dzm.dzmedia.repheraserapp.activities.SignInActivity
import dzm.dzmedia.repheraserapp.activities.VerifyEmailActivity
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.DataObjectModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.FragmentSummarizeBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.PathUtils
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.GetInfo.Data
import dzm.dzmedia.repheraserapp.model.GrammarCheckerFree.GrammarCheckerApiResponse
import dzm.dzmedia.repheraserapp.model.SummarizerFree.SummarizerFree
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

class SummarizeFragment : Fragment(), View.OnClickListener {

    private val TAG = SummarizeFragment::class.simpleName
    private lateinit var binding: FragmentSummarizeBinding
    var mCounts: Int = 0
    var encrypted = false
    private val OPEN_FILE_REQUEST_CODE = 1
    private val OPEN_DOCX_FILE_REQUEST_CODE = 2
    private val OPEN_TEXT_FILE_REQUEST_CODE = 3
    var intentOpenDocxfile: Intent? = null
    var intentOpenTextfile: Intent? = null
    var intentOpenfile: Intent? = null
    var fileUri: Uri? = null
    var isFirstTime = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSummarizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.delText.setOnClickListener(this)
        binding.btnUpload.setOnClickListener(this)
        binding.placeTextImg.setOnClickListener(this)
        binding.copyRephrasedText.setOnClickListener(this)
        binding.rpExport.setOnClickListener(this)
        binding.summarizeText.setOnClickListener(this)

        binding.summarizeOriginalET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.placeTextImg.visibility = View.GONE
                binding.placeText.visibility = View.GONE
                if (s.isEmpty()) {
                    binding.placeTextImg.visibility = View.VISIBLE
                    binding.placeText.visibility = View.VISIBLE
                }
                try {
                    val words: String = s.toString().trim()
                    mCounts =
                        if (words.isEmpty()) 0 else words.split("\\s+".toRegex())
                            .toTypedArray().size
                    binding.writenWords.text = "$mCounts"
                    if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                        //do login for free and without login user
                        if (mCounts >= MainActivity.freeInfo!!.free_max_limit + 1) {
                            binding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.red_clr
                                )
                            )
                        } else {
                            binding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.word_count_color
                                )
                            )
                        }
                    } else {
                        //change color if word limit excceeds.
                        if (mCounts >= MainActivity.userModel!!.word_limit + 1
                            && !MainActivity.userModel!!.subscribed
                            && !MainActivity.userModel!!.verified
                        ) {
                            binding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red_clr
                                )
                            )
                        } else if (mCounts >= MainActivity.userModel!!.word_limit + 1
                            && !MainActivity.userModel!!.subscribed
                            && MainActivity.userModel!!.verified
                        ) {
                            binding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red_clr
                                )
                            )
                        } else if (mCounts >= MainActivity.userModel!!.word_limit + 1 && MainActivity.userModel!!.subscribed) {
                            binding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red_clr
                                )
                            )
                        } else {
                            binding.writenWords.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.word_count_color
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
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        binding.summarizeOutputET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.resultHereText.visibility = View.GONE

                if (s.isEmpty()) {
                    binding.resultHereText.visibility = View.VISIBLE
                }
                try {
                    val words: String = s.toString().trim()
                    mCounts =
                        if (words.isEmpty()) 0 else words.split("\\s+".toRegex())
                            .toTypedArray().size
                    binding.rephrasedWritenWords.text = "$mCounts"
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (a: ArrayIndexOutOfBoundsException) {
                    a.printStackTrace()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        try {
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                val wordLimit = MainActivity.freeInfo!!.free_max_limit
                binding.totalWords.text = "/$wordLimit"
            } else {
                val wordLimit: Int = MainActivity.userModel!!.word_limit
                //set words limit according to payment.
                if (MainActivity.userModel!!.email == Prefs.getString(Constants.guestEmail)) {
                    binding.totalWords.text = "/$wordLimit"
                } else if (!MainActivity.userModel!!.subscribed && !MainActivity.userModel!!.verified) {
                    binding.totalWords.text = "/$wordLimit"
                } else if (!MainActivity.userModel!!.subscribed && MainActivity.userModel!!.verified) {
                    binding.totalWords.text = "/$wordLimit"
                } else {
                    binding.totalWords.text = "/$wordLimit"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ApiResponce(ip: String) {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        dialog.show()
        var intputText = binding.summarizeOriginalET.text.toString()
        var userID = MainActivity.userModel!!.user_id
        RetroClientRephraseText.APIClient().SendToSummarizeChecker(
            "Bearer " + MainActivity.userModel!!.token,
            intputText, Constants.Summarizer_ACTION_TYPE, userID, ip
        ).enqueue(object : Callback<RephraserApiResponce> {
            override fun onResponse(
                call: Call<RephraserApiResponce>,
                response: Response<RephraserApiResponce>
            ) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: $response")
                    val res = response.body()?.data
                    if (MainActivity.userModel!!.word_limit == 250) {
                        if (AdManger.isInterstialLoaded()) {
                            AdManger.showInterstial(
                                requireActivity(),
                                requireContext(),
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        binding.summarizeOutputET.setText(res!!.output_text)
                                        binding.resultHereText.visibility = View.GONE
                                        Log.d(TAG, "onResponse: $res")
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                        binding.summarizeOutputET.setText(res!!.output_text)
                                        binding.resultHereText.visibility = View.GONE
                                        Log.d(TAG, "onResponse: $res")
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        binding.summarizeOutputET.setText(res!!.output_text)
                                        binding.resultHereText.visibility = View.GONE
                                        Log.d(TAG, "onResponse: $res")
                                    }
                                })
                        }
                    } else {
                        binding.summarizeOutputET.setText(res!!.output_text)
                        binding.resultHereText.visibility = View.GONE
                        Log.d(TAG, "onResponse: $res")
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn,
                                requireContext()
                            )
                        } catch (exception: Exception) {
                            Toast.makeText(requireContext(), "Try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RephraserApiResponce>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "" + t.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun getSummarizerAttempt(ip: String) {
        val inputText = binding.summarizeOriginalET.text.toString()
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        dialog.show()
        RetrofitAuthenticationClient.AuthClient()
            .getSummarizerFree(
                Constants.FreeUserKey,
                inputText,
                Constants.REPHRASER_ACTION_TYPE,
                ip
            )
            .enqueue(object : Callback<SummarizerFree> {
                override fun onResponse(
                    call: Call<SummarizerFree>,
                    response: Response<SummarizerFree>
                ) {
                    try {
                        if (dialog != null && dialog.isShowing) dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    if (response.isSuccessful) {
                        val freeInfo: SummarizerFree =
                            response.body() as SummarizerFree
                        val mInfo = freeInfo.data
                        if (AdManger.isInterstialLoaded()) {
                            AdManger.showInterstial(requireActivity(),
                                requireContext(),
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        binding.summarizeOutputET.setText(
                                            mInfo.output_text
                                        )
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                        binding.summarizeOutputET.setText(
                                            mInfo.output_text
                                        )
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        binding.summarizeOutputET.setText(
                                            mInfo.output_text
                                        )
                                    }
                                })
                        }
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "Internet connection or server error\n plz restart your app." /*+ t.message*/,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SummarizerFree>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(
                        requireActivity(),
                        "Internet connection or server error\n plz restart your app." /*+ t.message*/,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.delText.id) {
            val containsOnlySpaces =
                binding.summarizeOriginalET.text.toString().trim().isEmpty()
            if (binding.summarizeOriginalET.text.toString().trim().isNotEmpty()) {
                binding.summarizeOriginalET.text.clear()
            } else if (containsOnlySpaces && binding.summarizeOriginalET.text.toString()
                    .isNotEmpty()
            ) {
                binding.summarizeOriginalET.text.clear()
            } else {
                Toast.makeText(requireActivity(), "No text to delete!", Toast.LENGTH_SHORT).show()
            }
        } else if (v?.id == binding.btnUpload.id) {
            val newText = binding.summarizeOriginalET.text.toString()
            if (isFirstTime) {
                if (newText == "") {
                    openPdfPasteDialog()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Please Delete Original Text!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                openPdfPasteDialog()
            }
        } else if (v?.id == binding.copyRephrasedText.id) {
            if (binding.summarizeOutputET.text.toString().trim() == "") {
                Toast.makeText(requireContext(), "No text available to copy!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                copyText(binding.summarizeOutputET.text.toString())
                Toast.makeText(requireContext(), "Text copied", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (v?.id == binding.rpExport.id) {
            hideKeyboard(requireActivity())
            if (binding.summarizeOutputET.text.toString().trim() == "") {
                Toast.makeText(requireContext(), "Please generate some text", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val exportTextDialogFragment = ExportTextDialogFragment(
                    binding.summarizeOutputET.text.toString().trim(),
                    binding.summarizeOriginalET.text.toString().trim()
                )
                Utils.openDialog(requireActivity().supportFragmentManager, exportTextDialogFragment)
            }
        } else if (v?.id == binding.summarizeText.id) {
            Log.d(TAG, "onClick:value $mCounts")
            hideKeyboard(requireActivity())
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                //do logic for free and without login user
                if (binding.summarizeOriginalET.text.toString().trim() == "") {
                    Toast.makeText(
                        requireContext(),
                        "Upload file or write text!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!isOnline(requireContext())) {
                    Toast.makeText(
                        requireContext(),
                        "No internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (mCounts >= MainActivity.freeInfo!!.free_max_limit) {
                    upGradeToPremium()
                } else {
                    val ip: String = Constants.getIpAddress(requireActivity())
                    getSummarizerAttempt(ip)
                }
            } else {
                //do logic for paid and login user
                if (binding.summarizeOriginalET.text.toString().trim() == "") {
                    Toast.makeText(
                        requireContext(),
                        "Upload file or write text!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!MainActivity.userModel!!.verified
                    && MainActivity.userModel!!.email != Constants.guestEmail
                    && mCounts >= MainActivity.userModel!!.word_limit + 1
                ) {
                    //VerifyEmailPopup()
                    upGradeToPremium()
                } else if (mCounts >= MainActivity.userModel!!.word_limit + 1
                    && !MainActivity.userModel!!.subscribed
                    && !MainActivity.userModel!!.verified
                ) {
                    upGradeToPremium()
                } else if (mCounts >= MainActivity.userModel!!.word_limit + 1
                    && !MainActivity.userModel!!.subscribed
                    && MainActivity.userModel!!.verified
                ) {
                    upGradeToPremium()
                } else if (mCounts >= 1501 && MainActivity.userModel!!.subscribed) {
                    Toast.makeText(
                        requireContext(),
                        "Words can't be more than 1500",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (MainActivity.userModel!!.email == Prefs.getString(Constants.guestEmail)
                    && mCounts >= MainActivity.userModel!!.word_limit + 1
                ) {
                    Toast.makeText(
                        requireContext(),
                        "register yourself to get more features",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (!isOnline(requireContext())) {
                    Toast.makeText(
                        requireContext(),
                        "No internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        val ip: String = Constants.getIpAddress(requireActivity())
                        ApiResponce(ip)
                    }
                }
            }
        } else if (v?.id == binding.placeTextImg.id) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            val item = clip?.getItemAt(0)
            val textToPaste = item?.text
            binding.summarizeOriginalET.setText(textToPaste.toString())
            binding.summarizeOriginalET.setSelection(binding.summarizeOriginalET.text.length)
            isFirstTime = true
        }
    }

    private fun upGradeToPremium() {
        startActivity(Intent(requireContext(), PremiumActivity::class.java))
/*        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.upgrade_to_premium_dialog)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnCross = dialog.findViewById<ImageView>(R.id.cross_btn)
        val btnVerifyNow = dialog.findViewById<AppCompatButton>(R.id.verifyNow)
        btnCross.setOnClickListener {
            dialog.dismiss()
        }
        btnVerifyNow.setOnClickListener {
            if (MainActivity.userModel!!.email != Constants.guestEmail) {
                startActivity(
                    Intent(
                        requireContext(),
                        PremiumActivity::class.java
                    )
                )
            } else {
                startActivity(Intent(requireContext(), SignInActivity::class.java))
                dialog.dismiss()
            }
        }
        dialog.show()*/
    }

    private fun VerifyEmailPopup() {
        val dialog = Dialog(requireContext())
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
            verifyEmailApiResponce()
        }
        dialog.show()
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val email = MainActivity.userModel!!.email

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
                        Toast.makeText(requireContext(), "$msg", Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(
                                requireContext(),
                                VerifyEmailActivity::class.java
                            )
                        )
                    } else {
                        if (response.errorBody() != null) {
                            val errorJSOn = response.errorBody()?.string()
                            Constants.errorHandler(
                                errorJSOn,
                                requireContext()
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<AuthenticationBaseModel>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun openPdfPasteDialog() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.fragment_dialog_main)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val btnPDF = dialog.findViewById<ImageView>(R.id.msPdfRelLay)
        val btnDocx = dialog.findViewById<ImageView>(R.id.msDocxRelLay)
        val btnPasteText = dialog.findViewById<ImageView>(R.id.msTextRelLay)
        val cancelBtn = dialog.findViewById<ImageView>(R.id.cancelBtn)

        btnPDF.setOnClickListener {
            intentOpenfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentOpenfile!!.type = "application/pdf"
            startActivityForResult(intentOpenfile!!, OPEN_FILE_REQUEST_CODE)
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

        btnPasteText.setOnClickListener {
            intentOpenTextfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentOpenTextfile!!.type =
                "text/plain"
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
                val file = File(path)
                val length = file.length()
                Log.d(TAG, "length: $length")
                if (length <= 1572864/*(1.5MB)*/ && !Prefs.getBoolean("subscription", false)) {
                    if (requestCode == OPEN_FILE_REQUEST_CODE) {
                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                            binding.progressRelative.visibility = View.VISIBLE
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
                                            )
                                                .show()
                                        }
                                    } else {
                                        val pdfStripper = PDFTextStripper()
                                        pdfStripper.startPage = 0
                                        pdfStripper.endPage = 5
                                        parsedText = pdfStripper.getText(document)
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
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
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (parsedText != null) {
                                        binding.progressRelative.visibility = View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        binding.summarizeOriginalET.setText(parsedText)
                                        isFirstTime = true
                                    } else {
                                        binding.progressRelative.visibility = View.GONE
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
                    } else if (requestCode == OPEN_DOCX_FILE_REQUEST_CODE) {
                        if (path.contains(".docx")) {
                            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                                binding.progressRelative.visibility = View.VISIBLE
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
                                                binding.summarizeOriginalET.setText(parsedText)
                                            }
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.summarizeOriginalET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            binding.progressRelative.visibility = View.GONE
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
                                binding.progressRelative.visibility = View.VISIBLE
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
                                            binding.summarizeOriginalET.setText(parsedText)
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.summarizeOriginalET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            binding.progressRelative.visibility = View.GONE
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
                    } else if (requestCode == OPEN_TEXT_FILE_REQUEST_CODE) {
                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {

                            binding.progressRelative.visibility = View.VISIBLE
                            GlobalScope.launch(Dispatchers.IO) {
                                fileUri = data.data

                                val inputStream: InputStream? =
                                    requireContext().contentResolver.openInputStream(fileUri!!)
                                val br = BufferedReader(InputStreamReader(inputStream))
                                var line: String
                                var entireFile = ""
                                try {
                                    while (br.readLine()
                                            .also { line = it } != null
                                    ) {
                                        entireFile += """ $line """.trimIndent()
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    System.gc()
                                    e.printStackTrace()
                                } finally {
                                    try {
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (entireFile != null) {
                                        binding.progressRelative.visibility = View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        binding.summarizeOriginalET.setText(entireFile)
                                        isFirstTime = true
                                    } else {
                                        binding.progressRelative.visibility = View.GONE
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
                    }
                } else if (Prefs.getBoolean("subscription", false)) {
                    if (requestCode == OPEN_FILE_REQUEST_CODE) {
                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                            binding.progressRelative.visibility = View.VISIBLE
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
                                            )
                                                .show()
                                        }
                                    } else {
                                        val pdfStripper = PDFTextStripper()
                                        pdfStripper.startPage = 0
                                        pdfStripper.endPage = 5
                                        parsedText = pdfStripper.getText(document)
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    encrypted = true
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
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
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (parsedText != null) {
                                        binding.progressRelative.visibility = View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        binding.summarizeOriginalET.setText(parsedText)
                                        isFirstTime = true
                                    } else {
                                        binding.progressRelative.visibility = View.GONE
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
                    } else if (requestCode == OPEN_DOCX_FILE_REQUEST_CODE) {
                        if (path.contains(".docx")) {
                            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                                binding.progressRelative.visibility = View.VISIBLE
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
                                                binding.summarizeOriginalET.setText(parsedText)
                                            }
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.summarizeOriginalET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            binding.progressRelative.visibility = View.GONE
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
                                binding.progressRelative.visibility = View.VISIBLE
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
                                            binding.summarizeOriginalET.setText(parsedText)
                                        }
                                    } catch (e: NullPointerException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: IOException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                        e.printStackTrace()
                                    } catch (e: OutOfMemoryError) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility = View.GONE
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.summarizeOriginalET.setText(parsedText)
                                            isFirstTime = true
                                        } else {
                                            binding.progressRelative.visibility = View.GONE
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
                    } else if (requestCode == OPEN_TEXT_FILE_REQUEST_CODE) {
                        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {

                            binding.progressRelative.visibility = View.VISIBLE
                            GlobalScope.launch(Dispatchers.IO) {
                                fileUri = data.data

                                val inputStream: InputStream? =
                                    requireContext().contentResolver.openInputStream(fileUri!!)
                                val br = BufferedReader(InputStreamReader(inputStream))
                                var line: String
                                var entireFile = ""
                                try {
                                    while (br.readLine()
                                            .also { line = it } != null
                                    ) {
                                        entireFile += """ $line """.trimIndent()
                                    }
                                } catch (e: NullPointerException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    e.printStackTrace()
                                } catch (e: OutOfMemoryError) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                    System.gc()
                                    e.printStackTrace()
                                } finally {
                                    try {
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        binding.progressRelative.visibility = View.GONE
                                    }
                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (entireFile != null) {
                                        binding.progressRelative.visibility = View.GONE
                                        Utils.closeDialog(requireActivity().supportFragmentManager)
                                        binding.summarizeOriginalET.setText(entireFile)
                                        isFirstTime = true
                                    } else {
                                        binding.progressRelative.visibility = View.GONE
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
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.file_size_larger, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (nE: NullPointerException) {
                nE.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    R.string.choose_file_from_other_folder,
                    Toast.LENGTH_SHORT
                ).show()
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

    fun copyText(text: String) {
        val myClipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
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
}