package cc.xiaowei.url_convert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("url_map")
public class URLMap { //todo
    @TableId(type = IdType.NONE)
    Long id;
    String url;
    Boolean deleted;
}
/*

-- `url-convert`.url_map 定义

CREATE TABLE `url-convert`.url_map (
	id BIGINT UNSIGNED NOT NULL,
	url varchar(1024) NOT NULL,
	deleted BOOL DEFAULT false NULL,
	CONSTRAINT url_map_pk PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;

* */