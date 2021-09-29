package com.tsato.mobile.inote.ui.addeditnote

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.tsato.mobile.inote.R
import com.tsato.mobile.inote.data.local.entities.Note
import com.tsato.mobile.inote.ui.BaseFragment
import com.tsato.mobile.inote.ui.dialogs.ColorPickerDialogFragment
import com.tsato.mobile.inote.util.Constants.DEFAULT_NOTE_COLOR
import com.tsato.mobile.inote.util.Constants.KEY_LOGGED_IN_EMAIL
import com.tsato.mobile.inote.util.Constants.NO_EMAIL
import com.tsato.mobile.inote.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_note.*
import kotlinx.android.synthetic.main.item_note.view.*
import java.util.*
import javax.inject.Inject

const val FRAGMENT_TAG = "AddEditNoteFragment"

@AndroidEntryPoint
class AddEditNoteFragment : BaseFragment(R.layout.fragment_add_edit_note) {

    private val viewModel: AddEditNoteViewModel by viewModels()

    private val args: AddEditNoteFragmentArgs by navArgs()

    private var currNote: Note? = null
    private var currColor: String = DEFAULT_NOTE_COLOR

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.id.isNotEmpty()) {
            viewModel.getNoteById(args.id)
            subscribeToObservers()
        }

        // *
        if (savedInstanceState != null) {
            val colorPickerDialog = parentFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                as ColorPickerDialogFragment?
            colorPickerDialog?.setPositiveListener {
                changeViewNoteColor(it)
            }
        }

        viewNoteColor.setOnClickListener {
            ColorPickerDialogFragment().apply {
                setPositiveListener {
                    changeViewNoteColor(it)
                }
            }.show(parentFragmentManager, FRAGMENT_TAG)
            // need FRAGMENT_TAG here because if the user opened the dialog and rotated the device,
            // the dialog will survive the screen rotation,
            // but the positiveListener in ColorPickerDialogFragment will be set to null and nothing
            // will happen when "Ok" is clicked.
            // So, we need to restore the dialog state in onViewCreated -- *
        }
    }

    private fun changeViewNoteColor(colorString: String) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.circle_shape, null)
        drawable?.let {
            val wrappedDrawable = DrawableCompat.wrap(it)
            val color = Color.parseColor("#$colorString")
            DrawableCompat.setTint(wrappedDrawable, color)
            viewNoteColor.background = wrappedDrawable
            currColor = colorString
        }
    }

    private fun subscribeToObservers() {
        viewModel.note.observe(viewLifecycleOwner, { event ->
            // will only return the actual Resource note first time
            event?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        val note = result.data!!

                        currNote = note

                        etNoteTitle.setText(note.title)
                        etNoteContent.setText(note.content)
                        changeViewNoteColor(note.color)
                    }
                    Status.ERROR -> {
                        showSnackbar(result.message ?: "Note not found")
                    }
                    Status.LOADING -> {}
                }
            }
        })
    }

    private fun saveNote() {
        val authEmail = sharedPref.getString(KEY_LOGGED_IN_EMAIL, NO_EMAIL) ?: NO_EMAIL

        val title = etNoteTitle.text.toString()
        val content = etNoteContent.text.toString()
        if (title.isEmpty() || content.isEmpty()) {
            return // don't save the note
        }

        val date = System.currentTimeMillis()
        val color = currColor
        val noteId = currNote?.id ?: UUID.randomUUID().toString()
        val owners = currNote?.owners ?: listOf(authEmail)

        val note = Note(title, content, date, owners, color, id = noteId)
        viewModel.insertNote(note)
    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }

}