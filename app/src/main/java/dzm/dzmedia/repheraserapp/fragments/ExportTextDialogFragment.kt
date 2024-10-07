package dzm.dzmedia.repheraserapp.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PDFPrint
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.permissionx.guolindev.PermissionX
import com.pixplicity.easyprefs.library.Prefs
import com.tejpratapsingh.pdfcreator.utils.PDFUtil
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.admob.AdManger
import dzm.dzmedia.repheraserapp.databinding.FragmentExportTextDialogBinding
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.model.SharedViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter


class ExportTextDialogFragment(var txtToBePDF: String, var desc: String) :
    DialogFragment() {

    override fun getTheme(): Int {
        return R.style.dialog
    }

    private lateinit var binding: FragmentExportTextDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExportTextDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AdManger.loadInterstial(requireContext())

        binding.saveAsDocx.setOnClickListener {
            if (binding.editText.text.toString().trim() == "") {
                binding.editText.error = "File name cannot be empty"
            } else {
                takePermissionForDOC()
                hideKeyboard(requireActivity())
            }
        }

        binding.saveAsPdf.setOnClickListener {
            if (binding.editText.text.toString().trim() == "") {
                binding.editText.error = "File name cannot be empty"
            } else {
                takePermissionforPDF()
                hideKeyboard(requireActivity())
            }
        }

        binding.saveAsText.setOnClickListener {
            if (binding.editText.text.toString().trim() == "") {
                binding.editText.error = "File name cannot be empty"
            } else {
                takePermissionforText()
                hideKeyboard(requireActivity())
            }
        }

        binding.closeBtn.setOnClickListener {
            Utils.closeDialog(requireActivity().supportFragmentManager)
        }
    }

    private fun permissionDenyDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.permission_deny_dialog)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val okBtn = dialog.findViewById<Button>(R.id.ok_btn)
        okBtn.setOnClickListener {
            takePermissionForDOC()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun takePermissionForDOC() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        saveDocFile()
                        val model =
                            ViewModelProvider(requireActivity())[SharedViewModel::class.java]
                        model.message.value = null
                        txtToBePDF.equals(null)

                    } else {
                        permissionDenyDialog()
                    }
                }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            saveDocFile()
            val model =
                ViewModelProvider(requireActivity())[SharedViewModel::class.java]
            model.message.value = null
            txtToBePDF.equals(null)
        } else {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        saveDocFile()
                        val model =
                            ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
                        model.message.value = null
                        txtToBePDF.equals(null)

                    } else {
                        permissionDenyDialog()
                    }
                }
        }
    }

    fun saveDocFile() {
        val directory_path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val app_path: String =
                requireContext().getExternalFilesDir("Rephraser")!!.absolutePath
            "$app_path/"
        } else {
            Environment.getExternalStorageDirectory().path + "/Rephraser/"
        }
        val file = File(directory_path)
        if (!file.exists()) {
            file.mkdirs()
            file.mkdir()
        }
        try {
            val gpxfile = File(
                directory_path, binding.editText.text.toString() + ".doc"
            )
            if (gpxfile.exists()) {
                Toast.makeText(requireContext(), "File Already Exist", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (!Prefs.getBoolean("subscription", false)) {
                    if (AdManger.isInterstialLoaded()) {
                        AdManger.showInterstial(
                            requireActivity(),
                            requireContext(),
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    saveDOC(gpxfile)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    saveDOC(gpxfile)
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    saveDOC(gpxfile)
                                }
                            })
                    }
                } else {
                    saveDOC(gpxfile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Pdfexceptionnn", e.message.toString())
        }
    }

    fun saveDOC(gpxfile: File) {
        var writer: FileWriter? = null
        writer = FileWriter(gpxfile)
        writer.append(txtToBePDF)
        writer.flush()
        writer.close()
        Utils.closeDialog(requireActivity().supportFragmentManager)
        Toast.makeText(
            requireContext(),
            "Doc saved Successfully",
            Toast.LENGTH_LONG
        ).show()
        hideKeyboard(requireActivity())

    }

    private fun takePermissionforPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        savePdfFile()
                        val model =
                            ViewModelProvider(requireActivity())[SharedViewModel::class.java]
                        model.message.value = null
                        txtToBePDF.equals(null)
                    } else {
                        permissionDenyDialog()
                    }
                }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            savePdfFile()
            val model =
                ViewModelProvider(requireActivity())[SharedViewModel::class.java]
            model.message.value = null
            txtToBePDF.equals(null)
        } else {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        savePdfFile()
                        val model =
                            ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
                        model.message.value = null
                        txtToBePDF.equals(null)
                    } else {
                        permissionDenyDialog()
                    }
                }
        }
    }

    fun savePdfFile() {
        val directory_path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val app_path: String =
                requireContext().getExternalFilesDir("Rephraser")!!.absolutePath
            "$app_path/"
        } else {
            Environment.getExternalStorageDirectory().path + "/Rephraser/"
        }
        val file = File(directory_path)
        Log.i("TAG", "savePdfFile: ${file.path}")
        if (!file.exists()) {
            file.mkdirs()
        }

        val targetPdf = directory_path + binding.editText.text.toString() + ".pdf"
        Log.i("ExportDialog", "savePdfFile: $targetPdf")
        val filePath = File(targetPdf)
        if (filePath.exists()) {
            Toast.makeText(requireContext(), "File Already Exist", Toast.LENGTH_SHORT).show()
        } else {
            if (!Prefs.getBoolean("subscription", false)) {
                if (AdManger.isInterstialLoaded()) {
                    AdManger.showInterstial(
                        requireActivity(),
                        requireContext(),
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                savePDF(filePath)
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                savePDF(filePath)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                savePDF(filePath)
                            }
                        })
                }
            } else {
                savePDF(filePath)
            }
        }
    }

    fun savePDF(filePath: File) {
        try {
            val htText = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<style>\n" +
                    "p {\n" +
                    "  letter-spacing: 1.5px;\n" +
                    "  text-align: justified;\n" +
                    "  font-size: 21px;\n" +
                    "}\n" +
                    "</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<p>$txtToBePDF</p>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>\n" +
                    "\n" +
                    "\n"

            PDFUtil.generatePDFFromHTML(requireContext(), filePath,
                htText, object : PDFPrint.OnPDFPrintListener {
                    override fun onSuccess(file: File?) {
                        // Open Pdf Viewer
                        Utils.closeDialog(requireActivity().supportFragmentManager)
                        Toast.makeText(
                            requireContext(),
                            "Pdf saved Successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        //document.close()
                        hideKeyboard(requireActivity())
                    }

                    override fun onError(exception: java.lang.Exception) {
                        exception.printStackTrace()
                    }
                })
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }


    private fun takePermissionforText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        saveTextFile()
                        val model =
                            ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
                        model.message.value = null
                        txtToBePDF.equals(null)
                    } else {
                        permissionDenyDialog()
                    }
                }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            saveTextFile()
            val model =
                ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
            model.message.value = null
            txtToBePDF.equals(null)
        } else {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        saveTextFile()
                        val model =
                            ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
                        model.message.value = null
                        txtToBePDF.equals(null)
                    } else {
                        permissionDenyDialog()
                    }
                }
        }
    }

    fun saveTextFile() {
        val directory_path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val app_path: String =
                requireContext().getExternalFilesDir("Rephraser")!!.absolutePath
            "$app_path/"
        } else {
            Environment.getExternalStorageDirectory().path + "/Rephraser/"
        }
        val file = File(directory_path)
        if (!file.exists()) {
            file.mkdirs()
        }

        val targetPdf = directory_path + binding.editText.text.toString() + ".txt"
        val filePath = File(targetPdf)
        if (filePath.exists()) {
            Toast.makeText(requireContext(), "File Already Exist", Toast.LENGTH_SHORT).show()
        } else {
            if (!Prefs.getBoolean("subscription", false)) {
                if (AdManger.isInterstialLoaded()) {
                    AdManger.showInterstial(
                        requireActivity(),
                        requireContext(),
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                saveText(directory_path)
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                saveText(directory_path)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                saveText(directory_path)
                            }
                        })
                }
            } else {
                saveText(directory_path)
            }
        }
    }

    fun saveText(directory_path: String) {
        val gpxfile = File(directory_path, binding.editText.text.toString() + ".txt")
        val fileWritter = FileWriter(gpxfile)
        fileWritter.append(txtToBePDF)
        fileWritter.flush()
        Utils.closeDialog(requireActivity().supportFragmentManager)
        Toast.makeText(
            requireContext(),
            "Text file saved Successfully",
            Toast.LENGTH_LONG
        ).show()
        fileWritter.close()
        hideKeyboard(requireActivity())
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
}