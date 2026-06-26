package cc.xiaowei.url_convert.common;

import cc.xiaowei.url_convert.properties.YearMonthShardingTableVals;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.*;

@Slf4j
public class IdTimeStampStandardShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    public static final YearMonthShardingTableVals staticYearMonthShardingTableVals = YearMonthShardingTableVals.staticYearMonthShardingTableVals;

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
         /*
        HACK:
        ShardingSphere requires target table to exist in availableTargetNames.
        Dynamically append future monthly tables here.
        availableTargetNames is from -> org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRouteEngine.routeTables(final ShardingTable shardingTable, final String routedDataSource,
                                             final ShardingStrategy tableShardingStrategy, final List<ShardingConditionValue> tableShardingValues)
        */
        availableTargetNames.addAll(staticYearMonthShardingTableVals.getShardingTables());
        log.info("doSharding availableTargetNames:{} shardingValue:{}", availableTargetNames, shardingValue);

        String shardedTableName = staticYearMonthShardingTableVals.getTableNameTemplate().replace(
                IDFactory.extractYearMonth(shardingValue.getValue()).format(staticYearMonthShardingTableVals.getDatePattern()), staticYearMonthShardingTableVals.getFMT()
        );

        if (!availableTargetNames.contains(shardedTableName)) {
            throw new AlgorithmInitializationException(this, "shardedTableNames not in availableTargetNames");
        }
        return shardedTableName;
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        /*
         no need range sharding
        */
        throw new AlgorithmInitializationException(this, "not supported range sharding");
    }

    @Override
    public String getType() {
        return "ID_DATETIME";
    }

}
