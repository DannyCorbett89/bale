drop table if exists mount_identifier;
create table mount_identifier
(
    id   int not null auto_increment,
    name varchar(191),
    primary key (id),
    unique (name)
);

drop table if exists trial;
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

drop table if exists mount;
create table mount
(
    id       int  not null auto_increment,
    name     varchar(191),
    tracking bool not null,
    primary key (id),
    unique (name)
);

drop table if exists fc_rank;
create table fc_rank
(
    id      int          not null auto_increment,
    name    varchar(191) not null,
    icon    varchar(191) not null,
    enabled bool         not null,
    primary key (id)
);

drop table if exists player;
create table player
(
    id      int          not null auto_increment,
    name    varchar(191) not null,
    visible bool         not null,
    url     varchar(191) not null,
    rank_id int,
    primary key (id),
    foreign key (rank_id) references fc_rank (id),
    unique (name)
);

drop table if exists mount_link;
create table mount_link
(
    id        int not null auto_increment,
    mount_id  int not null default 0,
    player_id int not null default 0,
    trial_id  int not null default 0,
    primary key (id)
);

drop table if exists minion;
create table minion
(
    id           int not null auto_increment,
    name         varchar(191),
    lodestone_id varchar(191),
    primary key (id)
);

drop table if exists minion_link;
create table minion_link
(
    id        int not null auto_increment,
    minion_id int not null default 0,
    player_id int not null default 0,
    primary key (id)
);

drop table if exists config;
create table config
(
    id    int not null auto_increment,
    name  varchar(191),
    value varchar(191),
    primary key (id)
);