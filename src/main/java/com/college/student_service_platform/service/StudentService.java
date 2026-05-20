package com.college.student_service_platform.service;

import com.college.student_service_platform.dto.StudentImportItem;
import com.college.student_service_platform.dto.StudentImportResult;
import com.college.student_service_platform.dto.StudentListItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StudentService {

    private final JdbcTemplate jdbcTemplate;
    private final AtomicLong idSeq = new AtomicLong(System.currentTimeMillis());

    public StudentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public StudentImportResult importStudents(List<StudentImportItem> students) {
        if (students == null || students.isEmpty()) {
            return new StudentImportResult(0, 0);
        }

        int inserted = 0;
        int updated = 0;

        for (StudentImportItem s : students) {
            String studentNo = normalize(s.getStudentNo());
            String name = normalize(s.getName());
            String roleCode = normalize(s.getRoleCode());
            if (roleCode.equals("admin")) {
                String username = studentNo.isEmpty() ? name : studentNo;
                if (username.isEmpty()) {
                    continue;
                }

                int status = s.getStatus() == null ? 1 : (s.getStatus() == 0 ? 0 : 1);
                String password = normalize(s.getPassword());
                if (password.isEmpty()) password = "123456";
                String wechatOpenid = normalize(s.getWechatOpenid());
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                int adminUpdated = jdbcTemplate.update(
                        """
                                UPDATE t_user
                                SET password = ?, status = ?, wechat_openid = ?, updated_at = ?
                                WHERE username = ? AND role_code = 'admin'
                                """,
                        password,
                        status,
                        wechatOpenid.isEmpty() ? null : wechatOpenid,
                        now,
                        username
                );

                if (adminUpdated == 0) {
                    Long userId = idSeq.incrementAndGet();
                    jdbcTemplate.update(
                            """
                                    INSERT INTO t_user
                                    (id, username, password, role_code, student_no, wechat_openid, status, created_at, updated_at)
                                    VALUES (?, ?, ?, 'admin', NULL, ?, ?, ?, ?)
                                    """,
                            userId,
                            username,
                            password,
                            wechatOpenid.isEmpty() ? null : wechatOpenid,
                            status,
                            now,
                            now
                    );
                    inserted += 1;
                } else {
                    updated += 1;
                }
                continue;
            }

            roleCode = "student";

            String className = normalize(s.getClassName());
            String major = normalize(s.getMajor());
            String grade = normalize(s.getGrade());
            if (studentNo.isEmpty() || name.isEmpty() || className.isEmpty() || major.isEmpty() || grade.isEmpty()) {
                continue;
            }

            String gender = normalize(s.getGender());
            if (gender.isEmpty()) gender = "未知";
            String ethnicity = normalize(s.getEthnicity());
            String politicalStatus = normalize(s.getPoliticalStatus());
            if (politicalStatus.isEmpty()) politicalStatus = "未知";
            String contact = normalize(s.getContact());

            int status = s.getStatus() == null ? 1 : (s.getStatus() == 0 ? 0 : 1);
            String password = normalize(s.getPassword());
            if (password.isEmpty()) password = "123456";
            String wechatOpenid = normalize(s.getWechatOpenid());

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            int partyStageId = 0;
            if (s.getPartyStageId() != null) {
                partyStageId = s.getPartyStageId();
            }
            if (partyStageId < 0 || partyStageId > 5) {
                throw new IllegalArgumentException("党团流程阶段不合法");
            }

            int studentUpdated = jdbcTemplate.update(
                    """
                            UPDATE t_student
                            SET name = ?, gender = ?, ethnicity = ?, political_status = ?, party_stage_id = ?, class_name = ?, major = ?, grade = ?, contact = ?, status = ?, updated_at = ?
                            WHERE student_no = ?
                            """,
                    name,
                    gender,
                    ethnicity,
                    politicalStatus,
                    partyStageId,
                    className,
                    major,
                    grade,
                    contact,
                    status,
                    now,
                    studentNo
            );

            if (studentUpdated == 0) {
                Long studentId = idSeq.incrementAndGet();
                jdbcTemplate.update(
                        """
                                INSERT INTO t_student
                                (id, student_no, name, gender, ethnicity, political_status, party_stage_id, class_name, major, grade, contact, status, created_at, updated_at)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """,
                        studentId,
                        studentNo,
                        name,
                        gender,
                        ethnicity,
                        politicalStatus,
                        partyStageId,
                        className,
                        major,
                        grade,
                        contact,
                        status,
                        now,
                        now
                );
                inserted += 1;
            } else {
                updated += 1;
            }

            int userUpdated = jdbcTemplate.update(
                    """
                            UPDATE t_user
                            SET username = ?, password = ?, role_code = ?, wechat_openid = ?, status = ?, updated_at = ?
                            WHERE student_no = ?
                            """,
                    studentNo,
                    password,
                    roleCode,
                    wechatOpenid.isEmpty() ? null : wechatOpenid,
                    status,
                    now,
                    studentNo
            );

            if (userUpdated == 0) {
                Long userId = idSeq.incrementAndGet();
                jdbcTemplate.update(
                        """
                                INSERT INTO t_user
                                (id, username, password, role_code, student_no, wechat_openid, status, created_at, updated_at)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """,
                        userId,
                        studentNo,
                        password,
                        roleCode,
                        studentNo,
                        wechatOpenid.isEmpty() ? null : wechatOpenid,
                        status,
                        now,
                        now
                );
            }

        }

        return new StudentImportResult(inserted, updated);
    }

    public List<StudentListItem> listStudents(String keyword) {
        String k = normalize(keyword).toLowerCase();
        boolean hasKeyword = !k.isEmpty();

        String sql = """
                SELECT
                  s.student_no,
                  s.name,
                  s.gender,
                  s.ethnicity,
                  s.political_status,
                  COALESCE(s.party_stage_id, 0) AS party_stage_id,
                  COALESCE(ps.stage_name, '未申请') AS party_stage,
                  s.class_name,
                  s.major,
                  s.grade,
                  s.contact,
                  COALESCE(u.role_code, 'student') AS role_code,
                  COALESCE(u.status, s.status) AS status,
                  s.created_at,
                  s.updated_at
                FROM t_student s
                LEFT JOIN t_user u ON u.student_no = s.student_no
                LEFT JOIN t_process_stage ps ON ps.stage_id = s.party_stage_id
                """;

        List<Object> args = new ArrayList<>();
        if (hasKeyword) {
            sql += """
                    WHERE LOWER(s.student_no) LIKE ?
                       OR LOWER(s.name) LIKE ?
                       OR LOWER(s.class_name) LIKE ?
                       OR LOWER(s.major) LIKE ?
                    """;
            String like = "%" + k + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }

        sql += " ORDER BY s.updated_at DESC";

        return jdbcTemplate.query(sql, args.toArray(), (rs, rowNum) -> {
            StudentListItem item = new StudentListItem();
            item.setStudentNo(rs.getString("student_no"));
            item.setName(rs.getString("name"));
            item.setGender(rs.getString("gender"));
            item.setEthnicity(rs.getString("ethnicity"));
            item.setPoliticalStatus(rs.getString("political_status"));
            item.setClassName(rs.getString("class_name"));
            item.setMajor(rs.getString("major"));
            item.setGrade(rs.getString("grade"));
            item.setContact(rs.getString("contact"));
            item.setRoleCode(rs.getString("role_code"));
            item.setStatus(rs.getInt("status"));
            item.setPartyStageId(rs.getInt("party_stage_id"));
            item.setPartyStage(rs.getString("party_stage"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (createdAt != null) item.setCreatedAt(createdAt.toLocalDateTime());
            if (updatedAt != null) item.setUpdatedAt(updatedAt.toLocalDateTime());
            return item;
        });
    }

    @Transactional
    public void deleteByStudentNo(String studentNo) {
        String no = normalize(studentNo);
        if (no.isEmpty()) return;
        jdbcTemplate.update("DELETE FROM t_student WHERE student_no = ?", no);
        jdbcTemplate.update("DELETE FROM t_user WHERE student_no = ?", no);
    }

    private String normalize(String v) {
        return v == null ? "" : v.trim();
    }
}
