package com.mushscope.view.analyzer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mushscope.databinding.FragmentAnalyzerBinding

class AnalyzerFragment : Fragment() {

    private var _binding: FragmentAnalyzerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val analyzerViewModel =
            ViewModelProvider(this).get(AnalyzerViewModel::class.java)

        _binding = FragmentAnalyzerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textAnalyzer
        analyzerViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}