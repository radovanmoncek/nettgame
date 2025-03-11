package cz.radovanmoncek.ship.injection.services;

import cz.radovanmoncek.ship.injection.annotations.AttributeInjectee;
import cz.radovanmoncek.ship.injection.exceptions.InjectionException;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import cz.radovanmoncek.ship.parents.creators.RepositoryCreator;
import cz.radovanmoncek.ship.parents.models.PersistableModel;
import cz.radovanmoncek.ship.parents.repositories.Repository;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ChannelHandlerInjectionService {
    private static final Logger logger = LogManager.getLogger(ChannelHandlerInjectionService.class);
    private final SessionFactory sessionFactory;
    private final LinkedList<ChannelHandlerCreator> channelHandlerCreators;
    private final RepositoryCreator repositoryCreator;
    private final LinkedList<PersistableModel> persistableModels;

    public ChannelHandlerInjectionService(SessionFactory sessionFactory, LinkedList<ChannelHandlerCreator> channelHandlerCreators, RepositoryCreator repositoryCreator, LinkedList<PersistableModel> persistableModels) {
        this.sessionFactory = sessionFactory;
        this.channelHandlerCreators = channelHandlerCreators;
        this.repositoryCreator = repositoryCreator;
        this.persistableModels = persistableModels;
    }

    public LinkedList<ChannelHandlerCreator> returnInjectedChannelHandlerCreators() {

        logger.warn("Starting ChannelHandler injection");

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

                                if (!field.isAnnotationPresent(AttributeInjectee.class)) {

                                    continue;
                                }

                                PersistableModel persistableModelToInject = null;

                                for (final var persistableModel : persistableModels) {

                                    if (persistableModel.getClass().equals(field.getType())) {

                                        persistableModelToInject = persistableModel;

                                        break;
                                    }
                                }

                                injectionExceptionReason.append(String.format(" %s into %s", channelHandler, field));

                                if (persistableModelToInject == null) {

                                    throw new InjectionException(injectionExceptionReason.toString());
                                }

                                Class<?> persistableModelClazz = persistableModelToInject.getClass();

                                while (persistableModelClazz.getSuperclass() != PersistableModel.class) {

                                    persistableModelClazz = persistableModelClazz.getSuperclass();
                                }

                                persistableModelClazz = persistableModelClazz.getSuperclass();

                                for (final var persistableModelField : persistableModelClazz.getDeclaredFields()) {

                                    if (!persistableModelField.isAnnotationPresent(AttributeInjectee.class) || !persistableModelField.getType().equals(Repository.class)) {

                                        continue;
                                    }

                                    final var repository = repositoryCreator.createRepository();

                                    for (final var repositoryField : repository.getClass().getDeclaredFields()) {

                                        if (!repositoryField.isAnnotationPresent(AttributeInjectee.class)) {

                                            continue;
                                        }

                                        repositoryField.setAccessible(true);

                                        if (repositoryField.getType().equals(SessionFactory.class)) {

                                            repositoryField.set(repository, sessionFactory);

                                            continue;
                                        }

                                        if (repositoryField.getType().equals(PersistableModel.class)) {

                                            repositoryField.set(repository, persistableModelToInject.getClass());
                                        }
                                    }

                                    persistableModelField.setAccessible(true);
                                    persistableModelField.set(persistableModelToInject, repository);
                                }

                                field.setAccessible(true);
                                field.set(channelHandler, persistableModelToInject);

                                logger.debug("Injecting {} into {}", channelHandler, field);

                            }

                            clazz = clazz.getSuperclass();

                        }
                        catch (final Exception exception) {

                            logger.error(exception.getMessage(), exception);

                            final var injectionException = new InjectionException(injectionExceptionReason.append(", expect incorrect operation!").toString(), exception);

                            logger.error(injectionException.getMessage(), injectionException);

                            return null;
                        }
                    }

                    return channelHandler;
                }
            };

            if(injectedChannelHandlerCreator.newProduct() == null)
                return new LinkedList<>();

            injectedChannelHandlerCreators.add(injectedChannelHandlerCreator);
        }

        return injectedChannelHandlerCreators;
    }
}
