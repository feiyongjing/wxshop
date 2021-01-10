create table SHOP
(
    ID            bigint primary key AUTO_INCREMENT,
    NAME          varchar(100),
    DESCRIPTION   varchar(1024),
    IMG_URL       varchar(1024),
    OWNER_USER_ID bigint,
    STATUS        varchar(16),
    CREATED_AT    TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED_AT    TIMESTAMP NOT NULL DEFAULT NOW()
) DEFAULT CHARSET = utf8mb4;
INSERT INTO SHOP (NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS)
VALUES ('shop1', 'desc1', 'url1', 1, 'ok');
INSERT INTO SHOP (NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS)
VALUES ('shop2', 'desc2', 'url2', 1, 'ok');