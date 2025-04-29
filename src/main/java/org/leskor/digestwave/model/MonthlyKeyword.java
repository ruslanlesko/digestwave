package org.leskor.digestwave.model;

import java.io.Serializable;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("monthlykeyword")
public class MonthlyKeyword {

    @PrimaryKeyColumn(name = "monthyear", type = PrimaryKeyType.PARTITIONED)
    private String monthYear;

    @PrimaryKeyColumn(name = "keyword", type = PrimaryKeyType.CLUSTERED)
    private String keyword;

    @Column("count")
    @CassandraType(type = CassandraType.Name.COUNTER)
    private long count;

    public MonthlyKeyword() {
        // Required empty constructor
    }

    public MonthlyKeyword(Key key, long count) {
        this.monthYear = key.getMonthYear();
        this.keyword = key.getKeyword();
        this.count = count;
    }

    public static MonthlyKeyword of(String monthYear, String keyword, long count) {
        return new MonthlyKeyword(new Key(monthYear, keyword), count);
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Key getKey() {
        return new Key(monthYear, keyword);
    }

    @PrimaryKeyClass
    public static class Key implements Serializable {

        @PrimaryKeyColumn(name = "monthyear", type = PrimaryKeyType.PARTITIONED)
        private String monthYear;

        @PrimaryKeyColumn(name = "keyword", type = PrimaryKeyType.CLUSTERED)
        private String keyword;

        public Key() {
            // Required empty constructor
        }

        public Key(String monthYear, String keyword) {
            this.monthYear = monthYear;
            this.keyword = keyword;
        }

        public String getMonthYear() {
            return monthYear;
        }

        public void setMonthYear(String monthYear) {
            this.monthYear = monthYear;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!monthYear.equals(key.monthYear)) return false;
            return keyword.equals(key.keyword);
        }

        @Override
        public int hashCode() {
            int result = monthYear.hashCode();
            result = 31 * result + keyword.hashCode();
            return result;
        }
    }
}
