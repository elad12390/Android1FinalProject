package com.hit.android1.finalproject.ui.main
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hit.android1.finalproject.app.AppFragmentWithModel
import com.hit.android1.finalproject.app.Globals.dao
import com.hit.android1.finalproject.databinding.MainFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : AppFragmentWithModel<MainFragmentBinding, MainViewModel>(MainViewModel::class.java) {
    override fun inflate() = MainFragmentBinding.inflate(layoutInflater)

    override fun runOnCreateView(view: View) {
        viewModel.cool = "UNCOOL ANYMORE 🎨"
        initializeViewModel()
    }

    private suspend fun initializeRecycler() {
        withContext(Dispatchers.Main) {
            binding.itemInventoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.HORIZONTAL, false)
            binding.itemInventoryRecyclerView.adapter = InventoryAdapter(viewModel.unlockedItems!!)
        }
    }

    private fun initializeViewModel() {
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.items = dao.getItems()
            viewModel.unlockedItems = dao.getUnlockedItems()
            for (item in viewModel.unlockedItems!!) {
                initializeRecycler()
            }
        }
    }
}