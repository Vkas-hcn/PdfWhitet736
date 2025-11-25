package com.urgolle.pdfslam.tihtg

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import com.urgolle.pdfslam.R
import com.urgolle.pdfslam.databinding.FragmentSetBinding

class SetFragment : Fragment() {

    private lateinit var binding: FragmentSetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_set, container, false)
        binding = FragmentSetBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ll1.setOnClickListener {
            runCatching {
                this.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://sites.google.com/view/easyy-pdf-reader/home".toUri()
                    )
                )
            }
        }
        binding.ll2.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    this.getString(R.string.app_name)
                )

                val appUrl =
                    "https://play.google.com/store/apps/details?id=${requireContext().packageName}"

                shareIntent.putExtra(Intent.EXTRA_TEXT, appUrl)
                this.startActivity(Intent.createChooser(shareIntent, "Share App"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Share failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


}