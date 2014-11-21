
create table tries (
  idtry serial,
  try integer not null,
  idgame integer references games(idgame),
  result varchar(4),
  primary key(idtry)
);

update schema_info set version = 1;

