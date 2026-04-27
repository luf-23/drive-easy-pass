package org.dep.backend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner init(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.update("DELETE FROM exam_reservations");
            jdbcTemplate.update("UPDATE exam_schedules SET reserved_slots = 0");
            System.out.println("=== 预约数据已重置 ===");
        };
    }
}