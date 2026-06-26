package cc.xiaowei.url_convert.properties;

import cc.xiaowei.url_convert.Application;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;


@Component
@ConfigurationProperties(prefix = "sharding-tables-vals")
@Slf4j
@Getter
public class YearMonthShardingTableVals {

    @Setter private String tableNameTemplate;
    private DateTimeFormatter datePattern;
    private YearMonth lowerDate;
    private YearMonth upperDate;

    private final Collection<String> shardingTables = new LinkedList<>();
    private final String FMT = "<{}>";
    public static YearMonthShardingTableVals staticYearMonthShardingTableVals;

    @PostConstruct
    private void init(){
        YearMonth lowerDate = this.getLowerDate();
        while (!lowerDate.isAfter(upperDate)){
            shardingTables.add(tableNameTemplate.replace(FMT, lowerDate.format(datePattern)));
            lowerDate = lowerDate.plusMonths(1L);
        }

        staticYearMonthShardingTableVals = this;
        log.info("init shardingTables:{}",shardingTables);
    }


    public void setDatePattern(String datePattern){
        this.datePattern = DateTimeFormatter.ofPattern(datePattern);
    }
    public void setLowerDate(String lowerDate) {
        this.lowerDate = YearMonth.parse(lowerDate, datePattern);
    }
    public void setUpperDate(String upperDate){
        this.upperDate = YearMonth.parse(upperDate, datePattern);
    }
}
