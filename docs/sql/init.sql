-- =====================================================
-- 学院学生综合服务与党团管理平台
-- 数据库初始化脚本
-- 数据库：Kingbase
-- 维护人：开发者 A
-- =====================================================

-- 开发阶段允许重复执行，先删除旧表
DROP TABLE IF EXISTS t_operation_log;
DROP TABLE IF EXISTS t_file;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_role;

-- =====================================================
-- 1. 角色表
-- 说明：当前系统只区分学生和管理员
-- =====================================================
CREATE TABLE t_role (
                        id BIGINT PRIMARY KEY,
                        role_code VARCHAR(50) NOT NULL UNIQUE,
                        role_name VARCHAR(100) NOT NULL,
                        description VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_role IS '角色表';
COMMENT ON COLUMN t_role.id IS '角色ID';
COMMENT ON COLUMN t_role.role_code IS '角色编码：student/admin';
COMMENT ON COLUMN t_role.role_name IS '角色名称';
COMMENT ON COLUMN t_role.description IS '角色描述';
COMMENT ON COLUMN t_role.created_at IS '创建时间';
COMMENT ON COLUMN t_role.updated_at IS '更新时间';

-- =====================================================
-- 2. 用户表
-- 说明：
-- username 表示真实姓名
-- student_no 表示学号，学生用户使用
-- role_code 用于区分 student/admin
-- =====================================================
CREATE TABLE t_user (
                        id BIGINT PRIMARY KEY,
                        username VARCHAR(100) NOT NULL,
                        password VARCHAR(255),
                        student_no VARCHAR(50),
                        phone VARCHAR(50),
                        email VARCHAR(100),
                        wechat_openid VARCHAR(100),
                        role_code VARCHAR(50) NOT NULL,
                        status INTEGER DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_user IS '用户表，统一存储学生和管理员账号';
COMMENT ON COLUMN t_user.id IS '用户ID';
COMMENT ON COLUMN t_user.username IS '真实姓名';
COMMENT ON COLUMN t_user.password IS '登录密码，后续应加密存储';
COMMENT ON COLUMN t_user.student_no IS '学号，学生用户使用';
COMMENT ON COLUMN t_user.phone IS '手机号';
COMMENT ON COLUMN t_user.email IS '邮箱';
COMMENT ON COLUMN t_user.wechat_openid IS '微信小程序openid';
COMMENT ON COLUMN t_user.role_code IS '角色编码：student/admin';
COMMENT ON COLUMN t_user.status IS '账号状态：1正常，0禁用';
COMMENT ON COLUMN t_user.created_at IS '创建时间';
COMMENT ON COLUMN t_user.updated_at IS '更新时间';

-- 学号唯一。管理员没有学号，可以为空。
CREATE UNIQUE INDEX uk_user_student_no ON t_user(student_no);

-- =====================================================
-- 3. 文件表
-- 说明：统一管理上传文件、模板、通知附件、成绩单、证明 PDF
-- =====================================================
CREATE TABLE t_file (
                        id BIGINT PRIMARY KEY,
                        original_name VARCHAR(255) NOT NULL,
                        stored_name VARCHAR(255) NOT NULL,
                        file_path VARCHAR(500) NOT NULL,
                        file_type VARCHAR(100),
                        file_size BIGINT,
                        uploader_id BIGINT,
                        business_type VARCHAR(100),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_file IS '文件表，统一管理上传文件、模板、通知附件、成绩单、证明PDF等';
COMMENT ON COLUMN t_file.id IS '文件ID';
COMMENT ON COLUMN t_file.original_name IS '原始文件名';
COMMENT ON COLUMN t_file.stored_name IS '服务器保存文件名';
COMMENT ON COLUMN t_file.file_path IS '文件存储路径';
COMMENT ON COLUMN t_file.file_type IS '文件类型';
COMMENT ON COLUMN t_file.file_size IS '文件大小，单位字节';
COMMENT ON COLUMN t_file.uploader_id IS '上传人ID';
COMMENT ON COLUMN t_file.business_type IS '业务类型：policy/template/notice/certificate/transcript/other';
COMMENT ON COLUMN t_file.created_at IS '创建时间';

-- =====================================================
-- 4. 操作日志表
-- 说明：记录用户操作，便于后续追踪问题和答辩展示
-- =====================================================
CREATE TABLE t_operation_log (
                                 id BIGINT PRIMARY KEY,
                                 user_id BIGINT,
                                 username VARCHAR(100),
                                 operation VARCHAR(255),
                                 request_method VARCHAR(20),
                                 request_url VARCHAR(500),
                                 request_ip VARCHAR(100),
                                 status INTEGER,
                                 error_message VARCHAR(1000),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_operation_log IS '操作日志表';
COMMENT ON COLUMN t_operation_log.id IS '日志ID';
COMMENT ON COLUMN t_operation_log.user_id IS '操作用户ID';
COMMENT ON COLUMN t_operation_log.username IS '操作用户真实姓名';
COMMENT ON COLUMN t_operation_log.operation IS '操作内容';
COMMENT ON COLUMN t_operation_log.request_method IS '请求方法，如 GET/POST';
COMMENT ON COLUMN t_operation_log.request_url IS '请求地址';
COMMENT ON COLUMN t_operation_log.request_ip IS '请求IP';
COMMENT ON COLUMN t_operation_log.status IS '操作状态：1成功，0失败';
COMMENT ON COLUMN t_operation_log.error_message IS '错误信息';
COMMENT ON COLUMN t_operation_log.created_at IS '创建时间';

-- =====================================================
-- 初始化基础角色数据
-- =====================================================
INSERT INTO t_role (id, role_code, role_name, description)
VALUES
    (1, 'student', '学生', '微信小程序学生端用户'),
    (2, 'admin', '管理员', 'Web后台管理员');

-- =====================================================
-- 可选测试数据
-- 正式提交时可以保留注释，不默认插入测试用户
-- 如果需要测试用户表，可以手动取消注释执行
-- =====================================================

-- INSERT INTO t_user (
--     id, username, password, student_no, phone, email, wechat_openid, role_code, status
-- )
-- VALUES
-- (1, '测试学生', '123456', '20260001', '13800000000', 'student001@example.com', NULL, 'student', 1),
-- (2, '测试管理员', '123456', NULL, '13900000000', 'admin001@example.com', NULL, 'admin', 1);