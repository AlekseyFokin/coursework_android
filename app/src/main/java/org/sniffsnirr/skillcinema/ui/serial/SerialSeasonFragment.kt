package org.sniffsnirr.skillcinema.ui.serial

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.sniffsnirr.skillcinema.MainActivity
import org.sniffsnirr.skillcinema.R
import org.sniffsnirr.skillcinema.databinding.FragmentSerialSeasonBinding
import org.sniffsnirr.skillcinema.entities.serialinfo.Episode
import org.sniffsnirr.skillcinema.ui.exception.BottomSheetErrorFragment
import org.sniffsnirr.skillcinema.ui.home.HomeFragment
import org.sniffsnirr.skillcinema.ui.onemovie.OneMovieFragment


//Фрагмент эпизодов по сезонам для сериалов
@AndroidEntryPoint
class SerialSeasonFragment : Fragment() {

    private val viewModel: SerialSeasonViewModel by viewModels()
    var _binding: FragmentSerialSeasonBinding? = null
    val binding get() = _binding!!
    private var movieName = ""
    private val serialSeasonAdapter = SerialSeasonAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idMovie = arguments?.getInt(HomeFragment.ID_MOVIE) ?: 0
        movieName = arguments?.getCharSequence(OneMovieFragment.MOVIE_NAME).toString()
        viewModel.getAllSerialData(idMovie)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarTitle(movieName)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSerialSeasonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showActionBar()

        binding.episodesRv.setHasFixedSize(true)
        binding.episodesRv.layoutManager =
            LinearLayoutManager(requireContext(), GridLayoutManager.VERTICAL, false)
        binding.episodesRv.adapter = serialSeasonAdapter

        viewModel.serialInfo.onEach {
            if (it != null) {
                it.items.forEach { item ->
                    val chip: Chip = View.inflate(
                        binding.seasonsChipgroup.context,
                        R.layout.chip_templ,
                        null
                    ) as Chip
                    chip.text = item.number.toString()

                    val episodeList = mutableListOf<Episode>()
                    item.episodes.map { episode -> episodeList.add(episode) }

                    chip.setOnClickListener {
                        binding.summaryAboutSeason.text =
                            "${item.number} сезон, ${item.episodes.size} серий"
                        serialSeasonAdapter.setData(episodeList)
                    }
                    binding.seasonsChipgroup.addView(chip)
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {// ожидание ошибки
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.take(1).collect { _ ->
                    BottomSheetErrorFragment().show(parentFragmentManager, "errordialog")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).setActionBarTitle("")
    }
}