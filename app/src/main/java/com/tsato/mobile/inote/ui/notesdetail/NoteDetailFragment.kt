package com.tsato.mobile.inote.ui.notesdetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tsato.mobile.inote.R
import com.tsato.mobile.inote.data.local.entities.Note
import com.tsato.mobile.inote.ui.BaseFragment
import com.tsato.mobile.inote.ui.dialogs.AddOwnerDialogFragment
import com.tsato.mobile.inote.util.Status
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_note_detail.*

const val ADD_OWNER_DIALOG_TAG = "ADD_OWNER_DIALOG_TAG"

@AndroidEntryPoint
class NoteDetailFragment : BaseFragment(R.layout.fragment_note_detail) {

    private val viewModel: NoteDetailViewModel by viewModels()

    private val args: NoteDetailFragmentArgs by navArgs()

    private var currNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        fabEditNote.setOnClickListener {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(args.id)
            )
        }

        if (savedInstanceState != null) {
            val addOwnerDialogFragment = parentFragmentManager.findFragmentByTag(ADD_OWNER_DIALOG_TAG)
                as AddOwnerDialogFragment?
            addOwnerDialogFragment?.setPositiveListener { email ->
                addOwnerToCurrentNote(email)
            }
        }
    }

    private fun showAddOwnerDialog() {
        AddOwnerDialogFragment().apply {
            setPositiveListener { email ->
                addOwnerToCurrentNote(email)
            }
        }.show(parentFragmentManager, ADD_OWNER_DIALOG_TAG)
    }

    private fun addOwnerToCurrentNote(email: String) {
        currNote?.let { note ->
            viewModel.addOwnerToNote(email, note.id)
        }
    }

    private fun subscribeToObservers() {
        viewModel.addOwnerStatus.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        addOwnerProgressBar.visibility = View.GONE
                        showSnackbar(result.data ?: "Successfully added owner to note")
                    }
                    Status.ERROR -> {
                        addOwnerProgressBar.visibility = View.GONE
                        showSnackbar(result.message ?: "An unknown error occurred")
                    }
                    Status.LOADING -> {
                        addOwnerProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.observeNoteById(args.id).observe(viewLifecycleOwner, { noteNullable ->
            noteNullable?.let { note ->
                tvNoteTitle.text = note.title
                setMarkdownText(note.content)
                currNote = note
            } ?: showSnackbar("Note not found")
        })
    }

    private fun setMarkdownText(text: String) {
        val markwon = Markwon.create(requireContext())
        val markdown = markwon.toMarkdown(text)
        markwon.setParsedMarkdown(tvNoteContent, markdown)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_note_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_owner -> {
                showAddOwnerDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}