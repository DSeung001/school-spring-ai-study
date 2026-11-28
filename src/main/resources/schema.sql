CREATE TABLE IF NOT EXISTS questions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(200) NOT NULL,
    answer TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS question_sources (
    question_id BIGINT NOT NULL,
    source_order INT NOT NULL,
    title TEXT NOT NULL,
    chunk_id VARCHAR(100) NOT NULL,
    source_text TEXT NOT NULL,
    score DOUBLE NOT NULL,
    PRIMARY KEY (question_id, source_order),
    CONSTRAINT fk_question_sources_question FOREIGN KEY (question_id)
        REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    content VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_question FOREIGN KEY (question_id)
        REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
