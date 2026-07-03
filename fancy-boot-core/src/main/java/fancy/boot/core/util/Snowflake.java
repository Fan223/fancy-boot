package fancy.boot.core.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花 ID.
 *
 * @param dataCenterId 数据中心 ID
 * @param workerId     工作节点 ID
 * @author Fan
 */
public record Snowflake(long dataCenterId, long workerId) {

    /**
     * 数据中心 ID 占用的位数.
     */
    private static final long DATA_CENTER_ID_BITS = 5;

    /**
     * 最大数据中心 ID. 数据中心 ID 占用 5 位, 则最大值为 31.
     * <ul>
     *     <li> -1 << 5 = -32, 原码为: 1111111111111111111111111111111111111111111111111111111111100000 </li>
     *     <li> 取反后为: 0000000000000000000000000000000000000000000000000000000000011111 </li>
     * </ul>
     */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * 工作节点 ID 占用的位数.
     */
    private static final long WORKER_ID_BITS = 5;

    /**
     * 最大工作节点 ID. 工作节点 ID 占用 5 位, 则最大值为 31.
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 序列号占用的位数.
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 序列掩码(最低 12 位为 1, 高位都为 0), 最大值为 4095. 主要用于与自增后的序列号进行位与, 如果值为 0, 则代表自增后的序列号超过了 4095.
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * {@code workerId} 需要左移的位数, 为 12 位, 也就是右边为 序列号.
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * {@code dataCenterId} 需要左移的位数, 为 12+5 位, 也就是右边为 工作节点ID+序列号.
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * {@code timestamp} 需要左移的位数, 为 12+5+5 位, 也就是右边为 数据中心ID+工作节点ID+序列号.
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 初始时间.
     */
    private static final long INITIAL_EPOCH = 1765879200000L;

    /**
     * 记录最后使用的毫秒时间戳, 主要用于判断是否同一毫秒, 以及用于服务器时钟回拨判断.
     */
    private static final AtomicLong LAST_TIMESTAMP = new AtomicLong(-1L);

    /**
     * 同一毫秒内的最新序列号, 最大值为(2^12 - 1 = 4095).
     */
    private static final AtomicLong SEQUENCE = new AtomicLong(0L);

    /**
     * 时钟错误信息.
     */
    private static final String CLOCK_ERROR_MESSAGE = "可能出现服务器时钟回拨问题, 请检查服务器时间. 当前服务器时间戳: %d, 上一次使用时间戳: %d";

    /**
     * 返回 {@link Snowflake} 实例, 自动生成 {@code dataCenterId} 和 {@code workerId}.
     */
    public static Snowflake of() {
        long dataCenterId = IdUtils.generateDataCenterId(MAX_DATA_CENTER_ID);
        return of(dataCenterId, IdUtils.generateWorkerId(dataCenterId, MAX_WORKER_ID));
    }

    /**
     * 返回使用指定 {@code dataCenterId} 和 {@code workerId} 创建的 {@link Snowflake} 实例.
     *
     * @param dataCenterId 数据中心 ID
     * @param workerId     工作节点 ID
     * @return {@link Snowflake}
     */
    public static Snowflake of(long dataCenterId, long workerId) {
        return new Snowflake(dataCenterId, workerId);
    }

    /**
     * 使用雪花算法生成 {@code nextId}, 这里使用 {@link AtomicLong} 原子类代替锁来保证线程安全.
     *
     * @return {@code long}
     */
    public long nextId() {
        // 如果当前时间戳小于上次使用的时间戳, 则表明操作系统时间已经倒退
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < LAST_TIMESTAMP.get()) {
            throw new IllegalStateException(String.format(CLOCK_ERROR_MESSAGE, currentTimestamp, LAST_TIMESTAMP.get()));
        }

        // 如果当前时间戳等于上次使用的时间戳, 则表示同一毫秒内, 序列号自增
        if (currentTimestamp == LAST_TIMESTAMP.get()) {
            // 序列号每次加 1, 然后和序列掩码进行位与, 使用原子操作保证线程安全
            final long seq = (SEQUENCE.getAndIncrement() + 1) & SEQUENCE_MASK;
            // 为 0 则表示序列号大于 4095, 当前毫秒使用的序列号已达到最大个数, 使用新的时间戳
            if (0 == seq) {
                currentTimestamp = tilNextMillis(LAST_TIMESTAMP.get());
            }
            SEQUENCE.set(seq);
        } else {
            // 不在同一毫秒内, 则序列号重新从 0 开始
            SEQUENCE.set(0L);
        }

        // 记录最后一次使用的毫秒时间戳
        LAST_TIMESTAMP.set(currentTimestamp);
        // 核心算法, 将不同部分的数值移动到指定的位置, 然后进行位或操作生成唯一ID
        return ((currentTimestamp - INITIAL_EPOCH) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | SEQUENCE.get();
    }

    /**
     * 等待下一毫秒时间戳.
     *
     * @param timestamp 时间戳
     * @return {@code long}
     */
    private long tilNextMillis(long timestamp) {
        // 循环, 直到操作系统时间戳更改
        long currentTimestamp;
        do {
            currentTimestamp = System.currentTimeMillis();
        } while (currentTimestamp == timestamp);

        // 如果当前时间戳小于上次使用的时间戳, 则表明操作系统时间已经倒退
        if (currentTimestamp < timestamp) {
            throw new IllegalStateException(String.format(CLOCK_ERROR_MESSAGE, currentTimestamp, timestamp));
        }
        return currentTimestamp;
    }
}
