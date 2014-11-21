
create table sessions (
  idsession serial,
  username varchar(20) references users(username),
  primary key(idsession)
);

update schema_info set version = 1;

