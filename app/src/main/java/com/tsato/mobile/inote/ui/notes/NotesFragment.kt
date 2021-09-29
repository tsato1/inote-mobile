package com.tsato.mobile.inote.ui.notes

import android.content.SharedPreferences
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tsato.mobile.inote.R
import com.tsato.mobile.inote.adapters.NoteAdapter
import com.tsato.mobile.inote.ui.BaseFragment
import com.tsato.mobile.inote.util.Constants.KEY_LOGGED_IN_EMAIL
import com.tsato.mobile.inote.util.Constants.KEY_PASSWORD
import com.tsato.mobile.inote.util.Constants.NO_EMAIL
import com.tsato.mobile.inote.util.Constants.NO_PASSWORD
import com.tsato.mobile.inote.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notes.*
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : BaseFragment(R.layout.fragment_notes) {

    private val viewModel: NotesViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    private lateinit var noteAdapter: NoteAdapter

    private val swipingItem = MutableLiveData<Boolean>(false)

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
        requireActivity().requestedOrientation = SCREEN_ORIENTATION_USER // the orientation user chose

        setupRecyclerView()
        subscribeToObservers()
        setSwipeRefreshLayout()

        noteAdapter.setOnItemClickListener {
            findNavController().navigate(
                NotesFragmentDirections.actionNotesFragmentToNoteDetailFragment(it.id)
            )
        }

        fabAddNote.setOnClickListener {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(""))
        }
    }

    private fun subscribeToObservers() {
        viewModel.allNotes.observe(viewLifecycleOwner, {
            it?.let { event -> // we made event because we don't want to show snackbar twice on configuration change
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {
                        noteAdapter.notes = result.data!! // not null because it's success
                        swipeRefreshLayout.isRefreshing = false
                    }
                    Status.ERROR -> { // consume the event content only in error, only on the first time
                        event.getContentIfNotHandled()?.let { errorResource ->
                            errorResource.message?.let { message ->
                                showSnackbar(message)
                            }
                        }
                        result.data?.let { notes ->
                            noteAdapter.notes = notes
                        }
                        swipeRefreshLayout.isRefreshing = false
                    }
                    Status.LOADING -> {
                        result.data?.let { notes ->
                            noteAdapter.notes = notes
                        }
                        swipeRefreshLayout.isRefreshing = true
                    }
                }
            }
        })

        swipingItem.observe(viewLifecycleOwner, {
            swipeRefreshLayout.isEnabled = !it
        })
    }

    private fun setSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.syncAllNotes()
        }
    }

    private fun setupRecyclerView() = rvNotes.apply {
        noteAdapter = NoteAdapter()
        adapter = noteAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
    }

    private fun logout() {
        sharedPref.edit().putString(KEY_LOGGED_IN_EMAIL, NO_EMAIL).apply()
        sharedPref.edit().putString(KEY_PASSWORD, NO_PASSWORD).apply()

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.notesFragment, true)
            .build()

        findNavController().navigate(
            NotesFragmentDirections.actionNotesFragmentToAuthFragment(),
            navOptions
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_notes, menu)
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                swipingItem.postValue(isCurrentlyActive)
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val note = noteAdapter.notes[position]
            viewModel.deleteNote(note.id)

            Snackbar.make(requireView(), "Note was successfully deleted", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    viewModel.insertNote(note)
                    viewModel.deleteLocallyDeletedNoteId(note.id)
                }
                show()
            }
        }
    }

}