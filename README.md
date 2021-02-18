# 极简的万能随机数据生成器

支持生成随机数字、字符串、对象、列表等

只需使用 `RANDOM.nextXXX()` 即可生成任何需要的随机数据

# 使用

## 引用依赖

```xml
<dependency>
    <groupId>com.github.dadiyang</groupId>
    <artifactId>RANDOM</artifactId>
    <version>1.0.0</version>
    <!--  如果只在单测时使用，则 scope 设为 test  -->
    <scope>test</scope>
</dependency>
<!--如果需要使用 MockJs 模板生成并指定类型对象，则需要依赖 fastjson-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>${fastjson.version}</version>
</dependency>
```

例如：
```java
    // 生成指定范围内的随机整数
    int n = RANDOM.nextInt(0,100);
    // 生成指定长度的字符串
    String str = RANDOM.nextString(10);
    // 根据正则表达式生成随机字符串
    String cn = RANDOM.nextStringWithReg("[\u4e00-\u9fa5]");
    // 随机枚举值
    GenderEnum genderEnum = RANDOM.nextEnum(GenderEnum.class);
    // 生成随机对象
    City city = RANDOM.nextObject(City.class);
    // 生成随机对象构成的列表
    List<City> city = RANDOM.nextList(City.class,10);
    // 生成随机身份证号
    String idCard = RANDOM.nextIdCard();
    // 生成指定年龄的男性身份证号
    String maleIdCard = RANDOM.nextMaleIdCard(18);
    
    // 根据指定模板生成随机的对象
    String tpl = "{'name': '@city','code|1-1000': 1}";
    City cityByMockJs = RANDOM.nextObjectWithMockJs(tpl, City.class);
``` 

# 底层依赖

* 随机数值使用 ThreadLocalRandom.current().nextXXX() 
* 随机对象生成采用 [easy-random](https://github.com/j-easy/easy-random) 生成
* MockJs 模板采用 MockJs4j 生成
