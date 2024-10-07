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
import android.os.TransactionTooLargeException
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.material.button.MaterialButton
import com.pixplicity.easyprefs.library.Prefs
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.activities.PremiumActivity
import dzm.dzmedia.repheraserapp.activities.VerifyEmailActivity
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.authentication_api.AuthenticationBaseModel
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.FragmentGrammarCheckerBinding
import dzm.dzmedia.repheraserapp.helpers.Constants
import dzm.dzmedia.repheraserapp.helpers.PathUtils
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.GrammarCheckerFree.GrammarCheckerApiResponse
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

class GrammarCheckerFragment : Fragment(), View.OnClickListener {
    private val TAG = GrammarCheckerFragment::class.simpleName
    private lateinit var binding: FragmentGrammarCheckerBinding
    var isFirstTime = false
    var intentOpenfile: Intent? = null
    var intentOpenDocxfile: Intent? = null
    var intentOpenTextfile: Intent? = null
    private val OPEN_FILE_REQUEST_CODE = 1
    private val OPEN_DOCX_FILE_REQUEST_CODE = 2
    private val OPEN_TEXT_FILE_REQUEST_CODE = 3
    var fileUri: Uri? = null
    var encrypted = false
    var mCounts: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGrammarCheckerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUpload.setOnClickListener(this)
        binding.btnDownload.setOnClickListener(this)
        binding.fixIssues.setOnClickListener(this)
        binding.deleteText.setOnClickListener(this)
        binding.copyText.setOnClickListener(this)
        binding.placeTextImg.setOnClickListener(this)

        binding.grammarCheckerInputAndOutputET.addTextChangedListener(object : TextWatcher {
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
                        //do logic for paid and login user
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
                            /*if (!MainActivity.userModel!!.verified && MainActivity.userModel!!.email != Constants.guestEmail
                            ) {
                                ///VerifyEmailPopup()
                                upGradeToPremium()
                            }*/
                        } else if (mCounts >= MainActivity.userModel!!.word_limit + 1 && !MainActivity.userModel!!.subscribed && MainActivity.userModel!!.verified) {
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

        binding.grammarCheckedET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.noResult.visibility = View.GONE

                if (s.isEmpty()) {
                    binding.noResult.visibility = View.VISIBLE
                }
                try {
                    val words: String = s.toString().trim()
                    mCounts = if (words.isEmpty()) 0 else words.split("\\s+".toRegex())
                        .toTypedArray().size
                    binding.grammarCheckedWritenWords.text = "$mCounts"
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

        try {
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                //do logic for free and without login user
                val wordLimit = MainActivity.freeInfo!!.free_max_limit
                binding.totalWords.text = "/$wordLimit"
            } else {
                //do logic for paid and login user
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
        Log.d(TAG, "ApiResponce: ${MainActivity.userModel?.user_id}")

        var intputText = binding.grammarCheckerInputAndOutputET.text.toString()
        var userID = MainActivity.userModel!!.user_id
        Log.i(TAG, "ApiResponse:grammar token: ${MainActivity.userModel?.token}, inputText: $intputText, actionType: ${Constants.GRAMMAR_CHECKER_ACTION_TYPE}, userId: $userID, ip: $ip")
        RetroClientRephraseText.APIClient().SendToGrammarChecker(
            "Bearer " + MainActivity.userModel!!.token,
            intputText,
            Constants.GRAMMAR_CHECKER_ACTION_TYPE,
            userID,
            ip
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
                                        binding.grammarCheckedET.setText(res!!.output_text)
                                        binding.placeText.visibility = View.GONE
                                        binding.placeTextImg.visibility = View.GONE
                                        Log.d(TAG, "onResponse: $res")
                                        (requireActivity() as MainActivity).generated = true
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                        binding.grammarCheckedET.setText(res!!.output_text)
                                        binding.placeText.visibility = View.GONE
                                        binding.placeTextImg.visibility = View.GONE
                                        Log.d(TAG, "onResponse: $res")
                                        (requireActivity() as MainActivity).generated = true
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        binding.grammarCheckedET.setText(res!!.output_text)
                                        binding.placeText.visibility = View.GONE
                                        binding.placeTextImg.visibility = View.GONE
                                        Log.d(TAG, "onResponse: $res")
                                        (requireActivity() as MainActivity).generated = true
                                    }
                                })
                        }
                    } else {
                        binding.grammarCheckedET.setText(res!!.output_text)
                        binding.placeText.visibility = View.GONE
                        binding.placeTextImg.visibility = View.GONE
                        Log.d(TAG, "onResponse: $res")
                        (requireActivity() as MainActivity).generated = true
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

    private fun getGrammarCheckerFreeAttempt(ip: String) {
        val inputText = binding.grammarCheckerInputAndOutputET.text.toString()
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        dialog.show()
        RetrofitAuthenticationClient.AuthClient()
            .getGrammarChecker(
                Constants.FreeUserKey,
                inputText,
                Constants.REPHRASER_ACTION_TYPE,
                ip
            )
            .enqueue(object : Callback<GrammarCheckerApiResponse> {
                override fun onResponse(
                    call: Call<GrammarCheckerApiResponse>,
                    response: Response<GrammarCheckerApiResponse>
                ) {
                    try {
                        if (dialog != null && dialog.isShowing) dialog.dismiss()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    if (response.isSuccessful) {
                        val freeInfo: GrammarCheckerApiResponse =
                            response.body() as GrammarCheckerApiResponse
                        val mInfo = freeInfo.data
                        if (AdManger.isInterstialLoaded()) {
                            AdManger.showInterstial(requireActivity(),
                                requireContext(),
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        (requireActivity() as MainActivity).generated = true
                                        binding.grammarCheckedET.setText(
                                            mInfo.output_text
                                        )
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                        (requireActivity() as MainActivity).generated = true
                                        binding.grammarCheckedET.setText(
                                            mInfo.output_text
                                        )
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        (requireActivity() as MainActivity).generated = true
                                        binding.grammarCheckedET.setText(
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

                override fun onFailure(call: Call<GrammarCheckerApiResponse>, t: Throwable) {
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
        if (v?.id == binding.btnUpload.id) {
            try {
                val newText = binding.grammarCheckerInputAndOutputET.text.toString()
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (v?.id == binding.fixIssues.id) {
            hideKeyboard(requireActivity())
            if (!Prefs.getBoolean(Constants.IS_LOGIN, false)) {
                //do logic for free and without login user
                if (binding.grammarCheckerInputAndOutputET.text.toString().trim() == "") {
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
                    getGrammarCheckerFreeAttempt(ip)
                }

            } else {
                // do logic for paid and login user
                if (binding.grammarCheckerInputAndOutputET.text.toString().trim() == "") {
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
                } else if (MainActivity.userModel!!.email == Prefs.getString(Constants.guestEmail)
                    && mCounts >= MainActivity.userModel!!.word_limit + 1
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Register yourself to get more features",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (mCounts >= 1501 && MainActivity.userModel!!.subscribed) {
                    Toast.makeText(
                        requireContext(),
                        "Words can't be more than 1500",
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
        } else if (v?.id == binding.deleteText.id) {
            val containsOnlySpaces = binding.grammarCheckerInputAndOutputET.text.toString().trim().isEmpty()
            if (binding.grammarCheckerInputAndOutputET.text.toString().trim().isNotEmpty()) {
                binding.grammarCheckerInputAndOutputET.text.clear()
            } else if (containsOnlySpaces && binding.grammarCheckerInputAndOutputET.text.toString().isNotEmpty()) {
                binding.grammarCheckerInputAndOutputET.text.clear()
            } else {
                Toast.makeText(requireActivity(), "No text to delete!", Toast.LENGTH_SHORT).show()
            }
        } else if (v?.id == binding.placeTextImg.id) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            val item = clip?.getItemAt(0)
            val textToPaste = item?.text
            binding.grammarCheckerInputAndOutputET.setText(textToPaste.toString())
            binding.grammarCheckerInputAndOutputET.setSelection(binding.grammarCheckerInputAndOutputET.text.length)
            isFirstTime = true
        } else if (v?.id == binding.copyText.id) {
            if (binding.grammarCheckerInputAndOutputET.text.toString().trim() == "") {
                Toast.makeText(requireContext(), "No text available to copy!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                copyText(binding.grammarCheckerInputAndOutputET.text.toString())
                Toast.makeText(requireContext(), "Text copied", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (v?.id == binding.btnDownload.id) {
            hideKeyboard(requireActivity())
            if (binding.grammarCheckerInputAndOutputET.text.toString().trim() == "") {
                Toast.makeText(
                    requireContext(),
                    "Please generate some text",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else if (binding.grammarCheckerInputAndOutputET.text.toString().trim() != ""
                && !(requireActivity() as MainActivity).generated
            ) {
                Toast.makeText(
                    requireContext(),
                    "Please generate text",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val exportTextDialogFragment = ExportTextDialogFragment(
                    binding.grammarCheckedET.text.toString().trim(),
                    binding.grammarCheckerInputAndOutputET.text.toString().trim()
                )
                Utils.openDialog(
                    requireActivity().supportFragmentManager,
                    exportTextDialogFragment
                )
            }
        }
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

    private fun upGradeToPremium() {
        startActivity(Intent(requireContext(), PremiumActivity::class.java))
        /*val dialog = Dialog(requireContext())
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

    fun copyText(text: String) {
        val myClipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
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
        val btnPDF = dialog.findViewById<ImageButton>(R.id.msPdfRelLay)
        val btnDocx = dialog.findViewById<ImageButton>(R.id.msDocxRelLay)
        val btnPasteText = dialog.findViewById<ImageButton>(R.id.msTextRelLay)
        val cancelBtn = dialog.findViewById<ImageView>(R.id.cancelBtn)

        btnPDF.setOnClickListener {
            try {
                dialog.dismiss()
                intentOpenfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intentOpenfile!!.type = "application/pdf"
                getPDFFilesLauncher.launch(intentOpenfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btnDocx.setOnClickListener {
            try {
                dialog.dismiss()
                intentOpenDocxfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intentOpenDocxfile!!.type = "*/*"
                val mimetypes = arrayOf(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword"
                )
                intentOpenDocxfile!!.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
                getWORDFilesLauncher.launch(intentOpenDocxfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btnPasteText.setOnClickListener {
            try {
                dialog.dismiss()
                intentOpenTextfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intentOpenTextfile!!.type =
                    "text/plain"
                getTEXTFileLauncher.launch(intentOpenTextfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private var getPDFFilesLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            try {
                val data: Intent = it.data!!
                fileUri = data!!.data

                val path = PathUtils.getPath(fileUri, requireContext())

                val file = File(/*fileUri?.path*/path)
                val length = file.length()
                Log.d(TAG, "length: $length")

                if (length <= /*1048576*/1572864/*(1.5MB)*/ && !Prefs.getBoolean(
                        "subscription",
                        false
                    )
                ) {
                    binding.progressRelative.visibility = View.VISIBLE
                    GlobalScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "onActivityResult: $fileUri")
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
                                binding.grammarCheckerInputAndOutputET.setText(parsedText)
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
                } else if (Prefs.getBoolean("subscription", false)) {
                    binding.progressRelative.visibility = View.VISIBLE
                    GlobalScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "onActivityResult: $fileUri")
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
                                binding.grammarCheckerInputAndOutputET.setText(parsedText)
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
                } else {
                    Toast.makeText(requireContext(), R.string.file_size_larger, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (nE: Exception) {
                nE.printStackTrace()
                Toast.makeText(
                    requireContext(), R.string.choose_file_from_other_folder, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private var getWORDFilesLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            try {
                val data: Intent = it.data!!
                fileUri = data!!.data
                val path = PathUtils.getPath(fileUri, requireContext())
                val file = File(path)
                val length = file.length()
                Log.d(TAG, "length: $length")
                if (length <= 1572864/*(1.5MB)*/ && !Prefs.getBoolean("subscription", false)) {
                    if (path.contains(".docx")) {
                        if (it.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                            try {
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
                                                binding.grammarCheckerInputAndOutputET.setText(
                                                    parsedText
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
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
                                    } catch (e: IllegalArgumentException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.grammarCheckerInputAndOutputET.setText(
                                                parsedText
                                            )
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
                            } catch (e: TransactionTooLargeException) {
                                e.printStackTrace()
                            }
                        }
                    } else if (path.contains(".doc")) {
                        if (it.resultCode == AppCompatActivity.RESULT_OK) {

                            try {
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
                                            binding.grammarCheckerInputAndOutputET.setText(
                                                parsedText
                                            )
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
                                    } catch (e: IllegalArgumentException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.grammarCheckerInputAndOutputET.setText(
                                                parsedText
                                            )
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
                            } catch (e: TransactionTooLargeException) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else if (Prefs.getBoolean("subscription", false)) {
                    if (path.contains(".docx")) {
                        if (it.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                            try {
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
                                                binding.grammarCheckerInputAndOutputET.setText(
                                                    parsedText
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
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
                                    } catch (e: IllegalArgumentException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.grammarCheckerInputAndOutputET.setText(
                                                parsedText
                                            )
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
                            } catch (e: TransactionTooLargeException) {
                                e.printStackTrace()
                            }
                        }
                    } else if (path.contains(".doc")) {
                        if (it.resultCode == AppCompatActivity.RESULT_OK) {

                            try {
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
                                            binding.grammarCheckerInputAndOutputET.setText(
                                                parsedText
                                            )
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
                                    } catch (e: IllegalArgumentException) {
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility =
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
                                            binding.progressRelative.visibility = View.GONE
                                        }
                                    }
                                    GlobalScope.launch(Dispatchers.Main) {
                                        if (parsedText != null) {
                                            binding.progressRelative.visibility = View.GONE
                                            Utils.closeDialog(requireActivity().supportFragmentManager)
                                            binding.grammarCheckerInputAndOutputET.setText(
                                                parsedText
                                            )
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
                            } catch (e: TransactionTooLargeException) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.file_size_larger, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(
                    requireContext(), R.string.choose_file_from_other_folder, Toast.LENGTH_SHORT
                ).show()
            }

        }
    }


    private var getTEXTFileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            try {
                val data: Intent = it.data!!
                fileUri = data!!.data
                val path = PathUtils.getPath(fileUri, requireContext())
                val file = File(path)
                val length = file.length()
                Log.d(TAG, "length: $length")
                if (length <= 1572864/*(1.5MB)*/ && !Prefs.getBoolean("subscription", false)) {
                    if (it.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                        binding.progressRelative.visibility = View.VISIBLE
                        GlobalScope.launch(Dispatchers.IO) {
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
                                    binding.grammarCheckerInputAndOutputET.setText(entireFile)
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
                } else if (Prefs.getBoolean("subscription", false)) {
                    if (it.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                        binding.progressRelative.visibility = View.VISIBLE
                        GlobalScope.launch(Dispatchers.IO) {
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
                                    binding.grammarCheckerInputAndOutputET.setText(entireFile)
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
                } else {
                    Toast.makeText(requireContext(), R.string.file_size_larger, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (nE: Exception) {
                nE.printStackTrace()
                Toast.makeText(
                    requireContext(), R.string.choose_file_from_other_folder, Toast.LENGTH_SHORT
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