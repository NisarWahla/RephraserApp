package dzm.dzmedia.repheraserapp.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dzm.dzmedia.repheraserapp.R
import dzm.dzmedia.repheraserapp.activities.MainActivity
import dzm.dzmedia.repheraserapp.adapter.UsageHistoryAdapter
import dzm.dzmedia.repheraserapp.authentication_api.RetrofitAuthenticationClient
import dzm.dzmedia.repheraserapp.databinding.FragmentParaphraserSavedHistoryBinding
import dzm.dzmedia.repheraserapp.helpers.Utils
import dzm.dzmedia.repheraserapp.usage_history.UsageHistory
import dzm.dzmedia.repheraserapp.usage_history.UsageHistoryData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsageHistoryFragment : Fragment() {
    private lateinit var binding: FragmentParaphraserSavedHistoryBinding
    private lateinit var list: ArrayList<UsageHistoryData>
    lateinit var adapter: UsageHistoryAdapter
    private val TAG = UsageHistoryFragment::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParaphraserSavedHistoryBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verifyEmailApiResponce()

        binding.searchView.queryHint = getString(R.string.search_hint)
        binding.searchView.onActionViewExpanded()
        binding.searchView.isIconified = false
        binding.searchView.clearFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                if (s != "") {
                    if (list.size > 0) {
                        val newArrayList: ArrayList<UsageHistoryData> = ArrayList()
                        for (customModel in list) {
                            val name = customModel.action_type.lowercase()
                            if (name.contains(s.lowercase())) {
                                newArrayList.add(customModel)
                            }
                        }
                        if (newArrayList.size == 0) {
                            binding.mianScrEmptyIcon.visibility = View.VISIBLE
                            binding.nothingShow.visibility = View.VISIBLE
                            adapter.updateList(newArrayList)
                        } else {
                            binding.mianScrEmptyIcon.visibility = View.GONE
                            binding.nothingShow.visibility = View.GONE
                            adapter.updateList(newArrayList)
                        }
                        return true
                    }
                } else {
                    binding.mianScrEmptyIcon.visibility = View.GONE
                    binding.nothingShow.visibility = View.GONE
                    adapter.updateList(list)
                }
                return false
            }
        })
        binding.searchView.setIconifiedByDefault(true)
    }

    fun verifyEmailApiResponce() {
        val dialog = Dialog(requireContext())
        Utils.loader(dialog)
        val token =
            "Bearer " + MainActivity.userModel!!.token
        val userId = MainActivity.userModel!!.user_id

        RetrofitAuthenticationClient.AuthClient()
            .UsageHistory(
                token,
                userId
            ).enqueue(object : Callback<UsageHistory> {
                override fun onResponse(
                    call: Call<UsageHistory>,
                    response: Response<UsageHistory>
                ) {
                    dialog.dismiss()
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: $response")
                        val res = response.body()
                        val msg = response.body()?.message
                        val data = res?.`data`
                        list = data as ArrayList<UsageHistoryData>

                        binding.savedFilesRecycler.setHasFixedSize(true)
                        binding.savedFilesRecycler.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )

                        adapter = UsageHistoryAdapter(requireContext(), list, binding)
                        binding.savedFilesRecycler.adapter = adapter
                        adapter.notifyDataSetChanged()

                        if (adapter.itemCount > 0) {
                            binding.savedFilesRecycler.visibility = View.VISIBLE
                            binding.mianScrEmptyIcon.visibility = View.GONE
                            binding.nothingShow.visibility = View.GONE
                        } else {
                            binding.savedFilesRecycler.visibility = View.GONE
                            binding.mianScrEmptyIcon.visibility = View.VISIBLE
                            binding.nothingShow.visibility = View.VISIBLE
                        }

                        Toast.makeText(requireContext(), "$msg", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        dialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "History Not Found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UsageHistory>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "" + t.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}