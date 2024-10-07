package dzm.dzmedia.repheraserapp.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import dzm.dzmedia.repheraserapp.databinding.FragmentArticleGeneratorBinding
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.ArticalTItleAndResponce
import dzm.dzmedia.repheraserapp.retrofit.RetroClientRephraseText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticleGeneratorFragment : Fragment(), View.OnClickListener {

    private val TAG = ArticleGeneratorFragment::class.simpleName
    private lateinit var binding: FragmentArticleGeneratorBinding
    val wordLimit: Int = 1000
    var mCounts: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.copyText.setOnClickListener(this)
        binding.btnDownload.setOnClickListener(this)
        binding.generteBtn.setOnClickListener(this)

        binding.totalWords.text = "/$wordLimit"
        binding.articleOutputET.addTextChangedListener(object : TextWatcher {

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
                    binding.writenWords.text = "Word Count:$mCounts"
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
    }

    fun ApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val title = binding.enterTitleEt.text.toString().trim()
        RetroClientRephraseText.APIClient().SendToArticalGenarator(title)
            .enqueue(object : Callback<ArticalTItleAndResponce> {
                override fun onResponse(
                    call: Call<ArticalTItleAndResponce>,
                    response: Response<ArticalTItleAndResponce>
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: $response")
                        dialog.dismiss()
                        val res = response.body()?.generatedArtical
                        binding.articleOutputET.setText(res)
                        binding.resultHereText.visibility = View.GONE
                        Log.d(TAG, "onResponse: $res")
                    }
                }

                override fun onFailure(call: Call<ArticalTItleAndResponce>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.copyText.id) {
            if (binding.articleOutputET.text.toString().trim() == "") {
                Toast.makeText(requireContext(), "No text available to copy!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                copyText(binding.articleOutputET.text.toString())
                Toast.makeText(requireContext(), "copied", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (v?.id == binding.btnDownload.id) {
            hideKeyboard(requireActivity())
            if (binding.articleOutputET.text.toString().trim() == "") {
                Toast.makeText(
                    requireContext(),
                    "Please generate artical first!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val exportTextDialogFragment = ExportTextDialogFragment(
                    binding.articleOutputET.text.toString().trim(),
                    binding.articleOutputET.text.toString().trim()
                )
                Utils.openDialog(requireActivity().supportFragmentManager, exportTextDialogFragment)
            }
        } else if (v?.id == binding.generteBtn.id) {
            hideKeyboard(requireActivity())
            if (binding.enterTitleEt.text.toString().trim() == "") {
                Toast.makeText(
                    requireContext(),
                    "Enter title to generate artical!",
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
                    ApiResponce()
                }
            }
        }
    }

    fun copyText(text: String) {
        val myClipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
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
