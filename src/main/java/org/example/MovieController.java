package org.example;

import org.example.model.Movie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies/search")
    public ResponseEntity<?> search(@RequestParam(name = "query", required = false) String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Query parameter is required. Example: /movies/search?query=comedy")
            );
        }

        try {
            List<Movie> movies = movieService.search(query);
            return ResponseEntity.ok(movies);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        } catch (IllegalStateException exception) {
            // The server remains running even when the external embedding API fails.
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "Netflix Movie Recommendation System");
    }
}
