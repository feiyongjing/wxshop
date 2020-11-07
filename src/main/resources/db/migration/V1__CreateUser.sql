create table USER
(
    ID         bigint primary key AUTO_INCREMENT,
    NAME       varchar(10),
    TEL        varchar(15) unique,
    AVATAR_URL varchar(1024),
    ADDRESS    VARCHAR(1024),
    CREATED_AT timestamp,
    UPDATED_AT timestamp
) DEFAULT CHARSET = utf8mb4;
INSERT INTO USER(NAME, TEL, AVATAR_URL, ADDRESS)
VALUES ('user1', '13800000000', 'http://url', '火星')