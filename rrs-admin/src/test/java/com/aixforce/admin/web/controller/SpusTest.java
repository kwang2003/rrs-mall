package com.aixforce.admin.web.controller;

import com.aixforce.category.dto.AttributeDto;
import com.aixforce.category.model.Spu;
import com.aixforce.common.utils.JsonMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-02
 */
public class SpusTest {
    @Test
    public void testToJson() throws Exception {
        Spu spu = new Spu();
        spu.setName("spu1");
        spu.setCategoryId(11L);
        //spu.setId(1L);


        Spus.RichSpu richSpu = new Spus.RichSpu();
        richSpu.setSpu(spu);
        richSpu.setAttributes(ImmutableList.of(new AttributeDto(5L, 1, "14", false, ""),
                new AttributeDto(4L, 0, "anything", false, ""), new AttributeDto(3L, 1, null, true, ""), new AttributeDto(2L, 1, null, true, "")));

        System.out.println(JsonMapper.nonEmptyMapper().toJson(richSpu));


    }

    @Test
    public void testFromJson() throws Exception {
        String data = "{\"spu\":{\"categoryId\":11,\"name\":\"spu1\"}," +
                "\"attributes\":[{\"keyId\":2,\"type\":1,\"value\":\"22\",\"isSku\":false}," +
                "{\"keyId\":3,\"type\":0,\"value\":\"anything\",\"isSku\":false}," +
                "{\"keyId\":4,\"type\":1,\"isSku\":true},{\"keyId\":5,\"type\":1,\"isSku\":true}]}";
        Spus.RichSpu richSpu = JsonMapper.nonEmptyMapper().fromJson(data, Spus.RichSpu.class);
        System.out.println(richSpu.getSpu());
    }

    @Test
    public void testShow() throws Exception {
        Spu spu = new Spu();
        spu.setName("spu1");
        spu.setCategoryId(11L);
        spu.setId(1L);


    }
}
