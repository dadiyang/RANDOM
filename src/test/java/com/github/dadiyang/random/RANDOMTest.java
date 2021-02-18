package com.github.dadiyang.random;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RANDOMTest {
    @Test
    void nextObject() {
        City city = RANDOM.nextObject(City.class);
        System.out.println(city);
        assertNotNull(city);
        assertNotNull(city.getCode());
        assertNotNull(city.getName());
    }

    @Test
    void nextIdCard() {
        String idCard = RANDOM.nextIdCard();
        System.out.println(idCard);
    }

    @Test
    void nextEventNumber() {
        for (int i = 0; i < 20; i++) {
            int rs = RANDOM.nextEvenNumber();
            System.out.println(rs);
            assertEquals(0, rs % 2);
        }
    }

    @Test
    void nextOddNumber() {
        for (int i = 0; i < 20; i++) {
            int rs = RANDOM.nextOddNumber();
            System.out.println(rs);
            assertNotEquals(0, rs % 2);
        }
    }

    @Test
    void nextStringWithReg() {
        String cn = RANDOM.nextStringWithReg("[\u4e00-\u9fa5]");
        System.out.println(cn);
    }

    @Test
    void nextEnumValue() {
        String gender = RANDOM.nextEnumValue(GenderEnum.class);
        GenderEnum genderEnum = GenderEnum.valueOf(gender);
        assertNotNull(genderEnum);
    }

    @Test
    void nextEnum() {
        GenderEnum genderEnum = RANDOM.nextEnum(GenderEnum.class);
        assertNotNull(genderEnum);
    }

    @Test
    void nextObjectWithMockJs() {
        String tpl = "{'name': '@city','code|1-1000': 1}";
        City city = RANDOM.nextObjectWithMockJs(tpl, City.class);
        System.out.println(JSON.toJSONString(city, true));
        assertNotNull(city);
        assertNotNull(city.getName());
        assertNotNull(city.getCode());
    }
}