package org.judahdonkor.chassis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

public interface Hierarchical<T> {
    T rootNode();

    Optional<T> findNode(Predicate<T> predicate);

    default boolean hasNode(Predicate<T> predicate) {
        return findNode(predicate).isPresent();
    }

    public static <T> Hierarchical<T> traverse(T entity, UnaryOperator<T> parent) {
        return new Hierarchical<T>() {

            @Override
            public T rootNode() {
                return findNode(node -> parent.apply(node) == null).get();
            }

            @Override
            public Optional<T> findNode(Predicate<T> predicate) {
                T node = entity;
                do {
                    if (predicate.test(node))
                        return Optional.of(node);
                    node = parent.apply(node);
                } while (node != null);
                return Optional.empty();
            }

        };
    }

    public static class Repository<T> {
        protected final org.judahdonkor.chassis.Repository<T> repos;
        protected final Function<Root<T>, Path<?>> parent;

        public Repository(org.judahdonkor.chassis.Repository.Factory reposFcty, Class<T> cls,
                Function<Root<T>, Path<?>> parent) {
            super();
            this.repos = reposFcty.of(cls);
            this.parent = parent;
        }

        public Repository(org.judahdonkor.chassis.Repository.Factory reposFcty, Class<T> cls) {
            this(reposFcty, cls, rt -> rt.get("parent"));
        }

        public List<T> children(T entity) {
            return repos.query((cb, rt, cq) -> cq.where(cb.equal(parent.apply(rt), entity))).getResultList();
        }

        public List<T> descendants(T entity) {
            var desc = new ArrayList<T>();
            List<T> children = new ArrayList<>(children(entity));
            do {
                desc.addAll(children);
                var grandChildren = repos.query((cb, rt, cq) -> cq.where(parent.apply(rt).in(children)))
                        .getResultList();
                children.clear();
                children.addAll(grandChildren);
            } while (!children.isEmpty());
            return desc;
        }

        @Dependent
        public static class Factory {
            @Inject
            org.judahdonkor.chassis.Repository.Factory reposFcty;

            public <T> Repository<T> of(Class<T> cls) {
                return new Repository<>(reposFcty, cls);
            }

            public <T> Repository<T> of(Class<T> cls, Function<Root<T>, Path<?>> parent) {
                return new Repository<>(reposFcty, cls, parent);
            }

        }
    }
}
