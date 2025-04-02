package cz.radovanmoncek.ship.engine.injection.services;

import cz.radovanmoncek.ship.engine.injection.annotations.ChannelHandlerAttributeInjectee;
import cz.radovanmoncek.ship.engine.injection.exceptions.InjectionException;
import cz.radovanmoncek.ship.bay.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.bay.repositories.Repository;
import cz.radovanmoncek.ship.bay.utilities.reflection.ReflectionUtilities;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import org.hibernate.SessionFactory;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelHandlerInjectionService {
    private static final Logger logger = Logger.getLogger(ChannelHandlerInjectionService.class.getName());
    private final SessionFactory sessionFactory;
    private final LinkedList<Repository<?>> repositories;

    public ChannelHandlerInjectionService(SessionFactory sessionFactory, LinkedList<Repository<?>> repositories) {

        this.sessionFactory = sessionFactory;
        this.repositories = repositories;
    }

    public LinkedList<ChannelHandlerCreator> returnInjectedChannelHandlerCreators(final LinkedList<ChannelHandlerCreator> channelHandlerCreators) {

        logger.warning("Starting ChannelHandler injection");

        final var injectedChannelHandlerCreators = new LinkedList<ChannelHandlerCreator>();

        for (final var channelHandlerCreator : channelHandlerCreators) {

            final var injectedChannelHandlerCreator = new ChannelHandlerCreator() {

                @Override
                public ChannelHandler newProduct() {

                    final var channelHandler = channelHandlerCreator.newProduct();

                    Class<?> clazz = channelHandler.getClass();

                    while (clazz.getSuperclass() != ChannelHandlerAdapter.class) {

                        final var declaredFields = clazz.getDeclaredFields();
                        final StringBuilder injectionExceptionReason = new StringBuilder("Failed to inject");

                        try {

                            for (final var field : declaredFields) {

                                if (!field.isAnnotationPresent(ChannelHandlerAttributeInjectee.class)) {

                                    continue;
                                }

                                Repository<?> repositoryToInject = null;

                                for (final var repository : repositories) {

                                    if (ReflectionUtilities.findActualClass(repository).equals(field.getType())) {

                                        repositoryToInject = repository;

                                        break;
                                    }
                                }

                                injectionExceptionReason.append(String.format(" %s into %s", channelHandler, field));

                                if (repositoryToInject == null) {

                                    throw new InjectionException(injectionExceptionReason.toString());
                                }

                                Class<?> repositoryToInjectClass = repositoryToInject.getClass();

                                while (repositoryToInjectClass.getSuperclass() != Repository.class) {

                                    repositoryToInjectClass = repositoryToInjectClass.getSuperclass();
                                }

                                repositoryToInjectClass = repositoryToInjectClass.getSuperclass();

                                for (final var repositoryField : repositoryToInjectClass.getDeclaredFields()) {

                                    if (!repositoryField.isAnnotationPresent(ChannelHandlerAttributeInjectee.class)) {

                                        continue;
                                    }

                                    repositoryField.setAccessible(true);

                                    if (repositoryField.getType().equals(SessionFactory.class)) {

                                        repositoryField.set(repositoryToInject, sessionFactory);
                                    }
                                }

                                field.setAccessible(true);
                                field.set(channelHandler, repositoryToInject);

                                logger.log(Level.FINEST, "Injecting {0} into {1}", new Object[]{channelHandler.getClass().getName(), field.getName()});
                            }

                            clazz = clazz.getSuperclass();
                        } catch (final Exception exception) {

                            logger.throwing(getClass().getName(), "returnInjectedChannelHandlerCreators", exception);

                            final var injectionException = new InjectionException(injectionExceptionReason.append(", expect incorrect operation!").toString(), exception);

                            logger.throwing(getClass().getName(), "returnInjectedChannelHandlerCreators", injectionException);

                            return null;
                        }
                    }

                    return channelHandler;
                }
            };

            if (injectedChannelHandlerCreator.newProduct() == null) {

                logger.warning("Nothing injected, returning empty list");

                return new LinkedList<>();
            }

            injectedChannelHandlerCreators.add(injectedChannelHandlerCreator);
        }

        logger.info("Successfully injected ChannelHandlers");

        return injectedChannelHandlerCreators;
    }
}
