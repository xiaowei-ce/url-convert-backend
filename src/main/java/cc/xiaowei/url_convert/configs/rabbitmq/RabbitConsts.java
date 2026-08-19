package cc.xiaowei.url_convert.configs.rabbitmq;


public class RabbitConsts {

    public static class URLMAP {

        public static final String TOPIC_EXCHANGE = "urlmap.topic";

        public static final String FINISHED_ROUTING_KEY = "urlmap.finished";

        public static final String DEL_ROUTING_KEY = "urlmap.delete";
    }
}
