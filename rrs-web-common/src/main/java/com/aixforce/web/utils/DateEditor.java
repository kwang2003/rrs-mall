package com.aixforce.web.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.beans.PropertyEditorSupport;
import java.util.Date;

import static com.aixforce.common.utils.Arguments.notNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-09-04 11:30 AM  <br>
 * Author: xiao
 */
@NoArgsConstructor
@AllArgsConstructor
public class DateEditor extends PropertyEditorSupport {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");


    @Setter
    private DateTimeFormatter dft;


    /**
     * Parse the Date from the given text, using the specified DateFormat.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (isEmpty(text)) {
            // Treat empty String as null value.
            setValue(null);
        } else {
            try {
                if(notNull(dft))
                    setValue(this.dft.parseDateTime(text).toDate());
                else {
                    if(text.contains(":"))
                        setValue(TIME_FORMATTER.parseDateTime(text).toDate());
                    else
                        setValue(DATE_FORMATTER.parseDateTime(text).toDate());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("convert.date.fail");
            }
        }
    }

    /**
     * Format the Date as String, using the specified DateFormat.
     */
    @Override
    public String getAsText() {
        Date value = (Date) getValue();
        DateTimeFormatter dateFormat = this.dft;
        if(dateFormat == null)
            dateFormat = DATE_FORMATTER;
        return (value != null ? dateFormat.print(new DateTime(value)) : "");
    }
}
