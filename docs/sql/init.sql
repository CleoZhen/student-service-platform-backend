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
DROP TABLE IF EXISTS t_notification_receipt;
DROP TABLE IF EXISTS t_notification;
DROP TABLE IF EXISTS t_certificate_apply;
DROP TABLE IF EXISTS t_student_stage;
DROP TABLE IF EXISTS t_process_stage;
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
COMMENT ON COLUMN t_student.status IS '学生状态：1正常，0无效';
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
                                 operation_status INTEGER,
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
COMMENT ON COLUMN t_operation_log.operation_status IS '操作状态：1成功，0失败';
COMMENT ON COLUMN t_operation_log.error_message IS '错误信息';
COMMENT ON COLUMN t_operation_log.created_at IS '创建时间';

-- =====================================================
-- 6. 党团事务标准流程表
-- 说明：
-- 定义入党流程的标准阶段
-- =====================================================
CREATE TABLE t_process_stage (
                                 stage_id INTEGER PRIMARY KEY,
                                 stage_name VARCHAR(100) NOT NULL,
                                 order_num INTEGER NOT NULL,
                                 duration INTEGER,
                                 description VARCHAR(255)
);

COMMENT ON TABLE t_process_stage IS '党团流程标准阶段定义表';
COMMENT ON COLUMN t_process_stage.stage_id IS '阶段ID';
COMMENT ON COLUMN t_process_stage.stage_name IS '阶段名称';
COMMENT ON COLUMN t_process_stage.order_num IS '阶段顺序';
COMMENT ON COLUMN t_process_stage.duration IS '标准持续时长，单位可按业务约定为天';
COMMENT ON COLUMN t_process_stage.description IS '阶段说明';

-- =====================================================
-- 7. 学生党团阶段记录表
-- 说明：
-- 记录每个学生当前所处党团阶段
-- =====================================================
CREATE TABLE t_student_stage (
                                 id BIGINT PRIMARY KEY,
                                 student_no VARCHAR(50) NOT NULL,
                                 stage_id INTEGER NOT NULL,
                                 start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 stage_status VARCHAR(50) DEFAULT '进行中',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_student_stage IS '学生当前党团阶段进度表';
COMMENT ON COLUMN t_student_stage.id IS '记录ID';
COMMENT ON COLUMN t_student_stage.student_no IS '学号，对应 t_student.student_no';
COMMENT ON COLUMN t_student_stage.stage_id IS '阶段ID，对应 t_process_stage.stage_id';
COMMENT ON COLUMN t_student_stage.start_date IS '当前阶段开始时间';
COMMENT ON COLUMN t_student_stage.stage_status IS '阶段状态：进行中/已完成/暂停';
COMMENT ON COLUMN t_student_stage.created_at IS '创建时间';
COMMENT ON COLUMN t_student_stage.updated_at IS '更新时间';

-- =====================================================
-- 8. 电子证明申请表
-- 说明：
-- 记录学生在线申请电子证明的流水
-- =====================================================
CREATE TABLE t_certificate_apply (
                                     id BIGINT PRIMARY KEY,
                                     student_no VARCHAR(50) NOT NULL,
                                     certificate_type VARCHAR(100) NOT NULL,
                                     apply_status VARCHAR(50) DEFAULT '待审核',
                                     extra_data VARCHAR(500),
                                     file_id BIGINT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_certificate_apply IS '电子证明在线申请流水表';
COMMENT ON COLUMN t_certificate_apply.id IS '申请ID';
COMMENT ON COLUMN t_certificate_apply.student_no IS '申请学生学号，对应 t_student.student_no';
COMMENT ON COLUMN t_certificate_apply.certificate_type IS '证明类型，如在校证明/请假条/用章申请';
COMMENT ON COLUMN t_certificate_apply.apply_status IS '申请状态：待审核/已通过/已驳回/已撤销';
COMMENT ON COLUMN t_certificate_apply.extra_data IS '额外申请信息，可存放简单JSON字符串';
COMMENT ON COLUMN t_certificate_apply.file_id IS '生成的证明文件ID，对应 t_file.id';
COMMENT ON COLUMN t_certificate_apply.created_at IS '创建时间';
COMMENT ON COLUMN t_certificate_apply.updated_at IS '更新时间';

-- =====================================================
-- 9. 通知公告表
-- 说明：
-- 管理员发布精准通知，支持标签和附件
-- =====================================================
CREATE TABLE t_notification (
                                id BIGINT PRIMARY KEY,
                                title VARCHAR(200) NOT NULL,
                                content TEXT,
                                tags VARCHAR(255),
                                is_urgent BOOLEAN DEFAULT FALSE,
                                file_id BIGINT,
                                publisher_id BIGINT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_notification IS '精准通知发布表';
COMMENT ON COLUMN t_notification.id IS '通知ID';
COMMENT ON COLUMN t_notification.title IS '通知标题';
COMMENT ON COLUMN t_notification.content IS '通知正文';
COMMENT ON COLUMN t_notification.tags IS '目标标签，多个标签可用逗号分隔';
COMMENT ON COLUMN t_notification.is_urgent IS '是否紧急通知';
COMMENT ON COLUMN t_notification.file_id IS '附件文件ID，对应 t_file.id';
COMMENT ON COLUMN t_notification.publisher_id IS '发布人ID，对应 t_user.id';
COMMENT ON COLUMN t_notification.created_at IS '创建时间';
COMMENT ON COLUMN t_notification.updated_at IS '更新时间';

-- =====================================================
-- 10. 通知已读回执表
-- 说明：
-- 记录学生是否确认阅读通知
-- =====================================================
CREATE TABLE t_notification_receipt (
                                        id BIGINT PRIMARY KEY,
                                        notification_id BIGINT NOT NULL,
                                        student_no VARCHAR(50) NOT NULL,
                                        is_confirmed BOOLEAN DEFAULT FALSE,
                                        confirmed_at TIMESTAMP,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_notification_receipt IS '学生通知已读回执表';
COMMENT ON COLUMN t_notification_receipt.id IS '回执ID';
COMMENT ON COLUMN t_notification_receipt.notification_id IS '通知ID，对应 t_notification.id';
COMMENT ON COLUMN t_notification_receipt.student_no IS '学生学号，对应 t_student.student_no';
COMMENT ON COLUMN t_notification_receipt.is_confirmed IS '是否已确认阅读';
COMMENT ON COLUMN t_notification_receipt.confirmed_at IS '确认阅读时间';
COMMENT ON COLUMN t_notification_receipt.created_at IS '创建时间';

-- =====================================================
-- 初始化基础角色数据
-- =====================================================
INSERT INTO t_role (id, role_code, role_name, description)
VALUES
    (1, 'student', '学生', '微信小程序学生端用户'),
    (2, 'admin', '管理员', 'Web后台管理员');

-- =====================================================
-- 初始化党团流程标准阶段
-- =====================================================
INSERT INTO t_process_stage (stage_id, stage_name, order_num, duration, description)
VALUES
    (1, '入党申请人', 1, NULL, '提交入党申请书后的初始阶段'),
    (2, '入党积极分子', 2, NULL, '经推荐和培养后确定为入党积极分子'),
    (3, '发展对象', 3, NULL, '经过培养考察后确定为发展对象'),
    (4, '预备党员', 4, NULL, '支部大会通过并经上级党组织批准后成为预备党员'),
    (5, '正式党员', 5, NULL, '预备期满并转正后成为正式党员');

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

-- INSERT INTO t_student_stage (
--     id, student_no, stage_id, start_date, stage_status
-- )
-- VALUES
--     (1, '20240001', 1, CURRENT_TIMESTAMP, '进行中');

-- INSERT INTO t_notification (
--     id, title, content, tags, is_urgent, file_id, publisher_id
-- )
-- VALUES
--     (1, '测试通知', '这是一条测试通知', '2024级,计科', FALSE, NULL, 2);

-- INSERT INTO t_notification_receipt (
--     id, notification_id, student_no, is_confirmed, confirmed_at
-- )
-- VALUES
--     (1, 1, '20240001', TRUE, CURRENT_TIMESTAMP);