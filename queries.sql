---
--- Get username, rolename, privilige name for user 4
---
SELECT
    u.username,
    r.rolename,
    p.name
FROM
    users u
        LEFT JOIN
    users_roles ur ON u.id = ur.userid
        LEFT JOIN
    roles r ON ur.roleid = r.id
        LEFT JOIN
    roles_privileges rp ON r.id = rp.roleid
        LEFT JOIN
    privilege p ON rp.privilegeid=p.id where u.id=4;

---
--- Get username, rolename, privilige name for all users
---
SELECT
    u.username,
    r.rolename,
    p.name
FROM
    users u
        LEFT JOIN
    users_roles ur ON u.id = ur.userid
        LEFT JOIN
    roles r ON ur.roleid = r.id
        LEFT JOIN
    roles_privileges rp ON r.id = rp.roleid
        LEFT JOIN
    privilege p ON rp.privilegeid=p.id
ORDER BY u.username, r.rolename, p.name;
username |   rolename    |      name
----------+---------------+-----------------
 admin    | ADMIN         | ALL
 bvpelt   | FUNC_OPERATOR | APP_WRITE
 bvpelt   | TECH_OPERATOR | APP_MAINTENANCE
 develop  | DEVELOPER     | ALL
 fber     | FUNC_OPERATOR | APP_WRITE
 user     | USER          | APP_READ


---
--- Get user.*, role.*, privilige.* for user 4
---
SELECT
    u.*,
    r.*,
    p.*
FROM
    users u
        LEFT JOIN
    users_roles ur ON u.id = ur.userid
        LEFT JOIN
    roles r ON ur.roleid = r.id
        LEFT JOIN
    roles_privileges rp ON r.id = rp.roleid
        LEFT JOIN
    privilege p ON rp.privilegeid=p.id where u.id=4;
