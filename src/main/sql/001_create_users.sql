
create table users (
  username varchar(20) not null,
  email varchar(50) not null,
  password varchar(255),
  primary key(username)
);

update schema_info set version = 1;

