package cc.xiaowei.url_convert.configs;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//@Configuration
@RequiredArgsConstructor
public class BloomFilterConfig {

    private final RedissonClient redissonClient;

    @Bean
    public RBloomFilter<Long> redissonBloomFilter() {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("bloomFilter");
        bloomFilter.tryInit(50000, 0.001);
        return bloomFilter;
    }
}
