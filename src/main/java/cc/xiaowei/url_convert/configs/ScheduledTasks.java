package cc.xiaowei.url_convert.configs;

import cc.xiaowei.url_convert.Application;
import cc.xiaowei.url_convert.properties.YearMonthShardingTableVals;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;


@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final YearMonthShardingTableVals yearMonthShardingTableVals;
    private final JdbcTemplate jdbcTemplate;


    private static final String CREATE_TABLE_SQL = """
             CREATE TABLE IF NOT EXISTS `%s`
                    (
                        id      BIGINT UNSIGNED    NOT NULL,
                        url     varchar(1024)      NOT NULL,
                        deleted BOOL DEFAULT false NULL,
                        CONSTRAINT url_map_pk PRIMARY KEY (id)
                    )
                        ENGINE = InnoDB
                        DEFAULT CHARSET = utf8mb4
                        COLLATE = utf8mb4_0900_ai_ci;
            """;

    @PostConstruct
    private void currentMonthServiceStart() {
        YearMonth serviceStartYearMonth = YearMonth.now(Application.ZONE_ID);
        shardingTableCreate(serviceStartYearMonth);
    }

    @Scheduled(cron = "0 0 4 1 *  ?", zone = "Asia/Shanghai")
    public void NextMonth() {
        YearMonth yearMonthNext = YearMonth.now(Application.ZONE_ID).plusMonths(1);
        shardingTableCreate(yearMonthNext);
    }


    private void shardingTableCreate(YearMonth yearMonth) {
        if (yearMonth.isAfter(yearMonthShardingTableVals.getUpperDate()) || yearMonth.isBefore(yearMonthShardingTableVals.getLowerDate())) {
            log.error("check sharding date config: lower-date < now < upper-date");
            return;
        }
        String tableName = yearMonthShardingTableVals.shardingTableNameFromFormattedYearMonth(yearMonth);
        try {
            jdbcTemplate.execute(CREATE_TABLE_SQL.formatted(tableName));
        } catch (Exception e) {
            //todo record and retry by mq
            log.error("create table {} fail", tableName, e);
        }
    }


}
