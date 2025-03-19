package com.moviereservationapi.movie.utils;

import com.moviereservationapi.movie.Enum.MovieGenre;
import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class MovieDataSeeder {

    private final Random random = new Random();

    private final String[] movieTitles = {
            "The Lost City", "Midnight Dreams", "Beyond the Horizon", "The Silent Echo",
            "Shadows of Tomorrow", "The Last Journey", "Whispers in the Dark", "Forgotten Path",
            "The Hidden Truth", "Star Voyager", "Crystal Memories", "The Ancient Secret",
            "Dawn of Destiny", "Winter's Promise", "The Broken Cipher", "Ocean's Calling",
            "The Phantom Menace", "Lightning Strike", "Emerald Forest", "Desert Storm"
    };

    @Bean
    public CommandLineRunner seedMovieData(MovieRepository movieRepository) {
        return args -> {
            if (movieRepository.count() > 0) {
                System.out.println("Movies already seeded. Skipping movie seeding.");
                return;
            }

            List<Movie> movies = new ArrayList<>();

            // Create 20 random movies
            for (int i = 0; i < 20; i++) {
                movies.add(createRandomMovie());
            }

            movies.add(Movie.builder()
                    .title("The Shawshank Redemption")
                    .duration(142.0)
                    .releaseDate(LocalDateTime.of(1994, 9, 23, 0, 0))
                    .movieGenre(MovieGenre.DRAMA)
                    .showtimeIds(new ArrayList<>())
                    .build());

            movies.add(Movie.builder()
                    .title("The Godfather")
                    .duration(175.0)
                    .releaseDate(LocalDateTime.of(1972, 3, 24, 0, 0))
                    .movieGenre(MovieGenre.CRIME)
                    .showtimeIds(new ArrayList<>())
                    .build());

            movies.add(Movie.builder()
                    .title("Pulp Fiction")
                    .duration(154.0)
                    .releaseDate(LocalDateTime.of(1994, 10, 14, 0, 0))
                    .movieGenre(MovieGenre.CRIME)
                    .showtimeIds(new ArrayList<>())
                    .build());

            movies.add(Movie.builder()
                    .title("The Dark Knight")
                    .duration(152.0)
                    .releaseDate(LocalDateTime.of(2008, 7, 18, 0, 0))
                    .movieGenre(MovieGenre.ACTION)
                    .showtimeIds(new ArrayList<>())
                    .build());

            movies.add(Movie.builder()
                    .title("Inception")
                    .duration(148.0)
                    .releaseDate(LocalDateTime.of(2010, 7, 16, 0, 0))
                    .movieGenre(MovieGenre.SCIFI)
                    .showtimeIds(new ArrayList<>())
                    .build());

            movieRepository.saveAll(movies);
            System.out.println("Successfully seeded " + movies.size() + " movies.");
        };
    }

    private Movie createRandomMovie() {
        String title = movieTitles[random.nextInt(movieTitles.length)];

        if (random.nextBoolean()) {
            if (random.nextBoolean()) {
                title += " " + (random.nextInt(3) + 2);
            } else {
                String[] suffixes = {": Resurrection", ": The Beginning", ": Final Chapter", ": New Generation"};
                title += suffixes[random.nextInt(suffixes.length)];
            }
        }

        double duration = 85.0 + random.nextDouble() * 125.0;
        duration = Math.round(duration * 10.0) / 10.0;

        int year = 2000 + random.nextInt(25);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        LocalDateTime releaseDate = LocalDateTime.of(year, month, day, 0, 0);

        MovieGenre[] genres = MovieGenre.values();
        MovieGenre genre = genres[random.nextInt(genres.length)];

        return Movie.builder()
                .title(title)
                .duration(duration)
                .releaseDate(releaseDate)
                .movieGenre(genre)
                .showtimeIds(new ArrayList<>())
                .build();
    }

}
