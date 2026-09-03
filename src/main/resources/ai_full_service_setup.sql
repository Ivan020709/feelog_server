-- 기존 MySQL feelog DB에서 실행할 수 있는 대화 저장 테이블입니다.
-- JPA ddl-auto:update가 자동 생성하지만, 팀 DB 명세 확인 및 수동 생성용으로 제공합니다.
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    character VARCHAR(30),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_chat_session_user (user_id, updated_at)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    sender VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    tool_name VARCHAR(50),
    file_name VARCHAR(255),
    file_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    INDEX idx_chat_message_session (session_id, created_at),
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id) REFERENCES chat_session(session_id)
        ON DELETE CASCADE
);
