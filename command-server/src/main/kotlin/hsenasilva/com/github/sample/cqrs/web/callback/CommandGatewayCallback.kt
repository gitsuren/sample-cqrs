package hsenasilva.com.github.sample.cqrs.web.callback

import hsenasilva.com.github.sample.cqrs.domain.CreateSampleCommand
import hsenasilva.com.github.sample.cqrs.domain.RequestSampleCommand
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.CommandResultMessage
import org.axonframework.commandhandling.callbacks.LoggingCallback
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.command.AggregateNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component


/**
 * @author hsena
 */
@Component
class CommandGatewayCallback(@Lazy private val commandGateway: CommandGateway) : CommandCallback<CreateSampleCommand, String> {

    override fun onResult(commandMessage: CommandMessage<out CreateSampleCommand>, commandResultMessage: CommandResultMessage<out String>) {
        val logger = LoggerFactory.getLogger(CommandGatewayCallback::class.java)

        when {
            commandResultMessage.isExceptional && commandResultMessage.exceptionResult() is AggregateNotFoundException -> {
                logger.error("Command resulted in exception: ${commandMessage.commandName}", commandResultMessage.exceptionResult())
                val createCommand = RequestSampleCommand(commandMessage.payload.id, commandMessage.payload.createSampleParameter)
                this.commandGateway.sendAndWait<Any>(createCommand)
                this.commandGateway.send(commandMessage.payload, LoggingCallback.INSTANCE)
            }
            commandResultMessage.isExceptional -> logger.warn("Command resulted in exception: ${commandMessage.commandName}", commandResultMessage.exceptionResult())
            else -> logger.info("Command executed successfully: ${commandMessage.commandName}")
        }
    }
}