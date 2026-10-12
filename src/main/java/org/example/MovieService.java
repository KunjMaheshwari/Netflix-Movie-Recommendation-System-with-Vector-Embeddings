package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Movie;
import org.example.model.MovieData;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MovieService {

    private static final int TOP_RESULTS = 5;

    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    /* Initialized lazily so a slow/unavailable embedding API does not prevent
       Spring Boot from starting and serving the frontend. */
    private volatile List<Movie> moviesEmbedding = List.of();

    public MovieService(EmbeddingModel embeddingModel, ObjectMapper objectMapper) {
        this.embeddingModel = embeddingModel;
        this.objectMapper = objectMapper;
    }

    private synchronized void initializeMoviesIfNeeded() {
        if (!moviesEmbedding.isEmpty()) {
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("movies.json");
            List<MovieData> movieDataList;

            try (InputStream inputStream = resource.getInputStream()) {
                movieDataList = objectMapper.readValue(
                        inputStream,
                        new TypeReference<List<MovieData>>() { }
                );
            }

            if (movieDataList == null || movieDataList.isEmpty()) {
                throw new IllegalStateException("movies.json contains no movies.");
            }

            for (MovieData data : movieDataList) {
                if (data == null || isBlank(data.getTitle()) || isBlank(data.getDescription())) {
                    throw new IllegalStateException(
                            "Every movie in movies.json must have a title and description."
                    );
                }
            }

            // Batch the descriptions into one embedding-model operation instead
            // of making one remote API call per movie during application startup.
            List<String> descriptions = movieDataList.stream()
                    .map(MovieData::getDescription)
                    .toList();

            List<float[]> embeddings = embeddingModel.embed(descriptions);

            if (embeddings == null || embeddings.size() != movieDataList.size()) {
                throw new IllegalStateException(
                        "The embedding provider returned an unexpected number of vectors."
                );
            }

            List<Movie> loadedMovies = new ArrayList<>(movieDataList.size());
            for (int i = 0; i < movieDataList.size(); i++) {
                float[] vector = embeddings.get(i);
                if (vector == null || vector.length == 0) {
                    throw new IllegalStateException(
                            "The embedding provider returned an empty vector for a movie."
                    );
                }

                MovieData data = movieDataList.get(i);
                loadedMovies.add(new Movie(data.getTitle(), data.getDescription(), vector));
            }

            moviesEmbedding = List.copyOf(loadedMovies);
            System.out.printf("Loaded %d movies; embedding dimensions: %d%n",
                    moviesEmbedding.size(), moviesEmbedding.get(0).getEmbedding().length);

        } catch (Exception exception) {
            // Do not cache the failure: a subsequent request can retry after the
            // API key, network, provider quota, or configuration is fixed.
            throw new IllegalStateException(
                    "Movie embeddings could not be initialized. Verify OPENROUTER_API_KEY, " +
                    "OpenRouter connectivity, model availability, and account quota, then retry.",
                    exception
            );
        }
    }

    public List<Movie> search(String query) {
        if (isBlank(query)) {
            throw new IllegalArgumentException("Search query cannot be empty.");
        }

        initializeMoviesIfNeeded();

        final float[] queryEmbedding;
        try {
            queryEmbedding = embeddingModel.embed(query.trim());
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "The embedding provider could not process this search. Check the API key, " +
                    "model availability, network connection, and account quota.",
                    exception
            );
        }

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new IllegalStateException("The embedding provider returned an empty query vector.");
        }

        return moviesEmbedding.stream()
                .map(movie -> new MovieScore(movie,
                        cosineSimilarity(queryEmbedding, movie.getEmbedding())))
                .sorted(Comparator.comparingDouble(MovieScore::score).reversed())
                .limit(TOP_RESULTS)
                .map(MovieScore::movie)
                .toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) {
            throw new IllegalArgumentException(
                    "Embedding vectors must be non-empty and have equal dimensions."
            );
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += (double) a[i] * b[i];
            magnitudeA += (double) a[i] * a[i];
            magnitudeB += (double) b[i] * b[i];
        }

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    private record MovieScore(Movie movie, double score) { }
}
