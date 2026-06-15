package cc.xiaowei.url_convert.mapper;

import cc.xiaowei.url_convert.entity.Converted;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConvertedMapper extends BaseMapper<Converted> {
    Integer insert_(@Param("converted") Converted converted);

    Converted select_(@Param("id") Long id);

    Integer delete_(@Param("id") Long id);
}
