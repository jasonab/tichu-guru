package com.tichuguru

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tichuguru.databinding.StatslistrowBinding

class RankingFragment : Fragment() {
    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_LABELS = "labels"
        private const val ARG_VALUES = "values"

        fun newInstance(
            title: String,
            labels: Array<String>,
            values: Array<String?>,
        ): RankingFragment =
            RankingFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(ARG_TITLE, title)
                        putStringArray(ARG_LABELS, labels)
                        @Suppress("UNCHECKED_CAST")
                        putStringArray(ARG_VALUES, values as Array<String>)
                    }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.rankinglist, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        requireActivity().title = args.getString(ARG_TITLE)

        val labels = requireNotNull(args.getStringArray(ARG_LABELS)) { "labels arg missing" }

        @Suppress("UNCHECKED_CAST")
        val values = args.getStringArray(ARG_VALUES) as Array<String?>

        view.findViewById<RecyclerView>(R.id.statsList).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = StatsAdapter(labels, values)
        }
    }

    private class StatsAdapter(private val labels: Array<String>, private val values: Array<String?>) :
        RecyclerView.Adapter<StatsAdapter.ViewHolder>() {
        class ViewHolder(val binding: StatslistrowBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ViewHolder = ViewHolder(StatslistrowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
        ) {
            holder.binding.statsLabel.text = labels[position]
            values[position]?.let { v ->
                holder.binding.statsLabel.setTypeface(null, Typeface.NORMAL)
                holder.binding.statsLabel.textSize = 18f
                holder.binding.statsValue.text = v
            } ?: run {
                holder.binding.statsLabel.setTypeface(null, Typeface.BOLD)
                holder.binding.statsLabel.textSize = 24f
                holder.binding.statsValue.text = ""
            }
        }

        override fun getItemCount() = labels.size
    }
}
