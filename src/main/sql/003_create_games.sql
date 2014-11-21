
create table games (
  idgame SERIAL,
  username varchar(20) references users(username),
  sequence varchar(4),
  started timestamp default CURRENT_TIMESTAMP,
  finished timestamp,
  status varchar(15) not null,
  tries decimal(2),
  primary key(idgame)
);

update schema_info set version = 1;
