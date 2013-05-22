# --- Session

# --- !Ups

create table Session (
  id                            varchar(255) not null primary key,
  email                         varchar(255) not null
);

# --- !Downs

drop table if exists Session;

