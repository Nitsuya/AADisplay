package io.github.nitsuya.aa.display.ui.window

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import io.github.nitsuya.aa.display.CoreApi
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.databinding.FragmentAaRecentTaskBinding
import io.github.nitsuya.aa.display.databinding.RecentTaskBinding
import io.github.nitsuya.aa.display.model.RecentTaskInfo

class DisplayRecyclerViewAdapter(
      private val recyclerView: RecyclerView
    , private val onExit: (() -> Unit)
) : RecyclerView.Adapter<DisplayRecyclerViewAdapter.ViewHolder>(){

    companion object {
        private const val TAG = "AADisplay_DisplayRecyclerViewAdapter"
    }

    private val items: MutableList<RecentTaskInfo> = ArrayList()
    lateinit var otherAdapter: DisplayRecyclerViewAdapter

    init {
        ItemTouchHelper(ItemTouchHelperCallback()).attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecentTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.ivIcon.setImageBitmap(item.logo)
        holder.binding.tvName.text = "${item.label} [${item.taskId}]"

        if(recyclerView.id == R.id.rv_recent_task_left){
            ConstraintSet().apply {
                clone(holder.binding.clItem)
                constrainPercentWidth(R.id.iv_snapshot,0.8f)
                setDimensionRatio(R.id.iv_snapshot,"W,9:16")
                applyTo(holder.binding.clItem)
            }
        }
        holder.binding.ivSnapshot.setImageBitmap(item.snapshot)
        holder.binding.root.setOnClickListener {
            onExit()
        }
        arrayOf(holder.binding.tvName, holder.binding.ivIcon, holder.binding.ivSnapshot).forEach {
            it.setOnClickListener {
                CoreApi.moveTaskToFront(item.taskId)
                onExit()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun clearItem(){
        setItems(emptyList())
    }

    fun removeItem(item: RecentTaskInfo){
        val index = items.indexOf(item)
        this.items.removeAt(index)
        this.notifyItemRemoved(index)
    }

    fun addItem(item: RecentTaskInfo){
        this.items.add(0, item)
        this.notifyItemInserted(0)
        this.recyclerView.scrollToPosition(0)
    }

    fun setItems(items: List<RecentTaskInfo>){
        this.items.clear()
        if(items.isNotEmpty()){
            this.items.addAll(items)
        }
        this.notifyDataSetChanged()
        if(this.items.isNotEmpty()){
            this.recyclerView.scrollToPosition(0)
        }
    }

    inner class ViewHolder(val binding: RecentTaskBinding): RecyclerView.ViewHolder(binding.root) {}

    inner class ItemTouchHelperCallback: ItemTouchHelper.Callback(){
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun isLongPressDragEnabled(): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val item = items.get(viewHolder.layoutPosition)
            removeItem(item)
            if(recyclerView.id == R.id.rv_recent_task_left){
                if (direction == ItemTouchHelper.LEFT) {
                    CoreApi.removeTask(item.taskId)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    otherAdapter.addItem(item)
                    CoreApi.moveTaskId(item.taskId, false)
                }
            } else if(recyclerView.id == R.id.rv_recent_task_right){
                if (direction == ItemTouchHelper.RIGHT) {
                    CoreApi.removeTask(item.taskId)
                } else if (direction == ItemTouchHelper.LEFT) {
                    otherAdapter.addItem(item)
                    CoreApi.moveTaskId(item.taskId, true)
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder?.itemView?.apply {
                    ViewCompat.animate(this)
                        .setDuration(200)
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .start()
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder?.itemView?.apply {
                ViewCompat.animate(this)
                    .setDuration(200)
                    .scaleX(1f)
                    .scaleY(1f)
                    .start()
            }
        }

    }

}