package com.sisi414.parcial.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Carga los datos migrados desde los DBF (seed/seed.sql) una sola vez:
 * solo si la tabla MES esta vacia (BD recien creada). Los cambios hechos por la API se conservan
 * entre reinicios porque la BD H2 vive en el archivo ./data/sisi414_db.mv.db.
 */
@Component
public class SeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final boolean enabled;

    public SeedDataLoader(DataSource dataSource, JdbcTemplate jdbcTemplate,
                          @Value("${sisi414.seed.enabled:true}") boolean enabled) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        Integer filas = jdbcTemplate.queryForObject("select count(*) from mes", Integer.class);
        if (filas != null && filas > 0) {
            log.info("La base ya tiene datos; no se vuelve a cargar la semilla.");
            return;
        }
        log.info("Base vacia: cargando datos migrados desde DBF (seed/seed.sql)...");
        ResourceDatabasePopulator populator =
                new ResourceDatabasePopulator(false, false, "UTF-8", new ClassPathResource("seed/seed.sql"));
        populator.execute(dataSource);
        log.info("Carga inicial completada.");
    }
}
