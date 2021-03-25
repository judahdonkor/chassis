package org.judahdonkor.chassis;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

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
	 * @param <T>
	 * @param type
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static <T> Predicate inferEqualPredicate(Class<T> type, Expression<T> arg0, Object arg1) {
		var cb = CDI.current().select(EntityManager.class).get().getCriteriaBuilder();
		return type.isAssignableFrom(arg1.getClass()) ? cb.equal(arg0, (Expression<T>) arg1) : cb.equal(arg0, arg1);
	}
}
