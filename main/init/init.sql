
CREATE TABLE IF NOT EXISTS city (
    id BIGSERIAL PRIMARY KEY,
    city_name VARCHAR(255),
    region_name VARCHAR(255)
);

COPY city (id, city_name, region_name) 
FROM '/docker-entrypoint-initdb.d/cities.csv' 
DELIMITER ',' 
CSV HEADER;
