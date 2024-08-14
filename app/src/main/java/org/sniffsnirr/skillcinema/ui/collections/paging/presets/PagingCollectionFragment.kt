package org.sniffsnirr.skillcinema.ui.collections.paging.presets

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.sniffsnirr.skillcinema.MainActivity
import org.sniffsnirr.skillcinema.databinding.FragmentPagingCollectionBinding
import org.sniffsnirr.skillcinema.ui.collections.paging.PagingLoadStateAdapter
import org.sniffsnirr.skillcinema.ui.home.HomeFragment

@AndroidEntryPoint
class PagingCollectionFragment : Fragment() {

    private val viewModel: PagingCollectionViewModel by viewModels()
    var _binding: FragmentPagingCollectionBinding? = null
    val binding get() = _binding!!
    val pagedAdapter= PagingCollectionAdapter{ string->onMovieClick(string)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val collectionType=arguments?.getCharSequence(HomeFragment.COLLECTION_TYPE)
        viewModel.collectionType=collectionType.toString()
        (activity as MainActivity).showActionBar()
        val collectionName=arguments?.getCharSequence(HomeFragment.COLLECTION_NAME)
        (activity as MainActivity).setActionBarTitle(collectionName.toString())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagingCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.moviePagingCollectionRv.adapter=pagedAdapter.withLoadStateFooter(
            PagingLoadStateAdapter()
        )

        viewModel.pagedMovies.onEach {
            pagedAdapter.submitData(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).hideActionBar()
    }
    fun onMovieClick(string: String) {
        Log.d("ButtonClick", string)
    }
}