package dzm.dzmedia.repheraserapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import dzm.dzmedia.repheraserapp.fragments.EnterMailForgotPassword
import dzm.dzmedia.repheraserapp.fragments.EnterVerificationCodeForgotPassword
import dzm.dzmedia.repheraserapp.fragments.RephraseParagraphFragment
import dzm.dzmedia.repheraserapp.fragments.ResetPassForgotPassword

class ForgotPasswordFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    val MAXIMUM: Int = 3

    override fun getItemCount(): Int {
        return MAXIMUM
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return EnterMailForgotPassword()
            1 -> return EnterVerificationCodeForgotPassword()
            2 -> return ResetPassForgotPassword()
        }
        return EnterMailForgotPassword()
    }
}