package io.github.nitsuya.aa.display.ui.aa.fragment

import android.view.MotionEvent
import androidx.recyclerview.widget.GridLayoutManager
import io.github.duzhaokun123.template.bases.BaseFragment
import io.github.nitsuya.aa.display.CoreApi
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.databinding.FragmentAaRecentTaskBinding
import io.github.nitsuya.aa.display.ui.aa.AaDisplayActivityKt
import io.github.nitsuya.aa.display.ui.window.DisplayRecyclerViewAdapter
import io.github.nitsuya.template.bases.runIO
import io.github.nitsuya.template.bases.runMain
import kotlin.math.abs

class AaRecentTaskFragment: BaseFragment<FragmentAaRecentTaskBinding>(FragmentAaRecentTaskBinding::class.java){
    companion object {
        const val TAG = "AADisplay_AaRecentTaskFragment"
    }

    override fun initViews() {
        baseBinding.rvRecentTaskLeft.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = DisplayRecyclerViewAdapter(this){
                AaDisplayActivityKt.hideRecentTask(parentFragmentManager)
            }
        }
        baseBinding.rvRecentTaskRight.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = DisplayRecyclerViewAdapter(this){
                AaDisplayActivityKt.hideRecentTask(parentFragmentManager)
            }.apply {
                otherAdapter = (baseBinding.rvRecentTaskLeft.adapter as DisplayRecyclerViewAdapter).also {
                    it.otherAdapter = this@apply
                }
            }
        }

        arrayOf(baseBinding.rvRecentTaskLeft, baseBinding.rvRecentTaskRight).forEach {
            it.setOnTouchListener { v, event ->
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setTag(R.id.drag_last_x, event.x)
                        v.setTag(R.id.drag_last_y, event.y)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (v.id != 0
                            && abs((v.getTag(R.id.drag_last_x) as? Float ?: 0f) - event.x) <= 5
                            && abs((v.getTag(R.id.drag_last_y) as? Float ?: 0f) - event.y) <= 5) {
                            AaDisplayActivityKt.hideRecentTask(parentFragmentManager)
                        }
                    }
                }
                return@setOnTouchListener false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        runIO {
            CoreApi.recentTask?.also { recentTask ->
                runMain {
                    (baseBinding.rvRecentTaskLeft.adapter as DisplayRecyclerViewAdapter)?.setItems(recentTask.virtualDisplay)
                    (baseBinding.rvRecentTaskRight.adapter as DisplayRecyclerViewAdapter)?.setItems(recentTask.mainDisplay)
                }
            }
        }
    }


}