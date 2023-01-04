package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Locale;
import java.util.Objects;

/**
 * Filters with {@code path not like %pattern%} where-clause and ignores pattern case
 */
public class NotLikeIgnoreCase<T> extends NotLike<T> implements LocaleAware {

    private static final long serialVersionUID = 1L;

    private Locale locale;

    public NotLikeIgnoreCase(QueryContext queryContext, String path, String... args) {
        super(queryContext, path, args);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return builder.not(builder.like(builder.upper(this.<String> path(root)), pattern.toUpperCase(locale)));
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(locale);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotLikeIgnoreCase other = (NotLikeIgnoreCase) obj;
        return Objects.equals(locale, other.locale);
    }

    @Override
    public String toString() {
        return "NotLikeIgnoreCase [locale=" + locale + ", pattern=" + pattern + ", path=" + path + "]";
    }
}
