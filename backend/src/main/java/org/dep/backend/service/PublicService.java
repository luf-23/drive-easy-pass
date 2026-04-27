package org.dep.backend.service;

import java.util.List;

import org.dep.backend.dto.CoursePackageDto;
import org.dep.backend.dto.ExamSiteDto;
import org.springframework.stereotype.Service;

@Service
public class PublicService {
    public List<CoursePackageDto> listCoursePackages() {
        return List.of(
                new CoursePackageDto("normal", "普通班", 2980, 58,
                        List.of("基础课程覆盖", "标准训练时段", "科目二三强化"), "入门推荐"),
                new CoursePackageDto("vip", "VIP 班", 4680, 66,
                        List.of("一人一车优先", "专属教练跟进", "考试节点提醒"), "高通过率"),
                new CoursePackageDto("weekend", "周末班", 3380, 60,
                        List.of("周末集中训练", "上班族友好", "灵活排课"), "时间友好"),
                new CoursePackageDto("auto", "C2 自动挡班", 3580, 56,
                        List.of("自动挡专项教学", "女性学员占比高", "快速上手"), "热门"));
    }

    public List<ExamSiteDto> listExamSites() {
        return List.of(
                new ExamSiteDto(
                        "site-a",
                        "高新区科目二考场",
                        "高新区科创大道 188 号",
                        List.of("科目二"),
                        "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=1200&q=60",
                        "入口后依次进入倒车入库、侧方停车、坡道定点起步、直角转弯、曲线行驶。"
                ),
                new ExamSiteDto(
                        "site-b",
                        "城西综合考场",
                        "城西环路 99 号",
                        List.of("科目二", "科目三"),
                        "https://images.unsplash.com/photo-1549399542-7e82138c2d37?auto=format&fit=crop&w=1200&q=60",
                        "科目三路段包含会车、变更车道、靠边停车与夜间模拟灯光。"
                )
        );
    }
}
