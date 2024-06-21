package cn.matthew.types.common;

public class Constants {

    public final static String SPLIT = ",";
    public final static String COLON = ":";
    public final static String SPACE = " ";
    public final static String UNDERLINE = "_";

    public final static String TREE_END = "END";

    public static class RedisKey {
        // 活动对应的所有SKU
        public static final String ACTIVITY_SKU_KEY = "big_market_activity_sku";
        //sku库存
        public static final String ACTIVITY_SKU_STOCK_COUNT_KEY = "big_market_activity_sku_stock_count_key";
        public static String ACTIVITY_KEY = "big_market_activity_key_";
        public static String ACTIVITY_COUNT_KEY = "big_market_activity_count_key_";
        public static String ACTIVITY_SKU_STOCK_KEY = "big_market_activity_sku_stock_key_";
        public static final String STRATEGY_AWARD_COUNT_QUERY_KEY = "strategy_award_count_query_key";
        public static String STRATEGY_KEY = "big_market_strategy_key";
        public static String STRATEGY_AWARD_KEY = "big_market_strategy_award_key_";
        public static String STRATEGY_RATE_TABLE_KEY = "big_market_strategy_rate_table_key_";
        public static String STRATEGY_RATE_RANGE_KEY = "big_market_strategy_rate_range_key_";
        public static String STRATEGY_AWARD_COUNT_KEY = "big_market_strategy_award_count_key_";
        public static String RULE_TREE_VO_KEY = "rule_tree_vo_key_";
        public static String RULE_WEIGHT = "rule_weight";

    }

}
