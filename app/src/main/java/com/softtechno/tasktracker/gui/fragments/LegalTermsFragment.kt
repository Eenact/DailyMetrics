package com.softtechno.tasktracker.gui.fragments

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.databinding.FragmentLegalTermsBinding


class LegalTermsFragment : Fragment() {

    private var _binding: FragmentLegalTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLegalTermsBinding.inflate(inflater, container, false)

        setLicenseString()

        return binding.root

    }

    private fun setLicenseString() {
        // Get Version code from PackageManager
        // Get Version code from PackageManager
        var versionCode = ""
        var versionName = ""
        try {
            val pinfo = requireContext().packageManager.getPackageInfo(
                requireActivity().packageName, 0
            )
            if (pinfo != null) {
                versionCode = pinfo.versionCode.toString() + ""
                versionName = pinfo.versionName + ""
            }
        } catch (_: PackageManager.NameNotFoundException) {
        }

        var appVersion = getString(R.string.copy_app)
        appVersion = appVersion.replace("{APP_NAME}", getString(R.string.app_name))
        appVersion = appVersion.replace("{BUILD}", versionCode)
        appVersion = appVersion.replace("{VERSION}", versionName)

        val sbLegalNotes = StringBuilder()
        sbLegalNotes.append(appVersion)
        sbLegalNotes.append("<br /><br />")

        // MPAndroidChart
        sbLegalNotes.append(getString(R.string.copy_mpchart))
        sbLegalNotes.append("<br /><br />")

        // Lottie
        sbLegalNotes.append(getString(R.string.copy_lottie))
        sbLegalNotes.append("<br /><br />")

        // Android

        // Android
        sbLegalNotes.append(getString(R.string.copy_android))
        sbLegalNotes.append("<br /><br />")
        sbLegalNotes.append(getString(R.string.apache2_license))
        sbLegalNotes.append("<br /><br />")

        binding.licenseTextView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(sbLegalNotes.toString(), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(sbLegalNotes.toString())
        }
    }

}