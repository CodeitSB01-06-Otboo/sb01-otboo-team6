DROP TABLE IF EXISTS
    attributes_defs,
    clothes,
    clothes_attributes,
    recommend_clothes,
    clothes_feeds,
    comments,
    direct_messages,
    feeds,
    feeds_likes,
    follows,
    notifications,
    profiles,
    users,
    weather_location_names,
    weathers,
    profile_location_names,
    user_linked_oauth_providers
    CASCADE;
DROP TYPE IF EXISTS notification_type CASCADE;

CREATE TABLE weathers
(
    id                        UUID PRIMARY KEY,
    forecasted_at             TIMESTAMP        NOT NULL, -- 발표 시각(baseDate+baseTime)
    forecast_at               TIMESTAMP        NOT NULL, -- 예보 시각(fcstDate+fcstTime)
    lat                       DOUBLE PRECISION NOT NULL,
    lon                       DOUBLE PRECISION NOT NULL,
    grid_x                    INTEGER         NOT NULL,
    grid_y                    INTEGER         NOT NULL,
    sky_status                VARCHAR(15)      NOT NULL, -- CLEAR / MOSTLY_CLOUDY / CLOUDY
    precipitation_type        VARCHAR(15)      NOT NULL, -- RAIN / SNOW …
    temperature_current       DOUBLE PRECISION,
    temperature_min           DOUBLE PRECISION,
    temperature_max           DOUBLE PRECISION,
    precipitation_amount      DOUBLE PRECISION,
    precipitation_amount_text VARCHAR(20),
    precipitation_probability DOUBLE PRECISION,
    humidity                  DOUBLE PRECISION,
    snow_amount               DOUBLE PRECISION,
    lightning                 DOUBLE PRECISION,
    wind_speed                DOUBLE PRECISION,
    wind_level                INTEGER,
    wind_direction            DOUBLE PRECISION,
    wind_u                    DOUBLE PRECISION,
    wind_v                    DOUBLE PRECISION,
    created_at                TIMESTAMP,
    updated_at                TIMESTAMP,
    UNIQUE (forecast_at, grid_x, grid_y)
);

-- forecast_at 단일 컬럼 인덱스 (00:00 조건 필터용)
CREATE INDEX IF NOT EXISTS ix_weathers_forecast_at
    ON weathers (forecast_at);

-- 격자 좌표 복합 인덱스
CREATE INDEX IF NOT EXISTS ix_weathers_grid
    ON weathers (grid_x, grid_y);


/* ---------------- 행정동 이름 목록 ---------------- */
CREATE TABLE weather_location_names
(
    id            UUID PRIMARY KEY,
    weather_id    UUID         NOT NULL
        REFERENCES weathers (id) ON DELETE CASCADE,
    location_name VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP,
    /* ── 동일 Weather-ID 내에서 중복 방지 ── */
    UNIQUE (weather_id, location_name)
);


-- 유저
CREATE TABLE users
(
    id                     UUID PRIMARY KEY,
    email                  VARCHAR NOT NULL UNIQUE,
    password               VARCHAR NOT NULL,
    name                   VARCHAR NOT NULL,
    role                   VARCHAR NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    locked                 BOOLEAN NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    linked_oauth_providers TEXT[]
);

-- 프로필
CREATE TABLE profiles
(
    id                      UUID PRIMARY KEY,
    name                    VARCHAR          NOT NULL,
    gender                  VARCHAR          NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    birth_date              DATE             NOT NULL,
    latitude                DOUBLE PRECISION NOT NULL,
    longitude               DOUBLE PRECISION NOT NULL,
    x                       INT              NOT NULL,
    y                       INT              NOT NULL,
    location_names          TEXT[],
    temperature_sensitivity INT              NOT NULL CHECK (temperature_sensitivity BETWEEN 1 AND 5),
    profile_image_url       VARCHAR,
    created_at              TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP
);

-- 프로필-유저 1:1 연결
ALTER TABLE profiles
    ADD CONSTRAINT fk_profiles_user FOREIGN KEY (id) REFERENCES users (id);





CREATE TABLE feeds
(
    id            UUID PRIMARY KEY NOT NULL,
    user_id       UUID             NOT NULL,
    weather_id    UUID             NOT NULL,
    content       TEXT             NOT NULL,
    like_count    BIGINT           NOT NULL,
    comment_count INTEGER          NOT NULL,
    liked_by_me   BOOLEAN          NOT NULL,
    created_at    TIMESTAMP        NOT NULL,
    updated_at    TIMESTAMP        NOT NULL
);

ALTER TABLE feeds
    ADD CONSTRAINT fk_feeds_user_id FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE;

ALTER TABLE feeds
    ADD CONSTRAINT fk_feeds_weather_id
        FOREIGN KEY (weather_id)
            REFERENCES weathers (id);


CREATE TABLE comments
(
    id          UUID PRIMARY KEY NOT NULL,
    author_id   UUID             NOT NULL,
    created_at  TIMESTAMP NULL,
    updated_at  TIMESTAMP NULL,
    content     TEXT             NOT NULL,
    author_name VARCHAR          NOT NULL,
    feed_id     UUID             NOT NULL
);

ALTER TABLE comments
    ADD CONSTRAINT fk_comments_user_id FOREIGN KEY (author_id)
        REFERENCES users (id)
        ON DELETE CASCADE;

ALTER TABLE comments
    ADD CONSTRAINT fk_feeds_user_id FOREIGN KEY (feed_id)
        REFERENCES feeds (id)
        ON DELETE CASCADE;


CREATE TABLE feeds_likes
(
    id         UUID PRIMARY KEY NOT NULL,
    user_id    UUID             NOT NULL,
    created_at TIMESTAMP        NOT NULL,
    updated_at TIMESTAMP        NOT NULL,
    feed_id    UUID             NOT NULL
);
ALTER TABLE feeds_likes
    ADD CONSTRAINT fk_comments_likes_id FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE;

ALTER TABLE feeds_likes
    ADD CONSTRAINT fk_feeds_likes_id FOREIGN KEY (feed_id)
        REFERENCES feeds (id)
        ON DELETE CASCADE;


CREATE TYPE notification_type AS ENUM ('INFO', 'WARNING', 'ERROR');


CREATE TABLE notifications
(
    id         UUID PRIMARY KEY,
    user_id    UUID              NOT NULL,
    content    TEXT              NOT NULL,
    created_at TIMESTAMP         NOT NULL,
    is_read    BOOLEAN           NOT NULL,
    type       notification_type NOT NULL
);
ALTER TABLE notifications
    ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id);


CREATE TABLE clothes
(
    id         UUID PRIMARY KEY,
    owner_id   UUID        NOT NULL,
    name       VARCHAR(40) NOT NULL,
    type       VARCHAR(20) NOT NULL,
    image_url  TEXT        NULL,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP NULL
);


-- 의상 속성
CREATE TABLE attributes_defs
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(40) NOT NULL,
    selectable_values TEXT[] NOT NULL,
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP NULL
);

-- 의상-의상속성 중간테이블
CREATE TABLE clothes_attributes
(
    id           UUID PRIMARY KEY,
    clothes_id   UUID        NOT NULL,
    attribute_id UUID        NOT NULL,
    value        VARCHAR(40) NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP NULL
);

-- 추천 의상 테이블
CREATE TABLE recommend_clothes (
   id UUID PRIMARY KEY,
   weather_id UUID NOT NULL REFERENCES weathers(id),
   user_id UUID NOT NULL REFERENCES users(id),
   clothes_ids uuid[] NOT NULL,
   created_at TIMESTAMP NOT NULL,
   updated_at TIMESTAMP NULL
);


-- 제약 조건
-- user(1) -> clothes(N)
ALTER TABLE clothes
    ADD CONSTRAINT fk_clothes_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE;

-- clothes(1) -> clothes_attributes(N)
ALTER TABLE clothes_attributes
    ADD CONSTRAINT fk_clothes_attributes_clothes FOREIGN KEY (clothes_id) REFERENCES clothes (id) ON DELETE CASCADE;

-- attributes_defs(1) -> clothes_attributes(N)
ALTER TABLE clothes_attributes
    ADD CONSTRAINT fk_clothes_attributes_attributes FOREIGN KEY (attribute_id) REFERENCES attributes_defs (id) ON DELETE CASCADE;



CREATE TABLE follows
(
    id          UUID PRIMARY KEY,
    follower_id UUID      NOT NULL,
    followee_id UUID      NOT NULL,
    created_at  TIMESTAMP NOT NULL,

    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_follows_pair UNIQUE (follower_id, followee_id)
);

CREATE TABLE direct_messages
(
    id          UUID PRIMARY KEY,
    sender_id   UUID      NOT NULL,
    receiver_id UUID      NOT NULL,
    content     TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL,

    CONSTRAINT fk_dm_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_dm_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE clothes_feeds
(
    id UUID PRIMARY KEY,
    clothes_id UUID NOT NULL,
    feed_id UUID NOT NULL,

    CONSTRAINT fk_clothes_feeds_clothes FOREIGN KEY (clothes_id) REFERENCES clothes(id) ON DELETE CASCADE,
    CONSTRAINT fk_clothes_feeds_feed FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE,
    CONSTRAINT uq_clothes_feed UNIQUE (clothes_id, feed_id) -- 중복 방지
);


CREATE TABLE profile_location_names
(
    profile_id UUID NOT NULL,
    location_name VARCHAR(255),
    FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE
);


CREATE TABLE user_linked_oauth_providers
(
    user_id UUID NOT NULL,
    provider VARCHAR(255),
    PRIMARY KEY (user_id, provider), --주키 설정
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
)

