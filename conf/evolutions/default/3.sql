# --- Tracking permission

# --- !Ups

create table TrackingPermission (
  subject                            varchar(255) not null,
  object                             varchar(255) not null,
  constraint TrackingPermission_PK   primary key (subject, object)
);

# --- !Downs

drop table if exists TrackingPermission;

