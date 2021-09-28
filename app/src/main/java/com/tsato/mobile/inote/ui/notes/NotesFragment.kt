package com.tsato.mobile.inote.ui.notes

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.tsato.mobile.inote.R
import com.tsato.mobile.inote.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_notes.*

class NotesFragment : BaseFragment(R.layout.fragment_notes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabAddNote.setOnClickListener {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(""))
        }
    }
}