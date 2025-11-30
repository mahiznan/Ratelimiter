package com.glint.ratelimiter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private static final Logger log = LoggerFactory.getLogger(MovieController.class);

    @GetMapping("/top/{count}")
    public List<Movie> getTop(@PathVariable int count) {
        return Stream.of(
                new Movie(1, "The Shawshank Redemption", 1994, "tt0111161"),
                new Movie(2, "The Godfather", 1972, "tt0068646"),
                new Movie(3, "The Dark Knight", 2008, "tt0468569"),
                new Movie(4, "The Godfather Part II", 1974, "tt0071562"),
                new Movie(5, "12 Angry Men", 1957, "tt0050083")
        ).limit(count).toList();
    }

    public record Movie(int rank, String title, int year, String imdb_id) {
    }

}
