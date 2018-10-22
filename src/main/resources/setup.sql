use bale;

drop table if exists mount_identifier;
create table mount_identifier (
  id int not null auto_increment,
  name varchar(200),
  primary key (id),
  unique (name)
);

insert into mount_identifier (name) values ('Whistle');
insert into mount_identifier (name) values ('Fife');

drop table if exists trial;
create table trial (
  id int not null auto_increment,
  name varchar(200),
  boss varchar(200),
  lodestone_id varchar(200),
  loaded bool not null,
  primary key (id),
  unique (name)
);

drop table if exists mount;
create table mount (
  id int not null auto_increment,
  name varchar(200),
  tracking bool not null,
  primary key (id),
  unique (name)
);

insert into mount (name, instance) values ('Aithon', 'Ifrit');
insert into mount (name, instance) values ('Xanthos', 'Garuda');
insert into mount (name, instance) values ('Gullfaxi', 'Titan');
insert into mount (name, instance) values ('Enbarr', 'Leviathan');
insert into mount (name, instance) values ('Markab', 'Ramuh');
insert into mount (name, instance) values ('Boreas', 'Shiva');
insert into mount (name, instance) values ('Kirin', 'Kirin');
# insert into mount (name, instance) values ('Nightmare', 'Nightmare - Garuda/Titan/Ifrit');
insert into mount (name, instance) values ('White Lanner', 'Bismark');
insert into mount (name, instance) values ('Rose Lanner', 'Ravana');
insert into mount (name, instance) values ('Round Lanner', 'Thordan');
insert into mount (name, instance) values ('Warring Lanner', 'Sephirot');
insert into mount (name, instance) values ('Dark Lanner', 'Nidhogg');
insert into mount (name, instance) values ('Demonic Lanner', 'Zurvan');
insert into mount (name, instance) values ('Sophic Lanner', 'Sophia');
insert into mount (name, instance) values ('Firebird', 'Firebird');
# insert into mount (name, instance) values ('Gobwalker', 'Gobwalker - A4S');
# insert into mount (name, instance) values ('Arrhidaeus', 'Arrhidaeus - A12S');
insert into mount (name, instance) values ('Reveling Kamuy', 'Susano');
insert into mount (name, instance) values ('Blissful Kamuy', 'Lakshmi');
insert into mount (name, instance) values ('Legendary Kamuy', 'Shinryu');
insert into mount (name, instance) values ('Auspicious Kamuy', 'Byakko');
insert into mount (name, instance) values ('Lunar Kamuy', 'Tsukuyomi');
# insert into mount (name, instance) values ('Alte Roite', 'Alte Roite - O4S');
# insert into mount (name, instance) values ('Ixion', 'Ixion');
# insert into mount (name, instance) values ('Magitek Predator', 'Magitek Predator - Ala Mhigo');

drop table if exists player;
create table player (
  id int not null auto_increment,
  name varchar(200) not null,
  tracking bool not null,
  url varchar(200) not null,
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

drop table if exists music_key_mapping;
create table music_key_mapping (
  id int not null auto_increment,
  name varchar(200) not null,
  c varchar(5) not null,
  c_sharp varchar(5) not null,
  d varchar(5) not null,
  e_flat varchar(5) not null,
  e varchar(5) not null,
  f varchar(5) not null,
  f_sharp varchar(5) not null,
  g varchar(5) not null,
  g_sharp varchar(5) not null,
  a varchar(5) not null,
  b_flat varchar(5) not null,
  b varchar(5) not null,
  octave_up varchar(5) not null,
  octave_down varchar(5) not null,
  primary key (id)
);

insert into music_key_mapping (name, c, c_sharp, d, e_flat, e, f, f_sharp, g, g_sharp, a, b_flat, b, octave_up, octave_down)
values ('Default Controller', 'L', 'R1+L', 'U', 'R1+U', 'R', 'D', 'R1+D', 'SQ',
                              'R1+SQ', 'TR', 'R1+TR', 'O', 'R2', 'L2');

insert into music_key_mapping (name, c, c_sharp, d, e_flat, e, f, f_sharp, g, g_sharp, a, b_flat, b, octave_up, octave_down)
values ('Default Keyboard', 'Q', '2', 'W', '3', 'E', 'R', '5', 'T',
                              '6', 'Y', '7', 'U', 'SHIFT', 'CTRL');

drop table if exists config;
create table config (
  id int not null auto_increment,
  name varchar(200),
  value varchar(200),
  primary key (id)
);

insert into config (name, value) values ('freeCompanyUrl', '/lodestone/freecompany/9229283011365743624/member/');