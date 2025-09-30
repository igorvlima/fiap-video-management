CREATE TABLE video (
                       id UUID PRIMARY KEY,
                       name TEXT NOT NULL,
                       url TEXT NOT NULL,
                       customer_id BIGINT NOT NULL,
                       customer_email TEXT,
                       status TEXT,
                       created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
