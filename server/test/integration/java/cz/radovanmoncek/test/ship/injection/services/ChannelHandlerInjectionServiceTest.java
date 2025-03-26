package cz.radovanmoncek.test.ship.injection.services;

import com.google.flatbuffers.Table;
import cz.radovanmoncek.ship.injection.annotations.ChannelHandlerAttributeInjectee;
import cz.radovanmoncek.ship.injection.services.ChannelHandlerInjectionService;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.handlers.ChannelGroupHandler;
import cz.radovanmoncek.ship.parents.repositories.Repository;
import cz.radovanmoncek.ship.utilities.logging.LoggingUtilities;
import cz.radovanmoncek.ship.utilities.reflection.ReflectionUtilities;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.Metamodel;
import org.hibernate.*;
import org.hibernate.Cache;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.graph.RootGraph;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.relational.SchemaManager;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelHandlerInjectionServiceTest {

    @BeforeAll
    static void setup() {

        LoggingUtilities.enableGlobalLoggingLevel(Level.ALL);
    }

    @Test
    void returnInjectedChannelHandlerCreators() throws NoSuchFieldException, IllegalAccessException {

        final var channelHandlerCreators = new LinkedList<ChannelHandlerCreator>();

        channelHandlerCreators.add(new ChannelHandlerCreator() {

            @Override
            public ChannelHandler newProduct() {

                return new ChannelGroupHandler<>() {
                    @ChannelHandlerAttributeInjectee
                    private Repository<Object> repository;

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Table msg) {

                    }
                };
            }
        });

        final var repository = new Repository<>() {};
        final var sessionFactory = new SessionFactory() {

            @Override
            public String getJndiName() {
                return "";
            }

            @Override
            public SessionBuilder withOptions() {
                return null;
            }

            @Override
            public Session openSession() throws HibernateException {
                return null;
            }

            @Override
            public Session getCurrentSession() throws HibernateException {
                return null;
            }

            @Override
            public StatelessSessionBuilder withStatelessOptions() {
                return null;
            }

            @Override
            public StatelessSession openStatelessSession() {
                return null;
            }

            @Override
            public StatelessSession openStatelessSession(Connection connection) {
                return null;
            }

            @Override
            public Session createEntityManager() {
                return null;
            }

            @Override
            public Session createEntityManager(Map<?, ?> map) {
                return null;
            }

            @Override
            public Session createEntityManager(SynchronizationType synchronizationType) {
                return null;
            }

            @Override
            public Session createEntityManager(SynchronizationType synchronizationType, Map<?, ?> map) {
                return null;
            }

            @Override
            public Statistics getStatistics() {
                return null;
            }

            @Override
            public SchemaManager getSchemaManager() {
                return null;
            }

            @Override
            public HibernateCriteriaBuilder getCriteriaBuilder() {
                return null;
            }

            @Override
            public void close() throws HibernateException {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public Cache getCache() {
                return null;
            }

            @Override
            public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) {
                return List.of();
            }

            @Override
            public RootGraph<?> findEntityGraphByName(String name) {
                return null;
            }

            @Override
            public RootGraph<Map<String, ?>> createGraphForDynamicEntity(String entityName) {
                return null;
            }

            @Override
            public Set<String> getDefinedFilterNames() {
                return Set.of();
            }

            @Override
            public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
                return null;
            }

            @Override
            public Set<String> getDefinedFetchProfileNames() {
                return Set.of();
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public SessionFactoryOptions getSessionFactoryOptions() {
                return null;
            }

            @Override
            public Metamodel getMetamodel() {
                return null;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public Map<String, Object> getProperties() {
                return Map.of();
            }

            @Override
            public PersistenceUnitUtil getPersistenceUnitUtil() {
                return null;
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return null;
            }

            @Override
            public void addNamedQuery(String name, Query query) {

            }

            @Override
            public <T> T unwrap(Class<T> cls) {
                return null;
            }

            @Override
            public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {

            }

            @Override
            public <R> Map<String, TypedQueryReference<R>> getNamedQueries(Class<R> resultType) {
                return Map.of();
            }

            @Override
            public <E> Map<String, EntityGraph<? extends E>> getNamedEntityGraphs(Class<E> entityType) {
                return Map.of();
            }

            @Override
            public void runInTransaction(Consumer<EntityManager> work) {

            }

            @Override
            public <R> R callInTransaction(Function<EntityManager, R> work) {
                return null;
            }

            @Override
            public Reference getReference() {
                return null;
            }
        };

        final var injectedChannelHandlerCreators = new ChannelHandlerInjectionService(sessionFactory, new LinkedList<>(List.of(repository)))
                .returnInjectedChannelHandlerCreators(channelHandlerCreators);

        assertEquals(repository, ReflectionUtilities.returnValueOnFieldReflectively(injectedChannelHandlerCreators.getFirst().newProduct(), "repository"));
        assertEquals(sessionFactory, ReflectionUtilities.returnValueOnFieldReflectively(ReflectionUtilities.returnValueOnFieldReflectively(injectedChannelHandlerCreators.getFirst().newProduct(), "repository"), "sessionFactory"));
    }
}
