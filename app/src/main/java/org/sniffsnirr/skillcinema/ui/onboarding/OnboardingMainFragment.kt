package org.sniffsnirr.skillcinema.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.sniffsnirr.skillcinema.R
import org.sniffsnirr.skillcinema.databinding.FragmentOnboardingMainBinding

// фрагмент onboarding - появляется только при первой загрузке приложения
// использует viewPager для отображения нескольких фрагментов внутри одного фрагмента вместе с ползунком

@AndroidEntryPoint
class OnboardingMainFragment : Fragment() {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore( // работа с хранилищем DataStore
        name = "skillcinema_settings",
        corruptionHandler = null,
        scope = CoroutineScope(Dispatchers.IO)
    )

    private val viewModel: OnboardingMainViewModel by viewModels()

    private var _binding: FragmentOnboardingMainBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch{
            requireContext().dataStore.data
                .take(1).collect{ pref ->
                    Log.d("isFirstStart 1","${pref[FIRST_START]}")
                    if (pref[FIRST_START]!=null) {findNavController().navigate(R.id.action_onboardingMainFragment_to_loadingFragment)}
                    requireContext().dataStore.edit { prefs ->// сохранение метки о первой загрузке
                        prefs[FIRST_START] = false
                    }
                }
        }

        binding.onboardingViewPager.adapter = PagerAdapter(this)
        binding.tabs.tabIconTint = null

        TabLayoutMediator(
            binding.tabs,
            binding.onboardingViewPager
        ) { tab, pos ->// первоначальная настройка TabLayout
            when (pos) {
                0 -> tab.setIcon(R.drawable.onboarding_label_black)
                1 -> tab.setIcon(R.drawable.onboarding_label_gray)
                2 -> tab.setIcon(R.drawable.onboarding_label_gray)
            }
        }.attach()

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // реакция tablayout на листание фрагментов
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.setIcon(R.drawable.onboarding_label_black)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.setIcon(R.drawable.onboarding_label_gray)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {// отслеживание листания фрагментов и переход на фрагмент загрузки, если табы закончились
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fragmentNumber.collect { fragmentNumber ->
                    if (fragmentNumber >= binding.tabs.tabCount) {
                        findNavController().navigate(R.id.action_onboardingMainFragment_to_loadingFragment)
                    } else {
                        binding.tabs.selectTab(binding.tabs.getTabAt(fragmentNumber))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        val FIRST_START =
            booleanPreferencesKey("first_start")// ячейка для хранения метки о первой загрузке приложения в DataStore
    }
}

