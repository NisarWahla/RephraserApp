package dzm.dzmedia.repheraserapp.fragments

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.databinding.FragmentDialogMainBinding
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

class DialogFragmentMainScr : DialogFragment() {

    private lateinit var dialogMainBinding: FragmentDialogMainBinding
    var encrypted = false
    private val OPEN_FILE_REQUEST_CODE = 1
    var intentOpenfile: Intent? = null
    var fileUri: Uri? = null
    lateinit var model: SharedViewModel


    override fun getTheme(): Int {
        return R.style.dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogMainBinding = FragmentDialogMainBinding.inflate(inflater, container, false)
        return dialogMainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogMainBinding.msTextRelLay.setOnClickListener {
            Utils.closeDialog(requireActivity().supportFragmentManager)
            val clipboard =
                (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE)) as? ClipboardManager
            val textToPaste = clipboard?.primaryClip?.getItemAt(0)?.text
        }

        PDFBoxResourceLoader.init(requireContext())
        dialogMainBinding.msPdfRelLay.setOnClickListener {
            intentOpenfile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentOpenfile!!.type = "application/pdf"
            startActivityForResult(intentOpenfile!!, OPEN_FILE_REQUEST_CODE)
            model = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        }
        dialogMainBinding.msDocxRelLay.setOnClickListener { }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GlobalScope.launch(Dispatchers.IO) {

            if (requestCode === OPEN_FILE_REQUEST_CODE) {
                if (resultCode === AppCompatActivity.RESULT_OK) {
                    fileUri = data!!.data
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
                        encrypted = true
                        e.printStackTrace()
                    } catch (e: IOException) {
                        encrypted = true
                        e.printStackTrace()
                    } finally {
                        try {
                            document?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        if (parsedText != null) {
                            Utils.closeDialog(requireActivity().supportFragmentManager)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Please choose valid pdf",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Utils.closeDialog(requireActivity().supportFragmentManager)
                        }
                    }
                }
            }
        }
    }
}