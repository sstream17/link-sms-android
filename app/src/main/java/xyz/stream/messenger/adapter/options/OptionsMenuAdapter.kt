package xyz.stream.messenger.adapter.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.stream.messenger.R
import xyz.stream.messenger.shared.data.pojo.OptionsSection

class OptionsMenuAdapter(private val sections: List<OptionsSection>): RecyclerView.Adapter<OptionsMenuAdapter.ViewHolder>(){

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.section_options_menu, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = sections[position]
        val sectionLayoutManager = LinearLayoutManager(holder.recyclerView.context, RecyclerView.VERTICAL, false)
        holder.recyclerView.apply {
            layoutManager = sectionLayoutManager
            adapter = OptionsItemAdapter(section.items)
            setRecycledViewPool(viewPool)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.section_recycler_view) as RecyclerView
    }
}