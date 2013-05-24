# --- Tracking state

# --- !Ups

create table TrackingState (
  email                              varchar(255) not null primary key,
-- user is currently tracking someone using this permission
  permissionId                       varchar(255) not null,
  constraint TrackingState_Permission_FK foreign key (permissionId) references TrackingPermission (id)
);

# --- !Downs

drop table if exists TrackingState;

