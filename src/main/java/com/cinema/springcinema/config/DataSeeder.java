package com.cinema.springcinema.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.domain.Screening;
import com.cinema.springcinema.domain.Showroom;
import com.cinema.springcinema.domain.User;
import com.cinema.springcinema.repository.MovieRepository;
import com.cinema.springcinema.repository.ScreeningRepository;
import com.cinema.springcinema.repository.ShowroomRepository;
import com.cinema.springcinema.repository.UserRepository;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedData(UserRepository userRepository, ShowroomRepository showroomRepository,
            MovieRepository movieRepository, ScreeningRepository screeningRepository) {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already seeded, skipping...");
                return;
            }

            log.info("Seeding database with initial data...");

            User admin = userRepository.save(new User("Admin", "admin@cinema.com",
                    "$2a$12$HxM6ZXJnfb2RzwUFMNr.geuofP5Glx7hoL56vKeBMYNyLRyJ4dnU2", User.Role.ADMIN));

            User demo = userRepository.save(new User("Demo User", "demo@cinema.com",
                    "$2a$12$HxM6ZXJnfb2RzwUFMNr.geuofP5Glx7hoL56vKeBMYNyLRyJ4dnU2", User.Role.USER));

            Showroom s1 = showroomRepository.save(new Showroom("Saal 1", 10, 10));
            Showroom s2 = showroomRepository.save(new Showroom("Saal 2", 10, 15));
            Showroom s3 = showroomRepository.save(new Showroom("Saal 3", 10, 20));

            Movie m1 = movieRepository.save(new Movie(
                    "The Matrix",
                    "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.",
                    136,
                    "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg"));

            Movie m2 = movieRepository.save(new Movie(
                    "Inception",
                    "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a CEO.",
                    148,
                    "https://image.tmdb.org/t/p/w500/t5WUY5ZSxwVIVExaMZmmIj88BKA.jpg"));

            Movie m3 = movieRepository.save(new Movie(
                    "The Dark Knight",
                    "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                    152,
                    "https://image.tmdb.org/t/p/w500/z1DfRQf2CgnROyhVZ6ch8FbWt71.jpg"));

            Movie m4 = movieRepository.save(new Movie(
                    "Interstellar",
                    "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                    169,
                    "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"));

            Movie m5 = movieRepository.save(new Movie(
                    "Pulp Fiction",
                    "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.",
                    154,
                    "https://image.tmdb.org/t/p/w500/hOg9USqmQmglmr5kGvpyg1XkhqN.jpg"));

            Movie m6 = movieRepository.save(new Movie(
                    "Forrest Gump",
                    "The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man with an IQ of 75.",
                    142,
                    "https://image.tmdb.org/t/p/w500/zUWRCzac72YuO9k5kEWSe0aGbs7.jpg"));

            LocalDateTime now = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0);

            for (int day = 0; day < 7; day++) {
                LocalDateTime base = now.plusDays(day);

                screeningRepository.save(new Screening(m1, s1, base.withHour(14), new BigDecimal("12.00")));
                screeningRepository.save(new Screening(m1, s1, base.withHour(18), new BigDecimal("14.00")));
                screeningRepository.save(new Screening(m1, s2, base.withHour(16), new BigDecimal("13.00")));

                screeningRepository.save(new Screening(m2, s1, base.withHour(15), new BigDecimal("13.00")));
                screeningRepository.save(new Screening(m2, s3, base.withHour(19), new BigDecimal("15.00")));

                screeningRepository.save(new Screening(m3, s2, base.withHour(14), new BigDecimal("12.00")));
                screeningRepository.save(new Screening(m3, s3, base.withHour(20), new BigDecimal("15.00")));

                screeningRepository.save(new Screening(m4, s1, base.withHour(17), new BigDecimal("14.00")));
                screeningRepository.save(new Screening(m4, s2, base.withHour(20), new BigDecimal("15.00")));

                screeningRepository.save(new Screening(m5, s3, base.withHour(13), new BigDecimal("11.00")));
                screeningRepository.save(new Screening(m5, s3, base.withHour(17), new BigDecimal("13.00")));

                screeningRepository.save(new Screening(m6, s2, base.withHour(15), new BigDecimal("12.00")));
                screeningRepository.save(new Screening(m6, s1, base.withHour(19), new BigDecimal("14.00")));
            }

            log.info("Seeded: 2 users, 3 showrooms, 6 movies, {} screenings",
                    screeningRepository.count());
            log.info("Demo credentials: demo@cinema.com / password");
            log.info("Admin credentials: admin@cinema.com / password");
        };
    }
}
