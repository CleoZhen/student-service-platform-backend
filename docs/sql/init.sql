-- =====================================================
-- 学院学生综合服务与党团管理平台
-- 数据库初始化脚本
-- 数据库：Kingbase
-- 维护人：开发者 A
-- =====================================================

-- =====================================================
-- 开发阶段允许重复执行，先删除旧表
-- 注意：有依赖关系的表要先删
-- =====================================================
DROP TABLE IF EXISTS t_operation_log;
DROP TABLE IF EXISTS t_file;
DROP TABLE IF EXISTS t_student;
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
-- t_user 只负责登录账号和身份识别
-- 学生详细信息放到 t_student
-- 管理员账号也存这里
-- =====================================================
CREATE TABLE t_user (
                        id BIGINT PRIMARY KEY,
                        username VARCHAR(100) NOT NULL,
                        password VARCHAR(255),
                        role_code VARCHAR(50) NOT NULL,
                        student_no VARCHAR(50),
                        wechat_openid VARCHAR(100),
                        status INTEGER DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_user IS '用户表，负责登录账号和身份识别';
COMMENT ON COLUMN t_user.id IS '用户ID';
COMMENT ON COLUMN t_user.username IS '登录用户名。学生可使用姓名或学号，管理员可使用账号名';
COMMENT ON COLUMN t_user.password IS '登录密码，后续应加密存储';
COMMENT ON COLUMN t_user.role_code IS '角色编码：student/admin';
COMMENT ON COLUMN t_user.student_no IS '学号，学生用户使用，用于关联 t_student.student_no';
COMMENT ON COLUMN t_user.wechat_openid IS '微信小程序openid';
COMMENT ON COLUMN t_user.status IS '账号状态：1正常，0禁用';
COMMENT ON COLUMN t_user.created_at IS '创建时间';
COMMENT ON COLUMN t_user.updated_at IS '更新时间';

-- 学号唯一。管理员没有学号，可以为空。
CREATE UNIQUE INDEX uk_user_student_no ON t_user(student_no);

-- =====================================================
-- 3. 学生基础信息表
-- 说明：
-- 对应学生信息管理页面的手动录入字段
-- 包含学号、姓名、性别、民族、政治面貌、班级、专业、年级、联系方式
-- =====================================================
CREATE TABLE t_student (
                           id BIGINT PRIMARY KEY,
                           student_no VARCHAR(50) NOT NULL UNIQUE,
                           name VARCHAR(100) NOT NULL,
                           gender VARCHAR(20) DEFAULT '未知',
                           ethnicity VARCHAR(50),
                           political_status VARCHAR(50) DEFAULT '未知',
                           class_name VARCHAR(100) NOT NULL,
                           major VARCHAR(100) NOT NULL,
                           grade VARCHAR(50) NOT NULL,
                           contact VARCHAR(100),
                           status INTEGER DEFAULT 1,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_student IS '学生基础信息表';
COMMENT ON COLUMN t_student.id IS '学生记录ID';
COMMENT ON COLUMN t_student.student_no IS '学号';
COMMENT ON COLUMN t_student.name IS '姓名';
COMMENT ON COLUMN t_student.gender IS '性别：男/女/未知';
COMMENT ON COLUMN t_student.ethnicity IS '民族';
COMMENT ON COLUMN t_student.political_status IS '政治面貌';
COMMENT ON COLUMN t_student.class_name IS '班级';
COMMENT ON COLUMN t_student.major IS '专业';
COMMENT ON COLUMN t_student.grade IS '年级';
COMMENT ON COLUMN t_student.contact IS '联系方式，手机号或邮箱';
COMMENT ON COLUMN t_student.status IS '学生状态：1正常，0禁用';
COMMENT ON COLUMN t_student.created_at IS '创建时间';
COMMENT ON COLUMN t_student.updated_at IS '更新时间';

-- =====================================================
-- 4. 文件表
-- 说明：
-- 统一管理上传文件、模板、通知附件、成绩单、证明 PDF
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
COMMENT ON COLUMN t_file.uploader_id IS '上传人ID，对应 t_user.id';
COMMENT ON COLUMN t_file.business_type IS '业务类型：policy/template/notice/certificate/transcript/other';
COMMENT ON COLUMN t_file.created_at IS '创建时间';

-- =====================================================
-- 5. 操作日志表
-- 说明：
-- 记录用户操作，便于后续追踪问题和答辩展示
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
COMMENT ON COLUMN t_operation_log.user_id IS '操作用户ID，对应 t_user.id';
COMMENT ON COLUMN t_operation_log.username IS '操作用户名';
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
-- 正式提交时保持注释，不默认插入测试数据
-- 如果需要测试，可以手动取消注释执行
-- =====================================================

-- INSERT INTO t_student (
--     id, student_no, name, gender, ethnicity, political_status,
--     class_name, major, grade, contact, status
-- )
-- VALUES
--     (1, '20240001', '张三', '未知', '汉族', '未知',
--      '计科2401', '计算机科学与技术', '2024', '13800000000', 1);

-- INSERT INTO t_user (
--     id, username, password, role_code, student_no, wechat_openid, status
-- )
-- VALUES
--     (1, '张三', '123456', 'student', '20240001', NULL, 1),
--     (2, 'admin', '123456', 'admin', NULL, NULL, 1);