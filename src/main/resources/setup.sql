use bale;

drop table mount;
create table mount (
  id int not null auto_increment,
  name varchar(200),
  instance varchar(200),
  primary key (id)
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

drop table player;
create table player (
  id int not null auto_increment,
  name varchar(200),
  primary key (id)
);

insert into player (name) values ("Ussa Xellus");
insert into player (name) values ("Syth Rilletta");
insert into player (name) values ("Cordia Crius");
insert into player (name) values ("Alveille Tekada");
insert into player (name) values ("Scullai Ponga");
insert into player (name) values ("Lelouch Vi");
insert into player (name) values ("Jazrail Paraxoi");
insert into player (name) values ("Neos Darkbright");

drop table mount_link;
create table mount_link (
  id int not null auto_increment,
  player_id int,
  mount_id int,
  primary key (id)
);
