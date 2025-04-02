package cz.radovanmoncek.ship.engine.injection.annotations;

import cz.radovanmoncek.ship.bay.repositories.Repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Serves for attribute injection of dependencies (namely {@link Repository}) into
 * a given {@link io.netty.channel.ChannelHandler}.
 * <p>
 *     In other words: automatic <i>attribute injection</i> will be performed for each field
 *     in a {@link io.netty.channel.ChannelHandler} annotated with {@link ChannelHandlerAttributeInjectee}.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ChannelHandlerAttributeInjectee {
}
