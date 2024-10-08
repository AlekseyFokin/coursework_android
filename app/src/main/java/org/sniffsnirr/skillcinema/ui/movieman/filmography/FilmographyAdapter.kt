package org.sniffsnirr.skillcinema.ui.movieman.filmography

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.sniffsnirr.skillcinema.databinding.MovieItemBinding

import org.sniffsnirr.skillcinema.ui.home.model.MovieRVModel

class FilmographyAdapter(val onMovieClick: (Int) -> Unit): RecyclerView.Adapter<FilmographyAdapter.MovieViewHolder>() {

    private var movieList: List<MovieRVModel> = emptyList()

    fun setData(movieList: List<MovieRVModel>) {
        this.movieList = movieList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun getItemCount() = movieList.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movieList[position]
        with(holder.binding) {
            Glide
                .with(poster.context)
                .load(movie.imageUrl)
                .into(poster)
            movieName.text = movie.movieName
            genre.text = movie.movieGenre
            if (movie.rate.trim() == "0" || movie.rate.trim() == "0.0") {
                raiting.visibility = View.INVISIBLE
            } else {
                raiting.text = movie.rate
            }
            if (movie.viewed) {
                viewed.visibility = View.VISIBLE
            } else {
                viewed.visibility = View.INVISIBLE
            }
        }
        holder.binding.root.setOnClickListener {
            onMovieClick(movie.kinopoiskId?:0)
        }
    }
    inner class MovieViewHolder(val binding: MovieItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}