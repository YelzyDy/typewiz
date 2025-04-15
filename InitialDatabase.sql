-- Create database
CREATE DATABASE TypeRush;
USE TypeRush;

-- Players table
CREATE TABLE Players (
    player_id INT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Enemies table
CREATE TABLE Enemies (
    enemy_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    base_hp INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Words table
CREATE TABLE Words (
    word_id INT AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(50) NOT NULL UNIQUE,
    difficulty_level INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Game sessions table
CREATE TABLE GameSessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    final_score INT,
    FOREIGN KEY (player_id) REFERENCES Players(player_id)
);

-- Score history table
CREATE TABLE Scores (
    score_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT,
    enemy_id INT,
    score_value INT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES GameSessions(session_id),
    FOREIGN KEY (enemy_id) REFERENCES Enemies(enemy_id)
);

-- Enemy encounters during game sessions
CREATE TABLE SessionEnemies (
    session_enemy_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT,
    enemy_id INT,
    remaining_hp INT NOT NULL,
    FOREIGN KEY (session_id) REFERENCES GameSessions(session_id),
    FOREIGN KEY (enemy_id) REFERENCES Enemies(enemy_id)
);

-- Words encountered during game sessions
CREATE TABLE SessionWords (
    session_word_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT,
    word_id INT,
    was_correct BOOLEAN,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES GameSessions(session_id),
    FOREIGN KEY (word_id) REFERENCES Words(word_id)
);

-- Top scores view
CREATE VIEW TopScores AS
SELECT s.score_value, p.player_id, g.start_time
FROM Scores s
JOIN GameSessions g ON s.session_id = g.session_id
JOIN Players p ON g.player_id = p.player_id
ORDER BY s.score_value DESC
LIMIT 10;
