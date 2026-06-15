package cc.xiaowei.url_convert.controller;

import cc.xiaowei.url_convert.common.Cast;
import cc.xiaowei.url_convert.common.Convertor;
import cc.xiaowei.url_convert.configs.rabbitmq.consts;
import cc.xiaowei.url_convert.entity.Converted;
import cc.xiaowei.url_convert.entity.Result;
import cc.xiaowei.url_convert.exception.BizException;
import cc.xiaowei.url_convert.mapper.ConvertedMapper;
import com.google.common.primitives.Longs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@RestController
@Slf4j
public class RedirectController {


    private final RedisTemplate<String, Object> redisTemplate;
    private final ConvertedMapper convertedMapper;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping("/{uri}")
    public RedirectView redirect(@PathVariable String uri) {

        Long id = Longs.tryParse(Convertor.revert(uri));
        if (id == null) {
            BizException.throw_("Incorrect short link because parsing failed");
        }

        String url = Cast.cast(redisTemplate.opsForValue().get("uri_id:" + id), String.class);
        if (url == null) {
            Converted selected = convertedMapper.selectById(id);
            if(selected == null){
                redisTemplate.opsForValue().set("uri_id:" + id, "", 10, TimeUnit.MINUTES);
                BizException.throw_("uri not exist");
            }else {
                redisTemplate.opsForValue().set("uri_id:" + id, selected.getOriginal(), 24, TimeUnit.HOURS);
                url = selected.getOriginal();
            }
        }
        if ("".equals(url)){
            BizException.throw_("uri not exist");
        }

        return new RedirectView(url);
    }


    @DeleteMapping("/del/{uri}")
    public Result<String> delete(@PathVariable String uri) {
        Long id = Longs.tryParse(Convertor.revert(uri));
        if (id == null) {
            BizException.throw_("Incorrect short link because parsing failed");
        }
        redisTemplate.delete("uri_ids:" + id);
        rabbitTemplate.convertAndSend(consts.TOPIC_EXCHANGE,consts.DELETE_ROUTING_KEY,id);
        return Result.success(null);
    }
}
