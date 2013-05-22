# --- First database schema

# --- !Ups

create table Position (
  id                            varchar(255) not null primary key,
  longitude                     decimal(9,6) not null,
  latitude                      decimal(9,6) not null,
  timestamp                     timestamp not null,
  userId                        varchar(255) not null
);

# --- !Downs

drop table if exists Position;

