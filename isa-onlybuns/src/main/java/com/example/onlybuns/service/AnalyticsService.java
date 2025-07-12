package com.example.onlybuns.service;

import com.example.onlybuns.dtos.AnalyticsDto;
import com.example.onlybuns.dtos.PostsCommentsDto;
import com.example.onlybuns.dtos.UserActivityDto;
import com.example.onlybuns.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;


@Service
public class AnalyticsService {
    @Autowired
    AnalyticsRepository analyticsRepo;

    public PostsCommentsDto getPostsComments(String period) {
        // 1) pull raw rows: List<Object[]> where each row is [Timestamp, count]
        ZonedDateTime since = thresholdFor(period);
        var postRows    = analyticsRepo.countPostsGroupedBy(period, since);
        var commentRows = analyticsRepo.countCommentsGroupedBy(period, since);

        List<String> dates = postRows.stream()
                .map(r -> ((Timestamp)r[0]).toLocalDateTime().toLocalDate().toString())
                .toList();

        List<Long> postCounts = postRows.stream()
                .map(r -> ((Number)r[1]).longValue())
                .toList();

        List<Long> commentCounts = commentRows.stream()
                .map(r -> ((Number)r[1]).longValue())
                .toList();

        return new PostsCommentsDto(dates, postCounts, commentCounts);
    }

    public UserActivityDto getUserActivity() {
        List<Object[]> rows = analyticsRepo.userActivityBreakdown();
        if(rows.isEmpty()) {
            return new UserActivityDto(0,0,0);
        }

        Object[] breakdown = rows.get(0);
        double posted  = ((Number)breakdown[0]).doubleValue();
        double comment = ((Number)breakdown[1]).doubleValue();
        double inactive= ((Number)breakdown[2]).doubleValue();
        double total   = posted + comment + inactive;

        // convert to percentages
        return new UserActivityDto(
                100 * (posted  / total),
                100 * (comment / total),
                100 * (inactive/ total)
        );
    }

    private ZonedDateTime thresholdFor(String period) {
        return switch (period) {
            case "week"  -> ZonedDateTime.now(ZoneOffset.UTC).minusWeeks(1);
            case "month" -> ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1);
            case "year"  -> ZonedDateTime.now(ZoneOffset.UTC).minusYears(1);
            default      -> ZonedDateTime.now(ZoneOffset.UTC).minusWeeks(1);
        };
    }

    public AnalyticsDto getAnalytics() {
        AnalyticsDto dto = new AnalyticsDto();

        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime nowUtc = ZonedDateTime.of(now, ZoneOffset.UTC);
        dto.postsByPeriod    = groupToMap( analyticsRepo.countPostsGroupedBy("week", nowUtc.minusWeeks(1)) );
        dto.commentsByPeriod = groupToMap( analyticsRepo.countCommentsGroupedBy("week", nowUtc.minusWeeks(1)) );
        // repeat for month / year if you want separate endpoints or pack them all

        List<Object[]> rows = analyticsRepo.userActivityBreakdown();
        if(rows.isEmpty()) {
            return new AnalyticsDto();
        }
        Object[] breakdown = rows.get(0);
        dto.userActivityBreakdown = Map.of(
                "posted",        ((Number)breakdown[0]).intValue(),
                "commentedOnly", ((Number)breakdown[1]).intValue(),
                "inactive",      ((Number)breakdown[2]).intValue()
        );

        return dto;
    }

    private Map<String, Long> groupToMap(List<Object[]> rows) {
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> ((Timestamp)row[0]).toLocalDateTime().toLocalDate().toString(),
                        row -> ((Number)row[1]).longValue(),
                        (a,b)->a, LinkedHashMap::new
                ));
    }
}

