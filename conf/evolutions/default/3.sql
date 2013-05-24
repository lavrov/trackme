# --- Tracking permission

# --- !Ups

create table TrackingPermission (
  id                                 varchar(255) default RANDOM_UUID() not null,
  subject                            varchar(255) not null,
  object                             varchar(255) not null,
  constraint TrackingPermission_PK   unique (subject, object)
);

# --- !Downs

drop table if exists TrackingPermission;

