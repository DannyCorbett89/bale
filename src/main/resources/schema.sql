drop table if exists mount_identifier;
create table mount_identifier (
  id int not null auto_increment,
  name varchar(191),
  primary key (id),
  unique (name)
);

drop table if exists trial;
create table trial (
  id int not null auto_increment,
  name varchar(191),
  boss varchar(191),
  lodestone_id varchar(191),
  loaded bool not null,
  primary key (id),
  unique (name)
);

drop table if exists mount;
create table mount (
  id int not null auto_increment,
  name varchar(191),
  tracking bool not null,
  primary key (id),
  unique (name)
);

drop table if exists player;
create table player (
  id int not null auto_increment,
  name varchar(191) not null,
  tracking bool not null,
  url varchar(191) not null,
  primary key (id),
  unique (name)
);

drop table if exists mount_link;
create table mount_link (
  id int not null auto_increment,
  mount_id int not null default 0,
  player_id int not null default 0,
  trial_id int not null default 0,
  primary key (id)
);

drop table if exists config;
create table config (
  id int not null auto_increment,
  name varchar(191),
  value varchar(191),
  primary key (id)
);