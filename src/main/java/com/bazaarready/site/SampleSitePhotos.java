package com.bazaarready.site;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The built-in sample sites are added by a database migration rather than published through the agent,
 * so their stock photos are looked up here, in the background, when the app starts.
 */
@Component
class SampleSitePhotos implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final ImageService images;

    SampleSitePhotos(JdbcTemplate jdbc, ImageService images) {
        this.jdbc = jdbc;
        this.images = images;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> queries = jdbc.queryForList("""
                select b.hero_query from business b where b.sample and b.hero_query is not null
                union
                select p.image_query from product p join business b on b.id = p.business_id
                where b.sample and p.image_query is not null
                """, String.class);
        images.ensureLater(queries);
    }
}
