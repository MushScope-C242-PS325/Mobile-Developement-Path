package com.mushscope.view.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mushscope.R
import com.mushscope.utils.ViewModelFactory
import com.mushscope.view.history.HistoryActivity

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchTheme = view.findViewById<SwitchMaterial>(R.id.switch_theme)
        val btnHistory = view.findViewById<Button>(R.id.btn_history)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        // Observe theme settings
        profileViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive: Boolean ->
            switchTheme.isChecked = isDarkModeActive
        }

        // Set the listener for the theme switch
        switchTheme.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            profileViewModel.saveThemeSetting(isChecked)
        }

        // Set the click listener for the history button
        btnHistory.setOnClickListener {
            // Start the HistoryActivity
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.logout))
            setMessage(getString(R.string.logout_confirmation))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                profileViewModel.logout()
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

}