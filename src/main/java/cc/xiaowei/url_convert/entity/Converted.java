package cc.xiaowei.url_convert.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`converted-url`")
public class Converted {
    @TableId
    private Long id;
    private String original;
    private String domain;
    private Boolean deleted;
}
