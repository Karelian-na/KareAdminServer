CREATE DATABASE kas;

USE kas;

CREATE USER IF NOT EXISTS `kas_user`@`%` IDENTIFIED WITH mysql_native_password BY '123456789';

GRANT ALL ON `kas`.* TO `kas_user`@`%`;

FLUSH PRIVILEGES;

-- 用户表
	DROP TABLE IF EXISTS users;
	CREATE TABLE users (
		id BIGINT(12) UNSIGNED PRIMARY KEY COMMENT 'ID',
		uid VARCHAR(16) UNIQUE NOT NULL COMMENT '用户名',
		pwd VARCHAR(64) NOT NULL DEFAULT '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92' COMMENT '密码', -- 123456
		last_login_ip VARCHAR(15) DEFAULT NULL COMMENT '最后登录IP',
		last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
		add_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
		bind_email VARCHAR(30) DEFAULT NULL COMMENT '绑定邮箱',
		bind_phone VARCHAR(11) DEFAULT NULL COMMENT '绑定手机',
		is_init BOOL NOT NULL DEFAULT FALSE COMMENT '是否初始化'
		, deleted BOOL NOT NULL DEFAULT FALSE COMMENT '是否删除'
		, delete_user VARCHAR(20) DEFAULT NULL COMMENT '删除人'
		, delete_time DATETIME DEFAULT NULL COMMENT '删除/注销时间'
		, delete_type TINYINT DEFAULT NULL COMMENT '删除方式' -- 0: 注销 1: 删除
	) COMMENT '管理用户的表';
	
	INSERT INTO users(id, uid) VALUES(999999, 'superadmin');

-- 用户信息表
	DROP TABLE IF EXISTS usermsgs;
	CREATE TABLE usermsgs (
		id BIGINT(12) UNSIGNED PRIMARY KEY COMMENT 'ID',
		name VARCHAR(20) NOT NULL COMMENT '姓名',
		gender TINYINT UNSIGNED DEFAULT NULL COMMENT '性别', -- 1-男; 2-女
		age TINYINT UNSIGNED DEFAULT NULL COMMENT '年龄',
		avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
		email VARCHAR(50) DEFAULT NULL COMMENT '电子邮箱',
		phone VARCHAR(11) DEFAULT NULL COMMENT '联系方式',
		political_status TINYINT UNSIGNED DEFAULT NULL COMMENT '政治面貌',
		clan TINYINT UNSIGNED DEFAULT NULL COMMENT '民族',
		profile VARCHAR(100) DEFAULT NULL COMMENT '个人简介',
		
		FOREIGN KEY(id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
	) COMMENT '管理用户基本信息的表';
	
-- 已删除/注销用户视图
	CREATE OR REPLACE VIEW deleted_usermsgs_view AS
		SELECT
			users.id
			, users.uid
			, usermsgs.name
			, usermsgs.avatar
			, users.bind_email
			, users.bind_phone
			, users.last_login_time
			, users.add_time
			, users.delete_user
			, users.delete_time
			, users.delete_type
			, usermsgs.email
			, usermsgs.phone
		FROM users
		LEFT JOIN usermsgs ON users.id = usermsgs.id
		WHERE users.deleted = 1
	;
	
	INSERT INTO usermsgs(id, name) VALUES(999999, '超级管理员');

-- 视图信息表
	DROP TABLE IF EXISTS views_info;
	CREATE TABLE views_info(
		id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
		view_name VARCHAR(30) UNIQUE COMMENT '视图名称',
		comment VARCHAR(100) DEFAULT NULL COMMENT '备注',
		fields_config VARCHAR(256) DEFAULT NULL COMMENT '字段配置',
		update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
		update_uid BIGINT(12) UNSIGNED COMMENT '更新人',
		
		FOREIGN KEY(update_uid) REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT
	) AUTO_INCREMENT = 100;

	INSERT INTO views_info(id, view_name, comment, fields_config) VALUES(1, "logs", "系统访问日志记录表", "fields/logs.js");
	INSERT INTO views_info(id, view_name, comment, fields_config) VALUES(2, "menus_view", "后台菜单表", "fields/menus_view.js");
	INSERT INTO views_info(id, view_name, comment, fields_config) VALUES(3, "permissions_view", "权限表", "fields/permissions_view.js");
	INSERT INTO views_info(id, view_name, comment, fields_config) VALUES(4, "roles", "角色表", "fields/roles.js");
	INSERT INTO views_info(id, view_name, comment, fields_config) VALUES(5, "usermsgs_view", "用户信息表", "fields/usermsgs_view.js");
	INSERT INTO views_info(id, view_name, comment, fields_config) VALUES(6, "deleted_usermsgs_view", "已删除/注销的用户信息表", "fields/deleted_usermsgs_view.js");
	INSERT INTO views_info(id, view_name, comment, fields_config) VALUES(7, "views", "管理表字段的表", "fields/views.js");

-- 视图信息视图
	CREATE OR REPLACE VIEW views AS
		SELECT 
			views_info.view_name,
			views_info.fields_config,
			views_info.`comment`,
			views_info.update_time,
			usermsgs.name AS update_user,
			NULL AS fields
		FROM views_info
		LEFT JOIN usermsgs ON views_info.update_uid = usermsgs.id
				;

-- 字段信息表
	DROP TABLE IF EXISTS table_fields_info;
	CREATE TABLE table_fields_info (
		id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
		table_name VARCHAR(50) NOT NULL COMMENT '表名或视图名',
		display_order TINYINT UNSIGNED DEFAULT 0 COMMENT '次序',
		display BOOL DEFAULT FALSE COMMENT '是否展示',
		searchable BOOL DEFAULT FALSE COMMENT '是否可检索',
		editable BOOL DEFAULT FALSE COMMENT '是否可编辑',
		editable_when_add BOOL DEFAULT NULL COMMENT '是否添加时可自定义',
		field_name VARCHAR(50) NOT NULL COMMENT '字段名',
		display_name VARCHAR(50) DEFAULT NULL COMMENT '展示名',
		UNIQUE(table_name, field_name)
	) AUTO_INCREMENT = 10000;

	INSERT INTO table_fields_info VALUES(1, 'views', 0, 1, 1, 0, NULL, 'view_name', '视图名称');
	INSERT INTO table_fields_info VALUES(2, 'views', 1, 1, 0, 1, NULL, 'fields_config', '字段配置');
	INSERT INTO table_fields_info VALUES(3, 'views', 2, 1, 0, 1, NULL, 'comment', '备注');
	INSERT INTO table_fields_info VALUES(4, 'views', 3, 1, 0, 0, NULL, 'update_time', '更新时间');
	INSERT INTO table_fields_info VALUES(5, 'views', 4, 1, 0, 0, NULL, 'update_user', '更新人');

	INSERT INTO table_fields_info VALUES(101, 'logs', 0, 1, 1, 0, NULL, 'id', NULL);
	INSERT INTO table_fields_info VALUES(102, 'logs', 1, 1, 0, 0, NULL, 'uid', NULL);
	INSERT INTO table_fields_info VALUES(103, 'logs', 2, 1, 0, 0, NULL, 'type', NULL);
	INSERT INTO table_fields_info VALUES(104, 'logs', 3, 1, 1, 0, NULL, 'title', NULL);
	INSERT INTO table_fields_info VALUES(105, 'logs', 4, 1, 1, 0, NULL, 'url', NULL);
	INSERT INTO table_fields_info VALUES(106, 'logs', 5, 1, 1, 0, NULL, 'ip', NULL);

	INSERT INTO table_fields_info VALUES(201, 'roles', 0, 1, 1, 1, 1, 'name', NULL);
	INSERT INTO table_fields_info VALUES(202, 'roles', 1, 1, 0, 1, 1, 'level', NULL);
	INSERT INTO table_fields_info VALUES(203, 'roles', 2, 1, 0, 0, 0, 'add_time', NULL);
	INSERT INTO table_fields_info VALUES(204, 'roles', 3, 1, 0, 0, 0, 'add_user', NULL);
	INSERT INTO table_fields_info VALUES(205, 'roles', 4, 1, 0, 1, 1, 'descrip', NULL);
	INSERT INTO table_fields_info VALUES(206, 'roles', 5, 1, 0, 0, 0, 'update_time', NULL);

	INSERT INTO table_fields_info VALUES(301, 'usermsgs_view', 0, 0, 1, 0, 1, 'id', NULL);
	INSERT INTO table_fields_info VALUES(302, 'usermsgs_view', 1, 1, 1, 1, 1, 'name', NULL);
	INSERT INTO table_fields_info VALUES(303, 'usermsgs_view', 2, 1, 1, 0, 1, 'uid', NULL);
	INSERT INTO table_fields_info VALUES(304, 'usermsgs_view', 3, 1, 1, 1, 1, 'phone', NULL);
	INSERT INTO table_fields_info VALUES(305, 'usermsgs_view', 0, 0, 0, 1, 1, 'avatar', NULL);
	INSERT INTO table_fields_info VALUES(306, 'usermsgs_view', 0, 0, 0, 1, 1, 'age', NULL);
	INSERT INTO table_fields_info VALUES(307, 'usermsgs_view', 0, 0, 0, 1, 1, 'bind_email', NULL);
	INSERT INTO table_fields_info VALUES(308, 'usermsgs_view', 0, 0, 0, 1, 1, 'bind_phone', NULL);
	INSERT INTO table_fields_info VALUES(309, 'usermsgs_view', 0, 0, 0, 1, 1, 'clan', NULL);
	INSERT INTO table_fields_info VALUES(310, 'usermsgs_view', 0, 0, 0, 1, 1, 'email', NULL);
	INSERT INTO table_fields_info VALUES(311, 'usermsgs_view', 0, 0, 0, 1, 1, 'gender', NULL);
	INSERT INTO table_fields_info VALUES(312, 'usermsgs_view', 0, 0, 0, 1, 1, 'political_status', NULL);
	INSERT INTO table_fields_info VALUES(313, 'usermsgs_view', 0, 0, 0, 1, 1, 'profile', NULL);

	INSERT INTO table_fields_info VALUES(401, 'menus_view', 0, 0, 0, 1, 1, 'pmid', '关联权限');
	INSERT INTO table_fields_info VALUES(402, 'menus_view', 0, 0, 0, 1, 1, 'pid', '父菜单');
	INSERT INTO table_fields_info VALUES(403, 'menus_view', 0, 1, 1, 1, 1, 'name', NULL);
	INSERT INTO table_fields_info VALUES(404, 'menus_view', 1, 1, 1, 1, 1, 'url', '地址');
	INSERT INTO table_fields_info VALUES(405, 'menus_view', 2, 1, 0, 0, 1, 'type', NULL);
	INSERT INTO table_fields_info VALUES(406, 'menus_view', 3, 1, 0, 1, 1, 'status', '状态');
	INSERT INTO table_fields_info VALUES(407, 'menus_view', 4, 1, 0, 0, 0, 'add_time', NULL);
	INSERT INTO table_fields_info VALUES(408, 'menus_view', 0, 0, 0, 0, 0, 'add_user', '添加人');
	INSERT INTO table_fields_info VALUES(409, 'menus_view', 0, 0, 0, 1, 1, 'descrip', NULL);
	INSERT INTO table_fields_info VALUES(410, 'menus_view', 0, 0, 0, 1, 1, 'icon', NULL);
	INSERT INTO table_fields_info VALUES(411, 'menus_view', 0, 0, 0, 1, 1, 'oper_type', '操作类型');

	INSERT INTO table_fields_info VALUES(501, 'permissions_view', 0, 1, 1, 0, 0, 'id', NULL);
	INSERT INTO table_fields_info VALUES(502, 'permissions_view', 1, 1, 1, 1, 1, 'name', NULL);
	INSERT INTO table_fields_info VALUES(503, 'permissions_view', 2, 1, 0, 1, 1, 'status', NULL);
	INSERT INTO table_fields_info VALUES(504, 'permissions_view', 3, 0, 1, 1, 1, 'guid', NULL);
	INSERT INTO table_fields_info VALUES(505, 'permissions_view', 4, 1, 0, 1, 1, 'oper_type', NULL);
	INSERT INTO table_fields_info VALUES(506, 'permissions_view', 5, 1, 0, 0, 0, 'add_user', '添加人');
	INSERT INTO table_fields_info VALUES(507, 'permissions_view', 6, 1, 0, 0, 0, 'add_time', NULL);
	INSERT INTO table_fields_info VALUES(508, 'permissions_view', 7, 1, 0, 0, 0, 'update_time', NULL);
	INSERT INTO table_fields_info VALUES(509, 'permissions_view', 8, 0, 0, 1, 1, 'descrip', NULL);

	INSERT INTO table_fields_info VALUES(601, 'deleted_usermsgs_view', 0, 1, 1, 0, NULL, 'name', NULL);
	INSERT INTO table_fields_info VALUES(602, 'deleted_usermsgs_view', 1, 1, 0, 0, NULL, 'delete_time', NULL);
	INSERT INTO table_fields_info VALUES(603, 'deleted_usermsgs_view', 2, 1, 0, 0, NULL, 'delete_user', NULL);
	INSERT INTO table_fields_info VALUES(604, 'deleted_usermsgs_view', 3, 1, 0, 0, NULL, 'add_time', NULL);
	INSERT INTO table_fields_info VALUES(605, 'deleted_usermsgs_view', 4, 1, 0, 0, NULL, 'last_login_time', NULL);
	INSERT INTO table_fields_info VALUES(606, 'deleted_usermsgs_view', 5, 1, 0, 0, NULL, 'delete_type', NULL);
	INSERT INTO table_fields_info VALUES(607, 'deleted_usermsgs_view', 6, 1, 0, 0, NULL, 'bind_email', NULL);
	INSERT INTO table_fields_info VALUES(608, 'deleted_usermsgs_view', 7, 1, 0, 0, NULL, 'bind_phone', NULL);

-- 字段信息视图
	CREATE OR REPLACE VIEW fields_info_view AS
		SELECT
			COLUMNS.TABLE_NAME as table_name,
			COLUMNS.COLUMN_NAME as field_name,
			(CASE 
				WHEN table_fields_info.display_name IS NULL
					THEN COLUMNS.COLUMN_COMMENT
				ELSE
					table_fields_info.display_name
				END
			) AS display_name,
			table_fields_info.display_order,
			table_fields_info.display,
			table_fields_info.searchable,
			table_fields_info.editable,
			table_fields_info.editable_when_add
		FROM information_schema.`COLUMNS` COLUMNS
		LEFT JOIN views ON COLUMNS.TABLE_NAME = views.view_name
		LEFT JOIN table_fields_info ON CONCAT(COLUMNS.TABLE_NAME, COLUMNS.COLUMN_NAME) = CONCAT(table_fields_info.table_name, table_fields_info.field_name)
		WHERE
			COLUMNS.TABLE_SCHEMA = 'kas' AND
			COLUMNS.TABLE_NAME NOT IN ("fields_info_view", "tables", "views_info") AND
			COLUMNS.COLUMN_NAME <> "deleted"
		ORDER BY COLUMNS.TABLE_NAME;

-- 角色表
	DROP TABLE IF EXISTS roles;
	CREATE TABLE roles (
		id TINYINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
		name VARCHAR(20) NOT NULL COMMENT '角色名称',
		add_user VARCHAR(20) NOT NULL DEFAULT '超级管理员' COMMENT '添加人',
		level TINYINT UNSIGNED COMMENT '角色级别',
		add_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
		update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
		descrip VARCHAR(100) DEFAULT NULL COMMENT '备注',
		
		deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除'
	) COMMENT '管理角色的表' AUTO_INCREMENT = 50;
	
	INSERT INTO roles(id, name, level) VALUES(1, '超级管理员', 1);
	INSERT INTO roles(id, name, level) VALUES(2, '管理员', 2);
	INSERT INTO roles(id, name, level) VALUES(9, '用户', 127);

-- 用户角色关联表
	DROP TABLE IF EXISTS user_role_assoc;
	CREATE TABLE user_role_assoc (
		uid BIGINT(12) UNSIGNED,
		rid TINYINT UNSIGNED,
		PRIMARY KEY(uid, rid),
		FOREIGN KEY (`uid`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
		FOREIGN KEY (`rid`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
	) COMMENT '用户角色关联表';

	INSERT INTO user_role_assoc VALUES(999999, 1);

-- 用户信息视图
	CREATE OR REPLACE VIEW usermsgs_view AS
		SELECT
			users.uid,
			usermsgs.*,
			GROUP_CONCAT(roles.name SEPARATOR ',') 'roles',
			MIN(roles.level) AS max_role_level,
			users.add_time,
			REPLACE(users.bind_email, SUBSTR(users.bind_email, 4, 6), '*****') AS bind_email,
			REPLACE(users.bind_phone, SUBSTR(users.bind_phone, 4, 4), '****') AS bind_phone
		FROM users
		JOIN usermsgs ON users.id = usermsgs.id
		JOIN user_role_assoc ON usermsgs.id = user_role_assoc.uid
		JOIN roles ON user_role_assoc.rid = roles.id
		WHERE users.deleted = 0
		GROUP BY users.id;

-- 自动添加新的用户的角色为用户
	DROP TRIGGER IF EXISTS on_user_insert;
	CREATE TRIGGER `on_user_insert` AFTER INSERT ON `users` 
	FOR EACH ROW
	BEGIN
		INSERT INTO user_role_assoc VALUES(NEW.id, 9);
		INSERT INTO usermsgs(id, name) VALUES(NEW.id, CONCAT('用户', NEW.id));
	END;
	
-- 日志表
	DROP TABLE IF EXISTS logs;
	CREATE TABLE logs (
		id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
		uid BIGINT(12) UNSIGNED COMMENT '请求用户ID',
		type ENUM('POST', 'GET', 'PUT', 'DELETE') NOT NULL COMMENT '请求类型',
		title VARCHAR(20) NOT NULL COMMENT '名称',
		url VARCHAR(255) NOT NULL COMMENT '地址',
		params JSON COMMENT '参数',
		ip VARCHAR(15) COMMENT '请求IP',
		date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
		
		FOREIGN KEY(uid) REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL
	);

-- 权限表
	DROP TABLE IF EXISTS permissions;
	CREATE TABLE permissions (
		id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
		name VARCHAR(50) NOT NULL COMMENT '名称',
		status BOOL DEFAULT TRUE COMMENT '状态',
		guid VARCHAR(128) NOT NULL COMMENT '唯一标识',
		descrip VARCHAR(100) DEFAULT NULL COMMENT '备注',
		oper_type TINYINT UNSIGNED DEFAULT NULL COMMENT '操作方式', -- 仅对type=oper时有效, 1代表只能批量操作, 2代表只能单一操作, 3代表既能批量也能单一操作, 
 		add_uid BIGINT(12) UNSIGNED DEFAULT 999999 COMMENT '添加人',
		add_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
		update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
		
		FOREIGN KEY(add_uid) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
		INDEX(guid)
	) COMMENT '管理权限的表' AUTO_INCREMENT = 100;

-- 权限视图
	CREATE OR REPLACE VIEW permissions_view AS
		SELECT
			permissions.id,
			permissions.name,
			permissions.status,
			permissions.guid,
			permissions.descrip,
			permissions.oper_type,
			usermsgs.name AS add_user,
			permissions.add_time,
			permissions.update_time
		FROM permissions
		LEFT JOIN usermsgs ON permissions.add_uid = usermsgs.id
		;

-- 菜单表
	DROP TABLE IF EXISTS menus;
	CREATE TABLE menus(
		id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
		name VARCHAR(50) NOT NULL COMMENT '名称',
		icon VARCHAR(255) DEFAULT NULL COMMENT '图标',
		type TINYINT UNSIGNED NOT NULL COMMENT '类型', -- 1-菜单; 2-选项; 3-标签; 4-操作;
		oper_type TINYINT UNSIGNED DEFAULT NULL COMMENT '操作类型',
		status BOOL DEFAULT TRUE COMMENT '状态',
		url VARCHAR(255) COMMENT '地址',
		pmid SMALLINT UNSIGNED COMMENT '关联权限ID',
		descrip VARCHAR(100) DEFAULT NULL COMMENT '备注',
		pid SMALLINT UNSIGNED DEFAULT NULL COMMENT '父权限ID',
		ref_id SMALLINT UNSIGNED DEFAULT NULL COMMENT '引用页面id',
		add_uid BIGINT(12) UNSIGNED DEFAULT 999999 COMMENT '添加人',
		add_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
		update_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
		
		UNIQUE(url),
		FOREIGN KEY(pid) REFERENCES menus(id) ON UPDATE CASCADE ON DELETE CASCADE,
		FOREIGN KEY(pmid) REFERENCES permissions(id) ON UPDATE CASCADE ON DELETE SET NULL,
		FOREIGN KEY(ref_id) REFERENCES menus(id) ON UPDATE CASCADE ON DELETE SET NULL
		, FOREIGN KEY(add_uid) REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL
	) AUTO_INCREMENT = 10000;

-- 菜单视图
	CREATE OR REPLACE VIEW menus_view AS
		SELECT 
			menus.id,
			menus.name,
			menus.icon,
			menus.type,
			(CASE
				WHEN menus.pmid IS NULL THEN
					menus.status
				ELSE
					menus.status AND permissions.status
				END
			) AS status,
			menus.pmid,
			menus.url,
			menus.descrip,
			menus.pid,
			menus.ref_id,
			(CASE WHEN permissions.oper_type IS NOT NULL THEN
				permissions.oper_type
			ELSE
				menus.oper_type
			END) AS oper_type,
			usermsgs.name AS add_user,
			menus.add_time,
			menus.update_time
		FROM menus
		LEFT JOIN permissions ON menus.pmid = permissions.id
		JOIN usermsgs ON usermsgs.id = menus.add_uid
		;

-- 角色、权限关联表
	DROP TABLE IF EXISTS role_perm_assoc;
	CREATE TABLE role_perm_assoc (
		id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
		rid TINYINT UNSIGNED COMMENT '角色ID',
		mid SMALLINT UNSIGNED COMMENT '菜单ID',
		FOREIGN KEY(rid) REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE,
		FOREIGN KEY(mid) REFERENCES menus(id) ON DELETE CASCADE ON UPDATE CASCADE,
		UNIQUE(rid, mid)
	) COMMENT '角色权限关联表';
	
	
-- 用户权限关联表
	DROP TABLE IF EXISTS user_perm_assoc;
	CREATE TABLE user_perm_assoc (
		id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
		uid BIGINT(12) UNSIGNED NOT NULL COMMENT '用户ID',
		mid SMALLINT UNSIGNED DEFAULT NULL COMMENT '菜单ID',
		authorize BOOL DEFAULT TRUE NOT NULL COMMENT '授权状态',
		FOREIGN KEY(uid) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
		FOREIGN KEY(mid) REFERENCES menus(id) ON DELETE CASCADE ON UPDATE CASCADE,
		UNIQUE(uid, mid)
	) COMMENT '用户权限关联表';

	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (11, '菜单列表', 'adm_menus_index', NULL);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (12, '菜单添加', 'adm_menus_add', 3);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (13, '菜单编辑', 'adm_menus_edit', 2);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (14, '菜单删除', 'adm_menus_delete', 2);
	
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (15, '权限列表', 'adm_permissions_index', NULL);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (16, '权限添加', 'adm_permissions_add', 1);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (17, '权限编辑', 'adm_permissions_edit', 2);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (18, '权限删除', 'adm_permissions_delete', 2);

	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (19, '用户列表', 'adm_users_index', NULL);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (20, '用户添加', 'adm_users_add', 1);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (21, '用户编辑', 'adm_users_edit', 2);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (22, '用户删除', 'adm_users_delete', 1);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (23, '重置用户密码', 'adm_users_reset', 1);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (24, '授权用户', 'adm_users_authorize', 2);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (25, '赋予用户角色', 'adm_users_assign', 1);

	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (26, '删除/注销用户列表', 'adm_users_deleted_index', NULL);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (27, '注销/删除用户恢复', 'adm_users_restore', 3);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (28, '永久删除用户', 'adm_users_delete_permanently', 3);

	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (29, '角色列表', 'adm_roles_index', NULL);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (30, '添加角色', 'adm_roles_add', 1);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (31, '编辑角色', 'adm_roles_edit', 2);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (32, '删除角色', 'adm_roles_delete', 2);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (33, '授权角色', 'adm_roles_authorize', 2);

	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (34, '数据库列表', 'adm_databases_index', NULL);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (35, '数据库管理', 'adm_databases_edit', 2);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (36, '重载字段配置', 'adm_databases_reload', 1);

	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (37, '日志列表', 'adm_logs_index', NULL);
	INSERT INTO `permissions`(id, name, guid, oper_type) VALUES (38, '删除日志', 'adm_logs_delete', 1);

-- 菜单插入
	CREATE TRIGGER `on_menu_insert` AFTER INSERT ON `menus` 
	FOR EACH ROW
	INSERT INTO role_perm_assoc(rid, mid) VALUES(1, NEW.id);
	
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1000, 1, NULL, NULL, NULL, '系统管理', 'man_system');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1001, 2, 1000, NULL, NULL, '菜单管理', 'man_menu');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1002, 4, 1001, 11, '/menus/index', '列表', 'index');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1003, 4, 1001, 12, '/menus/add', '添加', 'add');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1004, 4, 1001, 13, '/menus/edit', '编辑', 'edit');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1005, 4, 1001, 14, '/menus/delete', '删除', 'delete');
	UPDATE menus SET ref_id = 1002 WHERE id = 1001;

	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1006, 2, 1000, NULL, NULL, '权限管理', 'man_permission');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1007, 4, 1006, 15, '/permissions/index', '列表', 'index');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1008, 4, 1006, 16, '/permissions/add', '添加', 'add');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1009, 4, 1006, 17, '/permissions/edit', '编辑', 'edit');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1010, 4, 1006, 18, '/permissions/delete', '删除', 'delete');
	UPDATE menus SET ref_id = 1007 WHERE id = 1006;

	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1011, 2, 1000, NULL, NULL, '用户信息', 'man_role');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1012, 3, 1011, NULL, NULL, '用户管理', 'page');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1013, 4, 1012, 19, '/users/index', '列表', 'index');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1014, 4, 1012, 20, '/users/add', '添加', 'add');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1015, 4, 1012, 21, '/users/edit', '编辑', 'edit');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1016, 4, 1012, 22, '/users/delete', '删除', 'delete');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1017, 4, 1012, 23, '/users/reset', '重置密码', 'reset');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1018, 4, 1012, 24, '/users/authorize', '授权', 'authorize');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1019, 4, 1012, 25, '/users/assign', '赋予角色', 'assign');
	UPDATE menus SET ref_id = 1013 WHERE id = 1011;
	UPDATE menus SET ref_id = 1013 WHERE id = 1012;
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1020, 3, 1011, NULL, NULL, '删除/注销管理', 'page');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1021, 4, 1020, 26, '/users/admin/deleted/index', '列表', 'index');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1022, 4, 1020, 27, '/users/admin/deleted/restore', '恢复', 'backward');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1023, 4, 1020, 28, '/users/admin/deleted/delete', '永久删除', 'delete');
	UPDATE menus SET ref_id = 1021 WHERE id = 1020;

	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1024, 2, 1000, NULL, NULL, '角色管理', 'man_user');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1025, 4, 1024, 29, '/roles/index', '列表', 'index');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1026, 4, 1024, 30, '/roles/add', '添加', 'add');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1027, 4, 1024, 31, '/roles/edit', '编辑', 'edit');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1028, 4, 1024, 32, '/roles/delete', '删除', 'delete');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1029, 4, 1024, 33, '/roles/authorize', '授权', 'authorize');
	UPDATE menus SET ref_id = 1025 WHERE id = 1024;

	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1030, 2, 1000, NULL, NULL, '数据库管理', 'man_database');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1031, 4, 1030, 34, '/databases/index', '列表', 'index');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1032, 4, 1030, 35, '/databases/edit', '管理', 'manage');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1033, 4, 1030, 36, '/databases/configs/fields/reload', '重载字段配置', 'manage');
	UPDATE menus SET ref_id = 1031 WHERE id = 1030;

	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1034, 2, 1000, NULL, NULL, '日志管理', 'man_log');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1035, 4, 1034, 37, '/logs/index', '列表', 'index');
	INSERT INTO `menus`(id, type, pid, pmid, url, name, icon) VALUES(1036, 4, 1034, 38, '/logs/delete', '删除', 'delete');
	UPDATE menus SET ref_id = 1035 WHERE id = 1034;
