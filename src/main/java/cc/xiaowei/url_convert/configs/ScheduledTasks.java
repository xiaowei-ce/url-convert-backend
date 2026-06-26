package cc.xiaowei.url_convert.configs;

import cc.xiaowei.url_convert.Application;
import cc.xiaowei.url_convert.exception.BizException;
import cc.xiaowei.url_convert.mapper.URLMapMapper;
import cc.xiaowei.url_convert.properties.YearMonthShardingTableVals;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;


@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final URLMapMapper uRLMapMapper;
    private final YearMonthShardingTableVals yearMonthShardingTableVals;


    @Scheduled(cron = "0 4 1 * * ?", zone = "Asia/Shanghai")
    public void shardingTablesCreate() {

        //todo mq

        YearMonth yearMonthNow = YearMonth.now(Application.ZONE_ID);
        if (yearMonthNow.isAfter(yearMonthShardingTableVals.getUpperDate()) || yearMonthNow.isBefore(yearMonthShardingTableVals.getLowerDate())) {
            throw new UnsupportedOperationException("check sharding date config: lower-date < now < upper-date");
        }

        uRLMapMapper.createShardingTable(
                yearMonthShardingTableVals.getTableNameTemplate().replace(
                        yearMonthShardingTableVals.getFMT(), yearMonthNow.format(yearMonthShardingTableVals.getDatePattern())
                )
        );
    }
}
