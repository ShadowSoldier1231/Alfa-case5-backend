
CREATE TABLE IF NOT EXISTS city (
    id BIGSERIAL PRIMARY KEY,
    city_name VARCHAR(255),
    region_name VARCHAR(255)
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM city LIMIT 1) THEN
        COPY city (id, city_name, region_name) 
        FROM '/docker-entrypoint-initdb.d/cities.csv' 
        DELIMITER ',' 
        CSV HEADER;
    END IF;
END $$;
