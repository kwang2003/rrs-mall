package com.aixforce.sms.haier;

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
public class XmlTranslator {
    private final static XStream xstream = new XStream();

    static {
        xstream.autodetectAnnotations(true);
        xstream.setMode(XStream.NO_REFERENCES);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromXML(String xml) {
        return (T) xstream.fromXML(xml);
    }

    public static <T> T fromXML(String xml, Class preProcessClass) {
        xstream.processAnnotations(preProcessClass);
        return fromXML(xml);
    }

    public static <T> String toXML(T t) {
        return xstream.toXML(t);
    }


    @SuppressWarnings("unused")
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
