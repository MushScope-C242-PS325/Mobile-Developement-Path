package com.mushscope.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.mushscope.R
import com.mushscope.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.apply {
            textTitleHome.text = getString(R.string.ensiklopedia_mushroom)
            textDescriptionHome.text = getString(R.string.text_mushroom_header)
            textPoisonousTitle.text = getString(R.string.poisonous_mushroom)
            textPoisonousDescription.text = getString(R.string.text_poisonous_mushroom)
            textEdibleTitle.text = getString(R.string.edible_mushroom)
            textEdibleDescription.text = getString(R.string.text_edible_mushroom)
        }

        val toolbar: Toolbar = binding.root.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_home)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
