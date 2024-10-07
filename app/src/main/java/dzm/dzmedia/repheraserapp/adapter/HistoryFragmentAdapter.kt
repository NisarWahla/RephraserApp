package dzm.dzmedia.repheraserapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import dzm.dzmedia.repheraserapp.fragments.PaymentHistoryFragment
import dzm.dzmedia.repheraserapp.fragments.UsageHistoryFragment

private const val NUM_TABS = 2

class HistoryFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return UsageHistoryFragment()
            1 -> return PaymentHistoryFragment()
        }
        return UsageHistoryFragment()
    }
}