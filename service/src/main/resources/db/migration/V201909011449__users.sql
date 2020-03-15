CREATE TABLE users (
  id BIGSERIAL NOT NULL,
  email TEXT UNIQUE NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT,
  password_hash TEXT NOT NULL,
  created_date TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  updated_date TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE accounts (
  id BIGSERIAL NOT NULL,
  name TEXT NOT NULL,
  type TEXT NOT NULL,
  created_date TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  updated_date TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE memberships (
  id BIGSERIAL NOT NULL,
  user_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  role TEXT NOT NULL,
  created_date TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  updated_date TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
  UNIQUE (user_id, account_id),
  PRIMARY KEY (id)
);

CREATE TABLE invalidated_tokens (
  id BIGSERIAL NOT NULL,
  value TEXT UNIQUE NOT NULL,
  user_id BIGINT NOT NULL,
  created_date TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  PRIMARY KEY (id)
);

CREATE INDEX ON invalidated_tokens (user_id);
