package com.example.onlybuns.repository;

import com.example.onlybuns.model.Comment;
import com.example.onlybuns.model.Post;
import com.example.onlybuns.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Post, Long> {

    /**
     * Count posts grouped by a time period (e.g. 'day', 'month', 'year')
     * Returns a list of [truncatedDate, count]
     */
    @Query(
            value = """
      SELECT
        date_trunc(:period, p.created_at)::timestamp AS period_start,
        COUNT(*) AS cnt
      FROM post p
      WHERE p.created_at >= :since
      GROUP BY period_start
      ORDER BY period_start
      """,
            nativeQuery = true
    )
    List<Object[]> countPostsGroupedBy(
            @Param("period") String period,
            @Param("since") ZonedDateTime since
    );

    /**
     * Count comments grouped by a time period (e.g. 'day', 'month', 'year')
     * Returns a list of [truncatedDate, count]
     *
     * Alternative for casting to ::timestamp in both comment and post is to Unbox an instand and then convert that to a
     * LocalDate in service
     *
     */
    @Query(
            value = """
      SELECT
        date_trunc(:period, c.creation_date)::timestamp AS period_start,
        COUNT(*) AS cnt
      FROM comment c
      WHERE c.creation_date >= :since
      GROUP BY period_start
      ORDER BY period_start
      """,
            nativeQuery = true
    )
    List<Object[]> countCommentsGroupedBy(
            @Param("period") String period,
            @Param("since") ZonedDateTime since
    );

    /**
     * User activity breakdown: returns counts of users who have:
     *  - made at least one post
     *  *- made comments only
     *  - made neither posts nor comments
     * Returns a single row [postedCount, commentedOnlyCount, noneCount]
     */
    @Query(
            "SELECT " +
                    "SUM(CASE WHEN (SELECT COUNT(p2) FROM Post p2 WHERE p2.user.id = u.id) > 0 THEN 1 ELSE 0 END), " +
                    "SUM(CASE WHEN (SELECT COUNT(p2) FROM Post p2 WHERE p2.user.id = u.id) = 0 " +
                    "          AND (SELECT COUNT(c2) FROM Comment c2 WHERE c2.user.id = u.id) > 0 THEN 1 ELSE 0 END), " +
                    "SUM(CASE WHEN (SELECT COUNT(p2) FROM Post p2 WHERE p2.user.id = u.id) = 0 " +
                    "          AND (SELECT COUNT(c2) FROM Comment c2 WHERE c2.user.id = u.id) = 0 THEN 1 ELSE 0 END) " +
                    "FROM UserInfo u"
    )
    List<Object[]> userActivityBreakdown();

}
