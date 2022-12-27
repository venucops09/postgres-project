CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE organization (
  id SERIAL PRIMARY KEY,
  form_data_id BIGINT,
  form_name VARCHAR,
  name VARCHAR,
  sequence BIGINT,
  parent_organization_id BIGINT, FOREIGN KEY (parent_organization_id) REFERENCES organization(id),
  tenant_id BIGINT, FOREIGN KEY (parent_organization_id) REFERENCES organization(id),
  created_by BIGINT NOT NULL,
  updated_by BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);

CREATE TABLE country (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  country_code VARCHAR NOT NULL,
  tenant_id BIGINT,
  created_by BIGINT NOT NULL, 
  updated_by BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);

insert into country(created_by,updated_by, name, country_code) values(1,1,'India', '91');

CREATE TABLE timezone (
  id SERIAL PRIMARY KEY,
  description VARCHAR,
  abbreviation VARCHAR,
  "offset" VARCHAR,
  created_by BIGINT NOT NULL,
  updated_by BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);


CREATE TABLE "user" (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR,
  last_name VARCHAR,
  middle_name VARCHAR,
  gender VARCHAR,
  phone_number VARCHAR,
  address VARCHAR,
  username VARCHAR,
  password VARCHAR,
  country_code VARCHAR,
  is_blocked BOOLEAN,
  blocked_date timestamp,
  forget_password_token VARCHAR,
  forget_password_time TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  forget_password_count INT,
  invalid_login_attempts INT,
  invalid_login_time timestamptz DEFAULT (CURRENT_TIMESTAMP),
  invalid_reset_time timestamptz DEFAULT (CURRENT_TIMESTAMP),
  is_password_reset_enabled BOOLEAN,
  password_reset_attempts INT,
  is_license_acceptance BOOLEAN,
  last_logged_in TIMESTAMP,
  last_logged_out TIMESTAMP,
  country_id BIGINT, FOREIGN KEY (country_id) REFERENCES country(id),
  timezone_id BIGINT, FOREIGN KEY (timezone_id) REFERENCES timezone(id),
  tenant_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  updated_by BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);


CREATE TABLE user_token (
  id SERIAL PRIMARY KEY,
  auth_token VARCHAR,
  refresh_token VARCHAR,
  user_id BIGINT, FOREIGN KEY (user_id) REFERENCES "user"(id),
  created_by BIGINT NOT NULL,
  updated_by BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);

CREATE TABLE role (
  id SERIAL PRIMARY KEY,
  name VARCHAR,
  level INT,
  created_by BIGINT,
  updated_by BIGINT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);

--INSERT INTO role (created_by, updated_by, name, is_active)
--VALUES(1, 1, 'SUPER_USER', true);

CREATE TABLE user_role (
  user_id BIGINT, FOREIGN KEY (user_id) REFERENCES "user"(id),
  role_id BIGINT, FOREIGN KEY (role_id) REFERENCES role(id)
);

--INSERT INTO user_role (user_id, role_id)
--VALUES(1, 1);

CREATE TABLE user_organization (
  user_id BIGINT, FOREIGN KEY (user_id) REFERENCES "user"(id),
  organization_id BIGINT, FOREIGN KEY (organization_id) REFERENCES organization(id)
);


CREATE TABLE county (
  id SERIAL PRIMARY KEY,
  name VARCHAR,
  display_order INT,
  country_id BIGINT, FOREIGN KEY (country_id) REFERENCES country(id),
  created_by BIGINT NOT NULL,
  updated_by BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);

CREATE TABLE sub_county (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  display_order INT,
  country_id BIGINT, FOREIGN KEY (country_id) REFERENCES country(id),
  county_id BIGINT, FOREIGN KEY (county_id) REFERENCES county(id),
  created_by BIGINT NOT NULL,
  updated_by BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  is_active BOOLEAN DEFAULT true,
  is_deleted BOOLEAN DEFAULT false
);


CREATE TABLE outbound_email(
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_id bigint, FOREIGN KEY (tenant_id) REFERENCES organization(id),
    is_processed BOOLEAN,
    "to" VARCHAR,
    retry_attempts int,
    form_data_id bigint,
    form_name VARCHAR,
    type VARCHAR,
    body VARCHAR,
    subject VARCHAR,
    cc VARCHAR,
    bcc VARCHAR,
    is_active BOOLEAN DEFAULT true,
    is_deleted BOOLEAN DEFAULT false
);



  CREATE TABLE audit(
    id SERIAL PRIMARY KEY,
    created_by VARCHAR NOT NULL,
    updated_by VARCHAR,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    entity VARCHAR,
    action VARCHAR,
    entity_id bigint,
    column_name VARCHAR,
    old_value VARCHAR,
    new_value VARCHAR
);

  CREATE TABLE api_role_permission (
    id SERIAL PRIMARY KEY,
    method VARCHAR,
    api VARCHAR,
    roles VARCHAR
);


CREATE TABLE outbound_sms(
	id SERIAL PRIMARY KEY, 
	username VARCHAR,
	form_data_id BIGINT,
	retry_attempts INT DEFAULT 0,
	phone_number VARCHAR,
	body VARCHAR,
	is_processed BOOLEAN DEFAULT false,
	notification_id BIGINT,
	tenant_id BIGINT,
	created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sms_template(
	id SERIAL PRIMARY KEY,
	body VARCHAR,
	type VARCHAR,
	created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP 
);

CREATE TABLE sms_template_values(
	id SERIAL PRIMARY KEY,
	key VARCHAR,
	template_id int, FOREIGN KEY (template_id) REFERENCES sms_template(id),
	created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO country ("name",country_code,tenant_id,created_by,updated_by,created_at,updated_at,is_active,is_deleted) VALUES
	 ('India','91',1,1,1,'2022-12-08 19:10:06.739','2022-12-08 19:10:06.739',true,false);

INSERT INTO timezone (description,abbreviation,"offset",created_by,updated_by,created_at,updated_at,is_active,is_deleted) VALUES
	 ('(UTC+05:30) Chennai, Kolkata, Mumbai, New Delhi','IST','UTC+05:30',1,1,'2022-12-08 19:11:09.138','2022-12-08 19:11:09.138',true,false); 
	 
INSERT INTO "user" (first_name,last_name,middle_name,gender,phone_number,address,username,"password",country_code,is_blocked,blocked_date,forget_password_token,forget_password_time,forget_password_count,invalid_login_attempts,invalid_login_time,invalid_reset_time,is_password_reset_enabled,password_reset_attempts,is_license_acceptance,last_logged_in,last_logged_out,country_id,timezone_id,created_by,updated_by,created_at,updated_at,is_active,is_deleted,tenant_id) VALUES
	 ('test','user','m','Male','98764321',NULL,'rajkumar@ideas2it.com','\xc30d040703021e2a37bef1a7243f61d2b101cd17bf7f7db36e4e9270c0a587d01272e49b2c54eafd4e86fc00e048d3c93e763808ab2599ffd3d5a914130bbe28132852951b58e1c3c389523b46821eb4cd8d5cc4809844a3c008c020858e9994407d70933745ea9c083030e2f6ac1386ea05549c64c75ebceee058fa68f49fd189e9b99e4aaca19dd50f8290a1b4048300119745c58f99be07138a527f4dad7ea9b74a3bad9c65d611799e4e371f04173cf4f87ed9a3bbc46722708e7815b78305d5',NULL,false,NULL,'1','2022-12-08 14:38:17.115',0,0,'2022-12-08 14:38:17.115','2022-12-08 14:38:17.115',true,0,false,NULL,NULL,1,1,1,1,'2022-12-08 14:38:17.115','2022-12-08 14:38:17.115',true,false,0);

INSERT INTO "role" ("name","level",created_by,updated_by,created_at,updated_at,is_active,is_deleted) VALUES
	 ('SUPER_USER',0,1,1,'2022-12-08 19:11:12.160','2022-12-08 19:11:12.160',true,false);

INSERT INTO user_role (user_id,role_id) VALUES
	 (1,1);
	 
CREATE TABLE email_template (
  id SERIAL PRIMARY KEY,
  type VARCHAR,
  vm_content VARCHAR,
  body VARCHAR,
  title VARCHAR,
  app_url VARCHAR
);

INSERT INTO public.email_template (id,type,vm_content,body,title,app_url) VALUES
     (1,'Forgot_Password','vmContent','<p><img src="https://mdt-shruti.s3.ap-south-1.amazonaws.com/logo/spiceEngage.png" style=\"height:70%;width:10%;"></p><p style=\"font-size: 11pt\>Dear Telecounseling Application User,<br><br>    <p>We have received a request to reset your Telecounseling account password. It was initiated after you selected “Forgot Password” in the Telecounseling software application.    <br><br>    <strong>Please click on this 
<a href="{{app_url_email}}">LINK</a> to reset your password. The link will expire in 60 minutes.If it is expires before the reset is completed, 
click on Forgot Password in the application to reset it again.</strong>    <br><br>  
 If you did not initiate an account password reset process or have a Telecounseling software application 
account associated with this email address, it is possible that someone else might be trying to access your account.
If so, please notify Medtronic LABS Support Team at support@medtroniclabs.org.    <br><br>   
If you have any additional questions, please contact the Medtronic LABS Support Team at support@medtroniclabs.org.  
 <br>      <br>    Sincerely,    <br>    The Medtronic LABS Telecounseling Platform Support
Team<p><img src="https://mdt-shruti.s3.ap-south-1.amazonaws.com/logo/logo2.png" style="height:6%;width:25%;">','Email_notification','app_url_email'),
     (2,'User_Creation','vmContent','<p><img src="https://mdt-shruti.s3.ap-south-1.amazonaws.com/logo/spiceEngage.png" style=\"height:70%;width:10%;"></p><p style=\"font-size: 11pt\>Dear Telecounseling Application User,<br><br>    <p>Welcome! An account has been created for you in the Telecounseling software application.     <br><br>    <strong>Your account username is your email address {{email}}. To finish setting up your Telecounseling application account, please click on this <a href="{{app_url_email}}">LINK</a> to create your password. </strong>    <br><br>  Once you have set up your password you will be able to access the application. <br><br> If you have any additional questions, please contact the Medtronic LABS Support Team at support@medtroniclabs.org.  <br>      <br>    Sincerely,    <br>    The Medtronic LABS Telecounseling Platform SupportTeam<p><img src="https://mdt-shruti.s3.ap-south-1.amazonaws.com/logo/logo2.png" style="height:6%;width:25%;">','Email_notification','app_url_email');

INSERT INTO api_role_permission ("method",api,roles) VALUES
	 ('POST','/user','SUPER_USER'),
	 ('GET','/user/clear','SUPER_USER');
 
INSERT INTO sms_template (body,"type",created_at,updated_at) VALUES
	 ('A Red Alert Notification was received for one of your patients. Log into the SPICE application to review patient details.
','RED_RISK','2022-12-16 12:56:09.161','2022-12-16 12:56:09.161'),
	 ('Hi {{name}}, thank you for getting screened by Spice health worker from {{orgname}} site. Your registration code is {{patient_id}}.

','ENROLL_PATIENT','2022-12-16 12:56:09.161','2022-12-16 12:56:09.161');


INSERT INTO sms_template_values ("key",template_id,created_at,updated_at) VALUES
	 ('name',2,'2022-12-16 12:57:17.811','2022-12-16 12:57:17.811'),
	 ('orgname',2,'2022-12-16 12:57:17.815','2022-12-16 12:57:17.815'),
	 ('patient_id',2,'2022-12-16 12:57:17.816','2022-12-16 12:57:17.816');


