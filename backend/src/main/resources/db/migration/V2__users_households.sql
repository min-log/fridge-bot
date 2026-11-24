CREATE TABLE users (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email       varchar(255),
    nickname    varchar(50)  NOT NULL,
    provider    varchar(20)  NOT NULL,
    provider_id varchar(100) NOT NULL,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_provider UNIQUE (provider, provider_id)
);

CREATE TABLE households (
    id                     bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                   varchar(50) NOT NULL,
    invite_code            varchar(16) NOT NULL,
    invite_code_expires_at timestamptz NOT NULL,
    created_at             timestamptz NOT NULL DEFAULT now(),
    updated_at             timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uk_households_invite_code UNIQUE (invite_code)
);

CREATE TABLE household_members (
    household_id bigint      NOT NULL REFERENCES households (id) ON DELETE CASCADE,
    user_id      bigint      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role         varchar(10) NOT NULL CHECK (role IN ('OWNER', 'MEMBER')),
    joined_at    timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (household_id, user_id),
    -- MVP: 사용자 한 명은 가구 하나에만 속함
    CONSTRAINT uk_household_members_user UNIQUE (user_id)
);
