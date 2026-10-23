package com.taihoang.social_backend.Controllers;

import com.taihoang.social_backend.Service.AdminStatisticsService;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.security.AuthenticatedUserDetails;
import com.taihoang.social_backend.dto.statistics.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/overview")
    public AdminOverviewStatisticsResponse getOverview(@AuthenticationPrincipal AuthenticatedUserDetails currentAdmin) {
        requireAdmin(currentAdmin);
        return adminStatisticsService.getOverview();
    }

    @GetMapping("/growth")
    public GrowthStatisticsResponse getGrowth(
            @AuthenticationPrincipal AuthenticatedUserDetails currentAdmin,
            @RequestParam StatisticMetric metric,
            @RequestParam(defaultValue = "SEVEN_DAYS") StatisticPeriod period) {
        requireAdmin(currentAdmin);
        return adminStatisticsService.getGrowth(metric, period);
    }

    @GetMapping("/top-posts")
    public List<TopPostStatisticsResponse> getTopPosts(
            @AuthenticationPrincipal AuthenticatedUserDetails currentAdmin,
            @RequestParam(defaultValue = "SEVEN_DAYS") StatisticPeriod period,
            @RequestParam(defaultValue = "10") int limit) {
        requireAdmin(currentAdmin);
        return adminStatisticsService.getTopPosts(period, limit);
    }

    @GetMapping("/active-users")
    public List<ActiveUserStatisticsResponse> getActiveUsers(
            @AuthenticationPrincipal AuthenticatedUserDetails currentAdmin,
            @RequestParam(defaultValue = "SEVEN_DAYS") StatisticPeriod period,
            @RequestParam(defaultValue = "10") int limit) {
        requireAdmin(currentAdmin);
        return adminStatisticsService.getActiveUsers(period, limit);
    }

    @GetMapping("/reports")
    public ReportStatisticsResponse getReports(@AuthenticationPrincipal AuthenticatedUserDetails currentAdmin) {
        requireAdmin(currentAdmin);
        return adminStatisticsService.getReportStatistics();
    }

    @GetMapping("/stories")
    public StoryStatisticsResponse getStories(@AuthenticationPrincipal AuthenticatedUserDetails currentAdmin) {
        requireAdmin(currentAdmin);
        return adminStatisticsService.getStoryStatistics();
    }

    @GetMapping("/chat")
    public ChatStatisticsResponse getChat(@AuthenticationPrincipal AuthenticatedUserDetails currentAdmin) {
        requireAdmin(currentAdmin);
        return adminStatisticsService.getChatStatistics();
    }

    private void requireAdmin(AuthenticatedUserDetails currentAdmin) {
        if (currentAdmin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
        }
        if (currentAdmin.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chi Admin moi co quyen xem thong ke");
        }
    }
}
