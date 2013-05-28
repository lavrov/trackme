# --- Notification area

# --- !Ups

create table NotificationArea (
  id                              varchar(255) not null primary key,
  name                            varchar(255) not null,
  interestedUser                  varchar(255) not null,
  trackedObject                   varchar(255) not null,
  lastAppearance                  timestamp,
  leftTopLongitude                decimal(9,6) not null,
  leftTopLatitude                 decimal(9,6) not null,
  rightBottomLongitude            decimal(9,6) not null,
  rightBottomLatitude             decimal(9,6) not null
);

# --- !Downs

drop table if exists NotificationArea;

