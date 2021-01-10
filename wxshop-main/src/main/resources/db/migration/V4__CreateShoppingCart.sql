create table SHOPPING_CART
(
    ID              bigint primary key AUTO_INCREMENT,
    USER_ID         bigint,
    GOODS_ID        bigint,
    NUMBER          int,
    STATUS          varchar(16),
    CREATED_AT      TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED_AT      TIMESTAMP NOT NULL DEFAULT NOW()
) DEFAULT CHARSET = utf8mb4;
