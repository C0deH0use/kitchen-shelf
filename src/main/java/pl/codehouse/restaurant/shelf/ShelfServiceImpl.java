package pl.codehouse.restaurant.shelf;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.codehouse.commons.ActionEvent;
import pl.codehouse.commons.ApplicableCommand;
import pl.codehouse.commons.ExecutionResult;
import reactor.core.publisher.Mono;

@Service
class ShelfServiceImpl implements ShelfService {
    private static final Logger log = LoggerFactory.getLogger(ShelfServiceImpl.class);

    private final List<ApplicableCommand<ActionEvent, ShelfDto>> shelfCommands;

    ShelfServiceImpl(List<ApplicableCommand<ActionEvent, ShelfDto>> shelfCommands) {
        this.shelfCommands = shelfCommands;
    }

    @Override
    @Transactional
    public Mono<ShelfDto> action(ActionEvent action) {
        log.info("Trying to executing command applicable for action: {} ...", action.getClass().getSimpleName());
        return shelfCommands.stream()
                .filter(command -> command.isApplicable(action))
                .findFirst()
                .map(command -> command.execute(action).map(ExecutionResult::handle))
                .orElseThrow(() -> new IllegalArgumentException("Missing configuration for the following action: " + action));
    }
}
