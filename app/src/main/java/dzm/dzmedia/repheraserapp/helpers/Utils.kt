package dzm.dzmedia.repheraserapp.helpers

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import dzm.dzmedia.repheraserapp.R

class Utils {
    companion object {
        const val AUTHORITY = "dzm.dzmedia.repheraserapp.fileProvider"

        fun openDialog(manager: FragmentManager, fragment: DialogFragment) {
            val ft = manager.beginTransaction()
            val prev = manager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            fragment.isCancelable = false
            fragment.show(ft, "dialog")
        }

        fun closeDialog(manager: FragmentManager) {
            val prev = manager.findFragmentByTag("dialog")
            if (prev != null) {
                val df = prev as DialogFragment
                df.dismiss()
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

        fun loader(dialog: Dialog) {
            dialog.setContentView(R.layout.loader_dialog)
            dialog.setCancelable(false)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }


        fun getDeviceInfo(context: Context): String {
            val sdk = Build.VERSION.SDK_INT      // API Level
            val model = Build.MODEL            // Model
            val brand = Build.BRAND          // Product
            var infoString = ""
            val locale: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0).country
            } else {
                context.resources.configuration.locale.country
            }
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val version = pInfo.versionName
                infoString += "Application Version: $version\n"
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            infoString += "Brand: " + brand + " (" + model + ")\n" +
                    "Android API: " + sdk

            if (locale.isNotEmpty()) {
                infoString += "\nCountry: $locale"
            }
            return infoString
        }

        private fun emailSubject(context: Context): String {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val version = pInfo.versionName
                val name = getApplicationName(context)
                return "$name - $version"
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return context.getString(R.string.app_name)
        }

        fun getApplicationName(context: Context): String {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
                stringId
            )
        }

        @JvmStatic
        fun startFeedbackEmail(context: Context) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("apps@dzinemedia.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject(context))
            intent.putExtra(
                Intent.EXTRA_TEXT,
                """---Detail Information---${getDeviceInfo(context)}""".trimIndent()
            )
            context.startActivity(Intent.createChooser(intent, "Email via..."))

        }
        @JvmStatic
        fun giveReview(context: Context) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("apps@dzinemedia.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject(context))
            context.startActivity(Intent.createChooser(intent, "Email via..."))
        }
    }
}