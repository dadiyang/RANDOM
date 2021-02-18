package com.github.dadiyang.random;

import com.alibaba.fastjson.JSON;
import com.github.dadiyang.mockjs.Mock;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerRegistry;
import org.jeasy.random.randomizers.RegularExpressionRandomizer;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

/**
 * 随机工具，封装 ThreadLocalRandom 提供的常规类型随机生成方法和 EasyRandom 提供的对象和 List 的随机生成方法
 *
 * @author dadiyang
 * @since 2021/2/11
 */
public class RANDOM {
    /**
     * 这个正则不是完整的身份证校验逻辑，只生成前面符合条件的 17 位数字
     * * 生日部分取了 1930-2019，我们尽最大可能地让生成的身份证号符合校验规则
     * * 不包含最后一位
     */
    private static final String ID_CARD_REG_WITHOUT_VERIFY_CODE = "^(12|11|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65|71|81|82|91)\\d{4}(19[3-9][0-9]|20[01][0-9])((0[1-9])|(10|11|12))(([0-2][1-8])|10|20)\\d{3}$";
    /**
     * 身份证前 16 位，用于指定性别
     */
    private static final String ID_CARD_PRE_16_REG = "^(12|11|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65|71|81|82|91)\\d{4}(19[3-9][0-9]|20[01][0-9])((0[1-9])|(10|11|12))(([0-2][1-8])|10|20)\\d{2}$";
    /**
     * 身份证前6位正则
     */
    private static final String ID_CARD_PRE_6_REG = "^(12|11|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65|71|81|82|91)\\d{4}$";
    private static final String[] VERIFY_CODES = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
    private static final String[] WT = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2"};

    private static final EasyRandom easyRandom;
    private static final int EVENT_SYMBOL = 2;

    private RANDOM() {
        throw new UnsupportedOperationException("静态工具类不允许被实例化");
    }

    static {
        EasyRandomParameters param = new EasyRandomParameters();
        param.setStringLengthRange(new EasyRandomParameters.Range<>(5, 10));
        param.randomizerRegistry(new BigDecimalRegistry());
        param.setScanClasspathForConcreteTypes(true);
        param.setObjectPoolSize(100);
        param.setIgnoreRandomizationErrors(true);
        // 我们通过修改 EasyRandom 的源码，支持单个字段生成出错时忽略该字段
        param.setIgnoreRandomizationFieldErrors(true);
        param.setRandomizationDepth(10);
        easyRandom = new EasyRandom(param);
    }

    /**
     * 根据给定的类型生成一个随机的对象
     */
    public static <T> T nextObject(Class<T> clz) {
        return easyRandom.nextObject(clz);
    }

    /**
     * 根据给定的 mockjs 模板生成一个随机的对象
     */
    public static <T> T nextObjectWithMockJs(String tpl, Class<T> clz) {
        return Mock.mock(tpl, clz);
    }

    /**
     * 根据给定的类型和大小生成一个随机对象的列表
     */
    public static <T> List<T> nextList(Class<T> clz, int size) {
        return objects(clz, size).collect(Collectors.toList());
    }

    /**
     * 根据给定的 mockjs 模板生成一个随机对象的列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> nextListWithMockJs(String script, Class<T> clz, int size) {
        List<Map<String, Object>> list = Mock.mockList(script, size);
        return JSON.parseArray(JSON.toJSONString(list), clz);
    }

    /**
     * 根据给定的类型和大小生成一个随机对象的集合
     */
    public static <T> Set<T> nextSet(Class<T> clz, int size) {
        return objects(clz, size).collect(Collectors.toSet());
    }

    /**
     * 根据给定的 mockjs 模板生成一个随机对象的集合
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> nextSetWithMockJs(String script, Class<T> clz, int size) {
        return new HashSet<>(nextListWithMockJs(script, clz, size));
    }

    /**
     * 根据给定的类型和大小生成一个随机对象流
     */
    public static <T> Stream<T> objects(Class<T> clz, int size) {
        return easyRandom.objects(clz, size);
    }

    /**
     * 随机字符串，只包含字母和数字
     */
    public static String nextString(int length) {
        return new RandomStringGenerator.Builder().withinRange(new char[]{'a', 'z'}, new char[]{'A', 'Z'}, new char[]{'0', '9'}).build().generate(length);
    }

    /**
     * 根据正则表达式生成随机字符串
     *
     * @param reg 正则表达式
     */
    public static String nextStringWithReg(String reg) {
        return new RegularExpressionRandomizer(reg, nextLong()).getRandomValue();
    }

    /**
     * 根据正则表达式生成随机字符串
     *
     * @param reg  正则表达式
     * @param size 数量
     */
    public static List<String> nextStringsWithReg(String reg, int size) {
        if (size == 0) {
            return Collections.emptyList();
        }
        RegularExpressionRandomizer randomizer = new RegularExpressionRandomizer(reg, nextLong());
        List<String> rs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rs.add(randomizer.getRandomValue());
        }
        return rs;
    }

    /**
     * 随机生成一个身份证号
     */
    public static String nextIdCard() {
        String card = nextStringWithReg(ID_CARD_REG_WITHOUT_VERIFY_CODE).substring(0, 17);
        return card + computeVerifyCode(card);
    }

    /**
     * 随机生成一个身份证号
     *
     * @param age 指定该身份证号的年龄
     */
    public static String nextIdCardWithAge(int age) {
        String card = nextStringWithReg(ID_CARD_PRE_6_REG);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - age);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        card += new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
        card += nextInt(100, 999);
        return card + computeVerifyCode(card);
    }

    /**
     * 随机生成一个身份证号
     *
     * @param gender 指定性别，奇数男，偶数女
     */
    public static String nextIdCardWithGender(int gender) {
        String card = nextStringWithReg(ID_CARD_PRE_16_REG);
        card += (gender % 10);
        return card + computeVerifyCode(card);
    }

    /**
     * 随机生成一个男性的随机身份证号
     */
    public static String nextMaleIdCard() {
        return nextIdCardWithAgeAndGender(nextInt(0, 100), Math.abs(nextOddNumber()) % 10);
    }

    /**
     * 随机生成一个指定年龄的男性的随机身份证号
     */
    public static String nextMaleIdCard(int age) {
        return nextIdCardWithAgeAndGender(age, Math.abs(nextOddNumber()) % 10);
    }

    /**
     * 随机生成一个女性的随机身份证号
     */
    public static String nextFemaleIdCard() {
        return nextIdCardWithAgeAndGender(nextInt(0, 100), Math.abs(nextEvenNumber()) % 10);
    }

    /**
     * 随机生成一个指定年龄的女性的随机身份证号
     */
    public static String nextFemaleIdCard(int age) {
        return nextIdCardWithAgeAndGender(age, Math.abs(nextEvenNumber()) % 10);
    }

    /**
     * 随机生成一个身份证号
     *
     * @param age    指定年龄
     * @param gender 指定性别，奇数男，偶数女
     */
    public static String nextIdCardWithAgeAndGender(int age, int gender) {
        String card = nextStringWithReg(ID_CARD_PRE_6_REG);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - age);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        card += new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
        card += nextInt(10, 99);
        card += (gender % 10);
        return card + computeVerifyCode(card);
    }

    /**
     * 随机生成一批身份证号
     */
    public static List<String> nextIdCards(int size) {
        List<String> arr = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            arr.add(nextIdCard());
        }
        return arr;
    }

    /**
     * 判断第18位校验码是否正确
     * 第18位校验码的计算方式：
     * 1. 对前17位数字本体码加权求和
     * 公式为：S = Sum(Id[i]* WT[i]), i = 0, ... , 16
     * 其中Id表示第i个位置上的身份证号码数字值，Wi表示第i位置上的加权因子，其各位对应的值依次为： 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
     * 2. 用11对计算结果取模
     * Y = mod(S, 11)
     * 3. 根据模的值得到对应的校验码
     * 对应关系为：
     * Y值：     0  1  2  3  4  5  6  7  8  9  10
     * 校验码：  1  0  X  9  8  7  6  5  4  3   2
     */
    private static String computeVerifyCode(String id) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.parseInt(String.valueOf(id.charAt(i))) * Integer.parseInt(WT[i]);
        }
        int modValue = sum % 11;
        return VERIFY_CODES[modValue];
    }

    /**
     * 随机奇数
     */
    public static int nextOddNumber() {
        return nextEvenNumber() + 1;
    }

    /**
     * 随机偶数
     */
    public static int nextEvenNumber() {
        int i = nextInt();
        if (i % EVENT_SYMBOL == 0) {
            return i;
        } else {
            return i + 1;
        }
    }

    /**
     * 随机字符串列表，只包含字母和数字
     */
    public static List<String> nextStrings(int minLength, int maxLength, int size) {
        List<String> strings = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            strings.add(nextString(minLength, maxLength));
        }
        return strings;
    }

    public static String nextString(int minLength, int maxLength) {
        return new RandomStringGenerator.Builder().withinRange(new char[]{'a', 'z'}, new char[]{'A', 'Z'}, new char[]{'0', '9'}).build().generate(minLength, maxLength);
    }

    public static String nextStringIn(String... values) {
        if (values.length <= 0) {
            throw new IllegalArgumentException("values不能为空");
        }
        return values[nextInt(0, values.length)];
    }

    /**
     * 随机生成一个枚举
     */
    public static <E extends Enum<E>> E nextEnum(final Class<E> enumClass) {
        List<E> list = EnumUtils.getEnumList(enumClass);
        return EnumUtils.getEnum(enumClass, nextStringIn(list.stream().map(Enum::name).toArray(String[]::new)));
    }

    /**
     * 随机生成一个枚举值
     */
    public static <E extends Enum<E>> String nextEnumValue(final Class<E> enumClass) {
        List<E> list = EnumUtils.getEnumList(enumClass);
        return nextStringIn(list.stream().map(Enum::name).toArray(String[]::new));
    }

    /**
     * 随机生成一批枚举值
     */
    public static <E extends Enum<E>> List<String> nextEnumValues(final Class<E> enumClass, int size) {
        List<E> list = EnumUtils.getEnumList(enumClass);
        return nextStringsIn(size, list.stream().map(Enum::name).toArray(String[]::new));
    }

    public static List<String> nextStringsIn(int size, String... values) {
        if (values.length <= 0 || size <= 0) {
            throw new IllegalArgumentException("values不能为空且size必须大于0");
        }
        List<String> rs = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            rs.add(nextStringIn(values));
        }
        return rs;
    }

    /**
     * 随机汉字
     */
    public static String nextChineseString(int length) {
        return new RandomStringGenerator.Builder().withinRange(new char[]{0x4e00, 0x9fa5}).build().generate(length);
    }

    /**
     * 随机汉字
     */
    public static String nextChineseString(int minLength, int maxLength) {
        return new RandomStringGenerator.Builder().withinRange(new char[]{0x4e00, 0x9fa5}).build().generate(minLength, maxLength);
    }

    public static int nextInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    public static int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static int nextInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    public static long nextLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    public static long nextLong(long bound) {
        return ThreadLocalRandom.current().nextLong(bound);
    }

    public static long nextLong(long origin, long bound) {
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }

    public static double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static double nextDouble(double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    public static double nextDouble(double origin, double bound) {
        return ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static float nextFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    public static double nextGaussian() {
        return ThreadLocalRandom.current().nextGaussian();
    }

    public static IntStream ints(long streamSize) {
        return ThreadLocalRandom.current().ints(streamSize);
    }

    public static IntStream ints() {
        return ThreadLocalRandom.current().ints();
    }

    public static IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        return ThreadLocalRandom.current().ints(streamSize, randomNumberOrigin, randomNumberBound);
    }

    public static IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        return ThreadLocalRandom.current().ints(randomNumberOrigin, randomNumberBound);
    }

    public static LongStream longs(long streamSize) {
        return ThreadLocalRandom.current().longs(streamSize);
    }

    public static LongStream longs() {
        return ThreadLocalRandom.current().longs();
    }

    public static LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        return ThreadLocalRandom.current().longs(streamSize, randomNumberOrigin, randomNumberBound);
    }

    public static LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        return ThreadLocalRandom.current().longs(randomNumberOrigin, randomNumberBound);
    }

    public static DoubleStream doubles(long streamSize) {
        return ThreadLocalRandom.current().doubles(streamSize);
    }

    public static DoubleStream doubles() {
        return ThreadLocalRandom.current().doubles();
    }

    public static DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        return ThreadLocalRandom.current().doubles(streamSize, randomNumberOrigin, randomNumberBound);
    }

    public static DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        return ThreadLocalRandom.current().doubles(randomNumberOrigin, randomNumberBound);
    }

    public static void nextBytes(byte[] bytes) {
        ThreadLocalRandom.current().nextBytes(bytes);
    }

    private static class BigDecimalRandomizer implements Randomizer<BigDecimal> {

        @Override
        public BigDecimal getRandomValue() {
            return BigDecimal.valueOf(nextDouble(0L, Integer.MAX_VALUE))
                    .setScale(nextInt(0, 2), BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * 默认生成的 BigDecimal 实例精度过大，将会导致用于插入数据库时超过精度而报错，所以我们默认精度取为 0 到 2
     */
    private static class BigDecimalRegistry implements RandomizerRegistry {
        static final BigDecimalRandomizer bigDecimalRandomizer = new BigDecimalRandomizer();

        @Override
        public void init(EasyRandomParameters easyRandomParameters) {

        }

        @Override
        public Randomizer<?> getRandomizer(Field field) {
            if (field.getType() == BigDecimal.class) {
                return bigDecimalRandomizer;
            } else {
                return null;
            }
        }

        @Override
        public Randomizer<?> getRandomizer(Class<?> aClass) {
            if (aClass == BigDecimal.class) {
                return bigDecimalRandomizer;
            } else {
                return null;
            }
        }
    }
}
