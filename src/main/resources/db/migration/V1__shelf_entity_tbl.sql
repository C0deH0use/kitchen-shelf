-- Migration script for creating OrderEntity table with dedicated sequence
CREATE SEQUENCE shelf_id_seq START WITH 1000;

CREATE TABLE "shelf"
(
    id             INT PRIMARY KEY DEFAULT NEXTVAL('shelf_id_seq'),
    menu_item_name TEXT      NOT NULL,
    menu_item_id   INT       NOT NULL,
    quantity       INT       NOT NULL,
    version        BIGINT    NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);