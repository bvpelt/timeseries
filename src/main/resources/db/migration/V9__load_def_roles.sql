-- roles
insert into roles ( rolename, description, hash)
values ( 'ADMIN', 'Administrator', 2072793375);
insert into roles ( rolename, description, hash)
values ( 'FUNC_OPERATOR', 'Functional Operator', -281572255);
insert into roles ( rolename, description, hash)
values ( 'TECH_OPERATOR', 'Technical Operator', 439955957);
insert into roles ( rolename, description, hash)
values ( 'USER', 'Application user with read access', -1051810471);
insert into roles ( rolename, description, hash)
values ( 'JWT-TOKEN', 'Registered user with read access', -1051810471);
insert into roles ( rolename, description, hash)
values ( 'DEVELOPER', 'Developer with all privileges', -1051810471);


-- Connect to roles
insert into roles_privileges ( roleid, privilegeid)
values ( 1, 1); -- ADMIN ALL
insert into roles_privileges ( roleid, privilegeid)
values ( 2, 3); -- FUNC_OPERATOR APP_WRITE
insert into roles_privileges ( roleid, privilegeid)
values ( 3, 2); -- TECH_OPERATOR APP_MAINTENANCE
insert into roles_privileges ( roleid, privilegeid)
values ( 4, 4); -- USER APP_READ
insert into roles_privileges ( roleid, privilegeid)
values ( 5, 5); -- JWT-TOKEN APP_READ_JWT
insert into roles_privileges ( roleid, privilegeid)
values ( 6, 1); -- DEVELOPER ALL

-- SELECT setval('roles_id_seq', 6, true); -- next value will be 7

-- SELECT setval('roles_privileges_id_seq', 6, true); -- next value will be 6