-- Auth Service database
CREATE DATABASE srll_auth;

-- Card Service database
CREATE DATABASE srll_cards;

-- Auth Service schema
\c srll_auth;

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Card Service schema
\c srll_cards;

CREATE TABLE decks (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    language    VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE cards (
    id              BIGSERIAL PRIMARY KEY,
    deck_id         BIGINT       NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    front           TEXT         NOT NULL,
    back            TEXT         NOT NULL,
    interval_days   INT          NOT NULL DEFAULT 1,
    repetitions     INT          NOT NULL DEFAULT 0,
    ease_factor     FLOAT        NOT NULL DEFAULT 2.5,
    next_review     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cards_next_review ON cards(deck_id, next_review);
CREATE INDEX idx_decks_user_id ON decks(user_id);
