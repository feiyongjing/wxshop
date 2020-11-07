create table GOODS
(
    ID         bigint primary key AUTO_INCREMENT,
    SHOP_ID    bigint,
    NAME       varchar(100),
    DESCRIPTION varchar (1024),
    DETAILS    text,
    IMG_URL    varchar(1024),
    PRICE      BIGINT,    -- 单位 分
    STOCK      int   NOT NULL DEFAULT 0,
    STATUS     varchar (16), -- 'ok' 正常 'deleted' 已经删除
    CREATED_AT TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED_AT TIMESTAMP NOT NULL DEFAULT NOW()
) DEFAULT CHARSET = utf8mb4;
INSERT INTO GOODS(SHOP_ID, NAME, DESCRIPTION, DETAILS, IMG_URL, PRICE, STOCK, STATUS)
VALUES (1, 'goods1', 'desc1', 'details1', 'url1', 100, 5, 'ok');
INSERT INTO GOODS(SHOP_ID, NAME, DESCRIPTION, DETAILS, IMG_URL, PRICE, STOCK, STATUS)
VALUES (1, 'goods2', 'desc2', 'details2', 'url2', 100, 5, 'ok');
INSERT INTO GOODS(SHOP_ID, NAME, DESCRIPTION, DETAILS, IMG_URL, PRICE, STOCK, STATUS)
VALUES (2, 'goods3', 'desc2', 'details3', 'url3', 100, 5, 'ok');
INSERT INTO GOODS(SHOP_ID, NAME, DESCRIPTION, DETAILS, IMG_URL, PRICE, STOCK, STATUS)
VALUES (2, 'goods4', 'desc2', 'details4', 'url4', 100, 5, 'ok');
INSERT INTO GOODS(SHOP_ID, NAME, DESCRIPTION, DETAILS, IMG_URL, PRICE, STOCK, STATUS)
VALUES (2, 'goods5', 'desc2', 'details5', 'url5', 200, 5, 'ok');