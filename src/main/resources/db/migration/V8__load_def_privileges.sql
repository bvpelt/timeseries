-- privileges
insert into privilege ( name, hash)
values ( 'ALL', 64897);
insert into privilege ( name, hash)
values ( 'APP_MAINTENANCE', 1562765749);
insert into privilege ( name, hash)
values ( 'APP_WRITE', 1253932033);
insert into privilege ( name, hash)
values ( 'APP_READ', 1979950356);
insert into privilege ( name, hash)
values ( 'APP_READ_JWT', -756069636);

-- SELECT setval('privilege_id_seq', 5, true); -- next value will be 6