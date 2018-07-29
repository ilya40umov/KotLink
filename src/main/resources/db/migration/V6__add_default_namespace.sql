-- a fake user account that will own "default" namespace
INSERT INTO user_account (email) VALUES ('admin@kotlink.org');

-- the "default" (has empty keyword) namespace for aliases
INSERT INTO namespace (keyword, description, owner_account_id)
VALUES ('', 'Namespace for links without a common prefix', (SELECT id FROM user_account WHERE email = 'admin@kotlink.org'));