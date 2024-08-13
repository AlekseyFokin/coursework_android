package org.sniffsnirr.skillcinema.ui.home.model

data class MainModel(
        val category: String,
        val MovieRVModelList: List<MovieRVModel>,
        val categoryDescription:Triple<String,Int?,Int?>,
        val banner:Boolean
    )

