package org.sniffsnirr.skillcinema.entities.onlyonemovie

import org.sniffsnirr.skillcinema.entities.Country
import org.sniffsnirr.skillcinema.entities.Genre


data class OnlyOneMovie(
    val completed: Boolean,
    val countries: List<Country>,
    val coverUrl: String,
    val description: String,
    val editorAnnotation: String?,
    val endYear: String?,
    val filmLength: Int,
    val genres: List<Genre>,
    val has3D: Boolean,
    val hasImax: Boolean,
    val imdbId: String,
    val isTicketsAvailable: Boolean,
    val kinopoiskHDId: String,
    val kinopoiskId: Int,
    val lastSync: String,
    val logoUrl: String?,
    val nameEn: String?,
    val nameOriginal: String,
    val nameRu: String,
    val posterUrl: String,
    val posterUrlPreview: String,
    val productionStatus: String?,
    val ratingAgeLimits: String,
    val ratingAwait: String?,
    val ratingAwaitCount: Int,
    val ratingFilmCritics: Double,
    val ratingFilmCriticsVoteCount: Int,
    val ratingGoodReview: Double,
    val ratingGoodReviewVoteCount: Int,
    val ratingImdb: Double,
    val ratingImdbVoteCount: Int,
    val ratingKinopoisk: Double,
    val ratingKinopoiskVoteCount: Int,
    val ratingMpaa: String,
    val ratingRfCritics: Double,
    val ratingRfCriticsVoteCount: Int,
    val reviewsCount: Int,
    val serial: Boolean,
    val shortDescription: String,
    val shortFilm: Boolean,
    val slogan: String,
    val startYear: String?,
    val type: String,
    val webUrl: String,
    val year: Int
)