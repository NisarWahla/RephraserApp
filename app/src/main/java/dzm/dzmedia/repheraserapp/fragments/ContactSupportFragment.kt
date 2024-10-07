package dzm.dzmedia.repheraserapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.databinding.ActivityMainBinding
import dzm.dzmedia.repheraserapp.databinding.FragmentContactSupportBinding
import java.util.regex.Pattern


class ContactSupportFragment : Fragment() {

    private lateinit var contactSupportBinding: FragmentContactSupportBinding
    lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var mainActivity: MainActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contactSupportBinding = FragmentContactSupportBinding.inflate(inflater, container, false)
        return contactSupportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = requireActivity() as MainActivity
        activityMainBinding = mainActivity.activityMainBinding

        contactSupportBinding.csBack.setOnClickListener {
            hideKeyboard(requireActivity())
            replaceFragment()
        }
        contactSupportBinding.sendQuery.setOnClickListener {
            var name = contactSupportBinding.etName.text.toString().trim()
            var mailTo = contactSupportBinding.etEmail.text.toString().trim()
            var title = contactSupportBinding.etTitle.text.toString().trim()
            var desc = contactSupportBinding.etDescription.text.toString().trim()
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (mailTo.equals("")) {
                contactSupportBinding.etEmail.error = "Email cannot be empty"
            } else if (!isValidEmailId(mailTo)) {
                Toast.makeText(
                    requireContext(),
                    "Invalid email address",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (title.equals("")) {
                contactSupportBinding.etTitle.error = "Title cannot be empty"
            } else if (desc.equals("")) {
                contactSupportBinding.etDescription.error = "Description cannot be empty"
            } else {
                sendEmail(mailTo, title, desc)
                contactSupportBinding.etName.text.clear()
                contactSupportBinding.etTitle.text.clear()
                contactSupportBinding.etDescription.text.clear()
            }
        }
    }

    private fun sendEmail(mailTo: String, title: String, desc: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        if (intent != null) {
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailTo))
            intent.putExtra(Intent.EXTRA_SUBJECT, title)
            intent.putExtra(Intent.EXTRA_TEXT, desc.trimIndent())
            context?.startActivity(Intent.createChooser(intent, "Email via..."))
        } else {
            Toast.makeText(
                requireContext(),
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isValidEmailId(email: String): Boolean {
        return Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        ).matcher(email).matches()
    }

    private fun replaceFragment() {
        val trans: FragmentTransaction = parentFragmentManager.beginTransaction()
        trans.replace(activityMainBinding.pager.id, SavedFilesFragment())
        trans.commit()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}