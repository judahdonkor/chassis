package org.judahdonkor.chassis;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import lombok.NonNull;

public class Repository<T> {
	protected final EntityManager em;
	protected final Class<T> cls;

	// For inheritance
	@SuppressWarnings("unchecked")
	@Inject
	public Repository(EntityManager em) {
		this.em = em;
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		this.cls = (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}

	// For instantiation
	public Repository(EntityManager em, Class<T> entityClass) {
		super();
		this.em = em;
		this.cls = entityClass;
	}

	public Optional<T> find(Object id) {
		return Optional.ofNullable(em.find(cls, id));
	}

	public void delete(Delete<T> d) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaDelete<T> delete = cb.createCriteriaDelete(cls);
		Root<T> root = delete.from(cls);
		d.build(cb, root, delete);
		em.createQuery(delete).executeUpdate();
	}

	public void update(Update<T> u) {
		CriteriaUpdate<T> update = em.getCriteriaBuilder().createCriteriaUpdate(cls);
		Root<T> root = update.from(cls);
		u.build(em.getCriteriaBuilder(), root, update);
		em.createQuery(update).executeUpdate();
	}

	@Dependent
	public static class Factory {
		final EntityManager entityManager;

		@Inject
		public Factory(EntityManager entityManager) {
			this.entityManager = entityManager;
		}

		public <T> Repository<T> of(Class<T> cls) {
			return new Repository<>(entityManager, cls);
		}

		public EntityManager em() {
			return entityManager;
		}

		public void persist(Object entity) {
			if (!entityManager.contains(entity)) {
				entityManager.persist(entity);
			}
		}

	}

	public <U> TypedQuery<U> query(Class<U> projection, Query<T, U> query) {
		CriteriaQuery<U> cq = em.getCriteriaBuilder().createQuery(projection);
		Root<T> root = cq.from(cls);
		query.build(em.getCriteriaBuilder(), root, cq);
		TypedQuery<U> tq = em.createQuery(cq);
		return tq;
	}

	public TypedQuery<T> query(Query<T, T> query) {
		return query(cls, query);
	}

	public TypedQuery<T> all() {
		return query((cb, rt, cq) -> {
		});
	}

	@FunctionalInterface
	public static interface Query<T, U> {
		void build(CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaQuery<U> criteriaQuery);
	}

	@FunctionalInterface
	public interface Update<T> {
		void build(CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaUpdate<T> criteriaUpdate);
	}

	@FunctionalInterface
	public interface Delete<T> {
		void build(CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaDelete<T> criteriaDelete);
	}

	public static <T> Optional<T> optional(TypedQuery<T> tq) {
		try {
			return Optional.ofNullable(tq.getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	/**
	 * Convenience method to infer the right equal method to call on
	 * {@link CriteriaBuilder}
	 * 
	 * @param arg0 in {@link CriteriaBuilder}.equal
	 * @param arg1 {@link Expression} or {@link Object}
	 * @return {@link Predicate}
	 */
	public static <T> Predicate inferEqualPredicate(Expression<T> arg0, ExpressionOrType<T> arg1) {
		var cb = CDI.current().select(EntityManager.class).get().getCriteriaBuilder();
		return arg1.isExpression() ? cb.equal(arg0, arg1.expression()) : cb.equal(arg0, arg1.type());
	}

	/**
	 * Convenience method to infer the right greaterThanOrEqualTo method to call on
	 * {@link CriteriaBuilder}
	 * 
	 * @param <T>  type
	 * @param arg0 in {@link CriteriaBuilder}.greaterThanOrEqualTo
	 * @param arg1 {@link Expression} or {@link Object}
	 * @return {@link Predicate}
	 */
	public static <T extends Comparable<? super T>> Predicate inferGreaterThanOrEqualToPredicate(Expression<T> arg0,
			ExpressionOrType<T> arg1) {
		var cb = CDI.current().select(EntityManager.class).get().getCriteriaBuilder();
		return Expression.class.isAssignableFrom(arg1.getClass()) ? cb.greaterThanOrEqualTo(arg0, arg1.expression())
				: cb.greaterThanOrEqualTo(arg0, arg1.type());
	}

	/**
	 * Convenience method to infer the right lessThanOrEqualTo method to call on
	 * {@link CriteriaBuilder}
	 * 
	 * @param <T>  type
	 * @param arg0 in {@link CriteriaBuilder}.lessThanOrEqualTo
	 * @param arg1 {@link Expression} or {@link Object}
	 * @return {@link Predicate}
	 */
	public static <T extends Comparable<? super T>> Predicate inferLessThanOrEqualToPredicate(Expression<T> arg0,
			ExpressionOrType<T> arg1) {
		var cb = CDI.current().select(EntityManager.class).get().getCriteriaBuilder();
		return Expression.class.isAssignableFrom(arg1.getClass()) ? cb.lessThanOrEqualTo(arg0, arg1.expression())
				: cb.lessThanOrEqualTo(arg0, arg1.type());
	}

	public static class ExpressionOrType<T> {
		private final Object val;

		private ExpressionOrType(@NonNull T val) {
			this.val = val;
		}

		private ExpressionOrType(@NonNull Expression<T> val) {
			this.val = val;
		}

		public boolean isExpression() {
			return Expression.class.isAssignableFrom(val.getClass());
		};

		public <U> U map(@NonNull Function<Expression<T>, U> expressionMapper, @NonNull Function<T, U> typeMapper) {
			return isExpression() ? expressionMapper.apply(expression()) : typeMapper.apply(type());
		}

		public Expression<T> expression() {
			if (!isExpression())
				throw Beef.internal().as(b -> b.when("Returning expression").detail("Value not expression")).build();
			return (Expression<T>) val;
		};

		public T type() {
			if (isExpression())
				throw Beef.internal().as(b -> b.when("Returning type").detail("Value is expression")).build();
			return (T) val;
		}

		public Object value() {
			return val;
		}

		public static <T> ExpressionOrType<T> of(T val) {
			return new ExpressionOrType<>(val);
		}

		public static <T> ExpressionOrType<T> of(Expression<T> val) {
			return new ExpressionOrType<>(val);
		}
	}
}
