CREATE TABLE public.country (
  id VARCHAR PRIMARY KEY,
  name VARCHAR,
  country_code int,
  is_active BOOLEAN,
  is_deleted BOOLEAN,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR NOT NULL,
  updated_by VARCHAR,
  tenant_id VARCHAR
);
INSERT INTO
  public.country (
    id,
    name,
    country_code,
    is_active,
    is_deleted,
    created_at,
    updated_at,
    created_by,
    updated_by,
    tenant_id
  )
VALUES
  (
    '6260176ad4800c0021822cdd',
    'Kenya',
    254,
    true,
    false,
    '2022-04-20 23:23:38+05:30',
    '2022-08-22 18:45:27+05:30',
    '62600b74dbebaa001f8e9af0',
    '628c7dfb9ebeef0012002181',
    '6260176ad4800c0021822cdc'
  );

CREATE TABLE timezone (
	id VARCHAR PRIMARY KEY,
	abbreviation VARCHAR,
	description VARCHAR,
	timezone_offset VARCHAR,
	is_active  BOOLEAN,
	is_deleted BOOLEAN,
	created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	created_by VARCHAR  NOT NULL,
	updated_by VARCHAR
);

INSERT INTO timezone (id, updated_by, is_active, is_deleted, abbreviation, description, timezone_offset, created_by )
VALUES(1, 1, true, false, 'IST', '(UTC+05:30) Chennai, Kolkata, Mumbai, New Delhi', 'UTC+05:30', 1);

  CREATE TABLE account (
    id VARCHAR PRIMARY KEY,
    name VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    country_id VARCHAR references country(id),
    max_no_of_users int,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    tenant_id VARCHAR
  );

  CREATE TABLE operatingunit(
    id VARCHAR PRIMARY KEY,
    name VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    country_id VARCHAR references country(id),
    account_id VARCHAR references account(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    tenant_id VARCHAR
  );

  CREATE TABLE county (
    id VARCHAR PRIMARY KEY,
    name VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    country_id VARCHAR references country(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    display_order int
  );

  CREATE TABLE subcounty (
    id VARCHAR PRIMARY KEY,
    name VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    country_id VARCHAR references country(id),
    county_id VARCHAR references county(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    display_order int
  );

  CREATE TABLE culture (
    id VARCHAR PRIMARY KEY,
    name VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    code VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    display_order int
  );

  CREATE TABLE site(
    id VARCHAR PRIMARY KEY,
    name VARCHAR,
    sub_county_id VARCHAR references subcounty(id),
    county_id VARCHAR references county(id),
    culture_id VARCHAR references culture(id),
    phone_number VARCHAR,
    postal_code VARCHAR,
    site_type VARCHAR,
    site_level VARCHAR,
    address_1 VARCHAR,
    address_type VARCHAR,
    latitude VARCHAR,
    longitude VARCHAR,
    address_use VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    country_id VARCHAR references country(id),
    account_id VARCHAR references account(id),
    operating_unit_id VARCHAR references operatingunit(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    tenant_id VARCHAR
  );

  CREATE TABLE program (
    id VARCHAR PRIMARY KEY,
    country_id VARCHAR references country(id),
    account_id VARCHAR references account(id),
    operating_unit_id VARCHAR references operatingunit(id),
    site_id VARCHAR references site(id),
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    name VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    tenant_id VARCHAR
  );

  CREATE TABLE state (
    id bigint PRIMARY KEY,
    name VARCHAR,
    is_active BOOLEAN,
    country_id VARCHAR references country(id)
  );

  CREATE TABLE district (
    id bigint PRIMARY KEY,
    name VARCHAR,
    is_active BOOLEAN,
    state_id bigint references state(id)
  );

  CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    to_email VARCHAR,
    cc_email VARCHAR,
    subject VARCHAR,
    body VARCHAR,
    status VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN
  );

  CREATE TABLE organizations(
    id VARCHAR PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR,
    first_name VARCHAR,
    last_name VARCHAR,
    middle_name VARCHAR,
    contact_address VARCHAR,
    state_id bigint references state(id),
    district_id bigint references district(id),
    zip_code VARCHAR,
    contact_person VARCHAR,
    contact_number VARCHAR,
    phonenumber VARCHAR,
    email_id VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN
  );

  CREATE TABLE role(
    id SERIAL PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR,
    description VARCHAR,
    is_active BOOLEAN,
    is_deleted BOOLEAN
  );
INSERT INTO
  role (
  	id,
    created_by,
    updated_by,
    name,
    description,
    is_active,
    is_deleted
  )
VALUES(1, 1, 1, 'SUPER_USER', 'superuser', true, false);

CREATE TABLE email_template (
    id SERIAL PRIMARY KEY,
    type VARCHAR,
    vm_content VARCHAR,
    body VARCHAR,
    title VARCHAR,
    app_url VARCHAR
  );

  CREATE TABLE email_template_value(
    id SERIAL PRIMARY KEY,
    name VARCHAR,
    email_template_id bigint references email_template(id)
  );

  CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    created_by bigint NOT NULL,
    gender VARCHAR,
    updated_by bigint,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    username VARCHAR UNIQUE,
    first_name VARCHAR,
    last_name VARCHAR,
    middle_name VARCHAR,
    password VARCHAR,
    phonenumber VARCHAR,
    auth_token VARCHAR,
    refresh_token VARCHAR,
    forget_password_token VARCHAR,
    tenant_id bigint,
    forget_password_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    forget_password_count int,
    is_blocked BOOLEAN,
    blocked_date TIMESTAMP,
    is_active BOOLEAN,
    is_deleted BOOLEAN,
    region VARCHAR references country(id),
    invalid_login_attempts int,
    invalid_login_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_password_reset_enabled BOOLEAN,
    password_reset_attempts int,
    invalid_reset_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    timezone_id VARCHAR references timezone(id),
    filters VARCHAR
  );
INSERT INTO
  "user" (
  	id,
    created_by,
    updated_by,
    username,
    first_name,
    last_name,
    middle_name,
    "password",
    phonenumber,
    auth_token,
    refresh_token,
    forget_password_token,
    tenant_id,
    forget_password_count,
    is_blocked,
    is_active,
    invalid_login_attempts,
    is_password_reset_enabled,
    password_reset_attempts,
    is_deleted,
    gender,
    region,
    timezone_id
  )
VALUES
  (
  	 1,
    '1',
    '1',
    'superuser@gmail.com',
    'test',
    'user',
    'm',
    '\xc30d040703021e2a37bef1a7243f61d2b101cd17bf7f7db36e4e9270c0a587d01272e49b2c54eafd4e86fc00e048d3c93e763808ab2599ffd3d5a914130bbe28132852951b58e1c3c389523b46821eb4cd8d5cc4809844a3c008c020858e9994407d70933745ea9c083030e2f6ac1386ea05549c64c75ebceee058fa68f49fd189e9b99e4aaca19dd50f8290a1b4048300119745c58f99be07138a527f4dad7ea9b74a3bad9c65d611799e4e371f04173cf4f87ed9a3bbc46722708e7815b78305d5',
    '98764321',
    '1',
    '1',
    '1',
    1,
    0,
    false,
    true,
    0,
    true,
    0,
    false,
    'Male',
    '6260176ad4800c0021822cdd',
    '1'
  );

  CREATE TABLE user_role(
    user_id bigint references "user"(id),
    role_id bigint references role(id)
  );
INSERT INTO
  user_role (user_id, role_id)
VALUES(1, 1);


CREATE TABLE organization_role(
    user_id bigint references "user"(id),
    organization_id VARCHAR references organizations(id)
  );

  CREATE TABLE audit(
    id SERIAL PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    entity VARCHAR,
    action VARCHAR,
    data_id bigint,
    column_name VARCHAR,
    old_value VARCHAR,
    new_value VARCHAR
);
INSERT INTO "user" (id, created_by, updated_by, username, first_name, last_name, middle_name, password, phonenumber, auth_token, refresh_token, forget_password_token, tenant_id,forget_password_count, is_blocked, is_active, invalid_login_attempts, is_password_reset_enabled, password_reset_attempts, is_deleted, region, timezone_id) values
	(2, 1, 1, 'teleadmin@gmail.com', 'test', 'user', 'm', '\xc30d040703021e2a37bef1a7243f61d2b101cd17bf7f7db36e4e9270c0a587d01272e49b2c54eafd4e86fc00e048d3c93e763808ab2599ffd3d5a914130bbe28132852951b58e1c3c389523b46821eb4cd8d5cc4809844a3c008c020858e9994407d70933745ea9c083030e2f6ac1386ea05549c64c75ebceee058fa68f49fd189e9b99e4aaca19dd50f8290a1b4048300119745c58f99be07138a527f4dad7ea9b74a3bad9c65d611799e4e371f04173cf4f87ed9a3bbc46722708e7815b78305d5', '98764321', '1', '1', '1', 1,0, false, true, 0, true, 0, false, '6260176ad4800c0021822cdd', '1'),
	(3, 1, 1, 'teleuser@gmail.com', 'test', 'user', 'm', '\xc30d040703021e2a37bef1a7243f61d2b101cd17bf7f7db36e4e9270c0a587d01272e49b2c54eafd4e86fc00e048d3c93e763808ab2599ffd3d5a914130bbe28132852951b58e1c3c389523b46821eb4cd8d5cc4809844a3c008c020858e9994407d70933745ea9c083030e2f6ac1386ea05549c64c75ebceee058fa68f49fd189e9b99e4aaca19dd50f8290a1b4048300119745c58f99be07138a527f4dad7ea9b74a3bad9c65d611799e4e371f04173cf4f87ed9a3bbc46722708e7815b78305d5', '98764321', '1', '1', '1', 1,0, false, true, 0, true, 0, false, '6260176ad4800c0021822cdd', '1');

INSERT INTO public."role" (id, created_by,updated_by,created_at,updated_at,"name",description,is_active,is_deleted) VALUES
	(2, '1','1','2022-08-30 16:34:27.578804+05:30','2022-08-30 16:34:27.578804+05:30','TELE_ADMIN','teleadmin',true,false),
	(3, '1','1','2022-08-30 16:34:27.578804+05:30','2022-09-13 15:23:53.323+05:30','TELE_USER','teleuser',true,false);

INSERT INTO user_role (user_id, role_id) VALUES
	(2, 2),
	(3,3);

CREATE EXTENSION IF NOT EXISTS pgcrypto;