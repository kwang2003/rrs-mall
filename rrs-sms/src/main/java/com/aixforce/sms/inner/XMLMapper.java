package com.aixforce.sms.inner;

import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-04-02
 */
public class XMLMapper {
    private final static XStream xstream = new XStream();

    static {
        MapEntryConverter converter = new MapEntryConverter();

        xstream.aliasType("resRoot", Map.class);
        xstream.registerConverter(converter);

        xstream.aliasType("response", SmsResponse.class);
        xstream.aliasType("message", SmsMessage.class);
        xstream.addImplicitCollection(SmsResponse.class, "messages");
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromXML(String xml) {
        return (T) xstream.fromXML(xml);
    }

    public static class MapEntryConverter implements Converter {
        public boolean canConvert(Class clazz) {
            return Map.class.isAssignableFrom(clazz);
        }

        @SuppressWarnings("unchecked")
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            Map<String, String> map = (Map<String, String>) value;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writer.startNode(entry.getKey());
                writer.setValue(entry.getValue());
                writer.endNode();
            }
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

            while (reader.hasMoreChildren()) {
                reader.moveDown();
                builder.put(reader.getNodeName(), reader.getValue());
                reader.moveUp();
            }
            return builder.build();
        }
    }
}
