package com.revesystems.tts.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.revesystems.tts.R
import com.revesystems.tts.databinding.SettingLayoutBinding
import com.revesystems.tts.utils.GONE
import com.revesystems.tts.utils.VISIBLE

class SettingsBottomSheetFragment: BottomSheetDialogFragment() {
    private lateinit var binding: SettingLayoutBinding

    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{
        binding = SettingLayoutBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickEvents()
    }
    
    private fun clickEvents(){
        binding.btnSave.setOnClickListener {
           dialog?.dismiss()
        }
        binding.btnClose.setOnClickListener {
            dialog?.dismiss()
        }

        binding.tvAscii.setOnClickListener {
            binding.tvAscii.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.tvAscii.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color.white))
            binding.tvUnicode.setBackgroundResource(R.color.white)
            binding.tvUnicode.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._136EE5))
        }
        binding.tvUnicode.setOnClickListener {
            binding.tvAscii.setBackgroundResource(R.color.white)
            binding.tvAscii.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._136EE5))
            binding.tvUnicode.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.tvUnicode.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color.white))
        }

        binding.tvText.setOnClickListener {
            binding.tvText.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.tvText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.tvSSML.setBackgroundResource(R.color.white)
            binding.tvSSML.setTextColor(ContextCompat.getColor(requireContext(), R.color._136EE5))
        }
        binding.tvSSML.setOnClickListener {
            binding.tvText.setBackgroundResource(R.color.white)
            binding.tvText.setTextColor(ContextCompat.getColor(requireContext(), R.color._136EE5))
            binding.tvSSML.setBackgroundResource(R.drawable.bg_r4_sol_136)
            binding.tvSSML.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        binding.tvMale.setOnClickListener {
            binding.tvMale.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.tvMale.setTextColor(ContextCompat.getColor(requireContext(), R.color._136EE5))
            binding.tvFemale.setBackgroundResource(R.color.white)
            binding.tvFemale.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._999DA7))
        }
        binding.tvFemale.setOnClickListener {
            binding.tvMale.setBackgroundResource(R.color.white)
            binding.tvMale.setTextColor(ContextCompat.getColor(requireContext(), R.color._999DA7))
            binding.tvFemale.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.tvFemale.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._136EE5))
        }

        binding.tvImmature.setOnClickListener {
            binding.tvImmature.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.tvImmature.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._136EE5))
            binding.tvMature.setBackgroundResource(R.color.white)
            binding.tvMature.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._999DA7))
        }
        binding.tvMature.setOnClickListener {
            binding.tvImmature.setBackgroundResource(R.color.white)
            binding.tvImmature.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._999DA7))
            binding.tvMature.setBackgroundResource(R.drawable.bg_r4_sol_fafa)
            binding.tvMature.setTextColor(
                ContextCompat.getColor(requireContext(),
                    R.color._136EE5))
        }
    }
}