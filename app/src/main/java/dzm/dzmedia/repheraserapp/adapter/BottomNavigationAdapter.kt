package dzm.dzmedia.repheraserapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import dzm.dzmedia.repheraserapp.fragments.GrammarCheckerFragment
import dzm.dzmedia.repheraserapp.fragments.RephraseParagraphFragment
import dzm.dzmedia.repheraserapp.fragments.SummarizeFragment

class BottomNavigationAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    val MAXIMUM: Int = 5

    override fun getItemCount(): Int {
        return MAXIMUM
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return RephraseParagraphFragment()
            1 -> return GrammarCheckerFragment()
            2 -> return SummarizeFragment()
        }
        return RephraseParagraphFragment()
    }
}