drop table if exists trial;
drop table if exists mount;
drop table if exists player;
drop table if exists fc_rank;
drop table if exists mount_link;
drop table if exists minion;
drop table if exists minion_link;
drop table if exists mount_item;
drop table if exists config;

create table trial
(
    id           int           not null auto_increment,
    name         varchar(191),
    boss         varchar(191),
    lodestone_id varchar(191),
    item_level   int default 0 not null,
    loaded       bool          not null,
    primary key (id),
    unique (name)
);

create table mount
(
    id      int  not null auto_increment,
    name    varchar(191),
    visible bool not null,
    hash    varchar(191),
    primary key (id),
    unique (name)
);

create table fc_rank
(
    id      int          not null auto_increment,
    name    varchar(191) not null,
    icon    varchar(191) not null,
    enabled bool         not null,
    primary key (id)
);

create table player
(
    id      int          not null auto_increment,
    name    varchar(191) not null,
    visible bool         not null,
    url     varchar(191) not null,
    rank_id int,
    icon    varchar(191) not null,
    primary key (id),
    foreign key (rank_id) references fc_rank (id),
    unique (name)
);

create table mount_link
(
    id        int not null auto_increment,
    mount_id  int not null default 0,
    player_id int not null default 0,
    trial_id  int not null default 0,
    primary key (id)
);

create table minion
(
    id           int not null auto_increment,
    name         varchar(191),
    lodestone_id varchar(191),
    hash         varchar(191),
    primary key (id)
);

create table minion_link
(
    id        int not null auto_increment,
    minion_id int not null default 0,
    player_id int not null default 0,
    primary key (id)
);

create table mount_item
(
    id         int not null auto_increment,
    item_name  varchar(191),
    mount_name varchar(191),
    primary key (id),
    unique (item_name)
);

create table config
(
    id    int not null auto_increment,
    name  varchar(191),
    value varchar(250),
    primary key (id),
    unique (name)
);