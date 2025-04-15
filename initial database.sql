--players
CREATE TABLE players (
    player_id INT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--scores
CREATE TABLE scores (
    score_id INT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL,
    points INT NOT NULL,
    duration_seconds INT DEFAULT 60,
    defeated_enemy BOOLEAN DEFAULT FALSE,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--words_used
CREATE TABLE words_used (
    word_id INT AUTO_INCREMENT PRIMARY KEY,
    score_id INT,
    word_text VARCHAR(100) NOT NULL,
    correct BOOLEAN,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (score_id) REFERENCES scores(score_id) ON DELETE CASCADE
);

--enemies
CREATE TABLE enemies (
    enemy_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    max_hp INT NOT NULL,
    difficulty_level VARCHAR(20)
);

