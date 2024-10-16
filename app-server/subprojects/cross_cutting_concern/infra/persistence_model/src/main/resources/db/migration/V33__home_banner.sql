CREATE TABLE IF NOT EXISTS home_banner (
    id VARCHAR(36) NOT NULL,
    logging_key VARCHAR(256) NOT NULL,
    image_url TEXT NOT NULL,
    click_page_url TEXT NOT NULL,
    click_page_title VARCHAR(32) NOT NULL,
    start_at TIMESTAMP(6) WITH TIME ZONE NULL,
    end_at TIMESTAMP(6) WITH TIME ZONE NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_home_banner_1 ON home_banner (end_at);
