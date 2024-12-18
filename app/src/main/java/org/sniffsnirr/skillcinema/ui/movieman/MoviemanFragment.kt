package org.sniffsnirr.skillcinema.ui.movieman

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.sniffsnirr.skillcinema.MainActivity
import org.sniffsnirr.skillcinema.R
import org.sniffsnirr.skillcinema.databinding.FragmentMoviemanBinding
import org.sniffsnirr.skillcinema.ui.collections.paging.presets.PagingCollectionFragment
import org.sniffsnirr.skillcinema.ui.exception.BottomSheetErrorFragment
import org.sniffsnirr.skillcinema.ui.home.HomeFragment
import org.sniffsnirr.skillcinema.ui.home.model.MovieRVModel
import org.sniffsnirr.skillcinema.ui.onemovie.OneMovieFragment

@AndroidEntryPoint
class MoviemanFragment : Fragment() {

    private var _binding: FragmentMoviemanBinding? = null
    val binding get() = _binding!!
    private val viewModel: MoviemanViewModel by viewModels()
    private var photoURL = ""
    private var moviemanName = ""
    private var idMovieman = 0
    private lateinit var bestMovies: List<MovieRVModel>
    private val bestMovieAdapter =
        BestMovieAdapter({ onCollectionClick() }, { idMovie -> onMovieClick(idMovie) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idMovieman = arguments?.getInt(OneMovieFragment.ID_STAFF) ?: 0
        viewModel.getBestMovies(idMovieman)
    }

    override fun onResume() {// если при восстановлении фрагмента получен сигнал об изменении данных - обновить состояние и передать выше
        super.onResume()
        (activity as MainActivity).setActionBarTitle("")
//обновление сработает и в варианте изменения в onemovieFragment и через промежуточный экран с rv
        var needUpdate = false

        setFragmentResultListener(HomeFragment.RV_ITEM_HAS_BEEN_CHANGED_REQUEST_KEY) { RV_ITEM_HAS_BEEN_CHANGED_REQUEST_KEY, bundle ->
            if (bundle.getBoolean(HomeFragment.RV_ITEM_HAS_BEEN_CHANGED_BUNDLE_KEY) != null) {
                if (bundle.getBoolean(HomeFragment.RV_ITEM_HAS_BEEN_CHANGED_BUNDLE_KEY)) {
                    needUpdate = true
                    viewModel.getBestMovies(idMovieman)
                }
            }
        }
        if (!needUpdate) {
            setFragmentResultListener(PagingCollectionFragment.RV_ITEM_HAS_BEEN_CHANGED_REQUEST_KEY) { REQUEST_KEY, bundle ->
                if (bundle.getBoolean(PagingCollectionFragment.RV_ITEM_HAS_BEEN_CHANGED_BUNDLE_KEY) != null) {
                    if (bundle.getBoolean(PagingCollectionFragment.RV_ITEM_HAS_BEEN_CHANGED_BUNDLE_KEY)) {
                        viewModel.getBestMovies(idMovieman)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMoviemanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showActionBar()

        viewModel.moviemanInfo.onEach {//общая информция по кинематографисту
            moviemanName = it?.nameRu ?: ""
            binding.nameMovieman.text = it?.nameRu
            binding.profOfMovieman.text = it?.profession
            photoURL = it?.posterUrl ?: ""
            Glide
                .with(binding.photoMovieman.context)
                .load(it?.posterUrl)
                .into(binding.photoMovieman)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.bestMoviesRv.setHasFixedSize(true)
        binding.bestMoviesRv.layoutManager =
            LinearLayoutManager(requireContext(), GridLayoutManager.HORIZONTAL, false)
        binding.bestMoviesRv.adapter = bestMovieAdapter

        viewModel.bestMovies.onEach {// загрузка лучших рейтинговых фильмов с участием актера
            bestMovieAdapter.setMovieList(it)
            bestMovies = it
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.numberMovies.onEach {
            binding.numberOfMovies.text = "$it фильма"
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.photoMovieman.setOnClickListener {//открытие фрагмента с фотографией актера
            val bundle = Bundle()
            bundle.putCharSequence(PHOTO_URL, photoURL)
            findNavController().navigate(
                R.id.action_moviemanFragment_to_moviemanPhotoFragment,
                bundle
            )
        }

        binding.allBestMoviesButton.setOnClickListener {//открытие фрагмента с лучшими фильмами актера
            getBesMovies()
        }

        binding.allMoviesFilmography.setOnClickListener {//открытие фрагмента с фильмографии
            val bundle = Bundle()
            bundle.putInt(MOVIEMAN_ID, idMovieman)
            findNavController().navigate(
                R.id.action_moviemanFragment_to_filmographyFragment,
                bundle
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {// ожидание ошибки
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.take(1).collect { _ ->
                    BottomSheetErrorFragment().show(parentFragmentManager, "errordialog")
                }
            }
        }
    }

    private fun onMovieClick(idMovie: Int?) {
        val bundle = Bundle()
        if (idMovie != null) {
            bundle.putInt(HomeFragment.ID_MOVIE, idMovie)
            findNavController().navigate(
                R.id.action_moviemanFragment_to_oneMovieFragment,
                bundle
            )
        }
    }

    private fun onCollectionClick() {
        getBesMovies()
    }

    private fun getBesMovies() {
        val bundle = Bundle()
        bundle.putCharSequence(MOVIEMAN_NAME, moviemanName)
        val arrayListOfBestMovies = ArrayList<MovieRVModel>()
        bestMovies.map { movie -> arrayListOfBestMovies.add(movie) }
        bundle.putParcelableArrayList(BEST_MOVIES_LIST, arrayListOfBestMovies)
        findNavController().navigate(
            R.id.action_moviemanFragment_to_tenBestMoviesFragment,
            bundle
        )
    }

    companion object {
        const val PHOTO_URL = "PHOTO_URL"
        const val MOVIEMAN_NAME = "MOVIEMAN_NAME"
        const val BEST_MOVIES_LIST = "BEST_MOVIES_LIST"
        const val MOVIEMAN_ID = "MOVIEMAN_ID"
    }
}