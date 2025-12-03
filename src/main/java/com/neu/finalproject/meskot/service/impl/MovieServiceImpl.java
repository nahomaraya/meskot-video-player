package com.neu.finalproject.meskot.service.impl;

import com.neu.finalproject.meskot.model.Movie;

import java.util.List;
import java.util.Optional;

public interface MovieServiceImpl {
    Movie saveMovie(Movie movie);
    List<Movie> listMovies();
    Optional<Movie> getMovieById(Long id);
}
