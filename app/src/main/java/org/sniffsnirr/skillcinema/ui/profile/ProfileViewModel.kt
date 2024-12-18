package org.sniffsnirr.skillcinema.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.sniffsnirr.skillcinema.room.dbo.CollectionCountMovies
import org.sniffsnirr.skillcinema.room.dbo.CollectionDBO
import org.sniffsnirr.skillcinema.ui.home.model.MovieRVModel
import org.sniffsnirr.skillcinema.usecases.CreateCollectionUsecase
import org.sniffsnirr.skillcinema.usecases.DeleteAllMoviesFromCollectionUsecase
import org.sniffsnirr.skillcinema.usecases.DeleteCollectionUsecase
import org.sniffsnirr.skillcinema.usecases.GetCollectionAndCountMoviesUsecase
import org.sniffsnirr.skillcinema.usecases.GetCountDbCollectionUsecase
import org.sniffsnirr.skillcinema.usecases.GetOneCollectionFromDBUsecase
import org.sniffsnirr.skillcinema.usecases.GetViewedMoviesUsecase
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getViewedMoviesUsecase: GetViewedMoviesUsecase,
    private val getCountDbCollectionUsecase: GetCountDbCollectionUsecase,
    private val deleteAllMoviesFromCollectionUsecase: DeleteAllMoviesFromCollectionUsecase,
    private val getCollectionAndCountMoviesUsecase: GetCollectionAndCountMoviesUsecase,
    private val deleteCollectionUsecase: DeleteCollectionUsecase,
    private val getOneCollectionFromDBUsecase: GetOneCollectionFromDBUsecase,
    private val createCollectionUsecase: CreateCollectionUsecase
) : ViewModel() {
    private val _viewedMovies = MutableStateFlow<List<MovieRVModel>?>(emptyList())
    val viewedMovies = _viewedMovies.asStateFlow()

    private val _countViewedMovies = MutableStateFlow(0)
    val countViewedMovies = _countViewedMovies.asStateFlow()

    private val _interestedMovies = MutableStateFlow<List<MovieRVModel>?>(emptyList())
    val interestedMovies = _interestedMovies.asStateFlow()

    private val _countInterestedMovies = MutableStateFlow(0)
    val countInterestedMovies = _countInterestedMovies.asStateFlow()

    private val _collectionsForRV = MutableStateFlow<List<CollectionCountMovies>?>(emptyList())
    val collectionsForRV = _collectionsForRV.asStateFlow()

    private val _viewedCollection = MutableStateFlow<CollectionDBO?>(null)
    val viewedCollection = _viewedCollection.asStateFlow()

    private val _interestedCollection = MutableStateFlow<CollectionDBO?>(null)
    val interestedCollection = _interestedCollection.asStateFlow()

    private val _error = Channel<Boolean>() // для передачи ошибки соединения с сервисом поиска
    val error = _error.receiveAsFlow()

    init{
        getViewedCollectionInfo()
        getInterestedCollectionInfo()
    }

    fun loadViewedMovies() {
        viewModelScope.launch(Dispatchers.IO) {// Запуск загрузки коллекции просмотренных фильмов
            kotlin.runCatching {
                getViewedMoviesUsecase.getViewedMovies(ProfileFragment.ID_VIEWED_COLLECTION)
            }.fold(
                onSuccess = {val movies=it.toMutableList()
                                 movies.add(MovieRVModel(isButton = true))// кнопка - удалить все
                             _viewedMovies.value = movies },
                onFailure = {
                    Log.d("Error", "Загрузка просмотренных фильмов: ${it.message}")
                    _error.send(true)  // показывать диалог с ошибкой - где onFailure
                }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {// Запуск загрузки размера коллекции просмотренных фильмов
            kotlin.runCatching {
                getCountDbCollectionUsecase.getCountCollection(ProfileFragment.ID_VIEWED_COLLECTION)
            }.fold(
                onSuccess = { _countViewedMovies.value = it },
                onFailure = {
                    Log.d("Error", "Загрузка размера коллекции просмотренных фильмов: ${it.message}")
                    _error.send(true)  // показывать диалог с ошибкой - где onFailure
             }
            )
        }
    }

    fun loadInterestedMovies() {
        viewModelScope.launch(Dispatchers.IO) {// Запуск загрузки коллекции интересных фильмов
            kotlin.runCatching {
                getViewedMoviesUsecase.getViewedMovies(ProfileFragment.ID_INTERESTED_COLLECTION)
            }.fold(
                onSuccess = {  val movies=it.toMutableList()
                                   movies.add(MovieRVModel(isButton = true)) // кнопка - удалить все
                              _interestedMovies.value = movies.toList() },
                onFailure = {
                    Log.d("Error", "Загрузка интересных фильмов: ${it.message}")
                    _error.send(true)  // показывать диалог с ошибкой - где onFailure
                }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {// Запуск загрузки размера коллекции интересных фильмов
            kotlin.runCatching {
                getCountDbCollectionUsecase.getCountCollection(ProfileFragment.ID_INTERESTED_COLLECTION)
            }.fold(
                onSuccess = { _countInterestedMovies.value = it },
                onFailure = {
                    Log.d("Error", "Загрузка размера коллекции интересных фильмов: ${it.message}")
                    _error.send(true)  // показывать диалог с ошибкой - где onFailure
                }
            )
        }
    }

    fun loadCollections() {
        viewModelScope.launch(Dispatchers.IO) {// Запуск загрузки коллекций
            kotlin.runCatching {
                getCollectionAndCountMoviesUsecase.getCollectionAndCountMovies()
            }.fold(
                onSuccess = { _collectionsForRV.value = it },
                onFailure = {
                    Log.d("Error", "Загрузка коллекций: ${it.message}")
                    _error.send(true)  // показывать диалог с ошибкой - где onFailure
                }
            )
        }
    }

    fun clearViewedCollection() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteAllMoviesFromCollectionUsecase.deleteAllMovieFromCollection(ProfileFragment.ID_VIEWED_COLLECTION)
            loadViewedMovies()
        }
    }

    fun clearInterstedCollection() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteAllMoviesFromCollectionUsecase.deleteAllMovieFromCollection(ProfileFragment.ID_INTERESTED_COLLECTION)
            loadInterestedMovies()
        }
    }

    fun deleteCollection(collection: CollectionCountMovies) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteCollectionUsecase.deleteCollection(collection)
            loadCollections()
        }
    }

    private fun getViewedCollectionInfo() {
        viewModelScope.launch(Dispatchers.IO) {// загрузка самой viewed коллекции
            kotlin.runCatching {
                getOneCollectionFromDBUsecase.getCollectionById(ProfileFragment.ID_VIEWED_COLLECTION)
            }.fold(
                onSuccess = { _viewedCollection.value = it },
                onFailure = {
                    Log.d("Error", "Загрузка viewed коллекции: ${it.message}")
                    _error.send(true)  // показывать диалог с ошибкой - где onFailure
                }
            )
        }
    }

    private fun getInterestedCollectionInfo() {
        viewModelScope.launch(Dispatchers.IO) {// загрузка самой interested коллекции
            kotlin.runCatching {
                getOneCollectionFromDBUsecase.getCollectionById(ProfileFragment.ID_INTERESTED_COLLECTION)
            }.fold(
                onSuccess = { _interestedCollection.value = it },
                onFailure = {
                    Log.d("Error", "Загрузка interested коллекции: ${it.message}")
                    _error.send(true)  // показывать диалог с ошибкой - где onFailure
                }
            )
        }
    }

    fun createCollection(nameCollection: String) {
        viewModelScope.launch(Dispatchers.IO) {
            createCollectionUsecase.insertCollection(nameCollection)
            loadCollections()
        }
    }

}