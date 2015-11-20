package net.michalfoksa.mqtt2influxdb.dto;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * Representation of a InfluxDB database Point.
 *
 * @author stefan.majer [at] gmail.com
 *
 */
public class Point {
    private String measurement;
    private Map<String, String> tags;
    /**
     * The time stored in nanos. FIXME ensure this
     */
    private Long time;
    private TimeUnit precision = TimeUnit.NANOSECONDS;
    private Map<String, Object> fields;

    Point() {
    }

    /**
     * Create a new Point Build build to create a new Point in a fluent manner-
     *
     * @param measurement
     *            the name of the measurement.
     * @return the Builder to be able to add further Builder calls.
     */

    public static Builder measurement(final String measurement) {
        return new Builder(measurement);
    }

    /**
     * Builder for a new Point.
     *
     * @author stefan.majer [at] gmail.com
     *
     */
    public static class Builder {

        private static Logger log = LoggerFactory.getLogger(Builder.class) ;

        private final String measurement;
        private final Map<String, String> tags = Maps.newTreeMap(Ordering.natural());
        private Long time;
        private TimeUnit precision = TimeUnit.NANOSECONDS;
        private final Map<String, Object> fields = Maps.newTreeMap(Ordering.natural());

        /**
         * @param measurement
         */
        Builder(final String measurement) {
            this.measurement = measurement;
        }

        /**
         * Add a tag to this point.
         *
         * @param tagName
         *            the tag name
         * @param value
         *            the tag value
         * @return the Builder instance.
         */
        public Builder tag(final String tagName, final String value) {
            tags.put(tagName, value);
            return this;
        }

        /**
         * Add a Map of tags to add to this point.
         *
         * @param tagsToAdd
         *            the Map of tags to add
         * @return the Builder instance.
         */
        public Builder tag(final Map<String, String> tagsToAdd) {
            tags.putAll(tagsToAdd);
            return this;
        }

        /**
         * Add a field to this point.
         *
         * @param field
         *            the field name
         * @param value
         *            the value of this field
         * @return the Builder instance.
         */
        public Builder field(final String field, final Object value) {
            fields.put(field, value);
            return this;
        }

        /**
         * Add a Map of fields to this point.
         *
         * @param fieldsToAdd
         *            the fields to add
         * @return the Builder instance.
         */
        public Builder fields(final Map<String, Object> fieldsToAdd) {
            fields.putAll(fieldsToAdd);
            return this;
        }

        /**
         * Add a time to this point
         *
         * @param precisionToSet
         * @param timeToSet
         * @return the Builder instance.
         */
        public Builder time(final long timeToSet, final TimeUnit precisionToSet) {
            Preconditions.checkNotNull(precisionToSet, "Precision must be not null!");
            time = timeToSet;
            precision = precisionToSet;
            return this;
        }

        /**
         * Create a new Point.
         *
         * @return the newly created Point.
         */
        public Point build() {
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(measurement),
                    "Point name must not be null or empty.");
            Point point = new Point();
            point.setFields(fields);
            point.setMeasurement(measurement);
            if (time != null) {
                point.setTime(time);
                point.setPrecision(precision);
            } else {
                point.setTime(System.currentTimeMillis());
                point.setPrecision(TimeUnit.MILLISECONDS);
            }
            point.setTags(tags);

            log.debug(point.toString());
            return point;
        }
    }

    /**
     * @param measurement
     *            the measurement to set
     */
    void setMeasurement(final String measurement) {
        this.measurement = measurement;
    }

    /***
     *
     * @return measurement
     */
    public String getMeasurement() {
        return measurement;
    }

    /**
     * @param time
     *            the time to set
     */
    void setTime(final Long time) {
        this.time = time;
    }

    /**
     * @return time
     */
    public Long getTime() {
        return time;
    }

    /**
     * @param tags
     *            the tags to set
     */
    void setTags(final Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param precision
     *            the precision to set
     */
    void setPrecision(final TimeUnit precision) {
        this.precision = precision;
    }

    /***
     * @return precision
     */
    public TimeUnit getPrecision() {
        return precision;
    }

    /**
     * @param fields
     *            the fields to set
     */
    void setFields(final Map<String, Object> fields) {
        this.fields = fields;
    }

    /***
     * @return fields
     */
    public Map<String, Object> getFields() {
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Point [name=");
        builder.append(measurement);
        builder.append(", time=");
        builder.append(time);
        builder.append(", tags=");
        builder.append(tags);
        builder.append(", precision=");
        builder.append(precision);
        builder.append(", fields=");
        builder.append(fields);
        builder.append("]");
        return builder.toString();
    }
}