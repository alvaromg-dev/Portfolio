INSERT OR IGNORE INTO languages (id, code, is_default, name, enabled)
VALUES (randomblob(16), 'es', 1, 'Español', 0);

INSERT OR IGNORE INTO languages (id, code, is_default, name, enabled)
VALUES (randomblob(16), 'en', 0, 'English', 1);
