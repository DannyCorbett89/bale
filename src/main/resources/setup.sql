use bale;

drop table if exists mount;
create table mount (
  id int not null auto_increment,
  name varchar(200),
  instance varchar(200),
  primary key (id),
  unique (name)
);

insert into mount (name, instance) values ("Aithon", "Ifrit");
insert into mount (name, instance) values ("Xanthos", "Garuda");
insert into mount (name, instance) values ("Gullfaxi", "Titan");
insert into mount (name, instance) values ("Enbarr", "Leviathan");
insert into mount (name, instance) values ("Markab", "Ramuh");
insert into mount (name, instance) values ("Boreas", "Shiva");
insert into mount (name, instance) values ("Kirin", "Kirin");
# insert into mount (name, instance) values ("Nightmare", "Nightmare - Garuda/Titan/Ifrit");
insert into mount (name, instance) values ("White Lanner", "Bismark");
insert into mount (name, instance) values ("Rose Lanner", "Ravana");
insert into mount (name, instance) values ("Round Lanner", "Thordan");
insert into mount (name, instance) values ("Warring Lanner", "Sephirot");
insert into mount (name, instance) values ("Dark Lanner", "Nidhogg");
insert into mount (name, instance) values ("Demonic Lanner", "Zurvan");
insert into mount (name, instance) values ("Sophic Lanner", "Sophia");
insert into mount (name, instance) values ("Firebird", "Firebird");
# insert into mount (name, instance) values ("Gobwalker", "Gobwalker - A4S");
# insert into mount (name, instance) values ("Arrhidaeus", "Arrhidaeus - A12S");
insert into mount (name, instance) values ("Reveling Kamuy", "Susano");
insert into mount (name, instance) values ("Blissful Kamuy", "Lakshmi");
insert into mount (name, instance) values ("Legendary Kamuy", "Shinryu");
# insert into mount (name, instance) values ("Alte Roite", "Alte Roite - O4S");
# insert into mount (name, instance) values ("Ixion", "Ixion");
# insert into mount (name, instance) values ("Magitek Predator", "Magitek Predator - Ala Mhigo");

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
  player_id int,
  mount_id int,
  primary key (id)
);

drop table if exists config;
create table config (
  id int not null auto_increment,
  name varchar(200),
  value varchar(200),
  primary key (id)
);

insert into config (name, value) values ("freecompany_url", "/lodestone/freecompany/9229283011365743624/member/");